# EUIPD 后端（euipdv2-master）

EUIPD 研发过程管理系统的后端，Spring Boot 2.7 + Spring Cloud 微服务，Java 11。项目介绍、本地运行步骤与配置说明见仓库根目录 [README.md](../README.md)。

## 模块

| 模块 | 说明 | 端口 | 上下文路径 |
| --- | --- | --- | --- |
| `gateway` | API 网关，浏览器唯一入口；JWT 验签与模块权限校验 | 18000 | 无 |
| `system` | 登录、用户、角色、权限、验证码 | 18001 | `/system` |
| `customer` | 研发业务主数据与流程、研发物料仓库 | 18005 | `/customer` |
| `file2` | 对象存储上传与签名下载 | 18003 | `/file` |
| `ftp` | 大文件传输 | 18007 | `/ftp` |

公共库 `server`（领域模型、MyBatis Mapper、统一响应、租户上下文、对象存储客户端）以 jar 形式提供，位于 `lib/repo/com/course/server/2.2.0/`，根 POM 已声明该仓库，构建时自动解析。代码中保留了早期在线课程业务的部分包与枚举，当前业务主线为研发管理（RDMS）。

## 目录

```
euipdv2-master/
├── pom.xml                  根 POM
├── gateway/ system/ customer/ file2/ ftp/
├── lib/repo/                项目内 Maven 仓库（server jar）
├── keys/                    JWT 密钥对（运行 scripts/generate-jwt-keys.sh 生成，不入版本库）
├── doc/db/                  euipd-schema.sql（全部表结构）、init-super-admin.sql（超级管理员初始化）
├── doc/api/                 HTTP 接口文档（由 scripts/generate-api-doc.py 生成）
├── docker/                  本地开发环境：env.example、start-env.sh、start-services.sh、init-super-admin.sh、minio/web-assets
├── docker-compose.yml       本地中间件编排
├── scripts/                 generate-local-env.sh、generate-jwt-keys.sh、generate-api-doc.py
└── THIRD-PARTY.txt          第三方依赖许可证清单
```

## 常用命令

```bash
bash scripts/generate-local-env.sh           # 生成本地参数文件（随机口令与测试账号），或 cp docker/env.example docker/.env.local 手工填写
bash scripts/generate-jwt-keys.sh            # 生成 JWT 密钥对
bash docker/start-env.sh                     # 启动 MySQL、Redis、MinIO 等中间件
bash docker/start-services.sh start --build  # 编译并启动五个微服务
mvn -DskipTests clean package                # 仅编译（须在线模式，-o 不读取 lib/repo）
python3 scripts/generate-api-doc.py          # 修改 Controller 后重新生成 doc/api/ 接口文档
```
