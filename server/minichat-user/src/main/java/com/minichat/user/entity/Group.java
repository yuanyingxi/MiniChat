package com.minichat.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("tb_group")
public class Group {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String name;
    private Long ownerId;
    private String notice;
    private Integer maxMembers;
    private Integer status;        // 1正常 2已解散
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
