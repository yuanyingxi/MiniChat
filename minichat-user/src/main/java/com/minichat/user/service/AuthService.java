package com.minichat.user.service;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.minichat.user.dto.LoginRequest;
import com.minichat.user.dto.RegisterRequest;
import com.minichat.user.entity.User;
import com.minichat.user.mapper.UserMapper;
import com.minichat.user.service.UserIndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
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

    @Autowired
    private UserIndexService userIndexService;

    @Autowired
    private StringRedisTemplate redisTemplate;

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

        // 4. 同步到 ES 索引（好友搜索用）
        userIndexService.indexUser(user);

        // 5. 返回 Token
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

    // 加入 Redis 黑名单
    public void logout(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;  // 没带 Token 也算退出成功
        }

        String token = authHeader.substring(7);  // 去掉 "Bearer " 前缀

        try {
            // 1. 获取 Token 剩余有效秒数
            long remainingTtl = jwtService.getRemainingTtl(token);

            if (remainingTtl > 0) {
                // 2. 把 Token 的哈希值加入 Redis 黑名单
                String tokenHash = DigestUtil.sha256Hex(token);
                redisTemplate.opsForValue().set(
                        "blacklist:token:" + tokenHash,
                        "1",
                        remainingTtl,
                        java.util.concurrent.TimeUnit.SECONDS
                );
            }
        } catch (Exception e) {
            // Token 不合法也视为退出成功
        }
    }

    private String passwordEncode(String raw) {
        return passwordEncoder.encode(raw);
    }

    private boolean passwordMatches(String raw, String encoded) {
        return passwordEncoder.matches(raw, encoded);
    }

}
