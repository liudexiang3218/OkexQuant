package com.xiang.service;

import java.util.List;

import com.okex.websocket.FutureInstrument;

/**
* @author xiang
* @createDate 2018年12月27日 上午11:00:48
*/
public interface FutureInstrumentService {
	public FutureInstrument getFutureInstrument(String instrumentId);
	public FutureInstrument getFutureInstrument(String coin,String contractType);
	public List<String> getSubscribes();
	public void refresh();
}
