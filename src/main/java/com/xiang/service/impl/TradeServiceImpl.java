package com.xiang.service.impl;

import java.util.Iterator;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.okex.websocket.FutureOrder;
import com.okex.websocket.WebSocketService;

/**
* @author xiang
* @createDate 2018年12月26日 下午3:48:38
*/
@Service("tradeService")
public class TradeServiceImpl implements WebSocketService{
	TradeManager tradeManager=TradeManager.getInstance();
	@Override
	public void onReceive(Object obj) {
		if (obj instanceof JSONObject) {
			JSONObject root = (JSONObject) obj;
			if (root.containsKey("table")) {
				String table = root.getString("table");
				if ("futures/order".equals(table)) {
					if (root.containsKey("data")) {
						System.out.println(obj);
						JSONArray data = root.getJSONArray("data");
						Iterator it = data.iterator();
						while (it.hasNext()) {
							Object order = it.next();
							if (order instanceof JSONObject) {
								JSONObject orderJSON = (JSONObject) order;
								FutureOrder futuresOrder=JSON.toJavaObject(orderJSON, FutureOrder.class);
								tradeManager.updateFuturesOrder(futuresOrder);
							}
						}
					}
				}
			}
		}
	}

}
