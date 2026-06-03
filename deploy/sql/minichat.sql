--
-- Table structure for table `tb_user`
--

DROP TABLE IF EXISTS `tb_user`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_user`
(
    `id`               bigint                                  NOT NULL COMMENT '用户ID（雪花算法）',
    `phone`            varchar(20) COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '手机号，用于登录',
    `password_hash`    varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '加密后的密码（可为空，支持短信验证码登录）',
    `nickname`         varchar(50) COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '用户昵称',
    `avatar`           varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像URL（OSS）',
    `signature`        varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '个性签名',
    `gender`           tinyint                                 DEFAULT '0' COMMENT '性别 0未知 1男 2女',
    `status`           tinyint                                 NOT NULL DEFAULT '1' COMMENT '状态 1正常 2封禁 3注销',
    `last_login_time`  datetime                                DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip`    varchar(64) COLLATE utf8mb4_unicode_ci  DEFAULT NULL COMMENT '最后登录IP',
    `create_time`      datetime                                NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    `update_time`      datetime                                NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_phone` (`phone`),
    KEY `idx_create_time` (`create_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户基本信息表';
/*!40101 SET character_set_client = @saved_cs_client */;


