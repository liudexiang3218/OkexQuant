package com.xiang.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import com.xiang.service.TradeApiService;
import com.xiang.spring.SpringContextHolder;

/**
 * 发起合约交易类
 * @author xiang
 * @createDate 2018年12月28日 上午8:59:25
 */
public class HedgingClient {
	private TradeApiService tradeApiService;
	private CoinServiceImpl coinServiceImpl;

	public HedgingClient(TradeApiService tradeApiService) {
		this.tradeApiService = tradeApiService;
		coinServiceImpl = new CoinServiceImpl();
	}

	/**
	 * 已被占用合约张数
	 */
	private int usedBuyVolume;// 已经买入了多少，对于level2上的委托卖出量减少了多少level2Sell
	private int usedSellVolume;// 已经卖出了多少，对于level2上的委托买入量减少了多少level2Buy
	private double usedMargin;// 已占用保证金
	private List<Hedging> hedgings = new LinkedList<Hedging>();

	public void start() {
		usedBuyVolume = 0;
		usedSellVolume = 0;
		usedMargin = 0;
	}

	public synchronized void addHedging(Hedging hedging) {
		if (hedging != null) {
			hedgings.add(hedging);
			if (hedging.getBuyTrade() != null) {
				usedBuyVolume += hedging.getBuyTrade().getAmount();
				usedMargin += coinServiceImpl.getMargin(hedging.getHedgingConfig().getCoin(),
						hedging.getBuyTrade().getPrice(), hedging.getBuyTrade().getLeverRate(),
						hedging.getBuyTrade().getAmount());
			}

			if (hedging.getSellTrade() != null) {
				usedSellVolume += hedging.getSellTrade().getAmount();
				usedMargin += coinServiceImpl.getMargin(hedging.getHedgingConfig().getCoin(),
						hedging.getSellTrade().getPrice(), hedging.getSellTrade().getLeverRate(),
						hedging.getSellTrade().getAmount());
			}
		}
	}

	public double getUsedMargin() {
		return usedMargin;
	}

	public void cancelHedging(Hedging hedging) {
		if (hedging != null) {
			if (hedging.getBuyTrade() != null) {
				usedBuyVolume -= hedging.getBuyTrade().getAmount();
				usedMargin -= coinServiceImpl.getMargin(hedging.getHedgingConfig().getCoin(),
						hedging.getBuyTrade().getPrice(), hedging.getBuyTrade().getLeverRate(),
						hedging.getBuyTrade().getAmount());
			}

			if (hedging.getSellTrade() != null) {
				usedSellVolume -= hedging.getSellTrade().getAmount();
				usedMargin -= coinServiceImpl.getMargin(hedging.getHedgingConfig().getCoin(),
						hedging.getSellTrade().getPrice(), hedging.getSellTrade().getLeverRate(),
						hedging.getSellTrade().getAmount());
			}

		}
	}

	private String getBatchOrdersKey(HedgingTrade trade) {
		return trade.getInstrumentId() + "-" + trade.getLeverRate();
	}

	/**
	 * 将可以一起批量提交的交易进行归并
	 * 
	 * @param trade
	 * @param sortTradesMap
	 * @return 返回可以一起批量提交的交易目前有多少个
	 */
	private int sortTrade(HedgingTrade trade, Map<String, List<HedgingTrade>> sortTradesMap) {
		if (trade != null && trade.getAmount() > 0 && trade.getStatus() == 0) {
			List<HedgingTrade> batchOrders = null;
			String key = getBatchOrdersKey(trade);
			if (!sortTradesMap.containsKey(key)) {
				batchOrders = new ArrayList<>();
				sortTradesMap.put(key, batchOrders);
			} else {
				batchOrders = sortTradesMap.get(key);
			}
			batchOrders.add(trade);
			TradeManager.getInstance().addHedgingTrade(trade);
			return batchOrders.size();
		}
		return 0;
	}

	private void finish(Map<String, List<HedgingTrade>> sortTradesMap) throws TimeoutException {
		for (List<HedgingTrade> batchOrders : sortTradesMap.values()) {
			if (!batchOrders.isEmpty()) {
				if ("production".equals(SpringContextHolder.getActiveProfile())) {
					tradeApiService.batchOrders(batchOrders);
				}
				tradeCount++;
				batchOrders.clear();
			}
		}
	}

	private int tradeCount = 0;

	public void finish() throws TimeoutException {
		tradeCount = 0;
		Map<String, List<HedgingTrade>> sortTradesMap = new HashMap<>();
		for (Hedging hedging : hedgings) {
			if (tradeCount >= 20) {
				// 超过了每2秒20次的请求限制
				cancelHedging(hedging);
			} else {
				int currentBuyCount = sortTrade(hedging.getBuyTrade(), sortTradesMap);
				int currentSellCount = sortTrade(hedging.getSellTrade(), sortTradesMap);
				if (currentBuyCount == 5 || currentSellCount == 5) {
					finish(sortTradesMap);
				}
			}
		}
		finish(sortTradesMap);
		hedgings.clear();
	}

	/**
	 * @return 单边占用合约张数量
	 */
	public int getUsedVolume() {
		return Math.max(usedBuyVolume, usedSellVolume);
	}

	public int getUsedBuyVolume() {
		return usedBuyVolume;
	}

	public int getUsedSellVolume() {
		return usedSellVolume;
	}

}
