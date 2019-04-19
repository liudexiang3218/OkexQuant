package com.xiang.service;

import com.xiang.service.impl.Level2Bean;

/**
* @author xiang
* @createDate 2018年11月27日 上午9:51:01
*/
public interface Level2Service {
	public Level2Bean getSellLevel2Postion(int pos);//卖几价,从1开始
	public Level2Bean getBuyLevel2Postion(int pos);//卖几价,从1开始
	public Level2Bean getSellFirst();//卖一价
	public Level2Bean getBuyFirst();//买一价
}
