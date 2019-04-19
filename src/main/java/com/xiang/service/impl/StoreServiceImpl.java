package com.xiang.service.impl;

import javax.annotation.PreDestroy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.okex.websocket.WebSocketBaseV3;
import com.okex.websocket.WebSocketService;

/**
* @author xiang
* @createDate 2018年12月26日 下午3:43:24
*/
@Service("storeService")
public class StoreServiceImpl implements WebSocketService{
	private Logger depthLogger = LogManager.getLogger("depth");
	@Override
	public void onReceive(Object obj) {
		// TODO Auto-generated method stub
		depthLogger.info(JSON.toJSONString(obj));
	}
	@PreDestroy
	public void destroy() {
		depthLogger.traceExit();
	}

}
