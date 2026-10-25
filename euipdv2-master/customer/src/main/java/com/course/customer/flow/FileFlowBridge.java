/**
 * Author：周朔鹏
 * 编制时间：2026-10-06
 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */

package com.course.customer.flow;

import com.course.server.domain.RdmsFileApply;
import com.course.server.dto.rdms.RdmsFileApplyDto;
import com.course.server.mapper.RdmsFlowBizRefMapper;
import com.course.server.security.tenant.TenantContext;
import com.course.server.service.rdms.RdmsFileApplyService;
import com.course.server.service.rdms.RdmsFileService;
import com.course.server.service.rdms.flow.FlowBizCode;
import com.course.server.service.rdms.flow.FlowContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

/**
 * 档案申请的保存、同意、驳回与流程运行时的衔接。
 * 新申请按开关决定是否启动引擎。已有实例的申请，同意与驳回继续走引擎。
 */
@Service
public class FileFlowBridge {

    public static final String LEGACY = "FLOW_LEGACY";

    @Resource
    private FlowSwitch flowSwitch;

    @Resource
    private FlowRuntimeService flowRuntimeService;

    @Resource
    private RdmsFlowBizRefMapper rdmsFlowBizRefMapper;

    @Resource
    private RdmsFileApplyService rdmsFileApplyService;

    @Resource
    private RdmsFileService rdmsFileService;

    public boolean engineForNew(String customerId) {
        return StringUtils.hasText(customerId)
                && flowSwitch.enabled(customerId, FlowBizCode.FILE_APPLY.getCode());
    }

    public boolean engineForExisting(String customerId, String bizId) {
        if (!StringUtils.hasText(customerId) || !StringUtils.hasText(bizId)) {
            return false;
        }
        return rdmsFlowBizRefMapper.selectByBiz(customerId, FlowBizCode.FILE_APPLY.getCode(), bizId) != null;
    }

    @Transactional(rollbackFor = Exception.class)
    public void submit(RdmsFileApplyDto dto) {
        rdmsFileApplyService.prepareNewApplication(dto);
        dto.setApplicationStatus(null);
        rdmsFileApplyService.save(dto);
        RdmsFileApply saved = rdmsFileApplyService.selectByPrimaryKey(dto.getId());
        FlowContext context = context(saved, saved.getProposerId());
        flowRuntimeService.start(context);
        flowRuntimeService.complete(context);
    }

    /** 操作人须已经 RdmsFileApplyService.requirePendingApply 校验，最终管理人代办时先改派给操作人。 */
    @Transactional(rollbackFor = Exception.class)
    public void agree(RdmsFileApply current, String operatorId) {
        decide(current, operatorId, "APPROVED");
    }

    @Transactional(rollbackFor = Exception.class)
    public void reject(RdmsFileApply current, String operatorId) {
        decide(current, operatorId, "REJECT");
    }

    private void decide(RdmsFileApply current, String operatorId, String approveResult) {
        FlowContext context = context(current, operatorId);
        context.putVariable("approveResult", approveResult);
        flowRuntimeService.reassignCurrent(context, operatorId);
        flowRuntimeService.complete(context);
    }

    private FlowContext context(RdmsFileApply apply, String operatorId) {
        FlowContext context = new FlowContext();
        String customerId = TenantContext.getTenantId();
        if (!StringUtils.hasText(customerId)) {
            customerId = apply.getCustomerId();
        }
        context.setCustomerId(customerId);
        context.setBizCode(FlowBizCode.FILE_APPLY.getCode());
        context.setBizId(apply.getId());
        context.setOperatorId(operatorId);
        context.setInitiatorId(apply.getProposerId());
        context.setBizEntity(apply);
        return context;
    }
}
