package com.xiang.service;

import java.util.LinkedList;
import java.util.Map;

import com.xiang.service.impl.HedgingContext;

/**
* @author xiang
* @createDate 2018年12月11日 下午2:55:20
*/
public interface HedgingDataService{
	public HedgingContext getHedgingContext(String coin);
	/**
	 * 获取临时缓存图表数据
	 * @param coin
	 * @param type tn,tq,nq
	 * @return
	 */
	public LinkedList<Map<String, Object>> getHedgingData(String coin,String type);
}
