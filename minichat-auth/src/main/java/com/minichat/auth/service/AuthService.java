package com.minichat.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.minichat.auth.dto.LoginRequest;
import com.minichat.auth.dto.RegisterRequest;
import com.minichat.auth.entity.User;
import com.minichat.auth.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SmsService smsService;

    @Autowired
    private JwtService jwtService;

    // 注册
    public String register(RegisterRequest req) {
        // 1. 校验手机号是否已注册
        User existing = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getPhone, req.getPhone())
        );
        if (existing != null) {
            throw new RuntimeException("该手机号已注册");
        }

        // 2. 校验短信验证码
        if (!smsService.verifyCode(req.getPhone(), req.getSmsCode())) {
            throw new RuntimeException("验证码错误或已过期");
        }

        // 3. 创建用户
        User user = new User();
        user.setPhone(req.getPhone());
        user.setPasswordHash(passwordEncode(req.getPassword()));
        user.setNickname("用户" + req.getPhone().substring(7));  // 默认昵称
        user.setCreateTime(LocalDateTime.now());

        userMapper.insert(user);

        // 4. 返回 Token
        return jwtService.createToken(user.getId());
    }

    // 登录
    public String login(LoginRequest req) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getPhone, req.getPhone())
        );
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (!passwordMatches(req.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("密码错误");
        }
        return jwtService.createToken(user.getId());
    }

    private String passwordEncode(String raw) {
        return passwordEncoder.encode(raw);
    }

    private boolean passwordMatches(String raw, String encoded) {
        return passwordEncoder.matches(raw, encoded);
    }

}
