package com.minichat.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.minichat.user.dto.CreateGroup;
import com.minichat.user.dto.GroupMemberVO;
import com.minichat.user.dto.GroupVO;
import com.minichat.user.dto.UpdateGroup;
import com.minichat.user.entity.Group;
import com.minichat.user.entity.GroupMember;
import com.minichat.user.entity.User;
import com.minichat.user.mapper.GroupMapper;
import com.minichat.user.mapper.GroupMemberMapper;
import com.minichat.user.mapper.UserMapper;
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
class GroupServiceTest {

    @Mock
    private GroupMapper groupMapper;

    @Mock
    private GroupMemberMapper memberMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private GroupService groupService;

    private Long userId = 1L;
    private Long friendId = 2L;
    private Long groupId = 10L;
    private CreateGroup createReq;
    private UpdateGroup updateReq;
    private Group group;
    private GroupMember ownerMember;
    private GroupMember normalMember;
    private User ownerUser;
    private User friendUser;

    @BeforeEach
    void setUp() {
        createReq = new CreateGroup();
        createReq.setName("测试群");
        createReq.setMemberIds(List.of(friendId));

        updateReq = new UpdateGroup();
        updateReq.setName("新群名");
        updateReq.setNotice("新公告");

        group = new Group();
        group.setId(groupId);
        group.setName("测试群");
        group.setOwnerId(userId);
        group.setStatus(1);
        group.setMaxMembers(200);
        group.setCreateTime(LocalDateTime.now());

        ownerMember = new GroupMember();
        ownerMember.setGroupId(groupId);
        ownerMember.setUserId(userId);
        ownerMember.setRole(2); // 群主
        ownerMember.setStatus(1);

        normalMember = new GroupMember();
        normalMember.setGroupId(groupId);
        normalMember.setUserId(friendId);
        normalMember.setRole(0); // 普通成员
        normalMember.setStatus(1);

        ownerUser = new User();
        ownerUser.setId(userId);
        ownerUser.setNickname("群主");

        friendUser = new User();
        friendUser.setId(friendId);
        friendUser.setNickname("好友");

        lenient().when(groupMapper.selectById(groupId)).thenReturn(group);
        lenient().when(userMapper.selectById(userId)).thenReturn(ownerUser);
    }

    // ==================== createGroup() ====================

    @Test
    void createGroup_ShouldInsertGroupAndMembers() {
        when(groupMapper.insert(any(Group.class))).thenAnswer(invocation -> {
            Group g = invocation.getArgument(0);
            g.setId(groupId);
            return 1;
        });
        when(userMapper.selectById(friendId)).thenReturn(friendUser);

        groupService.createGroup(userId, createReq);

        verify(groupMapper, times(1)).insert(any(Group.class));
        verify(memberMapper, times(2)).insert(any(GroupMember.class)); // 群主 + 好友
    }

    @Test
    void createGroup_ShouldThrow_WhenNameBlank() {
        createReq.setName("");
        createReq.setMemberIds(List.of(friendId));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> groupService.createGroup(userId, createReq));
        assertEquals("群名称不能为空", ex.getMessage());
    }

    // ==================== quitGroup() ====================

    @Test
    void quitGroup_ShouldUpdateMemberStatus() {
        when(memberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(normalMember);

        groupService.quitGroup(friendId, groupId); // ★ friendId=2L 是普通成员，可以退群

        assertEquals(2, normalMember.getStatus()); // 已退群
        assertNotNull(normalMember.getQuitTime());
        ArgumentCaptor<GroupMember> captor = ArgumentCaptor.forClass(GroupMember.class);
        verify(memberMapper, times(1)).updateById(captor.capture());
        assertEquals(2, captor.getValue().getStatus());
    }

    @Test
    void quitGroup_ShouldThrow_WhenOwner() {
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> groupService.quitGroup(userId, groupId)); // userId=1L 是群主

        assertTrue(ex.getMessage().contains("群主不能退群"));
    }

    // ==================== kickMember() ====================

    @Test
    void kickMember_ShouldUpdateStatus() {
        GroupMember target = new GroupMember();
        target.setGroupId(groupId);
        target.setUserId(friendId);
        target.setRole(0);
        target.setStatus(1);
        when(memberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(target);

        groupService.kickMember(userId, groupId, friendId); // userId=1L 是群主，踢 friendId

        assertEquals(3, target.getStatus()); // 被踢
        assertNotNull(target.getQuitTime());
        ArgumentCaptor<GroupMember> captor = ArgumentCaptor.forClass(GroupMember.class);
        verify(memberMapper, times(1)).updateById(captor.capture());
        assertEquals(3, captor.getValue().getStatus());
    }

    @Test
    void kickMember_ShouldThrow_WhenNotOwner() {
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> groupService.kickMember(friendId, groupId, 3L)); // friendId=2L 不是群主
        assertEquals("只有群主才能踢人", ex.getMessage());
    }

    // ==================== getMyGroups() ====================

    @Test
    void getMyGroups_ShouldReturnList() {
        GroupMember membership = new GroupMember();
        membership.setGroupId(groupId);
        membership.setUserId(userId);
        membership.setRole(2);
        membership.setStatus(1);

        when(memberMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(membership));

        List<GroupVO> result = groupService.getMyGroups(userId);

        assertEquals(1, result.size());
        assertEquals("测试群", result.get(0).getName());
    }

    // ==================== getMembers() ====================

    @Test
    void getMembers_ShouldReturnList() {
        when(memberMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(ownerMember));
        when(userMapper.selectById(userId)).thenReturn(ownerUser);

        List<GroupMemberVO> result = groupService.getMembers(groupId);

        assertEquals(1, result.size());
        assertEquals("群主", result.get(0).getNickname());
    }

    // ==================== updateGroup() ====================

    @Test
    void updateGroup_ShouldSucceed_WhenOwner() {
        groupService.updateGroup(userId, groupId, updateReq); // userId=1L 是群主

        ArgumentCaptor<Group> captor = ArgumentCaptor.forClass(Group.class);
        verify(groupMapper, times(1)).updateById(captor.capture());
    }

    @Test
    void updateGroup_ShouldThrow_WhenNotOwner() {
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> groupService.updateGroup(friendId, groupId, updateReq)); // friendId=2L 不是群主
        assertEquals("只有群主才能修改群信息", ex.getMessage());
    }

    // ==================== dismissGroup() ====================

    @Test
    void dismissGroup_ShouldSucceed_WhenOwner() {
        GroupMember m1 = new GroupMember();
        m1.setGroupId(groupId);
        m1.setUserId(friendId);
        m1.setRole(0);
        m1.setStatus(1);

        when(memberMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(m1));

        groupService.dismissGroup(userId, groupId); // userId=1L 是群主

        assertEquals(2, group.getStatus()); // 已解散
        ArgumentCaptor<Group> groupCaptor = ArgumentCaptor.forClass(Group.class);
        verify(groupMapper, times(1)).updateById(groupCaptor.capture());
        assertEquals(2, groupCaptor.getValue().getStatus());

        ArgumentCaptor<GroupMember> memberCaptor = ArgumentCaptor.forClass(GroupMember.class);
        verify(memberMapper, times(1)).updateById(memberCaptor.capture());
        assertEquals(2, memberCaptor.getValue().getStatus());
    }

    @Test
    void dismissGroup_ShouldThrow_WhenNotOwner() {
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> groupService.dismissGroup(friendId, groupId)); // friendId=2L 不是群主
        assertEquals("只有群主才能解散群", ex.getMessage());
    }

    // ==================== quitAllGroupsForUser()（账号注销清理） ====================

    @Test
    void quitAllGroupsForUser_ShouldMarkAllMembershipsAsQuit() {
        // 模拟：用户在群 A 是普通成员
        GroupMember m1 = new GroupMember();
        m1.setId(100L);
        m1.setGroupId(10L);
        m1.setUserId(userId);
        m1.setStatus(1);  // 正常

        when(memberMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(m1));
        when(groupMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of());  // 没建群

        groupService.quitAllGroupsForUser(userId);

        // 成员状态被改为 2
        ArgumentCaptor<GroupMember> captor = ArgumentCaptor.forClass(GroupMember.class);
        verify(memberMapper, atLeastOnce()).updateById(captor.capture());

        boolean hasQuit = captor.getAllValues().stream()
                .anyMatch(m -> m.getStatus() != null && m.getStatus() == 2 && m.getQuitTime() != null);
        assertTrue(hasQuit, "至少应有一条成员记录被标记为已退群");
    }

    @Test
    void quitAllGroupsForUser_ShouldDismissOwnedGroups_AndQuitOtherMembers() {
        // 模拟：用户是群 A 的群主
        Group ownedGroup = new Group();
        ownedGroup.setId(10L);
        ownedGroup.setOwnerId(userId);
        ownedGroup.setStatus(1);

        // 群 A 还有其他成员
        GroupMember otherMember = new GroupMember();
        otherMember.setId(200L);
        otherMember.setGroupId(10L);
        otherMember.setUserId(3L);
        otherMember.setStatus(1);

        // memberMapper.selectList 调用顺序：
        //   1) 查"我的成员记录" → 空（已注销用户本来就不该再有群关系）
        //   2) 查"群内其他成员"（在循环 ownedGroups 内部）→ otherMember
        // 用 Answer 按调用次数分支，避免 thenReturn 队列被 groupMapper 调用错位消耗
        java.util.concurrent.atomic.AtomicInteger memberSelectCalls = new java.util.concurrent.atomic.AtomicInteger(0);
        when(memberMapper.selectList(any(LambdaQueryWrapper.class))).thenAnswer(inv -> {
            int n = memberSelectCalls.incrementAndGet();
            if (n == 1) return List.of();              // myMemberships
            return List.of(otherMember);                // others
        });
        when(groupMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(ownedGroup));

        groupService.quitAllGroupsForUser(userId);

        // 群被标记为已解散
        ArgumentCaptor<Group> groupCaptor = ArgumentCaptor.forClass(Group.class);
        verify(groupMapper, atLeastOnce()).updateById(groupCaptor.capture());
        boolean hasDismissed = groupCaptor.getAllValues().stream()
                .anyMatch(g -> g.getStatus() != null && g.getStatus() == 2);
        assertTrue(hasDismissed, "群主身份时，所建群应被标记为已解散");

        // 其他成员被连带退群
        ArgumentCaptor<GroupMember> memberCaptor = ArgumentCaptor.forClass(GroupMember.class);
        verify(memberMapper, atLeastOnce()).updateById(memberCaptor.capture());
        boolean otherQuit = memberCaptor.getAllValues().stream()
                .anyMatch(m -> m.getUserId() != null && m.getUserId().equals(3L) && m.getStatus() == 2);
        assertTrue(otherQuit, "群解散时，其他成员应被连带标记为已退群");
    }

    @Test
    void quitAllGroupsForUser_ShouldBeIdempotent_WhenNoMemberships() {
        when(memberMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of());
        when(groupMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of());

        // 不应抛异常
        assertDoesNotThrow(() -> groupService.quitAllGroupsForUser(userId));

        // 没有任何 update 调用（用 ArgumentCaptor 避免类型歧义）
        ArgumentCaptor<GroupMember> memberCaptor = ArgumentCaptor.forClass(GroupMember.class);
        verify(memberMapper, never()).updateById(memberCaptor.capture());

        ArgumentCaptor<Group> groupCaptor = ArgumentCaptor.forClass(Group.class);
        verify(groupMapper, never()).updateById(groupCaptor.capture());
    }
}
