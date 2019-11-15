package com.xyy.config.jwt;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xyy.pojo.User;
import io.jsonwebtoken.*;
import org.joda.time.DateTime;
import org.omg.CORBA.PUBLIC_MEMBER;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * JWT工具类
 */
@Component
public class JwtUtil {

    /**
     * 过期时间 24 小时
     */
    private static final long EXPIRE_TIME = 60 * 24 * 60 * 1000;
    /**
     * 密钥
     */
    public static final String SECRET = "base64EncodedSecretKey";

    /**
     * 根据用户名和密码生成token
     */
    public static String createToken(User user,String base64EncodedSecretKey,int expireMinutes) throws Exception{
       return Jwts.builder()
                .claim("username",user.getUsername())
                .claim("password",user.getPassword())
                .setExpiration(DateTime.now().plusMinutes(expireMinutes).toDate())
                .signWith(SignatureAlgorithm.HS256, SECRET)
                .compact();
    }

    /**
     *
     * @param token
     * @param publicKey
     * @return
     * @throws Exception
     */
    private static Jws<Claims> parserToken(String token,String publicKey) throws Exception {
        return Jwts.parser().setSigningKey(publicKey)
                .parseClaimsJws(token);
    }

    /**
     * 获取token中的用户信息
     * @param token     用户请求中的令牌
     * @param publicKey 公钥
     * @return 用户信息
     * @throws Exception
     */
    public static User getUserFromToken(String token, String publicKey) throws Exception {
        Jws<Claims> claimsJws = parserToken(token, publicKey);
        Claims body = claimsJws.getBody();
        return new User(body.get("username").toString(),body.get("password").toString());
    }

    /**
     * 验证token是否正确
     * @param token
     * @return
     * @throws JsonProcessingException
     */
    public static boolean verify(String token) throws JsonProcessingException {
       String sign = null;
       try{
           System.out.println("校验token"+token);
           //获取签名
           sign = token.split("\\.")[2];
           //获取载荷
           Map<String,Object> JwtClaims = Jwts
                                            .parser()
                                           .setSigningKey(SECRET)
                                           .parseClaimsJws(token)
                                            .getBody();
           ObjectMapper mapper = new ObjectMapper();
           //将载荷转化为字符串
           String payload = mapper.writeValueAsString(JwtClaims);
            //重新生成签名
           JwtBuilder builder = Jwts.builder()
                   .setPayload(payload)
                   .signWith(SignatureAlgorithm.HS256,SECRET);
           String newSign = builder.compact().split("\\.")[2];
           if (!sign.equals(newSign)){
               return false;
           }
       }catch (ExpiredJwtException e){
           e.getMessage();
           System.out.println("token过期");
       }catch (SignatureException  |MalformedJwtException m){
           m.getMessage();
           return false;
       }
        System.out.println("token校验正确");
       return true;
    }
}
