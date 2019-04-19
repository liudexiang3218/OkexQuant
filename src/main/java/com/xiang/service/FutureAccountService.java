package com.xiang.service;

import com.okex.websocket.FutureAccount;

public interface FutureAccountService {
	public FutureAccount getFutureAccount(String coin);
	public int getAvailableVolume(String coin,float price,int leverRate);
	public int getAvailableVolume(String coin,double availableMargin,float price,int leverRate);
	public double getAvailableMargin(String coin);
	
}
