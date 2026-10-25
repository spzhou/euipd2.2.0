/**
 * Author：周朔鹏
 * 编制时间：2026-10-06
 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */

package com.course.customer.flow.binding;

import com.course.server.domain.RdmsFeeManage;
import com.course.server.domain.RdmsFeeProcess;
import com.course.server.dto.rdms.RdmsFeeManageDto;
import com.course.server.enums.rdms.ApplicationStatusEnum;
import com.course.server.enums.rdms.DocTypeEnum;
import com.course.server.exception.BusinessException;
import com.course.server.exception.BusinessExceptionCode;
import com.course.server.service.rdms.ApplicationBudgetExeWriter;
import com.course.server.service.rdms.RdmsFeeManageService;
import com.course.server.service.rdms.RdmsFeeProcessService;
import com.course.server.service.rdms.RdmsManhourService;
import com.course.server.service.rdms.flow.FlowBizCode;
import com.course.server.service.rdms.flow.FlowBudgetBinding;
import com.course.server.service.rdms.flow.FlowContext;
import com.course.server.service.rdms.flow.FlowDomainBinding;
import com.course.server.util.CopyUtil;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * 费用申请的领域绑定。实施方案第 5 节试点。
 * <p>
 * 本类是领域动作落到费用业务表的唯一出口。状态判定、过程记录与预算流水的写入口径
 * 与 {@code FeeController} 现有实现一致，只是改由节点参数驱动，不再由控制器内的
 * 条件分支决定。
 * <p>
 * 业务主键取申请单号 code，与 {@code rdms_flow_biz_ref.biz_id} 对应。
 */
@Component
public class FeeFlowDomainBinding implements FlowDomainBinding, FlowBudgetBinding {

    /** 费用申请的八个状态，发布校验据此判定 guard 与 WRITE_STATUS 的取值。 */
    private static final Set<String> VALID_STATUSES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            ApplicationStatusEnum.APPLICATION.getStatus(),
            ApplicationStatusEnum.APPROVED.getStatus(),
            ApplicationStatusEnum.REJECT.getStatus(),
            ApplicationStatusEnum.SUBMIT.getStatus(),
            ApplicationStatusEnum.SUPPLEMENT.getStatus(),
            ApplicationStatusEnum.CANCEL.getStatus(),
            ApplicationStatusEnum.PRE_COMPLETE.getStatus(),
            ApplicationStatusEnum.COMPLETE.getStatus())));

    @Resource
    private RdmsFeeManageService rdmsFeeManageService;

    @Resource
    private RdmsFeeProcessService rdmsFeeProcessService;

    @Resource
    private ApplicationBudgetExeWriter applicationBudgetExeWriter;

    @Resource
    private RdmsManhourService rdmsManhourService;

    @Override
    public String bizCode() {
        return FlowBizCode.FEE_APPLY.getCode();
    }

    @Override
    public Set<String> validStatuses() {
        return VALID_STATUSES;
    }

    @Override
    public String currentStatus(FlowContext context) {
        return require(context).getStatus();
    }

    /**
     * 写状态。来源与目标的配对关系与现有审批入口一致，不接受节点上配出的任意跃迁。
     * 不调用 {@code LifecycleTransition.allows}：该方法对同态直接放行，而费用除
     * 提交复核可重复提交外，不接受同态重入。
     */
    @Override
    public void writeStatus(FlowContext context, String targetStatus) {
        RdmsFeeManageDto current = require(context);
        assertTransition(current.getStatus(), targetStatus);

        RdmsFeeManage update = new RdmsFeeManage();
        update.setId(current.getId());
        update.setStatus(targetStatus);
        update.setApproveTime(new Date());
        if (ApplicationStatusEnum.feeApproveConclusions().contains(targetStatus)
                || ApplicationStatusEnum.PRE_COMPLETE.getStatus().equals(targetStatus)) {
            // 审批与复核通过以库内实际费用记账，没有实际费用时沿用申请金额；退回增补保留原记账金额，复核通过时再校验增量
            BigDecimal booked = ObjectUtils.isEmpty(current.getActualCost())
                    ? current.getAppliedAmount() : current.getActualCost();
            update.setAccountCost(booked);
            current.setAccountCost(booked);
        }
        rdmsFeeManageService.updateByPrimaryKeySelective(update);
        current.setStatus(targetStatus);
    }

    @Override
    public void writeProcess(FlowContext context, String text) {
        RdmsFeeManageDto current = require(context);
        RdmsFeeProcess process = new RdmsFeeProcess();
        process.setFeeId(current.getId());
        process.setExecutorId(context.getOperatorId());
        long existing = rdmsFeeProcessService.getFeeProcessCount(current.getId());
        // 首条与原保存接口的固定深度 1 对齐，其后仍按已有条数计
        process.setDeep(existing == 0 ? 1 : (int) existing);
        process.setDescription(text);
        String nextNode = current.getNextNode();
        if (ApplicationStatusEnum.CANCEL.getStatus().equals(current.getStatus())
                || ApplicationStatusEnum.COMPLETE.getStatus().equals(current.getStatus())) {
            nextNode = null;
        }
        process.setNextNode(nextNode);
        process.setSubmitTime(new Date());
        process.setStatus(current.getStatus());
        rdmsFeeProcessService.save(process);
    }

    @Override
    public void writeNextNode(FlowContext context, String assigneeId) {
        RdmsFeeManageDto current = require(context);
        RdmsFeeManage update = new RdmsFeeManage();
        update.setId(current.getId());
        update.setNextNode(assigneeId);
        rdmsFeeManageService.updateByPrimaryKeySelective(update);
        current.setNextNode(assigneeId);
    }

    @Override
    public void writeBudgetExe(FlowContext context, String mode) {
        RdmsFeeManageDto current = require(context);
        RdmsFeeManage feeManage = CopyUtil.copy(current, RdmsFeeManage.class);
        RdmsFeeManageDto exeDto = CopyUtil.copy(feeManage, RdmsFeeManageDto.class);
        // 记账口径由 mode 决定：PAID 取核定金额，UNPAID 取申请金额，
        // 与 ApplicationBudgetExeWriter 内按 PRE_COMPLETE 判定的口径等价
        exeDto.setApproveStatus(MODE_PAID.equals(mode)
                ? ApplicationStatusEnum.PRE_COMPLETE.getStatus()
                : ApplicationStatusEnum.APPROVED.getStatus());
        applicationBudgetExeWriter.writeFeeApprove(exeDto, feeManage);
    }

    @Override
    public void revokeBudgetExe(FlowContext context) {
        applicationBudgetExeWriter.removeFeeUnpaidExe(require(context).getCode());
    }

    /**
     * 校验预算余额。口径与审批页显示一致：挂特性的取特性其他费用余额，否则取子项目
     * 可用额合计减执行流水（已执行与进行中）；预研、产品级与缺陷费用没有对应的余额方法，不做拦截。
     */
    @Override
    public void checkBudgetBalance(FlowContext context, String amountField, String denyCode) {
        RdmsFeeManageDto fee = require(context);
        BigDecimal increment = amount(fee, amountField);
        if (ObjectUtils.isEmpty(increment) || increment.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        if (!DocTypeEnum.SUB_PROJECT.getType().equals(fee.getAuxStatus())
                || !ObjectUtils.isEmpty(fee.getQualityId())) {
            return;
        }
        BigDecimal remain;
        if (!ObjectUtils.isEmpty(fee.getCharacterId())) {
            remain = rdmsManhourService.getCharacterBudgetExeSummary(fee.getCharacterId()).getOtherFeeRemain();
        } else if (!ObjectUtils.isEmpty(fee.getSubprojectId())) {
            remain = rdmsManhourService.getSubprojectSelfRemainBudget(fee.getSubprojectId());
        } else {
            return;
        }
        if (!ObjectUtils.isEmpty(remain) && increment.compareTo(remain) > 0) {
            throw new BusinessException(denyCode(denyCode));
        }
    }

    /**
     * 复核时只校验核定金额相对已记账金额的增量，SUBMIT 单据已按原记账金额计入余额。
     */
    private BigDecimal amount(RdmsFeeManageDto fee, String amountField) {
        if ("actualCost".equals(amountField)) {
            if (ObjectUtils.isEmpty(fee.getActualCost())) {
                return BigDecimal.ZERO;
            }
            BigDecimal booked = ObjectUtils.isEmpty(fee.getAccountCost())
                    ? BigDecimal.ZERO : fee.getAccountCost();
            return fee.getActualCost().subtract(booked);
        }
        if ("appliedAmount".equals(amountField)) {
            return fee.getAppliedAmount();
        }
        throw new BusinessException(BusinessExceptionCode.FLOW_NODE_CONFIG_INVALID,
                "费用申请不认识金额字段 " + amountField);
    }

    private void assertTransition(String source, String target) {
        boolean allowed;
        if (ApplicationStatusEnum.APPLICATION.getStatus().equals(target)) {
            // 首次提交时单据还没有状态。不使用 LifecycleTransition.allows，避免同态被放行
            allowed = !StringUtils.hasText(source);
        } else if (ApplicationStatusEnum.feeApproveConclusions().contains(target)) {
            allowed = ApplicationStatusEnum.APPLICATION.getStatus().equals(source);
        } else if (ApplicationStatusEnum.feeRecheckConclusions().contains(target)) {
            allowed = ApplicationStatusEnum.SUBMIT.getStatus().equals(source);
        } else if (ApplicationStatusEnum.SUBMIT.getStatus().equals(target)) {
            allowed = ApplicationStatusEnum.feeSubmitSources().contains(source);
        } else if (ApplicationStatusEnum.CANCEL.getStatus().equals(target)) {
            allowed = ApplicationStatusEnum.feeCancelSources().contains(source);
        } else if (ApplicationStatusEnum.COMPLETE.getStatus().equals(target)) {
            allowed = ApplicationStatusEnum.feeCompleteSources().contains(source);
        } else {
            throw new BusinessException(BusinessExceptionCode.STATUS_VALUE_INVALID,
                    "费用申请不接受目标状态 " + target);
        }
        if (!allowed) {
            throw new BusinessException(BusinessExceptionCode.LIFECYCLE_TRANSITION_FORBIDDEN,
                    "费用申请不可由 " + source + " 转为 " + target);
        }
    }

    /**
     * 取当前单据。优先用上下文装入的实体，避免同一次推进内重复查库；
     * 未装入时按业务主键查，查不到即拒绝。
     */
    private RdmsFeeManageDto require(FlowContext context) {
        if (context.getBizEntity() instanceof RdmsFeeManageDto) {
            return (RdmsFeeManageDto) context.getBizEntity();
        }
        RdmsFeeManageDto fee = rdmsFeeManageService.getFeeManageSimpleInfoByCode(context.getBizId());
        if (fee == null) {
            throw new BusinessException(BusinessExceptionCode.FLOW_INSTANCE_NOT_FOUND,
                    "找不到申请单 " + context.getBizId());
        }
        context.setBizEntity(fee);
        return fee;
    }

    private BusinessExceptionCode denyCode(String configured) {
        if (!StringUtils.hasText(configured)) {
            return BusinessExceptionCode.FEE_BUDGET_OVERFLOW;
        }
        try {
            return BusinessExceptionCode.valueOf(configured);
        } catch (IllegalArgumentException e) {
            return BusinessExceptionCode.FEE_BUDGET_OVERFLOW;
        }
    }
}
