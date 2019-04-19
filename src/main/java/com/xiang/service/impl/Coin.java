package com.xiang.service.impl;
/**
* @author xiang
* @createDate 2018年12月14日 上午10:28:58
*/
public class Coin {
	public Coin(String symbol,String type,String base)
	{
		this.symbol=symbol;
		this.type=type;
		this.base=base;
	}
	private String symbol;//btc_usd
	private String type;//btc
	private String base;//f_usd_btc
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getBase() {
		return base;
	}
	public void setBase(String base) {
		this.base = base;
	}

}
