/**
 * Author：周朔鹏
 * 编制时间：2026-10-06
 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */

package com.course.customer.flow.binding;

import com.course.server.domain.RdmsJobItem;
import com.course.server.domain.RdmsJobItemProperty;
import com.course.server.enums.rdms.JobItemStatusEnum;
import com.course.server.exception.BusinessException;
import com.course.server.exception.BusinessExceptionCode;
import com.course.server.service.rdms.JobItemProcessRecorder;
import com.course.server.service.rdms.LifecycleTransition;
import com.course.server.service.rdms.RdmsJobItemPropertyService;
import com.course.server.service.rdms.RdmsJobItemService;
import com.course.server.service.rdms.flow.FlowBizCode;
import com.course.server.service.rdms.flow.FlowContext;
import com.course.server.service.rdms.flow.FlowDomainBinding;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * 工单主干的领域绑定。状态转移使用 {@link JobItemStatusEnum} 上已有的来源集合，
 * 不使用 {@link LifecycleTransition#allows}，以免同态写入被放行。
 * 绩效工时与人员费流水仍由评价服务在完成本节点之前写入。
 */
@Component
public class JobFlowDomainBinding implements FlowDomainBinding {

    static final String MOVE_RECHECK = "RECHECK";

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
    @Resource
    private RdmsJobItemPropertyService rdmsJobItemPropertyService;

    @Override
    public String bizCode() {
        return FlowBizCode.JOB_ITEM.getCode();
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
        String move = context.variableAsString("jobMove");
        assertTransition(job.getStatus(), targetStatus, move);
        if (JobItemStatusEnum.CANCEL.getStatus().equals(job.getStatus())
                && JobItemStatusEnum.CANCEL.getStatus().equals(targetStatus)) {
            return;
        }
        job.setStatus(targetStatus);
        if (JobItemStatusEnum.SUBMIT.getStatus().equals(targetStatus)
                || JobItemStatusEnum.CANCEL.getStatus().equals(targetStatus)
                || JobItemStatusEnum.REFUSE.getStatus().equals(targetStatus)) {
            job.setActualSubmissionTime(new Date());
        }
        if (JobItemStatusEnum.HANDLING.getStatus().equals(targetStatus)) {
            job.setActualSubmissionTime(null);
        }
        if (JobItemStatusEnum.COMPLETED.getStatus().equals(targetStatus)) {
            Date now = new Date();
            job.setCompleteTime(now);
            job.setNextNode(null);
            if (MOVE_RECHECK.equals(move)) {
                job.setActualSubmissionTime(now);
            }
        }
        if (JobItemStatusEnum.CANCEL.getStatus().equals(targetStatus)) {
            job.setNextNode(null);
        }
        rdmsJobItemService.update(job);
        context.setJobItem(job);
        context.setBizEntity(job);
    }

    @Override
    public void writeProcess(FlowContext context, String text) {
        RdmsJobItem job = reload(context.getBizId());
        String kind = context.variableAsString("processKind");
        String actor = context.getOperatorId();
        if ("SUBMIT".equals(kind)) {
            jobItemProcessRecorder.submit(job.getId(), actor, job.getNextNode(), text, submittedFileList(job));
            return;
        }
        if ("REJECT".equals(kind)) {
            jobItemProcessRecorder.reject(job.getId(), actor, job.getNextNode(), text);
            return;
        }
        if ("COMPLETE".equals(kind)) {
            jobItemProcessRecorder.complete(job.getId(), actor, text);
            return;
        }
        if ("CANCEL".equals(kind)) {
            jobItemProcessRecorder.cancel(job.getId(), actor, text);
            return;
        }
        if ("REFUSE".equals(kind)) {
            jobItemProcessRecorder.refuse(job.getId(), actor, job.getNextNode(), text);
            return;
        }
        if ("ISSUE".equals(kind)) {
            jobItemProcessRecorder.issue(job.getId(), actor, job.getNextNode(), text);
            return;
        }
        throw new BusinessException(BusinessExceptionCode.FLOW_NODE_CONFIG_INVALID, "工单过程类型未指定");
    }

    @Override
    public void writeNextNode(FlowContext context, String assigneeId) {
        RdmsJobItem job = require(context);
        job.setNextNode(assigneeId);
        rdmsJobItemService.update(job);
        context.setJobItem(job);
        context.setBizEntity(job);
    }

    /**
     * 复核完成允许从任意未收口状态到 COMPLETED，与现行复核入口一致。
     * 评价完成只接受评价来源集合，避免把处理中的工单直接评完。
     */
    static void assertTransition(String from, String to, String move) {
        if (!StringUtils.hasText(to) || !VALID_STATUSES.contains(to)) {
            throw new BusinessException(BusinessExceptionCode.JOB_ITEM_STATUS_INVALID);
        }
        if (JobItemStatusEnum.HANDLING.getStatus().equals(to)) {
            if (!StringUtils.hasText(from)
                    || JobItemStatusEnum.rejectSources().contains(from)
                    || JobItemStatusEnum.reissueSources().contains(from)) {
                return;
            }
            throw new BusinessException(BusinessExceptionCode.JOB_ITEM_STATUS_INVALID);
        }
        if (JobItemStatusEnum.SUBMIT.getStatus().equals(to)) {
            LifecycleTransition.requireSource(from, JobItemStatusEnum.propertySubmitSources(),
                    BusinessExceptionCode.JOB_ITEM_STATUS_INVALID);
            return;
        }
        if (JobItemStatusEnum.REFUSE.getStatus().equals(to)) {
            LifecycleTransition.requireSource(from, JobItemStatusEnum.refuseSources(),
                    BusinessExceptionCode.JOB_ITEM_STATUS_INVALID);
            return;
        }
        if (JobItemStatusEnum.COMPLETED.getStatus().equals(to)) {
            if (MOVE_RECHECK.equals(move)) {
                if (from != null && !JobItemStatusEnum.closedStatuses().contains(from)) {
                    return;
                }
                throw new BusinessException(BusinessExceptionCode.JOB_ITEM_STATUS_INVALID);
            }
            LifecycleTransition.requireSource(from, JobItemStatusEnum.evaluateSources(),
                    BusinessExceptionCode.JOB_ITEM_STATUS_INVALID);
            return;
        }
        if (JobItemStatusEnum.CANCEL.getStatus().equals(to)) {
            if (JobItemStatusEnum.CANCEL.getStatus().equals(from)) {
                return;
            }
            if (from == null || JobItemStatusEnum.closedStatuses().contains(from)) {
                throw new BusinessException(BusinessExceptionCode.JOB_ITEM_STATUS_INVALID);
            }
            return;
        }
        throw new BusinessException(BusinessExceptionCode.LIFECYCLE_TRANSITION_FORBIDDEN);
    }

    private RdmsJobItem require(FlowContext context) {
        if (context.getBizEntity() instanceof RdmsJobItem) {
            return (RdmsJobItem) context.getBizEntity();
        }
        if (context.getJobItem() != null && context.getBizId().equals(context.getJobItem().getId())) {
            context.setBizEntity(context.getJobItem());
            return context.getJobItem();
        }
        RdmsJobItem job = reload(context.getBizId());
        context.setJobItem(job);
        context.setBizEntity(job);
        return job;
    }

    private RdmsJobItem reload(String jobItemId) {
        RdmsJobItem job = rdmsJobItemService.selectByPrimaryKey(jobItemId);
        if (job == null) {
            throw new BusinessException(BusinessExceptionCode.DB_RECORD_NOT_EXIST);
        }
        return job;
    }

    /** 提交交付件时过程记录带上该次提交的文件清单。 */
    private String submittedFileList(RdmsJobItem job) {
        if (!StringUtils.hasText(job.getPropertyId())) {
            return null;
        }
        RdmsJobItemProperty property = rdmsJobItemPropertyService.selectByPrimaryKey(job.getPropertyId());
        return property == null ? null : property.getFileListStr();
    }
}
