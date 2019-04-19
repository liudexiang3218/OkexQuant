package com.xiang.service.impl;

import java.io.Serializable;
import java.util.UUID;

public class HedgingConfig implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7009794771611501323L;

	private String title = "新策略";

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	private String configId = UUID.randomUUID().toString().replaceAll("-", "");

	public String getConfigId() {
		return configId;
	}

	public void setConfigId(String configId) {
		this.configId = configId;
	}
	/**
	 *  BTC,ETC...
	 */
	private String coin;
	/**
	 * type in (tt,tn,tq,nq)
	 */
	private String type;

	/**
	 * 是否使用自动阈值模式，如果使用自动模式将忽略sellBuyThresholdRate，buySellThresholdRate
	 */
	private boolean autoSellBuyThresholdRate;
	private boolean autoBuySellThresholdRate;

	/**
	 * 近期开空远期开多阈值，当近期买一价减去远期卖一价%超过这个阈值，进行对冲套利
	 */
	private float sellBuyThresholdRate = 0.7f;
	/**
	 * 近期开多远期开空阈值，当远期买一价减去近期卖一价%超过这个阈值，进行对冲套利
	 */
	private float buySellThresholdRate = -0.15f;

	
	/**
	 * 开仓滑点%，加大买价和降低卖价，促使交易完全成交
	 */
	private float startPremiumRate = 0f;
	/**
	 * 平仓滑点%，加大买价和降低卖价，促使交易完全成交
	 */
	private float finishPremiumRate = 0f;
	/**
	 * 止盈率 %
	 */
	private float profitRate = 3f;

	/**
	 * 杠杆率
	 */
	private int leverRate = 20;

	/**
	 * 账号剩余合约交易张数
	 */
	private int volume = 1;

	/**
	 * 每笔最大交易合约张数，0不限制
	 */
	private int maxTradeVolume = 1;

	/**
	 * 距离交割时间在多少小时内不开仓，目前以星期五交割日16:00交割,值为0代表不限制
	 */
	private int lastHegingHour = 5;

	/**
	 * 对冲交易成交后，多少小时未止盈平仓的，采取强制平仓策略，0为不强制平仓。和可接受的强制平仓止损率LiquidRate一起配合使用
	 */
	private int maxHedgingHour = 0;

	private int buyLevel = 2;// 由于使用买一价来判断和购买是否可以对冲，经常容易导致只成交了一方，而对手方无法成功成交到，所以可以使用其他买几价来减低这个概率。使用买几价来匹配，买一价，买二价，买三价，依此类推。
	private int sellLevel = 2;// 由于使用卖一价来判断和购买是否可以对冲，经常容易导致只成交了一方，而对手方无法成功成交到，所以可以使用其他卖几价来减低这个概率。使用卖几价来匹配，卖一价，卖二价，卖三价，依此类推。
	/**
	 * 是否开始对冲套利
	 */
	private boolean start;
	/**
	 * 开仓最低剩余委托金额（美元），对冲完成后剩余的买卖双方买卖价格线的的总委托金额必须大于等于这个阀值才开启对冲，为了防止对冲时对冲失败
	 */
	private float startThresholdAmount=500;
	/**
	 * 平仓最低剩余委托金额（美元），对冲完成后剩余的买卖双方买卖价格线的的总委托金额必须大于等于这个阀值才开启对冲，为了防止对冲时对冲失败
	 */
	private float finishThresholdAmount=500;

	public float getFinishThresholdAmount() {
		return finishThresholdAmount;
	}

	public void setFinishThresholdAmount(float finishThresholdAmount) {
		this.finishThresholdAmount = finishThresholdAmount;
	}

	public float getStartThresholdAmount() {
		return startThresholdAmount;
	}

	public void setStartThresholdAmount(float startThresholdAmount) {
		this.startThresholdAmount = startThresholdAmount;
	}

	public int getBuyLevel() {
		return buyLevel;
	}

	public void setBuyLevel(int buyLevel) {
		this.buyLevel = buyLevel;
	}

	public int getSellLevel() {
		return sellLevel;
	}

	public void setSellLevel(int sellLevel) {
		this.sellLevel = sellLevel;
	}

	public boolean isStart() {
		return start;
	}

	public void setStart(boolean start) {
		this.start = start;
	}

	public float getSellBuyThresholdRate() {
		return sellBuyThresholdRate;
	}

	public void setSellBuyThresholdRate(float sellBuyThresholdRate) {
		this.sellBuyThresholdRate = sellBuyThresholdRate;
	}

	public float getBuySellThresholdRate() {
		return buySellThresholdRate;
	}

	public void setBuySellThresholdRate(float buySellThresholdRate) {
		this.buySellThresholdRate = buySellThresholdRate;
	}

	public float getProfitRate() {
		return profitRate;
	}

	public void setProfitRate(float profitRate) {
		this.profitRate = profitRate;
	}

	public int getLeverRate() {
		return leverRate;
	}

	public void setLeverRate(int leverRate) {
		this.leverRate = leverRate;
	}

	public int getVolume() {
		return volume;
	}

	public synchronized void setVolume(int volume) {
		this.volume = volume;
	}

	public int getMaxTradeVolume() {
		return maxTradeVolume;
	}

	public void setMaxTradeVolume(int maxTradeVolume) {
		this.maxTradeVolume = maxTradeVolume;
	}

	public int getMaxHedgingHour() {
		return maxHedgingHour;
	}

	public void setMaxHedgingHour(int maxHedgingHour) {
		this.maxHedgingHour = maxHedgingHour;
	}


	public int getLastHegingHour() {
		return lastHegingHour;
	}

	public void setLastHegingHour(int lastHegingHour) {
		this.lastHegingHour = lastHegingHour;
	}

	public boolean isAutoSellBuyThresholdRate() {
		return autoSellBuyThresholdRate;
	}

	public void setAutoSellBuyThresholdRate(boolean autoSellBuyThresholdRate) {
		this.autoSellBuyThresholdRate = autoSellBuyThresholdRate;
	}

	public boolean isAutoBuySellThresholdRate() {
		return autoBuySellThresholdRate;
	}

	public void setAutoBuySellThresholdRate(boolean autoBuySellThresholdRate) {
		this.autoBuySellThresholdRate = autoBuySellThresholdRate;
	}

	public String getCoin() {
		return coin;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	public void setCoin(String coin) {
		this.coin = coin;
	}

	public float getStartPremiumRate() {
		return startPremiumRate;
	}

	public void setStartPremiumRate(float startPremiumRate) {
		this.startPremiumRate = startPremiumRate;
	}

	public float getFinishPremiumRate() {
		return finishPremiumRate;
	}

	public void setFinishPremiumRate(float finishPremiumRate) {
		this.finishPremiumRate = finishPremiumRate;
	}

}
