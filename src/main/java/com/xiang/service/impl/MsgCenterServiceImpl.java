package com.xiang.service.impl;

import javax.annotation.Resource;

import com.okex.websocket.WebSocketService;
import com.xiang.spring.LogExecuteTime;

public class MsgCenterServiceImpl implements WebSocketService {
	
	@Resource(name="loginService")
	private WebSocketService loginService;
	
	@Resource(name="tradeService")
	private WebSocketService tradeService;
	
	@Resource(name="instrumentsDepthService")
	private WebSocketService instrumentsDepthService;
	
	@Resource(name="storeService")
	private WebSocketService storeService;
	
	@Resource(name="startHedgingService")
	private WebSocketService startHedgingService;
	
	@Resource(name="futureAccountService")
	private WebSocketService futureAccountService;
	@LogExecuteTime
	@Override
	public void onReceive(Object obj) {
		loginService.onReceive(obj);
		futureAccountService.onReceive(obj);
		instrumentsDepthService.onReceive(obj);
		tradeService.onReceive(obj);
		startHedgingService.onReceive(obj);
		storeService.onReceive(obj);
	}
}
