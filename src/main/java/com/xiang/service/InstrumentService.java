package com.xiang.service;

import java.util.List;

/**
* @author xiang
* @createDate 2018年12月27日 上午11:00:48
*/
public interface InstrumentService {
	public Instrument getInstrument(String instrumentId);
	public Instrument getInstrument(String table,String instrumentId);
	public Instrument getInstrument(String table,String coin,String contractType);
	public List<String> getSubscribes();
}
