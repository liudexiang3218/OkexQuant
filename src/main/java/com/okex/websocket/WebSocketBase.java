package com.okex.websocket;

public interface WebSocketBase {
	public void setStatus(boolean status);
	public void reConnect();
	public void sentPing();
	public void stop();
	public void start();
}
