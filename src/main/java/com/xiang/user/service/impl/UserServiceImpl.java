package com.xiang.user.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.xiang.po.User;
import com.xiang.shiro.JWTAuth;
import com.xiang.spring.APIException;
import com.xiang.spring.ErrorCodes;
import com.xiang.user.service.UserService;
import com.xiang.vo.UserVo;

/**
 * @author xiang
 * @createDate 2018年12月20日 下午2:18:51
 */
@Service("userService")
public class UserServiceImpl implements UserService {

	public User getQueryUser(String userName) {
		// 实现自己的用户逻辑
		if ("admin".equals(userName)) {
			User user = new User();
			user.setUserName("admin");
			user.setPassword("21232f297a57a5a743894a0e4a801fc3");
			user.setPermission("user");
			user.setRole("admin");
			return user;
		} else if ("test".equals(userName)) {
			User user = new User();
			user.setUserName("test");
			user.setPassword("21232f297a57a5a743894a0e4a801fc3");
			user.setPermission("user");
			user.setRole("user");
			return user;
		}
		throw new APIException(ErrorCodes.AUTH);
	}

	@Override
	public UserVo login(String userName, String password) {
		// TODO Auto-generated method stub
		String passwordSercet = DigestUtils.md5Hex(password);
		User user = getQueryUser(userName);
		if (!Objects.isNull(user)) {
			if (passwordSercet.equals(user.getPassword())) {
				return getUserVo(user);
			}
		}
		throw new APIException(ErrorCodes.AUTH);
	}

	public UserVo getUserVo(User user) {
		UserVo vo = new UserVo();
		BeanUtils.copyProperties(user, vo);
		Map<String, String> claims = new HashMap<String, String>();
		claims.put("userName", vo.getUserName());
		vo.setToken(JWTAuth.createToken(claims));
		return vo;
	}

	@Override
	public UserVo login(String userName) {
		return getUserVo(getQueryUser(userName));
	}
}
