package com.xiang.service.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.base.Strings;
import com.okex.websocket.FutureOrder;

/**
 * 接收到的交易数据管理类
 * @author xiang
 * @createDate 2018年12月7日 下午2:56:01
 */
public class TradeManager {
	private TradeManager() {
	};

	public static TradeManager getInstance() {
		return SingletonHolder.instance;
	}
	/**
	 * key 是hedgingTradeId
	 */
	private Map<String, HedgingTrade> trades = new ConcurrentHashMap<String, HedgingTrade>();
	/**
	 * key 是hedgingTradeId 和 order_id
	 */
	private Map<String, FutureOrder> dissFutureusdTrades = new ConcurrentHashMap<String, FutureOrder>();// 游离的交易信息，未匹配成功的交易信息

	public HedgingTrade getHedgingTradeById(String hedgingTradeId) {
		return trades.get(hedgingTradeId);
	}
	public void addHedgingTrade(HedgingTrade hedgingTrade) {
		trades.put(hedgingTrade.getHedgingTradeId(), hedgingTrade);
	}
	public HedgingTrade getHedgingTradeByOrderId(String orderId) {
		for (HedgingTrade hedgingTrade : trades.values()) {
			if (orderId.equals(hedgingTrade.getOrderId()))
				return hedgingTrade;
		}
		return null;
	}
	/**
	 * @param hedging
	 *            设置正确的成交信息
	 */
	public void correctOrderId(Hedging hedging) {
		HedgingTrade buyTrade = hedging.getBuyTrade();
		if (buyTrade != null && buyTrade.getFutureOrder() == null && (buyTrade.getStatus()==1 || buyTrade.getStatus()==2)) {
			// 优先使用orderid
			if (buyTrade.getOrderId() != null && dissFutureusdTrades.containsKey(buyTrade.getOrderId())) {
				buyTrade.setFutureOrder(dissFutureusdTrades.get(buyTrade.getOrderId()));
				dissFutureusdTrades.remove(buyTrade.getOrderId());
				dissFutureusdTrades.remove(buyTrade.getHedgingTradeId());
			} else if (buyTrade.getHedgingTradeId() != null
					&& dissFutureusdTrades.containsKey(buyTrade.getHedgingTradeId())) {
				// client_oid
				buyTrade.setFutureOrder(dissFutureusdTrades.get(buyTrade.getHedgingTradeId()));
				dissFutureusdTrades.remove(buyTrade.getHedgingTradeId());
				dissFutureusdTrades.remove(buyTrade.getOrderId());
			}
		}
		HedgingTrade sellTrade = hedging.getSellTrade();
		if (sellTrade != null && sellTrade.getFutureOrder() == null && (sellTrade.getStatus()==1 || sellTrade.getStatus()==2)) {
			if (sellTrade.getOrderId() != null && dissFutureusdTrades.containsKey(sellTrade.getOrderId())) {
				sellTrade.setFutureOrder(dissFutureusdTrades.get(sellTrade.getOrderId()));
				dissFutureusdTrades.remove(sellTrade.getOrderId());
				dissFutureusdTrades.remove(sellTrade.getHedgingTradeId());
			} else if (sellTrade.getHedgingTradeId() != null
					&& dissFutureusdTrades.containsKey(sellTrade.getHedgingTradeId())) {
				sellTrade.setFutureOrder(dissFutureusdTrades.get(sellTrade.getHedgingTradeId()));
				dissFutureusdTrades.remove(sellTrade.getHedgingTradeId());
				dissFutureusdTrades.remove(sellTrade.getOrderId());
			}
		}
	}
	/**
	 * 前提假设okex 推送消息按顺序，如果顺序出问题，将导致对应到旧状态的订单
	 * @param futureOrder
	 */
	public void updateFuturesOrder(FutureOrder futureOrder)
	{
		HedgingTrade trade = null;
		if (!Strings.isNullOrEmpty(futureOrder.getClientOid())) {
			trade = getHedgingTradeById(futureOrder.getClientOid());
		}
		if (trade == null && !Strings.isNullOrEmpty(futureOrder.getOrderId())) {
			trade = this.getHedgingTradeByOrderId(futureOrder.getOrderId());
		}
		if (trade != null) {
			trade.setFutureOrder(futureOrder);
			trade.setOrderId(futureOrder.getOrderId());
			trade.setStatus(2);
		} else {
			if (!Strings.isNullOrEmpty(futureOrder.getClientOid())) {
				dissFutureusdTrades.put(futureOrder.getClientOid(), futureOrder);
			}
			if (!Strings.isNullOrEmpty(futureOrder.getOrderId())) {
				dissFutureusdTrades.put(futureOrder.getOrderId(), futureOrder);
			}
		}
	}
	private static class SingletonHolder {
		private static final TradeManager instance = new TradeManager();
	}
}
