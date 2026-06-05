package com.minichat.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FriendVO {
    private Long friendId;      // 好友的用户ID
    private String nickname;    // 好友昵称
    private String avatar;      // 好友头像
    private String remark;      // 你给他备注的名字
    private LocalDateTime createTime;  // 成为好友的时间
}
