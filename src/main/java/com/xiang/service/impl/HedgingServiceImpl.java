package com.xiang.service.impl;

import java.util.List;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.stereotype.Service;

import com.xiang.service.HedgingService;
import com.xiang.service.TradeApiService;

/**
 * 套利策略服务类
 * @author xiang
 * @createDate 2018年11月5日 上午9:50:59
 */
@Service("hedgingService")
@EnableRetry
public class HedgingServiceImpl implements HedgingService {
	/**
	 * okex交易api服务
	 */
	@Autowired
	private TradeApiService tradeApiService;

	/**
	 * 删除策略
	 */
	@Override
	public void removeHedgingConfig(String configId) {
		HedgingConfigManager.getInstance().delete(configId);
	}

	@Override
	public List<Hedging> getHedgings() {
		return HedgingManager.getInstance().getHedgings();
	}
	/**
	 * 策略列表
	 */
	@Override
	public List<HedgingConfig> getConfigs(String coin, String type) {
		return HedgingConfigManager.getInstance().getConfigs(coin, type);
	}
	/**
	 * 获取策略
	 */
	@Override
	public HedgingConfig getHedgingConfig(String configId) {
		return HedgingConfigManager.getInstance().getHedgingConfig(configId);
	}
	/**
	 * 添加策略
	 */
	@Override
	public void addHedgingConfig(HedgingConfig config) {
		HedgingConfigManager.getInstance().save(config);
	}

	

	/**
	 * 强制平仓套利交易
	 */
	@Override
	public void liquidHedging(String hedgingId) {
		HedgingManager.getInstance().liquidHedging(hedgingId);
	}

	/**
	 * 修复对冲（修复实际已成交，但未收到订单信息的对冲交易）
	 * 
	 * @param trade
	 * @throws TimeoutException
	 */
	@Override
	public Hedging repairHedging(Hedging hedging) throws TimeoutException {
		if (hedging.getStatus() != 1) {
			tradeApiService.repairHedgingTrade(hedging.getBuyTrade());
			tradeApiService.repairHedgingTrade(hedging.getSellTrade());
			for (Hedging reversehedging : hedging.getReverseHedgings()) {
				repairHedging(reversehedging);
			}
		}
		return hedging;
	}

	@Override
	public Hedging repairHedging(String hedgingId) throws TimeoutException {
		for (Hedging hedging : HedgingManager.getInstance().getHedgings()) {
			if (hedgingId.equals(hedging.getHedgingId())) {
				return repairHedging(hedging);
			}
		}
		return null;
	}
	/**
	 * 强制平仓所有套利交易
	 */
	@Override
	public void liquidAllHedging() {
		for (Hedging hedging : HedgingManager.getInstance().getHedgings()) {
			if (hedging.getStatus() != 1)
				hedging.setLiquid(true);
		}
	}
	/**
	 * 新建策略初始化模板
	 */
	@Override
	public HedgingConfig newHedgingConfig(String coin, String type) {
		// TODO Auto-generated method stub
		HedgingConfig hedgingConfig = new HedgingConfig();
		hedgingConfig.setCoin(coin);
		hedgingConfig.setType(type);
		return hedgingConfig;
	}

	
}
