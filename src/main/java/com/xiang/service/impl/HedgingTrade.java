package com.xiang.service.impl;

import java.io.Serializable;
import java.util.UUID;

import com.google.common.base.Strings;
import com.okex.websocket.FutureOrder;

public class HedgingTrade implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String hedgingTradeId = UUID.randomUUID().toString().replaceAll("-", "");
	private int status = 0;// 提交状态 0未开始 1提交中 2成功 3失败
	private FutureOrder futureOrder;//对应的交易所交易订单
	private String instrumentId;// 合约号如BTC-USD-180213
	private long deliveryTime;//合约到期时间
	private float price;//委托下单金额
	private int amount;//委托下单合约张数
	private int leverRate;//杠杠率10,20
	private String type;//1 买入，2 卖出，3 平买，4平卖
	private String orderId;////对应的交易所交易订单编号
	private long addTime;
	public HedgingTrade()
	{
		addTime=System.currentTimeMillis();
	}

	public long getAddTime() {
		return addTime;
	}

	public void setAddTime(long addTime) {
		this.addTime = addTime;
	}

	public long getDeliveryTime() {
		return deliveryTime;
	}

	public void setDeliveryTime(long deliveryTime) {
		this.deliveryTime = deliveryTime;
	}

	public String getInstrumentId() {
		return instrumentId;
	}

	public void setInstrumentId(String instrumentId) {
		this.instrumentId = instrumentId;
	}

	public int getLeverRate() {
		return leverRate;
	}

	public void setLeverRate(int leverRate) {
		this.leverRate = leverRate;
	}

	public String getOrderId() {
		return orderId;
	}

	public synchronized void setOrderId(String orderId) {
		if (Strings.isNullOrEmpty(this.orderId))
			this.orderId = orderId;
	}

	public float getPrice() {
		return price;
	}

	public void setPrice(float price) {
		this.price = price;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getReverseType() {
		if (type == null)
			return null;
		if (type.equals("1"))
			return "3";
		if (type.equals("2"))
			return "4";
		if (type.equals("3"))
			return "1";
		if (type.equals("4"))
			return "2";
		return null;
	}

	public int getStatus() {
		return status;
	}

	public synchronized void setStatus(int status) {
		if (this.status != 2)// 提交成功,级别最大
			this.status = status;
	}

	public String getHedgingTradeId() {
		return hedgingTradeId;
	}

	public void setHedgingTradeId(String hedgingTradeId) {
		this.hedgingTradeId = hedgingTradeId;
	}

	public FutureOrder getFutureOrder() {
		return futureOrder;
	}

	public void setFutureOrder(FutureOrder futureOrder) {
		this.futureOrder = futureOrder;
	}

}
