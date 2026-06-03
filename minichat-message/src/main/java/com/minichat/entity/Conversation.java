package com.minichat.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@TableName("tb_conversation")
@Data
public class Conversation {

    @TableId
    private Long id;

    private Integer conversationType;

    private Long targetId;

    private Long lastMessageId;

    private String lastMessageContent;

    private LocalDateTime lastMessageTime;

    private LocalDateTime createTime;
}
