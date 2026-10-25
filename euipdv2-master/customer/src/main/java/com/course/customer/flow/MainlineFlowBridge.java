/**
 * Author：周朔鹏
 * 编制时间：2026-10-06
 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */

package com.course.customer.flow;

import com.course.server.domain.RdmsFlowBizRef;
import com.course.server.enums.rdms.FlowInstanceStatusEnum;
import com.course.server.exception.BusinessException;
import com.course.server.exception.BusinessExceptionCode;
import com.course.server.mapper.RdmsFlowBizRefMapper;
import com.course.server.service.rdms.flow.FlowContext;
import com.course.server.service.rdms.flow.MainlineFlowPort;
import com.course.server.service.rdms.flow.MainlineMove;
import com.course.server.service.rdms.flow.MainlineTransitions;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

/**
 * 主线状态推进与流程运行时的衔接。操作人已经过原服务的权限校验，
 * 本类把当前任务改派给该操作人后再完成，使处理人记在流程历史上。
 */
@Primary
@Service("mainlineFlowPort")
public class MainlineFlowBridge implements MainlineFlowPort {

    @Resource
    private FlowSwitch flowSwitch;

    @Resource
    private FlowRuntimeService flowRuntimeService;

    @Resource
    private RdmsFlowBizRefMapper rdmsFlowBizRefMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean move(MainlineMove move) {
        if (move == null || !StringUtils.hasText(move.getCustomerId()) || !StringUtils.hasText(move.getBizCode())
                || !StringUtils.hasText(move.getBizId()) || !StringUtils.hasText(move.getOperatorId())
                || !StringUtils.hasText(move.getTargetStatus())) {
            return false;
        }
        if (!flowSwitch.enabled(move.getCustomerId(), move.getBizCode())) {
            return false;
        }
        FlowContext context = context(move);
        RdmsFlowBizRef ref = rdmsFlowBizRefMapper.selectByBiz(move.getCustomerId(), move.getBizCode(), move.getBizId());
        if (ref == null) {
            if (move.isExistingOnly()) {
                return false;
            }
            flowRuntimeService.start(context);
        } else if (FlowInstanceStatusEnum.RUNNING.getStatus().equals(ref.getStatus())) {
            flowRuntimeService.reassignCurrent(context, move.getOperatorId());
        } else if (move.isExistingOnly()) {
            return false;
        } else {
            throw new BusinessException(BusinessExceptionCode.FLOW_INSTANCE_NOT_FOUND, "主线实例已结束");
        }
        // 本步意见随 processText 变量由运行时统一记为任务批注，此处不再另记。
        flowRuntimeService.complete(context);
        return true;
    }

    private FlowContext context(MainlineMove move) {
        FlowContext context = new FlowContext();
        context.setCustomerId(move.getCustomerId());
        context.setBizCode(move.getBizCode());
        context.setBizId(move.getBizId());
        context.setOperatorId(move.getOperatorId());
        context.setInitiatorId(move.getOperatorId());
        context.putVariable("targetStatus", move.getTargetStatus());
        context.putVariable("archiveStatus", move.getTargetStatus());
        context.putVariable("archive", MainlineTransitions.archiveFamily(move.getTargetStatus())
                ? MainlineTransitions.YES : "NO");
        context.putVariable("terminal", MainlineTransitions.terminal(move.getTargetStatus())
                ? MainlineTransitions.YES : "NO");
        context.putVariable("processText", move.getProcessText() == null ? "" : move.getProcessText());
        context.putVariable("mainProject", move.isMainProject() ? MainlineTransitions.YES : "NO");
        context.putVariable(MainlineTransitions.ALLOW_MAIN_ONGOING,
                move.isAllowMainOngoing() ? MainlineTransitions.YES : "NO");
        if (StringUtils.hasText(move.getEntityKind())) {
            context.putVariable(MainlineTransitions.ENTITY_KIND, move.getEntityKind());
        }
        if (StringUtils.hasText(move.getCascade())) {
            context.putVariable(MainlineTransitions.CASCADE, move.getCascade());
        }
        return context;
    }
}
