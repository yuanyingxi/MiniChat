# MiniChat

基于 Spring Cloud Alibaba 微服务 + Vue 3 的即时通讯系统。

## 项目结构

```
MiniChat/
├── client/                     # Vue 3 前端
│   └── src/
│       ├── api/                # API + WebSocket 封装
│       ├── components/         # Vue 组件（聊天窗口、好友列表等）
│       ├── stores/             # Pinia 状态管理
│       ├── types/              # TypeScript 类型定义
│       └── utils/              # axios 请求拦截
├── server/                     # Spring Boot 后端
│   ├── pom.xml                 # Maven 父工程
│   ├── minichat-common/        # 公共模块（DTO、Result、常量）
│   ├── minichat-gateway/       # API 网关 :8080
│   ├── minichat-user/          # 用户服务 :8081
│   ├── minichat-message/       # 消息服务 :8082
│   ├── start.ps1               # 一键编译 + 启动（PowerShell）
│   └── rebuild.ps1             # 仅编译
└── deploy/                     # Docker 中间件编排
    ├── docker-compose.yml
    ├── .env / .env.example     # 环境变量
    ├── sql/                    # 初始化 SQL
    └── config/                 # Nacos / Nginx / RocketMQ 配置
```

## 技术栈

| 层 | 技术 |
|----|------|
| 前端 | Vue 3 + TypeScript + Pinia + Element Plus + Axios + WebSocket |
| 网关 | Spring Cloud Gateway + Nacos 注册中心 + JWT 鉴权 |
| 用户服务 | Spring Boot 3.2 + MyBatis-Plus + MySQL + Redis + Elasticsearch |
| 消息服务 | Spring Boot 3.2 + WebSocket + RocketMQ + Elasticsearch + OSS |
| 中间件 | Nacos / MySQL 8 / Redis 7 / RocketMQ 5.1 / ES 7.17 / Nginx |

## 快速启动

### 1. 启动中间件

确保 Docker Desktop 已运行：

```bash
cd deploy
cp .env.example .env          # 首次，按需修改密钥
docker compose up -d           # 启动全部中间件
```

### 2. 编译并启动后端

```powershell
cd server
.\rebuild.ps1                  # 编译全部模块
.\start.ps1                    # 编译 + 启动（3 个终端窗口）
```

三个服务端口：**Gateway 8080** / **User 8081** / **Message 8082**

### 3. 启动前端

```bash
cd client
npm install                    # 首次
npm run dev                    # http://localhost:5173
```

### 4. 初始化数据库表

```bash
# 用户相关表
docker exec -i minichat-mysql mysql -uroot -pyour_secure_password --default-character-set=utf8mb4 minichat_db < server/minichat-user/deploy/sql/init.sql
```

## 默认账号密码

| 服务 | 地址 | 用户/密码 |
|------|------|-----------|
| 前端 | http://localhost:5173 | — |
| 网关 | http://localhost:8080 | — |
| Nacos | http://localhost:8848/nacos | nacos/nacos |
| MySQL | localhost:3306 | root / your_secure_password |
| Redis | localhost:6379 | 密码 your_secure_password |

## 网关路由

| 路径 | 转发服务 |
|------|----------|
| `/auth/**`, `/user/**`, `/friend/**`, `/group/**` | minichat-user |
| `/message/**`, `/oss/**`, `/ws/**` | minichat-message |

## 核心架构

- **鉴权** — JWT，登录后 token 存 localStorage，axios 自动带 `Authorization: Bearer xxx`
- **WebSocket** — `ws://localhost:8080/ws?token=xxx`，网关验 JWT 后透传 userId
- **消息收发** — WS 发送 → ACK → RocketMQ 异步推送 → MessagePusher 推在线用户
- **文件存储** — 阿里云 OSS（广州 Bucket），上传返回永久 URL 写入消息体
- **雪花 ID** — 后端 Long → Jackson 序列化为 String，前端避免精度丢失
- **搜索** — ES 优先，MySQL LIKE 兜底（支持手机号 + 昵称搜索）
- **内部接口** — 需要查用户资料时，通过 Feign 调用 `GET /internal/user/{id}`（返回裸对象，不包 Result）
