package com.xiang.shiro;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.springframework.http.HttpStatus;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;

public class JwtAuthFilter extends AuthenticatingFilter {

	@Override
	protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) throws Exception {
		HttpServletRequest httpServletRequest = (HttpServletRequest) request;
		String token = httpServletRequest.getHeader(JWTAuth.TOKENHEADER);
		if (!ObjectUtils.isEmpty(token)) {
			return new JWTToken(token);
		}
		return null;
	}
	@Override
	public boolean onPreHandle(ServletRequest request, ServletResponse response, Object mappedValue) throws Exception {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		if (httpRequest.getMethod().equals(RequestMethod.OPTIONS.name())) {
			return true;
		}
		return super.onPreHandle(request, response, mappedValue);
	}

	@Override
	protected AuthenticationToken createToken(String username, String password, ServletRequest request,
			ServletResponse response) {
		// TODO Auto-generated method stub
		HttpServletRequest httpServletRequest = (HttpServletRequest) request;
		String token = httpServletRequest.getHeader(JWTAuth.TOKENHEADER);
		if (!ObjectUtils.isEmpty(token)) {
			return new JWTToken(token);
		}
		return null;
	}

	@Override
	protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
		Subject subject = SecurityUtils.getSubject();
		try {
			subject.login(createToken(request, response));
		} catch (Exception ex) {
			return false;
		}
		return super.isAccessAllowed(request, response, mappedValue);
	}

	@Override
	protected void postHandle(ServletRequest request, ServletResponse response) throws Exception {
		HttpServletRequest httpServletRequest = (HttpServletRequest) request;
		if (httpServletRequest != null) {
			String token = httpServletRequest.getHeader(JWTAuth.TOKENHEADER);
			if (!StringUtils.isEmpty(token)) {
				try {
					token = JWTAuth.refreshToken(token);
					if (!StringUtils.isEmpty(token)) {
						HttpServletResponse httpServletResponse = (HttpServletResponse) response;
						httpServletResponse.setHeader(JWTAuth.TOKENHEADER, token);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		super.postHandle(request, response);
	}

	@Override
	protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
		HttpServletResponse httpServletResponse = (HttpServletResponse) response;
		httpServletResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
		return false;
	}

}
