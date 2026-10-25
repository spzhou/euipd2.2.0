# file2 服务接口

文件记录、对象存储上传与签名下载、富文本资源、Word 导入与在线查看（WOPI）。

- 上下文路径：`/file`，缺省端口 18003；浏览器经网关访问，路径不变。
- 接口数：55。公共约定（请求头、响应结构、鉴权列含义）见 [README.md](README.md)。
- 本文件由 `scripts/generate-api-doc.py` 生成，修改 Controller 后重新生成，不要手工编辑。

## 目录

- [文件上传](#文件上传-voduploadcontroller)（1）
- [视频点播](#视频点播-vodcontroller)（5）
- [文件管理](#文件管理-rdmsfilecontroller)（34）
- [文件上传下载](#文件上传下载-rdmsosscontroller)（3）
- [参考文献管理](#参考文献管理-rdmsfilereferencecontroller)（3）
- [富文本图片](#富文本图片-rdmsrichassetcontroller)（2）
- [Word 导入](#word-导入-rdmsrichdocimportcontroller)（1）
- [富文本渲染与在线查看](#富文本渲染与在线查看-rdmsrichrendercontroller)（4）
- [WOPI（Collabora 回调）](#wopicollabora-回调-wopicontroller)（2）

## 文件上传 VodUploadController

源码：`file2/src/main/java/com/course/file2/controller/VodUploadController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| GET | `/file/admin/check/{key}` | 检查文件上传 | — | — | FILE:VIEW |

## 视频点播 VodController

源码：`file2/src/main/java/com/course/file2/controller/VodController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| GET | `/file/admin/get-auth/{vod}` | 查询授权 | — | — | FILE:VIEW |
| POST | `/file/admin/file-save` | 保存文件 | 体 `RdmsVodFileDto` | — | FILE:UPDATE |
| GET | `/file/admin/file-info/{key}` | 查询文件 | — | — | FILE:VIEW |
| POST | `/file/admin/get-address-auth` | 查询上传地址和上传授权 | 体 `RdmsVodFileDto` | — | FILE:VIEW |
| POST | `/file/admin/refresh-address-auth` | 刷新上传地址和上传授权 | 体 `RdmsVodFileDto` | — | FILE:UPDATE |

## 文件管理 RdmsFileController

源码：`file2/src/main/java/com/course/file2/controller/RdmsFileController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/file/rdms/list` | 分页查询文件列表 | 体 `PageDto` | — | FILE:VIEW |
| POST | `/file/rdms/listApplyFiles` | 分页查询申请文件列表 | 体 `PageDto` | — | FILE:VIEW |
| POST | `/file/rdms/list-download` | 分页查询文件下载列表 | 体 `PageDto` | — | FILE:VIEW |
| POST | `/file/rdms/applyFile` | 申请文件：提交文件申请，设置审批人并保存申请信息 | 体 `RdmsFileApplyDto` | — | FILE:UPDATE |
| POST | `/file/rdms/applyFileList` | 批量申请文件 | 体 `RdmsFileApplyDto` | — | FILE:UPDATE |
| POST | `/file/rdms/applyClose` | 关闭文件申请 | 体 `RdmsFileApplyDto` | — | FILE:UPDATE |
| POST | `/file/rdms/searchByKeyWord` | 根据关键词搜索文件 | 体 `PageDto<RdmsFileDto>` | `PageDto<RdmsFileDto>` | FILE:VIEW |
| POST | `/file/rdms/searchDownloadFileByKeyWord` | 按关键字搜索下载文件 | 体 `PageDto<RdmsFileDownloadDto>` | `PageDto<RdmsFileDownloadDto>` | FILE:VIEW |
| POST | `/file/rdms/searchMoreFileByKeyWord` | 检索不是自己的文件 | 体 `PageDto<RdmsFileDto>` | `PageDto<RdmsFileDto>` | FILE:VIEW |
| POST | `/file/rdms/getApplyNum` | 查询申请数量 | 体 `RdmsHmiIdsDto` | `Long` | FILE:VIEW |
| GET | `/file/rdms/versions/{docKey}` | 同一文档的各版本，按版本号倒序 | — | `List<RdmsFileDto>` | FILE:VIEW |
| POST | `/file/rdms/save` | 保存上传文件的记录。带 previousFileId 时作为该文件的新版本保存（版本号加一，原版本保留） | 体 `RdmsFileDto` | `RdmsFileDto` | FILE:CREATE |
| POST | `/file/rdms/list-file-info` | 根据RdmsAttachmentDto对文件进行查询 | 体 `RdmsAttachmentDto` | `List<RdmsFileDto>` | FILE:VIEW |
| POST | `/file/rdms/getFileAbsPath` | 查询文件绝对路径 | 体 `RdmsFileDto` | `RdmsFileDto` | FILE:VIEW |
| POST | `/file/rdms/getFileListByCharacterId` | 按ID列表查询文件列表 | 参 `characterId` | `List<RdmsFileDto>` | FILE:VIEW |
| POST | `/file/rdms/getMyArchiveTreeByWorkerId/{workerId}` | 按成员ID查询我的归档树 | — | `List<RdmsHmiTree<RdmsFileDto>>` | FILE:VIEW |
| POST | `/file/rdms/getReferenceArchiveTreeByCharacter` | 按特性ID查询参考文献归档树 | 参 `characterId` | `List<RdmsHmiTree<RdmsFileDto>>` | FILE:VIEW |
| POST | `/file/rdms/getReferenceArchiveTreeBySubproject` | 按子项目ID查询参考文献归档树 | 参 `subprojectId` | `List<RdmsHmiTree<RdmsFileDto>>` | FILE:VIEW |
| POST | `/file/rdms/getCbbArchiveTree/{cbbId}` | 查询CBB归档树 | — | `List<RdmsHmiTree<RdmsFileDto>>` | FILE:VIEW |
| POST | `/file/rdms/getArchiveTreeBySubprojectId` | 按子项目ID查询归档树 | 参 `subprojectId` | `List<RdmsHmiTree<RdmsFileDto>>` | FILE:VIEW |
| POST | `/file/rdms/getArchiveTreeByPreProjectId` | 按预立项ID查询归档树 | 参 `preprojectId` | `List<RdmsHmiTree<RdmsFileDto>>` | FILE:VIEW |
| POST | `/file/rdms/getTestArchiveTreeByProjectId` | 按项目ID查询测试归档树 | 参 `projectId` | `List<RdmsHmiTree<RdmsFileDto>>` | FILE:VIEW |
| POST | `/file/rdms/getFileListByDemandId` | 按需求ID查询文件列表 | 参 `demandId` | `List<RdmsFileDto>` | FILE:VIEW |
| POST | `/file/rdms/getFileSimpleInfo/{fileId}` | 查询文件简要信息 | — | `RdmsFileDto` | FILE:VIEW |
| POST | `/file/rdms/getAllProcessFileListByJobitemId/{jobItemId}` | 按工单ID查询文件列表 | — | `List<RdmsFileDto>` | FILE:VIEW |
| POST | `/file/rdms/getProjectRecordFileList/{projectId}` | 查询项目记录文件列表 | — | `List<RdmsFileDto>` | FILE:VIEW |
| POST | `/file/rdms/getApproverInfo/{fileId}` | 查询审批人信息 | — | `RdmsCustomerUserDto` | FILE:VIEW |
| POST | `/file/rdms/getFileListByUseAndItemId` | 按使用和条目ID查询文件列表 | 体 `RdmsFileQueryDto` | `List<RdmsFileDto>` | FILE:VIEW |
| POST | `/file/rdms/getFileListByJobitemId` | 按工单ID查询文件列表 | 参 `jobitemId` | `List<RdmsFileDto>` | FILE:VIEW |
| POST | `/file/rdms/getApplyList/{approverId}` | 查询申请列表 | — | `List<RdmsFileApplyDto>` | FILE:VIEW |
| POST | `/file/rdms/getApplyInfo/{applyId}` | 查询申请信息 | — | `RdmsFileApplyDto` | FILE:VIEW |
| POST | `/file/rdms/applyFile-agree/{applyId}` | 申请批准 | — | `String` | FILE:UPDATE |
| POST | `/file/rdms/applyFile-reject/{applyId}` | 申请驳回 | — | `String` | FILE:UPDATE |
| DELETE | `/file/rdms/del/{id}` | 删除文件 | — | — | FILE:DELETE |

## 文件上传下载 RdmsOssController

源码：`file2/src/main/java/com/course/file2/controller/RdmsOssController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/file/rdms/oss-simple-rdms` | 文件上传V2 | 参 `folder`；表单 `file` | `FileDto` | FILE:UPDATE |
| POST | `/file/rdms/oss-file-upload-web` | 对象存储文件上传页面 | 参 `folder`；表单 `file` | `FileDto` | FILE:UPDATE |
| POST | `/file/rdms/oss-get-signed-url` | 查询签名地址 | 参 `fileFullName`；参 `customer`；参 `user` | `String` | FILE:VIEW |

## 参考文献管理 RdmsFileReferenceController

源码：`file2/src/main/java/com/course/file2/controller/RdmsFileReferenceController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/file/rdms/reference/list` | 查询参考文献列表 | 体 `PageDto` | — | FILE:VIEW |
| POST | `/file/rdms/reference/save` | 已停用：文件的版本关系改由 rdms_file 的版本列表达，本表只读保留 | 体 `RdmsFileReferenceDto` | `RdmsFileReferenceDto` | FILE:CREATE |
| DELETE | `/file/rdms/reference/del/{id}` | 已停用 | — | — | FILE:DELETE |

## 富文本图片 RdmsRichAssetController

源码：`file2/src/main/java/com/course/file2/controller/RdmsRichAssetController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/file/rdms/rich-asset/upload` | 上传富文本图片 | 参 `file` | `Map<String, Object>` | FILE:UPDATE |
| POST | `/file/rdms/rich-asset/signed-url` | 请求体 {"assetIds": [...]}；逐个按当前机构校验，只返回本机构存在的图片， | 体 `Map<String, List<String>>` | `Map<String, Object>` | FILE:VIEW |

## Word 导入 RdmsRichDocImportController

源码：`file2/src/main/java/com/course/file2/rich/RdmsRichDocImportController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/file/rdms/rich-doc/word-parse` | Word解析 | 参 `file`；参 `bizType` | `Map<String, Object>` | FILE:UPDATE |

## 富文本渲染与在线查看 RdmsRichRenderController

源码：`file2/src/main/java/com/course/file2/rich/RdmsRichRenderController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/file/rdms/rich-render/get-content` | 查询内容 | 体 `RenderRequest` | `ResponseEntity<byte[]>` | FILE:VIEW |
| POST | `/file/rdms/rich-render/get-viewer` | 查询查看器 | 体 `RenderRequest` | `Map<String, Object>` | FILE:VIEW |
| POST | `/file/rdms/rich-render/signed-url` | 签名地址 | 体 `RenderRequest` | `Map<String, Object>` | FILE:VIEW |
| POST | `/file/rdms/rich-render/save-as-file` | 本人编辑保存的版本导出为邮件附件：生成独立的文件记录，返回文件编号与快照信息 | 体 `RenderRequest` | `Map<String, Object>` | FILE:CREATE |

## WOPI（Collabora 回调） WopiController

源码：`file2/src/main/java/com/course/file2/rich/WopiController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| GET | `/file/wopi/files/{fileId}` | 检查文件信息 | 参 `access_token` | `ResponseEntity<?>` | 公开 |
| GET | `/file/wopi/files/{fileId}/contents` | 查询文件 | 参 `access_token` | `ResponseEntity<?>` | 公开 |
