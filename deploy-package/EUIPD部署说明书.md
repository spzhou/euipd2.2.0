# EUIPD 研发过程管理系统 部署说明书

| 项目 | 内容 |
| --- | --- |
| 适用版本 | EUIPD 2.2.0 |
| 部署方式 | 单台 Linux 服务器，Docker Compose |
| 部署包 | 源码仓库根目录 `deploy-package/` |
| 读者 | 负责安装、升级与日常运维的人员 |
| 修订 | V1.0（2026-10-25）：首版 |

本说明书按顺序说明从源码构建部署包、填写配置、首次安装、验证登录、日常运维与故障排查。部署包只提供脚本、编排文件与配置模板，不含任何账号、口令或密钥；全部账号与密钥由部署者在 `.env` 中填写或由脚本随机生成。

## 一、部署架构

### 1.1 组件与访问路径

浏览器只访问 nginx 的一个端口，nginx 按路径转发到内部服务。除 nginx 端口与只绑定本机回环地址的 MinIO 控制台外，其他组件不对外发布端口。

```
浏览器  http://SERVER_HOST[:HTTP_PORT]
   |
   v
nginx :80（容器内）
   +-- /                     -> 前端静态页面 web/dist
   +-- /api/**               -> gateway :18000（去掉 /api 前缀）
   |        +-- /system/**   -> system   :18001  登录、用户、角色、权限
   |        +-- /customer/** -> customer :18005  研发业务、流程引擎、定时任务
   |        +-- /file/**     -> file2    :18003  文件上传、签名下载、在线查看
   |        +-- /ftp/**      -> ftp      :18007  大文件传输
   +-- /euipd-doc/、/euipd-web/  -> minio :9000   对象存储（预签名地址同源访问）
   +-- /cool/、/browser/、/hosting/ -> collabora :9980（可选）
   |
内部组件：mysql :3306（库 mycourse_bk）  redis :6379  minio :9000
```

| 容器 | 镜像 | 作用 |
| --- | --- | --- |
| nginx | `nginx:1.27-alpine` | 统一入口，对外发布 `HTTP_PORT` |
| gateway、system、customer、file2、ftp | `euipd/app`（本机构建） | 5 个后端微服务，共用一个镜像，按环境变量 `SERVICE` 启动对应 jar |
| mysql | `mysql:8.0` | 业务数据库；首次启动时建表并写入超级管理员 |
| redis | `redis:7-alpine` | 会话、登录失败计数与缓存 |
| minio | `bitnamilegacy/minio:2025.4.22` | 对象存储，桶 `euipd-doc`（私有）与 `euipd-web`（公开静态资源）。MinIO 官方已停止公开发布社区版镜像，使用同一发行版的 Bitnami 存档镜像，容器以 uid 1001 运行 |
| minio-init | 同上（镜像内含 `mc`） | 建桶、建应用专用账号、上传登录页图片，执行完即退出 |
| collabora | `euipd/collabora-code`（本机构建，可选） | 正式文件生成 PDF、Word 导入与只读在线查看 |

### 1.2 账号与密钥一览

系统运行涉及的全部账号、口令与密钥如下，均在 `.env` 中维护。代码与配置文件中没有内置账号。

| 类别 | 参数 | 来源 | 使用者 |
| --- | --- | --- | --- |
| 平台超级管理员 | `SUPER_ADMIN_PHONE`、`SUPER_ADMIN_NAME`、`SUPER_ADMIN_PASSWORD` | 部署者填写 | 只在首次初始化数据库时写入 |
| 成员初始口令 | `EUIPD_INITIAL_PASSWORD` | 部署者填写 | 新建成员与重置口令 |
| MySQL | `MYSQL_ROOT_PASSWORD`（root）、`EUIPD_DB_PASSWORD`（应用账号 `euipd`） | `init` 随机生成 | root 只用于初始化、备份与运维；应用只使用 `euipd` |
| Redis | `EUIPD_REDIS_PASSWORD` | `init` 随机生成 | 后端服务 |
| MinIO 管理员 | `MINIO_ROOT_USER`、`MINIO_ROOT_PASSWORD` | `init` 生成 | 只用于 minio-init 与管理控制台 |
| MinIO 应用账号 | `EUIPD_OSS_ACCESS_KEY_ID`、`EUIPD_OSS_ACCESS_KEY_SECRET` | `init` 生成 | 后端服务读写对象存储 |
| 令牌签名密钥 | `keys/jwt-private.pem`、`keys/jwt-public.pem` | `init` 生成 RSA 2048 | system 签发令牌，网关验签 |
| 前端加盐字符串 | `VUE_APP_PASSWORD_KEY` | 构建时生成 | 编译进前端，只用于区分部署，不是密钥 |
| 外部集成（可选） | 短信、视频点播、保利威、GitLab、FTP、泛微 OA、SQL Server | 部署者按需填写 | 见第五节 |

### 1.3 部署包目录

| 路径 | 入库 | 说明 |
| --- | --- | --- |
| `README.md`、`EUIPD部署说明书.md` | 是 | 说明文档 |
| `.env.example` | 是 | 账号、口令、密钥与运行参数模板 |
| `euipd.sh` | 是 | 运维脚本：安装、启停、检查、备份、恢复、升级 |
| `docker-compose.yml` | 是 | 容器编排 |
| `app/Dockerfile`、`app/entrypoint.sh` | 是 | 后端镜像定义与启动脚本 |
| `config/<服务>/application-prod.properties` | 是 | 部署环境覆盖配置，口令一律引用环境变量 |
| `nginx/euipd.conf` | 是 | nginx 转发规则 |
| `mysql/init/01-init-db.sh` | 是 | 数据库首次初始化脚本 |
| `minio/init.sh` | 是 | 对象存储初始化脚本 |
| `scripts/build-artifacts.sh` | 是 | 从源码生成构建产物 |
| `app/jars/`、`app/VERSION` | 否 | 构建产物：5 个后端 jar 与版本信息 |
| `web/dist/` | 否 | 构建产物：前端静态页面 |
| `mysql/db/`、`mysql/my.cnf` | 否 | 构建产物：建库脚本与 MySQL 配置 |
| `minio/web-assets/` | 否 | 构建产物：登录页图片 |
| `collabora/` | 否 | 构建产物：Collabora 镜像构建目录与中文字体 |
| `.env` | 否 | 服务器上的实际参数，含口令，权限 600 |
| `keys/` | 否 | 令牌签名密钥 |
| `data/` | 否 | 数据目录：`mysql/`、`redis/`、`minio/`、`logs/` |
| `backups/` | 否 | 备份目录 |
| `images/` | 否 | 可选的离线镜像（`*.tar`、`*.tar.gz`） |

“入库”为否的文件由 `deploy-package/.gitignore` 忽略，不得提交到版本库，也不得随部署包外发。

## 二、环境要求

### 2.1 构建机

构建机用于编译源码、生成部署包，可以是开发电脑，也可以是部署服务器本身。

| 项目 | 要求 |
| --- | --- |
| 源码 | 完整的 EUIPD 源码仓库 |
| JDK | 11 或 17 |
| Node.js | 16 以上（已在 20、24 上验证） |
| 网络 | 能访问 Maven 中央仓库与 npm 仓库（或已配置可用的镜像） |
| 工具 | bash、tar；Linux 或 macOS |

### 2.2 部署服务器

| 项目 | 最低 | 建议 |
| --- | --- | --- |
| 操作系统 | 64 位 Linux（Ubuntu 22.04/24.04、Debian 12、Rocky/RHEL 9 等），x86_64 | 同左 |
| CPU | 2 核 | 4 核以上 |
| 内存 | 8GB | 16GB |
| 磁盘 | 40GB | 数据另挂独立磁盘，按文件量规划 |
| Docker | Docker Engine 24 以上，含 Compose 插件（`docker compose`） | 最新稳定版 |
| 工具 | bash、openssl、curl、tar、gzip | 同左 |

空库空闲时，5 个 Java 服务合计占用内存约 2.5GB，MySQL、MinIO、Redis、nginx 合计约 1GB；启用 Collabora 另需约 1.2GB 内存与约 2.3GB 镜像空间。内存不足 8GB 时按第 5.6 节调低堆内存，并配置交换分区。

### 2.3 安装 Docker

以 Ubuntu 为例（其他发行版见 Docker 官方文档）：

```bash
curl -fsSL https://get.docker.com | sh
sudo systemctl enable --now docker
docker compose version
```

运维命令须以 root 或 docker 组成员执行。

服务器无法访问 Docker Hub 时，配置镜像加速后重启 Docker：

```bash
sudo mkdir -p /etc/docker
sudo tee /etc/docker/daemon.json <<'JSON'
{ "registry-mirrors": ["https://<镜像加速地址>"] }
JSON
sudo systemctl restart docker
```

个别镜像经 `registry-mirrors` 拉取失败时，在 `.env` 的 `REGISTRY_MIRROR` 中填写加速地址的主机名（不带 `https://`），`init` 会对直接拉取失败的镜像改以 `<REGISTRY_MIRROR>/library/<镜像>` 或 `<REGISTRY_MIRROR>/<镜像>` 拉取，再打回原名。

仍无法拉取时，改用离线导入：在能拉取镜像的 x86_64 机器上执行 `docker pull` 后导出，放入部署目录的 `images/`，`init` 会在拉取前自动导入。例如：

```bash
docker pull --platform linux/amd64 bitnamilegacy/minio:2025.4.22
docker save bitnamilegacy/minio:2025.4.22 | gzip > images/minio.tar.gz
```

## 三、构建部署包

### 3.1 生成构建产物

在构建机的源码仓库根目录执行：

```bash
bash deploy-package/scripts/build-artifacts.sh
```

脚本依次完成：

1. 在 `euipdv2-master/` 执行 `./mvnw -DskipTests clean package`，把 gateway、system、customer、file2、ftp 五个 jar 复制到 `deploy-package/app/jars/`，并写入 `app/VERSION`（版本号、构建时间、提交号）。
2. 在 `euipdv2-vue2/` 执行 `npm ci` 与 `npm run build-prod`，把 `dist/` 复制到 `deploy-package/web/dist/`。
3. 复制建库脚本 `doc/db/euipd-schema.sql`、`doc/db/init-super-admin.sql` 到 `mysql/db/`，复制 `docker/mysql/my.cnf` 到 `mysql/my.cnf`。
4. 复制登录页图片 `docker/minio/web-assets/` 到 `minio/web-assets/`。
5. 复制 Collabora 镜像构建目录 `docker/collabora/` 到 `collabora/`（含已下载的中文字体）。

可选参数：

| 参数 | 作用 |
| --- | --- |
| `--skip-backend` | 不重新编译后端，沿用 `app/jars/` |
| `--skip-frontend` | 不重新构建前端，沿用 `web/dist/` |
| `--pack` | 另外打包为 `release/euipd-deploy-<版本>.tar.gz`，不含 `.env`、`keys/`、`data/`、`backups/`、`images/` |

### 3.2 前端加盐字符串

前端构建需要 `VUE_APP_PASSWORD_KEY`，用于机构注册口令的前端加盐。脚本按以下顺序取值：环境变量 `VUE_APP_PASSWORD_KEY`、`deploy-package/.env` 中的同名项；都为空时生成 32 位随机十六进制串，写入 `deploy-package/.env`（不存在时按模板创建）。

该值编译进前端代码，只用于区分不同部署，不是密钥。系统投入使用后，每次升级构建都必须使用同一个值，否则已注册机构的注册口令校验会不一致。构建机与服务器不是同一台时，把该值同时记入服务器的 `.env`，升级构建前在构建机上设置相同的环境变量：

```bash
VUE_APP_PASSWORD_KEY=<服务器 .env 中的值> bash deploy-package/scripts/build-artifacts.sh --pack
```

### 3.3 Collabora 中文字体（可选）

启用 Collabora 时，正式文件转 PDF 与在线查看需要公文字体。构建部署包前在源码仓库执行：

```bash
bash euipdv2-master/docker/collabora/fetch-fonts.sh
```

字体下载到 `docker/collabora/fonts/`，再执行第 3.1 节即随部署包带出。未下载字体时 Collabora 按备选字体渲染，版式可能与原文件不同。

### 3.4 拷贝到服务器

构建机即服务器时，直接在 `deploy-package/` 中操作，可跳过本节。否则：

```bash
# 构建机
bash deploy-package/scripts/build-artifacts.sh --pack
scp release/euipd-deploy-2.2.0.tar.gz root@<服务器>:/opt/

# 服务器
cd /opt && tar -xzf euipd-deploy-2.2.0.tar.gz && mv deploy-package euipd && cd euipd
```

以下各节的命令均在服务器部署目录（如 `/opt/euipd`）中执行。

## 四、首次安装

### 4.1 填写 .env

```bash
cp .env.example .env
chmod 600 .env
vi .env
```

首次安装至少填写以下 4 项，其余留空由 `init` 生成或使用缺省值：

| 参数 | 要求 |
| --- | --- |
| `SERVER_HOST` | 浏览器访问用的服务器 IP 或域名，不能是 `127.0.0.1` 或 `localhost` |
| `SUPER_ADMIN_PHONE` | 平台超级管理员的登录手机号，11 位数字 |
| `SUPER_ADMIN_PASSWORD` | 超级管理员初始口令，8 至 32 位并同时含字母与数字，不含引号、反斜杠、反引号与 `$` |
| `EUIPD_INITIAL_PASSWORD` | 成员初始口令，8 至 32 位并同时含字母与数字 |

`HTTP_PORT` 缺省 80，被占用时改为其他端口。经外部 HTTPS 代理或域名访问时，另填 `PUBLIC_URL`（见第六节）。

### 4.2 初始化

```bash
chmod +x euipd.sh
./euipd.sh init
```

`init` 依次完成：

1. 检查 Docker、Compose 插件、openssl、curl。
2. 为空的 `MYSQL_ROOT_PASSWORD`、`EUIPD_DB_PASSWORD`、`EUIPD_REDIS_PASSWORD`、`MINIO_ROOT_PASSWORD`、`EUIPD_OSS_ACCESS_KEY_SECRET` 生成 24 位随机口令；`MINIO_ROOT_USER`、`EUIPD_OSS_ACCESS_KEY_ID` 为空时分别取 `euipd-admin`、`euipd-app`。生成值写回 `.env`。
3. 生成令牌签名密钥 `keys/jwt-private.pem`、`keys/jwt-public.pem`（已存在时不覆盖）。
4. 校验 `.env` 参数与超级管理员参数，检查构建产物是否齐全。
5. 导入 `images/` 下的离线镜像（如有），构建应用镜像 `euipd/app:current` 并另打版本号标签；启用 Collabora 时构建 `euipd/collabora-code:24.04`。
6. 拉取 MySQL、Redis、nginx、MinIO 镜像。

`init` 可重复执行，已有的口令与密钥不会被覆盖。

### 4.3 启动

```bash
./euipd.sh up
```

首次启动时：

- MySQL 数据目录为空，执行 `mysql/init/01-init-db.sh`：导入表结构（120 张业务表），再按 `SUPER_ADMIN_*` 写入平台超级管理员、平台机构“平台超级用户”与顶级部门“平台总部”。参数不合法时初始化失败，MySQL 不会变为健康。
- minio-init 建桶 `euipd-doc`（私有）、`euipd-web`（匿名只读），建应用账号并授予读写权限，上传登录页图片。
- customer 首次启动时由流程引擎 Flowable 自动建表并写入平台流程定义，耗时数分钟。

脚本等待全部服务健康（最长 15 分钟），随后执行第 4.4 节的检查。成功时输出：

```
[EUIPD] 全部服务已就绪：http://<SERVER_HOST>
[EUIPD] 数据库已初始化。超级管理员登录手机号：<SUPER_ADMIN_PHONE>，口令为 .env 中的 SUPER_ADMIN_PASSWORD
```

### 4.4 验证

```bash
./euipd.sh status
```

检查项与正常结果：

| 检查项 | 方法 | 正常结果 |
| --- | --- | --- |
| 前端页面 | 本机访问 `/` | HTTP 200 |
| 后端接口 | 本机访问 `/api/system/auth/public-key` | HTTP 200 |
| 对象存储（经 nginx） | 本机访问 `/euipd-doc/` | HTTP 403（私有桶拒绝匿名访问） |
| 对象存储（服务端回访） | 在 file2 容器内访问 `PUBLIC_URL/euipd-doc/` | HTTP 403 |
| Collabora（启用时） | 本机访问 `/hosting/discovery` | HTTP 200 |

“服务端回访”失败时，上传与下载文件会报错，按第九节处理。

### 4.5 首次登录

1. 浏览器打开 `http://SERVER_HOST[:HTTP_PORT]/`。
2. 以 `SUPER_ADMIN_PHONE` 与 `SUPER_ADMIN_PASSWORD` 登录，机构选择“平台超级用户”。
3. 登录后点右上角“修改密码”更换口令。修改后全部会话失效，以新口令重新登录。
4. 编辑 `.env`，清空 `SUPER_ADMIN_PASSWORD=` 后的内容。数据库建成后该项不再使用，保留只会增加泄露风险。

### 4.6 建立机构与成员

1. 超级管理员在“超级用户 → 机构/团队”中点“添加机构/团队”，填写机构名称、管理员姓名与管理员手机（作为机构管理员的登录名），机构状态选“在业”。
2. 机构管理员以管理员手机和成员初始口令（`EUIPD_INITIAL_PASSWORD`）登录，选择本机构，按提示设置新口令后进入系统。
3. 机构管理员在“团队管理”中逐个添加成员，或下载员工信息表模板批量导入。新成员以成员初始口令首次登录，须先设置新口令。
4. 成员遗忘口令时，由机构管理员在“团队管理”中“重置密码”，口令恢复为成员初始口令，已登录会话随即失效、登录锁定一并解除。

### 4.7 防火墙

只放行 `HTTP_PORT`。firewalld：

```bash
firewall-cmd --permanent --add-port=80/tcp && firewall-cmd --reload
```

ufw：

```bash
ufw allow 80/tcp
```

Docker 发布的端口直接写入 iptables，ufw 的拒绝规则对其不一定生效。须只允许指定网段访问时，在上层网络设备或 iptables 的 `DOCKER-USER` 链中限制。云服务器在安全组中放行该端口，授权对象尽量限定为使用者的出口地址。

## 五、参数说明

`.env` 中每一项均有注释，本节补充取值要点。修改 `.env` 后执行 `./euipd.sh up` 使之生效（`up` 只重建参数有变化的容器）。

### 5.1 访问地址

| 参数 | 说明 |
| --- | --- |
| `SERVER_HOST` | 服务器 IP 或域名。也用于 Collabora 的页面嵌入来源限制 |
| `HTTP_PORT` | nginx 对外端口，缺省 80 |
| `PUBLIC_URL` | 浏览器访问系统的完整地址，不带结尾 `/`。留空时取 `http://SERVER_HOST[:HTTP_PORT]`。对象存储的预签名地址、页面跳转地址与 Collabora 地址均由此生成，必须与浏览器地址栏一致 |

### 5.2 超级管理员与成员初始口令

`SUPER_ADMIN_*` 只在 MySQL 数据目录为空、首次初始化时使用，服务运行时不读取。数据库建成后修改这三项不起作用；要更换超级管理员口令，在系统内“修改密码”。

`EUIPD_INITIAL_PASSWORD` 由 system 与 customer 服务读取，修改后执行 `./euipd.sh up` 生效。以该口令登录的成员（超级管理员除外）须先设置新口令。

### 5.3 基础组件口令

MySQL、MinIO 只在首次初始化数据目录时读取口令。数据目录建成后，直接修改 `.env` 中的 `MYSQL_ROOT_PASSWORD`、`EUIPD_DB_PASSWORD`、`MINIO_ROOT_*`、`EUIPD_OSS_*` 会导致服务连不上组件。确需更换时，先在组件内修改，再改 `.env`：

```bash
# MySQL 应用账号口令（root 口令同理，账号改为 'root'@'%' 与 'root'@'localhost'）
./euipd.sh mysql -e "ALTER USER 'euipd'@'%' IDENTIFIED BY '<新口令>'"
# 改 .env 的 EUIPD_DB_PASSWORD 后
./euipd.sh up
```

`EUIPD_OSS_ACCESS_KEY_SECRET` 修改后执行 `./euipd.sh up`，minio-init 会按新值更新应用账号口令。Redis 口令每次启动都从 `.env` 读取，修改后执行 `./euipd.sh up` 即可。

### 5.4 可选外部集成

不使用的集成保持为空，对应功能不可用，其他功能不受影响。

| 集成 | 参数 | 用途 |
| --- | --- | --- |
| 阿里云短信 | `EUIPD_CUSTOMER_ACCESS_KEY_ID`、`EUIPD_CUSTOMER_ACCESS_KEY_SECRET`、`EUIPD_SMS_SIGNATURE`、`EUIPD_SMS_*_TEMPLATE` | 短信验证码、短信找回口令、业务通知、机构注册复核通知 |
| 阿里云视频点播 | `EUIPD_VOD_ACCESS_KEY_ID`、`EUIPD_VOD_ACCESS_KEY_SECRET`、`EUIPD_VOD_REGION_ID` 等 | 登录页介绍视频与视频上传 |
| 保利威直播 | `EUIPD_POLYV_USER_ID`、`EUIPD_POLYV_APP_ID`、`EUIPD_POLYV_APP_SECRET` | 直播 |
| GitLab | `EUIPD_GITLAB_URL`、`EUIPD_GITLAB_TOKEN` | 代码仓库关联 |
| FTP | `EUIPD_FTP_HOST`、`EUIPD_FTP_PORT`、`EUIPD_FTP_USERNAME`、`EUIPD_FTP_PASSWORD`、`EUIPD_FTP_WORKING_DIRECTORY` | ftp 服务的大文件传输 |
| 泛微 OA | `EUIPD_OA_HOST`、`EUIPD_OA_APPID`、`EUIPD_OA_SECRET`、`EUIPD_OA_SPK`、`EUIPD_OA_MATERIAL_FORM_CREATER` | 物料表单推送 |
| SQL Server 物料库 | `EUIPD_SQL_SERVER_URL`、`EUIPD_SQL_SERVER_USER`、`EUIPD_SQL_SERVER_PASSWORD` | 与泛微 OA 集成配套 |

浏览器端视频上传另需阿里云视频点播上传 SDK，因未声明开源许可证，源码仓库不提供。需要时在构建前把 SDK 放入 `euipdv2-vue2/public/static/aliyunlib/`（目录结构见源码仓库 README），再重新构建前端。

### 5.5 运行参数

| 参数 | 缺省 | 说明 |
| --- | --- | --- |
| `TZ` | `Asia/Shanghai` | 容器时区 |
| `DATA_DIR` | `./data` | 数据目录，建议改到独立磁盘的绝对路径；首次启动后不要再改，除非连同数据一起迁移 |
| `DOCKER_SUBNET_PREFIX` | `172.30.0` | 容器网络 /24 前缀，与本单位网段冲突时修改，须在首次 `up` 前确定 |
| `MINIO_CONSOLE_PORT` | `18101` | MinIO 管理控制台，只绑定 `127.0.0.1` |
| `COMPOSE_PROFILES` | 空 | 填 `collabora` 启用 Collabora |
| `COLLABORA_SSL_TERMINATION` | `false` | 外部 HTTPS 代理且启用 Collabora 时改为 `true` |
| `COMPOSE_PROJECT_NAME` | `euipd` | 容器名前缀，同一服务器部署多套时各取不同值 |
| `BACKUP_KEEP` | `14` | 备份保留份数 |
| `EUIPD_VERSION` | 空（即 `current`） | 指定启动的应用镜像版本，用于回退 |

### 5.6 Java 堆内存

`JAVA_OPTS_<服务>` 设定各服务的堆大小，模板按 8GB 以上内存设定，堆上限合计约 4.1GB。16GB 内存的服务器可把 customer 调到 `-Xmx3g`、file2 调到 `-Xmx2g`。8GB 以下内存的服务器建议：

```
JAVA_OPTS_GATEWAY=-Xms128m -Xmx384m
JAVA_OPTS_SYSTEM=-Xms128m -Xmx512m
JAVA_OPTS_CUSTOMER=-Xms256m -Xmx1024m
JAVA_OPTS_FILE2=-Xms128m -Xmx512m
JAVA_OPTS_FTP=-Xms64m -Xmx256m
```

### 5.7 服务配置文件

`config/common/application-prod.properties` 对 5 个服务生效，`config/<服务>/application-prod.properties` 只对该服务生效，二者覆盖 jar 内的同名配置。常用项：

| 文件 | 配置项 | 说明 |
| --- | --- | --- |
| `common` | `euipd.security.jwt.*-location`、`euipd.security.tenant*` | 令牌密钥位置与多租户数据过滤开关，一般不改 |
| `system` | `euipd.security.jwt.*-ttl-seconds` | 访问令牌 30 分钟、刷新令牌 8 小时、单次登录最长 24 小时 |
| `file2` | `spring.servlet.multipart.*` | 单文件上限，缺省 1024MB；调大时同步修改 `nginx/euipd.conf` 的 `client_max_body_size` |
| `file2` | `rdms.file.purge.*` | 软删附件对象清理，缺省关闭；开启时先保持试运行，核对日志后再关闭试运行 |

配置文件中的口令一律以 `${EUIPD_*}` 引用环境变量，不要把口令直接写进配置文件。修改后执行 `./euipd.sh restart <服务>`。

## 六、域名与 HTTPS

部署包内的 nginx 只监听 HTTP。对外提供 HTTPS 时，在服务器或上层网络设备上再部署一层 HTTPS 反向代理，把全部路径原样转发到 `http://<服务器>:<HTTP_PORT>`。

1. `.env` 中设置：
   ```
   SERVER_HOST=euipd.example.com
   PUBLIC_URL=https://euipd.example.com
   COLLABORA_SSL_TERMINATION=true   # 启用 Collabora 时
   ```
2. 外层代理须满足：
   - 转发时保留 `Host` 头（对象存储预签名地址按 Host 校验）；
   - 请求体上限不低于 1100MB，读写超时不低于 600 秒，关闭请求缓冲；
   - `/cool/` 下的 WebSocket（`/cool/<任意>/ws`）须支持 `Upgrade`；
   - 设置 `X-Forwarded-Proto https`。
3. 服务端也通过 `PUBLIC_URL` 读写对象存储，因此服务器本机须能解析该域名并访问该 HTTPS 地址（证书须受信任）。内网域名可在服务器 `/etc/hosts` 中解析到本机或代理地址。
4. 执行 `./euipd.sh up`，再执行 `./euipd.sh status` 确认“服务端回访”正常。

外层代理示例（nginx）：

```nginx
server {
    listen 443 ssl http2;
    server_name euipd.example.com;
    ssl_certificate     /etc/nginx/certs/euipd.crt;
    ssl_certificate_key /etc/nginx/certs/euipd.key;

    client_max_body_size 1100m;
    proxy_read_timeout 600s;
    proxy_send_timeout 600s;
    proxy_request_buffering off;
    proxy_buffering off;

    location / {
        proxy_pass http://127.0.0.1:8080;      # HTTP_PORT
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Forwarded-Proto https;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection $http_connection;
    }
}
```

## 七、可选组件 Collabora

Collabora CODE 用于正式文件生成 PDF、Word 文档导入时的格式转换与只读在线查看。不启用时上述功能不可用，其他功能正常。

启用步骤：

1. 构建部署包前按第 3.3 节下载中文字体。
2. `.env` 中设置 `COMPOSE_PROFILES=collabora`。
3. 执行 `./euipd.sh init`（构建 Collabora 镜像，首次需下载约 1GB 基础镜像），再执行 `./euipd.sh up`。

说明：

- 浏览器必须以 `SERVER_HOST`（或 `PUBLIC_URL`）访问系统，用其他 IP 或主机名访问时在线查看页会被拒绝嵌入。
- Collabora 只接受 file2 的内部 WOPI 地址作为文件来源；nginx 拒绝外部访问 `/api/file/wopi/`、`/cool/convert-to` 与 `/cool/getMetrics`。
- CODE 为社区开发版，无服务等级协议；正式规模化使用时应评估商业版 Collabora Online。

## 八、日常运维

### 8.1 命令一览

| 命令 | 作用 |
| --- | --- |
| `./euipd.sh init` | 首次安装或补齐口令、密钥与镜像 |
| `./euipd.sh up` | 启动全部服务并等待就绪；修改 `.env` 后也用此命令生效 |
| `./euipd.sh down` | 停止并移除容器，数据保留 |
| `./euipd.sh restart [服务...]` | 重启全部或指定服务，如 `./euipd.sh restart customer` |
| `./euipd.sh status` | 容器状态与连通性检查 |
| `./euipd.sh check` | 只做连通性检查，适合加入监控 |
| `./euipd.sh logs [服务]` | 跟踪容器控制台日志 |
| `./euipd.sh backup` | 备份 |
| `./euipd.sh restore <备份目录>` | 从备份恢复 |
| `./euipd.sh upgrade` | 升级 |
| `./euipd.sh mysql [参数]` | 以 root 进入数据库命令行；`./euipd.sh mysql < x.sql` 执行脚本 |
| `./euipd.sh unlock <手机号>` | 解除登录锁定 |

服务名：`gateway`、`system`、`customer`、`file2`、`ftp`、`nginx`、`mysql`、`redis`、`minio`、`collabora`。

### 8.2 日志

| 位置 | 内容 |
| --- | --- |
| `data/logs/<服务>/` | 各 Java 服务按日滚动的文件日志，错误单独记入 `sys-error.log` |
| `data/logs/nginx/` | nginx 访问日志与错误日志 |
| `./euipd.sh logs <服务>` | 容器控制台输出，每个容器最多保留 5 个 50MB 文件 |

### 8.3 备份

```bash
./euipd.sh backup
```

在 `backups/<时间戳>/` 生成：

| 文件 | 内容 |
| --- | --- |
| `mycourse_bk.sql.gz` | 数据库全量导出（含存储过程、触发器、事件） |
| `minio.tar.gz` | 对象存储数据目录 |
| `config.tar.gz` | `.env`、`keys/`、`config/`、`nginx/` |

只保留最近 `BACKUP_KEEP` 份。备份含全部口令与密钥，应复制到另一台机器或存储妥善保管。建议用 cron 每日执行，例如每天 2:30：

```bash
30 2 * * * cd /opt/euipd && ./euipd.sh backup >> backups/backup.log 2>&1
```

### 8.4 恢复

```bash
./euipd.sh restore backups/20261025-023000
```

输入 `RESTORE` 确认后：停止应用服务，删除并重建数据库，替换对象存储数据目录，清空 Redis 缓存，再启动全部服务。恢复会覆盖现有全部数据。

`config.tar.gz` 不会自动恢复。迁移到新服务器时，先在新服务器解压部署包，再解压该文件得到 `.env` 与 `keys/`，执行 `./euipd.sh init` 与 `./euipd.sh up`（生成空库），最后执行 `restore`。

### 8.5 升级

1. 在构建机拉取新版源码，按第 3 节重新构建，使用与原部署相同的 `VUE_APP_PASSWORD_KEY`。
2. 在服务器上先备份：`./euipd.sh backup`。
3. 用新部署包中的 `app/`、`web/`、`mysql/db/`、`minio/web-assets/`、`collabora/`、`euipd.sh`、`docker-compose.yml`、`nginx/`、`config/`、`.env.example` 替换服务器上的对应内容；保留服务器上的 `.env`、`keys/`、`data/`、`backups/`。
4. 对照新版 `.env.example`，把新增参数补进 `.env`。
5. 新版本含数据库结构变化时，按发布说明执行变更语句（`./euipd.sh mysql < 变更脚本.sql`）。
6. 执行：
   ```bash
   ./euipd.sh upgrade
   ```
   当前镜像另存为 `euipd/app:previous`，以新 jar 构建 `euipd/app:current` 并重建 5 个后端容器，重新同步登录页图片并重启 nginx 挂载新前端，最后执行连通性检查。

### 8.6 回退

后端回退到上一个镜像：

```bash
docker images euipd/app                     # 查看已有版本标签
# .env 中设 EUIPD_VERSION=previous（或具体版本号）
./euipd.sh up
```

前端回退须换回旧的 `web/dist/` 后执行 `./euipd.sh restart nginx`。已执行数据库结构变更时，以升级前的备份恢复（第 8.4 节）。回退完成后把 `EUIPD_VERSION` 清空，下次升级才会使用新构建的镜像。

### 8.7 解除登录锁定

同一手机号 15 分钟内输错口令 5 次，锁定 15 分钟。须立即解除时：

```bash
./euipd.sh unlock <11 位手机号>
```

输出删除的键数，为 0 表示该手机号没有失败计数与锁定。成员的锁定也可由机构管理员“重置密码”一并解除。

### 8.8 例行检查

| 周期 | 内容 |
| --- | --- |
| 每日 | 备份是否生成；`./euipd.sh check` |
| 每周 | 磁盘占用 `df -h`、`du -sh data/* backups`；`data/logs/*/sys-error.log` 有无持续报错 |
| 每月 | 在另一台机器上以备份做一次恢复演练；更新服务器操作系统与 Docker 的安全补丁 |

## 九、故障排查

| 现象 | 可能原因 | 处理 |
| --- | --- | --- |
| `init` 提示缺少构建产物 | 未执行构建脚本，或拷贝时遗漏 | 按第三节构建后重新拷贝 |
| `init` 拉取镜像失败 | 无法访问 Docker Hub | 按第 2.3 节配置镜像加速，或填 `REGISTRY_MIRROR`；也可在能联网的机器上 `docker save` 后放入 `images/` |
| `up` 时 mysql 一直不健康 | 超级管理员参数不合法，或数据目录残留 | `./euipd.sh logs mysql` 查看 `[euipd]` 开头的提示；确认是首次安装后，`./euipd.sh down`，删除 `data/mysql` 后修正参数重新 `up` |
| customer 长时间不健康 | 首次启动建流程引擎表，或内存不足 | 首次启动等待 5 至 10 分钟；`./euipd.sh logs customer` 查看；`free -h` 检查内存，按第 5.6 节调整 |
| 页面能打开但登录报网络错误 | gateway 或 system 未就绪 | `./euipd.sh status`；`./euipd.sh logs gateway system` |
| 登录提示口令错误次数过多 | 登录锁定 | 第 8.7 节 |
| 检查提示“服务端回访”失败，上传或下载文件报错 | 宿主防火墙拦截容器访问宿主地址，或 `SERVER_HOST`/`PUBLIC_URL` 填错 | 核对两项参数；firewalld 执行 `firewall-cmd --permanent --zone=trusted --add-source=172.30.0.0/24 && firewall-cmd --reload`；ufw 执行 `ufw allow from 172.30.0.0/24 to any port 80 proto tcp`（网段与端口按实际修改）；HTTPS 时见第六节第 3 条 |
| 下载文件提示签名不匹配 | 浏览器地址与 `PUBLIC_URL` 不一致，或外层代理改写了 `Host` | 以 `PUBLIC_URL` 访问；外层代理保留 `Host` |
| 登录页图片不显示 | minio-init 未成功 | `./euipd.sh logs minio-init`；`docker compose up -d --force-recreate minio-init` |
| 新建成员提示未配置初始口令 | `EUIPD_INITIAL_PASSWORD` 为空 | 填写后 `./euipd.sh up` |
| 机构注册口令校验失败 | 升级时前端换了 `VUE_APP_PASSWORD_KEY` | 用原值重新构建前端并升级 |
| Collabora 不健康或在线查看空白 | 内存不足、镜像构建失败，或浏览器地址与 `SERVER_HOST` 不一致 | `./euipd.sh logs collabora`；以 `SERVER_HOST` 访问 |
| 容器网络与本单位网段冲突 | `DOCKER_SUBNET_PREFIX` 与内网重叠 | `./euipd.sh down` 后修改该项，再 `up` |
| 修改基础组件口令后服务连不上 | 口令只在首次初始化时读取 | 按第 5.3 节先在组件内改口令 |

## 十、安全要求

1. `.env`、`keys/`、`backups/` 含全部口令与密钥：权限分别为 600、700、700，不提交版本库，不随部署包外发，不在聊天工具或邮件中明文传递。
2. 首次登录后修改超级管理员口令，并清空 `.env` 中的 `SUPER_ADMIN_PASSWORD`。
3. 成员初始口令不要使用弱口令，也不要与超级管理员口令相同。
4. 只对外开放 `HTTP_PORT`。MySQL、Redis、MinIO API 与各微服务端口不对外发布；MinIO 控制台只绑定本机回环地址，远程使用时经 SSH 端口转发：`ssh -L 18101:127.0.0.1:18101 <服务器>`，再在本机浏览器打开 `http://127.0.0.1:18101`。
5. 对外服务启用 HTTPS（第六节）。
6. 令牌签名私钥泄露时，删除 `keys/` 下两个文件后执行 `./euipd.sh init` 重新生成，再 `./euipd.sh restart`；全部用户须重新登录。
7. 定期备份并异地保存；定期更新操作系统与 Docker 安全补丁，基础组件镜像按需更新版本后执行 `init` 与 `up`。
8. 发现安全问题按源码仓库 `SECURITY.md` 报告。

## 附录 A 首次安装命令汇总

```bash
# 构建机：源码仓库根目录
bash euipdv2-master/docker/collabora/fetch-fonts.sh        # 启用 Collabora 时
bash deploy-package/scripts/build-artifacts.sh --pack
scp release/euipd-deploy-2.2.0.tar.gz root@<服务器>:/opt/

# 服务器
cd /opt && tar -xzf euipd-deploy-2.2.0.tar.gz && mv deploy-package euipd && cd euipd
cp .env.example .env && chmod 600 .env
vi .env           # 填 SERVER_HOST、SUPER_ADMIN_PHONE、SUPER_ADMIN_PASSWORD、EUIPD_INITIAL_PASSWORD
./euipd.sh init
./euipd.sh up
./euipd.sh status
# 浏览器登录、修改超级管理员口令后，清空 .env 中的 SUPER_ADMIN_PASSWORD
```

## 附录 B 首次初始化写入的数据

| 表 | 内容 |
| --- | --- |
| `rdms_user` | 超级管理员账号，登录名为 `SUPER_ADMIN_PHONE`；口令以 `md5(md5(明文 + 盐))` 写入，首次登录成功后自动升级为 BCrypt |
| `rdms_customer` | 平台机构，标识固定为 `fm4ek0Zy`（代码常量，不得修改），名称“平台超级用户” |
| `rdms_department` | 平台机构的顶级部门“平台总部” |
| `rdms_customer_user`、`rdms_boss` | 超级管理员在平台机构的成员记录与最终管理人身份 |
| `rdms_customer_role`、`rdms_customer_role_customer`、`rdms_customer_resource` | 平台超级用户角色与菜单 |
| 流程引擎表（`ACT_`、`FLW_` 开头） | customer 首次启动时由 Flowable 建立 |

## 附录 C 端口

| 端口 | 位置 | 说明 |
| --- | --- | --- |
| `HTTP_PORT`（缺省 80） | 宿主机，对外 | nginx 统一入口 |
| `MINIO_CONSOLE_PORT`（缺省 18101） | 宿主机 `127.0.0.1` | MinIO 管理控制台 |
| 18000、18001、18003、18005、18007 | 容器网络内 | gateway、system、file2、customer、ftp |
| 3306、6379、9000、9980 | 容器网络内 | MySQL、Redis、MinIO API、Collabora |
