package com.minichat.user.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {
    @Size(max = 50, message = "昵称最长 50 个字符")
    private String nickname;
    private String avatar;
    @Size(max = 255, message = "签名最长 255 个字符")
    private String signature;
    private Integer gender;
}
