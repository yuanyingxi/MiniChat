package com.minichat.message.websocket;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
public class AuthHandshakeInterceptor implements HandshakeInterceptor {

    private static final String SECRET = "MiniChatSecretKey2026MustBeAtLeast256BitsLongForHMACSHA256!";

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        // 网关已验证 JWT 并透传 userId 到 header
        String userIdStr = request.getHeaders().getFirst("userId");
        if (userIdStr == null) return false;

        Long userId = Long.valueOf(userIdStr);
        attributes.put("userId", userId);
        System.out.println("WS 握手成功: userId=" + userId);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
    }
}
