/**
 * Author：周朔鹏
 * 编制时间：2026-10-06
 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */

package com.course.customer.flow;

import com.course.server.domain.RdmsJobItem;
import com.course.server.domain.RdmsProjectSubproject;
import com.course.server.dto.rdms.RdmsJobItemDto;
import com.course.server.enums.rdms.JobItemTypeEnum;
import com.course.server.enums.rdms.ProjectTypeEnum;
import com.course.server.exception.BusinessException;
import com.course.server.exception.BusinessExceptionCode;
import com.course.server.mapper.RdmsFlowBizRefMapper;
import com.course.server.service.rdms.RdmsJobItemService;
import com.course.server.service.rdms.RdmsSubprojectService;
import com.course.server.service.rdms.flow.FlowBizCode;
import com.course.server.service.rdms.flow.FlowContext;
import com.course.server.service.rdms.flow.JobFlowPort;
import com.course.server.service.rdms.flow.JobItemTrunk;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

/**
 * 工单主干六个流转入口与流程运行时的衔接。
 * 新工单是否走引擎由开关决定。已经挂上实例的工单，后续调用继续走引擎。
 * 评审、通知、组件定义与集成不经本类。
 */
@Primary
@Service("jobFlowPort")
public class JobFlowBridge implements JobFlowPort {

    private static final String ROUTED = "ROUTED";
    private static final String PROPERTY = "PROPERTY";
    private static final String TEST = "TEST";
    private static final String BUG_TEST = "BUG_TEST";

    @Resource
    private FlowSwitch flowSwitch;

    @Resource
    private FlowRuntimeService flowRuntimeService;

    @Resource
    private RdmsFlowBizRefMapper rdmsFlowBizRefMapper;

    @Resource
    private RdmsJobItemService rdmsJobItemService;

    @Resource
    private RdmsSubprojectService rdmsSubprojectService;

    @Override
    public boolean engineForNew(String customerId) {
        return StringUtils.hasText(customerId)
                && flowSwitch.enabled(customerId, FlowBizCode.JOB_ITEM.getCode());
    }

    @Override
    public boolean engineForExisting(String customerId, String jobItemId) {
        if (!StringUtils.hasText(customerId) || !StringUtils.hasText(jobItemId)) {
            return false;
        }
        return rdmsFlowBizRefMapper.selectByBiz(customerId, FlowBizCode.JOB_ITEM.getCode(), jobItemId) != null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void openIssue(String jobItemId, String operatorId) {
        RdmsJobItem job = require(jobItemId);
        FlowContext context = context(job, operatorId);
        context.putVariable("processKind", "ISSUE");
        context.putVariable("processText", JobItemTrunk.issueText(job.getType()));
        flowRuntimeService.start(context);
        flowRuntimeService.complete(context);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitRouted(RdmsJobItem job, RdmsJobItemDto request) {
        if (request == null || ObjectUtils.isEmpty(request.getProjectType())
                || ProjectTypeEnum.SUB_PROJECT.getType().equals(request.getProjectType())) {
            throw new BusinessException(BusinessExceptionCode.INPUT_ERROR);
        }
        RdmsJobItem fresh = require(job.getId());
        if (ProjectTypeEnum.DEV_PROJECT.getType().equals(request.getProjectType())) {
            RdmsProjectSubproject subproject = rdmsSubprojectService.selectByPrimaryKey(request.getSubprojectId());
            if (subproject == null || !StringUtils.hasText(subproject.getProjectManagerId())) {
                throw new BusinessException(BusinessExceptionCode.FLOW_ASSIGNEE_NOT_FOUND, "子项目经理为空");
            }
            fresh.setProjectManagerId(subproject.getProjectManagerId());
            rdmsJobItemService.update(fresh);
        }
        String text = request.getJobDescription();
        move(fresh.getId(), actor(request.getLoginUserId(), fresh.getExecutorId()),
                "SUBMIT", "SUBMIT", StringUtils.hasText(text) ? text : "任务提交", ROUTED, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitProperty(RdmsJobItem job, String operatorId, String description) {
        String route = JobItemTypeEnum.BUG.getType().equals(job.getType()) ? BUG_TEST : PROPERTY;
        move(job.getId(), operatorId, "SUBMIT", "SUBMIT",
                StringUtils.hasText(description) ? description : "任务提交", route, null);
    }

    /** 质量工单交给工单上的产品经理，缺陷工单交给测试主管。代办人须已由调用方校验。 */
    public void submitWithRoute(RdmsJobItem job, String operatorId, String description, String route) {
        move(job.getId(), operatorId, "SUBMIT", "SUBMIT",
                StringUtils.hasText(description) ? description : "任务提交", route, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitTest(RdmsJobItem job, String operatorId) {
        move(job.getId(), operatorId, "SUBMIT", "SUBMIT", "提交测试任务", TEST, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(RdmsJobItem job, String operatorId, String description) {
        move(job.getId(), operatorId, "HANDLING", "REJECT", description, null, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeEvaluate(String jobItemId, String operatorId) {
        move(jobItemId, operatorId, "COMPLETED", "COMPLETE", "对工单质量进行了评价", null, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(RdmsJobItem job, String operatorId) {
        move(job.getId(), operatorId, "CANCEL", "CANCEL", "工作任务取消", null, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refuse(RdmsJobItem job, String operatorId, String description) {
        move(job.getId(), operatorId, "REFUSE", "REFUSE", description, null, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recheck(RdmsJobItem job, String operatorId, String description) {
        String text = StringUtils.hasText(description) ? description : "复核完成";
        move(job.getId(), operatorId, "COMPLETED", "COMPLETE", text, null, "RECHECK");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reissue(RdmsJobItem job, String operatorId) {
        move(job.getId(), operatorId, "HANDLING", "ISSUE", "重新发工单", null, null);
    }

    /**
     * 操作人已由调用方经 JobActorGuard 判定（执行人、审批人、创建人、项目经理、最终管理人代办等），
     * 不一定是当前待办处理人，推进前先把待办改派给操作人。
     */
    private void move(String jobItemId, String operatorId, String jobStatus, String processKind,
                      String processText, String submitRoute, String jobMove) {
        RdmsJobItem fresh = require(jobItemId);
        FlowContext context = context(fresh, operatorId);
        if (StringUtils.hasText(operatorId)) {
            flowRuntimeService.reassignCurrent(context, operatorId);
        }
        context.putVariable("jobStatus", jobStatus);
        context.putVariable("processKind", processKind);
        context.putVariable("processText", processText);
        context.putVariable("projectType", fresh.getProjectType());
        context.putVariable("definitionJob", JobItemTrunk.definitionType(fresh.getType()));
        if (submitRoute != null) {
            context.putVariable("submitRoute", submitRoute);
        }
        if (jobMove != null) {
            context.putVariable("jobMove", jobMove);
        }
        flowRuntimeService.complete(context);
    }

    private FlowContext context(RdmsJobItem job, String operatorId) {
        FlowContext context = new FlowContext();
        context.setCustomerId(job.getCustomerId());
        context.setBizCode(FlowBizCode.JOB_ITEM.getCode());
        context.setBizId(job.getId());
        context.setOperatorId(operatorId);
        context.setInitiatorId(job.getCreaterId());
        context.setBizEntity(job);
        context.setJobItem(job);
        return context;
    }

    private RdmsJobItem require(String jobItemId) {
        RdmsJobItem job = rdmsJobItemService.selectByPrimaryKey(jobItemId);
        if (job == null) {
            throw new BusinessException(BusinessExceptionCode.DB_RECORD_NOT_EXIST);
        }
        return job;
    }

    private static String actor(String loginUserId, String fallback) {
        return StringUtils.hasText(loginUserId) ? loginUserId : fallback;
    }
}
