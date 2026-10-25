/*
 * Author: 周朔鹏
 * Date: 2025-08-04
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
 */

EDIT_TYPE={
    CREATE:{key:"CREATE", value:"创建"},
    MODIFY:{key:"MODIFY", value:"修改"},
};

SMS_USE={
    REGISTER:{key:"R", value:"注册"},
    FORGET:{key:"F", value:"忘记密码"},
};

FILE_GROUPING={
    DEFAULT:{key:"default", value:"默认"},
    SYSTEM:{key:"system", value:"系统"},
    USER_INFO_TEMPLATE:{key:"user_info_template", value:"用户信息模板"},
    CUSTOMER:{key:"customer", value:"机构"},
    USER:{key:"user", value:"用户"},
    DEMAND:{key:"demand", value:"需求文件"},
    CHARACTER:{key:"character", value:"特性定义"},
    JOBITEM:{key:"jobitem", value:"任务文件"},
    PRE_PROJECT:{key:"preproject", value:"预立项文件"},
    PROJECT:{key:"project", value:"项目文件"},
    MATERIAL_APP:{key:"material_app", value:"物料申请"},
    MATERIAL_DEAL:{key:"material_deal", value:"物料处置"},
    MATERIAL_SUBMIT:{key:"material_submit", value:"处置完成"},
    FEE_APP:{key:"fee_app", value:"费用申请"},
    FEE_DEAL:{key:"fee_deal", value:"费用处理"},
    FEE_SUBMIT:{key:"fee_submit", value:"支付完成"},
    QUALITY:{key:"quality", value:"质量处理"},
    BUG:{key:"bug", value:"缺陷处理"},
    PROPERTY:{key:"property", value:"研发资产"},
    COURSE:{key:"course", value:"培训课程"},
    MAIL:{key:"mail", value:"邮件附件"},
};

DOC_TYPE={
    DEFAULT:{key:"DEFAULT", value:"默认"},
    FILE:{key:"FILE", value:"文件"},
    DEMAND:{key:"DEMAND", value:"需求"},
    CHARACTER:{key:"CHARACTER", value:"特性"},
    QUALITY:{key:"QUALITY", value:"质量"},
    BUG:{key:"BUG", value:"缺陷"},
    JOBITEM:{key:"JOBITEM", value:"工单"},
    SUB_PROJECT:{key:"SUB_PROJECT", value:"子项目"},
    PRE_PROJECT:{key:"PRE_PROJECT", value:"预立项"},
    MILESTONE:{key:"MILESTONE", value:"里程碑"},
    BUDGET:{key:"BUDGET", value:"预算"},
    BUDGET_ADJUST:{key:"BUDGET_ADJUST", value:"预算申请单"},
    PROJECT:{key:"PROJECT", value:"主项目"},
    CBB:{key:"CBB", value:"CBB"},
};

WORKER_STATUS={
    NORMAL:{key:"NORMAL", value:"正常"},
    // ASKFORLEAVE:{key:"LEAVE", value:"请假"},
    // HOLIDAY:{key:"HOLIDAY", value:"休假"},
    VACATE:{key:"VACATE", value:"辞职"},
    DISMISS:{key:"DISMISS", value:"辞退"}
};

OVERLOAD_STATUS={
    NORMAL:{key:"NORMAL", value:"正常工作量"},
    OVERLOAD:{key:"OVERLOAD", value:"工作超负荷"}
};

CUSTOMER_STATUS={
    REGISTER:{key:"REGISTER", value:"注册"},
    NORMAL:{key:"NORMAL", value:"在业"},
    STOP:{key:"STOP", value:"停业"},
    HOLIDAY:{key:"HOLIDAY", value:"休假"},
    FROZEN:{key:"FROZEN", value:"冻结"}
};

CUSTOMER_TYPE={
    PRODUCT_COMPANY:{key:"PRODUCT_COMPANY", value:"产品型公司"},
    PROJECT_COMPANY:{key:"PROJECT_COMPANY", value:"项目型公司"},
    RESEARCH_INSTITUTE:{key:"RESEARCH_INSTITUTE", value:"科研机构"},
    DEVELOPER_COMMUNITY:{key:"DEVELOPER_COMMUNITY", value:"开发者社区"},
    HR_OUTSOURCING:{key:"HR_OUTSOURCING", value:"人资外包机构"}
};

CUSTOMER_CONFIG_STATUS={
    USED:{key:"USED", value:"使用"},
    UNUSED:{key:"UNUSED", value:"未使用"},
    SUSPEND:{key:"SUSPEND", value:"暂停"},
    FROZEN:{key:"FROZEN", value:"冻结"},
    STOP:{key:"STOP", value:"停用"},
};

ENTRANCE_CLASS={
    DEMAND:{key:"DEMAND", value:"需求管理"},
    CHARACTER:{key:"CHARACTER", value:"特性管理"},
    FUNCTION:{key:"FUNCTION", value:"系统工程(已废弃)"},
    SETUP:{key:"SETUP", value:"立项管理"},
    DEVELOP:{key:"DEVELOP", value:"开发管理"},
    TEST:{key:"TEST", value:"测试管理"},
    INTEGRATION:{key:"INTEGRATION", value:"集成管理"},
    TASK:{key:"INTEGRATION", value:"任务管理"},
    MANHOUR:{key:"MANHOUR", value:"工时管理"},
    PROJECT:{key:"PROJECT", value:"项目管理"},
    REVIEW:{key:"REVIEW", value:"评审管理"},
    SYSTEM:{key:"SYSTEM", value:"系统工程管理"},
    PRODUCT:{key:"PRODUCT", value:"产品管理"},
    SUBPROJECT:{key:"SUBPROJECT", value:"子项目管理"},
    PROPERTY:{key:"PROPERTY", value:"资产管理"},
    QUALITY:{key:"QUALITY", value:"质量管理"},
    SUPPLYCHAIN:{key:"SUPPLYCHAIN", value:"供应链管理"},
    BUDGET:{key:"BUDGET", value:"预算管理"},
};

PROJECT_STATUS={
    // 立项
    SETUP_EDIT:{key:"SETUP_EDIT", value:"立项编辑"},
    SETUP:{key:"SETUP", value:"申请立项"},
    SETUPED:{key:"SETUPED", value:"已立项"},
    // 执行
    ONGOING:{key:"ONGOING", value:"执行中"},
    OUT_SOURCE:{key:"OUT_SOURCE", value:"项目外包"},
    DEV_COMPLETE:{key:"DEV_COMPLETE", value:"增补材料"},
    // 评审
    REVIEW_SUBP:{key:"REVIEW_SUBP", value:"子项目评审中"},
    REVIEW_PRO:{key:"REVIEW_PRO", value:"主项目评审中"},
    // 收口
    COMPLETE:{key:"COMPLETE", value:"已完成"},
    CLOSE:{key:"CLOSE", value:"关闭"},
    ARCHIVED:{key:"ARCHIVED", value:"已归档"},
    // 僵尸：历史只读
    PREPARE:{key:"PREPARE", value:"准备立项"},
    SUSPEND:{key:"SUSPEND", value:"暂停"},
    EN_REVIEW:{key:"EN_REVIEW", value:"可评审"},
    INTEGRATION:{key:"INTEGRATION", value:"项目集成"},
};

/** 预立项逻辑态（后端 lifecycleStatus，非库字段） */
PRE_PROJECT_LIFECYCLE={
    DRAFT:{key:"DRAFT", value:"草稿"},
    ACTIVE:{key:"ACTIVE", value:"进行中"},
    CONVERTED:{key:"CONVERTED", value:"已立项"},
    CLOSED:{key:"CLOSED", value:"已关闭"},
};

PROJECT_TYPE={
    PRE_PROJECT:{key:"PRE_PROJECT", value:"预立项项目"},
    DEV_PROJECT:{key:"DEV_PROJECT", value:"项目定义"},
    SUB_PROJECT:{key:"SUB_PROJECT", value:"子项目开发"},
    PROJECT:{key:"PROJECT", value:"产品开发"},
    PRODUCT:{key:"PRODUCT", value:"PLM阶段"},
    UPD_PROJECT:{key:"UPD_PROJECT", value:"迭代开发"},
    MODULE:{key:"MODULE", value:"特性开发"}
};

RD_TYPE={
    DEVELOPMENT:{key:"DEVELOPMENT", value:"开发项目"},
    RESEARCH:{key:"RESEARCH", value:"课题项目"},
    DIRECT:{key:"DIRECT", value:"直接立项"},
    MODULE:{key:"MODULE", value:"特性开发项目"},
    INTEGRATION:{key:"INTEGRATION", value:"集成项目"},
    PRODUCT:{key:"PRODUCT", value:"既有产品"}
};

FILE_APPLY_TYPE={
    CONSULT:{key:"CONSULT", value:"文档调阅"},
    APPLY:{key:"APPLY", value:"文档申请"}
};

MIX_TYPE={
    SUB_PROJECT:{key:"SUB_PROJECT", value:"子项目"},
    PROJECT:{key:"PROJECT", value:"主项目"},

    CHARACTER:{key:"CHARACTER", value:"特性"},
    JOBITEM:{key:"JOBITEM", value:"任务"},

};

DEMAND_STATUS={
    DEFAULT:{key:"NULL", value:"未设定"},
    SAVED:{key:"SAVED", value:"已保存"},
    HANDLING:{key:"HANDLING", value:"处理中"},
    SUBMIT:{key:"SUBMIT", value:"已提交"},
    ABANDON:{key:"ABANDON", value:"废弃"},
    COMPLETED:{key:"COMPLETED", value:"已完成"}
};

CHARACTER_STATUS={
    // 定义审批
    NOTSET:{key:"NOTSET", value:"未设置"},
    SAVED:{key:"SAVED", value:"保存"},
    SUBMIT:{key:"SUBMIT", value:"提交"},
    APPROVE:{key:"APPROVE", value:"审批中"},
    APPROVED:{key:"APPROVED", value:"批准"},
    REFUSE:{key:"REFUSE", value:"拒绝"},
    DISCARD:{key:"DISCARD", value:"废弃"},
    // 立项
    SETUP:{key:"SETUP", value:"申请立项"},
    SETUPED:{key:"SETUPED", value:"立项"},
    // 研发
    DECOMPOSED:{key:"DECOMPOSED", value:"分解"},
    DEVELOPING:{key:"DEVELOPING", value:"开发中"},
    INTEGRATION:{key:"INTEGRATION", value:"集成中"},
    DEV_COMPLETE:{key:"DEV_COMPLETE", value:"开发完成"},
    QUALITY:{key:"QUALITY", value:"质量问题处理"},
    OUT_SOURCE:{key:"OUT_SOURCE", value:"特性外包"},
    // 评审收口
    REVIEW:{key:"REVIEW", value:"特性评审"},
    ARCHIVED:{key:"ARCHIVED", value:"已归档"},  //归档是指正常完成后的最终状态
    // 迭代旁路
    UPDATE:{key:"UPDATE", value:"迭代处理"},
    UPDATED:{key:"UPDATED", value:"迭代更新"},
    UPDATED_HISTORY:{key:"UPDATED_HISTORY", value:"迭代留档"},
    HISTORY:{key:"HISTORY", value:"修订留档"},
    ITERATING:{key:"ITERATING", value:"修订"}, // 保留码
    MERGED:{key:"MERGED", value:"合并"},
    // 显示兼容，不是特性主状态
    REVIEW_SUBP:{key:"REVIEW_SUBP", value:"子项目评审"},
    REVIEW_PRO:{key:"REVIEW_PRO", value:"项目评审"},
    COMPLETE:{key:"COMPLETE", value:"已完成"},
    CLOSE:{key:"CLOSE", value:"关闭"},
};

// 与后端 CbbStatusEnum 一致
CBB_STATUS={
    SAVED:{key:"SAVED", value:"编辑中"},
    SUBMIT:{key:"SUBMIT", value:"已提交"},
    REFUSE:{key:"REFUSE", value:"已驳回"},
    DISCARD:{key:"DISCARD", value:"已废弃"},
    UPDATE:{key:"UPDATE", value:"迭代中"},
    ARCHIVED:{key:"ARCHIVED", value:"已发布"},
    UPDATED_HISTORY:{key:"UPDATED_HISTORY", value:"历史版本"},
    DISABLED:{key:"DISABLED", value:"已停用"},
};

// 与后端 BomItemTypeEnum 一致；CBB 行不计入金额与数量汇总
BOM_ITEM_TYPE={
    MATERIAL:{key:"MATERIAL", value:"物料"},
    CBB:{key:"CBB", value:"软件（CBB）"},
};

CHARACTER_PROCESS={
    EDITING:{key:"EDITING", value:"编辑"},
    SUBMIT:{key:"SUBMIT", value:"提交"},
    EXAMINE:{key:"EXAMINE", value:"签审"},
    APPROVE:{key:"APPROVE", value:"审批中"},
    APPROVED:{key:"APPROVED", value:"批准"},
    SETUP:{key:"SETUP", value:"申请立项"},
    SETUPED:{key:"SETUPED", value:"立项"},
    DISTRIBUTE:{key:"DISTRIBUTE", value:"已分配"},
    ONGOING:{Key:"ONGOING", value:"开发中"},
    ITERATION:{key:"ITERATION", value:"修订"},
    DECOMPOSE:{key:"DECOMPOSE", value:"分解"},
    REFUSE:{key:"REFUSE", value:"驳回"},
    REVIEW:{key:"REVIEW", value:"评审"},
    QUALITY:{key:"QUALITY", value:"质量处理"},
    COMPLETE:{key:"COMPLETE", value:"完成"}
};

ACTOR_TYPE={
    DEVELOPER:{key:"DEVELOPER", value:"员工"},
    PRODUCT_MANAGER:{key:"PDM", value:"产品经理"},
    PROJECT_MANAGER:{key:"PJM", value:"项目经理"},
    DEPARTMENT_MANAGER:{key:"DTM", value:"部门负责人"},
    CUSTOMER_MANAGER:{key:"CTM", value:"客户负责人"},
    SUPER:{key:"SUPER", value:"超级管理员"}
};

PARENT_CHILDREN={
    PARENT:{key:"PARENT", value:"父级"},
    CHILDREN:{key:"CHILDREN", value:"子级"}
};

OPERATE_TYPE={
    DEVELOP:{key:"DEVELOP", value:"开发"},
    TEST:{key:"TEST", value:"测试"},
};

LOGIN_USER_GROUP={
    CUSTOMER:{key:"CUSTOMER", value:"机构客户"},
    DEVELOPER:{key:"DEVELOPER", value:"员工"},
    CUSTOMER_VIP1:{key:"CUSTOMER_VIP1", value:"高级机构客户-1级"},
    DEVELOPER_VIP1:{key:"DEVELOPER_VIP1", value:"高级员工-1级"}
};

GANTT_PLAN_STATUS={
    PLAN:{key:"PLAN", value:"待下发"},
    ISSUED:{key:"ISSUED", value:"已下发"},
    FAILED:{key:"FAILED", value:"下发失败"},
    CANCEL:{key:"CANCEL", value:"已取消"}
};

JOB_ITEM_STATUS={
    DEFAULT:{key:"NULL", value:"未设定"},
    PLAN:{key:"PLAN", value:"计划中"},
    HANDLING:{key:"HANDLING", value:"处理中"},
    REFUSE:{key:"REFUSE", value:"拒收"},
    SUBMIT:{key:"SUBMIT", value:"已提交"},
    TESTING:{key:"TESTING", value:"测试中"},
    CHA_RECHECK:{key:"CHA_RECHECK", value:"特性待复核"},
    QUA_RECHECK:{key:"QUA_RECHECK", value:"质量复核"},
    SUB_RECHECK:{key:"SUB_RECHECK", value:"项目待复核"},
    EVALUATE:{key:"EVALUATE", value:"评价"},
    APPROVED:{key:"APPROVED", value:"批准"},
    COMPLETED:{key:"COMPLETED", value:"完成"},
    ARCHIVED:{key:"ARCHIVED", value:"已归档"},
    ALARM:{key:"ALARM", value:"报警"},
    CANCEL:{key:"CANCEL", value:"取消"},
    OVERDUE:{key:"OVERDUE", value:"逾期"}
};

QUALITY_STATUS={
    SAVE:{key:"SAVE", value:"保存"},
    SUBMIT:{key:"SUBMIT", value:"已提交"},
    SAVE_ANALYSIS:{key:"SAVE_ANALYSIS", value:"保存分析"},
    HANDLING:{key:"HANDLING", value:"处理中"},
    JOB_SUBMIT:{key:"JOB_SUBMIT", value:"作业提交"},
    RECHECK:{key:"RECHECK", value:"复核"},
    REJECT:{key:"REJECT", value:"驳回"},
    COMPLETED:{key:"COMPLETED", value:"完成"},
    ARCHIVED:{key:"ARCHIVED", value:"归档"},
};

JOB_ITEM_TYPE={
    // 定义
    NOTSET:{key:"NOTSET", value:"未设定"},
    DEMAND:{key:"DEMAND", value:"需求定义"},
    DECOMPOSE:{key:"DECOMPOSE", value:"分解任务"},
    ITERATION:{key:"ITERATION", value:"修订任务"},
    MERGE:{key:"MERGE", value:"合并任务"},
    JOIN_TO:{key:"JOIN_TO", value:"归并任务(已废弃)"},
    SUGGEST:{key:"SUGGEST", value:"增补建议"},
    SUGGEST_UPDATE:{key:"SUGGEST_UPDATE", value:"特性迭代建议"},
    CBB_DEFINE:{key:"CBB_DEFINE", value:"CBB 定义"},
    UPDATE:{key:"UPDATE", value:"迭代定义"},
    FUNCTION:{key:"FUNCTION", value:"系统工程(已废弃)"},
    // 研发
    CHA_RECHECK:{key:"CHA_RECHECK", value:"复核任务"},
    DEVELOP:{key:"DEVELOP", value:"开发任务"},
    QUALITY:{key:"QUALITY", value:"质量处理"},
    ASSIST:{key:"ASSIST", value:"协作任务"},
    BUG:{key:"BUG", value:"测试缺陷"},
    TEST:{key:"TEST", value:"测试工单"},
    CHARACTER_INT:{key:"CHARACTER_INT", value:"特性集成"},
    SUBPROJECT_INT:{key:"SUBPROJECT_INT", value:"子项目集成"},
    PROJECT_INT:{key:"PROJECT_INT", value:"主项目集成"},
    // 评审
    REVIEW:{key:"REVIEW", value:"特性评审"},
    REVIEW_PRO:{key:"REVIEW_PRO", value:"项目评审"},
    REVIEW_SUBP:{key:"REVIEW_SUBP", value:"子项目评审"},
    REVIEW_MILESTONE_PRE:{key:"REVIEW_MILESTONE_PRE", value:"里程碑评审发起"},
    REVIEW_MILESTONE:{key:"REVIEW_MILESTONE", value:"里程碑评审"},
    REVIEW_COO:{key:"REVIEW_COO", value:"委员单"},
    REVIEW_CHANGE:{key:"REVIEW_CHANGE", value:"变更评审"},
    // 任务
    TASK:{key:"TASK", value:"特性任务"},
    TASK_SUBP:{key:"TASK_SUBP", value:"子项目任务"},
    TASK_MILESTONE:{key:"TASK_MILESTONE", value:"里程碑任务"},
    TASK_PRO:{key:"TASK_PRO", value:"主项目任务"},
    TASK_PRODUCT:{key:"TASK_PRODUCT", value:"产品任务"},
    TASK_FUNCTION:{key:"TASK_FUNCTION", value:"系统工程任务"},
    TASK_TEST:{key:"TASK_TEST", value:"特性测试"},
    TASK_BUG:{key:"TASK_BUG", value:"缺陷处理"},
    // 审批
    CHARACTER_TERMINATE:{key:"CHARACTER_TERMINATE", value:"特性终止申请"},
    // 通知
    REVIEW_NOTIFY:{key:"REVIEW_NOTIFY", value:"评审通知"},
    MILESTONE_NOTIFY:{key:"MILESTONE_NOTIFY", value:"里程碑通知"},
    REFUSE_NOTIFY:{key:"REFUSE_NOTIFY", value:"拒收通知"},
    BUDGET_ADJUST_NOTIFY:{key:"BUDGET_ADJUST_NOTIFY", value:"预算审批通知"},
    DEFINE_UPDATE_NOTIFY:{key:"DEFINE_UPDATE_NOTIFY", value:"定义更新通知"},
    CBB_UPDATE_NOTIFY:{key:"CBB_UPDATE_NOTIFY", value:"CBB 状态通知"},
    PLAN_ISSUE_NOTIFY:{key:"PLAN_ISSUE_NOTIFY", value:"计划下发失败通知"},
};

JOB_ITEM_PROCESS={
    ISSUE:{key:"ISSUE", value:"任务下发"},
    REFUSE:{key:"REFUSE", value:"任务拒收"},
    SUBMIT:{key:"SUBMIT", value:"作业提交"},
    TESTING:{key:"TESTING", value:"测试中"},
    APPROVE:{key:"APPROVE", value:"作业签审"},
    REJECT:{key:"REJECT", value:"任务驳回"},
    TRANSFER:{key:"TRANSFER", value:"转交修订"},
    CHA_RECHECK:{key:"CHA_RECHECK", value:"特性复核"},
    QUA_RECHECK:{key:"QUA_RECHECK", value:"质量复核"},
    EVALUATE:{key:"EVALUATE", value:"工作评价"},
    COMPLETE:{key:"COMPLETE", value:"工作完成"}
};

REVIEW_RESULT={
    PASS:{key:"PASS", value:"通过" ,num:"2"},
    CONDITIONAL:{key:"CONDITIONAL", value:"有条件通过" ,num:"4"},
    SUPPLEMENT:{key:"SUPPLEMENT", value:"增补" ,num:"6"},
    REJECT:{key:"REJECT", value:"驳回" ,num:"8"}
};

SETUP_STAGE={
    SETUP:{key:"SETUP", value:"预立项"},
    SETUPED:{key:"SETUPED", value:"已立项"}
};

APPLICATION_STATUS={
    NOTSET:{key:"NOTSET", value:"未设置"},
    APPLICATION:{key:"APPLICATION", value:"申请中"},
    REJECT:{key:"REJECT", value:"拒绝"},
    PRE_APPROVED:{key:"PRE_APPROVED", value:"预批准"},
    APPROVED:{key:"APPROVED", value:"批准"},
    SUPPLEMENT:{key:"SUPPLEMENT", value:"驳回"},  //对提交的材料有异议, 退回处理
    SUBMIT:{key:"SUBMIT", value:"待复核"},  //物料处置完成后, 提交审批
    COMPLETE:{key:"COMPLETE", value:"已结束"},
    PRE_COMPLETE:{key:"PRE_COMPLETE", value:"复核通过"},
    CANCEL:{key:"CANCEL", value:"取消"},
};

FILE_APPLICATION_STATUS={
    NOTSET:{key:"NOTSET", value:"可申请"},
    APPLICATION:{key:"APPLICATION", value:"申请中"},
    REJECT:{key:"REJECT", value:"拒绝"},
    APPROVED:{key:"APPROVED", value:"已批准"},
    COMPLETE:{key:"COMPLETE", value:"结束"},
    CANCEL:{key:"CANCEL", value:"取消"},
    DOWNLOADED:{key:"DOWNLOADED", value:"已下载"},
};

MATERIAL_USAGE_MODE={
    BORROW:{key:"BORROW", value:"借用"},
    CAPITALIZATION:{key:"CAPITALIZATION", value:"资产化"},
    EXPENDITURE:{key:"EXPENDITURE", value:"费用化"},
};

MATERIAL_OPE_STATUS={
    APPLICATION:{key:"APPLICATION", value:"申请中"},
    GTE_MATERIAL:{key:"GTE_MATERIAL", value:"领料中"},
    RETURN:{key:"RETURN", value:"退料处理中..."},
    RETURN_APPLICATION:{key:"RETURN_APPLICATION", value:"退料申请中..."},
    RETURN_PRE_APPLICATION:{key:"RETURN_PRE_APPLICATION", value:"OA审批中..."},
    RETURN_APPROVED:{key:"RETURN_APPROVED", value:"审批通过"},
    RETURN_PRE_APPROVED:{key:"RETURN_PRE_APPROVED", value:"预批准"},
    RETURN_COMPLETE:{key:"RETURN_COMPLETE", value:"退料完成"},
    APPROVED:{key:"APPROVED", value:"已审批"},
    COMPLETE:{key:"COMPLETE", value:"完成"},
};

AUX_STATUS={
    NOTSET:{key:"NOTSET", value:"未设定"},
    PRINTED:{key:"PRINTED", value:"已打印"},
};

BUDGET_EXE_TYPE={
    DEVELOP:{key:"DEVELOP", value:"开发任务"},
    ASSIST:{key:"ASSIST", value:"协作任务"},
    TEST:{key:"TEST", value:"测试工单"},
    MATERIAL:{key:"MATERIAL", value:"物料费用"},
    OTHER_FEE:{key:"OTHER_FEE", value:"其他费用"},
};

BOM_STATUS={
    NORMAL:{key:"NORMAL", value:"正常使用"},
    REPLACED:{key:"REPLACED", value:"被替换"},
    CANCEL:{key:"CANCEL", value:"取消"},
    OPTIONAL:{key:"OPTIONAL", value:"可选的"},
};

CHARACTER_BOM_STATUS={
    EDIT:{key:"EDIT", value:"编辑中"},
    SUBMIT:{key:"SUBMIT", value:"已提交"},
    RECHECK:{key:"RECHECK", value:"复核中"},
    LOCKED:{key:"LOCKED", value:"锁定"},
};

USER_ROLE={
    STAFF:{key:"STAFF", value:"员工"},
    SUPER:{key:"SUPER", value:"ROOT"},
    ADMIN:{key:"ADMIN", value:"机构管理员"},
    IPMT:{key:"IPMT", value:"IPMT"},
    UltiMng:{key:"UltiMng", value:"最终管理人"},
    PDM:{key:"PDM", value:"产品经理"},
    TM:{key:"TM", value:"测试主管"},
    PJM:{key:"PJM", value:"项目经理"},
    SM:{key:"SM", value:"系统工程师"},
    SYSGM:{key:"SYSGM", value:"系统工程总监"},
    PDGM:{key:"PDGM", value:"产品总监"},
    QCGM:{key:"QCGM", value:"质量总监"},
    FGGM:{key:"FGGM", value:"法规总监"},
    TGM:{key:"TGM", value:"测试总监"},
};

BUDGET_CLASSIFY_STD ={
    EQUIPMENT:{key:"F1.1",  value:"设备费"},
    MATERIAL:{key:"F2.1",   value:"业务费-材料费"}, 
    TEST:{key:"F2.2",       value:"业务费-测试化验加工费"},
    POWER:{key:"F2.3",      value:"业务费-燃料动力费"},
    CONFERENCE:{key:"F2.4.1",   value:"业务费-会议费"},
    BUSINESS:{key:"F2.4.2", value:"业务费-差旅费"},
    COOPERATION:{key:"F2.4.3"   ,value:"业务费-合作与交流费"},
    PROPERTY:{key:"F2.5",   value:"业务费-出版/文献..."},
    CONSULTING:{key:"F2.6",     value:"专家咨询费"},
    OTHER:{key:"F2.7",      value:"业务费-其他支出"},
    LABOR:{key:"F3.1",      value:"劳务费"},
    MANAGEMENT:{key:"F4.1", value:"间接经费-管理费/期间费"},
    PERFORMANCE:{key:"F4.2",    value:"间接经费-绩效支出"},
    STAFF:{key:"F5.1",      value:"人员费"},
    INFRASTRUCTURE:{key:"F6.1", value:"基本建设费"},
};

FEE_TYPE_STD={
    TEST:{key:"F2.2",       value:"业务费-测试化验加工费"},
    POWER:{key:"F2.3",      value:"业务费-燃料动力费"},
    CONFERENCE:{key:"F2.4.1",   value:"业务费-会议费"},
    BUSINESS:{key:"F2.4.2", value:"业务费-差旅费"},
    COOPERATION:{key:"F2.4.3"   ,value:"业务费-合作与交流费"},
    PROPERTY:{key:"F2.5",   value:"业务费-出版/文献..."},
    CONSULTING:{key:"F2.6",     value:"专家咨询费"},
    OTHER:{key:"F2.7",      value:"业务费-其他支出"},
    LABOR:{key:"F3.1",      value:"劳务费"},
    MANAGEMENT:{key:"F4.1", value:"间接经费-管理费/期间费"},
    PERFORMANCE:{key:"F4.2",    value:"间接经费-绩效支出"},
    INFRASTRUCTURE:{key:"F6.1", value:"基本建设费"},
};

CAPITAL_SOURCE={
    SUBSIDY:{key:"SUBSIDY", value:"国拨"},
    SELF:{key:"SELF", value:"自筹"},
};

PAYMENT_STATUS={
    UNPAID:{key:"UNPAID", value:"应付未付"},
    PAID:{key:"PAID", value:"已支付"},
};

PAY_CLASSIFY={
    TASK_FUNCTION:{key:"TASK_FUNCTION", value:"系统任务"},  //预立项阶段系统工程的全部费用归集
    FUNCTION_FEE:{key:"FUNCTION_FEE", value:"系统工程费用"},

    CHARACTER:{key:"CHARACTER", value:"特性开发"},   //这个是在立项后对 特性 的处理
    TASK_SUBP:{key:"TASK_SUBP", value:"子项目任务"},
    TASK_TEST:{key:"TASK_TEST", value:"特性测试"},
    SUBP_MATERIAL:{key:"SUBP_MATERIAL", value:"开发阶段物料"},
    SUBP_FEE:{key:"SUBP_FEE", value:"开发阶段费用"},

    SUB_PROJECT:{key:"SUB_PROJECT", value:"子项目费用"},   //比如:子项目评审手动添加的费用, 主项目评审时添加费用,仍使用这个分类

    PROJECT:{key:"PROJECT", value:"产品任务"},   //所有产品中心发的任务成本 归集
    PROJ_MATERIAL:{key:"PROJ_MATERIAL", value:"产品阶段物料"},
    PROJ_FEE:{key:"PROJ_FEE", value:"产品阶段费用"},
};

BUDGET_STATUS={
    CREATED:{key:"CREATED", value:"创建"},
    ADJUSTED:{key:"ADJUSTED", value:"调整"},
    PAUSED:{key:"PAUSED", value:"暂停"},
    SUSPENDED:{key:"SUSPENDED", value:"中止"},
    COMPLETE:{key:"COMPLETE", value:"完成"},
};

BUDGET_APPLICANT_STATUS={
    SAVE:{key:"SAVE", value:"保存"},
    SUBMIT:{key:"SUBMIT", value:"提交"},
    REFUSED:{key:"REFUSED", value:"拒绝"},
    APPROVED:{key:"APPROVED", value:"批准"},
    COMPLETE:{key:"COMPLETE", value:"完成"},
};

BUDGET_TYPE={
    PRE_PROJECT:{key:"PRE_PROJECT", value:"预立项预算"},
    SUB_PROJECT:{key:"SUB_PROJECT", value:"子项目预算"},
    PROJECT:{key:"PROJECT", value:"项目预算"},
    PRODUCT:{key:"PRODUCT", value:"产品预算"},
};
FIXED_DEPARTMENT={
    PRODUCT_CENTER:{key:"PRODUCT", value:"产品中心"},
    SYSTEM_ENGINEERING_DEPARTMENT:{key:"SYSTEM", value:"系统工程部"},
    RESEARCH_AND_DEVELOPMENT_CENTER:{key:"R_AND_D", value:"研发中心"},
    TEST_DEPARTMENT:{key:"TEST", value:"测试部"},
    QUALITY_DEPARTMENT:{key:"QUALITY", value:"质量部"},
    REGULATION_DEPARTMENT:{key:"REGULATION", value:"法规部"},
    MATERIAL_DEPARTMENT:{key:"MATERIAL", value:"物料部"},
};



