package com.xiang.spring;

/**
 * API Exception
 *
 * @author xiang
 * @version 1.0.0
 * @date 2018/11/22 19:59
 */
public class APIException extends RuntimeException {

	private ErrorCodes err;

	public APIException(ErrorCodes err) {
		super(err.getErrorMessage());
		this.err = err;
	}

	public ErrorCodes getErr() {
		return err;
	}

	public void setErr(ErrorCodes err) {
		this.err = err;
	}
}
