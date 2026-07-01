package com.minichat.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FriendRequestVO {
    private Long id;
    private Long fromId;
    private Long toId;
    private String fromUserNickname;   // 发送者昵称
    private String fromUserAvatar;     // 发送者头像
    private String remark;
    private Integer status;            // 0待处理 1同意 2拒绝
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
