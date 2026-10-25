# EUIPD 部署包

在一台 Linux 服务器上以 Docker Compose 部署 EUIPD 前后端及其依赖组件（MySQL、Redis、MinIO、nginx，可选 Collabora）。完整步骤、参数说明、运维与故障排查见 [EUIPD部署说明书.md](EUIPD部署说明书.md)。

## 快速开始

```bash
# 1. 构建机：在源码仓库根目录生成构建产物（后端 jar、前端 dist、建库脚本等）并打包
bash deploy-package/scripts/build-artifacts.sh --pack

# 2. 服务器：解压后填写参数
tar -xzf euipd-deploy-2.2.0.tar.gz && mv deploy-package euipd && cd euipd
cp .env.example .env && chmod 600 .env
vi .env    # 至少填写 SERVER_HOST、SUPER_ADMIN_PHONE、SUPER_ADMIN_PASSWORD、EUIPD_INITIAL_PASSWORD

# 3. 初始化（生成其余口令与令牌密钥、构建镜像）并启动
./euipd.sh init
./euipd.sh up
```

浏览器打开 `http://SERVER_HOST/`，以超级管理员手机号与口令登录，机构选择“平台超级用户”。

## 账号与密钥

部署包不含任何账号、口令或密钥。`.env.example` 是唯一的参数模板，列出了部署所需的全部账号与密钥：

- 超级管理员与成员初始口令由部署者填写；
- MySQL、Redis、MinIO 的口令与令牌签名密钥由 `./euipd.sh init` 随机生成；
- 短信、视频点播、GitLab、FTP、OA 等外部集成按需填写，不用则留空。

`.env`、`keys/`、`data/`、`backups/` 及构建产物已由 `.gitignore` 忽略，不得提交到版本库或随部署包外发。

## 目录

| 路径 | 说明 |
| --- | --- |
| `.env.example` | 参数模板（复制为 `.env` 后填写） |
| `euipd.sh` | 运维脚本：`init`、`up`、`down`、`restart`、`status`、`logs`、`backup`、`restore`、`upgrade`、`mysql`、`unlock` |
| `docker-compose.yml` | 容器编排 |
| `app/` | 后端镜像定义；`app/jars/` 为构建产物 |
| `config/` | 各服务的生产配置覆盖文件 |
| `nginx/euipd.conf` | 统一入口转发规则 |
| `mysql/init/` | 数据库首次初始化脚本；`mysql/db/` 为构建产物 |
| `minio/init.sh` | 对象存储初始化脚本 |
| `scripts/build-artifacts.sh` | 从源码生成构建产物 |
| `web/dist/`、`collabora/` | 构建产物：前端页面、Collabora 镜像构建目录 |
