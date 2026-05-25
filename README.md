## 开发环境准备

### 1. 本地公共基础设施一键起航
项目采用 Docker Compose 编排本地开发环境：
- **MySQL 8.0**
- **Redis 7.0-alpine**
- **Nacos Server v2.2.3**
- **RocketMQ 5.1.4**
- **Elasticsearch & Kibana 7.17.10**
- **Nginx alpine**

**本地启动步骤：**
1. 确保本地已启动 **Docker Desktop**。
2. 切换到 `main` 分支并拉取最新代码：`git pull origin main`。
3. 进入项目 **`/deploy`** 目录。
4. 将该目录下的 `.env.example` 文件复制一份，并在同级目录下重命名为 **`.env`**（根据需要配置本地数据挂载路径与密码，默认均为 `123456`）。
5. 在 `/deploy` 目录下打开终端，执行以下命令一键后台启动所有中间件：
```bash
docker-compose up -d
```

**PS：**
1. 当改动了【环境、数据库、公共架构】，必须在 PR 里写清楚通知
