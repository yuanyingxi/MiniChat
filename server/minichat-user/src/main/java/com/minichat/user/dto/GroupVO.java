package com.minichat.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupVO {
    private Long groupId;
    private String name;
    private Long ownerId;
    private String ownerNickname;   // 群主昵称
    private String notice;
    private Integer memberCount;    // 成员数
    private LocalDateTime createTime;
}
