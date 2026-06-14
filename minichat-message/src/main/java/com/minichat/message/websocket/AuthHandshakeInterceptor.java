package com.minichat.message.websocket;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

// 握手拦截器
@Component
public class AuthHandshakeInterceptor implements HandshakeInterceptor {

    private static final String SECRET = "MiniChatSecretKey2026MustBeAtLeast256BitsLongForHMACSHA256!";

    /**
     * 握手前调用：验证身份，决定是否允许连接
     * return true  → 继续握手
     * return false → 拒绝连接（前端收到 1006）
     */
    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes) {

//        // 从 Header 取 token
//        String token = ((ServletServerHttpRequest) request).getServletRequest().getHeader("Authorization");
//        if (token == null || !token.startsWith("Bearer ")) return false;

//        // 从 URL 参数取 token
//        URI uri = request.getURI();
//
//        String token = UriComponentsBuilder
//                .fromUri(uri)
//                .build()
//                .getQueryParams()
//                .getFirst("token");
//
//        if(token == null) return false;
//
//        try {
//
//            JWT jwt = JWTUtil.parseToken(token);
//
//            // 验证token
//            jwt.setKey(SECRET.getBytes());
//            if (!jwt.validate(0)) {
//                System.out.println("签名验证失败或 token 过期");
//                return false;
//            }
//
//            // 取userId
//            Long userId = (Long) jwt.getPayload("sub");
//            attributes.put("userId", userId);
//            return true;
//
//        } catch (Exception e) {
//            return false;
//        }

        // 从URL取userId
        URI uri = request.getURI();

        String userId = UriComponentsBuilder
                .fromUri(uri)
                .build()
                .getQueryParams()
                .getFirst("userId");

        attributes.put("userId", Long.valueOf(userId));

        return true;

//        // 网关透传过来的 userId
//        String userId = request.getHeaders().getFirst("userId");
//        attributes.put("userId", userId);
//        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }
}
