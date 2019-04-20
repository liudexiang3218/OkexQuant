package com.xiang.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import com.xiang.service.TradeApiService;

/**
 * 每笔交易代理类，集中处理一次性提交所有的交易
 * @author xiang
 * @createDate 2018年12月28日 上午8:59:25
 */
public class HedgingTradeClient {
	private TradeApiService tradeApiService;

	public HedgingTradeClient(TradeApiService tradeApiService) {
		this.tradeApiService = tradeApiService;
	}

	private List<HedgingTrade> trades = new LinkedList<HedgingTrade>();

	public void start() {
	}

	public synchronized void addHedgingTrade(HedgingTrade trade) {
		if (trade != null) {
			trades.add(trade);
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
		List<HedgingTrade> batchOrders = null;
		String key = getBatchOrdersKey(trade);
		if (!sortTradesMap.containsKey(key)) {
			batchOrders = new ArrayList<>();
			sortTradesMap.put(key, batchOrders);
		} else {
			batchOrders = sortTradesMap.get(key);
		}
		batchOrders.add(trade);
		return batchOrders.size();
	}

	private void cancelFinish(Map<String, List<HedgingTrade>> sortTradesMap) throws TimeoutException {
		for (List<HedgingTrade> batchOrders : sortTradesMap.values()) {
			if (!batchOrders.isEmpty()) {
				cancelFinish(batchOrders);
			}
		}
	}

	private void cancelFinish(List<HedgingTrade> batchOrders) throws TimeoutException {
		if (!batchOrders.isEmpty()) {
			tradeApiService.batchCancel(batchOrders);
			tradeCount++;
			batchOrders.clear();
		}
	}

	private int tradeCount = 0;

	public void cancelFinish() throws TimeoutException {
		tradeCount = 0;
		Map<String, List<HedgingTrade>> sortTradesMap = new HashMap<>();
		for (HedgingTrade trade : trades) {
			if (tradeCount >= 20) {
				// 超过了每2秒20次的请求限制

			} else {
				int currentCount = sortTrade(trade, sortTradesMap);
				if (currentCount == 20) {//每次最多撤销20张单
					cancelFinish(sortTradesMap.get(getBatchOrdersKey(trade)));
				}
			}
		}
		cancelFinish(sortTradesMap);
		trades.clear();
	}
}
