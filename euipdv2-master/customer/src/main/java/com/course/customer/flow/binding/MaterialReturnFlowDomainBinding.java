/**
 * Author：周朔鹏
 * 编制时间：2026-10-09
 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */

package com.course.customer.flow.binding;

import com.course.customer.flow.FlowRuntimeService;
import com.course.server.domain.RdmsMaterialManage;
import com.course.server.domain.RdmsMaterialManageReturn;
import com.course.server.domain.RdmsMaterialProcessReturn;
import com.course.server.dto.rdms.RdmsMaterialManageDto;
import com.course.server.enums.rdms.ApplicationStatusEnum;
import com.course.server.enums.rdms.MaterialOpeStatusEnum;
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
import java.util.Date;
import java.util.Set;

/**
 * 退料单的领域绑定。状态取值与转移同领料单；写状态时同步退料操作状态：
 * 批准为 RETURN_APPROVED，驳回与结束为 RETURN_COMPLETE。执行流水按退料金额取反记账，借用不记账。
 */
@Component
public class MaterialReturnFlowDomainBinding implements FlowDomainBinding, FlowBudgetBinding {

    @Resource
    private RdmsMaterialManageService rdmsMaterialManageService;

    @Resource
    private RdmsMaterialProcessService rdmsMaterialProcessService;

    @Resource
    private ApplicationBudgetExeWriter applicationBudgetExeWriter;

    @Override
    public String bizCode() {
        return FlowBizCode.MATERIAL_RETURN.getCode();
    }

    @Override
    public Set<String> validStatuses() {
        return MaterialFlowDomainBinding.VALID_STATUSES;
    }

    @Override
    public String currentStatus(FlowContext context) {
        return require(context).getStatus();
    }

    @Override
    public void writeStatus(FlowContext context, String targetStatus) {
        RdmsMaterialManageDto current = require(context);
        MaterialFlowDomainBinding.assertTransition(current.getStatus(), targetStatus);
        RdmsMaterialManageReturn update = new RdmsMaterialManageReturn();
        update.setId(current.getId());
        update.setStatus(targetStatus);
        String opeStatus = opeStatusFor(targetStatus);
        if (opeStatus != null) {
            update.setOpeStatus(opeStatus);
            current.setOpeStatus(opeStatus);
        }
        rdmsMaterialManageService.updateByPrimaryKeySelective_return(update);
        current.setStatus(targetStatus);
    }

    public static String opeStatusFor(String targetStatus) {
        if (ApplicationStatusEnum.APPROVED.getStatus().equals(targetStatus)
                || ApplicationStatusEnum.PRE_APPROVED.getStatus().equals(targetStatus)) {
            return MaterialOpeStatusEnum.RETURN_APPROVED.getStatus();
        }
        if (ApplicationStatusEnum.REJECT.getStatus().equals(targetStatus)
                || ApplicationStatusEnum.COMPLETE.getStatus().equals(targetStatus)) {
            return MaterialOpeStatusEnum.RETURN_COMPLETE.getStatus();
        }
        return null;
    }

    @Override
    public void writeProcess(FlowContext context, String text) {
        RdmsMaterialManageDto current = require(context);
        RdmsMaterialProcessReturn process = new RdmsMaterialProcessReturn();
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
        rdmsMaterialProcessService.save_return(process);
    }

    @Override
    public void writeNextNode(FlowContext context, String assigneeId) {
        RdmsMaterialManageDto current = require(context);
        RdmsMaterialManageReturn update = new RdmsMaterialManageReturn();
        update.setId(current.getId());
        update.setNextNode(assigneeId);
        rdmsMaterialManageService.updateByPrimaryKeySelective_return(update);
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
        applicationBudgetExeWriter.writeMaterialApprove(exe, entity, true);
    }

    @Override
    public void revokeBudgetExe(FlowContext context) {
        applicationBudgetExeWriter.removeMaterialUnpaidExe(require(context).getCode());
    }

    @Override
    public void checkBudgetBalance(FlowContext context, String amountField, String denyCode) {
        // 退料只减少成本，不做余额拦截。
    }

    private RdmsMaterialManageDto require(FlowContext context) {
        Object entity = context.getBizEntity();
        if (!(entity instanceof RdmsMaterialManageDto) || !StringUtils.hasText(((RdmsMaterialManageDto) entity).getId())) {
            throw new BusinessException(BusinessExceptionCode.FLOW_INSTANCE_NOT_FOUND, "退料单不在流程上下文中");
        }
        return (RdmsMaterialManageDto) entity;
    }
}
