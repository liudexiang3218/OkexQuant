package com.xiang.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.okex.websocket.OkexConstant;
import com.xiang.service.FutureInstrumentService;
import com.xiang.service.Instrument;
import com.xiang.service.InstrumentService;
import com.xiang.service.SpotInstrumentService;

/**
 * 获取现货和期货合约ID服务类
 * @author Administrator
 *
 */
@Service("instrumentService")
public class InstrumentServiceImpl implements InstrumentService {
	@Autowired
	FutureInstrumentService futureInstrumentService;
	@Autowired
	SpotInstrumentService spotInstrumentService;

	@Override
	public Instrument getInstrument(String table, String instrumentId) {
		switch (table) {
		case OkexConstant.FUTURES_DEPTH5:
		case OkexConstant.FUTURES_DEPTH:
			Instrument instrument = futureInstrumentService.getFutureInstrument(instrumentId);
			if (instrument == null) {
				futureInstrumentService.refresh();
			}
			return instrument;
		case OkexConstant.SPOT_DEPTH5:
		case OkexConstant.SPOT_DEPTH:
			instrument = spotInstrumentService.getSpotInstrument(instrumentId);
			if (instrument == null) {
				futureInstrumentService.refresh();
			}
			return instrument;
		}
		return null;
	}

	@Override
	public Instrument getInstrument(String table, String coin, String contractType) {
		switch (table) {
		case OkexConstant.FUTURES_DEPTH5:
		case OkexConstant.FUTURES_DEPTH:
			Instrument instrument = futureInstrumentService.getFutureInstrument(coin,contractType);
			if (instrument == null) {
				System.out.println(coin+"   "+contractType);
				futureInstrumentService.refresh();
			}
			return instrument;
		case OkexConstant.SPOT_DEPTH5:
		case OkexConstant.SPOT_DEPTH:
			instrument = spotInstrumentService.getSpotInstrument(coin,"USDT");
			if (instrument == null) {
				futureInstrumentService.refresh();
			}
			return instrument;
		}
		return null;
	}

	@Override
	public List<String> getSubscribes() {
		return null;
	}

	@Override
	public Instrument getInstrument(String instrumentId) {
		Instrument instrument = futureInstrumentService.getFutureInstrument(instrumentId);
		if (instrument == null) {
			instrument=spotInstrumentService.getSpotInstrument(instrumentId);
		}
		return instrument;
	}
}
