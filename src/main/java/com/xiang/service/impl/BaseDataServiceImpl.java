package com.xiang.service.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
* @author xiang
* @createDate 2018年12月24日 上午10:14:45
*/
public abstract class BaseDataServiceImpl{
	protected Map<String, HedgingContext> HedgingContexts = new HashMap<String, HedgingContext>();
	public HedgingContext getHedgingContext(String coin) {
		coin=coin.toLowerCase();
		if(!HedgingContexts.containsKey(coin))
		{
			HedgingContexts.put(coin, new HedgingContext(coin));
		}
		return HedgingContexts.get(coin);
	}
	/**
	 * @param coin
	 * @param type tn,tq,nq
	 * @return
	 */
	public LinkedList<Map<String, Object>> getHedgingData(String coin, String type) {
		HedgingContext hc = getHedgingContext(coin);
		if (hc != null) {
			return hc.getHedgingData(type);
		}
		return null;
	}
}
