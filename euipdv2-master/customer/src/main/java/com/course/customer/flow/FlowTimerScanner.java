/**
 * Author：周朔鹏
 * 编制时间：2026-10-06
 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */

package com.course.customer.flow;

import com.course.server.service.rdms.flow.FlowTimerRules;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * 扫描已到期的节点时限。只在 {@code rdms.flow.timer-scan=true} 时装配。
 * 不打开 Flowable 异步作业执行器。
 */
@Component
@ConditionalOnProperty(name = "rdms.flow.timer-scan", havingValue = "true")
public class FlowTimerScanner {

    @Resource
    private TaskService taskService;

    @Resource
    private RuntimeService runtimeService;

    @Resource
    private FlowRuntimeService flowRuntimeService;

    @Scheduled(fixedDelayString = "${rdms.flow.timer-scan-delay-ms:60000}")
    public void scan() {
        List<Task> tasks = taskService.createTaskQuery().active().list();
        long now = System.currentTimeMillis();
        for (Task task : tasks) {
            if (!due(task, now)) {
                continue;
            }
            try {
                flowRuntimeService.applyTimer(task.getId());
            } catch (RuntimeException ex) {
                flowRuntimeService.retireTimer(task.getId(), ex.getMessage());
            }
        }
    }

    private boolean due(Task task, long now) {
        if (!StringUtils.hasText(task.getExecutionId())) {
            return false;
        }
        Object due = runtimeService.getVariableLocal(task.getExecutionId(), FlowTimerRules.DUE);
        return due instanceof Number && ((Number) due).longValue() <= now;
    }
}
