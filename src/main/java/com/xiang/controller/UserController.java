package com.xiang.controller;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.xiang.po.User;
import com.xiang.spring.APIException;
import com.xiang.spring.ErrorCodes;
import com.xiang.user.service.UserService;
import com.xiang.vo.UserVo;

/**
 * @author xiang
 *
 */
@CrossOrigin
@RestController
@RequestMapping(value = "/user")
public class UserController {
	@Resource
	private UserService userService;

	@RequestMapping(value = "/login")
	public UserVo login(@RequestParam("userName") @NotNull(message = "userName不能为空") String userName,
			@RequestParam("password") @NotNull(message = "password不能为空") String password) {
		User user = new User();
		user.setUserName(userName);
		user.setPassword(password);
		return login(user);
	}

	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public UserVo login(@RequestBody User user) {
		Subject subject = SecurityUtils.getSubject();
		UsernamePasswordToken token = new UsernamePasswordToken(user.getUserName(), user.getPassword());
		try {
			subject.login(token);
			if (subject.isAuthenticated()) {
				return (UserVo)subject.getPrincipal();
			}
		} catch (Exception ex) {
			throw new APIException(ErrorCodes.AUTH);
		}
		throw new APIException(ErrorCodes.AUTH);
	}
}
