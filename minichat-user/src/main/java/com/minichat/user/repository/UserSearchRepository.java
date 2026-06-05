package com.minichat.user.repository;

import com.minichat.user.entity.UserDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserSearchRepository extends ElasticsearchRepository<UserDocument, Long> {

    // 按昵称搜索用户（ES 自动实现，使用 match 查询）
    List<UserDocument> findByNickname(String nickname);
}
