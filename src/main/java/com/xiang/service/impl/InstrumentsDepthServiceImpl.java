package com.xiang.service.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xiang.service.DepthService;
import com.xiang.service.InstrumentsDepthService;
import com.xiang.service.WebSocketService;

/**
 * 全量200档深度数据，目前暂无使用
 * @author xiang
 * @createDate 2018年12月26日 下午2:17:28
 */
@Service("instrumentsDepthService2")
public class InstrumentsDepthServiceImpl implements WebSocketService,InstrumentsDepthService {
	Map<String, DepthService> depthServices = new HashMap<>();
	@Resource(name="webSoketClient")
	private WebSoketClient client;
	@Override
	public void onReceive(Object obj) {
		if (obj instanceof JSONObject) {
			JSONObject root = (JSONObject) obj;
			if (root.containsKey("table")) {
				String table = root.getString("table");
				if ("futures/depth".equals(table) || "spot/depth".equals(table)) {
					String action = root.getString("action");
					if (root.containsKey("data")) {
						JSONArray data = root.getJSONArray("data");
						Iterator it = data.iterator();
						while (it.hasNext()) {
							Object instrument = it.next();
							if (instrument instanceof JSONObject) {
								JSONObject instrumentJSON = (JSONObject) instrument;
								String instrumentId = instrumentJSON.getString("instrument_id");
								DepthService depthService = depthServices.get(instrumentId);
								if (depthService == null) {
									depthService = new TreeDepthServiceImpl();
									depthServices.put(instrumentId, depthService);
								}
								depthService.processData(table,action, (JSONObject) instrument);
								long checksum = instrumentJSON.getLong("checksum");
								if (!depthService.validateChecksum(checksum)) {
									System.out.println("checksum false " + action);
									if(client!=null)
									{
										client.removeChannel(table + ":" + instrumentId);
										client.addChannel("subscribe", new String[] { table + ":" + instrumentId });
									}else
									{
										System.out.println("client is null reChannel fail ");
									}
								}
							}
						}
					}
				}
			}
		}
	}
	@Override
	public Level2Bean getSellLevel2Postion(String instrumentId, int pos) {
		// TODO Auto-generated method stub
		DepthService depthService = depthServices.get(instrumentId);
		if(depthService!=null)
			return depthService.getSellLevel2Postion(pos);
		return null;
	}
	@Override
	public Level2Bean getBuyLevel2Postion(String instrumentId, int pos) {
		DepthService depthService = depthServices.get(instrumentId);
		if(depthService!=null)
			return depthService.getBuyLevel2Postion(pos);
		return null;
	}
	@Override
	public Level2Bean getSellFirst(String instrumentId) {
		DepthService depthService = depthServices.get(instrumentId);
		if(depthService!=null)
			return depthService.getSellFirst();
		return null;
	}
	@Override
	public Level2Bean getBuyFirst(String instrumentId) {
		DepthService depthService = depthServices.get(instrumentId);
		if(depthService!=null)
			return depthService.getBuyFirst();
		return null;
	}

}
