package com.minichat.controller;

import com.minichat.dto.CreateGroupReq;
import com.minichat.entity.GroupMember;
import com.minichat.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/group")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @PostMapping("/create")
    public Long create(
            @RequestBody CreateGroupReq req
    ) {
        return groupService.createGroup(req);
    }

    @PostMapping("/join")
    public void join(
            @RequestParam Long groupId
    ) {
        groupService.joinGroup(groupId);
    }

    @GetMapping("/members")
    public List<GroupMember> members(
            @RequestParam("groupId") Long groupId
    ) {
        return groupService.getMembers(groupId);
    }
}
