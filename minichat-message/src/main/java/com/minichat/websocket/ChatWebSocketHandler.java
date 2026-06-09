package com.minichat.websocket;

import com.minichat.dto.SendMessageReq;
import com.minichat.dto.WsMessage;
import com.minichat.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import lombok.RequiredArgsConstructor;
import cn.hutool.json.JSONUtil;


import java.io.IOException;

@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private final MessageService messageService;
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

//    private void handleAck(
//            WebSocketSession session,
//            JSONObject json
//    ) {
//
//        Long messageId = json.getLong("messageId");
//
//        if (messageId == null) return;
//
//        Long userId =
//                (Long) session.getAttributes()
//                        .get("userId");
//
//        messageMapper.update(
//                null,
//                Wrappers.<Message>lambdaUpdate()
//                        .eq(Message::getId, messageId)
//                        .set(Message::getStatus, 2)
//        );
//
//        System.out.println("[ACK] messageId=" + messageId + ", userId=" + userId);
//    }
}