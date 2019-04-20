package com.xiang.service.impl;

import java.util.LinkedList;
import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

/**
 * 套利策略管理
 * 
 * @author xiang
 * @createDate 2018年12月7日 下午2:56:01
 */
public class HedgingConfigManager {
	private HedgingConfigManager() {};

	public static HedgingConfigManager getInstance() {
		return SingletonHolder.instance;
	}

	/**
	 * @param coin
	 *            BTC,ETC...
	 * @param type
	 *            tn,tq,nq
	 * @return
	 */
	public List<HedgingConfig> getConfigs(String coin, String type) {
		List<HedgingConfig> result = new LinkedList<HedgingConfig>();
		CacheManager manager = CacheManager.getInstance();
		Cache cache = manager.getCache("hedgingConfigCache");
		List list = cache.getKeys();
		if (list != null) {
			for (Object key : list) {
				Element e = cache.get(key);
				HedgingConfig hc = (HedgingConfig) e.getObjectValue();
				if (hc.getCoin().equalsIgnoreCase(coin) && hc.getType().equalsIgnoreCase(type))
					result.add(hc);
			}
		}
		return result;
	}


	public HedgingConfig getHedgingConfig(String configId) {

		CacheManager manager = CacheManager.getInstance();
		Cache cache = manager.getCache("hedgingConfigCache");
		Element e = cache.get(configId);
		if (e != null) {
			return (HedgingConfig) e.getObjectValue();
		}
		return null;
	}


	public void save(HedgingConfig config) {
		update(config);
	}

	public void delete(String configId) {
		CacheManager manager = CacheManager.getInstance();
		Cache cache = manager.getCache("hedgingConfigCache");
		cache.remove(configId);
		cache.flush();
	}

	public void update(HedgingConfig config) {
		CacheManager manager = CacheManager.getInstance();
		Cache cache = manager.getCache("hedgingConfigCache");
		Element e = new Element(config.getConfigId(), config, true);
		cache.put(e);
		cache.flush();
	}

	private static class SingletonHolder {
		private static final HedgingConfigManager instance = new HedgingConfigManager();
	}
}
