package com.xiang.service.impl;

import com.okcoin.commons.okex.open.api.config.APIConfiguration;

/**
 * @author xiang
 * @createDate 2018年11月20日 下午6:54:45
 */
public class SystemConfig extends APIConfiguration{
	private String okWebSocketURL;
	private String coins;
	private String signKey;
	public String getSignKey() {
		return signKey;
	}

	public void setSignKey(String signKey) {
		this.signKey = signKey;
	}

	public String getOkWebSocketURL() {
		return okWebSocketURL;
	}

	public void setOkWebSocketURL(String okWebSocketURL) {
		this.okWebSocketURL = okWebSocketURL;
	}

	public String getCoins() {
		return coins;
	}

	public void setCoins(String coins) {
		this.coins = coins;
	}
}
