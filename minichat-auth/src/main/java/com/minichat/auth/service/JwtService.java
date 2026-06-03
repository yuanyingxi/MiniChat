package com.minichat.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String secret;      // 签名密钥

    @Value("${jwt.expire-hours:24}")
    private long expireHours;

    private SecretKey getKey() {
        // 生成钥匙
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // 生成 Token
    public String createToken(Long userId) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))        // 1. 盖上印章：持有者是谁
                .issuedAt(now)                          // 2. 签发时间
                .expiration(new Date(now.getTime() + expireHours * 3600_000))   // 3. 到期时间
                .signWith(getKey())                     // 4. 签上防伪标签 (signature)
                .compact();                             // 5. 压缩打包
    }

    // 从 Token 中解析用户 ID
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return Long.valueOf(claims.getSubject());
    }

}
