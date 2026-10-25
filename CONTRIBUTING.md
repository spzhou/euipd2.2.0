# 参与贡献

欢迎通过 Issue 与 Pull Request 参与 EUIPD 的改进。参与者请遵守 [行为准则](CODE_OF_CONDUCT.md)。

## 一、提交 Issue

1. 提交前先搜索已有 Issue，避免重复。
2. 缺陷报告写明版本、运行环境、复现步骤、期望结果与实际结果，附相关日志（去除口令、令牌等敏感信息）。
3. 功能建议写明使用场景与期望行为。
4. 安全漏洞不要公开提交，按 [SECURITY.md](SECURITY.md) 私下报告。

## 二、提交 Pull Request

1. Fork 仓库，从主分支创建特性分支，分支名如 `feature/xxx`、`fix/xxx`。
2. 一个 Pull Request 只解决一个问题，改动较大时先开 Issue 讨论方案。
3. 提交前完成本地编译，并在本地开发环境中验证改动涉及的功能。
4. 提交说明采用 `类型: 简述` 格式，类型取 `feat`、`fix`、`docs`、`refactor`、`perf`、`chore`。
5. 按模板填写 Pull Request 说明，关联对应 Issue。

## 三、开发约定

### 3.1 后端

1. 代码风格与周边代码保持一致；Java 11 语法，不引入新的大型框架。
2. 公共库 `server` 以 jar 提供，位于 `euipdv2-master/lib/repo/`，Pull Request 不修改该 jar。需要公共库配合的改动请先开 Issue 说明。
3. 口令、密钥、令牌一律通过 `${EUIPD_*}` 占位符从环境变量读取，不得写入代码或配置文件；新增变量须同步登记到 `docker/env.example` 与根目录 `README.md` 的配置说明；生产部署需要的，同时登记到 `deploy-package/.env.example` 与 `deploy-package/docker-compose.yml` 的 `x-app-env`。
4. 接口权限：网关按 `gateway/src/main/resources/security/module-path.yml` 校验模块路径，各服务按 jar 内 `security/module-path.yml` 与 `security/tenant-ui-permission.json` 校验菜单与操作权限。新增接口需调整时，在对应模块的 `src/main/resources/security/` 下放置同名文件覆盖 jar 内版本，并保持网关与服务两份 `module-path.yml` 一致。
5. 新增、删除或修改接口后，在 `euipdv2-master/` 目录执行 `python3 scripts/generate-api-doc.py` 重新生成 `doc/api/` 接口文档，并随代码一起提交。
6. 本地测试用的口令、密钥与账号只放在 `docker/.env.local`、`euipdv2-vue2/.env.dev.local` 等被忽略的文件中（可用 `scripts/generate-local-env.sh` 生成），提交前确认 `git status` 中没有这类文件。入库与不入库的划分见第四节。

### 3.2 数据库

1. 表结构以 `euipdv2-master/doc/db/euipd-schema.sql` 为准，变更时直接修改该文件中的建表语句，保持脚本可在空库上一次执行完成。
2. Pull Request 说明中列出变更的表与字段，并附已有数据库所需的 `ALTER` 语句，供使用者升级时参考。
3. 字符集与排序规则与相邻表保持一致（utf8mb4）。

### 3.3 前端

1. 提交前执行 `npm run lint`，修正全部报错。
2. 接口基址与对象存储地址只从 `.env.*` 的 `VUE_APP_*` 变量读取，不得硬编码服务器地址。
3. 公共图片放在 MinIO 公共桶（来源目录 `euipdv2-master/docker/minio/web-assets/`），页面经 `webAssetUrl()` 取地址。

### 3.4 第三方依赖

新增依赖应当采用 MIT、Apache-2.0、BSD 等宽松许可证；引入 GPL、AGPL 等强著佐权许可证或附加使用条件的依赖前，先开 Issue 讨论，并同步更新 `NOTICE`。

### 3.5 文档

文档使用中文，引号用中文弯引号“”，不使用 emoji。改动影响使用方式时，同步更新相应的 `README.md` 与 `CHANGELOG.md`。

## 四、入库与不入库的文件

入库（模板与不含口令的配置）：

| 文件 | 说明 |
| --- | --- |
| `euipdv2-master/docker/env.example` | 后端全部账号、口令、密钥的模板，取值留空 |
| `euipdv2-vue2/.env.example`、`.env.dev`、`.env.prod` | 前端变量模板与开发、生产缺省值，`VUE_APP_PASSWORD_KEY` 留空 |
| 各模块 `application*.properties` | 口令与密钥均为 `${EUIPD_*}` 占位符 |
| `euipdv2-master/doc/db/euipd-schema.sql`、`init-super-admin.sql` | 建库脚本与超级管理员初始化脚本，不含账号取值 |
| `euipdv2-master/lib/repo/**/*.jar`、`.mvn/wrapper/maven-wrapper.jar` | 公共库与 Maven Wrapper |
| `deploy-package/` 下的脚本、编排文件、`config/`、`nginx/`、`.env.example` 与说明书 | 部署包；`.env.example` 为部署参数模板，取值留空 |

不入库（已由 `.gitignore` 忽略）：

| 文件 | 说明 |
| --- | --- |
| `euipdv2-master/docker/.env.local`、`*.env`、`.env` | 本机口令、密钥与测试账号 |
| `euipdv2-vue2/.env.*.local` | 前端本机变量（含 `VUE_APP_PASSWORD_KEY`） |
| `euipdv2-master/keys/`、`*.pem`、`*.key`、`*.p12`、`*.pfx`、`*.jks`、`*.keystore` | JWT 密钥对与证书 |
| `application-local.*`、`bootstrap-local.properties` | 模块本机配置覆盖 |
| `backup/`、`backups/`、`*.dump`、`*dump*.sql`、`*.sql.gz` | 数据库备份与导出 |
| `target/`、`node_modules/`、`dist/`、`logs/`、`*.log` | 构建产物、依赖与日志 |
| `docker/collabora/fonts/*`、`euipdv2-vue2/public/static/aliyunlib/` | 由使用者自行下载的字体与 SDK |
| `deploy-package/` 下的 `app/jars/`、`app/VERSION`、`web/`、`mysql/db/`、`mysql/my.cnf`、`minio/web-assets/`、`collabora/` | 部署包构建产物，由 `scripts/build-artifacts.sh` 生成 |
| `deploy-package/` 下的 `.env`、`keys/`、`data/`、`backups/`、`images/`，根目录 `release/` | 部署服务器的口令、密钥、数据与备份，打包产物 |

## 五、许可

提交贡献即表示同意贡献内容按本项目的 [MIT 许可证](LICENSE) 发布。
