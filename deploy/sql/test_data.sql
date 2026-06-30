-- 清掉之前损坏的测试数据
DELETE FROM tb_friend WHERE user_id IN (2071578947752329219, 2071578947752329220, 2071578947752329221, 2071578947752329222, 2071578947752329223) OR friend_id IN (2071578947752329219, 2071578947752329220, 2071578947752329221, 2071578947752329222, 2071578947752329223);
DELETE FROM tb_user WHERE id IN (2071578947752329219, 2071578947752329220, 2071578947752329221, 2071578947752329222, 2071578947752329223);

-- 插入 5 个测试用户
INSERT INTO tb_user (id, phone, password_hash, nickname, avatar, signature, gender, status, create_time, update_time) VALUES
(2071578947752329219, '13800000001', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '张三', 'https://api.dicebear.com/7.x/adventurer/svg?seed=zhangsan', '今天天气不错', 1, 1, NOW(), NOW()),
(2071578947752329220, '13800000002', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '李四', 'https://api.dicebear.com/7.x/adventurer/svg?seed=lisi', '学习使我快乐', 1, 1, NOW(), NOW()),
(2071578947752329221, '13800000003', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '王五', 'https://api.dicebear.com/7.x/adventurer/svg?seed=wangwu', '加油！', 2, 1, NOW(), NOW()),
(2071578947752329222, '13800000004', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '赵六', 'https://api.dicebear.com/7.x/adventurer/svg?seed=zhaoliu', '永远年轻', 1, 1, NOW(), NOW()),
(2071578947752329223, '13800000005', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '孙七', 'https://api.dicebear.com/7.x/adventurer/svg?seed=sunqi', '全栈工程师', 1, 1, NOW(), NOW());

-- 好友关系（双向）
INSERT INTO tb_friend (user_id, friend_id, remark, status, create_time) VALUES
(2071578947752329218, 2071578947752329219, '小张', 1, NOW()),
(2071578947752329219, 2071578947752329218, '', 1, NOW()),
(2071578947752329218, 2071578947752329220, '老李', 1, NOW()),
(2071578947752329220, 2071578947752329218, '', 1, NOW()),
(2071578947752329218, 2071578947752329223, '孙七', 1, NOW()),
(2071578947752329223, 2071578947752329218, '', 1, NOW());
