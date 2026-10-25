/**
 * Author：周朔鹏
 * 编制时间：2026-10-06
 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */

package com.course.customer.flow;

import com.course.server.domain.RdmsFeeManage;
import com.course.server.domain.RdmsJobItem;
import com.course.server.dto.rdms.RdmsFeeManageDto;
import com.course.server.mapper.RdmsFlowBizRefMapper;
import com.course.server.security.tenant.TenantContext;
import com.course.server.service.rdms.RdmsFeeManageService;
import com.course.server.service.rdms.RdmsJobItemService;
import com.course.server.service.rdms.flow.FlowBizCode;
import com.course.server.service.rdms.flow.FlowContext;
import com.course.server.util.CopyUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Date;

/**
 * 费用申请五个流转入口与流程运行时之间的衔接。
 * <p>
 * 新单据是否走引擎由开关决定。已经挂上流程实例的单据，后续审批、提交、取消、
 * 归档仍走引擎，即使开关随后关闭。开关打开之前已经在途的单据没有实例，继续走原代码。
 * 本类不判定审批人，审批人由 {@code FeeApproverClassifier} 在进入本类之前写好。
 */
@Service
public class FeeFlowBridge {

    @Resource
    private FlowSwitch flowSwitch;

    @Resource
    private FlowRuntimeService flowRuntimeService;

    @Resource
    private RdmsFlowBizRefMapper rdmsFlowBizRefMapper;

    @Resource
    private RdmsFeeManageService rdmsFeeManageService;

    @Resource
    private RdmsJobItemService rdmsJobItemService;

    public boolean engineForNew(String customerId) {
        return StringUtils.hasText(customerId)
                && flowSwitch.enabled(customerId, FlowBizCode.FEE_APPLY.getCode());
    }

    public boolean engineForExisting(String customerId, String bizId) {
        if (!StringUtils.hasText(customerId) || !StringUtils.hasText(bizId)) {
            return false;
        }
        return rdmsFlowBizRefMapper.selectByBiz(customerId, FlowBizCode.FEE_APPLY.getCode(), bizId) != null;
    }

    /**
     * 保存申请并在同一事务内走完“提交申请”节点，使单据直接停在费用审批。
     * 状态由节点动作写入，此处不预写 APPLICATION。
     */
    @Transactional(rollbackFor = Exception.class)
    public String submitApplication(RdmsFeeManageDto dto, RdmsJobItem jobItem) {
        RdmsFeeManage fee = CopyUtil.copy(dto, RdmsFeeManage.class);
        fee.setStatus(null);
        fee.setJobType(jobItem.getType());
        fee.setApproverId(dto.getApproverId());
        fee.setAccountCost(fee.getAppliedAmount());
        rdmsFeeManageService.save(fee);

        RdmsFeeManageDto loaded = rdmsFeeManageService.getFeeManageSimpleInfoByCode(dto.getCode());
        FlowContext context = context(loaded, jobItem, dto.getWriterId());
        flowRuntimeService.start(context);
        flowRuntimeService.complete(context);
        return fee.getId();
    }

    /** 审批与复核共用。结论放进流程变量 approveResult，由网关选择去向。 */
    @Transactional(rollbackFor = Exception.class)
    public void approve(RdmsFeeManageDto request, RdmsFeeManageDto current, String operatorId) {
        RdmsFeeManage patch = new RdmsFeeManage();
        patch.setId(current.getId());
        patch.setApproveDescription(request.getApproveDescription());
        patch.setCapitalSource(request.getCapitalSource());
        patch.setApproveTime(new Date());
        rdmsFeeManageService.updateByPrimaryKeySelective(patch);

        RdmsFeeManageDto fresh = rdmsFeeManageService.getFeeManageSimpleInfoByCode(current.getCode());
        FlowContext context = context(fresh, loadJob(fresh), operatorId);
        context.putVariable("approveResult", request.getApproveStatus());
        note(context, request.getApproveDescription());
        flowRuntimeService.complete(context);
    }

    /** 审批或报销说明记为本步意见，供流程图的办理记录显示；原过程记录仍按节点配置写。 */
    private void note(FlowContext context, String text) {
        if (StringUtils.hasText(text)) {
            context.putVariable(FlowRuntimeService.VAR_PROCESS_TEXT, text.trim());
        }
    }

    /** 处理并提交报销。实际费用与说明先落库，再由节点动作改状态。 */
    /** proxy 为真表示最终管理人代填表人办理，调用方已校验身份 */
    @Transactional(rollbackFor = Exception.class)
    public void submit(RdmsFeeManageDto request, RdmsFeeManageDto current, String operatorId, boolean proxy) {
        RdmsFeeManage patch = new RdmsFeeManage();
        patch.setId(current.getId());
        patch.setActualCost(request.getActualCost());
        patch.setSubmitDescription(request.getSubmitDescription());
        rdmsFeeManageService.updateByPrimaryKeySelective(patch);

        RdmsFeeManageDto fresh = rdmsFeeManageService.getFeeManageSimpleInfoByCode(current.getCode());
        FlowContext context = context(fresh, loadJob(fresh), operatorId);
        note(context, request.getSubmitDescription());
        completeAs(context, operatorId, proxy);
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancel(RdmsFeeManageDto current, String operatorId, boolean proxy) {
        completeAs(context(current, loadJob(current), operatorId), operatorId, proxy);
    }

    @Transactional(rollbackFor = Exception.class)
    public void archive(RdmsFeeManageDto current, String operatorId, boolean proxy) {
        completeAs(context(current, loadJob(current), operatorId), operatorId, proxy);
    }

    private void completeAs(FlowContext context, String operatorId, boolean proxy) {
        if (proxy) {
            flowRuntimeService.reassignCurrent(context, operatorId);
        }
        flowRuntimeService.complete(context);
    }

    private RdmsJobItem loadJob(RdmsFeeManageDto fee) {
        if (fee == null || !StringUtils.hasText(fee.getJobitemId())) {
            return null;
        }
        return rdmsJobItemService.selectByPrimaryKey(fee.getJobitemId());
    }

    private FlowContext context(RdmsFeeManageDto fee, RdmsJobItem jobItem, String operatorId) {
        FlowContext context = new FlowContext();
        String customerId = TenantContext.getTenantId();
        if (!StringUtils.hasText(customerId)) {
            customerId = fee.getCustomerId();
        }
        context.setCustomerId(customerId);
        context.setBizCode(FlowBizCode.FEE_APPLY.getCode());
        context.setBizId(fee.getCode());
        context.setOperatorId(operatorId);
        context.setInitiatorId(fee.getWriterId());
        context.setBizEntity(fee);
        context.setJobItem(jobItem);
        return context;
    }
}
