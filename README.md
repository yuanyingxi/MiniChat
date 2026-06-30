# MiniChat

基于 Spring Cloud Alibaba 微服务 + Vue 3 的即时通讯系统。

## 🏗 项目结构

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
    └── config/                 # Nacos / Nginx / RocketMQ 配置
```

## 🛠 项目技术栈与版本

### 框架版本

| 框架                   | 版本         |
|----------------------|------------|
| Java                 | 21         |
| Spring Boot          | 3.2.5      |
| Spring Cloud         | 2023.0.2   |
| Spring Cloud Alibaba | 2023.0.1.0 |

### 中间件版本

| 层 | 技术 |
|----|------|
| 前端 | Vue 3 + TypeScript + Pinia + Element Plus + Axios + WebSocket |
| 网关 | Spring Cloud Gateway + Nacos 注册中心 + JWT 鉴权 |
| 用户服务 | Spring Boot 3.2 + MyBatis-Plus + MySQL + Redis + Elasticsearch |
| 消息服务 | Spring Boot 3.2 + WebSocket + RocketMQ + Elasticsearch + OSS |
| 中间件 | Nacos / MySQL 8 / Redis 7 / RocketMQ 5.1 / ES 7.17 / Nginx |

### 第三方依赖版本

| 依赖           | 版本     | 用途         |
|--------------|--------|------------|
| MyBatis-Plus | 3.5.7  | ORM 框架     |
| Hutool       | 5.8.28 | Java 工具类库  |
| 阿里云 OSS      | 3.17.4 | 文件存储       |
| Knife4j      | 4.4.0  | API 文档     |
| Redisson     | 3.34.0 | Redis 分布式锁 |

## 🚀 本地开发环境搭建

### 前置条件

| 工具                  | 要求           | 验证命令                    |
|---------------------|--------------|-------------------------|
| JDK                 | 21           | `java -version`         |
| Maven               | 3.9          | `mvn --version`         |
| Docker Desktop      | 稳定版          | `docker --version`      |


### 启动步骤

#### 1. 启动中间件

```bash
# 确保 Docker Desktop 已启动

# 切换到 main 分支并拉取最新代码
git checkout main
git pull origin main

# 进入 deploy 目录
cd deploy

# 从模板创建环境配置（首次运行）
cp .env.example .env

# 一键启动所有中间件
docker-compose up -d
```

#### 2. 编译并启动后端

```bash
powershell
cd server
.\rebuild.ps1                  # 编译全部模块
.\start.ps1                    # 编译 + 启动（3 个终端窗口）
```

三个服务端口：**Gateway 8080** / **User 8081** / **Message 8082**



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

- **不允许直接操作 main 分支**，必须在 feature 分支开发
- 改完测试全部通过后，通过 PR 合并到 main
- 当改动了【环境、数据库、公共架构】，必须在 PR 里写清楚通知，并在群里同步
- 合并前执行 `mvn clean compile` 确保编译通过
- 新的依赖统一写在父 pom.xml 里面，然后子 pom.xml 继承，方便看整个项目上技术栈

## 🔐 敏感配置约定

每个服务的 `application.yml` 都不允许出现明文密码/密钥，统一用 `${ENV:默认值}` 形式。

**加载顺序（高优先级覆盖低优先级）：**

```
Nacos 远程配置 > 环境变量 > application-secret.yml > application.yml 默认值
```

## 提醒

> **前端开发**：查看路由，直接 pull user 模块，然后启动访问 http://localhost:8081/doc.html ；或者用 api-docs.json 也行，用插件或者导入 apifox 都行。
>
> **其他模块开发**：需要查用户资料时，通过 Feign 调用 `GET /internal/user/{id}`（返回裸对象，不包 Result）。`InternalUserResponse` 定义在 `minichat-common`。后续网关建好后统一走网关，内部接口不走网关。
