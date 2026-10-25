# EUIPD 研发过程管理系统

| 项目 | 内容 |
| --- | --- |
| 版本 | 2.2.0 |
| 许可证 | [MIT](LICENSE)，第三方组件说明见 [NOTICE](NOTICE) |
| 后端 | `euipdv2-master/`，Spring Boot 2.7 + Spring Cloud 微服务，Java 11 |
| 前端 | `euipdv2-vue2/`，Vue 2.7 + Element UI |
| 作者 | 周朔鹏（391902958@qq.com），[https://www.euipd.com](https://www.euipd.com) |

## 一、项目简介

EUIPD 是一套按 IPD（集成产品开发）方法组织的研发过程管理系统，面向研发机构，覆盖从需求识别到产品发布的全过程。系统支持多租户，各机构数据相互隔离；使用者通过浏览器进入统一工作台，按角色看到不同菜单。

### 1.1 功能概览

| 功能域 | 主要内容 |
| --- | --- |
| 系统管理 | 租户机构、成员、角色、菜单与操作权限、审计日志、工作台待办聚合 |
| 需求管理 | 需求创建与状态流转、列表检索、附件与关联、评估字段 |
| 系统工程与特性 | 特性定义与审批、功能分解、追溯链、CBB（共用构建模块）仓库 |
| 立项与项目管理 | 预立项、立项与预算审批、项目与子项目、里程碑、甘特图计划 |
| 任务与开发执行 | 任务派发、状态与交付物、我的任务、工时填报 |
| 测试管理 | 组件级与项目级测试方案、缺陷转任务、测试统计 |
| 评审管理 | 评审发起与材料汇总、评审结论与责任转移 |
| 质量管理 | 质量缺陷登记与闭环、质量成本统计 |
| 预算与料工费 | 预算编制与执行、费用申请与支付、物料申请与处置 |
| 工时与绩效 | 规定工时与申报、绩效规则与指标、统计报表 |
| 文档与归档 | 文档上传与关联、文档权限与申请、归档目录与校验、富文本在线编辑 |
| 研发物料仓库 | 仓库、物料入库与导入、领料、退料、库存预警 |
| 内部邮件 | 站内邮件与附件 |

## 二、系统架构

```
浏览器（Vue 2.7，开发服务 :8080）
    |
    v
gateway :18000        JWT 验签、模块权限校验、写入身份头
    +-- /system/**   -> system    :18001   登录、用户、角色、权限、验证码
    +-- /file/**     -> file2     :18003   对象存储上传与签名下载
    +-- /customer/** -> customer  :18005   研发业务主数据与流程、物料仓库
    +-- /ftp/**      -> ftp       :18007   大文件传输
    |
    v
公共库 server（jar，非独立进程）：领域模型、MyBatis Mapper、统一响应、租户上下文、对象存储客户端
    |
    v
MySQL 8（库名 mycourse_bk）   Redis 7   MinIO（S3 兼容对象存储）
```

- 不使用注册中心与配置中心。网关按路径前缀转发到固定地址，各进程只读取自身的配置文件。
- 服务之间不互相调用，均通过公共库 `server` 访问同一业务库。
- 令牌采用 RS256 签名的 JWT，私钥由 system 服务持有，公钥由网关持有。
- RabbitMQ 随开发环境启动，当前代码未使用；Collabora Online 为可选组件，用于正式文件与 Word 文档的在线查看。

### 2.1 技术栈

| 层 | 选用 |
| --- | --- |
| 语言与构建 | Java 11，Maven 3.6 以上 |
| 应用框架 | Spring Boot 2.7.18，Spring Cloud 2021.0.9，Spring Cloud Alibaba 2021.0.6.2（仅用 Sentinel） |
| 持久化 | MySQL 8.0，MyBatis，PageHelper |
| 流程引擎 | Flowable |
| 缓存 | Redis 7 |
| 对象存储 | MinIO（配置键名为 `oss.*`） |
| 前端 | Vue 2.7，Vue Router 3，Element UI 2.15，axios，Tiptap，bpmn-js，dhtmlx-gantt，Vue CLI 4 |

### 2.2 目录结构

```
.
├── euipdv2-master/              后端
│   ├── pom.xml                  根 POM（com.course:euipd）
│   ├── gateway/ system/ customer/ file2/ ftp/   五个微服务
│   ├── lib/repo/                项目内 Maven 仓库，存放公共库 server 的 jar 与 pom
│   ├── doc/db/                  数据库脚本：euipd-schema.sql（全部表结构）、init-super-admin.sql
│   ├── doc/api/                 HTTP 接口文档（985 个接口）
│   ├── docker/                  本地开发环境：参数模板、启停脚本、MySQL 初始化、MinIO 公共图片
│   ├── docker-compose.yml       本地中间件编排
│   ├── scripts/                 生成本地参数文件、JWT 密钥对与接口文档的脚本
│   └── THIRD-PARTY.txt          后端第三方依赖许可证清单
├── euipdv2-vue2/                前端
├── deploy-package/              生产部署包：Docker Compose 编排、运维脚本、参数模板与部署说明书
├── LICENSE  NOTICE  CHANGELOG.md  CONTRIBUTING.md  SECURITY.md  CODE_OF_CONDUCT.md
└── .github/                     Issue 与 Pull Request 模板、构建检查（GitHub Actions）
```

### 2.3 关于公共库 server

公共库 `server` 以编译后的 jar 提供，不附源代码，位于 `euipdv2-master/lib/repo/com/course/server/2.2.0/`。根 POM 将该目录声明为 Maven 仓库（`euipd-local`），各微服务按普通依赖引用 `com.course:server`，构建时无需额外安装。该 jar 同样按 MIT 许可证发布。

jar 内的 `config/application.properties` 只含占位符，口令与密钥一律经环境变量注入，见第四节。

### 2.4 接口文档

四个业务服务共 985 个 HTTP 接口，文档位于 [euipdv2-master/doc/api/](euipdv2-master/doc/api/README.md)：总览说明访问方式、登录与令牌、统一响应结构和鉴权规则；`system.md`、`customer.md`、`file.md`、`ftp.md` 按业务分组列出每个接口的方法、路径、说明、参数、返回内容与所需权限。接口文档由 `scripts/generate-api-doc.py` 从 Controller 源码生成。

## 三、快速开始（本地开发）

以下命令除特别说明外，均在 `euipdv2-master/` 目录执行。

### 3.1 环境要求

| 软件 | 版本 |
| --- | --- |
| Docker（含 Compose v2） | Docker Desktop 分配内存不少于 4 GB |
| JDK | 11（17 亦可编译运行） |
| Maven | 3.6 以上，需能访问 Maven 中央仓库或镜像 |
| Node.js | 16 以上（已在 20、24 上验证构建） |
| OpenSSL | 用于生成 JWT 密钥对 |

本机内存建议 16 GB 以上：五个微服务与中间件同时运行约占 6 GB。

### 3.2 填写参数

口令、密钥与测试账号集中在本地参数文件中，仓库只提供模板 `docker/env.example`。任选一种方式生成：

```bash
# 方式一：自动生成。必填项填入随机值，测试超级管理员手机号缺省 13800000000
bash scripts/generate-local-env.sh

# 方式二：手工填写
cp docker/env.example docker/.env.local
```

方式一同时生成前端的 `euipdv2-vue2/.env.dev.local`（口令加盐字符串 `VUE_APP_PASSWORD_KEY`）。超级管理员手机号可用环境变量指定，例如 `SUPER_ADMIN_PHONE=13900000000 bash scripts/generate-local-env.sh`；`docker/.env.local` 已存在时脚本保留原有取值、按模板补齐新增项（模板更新后重新执行即可），加 `--force` 则全部重新生成。

手工填写时按文件内注释填写 `docker/.env.local`。必填项为 MySQL、Redis、MinIO、RabbitMQ 的账号口令，微服务连接口令，对象存储访问密钥，成员初始口令，以及平台超级管理员的手机号与口令。可选外部集成（阿里云短信、GitLab、FTP）不使用时保持为空。

`docker/.env.local` 与 `euipdv2-vue2/.env.dev.local` 已被 `.gitignore` 忽略，只用于本机开发与测试，请勿提交到版本库或外传。

### 3.3 生成 JWT 密钥对

```bash
bash scripts/generate-jwt-keys.sh
```

脚本在 `euipdv2-master/keys/` 生成 `jwt-private.pem`（PKCS#8）与 `jwt-public.pem`。已有密钥时不会覆盖，需重新生成时加 `--force`（已签发的令牌随之失效）。

### 3.4 启动中间件并初始化数据库

```bash
bash docker/start-env.sh
```

脚本启动 MySQL、Redis、MinIO、RabbitMQ 容器。MySQL 首次创建时自动完成以下初始化：

1. 执行 `doc/db/euipd-schema.sql`，建库 `mycourse_bk` 并建立全部业务表；
2. 按 `SUPER_ADMIN_*` 参数执行 `doc/db/init-super-admin.sql`，创建平台超级管理员。

MinIO 自动创建 `euipd-doc`、`euipd-web` 等存储桶，并上传登录页使用的公共图片。MinIO 控制台为 http://127.0.0.1:18101。

数据库已存在时初始化不会重复执行。如需补建超级管理员，填写参数后执行 `bash docker/init-super-admin.sh`。

### 3.5 编译并启动后端

```bash
bash docker/start-services.sh start --build
```

脚本加载 `docker/.env.local`，编译五个微服务并依次启动，等待端口就绪。常用命令：

```bash
bash docker/start-services.sh status          # 查看状态
bash docker/start-services.sh logs customer   # 跟踪某个服务的控制台日志
bash docker/start-services.sh stop            # 停止全部服务
```

控制台日志位于 `logs/console/`。

### 3.6 启动前端

在 `euipdv2-vue2/` 目录执行：

```bash
npm install
npm run serve-dev
```

开发服务地址为 http://localhost:8080，接口经 `.env.dev` 中的 `VUE_APP_SERVER`（缺省 `http://127.0.0.1:18000`）访问网关。

机构注册对口令加盐使用的字符串 `VUE_APP_PASSWORD_KEY` 在仓库中为空。按 3.2 方式一生成参数时已写入 `.env.dev.local`；手工填写时自行生成随机值写入该文件（已被 git 忽略）：

```bash
echo "VUE_APP_PASSWORD_KEY=$(openssl rand -hex 16)" >> .env.dev.local
```

### 3.7 登录

使用 `SUPER_ADMIN_PHONE` 与 `SUPER_ADMIN_PASSWORD` 登录平台超级管理员，创建机构及其管理员；机构管理员登录后维护部门、成员与角色。新建成员与重置口令后的初始口令为 `EUIPD_INITIAL_PASSWORD`，首次登录须修改口令。

## 四、配置说明

### 4.1 配置加载顺序

每个微服务按以下顺序读取配置，前者优先：

1. 模块内 `application-dev.properties`（生产为 `application-prod.properties`）；
2. 公共库 jar 内的 `config/application.properties`；
3. 模块内 `application.properties`。

配置中的 `${EUIPD_*}` 占位符取自环境变量。本地开发由 `docker/start-services.sh` 从 `docker/.env.local` 加载；生产环境由部署者通过环境变量或外部配置文件提供。

### 4.2 环境变量

| 变量 | 必填 | 说明 |
| --- | --- | --- |
| `EUIPD_DB_URL` | 否 | 数据库 JDBC 地址，缺省连接本机 3306 端口的 `mycourse_bk` |
| `EUIPD_DB_USERNAME` | 否 | 数据库账号，缺省 `root` |
| `EUIPD_DB_PASSWORD` | 是 | 数据库口令 |
| `EUIPD_REDIS_HOST` / `EUIPD_REDIS_PORT` | 否 | Redis 地址，缺省 `127.0.0.1:6379` |
| `EUIPD_REDIS_PASSWORD` | 是 | Redis 口令 |
| `EUIPD_OSS_ACCESS_KEY_ID` / `EUIPD_OSS_ACCESS_KEY_SECRET` | 是 | 对象存储访问密钥 |
| `EUIPD_OSS_ENDPOINT` | 否 | 对象存储地址（浏览器与服务均可访问），缺省 `http://127.0.0.1:18100` |
| `EUIPD_INITIAL_PASSWORD` | 是 | 新建成员与重置口令后的初始口令，8 至 32 位并同时含字母与数字；未配置时新建成员与重置口令不可用 |
| `EUIPD_JWT_PRIVATE_KEY` | 否 | JWT 私钥位置，缺省 `file:../keys/jwt-private.pem`（相对模块目录） |
| `EUIPD_JWT_PUBLIC_KEY` | 否 | JWT 公钥位置，缺省 `file:../keys/jwt-public.pem` |
| `EUIPD_WEB_ROOT` | 否 | 前端访问地址（生产配置），缺省 `http://localhost:8080/` |
| `EUIPD_CUSTOMER_ACCESS_KEY_ID` / `EUIPD_CUSTOMER_ACCESS_KEY_SECRET` | 否 | 阿里云 AccessKey，用于短信 |
| `EUIPD_SMS_SIGNATURE` | 否 | 短信签名 |
| `EUIPD_SMS_VERIFICATION_TEMPLATE` / `EUIPD_SMS_NOTIFY_TEMPLATE` | 否 | 验证码与通知短信模板编号 |
| `EUIPD_SMS_REGISTER_RECHECK_TEMPLATE` / `EUIPD_SMS_REGISTER_PASS_TEMPLATE` | 否 | 新机构注册复核、复核通过的通知短信模板编号；留空则不发送 |
| `EUIPD_GITLAB_URL` / `EUIPD_GITLAB_TOKEN` | 否 | GitLab 地址与访问令牌 |
| `EUIPD_FTP_HOST` / `EUIPD_FTP_PORT` | 否 | FTP 服务器地址，缺省 `127.0.0.1:21` |
| `EUIPD_FTP_USERNAME` / `EUIPD_FTP_PASSWORD` | 否 | FTP 账号口令 |
| `EUIPD_FTP_WORKING_DIRECTORY` | 否 | FTP 工作目录，缺省 `/` |
| `EUIPD_VOD_ACCESS_KEY_ID` / `EUIPD_VOD_ACCESS_KEY_SECRET` | 否 | 阿里云视频点播 AccessKey，用于登录页介绍视频与视频上传；为空时视频不可用 |
| `EUIPD_VOD_REGION_ID` / `EUIPD_VOD_CATEGORY_ID` / `EUIPD_VOD_WORKFLOW_ID` / `EUIPD_VOD_STORAGE_LOCATION` | 否 | 视频上传参数，地域缺省 `cn-shanghai` |
| `EUIPD_POLYV_USER_ID` / `EUIPD_POLYV_APP_ID` / `EUIPD_POLYV_APP_SECRET` | 否 | 保利威直播账号；为空时跳过直播初始化 |
| `EUIPD_OA_HOST` / `EUIPD_OA_APPID` / `EUIPD_OA_SECRET` / `EUIPD_OA_SPK` | 否 | 泛微 OA（ecology）地址、应用标识、密钥与公钥 |
| `EUIPD_OA_MATERIAL_FORM_CREATER` | 否 | OA 物料表单创建人（OA 集成使用） |
| `EUIPD_SQL_SERVER_URL` / `EUIPD_SQL_SERVER_USER` / `EUIPD_SQL_SERVER_PASSWORD` | 否 | 外部 SQL Server 物料库（与 OA 集成配套） |

`docker/env.example` 中另有 MySQL、Redis、MinIO、RabbitMQ 容器自身的账号口令与超级管理员参数，说明见文件注释。该模板列出了系统用到的全部账号、口令与密钥。

平台超级管理员（`SUPER_ADMIN_PHONE`、`SUPER_ADMIN_PASSWORD`、`SUPER_ADMIN_NAME`）只在初始化数据库时使用：`docker/start-env.sh` 首次建库或 `docker/init-super-admin.sh` 按这三项创建账号，服务运行时不读取。之后修改超级管理员口令须登录系统修改，改 `.env.local` 不会生效。

### 4.3 其他配置文件

| 文件 | 说明 |
| --- | --- |
| `euipdv2-vue2/.env.dev`、`.env.prod` | 前端接口基址、对象存储公共地址与口令加盐字符串 `VUE_APP_PASSWORD_KEY`（空白，需自行生成），变量说明见 `euipdv2-vue2/.env.example` |
| 各模块 `application-*.properties` | 端口、上下文路径、对象存储地址等；口令均为 `${EUIPD_*}` 占位符 |
| jar 内 `config/ecology.properties` | 泛微 OA 与 SQL Server 集成参数，文件内不填值；各项取环境变量 `EUIPD_OA_*`、`EUIPD_SQL_SERVER_*` |

视频点播上传使用阿里云浏览器端上传 SDK，该 SDK 未声明开源许可证，不随仓库提供。需要该功能时，从阿里云视频点播文档下载 Web 端上传 SDK 1.5.3，按以下结构放入前端目录：

```
euipdv2-vue2/public/static/aliyunlib/aliyun-upload-sdk/
├── aliyun-upload-sdk-1.5.3.min.js
└── lib/
    ├── aliyun-oss-sdk-6.17.1.min.js
    └── es6-promise.min.js
```

缺少这些文件时，视频上传组件提示“未找到阿里云视频上传 SDK”，其他功能不受影响。

## 五、生产部署

单台 Linux 服务器以 Docker Compose 部署时，直接使用部署包 [`deploy-package/`](deploy-package/README.md)：执行 `deploy-package/scripts/build-artifacts.sh` 从源码生成后端 jar 与前端页面，在服务器上把 `.env.example` 复制为 `.env` 并填写，再执行 `./euipd.sh init` 与 `./euipd.sh up`。部署包不含任何账号与密钥，完整步骤见 [EUIPD部署说明书](deploy-package/EUIPD部署说明书.md)。

采用其他方式（物理机、虚拟机、Kubernetes 等）自行编排时，以下为部署所需的产物、组件与配置要点。

### 5.1 构建产物

```bash
# 后端：在 euipdv2-master/ 执行，产物为各模块 target/<模块>.jar
mvn -DskipTests clean package

# 前端：在 euipdv2-vue2/ 执行，产物为 dist/
npm ci
npm run build-prod
```

前端生产构建使用 `.env.prod`：接口前缀 `VUE_APP_SERVER=/api`，对象存储经同源路径 `/euipd-doc`、`/euipd-web` 访问。地址规划不同时修改该文件。构建前在 `.env.prod.local` 中设置 `VUE_APP_PASSWORD_KEY`（随机字符串，各部署自行生成，不要与他人共用）。

### 5.2 基础组件

| 组件 | 要求 |
| --- | --- |
| MySQL 8.0 | 执行 `doc/db/euipd-schema.sql` 建库建表，再执行 `doc/db/init-super-admin.sql` 创建超级管理员（用法见脚本开头注释）；建议为应用单独建账号 |
| Redis 7 | 设置访问口令 |
| MinIO | 建桶 `euipd-doc`（私有）与 `euipd-web`（匿名只读），将 `euipdv2-master/docker/minio/web-assets/` 中的图片上传到 `euipd-web`；为应用单独建访问密钥 |
| Collabora CODE（可选） | 正式文件与 Word 文档在线查看，镜像构建见 `euipdv2-master/docker/collabora/` |

### 5.3 运行微服务

每个服务以 `prod` 配置启动，例如：

```bash
java -jar system.jar --spring.profiles.active=prod \
     --spring.config.additional-location=optional:file:/etc/euipd/
```

1. 口令与地址通过第四节的环境变量提供，至少设置 `EUIPD_DB_URL`、`EUIPD_DB_USERNAME`、`EUIPD_DB_PASSWORD`、`EUIPD_REDIS_HOST`、`EUIPD_REDIS_PASSWORD`、`EUIPD_OSS_ENDPOINT`、`EUIPD_OSS_ACCESS_KEY_ID`、`EUIPD_OSS_ACCESS_KEY_SECRET`、`EUIPD_INITIAL_PASSWORD`、`EUIPD_WEB_ROOT`。其他配置项可写入 `--spring.config.additional-location` 指定目录下的 `application-prod.properties` 覆盖。
2. JWT 密钥对用 `scripts/generate-jwt-keys.sh` 生成后，以 `EUIPD_JWT_PRIVATE_KEY`（system 服务）与 `EUIPD_JWT_PUBLIC_KEY`（gateway 及其他服务）指向其绝对路径，例如 `file:/etc/euipd/keys/jwt-private.pem`。私钥只对 system 服务可读。
3. system、customer、file2、ftp 缺省只监听 `127.0.0.1`，网关路由缺省转发到本机 18001、18003、18005、18007 端口。服务分布在不同主机或容器时，为这四个服务设置环境变量 `SERVER_ADDRESS=0.0.0.0`（并用防火墙限制访问），同时覆盖网关的 `spring.cloud.gateway.routes[0..3].uri`。
4. 启动顺序建议为 system、customer、file2、ftp、gateway。customer 首次启动时建立 Flowable 流程引擎表并写入平台流程定义，耗时数分钟。
5. 对象存储经反向代理同源访问时，`EUIPD_OSS_ENDPOINT` 设为浏览器访问系统的地址（如 `https://euipd.example.com`），预签名地址才能通过校验。

### 5.4 反向代理

浏览器只访问反向代理，代理按路径转发：

| 路径 | 转发目标 | 要点 |
| --- | --- | --- |
| `/api/` | gateway:18000 | 去掉 `/api` 前缀；请求体上限不低于 1100 MB；超时不低于 600 秒 |
| `/api/file/wopi/` | 拒绝 | WOPI 回调只供 Collabora 在内部网络调用 |
| `/euipd-doc/`、`/euipd-web/` | MinIO API（缺省 9000） | 路径与 `Host` 头原样转发 |
| `/cool/`、`/browser/`、`/hosting/` | Collabora（缺省 9980） | 启用 Collabora 时配置；`/cool/*/ws` 需支持 WebSocket；拒绝 `/cool/convert-to` 与 `/cool/getMetrics` |
| 其他路径 | 前端 `dist/` | history 路由，未知路径回落到 `index.html` |

Nginx 示例（不含 Collabora）：

```nginx
server {
    listen 80;
    client_max_body_size 1100m;
    proxy_read_timeout 600s;
    root /usr/share/nginx/html;            # 前端 dist/

    location ^~ /api/file/wopi/ { return 403; }
    location ^~ /api/ {
        proxy_pass http://127.0.0.1:18000/; # 结尾的 / 去掉 /api 前缀
        proxy_set_header Host $http_host;
        proxy_set_header X-Forwarded-For $remote_addr;
        proxy_request_buffering off;
    }
    location ~ ^/(euipd-doc|euipd-web)(/|$) {
        proxy_pass http://127.0.0.1:18100;
        proxy_set_header Host $http_host;
        proxy_buffering off;
        proxy_request_buffering off;
    }
    location = /index.html { add_header Cache-Control "no-cache"; }
    location / { try_files $uri $uri/ /index.html; }
}
```

### 5.5 安全要求

只对外开放反向代理端口，MySQL、Redis、MinIO 与各微服务端口只在内部网络可达；对外服务启用 HTTPS；定期备份数据库、对象存储与 JWT 密钥。更多要求见 [SECURITY.md](SECURITY.md)。

## 六、常见问题

**编译时提示找不到 `com.course:server:2.2.0`**

公共库从项目内仓库 `lib/repo` 解析。Maven 离线模式（`-o`）不读取 `file://` 仓库，请以在线模式编译；若 `settings.xml` 配置了 `<mirrorOf>*</mirrorOf>` 的镜像，请改为 `<mirrorOf>*,!euipd-local</mirrorOf>`。

**在 IDE 中启动微服务时报口令或密钥缺失**

IDE 不会读取 `docker/.env.local`。请在运行配置中设置相同的环境变量（或使用 EnvFile 类插件），并将工作目录设为模块目录，使 JWT 密钥的缺省相对路径 `../keys/` 生效；也可设置 `EUIPD_JWT_PRIVATE_KEY`、`EUIPD_JWT_PUBLIC_KEY` 为绝对路径。

**新建成员时提示“未配置初始口令”**

设置 `EUIPD_INITIAL_PASSWORD` 后重启 system 与 customer 服务。

**前端 `npm run serve-dev` 报 `ERR_OSSL_EVP_UNSUPPORTED`**

项目使用的 webpack 4.47 已兼容 Node.js 17 以上。若因依赖版本较旧仍出现该错误，执行 `npm ci` 按锁文件重装依赖，或设置 `NODE_OPTIONS=--openssl-legacy-provider`。

**正式文件与 Word 文档无法在线查看**

该功能依赖可选组件 Collabora Online。执行 `bash docker/collabora/fetch-fonts.sh` 下载中文字体后，以 `docker compose --env-file docker/.env.local --profile rich up -d --build collabora` 启动。

**修改了口令参数但容器仍使用旧口令**

MySQL、MinIO 等容器只在首次创建数据卷时读取口令。开发环境可执行 `docker compose --env-file docker/.env.local down -v` 删除数据卷后重新启动（数据将清空）。

## 七、参与贡献

欢迎提交 Issue 与 Pull Request，流程与约定见 [CONTRIBUTING.md](CONTRIBUTING.md)，参与者请遵守 [行为准则](CODE_OF_CONDUCT.md)。安全问题请按 [SECURITY.md](SECURITY.md) 私下报告，不要公开提交 Issue。

## 八、许可证

本项目按 [MIT 许可证](LICENSE) 发布，可自由复制、使用、修改、合并、出版发行、再授权与销售，须保留版权声明与许可声明。

第三方依赖按各自许可证使用，以下几项附带使用条件，详见 [NOTICE](NOTICE)：

| 依赖 | 许可证 | 使用条件 |
| --- | --- | --- |
| dhtmlx-gantt 7.1.13 标准版（前端甘特图） | GPL-2.0 | 分发含该组件的前端构建产物时须遵守 GPL-2.0；闭源或商业分发须购买商业授权或替换该组件 |
| bpmn-js（前端流程图） | MIT 加附加条款 | 流程图中的 bpmn.io 水印不得移除或遮挡 |
| MySQL Connector/J（后端） | GPL-2.0 附 FOSS 例外 | 闭源分发宜由使用者自行获取驱动或改用 MariaDB Connector/J |
| hutool（后端） | 木兰宽松许可证第 2 版 | 保留版权与许可声明 |

后端完整依赖许可证清单见 `euipdv2-master/THIRD-PARTY.txt`。

## 九、联系方式

- 作者：周朔鹏
- 邮箱：391902958@qq.com
- 网站：[https://www.euipd.com](https://www.euipd.com)
