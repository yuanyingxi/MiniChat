package com.minichat.websocket;

import com.minichat.constant.RedisKeyConstant;
import com.minichat.mapper.MessageMapper;
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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final Map<Long, WebSocketSession>
            ONLINE_USERS = new ConcurrentHashMap<>();
    private final MessageMapper messageMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {

        String query = session.getUri().getQuery();

        Long userId = Long.parseLong(
                query.split("=")[1]
        );

        ONLINE_USERS.put(userId, session);

        System.out.println(
                "用户上线：" + userId
        );

        System.out.println(
                "当前在线人数：" +
                        ONLINE_USERS.size()
        );


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
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {

        ONLINE_USERS.values().remove(session);
        System.out.println("连接关闭");

        // TODO: 可以解析userId（后续优化）
    }

    public boolean isOnline(Long userId) {
        return ONLINE_USERS.containsKey(userId);
    }

    public void onUserOffline(Long userId) {
        System.out.println("用户离线 userId=" + userId);

        // 👉 下一步：这里接 Redis 离线消息
    }

    public void sendToUser(Long userId, String message) {

        WebSocketSession session = ONLINE_USERS.get(userId);

        if (session == null) {
            System.out.println("用户离线 userId=" + userId);
            return;
        }

        try {
            session.sendMessage(new TextMessage(message));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void handleTextMessage(
            WebSocketSession session,
            TextMessage message
    ) throws Exception {

        String payload = message.getPayload();

        JSONObject json = JSONUtil.parseObj(payload);

        Integer type = json.getInt("type");

        // 1️⃣ ACK消息
        if (type != null && type == 2) {
            handleAck(json);
            return;
        }

        // 2️⃣ 普通消息（如果你未来要扩展双向WS）
    }

    private void handleAck(JSONObject json) {

        Long messageId = json.getLong("messageId");
        Long userId = json.getLong("userId");

        if (messageId == null) return;

        messageMapper.update(
                null,
                Wrappers.<Message>lambdaUpdate()
                        .eq(Message::getId, messageId)
                        .set(Message::getStatus, 2) // delivered
        );

        System.out.println("[ACK] messageId=" + messageId + ", userId=" + userId);
    }
}