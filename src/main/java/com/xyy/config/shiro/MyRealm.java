package com.xyy.config.shiro;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.xyy.config.jwt.JwtToken;
import com.xyy.config.jwt.JwtUtil;
import com.xyy.mapper.UserMapper;
import com.xyy.pojo.User;
import org.apache.catalina.security.SecurityUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Service
public class MyRealm extends AuthorizingRealm {

    private static final Logger LOGGER = LogManager.getLogger(MyRealm.class);

    @Autowired
    private UserMapper userMapper;

    /**
     * 大坑！，必须重写此方法，不然Shiro会报错
     */
    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JwtToken;
    }

    /**
     * 只有当需要检测用户权限的时候才会调用此方法，例如checkRole,checkPermission之类的
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        try{
            User user = JwtUtil.getUserFromToken(principals.toString(),JwtUtil.SECRET);
            QueryWrapper queryWrapper = new QueryWrapper();
            queryWrapper.eq("username",user.getUsername());
            queryWrapper.eq("password",user.getPassword());
            User user1 = userMapper.selectOne(queryWrapper);
            SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
            simpleAuthorizationInfo.addRole(user1.getRole());
            Set<String> permission = new HashSet<>(Arrays.asList(user1.getPermission().split("\\,")));
            simpleAuthorizationInfo.addStringPermissions(permission);
            return simpleAuthorizationInfo;
        }catch (Exception e){
            e.getMessage();
        }
        return null;
    }

    /**
     * 默认使用此方法进行用户名正确与否验证，错误抛出异常即可。
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken auth) throws AuthenticationException {
        String token = (String) auth.getCredentials();
        // 解密获得username，用于和数据库进行对比
        try{
            User user = JwtUtil.getUserFromToken(token,JwtUtil.SECRET);
            if (user == null) {
                throw new AuthenticationException("非法token");
            }
            QueryWrapper queryWrapper = new QueryWrapper();
            queryWrapper.eq("username",user.getUsername());
            queryWrapper.eq("password",user.getPassword());
            User user1 = userMapper.selectOne(queryWrapper);
            if (user1 == null) {
                throw new AuthenticationException("User didn't existed!");
            }
            if (! JwtUtil.verify(token)) {
                throw new AuthenticationException("Username or password error");
            }
            return new SimpleAuthenticationInfo(token, token, "my_realm");
        }catch (Exception e){
            e.getMessage();
        }
        return null;
    }
}
