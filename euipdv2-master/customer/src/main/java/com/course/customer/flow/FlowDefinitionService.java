/**
 * Author：周朔鹏
 * 编制时间：2026-10-06
 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */

package com.course.customer.flow;

import com.course.server.domain.RdmsFlowDef;
import com.course.server.enums.rdms.FlowDefStatusEnum;
import com.course.server.exception.BusinessException;
import com.course.server.exception.BusinessExceptionCode;
import com.course.server.mapper.RdmsFlowBizRefMapper;
import com.course.server.mapper.RdmsFlowDefMapper;
import com.course.server.service.rdms.flow.FlowBizCode;
import com.course.server.util.UuidUtil;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 流程定义的保存、校验、发布、停用与版本列表。实施方案第 4.2、4.6 节。
 * <p>
 * 发布是唯一把定义写入引擎的入口，一律先过发布校验。引擎内的定义带机构标识作
 * tenantId，避免跨机构启动实例。
 */
@Service
public class FlowDefinitionService {

    @Resource
    private RdmsFlowDefMapper rdmsFlowDefMapper;

    @Resource
    private RdmsFlowBizRefMapper rdmsFlowBizRefMapper;

    @Resource
    private RepositoryService repositoryService;

    @Resource
    private FlowBpmnParser flowBpmnParser;

    @Resource
    private FlowPublishValidator flowPublishValidator;

    /** 版本列表。含机构自有与平台内置定义，不含定义全文。 */
    public List<RdmsFlowDef> listVersions(String customerId, String bizCode) {
        requireBiz(bizCode);
        return rdmsFlowDefMapper.listByBizCode(customerId, bizCode);
    }

    /** 取一条定义，含全文与节点参数。 */
    public RdmsFlowDef get(String id) {
        RdmsFlowDef def = rdmsFlowDefMapper.selectById(id);
        if (def == null) {
            throw new BusinessException(BusinessExceptionCode.FLOW_DEF_NOT_FOUND);
        }
        return def;
    }

    /** 取该机构该业务码当前启用的定义，没有则拒绝。启动实例与推进都经此取版本。 */
    public RdmsFlowDef requireActive(String customerId, String bizCode) {
        RdmsFlowDef def = rdmsFlowDefMapper.selectActive(customerId, bizCode);
        if (def == null) {
            throw new BusinessException(BusinessExceptionCode.FLOW_DEF_NOT_FOUND,
                    "业务 " + bizCode + " 没有启用的流程定义");
        }
        return def;
    }

    /** 只校验不发布，返回问题清单供设计器展示。 */
    public List<String> validate(String bizCode, String bpmnXml) {
        return flowPublishValidator.validate(flowBpmnParser.parse(bpmnXml), bizCode);
    }

    /**
     * 保存草稿。草稿不入引擎，只校验能否解析成 BPMN，不校验流程标识与节点参数，
     * 允许机构把画了一半的图存下来。流程标识与五类校验在发布时执行。
     */
    @Transactional(rollbackFor = Exception.class)
    public RdmsFlowDef saveDraft(String customerId, String bizCode, String bpmnXml) {
        requireCustomer(customerId);
        FlowBizCode biz = requireBiz(bizCode);
        flowBpmnParser.parse(bpmnXml);

        Date now = new Date();
        RdmsFlowDef draft = rdmsFlowDefMapper.selectDraft(customerId, bizCode);
        if (draft != null) {
            draft.setBpmnXml(bpmnXml);
            draft.setUpdateTime(now);
            rdmsFlowDefMapper.updateBpmnXml(draft);
            return draft;
        }

        draft = new RdmsFlowDef();
        draft.setId(UuidUtil.getShortUuid());
        draft.setCustomerId(customerId);
        draft.setBizCode(biz.getCode());
        draft.setProcessKey(biz.getProcessKey());
        draft.setVersion(0);
        draft.setStatus(FlowDefStatusEnum.DRAFT.getStatus());
        draft.setBpmnXml(bpmnXml);
        draft.setCreateTime(now);
        draft.setUpdateTime(now);
        rdmsFlowDefMapper.insert(draft);
        return draft;
    }

    /**
     * 以已发布的历史版本（本机构或平台内置）为基础建立本机构草稿，覆盖现有草稿。
     * 回退即由此建草稿后重新发布，版本号继续加一；不改引擎内已部署的定义，在途实例仍按启动时的版本执行。
     */
    @Transactional(rollbackFor = Exception.class)
    public RdmsFlowDef copyAsDraft(String customerId, String defId) {
        requireCustomer(customerId);
        RdmsFlowDef source = get(defId);
        if (source.getCustomerId() != null && !customerId.equals(source.getCustomerId())) {
            throw new BusinessException(BusinessExceptionCode.NO_PERMISSION, "不可复制其他机构的流程定义");
        }
        if (FlowDefStatusEnum.DRAFT.getStatus().equals(source.getStatus())) {
            throw new BusinessException(BusinessExceptionCode.FLOW_DEF_INVALID, "草稿不能作为建草稿的来源，请直接打开编辑");
        }
        return saveDraft(customerId, source.getBizCode(), source.getBpmnXml());
    }

    /**
     * 校验并发布。校验不通过即拒绝，不写引擎也不写库。
     * 发布成功后该机构该业务码其余启用定义转为停用，在途实例仍按各自启动时的版本执行。
     */
    @Transactional(rollbackFor = Exception.class)
    public RdmsFlowDef publish(String customerId, String bizCode, String bpmnXml, String publisherId) {
        requireCustomer(customerId);
        FlowBizCode biz = requireBiz(bizCode);

        BpmnModel model = flowBpmnParser.parse(bpmnXml);
        flowPublishValidator.assertValid(model, bizCode);

        Deployment deployment = repositoryService.createDeployment()
                .name(biz.getName() + " " + customerId)
                .key(customerId + "-" + biz.getCode())
                .tenantId(customerId)
                .addString(biz.getProcessKey() + ".bpmn20.xml", bpmnXml)
                .deploy();
        ProcessDefinition deployed = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deployment.getId())
                .processDefinitionKey(biz.getProcessKey())
                .singleResult();
        if (deployed == null) {
            throw new BusinessException(BusinessExceptionCode.FLOW_DEF_INVALID,
                    "引擎未能解析出流程 " + biz.getProcessKey());
        }

        Integer maxVersion = rdmsFlowDefMapper.selectMaxVersion(customerId, bizCode);
        Date now = new Date();

        RdmsFlowDef target = rdmsFlowDefMapper.selectDraft(customerId, bizCode);
        if (target != null) {
            target.setBpmnXml(bpmnXml);
            target.setDefinitionId(deployed.getId());
            target.setVersion(maxVersion == null ? 1 : maxVersion + 1);
            target.setPublisherId(publisherId);
            target.setPublishTime(now);
            target.setUpdateTime(now);
            rdmsFlowDefMapper.updateBpmnXml(target);
            rdmsFlowDefMapper.publish(target);
            target.setStatus(FlowDefStatusEnum.ACTIVE.getStatus());
        } else {
            target = new RdmsFlowDef();
            target.setId(UuidUtil.getShortUuid());
            target.setCustomerId(customerId);
            target.setBizCode(biz.getCode());
            target.setProcessKey(biz.getProcessKey());
            target.setDefinitionId(deployed.getId());
            target.setVersion(maxVersion == null ? 1 : maxVersion + 1);
            target.setStatus(FlowDefStatusEnum.ACTIVE.getStatus());
            target.setBpmnXml(bpmnXml);
            target.setPublisherId(publisherId);
            target.setPublishTime(now);
            target.setCreateTime(now);
            target.setUpdateTime(now);
            rdmsFlowDefMapper.insert(target);
        }

        rdmsFlowDefMapper.deactivateOthers(customerId, bizCode, target.getId(), now);
        return target;
    }

    /**
     * 发布平台内置定义。customer_id 为空，机构无自定义启用定义时由 {@code selectActive} 回落到它。
     * 全文与当前启用版本一致时不再部署。只停用其余平台版本，不改机构自定义定义。
     * 引擎侧 tenantId 固定为 platform，启动时按流程定义标识加载，实例仍记机构标识。
     */
    @Transactional(rollbackFor = Exception.class)
    public RdmsFlowDef publishPlatform(String bizCode, String bpmnXml) {
        FlowBizCode biz = requireBiz(bizCode);
        String normalized = bpmnXml == null ? "" : bpmnXml.replace("\r\n", "\n");
        RdmsFlowDef current = rdmsFlowDefMapper.selectPlatformActive(bizCode);
        if (current != null && current.getBpmnXml() != null
                && normalized.equals(current.getBpmnXml().replace("\r\n", "\n"))) {
            return current;
        }

        BpmnModel model = flowBpmnParser.parse(normalized);
        flowPublishValidator.assertValid(model, bizCode);

        Deployment deployment = repositoryService.createDeployment()
                .name(biz.getName() + " platform")
                .key("platform-" + biz.getCode())
                .tenantId(PLATFORM_TENANT)
                .addString(biz.getProcessKey() + ".bpmn20.xml", normalized)
                .deploy();
        ProcessDefinition deployed = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deployment.getId())
                .processDefinitionKey(biz.getProcessKey())
                .singleResult();
        if (deployed == null) {
            throw new BusinessException(BusinessExceptionCode.FLOW_DEF_INVALID,
                    "引擎未能解析出流程 " + biz.getProcessKey());
        }

        Integer maxVersion = rdmsFlowDefMapper.selectPlatformMaxVersion(bizCode);
        Date now = new Date();
        RdmsFlowDef target = new RdmsFlowDef();
        target.setId(UuidUtil.getShortUuid());
        target.setCustomerId(null);
        target.setBizCode(biz.getCode());
        target.setProcessKey(biz.getProcessKey());
        target.setDefinitionId(deployed.getId());
        target.setVersion(maxVersion == null ? 1 : maxVersion + 1);
        target.setStatus(FlowDefStatusEnum.ACTIVE.getStatus());
        target.setBpmnXml(normalized);
        target.setPublisherId(PLATFORM_TENANT);
        target.setPublishTime(now);
        target.setCreateTime(now);
        target.setUpdateTime(now);
        rdmsFlowDefMapper.insert(target);
        rdmsFlowDefMapper.deactivatePlatformOthers(bizCode, target.getId(), now);
        return target;
    }

    /** 平台定义在引擎中的租户标识。与机构标识同为 8 位，便于写入 publisher_id。 */
    public static final String PLATFORM_TENANT = "platform";

    /**
     * 停用。仍有在途实例时拒绝，由操作人先处理完在途单据或改为发布新版本。
     * 平台内置定义不得由机构停用。
     */
    @Transactional(rollbackFor = Exception.class)
    public void disable(String customerId, String id) {
        requireCustomer(customerId);
        RdmsFlowDef def = get(id);
        if (!customerId.equals(def.getCustomerId())) {
            throw new BusinessException(BusinessExceptionCode.NO_PERMISSION,
                    "不可停用其他机构或平台内置的流程定义");
        }
        if (rdmsFlowBizRefMapper.countRunningByDef(id) > 0) {
            throw new BusinessException(BusinessExceptionCode.FLOW_DEF_IN_USE);
        }
        rdmsFlowDefMapper.updateStatus(id, FlowDefStatusEnum.INACTIVE.getStatus(), new Date());
    }

    /** 取引擎内该版本的模型，用于解析节点参数与流程图显示。 */
    public BpmnModel bpmnModel(RdmsFlowDef def) {
        if (!StringUtils.hasText(def.getDefinitionId())) {
            throw new BusinessException(BusinessExceptionCode.FLOW_DEF_NOT_FOUND,
                    "流程定义 " + def.getId() + " 尚未发布到引擎");
        }
        return repositoryService.getBpmnModel(def.getDefinitionId());
    }

    /** 未登记的业务码在进入引擎之前拒绝，避免空指针，也避免把非法码写成草稿。 */
    private FlowBizCode requireBiz(String bizCode) {
        FlowBizCode biz = FlowBizCode.of(bizCode);
        if (biz == null) {
            throw new BusinessException(BusinessExceptionCode.FLOW_NODE_CONFIG_INVALID,
                    "业务码 " + bizCode + " 未登记");
        }
        return biz;
    }

    private void requireCustomer(String customerId) {
        if (!StringUtils.hasText(customerId)) {
            throw new BusinessException(BusinessExceptionCode.NO_PERMISSION, "缺少机构标识");
        }
    }
}
