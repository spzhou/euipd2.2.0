/**
 * Author：周朔鹏
 * 编制时间：2026-10-09
 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */
package com.course.customer.flow;

import com.course.server.domain.RdmsFlowBizRef;
import com.course.server.mapper.RdmsFlowBizRefMapper;
import com.course.server.service.rdms.flow.FlowTaskHandover;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 工作移交时改派引擎中的当前任务。
 * <p>
 * 实例沿用定义的租户，平台默认定义下的任务租户不是机构标识，机构归属只能经
 * rdms_flow_biz_ref 按实例判定。
 * <p>
 * 业务表的下一处理人由移交服务另行改写，本类只改引擎侧：任务处理人、非会签任务的
 * {@link FlowRuntimeService#VAR_CURRENT_ASSIGNEE} 与在途实例的 {@link FlowRuntimeService#VAR_INITIATOR}。
 * 会签任务的处理人只在节点进入时取自执行本地变量 reviewerId，之后改任务处理人即可。
 */
@Service
public class FlowHandoverService implements FlowTaskHandover {

    static final String HANDOVER_COMMENT = "工作移交";

    @Resource
    private TaskService taskService;

    @Resource
    private RuntimeService runtimeService;

    @Resource
    private RdmsFlowBizRefMapper rdmsFlowBizRefMapper;

    @Override
    public int countTasks(String customerId, String fromId) {
        return tasksOf(customerId, fromId).size();
    }

    @Override
    public int transferTasks(String customerId, String fromId, String toId) {
        if (!StringUtils.hasText(toId)) {
            return 0;
        }
        List<Task> tasks = tasksOf(customerId, fromId);
        for (Task task : tasks) {
            taskService.setAssignee(task.getId(), toId);
            if (!isLoopTask(task) && fromId.equals(text(runtimeService.getVariable(
                    task.getProcessInstanceId(), FlowRuntimeService.VAR_CURRENT_ASSIGNEE)))) {
                runtimeService.setVariable(task.getProcessInstanceId(), FlowRuntimeService.VAR_CURRENT_ASSIGNEE, toId);
            }
            taskService.addComment(task.getId(), task.getProcessInstanceId(), HANDOVER_COMMENT);
        }
        List<ProcessInstance> initiated = runtimeService.createProcessInstanceQuery()
                .variableValueEquals(FlowRuntimeService.VAR_INITIATOR, fromId)
                .list();
        for (ProcessInstance instance : initiated) {
            if (belongsTo(customerId, instance.getId())) {
                runtimeService.setVariable(instance.getId(), FlowRuntimeService.VAR_INITIATOR, toId);
            }
        }
        return tasks.size();
    }

    private List<Task> tasksOf(String customerId, String fromId) {
        List<Task> matched = new ArrayList<>();
        if (!StringUtils.hasText(customerId) || !StringUtils.hasText(fromId)) {
            return matched;
        }
        for (Task task : taskService.createTaskQuery().taskAssignee(fromId).list()) {
            if (belongsTo(customerId, task.getProcessInstanceId())) {
                matched.add(task);
            }
        }
        return matched;
    }

    private boolean belongsTo(String customerId, String processInstanceId) {
        RdmsFlowBizRef ref = rdmsFlowBizRefMapper.selectByInstance(processInstanceId);
        return ref != null && customerId.equals(ref.getCustomerId());
    }

    private boolean isLoopTask(Task task) {
        return StringUtils.hasText(task.getExecutionId())
                && runtimeService.getVariableLocal(task.getExecutionId(), "reviewerId") != null;
    }

    private static String text(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
