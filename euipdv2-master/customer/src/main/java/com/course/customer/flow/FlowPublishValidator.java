/**
 * Author：周朔鹏
 * 编制时间：2026-10-06

 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */

package com.course.customer.flow;

import com.course.server.exception.BusinessException;
import com.course.server.exception.BusinessExceptionCode;
import com.course.server.service.rdms.flow.AssigneeResolverRegistry;
import com.course.server.service.rdms.flow.DomainAction;
import com.course.server.service.rdms.flow.DomainActionRegistry;
import com.course.server.service.rdms.flow.FlowBudgetBinding;
import com.course.server.service.rdms.flow.FlowBizCode;
import com.course.server.service.rdms.flow.FlowDomainBinding;
import com.course.server.service.rdms.flow.FlowDomainBindingRegistry;
import com.course.server.service.rdms.flow.FlowFormRegistry;
import com.course.server.service.rdms.flow.FlowNodeConfig;
import com.course.server.service.rdms.flow.FlowTimerRules;
import com.course.server.service.rdms.flow.resolver.RoleInOrgResolver;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.EndEvent;
import org.flowable.bpmn.model.ExclusiveGateway;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.FlowNode;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.ScriptTask;
import org.flowable.bpmn.model.SequenceFlow;
import org.flowable.bpmn.model.StartEvent;
import org.flowable.bpmn.model.UserTask;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 流程定义发布校验（实施方案第 4.6 节）。五类校验任一不通过即拒绝发布并指出节点。
 * 校验的目的是保证流程图里只有编排，不含可执行逻辑。
 */
@Component
public class FlowPublishValidator {

    /** 表达式只允许变量只读取值，形如 #{approveResult} 或 #{a.b}。 */
    private static final Pattern READ_ONLY_EXPRESSION =
            Pattern.compile("^#\\{\\s*[A-Za-z_][A-Za-z0-9_]*(\\.[A-Za-z_][A-Za-z0-9_]*)*\\s*(==\\s*'[^']*'\\s*)?}$");

    /** 表达式中出现方法调用的特征。 */
    private static final Pattern METHOD_CALL = Pattern.compile("[A-Za-z0-9_\\]]\\s*\\(");

    @Resource
    private FlowNodeConfigParser nodeConfigParser;

    @Resource
    private AssigneeResolverRegistry assigneeResolvers;

    @Resource
    private DomainActionRegistry domainActions;

    @Resource
    private FlowDomainBindingRegistry domainBindings;

    /** 关闭时拒绝带时限的定义，避免配出不会执行的超时。 */
    @Value("${rdms.flow.timer-scan:false}")
    private boolean timerScan;

    /**
     * 校验一份流程定义。
     *
     * @param model   已解析的 BPMN 模型
     * @param bizCode 业务流程码
     * @return 全部不通过项的说明。为空表示校验通过
     */
    public List<String> validate(BpmnModel model, String bizCode) {
        List<String> problems = new ArrayList<>();

        Process process = model == null ? null : model.getMainProcess();
        if (process == null) {
            problems.add("流程定义里没有可用的流程");
            return problems;
        }

        validateBizCode(process, bizCode, problems);
        validateStructure(process, problems);
        validateNodes(process, bizCode, problems);
        return problems;
    }

    /** 校验通过则返回，不通过则抛出拒绝码并带上全部问题说明。 */
    public void assertValid(BpmnModel model, String bizCode) {
        List<String> problems = validate(model, bizCode);
        if (!problems.isEmpty()) {
            throw new BusinessException(BusinessExceptionCode.FLOW_DEF_INVALID, String.join("；", problems));
        }
    }

    /** 第 5 项：process_key 与 biz_code 对应。同码启用定义的唯一性由定义服务在落库时判定。 */
    private void validateBizCode(Process process, String bizCode, List<String> problems) {
        FlowBizCode code = FlowBizCode.of(bizCode);
        if (code == null) {
            problems.add("业务码 " + bizCode + " 未登记");
            return;
        }
        if (!Objects.equals(code.getProcessKey(), process.getId())) {
            problems.add("流程标识应当为 " + code.getProcessKey() + "，当前为 " + process.getId());
        }
    }

    /** 第 1 项：结构校验。 */
    private void validateStructure(Process process, List<String> problems) {
        Collection<FlowElement> elements = process.getFlowElements();

        List<StartEvent> starts = new ArrayList<>();
        List<EndEvent> ends = new ArrayList<>();
        List<SequenceFlow> flows = new ArrayList<>();
        Set<String> nodeIds = new HashSet<>();
        for (FlowElement element : elements) {
            if (element instanceof StartEvent) {
                starts.add((StartEvent) element);
            } else if (element instanceof EndEvent) {
                ends.add((EndEvent) element);
            } else if (element instanceof SequenceFlow) {
                flows.add((SequenceFlow) element);
            }
            if (element instanceof FlowNode) {
                nodeIds.add(element.getId());
            }
        }

        if (starts.size() != 1) {
            problems.add("应当有且只有一个开始事件，当前为 " + starts.size() + " 个");
        }
        if (ends.isEmpty()) {
            problems.add("应当至少有一个结束事件");
        }

        // 无悬挂边：连线两端都应当是本流程内的节点
        for (SequenceFlow flow : flows) {
            if (!nodeIds.contains(flow.getSourceRef())) {
                problems.add("连线 " + flow.getId() + " 的起点 " + flow.getSourceRef() + " 不存在");
            }
            if (!nodeIds.contains(flow.getTargetRef())) {
                problems.add("连线 " + flow.getId() + " 的终点 " + flow.getTargetRef() + " 不存在");
            }
        }

        // 每个节点可达
        if (starts.size() == 1) {
            Set<String> reachable = reachableFrom(starts.get(0).getId(), flows);
            for (String nodeId : nodeIds) {
                if (!reachable.contains(nodeId)) {
                    problems.add("节点 " + nodeId + " 自开始事件不可达");
                }
            }
        }

        // 每个节点都能走到某个结束事件，否则实例会停在该处无法收口
        if (!ends.isEmpty()) {
            List<SequenceFlow> reversed = new ArrayList<>();
            for (SequenceFlow flow : flows) {
                SequenceFlow back = new SequenceFlow(flow.getTargetRef(), flow.getSourceRef());
                reversed.add(back);
            }
            Set<String> canFinish = new HashSet<>();
            for (EndEvent end : ends) {
                canFinish.addAll(reachableFrom(end.getId(), reversed));
            }
            for (String nodeId : nodeIds) {
                if (!canFinish.contains(nodeId)) {
                    problems.add("节点 " + nodeId + " 不能到达任何结束事件");
                }
            }
        }

        // 每条排他网关的出口有默认分支，非默认出口有条件表达式
        for (FlowElement element : elements) {
            if (!(element instanceof ExclusiveGateway)) {
                continue;
            }
            ExclusiveGateway gateway = (ExclusiveGateway) element;
            List<SequenceFlow> outgoing = new ArrayList<>();
            for (SequenceFlow flow : flows) {
                if (Objects.equals(flow.getSourceRef(), gateway.getId())) {
                    outgoing.add(flow);
                }
            }
            if (outgoing.size() <= 1) {
                continue;
            }
            String defaultFlow = gateway.getDefaultFlow();
            if (defaultFlow == null || defaultFlow.isEmpty()) {
                problems.add("排他网关 " + gateway.getId() + " 没有默认分支");
            }
            for (SequenceFlow flow : outgoing) {
                if (Objects.equals(flow.getId(), defaultFlow)) {
                    continue;
                }
                if (flow.getConditionExpression() == null || flow.getConditionExpression().trim().isEmpty()) {
                    problems.add("排他网关 " + gateway.getId() + " 的出口 " + flow.getId() + " 既不是默认分支也没有条件表达式");
                }
            }
        }
    }

    private Set<String> reachableFrom(String startId, List<SequenceFlow> flows) {
        Set<String> visited = new HashSet<>();
        Deque<String> pending = new ArrayDeque<>();
        pending.push(startId);
        visited.add(startId);
        while (!pending.isEmpty()) {
            String current = pending.pop();
            for (SequenceFlow flow : flows) {
                if (Objects.equals(flow.getSourceRef(), current) && visited.add(flow.getTargetRef())) {
                    pending.push(flow.getTargetRef());
                }
            }
        }
        return visited;
    }

    /** 第 2、3、4 项：节点参数、状态合法、表达式。 */
    private void validateNodes(Process process, String bizCode, List<String> problems) {
        FlowDomainBinding binding = domainBindings.find(bizCode);
        if (binding == null) {
            problems.add("业务码 " + bizCode + " 没有登记领域绑定，不可发布");
        }
        boolean budgetSupported = binding instanceof FlowBudgetBinding;
        Set<String> validStatuses = binding == null ? null : binding.validStatuses();

        for (FlowElement element : process.getFlowElements()) {
            // 第 4 项：不接受脚本任务
            if (element instanceof ScriptTask) {
                problems.add("节点 " + element.getId() + " 是脚本任务，流程图内不得写可执行逻辑");
                continue;
            }
            if (element instanceof SequenceFlow) {
                checkExpression(((SequenceFlow) element).getConditionExpression(),
                        "连线 " + element.getId() + " 的条件", problems);
                continue;
            }
            if (!(element instanceof FlowNode)) {
                continue;
            }

            FlowNodeConfig config = nodeConfigParser.parse(element);

            if (element instanceof UserTask) {
                validateUserTask((UserTask) element, config, problems);
            }
            validateGuard(config, validStatuses, problems);
            validateForm(config, problems);
            validateTimer(config, problems);
            validateActions(config.getOnEnter(), "onEnter", config, validStatuses, budgetSupported, problems);
            validateActions(config.getOnComplete(), "onComplete", config, validStatuses, budgetSupported, problems);
        }
    }

    /** 表单码可空。写了就必须在登记表内。 */
    private void validateForm(FlowNodeConfig config, List<String> problems) {
        if (config.getFormCode() == null || config.getFormCode().isEmpty()) {
            return;
        }
        if (!FlowFormRegistry.contains(config.getFormCode())) {
            problems.add("节点 " + config.getNodeId() + " 的表单 " + config.getFormCode() + " 未登记");
        }
    }

    /** 时限动作只有提醒、转交、自动完成。调度关闭时不得配置时限。 */
    private void validateTimer(FlowNodeConfig config, List<String> problems) {
        FlowNodeConfig.FlowTimerConfig timer = config.getTimer();
        if (timer == null) {
            return;
        }
        if (!timerScan) {
            problems.add("节点 " + config.getNodeId() + " 配置了时限，当前未打开时限扫描");
            return;
        }
        if (!FlowTimerRules.knownAction(timer.getAction())) {
            problems.add("节点 " + config.getNodeId() + " 的时限动作只接受 NOTIFY、REASSIGN、AUTO_COMPLETE");
        }
        if (FlowTimerRules.millis(timer.getDuration()) == null) {
            problems.add("节点 " + config.getNodeId() + " 的时限时长不是可折成毫秒的 ISO-8601");
        }
    }

    /** 第 2 项：每个用户任务配有 assignee，且解析器码已登记。 */
    private void validateUserTask(UserTask task, FlowNodeConfig config, List<String> problems) {
        if (config.getAssigneeResolver() == null || config.getAssigneeResolver().isEmpty()) {
            problems.add("用户任务 " + task.getId() + " 没有配置处理人");
            return;
        }
        if (!assigneeResolvers.contains(config.getAssigneeResolver())) {
            problems.add("用户任务 " + task.getId() + " 的处理人解析器 "
                    + config.getAssigneeResolver() + " 未登记");
        }
        if (config.getAssigneeFallback() != null && !config.getAssigneeFallback().isEmpty()
                && !assigneeResolvers.contains(config.getAssigneeFallback())) {
            problems.add("用户任务 " + task.getId() + " 的回退解析器 "
                    + config.getAssigneeFallback() + " 未登记");
        }
        if (config.getAssigneeDenyCode() != null && !config.getAssigneeDenyCode().isEmpty()
                && !isKnownDenyCode(config.getAssigneeDenyCode())) {
            problems.add("用户任务 " + task.getId() + " 的拒绝码 "
                    + config.getAssigneeDenyCode() + " 不存在");
        }
        boolean role = "ROLE_IN_ORG".equals(config.getAssigneeResolver())
                || "ROLE_IN_ORG".equals(config.getAssigneeFallback());
        if (role && !RoleInOrgResolver.supported(param(config, "roleCode"))) {
            problems.add("用户任务 " + task.getId() + " 的机构角色码未登记");
        }
        boolean fixed = "FIXED_USER".equals(config.getAssigneeResolver())
                || "FIXED_USER".equals(config.getAssigneeFallback());
        if (fixed && (param(config, "userId") == null || param(config, "userId").isEmpty())) {
            problems.add("用户任务 " + task.getId() + " 的指定成员缺少 userId");
        }
    }

    private String param(FlowNodeConfig config, String name) {
        if (config.getAssigneeParams() == null) {
            return null;
        }
        return config.getAssigneeParams().get(name);
    }

    /** 第 3 项：guard 引用的状态码在该业务对象的状态枚举内。 */
    private void validateGuard(FlowNodeConfig config, Set<String> validStatuses, List<String> problems) {
        if (config.getGuardSourceStates().isEmpty()) {
            return;
        }
        if (validStatuses != null) {
            for (String state : config.getGuardSourceStates()) {
                if (!validStatuses.contains(state)) {
                    problems.add("节点 " + config.getNodeId() + " 的 guard 引用了未定义的状态码 " + state);
                }
            }
        }
        if (config.getGuardDenyCode() != null && !config.getGuardDenyCode().isEmpty()
                && !isKnownDenyCode(config.getGuardDenyCode())) {
            problems.add("节点 " + config.getNodeId() + " 的 guard 拒绝码 "
                    + config.getGuardDenyCode() + " 不存在");
        }
    }

    /** 第 2、3、4 项：动作码已登记、必填参数齐备、无多余参数、状态与表达式合法。 */
    private void validateActions(List<FlowNodeConfig.FlowActionConfig> configs, String phase,
                                 FlowNodeConfig node, Set<String> validStatuses,
                                 boolean budgetSupported, List<String> problems) {
        if (configs == null) {
            return;
        }
        for (FlowNodeConfig.FlowActionConfig config : configs) {
            String where = "节点 " + node.getNodeId() + " 的 " + phase;
            if (config.getAction() == null || config.getAction().isEmpty()) {
                problems.add(where + " 没有指定动作码");
                continue;
            }
            DomainAction action = domainActions.get(config.getAction());
            if (action == null) {
                problems.add(where + " 引用了未登记的动作 " + config.getAction());
                continue;
            }

            Map<String, String> params = config.getParams();
            for (String required : action.requiredParams()) {
                String value = params == null ? null : params.get(required);
                if (value == null || value.isEmpty()) {
                    problems.add(where + " 的动作 " + config.getAction() + " 缺少必填参数 " + required);
                }
            }
            if (params != null) {
                Set<String> accepted = new HashSet<>(action.requiredParams());
                accepted.addAll(action.optionalParams());
                for (String name : params.keySet()) {
                    if (!accepted.contains(name)) {
                        problems.add(where + " 的动作 " + config.getAction() + " 有多余参数 " + name);
                    }
                }
            }

            // 预算类动作须该业务登记了预算绑定
            if (!budgetSupported && isBudgetAction(config.getAction())) {
                problems.add(where + " 的动作 " + config.getAction() + " 在本业务不可用，该业务未实现预算绑定");
            }

            // WRITE_STATUS 的目标状态须在状态枚举内；写成表达式时在运行期判定
            if (("WRITE_STATUS".equals(config.getAction()) || "CLOSE_JOB_ITEM".equals(config.getAction()))
                    && validStatuses != null && params != null) {
                String status = params.get("status");
                if (status != null && !isExpression(status) && !validStatuses.contains(status)) {
                    problems.add(where + " 的 " + config.getAction() + " 引用了未定义的状态码 " + status);
                }
            }

            if (params != null) {
                params.forEach((name, value) ->
                        checkExpression(value, where + " 的参数 " + name, problems));
            }
            checkExpression(config.getWhen(), where + " 的执行条件", problems);
        }
    }

    private boolean isBudgetAction(String action) {
        return "WRITE_BUDGET_EXE".equals(action)
                || "REVOKE_BUDGET_EXE".equals(action)
                || "CHECK_BUDGET_BALANCE".equals(action);
    }

    private boolean isExpression(String value) {
        return value != null && value.contains("#{");
    }

    /** 第 4 项：表达式只含变量只读取值，不含方法调用与脚本。 */
    private void checkExpression(String value, String where, List<String> problems) {
        if (!isExpression(value)) {
            return;
        }
        if (METHOD_CALL.matcher(value).find()) {
            problems.add(where + " 的表达式含方法调用，只接受变量取值");
            return;
        }
        if (!READ_ONLY_EXPRESSION.matcher(value.trim()).matches()) {
            problems.add(where + " 的表达式不是允许的变量取值形式");
        }
    }

    private boolean isKnownDenyCode(String code) {
        try {
            BusinessExceptionCode.valueOf(code);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
