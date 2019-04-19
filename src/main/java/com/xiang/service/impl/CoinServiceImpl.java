package com.xiang.service.impl;

import org.springframework.stereotype.Service;

/**
* @author xiang
* @createDate 2018年12月27日 下午3:51:19
*/
@Service("coinService")
public class CoinServiceImpl {

	public int getUnitAmount(String coin)
	{
		if ("btc".equals(coin.toLowerCase()))
			return 100;
		return 10;
	}
	/**
	 * @param coin
	 * @param price
	 * @param leverRate
	 * @param amount
	 * @return 计算需要占用的保证金
	 */
	public double getMargin(String coin, float price, int leverRate,int amount)
	{
		return getUnitAmount(coin)*amount/price/leverRate;
	}
}
