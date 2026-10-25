/**
 * Author: 周朔鹏
 * Date: 2026-10-07
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
 *
 * 主线业务的状态流转图。主线九个业务码的可执行定义是通用的“状态步骤”循环，
 * 实际的状态迁移由 MainlineTransitions 与 LifecycleTransition 的来源集合约束，
 * 这里按代码与《程序流程》分册整理成只读流程图，不参与发布与执行。
 * 节点类型：start、end、endOk、endBad、task、ok、bad、gateway。
 */

import { withLayout } from './flow-layout';

const PALETTE = {
  ok: { fill: '#f0f9eb', stroke: '#67c23a' },
  bad: { fill: '#fef0f0', stroke: '#f56c6c' },
  gateway: { fill: '#fdf6ec', stroke: '#e6a23c' }
};

export const BUSINESS_FLOWS = [
  {
    code: 'MAINLINE',
    name: '主线全景',
    source: '《程序流程》01 至 14 分册',
    note: '从需求到产品的主线阶段。各阶段的状态迁移见对应业务的流程图。',
    nodes: [
      ['start', '开始', 'start'],
      ['demand', '需求识别', 'task'],
      ['define', '预立项与特性定义审批', 'task'],
      ['setup', '立项', 'task'],
      ['decompose', '分解与子项目', 'task'],
      ['develop', '研发启动与执行', 'task'],
      ['review', '特性、子项目与项目评审', 'task'],
      ['closeout', '完成归档', 'ok'],
      ['product', '产品发布与停产', 'ok'],
      ['closed', '关闭或废弃', 'endBad'],
      ['end', '结束', 'endOk']
    ],
    flows: [
      ['start', 'demand', ''],
      ['demand', 'define', '挂接特性'],
      ['define', 'setup', '申请立项'],
      ['setup', 'decompose', 'IPMT 批准'],
      ['decompose', 'develop', '划转特性'],
      ['develop', 'review', '集成评价后发起评审'],
      ['review', 'develop', '驳回或增补'],
      ['review', 'closeout', '通过'],
      ['closeout', 'product', '发布产品'],
      ['product', 'end', '停产'],
      ['closeout', 'define', '修订与迭代'],
      ['define', 'closed', '预立项关闭'],
      ['develop', 'closed', '特性终止或项目废弃']
    ]
  },
  {
    code: 'DEMAND',
    name: '需求',
    source: '《程序流程》01-需求识别',
    note: '已完成的需求不能废弃，处理中、已完成或已废弃的需求不能再保存、提交。',
    nodes: [
      ['start', '开始', 'start'],
      ['SAVED', '已保存\nSAVED', 'task'],
      ['SUBMIT', '已提交\nSUBMIT', 'task'],
      ['HANDLING', '处理中\nHANDLING', 'task'],
      ['attach', '挂接特性', 'gateway'],
      ['abandon', '废弃', 'gateway'],
      ['COMPLETED', '已完成 COMPLETED', 'endOk'],
      ['ABANDON', '废弃 ABANDON', 'endBad'],
      ['DELETED', '已删除', 'endBad']
    ],
    flows: [
      ['start', 'SAVED', '保存'],
      ['SAVED', 'SUBMIT', '提交'],
      ['SAVED', 'HANDLING', '派发需求工单'],
      ['SUBMIT', 'HANDLING', '派发需求工单'],
      ['SAVED', 'attach', ''],
      ['SUBMIT', 'attach', ''],
      ['HANDLING', 'attach', ''],
      ['attach', 'COMPLETED', ''],
      ['SAVED', 'abandon', ''],
      ['SUBMIT', 'abandon', ''],
      ['HANDLING', 'abandon', ''],
      ['abandon', 'ABANDON', ''],
      ['SAVED', 'DELETED', '删除']
    ]
  },
  {
    code: 'CHARACTER',
    name: '特性',
    source: '《程序流程》02、06、08、12 分册；《特性处理全流程方案说明》第 5、7、12 节',
    note: '特性定义审批只接受“提交”来源；预立项特性废弃接受保存、提交、批准、拒绝与迭代记录，'
      + '预立项整体关闭另废弃审批中的特性。预立项分解来源为保存、提交、批准、拒绝，开发域分解来源为立项；'
      + '取消分解时父特性按分解前记录还原。研发与集成任务只能向开发中、集成中、开发完成的特性下发；'
      + '特性评审审结前须结束派生与增补任务，通过前对象下不能有未结束工单。'
      + '开发域特性在立项至开发完成之间可申请终止，子项目产品经理批准后废弃，并经特性主线实例写入。'
      + '项目废弃时未结束的特性废弃，外包原记录保持外包。',
    nodes: [
      ['start', '开始', 'start'],
      ['SAVED', '保存\nSAVED', 'task'],
      ['SUBMIT', '提交\nSUBMIT', 'task'],
      ['APPROVED', '批准\nAPPROVED', 'task'],
      ['SETUP', '申请立项或待复核\nSETUP', 'task'],
      ['REFUSE', '拒绝\nREFUSE', 'bad'],
      ['SETUPED', '立项\nSETUPED', 'task'],
      ['OUT_SOURCE', '外包\nOUT_SOURCE', 'task'],
      ['DEVELOPING', '开发中\nDEVELOPING', 'task'],
      ['DEV_COMPLETE', '开发完成\nDEV_COMPLETE', 'task'],
      ['REVIEW', '特性评审\nREVIEW', 'task'],
      ['INTEGRATION', '集成中\nINTEGRATION', 'task'],
      ['ARCHIVED', '归档 ARCHIVED', 'endOk'],
      ['DECOMPOSED', '分解\nDECOMPOSED', 'task'],
      ['preClose', '预立项关闭', 'gateway'],
      ['terminate', '特性终止', 'gateway'],
      ['DISCARD', '废弃 DISCARD', 'endBad']
    ],
    flows: [
      ['start', 'SAVED', '保存定义'],
      ['SAVED', 'SUBMIT', '提交'],
      ['SUBMIT', 'APPROVED', '审批通过'],
      ['SUBMIT', 'SAVED', '驳回'],
      ['APPROVED', 'SETUP', '申请立项'],
      ['SETUP', 'SETUPED', '立项批准'],
      ['SETUP', 'REFUSE', '立项清单驳回'],
      ['APPROVED', 'SUBMIT', '立项清单驳回'],
      ['REFUSE', 'SAVED', '重新编制'],
      ['SETUP', 'SAVED', '分解驳回或复核不通过'],
      ['APPROVED', 'DECOMPOSED', '预立项分解'],
      ['SETUPED', 'DECOMPOSED', '拆分为子特性'],
      ['DECOMPOSED', 'SETUPED', '取消分解，按记录还原'],
      ['SETUPED', 'OUT_SOURCE', '子项外包'],
      ['OUT_SOURCE', 'ARCHIVED', '外包子项目评审通过'],
      ['SETUPED', 'DEVELOPING', '启动研发'],
      ['DEVELOPING', 'DEV_COMPLETE', '集成评价'],
      ['DEVELOPING', 'INTEGRATION', '下发特性集成工单'],
      ['DEV_COMPLETE', 'INTEGRATION', '下发特性集成工单'],
      ['DEV_COMPLETE', 'REVIEW', '发起特性评审'],
      ['REVIEW', 'ARCHIVED', '通过或有条件通过'],
      ['REVIEW', 'INTEGRATION', '驳回'],
      ['REVIEW', 'DEV_COMPLETE', '增补'],
      ['INTEGRATION', 'DEV_COMPLETE', '集成评价'],
      ['SAVED', 'preClose', ''],
      ['SUBMIT', 'preClose', ''],
      ['APPROVED', 'preClose', ''],
      ['REFUSE', 'preClose', ''],
      ['preClose', 'DISCARD', ''],
      ['SETUPED', 'terminate', ''],
      ['DEVELOPING', 'terminate', ''],
      ['INTEGRATION', 'terminate', ''],
      ['DEV_COMPLETE', 'terminate', ''],
      ['terminate', 'DISCARD', '产品经理批准']
    ]
  },
  {
    code: 'PRE_PROJECT',
    name: '预立项',
    source: '《程序流程》03-预立项；《特性处理全流程方案说明》第 12.1 节',
    note: '关闭限预立项产品经理，最终管理人可以代办。已有项目立项的预立项不能关闭或删除；项目均已撤回后可以关闭。'
      + '名下有未完成工单时须确认级联：取消工单、释放合并来源、废弃未立项特性并还原迭代源记录。'
      + '关闭写关闭标记并保留记录，列表可只看已关闭。',
    nodes: [
      ['start', '开始', 'start'],
      ['DRAFT', '草稿\nDRAFT', 'task'],
      ['ACTIVE', '进行中\nACTIVE', 'task'],
      ['openJobs', '名下有未完成工单', 'gateway'],
      ['CONVERTED', '已立项 CONVERTED', 'endOk'],
      ['CLOSED', '已关闭 CLOSED', 'endBad']
    ],
    flows: [
      ['start', 'DRAFT', '创建'],
      ['DRAFT', 'ACTIVE', '首个特性定义或工单'],
      ['ACTIVE', 'CONVERTED', '项目立项'],
      ['DRAFT', 'CLOSED', '关闭或删除'],
      ['ACTIVE', 'openJobs', '关闭'],
      ['openJobs', 'CLOSED', '无，或确认级联']
    ]
  },
  {
    code: 'PROJECT_SETUP',
    name: '立项',
    source: '《程序流程》04-立项',
    note: '立项批准后，根子项与清单内特性同步写成已立项。研发类与模块类的立项草稿可以撤回，'
      + '限创建人或最终管理人；撤回后清单内特性回到批准。',
    nodes: [
      ['start', '新建立项', 'start'],
      ['moduleStart', '模块立项', 'start'],
      ['SETUP_EDIT', '立项编辑\nSETUP_EDIT', 'task'],
      ['SETUP', '申请立项\nSETUP', 'task'],
      ['SETUPED', '已立项\nSETUPED', 'ok'],
      ['sync', '根子项与特性同步立项', 'endOk'],
      ['CLOSE', '关闭 CLOSE', 'endBad']
    ],
    flows: [
      ['start', 'SETUP_EDIT', ''],
      ['moduleStart', 'SETUP', '直接待批'],
      ['SETUP_EDIT', 'SETUP', '提交'],
      ['SETUP', 'SETUP_EDIT', '驳回'],
      ['SETUP', 'SETUPED', 'IPMT 批准'],
      ['SETUPED', 'sync', ''],
      ['SETUP_EDIT', 'CLOSE', '撤回']
    ]
  },
  {
    code: 'DECOMPOSE',
    name: '分解',
    source: '《程序流程》05-分解与子项目；《特性处理全流程方案说明》第 5 节',
    note: '父级处于评审、完成、增补再评准备或已归档时，不能再新建子项或划转特性。'
      + '开发域分解的子特性审批后待复核，复核通过后父特性保持分解；复核不通过时子特性退回编写、分解工单回到执行人。'
      + '子特性均未批准或立项时才能取消分解工单，取消后子特性废弃、父特性按分解前记录还原、未结束的复核单取消。',
    nodes: [
      ['start', '开始', 'start'],
      ['ROOT', '根子项已立项\nSETUPED', 'task'],
      ['CHILD', '新子项已立项\nSETUPED', 'task'],
      ['OUT_SOURCE', '子项外包\nOUT_SOURCE', 'task'],
      ['check', '特性为已立项且在上级', 'gateway'],
      ['pool', '预算池校验', 'gateway'],
      ['ready', '可启动研发', 'endOk'],
      ['deny', '拒绝划转', 'endBad'],
      ['overflow', '超出预算池', 'endBad'],
      ['DECOMPOSED', '父特性分解\nDECOMPOSED', 'task'],
      ['childSetup', '子特性待复核\nSETUP', 'task'],
      ['recheck', '分解复核', 'gateway'],
      ['childSaved', '子特性退回编写\nSAVED', 'bad'],
      ['cancel', '取消分解：子特性废弃，父特性还原', 'endBad']
    ],
    flows: [
      ['start', 'ROOT', '立项批准'],
      ['ROOT', 'CHILD', '新建子项'],
      ['CHILD', 'OUT_SOURCE', '外包'],
      ['ROOT', 'check', '划转特性'],
      ['CHILD', 'check', '划转特性'],
      ['check', 'pool', '是'],
      ['check', 'deny', '否'],
      ['pool', 'ready', '通过'],
      ['pool', 'overflow', '超池且策略为拒绝'],
      ['ROOT', 'DECOMPOSED', '拆分父特性'],
      ['DECOMPOSED', 'childSetup', '子特性审批通过'],
      ['childSetup', 'recheck', ''],
      ['recheck', 'check', '通过，子特性已立项'],
      ['recheck', 'childSaved', '不通过'],
      ['childSaved', 'childSetup', '修改后重新审批'],
      ['DECOMPOSED', 'cancel', '取消分解工单']
    ]
  },
  {
    code: 'DEVELOP',
    name: '研发启动',
    source: '《程序流程》06-研发启动与执行',
    note: '启动研发须为项目经理或管理员；评审进行中的子项不能再派发五类任务。'
      + '研发与特性集成工单只能向开发中、集成中、开发完成的特性下发，下发特性集成工单时特性写成集成中。',
    nodes: [
      ['start', '启动研发', 'start'],
      ['target', '研发对象', 'gateway'],
      ['DEVELOPING', '特性开发中\nDEVELOPING', 'task'],
      ['DEV_COMPLETE', '特性开发完成\nDEV_COMPLETE', 'ok'],
      ['ONGOING', '子项执行中\nONGOING', 'task'],
      ['MAIN_ONGOING', '主项执行中\nONGOING', 'task'],
      ['keep', '主项保持已立项', 'end'],
      ['outsource', '外包子项不进入内部研发', 'end'],
      ['toCharacterReview', '进入特性评审', 'endOk'],
      ['toSubReview', '进入子项目评审', 'endOk'],
      ['toMainReview', '进入项目评审', 'endOk']
    ],
    flows: [
      ['start', 'target', ''],
      ['target', 'DEVELOPING', '特性已立项'],
      ['target', 'ONGOING', '子项已立项'],
      ['target', 'MAIN_ONGOING', '主项且允许执行中'],
      ['target', 'keep', '主项默认'],
      ['target', 'outsource', '子项外包'],
      ['DEVELOPING', 'DEV_COMPLETE', '集成评价'],
      ['DEV_COMPLETE', 'toCharacterReview', ''],
      ['ONGOING', 'toSubReview', ''],
      ['MAIN_ONGOING', 'toMainReview', '']
    ]
  },
  {
    code: 'CLOSEOUT',
    name: '完成归档',
    source: '《程序流程》09、10、12 分册',
    note: '草稿、待批、评审中、已完成或已归档的项目不能废弃；仅已完成与关闭的项目可以归档，重复归档幂等成功。'
      + '归档限主项目的产品经理或监管委员，对象下仍有未结束工单时拒绝；评审通过前同样不能有未结束工单。'
      + '根子项的特性评审通过或根子项特性终止后，根下其余特性均已结束、其余子项目均已完成、归档或关闭，才完成主项目。'
      + '子项目评审通过时子项目物料清单锁定，外包子项目评审通过时外包原记录归档；'
      + '项目废弃时外包原记录保持外包，并向对方机构项目经理发定义更新通知。',
    nodes: [
      ['start', '立项批准', 'start'],
      ['draftStart', '立项草稿', 'start'],
      ['RUN', '执行中\nSETUPED / ONGOING / OUT_SOURCE', 'task'],
      ['SETUP_EDIT', '立项编辑\nSETUP_EDIT', 'task'],
      ['REVIEWING', '子项目或主项目评审中\nREVIEW_SUBP / REVIEW_PRO', 'task'],
      ['DEV_COMPLETE', '增补材料\nDEV_COMPLETE', 'task'],
      ['COMPLETE', '已完成\nCOMPLETE', 'ok'],
      ['CLOSE', '关闭\nCLOSE', 'bad'],
      ['ARCHIVED', '已归档 ARCHIVED', 'endOk']
    ],
    flows: [
      ['start', 'RUN', ''],
      ['draftStart', 'SETUP_EDIT', ''],
      ['RUN', 'REVIEWING', '发起评审'],
      ['REVIEWING', 'COMPLETE', '通过或有条件通过'],
      ['REVIEWING', 'DEV_COMPLETE', '增补'],
      ['DEV_COMPLETE', 'REVIEWING', '增补后再评'],
      ['DEV_COMPLETE', 'COMPLETE', '原主单审结'],
      ['REVIEWING', 'RUN', '驳回'],
      ['RUN', 'CLOSE', '废弃'],
      ['DEV_COMPLETE', 'CLOSE', '废弃'],
      ['SETUP_EDIT', 'CLOSE', '撤回'],
      ['COMPLETE', 'ARCHIVED', '归档'],
      ['CLOSE', 'ARCHIVED', '归档']
    ]
  },
  {
    code: 'PRODUCT',
    name: '产品',
    source: '《程序流程》13-产品发布与停产',
    note: '项目未完成不能发布；同项目台账均已停产时不能再次发布；已发布或已停产的台账不能删除。'
      + '从项目发布限项目的产品经理；停产限台账的产品经理，台账未填时取来源项目的产品经理；最终管理人可以代办。',
    nodes: [
      ['start', '项目评审通过', 'start'],
      ['draftStart', '新建台账', 'start'],
      ['COMPLETE', '项目已完成\nCOMPLETE', 'task'],
      ['DRAFT', '草稿\nDRAFT', 'task'],
      ['ACTIVE', '已发布\nACTIVE', 'ok'],
      ['EOL', '停产 EOL', 'endOk'],
      ['deleted', '已删除', 'endBad']
    ],
    flows: [
      ['start', 'COMPLETE', ''],
      ['draftStart', 'DRAFT', ''],
      ['COMPLETE', 'ACTIVE', '从项目发布'],
      ['DRAFT', 'ACTIVE', '再次发布'],
      ['ACTIVE', 'EOL', '停产'],
      ['DRAFT', 'deleted', '删除']
    ]
  },
  {
    code: 'REVISION',
    name: '修订',
    source: '《程序流程》14-修订与迭代',
    note: '修订确认使版本线编号加一，项目锁定版本不变；纳入项目经变更评审合入后锁定版本才更新。'
      + '修订只能从现行主记录发起；修订工单由产品经理或子项目经理派发，最终管理人可以代派；修订确认后不能再取消工单。'
      + '立项锁定版本线时，未合入的预立项修订分支放弃，编辑记录废弃，修订工单取消。'
      + '迭代记录废弃或迭代工单取消后，同一源记录下没有其他有效迭代记录时，旧特性按迭代前状态还原。'
      + 'CBB 本身不迭代：需要新的 CBB 时迭代其承载特性，评审通过后重新发布为独立的新 CBB，原 CBB 不变。',
    nodes: [
      ['start', '开始', 'start'],
      ['intent', '修订意图', 'gateway'],
      ['ITERATION', '修订工单\nITERATION', 'task'],
      ['branch', '编辑记录与修订分支', 'task'],
      ['confirm', '派发人确认', 'gateway'],
      ['head', '版本线新版本\n编号加一，锁定不变', 'ok'],
      ['adopt', '纳入分支\n发起变更评审', 'task'],
      ['reopen', '纳入分支回到编辑中', 'bad'],
      ['merge', '合入：锁定版本更新，预算调整', 'endOk'],
      ['approve', '修订审批', 'gateway'],
      ['sysMerge', '系统合入主线', 'endOk'],
      ['one', '单特性迭代', 'task'],
      ['updated', '旧特性 UPDATED\n新记录 UPDATE', 'task'],
      ['project', '项目升版', 'task'],
      ['prePro', '新预立项', 'task'],
      ['define', '重新走定义与审批', 'endOk'],
      ['iterRollback', '迭代记录废弃\n旧特性还原', 'end'],
      ['lockAbandon', '修订失效 ABANDONED', 'endBad']
    ],
    flows: [
      ['start', 'intent', ''],
      ['intent', 'ITERATION', '同版本改定义'],
      ['ITERATION', 'branch', ''],
      ['branch', 'confirm', '开发域提交'],
      ['confirm', 'head', '确认'],
      ['confirm', 'branch', '退回'],
      ['head', 'adopt', '纳入项目'],
      ['adopt', 'merge', '评审通过'],
      ['adopt', 'reopen', '增补或驳回'],
      ['reopen', 'adopt', '重新发起评审'],
      ['branch', 'approve', '预立项提交'],
      ['approve', 'sysMerge', '通过'],
      ['approve', 'branch', '驳回'],
      ['branch', 'lockAbandon', '立项锁定版本线'],
      ['intent', 'one', '单特性换预立项'],
      ['one', 'updated', ''],
      ['updated', 'define', ''],
      ['updated', 'iterRollback', '废弃迭代记录或取消工单'],
      ['intent', 'project', '项目升版'],
      ['project', 'prePro', ''],
      ['prePro', 'define', '']
    ]
  }
];

/** 状态码在该业务流程图中的中文名；没有对应节点时返回空。 */
export function statusLabel(code, status) {
  const flow = businessFlow(code);
  if (!flow || !status) {
    return '';
  }
  const node = flow.nodes.filter(function (item) { return item[0] === status; })[0];
  return node ? node[1].split('\n')[0] : '';
}

export function businessFlow(code) {
  return BUSINESS_FLOWS.filter(function (item) { return item.code === code; })[0] || null;
}

export function businessXml(flow) {
  const colors = {};
  const body = [];
  flow.nodes.forEach(function (node) {
    const id = node[0];
    const name = escapeXml(node[1]);
    const type = node[2];
    if (type === 'start') {
      body.push('<bpmn:startEvent id="' + id + '" name="' + name + '"/>');
    } else if (type === 'end' || type === 'endOk' || type === 'endBad') {
      body.push('<bpmn:endEvent id="' + id + '" name="' + name + '"/>');
    } else if (type === 'gateway') {
      body.push('<bpmn:exclusiveGateway id="' + id + '" name="' + name + '"/>');
    } else {
      body.push('<bpmn:task id="' + id + '" name="' + name + '"/>');
    }
    if (type === 'ok' || type === 'endOk') {
      colors[id] = PALETTE.ok;
    } else if (type === 'bad' || type === 'endBad') {
      colors[id] = PALETTE.bad;
    } else if (type === 'gateway') {
      colors[id] = PALETTE.gateway;
    }
  });
  flow.flows.forEach(function (edge, index) {
    body.push('<bpmn:sequenceFlow id="flow_' + (index + 1) + '" sourceRef="' + edge[0] + '" targetRef="' + edge[1] + '"'
      + (edge[2] ? ' name="' + escapeXml(edge[2]) + '"' : '') + '/>');
  });
  const xml = '<?xml version="1.0" encoding="UTF-8"?>'
    + '<bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"'
    + ' id="Definitions_' + flow.code + '" targetNamespace="http://euipd.com/flow/business">'
    + '<bpmn:process id="business_' + flow.code + '" name="' + escapeXml(flow.name) + '" isExecutable="false">'
    + body.join('')
    + '</bpmn:process>'
    + '</bpmn:definitions>';
  return withLayout(xml, colors);
}

function escapeXml(text) {
  return String(text)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/\n/g, '&#10;');
}
