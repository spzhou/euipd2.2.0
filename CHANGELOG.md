# 更新日志

格式参照 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/)，版本号遵循 [语义化版本](https://semver.org/lang/zh-CN/)。

## [2.2.0] - 2026-10-21

首个开源版本。

### 新增

- 研发过程管理全流程：需求、特性与系统工程、预立项与立项、项目与子项目、任务、测试、评审、里程碑、产品发布。
- 预算与料工费、工时与绩效、质量缺陷、文档与归档、研发物料仓库、内部邮件。
- 多租户机构管理，基于角色的菜单与操作权限。
- 本地开发环境脚本（`docker/`）。
- 数据库脚本 `doc/db/euipd-schema.sql`（全部表结构）与 `doc/db/init-super-admin.sql`（平台超级管理员初始化）。

### 开源调整

- 采用 MIT 许可证。
- 公共库 `server` 以 jar 形式提供，置于项目内 Maven 仓库 `lib/repo`。
- 口令、密钥与外部集成参数全部改为环境变量配置，仓库不含任何可用口令；成员初始口令改为配置项 `EUIPD_INITIAL_PASSWORD`。
- JWT 密钥对改为由使用者生成，存放于 `euipdv2-master/keys/`。
- 不提供增量迁移脚本与需求文档。
- 新增生产部署包 `deploy-package/`：Docker Compose 编排（nginx、5 个微服务、MySQL、Redis、MinIO，可选 Collabora）、运维脚本 `euipd.sh`（安装、启停、检查、备份、恢复、升级、解除锁定）、构建脚本 `scripts/build-artifacts.sh` 与《EUIPD部署说明书》。部署包只含参数模板 `.env.example`，账号与超级管理员口令由部署者填写，组件口令与令牌密钥由脚本随机生成；构建产物、`.env`、密钥与数据不入版本库。
- 对象存储地址改为配置项 `EUIPD_OSS_ENDPOINT`。
- 前端口令加盐字符串改为配置项 `VUE_APP_PASSWORD_KEY`，由使用者自行生成；server jar 中未使用的同名常量与直播频道编号已移除。
- 新增 HTTP 接口文档 `euipdv2-master/doc/api/`（985 个接口，含鉴权要求）及生成脚本 `scripts/generate-api-doc.py`。
- 新增 `scripts/generate-local-env.sh`：按模板生成本地参数文件（随机口令、测试超级管理员、前端加盐字符串），生成的文件不入版本库。
- 新增 `.gitattributes` 与 GitHub Actions 构建检查（后端编译、前端 lint 与生产构建）。
- 端口改用 18000 段：gateway 18000、system 18001、file2 18003、customer 18005、ftp 18007，本地 MinIO 18100（API）与 18101（控制台），Collabora 18980。
- 第三方依赖许可证核查结果写入 `NOTICE` 与 `euipdv2-master/THIRD-PARTY.txt`。

### 安全

- 移除登录时自动创建平台超级机构与固定手机号管理员的逻辑；平台超级管理员只能由 `doc/db/init-super-admin.sql` 按使用者填写的参数创建。
- 移除登录前经 SSH 读取主机标识的授权校验（`spring.system.*` 参数与 JSch 依赖）。
- 新机构注册的复核短信改发平台机构登记的联系电话，模板编号改为配置项 `EUIPD_SMS_REGISTER_RECHECK_TEMPLATE`、`EUIPD_SMS_REGISTER_PASS_TEMPLATE`；移除短信工具类中的测试入口与写死的签名、模板、号码。
- 接口授权（`/system/auth2`）的查询、新增、删除及 OA 建用户接口增加机构校验，只能操作本机构（平台超级机构可代管）；AccessKey 与 AccessSecret 一律由服务端生成。
- 前端未使用的旧课程接口 `/customer/web/` 移出网关免登录名单。
- 全部账号与密钥改由环境变量配置并登记到 `docker/env.example`：视频点播（`EUIPD_VOD_*`，原为无法生效的常量占位符）、保利威直播（`EUIPD_POLYV_*`）、泛微 OA 与 SQL Server 物料库（`EUIPD_OA_*`、`EUIPD_SQL_SERVER_*`，原只能修改 jar 内文件）；server jar 中未使用的密钥占位常量已删除。
- `scripts/generate-local-env.sh` 在 `.env.local` 已存在时保留原有取值并按模板补齐新增项。
- `.gitignore` 增加 `.env`、`*.env`、证书库与数据库备份导出文件；入库与不入库的划分见 `CONTRIBUTING.md` 第四节。
- org.json 升级到 20231013（公有领域许可）。
- 移除未声明开源许可证的阿里云视频点播上传 SDK（Java 与浏览器端），删除依赖该 SDK 的服务端视频上传接口 `POST /file/admin/vod`；浏览器端视频上传需使用者自行放置 SDK。
- 第三方静态资源（Bootstrap、Font Awesome、md5.js）恢复原作者的版权声明；删除未使用的 Ace 模板样式表。
