package com.minichat.user.service;

import com.minichat.user.dto.UpdateUserRequest;
import com.minichat.user.dto.UserInfoResponse;
import com.minichat.user.entity.User;
import com.minichat.user.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private Long userId = 1L;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(userId);
        user.setPhone("13800138000");
        user.setNickname("测试用户");
        user.setAvatar("old_avatar.jpg");
        user.setSignature("旧签名");
        user.setGender(1);
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
    }

    // ==================== getUserInfo() ====================

    @Test
    void getUserInfo_ShouldReturnUserInfo() {
        when(userMapper.selectById(userId)).thenReturn(user);

        UserInfoResponse result = userService.getUserInfo(userId);

        assertNotNull(result);
        assertEquals("测试用户", result.getNickname());
        assertEquals("13800138000", result.getPhone());
        assertEquals("旧签名", result.getSignature());
    }

    @Test
    void getUserInfo_ShouldThrow_WhenUserNotFound() {
        when(userMapper.selectById(userId)).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.getUserInfo(userId));
        assertEquals("用户不存在", ex.getMessage());
    }

    // ==================== updateUser() ====================

    @Test
    void updateUser_ShouldUpdateAllFields() {
        when(userMapper.selectById(userId)).thenReturn(user);

        UpdateUserRequest req = new UpdateUserRequest();
        req.setNickname("新昵称");
        req.setAvatar("new_avatar.jpg");
        req.setSignature("新签名");
        req.setGender(2);

        userService.updateUser(userId, req);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userMapper, times(1)).updateById(captor.capture());

        User updated = captor.getValue();
        assertEquals("新昵称", updated.getNickname());
        assertEquals("new_avatar.jpg", updated.getAvatar());
        assertEquals("新签名", updated.getSignature());
        assertEquals(2, updated.getGender());
    }

    @Test
    void updateUser_ShouldUpdatePartialFields() {
        when(userMapper.selectById(userId)).thenReturn(user);

        UpdateUserRequest req = new UpdateUserRequest();
        req.setNickname("仅改昵称"); // 只改昵称，其他为 null

        userService.updateUser(userId, req);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userMapper, times(1)).updateById(captor.capture());

        User updated = captor.getValue();
        assertEquals("仅改昵称", updated.getNickname());
        assertNull(updated.getAvatar());  // 为 null 表示无需更新
        assertNull(updated.getSignature());
        assertNull(updated.getGender());
    }

    @Test
    void updateUser_ShouldThrow_WhenUserNotFound() {
        when(userMapper.selectById(userId)).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.updateUser(userId, new UpdateUserRequest()));
        assertEquals("用户不存在", ex.getMessage());
    }
}
