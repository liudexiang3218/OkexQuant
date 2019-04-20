package com.xiang.service.impl;

import org.springframework.stereotype.Service;

/**
* @author xiang
* @createDate 2018年12月27日 下午3:51:19
*/
@Service("coinService")
public class CoinServiceImpl {

	/**
	 * 获取虚拟币单张合约美元价值
	 * @param coin
	 * @return
	 */
	public int getUnitAmount(String coin)
	{
		if ("btc".equals(coin.toLowerCase()))
			return 100;
		return 10;
	}
	/**
	 * @param coin 虚拟币
	 * @param price 价格
	 * @param leverRate 杠杆
	 * @param amount 合约张数
	 * @return 计算需要占用的保证金
	 */
	public double getMargin(String coin, float price, int leverRate,int amount)
	{
		return getUnitAmount(coin)*amount/price/leverRate;
	}
}
