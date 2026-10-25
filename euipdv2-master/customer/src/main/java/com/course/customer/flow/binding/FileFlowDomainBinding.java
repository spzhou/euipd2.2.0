/**
 * Author：周朔鹏
 * 编制时间：2026-10-06
 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */

package com.course.customer.flow.binding;

import com.course.server.domain.RdmsFileApply;
import com.course.server.enums.rdms.FileApplicationStatusEnum;
import com.course.server.exception.BusinessException;
import com.course.server.exception.BusinessExceptionCode;
import com.course.server.service.rdms.RdmsFileApplyService;
import com.course.server.service.rdms.flow.FlowBizCode;
import com.course.server.service.rdms.flow.FlowContext;
import com.course.server.service.rdms.flow.FlowDomainBinding;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * 档案申请的领域绑定。档案没有过程表，过程动作不落库。
 * 下载签发把已同意申请改为 COMPLETE，仍由签发接口完成，不经过本绑定。
 */
@Component
public class FileFlowDomainBinding implements FlowDomainBinding {

    private static final Set<String> VALID_STATUSES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            FileApplicationStatusEnum.APPLICATION.getStatus(),
            FileApplicationStatusEnum.APPROVED.getStatus(),
            FileApplicationStatusEnum.REJECT.getStatus(),
            FileApplicationStatusEnum.COMPLETE.getStatus())));

    @Resource
    private RdmsFileApplyService rdmsFileApplyService;

    @Override
    public String bizCode() {
        return FlowBizCode.FILE_APPLY.getCode();
    }

    @Override
    public Set<String> validStatuses() {
        return VALID_STATUSES;
    }

    @Override
    public String currentStatus(FlowContext context) {
        return require(context).getApplicationStatus();
    }

    @Override
    public void writeStatus(FlowContext context, String targetStatus) {
        RdmsFileApply current = require(context);
        assertTransition(current.getApplicationStatus(), targetStatus);
        RdmsFileApply update = new RdmsFileApply();
        update.setId(current.getId());
        update.setApplicationStatus(targetStatus);
        Date now = new Date();
        if (!FileApplicationStatusEnum.APPLICATION.getStatus().equals(targetStatus)) {
            update.setApprovalTime(now);
        }
        update.setUpdateTime(now);
        rdmsFileApplyService.updateByPrimaryKeySelective(update);
        current.setApplicationStatus(targetStatus);
    }

    @Override
    public void writeProcess(FlowContext context, String text) {
        // 档案申请没有过程表。节点上的过程文案只供流程图阅读。
    }

    @Override
    public void writeNextNode(FlowContext context, String assigneeId) {
        RdmsFileApply current = require(context);
        if (!StringUtils.hasText(assigneeId)) {
            return;
        }
        RdmsFileApply update = new RdmsFileApply();
        update.setId(current.getId());
        update.setApproverId(assigneeId);
        rdmsFileApplyService.updateByPrimaryKeySelective(update);
        current.setApproverId(assigneeId);
    }

    private RdmsFileApply require(FlowContext context) {
        Object entity = context.getBizEntity();
        if (!(entity instanceof RdmsFileApply) || !StringUtils.hasText(((RdmsFileApply) entity).getId())) {
            throw new BusinessException(BusinessExceptionCode.FLOW_INSTANCE_NOT_FOUND, "档案申请不在流程上下文中");
        }
        return (RdmsFileApply) entity;
    }

    /** 空白只可记为申请；申请中只可同意或驳回。不使用同态放行。 */
    private static void assertTransition(String source, String target) {
        if (!StringUtils.hasText(source)) {
            if (FileApplicationStatusEnum.APPLICATION.getStatus().equals(target)) {
                return;
            }
            throw forbidden(source, target);
        }
        if (FileApplicationStatusEnum.APPLICATION.getStatus().equals(source)
                && (FileApplicationStatusEnum.APPROVED.getStatus().equals(target)
                || FileApplicationStatusEnum.REJECT.getStatus().equals(target))) {
            return;
        }
        throw forbidden(source, target);
    }

    private static BusinessException forbidden(String source, String target) {
        return new BusinessException(BusinessExceptionCode.LIFECYCLE_TRANSITION_FORBIDDEN,
                "档案申请不能从 " + source + " 改为 " + target);
    }
}
