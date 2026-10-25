/**
 * Author：周朔鹏
 * 编制时间：2026-10-06
 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */

package com.course.customer.flow;

import com.course.server.domain.RdmsJobItem;
import com.course.server.exception.BusinessException;
import com.course.server.exception.BusinessExceptionCode;
import com.course.server.mapper.RdmsFlowBizRefMapper;
import com.course.server.service.rdms.RdmsJobItemService;
import com.course.server.service.rdms.flow.FlowBizCode;
import com.course.server.service.rdms.flow.FlowContext;
import com.course.server.service.rdms.flow.ReviewFlowPort;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 评审主单与流程运行时的衔接。新评审是否走引擎由开关决定。
 * 已经挂上实例的评审，后续会签与审结继续走引擎。
 */
@Primary
@Service("reviewFlowPort")
public class ReviewFlowBridge implements ReviewFlowPort {

    @Resource
    private FlowSwitch flowSwitch;

    @Resource
    private FlowRuntimeService flowRuntimeService;

    @Resource
    private RdmsFlowBizRefMapper rdmsFlowBizRefMapper;

    @Resource
    private RdmsJobItemService rdmsJobItemService;

    @Override
    public boolean engineForNew(String customerId) {
        return StringUtils.hasText(customerId)
                && flowSwitch.enabled(customerId, FlowBizCode.REVIEW.getCode());
    }

    @Override
    public boolean engineForExisting(String customerId, String jobItemId) {
        if (!StringUtils.hasText(customerId) || !StringUtils.hasText(jobItemId)) {
            return false;
        }
        return rdmsFlowBizRefMapper.selectByBiz(customerId, FlowBizCode.REVIEW.getCode(), jobItemId) != null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void open(String jobItemId, String operatorId, List<String> reviewerIds) {
        RdmsJobItem job = require(jobItemId);
        List<String> reviewers = distinct(reviewerIds);
        FlowContext context = context(job, operatorId);
        context.putVariable("reviewerIds", reviewers);
        context.putVariable("hasReviewers", reviewers.isEmpty() ? "NO" : "YES");
        flowRuntimeService.start(context);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sign(String mainJobItemId, String operatorId) {
        RdmsJobItem job = require(mainJobItemId);
        FlowContext context = context(job, operatorId);
        context.setNodeId("sign");
        flowRuntimeService.complete(context);
    }

    /** 审结人由调用方按下一节点校验，最终管理人可代办，推进前把审结待办改派给操作人。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void conclude(String jobItemId, String operatorId, String reviewResult) {
        RdmsJobItem job = require(jobItemId);
        FlowContext context = context(job, operatorId);
        context.putVariable("reviewResult", reviewResult);
        if (StringUtils.hasText(operatorId)) {
            flowRuntimeService.reassignCurrent(context, operatorId);
        }
        flowRuntimeService.complete(context);
    }

    private FlowContext context(RdmsJobItem job, String operatorId) {
        FlowContext context = new FlowContext();
        context.setCustomerId(job.getCustomerId());
        context.setBizCode(FlowBizCode.REVIEW.getCode());
        context.setBizId(job.getId());
        context.setOperatorId(operatorId);
        context.setInitiatorId(job.getCreaterId() == null ? operatorId : job.getCreaterId());
        context.setJobItem(job);
        context.setBizEntity(job);
        return context;
    }

    private RdmsJobItem require(String jobItemId) {
        RdmsJobItem job = rdmsJobItemService.selectByPrimaryKey(jobItemId);
        if (job == null) {
            throw new BusinessException(BusinessExceptionCode.DB_RECORD_NOT_EXIST);
        }
        return job;
    }

    private static List<String> distinct(List<String> reviewerIds) {
        Set<String> ids = new LinkedHashSet<>();
        if (reviewerIds != null) {
            for (String id : reviewerIds) {
                if (StringUtils.hasText(id)) {
                    ids.add(id);
                }
            }
        }
        return new ArrayList<>(ids);
    }
}
