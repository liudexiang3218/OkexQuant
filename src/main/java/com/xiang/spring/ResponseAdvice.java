package com.xiang.spring;

import java.util.concurrent.TimeoutException;

import org.apache.shiro.authc.AuthenticationException;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * @author xiang
 * @createDate 2018年10月19日 上午10:41:01
 */
@ControllerAdvice
public class ResponseAdvice implements ResponseBodyAdvice<Object> {

	@Override
	public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
		// TODO Auto-generated method stub
		return com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter.class.isAssignableFrom(converterType);
	}

	@ExceptionHandler(APIException.class)
	@ResponseBody
	public Object exceptionHander(APIException exception) {
		return exception.getErr();
	}

	@ExceptionHandler(TimeoutException.class)
	@ResponseBody
	public Object exceptionHander(TimeoutException exception) {
		return ErrorCodes.TIME_OUT;
	}

	@ExceptionHandler(Exception.class)
	@ResponseBody
	public ErrorCodes exceptionHandler(Exception e) {
		e.printStackTrace();
		if (e instanceof APIException) { // 自定义异常,直接返回
			APIException APIException = (APIException) e;
			return APIException.getErr();
		} else if (e instanceof AuthenticationException) {// 认证错误是用户名，密码不正确
			return ErrorCodes.AUTH;
		}
		return ErrorCodes.ERROR; // 其他异常,返回"系统错误"
	}

	@Override
	public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
			Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
			ServerHttpResponse response) {
		if (body == null)
			return null;
		Response res = null;
		if (body instanceof ErrorCodes) {
			res = new Response(null, (ErrorCodes) body);
		} else {
			res = new Response(body, ErrorCodes.OK);
		}
		return res;
	}
}
