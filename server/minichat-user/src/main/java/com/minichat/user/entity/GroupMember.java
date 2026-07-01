package com.minichat.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_group_member")
public class GroupMember {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long groupId;
    private Long userId;
    private Integer role;          // 0普通成员 1管理员 2群主
    private String alias;          // 群昵称
    private Integer status;        // 1正常 2已退群 3被踢出
    private LocalDateTime joinTime;
    private LocalDateTime quitTime;

}
