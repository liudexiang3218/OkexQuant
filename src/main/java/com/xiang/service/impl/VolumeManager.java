package com.xiang.service.impl;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.util.ObjectUtils;

/**
 * 合约库存管理 保存已被占用的合约张数
 * 
 * @author xiang
 * @createDate 2018年12月7日 下午2:56:01
 */
public class VolumeManager {
	private VolumeManager() {
	};

	public static VolumeManager getInstance() {
		return SingletonHolder.instance;
	}

	public void init(List<Hedging> list) {
		if (ObjectUtils.isEmpty(list)) {
			for (Hedging hedging : list) {
				getSetVolume(hedging.getHedgingConfig(), hedging.getAmount());
			}
		}
	}

	ConcurrentHashMap<String, Integer> usedVolumesMap = new ConcurrentHashMap<String, Integer>();

	public synchronized int getVolume(HedgingConfig config) {
		if (!usedVolumesMap.containsKey(config.getConfigId()))
			usedVolumesMap.put(config.getConfigId(), 0);
		int volume = config.getVolume() - usedVolumesMap.get(config.getConfigId());
		return volume;
	}

	/**
	 * 占用合约张数，如果要的合约张数超过库存，将占用失败返回0
	 * 
	 * @param config
	 * @param volume
	 * @return
	 */
	public synchronized int getSetVolume(HedgingConfig config, int volume) {
		if (volume < 0)
			return 0;
		int usedVolume = 0;
		if (usedVolumesMap.containsKey(config.getConfigId()))
			usedVolume = usedVolumesMap.get(config.getConfigId());
		int preVolume = volume + usedVolume;// 操作后预期合约张数
		if (preVolume > config.getVolume())
			return 0;
		usedVolumesMap.put(config.getConfigId(), preVolume);
		return volume;
	}

	/**
	 * 释放合约张数
	 * 
	 * @param config
	 * @param volume
	 * @return
	 */
	public synchronized void releaseVolume(HedgingConfig config, int volume) {
		int usedVolume = 0;
		if (usedVolumesMap.containsKey(config.getConfigId()))
			usedVolume = usedVolumesMap.get(config.getConfigId());
		int preVolume = usedVolume - volume;// 操作后预期合约张数
		if (preVolume < 0)
			preVolume = 0;
		usedVolumesMap.put(config.getConfigId(), preVolume);
	}

	private static class SingletonHolder {
		private static final VolumeManager instance = new VolumeManager();
	}
}
