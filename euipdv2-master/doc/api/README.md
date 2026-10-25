# EUIPD HTTP 接口文档

本目录列出 system、customer、file2、ftp 四个微服务对外提供的全部 HTTP 接口，共 985 个。公共库 server 只提供 jar，不含 HTTP 接口；各接口的业务实现位于 server jar 中，接口定义位于各微服务的 Controller 源码。

| 服务 | 文档 | 上下文路径 | 接口数 | 内容 |
| --- | --- | --- | --- | --- |
| system | [system.md](system.md) | `/system` | 78 | 登录认证、令牌、机构、用户、角色、资源权限、验证码与短信 |
| customer | [customer.md](customer.md) | `/customer` | 851 | 需求、特性、产品、项目与子项目、工单、评审、里程碑、预算与费用、质量与缺陷、物料仓库、工时绩效、流程、邮件 |
| file2 | [file.md](file.md) | `/file` | 55 | 文件记录、对象存储上传与签名下载、富文本资源、Word 导入、在线查看 |
| ftp | [ftp.md](ftp.md) | `/ftp` | 1 | 大文件经 FTP 中转上传 |

## 1. 访问方式

浏览器与第三方调用方只访问网关：

- 本地开发：`http://127.0.0.1:18000` + 接口路径，例如 `http://127.0.0.1:18000/system/auth/login`。
- 生产环境：经反向代理同源访问，`/api` + 接口路径（代理去掉 `/api` 前缀后转发到网关）。

网关按路径前缀把请求转发到对应服务，路径不变。各服务只监听本机地址，不应绕过网关直接访问。

## 2. 请求约定

- 绝大多数接口为 `POST`，请求体为 JSON，`Content-Type: application/json`。
- 需要登录的接口在请求头携带 `Authorization: Bearer <accessToken>`。
- 路径中的 `{xxx}` 为路径参数。文档“参数”列的含义：`体` 为 JSON 请求体类型，`参` 为查询参数，`表单` 为 `multipart/form-data` 字段；`—` 表示没有请求体与查询参数。
- 请求体、返回内容中的类型（如 `RdmsProjectDto`、`PageDto`）定义在 server jar 的 `com.course.server.dto` 包中，可用 IDE 查看 jar 内的类结构。
- 网关会删除客户端传入的 `X-Tenant-Id`、`X-User-Id`、`X-Membership-Id`、`X-User-Group`、`X-Scopes` 请求头，并按令牌内容重新写入后转发给服务；调用方不要依赖这些请求头。

## 3. 登录与令牌

| 步骤 | 接口 | 说明 |
| --- | --- | --- |
| 1 | `GET /system/rdms/kaptcha/image-code/{imageCodeToken}` | 获取图片验证码，`imageCodeToken` 由调用方生成的随机串 |
| 2 | `GET /system/rdms/user/login-salt/{loginName}` | 按登录手机号读取口令盐值，返回在 `content` 中 |
| 3 | `POST /system/auth/login` | 体：`loginName`、`password`（`md5(明文口令 + 盐值)` 的十六进制小写串）、`imageCode`、`imageCodeToken` |
| 4 | `POST /system/auth/refresh` | 体：`refreshToken`；访问令牌过期前后换取新令牌 |
| 5 | `POST /system/auth/logout` | 注销当前令牌 |

登录成功时 `content` 中含 `accessToken`、`refreshToken` 与用户信息。若 `content.passwordChangeRequired` 为 `true`（以初始口令登录），须调用 `POST /system/auth/password/initial`（体：`passwordChangeTicket`、`newPassword`）设置新口令后才获得令牌。

令牌为 RSA 签名的 JWT，公钥可从 `GET /system/auth/jwks` 或 `GET /system/auth/public-key` 获取。访问令牌缺省有效期 30 分钟，刷新令牌 8 小时，见 system 服务的 `euipd.security.jwt.*` 配置。

## 4. 响应结构

业务接口统一返回 `ResponseDto`：

```json
{
  "success": true,
  "code": null,
  "message": "保存成功",
  "content": {}
}
```

| 字段 | 说明 |
| --- | --- |
| `success` | 业务是否成功。业务校验失败时 HTTP 状态仍为 200，`success` 为 `false`，原因见 `message` |
| `code` | 业务错误码，多数接口为空 |
| `message` | 提示信息 |
| `content` | 返回内容，类型见文档“返回内容”列；`—` 表示不返回内容或类型不固定 |

分页接口的请求体与返回内容为 `PageDto`：请求时填写 `page`（从 1 开始）、`size` 及筛选字段（`keyWord`、`status`、`type`、`customerId`、`subprojectId`、`loginUserId` 等，按接口需要），返回时 `total` 为总数、`list` 为当前页数据。

少数接口不使用 `ResponseDto`，例如验证码图片、文件流下载、WOPI 回调、JWKS，文档“返回内容”列给出其类型。

## 5. 鉴权

网关对每个请求按以下顺序判定，结果即文档“鉴权”列：

| 鉴权列 | 含义 |
| --- | --- |
| `公开` | 在网关白名单中，不需要令牌 |
| `登录` | 需要有效令牌，不校验模块权限 |
| `模块:操作`，如 `PROJECT:VIEW` | 需要有效令牌，且令牌权限范围含该项、`模块:*` 或 `PLATFORM:*` |
| `另需 …` | 服务端方法上另有 `@RequiresScope` 校验，须满足其中之一 |

模块由路径前缀决定，映射表为 `gateway/src/main/resources/security/module-path.yml`（最长前缀优先）。操作由 HTTP 方法与路径推断：

| 条件 | 操作 |
| --- | --- |
| `GET` 请求 | `VIEW`；路径含 `/export` 为 `EXPORT`，含 `/download` 为 `DOWNLOAD` |
| `POST` 路径含 `/save`、`/create`、`/add`、`/withdraw`、`/setup-application`、`/direct-setup`、`/module-setup` | `CREATE` |
| `POST` 路径含 `/approve`、`/review`、`/audit` 或以 `/setup` 结尾等审批类路径 | `APPROVE` |
| `POST` 路径含 `/export`、`/import` | `EXPORT`、`IMPORT` |
| `POST` 路径含 `/update`、`/edit`、`/modify` | `UPDATE` |
| `POST` 路径含 `/delete`、`/del`、`/remove` | `DELETE` |
| `POST` 路径含 `/list`、`/search`、`/query`、`/get`、`/count` 等 | `VIEW` |
| 其他 `POST`，以及 `PUT`、`PATCH` | `UPDATE` |
| `DELETE` 请求 | `DELETE` |

规则按表中顺序匹配，先命中者生效。用户的权限范围由角色与机构授权计算，登录时写入令牌。除网关校验外，服务端还按机构（租户）隔离数据，只能访问本机构的数据。

网关拒绝请求时返回：

| HTTP 状态 | `code` | 原因 |
| --- | --- | --- |
| 401 | `TOKEN_MISSING` | 未携带令牌 |
| 401 | `TOKEN_EXPIRED` | 令牌已过期，应调用刷新接口 |
| 401 | `TOKEN_INVALID` | 令牌无效或已注销 |
| 403 | `NO_PERMISSION` | 令牌权限范围不含所需的 `模块:操作` |

## 6. 文档说明与维护

- “说明”列优先取 Controller 方法上的注释；没有注释或注释为复制粘贴的通用文字时，由方法名按术语表生成，用词简明，具体行为以源码为准。
- 术语对照：工单（job item）、特性（character）、CBB（共用基础模块）、PJM 项目经理、PDM 产品经理、SM 系统工程师、TM 测试主管、SYSGM 系统工程总监、PDGM 产品总监、QCGM 质量总监、FGGM 法规总监、TGM 测试总监、IPMT 集成组合管理团队。
- `system.md`、`customer.md`、`file.md`、`ftp.md` 由 `scripts/generate-api-doc.py` 生成。新增或修改接口后，在 `euipdv2-master/` 目录执行 `python3 scripts/generate-api-doc.py` 重新生成，不要手工编辑。
