package com.minichat.user.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SendFriendRequest {
    @NotNull(message = "请求ID不能为空")
    private Long toId;
    private String remark;  // 申请附言
}
