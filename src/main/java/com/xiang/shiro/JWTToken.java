package com.xiang.shiro;

import org.apache.shiro.authc.HostAuthenticationToken;

/**
 * @author xiang
 * @createDate 2018年12月20日 下午2:41:25
 */
public class JWTToken implements HostAuthenticationToken {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String token;
    private String host;

	public JWTToken(String token) {
		 this(token, null);
	}
	public JWTToken(String token, String host) {
        this.token = token;
        this.host = host;
    }
	public String getToken(){
        return this.token;
    }
	/* @return username
	 */
	@Override
	public Object getPrincipal() {
		return token;
	}

	/* @return password
	 */
	@Override
	public Object getCredentials() {
		return token;
	}

	@Override
	public String getHost() {
		return host;
	}
}
