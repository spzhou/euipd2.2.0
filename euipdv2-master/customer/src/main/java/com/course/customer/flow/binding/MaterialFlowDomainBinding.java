/**
 * Author：周朔鹏
 * 编制时间：2026-10-06
 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */

package com.course.customer.flow.binding;

import com.course.customer.flow.FlowRuntimeService;
import com.course.server.domain.RdmsMaterialManage;
import com.course.server.domain.RdmsMaterialProcess;
import com.course.server.dto.rdms.RdmsMaterialManageDto;
import com.course.server.enums.rdms.ApplicationStatusEnum;
import com.course.server.exception.BusinessException;
import com.course.server.exception.BusinessExceptionCode;
import com.course.server.service.rdms.ApplicationBudgetExeWriter;
import com.course.server.service.rdms.RdmsMaterialManageService;
import com.course.server.service.rdms.RdmsMaterialProcessService;
import com.course.server.service.rdms.flow.FlowBizCode;
import com.course.server.service.rdms.flow.FlowBudgetBinding;
import com.course.server.service.rdms.flow.FlowContext;
import com.course.server.service.rdms.flow.FlowDomainBinding;
import com.course.server.util.CopyUtil;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * 领料申请的领域绑定。退料单见 {@link MaterialReturnFlowDomainBinding}。
 * 定义含发料确认节点时由 MATERIAL_ISSUE 动作扣库存，否则由控制器在审批时调用库存服务。借用在记账实现内直接返回。
 * 预算余额没有单独的拦截方法，库存不足仍返回 MATERIAL_STOCK_SHORT。
 */
@Component
public class MaterialFlowDomainBinding implements FlowDomainBinding, FlowBudgetBinding {

    static final Set<String> VALID_STATUSES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            ApplicationStatusEnum.APPLICATION.getStatus(),
            ApplicationStatusEnum.PRE_APPROVED.getStatus(),
            ApplicationStatusEnum.APPROVED.getStatus(),
            ApplicationStatusEnum.REJECT.getStatus(),
            ApplicationStatusEnum.SUBMIT.getStatus(),
            ApplicationStatusEnum.SUPPLEMENT.getStatus(),
            ApplicationStatusEnum.CANCEL.getStatus(),
            ApplicationStatusEnum.COMPLETE.getStatus())));

    @Resource
    private RdmsMaterialManageService rdmsMaterialManageService;

    @Resource
    private RdmsMaterialProcessService rdmsMaterialProcessService;

    @Resource
    private ApplicationBudgetExeWriter applicationBudgetExeWriter;

    @Override
    public String bizCode() {
        return FlowBizCode.MATERIAL.getCode();
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
        RdmsMaterialManageDto current = require(context);
        assertTransition(current.getStatus(), targetStatus);
        RdmsMaterialManage update = new RdmsMaterialManage();
        update.setId(current.getId());
        update.setStatus(targetStatus);
        rdmsMaterialManageService.updateByPrimaryKeySelective(update);
        current.setStatus(targetStatus);
    }

    @Override
    public void writeProcess(FlowContext context, String text) {
        RdmsMaterialManageDto current = require(context);
        RdmsMaterialProcess process = new RdmsMaterialProcess();
        process.setMaterialId(current.getId());
        process.setExecutorId(context.getOperatorId());
        long existing = rdmsMaterialProcessService.getMaterialProcessCount(current.getId());
        process.setDeep(existing == 0 ? 1 : (int) existing);
        Object note = context.getVariables().get(FlowRuntimeService.VAR_PROCESS_TEXT);
        process.setDescription(note != null && StringUtils.hasText(String.valueOf(note))
                ? text + "：" + note : text);
        String nextNode = current.getNextNode();
        if (ApplicationStatusEnum.CANCEL.getStatus().equals(current.getStatus())
                || ApplicationStatusEnum.COMPLETE.getStatus().equals(current.getStatus())) {
            nextNode = null;
        }
        process.setNextNode(nextNode);
        process.setSubmitTime(new Date());
        process.setStatus(current.getStatus());
        rdmsMaterialProcessService.save(process);
    }

    @Override
    public void writeNextNode(FlowContext context, String assigneeId) {
        RdmsMaterialManageDto current = require(context);
        RdmsMaterialManage update = new RdmsMaterialManage();
        update.setId(current.getId());
        update.setNextNode(assigneeId);
        rdmsMaterialManageService.updateByPrimaryKeySelective(update);
        current.setNextNode(assigneeId);
    }

    @Override
    public void writeBudgetExe(FlowContext context, String mode) {
        RdmsMaterialManageDto current = require(context);
        RdmsMaterialManage entity = CopyUtil.copy(current, RdmsMaterialManage.class);
        RdmsMaterialManageDto exe = CopyUtil.copy(entity, RdmsMaterialManageDto.class);
        exe.setJobType(current.getJobType());
        exe.setApproveStatus(MODE_PAID.equals(mode)
                ? ApplicationStatusEnum.COMPLETE.getStatus()
                : ApplicationStatusEnum.APPROVED.getStatus());
        applicationBudgetExeWriter.writeMaterialApprove(exe, entity, false);
    }

    @Override
    public void revokeBudgetExe(FlowContext context) {
        applicationBudgetExeWriter.removeMaterialUnpaidExe(require(context).getCode());
    }

    @Override
    public void checkBudgetBalance(FlowContext context, String amountField, String denyCode) {
        // 领料的数量约束在库存服务，超出返回 MATERIAL_STOCK_SHORT。这里不再做第二套余额拦截。
    }

    private RdmsMaterialManageDto require(FlowContext context) {
        Object entity = context.getBizEntity();
        if (!(entity instanceof RdmsMaterialManageDto) || !StringUtils.hasText(((RdmsMaterialManageDto) entity).getId())) {
            throw new BusinessException(BusinessExceptionCode.FLOW_INSTANCE_NOT_FOUND, "物料申请不在流程上下文中");
        }
        return (RdmsMaterialManageDto) entity;
    }

    static void assertTransition(String source, String target) {
        if (!StringUtils.hasText(source)) {
            if (ApplicationStatusEnum.APPLICATION.getStatus().equals(target)) {
                return;
            }
            throw forbidden(source, target);
        }
        if (ApplicationStatusEnum.APPLICATION.getStatus().equals(source)
                && ApplicationStatusEnum.materialApplicationConclusions().contains(target)) {
            return;
        }
        if ((ApplicationStatusEnum.APPROVED.getStatus().equals(source)
                || ApplicationStatusEnum.PRE_APPROVED.getStatus().equals(source)
                || ApplicationStatusEnum.SUPPLEMENT.getStatus().equals(source))
                && ApplicationStatusEnum.SUBMIT.getStatus().equals(target)) {
            return;
        }
        // 发料确认退回、退料收料驳回
        if (ApplicationStatusEnum.materialIssueSources().contains(source)
                && ApplicationStatusEnum.REJECT.getStatus().equals(target)) {
            return;
        }
        if (ApplicationStatusEnum.SUBMIT.getStatus().equals(source)
                && ApplicationStatusEnum.materialSubmitConclusions().contains(target)) {
            return;
        }
        if (ApplicationStatusEnum.REJECT.getStatus().equals(source)
                && (ApplicationStatusEnum.CANCEL.getStatus().equals(target)
                || ApplicationStatusEnum.APPLICATION.getStatus().equals(target))) {
            return;
        }
        throw forbidden(source, target);
    }

    private static BusinessException forbidden(String source, String target) {
        return new BusinessException(BusinessExceptionCode.LIFECYCLE_TRANSITION_FORBIDDEN,
                "物料申请不能从 " + source + " 改为 " + target);
    }
}
