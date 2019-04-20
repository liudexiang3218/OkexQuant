package com.xiang.service.impl;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.xiang.service.WebSocketService;
import com.xiang.spring.LogExecuteTime;

/**
 * 接收ws推送的消息处理服务
 * @author xiang
 *
 */
@Service("msgCenterService")
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
		loginService.onReceive(obj);//处理登录与登出
		futureAccountService.onReceive(obj);//处理账户权益数据
		instrumentsDepthService.onReceive(obj);//处理深度委托数据s
		tradeService.onReceive(obj);//处理交易数据
		startHedgingService.onReceive(obj);//策略套利监控购买，如果需要实时实现结束策略套利监控，可以在这里开启finishHedgingService服务，目前结束策略套利监控只是使用200毫秒采集监控一次
		storeService.onReceive(obj);//数据存储服务
	}
}
