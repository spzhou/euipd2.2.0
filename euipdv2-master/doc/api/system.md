# system 服务接口

登录认证、令牌、机构、用户、角色、资源权限、验证码与短信。

- 上下文路径：`/system`，缺省端口 18001；浏览器经网关访问，路径不变。
- 接口数：78。公共约定（请求头、响应结构、鉴权列含义）见 [README.md](README.md)。
- 本文件由 `scripts/generate-api-doc.py` 生成，修改 Controller 后重新生成，不要手工编辑。

## 目录

- [资源](#资源-resourcecontroller)（4）
- [应用信息](#应用信息-appinfocontroller)（1）
- [登录认证](#登录认证-authcontroller)（8）
- [接口授权登录](#接口授权登录-rdmsauthcontroller)（8）
- [机构配置](#机构配置-rdmscustomerconfigcontroller)（4）
- [机构](#机构-rdmscustomercontroller)（17）
- [图片验证码](#图片验证码-rdmskaptchacontroller)（1）
- [用户资源](#用户资源-rdmsuserresourcecontroller)（3）
- [机构角色](#机构角色-rdmscustomerrolecontroller)（5）
- [用户角色](#用户角色-rdmsuserrolecontroller)（8）
- [SMS短信](#sms短信-rdmssmscontroller)（1）
- [用户](#用户-rdmsusercontroller)（13）
- [机构权限策略](#机构权限策略-tenantuipolicycontroller)（4）
- [视频点播](#视频点播-vodcontroller)（1）

## 资源 ResourceController

源码：`system/src/main/java/com/course/system/controller/admin/ResourceController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/system/admin/resource/list` | 查询资源列表 | 体 `PageDto` | — | SYSTEM:VIEW |
| POST | `/system/admin/resource/save` | 保存，id有值时更新，无值时新增 | 体 `String` | — | SYSTEM:CREATE |
| DELETE | `/system/admin/resource/delete/{id}` | 删除资源 | — | — | SYSTEM:DELETE |
| GET | `/system/admin/resource/load-tree` | 资源树查询 | — | — | SYSTEM:VIEW |

## 应用信息 AppInfoController

源码：`system/src/main/java/com/course/system/controller/AppInfoController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| GET | `/system/app-info/version` | 查询版本 | — | `Map<String, String>` | MEMBER:VIEW |

## 登录认证 AuthController

源码：`system/src/main/java/com/course/system/controller/AuthController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/system/auth/login` | 登录，返回访问令牌与刷新令牌 | 体 `RdmsUserDto` | — | 公开 |
| POST | `/system/auth/refresh` | 用刷新令牌换取新的访问令牌 | 体 `Map<String, String>` | — | 公开 |
| POST | `/system/auth/logout` | 退出登录并注销令牌 | 体 `Map<String, String>` | — | 公开 |
| POST | `/system/auth/tenant/switch` | 切换当前机构，返回新的登录结果 | 体 `Map<String, String>` | — | 公开 |
| POST | `/system/auth/password` | 本人修改密码。体为 oldPassword、newPassword，均为 md5(明文 + 盐)。成功后须重新登录 | 体 `Map<String, String>` | — | 公开 |
| POST | `/system/auth/password/initial` | 以初始密码登录后设置新密码。体为 passwordChangeTicket、newPassword；成功后返回登录结果 | 体 `Map<String, String>` | — | 公开 |
| GET | `/system/auth/jwks` | 查询JWKS 公钥集 | — | `Map<String, Object>` | 公开 |
| GET | `/system/auth/public-key` | 查询公钥 | — | `Map<String, String>` | 公开 |

## 接口授权登录 RdmsAuthController

源码：`system/src/main/java/com/course/system/controller/rdms/RdmsAuthController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/system/auth2/getAccessByCustomerId/{customerId}` | 根据客户的ID获取相应客户的授权信息列表 | — | `List<RdmsAuthInfoDto>` | WORKER:VIEW |
| POST | `/system/auth2/save` | 保存一条客户授权信息 | 体 `RdmsAuthDto` | `RdmsAuth` | WORKER:CREATE |
| DELETE | `/system/auth2/del/{id}` | 删除一条客户授权信息 | — | `String` | WORKER:DELETE |
| POST | `/system/auth2/getToken` | 根据AccessKey和AccessSecret获取认证令牌 | 体 `RdmsTokenRequestDto` | `RdmsAuthDto` | 公开 |
| POST | `/system/auth2/api/getAuthTestInfo/{customerId}` | 提供一个API接口测试函数 | — | `String` | AUTH2:VIEW |
| POST | `/system/auth2/api/feedbackWorkflowStatus` | OA流程处理完成后,返回一个结构体,本服务根据状态对后台数据进行操作 | 体 `RdmsAuthWorkflowStatusDto` | `String` | AUTH2:UPDATE |
| POST | `/system/auth2/api/feedbackReturnWarehouseApprovalStatus` | OA流程物料退库处理完成后,返回一个结构体,本服务根据状态对后台数据进行操作 | 体 `RdmsAuthWorkflowStatusDto` | `String` | AUTH2:UPDATE |
| POST | `/system/auth2/api/createUser` | 从OA发起创建新用户的程序接口 | 体 `RdmsCustomerUserDto` | `RdmsCustomerUserDto` | AUTH2:CREATE |

## 机构配置 RdmsCustomerConfigController

源码：`system/src/main/java/com/course/system/controller/rdms/RdmsCustomerConfigController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| GET | `/system/rdms/config/customer/get-customer-config/{customerId}` | 查询机构配置 | — | `RdmsCustomerConfigDto` | SYSTEM:VIEW |
| POST | `/system/rdms/config/customer/save` | 保存机构配置 | 体 `RdmsCustomerConfigDto` | `RdmsCustomerConfigDto` | SYSTEM:CREATE |
| POST | `/system/rdms/config/customer/list` | 查询机构配置列表 | 体 `PageDto<RdmsCustomerConfigDto>` | `PageDto<RdmsCustomerConfigDto>` | SYSTEM:VIEW |
| DELETE | `/system/rdms/config/customer/del/{id}` | 删除机构配置 | — | `PageDto<RdmsCustomerConfigDto>` | SYSTEM:DELETE |

## 机构 RdmsCustomerController

源码：`system/src/main/java/com/course/system/controller/rdms/RdmsCustomerController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/system/rdms/customer/save` | 保存机构 | 体 `RdmsCustomerDto` | — | CUSTOMER:CREATE |
| POST | `/system/rdms/customer/modify` | 修改客户信息 | 体 `RdmsCustomerDto` | — | CUSTOMER:UPDATE |
| POST | `/system/rdms/customer/changeAdmin` | 修改客户管理员后需要提示重新设置密码 | 体 `RdmsHmiAdminDto` | — | CUSTOMER:UPDATE |
| POST | `/system/rdms/customer/saveBoss` | 保存客户关联的最终管理人信息 | 参 `customerId` | `RdmsBossDto` | CUSTOMER:CREATE |
| POST | `/system/rdms/customer/changeAdminPassword` | 修改客户管理员密码 | 体 `RdmsHmiAdminDto` | — | CUSTOMER:UPDATE |
| POST | `/system/rdms/customer/get-customer-info/{customerId}` | 根据客户ID获取客户详细信息 | — | `RdmsCustomerDto` | CUSTOMER:VIEW |
| POST | `/system/rdms/customer/get-customer-by-loginname/{loginname}` | 根据登录名称获取客户信息 | — | `RdmsCustomerDto` | 公开 |
| POST | `/system/rdms/customer/login-search` | 登录搜索 | 体 `RdmsCustomerDto` | — | CUSTOMER:UPDATE |
| POST | `/system/rdms/customer/inst-search` | 实例搜索 | 体 `RdmsCustomerDto` | — | CUSTOMER:UPDATE |
| POST | `/system/rdms/customer/contact-search` | 根据联系电话进行查询 | 体 `RdmsCustomerDto` | — | CUSTOMER:UPDATE |
| POST | `/system/rdms/customer/list` | 查询机构列表 | 体 `PageDto` | — | CUSTOMER:VIEW |
| POST | `/system/rdms/customer/customerList` | 机构列表 | 体 `PageDto` | — | CUSTOMER:UPDATE |
| POST | `/system/rdms/customer/getOutSourcingCustomerList/{customerId}` | 查询外协机构列表 | — | `List<RdmsCustomerDto>` | SUB_PROJECT:VIEW |
| POST | `/system/rdms/customer/delete-check` | 删除前核对：体为 id、customerName（手工输入的机构名称）、password（md5(超管明文 + 超管盐)） | 体 `RdmsCustomerDto` | — | CUSTOMER:DELETE；另需 CUSTOMER:DELETE / CUSTOMER:* / PLATFORM:* |
| POST | `/system/rdms/customer/delete` | 删除机构：体同 delete-check，服务端再次核对后置删除标记，该机构会话随即失效 | 体 `RdmsCustomerDto` | — | CUSTOMER:DELETE；另需 CUSTOMER:DELETE / CUSTOMER:* / PLATFORM:* |
| POST | `/system/rdms/customer/save-password` | 设置机构侧密码。password 为 md5(明文 + 盐)；该机构已签发的会话随即失效 | 体 `RdmsCustomerDto` | — | CUSTOMER:CREATE；另需 CUSTOMER:UPDATE / USER:UPDATE / PLATFORM:* |
| GET | `/system/rdms/customer/initDefaultDepartment` | 补数：遍历全部机构写入默认部门与默认职级，限超管调用 | — | — | WORKER:VIEW |

## 图片验证码 RdmsKaptchaController

源码：`system/src/main/java/com/course/system/controller/rdms/RdmsKaptchaController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| GET | `/system/rdms/kaptcha/image-code/{imageCodeToken}` | 查询图片验证码 | — | — | 公开 |

## 用户资源 RdmsUserResourceController

源码：`system/src/main/java/com/course/system/controller/rdms/RdmsUserResourceController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/system/rdms/resource/user/list` | 查询用户资源列表 | 体 `PageDto` | — | RESOURCE:VIEW |
| POST | `/system/rdms/resource/user/save` | 保存，id有值时更新，无值时新增 | 体 `String` | — | RESOURCE:CREATE |
| DELETE | `/system/rdms/resource/user/delete/{id}` | 删除用户资源 | — | — | RESOURCE:DELETE |

## 机构角色 RdmsCustomerRoleController

源码：`system/src/main/java/com/course/system/controller/rdms/RdmsCustomerRoleController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/system/rdms/role/customer/list` | 查询机构角色列表 | 体 `PageDto` | — | ROLE:VIEW |
| POST | `/system/rdms/role/customer/save` | 保存，id有值时更新，无值时新增 | 体 `RdmsRoleDto` | — | ROLE:CREATE |
| DELETE | `/system/rdms/role/customer/delete/{id}` | 删除机构角色 | — | — | ROLE:DELETE |
| POST | `/system/rdms/role/customer/save-user` | 保存用户 | 体 `RdmsRoleDto` | — | ROLE:CREATE |
| GET | `/system/rdms/role/customer/list-user/{roleId}` | 加载用户 | — | — | ROLE:VIEW |

## 用户角色 RdmsUserRoleController

源码：`system/src/main/java/com/course/system/controller/rdms/RdmsUserRoleController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/system/rdms/role/user/list` | 查询用户角色列表 | 体 `PageDto` | — | WORKER:VIEW |
| POST | `/system/rdms/role/user/save` | 保存，id有值时更新，无值时新增 | 体 `RdmsRoleDto` | — | WORKER:CREATE；另需 WORKER:UPDATE / WORKER:CREATE / WORKER:* |
| DELETE | `/system/rdms/role/user/delete/{id}` | 删除用户角色 | — | — | WORKER:DELETE |
| POST | `/system/rdms/role/user/save-auth` | 修改授权 | 体 `RdmsRoleDto` | — | WORKER:CREATE |
| POST | `/system/rdms/role/user/get-customer-roles/{customerId}` | 加载机构下的用户角色 | — | `List<RdmsUserRole>` | WORKER:VIEW |
| POST | `/system/rdms/role/user/get-user-authed-roles` | 加载已关联的资源 | 体 `RdmsRoleDto` | `List<String>` | WORKER:VIEW |
| POST | `/system/rdms/role/user/save-user` | 保存用户 | 体 `RdmsRoleDto` | — | WORKER:CREATE |
| GET | `/system/rdms/role/user/list-user/{roleId}` | 加载用户 | — | — | WORKER:VIEW |

## SMS短信 RdmsSmsController

源码：`system/src/main/java/com/course/system/controller/rdms/RdmsSmsController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/system/rdms/sms/send` | 发送短信验证码 | 体 `RdmsSmsDto` | — | 公开 |

## 用户 RdmsUserController

源码：`system/src/main/java/com/course/system/controller/rdms/RdmsUserController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/system/rdms/user/save` | 保存用户 | 体 `RdmsUserDto` | — | USER:CREATE |
| POST | `/system/rdms/user/modify` | 用户信息修改 | 体 `RdmsUserDto` | — | USER:UPDATE |
| POST | `/system/rdms/user/login-search` | 登录搜索 | 体 `RdmsUserDto` | — | USER:UPDATE |
| POST | `/system/rdms/user/name-search` | 名称搜索 | 体 `RdmsUserDto` | — | USER:UPDATE |
| POST | `/system/rdms/user/list` | 查询用户列表 | 体 `PageDto` | — | USER:VIEW |
| POST | `/system/rdms/user/searchList` | 搜索用户列表 | 体 `PageDto<RdmsUserDto>` | `PageDto<RdmsUserDto>` | USER:VIEW |
| POST | `/system/rdms/user/searchUserByLoginName/{loginName}` | 按登录名搜索用户 | — | — | USER:VIEW |
| DELETE | `/system/rdms/user/del/{id}` | 删除用户 | — | — | USER:DELETE |
| POST | `/system/rdms/user/save-password` | 管理员设置成员密码。password 为 md5(明文 + 盐)；该成员已签发的会话随即失效，登录锁定随即解除 | 体 `RdmsUserDto` | — | USER:CREATE；另需 USER:UPDATE / USER:* / PLATFORM:* |
| GET | `/system/rdms/user/is-mobile-exist/{loginName}` | 校验登录名是否存在 | — | — | USER:VIEW |
| POST | `/system/rdms/user/reset-password` | 短信验证后重置密码。体为 loginName、password（md5(明文 + 盐)）、smsCode | 体 `RdmsUserDto` | — | 公开 |
| GET | `/system/rdms/user/login-salt/{loginName}` | 登录前按手机号读取密码盐，前端用它计算 md5(明文 + 盐)。未登记的手机号返回固定伪盐 | — | — | 公开 |
| GET | `/system/rdms/user/list-customer/{loginName}` | 登录前列出该手机号所属的在业且未删除的机构，只返回机构标识与名称 | — | `List<Map<String, String>>` | 公开 |

## 机构权限策略 TenantUiPolicyController

源码：`system/src/main/java/com/course/system/controller/TenantUiPolicyController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| GET | `/system/tenant-ui-policy` | 查询机构权限策略 | — | `Map<String, Object>` | WORKER:VIEW |
| GET | `/system/tenant-ui-policy/diff` | 平台模板相对本机构保存时基准的差异 | — | `Map<String, Object>` | WORKER:VIEW |
| PUT | `/system/tenant-ui-policy` | 保存机构权限策略 | 体 `Map<String, Object>` | `Void` | WORKER:UPDATE |
| POST | `/system/tenant-ui-policy/reset` | 重置机构权限策略 | — | `Void` | WORKER:UPDATE |

## 视频点播 VodController

源码：`system/src/main/java/com/course/system/controller/rdms/VodController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| GET | `/system/vod/getVideoPlayAuth/{videoId}` | 查询视频播放授权 | — | — | 公开 |
