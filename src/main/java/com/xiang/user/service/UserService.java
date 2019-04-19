package com.xiang.user.service;

import com.xiang.vo.UserVo;

/**
* @author xiang
* @createDate 2018年12月20日 下午2:18:00
*/
public interface UserService {
	public UserVo login(String userName,String password);
	public UserVo login(String userName);
}
