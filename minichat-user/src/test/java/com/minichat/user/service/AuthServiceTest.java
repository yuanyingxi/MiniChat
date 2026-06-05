package com.minichat.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.minichat.user.dto.LoginRequest;
import com.minichat.user.dto.RegisterRequest;
import com.minichat.user.entity.User;
import com.minichat.user.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @Mock
    private SmsService smsService;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserIndexService userIndexService;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerReq;
    private LoginRequest loginReq;
    private User existingUser;

    @BeforeEach
    void setUp() {
        registerReq = new RegisterRequest();
        registerReq.setPhone("13800138000");
        registerReq.setPassword("password123");
        registerReq.setSmsCode("123456");

        loginReq = new LoginRequest();
        loginReq.setPhone("13800138000");
        loginReq.setPassword("password123");

        existingUser = new User();
        existingUser.setId(1L);
        existingUser.setPhone("13800138000");
        existingUser.setPasswordHash("encoded_password");
        existingUser.setNickname("测试用户");
        existingUser.setCreateTime(LocalDateTime.now());

        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    // ==================== register() ====================

    @Test
    void register_ShouldSucceed_WhenPhoneNotRegistered() {
        // Arrange
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(smsService.verifyCode("13800138000", "123456")).thenReturn(true);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        // ★ 模拟 MyBatis-Plus 插入后自动回写自增 ID
        when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return 1;
        });
        when(jwtService.createToken(eq(1L))).thenReturn("jwt_token");

        // Act
        String token = authService.register(registerReq);

        // Assert
        assertEquals("jwt_token", token);
        verify(userMapper, times(1)).insert(any(User.class));
        verify(userIndexService, times(1)).indexUser(any(User.class));
    }

    @Test
    void register_ShouldThrow_WhenPhoneAlreadyExists() {
        // Arrange
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existingUser);

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.register(registerReq));
        assertEquals("该手机号已注册", ex.getMessage());
        verify(userMapper, never()).insert(isA(User.class));
        verify(userIndexService, never()).indexUser(isA(User.class));
    }

    @Test
    void register_ShouldThrow_WhenSmsCodeInvalid() {
        // Arrange
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(smsService.verifyCode("13800138000", "123456")).thenReturn(false);

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.register(registerReq));
        assertEquals("验证码错误或已过期", ex.getMessage());
        verify(userMapper, never()).insert(isA(User.class));
        verify(userIndexService, never()).indexUser(isA(User.class));
    }

    // ==================== login() ====================

    @Test
    void login_ShouldSucceed_WhenCredentialsValid() {
        // Arrange
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existingUser);
        when(passwordEncoder.matches("password123", "encoded_password")).thenReturn(true);
        when(jwtService.createToken(1L)).thenReturn("jwt_token");

        // Act
        String token = authService.login(loginReq);

        // Assert
        assertEquals("jwt_token", token);
    }

    @Test
    void login_ShouldThrow_WhenUserNotFound() {
        // Arrange
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.login(loginReq));
        assertEquals("用户不存在", ex.getMessage());
    }

    @Test
    void login_ShouldThrow_WhenPasswordWrong() {
        // Arrange
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existingUser);
        when(passwordEncoder.matches("password123", "encoded_password")).thenReturn(false);

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.login(loginReq));
        assertEquals("密码错误", ex.getMessage());
    }

    // ==================== logout() ====================

    @Test
    void logout_ShouldAddToBlacklist_WhenValidToken() {
        // Arrange
        String authHeader = "Bearer valid.jwt.token";
        when(jwtService.getRemainingTtl("valid.jwt.token")).thenReturn(3600L);

        // Act
        authService.logout(authHeader);

        // Assert
        verify(valueOperations, times(1)).set(
                startsWith("blacklist:token:"),
                eq("1"),
                eq(3600L),
                any()
        );
    }

    @Test
    void logout_ShouldDoNothing_WhenNoAuthHeader() {
        // Act
        authService.logout(null);

        // Assert
        verify(valueOperations, never()).set(anyString(), anyString(), anyLong(), any());
    }

    @Test
    void logout_ShouldNotThrow_WhenTokenInvalid() {
        // Arrange
        String authHeader = "Bearer invalid_token";
        when(jwtService.getRemainingTtl("invalid_token")).thenThrow(new RuntimeException("Token invalid"));

        // Act (should not throw)
        authService.logout(authHeader);

        // Assert
        verify(valueOperations, never()).set(anyString(), anyString(), anyLong(), any());
    }
}
