package com.minichat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.minichat.entity.Group;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GroupMapper
        extends BaseMapper<Group> {
}
