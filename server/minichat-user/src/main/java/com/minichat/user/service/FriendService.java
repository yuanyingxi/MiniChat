package com.minichat.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.minichat.user.dto.FriendVO;
import com.minichat.user.dto.HandleFriendRequest;
import com.minichat.user.dto.SendFriendRequest;
import com.minichat.user.dto.UserInfoResponse;
import com.minichat.user.entity.Friend;
import com.minichat.user.entity.FriendRequest;
import com.minichat.user.entity.User;
import com.minichat.user.entity.UserDocument;
import com.minichat.user.mapper.FriendMapper;
import com.minichat.user.mapper.FriendRequestMapper;
import com.minichat.user.mapper.UserMapper;
import com.minichat.user.repository.UserSearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FriendService {
    @Autowired
    private FriendMapper friendMapper;

    @Autowired
    private FriendRequestMapper requestMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserSearchRepository searchRepository;

    // 发送好友请求
    public void sendRequest(Long fromId, SendFriendRequest req) {
        // 1. 不能加自己
        if (fromId.equals(req.getToId())) {
            throw new RuntimeException("不能添加自己为好友");
        }

        // 2. 对方用户必须存在
        User toUser = userMapper.selectById(req.getToId());
        if (toUser == null) {
            throw new RuntimeException("用户不存在");
        }

        // 3. 是否已经是好友
        Friend existing = friendMapper.selectOne(
                new LambdaQueryWrapper<Friend>()
                        .eq(Friend::getUserId, fromId)
                        .eq(Friend::getFriendId, req.getToId())
                        .eq(Friend::getStatus, 1)
        );
        if (existing != null) {
            throw new RuntimeException("对方已经是你的好友");
        }

        // 4. 是否有待处理的请求（防止重复发送）
        Long count = requestMapper.selectCount(
                new LambdaQueryWrapper<FriendRequest>()
                        .eq(FriendRequest::getFromId, fromId)
                        .eq(FriendRequest::getToId, req.getToId())
                        .eq(FriendRequest::getStatus, 0)
        );
        if (count > 0) {
            throw new RuntimeException("已发送过好友请求，请等待对方处理");
        }

        // 5. 创建请求记录
        FriendRequest request = new FriendRequest();
        // id 由雪花算法自动生成
        request.setFromId(fromId);
        request.setToId(req.getToId());
        request.setRemark(req.getRemark());
        request.setStatus(0);  // 待处理
        requestMapper.insert(request);
    }

    // 同意好友请求（A→B 和 B→A 各插一条记录）
    @Transactional
    public void acceptRequest(Long userId, HandleFriendRequest req) {
        FriendRequest request = requestMapper.selectById(req.getRequestId());
        if (request == null) {
            throw new RuntimeException("请求不存在");
        }
        // 只有接收方才能操作
        if (!request.getToId().equals(userId)) {
            throw new RuntimeException("无权操作此请求");
        }
        if (request.getStatus() != 0) {
            throw new RuntimeException("请求已处理");
        }

        // 1. 更新请求状态为「已同意」
        request.setStatus(1);
        requestMapper.updateById(request);

        // 2. 插入两条好友关系（双向）
        Friend friendA = new Friend();
        friendA.setUserId(request.getFromId());
        friendA.setFriendId(request.getToId());
        friendA.setStatus(1);
        friendMapper.insert(friendA);

        Friend friendB = new Friend();
        friendB.setUserId(request.getToId());
        friendB.setFriendId(request.getFromId());
        friendB.setStatus(1);
        friendMapper.insert(friendB);
    }

    // 拒绝好友请求
    public void rejectRequest(Long userId, HandleFriendRequest req) {
        FriendRequest request = requestMapper.selectById(req.getRequestId());
        if (request == null) {
            throw new RuntimeException("请求不存在");
        }
        if (!request.getToId().equals(userId)) {
            throw new RuntimeException("无权操作此请求");
        }
        if (request.getStatus() != 0) {
            throw new RuntimeException("请求已处理");
        }

        request.setStatus(2);  // 拒绝
        requestMapper.updateById(request);
    }

    // 查看收到的待处理请求
    public List<FriendRequest> getIncomingRequests(Long userId) {
        return requestMapper.selectList(
                new LambdaQueryWrapper<FriendRequest>()
                        .eq(FriendRequest::getToId, userId)
                        .eq(FriendRequest::getStatus, 0)
                        .orderByDesc(FriendRequest::getCreateTime)
        );
    }

    // 好友列表
    public List<FriendVO> getFriendList(Long userId) {
        // 1. 查用户的所有好友关系
        List<Friend> friends = friendMapper.selectList(
                new LambdaQueryWrapper<Friend>()
                        .eq(Friend::getUserId, userId)
                        .eq(Friend::getStatus, 1)
        );

        // 2. 逐个查好友信息
        return friends.stream().map(f -> {
            User friendUser = userMapper.selectById(f.getFriendId());
            FriendVO vo = new FriendVO();
            vo.setFriendId(f.getFriendId());
            vo.setNickname(friendUser != null ? friendUser.getNickname() : "已注销");
            vo.setAvatar(friendUser != null ? friendUser.getAvatar() : null);
            vo.setRemark(f.getRemark());
            vo.setCreateTime(f.getCreateTime());
            return vo;
        }).collect(Collectors.toList());
    }

    // 删除好友（单向）
    public void deleteFriend(Long userId, Long friendId) {
        // 删除 A→B 方向
        // B 的好友列表里仍然有 A
        Friend friend = friendMapper.selectOne(
                new LambdaQueryWrapper<Friend>()
                        .eq(Friend::getUserId, userId)
                        .eq(Friend::getFriendId, friendId)
        );
        if (friend != null) {
            friend.setStatus(0);
            friendMapper.updateById(friend);
        }
    }

    // 拉黑/取消拉黑（开关）
    public void toggleBlock(Long userId, Long friendId) {
        Friend friend = friendMapper.selectOne(
                new LambdaQueryWrapper<Friend>()
                        .eq(Friend::getUserId, userId)
                        .eq(Friend::getFriendId, friendId)
        );
        if (friend == null) {
            throw new RuntimeException("不是好友关系");
        }
        // 切换：1→2（拉黑），2→1（取消拉黑）
        friend.setStatus(friend.getStatus() == 2 ? 1 : 2);
        friendMapper.updateById(friend);
    }

    // 全局搜索用户（ES + MySQL 兜底）
    public List<UserInfoResponse> searchUsers(Long userId, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new RuntimeException("搜索关键词不能为空");
        }

        // 先用 ES 搜索
        List<UserDocument> docs = searchRepository.findByNickname(keyword);

        // ES 无结果时用 MySQL 兜底（支持手机号搜索）
        if (docs.isEmpty()) {
            return userMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                    .and(w -> w.like(User::getPhone, keyword).or().like(User::getNickname, keyword))
                    .ne(User::getId, userId)
                    .last("LIMIT 20")
            ).stream().map(user -> {
                UserInfoResponse vo = new UserInfoResponse();
                vo.setId(user.getId());
                vo.setNickname(user.getNickname());
                vo.setPhone(user.getPhone());
                vo.setAvatar(user.getAvatar());
                return vo;
            }).collect(Collectors.toList());
        }

        return docs.stream()
                .filter(doc -> !doc.getId().equals(userId))
                .limit(20)
                .map(doc -> {
                    UserInfoResponse vo = new UserInfoResponse();
                    vo.setId(doc.getId());
                    vo.setNickname(doc.getNickname());
                    vo.setPhone(doc.getPhone());
                    vo.setAvatar(doc.getAvatar());
                    return vo;
                }).collect(Collectors.toList());
    }
}
