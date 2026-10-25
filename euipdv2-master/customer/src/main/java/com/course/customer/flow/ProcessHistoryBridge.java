/**
 * Author：周朔鹏
 * 编制时间：2026-10-06
 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */

package com.course.customer.flow;

import com.course.server.domain.RdmsFlowBizRef;
import com.course.server.mapper.RdmsFlowBizRefMapper;
import com.course.server.service.rdms.flow.ProcessHistoryItem;
import com.course.server.service.rdms.flow.ProcessHistoryPort;
import org.flowable.engine.HistoryService;
import org.flowable.engine.TaskService;
import org.flowable.engine.impl.persistence.entity.CommentEntity;
import org.flowable.engine.task.Comment;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 从已结束的流程任务读取处理人、时间与意见。没有业务映射时返回空列表。
 */
@Primary
@Service("processHistoryPort")
public class ProcessHistoryBridge implements ProcessHistoryPort {

    @Resource
    private RdmsFlowBizRefMapper rdmsFlowBizRefMapper;

    @Resource
    private HistoryService historyService;

    @Resource
    private TaskService taskService;

    @Override
    public List<ProcessHistoryItem> list(String customerId, String bizCode, String bizId) {
        if (!StringUtils.hasText(customerId) || !StringUtils.hasText(bizCode) || !StringUtils.hasText(bizId)) {
            return Collections.emptyList();
        }
        RdmsFlowBizRef ref = rdmsFlowBizRefMapper.selectByBiz(customerId, bizCode, bizId);
        if (ref == null || !StringUtils.hasText(ref.getProcessInstanceId())) {
            return Collections.emptyList();
        }
        List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(ref.getProcessInstanceId())
                .orderByHistoricTaskInstanceEndTime().asc()
                .list();
        Map<String, String> opinionByTask = new HashMap<>();
        List<Comment> comments = taskService.getProcessInstanceComments(ref.getProcessInstanceId());
        if (comments != null) {
            for (Comment comment : comments) {
                if (comment.getTaskId() == null || !StringUtils.hasText(comment.getFullMessage())
                        || !CommentEntity.TYPE_COMMENT.equals(comment.getType())) {
                    continue;
                }
                opinionByTask.merge(comment.getTaskId(), comment.getFullMessage(), (left, right) -> left + "\n" + right);
            }
        }
        List<ProcessHistoryItem> rows = new ArrayList<>();
        if (tasks == null) {
            return rows;
        }
        for (HistoricTaskInstance task : tasks) {
            if (task.getEndTime() == null) {
                continue;
            }
            ProcessHistoryItem item = new ProcessHistoryItem();
            item.setActorId(task.getAssignee());
            item.setOccurredAt(task.getEndTime());
            item.setOpinion(opinionByTask.get(task.getId()));
            rows.add(item);
        }
        return rows;
    }
}
