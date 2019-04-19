package com.xiang.spring;

import java.math.BigDecimal;
import java.math.BigInteger;

import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.ToStringSerializer;
import com.alibaba.fastjson.support.config.FastJsonConfig;

/**
 * @author xiang 解决js long 精度丢失问题，统一大数类型使用string输出
 */
public class FastJsonConfigExt extends FastJsonConfig {
	public FastJsonConfigExt() {
		super();
		SerializeConfig serializeConfig = SerializeConfig.globalInstance;
		serializeConfig.put(BigInteger.class, ToStringSerializer.instance);
		serializeConfig.put(BigDecimal.class, ToStringSerializer.instance);
		serializeConfig.put(Long.class, ToStringSerializer.instance);
		serializeConfig.put(Long.TYPE, ToStringSerializer.instance);
		this.setSerializeConfig(serializeConfig);
	}

}
