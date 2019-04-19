package com.xiang.service.impl;

import java.util.LinkedList;
import java.util.List;

import net.sf.ehcache.CacheManager;

/**
 * 合约库存管理 保存已被占用的合约张数
 * 
 * @author xiang
 * @createDate 2018年12月7日 下午2:56:01
 */
public class HedgingManager {
	private HedgingManager() {
	    
	};

	private List<Hedging> hedgings = new LinkedList<Hedging>();
	public void initHedgings(List<Hedging> list)
	{
		hedgings=list;
	}
	public List<Hedging> getHedgings() {
		// TODO Auto-generated method stub
		return hedgings;
	}

	public void addHedging(Hedging hedging) {
		hedgings.add(hedging);
	}

	public static HedgingManager getInstance() {
		return SingletonHolder.instance;
	}

	public void liquidHedging(String hedgingId) {
		for (Hedging hedging : hedgings) {
			if (hedgingId.equals(hedging.getHedgingId())) {
				hedging.setLiquid(true);
				return;
			}
		}
	}

	private static class SingletonHolder {
		private static final HedgingManager instance = new HedgingManager();
	}
}
