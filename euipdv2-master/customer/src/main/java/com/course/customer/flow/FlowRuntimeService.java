/**
 * Author：周朔鹏
 * 编制时间：2026-10-06
 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */

package com.course.customer.flow;

import com.course.server.domain.RdmsFlowBizRef;
import com.course.server.domain.RdmsFlowDef;
import com.course.server.enums.rdms.FlowInstanceStatusEnum;
import com.course.server.exception.BusinessException;
import com.course.server.exception.BusinessExceptionCode;
import com.course.server.mapper.RdmsFlowBizRefMapper;
import com.course.server.security.tenant.TenantScope;
import com.course.server.service.rdms.flow.AssigneeResolverRegistry;
import com.course.server.service.rdms.flow.DomainActionRegistry;
import com.course.server.service.rdms.flow.FlowContext;
import com.course.server.service.rdms.flow.FlowDomainBinding;
import com.course.server.service.rdms.flow.FlowDomainBindingRegistry;
import com.course.server.service.rdms.flow.FlowNodeConfig;
import com.course.server.service.rdms.flow.FlowTimerRules;
import com.course.server.util.UuidUtil;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 适配层运行时。实施方案第 4.1、4.5 节。
 * <p>
 * 引擎不感知业务表，业务服务不感知引擎，二者经本类与领域绑定衔接。业务侧只调用
 * {@link #start} 与 {@link #complete}，节点上配的处理人解析与领域动作由本类按
 * 节点参数驱动。
 * <p>
 * 本类的方法须在业务事务内调用：引擎与业务共用同一数据源与事务管理器，任一动作
 * 失败时引擎写入与业务写入一同回滚。
 */
@Service
public class FlowRuntimeService {

    /** 节点进入后写回业务表的处理人，供 SET_NEXT_NODE 取用。 */
    public static final String VAR_CURRENT_ASSIGNEE = "currentAssigneeId";

    /** 流程发起人，供 INITIATOR 解析器取用。 */
    public static final String VAR_INITIATOR = "initiatorId";

    /** 本步意见，各业务桥接在推进时写入，记为任务批注。 */
    public static final String VAR_PROCESS_TEXT = "processText";

    /** 记录本步结论（业务状态码）的批注类型，与普通意见区分。 */
    public static final String COMMENT_CONCLUSION = "conclusion";

    @Resource
    private RuntimeService runtimeService;

    @Resource
    private TaskService taskService;

    @Resource
    private RdmsFlowBizRefMapper rdmsFlowBizRefMapper;

    @Resource
    private FlowDefinitionService flowDefinitionService;

    @Resource
    private FlowNodeConfigParser flowNodeConfigParser;

    @Resource
    private AssigneeResolverRegistry assigneeResolverRegistry;

    @Resource
    private DomainActionRegistry domainActionRegistry;

    @Resource
    private FlowDomainBindingRegistry flowDomainBindingRegistry;

    /**
     * 按当前启用定义启动实例，并落一条业务映射。
     * 启动后立即处理首个节点的进入，使业务表的 next_node 在本次事务内就写好。
     */
    @Transactional(rollbackFor = Exception.class)
    public RdmsFlowBizRef start(FlowContext context) {
        requireKeys(context);
        if (rdmsFlowBizRefMapper.selectByBiz(context.getCustomerId(), context.getBizCode(),
                context.getBizId()) != null) {
            throw new BusinessException(BusinessExceptionCode.FLOW_NODE_CONFIG_INVALID,
                    "单据 " + context.getBizId() + " 已有流程实例");
        }

        RdmsFlowDef def = flowDefinitionService.requireActive(context.getCustomerId(), context.getBizCode());
        Map<String, Object> variables = new HashMap<>(context.getVariables());
        variables.put(VAR_INITIATOR, context.getInitiatorId());

        ProcessInstance instance = runtimeService.createProcessInstanceBuilder()
                .processDefinitionId(def.getDefinitionId())
                .businessKey(context.getBizId())
                .tenantId(context.getCustomerId())
                .variables(variables)
                .start();

        Date now = new Date();
        RdmsFlowBizRef ref = new RdmsFlowBizRef();
        ref.setId(UuidUtil.getShortUuid());
        ref.setCustomerId(context.getCustomerId());
        ref.setBizCode(context.getBizCode());
        ref.setBizId(context.getBizId());
        ref.setProcessInstanceId(instance.getId());
        ref.setFlowDefId(def.getId());
        ref.setStatus(FlowInstanceStatusEnum.RUNNING.getStatus());
        ref.setCreateTime(now);
        ref.setUpdateTime(now);
        rdmsFlowBizRefMapper.insert(ref);

        context.setProcessInstanceId(instance.getId());
        enterCurrentNodes(context, def, ref);
        return ref;
    }

    /**
     * 推进当前节点。实施方案第 4.5 节第 3 条：页面上的提交、审批、驳回都走此入口。
     * <p>
     * 顺序为门禁判定、写入流程变量、执行除过程记录外的 onComplete、完成引擎任务、
     * 处理新节点的进入，最后写过程记录。过程记录延后，是为了记下新节点进入时
     * 已经写好的下一处理人。状态类动作仍先于任务完成，便于按来源状态判定。
     */
    @Transactional(rollbackFor = Exception.class)
    public void complete(FlowContext context) {
        requireKeys(context);
        RdmsFlowBizRef ref = requireRunningRef(context);
        context.setProcessInstanceId(ref.getProcessInstanceId());
        if (!StringUtils.hasText(context.getInitiatorId())) {
            Object initiator = runtimeService.getVariable(ref.getProcessInstanceId(), VAR_INITIATOR);
            if (initiator != null) {
                context.setInitiatorId(String.valueOf(initiator));
            }
        }

        RdmsFlowDef def = flowDefinitionService.get(ref.getFlowDefId());
        Task task = currentTask(ref.getProcessInstanceId(), context);
        context.setTaskId(task.getId());
        context.setNodeId(task.getTaskDefinitionKey());
        context.setPreviousAssigneeId(task.getAssignee());

        FlowNodeConfig node = nodeConfig(def, task.getTaskDefinitionKey());
        assertGuard(node, context);
        assertAssignee(task, context);

        context.getVariables().forEach((name, value) ->
                runtimeService.setVariable(ref.getProcessInstanceId(), name, value));
        List<FlowNodeConfig.FlowActionConfig> immediate = new ArrayList<>();
        List<FlowNodeConfig.FlowActionConfig> processWrites = new ArrayList<>();
        for (FlowNodeConfig.FlowActionConfig action : node.getOnComplete()) {
            if ("WRITE_PROCESS".equals(action.getAction())) {
                processWrites.add(action);
            } else {
                immediate.add(action);
            }
        }
        domainActionRegistry.executeAll(immediate, context);
        recordStep(task, context);
        taskService.complete(task.getId(), new HashMap<>(context.getVariables()));

        if (runtimeService.createProcessInstanceQuery()
                .processInstanceId(ref.getProcessInstanceId()).count() == 0) {
            rdmsFlowBizRefMapper.updateStatus(ref.getId(),
                    FlowInstanceStatusEnum.FINISHED.getStatus(), new Date());
        } else {
            enterCurrentNodes(context, def, ref);
        }
        domainActionRegistry.executeAll(processWrites, context);
    }

    /**
     * 把本步的意见与结论记在引擎任务上，供流程图的办理记录与过程列表读取。
     * 意见取 processText 变量，记为普通批注；结论取状态类动作执行后的业务状态码，
     * 记为 {@value #COMMENT_CONCLUSION} 类批注。业务没有登记绑定或读不到状态时只记意见。
     */
    private void recordStep(Task task, FlowContext context) {
        Object text = context.getVariables().get(VAR_PROCESS_TEXT);
        if (text != null && StringUtils.hasText(String.valueOf(text))) {
            taskService.addComment(task.getId(), task.getProcessInstanceId(), String.valueOf(text));
        }
        FlowDomainBinding binding = flowDomainBindingRegistry.find(context.getBizCode());
        if (binding == null) {
            return;
        }
        String status = binding.currentStatus(context);
        if (StringUtils.hasText(status)) {
            taskService.addComment(task.getId(), task.getProcessInstanceId(), COMMENT_CONCLUSION, status);
        }
    }

    /**
     * 把当前待办改派给指定成员，再由 {@link #complete} 校验操作人。
     * 质量分析在工单执行人确定之后才知道处理人，进入节点时还只能解析到产品经理或测试主管。
     */
    @Transactional(rollbackFor = Exception.class)
    public void reassignCurrent(FlowContext context, String assigneeId) {
        requireKeys(context);
        if (!StringUtils.hasText(assigneeId)) {
            throw new BusinessException(BusinessExceptionCode.FLOW_ASSIGNEE_NOT_FOUND, "改派时没有处理人");
        }
        RdmsFlowBizRef ref = requireRunningRef(context);
        Task task = currentTask(ref.getProcessInstanceId(), context);
        taskService.setAssignee(task.getId(), assigneeId);
    }

    /**
     * 业务已在流程之外办结时收尾实例：在各当前任务记下意见与结论，再直接移到指定结束事件。
     * 不执行节点的 onComplete 动作，业务状态以调用方写好的为准。实例不存在或已结束时不做任何事。
     */
    @Transactional(rollbackFor = Exception.class)
    public void finishAt(FlowContext context, String endNodeId) {
        requireKeys(context);
        RdmsFlowBizRef ref = rdmsFlowBizRefMapper.selectByBiz(context.getCustomerId(), context.getBizCode(),
                context.getBizId());
        if (ref == null || !FlowInstanceStatusEnum.RUNNING.getStatus().equals(ref.getStatus())) {
            return;
        }
        context.setProcessInstanceId(ref.getProcessInstanceId());
        List<String> activityIds = new ArrayList<>();
        for (Task task : taskService.createTaskQuery().processInstanceId(ref.getProcessInstanceId()).list()) {
            recordStep(task, context);
            if (!activityIds.contains(task.getTaskDefinitionKey())) {
                activityIds.add(task.getTaskDefinitionKey());
            }
        }
        if (!activityIds.isEmpty()) {
            runtimeService.createChangeActivityStateBuilder()
                    .processInstanceId(ref.getProcessInstanceId())
                    .moveActivityIdsToSingleActivityId(activityIds, endNodeId)
                    .changeState();
        }
        if (runtimeService.createProcessInstanceQuery()
                .processInstanceId(ref.getProcessInstanceId()).count() == 0) {
            rdmsFlowBizRefMapper.updateStatus(ref.getId(), FlowInstanceStatusEnum.FINISHED.getStatus(), new Date());
        }
    }

    /** 单据所绑定的流程定义里是否有该节点。实例沿用启动时的定义，新增节点对在途单据不生效。 */
    public boolean definitionHasNode(String customerId, String bizCode, String bizId, String nodeId) {
        RdmsFlowBizRef ref = rdmsFlowBizRefMapper.selectByBiz(customerId, bizCode, bizId);
        if (ref == null) {
            return false;
        }
        RdmsFlowDef def = flowDefinitionService.get(ref.getFlowDefId());
        return flowDefinitionService.bpmnModel(def).getMainProcess().getFlowElement(nodeId) != null;
    }

    /** 按业务码与业务主键取映射，不存在时拒绝。 */
    public RdmsFlowBizRef requireRef(String customerId, String bizCode, String bizId) {
        RdmsFlowBizRef ref = rdmsFlowBizRefMapper.selectByBiz(customerId, bizCode, bizId);
        if (ref == null) {
            throw new BusinessException(BusinessExceptionCode.FLOW_INSTANCE_NOT_FOUND);
        }
        return ref;
    }

    /**
     * 处理当前各节点的进入：解析处理人、写回引擎任务、执行 onEnter 动作。
     * <p>
     * 处理人由本处解析后经 setAssignee 写入引擎，而不写在 BPMN 的 assignee 属性里，
     * 使流程图只描述“由哪类角色处理”，具体取人仍由登记过的解析器决定。
     */
    private void enterCurrentNodes(FlowContext context, RdmsFlowDef def, RdmsFlowBizRef ref) {
        List<Task> tasks = taskService.createTaskQuery()
                .processInstanceId(ref.getProcessInstanceId())
                .list();
        for (Task task : tasks) {
            if (StringUtils.hasText(task.getAssignee())) {
                continue;
            }
            FlowNodeConfig node = nodeConfig(def, task.getTaskDefinitionKey());
            FlowContext entry = entryContext(context, task, node);
            String assignee = loopAssignee(task);
            if (!StringUtils.hasText(assignee)) {
                List<String> assignees = assigneeResolverRegistry.resolve(node, entry);
                assignee = assignees.get(0);
            }
            taskService.setAssignee(task.getId(), assignee);
            entry.putVariable(VAR_CURRENT_ASSIGNEE, assignee);
            if (loopAssignee(task) == null) {
                runtimeService.setVariable(ref.getProcessInstanceId(), VAR_CURRENT_ASSIGNEE, assignee);
            }
            armTimer(task, node);

            domainActionRegistry.executeAll(node.getOnEnter(), entry);
        }
    }

    /**
     * 节点进入时把时限写到执行本地变量。并行任务各有自己的执行，互不覆盖。
     * 扫描由 {@code rdms.flow.timer-scan} 控制，不启动 Flowable 异步作业执行器。
     */
    private void armTimer(Task task, FlowNodeConfig node) {
        FlowNodeConfig.FlowTimerConfig timer = node.getTimer();
        if (timer == null || !StringUtils.hasText(task.getExecutionId())) {
            return;
        }
        Long millis = FlowTimerRules.millis(timer.getDuration());
        if (millis == null || !FlowTimerRules.knownAction(timer.getAction())) {
            return;
        }
        String executionId = task.getExecutionId();
        runtimeService.setVariableLocal(executionId, FlowTimerRules.DUE, System.currentTimeMillis() + millis);
        runtimeService.setVariableLocal(executionId, FlowTimerRules.ACTION, timer.getAction());
        runtimeService.setVariableLocal(executionId, FlowTimerRules.REPEAT, timer.isRepeat());
        runtimeService.setVariableLocal(executionId, FlowTimerRules.DURATION, timer.getDuration().trim());
    }

    /**
     * 执行一条已到期的节点时限。失败时由调用方另开事务写说明并清除时限，避免每分钟重试。
     */
    @Transactional(rollbackFor = Exception.class)
    public void applyTimer(String taskId) {
        Task task = requireTimedTask(taskId);
        RdmsFlowBizRef ref = requireRefByInstance(task);
        TenantScope.runAs(ref.getCustomerId(), null, task.getAssignee(), () -> applyTimer(task));
    }

    private void applyTimer(Task task) {
        String action = localText(task, FlowTimerRules.ACTION);
        if (FlowTimerRules.NOTIFY.equals(action)) {
            taskService.addComment(task.getId(), task.getProcessInstanceId(), "时限提醒");
            if (Boolean.TRUE.equals(local(task, FlowTimerRules.REPEAT))) {
                advanceTimer(task);
            } else {
                clearTimer(task);
            }
            return;
        }
        if (FlowTimerRules.REASSIGN.equals(action)) {
            reassignByFallback(task);
            clearTimer(task);
            return;
        }
        if (FlowTimerRules.AUTO_COMPLETE.equals(action)) {
            complete(timerContext(task));
            return;
        }
        throw new BusinessException(BusinessExceptionCode.FLOW_NODE_CONFIG_INVALID, "时限动作无法执行");
    }

    /** 时限执行失败后留下说明并清除，使同一任务不再被每分钟重试。 */
    @Transactional(rollbackFor = Exception.class)
    public void retireTimer(String taskId, String reason) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            return;
        }
        String text = StringUtils.hasText(reason) ? reason : "时限执行失败";
        if (text.length() > 200) {
            text = text.substring(0, 200);
        }
        taskService.addComment(task.getId(), task.getProcessInstanceId(), "时限未执行：" + text);
        clearTimer(task);
    }

    private void reassignByFallback(Task task) {
        RdmsFlowBizRef ref = requireRefByInstance(task);
        RdmsFlowDef def = flowDefinitionService.get(ref.getFlowDefId());
        FlowNodeConfig node = nodeConfig(def, task.getTaskDefinitionKey());
        List<String> assignees = assigneeResolverRegistry.resolveFallback(node, timerContext(task));
        if (assignees.isEmpty()) {
            taskService.addComment(task.getId(), task.getProcessInstanceId(), "时限已到，回退处理人为空");
            return;
        }
        String assignee = assignees.get(0);
        taskService.setAssignee(task.getId(), assignee);
        runtimeService.setVariable(task.getProcessInstanceId(), VAR_CURRENT_ASSIGNEE, assignee);
        FlowContext context = timerContext(task);
        context.setOperatorId(assignee);
        flowDomainBindingRegistry.require(ref.getBizCode()).writeNextNode(context, assignee);
    }

    private FlowContext timerContext(Task task) {
        RdmsFlowBizRef ref = requireRefByInstance(task);
        FlowContext context = new FlowContext();
        context.setCustomerId(ref.getCustomerId());
        context.setBizCode(ref.getBizCode());
        context.setBizId(ref.getBizId());
        context.setProcessInstanceId(task.getProcessInstanceId());
        context.setTaskId(task.getId());
        context.setNodeId(task.getTaskDefinitionKey());
        context.setOperatorId(task.getAssignee());
        context.setPreviousAssigneeId(task.getAssignee());
        Object initiator = runtimeService.getVariable(task.getProcessInstanceId(), VAR_INITIATOR);
        if (initiator != null) {
            context.setInitiatorId(String.valueOf(initiator));
        }
        return context;
    }

    private RdmsFlowBizRef requireRefByInstance(Task task) {
        RdmsFlowBizRef ref = rdmsFlowBizRefMapper.selectByInstance(task.getProcessInstanceId());
        if (ref == null) {
            throw new BusinessException(BusinessExceptionCode.FLOW_INSTANCE_NOT_FOUND);
        }
        return ref;
    }

    private Task requireTimedTask(String taskId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null || !StringUtils.hasText(task.getExecutionId())) {
            throw new BusinessException(BusinessExceptionCode.FLOW_TASK_NOT_CURRENT, "时限任务已不存在");
        }
        return task;
    }

    private void advanceTimer(Task task) {
        Long millis = FlowTimerRules.millis(localText(task, FlowTimerRules.DURATION));
        if (millis == null) {
            clearTimer(task);
            return;
        }
        runtimeService.setVariableLocal(task.getExecutionId(), FlowTimerRules.DUE, System.currentTimeMillis() + millis);
    }

    private void clearTimer(Task task) {
        if (!StringUtils.hasText(task.getExecutionId())) {
            return;
        }
        runtimeService.removeVariableLocal(task.getExecutionId(), FlowTimerRules.DUE);
        runtimeService.removeVariableLocal(task.getExecutionId(), FlowTimerRules.ACTION);
        runtimeService.removeVariableLocal(task.getExecutionId(), FlowTimerRules.REPEAT);
        runtimeService.removeVariableLocal(task.getExecutionId(), FlowTimerRules.DURATION);
    }

    private Object local(Task task, String name) {
        return runtimeService.getVariableLocal(task.getExecutionId(), name);
    }

    private String localText(Task task, String name) {
        Object value = local(task, name);
        return value == null ? null : String.valueOf(value);
    }

    /**
     * 并行会签的处理人取多实例元素变量，不把同一解析结果写给每一个会签任务。
     */
    private String loopAssignee(Task task) {
        if (!StringUtils.hasText(task.getExecutionId())) {
            return null;
        }
        Object value = runtimeService.getVariableLocal(task.getExecutionId(), "reviewerId");
        if (value == null) {
            return null;
        }
        String assignee = String.valueOf(value);
        return StringUtils.hasText(assignee) ? assignee : null;
    }

    /** 节点进入时的上下文：换成新节点，并把处理人带上，其余沿用调用方装入的业务对象。 */
    private FlowContext entryContext(FlowContext source, Task task, FlowNodeConfig node) {
        FlowContext entry = new FlowContext();
        entry.setCustomerId(source.getCustomerId());
        entry.setBizCode(source.getBizCode());
        entry.setBizId(source.getBizId());
        entry.setProcessInstanceId(source.getProcessInstanceId());
        entry.setOperatorId(source.getOperatorId());
        entry.setInitiatorId(source.getInitiatorId());
        entry.setPreviousAssigneeId(source.getPreviousAssigneeId());
        entry.setBizEntity(source.getBizEntity());
        entry.setJobItem(source.getJobItem());
        entry.getVariables().putAll(source.getVariables());
        entry.setTaskId(task.getId());
        entry.setNodeId(node.getNodeId());
        return entry;
    }

    /** 门禁：业务对象当前状态须在节点配置的来源集合内，拒绝码取节点配置的原有码。 */
    private void assertGuard(FlowNodeConfig node, FlowContext context) {
        if (node.getGuardSourceStates() == null || node.getGuardSourceStates().isEmpty()) {
            return;
        }
        FlowDomainBinding binding = flowDomainBindingRegistry.require(context.getBizCode());
        String current = binding.currentStatus(context);
        if (!node.getGuardSourceStates().contains(current)) {
            throw new BusinessException(denyCode(node.getGuardDenyCode()),
                    "单据当前状态 " + current + " 不满足节点 " + node.getNodeId() + " 的进入条件");
        }
    }

    /** 操作人须为当前任务处理人。任务未指派处理人时不校验，由门禁与业务权限兜住。 */
    private void assertAssignee(Task task, FlowContext context) {
        if (!StringUtils.hasText(task.getAssignee())) {
            return;
        }
        if (!task.getAssignee().equals(context.getOperatorId())) {
            throw new BusinessException(BusinessExceptionCode.FLOW_TASK_NOT_CURRENT,
                    "当前节点的处理人不是操作人本人");
        }
    }

    private Task currentTask(String processInstanceId, FlowContext context) {
        List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
        if (tasks.isEmpty()) {
            throw new BusinessException(BusinessExceptionCode.FLOW_TASK_NOT_CURRENT,
                    "该实例没有待办节点");
        }
        String nodeId = context.getNodeId();
        List<Task> matched = tasks;
        if (StringUtils.hasText(nodeId)) {
            matched = new ArrayList<>();
            for (Task task : tasks) {
                if (nodeId.equals(task.getTaskDefinitionKey())) {
                    matched.add(task);
                }
            }
            if (matched.isEmpty()) {
                throw new BusinessException(BusinessExceptionCode.FLOW_TASK_NOT_CURRENT,
                        "节点 " + nodeId + " 不是该实例的当前待办节点");
            }
        }
        if (matched.size() == 1) {
            return matched.get(0);
        }
        String operatorId = context.getOperatorId();
        for (Task task : matched) {
            if (StringUtils.hasText(operatorId) && operatorId.equals(task.getAssignee())) {
                return task;
            }
        }
        throw new BusinessException(BusinessExceptionCode.FLOW_TASK_NOT_CURRENT,
                "该实例有多个待办节点，须由当前处理人推进");
    }

    private FlowNodeConfig nodeConfig(RdmsFlowDef def, String nodeId) {
        BpmnModel model = flowDefinitionService.bpmnModel(def);
        FlowElement element = model.getMainProcess().getFlowElement(nodeId);
        if (element == null) {
            throw new BusinessException(BusinessExceptionCode.FLOW_NODE_CONFIG_INVALID,
                    "流程定义内没有节点 " + nodeId);
        }
        return flowNodeConfigParser.parse(element);
    }

    private RdmsFlowBizRef requireRunningRef(FlowContext context) {
        RdmsFlowBizRef ref = requireRef(context.getCustomerId(), context.getBizCode(), context.getBizId());
        if (!FlowInstanceStatusEnum.RUNNING.getStatus().equals(ref.getStatus())) {
            throw new BusinessException(BusinessExceptionCode.FLOW_TASK_NOT_CURRENT,
                    "该单据的流程已结束");
        }
        return ref;
    }

    private void requireKeys(FlowContext context) {
        if (!StringUtils.hasText(context.getCustomerId()) || !StringUtils.hasText(context.getBizCode())
                || !StringUtils.hasText(context.getBizId())) {
            throw new BusinessException(BusinessExceptionCode.FLOW_NODE_CONFIG_INVALID,
                    "流程上下文缺少机构标识、业务码或业务主键");
        }
    }

    private BusinessExceptionCode denyCode(String configured) {
        if (!StringUtils.hasText(configured)) {
            return BusinessExceptionCode.LIFECYCLE_TRANSITION_FORBIDDEN;
        }
        try {
            return BusinessExceptionCode.valueOf(configured);
        } catch (IllegalArgumentException e) {
            return BusinessExceptionCode.LIFECYCLE_TRANSITION_FORBIDDEN;
        }
    }
}
