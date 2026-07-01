package com.minichat.user.controller;

import com.minichat.common.result.Result;
import com.minichat.user.dto.CreateGroup;
import com.minichat.user.dto.GroupMemberVO;
import com.minichat.user.dto.GroupVO;
import com.minichat.user.dto.UpdateGroup;
import com.minichat.user.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/group")
@Tag(name = "群组管理")
public class GroupController {

    @Autowired
    private GroupService groupService;

    @PostMapping
    @Operation(summary = "创建群")
    public Result<Long> createGroup(@RequestHeader("userId") Long userId,
                                    @Valid @RequestBody CreateGroup req) {
        return Result.success(groupService.createGroup(userId, req));
    }

    @PostMapping("/{groupId}/quit")
    @Operation(summary = "退出群")
    public Result<Void> quitGroup(@RequestHeader("userId") Long userId,
                                  @PathVariable Long groupId) {
        groupService.quitGroup(userId, groupId);
        return Result.success(null);
    }

    @PostMapping("/{groupId}/kick/{memberId}")
    @Operation(summary = "踢出群成员（群主操作）")
    public Result<Void> kickMember(@RequestHeader("userId") Long userId,
                                   @PathVariable Long groupId,
                                   @PathVariable Long memberId) {
        groupService.kickMember(userId, groupId, memberId);
        return Result.success(null);
    }

    @GetMapping("/list")
    @Operation(summary = "我的群列表")
    public Result<List<GroupVO>> getMyGroups(@RequestHeader("userId") Long userId) {
        return Result.success(groupService.getMyGroups(userId));
    }

    @GetMapping("/{groupId}/members")
    @Operation(summary = "群成员列表")
    public Result<List<GroupMemberVO>> getMembers(@RequestHeader("userId") Long userId,
                                                  @PathVariable Long groupId) {
        return Result.success(groupService.getMembers(userId, groupId));
    }

    @PutMapping("/{groupId}")
    @Operation(summary = "修改群信息（群主操作）")
    public Result<Void> updateGroup(@RequestHeader("userId") Long userId,
                                    @PathVariable Long groupId,
                                    @Valid @RequestBody UpdateGroup req) {
        groupService.updateGroup(userId, groupId, req);
        return Result.success(null);
    }

    @DeleteMapping("/{groupId}")
    @Operation(summary = "解散群（群主操作）")
    public Result<Void> dismissGroup(@RequestHeader("userId") Long userId,
                                     @PathVariable Long groupId) {
        groupService.dismissGroup(userId, groupId);
        return Result.success(null);
    }
}
