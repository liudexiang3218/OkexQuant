package com.xiang.service.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.google.common.base.Strings;
import com.okex.websocket.WebSoketClient;
import com.xiang.service.FutureInstrumentService;
import com.xiang.service.HedgingService;
import com.xiang.service.SpotInstrumentService;
import com.xiang.service.TradeApiService;
import com.xiang.spring.SpringContextHolder;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

/**
 * @author xiang
 * @createDate 2018年11月5日 上午9:50:59
 */
@Service("hedgingService")
@EnableRetry
public class HedgingServiceImpl implements HedgingService, ApplicationListener<ContextRefreshedEvent> {
	@Autowired
	private WebSoketClient client;
	@Autowired
	private SystemConfig systemConfig;
	@Autowired
	private FutureInstrumentService futureInstrumentService;
	@Autowired
	private SpotInstrumentService spotInstrumentService;
	@Autowired
	private TradeApiService tradeApiService;
	private HedgingManager hedgingManager = HedgingManager.getInstance();

	@Override
	public void removeHedgingConfig(String configId) {
		HedgingConfigManager.getInstance().delete(configId);
	}

	@Override
	public List<Hedging> getHedgings() {
		return HedgingManager.getInstance().getHedgings();
	}

	@Override
	public List<HedgingConfig> getConfigs(String coin, String type) {
		// TODO Auto-generated method stub
		return HedgingConfigManager.getInstance().getConfigs(coin, type);
	}

	@Override
	public HedgingConfig getHedgingConfig(String configId) {
		return HedgingConfigManager.getInstance().getHedgingConfig(configId);
	}

	@Override
	public void addHedgingConfig(HedgingConfig config) {
		// TODO Auto-generated method stub
		HedgingConfigManager.getInstance().save(config);
	}

	@PostConstruct
	private void init() {
		System.out.println("start client");
		client.start();
	}

	@PreDestroy
	public void destroy() {
		System.out.println("destroy");
		if (hedgingManager.getHedgings().size() > 0) {
			EhCacheCacheManager cacheCacheManager = SpringContextHolder.applicationContext
					.getBean(EhCacheCacheManager.class);
			Cache cache = cacheCacheManager.getCacheManager().getCache("hedgingsCache");
			cache.put(new Element("hedgings", hedgingManager.getHedgings()));
			cache.flush();
			System.out.println("save hedgings " + hedgingManager.getHedgings().size());
		}
		client.stop();
	}

	@Override
	public void liquidHedging(String hedgingId) {
		HedgingManager.getInstance().liquidHedging(hedgingId);
	}

	/**
	 * 修复对冲（修复实际已成交，但未收到订单信息的对冲交易）
	 * 
	 * @param trade
	 * @throws TimeoutException
	 */
	@Override
	public Hedging repairHedging(Hedging hedging) throws TimeoutException {
		if (hedging.getStatus() != 1) {
			tradeApiService.repairHedgingTrade(hedging.getBuyTrade());
			tradeApiService.repairHedgingTrade(hedging.getSellTrade());
			for (Hedging reversehedging : hedging.getReverseHedgings()) {
				repairHedging(reversehedging);
			}
		}
		return hedging;
	}

	@Override
	public Hedging repairHedging(String hedgingId) throws TimeoutException {
		for (Hedging hedging : hedgingManager.getHedgings()) {
			if (hedgingId.equals(hedging.getHedgingId())) {
				return repairHedging(hedging);
			}
		}
		return null;
	}

	@Override
	public void liquidAllHedging() {
		for (Hedging hedging : hedgingManager.getHedgings()) {
			if (hedging.getStatus() != 1)
				hedging.setLiquid(true);
		}
	}

	@Override
	public HedgingConfig newHedgingConfig(String coin, String type) {
		// TODO Auto-generated method stub
		HedgingConfig hedgingConfig = new HedgingConfig();
		hedgingConfig.setCoin(coin);
		hedgingConfig.setType(type);
		return hedgingConfig;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		System.out.println("onApplicationEvent");
		if (event.getApplicationContext().getParent() == null) {
			futureInstrumentService.refresh();
			List<String> subscribes = futureInstrumentService.getSubscribes();
			if (subscribes == null) {
				subscribes = new LinkedList<String>();
			}
			List<String> spots = spotInstrumentService.getSubscribes();
			if (!ObjectUtils.isEmpty(spots))
				subscribes.addAll(spots);
			String[] coins = null;
			if (!Strings.isNullOrEmpty(systemConfig.getCoins())) {
				coins = systemConfig.getCoins().split(",");
			}
			if (coins != null)
				for (String coin : coins) {
					subscribes.add("futures/account:" + coin.toUpperCase());
				}
			client.addChannel("subscribe", subscribes);

			System.out.println("started");
			
			EhCacheCacheManager cacheCacheManager = SpringContextHolder.applicationContext
					.getBean(EhCacheCacheManager.class);
			Cache cache = cacheCacheManager.getCacheManager().getCache("hedgingsCache");
			Element element = cache.get("hedgings");
			if (element != null) {
				List<Hedging> hedgings = (List<Hedging>) element.getObjectValue();
				if (!ObjectUtils.isEmpty(hedgings)) {
					hedgingManager.initHedgings(hedgings);
					VolumeManager.getInstance().init(hedgings);
					System.out.println("init hedgings  " + hedgings.size());
				}
			}
		}
	}
}
