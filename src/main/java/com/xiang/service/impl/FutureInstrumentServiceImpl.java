package com.xiang.service.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.google.common.base.Strings;
import com.okcoin.commons.okex.open.api.bean.futures.result.Instruments;
import com.okcoin.commons.okex.open.api.service.futures.FuturesMarketAPIService;
import com.okcoin.commons.okex.open.api.service.futures.impl.FuturesMarketAPIServiceImpl;
import com.okex.websocket.FutureInstrument;
import com.xiang.service.FutureInstrumentService;

/**
 * 虚拟币合约ID列表
 * @author xiang
 * @createDate 2018年12月27日 上午10:01:22
 */
@Service("futureInstrumentService")
@EnableScheduling
public class FutureInstrumentServiceImpl
		implements FutureInstrumentService{
	private FuturesMarketAPIService futuresMarketV3;
	private Map<String, FutureInstrument> cacheInstruments = new HashMap<>();
	private Map<String, FutureInstrument> cachePeriodInstruments = new HashMap<>();
	@Autowired
	private WebSoketClient client;
	@Autowired
	private SystemConfig systemConfig;
	private List<Instruments> getInstruments()
	{
		List<Instruments> instruments = futuresMarketV3.getInstruments();
		return instruments;
	}
	@Scheduled(cron = "0/3 0-30 16 ? * FRI") // 每个星期五下午16点0分到30分，每隔3秒刷新一次
	@Cacheable(value = "instrumentCache", sync=true,key = "#root.methodName")
	public void refresh() {
		SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		// 将列表根据币种进行排序，币种的第一个合约当作当周，第二个合约当作次周，第三个合约当作季度，其他合约忽略
		List<Instruments> instruments = getInstruments();
		String currentDate = format.format(cal.getTime());
		cal.add(Calendar.DATE, 7);
		String next7Date = format.format(cal.getTime());
		cal.add(Calendar.DATE, 7);
		String next14Date = format.format(cal.getTime());
		System.out.println(currentDate + " " + next7Date + "  " + next14Date);
		if (instruments != null) {
			for (Instruments instrument : instruments) {
				FutureInstrument futureInstrument = new FutureInstrument();
				BeanUtils.copyProperties(instrument, futureInstrument);
				try {
					futureInstrument
							.setDeliveryTime(dateformat.parse(futureInstrument.getDelivery() + " 16:00:00").getTime());
				} catch (ParseException e) {
				}
				if (currentDate.compareTo(futureInstrument.getDelivery()) > 0)// 已过期
				{
					continue;
				}
				if (currentDate.compareTo(futureInstrument.getDelivery()) == 0 && hour>15)// 已过期
				{
					continue;
				}

				if (next7Date.compareTo(futureInstrument.getDelivery()) > 0
						|| (hour > 15 && next7Date.compareTo(futureInstrument.getDelivery()) == 0))
					futureInstrument.setContractType("this_week");
				else if (next14Date.compareTo(futureInstrument.getDelivery()) > 0
						|| (hour > 15 && next14Date.compareTo(futureInstrument.getDelivery()) == 0))
					futureInstrument.setContractType("next_week");
				else
					futureInstrument.setContractType("quarter");
				System.out.println(futureInstrument.getInstrument_id() + " " + futureInstrument.getDelivery() + " "
						+ futureInstrument.getUnderlying_index() + "  " + futureInstrument.getContractType());
				cacheInstruments.put(futureInstrument.getInstrument_id(), futureInstrument);
				String key = periodKey(futureInstrument.getUnderlying_index(), futureInstrument.getContractType());
				cachePeriodInstruments.put(key, futureInstrument);
			}
		}
		List<String> subscribes = getSubscribes();
		client.tryAddChannel("subscribe", subscribes);
	}

	private String periodKey(String coin, String contractType) {
		return coin.toUpperCase() + "-" + contractType;
	}

	/**
	 * 
	 * @param coin   (BTC,ETC..)
	 * @param period (this_week,next_week,quarter)
	 * @return
	 */
	public FutureInstrument getFutureInstrument(String coin, String contractType) {
	
		String key = periodKey(coin, contractType);
//		if (!cachePeriodInstruments.containsKey(key))
//		{
//			refresh();
//		}
			
		return cachePeriodInstruments.get(key);
	}

	public List<String> getSubscribes() {
		String[] coins = null;
		if (!Strings.isNullOrEmpty(systemConfig.getCoins())) {
			coins = systemConfig.getCoins().split(",");
		}
		return getSubscribes(coins);
	}

	private List<String> getSubscribes(String[] coins) {
		List<String> subscribes = new LinkedList<String>();
		for (String coin : coins) {
			FutureInstrument instrument = getFutureInstrument(coin, "this_week");
			if (instrument != null) {
				subscribes.add("futures/depth5:" + instrument.getInstrument_id());
				subscribes.add("futures/order:" + instrument.getInstrument_id());
				//subscribes.add("futures/trade:" + instrument.getInstrument_id());
			}
			instrument = getFutureInstrument(coin, "next_week");
			if (instrument != null) {
				subscribes.add("futures/depth5:" + instrument.getInstrument_id());
				subscribes.add("futures/order:" + instrument.getInstrument_id());
				//subscribes.add("futures/trade:" + instrument.getInstrument_id());
			}
			instrument = getFutureInstrument(coin, "quarter");
			if (instrument != null) {
				subscribes.add("futures/depth5:" + instrument.getInstrument_id());
				subscribes.add("futures/order:" + instrument.getInstrument_id());
				//subscribes.add("futures/trade:" + instrument.getInstrument_id());
			}
		}
		return subscribes;
	}

	@PostConstruct
	private void init() {
		futuresMarketV3 = new FuturesMarketAPIServiceImpl(systemConfig);

	}

	@Override
	public FutureInstrument getFutureInstrument(String instrumentId) {
//		// TODO Auto-generated method stub
//		if (!cacheInstruments.containsKey(instrumentId))
//			refresh();
		return cacheInstruments.get(instrumentId);
	}
}
