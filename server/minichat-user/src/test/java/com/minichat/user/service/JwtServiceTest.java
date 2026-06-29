package com.minichat.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        jwtService.secret = "MiniChatSecretKey2026MustBeAtLeast256BitsLongForHMACSHA256!";
        jwtService.expireHours = 24;
        jwtService.init();
    }

    // ==================== createToken() & getUserIdFromToken() ====================

    @Test
    void createToken_ShouldEncodeUserId() {
        String token = jwtService.createToken(1L);
        assertNotNull(token);

        Long userId = jwtService.getUserIdFromToken(token);
        assertEquals(1L, userId);
    }

    @Test
    void createToken_ShouldCreateDifferentTokensForDifferentUsers() {
        String token1 = jwtService.createToken(1L);
        String token2 = jwtService.createToken(2L);

        assertNotEquals(token1, token2);
    }

    // ==================== getUserIdFromToken() ====================

    @Test
    void getUserIdFromToken_ShouldThrow_WhenTokenInvalid() {
        assertThrows(Exception.class,
                () -> jwtService.getUserIdFromToken("invalid_token"));
    }

    // ==================== getRemainingTtl() ====================

    @Test
    void getRemainingTtl_ShouldReturnPositive() {
        String token = jwtService.createToken(1L);

        long ttl = jwtService.getRemainingTtl(token);

        assertTrue(ttl > 0);
        assertTrue(ttl <= 24 * 3600); // 不超过 24 小时
    }
}
