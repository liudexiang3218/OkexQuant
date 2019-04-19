package com.okex.websocket;

import com.okcoin.commons.okex.open.api.bean.spot.result.Product;
import com.xiang.service.Instrument;

public class SpotInstrument extends Product implements Instrument {
	private String contractType;

	public String getContractType() {
		return contractType;
	}

	public void setContractType(String contractType) {
		this.contractType = contractType;
	}

	public String getCoin() {
		return getBase_currency();
	}

	public long getDeliveryTime() {
		return Long.MAX_VALUE;
	}

	public String getInstrumentId() {
		return getInstrument_id();
	}
}
