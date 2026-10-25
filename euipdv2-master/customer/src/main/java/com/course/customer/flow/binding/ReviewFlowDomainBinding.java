/**
 * Author：周朔鹏
 * 编制时间：2026-10-06
 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */

package com.course.customer.flow.binding;

import com.course.server.domain.RdmsJobItem;
import com.course.server.enums.rdms.JobItemStatusEnum;
import com.course.server.exception.BusinessException;
import com.course.server.exception.BusinessExceptionCode;
import com.course.server.service.rdms.JobItemProcessRecorder;
import com.course.server.service.rdms.RdmsJobItemService;
import com.course.server.service.rdms.flow.FlowBizCode;
import com.course.server.service.rdms.flow.FlowContext;
import com.course.server.service.rdms.flow.FlowDomainBinding;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 评审主单的领域绑定。审结时的状态与过程仍由四个结论处理器写入。
 * 本绑定供门禁读取当前状态，并在节点显式配置状态动作时沿用工单转移规则。
 */
@Component
public class ReviewFlowDomainBinding implements FlowDomainBinding {

    private static final Set<String> VALID_STATUSES;

    static {
        Set<String> statuses = new HashSet<>();
        for (JobItemStatusEnum item : JobItemStatusEnum.values()) {
            statuses.add(item.getStatus());
        }
        VALID_STATUSES = Collections.unmodifiableSet(statuses);
    }

    @Resource
    private RdmsJobItemService rdmsJobItemService;

    @Resource
    private JobItemProcessRecorder jobItemProcessRecorder;

    @Override
    public String bizCode() {
        return FlowBizCode.REVIEW.getCode();
    }

    @Override
    public Set<String> validStatuses() {
        return VALID_STATUSES;
    }

    @Override
    public String currentStatus(FlowContext context) {
        return require(context).getStatus();
    }

    @Override
    public void writeStatus(FlowContext context, String targetStatus) {
        RdmsJobItem job = require(context);
        JobFlowDomainBinding.assertTransition(job.getStatus(), targetStatus, context.variableAsString("jobMove"));
        job.setStatus(targetStatus);
        rdmsJobItemService.update(job);
        context.setJobItem(job);
        context.setBizEntity(job);
    }

    @Override
    public void writeProcess(FlowContext context, String text) {
        jobItemProcessRecorder.complete(context.getBizId(), context.getOperatorId(), text);
    }

    @Override
    public void writeNextNode(FlowContext context, String assigneeId) {
        RdmsJobItem job = require(context);
        job.setNextNode(assigneeId);
        rdmsJobItemService.update(job);
        context.setJobItem(job);
        context.setBizEntity(job);
    }

    private RdmsJobItem require(FlowContext context) {
        if (context.getJobItem() != null && context.getBizId().equals(context.getJobItem().getId())) {
            return context.getJobItem();
        }
        RdmsJobItem job = rdmsJobItemService.selectByPrimaryKey(context.getBizId());
        if (job == null) {
            throw new BusinessException(BusinessExceptionCode.DB_RECORD_NOT_EXIST);
        }
        context.setJobItem(job);
        context.setBizEntity(job);
        return job;
    }
}
