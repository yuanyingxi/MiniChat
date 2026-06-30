package com.minichat.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("tb_friend_request")
public class FriendRequest {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long fromId;
    private Long toId;
    private String remark;
    private Integer status;      // 0待处理 1同意 2拒绝 3忽略
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

}
