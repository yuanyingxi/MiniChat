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

    @Mock
    private UserIndexService userIndexService;

    @Mock
    private FriendService friendService;

    @Mock
    private GroupService groupService;

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
        user.setStatus(1);  // 正常状态
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
        req.setNickname("仅改昵称");

        userService.updateUser(userId, req);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userMapper, times(1)).updateById(captor.capture());

        User updated = captor.getValue();
        assertEquals("仅改昵称", updated.getNickname());
        assertNull(updated.getAvatar());
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

    // ==================== cancelAccount() ====================

    @Test
    void cancelAccount_ShouldSetStatusTo3_AndClearES_AndCleanRelations_AndQuitGroups() {
        when(userMapper.selectById(userId)).thenReturn(user);

        userService.cancelAccount(userId);

        // 1. User 表 status = 3
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userMapper, times(1)).updateById(userCaptor.capture());
        assertEquals(3, userCaptor.getValue().getStatus());

        // 2. ES 索引删除
        verify(userIndexService, times(1)).removeUser(userId);

        // 3. 清理好友关系
        verify(friendService, times(1)).removeAllForUser(userId);

        // 4. 退出所有群
        verify(groupService, times(1)).quitAllGroupsForUser(userId);
    }

    @Test
    void cancelAccount_ShouldThrow_WhenUserNotFound() {
        when(userMapper.selectById(userId)).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.cancelAccount(userId));
        assertEquals("用户不存在", ex.getMessage());

        // 不应触发任何清理操作
        verify(userIndexService, never()).removeUser(any());
        verify(friendService, never()).removeAllForUser(any());
        verify(groupService, never()).quitAllGroupsForUser(any());
    }

    @Test
    void cancelAccount_ShouldThrow_WhenAlreadyCancelled() {
        user.setStatus(3);  // 已注销
        when(userMapper.selectById(userId)).thenReturn(user);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.cancelAccount(userId));
        assertEquals("账号已注销，无需重复操作", ex.getMessage());

        // 幂等保护：不应重复清理（用 ArgumentCaptor 消除 updateById 类型推断歧义）
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userMapper, never()).updateById(userCaptor.capture());
        verify(userIndexService, never()).removeUser(any());
        verify(friendService, never()).removeAllForUser(any());
        verify(groupService, never()).quitAllGroupsForUser(any());
    }
}
