package com.minichat.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_message")
public class Message {

    @TableId
    private Long messageId;

    private Integer chatType;

    private Long fromId;

    private Long toId;

    private Integer messageType;

    private Integer status;

    private Long clientSendTime;

    private String content;

    private LocalDateTime createTime;
}