package com.xiang.shiro;

import java.util.Map;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.credential.CredentialsMatcher;

public class JWTCredentialsMatcher implements CredentialsMatcher{

	@Override
	public boolean doCredentialsMatch(AuthenticationToken authenticationToken, AuthenticationInfo authenticationInfo) {
		String token = (String) authenticationToken.getCredentials();
			Map<String, String> userMap = JWTAuth.verifyToken(token);
			if (userMap.containsKey(JWTAuth.USERNAME)) {
				return true;
			}
        return false;
	}

}
