package com.minichat.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 内部服务间调用使用的用户信息 DTO（比对外接口返回更多字段）
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InternalUserResponse {
    private Long id;
    private String phone;
    private String nickname;
    private String avatar;
    private String signature;
    private Integer gender;
    private Integer status;              // 账号状态（内部使用）
    private LocalDateTime lastLoginTime; // 最后登录时间
    private String lastLoginIp;          // 最后登录 IP
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
