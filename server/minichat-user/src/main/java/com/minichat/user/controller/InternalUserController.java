package com.minichat.user.controller;

import com.minichat.common.dto.InternalUserResponse;
import com.minichat.user.entity.User;
import com.minichat.user.mapper.UserMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 内部服务调用接口（不对外暴露，不走网关权限）
 *
 * 设计原则：内部 RPC 直接返回裸对象，不包 Result<T>。
 * 调用方只用判 null，不用 .getData()，符合本地调用习惯。
 */
@RestController
@RequestMapping("/internal/user")
public class InternalUserController {

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/{id}")
    public InternalUserResponse getUserById(@PathVariable("id") Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return null;
        }
        InternalUserResponse resp = new InternalUserResponse();
        BeanUtils.copyProperties(user, resp);
        return resp;
    }
}
