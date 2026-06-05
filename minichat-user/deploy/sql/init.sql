-- =====================================================
-- MiniChat User Service Database Schema
-- 数据库名: minichat_user_db
-- 所属微服务: minichat-user
-- =====================================================

CREATE DATABASE IF NOT EXISTS `minichat_user_db`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE `minichat_user_db`;

-- 用户基本信息表
CREATE TABLE `tb_user`
(
    `id`              bigint      NOT NULL COMMENT '用户ID（雪花算法）',
    `phone`           varchar(20) NOT NULL COMMENT '手机号，用于登录',
    `password_hash`   varchar(255)         DEFAULT NULL COMMENT '加密后的密码',
    `nickname`        varchar(50) NOT NULL COMMENT '用户昵称',
    `avatar`          varchar(255)         DEFAULT NULL COMMENT '头像URL（OSS）',
    `signature`       varchar(255)         DEFAULT NULL COMMENT '个性签名',
    `gender`          tinyint              DEFAULT '0' COMMENT '性别 0未知 1男 2女',
    `status`          tinyint     NOT NULL DEFAULT '1' COMMENT '状态 1正常 2封禁 3注销',
    `last_login_time` datetime             DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip`   varchar(64)          DEFAULT NULL COMMENT '最后登录IP',
    `create_time`     datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    `update_time`     datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_phone` (`phone`),
    KEY `idx_create_time` (`create_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户基本信息表';

-- 好友关系表
CREATE TABLE `tb_friend`
(
    `id`          bigint   NOT NULL AUTO_INCREMENT COMMENT '关系ID',
    `user_id`     bigint   NOT NULL COMMENT '请求方 (好友) ID',
    `friend_id`   bigint   NOT NULL COMMENT '好友ID',
    `remark`      varchar(50)       DEFAULT NULL COMMENT '好友备注',
    `status`      tinyint  NOT NULL DEFAULT '1' COMMENT '状态 1正常 0删除 2拉黑',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_friend` (`user_id`, `friend_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='好友关系表';

-- 好友请求表
CREATE TABLE `tb_friend_request`
(
    `id`          bigint   NOT NULL AUTO_INCREMENT COMMENT '请求ID',
    `from_id`     bigint   NOT NULL COMMENT '发送方',
    `to_id`       bigint   NOT NULL COMMENT '接收方',
    `remark`      varchar(100)      DEFAULT NULL COMMENT '申请附言',
    `status`      tinyint  NOT NULL DEFAULT '0' COMMENT '状态 0待处理 1同意 2拒绝 3忽略',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_to_id_status` (`to_id`, `status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='好友申请表';

-- 群组表
CREATE TABLE `tb_group`
(
    `id`          bigint      NOT NULL COMMENT '群ID',
    `name`        varchar(50) NOT NULL COMMENT '群名称',
    `owner_id`    bigint      NOT NULL COMMENT '群主ID',
    `notice`      varchar(500)         DEFAULT NULL COMMENT '群公告',
    `max_members` int         NOT NULL DEFAULT '200' COMMENT '最大成员数',
    `status`      tinyint     NOT NULL DEFAULT '1' COMMENT '状态 1正常 2已解散',
    `create_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后活跃时间',
    PRIMARY KEY (`id`),
    KEY `idx_owner_id` (`owner_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='群组表';

-- 群成员表
CREATE TABLE `tb_group_member`
(
    `id`        bigint   NOT NULL AUTO_INCREMENT COMMENT '成员关系ID',
    `group_id`  bigint   NOT NULL COMMENT '群ID',
    `user_id`   bigint   NOT NULL COMMENT '用户ID',
    `role`      tinyint  NOT NULL DEFAULT '0' COMMENT '角色 0普通成员 1管理员 2群主',
    `alias`     varchar(50)       DEFAULT NULL COMMENT '群昵称',
    `status`    tinyint  NOT NULL DEFAULT '1' COMMENT '状态 1正常 2已退群 3被踢出',
    `join_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `quit_time` datetime          DEFAULT NULL COMMENT '退群/被踢时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_group_user` (`group_id`, `user_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='群成员表';