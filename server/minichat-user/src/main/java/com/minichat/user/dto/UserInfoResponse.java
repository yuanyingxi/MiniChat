package com.minichat.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoResponse {
    private Long id;
    private String phone;
    private String nickname;
    private String avatar;
    private String signature;
    private Integer gender;
    private LocalDateTime createTime;
}
