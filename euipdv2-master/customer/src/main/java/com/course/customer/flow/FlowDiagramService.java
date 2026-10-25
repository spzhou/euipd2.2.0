/**
 * Author：周朔鹏
 * 编制时间：2026-10-06
 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */

package com.course.customer.flow;

import com.course.server.domain.RdmsCustomerUser;
import com.course.server.domain.RdmsFlowBizRef;
import com.course.server.domain.RdmsFlowDef;
import com.course.server.dto.rdms.FlowDiagramDto;
import com.course.server.service.rdms.RdmsCustomerUserService;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.SequenceFlow;
import org.flowable.engine.HistoryService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.impl.persistence.entity.CommentEntity;
import org.flowable.engine.task.Comment;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 流程图显示数据。实施方案第 4.7 节。
 * <p>
 * 定义全文取该实例启动时所用的版本，不取当前启用版本，使在途单据显示的是它实际
 * 执行的那张图。办理记录按活动实例逐次列出：回环节点每进入一次一行，会签每个
 * 委员一行。处理人名称取机构成员表；结论取该步离开时所经连线的名称；意见取写在
 * 该任务上的引擎批注。
 */
@Service
public class FlowDiagramService {

    /** 引擎把连线也记为活动实例，类型为此值。 */
    private static final String TYPE_SEQUENCE_FLOW = "sequenceFlow";

    /** 网关没有处理人与停留意义，不进办理记录。 */
    private static final String TYPE_GATEWAY_SUFFIX = "Gateway";

    @Resource
    private HistoryService historyService;

    @Resource
    private TaskService taskService;

    @Resource
    private FlowRuntimeService flowRuntimeService;

    @Resource
    private FlowDefinitionService flowDefinitionService;

    @Resource
    private RdmsCustomerUserService rdmsCustomerUserService;

    public FlowDiagramDto diagram(String customerId, String bizCode, String bizId) {
        RdmsFlowBizRef ref = flowRuntimeService.requireRef(customerId, bizCode, bizId);
        RdmsFlowDef def = flowDefinitionService.get(ref.getFlowDefId());

        FlowDiagramDto dto = new FlowDiagramDto();
        dto.setBpmnXml(def.getBpmnXml());
        dto.setInstanceStatus(ref.getStatus());

        List<HistoricActivityInstance> instances = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(ref.getProcessInstanceId())
                .orderByHistoricActivityInstanceStartTime().asc()
                .list();
        // 同一事务内的多条活动开始时间相同，再按引擎记录的事务内次序排，使“开始”不会排到第一个任务之后。
        instances.sort(Comparator
                .comparing(HistoricActivityInstance::getStartTime, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(HistoricActivityInstance::getTransactionOrder,
                        Comparator.nullsLast(Comparator.naturalOrder())));
        Map<String, SequenceFlow> flowsById = sequenceFlows(def);
        Map<String, String> opinionByTask = new HashMap<>();
        Map<String, String> conclusionByTask = new HashMap<>();
        readComments(ref.getProcessInstanceId(), opinionByTask, conclusionByTask);

        Set<String> finishedNodes = new LinkedHashSet<>();
        Set<String> currentNodes = new LinkedHashSet<>();
        Set<String> finishedFlows = new LinkedHashSet<>();
        List<FlowDiagramDto.FlowNodeDetailDto> details = new ArrayList<>();
        Map<String, FlowDiagramDto.FlowNodeDetailDto> lastBySource = new HashMap<>();
        Map<String, String> names = new HashMap<>();
        Date now = new Date();

        FlowDiagramDto.FlowNodeDetailDto lastTask = null;
        for (HistoricActivityInstance instance : instances) {
            if (TYPE_SEQUENCE_FLOW.equals(instance.getActivityType())) {
                finishedFlows.add(instance.getActivityId());
                conclude(lastBySource, lastTask, flowsById.get(instance.getActivityId()));
                continue;
            }
            boolean finished = instance.getEndTime() != null;
            if (finished) {
                finishedNodes.add(instance.getActivityId());
            } else {
                currentNodes.add(instance.getActivityId());
            }
            if (isGateway(instance.getActivityType())) {
                continue;
            }
            FlowDiagramDto.FlowNodeDetailDto detail = detail(instance, finished, now, names, opinionByTask);
            if (instance.getTaskId() != null) {
                detail.setResultStatus(conclusionByTask.get(instance.getTaskId()));
            }
            details.add(detail);
            lastBySource.put(instance.getActivityId(), detail);
            if (instance.getTaskId() != null && finished) {
                lastTask = detail;
            }
        }

        // 回环节点可能既完成过又再次进入，以当前为准，避免同时着两种色。
        finishedNodes.removeAll(currentNodes);

        if (finishedFlows.isEmpty()) {
            finishedFlows.addAll(inferFlows(flowsById, finishedNodes, currentNodes));
        }

        dto.getFinishedNodes().addAll(finishedNodes);
        dto.getCurrentNodes().addAll(currentNodes);
        dto.getFinishedFlows().addAll(finishedFlows);
        dto.getNodeDetails().addAll(details);
        return dto;
    }

    private FlowDiagramDto.FlowNodeDetailDto detail(HistoricActivityInstance instance, boolean finished, Date now,
                                                    Map<String, String> names, Map<String, String> opinionByTask) {
        FlowDiagramDto.FlowNodeDetailDto detail = new FlowDiagramDto.FlowNodeDetailDto();
        detail.setNodeId(instance.getActivityId());
        detail.setNodeName(instance.getActivityName());
        detail.setAssigneeId(instance.getAssignee());
        detail.setAssigneeName(userName(instance.getAssignee(), names));
        detail.setStartTime(instance.getStartTime());
        detail.setEndTime(instance.getEndTime());
        detail.setCurrent(!finished);
        if (instance.getTaskId() != null) {
            detail.setOpinion(opinionByTask.get(instance.getTaskId()));
        }
        Date end = finished ? instance.getEndTime() : now;
        if (instance.getStartTime() != null && end != null) {
            detail.setDurationSeconds((end.getTime() - instance.getStartTime().getTime()) / 1000);
        }
        return detail;
    }

    /**
     * 连线带名称时，把名称记为其来源节点最近一次办理的结论，如“通过”“驳回”。
     * 来源是网关时，归到网关之前刚办结的那个任务。
     */
    private void conclude(Map<String, FlowDiagramDto.FlowNodeDetailDto> lastBySource,
                          FlowDiagramDto.FlowNodeDetailDto lastTask, SequenceFlow flow) {
        if (flow == null || !StringUtils.hasText(flow.getName())) {
            return;
        }
        FlowDiagramDto.FlowNodeDetailDto detail = lastBySource.get(flow.getSourceRef());
        if (detail == null) {
            detail = lastTask;
        }
        if (detail != null && Boolean.FALSE.equals(detail.getCurrent())) {
            detail.setConclusion(flow.getName().trim());
        }
    }

    private String userName(String userId, Map<String, String> names) {
        if (!StringUtils.hasText(userId)) {
            return null;
        }
        if (!names.containsKey(userId)) {
            RdmsCustomerUser user = rdmsCustomerUserService.selectByPrimaryKey(userId);
            names.put(userId, user == null ? null : user.getTrueName());
        }
        return names.get(userId);
    }

    /** 普通批注为意见，可多条拼接；结论类批注取最后一条。引擎自记的事件类批注不读。 */
    private void readComments(String processInstanceId, Map<String, String> opinionByTask,
                              Map<String, String> conclusionByTask) {
        if (!StringUtils.hasText(processInstanceId)) {
            return;
        }
        List<Comment> comments = taskService.getProcessInstanceComments(processInstanceId);
        if (comments == null) {
            return;
        }
        for (Comment comment : comments) {
            if (comment.getTaskId() == null || !StringUtils.hasText(comment.getFullMessage())) {
                continue;
            }
            if (FlowRuntimeService.COMMENT_CONCLUSION.equals(comment.getType())) {
                conclusionByTask.put(comment.getTaskId(), comment.getFullMessage());
            } else if (CommentEntity.TYPE_COMMENT.equals(comment.getType())) {
                opinionByTask.merge(comment.getTaskId(), comment.getFullMessage(), (left, right) -> left + "\n" + right);
            }
        }
    }

    private Map<String, SequenceFlow> sequenceFlows(RdmsFlowDef def) {
        Map<String, SequenceFlow> flows = new HashMap<>();
        if (!StringUtils.hasText(def.getDefinitionId())) {
            return flows;
        }
        BpmnModel model = flowDefinitionService.bpmnModel(def);
        if (model == null || model.getMainProcess() == null) {
            return flows;
        }
        for (FlowElement element : model.getMainProcess().getFlowElements()) {
            if (element instanceof SequenceFlow) {
                flows.put(element.getId(), (SequenceFlow) element);
            }
        }
        return flows;
    }

    private static boolean isGateway(String activityType) {
        return activityType != null && activityType.endsWith(TYPE_GATEWAY_SUFFIX);
    }

    /**
     * 兜底推断。audit 级历史已记连线，实测可直接取用，此处只为历史级别被调低或
     * 历史被清理时仍能着色：两端都已到达的连线视为已经过。
     * 该口径在含回环的图上会把回环的两条出边都算作已经过，仅用于着色，不作判定依据。
     */
    private List<String> inferFlows(Map<String, SequenceFlow> flowsById, Set<String> finishedNodes,
                                    Set<String> currentNodes) {
        List<String> flows = new ArrayList<>();
        Set<String> visited = new LinkedHashSet<>(finishedNodes);
        visited.addAll(currentNodes);
        for (SequenceFlow flow : flowsById.values()) {
            if (finishedNodes.contains(flow.getSourceRef()) && visited.contains(flow.getTargetRef())) {
                flows.add(flow.getId());
            }
        }
        return flows;
    }
}
