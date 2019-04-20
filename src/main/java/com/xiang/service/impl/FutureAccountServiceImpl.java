package com.xiang.service.impl;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import com.okcoin.commons.okex.open.api.service.futures.FuturesTradeAPIService;
import com.okcoin.commons.okex.open.api.service.futures.impl.FuturesTradeAPIServiceImpl;
import com.okex.websocket.FutureAccount;
import com.xiang.service.FutureAccountService;
import com.xiang.service.WebSocketService;

/**
 * 合约的账户权益
 * @author xiang
 * @createDate 2018年12月26日 下午4:26:14
 */
@Service("futureAccountService")
@EnableRetry
@EnableScheduling
public class FutureAccountServiceImpl implements WebSocketService, FutureAccountService {
	FuturesTradeAPIService futurePostV3;
	@Autowired
	private SystemConfig systemConfig;
	@Autowired
	CoinServiceImpl coinService;
	private String[] coins;
	private Map<String, FutureAccount> accounts = new ConcurrentHashMap<>();

	@PostConstruct
	public void init() {
		futurePostV3 = new FuturesTradeAPIServiceImpl(systemConfig);
		if (!Strings.isNullOrEmpty(systemConfig.getCoins())) {
			coins = systemConfig.getCoins().split(",");
		}
		if (!ObjectUtils.isEmpty(coins)) {
			for (String coin : coins) {
//				getFutureAccount(coin);
			}
		}
	}
	@Scheduled(cron = "0 0 14 ? * *") // 每天执行一次
	public void refresh() {
		System.out.println("refresh FutureAccount");
		if (!Strings.isNullOrEmpty(systemConfig.getCoins())) {
			coins = systemConfig.getCoins().split(",");
		}
		if (!ObjectUtils.isEmpty(coins)) {
			for (String coin : coins) {
				getFutureAccount(coin, false);
			}
		}
	}

	@Retryable(value = { TimeoutException.class }, maxAttempts = 3, backoff = @Backoff(delay = 200l)) // timeout异常使用重试机制
	public FutureAccount getRestFutureAccount(String coin) {
		try {
			JSONObject tokenJSON = futurePostV3.getAccountsByCurrency(coin.toLowerCase());
			FutureAccount futureAccount = JSON.toJavaObject(tokenJSON, FutureAccount.class);
			return futureAccount;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	@Override
	public void onReceive(Object obj) {
		if (obj instanceof JSONObject) {
			JSONObject root = (JSONObject) obj;
			if (root.containsKey("table")) {
				String table = root.getString("table");
				if ("futures/account".equals(table)) {
					System.out.println(root);
					if (root.containsKey("data")) {
						JSONArray data = root.getJSONArray("data");

						Iterator it = data.iterator();
						while (it.hasNext()) {
							Object account = it.next();
							if (account instanceof JSONObject) {
								JSONObject accountJSON = (JSONObject) account;
								if (!ObjectUtils.isEmpty(coins)) {
									for (String coin : coins) {
										String token = coin.toUpperCase();
										if (accountJSON.containsKey(token)) {
											JSONObject tokenJSON = accountJSON.getJSONObject(token);
											FutureAccount futureAccount = JSON.toJavaObject(tokenJSON,
													FutureAccount.class);
											if (futureAccount != null)
												accounts.put(token, futureAccount);
										}
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
	public FutureAccount getFutureAccount(String coin) {
		return getFutureAccount(coin, true);
	}

	private FutureAccount getFutureAccount(String coin, boolean cache) {
		if (!accounts.containsKey(coin.toUpperCase()) || !cache) {
			FutureAccount futureAccount = getRestFutureAccount(coin);
			if (futureAccount != null)
				accounts.put(coin.toUpperCase(), futureAccount);
		}
		return accounts.get(coin.toUpperCase());
	}

	/*
	 * 可以使用保证金
	 */
	public double getAvailableMargin(String coin) {
		FutureAccount futureAccount = getFutureAccount(coin);
		if (futureAccount != null) {
			return futureAccount.getEquity() - futureAccount.getMargin();
		}
		return 0;
	}

	/*
	 * 可开张数
	 */
	@Override
	public int getAvailableVolume(String coin, float price, int leverRate) {
		FutureAccount futureAccount = getFutureAccount(coin);
		if (futureAccount != null) {
			double availCoin = futureAccount.getEquity() - futureAccount.getMargin();
			return (int) (availCoin * price * leverRate / coinService.getUnitAmount(coin));
		}
		return 0;
	}
	@Override
	public int getAvailableVolume(String coin,double availableMargin, float price, int leverRate) {
		// TODO Auto-generated method stub
		return (int) (availableMargin * price * leverRate / coinService.getUnitAmount(coin));
	}

}
