package com.xiang.service;

import com.alibaba.fastjson.JSONObject;

/**
* @author xiang
* @createDate 2018年12月24日 下午5:19:08
*/
public interface DepthService extends Level2Service{
	public void processData(String table,String action,JSONObject data);
	public boolean validateChecksum(long checksum);
}
