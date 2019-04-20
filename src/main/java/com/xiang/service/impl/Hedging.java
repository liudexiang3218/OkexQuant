package com.xiang.service.impl;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.UUID;

/**
 * 套利交易存储对象
 * @author Administrator
 *
 */
public class Hedging implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Hedging(HedgingConfig config) {
		this.hedgingConfig = config;
		this.time = System.currentTimeMillis();
	}

	public HedgingConfig getHedgingConfig() {
		return hedgingConfig;
	}

	private String hedgingId = UUID.randomUUID().toString().replaceAll("-", "");

	public String getHedgingId() {
		return hedgingId;
	}

	public void setHedgingId(String hedgingId) {
		this.hedgingId = hedgingId;
	}
	/**
	 * 对冲合约张数
	 */
	private int amount;
	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	/**
	 * 开始对冲时间
	 */
	private long time;

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	/**
	 * 当前对冲的盈利率
	 */
	private float profitRate;

	public float getProfitRate() {
		return profitRate;
	}

	public void setProfitRate(float profitRate) {
		this.profitRate = profitRate;
	}

	/**
	 * 对冲交易所对应的配置参数
	 */
	private HedgingConfig hedgingConfig;
	/**
	 * 买单交易
	 */
	private HedgingTrade buyTrade;
	/**
	 * 卖单交易
	 */
	private HedgingTrade sellTrade;

	/**
	 * 所有的对冲记录
	 */
	private LinkedList<Hedging> reverseHedgings = new LinkedList<Hedging>();
	/**
	 * 是否强制平仓，手动平仓交易
	 */
	private boolean liquid = false;

	private String title;

	/**
	 * 显示标题
	 */
	public String getTitle() {
		if (!org.apache.commons.lang3.StringUtils.isEmpty(title))
			return title;
		if (this.hedgingConfig != null) {
			StringBuffer buffer = new StringBuffer();
			buffer.append(this.hedgingConfig.getTitle());
			title = buffer.toString();
			return title;
		}
		return "";
	}

	public boolean isLiquid() {
		return liquid;
	}

	public void setLiquid(boolean liquid) {
		this.liquid = liquid;
	}

	private int status;// 0未完成 ,1完成 ,2强制平仓中

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public LinkedList<Hedging> getReverseHedgings() {
		return reverseHedgings;
	}

	public void addReverseHedging(Hedging reverseHedging) {
		reverseHedgings.add(reverseHedging);
	}

	public HedgingTrade getBuyTrade() {
		return buyTrade;
	}

	public void setBuyTrade(HedgingTrade buyTrade) {
		this.buyTrade = buyTrade;
	}

	public HedgingTrade getSellTrade() {
		return sellTrade;
	}

	public void setSellTrade(HedgingTrade sellTrade) {
		this.sellTrade = sellTrade;
	}
}
