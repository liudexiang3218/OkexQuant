package com.okex.websocket;

import java.io.Serializable;

/**
 * 期货交易订单
 * @author xiang
 *
 */
public class FutureOrder implements Serializable{
	private static final long serialVersionUID = 1L;
	private String instrumentId;// 	String 	合约ID，如BTC-USDT-180213
	private int size;// 	String 	数量
	private String timestamp; //	String 	委托时间
	private int filledQty;// 	String 	成交数量
	private double fee; 	//String 	手续费
	private String orderId; //	String 	订单ID
	private float price; 	//String 	订单价格
	private float priceAvg; //	String 	平均价格
	private int status; //	String 	订单状态(-1.撤单成功；0:等待成交 1:部分成交 2:全部成交 6：未完成（等待成交+部分成交）7：已完成（撤单成功+全部成交））
	private int type; 	//String 	订单类型(1:开多 2:开空 3:平多 4:平空)
	private int contractVal; 	//String 	合约面值
	private int leverage; 	//String 	杠杆倍数 value:10/20 默认10
	private String clientOid;
	public String getInstrumentId() {
		return instrumentId;
	}
	public void setInstrumentId(String instrumentId) {
		this.instrumentId = instrumentId;
	}
	public int getSize() {
		return size;
	}
	public String getClientOid() {
		return clientOid;
	}
	public void setClientOid(String clientOid) {
		this.clientOid = clientOid;
	}
	public void setSize(int size) {
		this.size = size;
	}
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	public int getFilledQty() {
		return filledQty;
	}
	public void setFilledQty(int filledQty) {
		this.filledQty = filledQty;
	}
	public double getFee() {
		return fee;
	}
	public void setFee(double fee) {
		this.fee = fee;
	}
	public String getOrderId() {
		return orderId;
	}
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	public float getPrice() {
		return price;
	}
	public void setPrice(float price) {
		this.price = price;
	}
	public float getPriceAvg() {
		return priceAvg;
	}
	public void setPriceAvg(float priceAvg) {
		this.priceAvg = priceAvg;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	
	public int getContractVal() {
		return contractVal;
	}
	public void setContractVal(int contractVal) {
		this.contractVal = contractVal;
	}
	public int getLeverage() {
		return leverage;
	}
	public void setLeverage(int leverage) {
		this.leverage = leverage;
	}
	
}
