package com.xiang.service.impl;

import java.util.LinkedList;
import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

/**
 * 合约库存管理 保存已被占用的合约张数
 * 
 * @author xiang
 * @createDate 2018年12月7日 下午2:56:01
 */
public class HedgingConfigManager {
	private HedgingConfigManager() {
	};

	public static HedgingConfigManager getInstance() {
		return SingletonHolder.instance;
	}

	// private Map<String, LinkedList<HedgingConfig>> configsMap = new
	// ConcurrentHashMap<String, LinkedList<HedgingConfig>>();

	// private String getKey(String coin, String type) {
	// return type + "_" + coin.toUpperCase();
	// }

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
		//
		// String key = getKey(coin, type);
		// if (!configsMap.containsKey(key)) {
		// configsMap.put(key, new LinkedList<HedgingConfig>());
		// }
		// return configsMap.get(key);
	}

//	public void removeHedgingConfig(String configId) {
//		delete(configId);
//		Iterator<LinkedList<HedgingConfig>> it = configsMap.values().iterator();
//		while (it.hasNext()) {
//			LinkedList<HedgingConfig> configs = it.next();
//			if (configs != null) {
//				Iterator<HedgingConfig> listIt = configs.iterator();
//				while (listIt.hasNext()) {
//					HedgingConfig config = listIt.next();
//					if (configId.equals(config.getConfigId())) {
//						it.remove();
//						return;
//					}
//				}
//			}
//		}
//	}

	public HedgingConfig getHedgingConfig(String configId) {

		CacheManager manager = CacheManager.getInstance();
		Cache cache = manager.getCache("hedgingConfigCache");
		Element e = cache.get(configId);
		if (e != null) {
			return (HedgingConfig) e.getObjectValue();
		}
		// Iterator<LinkedList<HedgingConfig>> it = configsMap.values().iterator();
		// while (it.hasNext()) {
		// LinkedList<HedgingConfig> configs = it.next();
		// if (configs != null) {
		// Iterator<HedgingConfig> listIt = configs.iterator();
		// while (listIt.hasNext()) {
		// HedgingConfig config = listIt.next();
		// if (configId.equals(config.getConfigId()))
		// return config;
		// }
		// }
		// }
		return null;
	}

//	public void addHedgingConfig(HedgingConfig config) {
//		// TODO Auto-generated method stub
//		LinkedList<HedgingConfig> configs = getConfigs(config.getCoin(), config.getType());
//		if (configs == null) {
//			configs = new LinkedList<HedgingConfig>();
//			String key = getKey(config.getCoin(), config.getType());
//			configsMap.put(key, configs);
//		}
//		configs.add(config);
//	}

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
