package com.minichat.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SendSmsCodeRequest {
    // 在请求进入业务之前，Spring 框架会先检查传过来的参数的格式正不正确，
    // 正确就正常进入 Controller 内部访问服务，不正确直接当场拦截，直接抛出异常。
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
}
