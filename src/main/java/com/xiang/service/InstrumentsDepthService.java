package com.xiang.service;

import com.xiang.service.impl.Level2Bean;

/**
* @author xiang
* @createDate 2018年11月27日 上午9:51:01
*/
public interface InstrumentsDepthService{
	public Level2Bean getSellLevel2Postion(String instrumentId,int pos);//卖几价,从1开始
	public Level2Bean getBuyLevel2Postion(String instrumentId,int pos);//卖几价,从1开始
	public Level2Bean getSellFirst(String instrumentId);//卖一价
	public Level2Bean getBuyFirst(String instrumentId);//买一价
}
