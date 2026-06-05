## 📖 项目简介

MiniChat 是一个基于 Spring Cloud Alibaba 微服务架构的即时通讯系统。

## 🏗 项目结构

```
MiniChat/
├── pom.xml                    # 父工程：统一版本管理
├── minichat-common/           # 公共模块：DTO、工具类、常量
├── deploy/                    # Docker Compose 本地开发环境编排
│   ├── docker-compose.yml
│   ├── .env.example           # 环境变量模板
│   └── sql/                   # 数据库初始化脚本
└── README.md
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

| 中间件           | Docker 服务端版本 | Java 客户端版本 | 版本管理方式             |
|---------------|--------------|------------|--------------------|
| Nacos         | v2.2.3       | 2.3.2      | Alibaba BOM 锁定     |
| MySQL         | 8.0          | 由 BOM 管理   | Spring Boot BOM 锁定 |
| Redis         | 7.0-alpine   | 由 BOM 管理   | Spring Boot BOM 锁定 |
| RocketMQ      | 5.1.4        | 5.1.4      | 父 pom 锁定           |
| Elasticsearch | 7.17.10      | 由 BOM 管理   | Spring Boot BOM 锁定 |

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

#### 1️⃣ 启动中间件（Docker Compose）

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

# ⑥ 确认所有容器正常运行
docker ps
```

#### 2️⃣ 创建新的业务服务模块

当你要开发自己的服务时（如 user、chat、gateway），参照 `minichat-common` 的模板来创建。

### 第 1 步：从 main 拉取你的功能分支

```bash
git checkout main
git pull origin main
git checkout -b feature/user-service     # 以用户服务为例，按需改名
```

### 第 2 步：创建子模块目录
> 模仿 minichat-common 模块创建 maven 子项目，所有依赖都不需要写版本号，父 pom 已经统一锁定，子模块直接引用即可。

### 第 3 步：在父 pom 注册你的模块

打开根目录 `pom.xml`，在 `<modules>` 中追加你的模块名：

```xml
<modules>
    <module>minichat-common</module>
    <module>minichat-user</module>     <!-- 新增你自己的模块 -->
</modules>
```

### 第 4 步：验证并提交

```bash
mvn clean compile                           # 确认编译通过
git add .
git commit -m "feat: init user service module"
git push origin feature/user-service
```

然后在 GitHub 上创建 Pull Request，合并到 `main`。

## 操作规范

```
main                        # 稳定主线，只接受合并，不直接开发
  └── feature/*             # 功能/基础设施分支
```

- **不允许直接操作 main 分支**，必须在 feature 分支开发
- 改完测试全部通过后，通过 PR 合并到 main
- 当改动了【环境、数据库、公共架构】，必须在 PR 里写清楚通知，并在群里同步
- 合并前执行 `mvn clean compile` 确保编译通过
- 新的依赖统一写在父 pom.xml 里面，然后子 pom.xml 继承，方便看整个项目上技术栈

---

## 提醒

> **前端开发**：查看路由，直接 pull user 模块，然后启动访问 http://localhost:8081/doc.html ；或者用 api-docs.json 也行，用插件或者导入 apifox 都行。
>
> **其他模块开发**：需要查用户资料时，通过 Feign 调用 `GET /internal/user/{id}`（返回裸对象，不包 Result）。`InternalUserResponse` 定义在 `minichat-common`。后续网关建好后统一走网关，内部接口不走网关。