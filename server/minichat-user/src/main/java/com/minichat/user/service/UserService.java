package com.minichat.user.service;

import com.minichat.user.dto.ChangePasswordRequest;
import com.minichat.user.dto.UpdateUserRequest;
import com.minichat.user.dto.UserInfoResponse;
import com.minichat.user.entity.User;
import com.minichat.user.mapper.UserMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserIndexService userIndexService;

    @Autowired
    private FriendService friendService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private PasswordEncoder passwordEncoder;

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

        // 同步 ES 索引：重新写入完整用户文档
        User updated = userMapper.selectById(userId);
        userIndexService.indexUser(updated);
    }

    /**
     * 账号注销（软删除）
     * 1. User 表 status = 3（注销）
     * 2. ES 索引删除该用户文档
     * 3. 清理好友关系 + 好友请求
     * 4. 退出所有群（群主身份则解散群）
     */
    @Transactional
    public void cancelAccount(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (user.getStatus() != null && user.getStatus() == 3) {
            throw new RuntimeException("账号已注销，无需重复操作");
        }

        // 1. 标记为注销状态
        User update = new User();
        update.setId(userId);
        update.setStatus(3);
        userMapper.updateById(update);

        // 2. 删 ES 索引
        userIndexService.removeUser(userId);

        // 3. 清理好友关系（双向）
        friendService.removeAllForUser(userId);

        // 4. 退出所有群（保留历史）
        groupService.quitAllGroupsForUser(userId);
    }

    /**
     * 修改密码
     * 1. 校验旧密码
     * 2. BCrypt 加密新密码
     * 3. 更新 passwordHash
     */
    public void changePassword(Long userId, ChangePasswordRequest req) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 校验旧密码
        if (!passwordEncoder.matches(req.getOldPassword(), user.getPasswordHash())) {
            throw new RuntimeException("旧密码错误");
        }

        // 新密码不能和旧密码相同
        if (passwordEncoder.matches(req.getNewPassword(), user.getPasswordHash())) {
            throw new RuntimeException("新密码不能与旧密码相同");
        }

        // 更新密码
        User update = new User();
        update.setId(userId);
        update.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        userMapper.updateById(update);
    }
}
