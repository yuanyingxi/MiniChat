package com.minichat.dto;

import cn.hutool.json.JSONObject;
import lombok.Data;

@Data
public class WsMessage {

    /**
     * 1 聊天消息
     * 2 ACK
     */
    private Integer type;

    /**
     * 具体业务数据
     */
    private JSONObject data;
}
