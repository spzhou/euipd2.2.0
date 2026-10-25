/**
 * Author：周朔鹏
 * 编制时间：2026-10-06
 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */

package com.course.customer.flow;

import com.course.server.domain.RdmsBugFeedback;
import com.course.server.domain.RdmsQuality;
import com.course.server.domain.RdmsFlowBizRef;
import com.course.server.mapper.RdmsFlowBizRefMapper;
import com.course.server.security.tenant.TenantContext;
import com.course.server.service.rdms.RdmsBugFeedbackService;
import com.course.server.service.rdms.RdmsQualityService;
import com.course.server.service.rdms.flow.FlowBizCode;
import com.course.server.service.rdms.flow.FlowContext;
import com.course.customer.flow.binding.QualityFlowDomainBinding;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

/**
 * 质量单与缺陷反馈单的状态写入与流程运行时的衔接。
 * 工单下发、评价仍走工单接口。分析节点在执行人确定后改派给该执行人。
 */
@Service
public class QualityFlowBridge {

    @Resource
    private FlowSwitch flowSwitch;

    @Resource
    private FlowRuntimeService flowRuntimeService;

    @Resource
    private RdmsFlowBizRefMapper rdmsFlowBizRefMapper;

    @Resource
    private TaskService taskService;

    @Resource
    private RdmsQualityService rdmsQualityService;

    @Resource
    private RdmsBugFeedbackService rdmsBugFeedbackService;

    public boolean engineForNew(String customerId) {
        return StringUtils.hasText(customerId)
                && flowSwitch.enabled(customerId, FlowBizCode.QUALITY.getCode());
    }

    public boolean engineForExisting(String customerId, String bizId) {
        if (!StringUtils.hasText(customerId) || !StringUtils.hasText(bizId)) {
            return false;
        }
        return rdmsFlowBizRefMapper.selectByBiz(customerId, FlowBizCode.QUALITY.getCode(), bizId) != null;
    }

    /** 当前待办节点。分析提交后若已离开分析节点，再次提交只回写状态，不再推进该节点。 */
    public String currentNode(String customerId, String bizId) {
        if (!StringUtils.hasText(customerId) || !StringUtils.hasText(bizId)) {
            return null;
        }
        RdmsFlowBizRef ref = rdmsFlowBizRefMapper.selectByBiz(customerId, FlowBizCode.QUALITY.getCode(), bizId);
        if (ref == null || !StringUtils.hasText(ref.getProcessInstanceId())) {
            return null;
        }
        Task task = taskService.createTaskQuery().processInstanceId(ref.getProcessInstanceId()).singleResult();
        return task == null ? null : task.getTaskDefinitionKey();
    }

    public static String qualityBizId(String id) {
        return QualityFlowDomainBinding.QUALITY_PREFIX + id;
    }

    public static String bugBizId(String id) {
        return QualityFlowDomainBinding.BUG_PREFIX + id;
    }

    @Transactional(rollbackFor = Exception.class)
    public void openSave(RdmsQuality quality) {
        FlowContext context = context(quality, quality.getWriterId(), qualityBizId(quality.getId()));
        flowRuntimeService.start(context);
        flowRuntimeService.complete(context);
    }

    @Transactional(rollbackFor = Exception.class)
    public void openSave(RdmsBugFeedback bug) {
        FlowContext context = context(bug, bug.getWriterId(), bugBizId(bug.getId()));
        flowRuntimeService.start(context);
        flowRuntimeService.complete(context);
    }

    /** 未经保存直接提交：同一事务内走完保存节点和提交节点。 */
    @Transactional(rollbackFor = Exception.class)
    public void submitNew(RdmsQuality quality) {
        FlowContext context = context(quality, quality.getWriterId(), qualityBizId(quality.getId()));
        flowRuntimeService.start(context);
        flowRuntimeService.complete(context);
        reloadQuality(context, quality.getId());
        flowRuntimeService.complete(context);
    }

    @Transactional(rollbackFor = Exception.class)
    public void submitNew(RdmsBugFeedback bug) {
        FlowContext context = context(bug, bug.getWriterId(), bugBizId(bug.getId()));
        flowRuntimeService.start(context);
        flowRuntimeService.complete(context);
        reloadBug(context, bug.getId());
        flowRuntimeService.complete(context);
    }

    @Transactional(rollbackFor = Exception.class)
    public void completeSubmit(RdmsQuality quality) {
        flowRuntimeService.complete(context(freshQuality(quality.getId()), quality.getWriterId(), qualityBizId(quality.getId())));
    }

    @Transactional(rollbackFor = Exception.class)
    public void completeSubmit(RdmsBugFeedback bug) {
        flowRuntimeService.complete(context(freshBug(bug.getId()), bug.getWriterId(), bugBizId(bug.getId())));
    }

    /** 分析、结案反馈与复核的操作人由调用方经 JobActorGuard 校验，可为最终管理人代办，推进前改派给操作人。 */
    @Transactional(rollbackFor = Exception.class)
    public void completeAnalysis(String id, boolean bug, String operatorId, String analysisResult) {
        Object fresh = bug ? freshBug(id) : freshQuality(id);
        String writerId = bug ? ((RdmsBugFeedback) fresh).getWriterId() : ((RdmsQuality) fresh).getWriterId();
        FlowContext context = context(fresh, operatorId, bug ? bugBizId(id) : qualityBizId(id));
        context.setInitiatorId(writerId);
        context.putVariable("analysisResult", analysisResult);
        flowRuntimeService.reassignCurrent(context, operatorId);
        flowRuntimeService.complete(context);
    }

    @Transactional(rollbackFor = Exception.class)
    public void completeFeedback(String id, boolean bug, String operatorId) {
        Object fresh = bug ? freshBug(id) : freshQuality(id);
        String writerId = bug ? ((RdmsBugFeedback) fresh).getWriterId() : ((RdmsQuality) fresh).getWriterId();
        FlowContext context = context(fresh, operatorId, bug ? bugBizId(id) : qualityBizId(id));
        context.setInitiatorId(writerId);
        flowRuntimeService.reassignCurrent(context, operatorId);
        flowRuntimeService.complete(context);
    }

    @Transactional(rollbackFor = Exception.class)
    public void completeReview(String id, boolean bug, String reviewResult) {
        Object fresh = bug ? freshBug(id) : freshQuality(id);
        String writerId = bug ? ((RdmsBugFeedback) fresh).getWriterId() : ((RdmsQuality) fresh).getWriterId();
        String operatorId = TenantContext.getMembershipId();
        if (!StringUtils.hasText(operatorId)) {
            operatorId = writerId;
        }
        FlowContext context = context(fresh, operatorId, bug ? bugBizId(id) : qualityBizId(id));
        context.setInitiatorId(writerId);
        context.putVariable("reviewResult", reviewResult);
        flowRuntimeService.reassignCurrent(context, operatorId);
        flowRuntimeService.complete(context);
    }

    private RdmsQuality freshQuality(String id) {
        return rdmsQualityService.selectByPrimaryKey(id);
    }

    private RdmsBugFeedback freshBug(String id) {
        return rdmsBugFeedbackService.selectByPrimaryKey(id);
    }

    private void reloadQuality(FlowContext context, String id) {
        context.setBizEntity(freshQuality(id));
        context.setNodeId(null);
        context.setTaskId(null);
    }

    private void reloadBug(FlowContext context, String id) {
        context.setBizEntity(freshBug(id));
        context.setNodeId(null);
        context.setTaskId(null);
    }

    private FlowContext context(Object entity, String operatorId, String bizId) {
        FlowContext context = new FlowContext();
        String customerId = TenantContext.getTenantId();
        String storedCustomer = entity instanceof RdmsBugFeedback
                ? ((RdmsBugFeedback) entity).getCustomerId()
                : ((RdmsQuality) entity).getCustomerId();
        if (!StringUtils.hasText(customerId)) {
            customerId = storedCustomer;
        }
        context.setCustomerId(customerId);
        context.setBizCode(FlowBizCode.QUALITY.getCode());
        context.setBizId(bizId);
        context.setOperatorId(operatorId);
        context.setInitiatorId(operatorId);
        context.setBizEntity(entity);
        return context;
    }
}
