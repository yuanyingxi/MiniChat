package com.minichat.user.service;

import com.aliyuncs.IAcsClient;
import com.aliyuncs.dypnsapi.model.v20170525.SendSmsVerifyCodeRequest;
import com.aliyuncs.dypnsapi.model.v20170525.SendSmsVerifyCodeResponse;
import com.minichat.common.constant.AuthConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;


import java.util.concurrent.TimeUnit;

@Service
public class SmsService {

    @Autowired
    private IAcsClient acsClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Value("${aliyun.sms.sign-name}")
    private String signName;

    @Value("${aliyun.sms.template-code}")
    private String templateCode;

    // 发送验证码
    public void sendCode(String phone) {
        // 60 秒内不能重复发验证码
        String lockKey = "sms:lock:" + phone;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(lockKey))) {
            throw new RuntimeException("请 60 秒后再试");
        }
        redisTemplate.opsForValue().set(lockKey, "1", 60, TimeUnit.SECONDS);

        // 生成 6 位随机码
        String code = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));

        // 存入 Redis，有效期 5 分钟
        redisTemplate.opsForValue().set(
                AuthConstant.SMS_REDIS_KEY_PREFIX + phone,
                code,
                AuthConstant.SMS_CODE_EXPIRE_MINUTES,
                TimeUnit.MINUTES
        );

        // 调用号码认证服务 SMS Verify API 发送验证码
        SendSmsVerifyCodeRequest request = new SendSmsVerifyCodeRequest();
        request.setPhoneNumber(phone);
        request.setSignName(signName);
        request.setTemplateCode(templateCode);
        // 模板内容: "您的验证码为${code}。...以上验证码${min}分钟内有效..."
        request.setTemplateParam("{\"code\":\"" + code + "\",\"min\":\"5\"}");
        request.setCodeLength(6L);   // 验证码长度 6 位
        request.setValidTime(300L);  // 有效期 300 秒（与 Redis 一致）
        request.setInterval(60L);    // 60 秒重发间隔

        try {
            SendSmsVerifyCodeResponse resp = acsClient.getAcsResponse(request);
            if (!"OK".equals(resp.getCode())) {
                throw new RuntimeException("短信发送失败: " + resp.getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException("短信发送异常", e);
        }
    }

    // 校验验证码（校验后自动删除，一次性使用）
    public boolean verifyCode(String phone, String code) {
        String key = AuthConstant.SMS_REDIS_KEY_PREFIX + phone;
        String saved = redisTemplate.opsForValue().get(key);    // 去 Redis 拿正确的答案
        if (saved != null && saved.equals(code)) {
            redisTemplate.delete(key);  // 用完即删
            return true;
        }
        return false;
    }

}
