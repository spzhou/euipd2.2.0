# customer 服务接口

研发业务：需求、特性、产品、项目与子项目、工单、评审、里程碑、预算与费用、质量与缺陷、物料仓库、工时绩效、流程与邮件等。

- 上下文路径：`/customer`，缺省端口 18005；浏览器经网关访问，路径不变。
- 接口数：851。公共约定（请求头、响应结构、鉴权列含义）见 [README.md](README.md)。
- 本文件由 `scripts/generate-api-doc.py` 生成，修改 Controller 后重新生成，不要手工编辑。

## 目录

- [GitLab集成](#gitlab集成-gitlabcontroller)（2）
- [最终管理人](#最终管理人-bosscontroller)（2）
- [直接立项预算执行管理](#直接立项预算执行管理-budgetresearchexecontroller)（17）
- [直接立项预算管理](#直接立项预算管理-budgetresearchcontroller)（7）
- [预算管理](#预算管理-budgetcontroller)（11）
- [预算处理](#预算处理-budgetsheetcontroller)（7）
- [缺陷管理](#缺陷管理-bugfeedbackcontroller)（26）
- [业务富文本文档](#业务富文本文档-bizrichdoccontroller)（36）
- [工时/工时费计算](#工时工时费计算-calmanhourcontroller)（2）
- [文件分类管理](#文件分类管理-categoryfilecontroller)（5）
- [项目分类管理](#项目分类管理-categoryprojectcontroller)（7）
- [分类管理](#分类管理-categorycontroller)（6）
- [CBB 共用基础模块](#cbb-共用基础模块-cbbcontroller)（19）
- [CBB 富文本文档](#cbb-富文本文档-cbbrichdoccontroller)（4）
- [篇章](#篇章-chaptercontroller)（4）
- [特性处理](#特性处理-characterprocesscontroller)（5）
- [特性定义](#特性定义-charactercontroller)（87）
- [特性富文本文档](#特性富文本文档-characterrichdoccontroller)（19）
- [分类](#分类-coursecategorycontroller)（6）
- [课程](#课程-coursecontroller)（8）
- [讲师](#讲师-teachercontroller)（6）
- [需求管理](#需求管理-demandcontroller)（10）
- [部门](#部门-departmentcontroller)（10）
- [费用管理](#费用管理-feecontroller)（22）
- [文件申请流程](#文件申请流程-fileapplyflowcontroller)（4）
- [流程定义与实例](#流程定义与实例-flowcontroller)（11）
- [功能树](#功能树-functiontreecontroller)（4）
- [Gantt图](#gantt图-ganttcontroller)（11）
- [IPMT](#ipmt-ipmtcontroller)（4）
- [工单处理](#工单处理-jobitemprocesscontroller)（5）
- [工单](#工单-jobitemcontroller)（131）
- [职级管理](#职级管理-joblevelcontroller)（4）
- [工单资产](#工单资产-jobitempropertycontroller)（7）
- [内部邮件](#内部邮件-mailcontroller)（14）
- [工时申报](#工时申报-manhourdeclarecontroller)（2）
- [工时](#工时-manhourcontroller)（6）
- [物料库存](#物料库存-materialstockcontroller)（5）
- [物料仓库](#物料仓库-materialwarehousecontroller)（3）
- [物料管理](#物料管理-materialcontroller)（71）
- [里程碑](#里程碑-milestonecontroller)（6）
- [通知角标](#通知角标-notifycontroller)（1）
- [产品总监与系统工程总监](#产品总监与系统工程总监-directorcontroller)（6）
- [绩效管理](#绩效管理-performancecontroller)（7）
- [预立项管理](#预立项管理-preprojectcontroller)（16）
- [产品经理](#产品经理-productmanagercontroller)（5）
- [产品管理](#产品管理-productcontroller)（7）
- [项目经理](#项目经理-projectmanagercontroller)（5）
- [保密等级](#保密等级-projectseclevelcontroller)（3）
- [项目任务](#项目任务-projecttaskcontroller)（3）
- [项目管理](#项目管理-projectcontroller)（61）
- [项目基线](#项目基线-baselinecontroller)（2）
- [项目富文本文档](#项目富文本文档-projectrichdoccontroller)（1）
- [质量管理](#质量管理-qualitycontroller)（24）
- [小节](#小节-sectioncontroller)（3）
- [统计](#统计-statisticscontroller)（10）
- [子项目](#子项目-subprojectcontroller)（42）
- [汇总数据](#汇总数据-summarycontroller)（1）
- [系统经理](#系统经理-systemmanagercontroller)（6）
- [系统设置](#系统设置-systemcontroller)（3）
- [下属员工](#下属员工-underlinecontroller)（1）
- [公开页面数据](#公开页面数据-webcontroller)（3）
- [工作日历](#工作日历-workcalendarcontroller)（2）
- [员工](#员工-workercontroller)（23）

## GitLab集成 GitLabController

源码：`customer/src/main/java/com/course/customer/controller/GitLabController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/customer/api/gitlab/projects` | 新建项目 | 参 `name` | `ResponseEntity<?>` | GITLAB:UPDATE |
| GET | `/customer/api/gitlab/projects` | 查询项目 | — | `ResponseEntity<?>` | GITLAB:VIEW |

## 最终管理人 BossController

源码：`customer/src/main/java/com/course/customer/controller/BossController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/customer/boss/list` | 分页查询所有最终管理人信息 | 体 `PageDto<RdmsBossDto>` | `PageDto<RdmsBossDto>` | BOSS:VIEW |
| POST | `/customer/boss/getBossByCustomerId/{customerId}` | 根据客户ID查询对应的最终管理人信息 | — | `RdmsBossDto` | BOSS:VIEW |

## 直接立项预算执行管理 BudgetResearchExeController

源码：`customer/src/main/java/com/course/customer/controller/BudgetResearchExeController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/customer/budget-direct-exe/listByKeywordIsSubprojectId` | 按关键字是否子项目ID查询直接立项预算执行列表 | 体 `PageDto<RdmsBudgetResearchExeDto>` | `PageDto<RdmsBudgetResearchExeDto>` | FEE:VIEW |
| POST | `/customer/budget-direct-exe/listByKeywordIsProjectId` | 按关键字是否项目ID查询直接立项预算执行列表 | 体 `PageDto<RdmsBudgetResearchExeDto>` | `PageDto<RdmsBudgetResearchExeDto>` | FEE:VIEW |
| POST | `/customer/budget-direct-exe/saveBudgetExeList` | 保存预算执行列表 | 体 `List<RdmsBudgetResearchExeDto>` | `List<String>` | FEE:CREATE |
| POST | `/customer/budget-direct-exe/updatePaymentStatus/{id}` | 只改手工录入流水的支付状态，且只允许应付未付转已支付；系统流水由各自的审批、评价流程转已支付 | — | `String` | FEE:UPDATE |
| POST | `/customer/budget-direct-exe/getSumBudgetBySubprojectId/{subprojectId}` | 按子项目ID查询汇总预算 | — | `BigDecimal` | FEE:VIEW |
| POST | `/customer/budget-direct-exe/getBudgetExecutionSummary/{subprojectId}` | 查询预算执行汇总 | — | `RdmsHmiBudgeStatisticsDto` | FEE:VIEW |
| POST | `/customer/budget-direct-exe/getBudgetExecutionSummary_preproject/{preprojectId}` | 查询预算执行汇总（预立项） | — | `RdmsHmiBudgeStatisticsDto` | FEE:VIEW |
| POST | `/customer/budget-direct-exe/getBudgetExecutionSummary_mainProject/{projectId}` | 查询预算执行汇总（主项目） | — | `RdmsHmiBudgeStatisticsDto` | FEE:VIEW |
| POST | `/customer/budget-direct-exe/getBudgetExecutionSummary_product/{projectId}` | 查询预算执行汇总（产品） | — | `RdmsHmiBudgeStatisticsDto` | FEE:VIEW |
| POST | `/customer/budget-direct-exe/getMainProjectBudgetExecutionSummaryList/{customerId}` | 查询主项目预算执行汇总列表 | — | `List<RdmsHmiBudgeStatisticsDto>` | FEE:VIEW |
| POST | `/customer/budget-direct-exe/getBudgetStageExecutionSummary_project/{customerId}` | 查询预算阶段执行汇总（项目） | — | `RdmsHmiBudgeStatisticsDto` | FEE:VIEW |
| POST | `/customer/budget-direct-exe/getBudgetStageExecutionSummary_product/{customerId}` | 查询预算阶段执行汇总（产品） | — | `RdmsHmiBudgeStatisticsDto` | FEE:VIEW |
| POST | `/customer/budget-direct-exe/getBudgetStageExecutionSummary_preProject/{customerId}` | 查询预算阶段执行汇总（预立项） | — | `RdmsHmiBudgeStatisticsDto` | FEE:VIEW |
| POST | `/customer/budget-direct-exe/getBudgetExecutionSummary_customer/{customerId}` | 查询预算执行汇总（机构） | — | `RdmsHmiBudgeStatisticsDto` | FEE:VIEW |
| POST | `/customer/budget-direct-exe/getMainProjectBudgetExecutionSummary/{projectId}` | 查询主项目预算执行汇总 | — | `RdmsHmiBudgeStatisticsDto` | FEE:VIEW |
| POST | `/customer/budget-direct-exe/getUnderlineProjectBudgetExecutionSummaryList` | 范围内已立项主项目的预算执行汇总；人员与机构取自登录身份 | 参 `customerUserId` | `List<RdmsHmiBudgeStatisticsDto>` | FEE:VIEW |
| DELETE | `/customer/budget-direct-exe/del/{id}` | 删除直接立项预算执行 | — | — | FEE:DELETE |

## 直接立项预算管理 BudgetResearchController

源码：`customer/src/main/java/com/course/customer/controller/BudgetResearchController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/customer/budget-direct/list` | 查询直接立项预算列表 | 体 `PageDto<RdmsBudgetResearchDto>` | `PageDto<RdmsBudgetResearchDto>` | PROJECT:VIEW |
| POST | `/customer/budget-direct/saveBudget` | 保存预算 | 体 `RdmsBudgetResearchDto` | `String` | PROJECT:CREATE |
| POST | `/customer/budget-direct/saveBudget_subproject` | 保存预算（子项目） | 体 `RdmsBudgetResearchDto` | `String` | PROJECT:CREATE |
| POST | `/customer/budget-direct/getBudget` | 查询预算 | 参 `projectId` | `RdmsBudgetResearchDto` | PROJECT:VIEW |
| POST | `/customer/budget-direct/getBudget_subproject` | 查询预算（子项目） | 体 `CustomerIdAndProjectId` | `RdmsBudgetResearchDto` | PROJECT:VIEW |
| POST | `/customer/budget-direct/getDirectSumBudget` | 查询直接汇总预算 | 体 `CustomerIdAndProjectId` | `Double` | PROJECT:VIEW |
| DELETE | `/customer/budget-direct/del/{id}` | 删除直接立项预算 | — | — | PROJECT:DELETE |

## 预算管理 BudgetController

源码：`customer/src/main/java/com/course/customer/controller/BudgetController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/customer/budget-mng/list` | 查询预算列表 | 体 `PageDto<RdmsBudgetDto>` | `PageDto<RdmsBudgetDto>` | BUDGET:VIEW |
| POST | `/customer/budget-mng/getBudgetManageJobNumByUserId/{userId}` | 按用户ID查询预算管理工作数量 | — | `Long` | BUDGET:VIEW |
| POST | `/customer/budget-mng/getProjectAndApplicationBudgetItems/{budgetId}` | 查询项目和申请预算条目 | — | `RdmsBudgetDto` | BUDGET:VIEW |
| POST | `/customer/budget-mng/getProjectAndApplicationCompleteBudgetItems/{budgetId}` | 查询项目和申请已完成预算条目 | — | `RdmsBudgetDto` | BUDGET:VIEW |
| POST | `/customer/budget-mng/saveBudgetApplication` | 保存预算申请 | 体 `RdmsBudgetAdjustDto` | `String` | BUDGET:CREATE |
| POST | `/customer/budget-mng/saveBudgetDistribution` | 保存预算分配 | 体 `RdmsBudgetAdjustDto` | `String` | BUDGET:CREATE |
| POST | `/customer/budget-mng/getExamineNum/{projectId}` | 查询审核数量 | — | `Integer` | BUDGET:VIEW |
| POST | `/customer/budget-mng/getBudgetSimpleInfo/{budgetId}` | 查询预算简要信息 | — | `RdmsBudgetDto` | BUDGET:VIEW |
| POST | `/customer/budget-mng/getBudgetAdjustSimpleInfo/{budgetAdjustId}` | 查询预算调整简要信息 | — | `RdmsBudgetAdjustDto` | BUDGET:VIEW |
| POST | `/customer/budget-mng/approvalPass` | 审批通过 | 体 `RdmsBudgetAdjustDto` | `String` | BUDGET:UPDATE |
| POST | `/customer/budget-mng/approvalReject` | 审批驳回 | 体 `RdmsBudgetAdjustDto` | `String` | BUDGET:UPDATE |

## 预算处理 BudgetSheetController

源码：`customer/src/main/java/com/course/customer/controller/BudgetSheetController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/customer/budget/list` | 查询预算列表 | 体 `PageDto<RdmsProjectBudgetDto>` | `PageDto<RdmsProjectBudgetDto>` | BUDGET:VIEW |
| POST | `/customer/budget/getCharacterBudget_decompose/{parentCharacterId}` | 对于分解记录而言, 这里的characterID是当前分解记录的 parentCharacterID | — | `RdmsCharacterBudgetDto` | BUDGET:VIEW |
| POST | `/customer/budget/getCharacterBudget_iteration/{characterId}` | 对于分迭代记录录而言, 这里的characterID是当前记录的characterID | — | `RdmsCharacterBudgetDto` | BUDGET:VIEW |
| POST | `/customer/budget/getCharacterBudgetWhenEdit/{characterId}` | 当编辑某一条被分解的character时处理budget问题 | — | `RdmsCharacterBudgetDto` | BUDGET:VIEW |
| POST | `/customer/budget/getProjectBudgetPoolBySubprojectId/{subprojectId}` | 按子项目ID查询项目预算池 | — | `BigDecimal` | BUDGET:VIEW |
| POST | `/customer/budget/save` | 保存预算 | 体 `RdmsProjectBudgetDto` | `RdmsProjectBudgetDto` | BUDGET:CREATE |
| DELETE | `/customer/budget/del/{id}` | 删除预算 | — | — | BUDGET:DELETE |

## 缺陷管理 BugFeedbackController

源码：`customer/src/main/java/com/course/customer/controller/BugFeedbackController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/customer/bugfeedback/getBugFeedbackNumByUserId/{userId}` | 按用户ID查询缺陷数量 | — | `Long` | BUG_FEEDBACK:VIEW |
| POST | `/customer/bugfeedback/getBugRecheckNumByUserId/{userId}` | 按用户ID查询缺陷复核数量 | — | `Long` | BUG_FEEDBACK:VIEW |
| POST | `/customer/bugfeedback/getBugDefineSubmitNumByPdm/{userId}` | 按产品经理查询缺陷定义提交数量 | — | `Long` | BUG_FEEDBACK:VIEW |
| POST | `/customer/bugfeedback/getBugAndSubmitJobitems/{bugfeedbackId}` | 查询缺陷和提交工单 | — | `RdmsBugFeedbackDto` | BUG_FEEDBACK:VIEW |
| POST | `/customer/bugfeedback/getBugCostListByProjectId/{projectId}` | 按项目ID查询缺陷成本列表 | — | `List<RdmsBugFeedbackDto>` | BUG_FEEDBACK:VIEW |
| POST | `/customer/bugfeedback/getBugHasListFlag/{userId}` | 查询缺陷有列表标志 | — | `Boolean` | BUG_FEEDBACK:VIEW |
| POST | `/customer/bugfeedback/getBugAndAnalysisJobList/{qualityId}` | 查询特性和工单列表 | — | `RdmsBugFeedbackDto` | BUG_FEEDBACK:VIEW |
| POST | `/customer/bugfeedback/list` | 查询缺陷列表 | 体 `PageDto<RdmsBugFeedbackDto>` | `PageDto<RdmsBugFeedbackDto>` | BUG_FEEDBACK:VIEW |
| POST | `/customer/bugfeedback/listByProjectId` | 按项目ID查询缺陷列表 | 体 `PageDto<RdmsBugFeedbackDto>` | `PageDto<RdmsBugFeedbackDto>` | BUG_FEEDBACK:VIEW |
| POST | `/customer/bugfeedback/listByProjectId_handling` | 按项目ID查询缺陷列表（处理中） | 体 `PageDto<RdmsBugFeedbackDto>` | `PageDto<RdmsBugFeedbackDto>` | BUG_FEEDBACK:VIEW |
| POST | `/customer/bugfeedback/getBugAndJobitemList/{bugId}` | 查询缺陷和工单列表 | — | `RdmsBugFeedbackDto` | BUG_FEEDBACK:VIEW |
| POST | `/customer/bugfeedback/listBugByPjm` | 按项目经理查询缺陷列表 | 体 `PageDto<RdmsBugFeedbackDto>` | `PageDto<RdmsBugFeedbackDto>` | BUG_FEEDBACK:VIEW |
| POST | `/customer/bugfeedback/listByTestManager` | 按测试经理查询缺陷列表 | 体 `PageDto<RdmsBugFeedbackDto>` | `PageDto<RdmsBugFeedbackDto>` | BUG_FEEDBACK:VIEW |
| POST | `/customer/bugfeedback/getBuaArchiveListBySubprojectId` | 按子项目ID查询BUA归档列表 | 体 `PageDto<RdmsBugFeedbackDto>` | `PageDto<RdmsBugFeedbackDto>` | BUG_FEEDBACK:VIEW |
| POST | `/customer/bugfeedback/listBugByCharacterId` | 按特性ID查询缺陷列表 | 参 `characterId` | `List<RdmsBugFeedbackDto>` | BUG_FEEDBACK:VIEW |
| POST | `/customer/bugfeedback/listByManager` | 按经理查询缺陷列表 | 体 `PageDto<RdmsBugFeedbackDto>` | `PageDto<RdmsBugFeedbackDto>` | BUG_FEEDBACK:VIEW |
| POST | `/customer/bugfeedback/getCharacterListIncludeNextLevelBySubprojectId/{subprojectId}` | 按子项目ID查询特性列表（含下一级） | — | `List<RdmsCharacterDto>` | BUG_FEEDBACK:VIEW |
| POST | `/customer/bugfeedback/getBugDetailInfo` | 查询缺陷详情信息 | 参 `bugfeedbackId`；参 `loginUserId` | `RdmsBugFeedbackDto` | BUG_FEEDBACK:VIEW |
| POST | `/customer/bugfeedback/getProjectTreeListByCustomerId/{customerId}` | 按机构ID查询项目树列表 | — | `List<RdmsHmiTree<RdmsHmiTreeInfoDto>>` | BUG_FEEDBACK:VIEW |
| POST | `/customer/bugfeedback/save` | 保存缺陷 | 体 `RdmsBugFeedbackDto` | `RdmsBugFeedbackDto` | BUG_FEEDBACK:CREATE |
| POST | `/customer/bugfeedback/saveBugFeedbackInfo` | 保存缺陷信息 | 体 `RdmsBugFeedbackDto` | `String` | BUG_FEEDBACK:CREATE |
| POST | `/customer/bugfeedback/submitBugFeedbackInfo` | 提交缺陷信息 | 体 `RdmsBugFeedbackDto` | `String` | BUG_FEEDBACK:UPDATE |
| POST | `/customer/bugfeedback/submit` | 提交缺陷 | 体 `RdmsBugFeedbackDto` | `RdmsBugFeedbackDto` | BUG_FEEDBACK:UPDATE |
| POST | `/customer/bugfeedback/saveAnalysis` | 保存分析 | 体 `RdmsBugFeedbackDto` | `RdmsBugFeedbackDto` | BUG_FEEDBACK:CREATE |
| POST | `/customer/bugfeedback/accept/{bugfeedbackId}` | 接受缺陷 | — | `String` | BUG_FEEDBACK:UPDATE |
| POST | `/customer/bugfeedback/reject/{bugfeedbackId}` | 驳回缺陷 | — | `String` | BUG_FEEDBACK:UPDATE |

## 业务富文本文档 BizRichDocController

源码：`customer/src/main/java/com/course/customer/controller/BizRichDocController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/customer/demand/rich-doc/get` | 查询业务富文本文档 | 体 `RichDocRequest` | `RichDocViewDto` | DEMAND:VIEW |
| POST | `/customer/quality/rich-doc/get` | 查询业务富文本文档 | 体 `RichDocRequest` | `RichDocViewDto` | QUALITY:VIEW |
| POST | `/customer/bugfeedback/rich-doc/get` | 查询业务富文本文档 | 体 `RichDocRequest` | `RichDocViewDto` | BUG_FEEDBACK:VIEW |
| POST | `/customer/jobitem-property/rich-doc/get` | 查询业务富文本文档 | 体 `RichDocRequest` | `RichDocViewDto` | JOB_ITEM:VIEW |
| POST | `/customer/demand/rich-doc/save` | 保存业务富文本文档 | 体 `RichDocRequest` | `Map<String, Object>` | DEMAND:CREATE |
| POST | `/customer/quality/rich-doc/save` | 保存业务富文本文档 | 体 `RichDocRequest` | `Map<String, Object>` | QUALITY:CREATE |
| POST | `/customer/bugfeedback/rich-doc/save` | 保存业务富文本文档 | 体 `RichDocRequest` | `Map<String, Object>` | BUG_FEEDBACK:CREATE |
| POST | `/customer/jobitem-property/rich-doc/save` | 保存业务富文本文档 | 体 `RichDocRequest` | `Map<String, Object>` | JOB_ITEM:CREATE |
| POST | `/customer/demand/rich-doc/get-versions` | 查询版本 | 体 `RichDocRequest` | `List<Map<String, Object>>` | DEMAND:VIEW |
| POST | `/customer/quality/rich-doc/get-versions` | 查询版本 | 体 `RichDocRequest` | `List<Map<String, Object>>` | QUALITY:VIEW |
| POST | `/customer/bugfeedback/rich-doc/get-versions` | 查询版本 | 体 `RichDocRequest` | `List<Map<String, Object>>` | BUG_FEEDBACK:VIEW |
| POST | `/customer/jobitem-property/rich-doc/get-versions` | 查询版本 | 体 `RichDocRequest` | `List<Map<String, Object>>` | JOB_ITEM:VIEW |
| POST | `/customer/demand/rich-doc/get-compare` | 查询对比 | 体 `RichDocRequest` | `List<Map<String, Object>>` | DEMAND:VIEW |
| POST | `/customer/quality/rich-doc/get-compare` | 查询对比 | 体 `RichDocRequest` | `List<Map<String, Object>>` | QUALITY:VIEW |
| POST | `/customer/bugfeedback/rich-doc/get-compare` | 查询对比 | 体 `RichDocRequest` | `List<Map<String, Object>>` | BUG_FEEDBACK:VIEW |
| POST | `/customer/jobitem-property/rich-doc/get-compare` | 查询对比 | 体 `RichDocRequest` | `List<Map<String, Object>>` | JOB_ITEM:VIEW |
| POST | `/customer/demand/rich-doc/restore` | 恢复业务富文本文档 | 体 `RichDocRequest` | `Map<String, Object>` | DEMAND:UPDATE |
| POST | `/customer/quality/rich-doc/restore` | 恢复业务富文本文档 | 体 `RichDocRequest` | `Map<String, Object>` | QUALITY:UPDATE |
| POST | `/customer/bugfeedback/rich-doc/restore` | 恢复业务富文本文档 | 体 `RichDocRequest` | `Map<String, Object>` | BUG_FEEDBACK:UPDATE |
| POST | `/customer/jobitem-property/rich-doc/restore` | 恢复业务富文本文档 | 体 `RichDocRequest` | `Map<String, Object>` | JOB_ITEM:UPDATE |
| POST | `/customer/demand/rich-doc/edit-lock` | 编辑锁定 | 体 `RichDocRequest` | `Map<String, Object>` | DEMAND:UPDATE |
| POST | `/customer/quality/rich-doc/edit-lock` | 编辑锁定 | 体 `RichDocRequest` | `Map<String, Object>` | QUALITY:UPDATE |
| POST | `/customer/bugfeedback/rich-doc/edit-lock` | 编辑锁定 | 体 `RichDocRequest` | `Map<String, Object>` | BUG_FEEDBACK:UPDATE |
| POST | `/customer/jobitem-property/rich-doc/edit-lock` | 编辑锁定 | 体 `RichDocRequest` | `Map<String, Object>` | JOB_ITEM:UPDATE |
| POST | `/customer/demand/rich-doc/draft/save` | 保存草稿 | 体 `RichDocRequest` | `Map<String, Object>` | DEMAND:CREATE |
| POST | `/customer/quality/rich-doc/draft/save` | 保存草稿 | 体 `RichDocRequest` | `Map<String, Object>` | QUALITY:CREATE |
| POST | `/customer/bugfeedback/rich-doc/draft/save` | 保存草稿 | 体 `RichDocRequest` | `Map<String, Object>` | BUG_FEEDBACK:CREATE |
| POST | `/customer/jobitem-property/rich-doc/draft/save` | 保存草稿 | 体 `RichDocRequest` | `Map<String, Object>` | JOB_ITEM:CREATE |
| POST | `/customer/demand/rich-doc/draft/get` | 查询草稿 | 体 `RichDocRequest` | `Map<String, Object>` | DEMAND:VIEW |
| POST | `/customer/quality/rich-doc/draft/get` | 查询草稿 | 体 `RichDocRequest` | `Map<String, Object>` | QUALITY:VIEW |
| POST | `/customer/bugfeedback/rich-doc/draft/get` | 查询草稿 | 体 `RichDocRequest` | `Map<String, Object>` | BUG_FEEDBACK:VIEW |
| POST | `/customer/jobitem-property/rich-doc/draft/get` | 查询草稿 | 体 `RichDocRequest` | `Map<String, Object>` | JOB_ITEM:VIEW |
| POST | `/customer/demand/rich-doc/get-lineage` | 查询版本谱系 | 体 `RichDocRequest` | `Map<String, Object>` | DEMAND:VIEW |
| POST | `/customer/quality/rich-doc/get-lineage` | 查询版本谱系 | 体 `RichDocRequest` | `Map<String, Object>` | QUALITY:VIEW |
| POST | `/customer/bugfeedback/rich-doc/get-lineage` | 查询版本谱系 | 体 `RichDocRequest` | `Map<String, Object>` | BUG_FEEDBACK:VIEW |
| POST | `/customer/jobitem-property/rich-doc/get-lineage` | 查询版本谱系 | 体 `RichDocRequest` | `Map<String, Object>` | JOB_ITEM:VIEW |

## 工时/工时费计算 CalManhourController

源码：`customer/src/main/java/com/course/customer/controller/calculate/CalManhourController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/customer/cal-manhour/getJobitemManhourInfo/{parentJobitemId}` | 获取工项工时信息 | — | `CalManhour` | MANHOUR:VIEW |
| POST | `/customer/cal-manhour/getRemainJobitemManhourInfo/{parentJobitemId}` | 查询剩余工单工时信息 | — | `Double` | MANHOUR:VIEW |

## 文件分类管理 CategoryFileController

源码：`customer/src/main/java/com/course/customer/controller/CategoryFileController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/customer/category-file/list` | 查询文件分类列表 | 体 `PageDto` | — | CATEGORY:VIEW |
| POST | `/customer/category-file/save` | 保存文件分类 | 体 `RdmsCategoryFileDto` | `RdmsCategoryFileDto` | CATEGORY:CREATE |
| POST | `/customer/category-file/getAllCategoryFilesByProjectId/{projectId}` | 按项目ID查询全部分类文件 | — | `List<RdmsCategoryFileDto>` | CATEGORY:VIEW |
| POST | `/customer/category-file/getCategoryFileSimpleInfo/{fileId}` | 查询分类文件简要信息 | — | `RdmsCategoryFileDto` | CATEGORY:VIEW |
| DELETE | `/customer/category-file/del/{id}` | 删除文件分类 | — | — | CATEGORY:DELETE |

## 项目分类管理 CategoryProjectController

源码：`customer/src/main/java/com/course/customer/controller/CategoryProjectController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/customer/category-project/getCategoryList_all/{customerId}` | 查询分类列表（全部） | — | `List<RdmsCategoryProjectDto>` | CATEGORY:VIEW |
| POST | `/customer/category-project/getCategoryList_all_show/{customerId}` | 查询分类列表（全部显示） | — | `List<RdmsCategoryProjectDto>` | CATEGORY:VIEW |
| POST | `/customer/category-project/getCategoryList/{projectId}` | 查询分类列表 | — | `List<String>` | CATEGORY:VIEW |
| POST | `/customer/category-project/updateCategoryItem` | 修改分类条目 | 体 `RdmsCategoryProjectDto` | `String` | CATEGORY:UPDATE |
| POST | `/customer/category-project/save` | 保存项目分类 | 体 `RdmsCategoryProjectDto` | `RdmsCategoryProjectDto` | CATEGORY:CREATE |
| POST | `/customer/category-project/saveCategoryList` | 保存分类列表 | 参 `projectId` | `String` | CATEGORY:CREATE |
| DELETE | `/customer/category-project/del/{id}` | 删除项目分类 | — | — | CATEGORY:DELETE |

## 分类管理 CategoryController

源码：`customer/src/main/java/com/course/customer/controller/CategoryController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/customer/category/getCategoryList_all_plus/{customerId}` | 查询分类列表（全部扩展） | — | `List<RdmsCategoryDto>` | CATEGORY:VIEW |
| POST | `/customer/category/getCategoryList_all/{customerId}` | 查询分类列表（全部） | — | `List<RdmsCategoryDto>` | CATEGORY:VIEW |
| POST | `/customer/category/getCategoryList_open` | 查询分类列表（开放） | 参 `customerId` | `List<RdmsCategoryDto>` | CATEGORY:VIEW |
| POST | `/customer/category/save` | 保存分类 | 体 `RdmsCategoryDto` | `RdmsCategoryDto` | CATEGORY:CREATE |
| POST | `/customer/category/updateCategoryItem` | 修改分类条目 | 体 `RdmsCategoryDto` | `RdmsCategoryDto` | CATEGORY:UPDATE |
| DELETE | `/customer/category/del/{id}` | 删除分类 | — | — | CATEGORY:DELETE |

## CBB 共用基础模块 CbbController

源码：`customer/src/main/java/com/course/customer/controller/CbbController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/customer/cbb/getRecordAndData` | 查询记录和数据 | 参 `cbbId` | `RdmsCharacterDto` | CBB:VIEW |
| POST | `/customer/cbb/list-module` | 查询模块列表 | 体 `PageDto<RdmsCbbDto>` | `PageDto<RdmsCbbDto>` | CBB:VIEW |
| POST | `/customer/cbb/disable/{cbbId}` | 停用CBB 共用基础模块 | — | `String` | CBB:UPDATE |
| POST | `/customer/cbb/enable/{cbbId}` | 启用CBB 共用基础模块 | — | `String` | CBB:UPDATE |
| POST | `/customer/cbb/discard/{cbbId}` | 废弃：请求体 reason 为废弃原因（10 至 500 字） | 体 `Map<String, String>` | `String` | CBB:UPDATE |
| POST | `/customer/cbb/tech-doc/get/{cbbId}` | 技术文档 | — | `CbbTechDocDto` | CBB:VIEW |
| POST | `/customer/cbb/change-owner/{cbbId}/{ownerId}` | 变更负责人 | — | `Integer` | CBB:UPDATE |
| POST | `/customer/cbb/where-used/{cbbId}` | 被引用位置 | — | `List<CbbWhereUsedDto>` | CBB:VIEW |
| POST | `/customer/cbb/define/approve/{jobitemId}` | 批准定义 | — | `String` | CBB:APPROVE |
| POST | `/customer/cbb/define/reject/{jobitemId}` | 驳回定义 | 参 `reason` | `String` | CBB:APPROVE |
| POST | `/customer/cbb/getCbbListByCustomerId/{customerId}` | 选用候选：已发布的 CBB，标签为“名称 发布版本”，带承载特性；同源 CBB 相邻 | — | `List<RdmsCbbCandidateDto>` | CBB:VIEW |
| POST | `/customer/cbb/getCbbDefineCandidates/{jobitemId}` | 查询CBB定义候选人 | — | `List<RdmsCbbCandidateDto>` | CBB:VIEW |
| POST | `/customer/cbb/saveRecordAndData` | 在保存的时候,现将数据存到character表中 | 体 `RdmsCharacterDto` | `RdmsCharacterDto` | CBB:CREATE |
| POST | `/customer/cbb/getCbbData/{cbbProjectId}` | 查询CBB数据 | — | `RdmsCharacterDto` | CBB:VIEW |
| POST | `/customer/cbb/getCbbDefineData/{characterId}` | CBB 定义工单按工单所指记录读取定义内容；历史新版本定义工单指向迭代建议记录，只读 | — | `RdmsCharacterDto` | CBB:VIEW |
| POST | `/customer/cbb/getCbbSimpleInfo/{cbbId}` | 查询CBB简要信息 | — | `RdmsCbb` | CBB:VIEW |
| POST | `/customer/cbb/getCbbSimpleInfoByCode/{cbbProjectId}` | 按编码查询CBB简要信息 | — | `RdmsCharacter` | CBB:VIEW |
| POST | `/customer/cbb/saveCharacterToCbb/{characterId}` | 保存特性CBB | — | `String` | CBB:CREATE |
| POST | `/customer/cbb/getCbbRecordDetailById` | 按ID查询CBB记录详情 | 参 `cbbId` | `RdmsCbbDto` | CBB:VIEW |

## CBB 富文本文档 CbbRichDocController

源码：`customer/src/main/java/com/course/customer/controller/CbbRichDocController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/customer/cbb/rich-doc/get` | 查询CBB 富文本文档 | 体 `RichDocRequest` | `RichDocViewDto` | CBB:VIEW |
| POST | `/customer/cbb/rich-doc/get-versions` | 查询版本 | 体 `RichDocRequest` | `List<Map<String, Object>>` | CBB:VIEW |
| POST | `/customer/cbb/rich-doc/get-compare` | 查询对比 | 体 `RichDocRequest` | `List<Map<String, Object>>` | CBB:VIEW |
| POST | `/customer/cbb/rich-doc/get-lineage` | 查询版本谱系 | 体 `RichDocRequest` | `Map<String, Object>` | CBB:VIEW |

## 篇章 ChapterController

源码：`customer/src/main/java/com/course/customer/controller/ChapterController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/customer/chapter/list` | 查询篇章列表 | 体 `RdmsChapterPageDto` | — | CHAPTER:VIEW |
| POST | `/customer/chapter/save` | 保存，id有值时更新，无值时新增 | 体 `RdmsChapterDto` | — | CHAPTER:CREATE |
| POST | `/customer/chapter/getChapterInfo/{chapterId}` | 查询篇章信息 | — | — | CHAPTER:VIEW |
| DELETE | `/customer/chapter/delete/{id}` | 删除篇章 | — | — | CHAPTER:DELETE |

## 特性处理 CharacterProcessController

源码：`customer/src/main/java/com/course/customer/controller/CharacterProcessController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/customer/character-process/submit` | 提交特性 | 体 `RdmsCharacterProcessDto` | `RdmsCharacterProcessDto` | CHARACTER_PROCESS:UPDATE |
| POST | `/customer/character-process/reject` | 驳回特性 | 体 `RdmsCharacterProcessDto` | `RdmsCharacterProcessDto` | CHARACTER_PROCESS:UPDATE |
| POST | `/customer/character-process/list-by-characterId/{id}` | 按特性ID查询特性列表 | — | `List<RdmsCharacterProcessDto>` | CHARACTER_PROCESS:VIEW |
| POST | `/customer/character-process/listProcessAllInfoByCharacterId/{id}` | 按特性ID查询处理全部信息 | — | `RdmsHmiCharacterProcessDto` | CHARACTER_PROCESS:VIEW |
| DELETE | `/customer/character-process/del/{id}` | 删除特性 | — | — | CHARACTER_PROCESS:DELETE |

## 特性定义 CharacterController

源码：`customer/src/main/java/com/course/customer/controller/CharacterController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/customer/character/listSuggestItems` | 查询建议条目列表 | 体 `PageDto` | — | CHARACTER:VIEW |
| POST | `/customer/character/checkoutAbandon/{characterId}` | 检出放弃 | — | `String` | CHARACTER:UPDATE |
| POST | `/customer/character/listByCustomerId` | 按机构ID查询特性定义列表 | 体 `PageDto<RdmsCharacterDto>` | `PageDto<RdmsCharacterDto>` | CHARACTER:VIEW |
| POST | `/customer/character/listDevelopingCharactersByPjm` | 按项目经理查询开发中特性列表 | 体 `PageDto<RdmsCharacterDto>` | `PageDto<RdmsCharacterDto>` | CHARACTER:VIEW |
| POST | `/customer/character/listTestCharactersByTm` | 按测试主管查询测试特性列表 | 体 `PageDto<RdmsCharacterDto>` | `PageDto<RdmsCharacterDto>` | CHARACTER:VIEW |
| POST | `/customer/character/listByPreProjectId` | 根据PreProjectId进行列表查询 | 体 `PageDto<RdmsCharacterDto>` | `PageDto<RdmsCharacterDto>` | CHARACTER:VIEW |
| POST | `/customer/character/listAllApprovedByPreProjectId` | 按预立项ID查询全部已批准列表 | 参 `preProjectId` | `List<RdmsCharacterDto>` | CHARACTER:VIEW |
| POST | `/customer/character/getDevelopCharacterHasListFlag/{userId}` | 查询开发特性有列表标志 | — | `Boolean` | CHARACTER:VIEW |
| POST | `/customer/character/listByProjectId/{projectId}` | 按项目ID查询特性定义列表 | — | `List<RdmsCharacterDto>` | CHARACTER:VIEW |
| POST | `/customer/character/getSetupedCharacterListByProjectId` | 按项目ID查询已立项特性列表 | 参 `projectId` | `List<RdmsCharacterDto>` | CHARACTER:VIEW |
| POST | `/customer/character/getSetupCharacterListByProjectId` | 按项目ID查询立项特性列表 | 参 `projectId` | `List<RdmsCharacterDto>` | CHARACTER:VIEW |
| POST | `/customer/character/getSetupedCharacterListBySubProjectId` | 按子项目ID查询已立项特性列表 | 参 `subprojectId` | `List<RdmsCharacterDto>` | CHARACTER:VIEW |
| POST | `/customer/character/restartSubprojectResearch/{subprojectId}` | 重启子项目研发 | — | — | CHARACTER:UPDATE |
| POST | `/customer/character/startResearchAndDevelopment` | 启动研发和开发 | 体 `RdmsCharacterService.CbbListStr` | `String` | CHARACTER:UPDATE |
| POST | `/customer/character/getCharacterSimpleInfo/{characterId}` | 查询特性简要信息 | — | `RdmsCharacterDto` | CHARACTER:VIEW |
| POST | `/customer/character/getCharacterNameListByIdList` | 根据功能的ID List查询功能名称List | 体 `RdmsHmiMergeCharactersDto` | `RdmsHmiMergeCharactersDto` | CHARACTER:VIEW |
| POST | `/customer/character/getCharacterTreeByProjectId/{subProjectId}` | 按项目ID查询特性树 | — | `List<RdmsHmiTree<RdmsCharacterSimpleDto>>` | CHARACTER:VIEW |
| POST | `/customer/character/getDocTreeByCharacterId` | 按特性ID查询文档树 | 参 `characterId` | `List<RdmsHmiTree<RdmsFileDto>>` | CHARACTER:VIEW |
| POST | `/customer/character/getCharacterReviewDetailInfo` | 查询特性评审详情信息 | 参 `characterId` | `RdmsHmiCharacterReviewDetailDto` | CHARACTER:VIEW |
| POST | `/customer/character/getCharacterDocPageInfo` | 查询特性文档分页信息 | 参 `characterId` | `RdmsHmiCharacterDocPageInfoDto` | CHARACTER:VIEW |
| POST | `/customer/character/getCharacterArchiveDocs` | 查询特性归档文档 | 参 `characterId` | `RdmsHmiCharacterArchiveDocsDto` | CHARACTER:VIEW |
| POST | `/customer/character/getCharacterManhourExeInfoList/{characterId}` | 查询特性预算执行信息列表 | — | `List<RdmsBudgeExeInfoDto>` | CHARACTER:VIEW |
| POST | `/customer/character/getCharacterBudgetExeSummary/{characterId}` | 查询特性预算执行汇总 | — | `RdmsBudgetSummaryDto` | CHARACTER:VIEW |
| POST | `/customer/character/getCharacterRecordDetailById` | 按ID查询特性记录详情 | 参 `characterId` | `RdmsCharacterDto` | CHARACTER:VIEW |
| POST | `/customer/character/getCharacterDocDetailById` | 按ID查询特性文档详情 | 参 `characterId` | `RdmsCharacterDto` | CHARACTER:VIEW |
| POST | `/customer/character/getCharacterListByJobitemId` | 根据工单号获取工单记录的CharacterList 列表 | 参 `jobitemId` | `List<RdmsCharacterDto>` | CHARACTER:VIEW |
| POST | `/customer/character/getIterationCharacterByJobitemId` | 按工单ID查询迭代特性 | 参 `jobitemId` | `RdmsCharacterDto` | CHARACTER:VIEW |
| POST | `/customer/character/getMergedCharacterByJobitemId` | 按工单ID查询已合并特性 | 参 `jobitemId` | `RdmsCharacterDto` | CHARACTER:VIEW |
| POST | `/customer/character/getProjectTeamIdsByCharacterId/{characterId}` | 按特性ID查询项目团队ID 列表 | — | `RdmProjectTeamInfoDto` | CHARACTER:VIEW |
| POST | `/customer/character/getMergedCharacter/{characterId}` | 查询已合并特性 | — | `List<MyCharacter>` | CHARACTER:VIEW |
| POST | `/customer/character/getUpdateCharacterList` | 查询修改特性列表 | 参 `preProjectId` | `List<RdmsCharacterDto>` | CHARACTER:VIEW |
| POST | `/customer/character/list-approved` | 查询已批准列表 | 体 `PageDto<RdmsCharacterDto>` | `PageDto<RdmsCharacterDto>` | CHARACTER:VIEW |
| POST | `/customer/character/list-module-super` | 按超级查询模块列表 | 体 `PageDto<RdmsCharacterDto>` | `PageDto<RdmsCharacterDto>` | CHARACTER:VIEW |
| POST | `/customer/character/getSetupListByProjectId` | 按项目ID查询立项列表 | 参 `projectId` | `List<RdmsCharacterDto>` | CHARACTER:VIEW |
| POST | `/customer/character/getHmiCharacterDocDto` | 获取一个Character的全部信息 | 参 `characterId` | `RdmsHmiCharacterDocDto` | CHARACTER:VIEW |
| POST | `/customer/character/getHmiAllAttachmentsOfCharacter/{characterId}` | 查询人机界面全部附件的特性 | — | `List<RdmsFileDto>` | CHARACTER:VIEW |
| POST | `/customer/character/getHmiAllAttachmentsOfIntCharacter/{characterId}` | 功能集成工单全部附件读取 | — | `List<RdmsFileDto>` | CHARACTER:VIEW |
| POST | `/customer/character/listAllCharactersByProjectId` | 按项目ID查询全部特性列表 | 参 `projectId` | `List<RdmsCharacterDto>` | CHARACTER:VIEW |
| POST | `/customer/character/listAllCharactersBySubProjectId` | 按子项目ID查询全部特性列表 | 参 `subprojectId` | `List<RdmsCharacterDto>` | CHARACTER:VIEW |
| POST | `/customer/character/getDecomposedChildrenCharacters` | 查询已分解下级特性 | 参 `parentCharacterId` | `List<RdmsCharacterDto>` | CHARACTER:VIEW |
| POST | `/customer/character/getCharacterListAndAllDevelopJobitem` | 查询特性列表和全部开发工单 | 体 `PageDto<RdmsCharacterDto>` | `PageDto<RdmsCharacterDto>` | CHARACTER:VIEW |
| POST | `/customer/character/getCharacterAndJobitemList/{characterId}` | 查询特性和工单列表 | — | `RdmsCharacterDto` | CHARACTER:VIEW |
| POST | `/customer/character/getCharacterAndTestTaskJobitemList/{characterId}` | 查询特性和测试任务工单列表 | — | `RdmsCharacterDto` | CHARACTER:VIEW |
| POST | `/customer/character/getCharacterListBySubprojectId/{subprojectId}` | 按子项目ID查询特性列表 | — | `List<RdmsHmiCharacterPlainDto>` | CHARACTER:VIEW |
| POST | `/customer/character/getArchivedCharacterListByCustomerId/{customerId}` | 按机构ID查询已归档特性列表 | — | `List<RdmsHmiCharacterPlainDto>` | CHARACTER:VIEW |
| POST | `/customer/character/getLastReviewInfo` | 查询最近评审信息 | 参 `characterId` | `RdmsHmiJobItemDocPageDto` | CHARACTER:VIEW |
| POST | `/customer/character/getCharacterMaterialBudgetExeInfoList/{pjmId}` | 按项目经理查询特性列表 | — | `List<RdmsCharacterBudgeExeInfoDto>` | CHARACTER:VIEW |
| POST | `/customer/character/getMaterialHasListFlag/{pjmId}` | 查询物料有列表标志 | — | `Boolean` | CHARACTER:VIEW |
| POST | `/customer/character/getCharacterAndSubmitJobitems/{characterId}` | 查询特性和提交工单 | — | `RdmsCharacterDto` | CHARACTER:VIEW |
| POST | `/customer/character/getCharacterAndTestJobSubmitJobitems/{characterId}` | 查询特性和测试工作提交工单 | — | `RdmsCharacterDto` | CHARACTER:VIEW |
| POST | `/customer/character/getCharacterDevelopCompleteStatus/{characterId}` | 查询特性开发已完成状态 | — | `RdmsHmiCharacterCompleteStatusDto` | CHARACTER:VIEW |
| POST | `/customer/character/getNumOfReviewCharactersByNextNode/{nextNode}` | 按下一节点查询数量的评审特性 | — | `Integer` | CHARACTER:VIEW |
| POST | `/customer/character/getUpdateCharacterNumByUserId/{userId}` | 按用户ID查询修改特性数量 | — | `Integer` | CHARACTER:VIEW |
| POST | `/customer/character/getCharacterListAndAllTestJobitem` | 查询特性列表和全部测试工单 | 体 `PageDto<RdmsCharacterDto>` | `PageDto<RdmsCharacterDto>` | CHARACTER:VIEW |
| POST | `/customer/character/getCharacterListAndAllSubmitTestJobitem` | 查询特性列表和全部提交测试工单 | 体 `PageDto<RdmsCharacterDto>` | `PageDto<RdmsCharacterDto>` | CHARACTER:VIEW |
| POST | `/customer/character/getDevelopingCharacterList` | 查询开发中特性列表 | 体 `PageDto<RdmsCharacterDto>` | `PageDto<RdmsCharacterDto>` | CHARACTER:VIEW |
| POST | `/customer/character/getEvaluateJobitemList` | 查询评估工单列表 | 体 `PageDto<RdmsCharacterDto>` | `PageDto<RdmsCharacterDto>` | CHARACTER:VIEW |
| POST | `/customer/character/getTestingJobitemList` | 查询测试中工单列表 | 体 `PageDto<RdmsCharacterDto>` | `PageDto<RdmsCharacterDto>` | CHARACTER:VIEW |
| POST | `/customer/character/getCharacterIntegrationJobList` | 查询特性集成工作列表 | 体 `PageDto<RdmsCharacterDto>` | `PageDto<RdmsCharacterDto>` | CHARACTER:VIEW |
| POST | `/customer/character/getCharacterIntSubmitOriginInfo/{characterId}` | 查询特性提交来源信息 | — | `RdmsHmiCharacterIntOriginDto` | CHARACTER:VIEW |
| POST | `/customer/character/getIntegrationJobitemInfo/{characterId}` | 查询集成工单信息 | — | `RdmsJobItemDto` | CHARACTER:VIEW |
| POST | `/customer/character/getApprovedJobitemList` | 查询已批准工单列表 | 体 `PageDto<RdmsCharacterDto>` | `PageDto<RdmsCharacterDto>` | CHARACTER:VIEW |
| POST | `/customer/character/save` | 保存特性定义 | 体 `RdmsCharacterDto` | `RdmsCharacterDto` | CHARACTER:CREATE |
| POST | `/customer/character/saveSuggestCharacterToSetuped` | 保存建议特性已立项 | 体 `RdmsCharacterDto` | `RdmsCharacterDto` | CHARACTER:CREATE |
| POST | `/customer/character/saveRecordAndData` | 保存记录和数据 | 体 `RdmsCharacterDto` | `RdmsCharacterDto` | CHARACTER:CREATE |
| POST | `/customer/character/getRecordAndData` | 查询记录和数据 | 参 `characterId` | `RdmsCharacterDto` | CHARACTER:VIEW |
| POST | `/customer/character/get-approved-items-num` | 查询已批准条目数量 | 体 `RdmsRequestDto` | `Long` | CHARACTER:VIEW |
| POST | `/customer/character/get-suggest-items-num/{customerUserId}` | 查询建议条目数量 | — | `RespSuggestNum` | CHARACTER:VIEW |
| POST | `/customer/character/approve` | 批准特性定义 | 体 `RdmsCharacterProcessDto` | `String` | CHARACTER:APPROVE |
| POST | `/customer/character/close/{characterId}` | 关闭特性定义 | — | `String` | CHARACTER:UPDATE |
| POST | `/customer/character/modifyCharactersToApprovedStatus` | 修改特性已批准状态 | 体 `List<RdmsCharacterDto>` | — | CHARACTER:UPDATE |
| POST | `/customer/character/saveDecomposeCharacter` | 保存分解特性 | 体 `RdmsCharacterDto` | `RdmsCharacterDto` | CHARACTER:CREATE |
| POST | `/customer/character/saveIterationCharacter` | 保存或提交修订编辑记录 E：正文写入修订分支，E 只承载非正文字段（实现方案第 8.5.7、8.5.8 节） | 体 `RdmsCharacterDto` | `RdmsCharacterDto` | CHARACTER:CREATE |
| POST | `/customer/character/saveMergeCharacter` | 保存合并特性 | 体 `RdmsCharacterDto` | `RdmsCharacterDto` | CHARACTER:CREATE |
| POST | `/customer/character/reject/{characterId}` | 在立项审批时,驳回某个特性定义给审批人,重新处理 | 体 `RejectReasonDto` | — | CHARACTER:UPDATE |
| POST | `/customer/character/submit` | 提交特性定义 | 体 `RdmsCharacterDto` | `RdmsCharacterDto` | CHARACTER:UPDATE |
| POST | `/customer/character/saveDistribution` | 保存分配 | 体 `RdmsCharacterDto` | `RdmsCharacterDto` | CHARACTER:CREATE |
| POST | `/customer/character/getCharacterTree/{characterId}` | 查询特性树 | — | `RdmsCharacterInheritTreeDto` | CHARACTER:VIEW |
| POST | `/customer/character/saveIterationCharacterRecord` | 保存迭代特性记录 | 体 `RdmsRequestDto` | `String` | CHARACTER:CREATE |
| POST | `/customer/character/saveProjectUpdate` | 项目级版本迭代：新预立项，旧项目记版本更新 | 体 `RdmsPreProjectDto` | `String` | CHARACTER:CREATE |
| POST | `/customer/character/terminate/apply/{characterId}` | 申请终止 | 参 `reason` | `String` | CHARACTER:UPDATE |
| POST | `/customer/character/terminate/approve/{jobitemId}` | 批准终止 | — | `String` | CHARACTER:APPROVE |
| POST | `/customer/character/terminate/reject/{jobitemId}` | 驳回终止 | 参 `reason` | `String` | CHARACTER:APPROVE |
| POST | `/customer/character/review-opinion/{characterId}` | 评审意见 | 参 `opinion` | `String` | CHARACTER:APPROVE |
| POST | `/customer/character/save-confirm-manhour` | 保存核定工时 | 体 `RdmsCharacterDto` | `RdmsCharacterDto` | CHARACTER:CREATE |
| DELETE | `/customer/character/del/{id}` | 删除特性定义 | — | — | CHARACTER:DELETE |
| GET | `/customer/character/version/list/{characterId}` | 按序列号列出特性版本：版本、内容版本、产生方式、时间、操作人、是否项目锁定 | — | `List<Map<String, Object>>` | CHARACTER:VIEW |

## 特性富文本文档 CharacterRichDocController

源码：`customer/src/main/java/com/course/customer/controller/CharacterRichDocController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/customer/character/rich-doc/get` | 查询特性富文本文档 | 体 `RichDocRequest` | `RichDocViewDto` | CHARACTER:VIEW |
| POST | `/customer/character/rich-doc/get-summaries` | 查询汇总 | 体 `RichDocRequest` | `Map<String, Map<String, Object>>` | CHARACTER:VIEW |
| POST | `/customer/character/rich-doc/save` | 保存特性富文本文档 | 体 `RichDocRequest` | `Map<String, Object>` | CHARACTER:CREATE |
| POST | `/customer/character/rich-doc/get-versions` | 查询版本 | 体 `RichDocRequest` | `List<Map<String, Object>>` | CHARACTER:VIEW |
| POST | `/customer/character/rich-doc/get-compare` | 查询对比 | 体 `RichDocRequest` | `List<Map<String, Object>>` | CHARACTER:VIEW |
| POST | `/customer/character/rich-doc/restore` | 恢复特性富文本文档 | 体 `RichDocRequest` | `Map<String, Object>` | CHARACTER:UPDATE |
| POST | `/customer/character/rich-doc/edit-lock` | 编辑锁定 | 体 `RichDocRequest` | `Map<String, Object>` | CHARACTER:UPDATE |
| POST | `/customer/character/rich-doc/draft/save` | 保存草稿 | 体 `RichDocRequest` | `Map<String, Object>` | CHARACTER:CREATE |
| POST | `/customer/character/rich-doc/draft/get` | 查询草稿 | 体 `RichDocRequest` | `Map<String, Object>` | CHARACTER:VIEW |
| POST | `/customer/character/rich-doc/branch/create` | 新建分支 | 体 `RichDocRequest` | `Map<String, Object>` | CHARACTER:CREATE |
| POST | `/customer/character/rich-doc/get-branches` | 查询分支 | 体 `RichDocRequest` | `List<Map<String, Object>>` | CHARACTER:VIEW |
| POST | `/customer/character/rich-doc/branch/abandon` | 放弃分支 | 体 `RichDocRequest` | `Map<String, Object>` | CHARACTER:UPDATE |
| POST | `/customer/character/rich-doc/branch/rebase` | 变基特性富文本文档 | 体 `RichDocRequest` | `Map<String, Object>` | CHARACTER:UPDATE |
| POST | `/customer/character/rich-doc/get-merge-preview` | 查询合并预览 | 体 `RichDocRequest` | `Map<String, Object>` | CHARACTER:VIEW |
| POST | `/customer/character/rich-doc/branch/approve-merge` | 批准合并 | 体 `RichDocRequest` | `Map<String, Object>` | CHARACTER:APPROVE |
| POST | `/customer/character/rich-doc/revision/approve-confirm` | 派发人确认修订：characterId 为修订编辑记录 | 体 `RichDocRequest` | `Map<String, Object>` | CHARACTER:APPROVE |
| POST | `/customer/character/rich-doc/revision/approve-return` | 派发人退回修订：characterId 为修订编辑记录 | 体 `RichDocRequest` | `Boolean` | CHARACTER:APPROVE |
| POST | `/customer/character/rich-doc/adoption/create` | 纳入项目：返回纳入分支，前端据此发起变更评审 | 体 `RichDocRequest` | `Map<String, Object>` | CHARACTER:CREATE |
| POST | `/customer/character/rich-doc/get-lineage` | 查询版本谱系 | 体 `RichDocRequest` | `Map<String, Object>` | CHARACTER:VIEW |

## 分类 CourseCategoryController

源码：`customer/src/main/java/com/course/customer/controller/CourseCategoryController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/customer/course/category/all` | 全部 | — | — | COURSE:UPDATE |
| POST | `/customer/course/category/getCategoryById/{categoryId}` | 按ID查询分类 | — | — | COURSE:VIEW |
| POST | `/customer/course/category/getLevel1List` | 查询级别1列表 | — | — | COURSE:VIEW |
| POST | `/customer/course/category/list` | 查询分类列表 | 体 `PageDto` | — | COURSE:VIEW |
| POST | `/customer/course/category/save` | 保存，id有值时更新，无值时新增 | 体 `RdmsCourseCategoryDto` | — | COURSE:CREATE |
| DELETE | `/customer/course/category/delete/{id}` | 删除分类 | — | — | COURSE:DELETE |

## 课程 CourseController

源码：`customer/src/main/java/com/course/customer/controller/CourseController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/customer/course/list` | 查询课程列表 | 体 `RdmsCoursePageDto` | — | COURSE:VIEW |
| POST | `/customer/course/save` | 保存，id有值时更新，无值时新增 | 体 `RdmsCourseDto` | — | COURSE:CREATE |
| DELETE | `/customer/course/delete/{id}` | 删除课程 | — | — | COURSE:DELETE |
| POST | `/customer/course/list-category/{courseId}` | 查找课程下所有分类 | — | — | COURSE:VIEW |
| GET | `/customer/course/find-content/{id}` | 查询内容 | — | — | COURSE:VIEW |
| POST | `/customer/course/save-content` | 保存内容 | 体 `RdmsCourseContentDto` | — | COURSE:CREATE |
| ANY | `/customer/course/sort` | 排序课程 | 体 `RdmsSortDto` | — | COURSE:UPDATE |
| POST | `/customer/course/user-course/{loginName}` | 用户产品 | — | — | COURSE:UPDATE |

## 讲师 TeacherController

源码：`customer/src/main/java/com/course/customer/controller/TeacherController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/customer/course/teacher/all` | 全部 | — | — | COURSE:UPDATE |
| POST | `/customer/course/teacher/my-teacher/{customerId}` | 我的讲师 | — | — | COURSE:UPDATE |
| POST | `/customer/course/teacher/list` | 查询讲师列表 | 体 `RdmsTeacherPageDto` | — | COURSE:VIEW |
| POST | `/customer/course/teacher/save` | 保存，id有值时更新，无值时新增 | 体 `RdmsTeacherDto` | — | COURSE:CREATE |
| DELETE | `/customer/course/teacher/delete/{id}` | 删除讲师 | — | — | COURSE:DELETE |
| POST | `/customer/course/teacher/list-category/{teacherId}` | 查询分类列表 | — | — | COURSE:VIEW |

## 需求管理 DemandController

源码：`customer/src/main/java/com/course/customer/controller/DemandController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/customer/demand/list` | 按机构ID查询已保存需求列表 | 体 `PageDto<RdmsDemandDto>` | `PageDto<RdmsDemandDto>` | DEMAND:VIEW |
| POST | `/customer/demand/list-submitted` | 按预立项ID查询提交需求列表 | 体 `PageDto<RdmsDemandDto>` | `PageDto<RdmsDemandDto>` | DEMAND:VIEW |
| POST | `/customer/demand/get-demand/{demandId}` | 按ID查询需求 | — | `RdmsDemandDto` | DEMAND:VIEW |
| POST | `/customer/demand/getDemandHasListFlag/{userId}` | 查询需求有列表标志 | — | `Boolean` | DEMAND:VIEW |
| POST | `/customer/demand/getDemandListByCharacterId/{characterId}` | 按特性ID查询需求列表 | — | `List<RdmsDemandDto>` | DEMAND:VIEW |
| POST | `/customer/demand/save` | 保存需求 | 体 `RdmsDemandDto` | `RdmsDemandDto` | DEMAND:CREATE |
| POST | `/customer/demand/submit` | 提交需求 | 体 `RdmsDemandDto` | `RdmsDemandDto` | DEMAND:UPDATE |
| POST | `/customer/demand/submit/{demandId}` | 按需求ID提交需求 | — | `RdmsDemandDto` | DEMAND:UPDATE |
| DELETE | `/customer/demand/del/{id}` | 删除需求 | — | — | DEMAND:DELETE |
| POST | `/customer/demand/abandon` | 废弃需求（COMPLETED 不可废弃） | 体 `RdmsDemandDto` | `RdmsDemandDto` | DEMAND:UPDATE |

## 部门 DepartmentController

源码：`customer/src/main/java/com/course/customer/controller/DepartmentController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/customer/department/listDepartment/{customerId}` | 查询部门列表 | — | — | DEPARTMENT:VIEW |
| POST | `/customer/department/getDepartmentMemberList/{departmentId}` | 查询部门成员列表 | — | — | DEPARTMENT:VIEW |
| POST | `/customer/department/save` | 新建部门，并把 memberIdList 中的人员归入新部门 | 体 `RdmsDepartmentDto` | — | DEPARTMENT:CREATE |
| POST | `/customer/department/edit` | 编辑部门。memberIdList 非空时成员按整表替换，移出者改为未分配；为 null 时不改成员 | 体 `RdmsDepartmentDto` | — | DEPARTMENT:UPDATE |
| POST | `/customer/department/get-departments` | 查询部门 | 体 `RdmsResponseDto` | — | DEPARTMENT:VIEW |
| POST | `/customer/department/getOrgTree` | 部门树：负责人姓名、直属人数、含下级人数，末尾附“未分配”节点 | — | `OrgNodeDto` | DEPARTMENT:VIEW |
| POST | `/customer/department/listUnassigned` | 查询未指派列表 | — | `List<RdmsCustomerUser>` | DEPARTMENT:VIEW |
| POST | `/customer/department/getSuperior/{customerUserId}` | 汇报关系：上级链与直接下级 | — | `OrgRelationDto` | DEPARTMENT:VIEW |
| POST | `/customer/department/assignMembers` | 把人员批量归入部门：id 为部门编号，memberIdList 为人员编号 | 体 `RdmsDepartmentDto` | — | DEPARTMENT:UPDATE |
| DELETE | `/customer/department/del/{id}` | 删除部门 | — | — | DEPARTMENT:DELETE |

## 费用管理 FeeController

源码：`customer/src/main/java/com/course/customer/controller/FeeController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/customer/fee/getQualityFeeApplicationNumByUserId/{userId}` | 按用户ID查询质量费用申请数量 | — | `Long` | FEE:VIEW |
| POST | `/customer/fee/saveFeeApplication` | 保存费用申请 | 体 `RdmsFeeManageDto` | `String` | FEE:CREATE |
| POST | `/customer/fee/approveFeeApplication` | 批准费用申请 | 体 `RdmsFeeManageDto` | `String` | FEE:APPROVE |
| POST | `/customer/fee/listFeesByCharacterId` | 按特性ID查询费用列表 | 体 `PageDto<RdmsFeeManageDto>` | `PageDto<RdmsFeeManageDto>` | FEE:VIEW |
| POST | `/customer/fee/getListByCharacterId/{characterId}` | 按特性ID查询费用列表 | — | `List<RdmsFeeManageDto>` | FEE:VIEW |
| POST | `/customer/fee/getListBySubprojectId/{subprojectId}` | 按子项目ID查询费用列表 | — | `List<RdmsFeeManageDto>` | FEE:VIEW |
| POST | `/customer/fee/getListByPreProjectId/{preprojectId}` | 按预立项ID查询费用列表 | — | `List<RdmsFeeManageDto>` | FEE:VIEW |
| POST | `/customer/fee/getListByProjectId/{projectId}` | 按项目ID查询费用列表 | — | `List<RdmsFeeManageDto>` | FEE:VIEW |
| POST | `/customer/fee/getListByQualityId/{qualityId}` | 按质量ID查询费用列表 | — | `List<RdmsFeeManageDto>` | FEE:VIEW |
| POST | `/customer/fee/getFeeApplicationListByWriterId/{writerId}` | 按编写人ID查询费用申请列表 | — | `List<RdmsFeeManageDto>` | FEE:VIEW |
| POST | `/customer/fee/getFeeApplicationNum/{userId}` | 查询费用申请数量 | — | `Integer` | FEE:VIEW |
| POST | `/customer/fee/getFeeApplicationNum_project/{userId}` | 查询费用申请数量（项目） | — | `Integer` | FEE:VIEW |
| POST | `/customer/fee/getFeeApplicationByCode/{code}` | 按编码查询费用申请 | — | `RdmsFeeManageDto` | FEE:VIEW |
| POST | `/customer/fee/confirmCancel/{code}` | 确认取消 | — | `String` | FEE:UPDATE |
| POST | `/customer/fee/updateFeeApproveFileListStr` | 修改费用批准文件列表字符串 | 体 `RdmsFeeManageDto` | `String` | FEE:UPDATE |
| POST | `/customer/fee/updateFeeDealWithFileListStr` | 修改费用处理含文件列表字符串 | 体 `RdmsFeeManageDto` | `String` | FEE:UPDATE |
| POST | `/customer/fee/updateFeeCompleteFileListStr` | 修改费用已完成文件列表字符串 | 体 `RdmsFeeManageDto` | `String` | FEE:UPDATE |
| POST | `/customer/fee/submit` | 提交费用 | 体 `RdmsFeeManageDto` | `String` | FEE:UPDATE |
| POST | `/customer/fee/complete/{code}` | 完成费用 | — | `String` | FEE:UPDATE |
| POST | `/customer/fee/diagram` | 本申请单的流程图。路径含 /diagram，按现有推断规则归费用模块的查看， | 体 `RdmsFeeManageDto` | `FlowDiagramDto` | FEE:VIEW |
| POST | `/customer/fee/history` | 本申请单的办理记录。路径含 /history，归费用模块的查看 | 体 `RdmsFeeManageDto` | `List<FlowDiagramDto.FlowNodeDetailDto>` | FEE:VIEW |
| POST | `/customer/fee/previewHandler/query` | 审批人预览。路径含 /query，按现有推断规则归为查看，申请人无需审批权限 | 体 `RdmsFeeManageDto` | `RdmsFeeManageDto` | FEE:VIEW |

## 文件申请流程 FileApplyFlowController

源码：`customer/src/main/java/com/course/customer/controller/FileApplyFlowController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/customer/file-apply/route` | 路由 | 体 `RdmsFileApplyDto` | `String` | FILE:UPDATE |
| POST | `/customer/file-apply/apply` | 申请文件申请流程 | 体 `RdmsFileApplyDto` | `RdmsFileApplyDto` | FILE:UPDATE |
| POST | `/customer/file-apply/agree/{applyId}` | 同意文件申请流程 | — | `String` | FILE:UPDATE |
| POST | `/customer/file-apply/reject/{applyId}` | 驳回文件申请流程 | — | `String` | FILE:UPDATE |

## 流程定义与实例 FlowController

源码：`customer/src/main/java/com/course/customer/controller/FlowController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/customer/flow/def/list` | 路径含 /list，按推断规则为 VIEW | 体 `FlowRequestDto` | `List<RdmsFlowDef>` | FLOW:VIEW |
| POST | `/customer/flow/def/get` | 路径含 /get，按推断规则为 VIEW。平台内置定义对当前机构可见 | 体 `FlowRequestDto` | `RdmsFlowDef` | FLOW:VIEW |
| POST | `/customer/flow/def/save` | 路径含 /save，按现行推断规则为 CREATE，而不是 UPDATE | 体 `FlowRequestDto` | `RdmsFlowDef` | FLOW:CREATE |
| POST | `/customer/flow/def/save-draft-from/{id}` | 以历史版本建草稿。路径含 /save，同为 CREATE | — | `RdmsFlowDef` | FLOW:CREATE |
| POST | `/customer/flow/def/validate` | 校验流程定义与实例 | 体 `FlowRequestDto` | `List<String>` | FLOW:VIEW |
| POST | `/customer/flow/def/publish` | 发布流程定义与实例 | 体 `FlowRequestDto` | `RdmsFlowDef` | FLOW:UPDATE |
| POST | `/customer/flow/def/disable` | 停用流程定义与实例 | 体 `FlowRequestDto` | `String` | FLOW:UPDATE |
| POST | `/customer/flow/capability/list` | 能力 | — | `FlowCapabilityDto` | FLOW:VIEW |
| POST | `/customer/flow/instance/diagram` | 图 | 体 `FlowRequestDto` | `FlowDiagramDto` | FLOW:VIEW |
| POST | `/customer/flow/instance/history` | 历史 | 体 `FlowRequestDto` | `List<FlowDiagramDto.FlowNodeDetailDto>` | FLOW:VIEW |
| POST | `/customer/flow/task/complete` | 统一推进入口。已有领域绑定的业务码可以调用；费用控制器仍走原代码，不经本接口 | 体 `FlowRequestDto` | `String` | FLOW:UPDATE |

## 功能树 FunctionTreeController

源码：`customer/src/main/java/com/course/customer/controller/FunctionTreeController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/customer/function-tree/saveFunctionTree` | 保存功能树 | 体 `List<RdmsFunctionTreeDto>` | `RdmsFunctionTreeDto` | PRE_PROJECT:CREATE |
| POST | `/customer/function-tree/getFunctionTree/{preprojectId}` | 查询功能树 | — | `RdmsFunctionTreeDto` | PRE_PROJECT:VIEW |
| POST | `/customer/function-tree/getFunctionTreeByProjectId/{projectId}` | 按项目ID查询功能树 | — | `RdmsFunctionTreeDto` | PRE_PROJECT:VIEW |
| POST | `/customer/function-tree/synchronizationFunctionTree/{preprojectId}` | 同步功能树 | — | `Integer` | PRE_PROJECT:UPDATE |

## Gantt图 GanttController

源码：`customer/src/main/java/com/course/customer/controller/GanttController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/customer/gantt/saveLink` | 保存关联 | 体 `RdmsGanttLinksDto` | `String` | GANTT:CREATE |
| POST | `/customer/gantt/delLink/{id}` | 删除关联 | — | `String` | GANTT:DELETE |
| POST | `/customer/gantt/getGanttLinkListBySubprojectId/{subprojectId}` | 按子项目ID查询甘特图关联列表 | — | `List<RdmsGanttLinksDto>` | GANTT:VIEW |
| POST | `/customer/gantt/saveTaskPlanJobitem` | 保存任务计划工单 | 体 `RdmsJobItemGanttDto` | `String` | GANTT:CREATE |
| POST | `/customer/gantt/updatePlanSchedule` | 修改计划计划 | 体 `RdmsJobItemGanttDto` | `String` | GANTT:UPDATE |
| POST | `/customer/gantt/deletePlanJobitem/{id}` | 删除计划工单 | — | `String` | GANTT:DELETE |
| POST | `/customer/gantt/issuePlanTaskBySubprojectId/{subprojectId}` | 按子项目ID问题计划任务 | — | `GanttIssueResultDto` | GANTT:UPDATE |
| POST | `/customer/gantt/issuePlanTaskNow/{planId}` | 问题计划任务当前 | — | `String` | GANTT:UPDATE |
| POST | `/customer/gantt/plan/loadPreview` | 加载预览 | 体 `RdmsJobItemGanttDto` | `GanttLoadPreviewDto` | GANTT:UPDATE |
| POST | `/customer/gantt/getGanttListBySubprojectId/{subprojectId}` | 按子项目ID查询甘特图列表 | — | `List<RdmsGanttDto>` | GANTT:VIEW |
| POST | `/customer/gantt/getJobItemGanttById/{id}` | 按ID查询工单甘特图 | — | `RdmsJobItemGanttDto` | GANTT:VIEW |

## IPMT IpmtController

源码：`customer/src/main/java/com/course/customer/controller/IpmtController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/customer/ipmt/list` | 查询IPMT列表 | 体 `PageDto<RdmsIpmtDto>` | `PageDto<RdmsIpmtDto>` | WORKER:VIEW |
| POST | `/customer/ipmt/listByCustomerId/{customerId}` | 查询IPMT列表 | — | `List<String>` | WORKER:VIEW |
| POST | `/customer/ipmt/save` | 保存IPMT | 体 `RdmsIpmtDto` | `RdmsIpmtDto` | WORKER:CREATE |
| DELETE | `/customer/ipmt/del/{id}` | 删除IPMT | — | — | WORKER:DELETE |

## 工单处理 JobItemProcessController

源码：`customer/src/main/java/com/course/customer/controller/JobItemProcessController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/customer/job-item-process/save` | 保存工单 | 体 `RdmsJobItemProcessDto` | `RdmsJobItemProcessDto` | JOB_ITEM_PROCESS:CREATE |
| POST | `/customer/job-item-process/submit` | 提交工单 | 体 `RdmsJobItemProcessDto` | `RdmsJobItemProcessDto` | JOB_ITEM_PROCESS:UPDATE |
| DELETE | `/customer/job-item-process/del/{id}` | 删除工单 | — | — | JOB_ITEM_PROCESS:DELETE |
| POST | `/customer/job-item-process/get-jobitem-process-info` | 查询工单处理信息 | 体 `RdmsJobItemProcessDto` | `RdmsJobItemProcessDto` | JOB_ITEM_PROCESS:VIEW |
| POST | `/customer/job-item-process/get-jobitem-process-info-list/{jobItemId}` | 查询工单处理信息列表 | — | `List<RdmsJobItemProcessDto>` | JOB_ITEM_PROCESS:VIEW |

## 工单 JobItemController

源码：`customer/src/main/java/com/course/customer/controller/JobItemController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/customer/job-items/getNumOfMilestoneReviewByNextNodeAndStatus/{nextNode}` | 根据下一节点和状态获取里程碑评审数量 | — | `Integer` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getDevelopJobitemListByReviewJobitem` | 根据评审工单获取开发工单列表 | 参 `jobitemId` | `List<RdmsJobItemDto>` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getTestJobitemListByReviewJobitem` | 根据评审工单获取测试工单列表 | 参 `jobitemId` | `List<RdmsJobItemDto>` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getMaterialListByReviewJobitem/{jobitemId}` | 根据评审工单获取物料列表 | — | `List<RdmsMaterialManageDto>` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getFeeListByReviewJobitem/{jobitemId}` | 根据评审工单获取费用列表 | — | `List<RdmsFeeManageDto>` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getExamineNum_develop/{characterId}` | 获取开发角色待审核工单数量 | — | `Integer` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getExamineNum_character_test/{characterId}` | 获取测试角色待审核工单数量 | — | `Integer` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getExamineNum_quality/{qualityId}` | 获取质量角色待审核工单数量 | — | `Integer` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getExamineNum_bugfeedback/{bugId}` | 获取Bug反馈待审核工单数量 | — | `Integer` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getExamineNum_task/{jobitemId}` | 获取任务待审核辅助工单数量 | — | `Integer` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getExamineNum_test/{jobitemId}` | 获取测试待审核Bug工单数量 | — | `Integer` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getNotifyDevelopNum/{nextNode}` | 查询通知开发数量 | — | `Integer` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/countQualityJobFlag/{nextNode}` | 统计质量工作标志 | — | `Integer` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getRefuseNumByUserId/{nextNode}` | 按用户ID查询拒绝数量 | — | `Integer` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getSubmitSubTaskJobitemNum/{userId}` | 查询提交子任务工单数量 | — | `Integer` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getSubmitSubTaskJobitemNumByPjmAndSubprojectId` | 按项目经理和子项目ID查询提交子任务工单数量 | 体 `RdmsHmiIdsDto` | `Integer` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getSubmitPreTaskJobitemNumBySysgmAndPreprojectId` | 按系统工程总监和预立项ID查询提交预任务工单数量 | 体 `RdmsHmiIdsDto` | `Integer` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getSubmitTaskJobitemNumByPjmAndProjectId` | 按项目经理和项目ID查询提交任务工单数量 | 体 `RdmsHmiIdsDto` | `Integer` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/listBySubprojectId` | 按子项目ID查询工单列表 | 体 `PageDto<RdmsJobItemDto>` | `PageDto<RdmsJobItemDto>` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/listByProjectId` | 按项目ID查询工单列表 | 体 `PageDto<RdmsJobItemDto>` | `PageDto<RdmsJobItemDto>` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/listJobitemListBySubProjectId` | 按子项目ID查询工单列表 | 体 `PageDto<RdmsJobItemDto>` | `PageDto<RdmsJobItemDto>` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/listJobitemListByPreProjectId` | 按预立项ID查询工单列表 | 体 `PageDto<RdmsJobItemDto>` | `PageDto<RdmsJobItemDto>` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/listJobItem` | 按项目ID和状态查询工单列表 | 体 `PageDto<RdmsJobItemDto>` | `PageDto<RdmsJobItemDto>` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getReviewHasListFlag/{userId}` | 查询评审有列表标志 | — | `Boolean` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/listReviewJobitems` | 查询评审工单列表 | 体 `PageDto<RdmsJobItemDto>` | `PageDto<RdmsJobItemDto>` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/listByCustomerId` | 按机构ID查询工单列表 | 体 `PageDto<RdmsJobItemDto>` | `PageDto<RdmsJobItemDto>` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/listByLoginUserId` | 按登录用户ID查询工单列表 | 体 `PageDto<RdmsJobItemDto>` | `PageDto<RdmsJobItemDto>` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getTestJobListBySubproject` | 按子项目查询测试工作列表 | 体 `PageDto<RdmsJobItemDto>` | `PageDto<RdmsJobItemDto>` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getBugJobListBySubproject` | 按子项目查询缺陷工作列表 | 体 `PageDto<RdmsJobItemDto>` | `PageDto<RdmsJobItemDto>` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/listReviewsByLoginUserId` | 按登录用户ID查询评审列表 | 体 `PageDto<RdmsJobItemDto>` | `PageDto<RdmsJobItemDto>` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getReviewJobitemsByCharacterId/{characterId}` | 按特性ID查询评审工单 | — | `List<RdmsJobItemDto>` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/jobListByLoginUserId/{workerId}` | 按登录用户ID工作列表 | — | `List<RdmsJobItemDto>` | JOB_ITEM:UPDATE |
| POST | `/customer/job-items/getCompleteJobitemListBySubprojectId/{subprojectId}` | 按子项目ID查询已完成工单列表 | — | `List<RdmsJobItemSimpleDto>` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/countJobitemsByLoginUserId/{userId}` | 按登录用户ID统计工单 | — | `Integer` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getReviewJobitemsByProjectId/{projectId}` | 按项目ID查询评审工单 | — | `List<RdmsJobItemDto>` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getJobitemHandlingList` | 查询工单处理中列表 | 体 `PageDto<RdmsJobItemDto>` | `PageDto<RdmsJobItemDto>` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getSystemJobitemHandlingList` | 查询系统工单处理中列表 | 体 `PageDto<RdmsJobItemDto>` | `PageDto<RdmsJobItemDto>` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getDevelopJobitemHandlingList` | 查询开发工单处理中列表 | 体 `PageDto<RdmsJobItemDto>` | `PageDto<RdmsJobItemDto>` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getJobitemExamineList` | 查询工单审核列表 | 体 `PageDto<RdmsJobItemDto>` | `PageDto<RdmsJobItemDto>` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getReCheckCharactersByJobitemId/{jobitemId}` | 按工单ID查询re检查特性 | — | `List<RdmsCharacterDto>` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getSystemJobitemExamineList` | 查询系统工单审核列表 | 体 `PageDto<RdmsJobItemDto>` | `PageDto<RdmsJobItemDto>` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getJobitemEvaluateList` | 查询工单评估列表 | 体 `PageDto<RdmsJobItemDto>` | `PageDto<RdmsJobItemDto>` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getSystemJobitemEvaluateList` | 查询系统工单评估列表 | 体 `PageDto<RdmsJobItemDto>` | `PageDto<RdmsJobItemDto>` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getDevelopJobitemEvaluateList` | 查询开发工单评估列表 | 体 `PageDto<RdmsJobItemDto>` | `PageDto<RdmsJobItemDto>` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getDevelopJobitemSubmitList` | 查询开发工单提交列表 | 体 `PageDto<RdmsJobItemDto>` | `PageDto<RdmsJobItemDto>` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getDevelopApprovedJobItemList` | 查询开发已批准工单列表 | 体 `PageDto<RdmsJobItemDto>` | `PageDto<RdmsJobItemDto>` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getJobitemAndAllAssistJobitems/{jobitemId}` | 查询工单和全部协办工单 | — | `List<RdmsJobItemDto>` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getReviewRelatedJobitems_handling/{jobitemId}` | 查询评审相关工单（处理中） | — | `RdmsHmiJobItemAndChildrenItemsDto` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getReviewRelatedJobitems_submit/{jobitemId}` | 查询评审相关工单（提交） | — | `List<RdmsJobItemDto>` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getReviewRelatedSubmitJobNum/{reviewJobitemId}` | 按评审工单ID查询评审相关提交工作数量 | — | `Integer` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getReviewRelatedSubmitJobNumByUserId/{userId}` | 按用户ID查询评审相关提交工作数量 | — | `Integer` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getJobitemAndAllAssistJobitems_submit/{jobitemId}` | 查询工单和全部协办工单（提交） | — | `List<RdmsJobItemDto>` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getJobitemAndAllEvaluateJobitems/{jobitemId}` | 查询工单和全部评估工单 | — | `List<RdmsJobItemDto>` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/get-examine-items-num` | 查询审核条目数量 | 体 `RdmsRequestDto` | `Long` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/get-system-examine-items-num` | 查询系统审核条目数量 | 体 `RdmsRequestDto` | `Long` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/get-evaluate-items-num` | 查询评估条目数量 | 体 `RdmsRequestDto` | `Long` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getRefuseNum` | 查询拒绝数量 | 体 `RdmsRequestDto` | `Long` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getEvaluateNumBySubprojectId` | 按子项目ID查询评估数量 | 体 `RdmsRequestDto` | `Long` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getHandlingByPreProjectId` | 按预立项ID查询处理中 | 体 `RdmsRequestDto` | `Long` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getExamineNumByPreProjectId` | 按预立项ID查询审核数量 | 体 `RdmsRequestDto` | `Long` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getEvaluateNumByPreProjectId` | 按预立项ID查询评估数量 | 体 `RdmsRequestDto` | `Long` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getTestManageJobNumByUserId/{userId}` | 按用户ID查询测试管理工作数量 | — | `Long` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getTestJobNumByUserId/{userId}` | 按用户ID查询测试工作数量 | — | `Long` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getFunctionManageJobNumByUserId/{userId}` | 按用户ID查询功能管理工作数量 | — | `Long` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/get-system-evaluate-items-num` | 查询系统评估条目数量 | 体 `RdmsRequestDto` | `Long` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getJobItemCreateDlgInfo/{characterId}` | 查询工单新建信息 | — | `RdmsHmiJobItemCreateDto` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getJobItemCreateDlgInfoByQualityId/{qualityId}` | 按质量ID查询工单新建信息 | — | `RdmsHmiJobItemCreateDto` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getJobItemCreateDlgInfoByBugfeedbackId/{bugfeedbackId}` | 按缺陷ID查询工单新建信息 | — | `RdmsHmiJobItemCreateDto` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getJobitemByCharacterId/{characterId}` | 按特性ID查询工单 | — | `RdmsJobItemDto` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getJobitemListByPjm/{customerUserId}` | 按项目经理查询工单列表 | — | `List<RdmsJobItemDto>` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/listJobitemListByPjm` | 按项目经理查询工单列表 | 体 `PageDto<RdmsJobItemDto>` | `PageDto<RdmsJobItemDto>` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getJobitemListByCreater` | 按创建人查询工单列表 | 体 `PageDto<RdmsJobItemDto>` | `PageDto<RdmsJobItemDto>` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/listJobsByPdm` | 按产品经理查询工作列表 | 体 `PageDto<RdmsJobItemDto>` | `PageDto<RdmsJobItemDto>` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getJobitemHasListFlag/{userId}` | 查询工单有列表标志 | — | `Boolean` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/listJobitemListByPjm_complete` | 按项目经理查询工单列表（已完成） | 体 `PageDto<RdmsJobItemDto>` | `PageDto<RdmsJobItemDto>` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/listJobsByPdm_complete` | 按产品经理查询工作列表（已完成） | 体 `PageDto<RdmsJobItemDto>` | `PageDto<RdmsJobItemDto>` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getCompletedJobitemList/{characterId}` | 查询已完成工单列表 | — | `RdmsHmiCompleteStatusInfoDto` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getCompletedJobitemList_testTask/{characterId}` | 查询已完成工单列表（测试任务） | — | `RdmsHmiCompleteStatusInfoDto` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getCompletedJobitemListByQualityId/{qualityId}` | 按质量ID查询已完成工单列表 | — | `RdmsHmiCompleteStatusInfoDto` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getCompletedJobitemListByBugfeedbackId/{bugfeedbackId}` | 按缺陷ID查询已完成工单列表 | — | `RdmsHmiCompleteStatusInfoDto` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getCompletedJobitemListBySubProjectId/{subprojectId}` | 按子项目ID查询已完成工单列表 | — | `RdmsHmiCompleteStatusInfoDto` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getCompletedJobitemListByWorkerId` | 按成员ID查询已完成工单列表 | 参 `month`；参 `customerId`；参 `workerId` | `List<RdmsJobItemDto>` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getCompletedJobitemListByPreProjectId/{preprojectId}` | 按预立项ID查询已完成工单列表 | — | `RdmsHmiCompleteStatusInfoDto` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getCompletedJobitemListByProjectId/{projectId}` | 按项目ID查询已完成工单列表 | — | `RdmsHmiCompleteStatusInfoDto` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getIdsDtoByJobitemId/{jobitemId}` | 按工单ID查询ID 列表 | — | `RdmsHmiIdsDto` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getCompletedAssistJobitemList/{jobitemId}` | 查询已完成协办工单列表 | — | `RdmsHmiCompleteStatusInfoDto` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getCompletedBugJobitemList/{jobitemId}` | 查询已完成缺陷工单列表 | — | `RdmsHmiCompleteStatusInfoDto` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getCharactersHandlingStatusOfJobitem` | 查询特性处理中状态的工单 | 参 `characterId` | `Boolean` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getJobItemCreateDlgInfo-demand` | 查询工单新建信息（需求） | 体 `RdmsHmiInCreateJobitemByMulDemandDto` | `RdmsHmiJobItemCreateDto` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getJobItemCreateDlgInfo-develop/{jobitemId}` | 查询工单新建信息（开发） | — | `RdmsHmiJobItemCreateDto` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getJobItemCreateDlgInfo-assist/{parentJobitemId}` | 功能集成或者项目任务的协作任务 | — | `RdmsHmiJobItemCreateDto` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getJobItemCreateDlgInfo-function-assist/{parentJobitemId}` | 查询工单新建信息（功能协办） | — | `RdmsHmiJobItemCreateDto` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getJobItemCreateDlgInfo-taskSubpAssist/{parentJobitemId}` | 查询工单新建信息（任务子项目协办） | — | `RdmsHmiJobItemCreateDto` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getJobItemCreateDlgInfo-quality-assist/{parentJobitemId}` | 查询工单新建信息（质量协办） | — | `RdmsHmiJobItemCreateDto` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getJobItemCreateDlgInfo-assist-test/{testedAssistJobId}` | 查询工单新建信息（协办测试） | — | `RdmsHmiJobItemCreateDto` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getJobItemCreateDlgInfo-function-assist-test/{testedAssistJobId}` | 查询工单新建信息（功能协办测试） | — | `RdmsHmiJobItemCreateDto` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getJobitemDetailInfo` | 查询工单详情信息 | 参 `jobitemId` | `RdmsJobItemDto` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getTestJobitemAuxPlainInfo` | 查询测试工单辅助明文信息 | 参 `jobitemId` | `RdmsHmiTestJobitemAuxPlainDto` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getJobitemRecordDetailInfo` | 查询工单记录详情信息 | 参 `jobitemId` | `RdmsJobItemDto` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getJobitemSimpleInfo` | 查询工单简要信息 | 参 `jobitemId` | `RdmsJobItemDto` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getMilestoneTaskJobList/{milestoneId}` | 获得里程碑任务列表 | — | `List<RdmsJobItemDto>` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getIterationOriginCharacterIdByJobitemId/{jobItemId}` | 按工单ID查询迭代来源特性ID | — | `String` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getReviewJobitemListDetail` | 查询评审工单列表详情 | 参 `jobitemId` | `RdmsHmiJobItemAndOtherReviewJobItemsDto` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getReviewNotifyInfo` | 查询评审通知信息 | 参 `jobitemId` | `RdmsHmiJobItemAndReviewResultDto` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/confirmMilestoneNotify/{jobItemId}` | 确认里程碑通知 | — | `String` | JOB_ITEM:UPDATE |
| POST | `/customer/job-items/confirmDefineUpdateNotify/{jobItemId}` | 定义更新通知确认：只能由接收人确认，确认后关闭 | — | `String` | JOB_ITEM:UPDATE |
| POST | `/customer/job-items/confirmCbbUpdateNotify/{jobItemId}` | 确认CBB修改通知 | — | `String` | JOB_ITEM:UPDATE |
| POST | `/customer/job-items/confirmPlanIssueNotify/{jobItemId}` | 确认计划问题通知 | — | `String` | JOB_ITEM:UPDATE |
| POST | `/customer/job-items/confirmBudgetAdjustNotify/{jobItemId}` | 确认预算调整通知 | — | `String` | JOB_ITEM:UPDATE |
| POST | `/customer/job-items/getReviewResultInfo` | 查询评审结果信息 | 参 `jobitemId` | `RdmsHmiJobItemAndReviewResultDto` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/confirmReviewResult/{jobItemId}` | 确认评审结果 | — | — | JOB_ITEM:UPDATE |
| POST | `/customer/job-items/getIntegrationJobitemDetailInfo` | 获取集成工单和所有协作工单信息 | 参 `jobitemId` | `RdmsJobItemDto` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/completeReCheckJobitem/{jobItemId}` | 复核工单执行人完成复核 | — | `Boolean` | JOB_ITEM:UPDATE |
| POST | `/customer/job-items/getJobitemAllAttachments/{jobItemId}` | 获取一个工单的所有相关的附件信息 | — | `List<RdmsFileDto>` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getAllChildrenJobitemAttachments/{jobItemId}` | 查询全部下级工单附件 | — | `List<RdmsFileDto>` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getChildrenJobitemNotCompleteNum/{parentJobitem}` | 查询下级工单未已完成数量 | — | `Long` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/refuseJobitem` | 拒绝工单 | 体 `RdmsJobItemProcessDto` | — | JOB_ITEM:UPDATE |
| POST | `/customer/job-items/cancelJobitem` | 取消工单 | 参 `jobitemId` | — | JOB_ITEM:UPDATE |
| POST | `/customer/job-items/reIssueJobitem` | 重新下发工单 | 体 `RdmsJobItemDto` | `String` | JOB_ITEM:UPDATE |
| POST | `/customer/job-items/modifyTimeLimit` | 修改时间限制 | 体 `RdmsJobItemDto` | `String` | JOB_ITEM:UPDATE |
| POST | `/customer/job-items/modifyManhour` | 修改工时 | 体 `RdmsJobItemDto` | `String` | JOB_ITEM:UPDATE |
| POST | `/customer/job-items/save` | 特性管理阶段对工单的保存处理 | 体 `RdmsJobItemDto` | `RdmsJobItemDto` | JOB_ITEM:CREATE |
| POST | `/customer/job-items/saveEvaluate` | 保存评估 | 体 `RdmsJobItemDto` | `RdmsJobItemDto` | JOB_ITEM:CREATE |
| POST | `/customer/job-items/submit` | 提交工单 | 体 `RdmsJobItemDto` | `RdmsJobItemDto` | JOB_ITEM:UPDATE |
| POST | `/customer/job-items/reject` | 驳回工单 | 体 `RdmsJobItemProcessDto` | — | JOB_ITEM:UPDATE |
| POST | `/customer/job-items/rechecked` | 已复核 | 体 `RdmsJobItemProcessDto` | — | JOB_ITEM:UPDATE |
| POST | `/customer/job-items/recheck-reject` | 分解复核不通过：jobDescription 为复核意见 | 体 `RdmsJobItemProcessDto` | — | JOB_ITEM:UPDATE |
| POST | `/customer/job-items/closeJobitem/{jobId}` | 关闭工作 | — | — | JOB_ITEM:UPDATE |
| POST | `/customer/job-items/archive` | 归档工单 | 体 `RdmsHmiIdsDto` | — | JOB_ITEM:UPDATE |
| POST | `/customer/job-items/getPjmProjectJobNotifyNum` | 查询项目经理项目工作通知数量 | 体 `RdmsHmiIdsDto` | `Integer` | JOB_ITEM:VIEW |
| POST | `/customer/job-items/getPdmProjectJobNotifyNum` | 查询产品经理项目工作通知数量 | 体 `RdmsHmiIdsDto` | `Integer` | JOB_ITEM:VIEW |

## 职级管理 JobLevelController

源码：`customer/src/main/java/com/course/customer/controller/JobLevelController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/customer/job-level/list-all/{customerId}` | 查询全部列表 | — | `List<RdmsCustomerUserJobLevelDto>` | JOB_LEVEL:VIEW |
| POST | `/customer/job-level/glance` | 概览 | 体 `RdmsCustomerUserJobLevelDto` | `Integer` | JOB_LEVEL:UPDATE |
| POST | `/customer/job-level/save` | 保存职级 | 体 `RdmsCustomerUserJobLevelDto` | `RdmsCustomerUserJobLevelDto` | JOB_LEVEL:CREATE |
| DELETE | `/customer/job-level/del/{id}` | 删除职级 | — | — | JOB_LEVEL:DELETE |

## 工单资产 JobItemPropertyController

源码：`customer/src/main/java/com/course/customer/controller/JobItemPropertyController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/customer/jobitem-property/getJobItemProperty` | 查询工单资产 | 参 `jobitemId` | `RdmsJobItemPropertyDto` | JOB_ITEM:VIEW |
| POST | `/customer/jobitem-property/save` | 保存工单资产 | 体 `RdmsJobItemPropertyDto` | `RdmsJobItemPropertyDto` | JOB_ITEM:CREATE |
| POST | `/customer/jobitem-property/saveJobitemProperty` | 保存工单资产 | 体 `RdmsJobItemPropertyDto` | `RdmsJobItemPropertyDto` | JOB_ITEM:CREATE |
| POST | `/customer/jobitem-property/submitJobitemProperty` | 提交工单资产 | 体 `RdmsJobItemPropertyDto` | — | JOB_ITEM:UPDATE |
| POST | `/customer/jobitem-property/submitReviewResult` | 提交评审结果 | 体 `RdmsJobItemPropertyDto` | — | JOB_ITEM:UPDATE |
| POST | `/customer/jobitem-property/submitJobitemTestResult` | 提交工单测试结果 | 体 `RdmsJobItemPropertyDto` | — | JOB_ITEM:UPDATE |
| DELETE | `/customer/jobitem-property/del/{id}` | 删除工单资产 | — | — | JOB_ITEM:DELETE |

## 内部邮件 MailController

源码：`customer/src/main/java/com/course/customer/controller/MailController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| GET | `/customer/mail/account/getSelf` | 查询本人 | — | `MailAccountDto` | MAIL:VIEW |
| POST | `/customer/mail/account/update` | 修改账号 | 体 `Map<String, String>` | `Boolean` | MAIL:UPDATE |
| POST | `/customer/mail/contact/list` | 联系人 | 体 `Map<String, String>` | `Map<String, Object>` | MAIL:VIEW |
| POST | `/customer/mail/box/list` | 查询内部邮件列表 | 体 `MailListQuery` | `MailPage<MailBoxItemDto>` | MAIL:VIEW |
| GET | `/customer/mail/box/count` | 统计内部邮件 | — | `Map<String, Integer>` | MAIL:VIEW |
| GET | `/customer/mail/message/get/{boxId}` | 查询内部邮件 | — | `MailDetailDto` | MAIL:VIEW |
| POST | `/customer/mail/draft/save` | 保存草稿 | 体 `MailSendDto` | `Map<String, String>` | MAIL:CREATE |
| POST | `/customer/mail/message/send` | 发送内部邮件 | 体 `MailSendDto` | `Map<String, String>` | MAIL:UPDATE |
| POST | `/customer/mail/box/mark` | 标记内部邮件 | 体 `MailBoxOpDto` | `Integer` | MAIL:UPDATE |
| POST | `/customer/mail/box/move` | 移动内部邮件 | 体 `MailBoxOpDto` | `Integer` | MAIL:UPDATE |
| POST | `/customer/mail/box/delete` | 删除内部邮件 | 体 `MailBoxOpDto` | `Integer` | MAIL:DELETE |
| POST | `/customer/mail/attachable/list` | 可关联 | 体 `Map<String, Object>` | `MailPage<MailAttachableDto>` | MAIL:VIEW |
| POST | `/customer/mail/attachable/rich-version/list` | 富文本版本 | 体 `Map<String, Object>` | `MailPage<MailRichVersionDto>` | MAIL:VIEW |
| POST | `/customer/mail/attachment/signed-url` | 签名地址 | 体 `Map<String, Object>` | `String` | MAIL:VIEW |

## 工时申报 ManhourDeclareController

源码：`customer/src/main/java/com/course/customer/controller/ManhourDeclareController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/customer/manhour-declare/save` | 保存工时申报 | 体 `RdmsManhourDeclareDto` | `String` | MANHOUR_DECLARE:CREATE |
| DELETE | `/customer/manhour-declare/del/{id}` | 删除工时申报 | — | — | MANHOUR_DECLARE:DELETE |

## 工时 ManhourController

源码：`customer/src/main/java/com/course/customer/controller/ManhourController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/customer/manhour/list` | 查询工时列表 | 体 `PageDto<RdmsManhourStandardDto>` | `PageDto<RdmsManhourStandardDto>` | MANHOUR:VIEW |
| POST | `/customer/manhour/get-standard/{customerId}` | 查询标准工时 | — | `RdmsManhourStandardDto` | MANHOUR:VIEW |
| POST | `/customer/manhour/getManhourFeeByWorkerId/{workerId}` | 按成员ID查询工时费用 | — | `String` | MANHOUR:VIEW |
| POST | `/customer/manhour/getManhourSummaryByCharacterId/{characterId}` | 按特性ID查询工时汇总 | — | `List<RdmsHmiWorkerManhourSummaryDto>` | MANHOUR:VIEW |
| POST | `/customer/manhour/save` | 保存工时 | 体 `RdmsManhourStandardDto` | `RdmsManhourStandardDto` | MANHOUR:CREATE |
| DELETE | `/customer/manhour/del/{id}` | 删除工时 | — | — | MANHOUR:DELETE |

## 物料库存 MaterialStockController

源码：`customer/src/main/java/com/course/customer/controller/MaterialStockController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/customer/material-stock/list/{customerId}` | 查询物料库存列表 | — | `List<RdmsMaterialStockDto>` | INVENTORY:VIEW |
| POST | `/customer/material-stock/inbound` | 入库 | 体 `RdmsMaterialStockDto` | `String` | INVENTORY:UPDATE |
| POST | `/customer/material-stock/inbound-batch` | 路径不得含 /import：网关会推断为 IMPORT 动作，而库存模块只授予增删改查与审批 | 体 `RdmsMaterialStockImportDto` | `String` | INVENTORY:UPDATE |
| POST | `/customer/material-stock/adjust` | 调整物料库存 | 体 `RdmsMaterialStockDto` | `String` | INVENTORY:UPDATE |
| POST | `/customer/material-stock/logs` | 日志 | 体 `RdmsMaterialStockLogQueryDto` | `List<RdmsMaterialStockLogDto>` | INVENTORY:UPDATE |

## 物料仓库 MaterialWarehouseController

源码：`customer/src/main/java/com/course/customer/controller/MaterialWarehouseController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/customer/material-warehouse/list/{customerId}` | 查询物料仓库列表 | — | `List<RdmsMaterialWarehouse>` | WAREHOUSE:VIEW |
| POST | `/customer/material-warehouse/save` | 保存物料仓库 | 体 `RdmsMaterialWarehouse` | `String` | WAREHOUSE:CREATE |
| POST | `/customer/material-warehouse/update-status` | 修改状态 | 体 `RdmsMaterialWarehouse` | `String` | WAREHOUSE:UPDATE |

## 物料管理 MaterialController

源码：`customer/src/main/java/com/course/customer/controller/MaterialController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/customer/material/saveMaterialApplication` | 保存物料申请 | 体 `RdmsMaterialManageDto` | `String` | MATERIAL:CREATE |
| POST | `/customer/material/saveMaterialApplication_return` | 保存物料申请（归还） | 体 `RdmsMaterialManageDto` | `String` | MATERIAL:CREATE |
| POST | `/customer/material/saveMaterialListToBom` | 保存物料列表BOM | 体 `RdmsHmiMaterialToBomDto` | `String` | MATERIAL:CREATE |
| POST | `/customer/material/saveBomTree` | 保存BOM树 | 体 `List<RdmsBomTreeDto>` | `String` | MATERIAL:CREATE |
| POST | `/customer/material/saveBomTree_main` | 保存BOM树（主） | 体 `List<RdmsBomTreeDto>` | `String` | MATERIAL:CREATE |
| POST | `/customer/material/getBomTree/{subprojectId}` | 查询BOM树 | — | `RdmsBomTreeDto` | MATERIAL:VIEW |
| POST | `/customer/material/getBomTree_main/{projectId}` | 查询BOM树（主） | — | `RdmsBomTreeDto` | MATERIAL:VIEW |
| POST | `/customer/material/synchronizationBomTree/{mainProjectId}` | 同步BOM树 | — | `Integer` | MATERIAL:UPDATE |
| POST | `/customer/material/saveMaterialListToBom_subproject` | 保存物料列表BOM（子项目） | 体 `RdmsHmiMaterialToBomDto` | `String` | MATERIAL:CREATE |
| POST | `/customer/material/submitBom/{characterId}` | 提交BOM | — | `String` | MATERIAL:UPDATE |
| POST | `/customer/material/submitSubProjectBom/{subprojectId}` | 提交子项目BOM | — | `String` | MATERIAL:UPDATE |
| POST | `/customer/material/rejectSubProjectBom/{subprojectId}` | 驳回子项目BOM | 体 `RejectReasonDto` | `String` | MATERIAL:UPDATE |
| POST | `/customer/material/rejectBom/{characterId}` | 驳回BOM | 体 `RejectReasonDto` | `String` | MATERIAL:UPDATE |
| POST | `/customer/material/saveBomItem` | 保存BOM条目 | 体 `RdmsBomDto` | `String` | MATERIAL:CREATE |
| POST | `/customer/material/saveEditedBomItem` | 保存已编辑BOM条目 | 体 `RdmsBomDto` | `String` | MATERIAL:CREATE |
| POST | `/customer/material/saveBomItem_subproject` | 保存BOM条目（子项目） | 体 `RdmsBomDto` | `String` | MATERIAL:CREATE |
| POST | `/customer/material/saveBomItem_subproject_import` | 保存BOM条目（子项目导入） | 体 `List<RdmsBomDto>` | `String` | MATERIAL:CREATE |
| POST | `/customer/material/approveMaterialApplication` | 批准物料申请 | 体 `RdmsMaterialManageDto` | `String` | MATERIAL:APPROVE |
| POST | `/customer/material/approveMaterialApplication-sap` | 按SAP批准物料申请 | 体 `RdmsMaterialManageDto` | `String` | MATERIAL:APPROVE |
| POST | `/customer/material/issueMaterialApplication` | 发料确认：物料管理员按批准的明细发料扣库存，或退回申请人。最终管理人可代办 | 体 `RdmsMaterialManageDto` | `String` | MATERIAL:UPDATE |
| POST | `/customer/material/receiveReturnApplication` | 退料收料确认：物料管理员按退料明细收料入库，或驳回退料。最终管理人可代办。结论与说明沿用 issueResult、issueDescription | 体 `RdmsMaterialManageDto` | `String` | MATERIAL:UPDATE |
| POST | `/customer/material/listMaterialsByCharacterId` | 按特性ID查询物料列表 | 体 `PageDto<RdmsMaterialManageDto>` | `PageDto<RdmsMaterialManageDto>` | MATERIAL:VIEW |
| POST | `/customer/material/getQualityMaterialApplicationNumByUserId/{userId}` | 按用户ID查询质量物料申请数量 | — | `Long` | MATERIAL:VIEW |
| POST | `/customer/material/getListByCharacterId/{characterId}` | 按特性ID查询物料列表 | — | `List<RdmsMaterialManageDto>` | MATERIAL:VIEW |
| POST | `/customer/material/getListByNextNodeIdForApprover/{loginUser}` | 按下一节点ID审批人查询物料列表 | — | `List<RdmsMaterialManageDto>` | MATERIAL:VIEW |
| POST | `/customer/material/getListByQualityId/{qualityId}` | 按质量ID查询物料列表 | — | `List<RdmsMaterialManageDto>` | MATERIAL:VIEW |
| POST | `/customer/material/getListBySubprojectId/{subprojectId}` | 按子项目ID查询物料列表 | — | `List<RdmsMaterialManageDto>` | MATERIAL:VIEW |
| POST | `/customer/material/getMaterialListByApplicationCode/{code}` | 按申请编码查询物料列表 | — | `List<RdmsMaterialDto>` | MATERIAL:VIEW |
| POST | `/customer/material/getListByPreProjectId/{preprojectId}` | 按预立项ID查询物料列表 | — | `List<RdmsMaterialManageDto>` | MATERIAL:VIEW |
| POST | `/customer/material/getListByProjectId/{projectId}` | 按项目ID查询物料列表 | — | `List<RdmsMaterialManageDto>` | MATERIAL:VIEW |
| POST | `/customer/material/getBomMaterialListByCharacterId/{characterId}` | 按特性ID查询BOM物料列表 | — | `List<RdmsBomDto>` | MATERIAL:VIEW |
| GET | `/customer/material/bom-snapshot/list/{ownerType}/{ownerId}` | 物料清单的各版快照，ownerType 为 CHARACTER 或 SUBPROJECT；不含清单行 | — | `List<Map<String, Object>>` | MATERIAL:VIEW |
| GET | `/customer/material/bom-snapshot/get/{id}` | 查询BOM快照 | — | `Map<String, Object>` | MATERIAL:VIEW |
| POST | `/customer/material/stock/list/{customerId}` | 查询库存列表 | — | `List<RdmsMaterialStockDto>` | MATERIAL:VIEW |
| POST | `/customer/material/active-warehouses/{customerId}` | 退料选择目标仓库：本机构启用仓库 | — | `List<RdmsMaterialWarehouse>` | MATERIAL:UPDATE |
| POST | `/customer/material/getSAPMaterialList/{customerId}` | 领料选料：只列启用仓库中数量大于 0 的物料 | — | `List<RdmsSAPMaterialDto>` | MATERIAL:VIEW |
| POST | `/customer/material/getSAPMaterialByBomCodeAndBatchCode` | 按BOM编码和批量编码查询SAP物料 | 参 `bomCode` | `List<RdmsSAPMaterialDto>` | MATERIAL:VIEW |
| POST | `/customer/material/getBomMaterialListBySubprojectId/{subprojectId}` | 按子项目ID查询BOM物料列表 | — | `List<RdmsBomDto>` | MATERIAL:VIEW |
| POST | `/customer/material/getCommonBomMaterialListByProjectId/{projectId}` | 按项目ID查询通用BOM物料列表 | — | `List<RdmsBomDto>` | MATERIAL:VIEW |
| POST | `/customer/material/getArchivedCharacterListByProjectId/{projectId}` | 按项目ID查询已归档特性列表 | — | `List<RdmsCharacterDto>` | MATERIAL:VIEW |
| POST | `/customer/material/getBomMaterialListByProjectId/{projectId}` | 按项目ID查询BOM物料列表 | — | `List<RdmsBomDto>` | MATERIAL:VIEW |
| POST | `/customer/material/getBomMaterialListByCharacterIdList` | 按特性ID列表查询BOM物料列表 | 体 `List<String>` | `List<RdmsBomDto>` | MATERIAL:VIEW |
| POST | `/customer/material/getMaterialListByProjectId/{projectId}` | 按项目ID查询物料列表 | — | `RdmsBomConfigDto` | MATERIAL:VIEW |
| POST | `/customer/material/getMaterialManageItemByCode/{code}` | 按编码查询物料管理条目 | — | `RdmsMaterialManageDto` | MATERIAL:VIEW |
| POST | `/customer/material/getMaterialListById/{bomConfigId}` | 按ID查询物料列表 | — | `RdmsBomConfigDto` | MATERIAL:VIEW |
| POST | `/customer/material/getCapitalizationListByCharacterId/{characterId}` | 按特性ID查询资本化列表 | — | `List<RdmsMaterialDto>` | MATERIAL:VIEW |
| POST | `/customer/material/getCapitalizationListBySubprojectId/{subprojectId}` | 按子项目ID查询资本化列表 | — | `List<RdmsMaterialDto>` | MATERIAL:VIEW |
| POST | `/customer/material/getCapitalizationSummaryListByCharacterId/{characterId}` | 按特性ID查询资本化汇总列表 | — | `List<RdmsMaterialDto>` | MATERIAL:VIEW |
| POST | `/customer/material/getBorrowListByCharacterId/{characterId}` | 按特性ID查询借用列表 | — | `List<RdmsMaterialDto>` | MATERIAL:VIEW |
| POST | `/customer/material/getBorrowListBySubprojectId/{subprojectId}` | 按子项目ID查询借用列表 | — | `List<RdmsMaterialDto>` | MATERIAL:VIEW |
| POST | `/customer/material/getBorrowListByCustomerId/{customerId}` | 按机构ID查询借用列表 | — | `List<RdmsMaterialDto>` | MATERIAL:VIEW |
| POST | `/customer/material/getExpenditureListByCustomerId/{customerId}` | 按机构ID查询支出列表 | — | `List<RdmsMaterialDto>` | MATERIAL:VIEW |
| POST | `/customer/material/getCapitalizationListByCustomerId/{customerId}` | 按机构ID查询资本化列表 | — | `List<RdmsMaterialDto>` | MATERIAL:VIEW |
| POST | `/customer/material/getBorrowSummaryListByCharacterId/{characterId}` | 按特性ID查询借用汇总列表 | — | `List<RdmsMaterialDto>` | MATERIAL:VIEW |
| POST | `/customer/material/getExpenditureListByCharacterId/{characterId}` | 按特性ID查询支出列表 | — | `List<RdmsMaterialDto>` | MATERIAL:VIEW |
| POST | `/customer/material/getExpenditureListBySubProjectId/{subprojectId}` | 按子项目ID查询支出列表 | — | `List<RdmsMaterialDto>` | MATERIAL:VIEW |
| POST | `/customer/material/getExpenditureSummaryListByCharacterId/{characterId}` | 按特性ID查询支出汇总列表 | — | `List<RdmsMaterialDto>` | MATERIAL:VIEW |
| POST | `/customer/material/getMaterialListByCharacterId/{characterId}` | 按特性ID查询物料列表 | — | `List<RdmsMaterialDto>` | MATERIAL:VIEW |
| POST | `/customer/material/getMaterialListForBomSelected/{characterId}` | 查询物料列表BOM已选 | — | `List<RdmsMaterialDto>` | MATERIAL:VIEW |
| POST | `/customer/material/getDirectMaterialListForBomSelected/{subprojectId}` | 查询直接物料列表BOM已选 | — | `List<RdmsMaterialDto>` | MATERIAL:VIEW |
| POST | `/customer/material/getMaterialApplicationListByWriterId/{writerId}` | 按编写人ID查询物料申请列表 | — | `List<RdmsMaterialManageDto>` | MATERIAL:VIEW |
| POST | `/customer/material/getMaterialApplicationNum/{userId}` | 查询物料申请数量 | — | `Integer` | MATERIAL:VIEW |
| POST | `/customer/material/getMaterialApplicationNum_project/{userId}` | 查询物料申请数量（项目） | — | `Integer` | MATERIAL:VIEW |
| POST | `/customer/material/getMaterialApplicationByCode/{code}` | 按编码查询物料申请 | — | `RdmsMaterialManageDto` | MATERIAL:VIEW |
| POST | `/customer/material/confirmCancel/{code}` | 确认取消 | — | `String` | MATERIAL:UPDATE |
| POST | `/customer/material/updateMaterialApproveFileListStr` | 修改物料批准文件列表字符串 | 体 `RdmsMaterialManageDto` | `String` | MATERIAL:UPDATE |
| POST | `/customer/material/updateMaterialDealWithFileListStr` | 修改物料处理含文件列表字符串 | 体 `RdmsMaterialManageDto` | `String` | MATERIAL:UPDATE |
| POST | `/customer/material/updateMaterialCompleteFileListStr` | 修改物料已完成文件列表字符串 | 体 `RdmsMaterialManageDto` | `String` | MATERIAL:UPDATE |
| POST | `/customer/material/submit` | 提交物料 | 体 `RdmsMaterialManageDto` | `String` | MATERIAL:UPDATE |
| DELETE | `/customer/material/delete/{id}` | 删除BOM条目 | — | — | MATERIAL:DELETE |
| POST | `/customer/material/saveCbbToBom/{characterId}/{cbbId}` | 物料清单带入软件模块（CBB）行：登记 CBB 标识、名称与版本号，不要求供应商与单价 | — | `String` | MATERIAL:CREATE |

## 里程碑 MilestoneController

源码：`customer/src/main/java/com/course/customer/controller/MilestoneController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/customer/milestone/listByProjectId/{projectId}` | 查询里程碑列表 | — | `List<RdmsMilestoneDto>` | MILESTONE:VIEW |
| POST | `/customer/milestone/customer-milestone-list` | 按机构ID查询已立项项目里程碑列表 | 体 `PageDto<RdmsMilestoneDto>` | `PageDto<RdmsMilestoneDto>` | MILESTONE:UPDATE |
| POST | `/customer/milestone/sendMilestoneTask` | 发送里程碑任务 | 体 `RdmsJobItemDto` | `Integer` | MILESTONE:UPDATE |
| POST | `/customer/milestone/save` | 保存里程碑 | 体 `RdmsMilestoneDto` | `RdmsMilestoneDto` | MILESTONE:CREATE |
| POST | `/customer/milestone/edit` | 编辑里程碑 | 体 `RdmsMilestoneDto` | `RdmsMilestoneDto` | MILESTONE:UPDATE |
| DELETE | `/customer/milestone/del/{id}` | 删除里程碑 | — | — | MILESTONE:DELETE |

## 通知角标 NotifyController

源码：`customer/src/main/java/com/course/customer/controller/NotifyController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| GET | `/customer/notify/badge-summary/{userId}` | 侧栏角标一次聚合：字段名对齐 admin-v2 taskCounts / taskFlags | — | `RdmsBadgeSummaryDto` | MEMBER:VIEW |

## 产品总监与系统工程总监 DirectorController

源码：`customer/src/main/java/com/course/customer/controller/DirectorController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/customer/pdgm-sysgm/getPdgmAndSysgm/{customerId}` | 查询产品总监与系统工程总监列表 | — | `RdmsDirectorDto` | DIRECTOR:VIEW |
| POST | `/customer/pdgm-sysgm/savePdgm` | 保存产品总监 | 体 `RdmsDirectorDto` | `String` | DIRECTOR:CREATE |
| POST | `/customer/pdgm-sysgm/saveSysgm` | 保存系统工程总监 | 体 `RdmsDirectorDto` | `String` | DIRECTOR:CREATE |
| POST | `/customer/pdgm-sysgm/saveFggm` | 保存法规总监 | 体 `RdmsDirectorDto` | `String` | DIRECTOR:CREATE |
| POST | `/customer/pdgm-sysgm/saveQcgm` | 保存质量总监 | 体 `RdmsDirectorDto` | `String` | DIRECTOR:CREATE |
| POST | `/customer/pdgm-sysgm/saveTgm` | 保存测试总监 | 体 `RdmsDirectorDto` | `String` | DIRECTOR:CREATE |

## 绩效管理 PerformanceController

源码：`customer/src/main/java/com/course/customer/controller/PerformanceController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/customer/performance/list` | 查询绩效列表 | 体 `PerformancePageDto` | `PageDto<RdmsPerformanceDto>` | PERFORMANCE:VIEW |
| POST | `/customer/performance/getCurrentPerformanceByLoginUser/{customerUserId}` | 按登录用户查询当前绩效 | — | `RdmsPerformanceDto` | PERFORMANCE:VIEW |
| POST | `/customer/performance/getCurrentDetail/{customerUserId}` | 当月绩效计入工单明细：本人或范围内人员 | — | `PerformanceDetailDto` | PERFORMANCE:VIEW |
| POST | `/customer/performance/listPerformance` | 查询绩效列表 | 参 `customerUserId` | `List<RdmsPerformanceDto>` | PERFORMANCE:VIEW |
| POST | `/customer/performance/getMonthlyReport` | 绩效月报：body.yearMonth 为 yyyy-MM；返回明细、部门小计（含吸收率）与总计 | 体 `Map<String, String>` | `PerformanceMonthlyReportDto` | PERFORMANCE:VIEW |
| POST | `/customer/performance/createPerformanceRecord/{customerUserId}` | 新建绩效记录 | — | `RdmsPerformanceDto` | PERFORMANCE:CREATE |
| POST | `/customer/performance/getPerformanceListByUserId/{customerUserId}` | 按机构用户ID查询绩效列表 | — | `List<RdmsPerformanceDto>` | PERFORMANCE:VIEW |

## 预立项管理 PreProjectController

源码：`customer/src/main/java/com/course/customer/controller/PreProjectController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/customer/pre-project/list` | 查询预立项列表 | 体 `PageDto<RdmsPreProjectDto>` | `PageDto<RdmsPreProjectDto>` | PRE_PROJECT:VIEW |
| POST | `/customer/pre-project/getPreProjectAndTaskJobitems_submit/{preProjectId}` | 查询预立项和任务工单（提交） | — | `RdmsHmiProjectAndJobItemMixDto` | PRE_PROJECT:VIEW |
| POST | `/customer/pre-project/getPreProjectHasListFlag/{userId}` | 查询预立项有列表标志 | — | `Boolean` | PRE_PROJECT:VIEW |
| POST | `/customer/pre-project/getPreProjectListBySysgm/{customerId}` | 按系统工程总监查询预立项列表 | — | `List<RdmsPreProjectDto>` | PRE_PROJECT:VIEW |
| POST | `/customer/pre-project/getPreProjectListByPdm` | 按产品经理查询预立项列表 | 参 `pdmId` | `List<RdmsPreProjectDto>` | PRE_PROJECT:VIEW |
| POST | `/customer/pre-project/getPreProjectListBySm` | 按系统工程师查询预立项列表 | 参 `customerId` | `List<RdmsPreProjectDto>` | PRE_PROJECT:VIEW |
| POST | `/customer/pre-project/list/{customerId}` | 按机构ID查询预立项列表 | — | `List<RdmsPreProjectDto>` | PRE_PROJECT:VIEW |
| POST | `/customer/pre-project/getPreProjectSimpleInfo/{preProjectId}` | 查询预立项简要信息 | — | `RdmsPreProjectDto` | PRE_PROJECT:VIEW |
| POST | `/customer/pre-project/getPreProjectAndTaskJobitems/{preProjectId}` | 查询预立项和任务工单 | — | `RdmsHmiProjectAndJobItemMixDto` | PRE_PROJECT:VIEW |
| POST | `/customer/pre-project/getModulePreProject/{customerId}` | 查询模块预立项 | — | `RdmsPreProjectDto` | PRE_PROJECT:VIEW |
| POST | `/customer/pre-project/getCharacterJobNotify` | 查询特性工作通知 | 体 `RdmsHmiNotifyDto` | `RdmsHmiNotifyDto` | PRE_PROJECT:VIEW |
| POST | `/customer/pre-project/getDemandJobNotify` | 查询需求工作通知 | 体 `RdmsHmiNotifyDto` | `RdmsHmiNotifyDto` | PRE_PROJECT:VIEW |
| POST | `/customer/pre-project/save` | 保存预立项 | 体 `RdmsPreProjectDto` | `RdmsPreProjectDto` | PRE_PROJECT:CREATE |
| DELETE | `/customer/pre-project/del/{id}` | 删除预立项 | — | — | PRE_PROJECT:DELETE |
| POST | `/customer/pre-project/close/{id}` | 关闭预立项 | 参 `false` | `String` | PRE_PROJECT:UPDATE |
| GET | `/customer/pre-project/product/list` | 所属产品候选：本机构未删除、未停产的产品，供立项、预立项选择所属产品 | — | `List<RdmsProduct>` | PRE_PROJECT:VIEW |

## 产品经理 ProductManagerController

源码：`customer/src/main/java/com/course/customer/controller/ProductManagerController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/customer/product-manager/list` | 查询产品经理列表 | 体 `PageDto<RdmsProductManagerDto>` | `PageDto<RdmsProductManagerDto>` | PRODUCT_MANAGER:VIEW |
| POST | `/customer/product-manager/listByCustomerId/{customerId}` | 按机构ID查询产品经理列表 | — | `List<String>` | PRODUCT_MANAGER:VIEW |
| POST | `/customer/product-manager/getListByCustomerId/{customerId}` | 按机构ID查询产品经理列表 | — | `List<RdmsCustomerUserDto>` | PRODUCT_MANAGER:VIEW |
| POST | `/customer/product-manager/save` | 保存产品经理 | 体 `RdmsProductManagerDto` | `RdmsProductManagerDto` | PRODUCT_MANAGER:CREATE |
| DELETE | `/customer/product-manager/del/{id}` | 删除产品经理 | — | — | PRODUCT_MANAGER:DELETE |

## 产品管理 ProductController

源码：`customer/src/main/java/com/course/customer/controller/ProductController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/customer/product/list` | 查询产品列表 | 体 `PageDto<RdmsProductDto>` | `PageDto<RdmsProductDto>` | PRODUCT:VIEW |
| POST | `/customer/product/save` | 保存产品 | 体 `RdmsProductDto` | `RdmsProductDto` | PRODUCT:CREATE |
| DELETE | `/customer/product/del/{id}` | 删除产品 | — | — | PRODUCT:DELETE |
| POST | `/customer/product/publish-from-project` | 从已完成项目发布产品（RDMS 台账 ACTIVE），同时生成发布基线 | 体 `Map<String, String>` | `RdmsProductDto` | PRODUCT:UPDATE |
| POST | `/customer/product/release/list` | 产品的发布版本，按发布时间倒序。body: productId 必填 | 体 `Map<String, String>` | `List<RdmsProductReleaseDto>` | PRODUCT:VIEW |
| POST | `/customer/product/release/eol` | 发布版本停产 ACTIVE→EOL；产品下全部停产后产品写 EOL。body: id 必填 | 体 `Map<String, String>` | `RdmsProductRelease` | PRODUCT:UPDATE |
| POST | `/customer/product/eol` | 产品停产：停产其全部发布版本，产品写 EOL。body: id 必填 | 体 `Map<String, String>` | `RdmsProductDto` | PRODUCT:UPDATE |

## 项目经理 ProjectManagerController

源码：`customer/src/main/java/com/course/customer/controller/ProjectManagerController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/customer/project-manager/list` | 查询项目经理列表 | 体 `PageDto<RdmsProjectManagerDto>` | `PageDto<RdmsProjectManagerDto>` | DIRECTOR:VIEW |
| POST | `/customer/project-manager/listByCustomerId/{customerId}` | 查询项目经理列表 | — | `List<String>` | DIRECTOR:VIEW |
| POST | `/customer/project-manager/getProjectManagerList/{customerId}` | 查询项目经理列表 | — | `List<RdmsCustomerUserDto>` | DIRECTOR:VIEW |
| POST | `/customer/project-manager/save` | 保存项目经理 | 体 `RdmsProjectManagerDto` | `RdmsProjectManagerDto` | DIRECTOR:CREATE |
| DELETE | `/customer/project-manager/del/{id}` | 删除项目经理 | — | — | DIRECTOR:DELETE |

## 保密等级 ProjectSecLevelController

源码：`customer/src/main/java/com/course/customer/controller/ProjectSecLevelController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/customer/project-sec-level/save` | 保存保密等级 | 体 `RdmsProjectSecLevelDto` | `RdmsProjectSecLevelDto` | PROJECT:CREATE |
| POST | `/customer/project-sec-level/get-by-projectId/{projectId}` | 按项目ID查询保密等级 | — | `List<RdmsProjectSecLevel>` | PROJECT:VIEW |
| DELETE | `/customer/project-sec-level/del/{id}` | 删除保密等级 | — | — | PROJECT:DELETE |

## 项目任务 ProjectTaskController

源码：`customer/src/main/java/com/course/customer/controller/ProjectTaskController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/customer/project-task/list` | 查询项目任务列表 | 体 `PageDto<RdmsProjectTaskDto>` | `PageDto<RdmsProjectTaskDto>` | PROJECT_TASK:VIEW |
| POST | `/customer/project-task/save` | 保存项目任务 | 体 `RdmsProjectTaskDto` | `RdmsProjectTaskDto` | PROJECT_TASK:CREATE |
| DELETE | `/customer/project-task/del/{id}` | 删除项目任务 | — | — | PROJECT_TASK:DELETE |

## 项目管理 ProjectController

源码：`customer/src/main/java/com/course/customer/controller/ProjectController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/customer/project/list` | 列表查询已经立项的项目 | 体 `PageDto<RdmsProjectDto>` | `PageDto<RdmsProjectDto>` | PROJECT:VIEW |
| POST | `/customer/project/list-setup-project` | 查询立项项目列表 | 参 `customerId` | `List<RdmsProjectDto>` | PROJECT:VIEW |
| POST | `/customer/project/list-setuped-project` | 查询已立项项目列表 | 体 `PageDto<RdmsProjectDto>` | `PageDto<RdmsProjectDto>` | PROJECT:VIEW |
| POST | `/customer/project/getSetupedProjectListByTm` | 按测试主管查询已立项项目列表 | 参 `customerId` | `List<RdmsProjectDto>` | PROJECT:VIEW |
| POST | `/customer/project/getTestedSubprojectListByTgm` | 按测试总监查询已测试子项目列表 | 参 `customerId` | `List<RdmsProjectSubprojectDto>` | PROJECT:VIEW |
| POST | `/customer/project/getTestedSubprojectListByTm` | 按测试主管查询已测试子项目列表 | 体 `PageDto<RdmsProjectSubprojectDto>` | `PageDto<RdmsProjectSubprojectDto>` | PROJECT:VIEW |
| POST | `/customer/project/getCompleteProjectListByCustomerId/{customerId}` | 按机构ID查询已完成项目列表 | — | `List<RdmsProjectDto>` | PROJECT:VIEW |
| POST | `/customer/project/countSetupProjectBySupervise/{userId}` | 按督办统计立项项目 | — | `Integer` | PROJECT:VIEW |
| POST | `/customer/project/getSubprojectListAndAllTaskJobitems/{projectId}` | 查询子项目列表和全部任务工单 | — | `List<RdmsHmiSubprojectAndJobItemMixDto>` | PROJECT:VIEW |
| POST | `/customer/project/getProjectTeamIdsByProjectId/{projectId}` | 按项目ID查询项目团队ID 列表 | — | `RdmProjectTeamInfoDto` | PROJECT:VIEW |
| POST | `/customer/project/getSubProjectByPrimaryKey/{subprojectId}` | 按主键查询子项目 | — | `RdmsProjectSubprojectDto` | PROJECT:VIEW |
| POST | `/customer/project/getProjectAndTaskJobitems/{projectId}` | 查询项目和任务工单 | — | `RdmsHmiProjectAndJobItemMixDto` | PROJECT:VIEW |
| POST | `/customer/project/getSubProjectAndTaskJobitems/{subProjectId}` | 查询子项目和任务工单 | — | `RdmsHmiProjectAndJobItemMixDto` | PROJECT:VIEW |
| POST | `/customer/project/getProjectReviewDetailInfo` | 查询项目评审详情信息 | 参 `projectId` | `RdmsHmiProjectReviewDetailDto` | PROJECT:VIEW |
| POST | `/customer/project/getSubProjectAndTestJobitems/{subprojectId}` | 查询子项目和测试工单 | — | `RdmsHmiProjectAndJobItemMixDto` | PROJECT:VIEW |
| POST | `/customer/project/getSubProjectAndTestJobitems_submit/{subprojectId}` | 查询子项目和测试工单（提交） | — | `RdmsHmiProjectAndJobItemMixDto` | PROJECT:VIEW |
| POST | `/customer/project/getProjectAndTaskJobitems_submit/{projectId}` | 查询项目和任务工单（提交） | — | `RdmsHmiProjectAndJobItemMixDto` | PROJECT:VIEW |
| POST | `/customer/project/getProjectHasListFlag/{userId}` | 查询项目有列表标志 | — | `Boolean` | PROJECT:VIEW |
| POST | `/customer/project/getProductHasListFlag/{userId}` | 查询产品有列表标志 | — | `Boolean` | PROJECT:VIEW |
| POST | `/customer/project/getSetupProjectHasListFlag/{userId}` | 查询立项项目有列表标志 | — | `Boolean` | PROJECT:VIEW |
| POST | `/customer/project/getSubProjectAndTaskJobitems_submit/{subProjectId}` | 查询子项目和任务工单（提交） | — | `RdmsHmiProjectAndJobItemMixDto` | PROJECT:VIEW |
| POST | `/customer/project/getSubprojectListByProjectId/{projectId}` | 按项目ID查询子项目列表 | — | `List<RdmsProjectSubprojectDto>` | PROJECT:VIEW |
| POST | `/customer/project/getProjectDevelopCompleteStatus/{projectId}` | 查询项目开发已完成状态 | — | `Boolean` | PROJECT:VIEW |
| POST | `/customer/project/getIdsDtoByProjectIdAndType` | 这里的projectID是个通用的说法, 需要通过projectType确定到底是什么类型的项目id | 体 `RdmsHmiProjectIdAndTypeDto` | `RdmsHmiIdsDto` | PROJECT:VIEW |
| POST | `/customer/project/joinToMainProject` | 加入主项目 | 体 `RdmsProjectDto` | `String` | PROJECT:UPDATE |
| POST | `/customer/project/getCharacterTreeByProjectIdWithLink/{projectId}` | 按项目ID含关联查询特性树 | — | `List<RdmsHmiTree<RdmsCharacterSimpleDto>>` | PROJECT:VIEW |
| POST | `/customer/project/getArchivedProjectList` | 查询已归档项目列表 | 体 `RdmsRequestDto` | `List<RdmsProjectDto>` | PROJECT:VIEW |
| POST | `/customer/project/getAllProjectArchivedList/{loginCustomerId}` | 查询全部项目已归档列表 | — | `List<RdmsProjectDto>` | PROJECT:VIEW |
| POST | `/customer/project/getAllProjectArchivedListByStaff/{loginCustomerId}` | 按员工查询全部项目已归档列表 | — | `List<RdmsProjectDto>` | PROJECT:VIEW |
| POST | `/customer/project/getDevelopingProjectList` | 查询开发中项目列表 | 体 `RdmsRequestDto` | `List<RdmsProjectDto>` | PROJECT:VIEW |
| POST | `/customer/project/getProjectInfo/{projectId}` | 查询项目信息 | — | `RdmsProjectDto` | PROJECT:VIEW |
| POST | `/customer/project/getProjectSimpleInfo/{projectId}` | 查询项目简要信息 | — | `RdmsProjectDto` | PROJECT:VIEW |
| POST | `/customer/project/getProjectDetailInfo` | 查询项目详情信息 | 参 `projectId` | `RdmsProjectDto` | PROJECT:VIEW |
| POST | `/customer/project/getProjectAndArchivedCharacters/{projectId}` | 查询项目和已归档特性 | — | `RdmsProjectDto` | PROJECT:VIEW |
| POST | `/customer/project/getBomConfigListByProjectId/{projectId}` | 按项目ID查询BOM配置列表 | — | `List<RdmsBomConfigDto>` | PROJECT:VIEW |
| POST | `/customer/project/getProjectInfoBySubprojectId/{subprojectId}` | 按子项目ID查询项目信息 | — | `RdmsProjectDto` | PROJECT:VIEW |
| POST | `/customer/project/listIpmtByCustomerId/{customerId}` | 按机构ID查询IPMT列表 | — | `List<RdmsCustomerUser>` | PROJECT:VIEW |
| POST | `/customer/project/listProductManagerByCustomerId/{customerId}` | 按机构ID查询产品经理列表 | — | `List<RdmsCustomerUser>` | PROJECT:VIEW |
| POST | `/customer/project/listProjectManagerByCustomerId/{customerId}` | 按机构ID查询项目经理列表 | — | `List<RdmsCustomerUser>` | PROJECT:VIEW |
| POST | `/customer/project/listSystemManagerByCustomerId/{customerId}` | 按机构ID查询系统经理列表 | — | `List<RdmsCustomerUser>` | PROJECT:VIEW |
| POST | `/customer/project/findKeyMemberIdListByProjectId/{projectId}` | 按项目ID查询键成员ID列表 | — | `List<String>` | PROJECT:UPDATE |
| POST | `/customer/project/save` | 保存项目信息 | 体 `RdmsProjectDto` | `RdmsProjectDto` | PROJECT:CREATE |
| POST | `/customer/project/saveBomConfig` | 保存BOM配置 | 体 `RdmsBomConfigDto` | `String` | PROJECT:CREATE |
| POST | `/customer/project/setup` | 立项 | 体 `RdmsProjectSetupDto` | `RdmsProjectSetupDto` | PROJECT:APPROVE |
| POST | `/customer/project/save-setup-info` | 保存立项信息 | 体 `RdmsProjectSetupDto` | `RdmsProjectSetupDto` | PROJECT:CREATE |
| POST | `/customer/project/save-product` | 保存产品 | 体 `RdmsProjectSetupDto` | `RdmsProjectSetupDto` | PROJECT:CREATE |
| POST | `/customer/project/setup-module-project` | 立项模块项目 | 体 `RdmsProjectSetupDto` | `RdmsProjectSetupDto` | PROJECT:APPROVE |
| POST | `/customer/project/edit` | 立项 | 体 `RdmsProjectDto` | `RdmsProjectDto` | PROJECT:UPDATE |
| POST | `/customer/project/append` | 立项管理“追加”：待批立项申请中的特性并入已立项项目，模块项目申请由 moduleSetupProjectId 指定 | 体 `RdmsProjectSetupDto` | `RdmsProjectSetupDto` | PROJECT:UPDATE |
| POST | `/customer/project/setup-application` | 立项申请 | 体 `RdmsProjectSetupDto` | `RdmsProjectSetupDto` | PROJECT:CREATE |
| POST | `/customer/project/direct-setup` | 直接立项 | 体 `RdmsProjectSetupDto` | `RdmsProjectSetupDto` | PROJECT:CREATE |
| POST | `/customer/project/module-setup` | 模块立项 | 体 `RdmsProjectSetupDto` | `RdmsProjectSetupDto` | PROJECT:CREATE |
| POST | `/customer/project/reject-application` | 驳回申请 | 体 `RdmsProjectSetupDto` | `RdmsProjectSetupDto` | PROJECT:APPROVE |
| POST | `/customer/project/withdrawProject/{projectId}` | 撤回项目 | — | `String` | PROJECT:CREATE |
| POST | `/customer/project/append-characters` | 追加特性 | 体 `RdmsProjectSetupDto` | `RdmsProjectSetupDto` | PROJECT:UPDATE |
| POST | `/customer/project/listProjectsByPreProjectId/{preProjectId}` | 按预立项ID查询项目列表 | — | `List<RdmsProjectDto>` | PROJECT:VIEW |
| POST | `/customer/project/getCbbList/{projectId}` | 查询CBB列表 | — | `List<RdmsCharacterDto>` | PROJECT:VIEW |
| POST | `/customer/project/list-setuped-projects/{customerId}` | 查询已立项项目列表 | — | `List<RdmsProjectDto>` | PROJECT:VIEW |
| POST | `/customer/project/abandonProject/{projectId}` | 放弃项目 | — | `String` | PROJECT:UPDATE |
| DELETE | `/customer/project/del/{id}` | 删除项目 | — | — | PROJECT:DELETE |
| POST | `/customer/project/product/update` | 存量项目补选所属产品：body 为 id、productId，或 newProductName、newProductCode 新建产品 | 体 `RdmsProjectSetupDto` | `RdmsProject` | PROJECT:UPDATE |

## 项目基线 BaselineController

源码：`customer/src/main/java/com/course/customer/controller/BaselineController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/customer/project/baseline/list` | body: ownerType（PROJECT/CHARACTER/SUBPROJECT/RELEASE）、ownerId；PROJECT 时列出该项目下全部基线 | 体 `Map<String, String>` | `List<Map<String, Object>>` | PROJECT:VIEW |
| GET | `/customer/project/baseline/get/{id}` | 查询项目基线 | — | `Map<String, Object>` | PROJECT:VIEW |

## 项目富文本文档 ProjectRichDocController

源码：`customer/src/main/java/com/course/customer/controller/ProjectRichDocController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/customer/project/rich-doc/get-lineage` | 查询版本谱系 | 体 `RichDocRequest` | `Map<String, Object>` | PROJECT:VIEW |

## 质量管理 QualityController

源码：`customer/src/main/java/com/course/customer/controller/QualityController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/customer/quality/getQualityRecheckNumByUserId/{userId}` | 按用户ID查询质量复核数量 | — | `Long` | QUALITY:VIEW |
| POST | `/customer/quality/getQualityDefineSubmitNumByPdm/{userId}` | 按产品经理查询质量定义提交数量 | — | `Long` | QUALITY:VIEW |
| POST | `/customer/quality/getQualityAndSubmitJobitems/{qualityId}` | 查询质量和提交工单 | — | `RdmsQualityDto` | QUALITY:VIEW |
| POST | `/customer/quality/getQualityCostListByProjectId/{projectId}` | 按项目ID查询质量成本列表 | — | `List<RdmsQualityDto>` | QUALITY:VIEW |
| POST | `/customer/quality/getQualityHasListFlag/{userId}` | 查询质量有列表标志 | — | `Boolean` | QUALITY:VIEW |
| POST | `/customer/quality/getQualityAndAnalysisJobList/{qualityId}` | 查询特性和工单列表 | — | `RdmsQualityDto` | QUALITY:VIEW |
| POST | `/customer/quality/list` | 查询质量列表 | 体 `PageDto<RdmsQualityDto>` | `PageDto<RdmsQualityDto>` | QUALITY:VIEW |
| POST | `/customer/quality/listByProjectId` | 按项目ID查询质量列表 | 体 `PageDto<RdmsQualityDto>` | `PageDto<RdmsQualityDto>` | QUALITY:VIEW |
| POST | `/customer/quality/listByProjectId_handling` | 按项目ID查询质量列表（处理中） | 体 `PageDto<RdmsQualityDto>` | `PageDto<RdmsQualityDto>` | QUALITY:VIEW |
| POST | `/customer/quality/getQualityAndJobitemList/{qualityId}` | 查询质量和工单列表 | — | `RdmsQualityDto` | QUALITY:VIEW |
| POST | `/customer/quality/listQualityByPjm` | 按项目经理查询质量列表 | 体 `PageDto<RdmsQualityDto>` | `PageDto<RdmsQualityDto>` | QUALITY:VIEW |
| POST | `/customer/quality/listQualityByPdm` | 按产品经理查询质量列表 | 体 `PageDto<RdmsQualityDto>` | `PageDto<RdmsQualityDto>` | QUALITY:VIEW |
| POST | `/customer/quality/listQualityByCharacterId` | 按特性ID查询质量列表 | 参 `characterId` | `List<RdmsQualityDto>` | QUALITY:VIEW |
| POST | `/customer/quality/listByManager` | 按经理查询质量列表 | 体 `PageDto<RdmsQualityDto>` | `PageDto<RdmsQualityDto>` | QUALITY:VIEW |
| POST | `/customer/quality/getProjectTreeListByCustomerId/{customerId}` | 按机构ID查询项目树列表 | — | `List<RdmsHmiTree<RdmsHmiTreeInfoDto>>` | QUALITY:VIEW |
| POST | `/customer/quality/getCharacterListIncludeNextLevelBySubprojectId/{subprojectId}` | 按子项目ID查询特性列表（含下一级） | — | `List<RdmsCharacterDto>` | QUALITY:VIEW |
| POST | `/customer/quality/getQualityDetailInfo` | 查询质量详情信息 | 参 `qualityId` | `RdmsQualityDto` | QUALITY:VIEW |
| POST | `/customer/quality/save` | 保存质量 | 体 `RdmsQualityDto` | `RdmsQualityDto` | QUALITY:CREATE |
| POST | `/customer/quality/saveQualityFeedbackInfo` | 保存质量反馈信息 | 体 `RdmsQualityDto` | `String` | QUALITY:CREATE |
| POST | `/customer/quality/submitQualityFeedbackInfo` | 提交质量反馈信息 | 体 `RdmsQualityDto` | `String` | QUALITY:UPDATE |
| POST | `/customer/quality/submit` | 提交质量 | 体 `RdmsQualityDto` | `RdmsQualityDto` | QUALITY:UPDATE |
| POST | `/customer/quality/saveAnalysis` | 保存分析 | 体 `RdmsQualityDto` | `RdmsQualityDto` | QUALITY:CREATE |
| POST | `/customer/quality/accept/{qualityId}` | 接受质量 | — | `String` | QUALITY:UPDATE |
| POST | `/customer/quality/reject/{qualityId}` | 驳回质量 | — | `String` | QUALITY:UPDATE |

## 小节 SectionController

源码：`customer/src/main/java/com/course/customer/controller/SectionController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/customer/section/list` | 查询小节列表 | 体 `SectionPageDto` | — | SECTION:VIEW |
| POST | `/customer/section/save` | 保存，id有值时更新，无值时新增 | 体 `RdmsSectionDto` | — | SECTION:CREATE |
| DELETE | `/customer/section/delete/{id}` | 删除小节 | — | — | SECTION:DELETE |

## 统计 StatisticsController

源码：`customer/src/main/java/com/course/customer/controller/StatisticsController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/customer/statistics/scope/get` | 查询范围 | — | `StatScopeViewDto` | SUMMARY:VIEW |
| POST | `/customer/statistics/workload/getDepartmentTree` | 查询部门树 | 体 `StatQueryDto` | `DepartmentWorkloadNodeDto` | SUMMARY:VIEW |
| POST | `/customer/statistics/workload/listDepartmentMembers/{departmentId}` | 查询部门成员列表 | 体 `StatQueryDto` | `List<WorkloadRowDto>` | SUMMARY:VIEW |
| POST | `/customer/statistics/workload/getPerson/{customerUserId}` | 查询人员 | 体 `StatQueryDto` | `PersonWorkloadDto` | SUMMARY:VIEW |
| POST | `/customer/statistics/project/list` | 查询项目列表 | 体 `StatQueryDto` | `PageDto<ProjectStatCardDto>` | SUMMARY:VIEW |
| POST | `/customer/statistics/project/listByIds` | 按ID 列表查询项目列表 | 体 `StatQueryDto` | `List<ProjectStatCardDto>` | SUMMARY:VIEW |
| POST | `/customer/statistics/project/getDetail/{projectId}` | 查询项目详情 | 体 `StatQueryDto` | `ProjectStatDetailDto` | SUMMARY:VIEW |
| POST | `/customer/statistics/project/getTrend/{projectId}` | 查询项目趋势 | 体 `StatQueryDto` | `List<ProjectTrendPointDto>` | SUMMARY:VIEW |
| POST | `/customer/statistics/dashboard/getOverview` | 管理驾驶舱：部门级及以上 | — | `DashboardOverviewDto` | SUMMARY:VIEW |
| POST | `/customer/statistics/workbench/getSelf` | 个人工作台：只看本人，不缓存 | — | `WorkbenchSelfDto` | SUMMARY:VIEW |

## 子项目 SubProjectController

源码：`customer/src/main/java/com/course/customer/controller/SubProjectController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/customer/sub-project/save` | 保存子项目 | 体 `RdmsProjectSubprojectDto` | `RdmsProjectSubprojectDto` | SUB_PROJECT:CREATE |
| POST | `/customer/sub-project/save-module-subproject` | 保存模块子项目 | 体 `RdmsProjectSubprojectDto` | `RdmsProjectSubprojectDto` | SUB_PROJECT:CREATE |
| POST | `/customer/sub-project/calSubprojectSelfBudgetActiveExeInfo/{subprojectId}` | 根据后台数据,计算实时的项目执行情况数据 | — | `RdmsBudgetExeSummaryDto` | SUB_PROJECT:UPDATE |
| POST | `/customer/sub-project/getBudgetExeSummaryBySubprojectId/{subprojectId}` | 根据后台数据,计算完工的项目执行情况数据 | — | `RdmsBudgetExeSummaryDto` | SUB_PROJECT:VIEW |
| POST | `/customer/sub-project/getTakePartInSubprojectList/{customerUserId}` | 查询参与子项目列表 | — | `List<RdmsProjectSubprojectDto>` | SUB_PROJECT:VIEW |
| POST | `/customer/sub-project/getCanBeOutsourcingFlag/{subproject}` | 查询可外协标志 | — | `Boolean` | SUB_PROJECT:VIEW |
| POST | `/customer/sub-project/getSubprojectBudgetExeInfoList/{subprojectId}` | 从数据库项目评审记录中读取相应的项目执行数据 | — | `RdmsBudgetExeDto` | SUB_PROJECT:VIEW |
| POST | `/customer/sub-project/getSubprojectBudgetExeSummary_researchExe/{subprojectId}` | 查询子项目预算执行汇总（研发执行） | — | `RdmsBudgetExeDto` | SUB_PROJECT:VIEW |
| POST | `/customer/sub-project/getSubprojectBoardInfo/{subprojectId}` | 查询子项目看板信息 | — | `RdmsBoardSubprojectDto` | SUB_PROJECT:VIEW |
| POST | `/customer/sub-project/saveOutSourcing` | 保存外协 | 体 `RdmsSubprojectService.OutsourceParameter` | — | SUB_PROJECT:CREATE |
| POST | `/customer/sub-project/getSubprojectAndChildrenExeSimpleInfo` | 根据后台数据, 计算项目执行情况的简要关键信息 | 参 `subprojectId` | `List<RdmsSubprojectExeSimpleInfoDto>` | SUB_PROJECT:VIEW |
| POST | `/customer/sub-project/getProjectAllReviewInfoList/{subprojectId}` | 查询项目全部评审信息列表 | — | `List<RdmsBudgetExeDto>` | SUB_PROJECT:VIEW |
| POST | `/customer/sub-project/getReviewJobInfo` | 查询最近评审信息 | 参 `subprojectId` | `RdmsHmiJobItemDocPageDto` | SUB_PROJECT:VIEW |
| POST | `/customer/sub-project/listProjectTaskJobChargeBySubProjectId/{subprojectId}` | 按子项目ID查询项目任务工作负责列表 | — | `BigDecimal` | SUB_PROJECT:VIEW |
| POST | `/customer/sub-project/getCharacterListBySubProjectReviewJobitem/{reviewJobitemId}` | 按子项目评审工单查询特性列表 | — | `List<RdmsBudgetExe>` | SUB_PROJECT:VIEW |
| POST | `/customer/sub-project/getCharacterListBySubprojectId/{subprojectId}` | 按子项目ID查询特性列表 | — | `List<RdmsBudgetExe>` | SUB_PROJECT:VIEW |
| POST | `/customer/sub-project/getSubProjectCbbList/{subprojectId}` | 查询子项目CBB列表 | — | `RdmsProjectSubprojectDto` | SUB_PROJECT:VIEW |
| POST | `/customer/sub-project/editSubProjectInfo` | 编辑子项目信息 | 体 `RdmsProjectSubprojectDto` | — | SUB_PROJECT:UPDATE |
| POST | `/customer/sub-project/getSubProjectForPjm` | 查询子项目项目经理 | 体 `RdmsHmiIdsDto` | `List<RdmsProjectSubprojectDto>` | SUB_PROJECT:VIEW |
| POST | `/customer/sub-project/getSubprojectExeData` | 按产品经理查询子项目执行数据 | 体 `RdmsHmiIdsDto` | `List<RdmsProjectSubprojectDto>` | SUB_PROJECT:VIEW |
| POST | `/customer/sub-project/getArchiveSubProjectForPjm` | 查询归档子项目项目经理 | 体 `RdmsHmiIdsDto` | `List<RdmsProjectSubprojectDto>` | SUB_PROJECT:VIEW |
| POST | `/customer/sub-project/ListSubprojectByIds` | 按ID 列表查询子项目列表 | 体 `RdmsHmiIdsDto` | `List<RdmsProjectSubprojectDto>` | SUB_PROJECT:VIEW |
| POST | `/customer/sub-project/getSubProjectInfo` | 查询子项目信息 | 参 `subProjectId` | `RdmsProjectSubprojectDto` | SUB_PROJECT:VIEW |
| POST | `/customer/sub-project/getSubProjectTree/{subProjectId}` | 查询子项目树 | — | `List<RdmsHmiTree<RdmsHmiTreeInfoDto>>` | SUB_PROJECT:VIEW |
| POST | `/customer/sub-project/getNextLevelSubproject/{subProjectId}` | 查询下一级别子项目 | — | `List<RdmsProjectSubprojectDto>` | SUB_PROJECT:VIEW |
| POST | `/customer/sub-project/getSubProjectDetailInfo/{subprojectId}` | 查询子项目详情信息 | — | `RdmsProjectSubprojectDto` | SUB_PROJECT:VIEW |
| POST | `/customer/sub-project/getSubProjectSimpleInfo/{subProjectId}` | 查询子项目简要信息 | — | `RdmsProjectSubprojectDto` | SUB_PROJECT:VIEW |
| POST | `/customer/sub-project/getModuleSubProjectInfo/{customerId}` | 查询模块子项目信息 | — | `RdmsProjectSubproject` | SUB_PROJECT:VIEW |
| POST | `/customer/sub-project/getSubprojectSelfRemainBudget/{subProjectId}` | 查询子项目本人剩余预算 | — | `BigDecimal` | SUB_PROJECT:VIEW |
| POST | `/customer/sub-project/getSubprojectDevelopCompleteStatus/{subprojectId}` | 查询子项目开发已完成状态 | — | `Boolean` | SUB_PROJECT:VIEW |
| POST | `/customer/sub-project/getSubprojectIntCompleteStatus/{subprojectId}` | 查询子项目已完成状态 | — | `Boolean` | SUB_PROJECT:VIEW |
| POST | `/customer/sub-project/getNumOfReviewSubprojectByNextNode/{nextNode}` | 按下一节点查询数量的评审子项目 | — | `Integer` | SUB_PROJECT:VIEW |
| POST | `/customer/sub-project/getSubprojectReviewInfoList` | 查询子项目评审信息列表 | 参 `subProjectId` | `List<RdmsJobItemDto>` | SUB_PROJECT:VIEW |
| POST | `/customer/sub-project/list` | 查询子项目列表 | 体 `PageDto<RdmsProjectSubprojectDto>` | `PageDto<RdmsProjectSubprojectDto>` | SUB_PROJECT:VIEW |
| POST | `/customer/sub-project/listSubProjectByPJM` | 按项目经理查询子项目列表 | 体 `PageDto<RdmsProjectSubprojectDto>` | `PageDto<RdmsProjectSubprojectDto>` | SUB_PROJECT:VIEW |
| POST | `/customer/sub-project/listSubprojectsAndCharacterList` | 查询子项目和特性列表 | 体 `PageDto<RdmsHmiSubprojectAndCharacterMixDto>` | `PageDto<RdmsHmiSubprojectAndCharacterMixDto>` | SUB_PROJECT:VIEW |
| POST | `/customer/sub-project/listSubProjectByTM` | 按测试主管查询子项目列表 | 体 `PageDto<RdmsProjectSubprojectDto>` | `PageDto<RdmsProjectSubprojectDto>` | SUB_PROJECT:VIEW |
| POST | `/customer/sub-project/archive/{subprojectId}` | 归档子项目 | — | — | SUB_PROJECT:UPDATE |
| POST | `/customer/sub-project/getTestSubprojectListByTM/{testManagerId}` | 按测试主管查询测试子项目列表 | — | `List<RdmsProjectSubprojectDto>` | SUB_PROJECT:VIEW |
| POST | `/customer/sub-project/getSubprojectTaskList/{subprojectId}` | 查询子项目任务列表 | — | `List<RdmsJobItemDto>` | SUB_PROJECT:VIEW |
| POST | `/customer/sub-project/getTestFlagSubprojectListByTM/{testManagerId}` | 按测试主管查询测试标志子项目列表 | — | `Boolean` | SUB_PROJECT:VIEW |
| DELETE | `/customer/sub-project/del/{id}` | 删除：删除子项目（现无页面入口）：只允许删除空的非根子项——无特性、无下级子项、未划入预算 | — | — | SUB_PROJECT:DELETE |

## 汇总数据 SummaryController

源码：`customer/src/main/java/com/course/customer/controller/SummaryController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/customer/summary/getCustomerSummaryInfo` | 机构大屏汇总，机构取自登录租户 | — | `CustomerSummaryDto` | SUMMARY:VIEW |

## 系统经理 SystemManagerController

源码：`customer/src/main/java/com/course/customer/controller/SystemManagerController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/customer/system-manager/list` | 查询系统经理列表 | 体 `PageDto<RdmsSystemManagerDto>` | `PageDto<RdmsSystemManagerDto>` | DIRECTOR:VIEW |
| POST | `/customer/system-manager/listByCustomerId/{customerId}` | 按机构ID查询系统经理列表 | — | `List<String>` | DIRECTOR:VIEW |
| POST | `/customer/system-manager/getListByCustomerId/{customerId}` | 按机构ID查询系统经理列表 | — | `List<RdmsCustomerUserDto>` | DIRECTOR:VIEW |
| POST | `/customer/system-manager/listSystemManagerByCustomerId/{customerId}` | 按机构ID查询系统经理列表 | — | `List<RdmsCustomerUser>` | DIRECTOR:VIEW |
| POST | `/customer/system-manager/save` | 保存系统经理 | 体 `RdmsSystemManagerDto` | `RdmsSystemManagerDto` | DIRECTOR:CREATE |
| DELETE | `/customer/system-manager/del/{id}` | 删除系统经理 | — | — | DIRECTOR:DELETE |

## 系统设置 SystemController

源码：`customer/src/main/java/com/course/customer/controller/SystemController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/customer/system/save` | 保存系统设置 | 体 `RdmsSystemDto` | `RdmsSystemDto` | PRODUCT:CREATE |
| POST | `/customer/system/getSystemSimpleInfo/{systemId}` | 查询系统简要信息 | — | `RdmsSystemDto` | PRODUCT:VIEW |
| POST | `/customer/system/getUserImportTemplateInfo/{systemId}` | 查询用户导入模板信息 | — | `RdmsFileDto` | PRODUCT:VIEW |

## 下属员工 UnderlineController

源码：`customer/src/main/java/com/course/customer/controller/UnderlineController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/customer/underline/getUnderline/{workerId}` | 本人的下级：所负责部门及其全部下级部门中的人员（不含本人）。路径中的人员编号只在无登录上下文时使用 | — | `List<RdmsCustomerUserDto>` | PERFORMANCE:VIEW |

## 公开页面数据 WebController

源码：`customer/src/main/java/com/course/customer/controller/WebController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/customer/web/getCourseDetailInfo/{courseId}` | 查询课程详情信息 | — | `RdmsCourseDto` | WEB:VIEW |
| POST | `/customer/web/getSectionInfo/{sectionId}` | 查询小节信息 | — | `RdmsSectionDto` | WEB:VIEW |
| POST | `/customer/web/course-list` | 课程列表 | 体 `RdmsCoursePageDto` | `RdmsCoursePageDto` | WEB:UPDATE |

## 工作日历 WorkCalendarController

源码：`customer/src/main/java/com/course/customer/controller/WorkCalendarController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| GET | `/customer/work-calendar/list/{year}` | 查询工作日历列表 | — | `Map<String, Object>` | WORKER:VIEW |
| POST | `/customer/work-calendar/update` | 修改工作日历 | 体 `WorkCalendarSetDto` | `Integer` | WORKER:UPDATE |

## 员工 WorkerController

源码：`customer/src/main/java/com/course/customer/controller/WorkerController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/customer/worker/list` | 查询员工列表 | 体 `RdmsWorkerPageDto` | — | WORKER:VIEW |
| POST | `/customer/worker/searchByKeyWord` | 按关键字搜索员工 | 体 `PageDto<RdmsCustomerUserDto>` | `PageDto<RdmsCustomerUserDto>` | WORKER:VIEW |
| GET | `/customer/worker/turnOver/preview` | 移交预览 | 参 `customerId`；参 `workerId`；参 `newWorkerId` | `WorkHandoverResultDto` | WORKER:VIEW |
| POST | `/customer/worker/turnOver` | 移交员工 | 参 `customerId`；参 `workerId`；参 `newWorkerId` | `WorkHandoverResultDto` | WORKER:UPDATE |
| GET | `/customer/worker/turnOver/logs/{workerId}` | 移交日志 | 参 `customerId` | `List<WorkHandoverLogDto>` | WORKER:VIEW |
| POST | `/customer/worker/getWorkerManhourInfo/{customerUserId}` | 查询员工的工时费 | — | `RdmsHmiWorkerManhourDto` | MEMBER:VIEW |
| POST | `/customer/worker/getWorkload/{customerUserId}` | 查询员工工作负荷 | — | `List<RdmsHmiWorkLoadDto>` | MEMBER:VIEW |
| POST | `/customer/worker/listAllWorkers/{customerId}` | 查询全部成员列表 | — | `List<RdmsCustomerUserDto>` | MEMBER:VIEW |
| POST | `/customer/worker/getSuperInfo/{customerId}` | 查询超级信息 | — | `RdmsCustomerUserDto` | WORKER:VIEW |
| POST | `/customer/worker/getBossInfo/{customerId}` | 查询最终管理人信息 | — | `RdmsBossDto` | WORKER:VIEW |
| POST | `/customer/worker/listWorkers` | 查询下属成员列表 | 参 `customerUserId` | `List<RdmsCustomerUserDto>` | MEMBER:VIEW |
| POST | `/customer/worker/listAllWorkersByPreProjectId/{preProjectId}` | 按预立项ID查询全部成员列表 | — | `List<RdmsHmiExecutorPlainDto>` | MEMBER:VIEW |
| POST | `/customer/worker/listAllWorkersByJobitemId/{jobitemId}` | 按工单ID查询全部成员列表 | — | `List<RdmsHmiExecutorPlainDto>` | MEMBER:VIEW |
| POST | `/customer/worker/listAllWorkersByCharacterId/{characterId}` | 按特性ID查询全部成员列表 | — | `List<RdmsHmiExecutorPlainDto>` | MEMBER:VIEW |
| POST | `/customer/worker/listAllWorkersByProjectId/{projectId}` | 按项目ID查询全部成员列表 | — | `List<RdmsHmiExecutorPlainDto>` | MEMBER:VIEW |
| POST | `/customer/worker/listAllWorkersBySubProjectId/{subprojectId}` | 按子项目ID查询全部成员列表 | — | `List<RdmsHmiExecutorPlainDto>` | MEMBER:VIEW |
| POST | `/customer/worker/save` | 保存员工 | 体 `RdmsCustomerUserDto` | — | WORKER:CREATE |
| POST | `/customer/worker/saveWorkers` | 保存成员 | 体 `List<RdmsCustomerUserDto>` | `WorkerImportResultDto` | WORKER:CREATE |
| POST | `/customer/worker/login-search` | 登录名称搜索 | 体 `RdmsCustomerUserDto` | — | WORKER:UPDATE |
| DELETE | `/customer/worker/del/{id}` | 删除员工 | — | — | WORKER:DELETE |
| POST | `/customer/worker/reset-password` | 机构管理员或最终管理人把本机构成员的密码重置为初始密码，成员下次登录须先设置新密码。体为 memberId | 体 `Map<String, String>` | `Map<String, String>` | WORKER:UPDATE |
| POST | `/customer/worker/listWorker` | 查询成员列表 | 体 `RdmsResponseDto` | — | WORKER:VIEW |
| POST | `/customer/worker/get-worker-info/{customerUserId}` | 根据customerUserID获取一个工作人员信息 | — | `RdmsCustomerUserDto` | MEMBER:VIEW |
