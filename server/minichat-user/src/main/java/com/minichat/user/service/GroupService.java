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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class GroupService {
    @Autowired
    private GroupMapper groupMapper;

    @Autowired
    private GroupMemberMapper memberMapper;

    @Autowired
    private UserMapper userMapper;

    // 创建群（自己 + 选择的成员 ≥ 2 人即可创建）
    @Transactional
    public Long createGroup(Long userId, CreateGroup req) {
        // 0. 校验群名称
        if (req.getName() == null || req.getName().trim().isEmpty()) {
            throw new RuntimeException("群名称不能为空");
        }

        // 1. 总人数 = 自己 + 选择的成员
        int totalMembers = 1 + req.getMemberIds().size();
        if (totalMembers < 2) {
            throw new RuntimeException("群至少需要 2 人");
        }
        if (totalMembers > 200) {
            throw new RuntimeException("群成员不能超过 200 人");
        }

        // 2. 创建群
        Group group = new Group();
        group.setName(req.getName());
        group.setOwnerId(userId);
        group.setStatus(1);
        group.setMaxMembers(200);
        groupMapper.insert(group);

        // 3. 添加群主（群主角色=2）
        GroupMember owner = new GroupMember();
        owner.setGroupId(group.getId());
        owner.setUserId(userId);
        owner.setRole(2);      // 群主
        owner.setStatus(1);
        owner.setJoinTime(LocalDateTime.now());
        memberMapper.insert(owner);

        // 4. 添加普通成员（角色=0）
        for (Long memberId : req.getMemberIds()) {
            // 跳过重复
            if (memberId.equals(userId)) continue;

            // 检查用户是否存在
            User member = userMapper.selectById(memberId);
            if (member == null) continue;

            GroupMember gm = new GroupMember();
            gm.setGroupId(group.getId());
            gm.setUserId(memberId);
            gm.setRole(0);      // 普通成员
            gm.setStatus(1);
            gm.setJoinTime(LocalDateTime.now());
            memberMapper.insert(gm);
        }

        return group.getId();
    }

    // 退出群
    public void quitGroup(Long userId, Long groupId) {
        Group group = groupMapper.selectById(groupId);
        if (group == null) {
            throw new RuntimeException("群不存在");
        }
        if (group.getStatus() == 2) {
            throw new RuntimeException("群已解散");
        }

        // 群主不能退群（只能解散群）
        if (group.getOwnerId().equals(userId)) {
            throw new RuntimeException("群主不能退群，如需解散请联系管理员");
        }

        GroupMember member = memberMapper.selectOne(
                new LambdaQueryWrapper<GroupMember>()
                        .eq(GroupMember::getGroupId, groupId)
                        .eq(GroupMember::getUserId, userId)
                        .eq(GroupMember::getStatus, 1)
        );
        if (member == null) {
            throw new RuntimeException("你不是该群成员");
        }

        member.setStatus(2);   // 已退群
        member.setQuitTime(LocalDateTime.now());
        memberMapper.updateById(member);
    }

    // 踢人（仅群主可操作）
    public void kickMember(Long ownerId, Long groupId, Long memberId) {
        Group group = groupMapper.selectById(groupId);
        if (group == null) {
            throw new RuntimeException("群不存在");
        }
        // 只有群主能踢人
        if (!group.getOwnerId().equals(ownerId)) {
            throw new RuntimeException("只有群主才能踢人");
        }
        // 不能踢自己
        if (ownerId.equals(memberId)) {
            throw new RuntimeException("不能踢自己");
        }

        GroupMember member = memberMapper.selectOne(
                new LambdaQueryWrapper<GroupMember>()
                        .eq(GroupMember::getGroupId, groupId)
                        .eq(GroupMember::getUserId, memberId)
                        .eq(GroupMember::getStatus, 1)
        );
        if (member == null) {
            throw new RuntimeException("该用户不在群中");
        }

        member.setStatus(3);   // 被踢出
        member.setQuitTime(LocalDateTime.now());
        memberMapper.updateById(member);
    }

    // 我的群列表
    public List<GroupVO> getMyGroups(Long userId) {
        // 1. 查出用户加入的所有群（状态=1）
        List<GroupMember> memberships = memberMapper.selectList(
                new LambdaQueryWrapper<GroupMember>()
                        .eq(GroupMember::getUserId, userId)
                        .eq(GroupMember::getStatus, 1)
        );

        // 2. 逐个查群信息
        return memberships.stream().map(m -> {
            Group group = groupMapper.selectById(m.getGroupId());
            if (group == null || group.getStatus() == 2) return null;

            GroupVO vo = new GroupVO();
            vo.setGroupId(group.getId());
            vo.setName(group.getName());
            vo.setOwnerId(group.getOwnerId());
            vo.setNotice(group.getNotice());
            vo.setCreateTime(group.getCreateTime());

            // 查群主昵称
            User owner = userMapper.selectById(group.getOwnerId());
            vo.setOwnerNickname(owner != null ? owner.getNickname() : "未知");

            // 统计当前成员数
            Long count = memberMapper.selectCount(
                    new LambdaQueryWrapper<GroupMember>()
                            .eq(GroupMember::getGroupId, group.getId())
                            .eq(GroupMember::getStatus, 1)
            );
            vo.setMemberCount(count.intValue());
            return vo;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    // 群成员列表
    public List<GroupMemberVO> getMembers(Long groupId) {
        // 1. 查出所有在群成员（status=1）
        List<GroupMember> members = memberMapper.selectList(
                new LambdaQueryWrapper<GroupMember>()
                        .eq(GroupMember::getGroupId, groupId)
                        .eq(GroupMember::getStatus, 1)
        );

        // 2. 查每个成员的用户信息
        return members.stream().map(m -> {
            User user = userMapper.selectById(m.getUserId());
            GroupMemberVO vo = new GroupMemberVO();
            vo.setUserId(m.getUserId());
            vo.setNickname(user != null ? user.getNickname() : "未知");
            vo.setAvatar(user != null ? user.getAvatar() : null);
            vo.setRole(m.getRole());
            vo.setAlias(m.getAlias());
            return vo;
        }).collect(Collectors.toList());
    }

    // 修改群信息（仅群主可操作）
    public void updateGroup(Long userId, Long groupId, UpdateGroup req) {
        Group group = groupMapper.selectById(groupId);
        if (group == null) {
            throw new RuntimeException("群不存在");
        }
        if (!group.getOwnerId().equals(userId)) {
            throw new RuntimeException("只有群主才能修改群信息");
        }

        if (req.getName() != null) group.setName(req.getName());
        if (req.getNotice() != null) group.setNotice(req.getNotice());
        groupMapper.updateById(group);
    }

    // 解散群（仅群主可操作）
    public void dismissGroup(Long userId, Long groupId) {
        Group group = groupMapper.selectById(groupId);
        if (group == null) {
            throw new RuntimeException("群不存在");
        }
        if (!group.getOwnerId().equals(userId)) {
            throw new RuntimeException("只有群主才能解散群");
        }

        group.setStatus(2);  // 已解散
        groupMapper.updateById(group);

        // 将所有成员状态改为已退群
        List<GroupMember> members = memberMapper.selectList(
                new LambdaQueryWrapper<GroupMember>()
                        .eq(GroupMember::getGroupId, groupId)
                        .eq(GroupMember::getStatus, 1)
        );
        for (GroupMember m : members) {
            m.setStatus(2);
            m.setQuitTime(LocalDateTime.now());
            memberMapper.updateById(m);
        }
    }
}
