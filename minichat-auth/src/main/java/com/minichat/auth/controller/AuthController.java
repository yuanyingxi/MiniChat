package com.minichat.auth.controller;

import com.minichat.auth.dto.LoginRequest;
import com.minichat.auth.dto.RegisterRequest;
import com.minichat.auth.dto.SendSmsCodeRequest;
import com.minichat.auth.service.AuthService;
import com.minichat.auth.service.SmsService;
import com.minichat.common.result.Result;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private SmsService smsService;

    // POST 发送验证码
    @PostMapping("/sms/code")
    public Result<Void> sendSmsCode(@Valid @RequestBody SendSmsCodeRequest req) {
        smsService.sendCode(req.getPhone());
        return Result.success(null);
    }

    // 注册
    @PostMapping("/register")
    public Result<String> register(@Valid @RequestBody RegisterRequest req) {
        String token = authService.register(req);
        return Result.success(token);
    }

    // 登录
    @PostMapping("/login")
    public Result<String> login(@Valid @RequestBody LoginRequest req) {
        String toekn = authService.login(req);
        return Result.success(toekn);
    }

}
