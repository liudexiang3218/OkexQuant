package com.okex.websocket;

import com.okcoin.commons.okex.open.api.bean.futures.result.Instruments;
import com.xiang.service.Instrument;

/**
* @author xiang
* @createDate 2018年12月27日 上午11:30:41
*/
public class FutureInstrument extends Instruments implements Instrument{

	/**
	 * 合约类型 this_week,next_week,quarter
	 */
	private String contractType;

	public String getContractType() {
		return contractType;
	}

	public void setContractType(String contractType) {
		this.contractType = contractType;
	}
	private long deliveryTime;

	public long getDeliveryTime() {
		return deliveryTime;
	}

	public void setDeliveryTime(long deliveryTime) {
		this.deliveryTime = deliveryTime;
	}
	
	public String getCoin()
	{
		return getUnderlying_index();
	}
	public String getInstrumentId()
	{
		return getInstrument_id();
	}
}
