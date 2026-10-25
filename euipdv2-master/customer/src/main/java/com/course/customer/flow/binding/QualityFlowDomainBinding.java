/**
 * Author：周朔鹏
 * 编制时间：2026-10-06
 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */

package com.course.customer.flow.binding;

import com.course.server.domain.RdmsBugFeedback;
import com.course.server.domain.RdmsQuality;
import com.course.server.enums.rdms.QualityStatusEnum;
import com.course.server.exception.BusinessException;
import com.course.server.exception.BusinessExceptionCode;
import com.course.server.service.rdms.RdmsBugFeedbackService;
import com.course.server.service.rdms.RdmsQualityService;
import com.course.server.service.rdms.flow.FlowBizCode;
import com.course.server.service.rdms.flow.FlowContext;
import com.course.server.service.rdms.flow.FlowDomainBinding;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 质量单与缺陷反馈单共用业务码 QUALITY。业务主键带前缀，Q: 为质量单，B: 为缺陷反馈单。
 * 两张单据都没有 next_node 列，待办仍按产品经理或测试主管查询。
 */
@Component
public class QualityFlowDomainBinding implements FlowDomainBinding {

    public static final String QUALITY_PREFIX = "Q:";
    public static final String BUG_PREFIX = "B:";

    private static final Set<String> VALID_STATUSES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            QualityStatusEnum.SAVE.getStatus(),
            QualityStatusEnum.SUBMIT.getStatus(),
            QualityStatusEnum.SAVE_ANALYSIS.getStatus(),
            QualityStatusEnum.JOB_SUBMIT.getStatus(),
            QualityStatusEnum.RECHECK.getStatus(),
            QualityStatusEnum.REJECT.getStatus(),
            QualityStatusEnum.ARCHIVED.getStatus())));

    @Resource
    private RdmsQualityService rdmsQualityService;

    @Resource
    private RdmsBugFeedbackService rdmsBugFeedbackService;

    @Override
    public String bizCode() {
        return FlowBizCode.QUALITY.getCode();
    }

    @Override
    public Set<String> validStatuses() {
        return VALID_STATUSES;
    }

    @Override
    public String currentStatus(FlowContext context) {
        Object entity = context.getBizEntity();
        if (entity instanceof RdmsQuality) {
            return ((RdmsQuality) entity).getStatus();
        }
        if (entity instanceof RdmsBugFeedback) {
            return ((RdmsBugFeedback) entity).getStatus();
        }
        return null;
    }

    @Override
    public void writeStatus(FlowContext context, String targetStatus) {
        assertTransition(currentStatus(context), targetStatus);
        Object entity = context.getBizEntity();
        if (entity instanceof RdmsBugFeedback) {
            RdmsBugFeedback current = (RdmsBugFeedback) entity;
            RdmsBugFeedback update = new RdmsBugFeedback();
            update.setId(current.getId());
            update.setStatus(targetStatus);
            rdmsBugFeedbackService.updateByPrimaryKeySelective(update);
            current.setStatus(targetStatus);
            return;
        }
        if (entity instanceof RdmsQuality) {
            RdmsQuality current = (RdmsQuality) entity;
            RdmsQuality update = new RdmsQuality();
            update.setId(current.getId());
            update.setStatus(targetStatus);
            rdmsQualityService.updateByPrimaryKeySelective(update);
            current.setStatus(targetStatus);
            return;
        }
        throw new BusinessException(BusinessExceptionCode.FLOW_INSTANCE_NOT_FOUND, "质量或缺陷单据不在流程上下文中");
    }

    @Override
    public void writeProcess(FlowContext context, String text) {
        // 单据本身没有过程表。处理工单的过程记录仍由工单接口写入。
    }

    @Override
    public void writeNextNode(FlowContext context, String assigneeId) {
        // 质量单与缺陷反馈单没有 next_node。列表按产品经理、测试主管查询。
    }

    static void assertTransition(String source, String target) {
        if (!StringUtils.hasText(source)) {
            if (QualityStatusEnum.SAVE.getStatus().equals(target)
                    || QualityStatusEnum.SUBMIT.getStatus().equals(target)) {
                return;
            }
            throw forbidden(source, target);
        }
        if (QualityStatusEnum.SAVE.getStatus().equals(source)
                && QualityStatusEnum.SUBMIT.getStatus().equals(target)) {
            return;
        }
        if ((QualityStatusEnum.SUBMIT.getStatus().equals(source)
                || QualityStatusEnum.SAVE_ANALYSIS.getStatus().equals(source))
                && (QualityStatusEnum.SAVE_ANALYSIS.getStatus().equals(target)
                || QualityStatusEnum.JOB_SUBMIT.getStatus().equals(target))) {
            return;
        }
        if ((QualityStatusEnum.JOB_SUBMIT.getStatus().equals(source)
                || QualityStatusEnum.REJECT.getStatus().equals(source))
                && QualityStatusEnum.RECHECK.getStatus().equals(target)) {
            return;
        }
        if (QualityStatusEnum.RECHECK.getStatus().equals(source)
                && (QualityStatusEnum.ARCHIVED.getStatus().equals(target)
                || QualityStatusEnum.REJECT.getStatus().equals(target))) {
            return;
        }
        throw forbidden(source, target);
    }

    private static BusinessException forbidden(String source, String target) {
        return new BusinessException(BusinessExceptionCode.LIFECYCLE_TRANSITION_FORBIDDEN,
                "质量或缺陷单据不能从 " + source + " 改为 " + target);
    }
}
