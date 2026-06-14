package com.minichat.message.dto;

import cn.hutool.json.JSONObject;
import lombok.Data;

@Data
public class WsMessage {

    /**
     * 1 聊天消息
     * 2 ACK
     * 3 心跳
     */
    private Integer type;

    /**
     * 具体业务数据
     */
    private JSONObject data;
}
