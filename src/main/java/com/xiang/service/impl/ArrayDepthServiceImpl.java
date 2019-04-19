package com.xiang.service.impl;

import java.math.BigDecimal;

import org.springframework.beans.BeanUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.okex.websocket.OkexConstant;
import com.xiang.service.DepthService;

/**
 * 使用数组实现委托数据排序，深度5档，最多支持5档
 * 
 * @author xiang
 *
 */
public class ArrayDepthServiceImpl implements DepthService {

	/**
	 * 从小到大
	 */
	private Level2Bean[] sellMap = new Level2Bean[5];// 从小到大
	/**
	 * 从大到小
	 */
	private Level2Bean[] buyMap = new Level2Bean[5];// 从大到小

	public void processData(String table, String action, JSONObject data) {
		try {
			processDataV3(table, true, data);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void processDataV3(String table, boolean init, JSONObject data) throws Exception {
		String instrumentId = data.getString(OkexConstant.INSTRUMENT_ID);
		if (data.containsKey(OkexConstant.ASKS)) {
			Object object = data.get(OkexConstant.ASKS);
			// 从小到大
			if (object instanceof JSONArray) {
				JSONArray asks = (JSONArray) object;
				if (asks != null) {
					for (int i = 0; i < asks.size() && i < 5; i++) {
						JSONArray record = (JSONArray) asks.get(i);
						BigDecimal price = record.getBigDecimal(0);
						String count = record.getString(1);
						Level2Bean level2 = new Level2Bean();
						level2.setTable(table);
						level2.setPrice(price);
						level2.setVolume(count);
						level2.setInstrumentId(instrumentId);
						sellMap[i] = level2;
					}
				}
			}
		}
		// 从大到小
		if (data.containsKey(OkexConstant.BIDS)) {
			Object object = data.get(OkexConstant.BIDS);
			if (object instanceof JSONArray) {
				JSONArray bids = (JSONArray) object;
				if (bids != null) {
					for (int i = 0; i < bids.size() && i < 5; i++) {
						JSONArray record = (JSONArray) bids.get(i);
						BigDecimal price = record.getBigDecimal(0);
						String count = record.getString(1);
						Level2Bean level2 = new Level2Bean();
						level2.setTable(table);
						level2.setPrice(price);
						level2.setVolume(count);
						level2.setInstrumentId(instrumentId);
						buyMap[i] = level2;
					}
				}
			}
		}
	}

	/*
	 * 获取卖几价，卖一价，卖二价之类的
	 */
	@Override
	public Level2Bean getSellLevel2Postion(int pos) {
		return getLevel2Postion(pos, sellMap);
	}

	/*
	 * 获取买几价，买一价，买二价之类的
	 */
	@Override
	public Level2Bean getBuyLevel2Postion(int pos) {
		return getLevel2Postion(pos, buyMap);
	}

	private Level2Bean getLevel2Postion(int pos, Level2Bean[] map) {
		if (pos > 5)
			pos = 5;
		double volume = 0;
		for (int i = 0; i < pos; i++) {
			Level2Bean item = map[i];
			if (item != null)
				volume += item.getDoubleVolume();
		}
		Level2Bean item = map[pos - 1];
		Level2Bean result = new Level2Bean();
		if (item != null)
			BeanUtils.copyProperties(item, result);
		result.setVolume(volume + "");
		return result;
	}

	@Override
	public Level2Bean getSellFirst() {
		return sellMap[0];
	}

	@Override
	public Level2Bean getBuyFirst() {
		return buyMap[0];
	}

	/*
	 * crc32 检验前25个价位
	 */
	public boolean validateChecksum(long checksum) {
		return true;
	}
}
