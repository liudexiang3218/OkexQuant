package com.xiang.shiro;

import javax.annotation.Resource;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.util.ObjectUtils;

import com.xiang.spring.APIException;
import com.xiang.spring.ErrorCodes;
import com.xiang.user.service.UserService;
import com.xiang.vo.UserVo;

/**
 * @author xiang
 * @createDate 2018年12月20日 上午11:56:45
 */
public class UserRealm extends AuthorizingRealm {

	@Resource
	private UserService userService;

	/*
	 * 认证
	 */
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken principals) {
		UsernamePasswordToken token = (UsernamePasswordToken) principals;
		String userName =token.getUsername();
		UserVo user = userService.login(userName);
		if (ObjectUtils.isEmpty(user)) {
			throw new APIException(ErrorCodes.AUTH);
		}
		return new SimpleAuthenticationInfo(user, user.getPassword(), "userRealm");
	}

	@Override
	public boolean supports(AuthenticationToken token) {
		return token instanceof UsernamePasswordToken;
	}

	/*
	 * 权限
	 */
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		String userName = (String) super.getAvailablePrincipal(principals);
		UserVo user = userService.login(userName);
		SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
		simpleAuthorizationInfo.addRole(user.getRole());
		return simpleAuthorizationInfo;
	}
}