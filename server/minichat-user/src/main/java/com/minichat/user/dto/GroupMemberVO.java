package com.minichat.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupMemberVO {
    private Long userId;
    private String nickname;
    private String avatar;
    private Integer role;           // 0普通 1管理员 2群主
    private String alias;           // 群昵称
}
