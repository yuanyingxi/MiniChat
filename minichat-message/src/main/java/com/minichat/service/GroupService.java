package com.minichat.service;

import com.minichat.dto.CreateGroupReq;
import com.minichat.entity.GroupMember;

import java.util.List;

public interface GroupService {

    Long createGroup(CreateGroupReq req);

    void joinGroup(Long groupId);

    List<GroupMember> getMembers(Long groupId);
}
