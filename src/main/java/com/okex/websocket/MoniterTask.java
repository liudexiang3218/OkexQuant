package com.okex.websocket;

import java.util.TimerTask;

public class MoniterTask extends TimerTask{

	private long startTime = System.currentTimeMillis();
	private int checkTime = 5000;
	private WebSocketBase client = null;

	public void updateTime() {
		startTime = System.currentTimeMillis();
	}

	public MoniterTask(WebSocketBase client) {
		this.client = client;
	}

	public void run() {
		long deadTime = System.currentTimeMillis() - startTime;
		if (deadTime > checkTime) {
			System.out.println("timeout " + (System.currentTimeMillis() - startTime));
			client.setStatus(false);
			client.reConnect();
		}
		client.sentPing();
	}
}
