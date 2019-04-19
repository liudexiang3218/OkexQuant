package com.okex.websocket;

public class FutureAccount {
	private String currency;//币种，如：btc
	private String marginMode;// 	账户类型：全仓 crossed
	private double equity;// 	账户权益
	private double totalAvailBalance;//账户余额
	private double margin;// 	保证金（挂单冻结+持仓已用）
	private double realizedPnl;//已实现盈亏
	private double unrealizedPnl;//未实现盈亏
	private float marginRatio;//保证金率
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public String getMarginMode() {
		return marginMode;
	}
	public void setMarginMode(String marginMode) {
		this.marginMode = marginMode;
	}
	public double getEquity() {
		return equity;
	}
	public void setEquity(double equity) {
		this.equity = equity;
	}
	public double getTotalAvailBalance() {
		return totalAvailBalance;
	}
	public void setTotalAvailBalance(double totalAvailBalance) {
		this.totalAvailBalance = totalAvailBalance;
	}
	public double getMargin() {
		return margin;
	}
	public void setMargin(double margin) {
		this.margin = margin;
	}
	public double getRealizedPnl() {
		return realizedPnl;
	}
	public void setRealizedPnl(double realizedPnl) {
		this.realizedPnl = realizedPnl;
	}
	public double getUnrealizedPnl() {
		return unrealizedPnl;
	}
	public void setUnrealizedPnl(double unrealizedPnl) {
		this.unrealizedPnl = unrealizedPnl;
	}
	public float getMarginRatio() {
		return marginRatio;
	}
	public void setMarginRatio(float marginRatio) {
		this.marginRatio = marginRatio;
	}
	
}
