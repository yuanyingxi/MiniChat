package com.minichat.user.service;

import com.minichat.user.dto.UpdateUserRequest;
import com.minichat.user.dto.UserInfoResponse;
import com.minichat.user.entity.User;
import com.minichat.user.mapper.UserMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    // 查询
    public UserInfoResponse getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        UserInfoResponse resp = new UserInfoResponse();
        BeanUtils.copyProperties(user, resp);
        return resp;
    }

    // 更新
    public void updateUser(Long userId, UpdateUserRequest req) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        User update = new User();
        update.setId(userId);
        if (req.getNickname() != null) update.setNickname(req.getNickname());
        if (req.getAvatar() != null) update.setAvatar(req.getAvatar());
        if (req.getSignature() != null) update.setSignature(req.getSignature());
        if (req.getGender() != null) update.setGender(req.getGender());
        userMapper.updateById(update);
    }
}
