package com.minichat.user.controller;

import com.minichat.common.result.Result;
import com.minichat.user.dto.FriendVO;
import com.minichat.user.dto.HandleFriendRequest;
import com.minichat.user.dto.SendFriendRequest;
import com.minichat.user.entity.FriendRequest;
import com.minichat.user.dto.UserInfoResponse;
import com.minichat.user.service.FriendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/friend")
@Tag(name = "好友管理")
public class FriendController {
    @Autowired
    private FriendService friendService;

    @PostMapping("/request")
    @Operation(summary = "发送好友请求")
    public Result<Void> sendRequest(@RequestHeader("userId") Long userId,
                                    @Valid @RequestBody SendFriendRequest req) {
        friendService.sendRequest(userId, req);
        return Result.success(null);
    }

    @PostMapping("/request/{id}/accept")
    @Operation(summary = "同意好友请求")
    public Result<Void> acceptRequest(@RequestHeader("userId") Long userId,
                                      @PathVariable("id") Long id) {
        HandleFriendRequest dto = new HandleFriendRequest();
        dto.setRequestId(id);
        friendService.acceptRequest(userId, dto);
        return Result.success(null);
    }

    @PostMapping("/request/{id}/reject")
    @Operation(summary = "拒绝好友请求")
    public Result<Void> rejectRequest(@RequestHeader("userId") Long userId,
                                      @PathVariable("id") Long id) {
        HandleFriendRequest dto = new HandleFriendRequest();
        dto.setRequestId(id);
        friendService.rejectRequest(userId, dto);
        return Result.success(null);
    }

    @GetMapping("/requests")
    @Operation(summary = "查看待处理的好友请求")
    public Result<List<FriendRequest>> getRequests(@RequestHeader("userId") Long userId) {
        return Result.success(friendService.getIncomingRequests(userId));
    }

    @GetMapping("/list")
    @Operation(summary = "好友列表")
    public Result<List<FriendVO>> getFriendList(@RequestHeader("userId") Long userId) {
        return Result.success(friendService.getFriendList(userId));
    }

    @DeleteMapping("/{friendId}")
    @Operation(summary = "删除好友")
    public Result<Void> deleteFriend(@RequestHeader("userId") Long userId,
                                     @PathVariable("friendId") Long friendId) {
        friendService.deleteFriend(userId, friendId);
        return Result.success(null);
    }

    @PostMapping("/{friendId}/block")
    @Operation(summary = "拉黑/取消拉黑")
    public Result<Void> toggleBlock(@RequestHeader("userId") Long userId,
                                    @PathVariable("friendId") Long friendId) {
        friendService.toggleBlock(userId, friendId);
        return Result.success(null);
    }

    @GetMapping("/search")
    @Operation(summary = "全局搜索用户（ES）")
    public Result<List<UserInfoResponse>> searchUsers(
            @RequestHeader("userId") Long userId,
            @RequestParam("keyword") String keyword) {
        return Result.success(friendService.searchUsers(userId, keyword));
    }
}
