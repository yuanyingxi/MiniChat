package com.minichat.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_group")
public class Group {

    @TableId
    private Long id;

    private String groupName;

    private Long ownerId;

    private String avatar;

    private String announcement;

    private Integer joinType;

    private Integer memberCount;

    private Integer maxMemberCount;

    private Integer status;

    private LocalDateTime createTime;
}
