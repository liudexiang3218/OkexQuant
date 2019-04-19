package com.xiang.spring;

import java.util.Date;

import org.apache.commons.codec.digest.DigestUtils;

import com.xiang.util.Constant;

/**
* @author xiang
* @createDate 2018年10月19日 上午10:51:15
*/
public class Response {
	public Response() {
		this.timestamp=new Date();
	}
	public Response( ErrorCodes errorCode) {
		this(null,errorCode);
	}
	public Response(Object data, ErrorCodes errorCode) {
		this.timestamp=new Date();
		this.data = data;
		this.errorCode = errorCode.getErrorCode();
		this.message = errorCode.getErrorMessage();
		if (this.errorCode == ErrorCodes.OK.getErrorCode()) {
			success = true;
		} else {
			success = false;
		}
	}
	private Object data;
	private int errorCode;
	private String message;
	private boolean success;
	private Date timestamp;
	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public String getSign() {
		return DigestUtils.md5Hex(timestamp.getTime()+Constant.SIGNKEY);
	}

	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}
	public int getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
}
