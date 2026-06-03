package com.minichat.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.minichat.dto.CreateGroupReq;
import com.minichat.entity.Group;
import com.minichat.entity.GroupMember;
import com.minichat.mapper.GroupMapper;
import com.minichat.mapper.GroupMemberMapper;
import com.minichat.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {

    private final GroupMapper groupMapper;
    private final GroupMemberMapper groupMemberMapper;

    @Override
    public Long createGroup(CreateGroupReq req) {

        Long ownerId = 1001L;

        Group group = new Group();

        group.setId(IdUtil.getSnowflakeNextId());
        group.setGroupName(req.getGroupName());
        group.setOwnerId(ownerId);
        group.setMemberCount(req.getMemberIds().size() + 1);

        groupMapper.insert(group);

        GroupMember owner = new GroupMember();

        owner.setId(IdUtil.getSnowflakeNextId());
        owner.setGroupId(group.getId());
        owner.setUserId(ownerId);
        owner.setRole(3);

        groupMemberMapper.insert(owner);

        for (Long userId : req.getMemberIds()) {

            GroupMember member = new GroupMember();

            member.setId(IdUtil.getSnowflakeNextId());
            member.setGroupId(group.getId());
            member.setUserId(userId);
            member.setRole(1);

            groupMemberMapper.insert(member);
        }

        return group.getId();
    }

    @Override
    public void joinGroup(Long groupId) {

        Long userId = 1001L;

        GroupMember member = new GroupMember();

        member.setId(IdUtil.getSnowflakeNextId());
        member.setGroupId(groupId);
        member.setUserId(userId);
        member.setRole(1);

        groupMemberMapper.insert(member);
    }

    @Override
    public List<GroupMember> getMembers(Long groupId) {

        return groupMemberMapper.selectList(
                Wrappers.<GroupMember>lambdaQuery()
                        .eq(
                                GroupMember::getGroupId,
                                groupId
                        )
        );
    }
}
