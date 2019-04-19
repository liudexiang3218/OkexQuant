package com.xiang.shiro;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.impl.PublicClaims;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.xiang.controller.XAuthToken;
import com.xiang.spring.APIException;
import com.xiang.spring.ErrorCodes;

/**
 * @author xiang
 * @createDate 2018年12月13日 上午9:38:01
 */
public class JWTAuth {
	private static final String SECRET = "<MQLMNQNQJQK sdfXX#$%()(#*!()!KL<>kjsdrow38854545fdf>?N<:{LWPW";
	private static final String ISSUER = "com.auth0";
	public static final String USERNAME="username";
	public static final String TOKENHEADER="xauthtoken";
	/**
	 * token过期时间，默认7天，单位天
	 */
	public static int DEFAULT_EXPIRE_DAY = 7;
	/**
	 * 间隔1小时刷新一次token，单位小时
	 */
	public static long DEFAULT_REFRESH_TOKEN = 1;

	/**
	 * 生成token
	 *
	 * @param claims
	 * @return
	 */
	public static String createToken(Map<String, String> claims) throws APIException {
		try {
			Algorithm algorithm = Algorithm.HMAC256(SECRET);
			JWTCreator.Builder builder = JWT.create().withIssuer(ISSUER)
					.withExpiresAt(DateUtils.addDays(new Date(), DEFAULT_EXPIRE_DAY));
			claims.forEach(builder::withClaim);
			return builder.sign(algorithm);
		} catch (IllegalArgumentException e) {
			throw new APIException(ErrorCodes.createErrorCode("生成token失败"));
		}
	}
	public static String refreshToken(String token) throws APIException {
		return refreshToken(verifyToken(token));
	}
	public static String refreshToken(Map<String, String> map) throws APIException {
		String exp = map.get(PublicClaims.EXPIRES_AT);
		if (exp != null) {
			long passTime = DateUtils.addDays(new Date(), DEFAULT_EXPIRE_DAY).getTime() - Long.parseLong(exp) * 1000l;
			if (passTime >= DEFAULT_REFRESH_TOKEN * 3600000) {
				map.remove(PublicClaims.EXPIRES_AT);
				return createToken(map);
			}
		}
		return null;
	}

	/**
	 * 验证jwt，并返回数据
	 */
	public static Map<String, String> verifyToken(String token) throws APIException {
		Algorithm algorithm;
		Map<String, Claim> map;
		try {
			algorithm = Algorithm.HMAC256(SECRET);
			JWTVerifier verifier = JWT.require(algorithm).withIssuer(ISSUER).build();
			DecodedJWT jwt = verifier.verify(token);
			map = jwt.getClaims();
		} catch (Exception e) {
			e.printStackTrace();
			throw new APIException(ErrorCodes.LOGIN);
		}
		Map<String, String> resultMap = new HashMap<>(map.size());
		for (String name : map.keySet()) {
			switch (name) {
			case PublicClaims.ISSUER:
				break;
			case PublicClaims.SUBJECT:
				break;
			case PublicClaims.EXPIRES_AT:
				if (map.get(name) != null) {
					Long exp = map.get(name).asLong();
					if (exp != null)
						resultMap.put(name, exp.toString());
				}
				break;
			case PublicClaims.NOT_BEFORE:
				break;
			case PublicClaims.ISSUED_AT:
				break;
			case PublicClaims.JWT_ID:
				break;
			case PublicClaims.AUDIENCE:
				break;
			default:
				resultMap.put(name, map.get(name).asString());
			}
		}
		return resultMap;
	}
	public static XAuthToken getXAuthToken(Map<String, String> claims)
	{
		XAuthToken xAuthToken=new XAuthToken();
		xAuthToken.setToken(claims.get(TOKENHEADER));
		xAuthToken.setUserName(claims.get(USERNAME));
		return xAuthToken;
	}
}
