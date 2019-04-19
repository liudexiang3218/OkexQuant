package com.xiang.service.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.google.common.base.Strings;
import com.okcoin.commons.okex.open.api.bean.spot.result.Product;
import com.okcoin.commons.okex.open.api.service.spot.SpotProductAPIService;
import com.okcoin.commons.okex.open.api.service.spot.impl.SpotProductAPIServiceImpl;
import com.okex.websocket.SpotInstrument;
import com.xiang.service.SpotInstrumentService;

/**
 * @author xiang
 * @createDate 2018年12月27日 上午10:01:22
 */
@Service("spotInstrumentService")
public class SpotInstrumentServiceImpl implements SpotInstrumentService {
	private SpotProductAPIService spotMarketV3;
	private Map<String, SpotInstrument> cacheInstruments = new ConcurrentHashMap<>();
	@Autowired
	private SystemConfig systemConfig;
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
			subscribes.add("spot/depth5:" + coin.toUpperCase() + "-USDT");
			subscribes.add("spot/order:" + coin.toUpperCase() + "-USDT");
		}
		return subscribes;
	}

	@PostConstruct
	private void init() {
		spotMarketV3 = new SpotProductAPIServiceImpl(systemConfig);
		SpotInstrument instrument=new SpotInstrument();
		instrument.setInstrument_id("BTC-USDT");
		instrument.setBase_currency("BTC");
		instrument.setQuote_currency("USDT");
		instrument.setContractType("this");
		cacheInstruments.put(instrument.getInstrument_id(), instrument);
	}
	private List<Product> getProducts()
	{
		System.out.println("getProducts");
		List<Product> list = spotMarketV3.getProducts();
		return list;
	}
	@Override
	public SpotInstrument getSpotInstrument(String instrumentId) {
		if (cacheInstruments.containsKey(instrumentId)) {
			return cacheInstruments.get(instrumentId);
		}
		List<Product> list = getProducts();
		if (!ObjectUtils.isEmpty(list)) {
			for (Product product : list) {
				SpotInstrument instrument=new SpotInstrument();
				BeanUtils.copyProperties(product, instrument);
				instrument.setContractType("this");
				cacheInstruments.put(product.getInstrument_id(), instrument);
			}
		}
		return cacheInstruments.get(instrumentId);
	}
	@Override
	public SpotInstrument getSpotInstrument(String base, String quote) {
		return getSpotInstrument(base.toUpperCase() + "-"+quote.toUpperCase());
	}
}
