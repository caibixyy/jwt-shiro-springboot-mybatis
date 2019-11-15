package com.xyy.controller;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xyy.config.exception.UnauthorizedException;
import com.xyy.config.redis.RedisUtil;
import com.xyy.mapper.UserMapper;
import com.xyy.pojo.User;
import com.xyy.config.jwt.JwtUtil;
import com.xyy.util.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.TimeoutUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class TestController {

    private static final Logger LOGGER = LogManager.getLogger(TestController.class);

    @Autowired
    private UserMapper userMapper;

    @PostMapping("/login")
    public Response login(@RequestParam("username") String username,
                          @RequestParam("password") String password,
                          HttpServletResponse httpServletResponse) throws Exception{
        QueryWrapper<User> queryWrapper = new QueryWrapper();
        queryWrapper
                .eq("username",username)
                .eq("password",password);
        User user = userMapper.selectOne(queryWrapper);
        if (user.getPassword().equals(password)) {
            return new Response(200, "登陆成功", "token为："+ JwtUtil.createToken(user,JwtUtil.SECRET,30));
        } else {
            throw new UnauthorizedException();
        }
    }

    @RequestMapping("test")
    public Response hehe(HttpServletRequest h){
        return new Response(200,h.getHeader("Authorization"),null);
    }

    @GetMapping("/article")
    public Response article() {
        Subject subject = SecurityUtils.getSubject();
        System.out.println(subject.getPrincipal().toString());
        if (subject.isAuthenticated()) {
            return new Response(200, "You are already logged in", null);
        } else {
            return new Response(200, "You are guest", null);
        }
    }

    @GetMapping("/require_auth")
    @RequiresAuthentication
    public Response requireAuth() {
        return new Response(200, "You are authenticated", null);
    }

    @GetMapping("/require_role")
    @RequiresRoles("admin")
    public Response requireRole() {
        return new Response(200, "You are visiting require_role", null);
    }

    @GetMapping("/require_permission")
    @RequiresPermissions(logical = Logical.AND, value = {"view", "edit"})
    public Response requirePermission() {
        return new Response(200, "You are visiting permission require edit,view", null);
    }

    @RequestMapping(path = "/401")
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Response unauthorized() {
        return new Response(401, "Unauthorized", null);
    }
}
