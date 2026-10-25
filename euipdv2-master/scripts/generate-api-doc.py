#!/usr/bin/env python3
"""从各微服务的 Controller 源码生成 HTTP 接口文档，输出到 doc/api/。

用法（在 euipdv2-master/ 目录）：python3 scripts/generate-api-doc.py
接口说明优先取方法上的注释；没有注释时按下方术语表由方法名生成。
鉴权要求按网关 security/module-path.yml 与 GatewayModulePathMatcher 的规则推断。
"""
import os
import re
import sys
from collections import OrderedDict

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, 'doc', 'api')

SERVICES = OrderedDict([
    ('system', {'ctx': '/system', 'doc': 'system.md', 'title': 'system 服务', 'port': 18001,
                'desc': '登录认证、令牌、机构、用户、角色、资源权限、验证码与短信。'}),
    ('customer', {'ctx': '/customer', 'doc': 'customer.md', 'title': 'customer 服务', 'port': 18005,
                  'desc': '研发业务：需求、特性、产品、项目与子项目、工单、评审、里程碑、预算与费用、质量与缺陷、物料仓库、工时绩效、流程与邮件等。'}),
    ('file2', {'ctx': '/file', 'doc': 'file.md', 'title': 'file2 服务', 'port': 18003,
               'desc': '文件记录、对象存储上传与签名下载、富文本资源、Word 导入与在线查看（WOPI）。'}),
    ('ftp', {'ctx': '/ftp', 'doc': 'ftp.md', 'title': 'ftp 服务', 'port': 18007,
             'desc': '大文件经 FTP 服务器中转上传。'}),
])

# Controller 未声明 BUSINESS_NAME 或名称不准确时使用的分组名
GROUP_NAMES = {
    'AppInfoController': '应用信息', 'RdmsUserController': '用户', 'RdmsUserResourceController': '用户资源',
    'RdmsUserRoleController': '用户角色', 'RdmsCustomerController': '机构', 'RdmsCustomerRoleController': '机构角色',
    'RdmsCustomerConfigController': '机构配置', 'VodController': '视频点播',
    'BaselineController': '项目基线', 'BizRichDocController': '业务富文本文档', 'CbbController': 'CBB 共用基础模块',
    'CbbRichDocController': 'CBB 富文本文档', 'CharacterRichDocController': '特性富文本文档',
    'DepartmentController': '部门', 'FileApplyFlowController': '文件申请流程', 'FlowController': '流程定义与实例',
    'JobItemController': '工单', 'ManhourController': '工时', 'ManhourDeclareController': '工时申报',
    'MaterialStockController': '物料库存', 'MaterialWarehouseController': '物料仓库',
    'ProjectRichDocController': '项目富文本文档', 'RdmsRichDocImportController': 'Word 导入',
    'RdmsRichRenderController': '富文本渲染与在线查看', 'WopiController': 'WOPI（Collabora 回调）',
    'DirectorController': '产品总监与系统工程总监', 'WebController': '公开页面数据',
}

VERBS = {
    'get': '查询', 'list': '查询', 'find': '查询', 'query': '查询', 'search': '搜索', 'load': '加载',
    'save': '保存', 'create': '新建', 'add': '添加', 'update': '修改', 'modify': '修改', 'edit': '编辑',
    'delete': '删除', 'del': '删除', 'remove': '移除', 'submit': '提交', 'approve': '批准', 'reject': '驳回',
    'refuse': '拒绝', 'complete': '完成', 'confirm': '确认', 'check': '检查', 'set': '设置', 'count': '统计',
    'upload': '上传', 'download': '下载', 'export': '导出', 'import': '导入', 'send': '发送', 'reset': '重置',
    'cancel': '取消', 'close': '关闭', 'open': '打开', 'restore': '恢复', 'lock': '锁定', 'merge': '合并',
    'publish': '发布', 'release': '发布', 'archive': '归档', 'abandon': '放弃', 'terminate': '终止',
    'withdraw': '撤回', 'evaluate': '评估', 'examine': '审核', 'review': '评审', 'apply': '申请',
    'assign': '指派', 'adjust': '调整', 'init': '初始化', 'refresh': '刷新', 'switch': '切换', 'login': '登录',
    'logout': '退出登录', 'validate': '校验', 'parse': '解析', 'render': '渲染', 'preview': '预览',
    'move': '移动', 'mark': '标记', 'sort': '排序', 'append': '追加', 'join': '加入', 'borrow': '借用',
    'accept': '接受', 'agree': '同意', 'enable': '启用', 'disable': '停用', 'decompose': '分解',
    'recheck': '复核', 'handle': '处理', 'deal': '处理', 'sync': '同步', 'change': '变更', 'has': '判断是否有',
    'is': '判断是否', 'can': '判断能否', 'play': '播放', 'start': '启动', 'restart': '重启', 'checkout': '检出',
    'rebase': '变基', 'declare': '申报', 'receive': '接收', 'supervise': '督办', 'fill': '填写', 'cal': '计算',
    'calc': '计算', 'sum': '汇总', 'generate': '生成', 'select': '选择', 'discard': '丢弃', 'show': '显示',
    'turn': '移交', 'pass': '通过', 'exist': '判断是否存在', 'write': '写入', 'read': '读取', 'copy': '复制',
}

# 多词组合优先于单词
PHRASES = [
    (('pre', 'project'), '预立项'), (('sub', 'project'), '子项目'), (('sub', 'projects'), '子项目'),
    (('job', 'items'), '工单'), (('job', 'item'), '工单'), (('bug', 'feedback'), '缺陷'),
    (('man', 'hour'), '工时'), (('rich', 'doc'), '富文本文档'), (('sec', 'level'), '保密等级'),
    (('work', 'calendar'), '工作日历'), (('turn', 'over'), '移交'), (('take', 'part', 'in'), '参与'),
    (('where', 'used'), '被引用位置'), (('login', 'name'), '登录名'), (('next', 'node'), '下一节点'),
    (('image', 'code'), '图片验证码'), (('public', 'key'), '公钥'), (('access', 'token'), '访问令牌'),
    (('job', 'level'), '职级'), (('primary', 'key'), '主键'), (('key', 'word'), '关键字'),
    (('out', 'sourcing'), '外协'), (('out', 'source'), '外协'), (('work', 'load'), '工作量'),
    (('test', 'task'), '测试任务'), (('signed', 'url'), '签名地址'), (('function', 'tree'), '功能树'),
    (('at', 'glance'), '概览'), (('flow', 'def'), '流程定义'), (('flow', 'inst'), '流程实例'),
    (('re', 'issue'), '重新下发'), (('v', '2'), 'V2'), (('include', 'next', 'level'), '（含下一级）'),
    (('budget', 'direct'), '直接立项预算'), (('direct', 'setup'), '直接立项'), (('module', 'setup'), '模块立项'),
    (('setup', 'application'), '立项申请'), (('badge', 'summary'), '角标汇总'), (('home', 'page'), '首页'),
]

NOUNS = {
    'customer': '机构', 'project': '项目', 'projects': '项目', 'subproject': '子项目', 'subprojects': '子项目',
    'subp': '子项目', 'character': '特性', 'characters': '特性', 'jobitem': '工单', 'jobitems': '工单',
    'job': '工作', 'item': '条目', 'items': '条目', 'material': '物料', 'materials': '物料', 'user': '用户',
    'file': '文件', 'files': '文件', 'budget': '预算', 'system': '系统', 'quality': '质量', 'doc': '文档',
    'docs': '文档', 'cbb': 'CBB', 'bom': 'BOM', 'tree': '树', 'category': '分类', 'application': '申请',
    'task': '任务', 'test': '测试', 'bugfeedback': '缺陷', 'bug': '缺陷', 'fee': '费用', 'fees': '费用',
    'summary': '汇总', 'summaries': '汇总', 'worker': '成员', 'workers': '成员', 'exe': '执行', 'demand': '需求',
    'manager': '经理', 'detail': '详情', 'info': '信息', 'list': '列表', 'num': '数量', 'count': '数量',
    'product': '产品', 'pjm': '项目经理', 'course': '课程', 'develop': '开发', 'developing': '开发中',
    'development': '开发', 'manhour': '工时', 'code': '编码', 'draft': '草稿', 'status': '状态',
    'notify': '通知', 'department': '部门', 'departments': '部门', 'property': '资产', 'execution': '执行',
    'setup': '立项', 'setuped': '已立项', 'dlg': '', 'assist': '协办', 'next': '下一', 'record': '记录',
    'records': '记录', 'completed': '已完成', 'key': '键', 'function': '功能', 'role': '角色', 'roles': '角色',
    'process': '处理', 'gantt': '甘特图', 'handling': '处理中', 'milestone': '里程碑', 'milestones': '里程碑',
    'versions': '版本', 'version': '版本', 'pdm': '产品经理', 'level': '级别', 'performance': '绩效',
    'password': '口令', 'admin': '管理员', 'feedback': '反馈', 'lineage': '版本谱系', 'define': '定义',
    'data': '数据', 'module': '模块', 'node': '节点', 'plan': '计划', 'mail': '邮件', 'name': '名称',
    'main': '主', 'sysgm': '系统工程总监', 'pdgm': '产品总监', 'tgm': '测试总监', 'qcgm': '质量总监',
    'fggm': '法规总监', 'tm': '测试主管', 'sm': '系统工程师', 'compare': '对比', 'approved': '已批准',
    'ids': 'ID 列表', 'id': 'ID', 'archived': '已归档', 'str': '字符串', 'config': '配置', 'mng': '管理',
    'flow': '流程', 'iteration': '迭代', 'issue': '问题', 'statistics': '统计', 'word': 'Word',
    'teacher': '讲师', 'parent': '上级', 'attachments': '附件', 'attachment': '附件', 'children': '下级',
    'link': '关联', 'related': '相关', 'result': '结果', 'signed': '已签名', 'url': '地址',
    'capitalization': '资本化', 'expenditure': '支出', 'resource': '资源', 'boss': '最终管理人',
    'chapter': '篇章', 'self': '本人', 'stock': '库存', 'reference': '参考文献', 'tenant': '租户',
    'api': '接口', 'approval': '审批', 'stage': '阶段', 'underline': '下属', 'analysis': '分析',
    'suggest': '建议', 'research': '研发', 'hmi': '人机界面', 'dto': '', 'int': '', 'integration': '集成',
    'branch': '分支', 'branches': '分支', 'content': '内容', 'contents': '内容', 'writer': '编写人',
    'ipmt': 'IPMT', 'tested': '已测试', 'box': '框', 'sap': 'SAP', 'section': '小节', 'warehouse': '仓库',
    'warehouses': '仓库', 'owner': '负责人', 'approver': '审批人', 'workload': '工作量', 'remain': '剩余',
    'web': '页面', 'ui': '界面', 'policy': '策略', 'sourcing': '外协', 'distribution': '分配', 'keyword': '关键字',
    'cost': '成本', 'team': '团队', 'super': '超级', 'origin': '来源', 'revision': '修订', 'member': '成员',
    'members': '成员', 'diagram': '图', 'history': '历史', 'synchronization': '同步', 'jobs': '工作',
    'active': '活动', 'selected': '已选', 'logs': '日志', 'current': '当前', 'eol': '停产', 'my': '我的',
    'oss': '对象存储', 'address': '地址', 'token': '令牌', 'contact': '联系人', 'contacts': '联系人',
    'image': '图片', 'video': '视频', 'last': '最近', 'account': '账号', 'attachable': '可关联',
    'snapshot': '快照', 'snapshots': '快照', 'type': '类型', 'batch': '批量', 'inbound': '入库',
    'initial': '初始', 'jwks': 'JWKS 公钥集', 'public': '公开', 'diff': '差异', 'access': '访问', 'workflow': '工作流',
    'loginname': '登录名', 'inst': '实例', 'default': '默认', 'mobile': '手机号', 'salt': '盐值', 'authed': '已授权',
    'vod': '视频点播', 'baseline': '基线', 'payment': '付款', 'pool': '池', 'bua': 'BUA', 'plus': '扩展',
    'tech': '技术', 'candidates': '候选人', 'page': '分页', 'decomposed': '已分解', 'testing': '测试中',
    'opinion': '意见', 'adoption': '采纳', 'org': '组织', 'unassigned': '未指派', 'superior': '上级',
    'handler': '处理人', 'route': '路由', 'capability': '能力', 'instance': '实例', 'schedule': '计划',
    'gitlab': 'GitLab', 'reviews': '评审', 'review': '评审', 'creater': '创建人', 'aux': '辅助', 'plain': '明文',
    'time': '时间', 'limit': '限制', 'rechecked': '已复核', 'message': '消息', 'standard': '标准',
    'edited': '已编辑', 'common': '通用', 'badge': '角标', 'monthly': '月度', 'report': '报告',
    'staff': '员工', 'scope': '范围', 'person': '人员', 'trend': '趋势', 'overview': '概览', 'workbench': '工作台',
    'outsourcing': '外协', 'board': '看板', 'charge': '负责', 'template': '模板', 'work': '工作',
    'calendar': '日历', 'more': '更多', 'abs': '绝对', 'path': '路径', 'asset': '资源', 'viewer': '查看器',
    'wopi': 'WOPI', 'single': '单个', 'ftp': 'FTP', 'app': '应用', 'kaptcha': '验证码', 'sms': '短信',
    'saved': '已保存', 'submitted': '已提交', 'rows': '行', 'options': '选项', 'dashboard': '仪表盘',
    'year': '年度', 'hour': '小时', 'all': '全部', 'simple': '简要', 'auth': '授权', 'flag': '标志',
    'apply': '申请', 'direct': '直接', 'complete': '完成', 'reject': '驳回', 'approve': '批准',
    'examine': '审核', 'archive': '归档', 'evaluate': '评估', 'confirm': '确认', 'abandon': '放弃',
    'close': '关闭', 'preview': '预览', 'borrow': '借用', 'change': '变更', 'adjust': '调整', 'reset': '重置',
    'send': '发送', 'recheck': '复核', 'merge': '合并', 'terminate': '终止', 'cancel': '取消',
    'refuse': '拒绝', 'import': '导入', 'export': '导出', 'upload': '上传', 'download': '下载',
    'release': '发布', 'publish': '发布', 'accept': '接受', 'decompose': '分解', 'append': '追加',
    'restore': '恢复', 'lock': '锁定', 'compare2': '对比', 'pre': '预', 'sub': '子', 'man': '人',
    'rich': '富文本', 'sec': '保密', 'property2': '', 'material2': '', 'payment2': '',
    'and': '和', 'of': '的', 'for': '', 'to': '', 'with': '含', 'from': '来自', 'in': '', 'as': '',
    'not': '未', 'be': '', 'when': '时', 'where': '', 'use': '使用', 'used': '使用', 'now': '当前',
    'part': '', 'take': '', 'over': '', 'join': '加入', 'move': '移动', 'mark': '标记', 'sort': '排序',
    'switch': '切换', 'login': '登录', 'logout': '退出登录', 'refresh': '刷新', 'enable': '启用',
    'disable': '停用', 'start': '启动', 'restart': '重启', 'show': '显示', 'play': '播放', 'parse': '解析',
    'render': '渲染', 'validate': '校验', 'init': '初始化', 'declare': '申报', 'receive': '接收',
    'supervise': '督办', 'agree': '同意', 'withdraw': '撤回', 'deal': '处理', 'assign': '指派',
    'checkout': '检出', 'rebase': '变基', 'discard': '丢弃', 'fill': '填写', 'sum': '汇总', 'cal': '计算',
    'return': '归还', 'turn': '移交', 'can': '可', 'is': '是否', 'has': '有', 'exist': '存在', 'v': 'V',
    'interface': '接口', 'manage': '管理', 'preproject': '预立项', 'include': '含', 'merged': '已合并',
    'open': '开放', 'glance': '概览', 'complete': '已完成', 'handling': '处理中', 'check': '检查', 'search': '搜索', 'query': '查询', 'get': '查询', 'save': '保存',
    'create': '新建', 'add': '添加', 'update': '修改', 'modify': '修改', 'edit': '编辑', 'delete': '删除',
    'del': '删除', 'remove': '移除', 'submit': '提交', 'set': '设置', 'load': '加载', 'find': '查询',
    'handle': '处理', 'sync': '同步', 'pass': '通过', 'select': '选择', 'generate': '生成', 'copy': '复制',
}

SUFFIX_FIRST = {'list', 'num', 'count', 'info', 'detail', 'tree', 'status', 'summary'}

MAP_RE = re.compile(r'@(Get|Post|Put|Delete|Patch|Request)Mapping(?:\(((?:[^()]|\([^()]*\))*)\))?')
METHOD_RE = re.compile(
    r'((?:[ \t]*(?://[^\n]*|/\*\*(?:(?!\*/).)*\*/)\s*\n)*)'
    r'((?:[ \t]*@[\w.]+(?:\((?:[^()]|\([^()]*\))*\))?\s*\n)+)'
    r'[ \t]*public\s+([\w<>\[\], ?.]+?)\s+(\w+)\s*\(((?:[^()]|\((?:[^()]|\([^()]*\))*\))*)\)', re.S)


def strip_blocks(src):
    """去掉非 Javadoc 的块注释（含被整段注释掉的旧接口），保留字符串与 Javadoc。"""
    out, i, n = [], 0, len(src)
    while i < n:
        c = src[i]
        if c in '"\'':
            j = i + 1
            while j < n and src[j] != c:
                j += 2 if src[j] == '\\' else 1
            out.append(src[i:j + 1])
            i = j + 1
            continue
        if src.startswith('//', i):
            j = src.find('\n', i)
            j = n if j < 0 else j
            out.append(src[i:j])
            i = j
            continue
        if src.startswith('/*', i):
            j = src.find('*/', i + 2)
            j = n if j < 0 else j + 2
            if src.startswith('/**', i) and not src.startswith('/**/', i):
                out.append(src[i:j])
            else:
                out.append('\n' * src.count('\n', i, j))
            i = j
            continue
        out.append(c)
        i += 1
    return ''.join(out)


def ann_paths(args):
    if not args:
        return ['']
    m = re.search(r'(?:value|path)\s*=\s*(\{[^}]*\}|"[^"]*")', args) or re.match(r'\s*(\{[^}]*\}|"[^"]*")', args)
    if not m:
        return ['']
    return re.findall(r'"([^"]*)"', m.group(1)) or ['']


def ann_method(kind, args):
    if kind != 'Request':
        return kind.upper()
    m = re.findall(r'RequestMethod\.(\w+)', args or '')
    return '/'.join(m) if m else 'ANY'


def join_path(*parts):
    p = ''
    for x in parts:
        if not x:
            continue
        p += x if x.startswith('/') else '/' + x
    return p


def parse_controllers():
    endpoints = []
    for svc, conf in SERVICES.items():
        base_dir = os.path.join(ROOT, svc, 'src', 'main', 'java')
        for dp, _, fs in os.walk(base_dir):
            for f in sorted(fs):
                if not f.endswith('.java'):
                    continue
                path = os.path.join(dp, f)
                src = strip_blocks(open(path, encoding='utf-8').read())
                if not re.search(r'@(Rest)?Controller\b', src):
                    continue
                cm = re.search(r'((?:@\w+(?:\([^)]*\))?\s*)*)public\s+(abstract\s+)?class\s+(\w+)', src)
                cls = cm.group(3)
                bases = ['']
                r = re.search(r'@RequestMapping\s*\((.*?)\)\s*$', cm.group(1), re.M | re.S)
                if r:
                    bases = ann_paths(r.group(1))
                body = src[cm.end():]
                if cm.group(2):
                    subs = list(re.finditer(
                        r'@RequestMapping\s*\(([^)]*)\)\s*public\s+static\s+class\s+\w+\s+extends\s+' + cls, src))
                    bases = [p for s in subs for p in ann_paths(s.group(1))]
                    if subs:
                        body = src[cm.end():subs[0].start()]
                bn = re.search(r'BUSINESS_NAME\s*=\s*"([^"]*)"', src)
                group = GROUP_NAMES.get(cls) or (bn.group(1) if bn else cls)
                for mm in METHOD_RE.finditer(body):
                    comm, anns, ret, name, params = mm.groups()
                    am = MAP_RE.search(anns)
                    if not am:
                        continue
                    rs = re.search(r'RequiresScope\(((?:[^()]|\([^()]*\))*)\)', anns)
                    endpoints.append({
                        'svc': svc, 'cls': cls, 'group': group, 'file': os.path.relpath(path, ROOT),
                        'http': ann_method(am.group(1), am.group(2)), 'name': name,
                        'ret': ret.strip(), 'params': parse_params(params),
                        'comment': clean_comment(comm),
                        'scope': re.findall(r'"([A-Z_]+:[A-Z_*]+)"', rs.group(1)) if rs else [],
                        'paths': [join_path(conf['ctx'], b, p) for b in bases for p in ann_paths(am.group(2))],
                    })
    return endpoints


def split_params(params):
    parts, depth, cur = [], 0, ''
    for ch in params:
        if ch in '(<':
            depth += 1
        elif ch in ')>':
            depth -= 1
        if ch == ',' and depth == 0:
            parts.append(cur)
            cur = ''
        else:
            cur += ch
    return parts + [cur]


def parse_params(params):
    out = []
    for p in split_params(params):
        p = p.strip()
        if not p:
            continue
        a = re.search(r'@(PathVariable|RequestBody|RequestParam|RequestPart|RequestHeader)(?:\(([^)]*)\))?', p)
        t = re.sub(r'@[\w.]+(\((?:[^()]|\([^()]*\))*\))?\s*', '', p).replace('final ', '').strip()
        parts = t.rsplit(' ', 1)
        name = parts[-1]
        if a and a.group(2):
            alias = re.search(r'(?:(?:value|name)\s*=\s*)?"([^"]+)"', a.group(2))
            if alias:
                name = alias.group(1)
        out.append({'ann': a.group(1) if a else '', 'type': parts[0] if len(parts) > 1 else t, 'name': name})
    return out


def clean_comment(comm):
    lines = []
    for line in comm.splitlines():
        s = re.sub(r'^\s*(//+|/\*\*|\*/|\*)\s?', '', line).replace('*/', '').strip()
        if not s or s.startswith('@') or re.search(r'[;{}]\s*$|^\w+\s*\(|=\s*\S', s):
            continue
        lines.append(s)
    lines = [re.sub(r'<[^>]+>', '', x).strip() for x in lines]
    lines = [x for x in lines if re.search(r'[\u4e00-\u9fa5]', x)]
    if not lines:
        return ''
    text = lines[0]
    if len(text) < 6 and len(lines) > 1:
        text += '：' + lines[1]
    return text if len(text) <= 90 else text[:88] + '…'


def tokens(name):
    raw = re.findall(r'[A-Z]?[a-z]+|[A-Z]+(?![a-z])|\d+', name.replace('_', ' '))
    return [t.lower() for t in raw]


def translate(words):
    out, i = [], 0
    while i < len(words):
        for ph, zh in PHRASES:
            if tuple(words[i:i + len(ph)]) == ph:
                out.append(zh)
                i += len(ph)
                break
        else:
            w = words[i]
            out.append(NOUNS.get(w, w if not w.isdigit() else w))
            i += 1
    text = ''
    for part in out:
        if not part:
            continue
        if text and re.match(r'[A-Za-z0-9]', part[0]) and re.match(r'[A-Za-z0-9]', text[-1]):
            text += ' '
        text += part
    return text


OVERRIDES = {
    '/system/auth/login': '登录，返回访问令牌与刷新令牌',
    '/system/auth/refresh': '用刷新令牌换取新的访问令牌',
    '/system/auth/logout': '退出登录并注销令牌',
    '/system/auth/tenant/switch': '切换当前机构，返回新的登录结果',
}


def describe(ep, group):
    for path in ep['paths']:
        if path in OVERRIDES:
            return OVERRIDES[path]
    if ep['comment']:
        return ep['comment'].rstrip('。')
    base, _, qual = ep['name'].partition('_')
    words = tokens(base)
    qual = translate(tokens(qual)) if qual else ''
    obj_default = re.sub(r'(管理|处理)$', '', group)
    cond = []
    if 'by' in words:
        k = words.index('by')
        words, cond = words[:k], words[k + 1:]
    verb = ''
    if words and words[0] in VERBS:
        verb = VERBS[words[0]]
        first = words[0]
        words = words[1:]
        if first == 'list' and (not words or words[-1] not in SUFFIX_FIRST):
            words = words + ['list']
    obj = translate(words)
    if not obj or obj in ('列表', '信息', '详情', '数量'):
        obj = obj_default + obj
    if not verb and ep['http'] == 'GET':
        verb = '查询'
    text = f'{verb}{obj}'
    if cond:
        text = f'按{translate(cond)}{text}'
    if qual:
        text += f'（{qual}）'
    return text


def load_gateway_rules():
    yml = open(os.path.join(ROOT, 'gateway', 'src', 'main', 'resources', 'security', 'module-path.yml'),
               encoding='utf-8').read()
    mappings = re.findall(r'-\s*prefix:\s*(\S+)\s*\n\s*module:\s*(\S+)', yml)
    mappings.sort(key=lambda m: -len(m[0]))

    def section(name):
        m = re.search(r'^' + name + r':\s*\n((?:\s+(?:-.*|#.*)\n?)*)', yml, re.M)
        return [x.strip() for x in re.findall(r'^\s*-\s*(\S+)', m.group(1), re.M)] if m else []
    return mappings, set(section('whitelist')), section('whitelist-prefixes')


def infer_operation(method, path):
    p = path.lower()
    if method == 'GET':
        if '/export' in p:
            return 'EXPORT'
        return 'DOWNLOAD' if '/download' in p else 'VIEW'
    if method in ('PUT', 'PATCH'):
        return 'UPDATE'
    if method == 'DELETE':
        return 'DELETE'
    if 'signed-url' in p:
        return 'VIEW'
    if '/download' in p:
        return 'DOWNLOAD'
    if any(x in p for x in ('/save', '/create', '/add', '/setup-application', '/direct-setup', '/module-setup',
                            '/withdraw')):
        return 'CREATE'
    if any(x in p for x in ('/approve', '/review', '/audit', '/setup-module-project', '/reject-application',
                            '/define/reject', '/terminate/reject')) or p.endswith('/setup') or '/setup/' in p:
        return 'APPROVE'
    if '/export' in p:
        return 'EXPORT'
    if '/import' in p:
        return 'IMPORT'
    if any(x in p for x in ('/update', '/edit', '/modify')):
        return 'UPDATE'
    if any(x in p for x in ('/delete', '/del', '/remove')):
        return 'DELETE'
    if any(x in p for x in ('/list', '/search', '/query', '/get', '/count', '/validate', '/diagram', '/history',
                            '/capability', '/where-used')):
        return 'VIEW'
    return 'UPDATE'


def auth_of(ep, path, rules):
    mappings, whitelist, prefixes = rules
    probe = re.sub(r'\{[^}]+\}', 'x', path)
    if probe in whitelist or path in whitelist or any(probe.startswith(p) for p in prefixes):
        text = '公开'
    else:
        module = next((m for pre, m in mappings if pre in probe), None)
        if module is None:
            text = '登录'
        else:
            method = 'POST' if ep['http'] in ('ANY', 'POST') else ep['http'].split('/')[0]
            text = f'{module}:{infer_operation(method, probe)}'
    if ep['scope']:
        text += '；另需 ' + ' / '.join(ep['scope'])
    return text


def short_type(t):
    t = re.sub(r'\b(java\.\w+\.)+', '', t)
    m = re.match(r'ResponseDto\s*<(.*)>$', t)
    if m:
        t = m.group(1)
    elif t.startswith('ResponseDto') or t == 'void':
        t = ''
    return t.replace('|', '\\|')


def params_text(ep):
    items = []
    for p in ep['params']:
        if p['ann'] == 'RequestBody':
            items.append(f'体 `{p["type"]}`')
        elif p['ann'] == 'RequestParam':
            items.append(f'参 `{p["name"]}`')
        elif p['ann'] == 'RequestPart':
            items.append(f'表单 `{p["name"]}`')
        elif p['type'] in ('MultipartFile', 'MultipartFile[]'):
            items.append(f'表单 `{p["name"]}`')
    return '；'.join(items) or '—'


def anchor(text):
    return re.sub(r'[^\w\u4e00-\u9fa5-]', '', text.lower().replace(' ', '-'))


def write_service(svc, conf, eps, rules):
    groups = OrderedDict()
    for ep in eps:
        groups.setdefault((ep['group'], ep['cls']), []).append(ep)
    groups = OrderedDict(sorted(groups.items(), key=lambda kv: min(p for e in kv[1] for p in e['paths'])))
    total = sum(len(e['paths']) for e in eps)
    lines = [
        f'# {conf["title"]}接口',
        '',
        f'{conf["desc"]}',
        '',
        f'- 上下文路径：`{conf["ctx"]}`，缺省端口 {conf["port"]}；浏览器经网关访问，路径不变。',
        f'- 接口数：{total}。公共约定（请求头、响应结构、鉴权列含义）见 [README.md](README.md)。',
        '- 本文件由 `scripts/generate-api-doc.py` 生成，修改 Controller 后重新生成，不要手工编辑。',
        '',
        '## 目录',
        '',
    ]
    for (group, cls), items in groups.items():
        n = sum(len(e['paths']) for e in items)
        lines.append(f'- [{group}](#{anchor(group + "-" + cls)})（{n}）')
    lines.append('')
    for (group, cls), items in groups.items():
        lines += [f'## {group} {cls}', '', f'源码：`{items[0]["file"]}`', '',
                  '| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |', '| --- | --- | --- | --- | --- | --- |']
        for ep in items:
            desc = describe(ep, group).replace('|', '\\|')
            for path in ep['paths']:
                ret = short_type(ep['ret'])
                lines.append(f'| {ep["http"]} | `{path}` | {desc} | {params_text(ep)} | '
                             f'{("`" + ret + "`") if ret else "—"} | {auth_of(ep, path, rules)} |')
        lines.append('')
    with open(os.path.join(OUT, conf['doc']), 'w', encoding='utf-8') as fh:
        fh.write('\n'.join(lines))
    return total


def drop_copied_comments(eps):
    """同一句注释挂在多个不同方法名上，多为复制粘贴未改，改用方法名生成的说明。"""
    owners = {}
    for ep in eps:
        if ep['comment']:
            owners.setdefault(ep['comment'], set()).add(ep['name'])
    for ep in eps:
        if ep['comment'] and (len(owners[ep['comment']]) > 1 or ep['comment'] in ('请求示例',)):
            ep['comment'] = ''


def main():
    os.makedirs(OUT, exist_ok=True)
    eps = parse_controllers()
    drop_copied_comments(eps)
    rules = load_gateway_rules()
    counts = OrderedDict()
    for svc, conf in SERVICES.items():
        counts[svc] = write_service(svc, conf, [e for e in eps if e['svc'] == svc], rules)
    print('接口数：', dict(counts), '合计', sum(counts.values()))


if __name__ == '__main__':
    sys.exit(main())
