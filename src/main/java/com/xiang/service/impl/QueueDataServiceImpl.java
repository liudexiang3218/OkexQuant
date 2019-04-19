package com.xiang.service.impl;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.springframework.scheduling.annotation.EnableScheduling;

import com.xiang.service.BaseDataService;

/**
 * @author xiang
 * @createDate 2018年12月11日 下午2:49:31
 */
public abstract class QueueDataServiceImpl extends BaseDataServiceImpl {
	protected ConcurrentLinkedQueue<Object> dataQueue = new ConcurrentLinkedQueue<Object>();
	public boolean addData(Object obj) {
		return dataQueue.add(obj);
	}
}
