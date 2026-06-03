package com.minichat.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_group_member")
public class GroupMember {

    @TableId
    private Long id;

    private Long groupId;

    private Long userId;

    private String groupNickname;

    private Integer role;

    private Long lastReadMessageId;

    private LocalDateTime joinTime;
}
