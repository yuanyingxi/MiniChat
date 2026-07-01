package com.minichat.user.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HandleFriendRequest {
    @NotNull(message = "请求ID不能为空")
    private Long requestId;
}
