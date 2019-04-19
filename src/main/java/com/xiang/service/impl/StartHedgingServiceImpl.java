package com.xiang.service.impl;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeoutException;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.okex.websocket.OkexConstant;
import com.okex.websocket.WebSocketService;
import com.xiang.service.FutureAccountService;
import com.xiang.service.Instrument;
import com.xiang.service.InstrumentService;
import com.xiang.service.InstrumentsDepthService;
import com.xiang.service.TradeApiService;

/**
 * @author xiang
 * @createDate 2018年12月27日 上午9:46:53
 */
@Service("startHedgingService")
public class StartHedgingServiceImpl implements WebSocketService {
	@Resource(name = "instrumentsDepthService")
	InstrumentsDepthService instrumentsDepthService;
	@Resource(name = "instrumentService")
	InstrumentService instrumentService;
	@Autowired
	TradeApiService tradeApiService;
	@Resource(name = "futureAccountService")
	private FutureAccountService futureAccountService;
	@Autowired
	CoinServiceImpl coinService;
	HedgingConfigManager hedgingConfigManager = HedgingConfigManager.getInstance();
	HedgingClient hedgingClient;

	@PostConstruct
	public void init() {
		hedgingClient = new HedgingClient(tradeApiService);
	}

	private void execute(String table, String instrumentId) {
		hedgingClient.start();
		Instrument instrument = instrumentService.getInstrument(table, instrumentId);
		if (instrument != null) {
			String coin = instrument.getCoin();
			Instrument thisInstrument = instrumentService.getInstrument(OkexConstant.FUTURES_DEPTH, coin, "this_week");
			// 已过期
			if (thisInstrument != null && thisInstrument.getDeliveryTime() < System.currentTimeMillis()) {
				thisInstrument = null;
			}
			Instrument nextInstrument = instrumentService.getInstrument(OkexConstant.FUTURES_DEPTH, coin, "next_week");
			// 已过期
			if (nextInstrument != null && nextInstrument.getDeliveryTime() < System.currentTimeMillis()) {
				nextInstrument = null;
			}
			Instrument quarterInstrument = instrumentService.getInstrument(OkexConstant.FUTURES_DEPTH, coin, "quarter");
			// 已过期
			if (quarterInstrument != null && quarterInstrument.getDeliveryTime() < System.currentTimeMillis()) {
				quarterInstrument = null;
			}
			Instrument spotInstrument = instrumentService.getInstrument(OkexConstant.SPOT_DEPTH, coin, "USDT");
			switch (instrument.getContractType()) {
			case "this":
				// spot,this_week
				execute(hedgingConfigManager.getConfigs(coin, "tt"), spotInstrument, thisInstrument);
				break;
			case "this_week":
				// spot,this_week
				execute(hedgingConfigManager.getConfigs(coin, "tt"), spotInstrument, thisInstrument);
				// this_week,next_week
				execute(hedgingConfigManager.getConfigs(coin, "tn"), thisInstrument, nextInstrument);
				// this_week,quarter
				execute(hedgingConfigManager.getConfigs(coin, "tq"), thisInstrument, quarterInstrument);
				break;
			case "next_week":
				execute(hedgingConfigManager.getConfigs(coin, "tn"), thisInstrument, nextInstrument);
				// next_week,quarter
				execute(hedgingConfigManager.getConfigs(coin, "nq"), nextInstrument, quarterInstrument);
				break;
			case "quarter":
				execute(hedgingConfigManager.getConfigs(coin, "tq"), thisInstrument, quarterInstrument);
				execute(hedgingConfigManager.getConfigs(coin, "nq"), nextInstrument, quarterInstrument);
				break;
			default:
				;
			}
		}
		try {
			hedgingClient.finish();
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void execute(List<HedgingConfig> configs, Instrument thisInstrument, Instrument nextInstrument) {
		if (configs != null && thisInstrument != null && nextInstrument != null
				&& !thisInstrument.getInstrumentId().equals(nextInstrument.getInstrumentId())
				&& thisInstrument.getDeliveryTime() != nextInstrument.getDeliveryTime()) {
			// 从大到小排列，优先提交高指数的策略
			configs.sort(new Comparator<HedgingConfig>() {
				@Override
				public int compare(HedgingConfig o1, HedgingConfig o2) {
					// TODO Auto-generated method stub
					return Float.compare(o2.getSellBuyThresholdRate(), o1.getSellBuyThresholdRate());
				}
			});
			for (HedgingConfig config : configs) {
//				手动近卖远买
				execute(config, thisInstrument, nextInstrument, config.getSellBuyThresholdRate());
			}
			// 从大到小排列，优先提交高指数的策略
			configs.sort(new Comparator<HedgingConfig>() {
				@Override
				public int compare(HedgingConfig o1, HedgingConfig o2) {
					// TODO Auto-generated method stub
					return Float.compare(o2.getBuySellThresholdRate(), o1.getBuySellThresholdRate());
				}
			});
			for (HedgingConfig config : configs) {
//				手动近买远卖
				execute(config, nextInstrument, thisInstrument, config.getBuySellThresholdRate());
			}
		}
	}

	private void execute(HedgingConfig config, Instrument preInstrument, Instrument lastInstrument,
			float thresholdRate) {
		if (config.isStart() && VolumeManager.getInstance().getVolume(config) > 0) {
			if (!isInHegingHour(config, preInstrument, lastInstrument)) {
				return;
			}
			Level2Bean level2Buy = instrumentsDepthService.getBuyLevel2Postion(preInstrument.getInstrumentId(),
					config.getBuyLevel());
			Level2Bean level2Sell = instrumentsDepthService.getSellLevel2Postion(lastInstrument.getInstrumentId(),
					config.getSellLevel());
			if (openHedging(config, level2Buy, level2Sell, thresholdRate)) {
				
				Hedging hedging = hedgingTrade(level2Buy, level2Sell, config, config.getStartPremiumRate());
				if (hedging != null) {
					addHedging(hedging);
				}
				
			}

		}
	}

	private void addHedging(Hedging hedging) {
		hedgingClient.addHedging(hedging);
		HedgingManager.getInstance().addHedging(hedging);
	}

	/**
	 * 提供对冲开仓平仓服务
	 * 
	 * @param level2Buy   当前市场委托买价
	 * @param level2Sell  当前市场委托卖价
	 * @param config      对冲策略配置
	 * @param premiumRate 溢价开平仓率
	 * @return
	 */
	private Hedging hedgingTrade(Level2Bean level2Buy, Level2Bean level2Sell, HedgingConfig config, float premiumRate) {
		Hedging hedging = null;
		// 计算可以交易合约张数，必须是买卖挂单最小值，同时小于最大单次下单合约数，小于可交易合约数
		int volume = Math.min(level2Buy.getIntVolume(), level2Sell.getIntVolume()) - hedgingClient.getUsedVolume();
		// 对冲后委托价上必须剩余这么多合约张数，防止对冲失败
		int levelVolume = (int) (config.getStartThresholdAmount() / coinService.getUnitAmount(config.getCoin()));
		volume = volume - levelVolume;

		if (config.getMaxTradeVolume() > 0) {
			volume = Math.min(config.getMaxTradeVolume(), volume);
		}
		// 检查保证金是否足够
		float price = getAvailablePrice(level2Buy, level2Sell, premiumRate);
	
		double availableMargin = futureAccountService.getAvailableMargin(config.getCoin());
		System.out.println("availableMargin " + availableMargin+"  getUsedMargin  "+hedgingClient.getUsedMargin());
		availableMargin=availableMargin- hedgingClient.getUsedMargin();// 可用保证金
		int availableVolume = futureAccountService.getAvailableVolume(config.getCoin(), availableMargin, price,
				config.getLeverRate());
		System.out.println("availableVolume " + availableVolume);
		volume = Math.min(volume, availableVolume / 2);

		// 检查库存
		// 限制合约张数，0为不限制
		if (config.getVolume() > 0) { 
			int leftVolume = VolumeManager.getInstance().getVolume(config);
			if (leftVolume > 0) {
				volume = Math.min(leftVolume, volume);
			}

			// 减掉库存
			volume = VolumeManager.getInstance().getSetVolume(config, volume);
		}
		System.out.println("can hedging volume " + volume);
		if (volume > 0) {
			hedging = hedgingTrade(level2Buy, level2Sell, volume, volume, "1", config, premiumRate);
			hedging.setAmount(volume);
		}
		return hedging;
	}

	private float getAvailablePrice(Level2Bean level2Buy, Level2Bean level2Sell, float premiumRate) {
		return level2Sell.getFloatPrice() * (1 + premiumRate / 100f) / 2f
				+ level2Buy.getFloatPrice() * (1 - premiumRate / 100f) / 2f;
	}

	private Hedging hedgingTrade(Level2Bean level2Buy, Level2Bean level2Sell, int buyVolume, int sellVolume,
			String type, HedgingConfig config, float premiumRate) {
		HedgingTrade buyTrade = new HedgingTrade();
		if (buyVolume > 0 && level2Sell != null) {
			buyTrade.setLeverRate(config.getLeverRate());
			buyTrade.setInstrumentId(level2Sell.getInstrumentId());
			Instrument futureInstrument = instrumentService.getInstrument(buyTrade.getInstrumentId());
			buyTrade.setDeliveryTime(futureInstrument.getDeliveryTime());
			buyTrade.setPrice(level2Sell.getFloatPrice() * (1 + premiumRate / 100f));
			buyTrade.setAmount(buyVolume);
			if ("3".equals(type)) {
				buyTrade.setType("4");
			} else {
				buyTrade.setType(type);
			}

		}
		HedgingTrade sellTrade = new HedgingTrade();
		if (sellVolume > 0 && level2Buy != null) {
			sellTrade.setLeverRate(config.getLeverRate());
			sellTrade.setInstrumentId(level2Buy.getInstrumentId());
			Instrument futureInstrument = instrumentService.getInstrument(sellTrade.getInstrumentId());
			sellTrade.setDeliveryTime(futureInstrument.getDeliveryTime());
			sellTrade.setPrice(level2Buy.getFloatPrice() * (1 - premiumRate / 100f));
			sellTrade.setAmount(sellVolume);
			if ("1".equals(type)) {
				sellTrade.setType("2");
			} else {
				sellTrade.setType(type);
			}
		}
		Hedging hedging = new Hedging(config);
		hedging.setBuyTrade(buyTrade);
		hedging.setSellTrade(sellTrade);
		return hedging;
	}

	/**
	 * 判断是否符合开仓条件
	 * 
	 * @param config
	 * @param level2Buy
	 * @param level2Sell
	 * @param thresholdRate
	 * @return
	 */
	private boolean openHedging(HedgingConfig config, Level2Bean level2Buy, Level2Bean level2Sell,
			float thresholdRate) {
		// 页面设置的是%比的设置，必须化成正常非%之后去计算
		thresholdRate = thresholdRate / 100f;
		if (level2Buy != null && level2Sell != null) {
			float value = (level2Buy.getFloatPrice() - level2Sell.getFloatPrice()) / level2Sell.getFloatPrice();
			if (config.isStart() && VolumeManager.getInstance().getVolume(config) > 0) {
				if (value >= thresholdRate) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 是否在允许的时间范围内开仓，距离交割日前多少小时可以开仓，交割的时候，16时12分前不进行任何对冲套利
	 * 
	 * @param config
	 * @return
	 */
	private boolean isInHegingHour(HedgingConfig config, Instrument preInstrument,
			Instrument lastInstrument) {
		LocalDateTime localDateTime = LocalDateTime.now();
		// 交割的时候，16时12分前不进行任何对冲套利
		if (localDateTime.getDayOfWeek() == DayOfWeek.FRIDAY) {
			int hour = localDateTime.getHour();
			int minute = localDateTime.getMinute();
			if (hour == 16 && minute < 12) {
				return false;
			}
		}
		// 开仓时间限制
		if (config.getLastHegingHour() == 0) {
			return true;
		}
		long time = System.currentTimeMillis() + config.getLastHegingHour() * 3600000;
		return time < preInstrument.getDeliveryTime() && time < lastInstrument.getDeliveryTime();
	}

	@Override
	public void onReceive(Object obj) {
		if (obj instanceof JSONObject) {
			JSONObject root = (JSONObject) obj;
			if (root.containsKey(OkexConstant.TABLE)) {
				String table = root.getString(OkexConstant.TABLE);
				if (OkexConstant.FUTURES_DEPTH.equals(table) || OkexConstant.FUTURES_DEPTH5.equals(table)) {
					// String action = root.getString("action");
					if (root.containsKey(OkexConstant.DATA)) {
						JSONArray data = root.getJSONArray(OkexConstant.DATA);
						Iterator it = data.iterator();
						while (it.hasNext()) {
							Object instrument = it.next();
							if (instrument instanceof JSONObject) {
								JSONObject instrumentJSON = (JSONObject) instrument;
								String instrumentId = instrumentJSON.getString(OkexConstant.INSTRUMENT_ID);
								execute(table, instrumentId);
							}
						}
					}
				}
			}
		}
	}
}
