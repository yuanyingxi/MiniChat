package com.minichat.message.config;

import com.minichat.message.websocket.AuthHandshakeInterceptor;
import com.minichat.message.websocket.ChatWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private AuthHandshakeInterceptor authInterceptor;

    @Autowired
    private final ChatWebSocketHandler chatWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {


        registry.addHandler(chatWebSocketHandler, "/ws")
                .addInterceptors(authInterceptor)  // 握手时拦截验证
                .setAllowedOrigins("*");
    }
}