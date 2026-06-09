package com.minichat.message.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_message")
public class Message {

    @TableId
    private Long id;

    private Long conversationId;

    private Integer chatType;

    private Long fromId;

    private Long toId;

    private Integer messageType;

    private Integer status;

    private Long clientSendTime;

    private String content;

    private LocalDateTime createTime;
}