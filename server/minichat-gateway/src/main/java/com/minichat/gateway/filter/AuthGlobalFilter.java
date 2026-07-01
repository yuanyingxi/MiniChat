package com.minichat.gateway.filter;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private static final String[] WHITE_LIST = {
            "/auth/login",
            "/auth/register",
            "/auth/sms/code",
            "/doc.html",
            "/webjars/",
            "/v3/api-docs",
            "/swagger-resources",
            "/favicon.ico",
    };

    @Value("${jwt.secret}")
    private String secret;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // /internal/** 是服务间 RPC 接口，禁止从网关外部访问
        if (path.startsWith("/internal/")) {
            log.warn("Blocked external access to internal endpoint: {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }

        // 白名单放行
        for (String prefix : WHITE_LIST) {
            if (path.startsWith(prefix)) {
                return chain.filter(exchange);
            }
        }

        String token;

        // WebSocket 升级请求：从 URL 参数取 token（浏览器 WS API 不支持自定义 header）
        String upgrade = exchange.getRequest().getHeaders().getFirst("Upgrade");
        if ("websocket".equalsIgnoreCase(upgrade)) {
            token = exchange.getRequest().getQueryParams().getFirst("token");
            if (token == null) {
                log.warn("WebSocket upgrade missing token in URL for {}", path);
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
        } else {
            // 普通 HTTP 请求：从 Authorization header 取 token
            String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Missing or invalid Authorization header for {}", path);
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
            token = authHeader.substring(7);
        }

        try {
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            String userId = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();

            // 将 userId 传给下游服务
            ServerHttpRequest mutated = exchange.getRequest().mutate()
                    .header("userId", userId)
                    .build();
            return chain.filter(exchange.mutate().request(mutated).build());

        } catch (Exception e) {
            log.warn("JWT validation failed for {}: {}", path, e.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
