/**
 * Author：周朔鹏
 * 编制时间：2026-10-09
 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */

package com.course.customer.flow;

import com.course.server.domain.RdmsJobItem;
import com.course.server.dto.rdms.RdmsMaterialManageDto;
import com.course.server.mapper.RdmsFlowBizRefMapper;
import com.course.server.security.tenant.TenantContext;
import com.course.server.service.rdms.RdmsJobItemService;
import com.course.server.service.rdms.RdmsMaterialManageService;
import com.course.server.service.rdms.flow.FlowBizCode;
import com.course.server.service.rdms.flow.FlowContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

/**
 * 退料单与流程运行时的衔接。开关命中时新退料单走 materialReturn 流程；
 * 没有流程实例的退料单（开关关闭时新建或发布前已有）仍走控制器原方法，审批通过即入库。
 */
@Service
public class MaterialReturnFlowBridge {

    static final String NODE_RECEIVE = "receive";
    static final String NODE_SUBMIT = "submit";
    static final String NODE_REJECTED = "rejected";
    static final String VAR_RECEIVE_RESULT = "receiveResult";

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
                && flowSwitch.enabled(customerId, FlowBizCode.MATERIAL_RETURN.getCode());
    }

    public boolean engineForExisting(String customerId, String code) {
        if (!StringUtils.hasText(customerId) || !StringUtils.hasText(code)) {
            return false;
        }
        return rdmsFlowBizRefMapper.selectByBiz(customerId, FlowBizCode.MATERIAL_RETURN.getCode(), code) != null;
    }

    @Transactional(rollbackFor = Exception.class)
    public void open(String code, String writerId) {
        FlowContext context = context(load(code), writerId);
        flowRuntimeService.start(context);
        flowRuntimeService.complete(context);
    }

    /** 审批与复核都推进当前节点，结论放在 approveResult */
    @Transactional(rollbackFor = Exception.class)
    public void approve(String code, String operatorId, String approveResult) {
        FlowContext context = context(load(code), operatorId);
        context.putVariable("approveResult", approveResult);
        flowRuntimeService.complete(context);
    }

    /** proxy 为真表示物料管理员以外的最终管理人代办，调用方已校验身份 */
    @Transactional(rollbackFor = Exception.class)
    public void receive(String code, String operatorId, String receiveResult, String description, boolean proxy) {
        FlowContext context = context(load(code), operatorId);
        context.setNodeId(NODE_RECEIVE);
        context.putVariable(VAR_RECEIVE_RESULT, receiveResult);
        context.putVariable(FlowRuntimeService.VAR_PROCESS_TEXT, description);
        takeOverIfProxy(context, operatorId, proxy);
        flowRuntimeService.complete(context);
    }

    /** proxy 为真表示最终管理人代填表人办理，调用方已校验身份 */
    @Transactional(rollbackFor = Exception.class)
    public void submit(String code, String operatorId, boolean proxy) {
        FlowContext context = context(load(code), operatorId);
        context.setNodeId(NODE_SUBMIT);
        takeOverIfProxy(context, operatorId, proxy);
        flowRuntimeService.complete(context);
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancel(String code, String operatorId, boolean proxy) {
        FlowContext context = context(load(code), operatorId);
        context.setNodeId(NODE_REJECTED);
        takeOverIfProxy(context, operatorId, proxy);
        flowRuntimeService.complete(context);
    }

    private RdmsMaterialManageDto load(String code) {
        return rdmsMaterialManageService.getMaterialApplicationByCode_return(code);
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
        context.setBizCode(FlowBizCode.MATERIAL_RETURN.getCode());
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
