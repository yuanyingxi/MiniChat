package com.minichat.websocket;

import com.minichat.constant.RedisKeyConstant;
import com.minichat.dto.SendMessageReq;
import com.minichat.dto.WsMessage;
import com.minichat.mapper.MessageMapper;
import com.minichat.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import lombok.RequiredArgsConstructor;
import cn.hutool.json.JSONUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.minichat.entity.Message;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private StringRedisTemplate redisTemplate;
    private final MessageService messageService;
    private final MessageMapper messageMapper;
    private final SessionManager sessionManager;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {

        // 获取用户Id
        Long userId =
                Long.parseLong(
                        session.getUri()
                                .getQuery()
                                .split("=")[1]
                );

        session.getAttributes().put(
                "userId",
                userId
        );

        // 用户上线
        sessionManager.addSession(
                userId,
                session
        );

        System.out.println(
                "用户上线：" + userId
        );

        System.out.println(
                "当前在线人数：" +
                        sessionManager.onlineCount()
        );


        // redis
        String key = RedisKeyConstant.OFFLINE_MESSAGE + userId;

        List<String> offlineMessages =
                redisTemplate.opsForList().range(key, 0, -1);

        if (offlineMessages != null && !offlineMessages.isEmpty()) {

            for (String msg : offlineMessages) {
                session.sendMessage(new TextMessage(msg));
            }

            // 清空离线消息
            redisTemplate.delete(key);
        }
    }

    @Override
    public void afterConnectionClosed(
            WebSocketSession session,
            CloseStatus status) {

        sessionManager.removeSession(session);

        System.out.println("连接关闭");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 客户端通过 WebSocket 向服务器发送文本消息时触发

        String payload = message.getPayload();

        WsMessage wsMessage = JSONUtil.toBean(payload, WsMessage.class);

        switch (wsMessage.getType()) {

            case 1:
                handleChat(session, wsMessage.getData());
                break;

            case 2:
                handleAck(session, wsMessage.getData());
                break;

            default:
                break;
        }
    }

    private void handleChat(
            WebSocketSession session,
            JSONObject data
    ) {

        SendMessageReq req =
                data.toBean(SendMessageReq.class);

        Long userId =
                (Long) session.getAttributes()
                        .get("userId");

        messageService.sendMessage(
                req,
                userId
        );
    }

    private void handleAck(
            WebSocketSession session,
            JSONObject json
    ) {

        Long messageId = json.getLong("messageId");

        if (messageId == null) return;

        Long userId =
                (Long) session.getAttributes()
                        .get("userId");

        messageMapper.update(
                null,
                Wrappers.<Message>lambdaUpdate()
                        .eq(Message::getId, messageId)
                        .set(Message::getStatus, 2)
        );

        System.out.println("[ACK] messageId=" + messageId + ", userId=" + userId);
    }
}