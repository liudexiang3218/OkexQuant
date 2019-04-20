package com.xiang.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.command.ActiveMQDestination;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import com.okex.websocket.FutureInstrument;
import com.xiang.service.FutureInstrumentService;
import com.xiang.service.HedgingDataService;
import com.xiang.service.InstrumentsDepthService;

/**
 * 数据采集服务，3秒采集一次价差数据，推送到MQ
 * @author xiang
 * @createDate 2018年12月11日 下午2:49:31
 */
@Service("hedgingDataService")
@EnableScheduling
public class HedgingDataServiceImpl extends BaseDataServiceImpl implements HedgingDataService {
	@Resource(name = "instrumentsDepthService")
	InstrumentsDepthService instrumentsDepthService;
	@Autowired
	private FutureInstrumentService futureInstrumentService;
	@Autowired
	private SystemConfig systemConfig;
	@Resource
	private JmsTemplate jmsTemplate;
	@Resource(name = "destinations")
	private Destination destinations;
	private Map<String, ActiveMQDestination> destinationsMap = new ConcurrentHashMap<String, ActiveMQDestination>();

	@Scheduled(fixedDelay = 3000)
	private void process() {
		String[] coins = null;
		if (!Strings.isNullOrEmpty(systemConfig.getCoins())) {
			coins = systemConfig.getCoins().split(",");
		}
		for (String coin : coins) {
			HedgingContext hc = getHedgingContext(coin);
			FutureInstrument thisInstrument = futureInstrumentService.getFutureInstrument(coin, "this_week");
			FutureInstrument nextInstrument = futureInstrumentService.getFutureInstrument(coin, "next_week");
			FutureInstrument quarterInstrument = futureInstrumentService.getFutureInstrument(coin, "quarter");
			if(thisInstrument==null || nextInstrument==null || quarterInstrument==null)
			{
				futureInstrumentService.refresh();
			}
			// 生成处理图表图表数据
			// 当周与次周
			if (thisInstrument != null && nextInstrument != null) {
				processData(hc, "tn", thisInstrument.getInstrument_id(), nextInstrument.getInstrument_id());
			}
			// 当周与季度
			if (thisInstrument != null && quarterInstrument != null) {
				processData(hc, "tq", thisInstrument.getInstrument_id(), quarterInstrument.getInstrument_id());
			}
			// 次周与季度
			if (nextInstrument != null && quarterInstrument != null) {
				processData(hc, "nq", nextInstrument.getInstrument_id(), quarterInstrument.getInstrument_id());
			}
		}
	}

	public void processData(HedgingContext hedgingContext, String type, String thisInstrumentId,
			String nextInstrumentId) {
		float sell_buy_value = 0f;
		Level2Bean level2Buy = instrumentsDepthService.getBuyFirst(thisInstrumentId);
		Level2Bean level2Sell = instrumentsDepthService.getSellFirst(nextInstrumentId);
		if (level2Buy != null && level2Sell != null) {
			// 开仓策略 近期开空，远期开多
			sell_buy_value = (level2Buy.getFloatPrice() - level2Sell.getFloatPrice()) / level2Sell.getFloatPrice();
		}
		float buy_sell_value = 0f;
		level2Buy = instrumentsDepthService.getBuyFirst(nextInstrumentId);
		level2Sell = instrumentsDepthService.getSellFirst(thisInstrumentId);
		if (level2Buy != null && level2Sell != null) {
			// 开仓策略 近期开多，远期开空
			buy_sell_value = (level2Buy.getFloatPrice() - level2Sell.getFloatPrice()) / level2Sell.getFloatPrice();
		}
		Map<String, Object> map = new HashMap<String, Object>();// 装的是%之后的结果
		map.put("type", type);
		map.put("time", System.currentTimeMillis()+"");
		map.put("s_b_v", sell_buy_value * 100f);
		map.put("b_s_v", buy_sell_value * 100f);
		hedgingContext.addHedgingData(map);
		send(type + "_" + hedgingContext.getCoin(), map);
	}

	/**
	 * @param hedgingtype 队列名： tn_btc（现周与次周价差）,tq_btc（现周与季度价差）,nq_btc.....（次周与季度价差）  
	 * @param map
	 */
	public void send(String hedgingtype, Map<String, Object> map) {
		ActiveMQDestination destination = destinationsMap.get(hedgingtype);
		if (destination == null) {
			ActiveMQDestination activeMQDestinations = (ActiveMQDestination) destinations;
			destination = activeMQDestinations.createDestination(hedgingtype);
			destinationsMap.put(hedgingtype, destination);
		}
		jmsTemplate.send(destination, new MessageCreator() {
			@Override
			public Message createMessage(Session session) throws JMSException {
				TextMessage textMessage = session.createTextMessage();
				String msg = JSONObject.toJSONString(map);
				textMessage.setText(msg);
				return textMessage;
			}
		});
	}
}
