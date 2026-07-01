package com.minichat.user.controller;

import com.minichat.common.dto.InternalGroupResponse;
import com.minichat.user.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/internal/group")
public class InternalGroupController {

    @Autowired
    private GroupService groupService;

    @GetMapping("/{groupId}/members")
    public List<InternalGroupResponse> getMembers(@PathVariable Long groupId) {
        return groupService.getMembersInternal(groupId).stream()
                .map(vo -> new InternalGroupResponse(vo.getUserId(), vo.getNickname(), vo.getAvatar()))
                .toList();
    }
}
