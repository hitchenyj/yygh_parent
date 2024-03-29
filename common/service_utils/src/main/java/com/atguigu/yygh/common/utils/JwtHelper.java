package com.atguigu.yygh.common.utils;

import io.jsonwebtoken.*;
import org.springframework.util.StringUtils;

import java.util.Date;

public class JwtHelper {
    private static long tokenExpiration = 24*60*60*1000;
    private static String tokenSignKey = "123456"; //盐值 saltValue

    public static String createToken(Long userId, String userName) {
        String token = Jwts.builder()
                .setSubject("YYGH-USER") //设置主题
                .setExpiration(new Date(System.currentTimeMillis() + tokenExpiration)) //设置Token的过期时间，单位是毫秒
                .claim("userId", userId)
                .claim("userName", userName)
                .signWith(SignatureAlgorithm.HS512, tokenSignKey)
                .compressWith(CompressionCodecs.GZIP)
                .compact();
        return token;
    }
    public static Long getUserId(String token) {
        if(StringUtils.isEmpty(token)) return null;
        Jws<Claims> claimsJws = Jwts.parser().setSigningKey(tokenSignKey).parseClaimsJws(token);
        Claims claims = claimsJws.getBody();
        Integer userId = (Integer)claims.get("userId");
        return userId.longValue();
    }
    public static String getUserName(String token) {
        if(StringUtils.isEmpty(token)) return "";
        Jws<Claims> claimsJws
                = Jwts.parser().setSigningKey(tokenSignKey).parseClaimsJws(token);
        Claims claims = claimsJws.getBody();
        return (String)claims.get("userName");
    }
    
    public static void main(String[] args) {
        String token = JwtHelper.createToken(1L, "55");
        System.out.println(token);
        System.out.println(JwtHelper.getUserId("exJhbGciOiJIUzUxMiIsInppcCI6IkdaSVAifQ.H4sIAAAAAAAAAKtWKi5NUrJSiox099ANDXYNUtJRSq0oULIyNDM3MDcxMzAw0FEqLU4t8kwBikGYfom5qUAtpqZKtQBZ44uHPwAAAA.d5kdU-WPzV7-nGbGxxixg7VUa1-XWMFMpJsSWCowdmAZwP6EDApRYM1R8QY2dUPjOvBQSw5yshUIWvCzoLw8jA"));
        System.out.println(JwtHelper.getUserName(token));
    }
}