package com.xiang.spring;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.xiang.controller.XAuthToken;
import com.xiang.shiro.JWTAuth;

public class LoginTokenArgumentResolver implements HandlerMethodArgumentResolver {
	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		if (parameter.getParameterAnnotation(LoginToken.class) != null
				&& parameter.getParameterType() == XAuthToken.class) {
			return true;
		}
		return false;
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		HttpServletRequest httpServletRequest = (HttpServletRequest) webRequest.getNativeRequest();
		String token = httpServletRequest.getHeader(JWTAuth.TOKENHEADER);
		Map<String, String> claims =JWTAuth.verifyToken(token);
		XAuthToken xAuthToken=JWTAuth.getXAuthToken(claims);
		xAuthToken.setToken(token);
		return xAuthToken;
	}
}
