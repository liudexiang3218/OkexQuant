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
import com.okcoin.commons.okex.open.api.bean.spot.result.Product;
import com.okex.websocket.FutureInstrument;
import com.xiang.service.FutureInstrumentService;
import com.xiang.service.HedgingDataService;
import com.xiang.service.InstrumentsDepthService;
import com.xiang.service.SpotInstrumentService;

/**
 * @author xiang
 * @createDate 2018年12月11日 下午2:49:31
 */
@Service("hedgingDataService")
@EnableScheduling
public class HedgingDataServiceImpl extends BaseDataServiceImpl implements HedgingDataService {
	@Autowired
	private AccountConfig accountConfig;
	@Resource(name = "instrumentsDepthService")
	InstrumentsDepthService instrumentsDepthService;
	@Autowired
	private FutureInstrumentService futureInstrumentService;
	@Autowired
	private SpotInstrumentService spotInstrumentService;
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
			Product instrument = spotInstrumentService.getSpotInstrument(coin, "USDT");
			FutureInstrument thisInstrument = futureInstrumentService.getFutureInstrument(coin, "this_week");
			FutureInstrument nextInstrument = futureInstrumentService.getFutureInstrument(coin, "next_week");
			FutureInstrument quarterInstrument = futureInstrumentService.getFutureInstrument(coin, "quarter");
			if(thisInstrument==null || nextInstrument==null || quarterInstrument==null)
			{
				futureInstrumentService.refresh();
			}
			// 生成处理图表图表数据
			// 现货与当周
			if (instrument != null && thisInstrument != null) {
				processData(hc, "tt", instrument.getInstrument_id(), thisInstrument.getInstrument_id());
			}
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

	/**
	 * @param profitRate %后的结果，由于页面设置使用的是%后的数值，所以这里必须转换成正常的数值
	 * @return 正常数值，非%后的结果
	 */
	private double getPs(float profitRate) {
		profitRate = profitRate / 100f;
		return (1 + accountConfig.getSellFeeRate() + profitRate) / (1 - accountConfig.getSellFeeRate());// 利润 profitRate
																										// 加上成本 开多
	}

	/**
	 * @param profitRate %后的结果，由于页面设置使用的是%后的数值，所以这里必须转换成正常的数值
	 * @return 正常数值，非%后的结果
	 */
	private double getPb(float profitRate) {
		profitRate = profitRate / 100f;
		return (1 - accountConfig.getBuyFeeRate() - profitRate) / (1 + accountConfig.getBuyFeeRate());// 利润 profitRate
																										// 加上成本 开空
	}

	public void processData(HedgingContext hedgingContext, String type, String thisInstrumentId,
			String nextInstrumentId) {
		double Ps = getPs(0.1f);// 千分之一的盈利率
		double Pb = getPb(0.1f);// 千分之一的盈利率
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
		map.put("time", System.currentTimeMillis() + "");
		map.put("s_b_v", sell_buy_value * 100f);
		map.put("b_s_v", buy_sell_value * 100f);

		// 开多 回本价=买入价*(1+手续费率)/(1-手续费率)
		// 开空 回本价=卖出价*(1-手续费率)/(1+手续费率)
		// 近卖远买的回本值(Ps-Pb(Pbsv+1))/ Pb(Pbsv+1),Ps=(1+手续费率)/(1-手续费率)，Pb=(1-手续费率)/(1+手续费率)
		// %
		double s_b_l = (Ps - (Pb * (buy_sell_value + 1))) / (Pb * (buy_sell_value + 1));
		map.put("s_b_l", s_b_l * 100f);

		// 近买远卖的回本值 ( Ps-Pb(Psbv+1) )/ Pb(Psbv+1)
		double b_s_l = (Ps - (Pb * (sell_buy_value + 1))) / (Pb * (sell_buy_value + 1));
		map.put("b_s_l", b_s_l * 100f);
		hedgingContext.addHedgingData(map);
		send(type + "_" + hedgingContext.getCoin(), map);
	}

	/**
	 * @param hedgingtype tn_btc,tq_btc,nq_btc.....
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
