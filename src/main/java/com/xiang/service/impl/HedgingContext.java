package com.xiang.service.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * 套利价差数据存储
 * @author xiang
 * @createDate 2018年12月10日 下午2:41:31
 */
public class HedgingContext {
	/**
	 * btc,eos...
	 */
	private String coin;
	private static int cacheSize = 4000;
	/**
	 * key (tt,tn,tq,nq)
	 */
	private Map<String, LinkedList<Map<String, Object>>> dataCaches = new HashMap<String, LinkedList<Map<String, Object>>>();

	public String getCoin() {
		return coin;
	}

	public void setCoin(String coin) {
		this.coin = coin;
	}

	public HedgingContext(String coin) {
		this.coin = coin;
	}

	/**
	 * @param type tt,tn,tq,nq
	 * @return
	 */
	public LinkedList<Map<String, Object>> getHedgingData(String type) {
		return dataCaches.get(type);
	}

	public void addHedgingData(Map<String, Object> map) {
		//tt,tn,tq,nq
		String type = (String) map.get("type");
		LinkedList<Map<String, Object>> dataCache = getHedgingData(type);
		if (dataCache == null) {
			dataCache = new LinkedList<Map<String, Object>>();
			dataCaches.put(type, dataCache);
		}
		dataCache.add(map);
		if (dataCache.size() > cacheSize) {
			dataCache.removeFirst();
		}
	}
}
