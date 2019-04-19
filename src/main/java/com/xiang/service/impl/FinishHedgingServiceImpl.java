package com.xiang.service.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.google.common.base.Strings;
import com.okex.websocket.FutureOrder;
import com.xiang.service.InstrumentsDepthService;
import com.xiang.service.TradeApiService;

/**
 * @author xiang
 * @createDate 2018年12月27日 上午9:46:53
 */
@Service("finishHedgingService")
@EnableScheduling
public class FinishHedgingServiceImpl {
	@Resource(name = "instrumentsDepthService")
	InstrumentsDepthService instrumentsDepthService;
	@Resource(name = "tradeApiService")
	TradeApiService tradeApiService;
	@Autowired
	AccountConfig accountConfig;
	@Autowired
	CoinServiceImpl coinService;
	HedgingManager hedgingManager = HedgingManager.getInstance();
	TradeManager tradeManager = TradeManager.getInstance();
	HedgingClient hedgingClient;
	HedgingTradeClient cancelClient;

	@PostConstruct
	private void init() {
		hedgingClient = new HedgingClient(tradeApiService);
		cancelClient = new HedgingTradeClient(tradeApiService);
	}

	@Scheduled(fixedDelay = 200)
	private void execute() {
		hedgingClient.start();
		cancelClient.start();
		for (Hedging hedging : hedgingManager.getHedgings()) {
			if (hedging.getStatus() != 1) {// 未完成
				execute(hedging);
			}
		}
		try {
			cancelClient.cancelFinish();
			hedgingClient.finish();
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void cancelHedgings(List<Hedging> hedgings) {
		if (hedgings != null) {
			for (Hedging hedging : hedgings) {
				cancelHedging(hedging);
			}
		}
	}

	private void cancelHedging(Hedging hedging) {
		if (hedging != null) {
			cancelHedgingTrade(hedging.getBuyTrade());
			cancelHedgingTrade(hedging.getSellTrade());

		}
	}

	private void cancelHedgingTrade(HedgingTrade trade) {
		if (trade != null && trade.getStatus() == 2) {
			if (trade.getAmount() > 0 && trade.getFutureOrder() == null) {
				try {
					tradeApiService.repairHedgingTrade(trade);
				} catch (TimeoutException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (trade.getAmount() > 0 && trade.getFutureOrder() != null) {
				FutureOrder futureOrder = trade.getFutureOrder();
				if (isCanCancel(futureOrder)) {// 状态必须满足撤单条件
					cancelClient.addHedgingTrade(trade);
				}
			}
		}
	}

	private double computeFee(Hedging hedging) {
		return computeFee(hedging.getBuyTrade(), hedging.getHedgingConfig(), accountConfig.getBuyFeeRate())
				+ computeFee(hedging.getSellTrade(), hedging.getHedgingConfig(), accountConfig.getSellFeeRate());
	}

	private double computeFee(HedgingTrade trade, HedgingConfig config, float feeRate) {
		if (trade != null) {
			int unitAmount = coinService.getUnitAmount(config.getCoin());
			switch (trade.getStatus()) {
			case 0:
			case 1:
				return computeFee(trade.getPrice(), trade.getAmount(), unitAmount, feeRate);
			case 2:
				FutureOrder futureOrder = trade.getFutureOrder();
				if (futureOrder == null) {
					return computeFee(trade.getPrice(), trade.getAmount(), unitAmount, feeRate);
				}
				return futureOrder.getFee();
			case 3:
				return 0d;
			}
		}
		return 0d;
	}

	private double computeFee(float price, int amount, int unitAmount, float feeRate) {
		if (price == 0)
			return 0;
		return -(amount * unitAmount * feeRate) / (double) price;
	}

	private double computeProfit(Hedging hedging) {
		return computeCoin(hedging.getBuyTrade(), hedging.getHedgingConfig())
				- computeCoin(hedging.getSellTrade(), hedging.getHedgingConfig());
	}

	private double computeCost(Hedging hedging) {
		return computeCoin(hedging.getBuyTrade(), hedging.getHedgingConfig())
				+ computeCoin(hedging.getSellTrade(), hedging.getHedgingConfig()) + Math.abs(computeFee(hedging));
	}

	private double computeCoin(HedgingTrade trade, HedgingConfig config) {
		if (trade != null) {
			int unitAmount = coinService.getUnitAmount(config.getCoin());
			switch (trade.getStatus()) {
			case 0:
			case 1:
				return computeCoin(trade.getPrice(), trade.getAmount(), unitAmount);
			case 2:
				FutureOrder futureOrder = trade.getFutureOrder();
				if (futureOrder == null) {
					return computeCoin(trade.getPrice(), trade.getAmount(), unitAmount);
				}
				return computeCoin(futureOrder.getPriceAvg(), futureOrder.getFilledQty(), futureOrder.getContractVal());
			case 3:
				return 0d;
			}
		}
		return 0d;
	}

	private double computeCoin(float price, int amount, int unitAmount) {
		if (price == 0)
			return 0;
		return (amount * unitAmount) / (double) price;
	}

	private boolean isFinish(Hedging hedging) {
		switch (hedging.getStatus()) {
		case 0:
		case 2:
			return isFinish(hedging.getBuyTrade()) && isFinish(hedging.getSellTrade());
		case 1:
			return true;
		}
		return false;
	}

	private boolean isFinish(HedgingTrade trade) {
		if (trade != null) {
			switch (trade.getStatus()) {
			case 0:
			case 1:
				return false;
			case 2:
				return isFinish(trade.getFutureOrder());
			case 3:
				return true;
			}
		}
		return true;
	}

	private boolean isFinish(FutureOrder futureOrder) {
		if (futureOrder != null) {
			switch (futureOrder.getStatus()) {
			case 0:
			case 1:
				return false;
			case 2:
			case -1:
				return true;
			}
		}
		return false;
	}

	private boolean isDeliveryTimeout(Hedging hedging) {
		long timeout = System.currentTimeMillis() + 120000;// 2分钟
		if (hedging.getBuyTrade() != null) {
			if (hedging.getBuyTrade().getDeliveryTime() <= timeout)
				return true;
		}
		if (hedging.getSellTrade() != null) {
			if (hedging.getSellTrade().getDeliveryTime() <= timeout)
				return true;
		}
		return false;
	}

	private void fixStatus(Hedging hedging) {
		if (hedging != null) {
			fixStatus(hedging.getBuyTrade());
			fixStatus(hedging.getSellTrade());
		}
		if (!ObjectUtils.isEmpty(hedging.getReverseHedgings())) {
			for (Hedging reverseHedging : hedging.getReverseHedgings()) {
				fixStatus(reverseHedging);
			}
		}
	}

	private void fixStatus(HedgingTrade trade) {
		if (trade != null) {
			switch (trade.getStatus()) {
			case 0:
			case 1:
				if (System.currentTimeMillis() - trade.getAddTime() > 30000) {
					trade.setStatus(3);
					return;
				}
			}
		}
	}

	private void execute(Hedging hedging) {
		tradeManager.correctOrderId(hedging);
		fixStatus(hedging);
		LinkedList<Hedging> reverseHedgings = hedging.getReverseHedgings();
		int[] reverseAmounts = getReverseAmount(reverseHedgings);// 0是买入委托总量，1买入成交总量，2卖出委托总量，3卖出成交总量
		int[] amounts = getAmount(hedging);// 0是买入委托总量，1买入成交总量，2卖出委托总量，3卖出成交总量
		if (amounts[1] == reverseAmounts[3] && amounts[3] == reverseAmounts[1]) {// 实际成交量，开仓买入等于平仓卖出，开仓卖出等于平仓买入
			if (isFinish(hedging)) {// 是否还有未完成交易的订单
				// 已完成结束所有交易
				hedging.setProfitRate(getProfitRate(hedging, null));
				hedging.setStatus(1);
				VolumeManager.getInstance().releaseVolume(hedging.getHedgingConfig(), hedging.getAmount());
				return;
			}
		}
		Level2Bean[] level2 = currentHedgingLevel2(hedging);// 0 level2Buy 当前买一价挂单信息，卖出价,1 level2Sell 当前卖一价挂单信息，买入价
		Level2Bean level2Buy = level2[0];
		Level2Bean level2Sell = level2[1];
		if (hedging.getHedgingConfig().getMaxHedgingHour() > 0) {
			// 超过规定的时间内就强平
			long time = System.currentTimeMillis() - hedging.getTime();
			if (time > (hedging.getHedgingConfig().getMaxHedgingHour() * 3600000)) {
				hedging.setLiquid(true);
			}
		}
		// 是否周五前2分钟清仓
		if (isDeliveryTimeout(hedging)) {
			System.out.println(hedging.getHedgingId() + " delivery timeout");
			hedging.setLiquid(true);
		}
		if (hedging.isLiquid()) {
			cancelHedging(hedging);
			cancelHedgings(reverseHedgings);
			executeLiquid(hedging, reverseAmounts, level2);
		} else {
			Hedging virtualReverse = new Hedging(hedging.getHedgingConfig());// 使用一个虚拟平仓，来计算收益
			virtualReverse.setStatus(0);// 未完成
			int leftBuyDealAmount = amounts[1] - reverseAmounts[2];// 平仓卖出
			if (leftBuyDealAmount > 0) {
				HedgingTrade sellTrade = new HedgingTrade();
				sellTrade.setStatus(0);
				sellTrade.setAmount(leftBuyDealAmount);
				sellTrade.setPrice(level2Buy.getFloatPrice());
				virtualReverse.setSellTrade(sellTrade);
			}
			int leftSellDealAmount = amounts[3] - reverseAmounts[0];// 平仓买入
			if (leftSellDealAmount > 0) {
				HedgingTrade buyTrade = new HedgingTrade();
				buyTrade.setStatus(0);
				buyTrade.setAmount(leftSellDealAmount);
				buyTrade.setPrice(level2Sell.getFloatPrice());
				virtualReverse.setBuyTrade(buyTrade);
			}
			hedging.setProfitRate(getProfitRate(hedging, virtualReverse));
			if (hedging.getProfitRate() >= hedging.getHedgingConfig().getProfitRate()) {
				// 如果买入和卖出不对等，需要分开2组平仓来处理，第1组提交对等的，第2组提交剩余的
				int volume = Math.min(level2Buy.getIntVolume(), level2Sell.getIntVolume())
						- hedgingClient.getUsedVolume();// 可使用平仓量
				// 对冲后委托价上必须剩余这么多合约张数，防止对冲失败
				int levelVolume = (int) (hedging.getHedgingConfig().getFinishThresholdAmount()
						/ coinService.getUnitAmount(hedging.getHedgingConfig().getCoin()));
				volume = volume - levelVolume;
				volume = Math.min(volume, leftBuyDealAmount);// 对等平仓量
				volume = Math.min(volume, leftSellDealAmount);// 对等平仓量
				if (volume < 0)
					volume = 0;
				if (volume > 0) {
					Hedging reverseHedging = hedgingTrade(level2Buy, level2Sell, volume, volume, "3",
							hedging.getHedgingConfig(), hedging.getHedgingConfig().getFinishPremiumRate());
					if (reverseHedging != null) {
						hedging.addReverseHedging(reverseHedging);
						hedgingClient.addHedging(reverseHedging);
					}
				}
				leftBuyDealAmount = leftBuyDealAmount - volume;// 剩余量
				leftSellDealAmount = leftSellDealAmount - volume;// 剩余量

				// 处理差量
				if (leftBuyDealAmount > leftSellDealAmount) {
					leftBuyDealAmount = leftBuyDealAmount - leftSellDealAmount;// 差量，不对等剩余量
					leftSellDealAmount = 0;
					volume = level2Buy.getIntVolume() - hedgingClient.getUsedSellVolume();
					volume = volume - levelVolume;
					leftBuyDealAmount = Math.min(leftBuyDealAmount, volume);
				} else {
					leftBuyDealAmount = 0;
					leftSellDealAmount = leftSellDealAmount - leftBuyDealAmount;// 差量，不对等剩余量
					volume = level2Sell.getIntVolume() - hedgingClient.getUsedBuyVolume();
					volume = volume - levelVolume;
					leftSellDealAmount = Math.min(leftSellDealAmount, volume);
				}

				if (leftSellDealAmount > 0 || leftBuyDealAmount > 0) {
					Hedging reverseHedging = hedgingTrade(level2Buy, level2Sell, leftSellDealAmount, leftBuyDealAmount,
							"3", hedging.getHedgingConfig(), hedging.getHedgingConfig().getFinishPremiumRate());
					if (reverseHedging != null) {
						hedging.addReverseHedging(reverseHedging);
						hedgingClient.addHedging(reverseHedging);
					}
				}
			}
		}
	}

	/**
	 * 
	 * @param hedging
	 * @param virtualReverse 虚拟的平仓交易，当未完成平仓时，以当前价格虚拟一个平仓交易来计算盈亏率
	 * @return
	 */
	private float getProfitRate(Hedging hedging, Hedging virtualReverse) {
		double profitCoin = getProfitCoin(hedging);// 收益 单位（币）
		for (Hedging reverse : hedging.getReverseHedgings()) {
			profitCoin += getProfitCoin(reverse);
		}
		if (virtualReverse != null) {
			profitCoin += getProfitCoin(virtualReverse);
		}
		double costCoin = computeCost(hedging);// 成本 单位（币） 用来计算盈亏率 使用币成长或者减少百分比
		double rate = profitCoin * 100d / costCoin;// 百分比
		return (float) rate * hedging.getHedgingConfig().getLeverRate();
	}

	private boolean isCanCancel(FutureOrder futureOrder) {
		return futureOrder.getSize() > futureOrder.getFilledQty()
				&& (futureOrder.getStatus() == 0 || futureOrder.getStatus() == 1 || futureOrder.getStatus() == 6);
	}

	/**
	 * 收益了多少个币
	 * 
	 * @param hedging
	 * @return
	 */
	private double getProfitCoin(Hedging hedging) {
		double profit = 0f;// 收益 币
		profit += computeProfit(hedging);
		profit += computeFee(hedging);
		return profit;
	}

	private int getLiquidAmount(HedgingTrade trade) {
		// 平仓之前，结束所有的委托未成交交易
		int liquidAmount = 0;// 可平仓的合约张数
		if (trade != null) {
			switch (trade.getStatus()) {
			case 0:
				trade.setStatus(3);
				break;
			case 1:
			case 3:
				break;
			case 2:
				if (trade.getFutureOrder() != null) {
					FutureOrder futureOrder = trade.getFutureOrder();
					liquidAmount = futureOrder.getFilledQty();
				}
				break;
			}
		}
		return liquidAmount;
	}

	/**
	 * 强制平仓
	 * 
	 * @param hedging
	 * @param reverseAmount
	 * @param level2
	 */
	private void executeLiquid(Hedging hedging, int[] reverseAmount, Level2Bean[] level2) {
		if (hedging.getStatus() == 2 || hedging.getStatus() == 0) {

			int leftBuyAmount = getLiquidAmount(hedging.getBuyTrade()) - reverseAmount[2];// 买入的剩余可平仓卖出的合约张数（不包含已委托未成交的）
			int leftSellAmount = getLiquidAmount(hedging.getSellTrade()) - reverseAmount[0];// 卖出的剩余可平仓买入的合约张数（不包含已委托未成交的）
			if (leftBuyAmount > 0 || leftSellAmount > 0) {
				hedging.setStatus(2);
				Hedging reverseHedging = hedgingTrade(level2[0], level2[1], leftSellAmount, leftBuyAmount, "3",
						hedging.getHedgingConfig(), hedging.getHedgingConfig().getFinishPremiumRate());
				if (reverseHedging != null) {
					hedgingClient.addHedging(reverseHedging);
					hedging.addReverseHedging(reverseHedging);
				}
			}
		}
	}

	private Hedging hedgingTrade(Level2Bean level2Buy, Level2Bean level2Sell, int buy_volume, int sell_volume,
			String type, HedgingConfig config, float premiumRate) {
		HedgingTrade buyTrade = new HedgingTrade();
		if (buy_volume > 0 && level2Sell != null) {
			buyTrade.setLeverRate(config.getLeverRate());
			buyTrade.setInstrumentId(level2Sell.getInstrumentId());
			buyTrade.setPrice(level2Sell.getFloatPrice() * (1 + premiumRate / 100f));
			buyTrade.setAmount(buy_volume);
			if (type.equals("3"))
				buyTrade.setType("4");
			else
				buyTrade.setType(type);
		}
		HedgingTrade sellTrade = new HedgingTrade();
		if (sell_volume > 0 && level2Buy != null) {
			sellTrade.setLeverRate(config.getLeverRate());
			sellTrade.setInstrumentId(level2Buy.getInstrumentId());
			sellTrade.setPrice(level2Buy.getFloatPrice() * (1 - premiumRate / 100f));
			sellTrade.setAmount(sell_volume);
			if (type.equals("1"))
				sellTrade.setType("2");
			else
				sellTrade.setType(type);
		}
		Hedging hedging = new Hedging(config);
		hedging.setBuyTrade(buyTrade);
		hedging.setSellTrade(sellTrade);
		return hedging;
	}

	/**
	 * @param hedging
	 * @return 0 level2Buy 当前买一价挂单信息，卖出价,1 level2Sell 当前卖一价挂单信息，买入价
	 */
	private Level2Bean[] currentHedgingLevel2(Hedging hedging) {
		Level2Bean[] result = new Level2Bean[] { null, null };
		if (hedging.getBuyTrade() != null && hedging.getBuyTrade().getAmount() > 0
				&& !Strings.isNullOrEmpty(hedging.getBuyTrade().getInstrumentId())) {
			result[0] = instrumentsDepthService.getBuyFirst(hedging.getBuyTrade().getInstrumentId());
		}
		if (hedging.getSellTrade() != null && hedging.getSellTrade().getAmount() > 0
				&& !Strings.isNullOrEmpty(hedging.getSellTrade().getInstrumentId())) {
			result[1] = instrumentsDepthService.getSellFirst(hedging.getSellTrade().getInstrumentId());
		}
		return result;
	}

	/**
	 * 已平仓委托总量
	 * 
	 * @param trade
	 * @return
	 */
	private int getAmount(HedgingTrade trade) {
		if (trade != null) {
			switch (trade.getStatus()) {
			case 0:
			case 1:
				return trade.getAmount();
			case 2:
				if (trade.getFutureOrder() == null)
					return trade.getAmount();
				return getReverseAmount(trade.getFutureOrder());
			case 3:
				return 0;
			}
		}
		return 0;
	}

	/**
	 * 已平仓委托总量
	 * 
	 * @param trade
	 * @return
	 */
	private int getReverseAmount(FutureOrder futureusdTrade) {
		if (futureusdTrade != null) {
			switch (futureusdTrade.getStatus()) {
			case 0:
			case 1:
			case 2:
				return futureusdTrade.getSize();
			case -1:
				return futureusdTrade.getFilledQty();
			}
		}
		return 0;
	}

	/**
	 * @param reverseHedgings
	 * @return 计算结果0是买入委托总量，1买入成交总量，2卖出委托总量，3卖出成交总量
	 */
	private int[] getAmount(Hedging hedging) {
		int[] result = new int[] { 0, 0, 0, 0 };

		tradeManager.correctOrderId(hedging);
		HedgingTrade buyTrade = hedging.getBuyTrade();
		if (buyTrade != null) {
			result[0] = getAmount(buyTrade);// 买入委托总量
			FutureOrder futureusdTrade = buyTrade.getFutureOrder();
			if (futureusdTrade != null) {
				result[1] = futureusdTrade.getFilledQty();
			}
		}
		HedgingTrade sellTrade = hedging.getSellTrade();
		if (sellTrade != null) {
			result[2] = getAmount(sellTrade);
			FutureOrder futureusdTrade = sellTrade.getFutureOrder();
			if (futureusdTrade != null) {
				result[3] = futureusdTrade.getFilledQty();
			}
		}

		return result;
	}

	/**
	 * @param reverseHedgings
	 * @return 计算结果0是买入委托总量，1买入成交总量，2卖出委托总量，3卖出成交总量
	 */
	private int[] getReverseAmount(List<Hedging> reverseHedgings) {
		int[] result = new int[] { 0, 0, 0, 0 };
		if (reverseHedgings == null)
			return result;
		for (Hedging reverseHedging : reverseHedgings) {
			int[] temp = getAmount(reverseHedging);
			result[0] += temp[0];
			result[1] += temp[1];
			result[2] += temp[2];
			result[3] += temp[3];
		}
		return result;
	}

//	@Override
//	public void onReceive(Object obj) {
//		if (obj instanceof JSONObject) {
//			JSONObject root = (JSONObject) obj;
//			if (root.containsKey("table")) {
//				String table = root.getString("table");
//				if ("futures/depth".equals(table)) {
//					if (root.containsKey("data")) {
//						JSONArray data = root.getJSONArray("data");
//						Iterator it = data.iterator();
//						while (it.hasNext()) {
//							Object instrument = it.next();
//							if (instrument instanceof JSONObject) {
//								JSONObject instrumentJSON = (JSONObject) instrument;
//								String instrumentId = instrumentJSON.getString("instrument_id");
//								execute();
//							}
//						}
//					}
//				}
//			}
//		}
//	}
}
