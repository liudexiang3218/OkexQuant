package com.xiang.spring;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * @author xiang
 * @createDate 2018年12月5日 上午9:57:36
 */
@Aspect
@Component
@Profile("!test")
public class LogTimeInterceptor {
	@Pointcut("@annotation(com.xiang.spring.LogExecuteTime)")
	public void logTimeMethodPointcut() {

	}

	@Around("logTimeMethodPointcut()")
	public Object interceptor(ProceedingJoinPoint pjp) {
		long startTime = System.currentTimeMillis();
		Object result = null;
		try {
			result = pjp.proceed();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
		long time=System.currentTimeMillis() - startTime;
		if(time>50)
		System.out.println(pjp.getSignature().getDeclaringTypeName() + "." + pjp.getSignature().getName() + " spend "
				+ time + "ms");
		
		return result;
	}
}
