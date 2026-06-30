package com.minichat.user.controller;

import com.minichat.common.result.Result;
import com.minichat.user.dto.UpdateUserRequest;
import com.minichat.user.dto.UserInfoResponse;
import com.minichat.user.service.FileService;
import com.minichat.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/user")
@Tag(name = "用户资料")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private FileService fileService;

    @GetMapping("/{id}")
    @Operation(summary = "查询用户资料")
    public Result<UserInfoResponse> getUserInfo(@PathVariable Long id) {
        return Result.success(userService.getUserInfo(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改用户资料")
    public Result<Void> updateUser(@PathVariable Long id,
                                   @Valid @RequestBody UpdateUserRequest req) {
        userService.updateUser(id, req);
        return Result.success(null);
    }

    @PostMapping("/avatar")
    @Operation(summary = "上传头像（OSS）")
    public Result<String> uploadAvatar(@RequestHeader("userId") Long userId,
                                       @RequestParam("file") MultipartFile file) {
        String url = fileService.uploadAvatar(userId, file);
        return Result.success(url);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "账号注销（软删除）")
    public Result<Void> cancelAccount(@PathVariable Long id,
                                      @RequestHeader("userId") Long userId) {
        if (!id.equals(userId)) {
            return Result.error(403, "只能注销自己的账号");
        }
        userService.cancelAccount(userId);
        return Result.success(null);
    }
}
