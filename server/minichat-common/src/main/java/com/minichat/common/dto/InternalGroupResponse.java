package com.minichat.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 内部服务: 访问群信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InternalGroupResponse {
    private Long userId;
    private String nickname;
    private String avatar;
}
