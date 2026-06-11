package com.minichat.message.websocket;

import com.minichat.message.dto.SendMessageReq;
import com.minichat.message.dto.WsMessage;
import com.minichat.message.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import lombok.RequiredArgsConstructor;
import cn.hutool.json.JSONUtil;
import org.springframework.web.util.UriComponentsBuilder;


import java.io.IOException;
import java.net.URI;

@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private final MessageService messageService;
    private final SessionManager sessionManager;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {

//        Long userId = (Long) session.getAttributes().get("userId");

        URI uri = session.getUri();

        String userIdStr = UriComponentsBuilder
                .fromUri(uri)
                .build()
                .getQueryParams()
                .getFirst("userId");

        Long userId = Long.valueOf(userIdStr);

        session.getAttributes().put("userId", userId);

        // 如果用户已在线则踢掉旧连接
        if (sessionManager.isOnline(userId)) {
            sessionManager.removeSession(userId);
        }

        sessionManager.addSession(userId, session);
        System.out.println("用户上线：" + userId);
        System.out.println("当前在线人数：" + sessionManager.onlineCount());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {

        sessionManager.removeSession(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 客户端通过 WebSocket 向服务器发送文本消息时触发

        String payload = message.getPayload();
        System.out.println("收到消息: " + payload);

        WsMessage wsMessage = JSONUtil.toBean(payload, WsMessage.class);

        switch (wsMessage.getType()) {

            case 1:
                SendMessageReq req = wsMessage.getData().toBean(SendMessageReq.class);

                Long userId = (Long) session.getAttributes().get("userId");

                messageService.sendMessage(req, userId);
                break;

            default:
                break;
        }
    }
}