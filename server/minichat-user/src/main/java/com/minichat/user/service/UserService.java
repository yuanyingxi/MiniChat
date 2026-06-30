package com.minichat.user.service;

import com.minichat.user.dto.UpdateUserRequest;
import com.minichat.user.dto.UserInfoResponse;
import com.minichat.user.entity.User;
import com.minichat.user.mapper.UserMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
}
