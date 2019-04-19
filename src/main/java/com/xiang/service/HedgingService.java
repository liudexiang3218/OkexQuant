package com.xiang.service;

import java.util.List;
import java.util.concurrent.TimeoutException;

import com.xiang.service.impl.Hedging;
import com.xiang.service.impl.HedgingConfig;

/**
* @author xiang
* @createDate 2018年11月5日 上午9:48:06
*/
public interface HedgingService {
	public void removeHedgingConfig(String configId);
	public List<Hedging> getHedgings();
	public List<HedgingConfig> getConfigs(String coin, String type);
	public HedgingConfig newHedgingConfig(String coin, String type);
	public HedgingConfig getHedgingConfig(String configId);
	public void addHedgingConfig(HedgingConfig config);
	//强制平仓
	public void liquidHedging(String hedgingId);
	public void liquidAllHedging();
	//修复交易（修复实际已成交，但未收到订单信息的对冲交易）
	public Hedging repairHedging(String hedgingId) throws TimeoutException;
	//修复对冲（修复实际已成交，但未收到订单信息的对冲交易）
	public Hedging repairHedging(Hedging hedging) throws TimeoutException;
}
