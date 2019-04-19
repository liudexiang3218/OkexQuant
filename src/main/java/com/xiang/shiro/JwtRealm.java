package com.xiang.shiro;

import java.util.Map;

import javax.annotation.Resource;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

import com.xiang.user.service.UserService;
import com.xiang.vo.UserVo;

/**
 * @author xiang
 * @createDate 2018年12月20日 上午11:56:45
 */
public class JwtRealm extends AuthorizingRealm {

	@Resource
	private UserService userService;
	/*
	 * 认证
	 */
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken principals) {
		 JWTToken jwtToken = (JWTToken) principals;
		 String token=jwtToken.getToken();
		 SimpleAuthenticationInfo authenticationInfo = new SimpleAuthenticationInfo(token, token, "jwtRealm");
	        return authenticationInfo;
	}

	@Override
	public boolean supports(AuthenticationToken token) {
		return token instanceof JWTToken;
	}

	/*
	 * 权限
	 */
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		String token = (String) super.getAvailablePrincipal(principals);
		Map<String, String> userMap = JWTAuth.verifyToken(token);
		String userName = userMap.get(JWTAuth.USERNAME);
		UserVo user = userService.login(userName);
		SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
		simpleAuthorizationInfo.addRole(user.getRole());
		return simpleAuthorizationInfo;
	}
}