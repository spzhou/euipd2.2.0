/**
 * Author：周朔鹏
 * 编制时间：2026-10-06
 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */

package com.course.customer.controller;

import com.course.customer.flow.FlowDefinitionService;
import com.course.customer.flow.FlowDiagramService;
import com.course.server.domain.RdmsFlowDef;
import com.course.server.dto.ResponseDto;
import com.course.server.dto.rdms.FlowCapabilityDto;
import com.course.server.dto.rdms.FlowDiagramDto;
import com.course.server.dto.rdms.FlowRequestDto;
import com.course.server.exception.BusinessException;
import com.course.server.exception.BusinessExceptionCode;
import com.course.server.security.tenant.TenantContext;
import com.course.server.service.rdms.flow.AssigneeResolverRegistry;
import com.course.server.service.rdms.flow.DomainActionRegistry;
import com.course.server.service.rdms.flow.FlowBizCode;
import com.course.server.service.rdms.flow.FlowFormRegistry;
import com.course.server.service.rdms.flow.FlowContext;
import com.course.server.util.ValidatorUtil;
import com.course.customer.flow.FlowRuntimeService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 流程配置与查看。实施方案第 4.9 节。
 * 机构标识一律取租户上下文，不取请求体。本控制器不改现有业务接口。
 */
@RestController
@RequestMapping("/flow")
public class FlowController {

    @Resource
    private FlowDefinitionService flowDefinitionService;

    @Resource
    private FlowRuntimeService flowRuntimeService;

    @Resource
    private FlowDiagramService flowDiagramService;

    @Resource
    private AssigneeResolverRegistry assigneeResolverRegistry;

    @Resource
    private DomainActionRegistry domainActionRegistry;

    /** 路径含 /list，按推断规则为 VIEW。 */
    @PostMapping("/def/list")
    public ResponseDto<List<RdmsFlowDef>> list(@RequestBody FlowRequestDto request) {
        ValidatorUtil.require(request.getBizCode(), "业务码");
        ResponseDto<List<RdmsFlowDef>> response = new ResponseDto<>();
        response.setContent(flowDefinitionService.listVersions(customerId(), request.getBizCode()));
        return response;
    }

    /** 路径含 /get，按推断规则为 VIEW。平台内置定义对当前机构可见。 */
    @PostMapping("/def/get")
    public ResponseDto<RdmsFlowDef> get(@RequestBody FlowRequestDto request) {
        ValidatorUtil.require(request.getId(), "定义标识");
        RdmsFlowDef def = flowDefinitionService.get(request.getId());
        if (def.getCustomerId() != null && !def.getCustomerId().equals(customerId())) {
            throw new BusinessException(BusinessExceptionCode.NO_PERMISSION, "不可查看其他机构的流程定义");
        }
        ResponseDto<RdmsFlowDef> response = new ResponseDto<>();
        response.setContent(def);
        return response;
    }

    /** 路径含 /save，按现行推断规则为 CREATE，而不是 UPDATE。 */
    @PostMapping("/def/save")
    public ResponseDto<RdmsFlowDef> save(@RequestBody FlowRequestDto request) {
        ValidatorUtil.require(request.getBizCode(), "业务码");
        ValidatorUtil.require(request.getBpmnXml(), "流程图");
        ResponseDto<RdmsFlowDef> response = new ResponseDto<>();
        response.setContent(flowDefinitionService.saveDraft(customerId(), request.getBizCode(), request.getBpmnXml()));
        response.setMessage("草稿已保存");
        return response;
    }

    /** 以历史版本建草稿。路径含 /save，同为 CREATE。 */
    @PostMapping("/def/save-draft-from/{id}")
    public ResponseDto<RdmsFlowDef> saveDraftFrom(@PathVariable("id") String id) {
        ValidatorUtil.require(id, "定义标识");
        ResponseDto<RdmsFlowDef> response = new ResponseDto<>();
        response.setContent(flowDefinitionService.copyAsDraft(customerId(), id));
        response.setMessage("已建草稿");
        return response;
    }

    @PostMapping("/def/validate")
    public ResponseDto<List<String>> validate(@RequestBody FlowRequestDto request) {
        ValidatorUtil.require(request.getBizCode(), "业务码");
        ValidatorUtil.require(request.getBpmnXml(), "流程图");
        ResponseDto<List<String>> response = new ResponseDto<>();
        response.setContent(flowDefinitionService.validate(request.getBizCode(), request.getBpmnXml()));
        return response;
    }

    @PostMapping("/def/publish")
    public ResponseDto<RdmsFlowDef> publish(@RequestBody FlowRequestDto request) {
        ValidatorUtil.require(request.getBizCode(), "业务码");
        ValidatorUtil.require(request.getBpmnXml(), "流程图");
        ResponseDto<RdmsFlowDef> response = new ResponseDto<>();
        response.setContent(flowDefinitionService.publish(
                customerId(), request.getBizCode(), request.getBpmnXml(), operatorId()));
        response.setMessage("已发布");
        return response;
    }

    @PostMapping("/def/disable")
    public ResponseDto<String> disable(@RequestBody FlowRequestDto request) {
        ValidatorUtil.require(request.getId(), "定义标识");
        flowDefinitionService.disable(customerId(), request.getId());
        ResponseDto<String> response = new ResponseDto<>();
        response.setContent(request.getId());
        response.setMessage("已停用");
        return response;
    }

    @PostMapping("/capability/list")
    public ResponseDto<FlowCapabilityDto> capability() {
        FlowCapabilityDto dto = new FlowCapabilityDto();
        dto.setResolvers(assigneeResolverRegistry.catalog());
        dto.setActions(domainActionRegistry.catalog());
        dto.setForms(FlowFormRegistry.catalog());
        for (FlowBizCode biz : FlowBizCode.values()) {
            FlowCapabilityDto.FlowBizItemDto item = new FlowCapabilityDto.FlowBizItemDto();
            item.setCode(biz.getCode());
            item.setProcessKey(biz.getProcessKey());
            item.setName(biz.getName());
            dto.getBizCodes().add(item);
        }
        ResponseDto<FlowCapabilityDto> response = new ResponseDto<>();
        response.setContent(dto);
        return response;
    }

    @PostMapping("/instance/diagram")
    public ResponseDto<FlowDiagramDto> diagram(@RequestBody FlowRequestDto request) {
        ValidatorUtil.require(request.getBizCode(), "业务码");
        ValidatorUtil.require(request.getBizId(), "业务主键");
        ResponseDto<FlowDiagramDto> response = new ResponseDto<>();
        response.setContent(flowDiagramService.diagram(customerId(), request.getBizCode(), request.getBizId()));
        return response;
    }

    @PostMapping("/instance/history")
    public ResponseDto<List<FlowDiagramDto.FlowNodeDetailDto>> history(@RequestBody FlowRequestDto request) {
        ValidatorUtil.require(request.getBizCode(), "业务码");
        ValidatorUtil.require(request.getBizId(), "业务主键");
        ResponseDto<List<FlowDiagramDto.FlowNodeDetailDto>> response = new ResponseDto<>();
        response.setContent(flowDiagramService.diagram(
                customerId(), request.getBizCode(), request.getBizId()).getNodeDetails());
        return response;
    }

    /** 统一推进入口。已有领域绑定的业务码可以调用；费用控制器仍走原代码，不经本接口。 */
    @PostMapping("/task/complete")
    public ResponseDto<String> complete(@RequestBody FlowRequestDto request) {
        ValidatorUtil.require(request.getBizCode(), "业务码");
        ValidatorUtil.require(request.getBizId(), "业务主键");
        // 工单的提交、评价门禁在业务接口上，不经通用推进
        if (FlowBizCode.JOB_ITEM.getCode().equals(request.getBizCode())) {
            throw new BusinessException(BusinessExceptionCode.LIFECYCLE_TRANSITION_FORBIDDEN, "工单须经业务接口办理");
        }
        FlowContext context = new FlowContext();
        context.setCustomerId(customerId());
        context.setBizCode(request.getBizCode());
        context.setBizId(request.getBizId());
        context.setNodeId(request.getNodeId());
        context.setOperatorId(operatorId());
        if (request.getVariables() != null) {
            context.getVariables().putAll(request.getVariables());
        }
        flowRuntimeService.complete(context);
        ResponseDto<String> response = new ResponseDto<>();
        response.setContent(request.getBizId());
        response.setMessage("已推进");
        return response;
    }

    private String customerId() {
        String customerId = TenantContext.getTenantId();
        if (!StringUtils.hasText(customerId)) {
            throw new BusinessException(BusinessExceptionCode.NO_PERMISSION, "缺少机构标识");
        }
        return customerId;
    }

    private String operatorId() {
        String operatorId = TenantContext.getMembershipId();
        if (!StringUtils.hasText(operatorId)) {
            throw new BusinessException(BusinessExceptionCode.NO_PERMISSION, "缺少操作人");
        }
        return operatorId;
    }
}
