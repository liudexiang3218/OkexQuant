package com.xiang.service.impl;

import org.springframework.stereotype.Service;

/**
* @author xiang
* @createDate 2018年11月29日 下午2:55:50
*/
@Service("accountConfig")
public class AccountConfig {
	/**
	 * 合约开仓手续费率
	 */
	private float buyFeeRate = 0.0003f;//万分之三
	/**
	 * 合约平仓手续费率
	 */
	private float sellFeeRate = 0.0003f;//万分之三
	
	/**
	 * 币币开仓手续费率
	 */
	private float buyFeeRateSpot = 0.0015f;
	/**
	 * 币币平仓手续费率
	 */
	private float sellFeeRateSpot = 0.0015f;
	
	public float getBuyFeeRateSpot() {
		return buyFeeRateSpot;
	}
	public void setBuyFeeRateSpot(float buyFeeRateSpot) {
		this.buyFeeRateSpot = buyFeeRateSpot;
	}
	public float getSellFeeRateSpot() {
		return sellFeeRateSpot;
	}
	public void setSellFeeRateSpot(float sellFeeRateSpot) {
		this.sellFeeRateSpot = sellFeeRateSpot;
	}
	public float getBuyFeeRate() {
		return buyFeeRate;
	}
	public void setBuyFeeRate(float buyFeeRate) {
		this.buyFeeRate = buyFeeRate;
	}
	public float getSellFeeRate() {
		return sellFeeRate;
	}
	public void setSellFeeRate(float sellFeeRate) {
		this.sellFeeRate = sellFeeRate;
	}
}
