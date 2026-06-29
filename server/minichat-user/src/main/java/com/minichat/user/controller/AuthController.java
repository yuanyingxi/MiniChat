package com.minichat.user.controller;

import com.minichat.user.dto.LoginRequest;
import com.minichat.user.dto.RegisterRequest;
import com.minichat.user.dto.SendSmsCodeRequest;
import com.minichat.user.service.AuthService;
import com.minichat.user.service.SmsService;
import com.minichat.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "用户认证")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private SmsService smsService;

    @PostMapping("/sms/code")
    @Operation(summary = "发送短信验证码")
    public Result<Void> sendSmsCode(@Valid @RequestBody SendSmsCodeRequest req) {
        smsService.sendCode(req.getPhone());
        return Result.success(null);
    }

    @PostMapping("/register")
    @Operation(summary = "注册")
    public Result<String> register(@Valid @RequestBody RegisterRequest req) {
        String token = authService.register(req);
        return Result.success(token);
    }

    @PostMapping("/login")
    @Operation(summary = "密码登录")
    public Result<String> login(@Valid @RequestBody LoginRequest req) {
        String token = authService.login(req);
        return Result.success(token);
    }

    @PostMapping("/logout")
    @Operation(summary = "退出登录")
    public Result<Void> logout(@RequestHeader("Authorization") String authHeader) {
        authService.logout(authHeader);
        return Result.success(null);
    }
}
