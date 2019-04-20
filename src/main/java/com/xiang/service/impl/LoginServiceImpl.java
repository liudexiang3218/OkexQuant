package com.xiang.service.impl;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.xiang.service.WebSocketService;

/**
 * 接受ws推送的登录消息处理
 * @author xiang
 * @createDate 2018年12月26日 下午4:26:14
 */
@Service("loginService")
public class LoginServiceImpl implements WebSocketService {
	@Resource(name="webSoketClient")
	private WebSoketClient client;

	@Override
	public void onReceive(Object obj) {
		if (obj instanceof JSONObject) {
			JSONObject root = (JSONObject) obj;
			String event = root.getString("event");
			if ("login".equals(event)) {
				System.out.println(obj);
				boolean success = root.getBoolean("success");
				if (success) {
					client.setLogin(true);
					client.reLoginAddChannel();
				}
			} else if ("error".equals(event)) {
				System.out.println(obj);
				String errorCode = root.getString("errorCode");
				switch (errorCode) {
				case "30001":
				case "30002":
				case "30004":
				case "30006":
				case "30012":
				case "30013":
				case "30027":
				case "30041":
					client.setLogin(false);
					break;
				}
			}else if ("subscribe".equals(event)) {
				System.out.println(obj);
			}
		}
	}

}
