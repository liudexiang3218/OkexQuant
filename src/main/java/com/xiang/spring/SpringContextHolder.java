package com.xiang.spring;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component
public class SpringContextHolder implements ApplicationContextAware {
	public static ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		SpringContextHolder.applicationContext = applicationContext;
		System.out.println("setApplicationContext" + " " + getActiveProfile());
	}

	public static String getActiveProfile() {
		String[] activeProfiles = applicationContext.getEnvironment().getActiveProfiles();
		if (ObjectUtils.isEmpty(activeProfiles)) {
			activeProfiles = applicationContext.getEnvironment().getDefaultProfiles();
		}
		return activeProfiles[0];
	}
}
