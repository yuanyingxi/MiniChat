package com.minichat.user.service;

import com.minichat.user.entity.User;
import com.minichat.user.entity.UserDocument;
import com.minichat.user.repository.UserSearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserIndexService {

    @Autowired
    private UserSearchRepository searchRepository;

    /**
     * 把用户信息写入 ES 索引
     * 注册成功后调用此方法
     */
    public void indexUser(User user) {
        UserDocument doc = new UserDocument();
        doc.setId(user.getId());
        doc.setNickname(user.getNickname());
        doc.setPhone(user.getPhone());
        doc.setAvatar(user.getAvatar());
        doc.setSignature(user.getSignature());
        doc.setGender(user.getGender());
        doc.setCreateTime(user.getCreateTime());
        searchRepository.save(doc);
    }
}
