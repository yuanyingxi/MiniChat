package com.minichat.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.minichat.user.dto.FriendVO;
import com.minichat.user.dto.HandleFriendRequest;
import com.minichat.user.dto.SendFriendRequest;
import com.minichat.user.dto.UserInfoResponse;
import com.minichat.user.entity.Friend;
import com.minichat.user.entity.FriendRequest;
import com.minichat.user.entity.User;
import com.minichat.user.entity.UserDocument;
import com.minichat.user.mapper.FriendMapper;
import com.minichat.user.mapper.FriendRequestMapper;
import com.minichat.user.mapper.UserMapper;
import com.minichat.user.repository.UserSearchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FriendServiceTest {

    @Mock
    private FriendMapper friendMapper;

    @Mock
    private FriendRequestMapper requestMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserSearchRepository searchRepository;

    @InjectMocks
    private FriendService friendService;

    private Long userId = 1L;
    private Long friendId = 2L;
    private SendFriendRequest sendReq;
    private HandleFriendRequest handleReq;
    private FriendRequest request;
    private User friendUser;

    @BeforeEach
    void setUp() {
        sendReq = new SendFriendRequest();
        sendReq.setToId(friendId);
        sendReq.setRemark("你好");

        handleReq = new HandleFriendRequest();
        handleReq.setRequestId(100L);

        request = new FriendRequest();
        request.setId(100L);
        request.setFromId(friendId);
        request.setToId(userId);
        request.setStatus(0);
        request.setCreateTime(LocalDateTime.now());

        friendUser = new User();
        friendUser.setId(friendId);
        friendUser.setNickname("好友");
        friendUser.setAvatar("avatar.jpg");

        lenient().when(userMapper.selectById(friendId)).thenReturn(friendUser);
    }

    // ==================== sendRequest() ====================

    @Test
    void sendRequest_ShouldSucceed() {
        when(friendMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(requestMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        friendService.sendRequest(userId, sendReq);

        verify(requestMapper, times(1)).insert(any(FriendRequest.class));
    }

    @Test
    void sendRequest_ShouldThrow_WhenAddSelf() {
        sendReq.setToId(userId);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> friendService.sendRequest(userId, sendReq));
        assertEquals("不能添加自己为好友", ex.getMessage());
    }

    @Test
    void sendRequest_ShouldThrow_WhenUserNotExist() {
        when(userMapper.selectById(friendId)).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> friendService.sendRequest(userId, sendReq));
        assertEquals("用户不存在", ex.getMessage());
    }

    @Test
    void sendRequest_ShouldThrow_WhenAlreadyFriends() {
        when(friendMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(new Friend());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> friendService.sendRequest(userId, sendReq));
        assertEquals("对方已经是你的好友", ex.getMessage());
    }

    @Test
    void sendRequest_ShouldThrow_WhenRequestExists() {
        when(friendMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(requestMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> friendService.sendRequest(userId, sendReq));
        assertEquals("已发送过好友请求，请等待对方处理", ex.getMessage());
    }

    // ==================== acceptRequest() ====================

    @Test
    void acceptRequest_ShouldCreateTwoFriendships() {
        when(requestMapper.selectById(100L)).thenReturn(request);

        friendService.acceptRequest(userId, handleReq);

        ArgumentCaptor<FriendRequest> reqCaptor = ArgumentCaptor.forClass(FriendRequest.class);
        verify(requestMapper, times(1)).updateById(reqCaptor.capture());
        assertEquals(1, reqCaptor.getValue().getStatus());
        verify(friendMapper, times(2)).insert(any(Friend.class));
    }

    @Test
    void acceptRequest_ShouldThrow_WhenNotRecipient() {
        when(requestMapper.selectById(100L)).thenReturn(request);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> friendService.acceptRequest(3L, handleReq));
        assertEquals("无权操作此请求", ex.getMessage());
    }

    @Test
    void acceptRequest_ShouldThrow_WhenAlreadyProcessed() {
        request.setStatus(1);
        when(requestMapper.selectById(100L)).thenReturn(request);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> friendService.acceptRequest(userId, handleReq));
        assertEquals("请求已处理", ex.getMessage());
    }

    // ==================== rejectRequest() ====================

    @Test
    void rejectRequest_ShouldSetStatusRejected() {
        when(requestMapper.selectById(100L)).thenReturn(request);

        friendService.rejectRequest(userId, handleReq);

        ArgumentCaptor<FriendRequest> captor = ArgumentCaptor.forClass(FriendRequest.class);
        verify(requestMapper, times(1)).updateById(captor.capture());
        assertEquals(2, captor.getValue().getStatus());
    }

    // ==================== getFriendList() ====================

    @Test
    void getFriendList_ShouldReturnList() {
        Friend friend = new Friend();
        friend.setUserId(userId);
        friend.setFriendId(friendId);
        friend.setStatus(1);
        friend.setCreateTime(LocalDateTime.now());

        when(friendMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(friend));
        when(userMapper.selectById(friendId)).thenReturn(friendUser);

        List<FriendVO> result = friendService.getFriendList(userId);

        assertEquals(1, result.size());
        assertEquals("好友", result.get(0).getNickname());
        assertEquals(friendId, result.get(0).getFriendId());
    }

    // ==================== deleteFriend() ====================

    @Test
    void deleteFriend_ShouldSetStatusToZero() {
        Friend friend = new Friend();
        friend.setId(1L);
        friend.setUserId(userId);
        friend.setFriendId(friendId);
        friend.setStatus(1);

        when(friendMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(friend);

        friendService.deleteFriend(userId, friendId);

        assertEquals(0, friend.getStatus());
        ArgumentCaptor<Friend> captor = ArgumentCaptor.forClass(Friend.class);
        verify(friendMapper, times(1)).updateById(captor.capture());
        assertEquals(0, captor.getValue().getStatus());
    }

    // ==================== toggleBlock() ====================

    @Test
    void toggleBlock_ShouldChangeToBlocked() {
        Friend friend = new Friend();
        friend.setId(1L);
        friend.setUserId(userId);
        friend.setFriendId(friendId);
        friend.setStatus(1);

        when(friendMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(friend);

        friendService.toggleBlock(userId, friendId);

        assertEquals(2, friend.getStatus());
    }

    @Test
    void toggleBlock_ShouldChangeToUnblocked() {
        Friend friend = new Friend();
        friend.setId(1L);
        friend.setUserId(userId);
        friend.setFriendId(friendId);
        friend.setStatus(2);

        when(friendMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(friend);

        friendService.toggleBlock(userId, friendId);

        assertEquals(1, friend.getStatus());
    }

    // ==================== searchUsers() ====================

    @Test
    void searchUsers_ShouldSearchByES() {
        UserDocument doc = new UserDocument();
        doc.setId(friendId);
        doc.setNickname("好友");
        doc.setPhone("13800138002");
        doc.setAvatar("avatar.jpg");

        when(searchRepository.findByNickname("好友")).thenReturn(List.of(doc));

        List<UserInfoResponse> result = friendService.searchUsers(userId, "好友");

        assertEquals(1, result.size());
        assertEquals("好友", result.get(0).getNickname());
        assertEquals("13800138002", result.get(0).getPhone());
    }

    @Test
    void searchUsers_ShouldExcludeSelf() {
        UserDocument self = new UserDocument();
        self.setId(userId);
        self.setNickname("自己");

        when(searchRepository.findByNickname("自己")).thenReturn(List.of(self));

        List<UserInfoResponse> result = friendService.searchUsers(userId, "自己");

        assertEquals(0, result.size());
    }

    @Test
    void searchUsers_ShouldThrow_WhenKeywordBlank() {
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> friendService.searchUsers(userId, "  "));
        assertEquals("搜索关键词不能为空", ex.getMessage());
    }

    // ==================== removeAllForUser()（账号注销清理） ====================

    @Test
    void removeAllForUser_ShouldDeleteFriendAndRequestRecords() {
        friendService.removeAllForUser(1L);

        // 验证 friendMapper.delete 被调用（带双向查询条件）
        ArgumentCaptor<LambdaQueryWrapper<Friend>> friendCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(friendMapper, times(1)).delete(friendCaptor.capture());
        assertNotNull(friendCaptor.getValue());

        // 验证 requestMapper.delete 被调用
        ArgumentCaptor<LambdaQueryWrapper<FriendRequest>> reqCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(requestMapper, times(1)).delete(reqCaptor.capture());
        assertNotNull(reqCaptor.getValue());
    }

    @Test
    void removeAllForUser_ShouldNotCallSearchRepository() {
        // 注销清理不涉及 ES 搜索（ES 由 UserIndexService 处理）
        friendService.removeAllForUser(1L);

        verifyNoInteractions(searchRepository);
    }
}
