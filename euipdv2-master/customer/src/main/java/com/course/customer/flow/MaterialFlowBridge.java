/**
 * Author：周朔鹏
 * 编制时间：2026-10-06
 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */

package com.course.customer.flow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.course.server.domain.RdmsJobItem;
import com.course.server.dto.rdms.RdmsMaterialManageDto;
import com.course.server.enums.rdms.ApplicationStatusEnum;
import com.course.server.mapper.RdmsFlowBizRefMapper;
import com.course.server.security.tenant.TenantContext;
import com.course.server.service.rdms.RdmsJobItemService;
import com.course.server.service.rdms.RdmsMaterialManageService;
import com.course.server.service.rdms.flow.FlowBizCode;
import com.course.server.service.rdms.flow.FlowContext;
import com.course.server.service.rdms.flow.MaterialFlowPort;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

/**
 * 领料申请与流程运行时的衔接。退库单不进入本类。
 * 库存变动仍由控制器在调用本类之前完成。
 */
@Primary
@Service
public class MaterialFlowBridge implements MaterialFlowPort {
    private static final Logger LOG = LoggerFactory.getLogger(MaterialFlowBridge.class);


    static final String NODE_REJECTED = "rejected";
    static final String NODE_END_COMPLETE = "endComplete";
    static final String NODE_ISSUE = "issue";
    static final String NODE_SUBMIT = "submit";
    static final String VAR_REJECT_ACTION = "rejectAction";
    static final String VAR_ISSUE_RESULT = "issueResult";

    @Resource
    private FlowSwitch flowSwitch;

    @Resource
    private FlowRuntimeService flowRuntimeService;

    @Resource
    private RdmsFlowBizRefMapper rdmsFlowBizRefMapper;

    @Resource
    private RdmsMaterialManageService rdmsMaterialManageService;

    @Resource
    private RdmsJobItemService rdmsJobItemService;

    public boolean engineForNew(String customerId) {
        return StringUtils.hasText(customerId)
                && flowSwitch.enabled(customerId, FlowBizCode.MATERIAL.getCode());
    }

    public boolean engineForExisting(String customerId, String code) {
        if (!StringUtils.hasText(customerId) || !StringUtils.hasText(code)) {
            return false;
        }
        return rdmsFlowBizRefMapper.selectByBiz(customerId, FlowBizCode.MATERIAL.getCode(), code) != null;
    }

    @Transactional(rollbackFor = Exception.class)
    public void openApplication(String code, String writerId) {
        RdmsMaterialManageDto loaded = rdmsMaterialManageService.getMaterialApplicationByCode_origin(code);
        FlowContext context = context(loaded, writerId);
        flowRuntimeService.start(context);
        flowRuntimeService.complete(context);
    }

    @Transactional(rollbackFor = Exception.class)
    public void approve(String code, String operatorId, String approveResult) {
        RdmsMaterialManageDto loaded = rdmsMaterialManageService.getMaterialApplicationByCode_origin(code);
        FlowContext context = context(loaded, operatorId);
        context.putVariable("approveResult", approveResult);
        flowRuntimeService.complete(context);
    }

    /** 单据所用流程含发料确认节点时，库存在发料确认时扣减，审批时不扣。 */
    public boolean requiresIssue(String customerId, String code) {
        return engineForExisting(customerId, code)
                && flowRuntimeService.definitionHasNode(customerId, FlowBizCode.MATERIAL.getCode(), code, NODE_ISSUE);
    }

    /** proxy 为真表示物料管理员以外的最终管理人代办，调用方已校验身份 */
    @Transactional(rollbackFor = Exception.class)
    public void issue(String code, String operatorId, String issueResult, String description, boolean proxy) {
        RdmsMaterialManageDto loaded = rdmsMaterialManageService.getMaterialApplicationByCode_origin(code);
        FlowContext context = context(loaded, operatorId);
        context.setNodeId(NODE_ISSUE);
        context.putVariable(VAR_ISSUE_RESULT, issueResult);
        context.putVariable(FlowRuntimeService.VAR_PROCESS_TEXT, description);
        takeOverIfProxy(context, operatorId, proxy);
        flowRuntimeService.complete(context);
    }

    /** proxy 为真表示最终管理人代填表人办理，调用方已校验身份 */
    @Transactional(rollbackFor = Exception.class)
    public void submit(String code, String operatorId, boolean proxy) {
        RdmsMaterialManageDto loaded = rdmsMaterialManageService.getMaterialApplicationByCode_origin(code);
        FlowContext context = context(loaded, operatorId);
        context.setNodeId(NODE_SUBMIT);
        takeOverIfProxy(context, operatorId, proxy);
        flowRuntimeService.complete(context);
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancel(String code, String operatorId, boolean proxy) {
        RdmsMaterialManageDto loaded = rdmsMaterialManageService.getMaterialApplicationByCode_origin(code);
        String operator = operatorId;
        if (!StringUtils.hasText(operator)) {
            operator = loaded.getWriterId();
        }
        FlowContext context = context(loaded, operator);
        context.putVariable(VAR_REJECT_ACTION, ApplicationStatusEnum.CANCEL.getStatus());
        takeOverIfProxy(context, operator, proxy);
        flowRuntimeService.complete(context);
    }

    /**
     * 被驳回的领料单修改后重新提交：明细与申请表已由控制器写回，此处推进“取消或修改重提”节点回到审批。
     * 早期定义上的实例没有该节点，只能取消后重新申请。
     */
    @Transactional(rollbackFor = Exception.class)
    public void resubmit(String code, String operatorId, boolean proxy) {
        RdmsMaterialManageDto loaded = rdmsMaterialManageService.getMaterialApplicationByCode_origin(code);
        FlowContext context = context(loaded, operatorId);
        context.setNodeId(NODE_REJECTED);
        context.putVariable(VAR_REJECT_ACTION, ApplicationStatusEnum.APPLICATION.getStatus());
        takeOverIfProxy(context, operatorId, proxy);
        flowRuntimeService.complete(context);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finishFullyReturned(String customerId, String code) {
        if (!engineForExisting(customerId, code)) {
            return;
        }
        // 自定义领料流程缺完成出口时不收尾实例，避免退料审批整笔回滚
        if (!flowRuntimeService.definitionHasNode(customerId, FlowBizCode.MATERIAL.getCode(), code, NODE_END_COMPLETE)) {
            LOG.warn("领料单 {} 的流程定义无节点 {}，全部退回后实例未收尾", code, NODE_END_COMPLETE);
            return;
        }
        RdmsMaterialManageDto loaded = rdmsMaterialManageService.getMaterialApplicationByCode_origin(code);
        FlowContext context = context(loaded, loaded.getWriterId());
        context.putVariable(FlowRuntimeService.VAR_PROCESS_TEXT, "物料已全部退回，领料单自动完成");
        flowRuntimeService.finishAt(context, NODE_END_COMPLETE);
    }

    private void takeOverIfProxy(FlowContext context, String operatorId, boolean proxy) {
        if (proxy) {
            flowRuntimeService.reassignCurrent(context, operatorId);
        }
    }

    private FlowContext context(RdmsMaterialManageDto material, String operatorId) {
        FlowContext context = new FlowContext();
        String customerId = TenantContext.getTenantId();
        if (!StringUtils.hasText(customerId)) {
            customerId = material.getCustomerId();
        }
        context.setCustomerId(customerId);
        context.setBizCode(FlowBizCode.MATERIAL.getCode());
        context.setBizId(material.getCode());
        context.setOperatorId(operatorId);
        context.setInitiatorId(material.getWriterId());
        context.setBizEntity(material);
        if (StringUtils.hasText(material.getJobitemId())) {
            RdmsJobItem job = rdmsJobItemService.selectByPrimaryKey(material.getJobitemId());
            context.setJobItem(job);
        }
        return context;
    }
}
