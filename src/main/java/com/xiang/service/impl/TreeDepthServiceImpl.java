package com.xiang.service.impl;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.zip.CRC32;

import org.springframework.beans.BeanUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.okex.websocket.OkexConstant;
import com.xiang.service.DepthService;

/**
 * 使用二叉树实现委托数据排序
 * 
 * @author xiang
 *
 */
public class TreeDepthServiceImpl implements DepthService {
	/**
	 * 是否可用,当validateChecksum失败时为不可用
	 */
	private boolean validate = true;
	/**
	 * 从小到大
	 */
	private TreeMap<BigDecimal, Level2Bean> sellMap = new TreeMap<BigDecimal, Level2Bean>();// 从小到大
	/**
	 * 从大到小
	 */
	private TreeMap<BigDecimal, Level2Bean> buyMap = new TreeMap<BigDecimal, Level2Bean>(new Comparator<BigDecimal>() {
		@Override
		public int compare(BigDecimal o1, BigDecimal o2) {
			// TODO Auto-generated method stub
			return o2.compareTo(o1);
		}
	});// 从大到小

	public void processData(String table,String action, JSONObject data) {
		try {
			if (OkexConstant.PARTIAL_ACTION.equals(action))
			{
				processDataV3(table,true, data);
			}
				
			else {
				processDataV3(table,false, data);
			}
		}catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
			
	}

	public void processDataV3(String table,boolean init, JSONObject data) throws Exception {
		if (init) {
			sellMap.clear();
			buyMap.clear();
		}
		String instrumentId = data.getString(OkexConstant.INSTRUMENT_ID);
		if (data.containsKey(OkexConstant.ASKS)) {
			Object object = data.get(OkexConstant.ASKS);
			// 从小到大
			if (object instanceof JSONArray) {
				JSONArray asks = (JSONArray) object;
				Iterator it2 = asks.iterator();
				while (it2.hasNext()) {
					JSONArray record = (JSONArray) it2.next();
					BigDecimal price = record.getBigDecimal(0);
					String count = record.getString(1);
					Level2Bean level2 = new Level2Bean();
					level2.setTable(table);
					level2.setPrice(price);
					level2.setVolume(count);
					level2.setInstrumentId(instrumentId);
					if (init) {
						sellMap.put(price, level2);
					} else {
						if (level2.getDoubleVolume() == 0) {
							sellMap.remove(price);
						} else {
							sellMap.put(price, level2);
						}
					}
				}
			}
		}
		// 从大到小
		if (data.containsKey(OkexConstant.BIDS)) {
			Object object = data.get(OkexConstant.BIDS);
			if (object instanceof JSONArray) {
				JSONArray bids = (JSONArray) object;
				Iterator it2 = bids.iterator();
				while (it2.hasNext()) {
					JSONArray record = (JSONArray) it2.next();
					BigDecimal price = record.getBigDecimal(0);
					String count = record.getString(1);
					Level2Bean level2 = new Level2Bean();
					level2.setTable(table);
					level2.setPrice(price);
					level2.setVolume(count);
					level2.setInstrumentId(instrumentId);
					if (init) {
						buyMap.put(price, level2);
					} else {
						if (level2.getDoubleVolume() == 0) {
							buyMap.remove(price);
						} else {
							buyMap.put(price, level2);
						}
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
		return getSellLevel2Postion(pos, sellMap);
	}

	/*
	 * 获取买几价，买一价，买二价之类的
	 */
	@Override
	public Level2Bean getBuyLevel2Postion(int pos) {
		return getSellLevel2Postion(pos, buyMap);
	}

	private Level2Bean getSellLevel2Postion(int pos, TreeMap<BigDecimal, Level2Bean> map) {
		if (!validate)
			return null;
		Level2Bean item = null;
		double volume = 0;
		for (Entry<BigDecimal, Level2Bean> entry : map.entrySet()) {
			item = entry.getValue();
			volume += item.getDoubleVolume();
			pos--;
			if (pos <= 0)
				break;
		}
		if (item != null) {
			Level2Bean result = new Level2Bean();
			BeanUtils.copyProperties(item, result);
			result.setVolume(volume+"");
			return result;
		}
		return item;
	}

	@Override
	public Level2Bean getSellFirst() {
		if (!validate)
			return null;
		if (sellMap.firstEntry() != null)
			return sellMap.firstEntry().getValue();
		return null;
	}

	@Override
	public Level2Bean getBuyFirst() {
		if (!validate)
			return null;
		if (buyMap.firstEntry() != null)
			return buyMap.firstEntry().getValue();
		return null;
	}

	CRC32 crc32 = new CRC32();

	/*
	 * crc32 检验前25个价位
	 */
	public boolean validateChecksum(long checksum) {
		// TODO Auto-generated method stub
		int checkcount = 25;
		Level2Bean[] bidsTop25 = new Level2Bean[checkcount];
		Level2Bean[] asksTop25 = new Level2Bean[checkcount];
		int pos = 0;
		for (Entry<BigDecimal, Level2Bean> entry : sellMap.entrySet()) {
			asksTop25[pos] = entry.getValue();
			pos++;
			if (pos == checkcount)
				break;
		}
		pos = 0;
		for (Entry<BigDecimal, Level2Bean> entry : buyMap.entrySet()) {
			bidsTop25[pos] = entry.getValue();
			pos++;
			if (pos == checkcount)
				break;
		}
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < checkcount; i++) {
			Level2Bean bid = bidsTop25[i];
			Level2Bean ask = asksTop25[i];
			if (bid != null) {
				buffer.append(bid.getPrice());
				buffer.append(OkexConstant.COLON);
				buffer.append(bid.getVolume());
				buffer.append(OkexConstant.COLON);
			}
			if (ask != null) {
				buffer.append(ask.getPrice());
				buffer.append(OkexConstant.COLON);
				buffer.append(ask.getVolume());
				buffer.append(OkexConstant.COLON);
			}
		}
		String str = buffer.toString();
		str = str.substring(0, str.length() - 1);
		crc32.reset();
		crc32.update(str.getBytes());
		long result = crc32.getValue();
		checksum = checksum & 0xffffffffL;
		validate = checksum == result;
		return validate;
	}
}
