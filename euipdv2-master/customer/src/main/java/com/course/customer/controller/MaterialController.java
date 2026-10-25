/**
 * Author：周朔鹏
 * 编制时间：2025-08-05

 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */

package com.course.customer.controller;

import com.course.server.domain.*;
import com.course.server.dto.PageDto;
import com.course.server.dto.ResponseDto;
import com.course.server.dto.rdms.*;
import com.course.server.enums.rdms.*;
import com.course.server.exception.BusinessException;
import com.course.server.exception.BusinessExceptionCode;
import com.course.server.security.tenant.TenantAccess;
import com.course.server.security.tenant.TenantContext;
import com.course.server.service.rdms.*;
import com.course.customer.flow.MaterialFlowBridge;
import com.course.customer.flow.MaterialReturnFlowBridge;
import com.course.server.service.rdms.flow.FeeApproverClassifier;
import com.course.server.service.rdms.helper.TenantParentGuard;
import com.course.server.util.CopyUtil;
import com.course.server.util.UuidUtil;
import com.course.server.util.ValidatorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/material")
public class MaterialController {
    private static final Logger LOG = LoggerFactory.getLogger(MaterialController.class);
    public static final String BUSINESS_NAME = "物料管理";

    @Resource
    private RdmsMaterialManageService rdmsMaterialManageService;
    @Resource
    private MaterialFlowBridge materialFlowBridge;
    @Resource
    private MaterialReturnFlowBridge materialReturnFlowBridge;
    @Resource
    private RdmsMaterialReturnService rdmsMaterialReturnService;
    @Resource
    private RdmsMaterialWarehouseService rdmsMaterialWarehouseService;
    @Resource
    private JobActorGuard jobActorGuard;
    @Resource
    private RdmsBomGateService rdmsBomGateService;
    @Resource
    private BomSnapshotService bomSnapshotService;
    @Resource
    private FeeApproverClassifier feeApproverClassifier;

    @Resource
    private RdmsMaterialService rdmsMaterialService;
    @Resource
    private RdmsMaterialProcessService rdmsMaterialProcessService;
    @Resource
    private RdmsCustomerUserService rdmsCustomerUserService;
    @Resource
    private RdmsSubprojectService rdmsSubprojectService;
    @Resource
    private RdmsBomService rdmsBomService;
    @Resource
    private RdmsCharacterService rdmsCharacterService;
    @Resource
    private RdmsJobItemService rdmsJobItemService;
    @Resource
    private ApplicationBudgetExeWriter applicationBudgetExeWriter;
    @Resource
    private RdmsMaterialStockService rdmsMaterialStockService;
    @Resource
    private RdmsProjectService rdmsProjectService;
    @Resource
    private RdmsBomTreeService rdmsBomTreeService;
    @Resource
    private RdmsBomConfigService rdmsBomConfigService;
    @Resource
    private TenantParentGuard tenantParentGuard;
    @Resource
    private RdmsPreProjectService rdmsPreProjectService;
    @Resource
    private RdmsSysgmService rdmsSysgmService;
    @Autowired
    private RdmsBossService rdmsBossService;

    /**
     * 保存客户信息
     */
    @PostMapping("/saveMaterialApplication")
    @Transactional
    public ResponseDto<String> saveMaterialApplication(@RequestBody RdmsMaterialManageDto materialManageDto) {
        ResponseDto<String> responseDto = new ResponseDto<>();

        ValidatorUtil.require(materialManageDto.getProjectType(), "项目类型");
        if (materialManageDto.getProjectType().equals(ProjectTypeEnum.PRE_PROJECT.getType())) {
            ValidatorUtil.require(materialManageDto.getPreProjectId(), "项目Id");
        } else if (materialManageDto.getProjectType().equals(ProjectTypeEnum.PROJECT.getType())) {
            ValidatorUtil.require(materialManageDto.getProjectId(), "项目Id");
        } else {
            ValidatorUtil.require(materialManageDto.getProjectId(), "项目Id");
            ValidatorUtil.require(materialManageDto.getSubprojectId(), "项目Id");
        }
        ValidatorUtil.require(materialManageDto.getCode(), "申请单号");
        ValidatorUtil.require(materialManageDto.getName(), "申请标题");
        ValidatorUtil.require(materialManageDto.getCustomerId(), "公司Id");
        ValidatorUtil.require(materialManageDto.getJobType(), "工单类型");

        ValidatorUtil.require(materialManageDto.getJobitemId(), "工单Id");
        ValidatorUtil.require(materialManageDto.getMaterialList(), "物料清单");
        ValidatorUtil.require(materialManageDto.getReason(), "领用事由");
        ValidatorUtil.require(materialManageDto.getWriterId(), "填表人");
        ValidatorUtil.require(materialManageDto.getNextNode(), "下一节点");
        ValidatorUtil.require(materialManageDto.getUsageMode(), "使用方式");

        ValidatorUtil.length(materialManageDto.getName(), "申请标题", 2, 30);
        ValidatorUtil.length(materialManageDto.getReason(), "领用事由", 6, 800);
        ValidatorUtil.length(materialManageDto.getUsageMode(), "使用方式", 2, 20);

        ValidatorUtil.listNotEmpty(materialManageDto.getMaterialList(), "物料清单");

        RdmsJobItem rdmsJobItem = rdmsJobItemService.selectByPrimaryKey(materialManageDto.getJobitemId());
        if (rdmsJobItem == null) {
            throw new BusinessException(BusinessExceptionCode.DB_RECORD_NOT_EXIST);
        }
        TenantAccess.assertSameTenant(rdmsJobItem.getCustomerId());
        String operatorId = this.resolveOperatorId(materialManageDto.getWriterId());
        materialManageDto.setWriterId(operatorId);
        String approverId = feeApproverClassifier.classify(rdmsJobItem).getApproverId();
        if (ObjectUtils.isEmpty(approverId)) {
            throw new BusinessException(BusinessExceptionCode.MATERIAL_APPROVER_NOT_FOUND);
        }
        materialManageDto.setNextNode(approverId);
        RdmsMaterialManage resubmitted = this.requireResubmittable(materialManageDto, operatorId);
        boolean proxy = resubmitted != null && !operatorId.equals(resubmitted.getWriterId());
        if (resubmitted != null) {
            materialManageDto.setWriterId(resubmitted.getWriterId());
        }
        if (!(rdmsJobItem.getType().equals(JobItemTypeEnum.QUALITY.getType())
                || rdmsJobItem.getType().equals(JobItemTypeEnum.TASK_BUG.getType()) || JobItemTypeEnum.TASK_BUG.getType().equals(rdmsJobItem.getAuxType())
                || rdmsJobItem.getType().equals(JobItemTypeEnum.TASK_SUBP.getType())
                || rdmsJobItem.getType().equals(JobItemTypeEnum.ASSIST.getType())
                || rdmsJobItem.getType().equals(JobItemTypeEnum.TEST.getType())
                || rdmsJobItem.getType().equals(JobItemTypeEnum.TASK_FUNCTION.getType())
                || rdmsJobItem.getType().equals(JobItemTypeEnum.TASK_PRODUCT.getType())
        )) {
            ValidatorUtil.require(materialManageDto.getCharacterId(), "功能特性Id");
        }

        rdmsMaterialStockService.bindToStock(rdmsJobItem.getCustomerId(), materialManageDto.getMaterialList());
        BigDecimal applicationCost = BigDecimal.ZERO;
        for (RdmsMaterialDto materialDto : materialManageDto.getMaterialList()) {
            RdmsMaterial material = CopyUtil.copy(materialDto, RdmsMaterial.class);
            if (!ObjectUtils.isEmpty(material.getId())) {
                RdmsMaterial storedLine = rdmsMaterialService.selectByPrimaryKey(material.getId());
                if (storedLine != null) {
                    tenantParentGuard.requireMaterialApplication(storedLine.getCode());
                }
                if (storedLine != null && !Objects.equals(storedLine.getCode(), materialManageDto.getCode())) {
                    throw new BusinessException(BusinessExceptionCode.NO_PERMISSION);
                }
            }
            material.setCode(materialManageDto.getCode());
            applicationCost = applicationCost.add(material.getPreTaxSumPrice());
            rdmsMaterialService.save(material);
        }

        //保存申请表
        RdmsMaterialManage materialManage = CopyUtil.copy(materialManageDto, RdmsMaterialManage.class);
        materialManage.setAccountCost(applicationCost);
        String flowCustomerId = TenantContext.getTenantId();
        if (!StringUtils.hasText(flowCustomerId)) {
            flowCustomerId = materialManageDto.getCustomerId();
        }
        boolean engineResubmit = resubmitted != null && materialFlowBridge.engineForExisting(flowCustomerId, resubmitted.getCode());
        boolean engine = !engineResubmit && materialFlowBridge.engineForNew(flowCustomerId);
        if (engineResubmit) {
            materialManage.setStatus(resubmitted.getStatus());
        } else {
            materialManage.setStatus(engine ? null : ApplicationStatusEnum.APPLICATION.getStatus());
        }
        materialManage.setJobType(rdmsJobItem.getType());
        materialManage.setApproverId(materialManageDto.getNextNode());
        if (materialManageDto.getProjectType().equals(ProjectTypeEnum.PRE_PROJECT.getType())) {
            materialManage.setAuxStatus(JobItemTypeEnum.TASK_FUNCTION.getType());
        } else if (materialManageDto.getProjectType().equals(ProjectTypeEnum.PROJECT.getType())) {
            if (materialManageDto.getJobType().equals(JobItemTypeEnum.TASK_TEST.getType())) {
                materialManage.setAuxStatus(JobItemTypeEnum.TASK_TEST.getType());
            } else {
                materialManage.setAuxStatus(JobItemTypeEnum.TASK_PRODUCT.getType());
            }
        } else {
            if(materialManageDto.getJobType().equals(JobItemTypeEnum.TASK_BUG.getType())
                    || (materialManageDto.getInheritJobType() != null && materialManageDto.getInheritJobType().equals(JobItemTypeEnum.TASK_BUG.getType()))){
                ValidatorUtil.require(materialManageDto.getProjectId(), "项目Id");
                ValidatorUtil.require(materialManageDto.getSubprojectId(), "子项目Id");
                materialManage.setAuxStatus(JobItemTypeEnum.TASK_BUG.getType());
            }else{
                ValidatorUtil.require(materialManageDto.getProjectId(), "项目Id");
                ValidatorUtil.require(materialManageDto.getSubprojectId(), "子项目Id");
                materialManage.setAuxStatus(JobItemTypeEnum.TASK_SUBP.getType());
            }

        }
//        materialManage.setOpeStatus(MaterialOpeStatusEnum.APPLICATION.getStatus());
        rdmsMaterialManageService.save(materialManage);
        if (engineResubmit) {
            materialFlowBridge.resubmit(materialManage.getCode(), operatorId, proxy);
            responseDto.setContent(materialManage.getId());
            responseDto.setMessage("保存成功");
            return responseDto;
        }
        if (engine) {
            materialFlowBridge.openApplication(materialManage.getCode(), materialManageDto.getWriterId());
            responseDto.setContent(materialManage.getId());
            responseDto.setMessage("保存成功");
            return responseDto;
        }

        //填写一条过程记录
        RdmsMaterialProcess materialProcess = new RdmsMaterialProcess();
        materialProcess.setMaterialId(materialManage.getId());
        materialProcess.setExecutorId(operatorId);
        materialProcess.setDeep(1);
        materialProcess.setDescription("提交物料申请");
        materialProcess.setNextNode(materialManage.getNextNode());
        materialProcess.setSubmitTime(new Date());
        materialProcess.setStatus(ApplicationStatusEnum.APPLICATION.getStatus());
        rdmsMaterialProcessService.save(materialProcess);

        responseDto.setContent(materialManage.getId());
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    @PostMapping("/saveMaterialApplication_return")
    @Transactional
    public ResponseDto<String> saveMaterialApplication_return(@RequestBody RdmsMaterialManageDto materialManageDto) {
        ResponseDto<String> responseDto = new ResponseDto<>();

        ValidatorUtil.require(materialManageDto.getCode(), "申请单号");
        ValidatorUtil.require(materialManageDto.getName(), "申请标题");
        ValidatorUtil.require(materialManageDto.getCustomerId(), "公司Id");
        ValidatorUtil.require(materialManageDto.getJobType(), "工单类型");

        ValidatorUtil.require(materialManageDto.getJobitemId(), "工单Id");
        ValidatorUtil.require(materialManageDto.getMaterialList(), "物料清单");
        ValidatorUtil.require(materialManageDto.getReason(), "领用事由");
        ValidatorUtil.require(materialManageDto.getWriterId(), "填表人");
//        ValidatorUtil.require(materialManageDto.getNextNode(), "下一节点");

        ValidatorUtil.length(materialManageDto.getName(), "申请标题", 2, 30);
        ValidatorUtil.length(materialManageDto.getReason(), "领用事由", 6, 800);

        ValidatorUtil.listNotEmpty(materialManageDto.getMaterialList(), "物料清单");

        ValidatorUtil.require(materialManageDto.getOriginCode(), "原始单号");
        RdmsMaterialManageDto originApplication = rdmsMaterialManageService.getMaterialManageSimpleInfoByCode_origin(materialManageDto.getOriginCode());
        if (originApplication == null) {
            throw new BusinessException(BusinessExceptionCode.DB_RECORD_NOT_EXIST);
        }
        TenantAccess.assertSameTenant(originApplication.getCustomerId());
        String returnOperatorId = this.resolveOperatorId(materialManageDto.getWriterId());
        this.assertWriterOrProxy(originApplication.getCustomerId(), originApplication.getWriterId(), returnOperatorId);
        if (!ApplicationStatusEnum.APPROVED.getStatus().equals(originApplication.getStatus())
                || StringUtils.hasText(originApplication.getOpeStatus())
                || originApplication.isPendingIssue()) {
            throw new BusinessException(BusinessExceptionCode.LIFECYCLE_TRANSITION_FORBIDDEN);
        }
        materialManageDto.setWriterId(originApplication.getWriterId());
        materialManageDto.setJobitemId(originApplication.getJobitemId());
        materialManageDto.setCustomerId(originApplication.getCustomerId());
        RdmsJobItem rdmsJobItem = rdmsJobItemService.selectByPrimaryKey(originApplication.getJobitemId());
        if (rdmsJobItem == null) {
            throw new BusinessException(BusinessExceptionCode.DB_RECORD_NOT_EXIST);
        }
        //将物料信息保存到物料表中
        BigDecimal totalCost = new BigDecimal(0);
        for (RdmsMaterialDto materialDto : materialManageDto.getMaterialList()) {
            ValidatorUtil.require(materialDto.getCheckOrNot(), "是否检查");
            ValidatorUtil.require(materialDto.getAmount(), "退库数量");
            // 本地库存按仓库+物料编码记账，不按批次；批次号/序列号仅作追溯备注
            ValidatorUtil.length(materialDto.getBatchCode(), "批次号/序列号", 0, 50);
            ValidatorUtil.require(materialDto.getTargetWarehouseCode(), "退库目标仓库");
            rdmsMaterialWarehouseService.requireActive(originApplication.getCustomerId(), materialDto.getTargetWarehouseCode().trim());

            String token_code = UuidUtil.getShortUuid();
            //先给原始物料设置一个token_code
            RdmsMaterial originMaterial = rdmsMaterialService.selectByPrimaryKey(materialDto.getId());
            if (originMaterial == null || Integer.valueOf(1).equals(originMaterial.getDeleted())) {
                throw new BusinessException(BusinessExceptionCode.DB_RECORD_NOT_EXIST);
            }
            if (!Objects.equals(originMaterial.getCode(), originApplication.getCode())) {
                throw new BusinessException(BusinessExceptionCode.NO_PERMISSION);
            }
            double originAmount = originMaterial.getAmount() == null ? 0 : originMaterial.getAmount();
            if (materialDto.getAmount() <= 0 || materialDto.getAmount() > originAmount) {
                throw new BusinessException(BusinessExceptionCode.INPUT_ERROR, "退库数量须大于 0 且不超过领用数量");
            }
            originMaterial.setTokenCode(token_code);
            rdmsMaterialService.update(originMaterial);

            RdmsMaterialReturn material = CopyUtil.copy(materialDto, RdmsMaterialReturn.class);
            material.setId(null);
            material.setBomCode(originMaterial.getBomCode());
            material.setName(originMaterial.getName());
            material.setModel(originMaterial.getModel());
            material.setUnit(originMaterial.getUnit());
            material.setPreTaxUnitPrice(originMaterial.getPreTaxUnitPrice() == null ? BigDecimal.ZERO : originMaterial.getPreTaxUnitPrice());
            material.setCode(originMaterial.getCode());
            material.setOriginCode(material.getCode());
            material.setCode(materialManageDto.getCode());
            material.setPreTaxSumPrice(material.getPreTaxUnitPrice().multiply(BigDecimal.valueOf(material.getAmount())));
            material.setTokenCode(token_code);
            rdmsMaterialService.save_return(material);
            totalCost = totalCost.add(material.getPreTaxUnitPrice().multiply(BigDecimal.valueOf(material.getAmount())));
        }

        //保存申请表
        RdmsMaterialManageReturn materialManageReturn = CopyUtil.copy(materialManageDto, RdmsMaterialManageReturn.class);
        materialManageReturn.setUsageMode(originApplication.getUsageMode());
        materialManageReturn.setCapitalSource(originApplication.getCapitalSource());
        materialManageReturn.setJobType(rdmsJobItem.getType());
        if (materialManageDto.getProjectType().equals(ProjectTypeEnum.PRE_PROJECT.getType())) {
            materialManageReturn.setAuxStatus(JobItemTypeEnum.TASK_FUNCTION.getType());
            RdmsSysgmDto sysgmByCustomerId = rdmsSysgmService.getSysgmByCustomerId(materialManageDto.getCustomerId());
            if (!ObjectUtils.isEmpty(sysgmByCustomerId)) {
                materialManageReturn.setNextNode(sysgmByCustomerId.getSysgmId());
            } else {
                LOG.error("系统工程总监未设置. 发生位置:saveMaterialApplication_return方法 {}", this.getClass().getName());
                throw new BusinessException(BusinessExceptionCode.SYSGM_NOT_SET);
            }

        } else if (materialManageDto.getProjectType().equals(ProjectTypeEnum.PROJECT.getType())) {
            if (materialManageDto.getJobType().equals(JobItemTypeEnum.TASK_TEST.getType())) {
                materialManageReturn.setAuxStatus(JobItemTypeEnum.TASK_TEST.getType());
                materialManageReturn.setNextNode(rdmsJobItem.getProjectManagerId());

            } else {
                materialManageReturn.setAuxStatus(JobItemTypeEnum.TASK_PRODUCT.getType());
                materialManageReturn.setNextNode(rdmsJobItem.getProductManagerId());
            }
        } else {
            if (JobItemTypeEnum.QUALITY.getType().equals(rdmsJobItem.getType()) || JobItemTypeEnum.QUALITY.getType().equals(originApplication.getAuxStatus())) {
                materialManageReturn.setAuxStatus(JobItemTypeEnum.TASK_PRODUCT.getType());
                materialManageReturn.setNextNode(rdmsJobItem.getProductManagerId());
            } else if (JobItemTypeEnum.TASK_BUG.getType().equals(rdmsJobItem.getType()) || JobItemTypeEnum.TASK_BUG.getType().equals(originApplication.getAuxStatus())) {
                materialManageReturn.setAuxStatus(JobItemTypeEnum.TASK_BUG.getType());
                materialManageReturn.setNextNode(rdmsJobItem.getTestManagerId());
            } else {
                ValidatorUtil.require(materialManageDto.getProjectId(), "项目Id");
                materialManageReturn.setAuxStatus(JobItemTypeEnum.TASK_SUBP.getType());
                materialManageReturn.setNextNode(rdmsJobItem.getProjectManagerId());
            }
        }
        materialManageReturn.setApproverId(materialManageReturn.getNextNode());
        boolean engine = materialReturnFlowBridge.engineForNew(originApplication.getCustomerId());
        materialManageReturn.setId(null);
        materialManageReturn.setStatus(engine ? null : ApplicationStatusEnum.APPLICATION.getStatus());
        materialManageReturn.setOpeStatus(MaterialOpeStatusEnum.RETURN_APPLICATION.getStatus());
        materialManageReturn.setAccountCost(totalCost);
        materialManageReturn.setOriginCode(originApplication.getCode());  //原始单号
        rdmsMaterialManageService.save_return(materialManageReturn);

        //标记原始单据的操作状态
        RdmsMaterialManage originMark = new RdmsMaterialManage();
        originMark.setId(originApplication.getId());
        originMark.setOpeStatus(MaterialOpeStatusEnum.RETURN.getStatus());
        rdmsMaterialManageService.updateByPrimaryKeySelective(originMark);
        if (engine) {
            materialReturnFlowBridge.open(materialManageReturn.getCode(), materialManageReturn.getWriterId());
            responseDto.setContent(materialManageReturn.getId());
            responseDto.setMessage("保存成功");
            return responseDto;
        }

        //填写一条过程记录
        RdmsMaterialProcessReturn materialProcess = new RdmsMaterialProcessReturn();
        materialProcess.setMaterialId(materialManageReturn.getId());
        materialProcess.setExecutorId(returnOperatorId);
        materialProcess.setDeep(1);
        materialProcess.setDescription("提交物料退库申请");
        materialProcess.setNextNode(materialManageReturn.getNextNode());
        materialProcess.setSubmitTime(new Date());
        materialProcess.setStatus(ApplicationStatusEnum.APPLICATION.getStatus());
        rdmsMaterialProcessService.save_return(materialProcess);

        responseDto.setContent(materialManageReturn.getId());
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    @PostMapping("/saveMaterialListToBom")
    @Transactional
    public ResponseDto<String> saveMaterialListToBom(@RequestBody RdmsHmiMaterialToBomDto materialToBomDto) {
        ResponseDto<String> responseDto = new ResponseDto<>();
        if (!ObjectUtils.isEmpty(materialToBomDto) && !ObjectUtils.isEmpty(materialToBomDto.getMaterialDtoList())) {
            ValidatorUtil.require(materialToBomDto.getCharacterId(), "功能特性Id");
            rdmsBomGateService.assertMaterialEditable(materialToBomDto.getCharacterId(), null);
            for (RdmsMaterialDto materialDto : materialToBomDto.getMaterialDtoList()) {
                materialDto.setStatus("");
                RdmsBom bom = CopyUtil.copy(materialDto, RdmsBom.class);
                bom.setDeleted(0);
                bom.setStatus(BomStatusEnum.NORMAL.getStatus());
                bom.setCharacterId(materialToBomDto.getCharacterId());
                RdmsCharacter rdmsCharacter = rdmsCharacterService.selectByPrimaryKey(materialToBomDto.getCharacterId());
                if (!ObjectUtils.isEmpty(rdmsCharacter) && !ObjectUtils.isEmpty(rdmsCharacter.getSubprojectId())) {
                    bom.setSubprojectId(rdmsCharacter.getSubprojectId());
                }
                RdmsBom storedBom = ObjectUtils.isEmpty(bom.getId()) ? null : rdmsBomService.selectByPrimaryKey(bom.getId());
                if (storedBom != null) {
                    assertStoredParentTenant(storedBom.getCharacterId(), storedBom.getSubprojectId());
                }
                rdmsBomService.save(bom);

                RdmsMaterial storedMaterial = ObjectUtils.isEmpty(materialDto.getId()) ? null : rdmsMaterialService.selectByPrimaryKey(materialDto.getId());
                if (storedMaterial != null) {
                    tenantParentGuard.requireMaterialApplication(storedMaterial.getCode());
                }
                RdmsMaterial rdmsMaterial = new RdmsMaterial();
                rdmsMaterial.setId(materialDto.getId());
                rdmsMaterial.setInBom(1);
                rdmsMaterialService.updateByPrimaryKeySelective(rdmsMaterial);
            }
        }
        responseDto.setContent(materialToBomDto.getCharacterId());
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    @PostMapping("/saveBomTree")
    @Transactional
    public ResponseDto<String> saveBomTree(@RequestBody List<RdmsBomTreeDto> bomTreeDtoList) {
        ResponseDto<String> responseDto = new ResponseDto<>();
        if (!CollectionUtils.isEmpty(bomTreeDtoList)) {
            List<RdmsBomTreeDto> nodes = flattenBomTree(bomTreeDtoList, new ArrayList<>());
            nodes.stream().map(RdmsBomTreeDto::getSubprojectId).distinct().forEach(tenantParentGuard::requireSubproject);
            for (RdmsBomTreeDto node : nodes) {
                RdmsBomTree storedNode = ObjectUtils.isEmpty(node.getId()) ? null : rdmsBomTreeService.selectByPrimaryKey(node.getId());
                if (storedNode != null) {
                    assertStoredParentTenant(null, storedNode.getSubprojectId());
                }
            }
            rdmsBomTreeService.saveBomTree(bomTreeDtoList);
        }
        responseDto.setContent("");
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    @PostMapping("/saveBomTree_main")
    @Transactional
    public ResponseDto<String> saveBomTree_main(@RequestBody List<RdmsBomTreeDto> bomTreeDtoList) {
        ResponseDto<String> responseDto = new ResponseDto<>();
        if (!CollectionUtils.isEmpty(bomTreeDtoList)) {
            List<RdmsBomTreeDto> nodes = flattenBomTree(bomTreeDtoList, new ArrayList<>());
            nodes.stream().map(RdmsBomTreeDto::getProjectId).distinct().forEach(tenantParentGuard::requireProject);
            for (RdmsBomTreeDto node : nodes) {
                RdmsBomTreeMain storedNode = ObjectUtils.isEmpty(node.getId()) ? null : rdmsBomTreeService.selectByPrimaryKey_main(node.getId());
                if (storedNode != null) {
                    assertStoredParentTenant(null, storedNode.getProjectId());
                }
            }
            rdmsBomTreeService.saveBomTree_main(bomTreeDtoList);
        }
        responseDto.setContent("");
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    @PostMapping("/getBomTree/{subprojectId}")
    @Transactional
    public ResponseDto<RdmsBomTreeDto> getBomTree(@PathVariable String subprojectId) {
//        subprojectId = "GlSbePeV";
        ResponseDto<RdmsBomTreeDto> responseDto = new ResponseDto<>();
        tenantParentGuard.requireSubproject(subprojectId);
        List<RdmsBomTree> bomTreeList = rdmsBomTreeService.getBomTreeListBySubprojectId(subprojectId);
        List<RdmsBomTreeDto> rdmsBomTreeDtos = new ArrayList<>();
        if (!CollectionUtils.isEmpty(bomTreeList)) {
            for (RdmsBomTree bomTree : bomTreeList) {
                RdmsBomTreeDto bomTreeDto = CopyUtil.copy(bomTree, RdmsBomTreeDto.class);
                bomTreeDto.setAmount(bomTree.getAmount().toString());
                rdmsBomTreeDtos.add(bomTreeDto);
            }
        }
        RdmsBomTreeDto bomTree = rdmsBomTreeService.getBomTree(rdmsBomTreeDtos);

        responseDto.setContent(bomTree);
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    @PostMapping("/getBomTree_main/{projectId}")
    @Transactional
    public ResponseDto<RdmsBomTreeDto> getBomTree_main(@PathVariable String projectId) {
        ResponseDto<RdmsBomTreeDto> responseDto = new ResponseDto<>();
        tenantParentGuard.requireProject(projectId);
        List<RdmsBomTreeMain> bomTreeMainListByProjectId = rdmsBomTreeService.getBomTreeMainListByProjectId(projectId);
        List<RdmsBomTreeDto> rdmsBomTreeDtos = new ArrayList<>();
        if (!CollectionUtils.isEmpty(bomTreeMainListByProjectId)) {
            for (RdmsBomTreeMain bomTree : bomTreeMainListByProjectId) {
                RdmsBomTreeDto bomTreeDto = CopyUtil.copy(bomTree, RdmsBomTreeDto.class);
                bomTreeDto.setAmount(bomTree.getAmount().toString());
                rdmsBomTreeDtos.add(bomTreeDto);
            }
        }
        RdmsBomTreeDto bomTree = rdmsBomTreeService.getBomTree(rdmsBomTreeDtos);

        responseDto.setContent(bomTree);
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    @PostMapping("/synchronizationBomTree/{mainProjectId}")
    @Transactional
    public ResponseDto<Integer> synchronizationBomTree(@PathVariable String mainProjectId) {
        ResponseDto<Integer> responseDto = new ResponseDto<>();
        int count = 0;
        if (!ObjectUtils.isEmpty(mainProjectId)) {
            RdmsProject rdmsProject = rdmsProjectService.selectByPrimaryKey(mainProjectId);
            if (rdmsProject != null) {
                TenantAccess.assertSameTenant(rdmsProject.getCustomerId());
                List<String> subprojectIds = new ArrayList<>();
                for (RdmsProjectSubprojectDto subprojectDto : rdmsProjectService.getSubprojectListByProjectId(mainProjectId)) {
                    subprojectIds.add(subprojectDto.getId());
                }
                count = rdmsBomTreeService.rebuildMainTree(mainProjectId, subprojectIds);
            }
        }
        responseDto.setContent(count);
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    @PostMapping("/saveMaterialListToBom_subproject")
    @Transactional
    public ResponseDto<String> saveMaterialListToBom_subproject(@RequestBody RdmsHmiMaterialToBomDto materialToBomDto) {
        ResponseDto<String> responseDto = new ResponseDto<>();
        if (!ObjectUtils.isEmpty(materialToBomDto) && !ObjectUtils.isEmpty(materialToBomDto.getMaterialDtoList())) {
            ValidatorUtil.require(materialToBomDto.getSubprojectId(), "项目Id");
            rdmsBomGateService.assertMaterialEditable(null, materialToBomDto.getSubprojectId());
            for (RdmsMaterialDto materialDto : materialToBomDto.getMaterialDtoList()) {
                materialDto.setStatus("");
                RdmsBom bom = CopyUtil.copy(materialDto, RdmsBom.class);
                bom.setDeleted(0);
                bom.setStatus(BomStatusEnum.NORMAL.getStatus());
                bom.setSubprojectId(materialToBomDto.getSubprojectId());
                RdmsBom storedBom = ObjectUtils.isEmpty(bom.getId()) ? null : rdmsBomService.selectByPrimaryKey(bom.getId());
                if (storedBom != null) {
                    assertStoredParentTenant(storedBom.getCharacterId(), storedBom.getSubprojectId());
                }
                rdmsBomService.save(bom);

                RdmsMaterial storedMaterial = ObjectUtils.isEmpty(materialDto.getId()) ? null : rdmsMaterialService.selectByPrimaryKey(materialDto.getId());
                if (storedMaterial != null) {
                    tenantParentGuard.requireMaterialApplication(storedMaterial.getCode());
                }
                RdmsMaterial rdmsMaterial = new RdmsMaterial();
                rdmsMaterial.setId(materialDto.getId());
                rdmsMaterial.setInBom(1);
                rdmsMaterialService.updateByPrimaryKeySelective(rdmsMaterial);
            }
        }
        responseDto.setContent(materialToBomDto.getCharacterId());
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    @PostMapping("/submitBom/{characterId}")
    @Transactional
    public ResponseDto<String> submitBom(@PathVariable String characterId) {
        ResponseDto<String> responseDto = new ResponseDto<>();
        responseDto.setContent(rdmsBomGateService.submitCharacterBom(characterId));
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    @PostMapping("/submitSubProjectBom/{subprojectId}")
    @Transactional
    public ResponseDto<String> submitSubProjectBom(@PathVariable String subprojectId) {
        ResponseDto<String> responseDto = new ResponseDto<>();
        responseDto.setContent(rdmsBomGateService.submitSubprojectBom(subprojectId));
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    @PostMapping("/rejectSubProjectBom/{subprojectId}")
    @Transactional
    public ResponseDto<String> rejectSubProjectBom(@PathVariable String subprojectId,
                                                @RequestBody(required = false) RejectReasonDto rejectReasonDto) {
        ResponseDto<String> responseDto = new ResponseDto<>();
        String rejectReason = RejectReasonDto.reasonOf(rejectReasonDto);
        ValidatorUtil.length(rejectReason, "驳回理由", 0, RejectReasonDto.MAX_LENGTH);
        responseDto.setContent(rdmsBomGateService.rejectSubprojectBom(subprojectId, rejectReason));
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    @PostMapping("/rejectBom/{characterId}")
    @Transactional
    public ResponseDto<String> rejectBom(@PathVariable String characterId,
                                                @RequestBody(required = false) RejectReasonDto rejectReasonDto) {
        ResponseDto<String> responseDto = new ResponseDto<>();
        String rejectReason = RejectReasonDto.reasonOf(rejectReasonDto);
        ValidatorUtil.length(rejectReason, "驳回理由", 0, RejectReasonDto.MAX_LENGTH);
        responseDto.setContent(rdmsBomGateService.rejectCharacterBom(characterId, rejectReason));
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    @PostMapping("/saveBomItem")
    @Transactional
    public ResponseDto<String> saveBomItem(@RequestBody RdmsBomDto bomDto) {
        ResponseDto<String> responseDto = new ResponseDto<>();
        ValidatorUtil.require(bomDto.getName(), "物料名称");
        ValidatorUtil.require(bomDto.getBomCode(), "物料编号");
        ValidatorUtil.require(bomDto.getUnit(), "计量单位");
        ValidatorUtil.require(bomDto.getPreTaxUnitPrice(), "未税单价");
        ValidatorUtil.require(bomDto.getCharacterId(), "功能特性Id");
        ValidatorUtil.require(bomDto.getAmount(), "使用数量");
        ValidatorUtil.require(bomDto.getSupplierName(), "供应商");

        ValidatorUtil.length(bomDto.getName(), "物料名称", 2, 80);
        ValidatorUtil.length(bomDto.getBomCode(), "物料编号", 2, 50);
        ValidatorUtil.length(bomDto.getModel(), "物料型号", 1, 50);
        ValidatorUtil.length(bomDto.getUnit(), "计量单位", 1, 10);
        ValidatorUtil.length(bomDto.getSupplierName(), "供应商", 2, 100);

        rdmsBomGateService.assertMaterialLine(bomDto.getId());
        rdmsBomGateService.assertMaterialWritable(bomDto.getId(), bomDto.getCharacterId(), null);
        RdmsBom bom = CopyUtil.copy(bomDto, RdmsBom.class);

        bom.setPreTaxSumPrice(bom.getPreTaxUnitPrice().multiply(BigDecimal.valueOf(bom.getAmount())));
        bom.setDeleted(0);
        bom.setStatus(BomStatusEnum.NORMAL.getStatus());

        RdmsCharacter rdmsCharacter = rdmsCharacterService.selectByPrimaryKey(bomDto.getCharacterId());
        if (!ObjectUtils.isEmpty(rdmsCharacter) && !ObjectUtils.isEmpty(rdmsCharacter.getSubprojectId())) {
            bom.setSubprojectId(rdmsCharacter.getSubprojectId());
        }
        rdmsBomService.save(bom);

        responseDto.setContent(bom.getId());
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    @PostMapping("/saveEditedBomItem")
    @Transactional
    public ResponseDto<String> saveEditedBomItem(@RequestBody RdmsBomDto bomDto) {
        ResponseDto<String> responseDto = new ResponseDto<>();
        ValidatorUtil.require(bomDto.getName(), "物料名称");
        ValidatorUtil.require(bomDto.getBomCode(), "物料编号");
        ValidatorUtil.require(bomDto.getUnit(), "计量单位");
        ValidatorUtil.require(bomDto.getPreTaxUnitPrice(), "未税单价");
        ValidatorUtil.require(bomDto.getAmount(), "使用数量");
        ValidatorUtil.require(bomDto.getSupplierName(), "供应商");

        ValidatorUtil.length(bomDto.getName(), "物料名称", 2, 80);
        ValidatorUtil.length(bomDto.getBomCode(), "物料编号", 2, 50);
        ValidatorUtil.length(bomDto.getModel(), "物料型号", 1, 50);
        ValidatorUtil.length(bomDto.getUnit(), "计量单位", 1, 10);
        ValidatorUtil.length(bomDto.getSupplierName(), "供应商", 2, 100);

        rdmsBomGateService.assertMaterialLine(bomDto.getId());
        rdmsBomGateService.assertMaterialWritable(bomDto.getId(), bomDto.getCharacterId(), bomDto.getSubprojectId());
        RdmsBom bom = CopyUtil.copy(bomDto, RdmsBom.class);
        bom.setPreTaxSumPrice(bom.getPreTaxUnitPrice().multiply(BigDecimal.valueOf(bom.getAmount())));
        rdmsBomService.save(bom);

        responseDto.setContent(bom.getId());
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    @PostMapping("/saveBomItem_subproject")
    @Transactional
    public ResponseDto<String> saveBomItem_subproject(@RequestBody RdmsBomDto bomDto) {
        ResponseDto<String> responseDto = new ResponseDto<>();
        ValidatorUtil.require(bomDto.getName(), "物料名称");
        ValidatorUtil.require(bomDto.getBomCode(), "物料编号");
        ValidatorUtil.require(bomDto.getUnit(), "计量单位");
        ValidatorUtil.require(bomDto.getPreTaxUnitPrice(), "未税单价");
        ValidatorUtil.require(bomDto.getSubprojectId(), "项目Id");
        ValidatorUtil.require(bomDto.getAmount(), "使用数量");
        ValidatorUtil.require(bomDto.getSupplierName(), "供应商");

        ValidatorUtil.length(bomDto.getName(), "物料名称", 2, 80);
        ValidatorUtil.length(bomDto.getBomCode(), "物料编号", 2, 50);
        ValidatorUtil.length(bomDto.getModel(), "物料型号", 1, 50);
        ValidatorUtil.length(bomDto.getUnit(), "计量单位", 1, 10);
        ValidatorUtil.length(bomDto.getSupplierName(), "供应商", 2, 100);

        rdmsBomGateService.assertMaterialLine(bomDto.getId());
        rdmsBomGateService.assertMaterialWritable(bomDto.getId(), null, bomDto.getSubprojectId());
        RdmsBom bom = CopyUtil.copy(bomDto, RdmsBom.class);

        bom.setPreTaxSumPrice(bom.getPreTaxUnitPrice().multiply(BigDecimal.valueOf(bom.getAmount())));
        bom.setDeleted(0);
        bom.setStatus(BomStatusEnum.NORMAL.getStatus());
//        bom.setSubprojectId(bomDto.getSubprojectId());

        rdmsBomService.save(bom);

        responseDto.setContent(bom.getId());
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    @PostMapping("/saveBomItem_subproject_import")
    @Transactional
    public ResponseDto<String> saveBomItem_subproject_import(@RequestBody List<RdmsBomDto> bomDtoList) {
        ResponseDto<String> responseDto = new ResponseDto<>();
        if (!CollectionUtils.isEmpty(bomDtoList)) {
            for (RdmsBomDto bomDto : bomDtoList) {
                ValidatorUtil.require(bomDto.getName(), "物料名称");
                ValidatorUtil.require(bomDto.getBomCode(), "物料编号");
                ValidatorUtil.require(bomDto.getUnit(), "计量单位");
                ValidatorUtil.require(bomDto.getPreTaxUnitPrice(), "未税单价");
                ValidatorUtil.require(bomDto.getSubprojectId(), "项目Id");
                ValidatorUtil.require(bomDto.getAmount(), "使用数量");
                ValidatorUtil.require(bomDto.getSupplierName(), "供应商");

                ValidatorUtil.length(bomDto.getName(), "物料名称", 2, 80);
                ValidatorUtil.length(bomDto.getBomCode(), "物料编号", 2, 50);
                ValidatorUtil.length(bomDto.getModel(), "物料型号", 1, 50);
                ValidatorUtil.length(bomDto.getUnit(), "计量单位", 1, 10);
                ValidatorUtil.length(bomDto.getSupplierName(), "供应商", 2, 100);

                rdmsBomGateService.assertMaterialLine(bomDto.getId());
                rdmsBomGateService.assertMaterialWritable(bomDto.getId(), null, bomDto.getSubprojectId());
                RdmsBom bom = CopyUtil.copy(bomDto, RdmsBom.class);

                bom.setPreTaxSumPrice(bom.getPreTaxUnitPrice().multiply(BigDecimal.valueOf(bom.getAmount())));
                bom.setDeleted(0);
                bom.setStatus(BomStatusEnum.NORMAL.getStatus());

                rdmsBomService.save(bom);
            }
        }

        responseDto.setContent("");
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    /**
     * 保存客户信息
     */
    @PostMapping("/approveMaterialApplication")
    @Transactional
    public ResponseDto<String> approveMaterialApplication(@RequestBody RdmsMaterialManageDto materialManageDto) {
        ResponseDto<String> responseDto = new ResponseDto<>();
        ValidatorUtil.require(materialManageDto.getCode(), "申请单号");
        ValidatorUtil.require(materialManageDto.getApproveDescription(), "审批意见");
        ValidatorUtil.require(materialManageDto.getCapitalSource(), "资金来源");
        if (CapitalSourceEnum.getCapitalSourceEnumBySource(materialManageDto.getCapitalSource()) == null) {
            throw new BusinessException(BusinessExceptionCode.INPUT_ERROR, "资金来源取值错误");
        }
        ValidatorUtil.length(materialManageDto.getApproveDescription(), "审批描述", 2, 500);
        ValidatorUtil.require(materialManageDto.getApproveStatus(), "审批结论");

        String approveStatus = materialManageDto.getApproveStatus();
        String requiredStatus;
        if (ApplicationStatusEnum.materialApplicationConclusions().contains(approveStatus)) {
            requiredStatus = ApplicationStatusEnum.APPLICATION.getStatus();
        } else if (ApplicationStatusEnum.materialSubmitConclusions().contains(approveStatus)) {
            requiredStatus = ApplicationStatusEnum.SUBMIT.getStatus();
        } else {
            throw new BusinessException(BusinessExceptionCode.STATUS_VALUE_INVALID);
        }
        RdmsMaterialManageDto current = rdmsMaterialManageService.getMaterialApplicationByCodeMix(materialManageDto.getCode());
        if (!requiredStatus.equals(current.getStatus())) {
            throw new BusinessException(BusinessExceptionCode.LIFECYCLE_TRANSITION_FORBIDDEN);
        }
        String operatorId = this.resolveOperatorId(materialManageDto.getLoginUserId());
        String expectedOperatorId = current.getNextNode();
        if (ApplicationStatusEnum.SUBMIT.getStatus().equals(current.getStatus()) && Objects.equals(expectedOperatorId, current.getWriterId())) {
            expectedOperatorId = current.getApproverId();
        }
        if (!operatorId.equals(expectedOperatorId)) {
            throw new BusinessException(BusinessExceptionCode.NO_PERMISSION);
        }
        ValidatorUtil.listNotEmpty(current.getMaterialList(), "物料清单");

        boolean returning = rdmsMaterialManageService.getMaterialManageSimpleInfoByCode_origin(current.getCode()) == null;
        boolean firstApproval = ApplicationStatusEnum.APPLICATION.getStatus().equals(current.getStatus());
        boolean rejected = ApplicationStatusEnum.REJECT.getStatus().equals(approveStatus);
        boolean supplement = ApplicationStatusEnum.SUPPLEMENT.getStatus().equals(approveStatus);
        String currentOpeStatus = current.getOpeStatus();
        String capitalSource = (!returning && firstApproval) || ObjectUtils.isEmpty(current.getCapitalSource())
                ? materialManageDto.getCapitalSource() : current.getCapitalSource();
        BigDecimal accountCost = this.applyMaterialLines(current, approveStatus, returning);
        String flowCustomerId = TenantContext.getTenantId();
        if (!StringUtils.hasText(flowCustomerId)) {
            flowCustomerId = current.getCustomerId();
        }

        if (returning && materialReturnFlowBridge.engineForExisting(flowCustomerId, current.getCode())) {
            RdmsMaterialManageReturn patch = new RdmsMaterialManageReturn();
            patch.setId(current.getId());
            patch.setApproveDescription(materialManageDto.getApproveDescription());
            patch.setCapitalSource(capitalSource);
            patch.setApproveTime(new Date());
            rdmsMaterialManageService.updateByPrimaryKeySelective_return(patch);
            materialReturnFlowBridge.approve(current.getCode(), operatorId, approveStatus);
            responseDto.setContent(current.getId());
            responseDto.setMessage("保存成功");
            return responseDto;
        }

        RdmsMaterialManageDto stockDto = new RdmsMaterialManageDto();
        stockDto.setCustomerId(current.getCustomerId());
        stockDto.setWarehouseCode(current.getWarehouseCode());
        stockDto.setUsageMode(current.getUsageMode());
        stockDto.setOpeStatus(returning ? MaterialOpeStatusEnum.RETURN_APPLICATION.getStatus() : null);
        stockDto.setCode(current.getCode());
        stockDto.setApproverId(operatorId);
        stockDto.setApproveStatus(approveStatus);
        stockDto.setMaterialList(current.getMaterialList());
        if (returning || !materialFlowBridge.requiresIssue(flowCustomerId, current.getCode())) {
            rdmsMaterialStockService.apply(stockDto);
        }

        if (returning) {
            //退库申请审批: 请求只取结论、审批意见, 其余以库内记录为准
            RdmsMaterialManageReturn materialManage = CopyUtil.copy(current, RdmsMaterialManageReturn.class);
            String executorId = operatorId;
            materialManage.setNextNode(current.getWriterId());  //提交之后, 出现在谁的页面, 那么, 下一节点就是谁
            materialManage.setStatus(approveStatus);
            materialManage.setApproveDescription(materialManageDto.getApproveDescription());
            materialManage.setCapitalSource(capitalSource);
            materialManage.setApproveTime(new Date());
            if (MaterialOpeStatusEnum.RETURN_APPROVED.getStatus().equals(currentOpeStatus) && !supplement) {
                materialManage.setOpeStatus(MaterialOpeStatusEnum.RETURN_COMPLETE.getStatus());
            } else if (MaterialOpeStatusEnum.RETURN_APPROVED.getStatus().equals(currentOpeStatus)) {
                materialManage.setOpeStatus(MaterialOpeStatusEnum.RETURN_APPROVED.getStatus());
            } else if (rejected) {
                materialManage.setOpeStatus(MaterialOpeStatusEnum.RETURN_COMPLETE.getStatus());
            } else {
                materialManage.setOpeStatus(MaterialOpeStatusEnum.RETURN_APPROVED.getStatus());
            }
            rdmsMaterialManageService.updateByPrimaryKeySelective_return(materialManage);
            RdmsMaterialManageDto returnDto = CopyUtil.copy(materialManage, RdmsMaterialManageDto.class);

            String processText;
            if (rejected) {
                applicationBudgetExeWriter.removeMaterialUnpaidExe(materialManage.getCode());
                rdmsMaterialReturnService.releaseOrigin(returnDto);
                processText = "退料申请被驳回!";
            } else if (supplement) {
                processText = "退料退回增补材料!";
            } else {
                if (MaterialOpeStatusEnum.RETURN_APPLICATION.getStatus().equals(currentOpeStatus)) {
                    rdmsMaterialReturnService.settleOrigin(returnDto);
                }
                if (!MaterialUsageModeEnum.BORROW.getStatus().equals(materialManage.getUsageMode())) {
                    returnDto.setApproveStatus(approveStatus);
                    applicationBudgetExeWriter.writeMaterialApprove(
                            returnDto,
                            CopyUtil.copy(materialManage, RdmsMaterialManage.class),
                            true);
                }
                processText = "主管经理审批完成";
            }

            RdmsMaterialProcessReturn materialProcess = new RdmsMaterialProcessReturn();
            materialProcess.setMaterialId(materialManage.getId());
            materialProcess.setExecutorId(executorId);
            long materialProcessCount = rdmsMaterialProcessService.getMaterialProcessCount(materialManage.getId());
            materialProcess.setDeep((int) materialProcessCount);
            materialProcess.setDescription(processText);
            materialProcess.setNextNode(materialManage.getNextNode());
            materialProcess.setSubmitTime(new Date());
            materialProcess.setStatus(approveStatus);
            rdmsMaterialProcessService.save_return(materialProcess);
            responseDto.setContent(materialManage.getId());

        } else {
            if (materialFlowBridge.engineForExisting(flowCustomerId, current.getCode())) {
                RdmsMaterialManage patch = new RdmsMaterialManage();
                patch.setId(current.getId());
                patch.setApproveDescription(materialManageDto.getApproveDescription());
                patch.setCapitalSource(capitalSource);
                patch.setAccountCost(accountCost);
                patch.setApproveTime(new Date());
                rdmsMaterialManageService.updateByPrimaryKeySelective(patch);
                materialFlowBridge.approve(current.getCode(), operatorId, approveStatus);
                responseDto.setContent(current.getId());
                responseDto.setMessage("保存成功");
                return responseDto;
            }
            //处理领料的情况: 明细与记账金额以库内记录为准
            RdmsMaterialManage materialManage = CopyUtil.copy(current, RdmsMaterialManage.class);
            String executorId = operatorId;
            materialManage.setNextNode(current.getWriterId());  //提交之后, 出现在谁的页面, 那么, 下一节点就是谁
            materialManage.setStatus(approveStatus);
            materialManage.setApproveDescription(materialManageDto.getApproveDescription());
            materialManage.setCapitalSource(capitalSource);
            materialManage.setAccountCost(accountCost);
            materialManage.setApproveTime(new Date());
            rdmsMaterialManageService.updateByPrimaryKeySelective(materialManage);

            //驳回撤销未支付流水; 退回增补不写流水, 保留批准时的未支付流水
            if (rejected) {
                applicationBudgetExeWriter.removeMaterialUnpaidExe(materialManage.getCode());
            } else if (!supplement && !MaterialUsageModeEnum.BORROW.getStatus().equals(materialManage.getUsageMode())) {
                RdmsMaterialManageDto exeDto = CopyUtil.copy(materialManage, RdmsMaterialManageDto.class);
                exeDto.setJobType(current.getJobType());
                exeDto.setApproveStatus(approveStatus);
                applicationBudgetExeWriter.writeMaterialApprove(exeDto, materialManage, false);
            }

            //填写一条过程记录
            RdmsMaterialProcess materialProcess = new RdmsMaterialProcess();
            materialProcess.setMaterialId(materialManage.getId());
            materialProcess.setExecutorId(executorId);
            long materialProcessCount = rdmsMaterialProcessService.getMaterialProcessCount(materialManage.getId());
            materialProcess.setDeep((int) materialProcessCount);
            materialProcess.setDescription("主管经理审批完成");
            materialProcess.setNextNode(materialManage.getNextNode());
            materialProcess.setSubmitTime(new Date());
            materialProcess.setStatus(approveStatus);
            rdmsMaterialProcessService.save(materialProcess);

            responseDto.setContent(materialManage.getId());
        }

        responseDto.setMessage("保存成功");
        return responseDto;
    }

    @PostMapping("/approveMaterialApplication-sap")
    @Transactional
    public ResponseDto<String> approveMaterialApplicationBySap(@RequestBody RdmsMaterialManageDto materialManageDto) {
        return approveMaterialApplication(materialManageDto);
    }

    /** 发料确认：物料管理员按批准的明细发料扣库存，或退回申请人。最终管理人可代办。 */
    @PostMapping("/issueMaterialApplication")
    @Transactional
    public ResponseDto<String> issueMaterialApplication(@RequestBody RdmsMaterialManageDto materialManageDto) {
        ValidatorUtil.require(materialManageDto.getCode(), "申请单号");
        ValidatorUtil.require(materialManageDto.getIssueResult(), "发料结论");
        if (!ApplicationStatusEnum.materialIssueConclusions().contains(materialManageDto.getIssueResult())) {
            throw new BusinessException(BusinessExceptionCode.STATUS_VALUE_INVALID);
        }
        RdmsMaterialManageDto current = rdmsMaterialManageService.getMaterialManageSimpleInfoByCode_origin(materialManageDto.getCode());
        if (current == null) {
            throw new BusinessException(BusinessExceptionCode.DB_RECORD_NOT_EXIST);
        }
        TenantAccess.assertSameTenant(current.getCustomerId());
        if (!ApplicationStatusEnum.materialIssueSources().contains(current.getStatus())
                || !current.isPendingIssue()) {
            throw new BusinessException(BusinessExceptionCode.LIFECYCLE_TRANSITION_FORBIDDEN);
        }
        String operatorId = this.resolveOperatorId(materialManageDto.getLoginUserId());
        boolean proxy = !operatorId.equals(current.getNextNode());
        if (proxy && !rdmsMaterialManageService.isMaterialSupervisor(current.getCustomerId(), operatorId)) {
            throw new BusinessException(BusinessExceptionCode.NO_PERMISSION);
        }
        ValidatorUtil.require(materialManageDto.getIssueDescription(), "发料说明");
        // 流程过程记录 description 为 varchar(200)，需给“物料管理员发料确认：”前缀留出余量
        ValidatorUtil.length(materialManageDto.getIssueDescription(), "发料说明", 2, 150);

        materialFlowBridge.issue(current.getCode(), operatorId, materialManageDto.getIssueResult(),
                materialManageDto.getIssueDescription(), proxy);
        ResponseDto<String> responseDto = new ResponseDto<>();
        responseDto.setContent(current.getId());
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    /** 退料收料确认：物料管理员按退料明细收料入库，或驳回退料。最终管理人可代办。结论与说明沿用 issueResult、issueDescription。 */
    @PostMapping("/receiveReturnApplication")
    @Transactional
    public ResponseDto<String> receiveReturnApplication(@RequestBody RdmsMaterialManageDto materialManageDto) {
        ValidatorUtil.require(materialManageDto.getCode(), "退料单号");
        ValidatorUtil.require(materialManageDto.getIssueResult(), "收料结论");
        if (!ApplicationStatusEnum.materialReceiveConclusions().contains(materialManageDto.getIssueResult())) {
            throw new BusinessException(BusinessExceptionCode.STATUS_VALUE_INVALID);
        }
        RdmsMaterialManageReturn stored = rdmsMaterialManageService.getMaterialManageSimpleInfoByCode_return(materialManageDto.getCode());
        if (stored == null) {
            throw new BusinessException(BusinessExceptionCode.DB_RECORD_NOT_EXIST);
        }
        TenantAccess.assertSameTenant(stored.getCustomerId());
        RdmsMaterialManageDto current = CopyUtil.copy(stored, RdmsMaterialManageDto.class);
        if (!ApplicationStatusEnum.materialIssueSources().contains(current.getStatus())
                || !current.isPendingIssue()
                || !materialReturnFlowBridge.engineForExisting(current.getCustomerId(), current.getCode())) {
            throw new BusinessException(BusinessExceptionCode.LIFECYCLE_TRANSITION_FORBIDDEN);
        }
        String operatorId = this.resolveOperatorId(materialManageDto.getLoginUserId());
        boolean proxy = !operatorId.equals(current.getNextNode());
        if (proxy && !rdmsMaterialManageService.isMaterialSupervisor(current.getCustomerId(), operatorId)) {
            throw new BusinessException(BusinessExceptionCode.NO_PERMISSION);
        }
        ValidatorUtil.require(materialManageDto.getIssueDescription(), "收料说明");
        // 流程过程记录 description 为 varchar(200)，需给“物料管理员收料确认：”前缀留出余量
        ValidatorUtil.length(materialManageDto.getIssueDescription(), "收料说明", 2, 150);

        materialReturnFlowBridge.receive(current.getCode(), operatorId, materialManageDto.getIssueResult(),
                materialManageDto.getIssueDescription(), proxy);
        ResponseDto<String> responseDto = new ResponseDto<>();
        responseDto.setContent(current.getId());
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    @PostMapping("/listMaterialsByCharacterId")
    public ResponseDto<PageDto<RdmsMaterialManageDto>> listMaterialsByCharacterId(@RequestBody PageDto<RdmsMaterialManageDto> pageDto) {
        ResponseDto<PageDto<RdmsMaterialManageDto>> responseDto = new ResponseDto<>();
        rdmsMaterialManageService.listByCharacterId(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

    @PostMapping("/getQualityMaterialApplicationNumByUserId/{userId}")
    public ResponseDto<Long> getQualityMaterialApplicationNumByUserId(@PathVariable String userId) {
        userId = TenantAccess.resolveMemberId(userId);
        ResponseDto<Long> responseDto = new ResponseDto<>();
        Long applicationNumByUserId = rdmsMaterialManageService.getQualityMaterialApplicationNumByUserId(userId);
        responseDto.setContent(applicationNumByUserId);
        return responseDto;
    }

    @PostMapping("/getListByCharacterId/{characterId}")
    public ResponseDto<List<RdmsMaterialManageDto>> getListByCharacterId(@PathVariable String characterId) {
        ResponseDto<List<RdmsMaterialManageDto>> responseDto = new ResponseDto<>();
        List<RdmsMaterialManageDto> listByCharacterId = rdmsMaterialManageService.getListByCharacterId(characterId, false);
        responseDto.setContent(listByCharacterId);
        return responseDto;
    }

    @PostMapping("/getListByNextNodeIdForApprover/{loginUser}")
    public ResponseDto<List<RdmsMaterialManageDto>> getListByNextNodeIdForApprover(@PathVariable String loginUser) {
        loginUser = TenantAccess.resolveMemberId(loginUser);
        ResponseDto<List<RdmsMaterialManageDto>> responseDto = new ResponseDto<>();
        String membershipId = TenantContext.getMembershipId();
        String viewerId = StringUtils.hasText(membershipId) ? membershipId : loginUser;
        responseDto.setContent(rdmsMaterialManageService.getApproverViewList(viewerId));
        return responseDto;
    }

    @PostMapping("/getListByQualityId/{qualityId}")
    public ResponseDto<List<RdmsMaterialManageDto>> getListByQualityId(@PathVariable String qualityId) {
        ResponseDto<List<RdmsMaterialManageDto>> responseDto = new ResponseDto<>();
        List<RdmsMaterialManageDto> listByQualityId = rdmsMaterialManageService.getListByQualityId(qualityId);
        responseDto.setContent(listByQualityId);
        return responseDto;
    }

    @PostMapping("/getListBySubprojectId/{subprojectId}")
    public ResponseDto<List<RdmsMaterialManageDto>> getListBySubprojectId(@PathVariable String subprojectId) {
        ResponseDto<List<RdmsMaterialManageDto>> responseDto = new ResponseDto<>();
        List<RdmsMaterialManageDto> listByJobitemId = rdmsMaterialManageService.getTaskJobMaterialListBySubprojectId(subprojectId);
        responseDto.setContent(listByJobitemId);
        return responseDto;
    }

    @PostMapping("/getMaterialListByApplicationCode/{code}")
    public ResponseDto<List<RdmsMaterialDto>> getMaterialListByApplicationCode(@PathVariable String code) {
        ResponseDto<List<RdmsMaterialDto>> responseDto = new ResponseDto<>();
        tenantParentGuard.requireMaterialApplication(code);
        List<RdmsMaterialDto> materialListByApplicationCode = rdmsMaterialService.getMaterialListByApplicationCode(code);
        responseDto.setContent(materialListByApplicationCode);
        return responseDto;
    }

    @PostMapping("/getListByPreProjectId/{preprojectId}")
    public ResponseDto<List<RdmsMaterialManageDto>> getListByPreProjectId(@PathVariable String preprojectId) {
        ResponseDto<List<RdmsMaterialManageDto>> responseDto = new ResponseDto<>();
        List<RdmsMaterialManageDto> listByJobitemId = rdmsMaterialManageService.getTaskJobMaterialListByPreProjectId(preprojectId);
        responseDto.setContent(listByJobitemId);
        return responseDto;
    }

    @PostMapping("/getListByProjectId/{projectId}")
    public ResponseDto<List<RdmsMaterialManageDto>> getListByProjectId(@PathVariable String projectId) {
        ResponseDto<List<RdmsMaterialManageDto>> responseDto = new ResponseDto<>();
        List<RdmsMaterialManageDto> listByJobitemId = rdmsMaterialManageService.getTaskJobMaterialListByProjectId(projectId);
        responseDto.setContent(listByJobitemId);
        return responseDto;
    }

    @PostMapping("/getBomMaterialListByCharacterId/{characterId}")
    public ResponseDto<List<RdmsBomDto>> getBomMaterialListByCharacterId(@PathVariable String characterId) {
        ResponseDto<List<RdmsBomDto>> responseDto = new ResponseDto<>();
        tenantParentGuard.requireCharacter(characterId);
        List<RdmsBomDto> bomMaterialListByCharacterId = rdmsMaterialService.getBomMaterialListByCharacterId(characterId);
        responseDto.setContent(bomMaterialListByCharacterId);
        return responseDto;
    }

    /** 物料清单的各版快照，ownerType 为 CHARACTER 或 SUBPROJECT；不含清单行。 */
    @GetMapping("/bom-snapshot/list/{ownerType}/{ownerId}")
    public ResponseDto<List<Map<String, Object>>> listBomSnapshots(@PathVariable String ownerType, @PathVariable String ownerId) {
        ResponseDto<List<Map<String, Object>>> responseDto = new ResponseDto<>();
        responseDto.setContent(bomSnapshotService.listForOwner(ownerType, ownerId));
        return responseDto;
    }

    @GetMapping("/bom-snapshot/get/{id}")
    public ResponseDto<Map<String, Object>> getBomSnapshot(@PathVariable String id) {
        ResponseDto<Map<String, Object>> responseDto = new ResponseDto<>();
        responseDto.setContent(bomSnapshotService.get(id));
        return responseDto;
    }

    @PostMapping("/stock/list/{customerId}")
    public ResponseDto<List<RdmsMaterialStockDto>> listStock(@PathVariable String customerId) {
        ResponseDto<List<RdmsMaterialStockDto>> responseDto = new ResponseDto<>();
        String tenantId = TenantAccess.resolveCustomerId(customerId);
        responseDto.setContent(rdmsMaterialStockService.listView(tenantId));
        return responseDto;
    }

    /** 退料选择目标仓库：本机构启用仓库 */
    @PostMapping("/active-warehouses/{customerId}")
    public ResponseDto<List<RdmsMaterialWarehouse>> listActiveWarehouses(@PathVariable String customerId) {
        ResponseDto<List<RdmsMaterialWarehouse>> responseDto = new ResponseDto<>();
        String tenantId = TenantAccess.resolveCustomerId(customerId);
        List<RdmsMaterialWarehouse> rows = new ArrayList<>();
        for (RdmsMaterialWarehouse warehouse : rdmsMaterialWarehouseService.list(tenantId)) {
            if (MaterialWarehouseStatusEnum.ACTIVE.getStatus().equals(warehouse.getStatus())) {
                rows.add(warehouse);
            }
        }
        responseDto.setContent(rows);
        return responseDto;
    }

    /** 领料选料：只列启用仓库中数量大于 0 的物料 */
    @PostMapping("/getSAPMaterialList/{customerId}")
    public ResponseDto<List<RdmsSAPMaterialDto>> getSAPMaterialList(@PathVariable String customerId) {
        ResponseDto<List<RdmsSAPMaterialDto>> responseDto = new ResponseDto<>();
        String tenantId = TenantAccess.resolveCustomerId(customerId);
        responseDto.setContent(toSapDtos(rdmsMaterialStockService.list(tenantId), activeWarehouseNames(tenantId)));
        return responseDto;
    }

    @PostMapping("/getSAPMaterialByBomCodeAndBatchCode")
    public ResponseDto<List<RdmsSAPMaterialDto>> getSAPMaterialByBomCodeAndBatchCode(@RequestParam String bomCode, String warehouseCode, String customerId) {
        ResponseDto<List<RdmsSAPMaterialDto>> responseDto = new ResponseDto<>();
        String tenantId = TenantAccess.resolveCustomerId(customerId);
        RdmsMaterialStock stock = rdmsMaterialStockService.find(tenantId, warehouseCode, bomCode);
        responseDto.setContent(stock == null ? new ArrayList<>()
                : toSapDtos(java.util.Collections.singletonList(stock), activeWarehouseNames(tenantId)));
        return responseDto;
    }

    @PostMapping("/getBomMaterialListBySubprojectId/{subprojectId}")
    public ResponseDto<List<RdmsBomDto>> getBomMaterialListBySubprojectId(@PathVariable String subprojectId) {
        ResponseDto<List<RdmsBomDto>> responseDto = new ResponseDto<>();
        tenantParentGuard.requireSubproject(subprojectId);
        List<RdmsBomDto> bomMaterialListByCharacterId = rdmsMaterialService.getBomMaterialListBySubprojectId(subprojectId);
        responseDto.setContent(bomMaterialListByCharacterId);
        return responseDto;
    }

    @PostMapping("/getCommonBomMaterialListByProjectId/{projectId}")
    public ResponseDto<List<RdmsBomDto>> getCommonBomMaterialListByProjectId(@PathVariable String projectId) {
        ResponseDto<List<RdmsBomDto>> responseDto = new ResponseDto<>();
        tenantParentGuard.requireProject(projectId);
        List<RdmsBomDto> bomMaterialListByCharacterId = rdmsMaterialService.getCommonBomMaterialListByProjectId(projectId);
        responseDto.setContent(bomMaterialListByCharacterId);
        return responseDto;
    }

    @PostMapping("/getArchivedCharacterListByProjectId/{projectId}")
    public ResponseDto<List<RdmsCharacterDto>> getArchivedCharacterListByProjectId(@PathVariable String projectId) {
        ResponseDto<List<RdmsCharacterDto>> responseDto = new ResponseDto<>();
        List<RdmsCharacterDto> archivedCharacterListByProjectId = rdmsMaterialService.getArchivedCharacterListByProjectId(projectId);
        responseDto.setContent(archivedCharacterListByProjectId);
        return responseDto;
    }

    @PostMapping("/getBomMaterialListByProjectId/{projectId}")
    public ResponseDto<List<RdmsBomDto>> getBomMaterialListByProjectId(@PathVariable String projectId) {
        ResponseDto<List<RdmsBomDto>> responseDto = new ResponseDto<>();
        tenantParentGuard.requireProject(projectId);
        List<RdmsBomDto> bomMaterialListByProjectId = rdmsMaterialService.getBomMaterialListByProjectId(projectId);
        responseDto.setContent(bomMaterialListByProjectId);
        return responseDto;
    }

    @PostMapping("/getBomMaterialListByCharacterIdList")
    public ResponseDto<List<RdmsBomDto>> getBomMaterialListByCharacterIdList(@RequestBody List<String> characterIdList) {
        ResponseDto<List<RdmsBomDto>> responseDto = new ResponseDto<>();
        if (!CollectionUtils.isEmpty(characterIdList)) {
            characterIdList.forEach(tenantParentGuard::requireCharacter);
        }
        List<RdmsBomDto> bomMaterialList = rdmsMaterialService.getBomMaterialListByCharacterIdList(characterIdList);
        responseDto.setContent(bomMaterialList);
        return responseDto;
    }

    @PostMapping("/getMaterialListByProjectId/{projectId}")
    public ResponseDto<RdmsBomConfigDto> getMaterialListByProjectId(@PathVariable String projectId) {
        ResponseDto<RdmsBomConfigDto> responseDto = new ResponseDto<>();
        tenantParentGuard.requireProject(projectId);
        RdmsBomConfigDto materialListByProjectId = rdmsMaterialService.getMaterialListByProjectId(projectId);
        responseDto.setContent(materialListByProjectId);
        return responseDto;
    }

    @PostMapping("/getMaterialManageItemByCode/{code}")
    public ResponseDto<RdmsMaterialManageDto> getMaterialManageItemByCode(@PathVariable String code) {
        ResponseDto<RdmsMaterialManageDto> responseDto = new ResponseDto<>();
        tenantParentGuard.requireMaterialApplication(code);
        RdmsMaterialManageDto materialApplicationByCode = rdmsMaterialManageService.getMaterialApplicationByCode_origin(code);
        responseDto.setContent(materialApplicationByCode);
        return responseDto;
    }

    @PostMapping("/getMaterialListById/{bomConfigId}")
    public ResponseDto<RdmsBomConfigDto> getMaterialListById(@PathVariable String bomConfigId) {
        ResponseDto<RdmsBomConfigDto> responseDto = new ResponseDto<>();
        RdmsBomConfig bomConfig = StringUtils.hasText(bomConfigId) ? rdmsBomConfigService.selectByPrimaryKey(bomConfigId) : null;
        if (bomConfig == null) {
            throw new BusinessException(BusinessExceptionCode.DB_RECORD_NOT_EXIST);
        }
        tenantParentGuard.requireProject(bomConfig.getProjectId());
        RdmsBomConfigDto materialList = rdmsMaterialService.getMaterialListById(bomConfigId);
        responseDto.setContent(materialList);
        return responseDto;
    }

    @PostMapping("/getCapitalizationListByCharacterId/{characterId}")
    public ResponseDto<List<RdmsMaterialDto>> getCapitalizationListByCharacterId(@PathVariable String characterId) {
        ResponseDto<List<RdmsMaterialDto>> responseDto = new ResponseDto<>();
        tenantParentGuard.requireCharacter(characterId);
        List<MaterialUsageModeEnum> usageModeEnums = new ArrayList<>();
        usageModeEnums.add(MaterialUsageModeEnum.CAPITALIZATION);
        List<RdmsMaterialManageDto> materialManageDtoList = rdmsMaterialManageService.getInHandListByCharacterIdAndUsageMode(characterId, usageModeEnums);
        if (!CollectionUtils.isEmpty(materialManageDtoList)) {
            List<String> stringList = materialManageDtoList.stream().map(RdmsMaterialManageDto::getCode).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(stringList)) {
                List<ApplicationStatusEnum> statusEnumList = new ArrayList<>();
                statusEnumList.add(ApplicationStatusEnum.APPROVED);
                statusEnumList.add(ApplicationStatusEnum.SUBMIT);
                statusEnumList.add(ApplicationStatusEnum.SUPPLEMENT);
                statusEnumList.add(ApplicationStatusEnum.PRE_APPROVED);
                List<RdmsMaterialDto> materialListByCodeInList = rdmsMaterialService.getMaterialListByCodeInListAndStatusList(stringList, statusEnumList);
                responseDto.setContent(materialListByCodeInList);
            }
        }
        return responseDto;
    }

    @PostMapping("/getCapitalizationListBySubprojectId/{subprojectId}")
    public ResponseDto<List<RdmsMaterialDto>> getCapitalizationListBySubprojectId(@PathVariable String subprojectId) {
        ResponseDto<List<RdmsMaterialDto>> responseDto = new ResponseDto<>();
        tenantParentGuard.requireSubproject(subprojectId);
        List<MaterialUsageModeEnum> usageModeEnums = new ArrayList<>();
        usageModeEnums.add(MaterialUsageModeEnum.CAPITALIZATION);
        List<RdmsMaterialManageDto> materialManageDtoList = rdmsMaterialManageService.getInHandListBySubprojectIdAndUsageMode(subprojectId, usageModeEnums);
        if (!CollectionUtils.isEmpty(materialManageDtoList)) {
            List<String> stringList = materialManageDtoList.stream().map(RdmsMaterialManageDto::getCode).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(stringList)) {
                List<ApplicationStatusEnum> statusEnumList = new ArrayList<>();
                statusEnumList.add(ApplicationStatusEnum.APPROVED);
                statusEnumList.add(ApplicationStatusEnum.SUBMIT);
                statusEnumList.add(ApplicationStatusEnum.SUPPLEMENT);
                statusEnumList.add(ApplicationStatusEnum.PRE_APPROVED);
                List<RdmsMaterialDto> materialListByCodeInList = rdmsMaterialService.getMaterialListByCodeInListAndStatusList(stringList, statusEnumList);
                responseDto.setContent(materialListByCodeInList);
            }
        }
        return responseDto;
    }

    @PostMapping("/getCapitalizationSummaryListByCharacterId/{characterId}")
    public ResponseDto<List<RdmsMaterialDto>> getCapitalizationSummaryListByCharacterId(@PathVariable String characterId) {
        ResponseDto<List<RdmsMaterialDto>> responseDto = new ResponseDto<>();
        tenantParentGuard.requireCharacter(characterId);
        List<MaterialUsageModeEnum> usageModeEnums = new ArrayList<>();
        usageModeEnums.add(MaterialUsageModeEnum.CAPITALIZATION);
        List<RdmsMaterialManageDto> materialManageDtoList = rdmsMaterialManageService.getMaterialSummaryListByCharacterIdAndUsageMode(characterId, usageModeEnums);
        if (!CollectionUtils.isEmpty(materialManageDtoList)) {
            List<String> stringList = materialManageDtoList.stream().map(RdmsMaterialManageDto::getCode).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(stringList)) {
                List<RdmsMaterialDto> materialListByCodeInList = rdmsMaterialService.getMaterialListByCodeInListAndStatusList(stringList, null);
                responseDto.setContent(materialListByCodeInList);
            }
        }
        return responseDto;
    }

    @PostMapping("/getBorrowListByCharacterId/{characterId}")
    public ResponseDto<List<RdmsMaterialDto>> getBorrowListByCharacterId(@PathVariable String characterId) {
        ResponseDto<List<RdmsMaterialDto>> responseDto = new ResponseDto<>();
        tenantParentGuard.requireCharacter(characterId);
        List<MaterialUsageModeEnum> usageModeEnums = new ArrayList<>();
        usageModeEnums.add(MaterialUsageModeEnum.BORROW);
        List<RdmsMaterialManageDto> materialManageDtoList = rdmsMaterialManageService.getInHandListByCharacterIdAndUsageMode(characterId, usageModeEnums);
        if (!CollectionUtils.isEmpty(materialManageDtoList)) {
            List<String> stringList = materialManageDtoList.stream().map(RdmsMaterialManageDto::getCode).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(stringList)) {
                List<ApplicationStatusEnum> statusEnumList = new ArrayList<>();
                statusEnumList.add(ApplicationStatusEnum.APPROVED);
                statusEnumList.add(ApplicationStatusEnum.SUBMIT);
                statusEnumList.add(ApplicationStatusEnum.SUPPLEMENT);
                statusEnumList.add(ApplicationStatusEnum.PRE_APPROVED);
                List<RdmsMaterialDto> materialListByCodeInList = rdmsMaterialService.getMaterialListByCodeInListAndStatusList(stringList, statusEnumList);
                responseDto.setContent(materialListByCodeInList);
            }
        }
        return responseDto;
    }

    @PostMapping("/getBorrowListBySubprojectId/{subprojectId}")
    public ResponseDto<List<RdmsMaterialDto>> getBorrowListBySubprojectId(@PathVariable String subprojectId) {
        ResponseDto<List<RdmsMaterialDto>> responseDto = new ResponseDto<>();
        tenantParentGuard.requireSubproject(subprojectId);
        List<MaterialUsageModeEnum> usageModeEnums = new ArrayList<>();
        usageModeEnums.add(MaterialUsageModeEnum.BORROW);
        List<RdmsMaterialManageDto> materialManageDtoList = rdmsMaterialManageService.getInHandListBySubprojectIdAndUsageMode(subprojectId, usageModeEnums);
        if (!CollectionUtils.isEmpty(materialManageDtoList)) {
            List<String> stringList = materialManageDtoList.stream().map(RdmsMaterialManageDto::getCode).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(stringList)) {
                List<ApplicationStatusEnum> statusEnumList = new ArrayList<>();
                statusEnumList.add(ApplicationStatusEnum.APPROVED);
                statusEnumList.add(ApplicationStatusEnum.SUBMIT);
                statusEnumList.add(ApplicationStatusEnum.SUPPLEMENT);
                statusEnumList.add(ApplicationStatusEnum.PRE_APPROVED);
                List<RdmsMaterialDto> materialListByCodeInList = rdmsMaterialService.getMaterialListByCodeInListAndStatusList(stringList, statusEnumList);
                responseDto.setContent(materialListByCodeInList);
            }
        }
        return responseDto;
    }

    @PostMapping("/getBorrowListByCustomerId/{customerId}")
    public ResponseDto<List<RdmsMaterialDto>> getBorrowListByCustomerId(@PathVariable String customerId) {
        ResponseDto<List<RdmsMaterialDto>> responseDto = new ResponseDto<>();
        List<MaterialUsageModeEnum> usageModeEnums = new ArrayList<>();
        usageModeEnums.add(MaterialUsageModeEnum.BORROW);
        List<RdmsMaterialManageDto> materialManageDtoList = rdmsMaterialManageService.getInHandListByCustomerIdAndUsageMode(customerId, usageModeEnums);
        if (!CollectionUtils.isEmpty(materialManageDtoList)) {
            List<String> stringList = materialManageDtoList.stream().map(RdmsMaterialManageDto::getCode).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(stringList)) {
                List<ApplicationStatusEnum> statusEnumList = new ArrayList<>();
                statusEnumList.add(ApplicationStatusEnum.APPROVED);
                statusEnumList.add(ApplicationStatusEnum.SUBMIT);
                statusEnumList.add(ApplicationStatusEnum.SUPPLEMENT);
                statusEnumList.add(ApplicationStatusEnum.PRE_APPROVED);
                List<RdmsMaterialDto> materialListByCodeInList = rdmsMaterialService.getMaterialListByCodeInListAndStatusList(stringList, statusEnumList);
                responseDto.setContent(materialListByCodeInList);
            }
        }
        return responseDto;
    }

    @PostMapping("/getExpenditureListByCustomerId/{customerId}")
    public ResponseDto<List<RdmsMaterialDto>> getExpenditureListByCustomerId(@PathVariable String customerId) {
        ResponseDto<List<RdmsMaterialDto>> responseDto = new ResponseDto<>();
        List<MaterialUsageModeEnum> usageModeEnums = new ArrayList<>();
        usageModeEnums.add(MaterialUsageModeEnum.EXPENDITURE);
        List<RdmsMaterialManageDto> materialManageDtoList = rdmsMaterialManageService.getInHandListByCustomerIdAndUsageMode(customerId, usageModeEnums);
        if (!CollectionUtils.isEmpty(materialManageDtoList)) {
            List<String> stringList = materialManageDtoList.stream().map(RdmsMaterialManageDto::getCode).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(stringList)) {
                List<ApplicationStatusEnum> statusEnumList = new ArrayList<>();
                statusEnumList.add(ApplicationStatusEnum.APPROVED);
                statusEnumList.add(ApplicationStatusEnum.SUBMIT);
                statusEnumList.add(ApplicationStatusEnum.SUPPLEMENT);
                statusEnumList.add(ApplicationStatusEnum.PRE_APPROVED);
                List<RdmsMaterialDto> materialListByCodeInList = rdmsMaterialService.getMaterialListByCodeInListAndStatusList(stringList, statusEnumList);
                responseDto.setContent(materialListByCodeInList);
            }
        }
        return responseDto;
    }

    @PostMapping("/getCapitalizationListByCustomerId/{customerId}")
    public ResponseDto<List<RdmsMaterialDto>> getCapitalizationListByCustomerId(@PathVariable String customerId) {
        ResponseDto<List<RdmsMaterialDto>> responseDto = new ResponseDto<>();
        List<MaterialUsageModeEnum> usageModeEnums = new ArrayList<>();
        usageModeEnums.add(MaterialUsageModeEnum.CAPITALIZATION);
        List<RdmsMaterialManageDto> materialManageDtoList = rdmsMaterialManageService.getInHandListByCustomerIdAndUsageMode(customerId, usageModeEnums);
        if (!CollectionUtils.isEmpty(materialManageDtoList)) {
            List<String> stringList = materialManageDtoList.stream().map(RdmsMaterialManageDto::getCode).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(stringList)) {
                List<ApplicationStatusEnum> statusEnumList = new ArrayList<>();
                statusEnumList.add(ApplicationStatusEnum.APPROVED);
                statusEnumList.add(ApplicationStatusEnum.SUBMIT);
                statusEnumList.add(ApplicationStatusEnum.SUPPLEMENT);
                statusEnumList.add(ApplicationStatusEnum.PRE_APPROVED);
                List<RdmsMaterialDto> materialListByCodeInList = rdmsMaterialService.getMaterialListByCodeInListAndStatusList(stringList, statusEnumList);
                responseDto.setContent(materialListByCodeInList);
            }
        }
        return responseDto;
    }

    @PostMapping("/getBorrowSummaryListByCharacterId/{characterId}")
    public ResponseDto<List<RdmsMaterialDto>> getBorrowSummaryListByCharacterId(@PathVariable String characterId) {
        ResponseDto<List<RdmsMaterialDto>> responseDto = new ResponseDto<>();
        tenantParentGuard.requireCharacter(characterId);
        List<MaterialUsageModeEnum> usageModeEnums = new ArrayList<>();
        usageModeEnums.add(MaterialUsageModeEnum.BORROW);
        List<RdmsMaterialManageDto> materialManageDtoList = rdmsMaterialManageService.getMaterialSummaryListByCharacterIdAndUsageMode(characterId, usageModeEnums);
        if (!CollectionUtils.isEmpty(materialManageDtoList)) {
            List<String> stringList = materialManageDtoList.stream().map(RdmsMaterialManageDto::getCode).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(stringList)) {
                List<RdmsMaterialDto> materialListByCodeInList = rdmsMaterialService.getMaterialListByCodeInListAndStatusList(stringList, null);
                responseDto.setContent(materialListByCodeInList);
            }
        }
        return responseDto;
    }

    @PostMapping("/getExpenditureListByCharacterId/{characterId}")
    public ResponseDto<List<RdmsMaterialDto>> getExpenditureListByCharacterId(@PathVariable String characterId) {
        ResponseDto<List<RdmsMaterialDto>> responseDto = new ResponseDto<>();
        tenantParentGuard.requireCharacter(characterId);
        List<MaterialUsageModeEnum> usageModeEnums = new ArrayList<>();
        usageModeEnums.add(MaterialUsageModeEnum.EXPENDITURE);
        List<RdmsMaterialManageDto> materialManageDtoList = rdmsMaterialManageService.getHaveApprovedListByCharacterIdAndUsageMode(characterId, usageModeEnums);
        if (!CollectionUtils.isEmpty(materialManageDtoList)) {
            List<String> stringList = materialManageDtoList.stream().map(RdmsMaterialManageDto::getCode).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(stringList)) {
                List<ApplicationStatusEnum> statusEnumList = new ArrayList<>();
                statusEnumList.add(ApplicationStatusEnum.PRE_APPROVED);
                statusEnumList.add(ApplicationStatusEnum.APPROVED);
                statusEnumList.add(ApplicationStatusEnum.SUBMIT);
                statusEnumList.add(ApplicationStatusEnum.SUPPLEMENT);
                statusEnumList.add(ApplicationStatusEnum.PRE_COMPLETE);
                statusEnumList.add(ApplicationStatusEnum.COMPLETE);
                List<RdmsMaterialDto> materialListByCodeInList = rdmsMaterialService.getMaterialListByCodeInListAndStatusList(stringList, statusEnumList);
                responseDto.setContent(materialListByCodeInList);
            }
        }
        return responseDto;
    }

    @PostMapping("/getExpenditureListBySubProjectId/{subprojectId}")
    public ResponseDto<List<RdmsMaterialDto>> getExpenditureListBySubProjectId(@PathVariable String subprojectId) {
        ResponseDto<List<RdmsMaterialDto>> responseDto = new ResponseDto<>();
        tenantParentGuard.requireSubproject(subprojectId);
        List<MaterialUsageModeEnum> usageModeEnums = new ArrayList<>();
        usageModeEnums.add(MaterialUsageModeEnum.EXPENDITURE);
        List<RdmsMaterialManageDto> materialManageDtoList = rdmsMaterialManageService.getHaveApprovedListBySubprojectIdAndUsageMode(subprojectId, usageModeEnums);
        if (!CollectionUtils.isEmpty(materialManageDtoList)) {
            List<String> stringList = materialManageDtoList.stream().map(RdmsMaterialManageDto::getCode).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(stringList)) {
                List<ApplicationStatusEnum> statusEnumList = new ArrayList<>();
                statusEnumList.add(ApplicationStatusEnum.PRE_APPROVED);
                statusEnumList.add(ApplicationStatusEnum.APPROVED);
                statusEnumList.add(ApplicationStatusEnum.SUBMIT);
                statusEnumList.add(ApplicationStatusEnum.SUPPLEMENT);
                statusEnumList.add(ApplicationStatusEnum.PRE_COMPLETE);
                statusEnumList.add(ApplicationStatusEnum.COMPLETE);
                List<RdmsMaterialDto> materialListByCodeInList = rdmsMaterialService.getMaterialListByCodeInListAndStatusList(stringList, statusEnumList);
                responseDto.setContent(materialListByCodeInList);
            }
        }
        return responseDto;
    }

    @PostMapping("/getExpenditureSummaryListByCharacterId/{characterId}")
    public ResponseDto<List<RdmsMaterialDto>> getExpenditureSummaryListByCharacterId(@PathVariable String characterId) {
        ResponseDto<List<RdmsMaterialDto>> responseDto = new ResponseDto<>();
        tenantParentGuard.requireCharacter(characterId);
        List<MaterialUsageModeEnum> usageModeEnums = new ArrayList<>();
        usageModeEnums.add(MaterialUsageModeEnum.EXPENDITURE);
        List<RdmsMaterialManageDto> materialManageDtoList = rdmsMaterialManageService.getMaterialSummaryListByCharacterIdAndUsageMode(characterId, usageModeEnums);
        if (!CollectionUtils.isEmpty(materialManageDtoList)) {
            List<String> stringList = materialManageDtoList.stream().map(RdmsMaterialManageDto::getCode).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(stringList)) {
                List<RdmsMaterialDto> materialListByCodeInList = rdmsMaterialService.getMaterialListByCodeInListAndStatusList(stringList, null);
                responseDto.setContent(materialListByCodeInList);
            }
        }
        return responseDto;
    }

    @PostMapping("/getMaterialListByCharacterId/{characterId}")
    public ResponseDto<List<RdmsMaterialDto>> getMaterialListByCharacterId(@PathVariable String characterId) {
        ResponseDto<List<RdmsMaterialDto>> responseDto = new ResponseDto<>();
        tenantParentGuard.requireCharacter(characterId);
        List<RdmsMaterialManageDto> materialManageDtoList = rdmsMaterialManageService.getMaterialSummaryListByCharacterIdAndUsageMode(characterId, null);
        if (!CollectionUtils.isEmpty(materialManageDtoList)) {
            List<String> stringList = materialManageDtoList.stream().map(RdmsMaterialManageDto::getCode).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(stringList)) {
                List<RdmsMaterialDto> materialListByCodeInList = rdmsMaterialService.getMaterialListByCodeInListAndStatusList(stringList, null);
                responseDto.setContent(materialListByCodeInList);
            }
        }
        return responseDto;
    }

    @PostMapping("/getMaterialListForBomSelected/{characterId}")
    public ResponseDto<List<RdmsMaterialDto>> getMaterialListForBomSelected(@PathVariable String characterId) {
        ResponseDto<List<RdmsMaterialDto>> responseDto = new ResponseDto<>();
        tenantParentGuard.requireCharacter(characterId);
        List<RdmsMaterialManage> materialManageList = rdmsMaterialManageService.getMaterialManageListForBomSelected(characterId);
        if (!CollectionUtils.isEmpty(materialManageList)) {
            List<String> stringList = materialManageList.stream().map(RdmsMaterialManage::getCode).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(stringList)) {
                List<RdmsMaterialDto> materialListByCodeInList = rdmsMaterialService.getMaterialListForBomSelected(stringList, characterId);
                responseDto.setContent(materialListByCodeInList);
            }
        }
        return responseDto;
    }

    @PostMapping("/getDirectMaterialListForBomSelected/{subprojectId}")
    public ResponseDto<List<RdmsMaterialDto>> getDirectMaterialListForBomSelected(@PathVariable String subprojectId) {
        ResponseDto<List<RdmsMaterialDto>> responseDto = new ResponseDto<>();
        tenantParentGuard.requireSubproject(subprojectId);
        List<RdmsMaterialManageDto> materialManageDtoList = rdmsMaterialManageService.getDirectMaterialSummaryListBySubprojectIdAndUsageMode(subprojectId, null);
        if (!CollectionUtils.isEmpty(materialManageDtoList)) {
            List<String> stringList = materialManageDtoList.stream().map(RdmsMaterialManageDto::getCode).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(stringList)) {
                List<RdmsMaterialDto> materialListByCodeInList = rdmsMaterialService.getMaterialListForBomSelected(stringList);
                responseDto.setContent(materialListByCodeInList);
            }
        }
        return responseDto;
    }

    @PostMapping("/getMaterialApplicationListByWriterId/{writerId}")
    public ResponseDto<List<RdmsMaterialManageDto>> getMaterialApplicationListByWriterId(@PathVariable String writerId) {
        writerId = TenantAccess.resolveMemberId(writerId);
        ResponseDto<List<RdmsMaterialManageDto>> responseDto = new ResponseDto<>();
        List<RdmsMaterialManageDto> listByWriterId = rdmsMaterialManageService.getMaterialApplicationListByWriterId(writerId);
        responseDto.setContent(listByWriterId);
        return responseDto;
    }

    @PostMapping("/getMaterialApplicationNum/{userId}")
    public ResponseDto<Integer> getMaterialApplicationNum(@PathVariable String userId) {
        userId = TenantAccess.resolveMemberId(userId);
        ResponseDto<Integer> responseDto = new ResponseDto<>();
        Integer materialApplicationNum = rdmsMaterialManageService.getMaterialApplicationAndSubmitSumNum(userId);
        responseDto.setContent(materialApplicationNum);
        return responseDto;
    }

    @PostMapping("/getMaterialApplicationNum_project/{userId}")
    public ResponseDto<Integer> getMaterialApplicationNum_project(@PathVariable String userId) {
        userId = TenantAccess.resolveMemberId(userId);
        ResponseDto<Integer> responseDto = new ResponseDto<>();
        Integer materialApplicationNum = rdmsMaterialManageService.getMaterialApplicationAndSubmitSumNum_project(userId);
        responseDto.setContent(materialApplicationNum);
        return responseDto;
    }

    @PostMapping("/getMaterialApplicationByCode/{code}")
    public ResponseDto<RdmsMaterialManageDto> getMaterialApplicationByCode(@PathVariable String code) {
        ResponseDto<RdmsMaterialManageDto> responseDto = new ResponseDto<>();
        tenantParentGuard.requireMaterialApplication(code);
        RdmsMaterialManageDto materialApplicationByCode = rdmsMaterialManageService.getMaterialApplicationByCodeMix(code);
        RdmsCustomerUser rdmsCustomerUser = rdmsCustomerUserService.selectByPrimaryKey(materialApplicationByCode.getWriterId());
        materialApplicationByCode.setWriterName(rdmsCustomerUser.getTrueName());
        if (!ObjectUtils.isEmpty(materialApplicationByCode.getNextNode())) {
            RdmsCustomerUser rdmsCustomerUser1 = rdmsCustomerUserService.selectByPrimaryKey(materialApplicationByCode.getNextNode());
            materialApplicationByCode.setNextNodeName(rdmsCustomerUser1.getTrueName());
        }
        if (!ObjectUtils.isEmpty(materialApplicationByCode.getApproverId())) {
            RdmsCustomerUser rdmsCustomerUser2 = rdmsCustomerUserService.selectByPrimaryKey(materialApplicationByCode.getApproverId());
            materialApplicationByCode.setApproverName(rdmsCustomerUser2.getTrueName());  //项目经理
        }
        if (!ObjectUtils.isEmpty(materialApplicationByCode.getJobitemId())) {
            RdmsJobItem rdmsJobItem = rdmsJobItemService.selectByPrimaryKey(materialApplicationByCode.getJobitemId());
            materialApplicationByCode.setJobitemName(rdmsJobItem.getJobName());
        }

        responseDto.setContent(materialApplicationByCode);
        return responseDto;
    }

    @PostMapping("/confirmCancel/{code}")
    @Transactional
    public ResponseDto<String> confirmCancel(@PathVariable String code) {
        ResponseDto<String> responseDto = new ResponseDto<>();
        RdmsMaterialManageDto materialApplication = rdmsMaterialManageService.getMaterialManageSimpleInfoByCode_origin(code);
        if (ObjectUtils.isEmpty(materialApplication)) {
            RdmsMaterialManageReturn materialManageReturn = rdmsMaterialManageService.getMaterialManageSimpleInfoByCode_return(code);
            if (ObjectUtils.isEmpty(materialManageReturn)) {
                responseDto.setContent(null);
            } else {
                assertCancelable(materialManageReturn.getStatus());
                TenantAccess.assertSameTenant(materialManageReturn.getCustomerId());
                String operatorId = this.resolveOperatorId(null);
                boolean proxy = this.assertWriterOrProxy(materialManageReturn.getCustomerId(), materialManageReturn.getWriterId(), operatorId);
                if (materialReturnFlowBridge.engineForExisting(materialManageReturn.getCustomerId(), code)) {
                    materialReturnFlowBridge.cancel(code, operatorId, proxy);
                    responseDto.setContent(materialManageReturn.getId());
                    return responseDto;
                }
                materialManageReturn.setStatus(ApplicationStatusEnum.CANCEL.getStatus());
                String s = rdmsMaterialManageService.update_return(materialManageReturn);
                applicationBudgetExeWriter.removeMaterialUnpaidExe(materialManageReturn.getCode());
                responseDto.setContent(s);

                //需要将原始的申请单的状态设置为为已批准状态
                RdmsMaterialManageDto materialManage = rdmsMaterialManageService.getMaterialManageSimpleInfoByCode_origin(materialManageReturn.getOriginCode());
                if(!ObjectUtils.isEmpty(materialManage)){
                    materialManage.setOpeStatus(null);
                    rdmsMaterialManageService.update(materialManage);
                }
            }
        } else {
            assertCancelable(materialApplication.getStatus());
            TenantAccess.assertSameTenant(materialApplication.getCustomerId());
            String operatorId = this.resolveOperatorId(null);
            boolean proxy = this.assertWriterOrProxy(materialApplication.getCustomerId(), materialApplication.getWriterId(), operatorId);
            String flowCustomerId = TenantContext.getTenantId();
            if (!StringUtils.hasText(flowCustomerId)) {
                flowCustomerId = materialApplication.getCustomerId();
            }
            if (materialFlowBridge.engineForExisting(flowCustomerId, code)) {
                materialFlowBridge.cancel(code, operatorId, proxy);
                responseDto.setContent(materialApplication.getId());
                return responseDto;
            }
            materialApplication.setStatus(ApplicationStatusEnum.CANCEL.getStatus());
            RdmsMaterialManage copy = CopyUtil.copy(materialApplication, RdmsMaterialManage.class);
            String s = rdmsMaterialManageService.update(copy);
            applicationBudgetExeWriter.removeMaterialUnpaidExe(materialApplication.getCode());
            responseDto.setContent(s);
        }
        return responseDto;
    }

    /**
     * 已有领料单只能在被驳回后修改重新提交，限填表人或最终管理人代办，单号不变且未发起退库。
     * 返回库中原单，新单返回 null。
     */
    private RdmsMaterialManage requireResubmittable(RdmsMaterialManageDto materialManageDto, String operatorId) {
        if (ObjectUtils.isEmpty(materialManageDto.getId())) {
            return null;
        }
        RdmsMaterialManage existing = rdmsMaterialManageService.selectByPrimaryKey(materialManageDto.getId());
        if (existing == null) {
            return null;
        }
        TenantAccess.assertSameTenant(existing.getCustomerId());
        this.assertWriterOrProxy(existing.getCustomerId(), existing.getWriterId(), operatorId);
        if (!ApplicationStatusEnum.REJECT.getStatus().equals(existing.getStatus())
                || StringUtils.hasText(existing.getOpeStatus())
                || !Objects.equals(existing.getCode(), materialManageDto.getCode())) {
            throw new BusinessException(BusinessExceptionCode.LIFECYCLE_TRANSITION_FORBIDDEN);
        }
        return existing;
    }

    /** 填表人本人或最终管理人代办；返回是否为代办 */
    private boolean assertWriterOrProxy(String customerId, String writerId, String operatorId) {
        if (operatorId.equals(writerId)) {
            return false;
        }
        if (jobActorGuard.hasFullAuthority(customerId, operatorId)) {
            return true;
        }
        throw new BusinessException(BusinessExceptionCode.NO_PERMISSION);
    }

    private static void assertSubmittable(String status) {
        if (!(ApplicationStatusEnum.APPROVED.getStatus().equals(status)
                || ApplicationStatusEnum.PRE_APPROVED.getStatus().equals(status)
                || ApplicationStatusEnum.SUPPLEMENT.getStatus().equals(status))) {
            throw new BusinessException(BusinessExceptionCode.LIFECYCLE_TRANSITION_FORBIDDEN);
        }
    }

    /** 与流程引擎的物料复核人一致：系统工程阶段为系统工程总监，产品类与质量工单为产品经理，其余为项目经理 */
    private String recheckNode(RdmsJobItem rdmsJobItem, String customerId) {
        if (JobItemTypeEnum.TASK_FUNCTION.getType().equals(rdmsJobItem.getType()) || JobItemTypeEnum.TASK_FUNCTION.getType().equals(rdmsJobItem.getAuxType())) {
            RdmsSysgmDto sysgm = rdmsSysgmService.getSysgmByCustomerId(customerId);
            if (sysgm == null || ObjectUtils.isEmpty(sysgm.getSysgmId())) {
                throw new BusinessException(BusinessExceptionCode.SYSGM_NOT_SET);
            }
            return sysgm.getSysgmId();
        }
        if (JobItemTypeEnum.TASK_PRODUCT.getType().equals(rdmsJobItem.getType()) || JobItemTypeEnum.TASK_PRODUCT.getType().equals(rdmsJobItem.getAuxType())
                || JobItemTypeEnum.QUALITY.getType().equals(rdmsJobItem.getType())) {
            return rdmsJobItem.getProductManagerId();
        }
        return rdmsJobItem.getProjectManagerId();
    }

    private RdmsJobItem requireJobItem(String jobitemId) {
        RdmsJobItem rdmsJobItem = ObjectUtils.isEmpty(jobitemId) ? null : rdmsJobItemService.selectByPrimaryKey(jobitemId);
        if (rdmsJobItem == null) {
            throw new BusinessException(BusinessExceptionCode.DB_RECORD_NOT_EXIST);
        }
        return rdmsJobItem;
    }

    private static void assertCancelable(String status) {
        if (!ApplicationStatusEnum.REJECT.getStatus().equals(status)) {
            throw new BusinessException(BusinessExceptionCode.LIFECYCLE_TRANSITION_FORBIDDEN);
        }
    }

    /**
     * 网关验签后以租户上下文中的成员身份为准，本地直连时退回请求中的 loginUserId
     */
    private String resolveOperatorId(String fallbackLoginUserId) {
        String operatorId = TenantContext.getMembershipId();
        if (!StringUtils.hasText(operatorId)) {
            operatorId = fallbackLoginUserId;
        }
        ValidatorUtil.require(operatorId, "操作者");
        return operatorId;
    }

    /**
     * 按库内明细写审批结论并返回记账金额。单价在申请时取自库存，审批不改价。
     */
    private BigDecimal applyMaterialLines(RdmsMaterialManageDto current, String approveStatus, boolean returning) {
        BigDecimal accountCost = BigDecimal.ZERO;
        for (RdmsMaterialDto line : current.getMaterialList()) {
            BigDecimal unitPrice = line.getPreTaxUnitPrice() == null ? BigDecimal.ZERO : line.getPreTaxUnitPrice();
            accountCost = accountCost.add(unitPrice.multiply(BigDecimal.valueOf(line.getAmount() == null ? 0 : line.getAmount())));
            if (returning) {
                RdmsMaterialReturn material = new RdmsMaterialReturn();
                material.setId(line.getId());
                material.setUsageMode(current.getUsageMode());
                material.setStatus(approveStatus);
                rdmsMaterialService.updateByPrimaryKeySelective_return(material);
            } else {
                RdmsMaterial material = new RdmsMaterial();
                material.setId(line.getId());
                material.setUsageMode(current.getUsageMode());
                material.setStatus(approveStatus);
                rdmsMaterialService.updateByPrimaryKeySelective(material);
            }
        }
        return accountCost;
    }

    @PostMapping("/updateMaterialApproveFileListStr")
    public ResponseDto<String> updateMaterialApproveFileListStr(@RequestBody RdmsMaterialManageDto materialManageDto) {
        ResponseDto<String> responseDto = new ResponseDto<>();
        if (!ObjectUtils.isEmpty(materialManageDto.getApproveFileListStr())) {
            RdmsMaterialManage copy = CopyUtil.copy(materialManageDto, RdmsMaterialManage.class);
            String id = "";
            if (!ObjectUtils.isEmpty(materialManageDto.getOpeStatus()) && materialManageDto.getOpeStatus().equals(MaterialOpeStatusEnum.RETURN_APPROVED.getStatus())) {
                id = rdmsMaterialManageService.updateMaterialApproveFileListStr_return(copy);
            } else {
                id = rdmsMaterialManageService.updateMaterialApproveFileListStr(copy);
            }
            responseDto.setContent(id);
        } else {
            responseDto.setContent(null);
        }
        return responseDto;
    }

    @PostMapping("/updateMaterialDealWithFileListStr")
    public ResponseDto<String> updateMaterialDealWithFileListStr(@RequestBody RdmsMaterialManageDto materialManageDto) {
        ResponseDto<String> responseDto = new ResponseDto<>();
        if (!ObjectUtils.isEmpty(materialManageDto.getDealWithFileListStr())) {
            RdmsMaterialManage copy = CopyUtil.copy(materialManageDto, RdmsMaterialManage.class);
            String id = "";
            if (!ObjectUtils.isEmpty(materialManageDto.getOpeStatus()) && materialManageDto.getOpeStatus().equals(MaterialOpeStatusEnum.RETURN_APPROVED.getStatus())) {
                id = rdmsMaterialManageService.updateMaterialDealWithFileListStr_return(copy);
            } else {
                id = rdmsMaterialManageService.updateMaterialDealWithFileListStr(copy);
            }
            responseDto.setContent(id);
        } else {
            responseDto.setContent(null);
        }
        return responseDto;
    }

    @PostMapping("/updateMaterialCompleteFileListStr")
    public ResponseDto<String> updateMaterialCompleteFileListStr(@RequestBody RdmsMaterialManageDto materialManageDto) {
        ResponseDto<String> responseDto = new ResponseDto<>();
        if (!ObjectUtils.isEmpty(materialManageDto.getCompleteFileListStr())) {
            RdmsMaterialManage copy = CopyUtil.copy(materialManageDto, RdmsMaterialManage.class);
            String id = "";
            if (!ObjectUtils.isEmpty(materialManageDto.getOpeStatus()) && materialManageDto.getOpeStatus().equals(MaterialOpeStatusEnum.RETURN_APPROVED.getStatus())) {
                id = rdmsMaterialManageService.updateMaterialCompleteFileListStr_return(copy);
            } else {
                id = rdmsMaterialManageService.updateMaterialCompleteFileListStr(copy);
            }
            responseDto.setContent(id);
        } else {
            responseDto.setContent(null);
        }
        return responseDto;
    }

    @PostMapping("/submit")
    @Transactional
    public ResponseDto<String> submit(@RequestBody RdmsMaterialManageDto materialManageDto) {
        ValidatorUtil.require(materialManageDto.getCode(), "申请单号");
        ValidatorUtil.require(materialManageDto.getLoginUserId(), "当前用户");
        ValidatorUtil.require(materialManageDto.getSubmitDescription(), "提交说明");
        ValidatorUtil.length(materialManageDto.getUsageMode(), "使用方式", 2, 20);

        ResponseDto<String> responseDto = new ResponseDto<>();
        String operatorId = this.resolveOperatorId(materialManageDto.getLoginUserId());
        if (!ObjectUtils.isEmpty(materialManageDto.getOpeStatus()) && materialManageDto.getOpeStatus().equals(MaterialOpeStatusEnum.RETURN_APPROVED.getStatus())) {

            RdmsMaterialManageDto materialApplication = rdmsMaterialManageService.getMaterialApplicationByCode_return(materialManageDto.getCode());
            if (materialApplication == null) {
                throw new BusinessException(BusinessExceptionCode.DB_RECORD_NOT_EXIST);
            }
            TenantAccess.assertSameTenant(materialApplication.getCustomerId());
            boolean proxy = this.assertWriterOrProxy(materialApplication.getCustomerId(), materialApplication.getWriterId(), operatorId);
            if (materialApplication.isPendingIssue()) {
                throw new BusinessException(BusinessExceptionCode.LIFECYCLE_TRANSITION_FORBIDDEN);
            }
            if (materialReturnFlowBridge.engineForExisting(materialApplication.getCustomerId(), materialManageDto.getCode())) {
                RdmsMaterialManageReturn patch = new RdmsMaterialManageReturn();
                patch.setId(materialApplication.getId());
                patch.setSubmitDescription(materialManageDto.getSubmitDescription());
                rdmsMaterialManageService.updateByPrimaryKeySelective_return(patch);
                materialReturnFlowBridge.submit(materialManageDto.getCode(), operatorId, proxy);
                responseDto.setContent(materialApplication.getCode());
                return responseDto;
            }
            assertSubmittable(materialApplication.getStatus());
            RdmsJobItem rdmsJobItem = this.requireJobItem(materialApplication.getJobitemId());
            materialApplication.setStatus(ApplicationStatusEnum.SUBMIT.getStatus());
            materialApplication.setSubmitDescription(materialManageDto.getSubmitDescription());
            materialApplication.setNextNode(this.recheckNode(rdmsJobItem, materialApplication.getCustomerId()));
            RdmsMaterialManageReturn copy = CopyUtil.copy(materialApplication, RdmsMaterialManageReturn.class);
            rdmsMaterialManageService.update_return(copy);

            //填写一条过程记录
            RdmsMaterialProcessReturn materialProcess = new RdmsMaterialProcessReturn();
            materialProcess.setMaterialId(materialApplication.getId());
            materialProcess.setExecutorId(operatorId);
            long materialProcessCount = rdmsMaterialProcessService.getMaterialProcessCount(materialApplication.getId());
            materialProcess.setDeep((int) materialProcessCount);
            materialProcess.setDescription("物料处置完成, 提交审批");
            materialProcess.setNextNode(this.processNextNode(rdmsJobItem, materialApplication));
            materialProcess.setSubmitTime(new Date());
            materialProcess.setStatus(ApplicationStatusEnum.SUBMIT.getStatus());
            rdmsMaterialProcessService.save_return(materialProcess);

            responseDto.setContent(materialApplication.getCode());
        } else {
            RdmsMaterialManageDto materialApplication = rdmsMaterialManageService.getMaterialApplicationByCode_origin(materialManageDto.getCode());
            if (materialApplication == null) {
                throw new BusinessException(BusinessExceptionCode.DB_RECORD_NOT_EXIST);
            }
            String flowCustomerId = TenantContext.getTenantId();
            if (!StringUtils.hasText(flowCustomerId)) {
                flowCustomerId = materialApplication.getCustomerId();
            }
            TenantAccess.assertSameTenant(materialApplication.getCustomerId());
            boolean proxy = this.assertWriterOrProxy(materialApplication.getCustomerId(), materialApplication.getWriterId(), operatorId);
            if (materialApplication.isPendingIssue()) {
                throw new BusinessException(BusinessExceptionCode.LIFECYCLE_TRANSITION_FORBIDDEN);
            }
            if (materialFlowBridge.engineForExisting(flowCustomerId, materialManageDto.getCode())) {
                RdmsMaterialManage patch = new RdmsMaterialManage();
                patch.setId(materialApplication.getId());
                patch.setSubmitDescription(materialManageDto.getSubmitDescription());
                rdmsMaterialManageService.updateByPrimaryKeySelective(patch);
                materialFlowBridge.submit(materialManageDto.getCode(), operatorId, proxy);
                responseDto.setContent(materialApplication.getCode());
                return responseDto;
            }
            assertSubmittable(materialApplication.getStatus());
            RdmsJobItem rdmsJobItem = this.requireJobItem(materialApplication.getJobitemId());
            materialApplication.setStatus(ApplicationStatusEnum.SUBMIT.getStatus());
            materialApplication.setSubmitDescription(materialManageDto.getSubmitDescription());
            materialApplication.setNextNode(this.recheckNode(rdmsJobItem, materialApplication.getCustomerId()));
            RdmsMaterialManage copy = CopyUtil.copy(materialApplication, RdmsMaterialManage.class);
            rdmsMaterialManageService.update(copy);

            //填写一条过程记录
            RdmsMaterialProcess materialProcess = new RdmsMaterialProcess();
            materialProcess.setMaterialId(materialApplication.getId());
            materialProcess.setExecutorId(operatorId);
            long materialProcessCount = rdmsMaterialProcessService.getMaterialProcessCount(materialApplication.getId());
            materialProcess.setDeep((int) materialProcessCount);
            materialProcess.setDescription("物料处置完成, 提交审批");
            materialProcess.setNextNode(this.processNextNode(rdmsJobItem, materialApplication));
            materialProcess.setSubmitTime(new Date());
            materialProcess.setStatus(ApplicationStatusEnum.SUBMIT.getStatus());
            rdmsMaterialProcessService.save(materialProcess);

            responseDto.setContent(materialApplication.getCode());
        }

        return responseDto;
    }

    /** 过程记录的下一节点：系统工程阶段记预立项的系统工程，产品类与质量工单记产品经理，其余记子项目经理 */
    private String processNextNode(RdmsJobItem rdmsJobItem, RdmsMaterialManageDto materialApplication) {
        if (JobItemTypeEnum.TASK_FUNCTION.getType().equals(rdmsJobItem.getType()) || JobItemTypeEnum.TASK_FUNCTION.getType().equals(rdmsJobItem.getAuxType())) {
            RdmsPreProject rdmsPreProject = ObjectUtils.isEmpty(rdmsJobItem.getPreProjectId()) ? null : rdmsPreProjectService.selectByPrimaryKey(rdmsJobItem.getPreProjectId());
            return rdmsPreProject == null ? materialApplication.getNextNode() : rdmsPreProject.getSystemManagerId();
        }
        if (JobItemTypeEnum.TASK_PRODUCT.getType().equals(rdmsJobItem.getType()) || JobItemTypeEnum.TASK_PRODUCT.getType().equals(rdmsJobItem.getAuxType())
                || JobItemTypeEnum.QUALITY.getType().equals(rdmsJobItem.getType())) {
            return rdmsJobItem.getProductManagerId();
        }
        RdmsProjectSubproject subproject = ObjectUtils.isEmpty(materialApplication.getSubprojectId()) ? null : rdmsSubprojectService.selectByPrimaryKey(materialApplication.getSubprojectId());
        return subproject == null ? materialApplication.getNextNode() : subproject.getProjectManagerId();
    }

    /**
     * 删除BOM条目
     */
    @DeleteMapping("/delete/{id}")
    public ResponseDto deleteBomItem(@PathVariable String id) {
        ResponseDto responseDto = new ResponseDto();
        rdmsBomGateService.deleteBomLine(id);
        return responseDto;
    }

    /**
     * 物料清单带入软件模块（CBB）行：登记 CBB 标识、名称与版本号，不要求供应商与单价
     */
    @PostMapping("/saveCbbToBom/{characterId}/{cbbId}")
    public ResponseDto<String> saveCbbToBom(@PathVariable String characterId, @PathVariable String cbbId) {
        ResponseDto<String> responseDto = new ResponseDto<>();
        responseDto.setContent(rdmsBomGateService.saveCbbToBom(characterId, cbbId));
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    private Map<String, String> activeWarehouseNames(String customerId) {
        Map<String, String> names = new HashMap<>();
        for (RdmsMaterialWarehouse warehouse : rdmsMaterialWarehouseService.list(customerId)) {
            if (MaterialWarehouseStatusEnum.ACTIVE.getStatus().equals(warehouse.getStatus())) {
                names.put(warehouse.getCode(), warehouse.getName());
            }
        }
        return names;
    }

    private List<RdmsSAPMaterialDto> toSapDtos(List<RdmsMaterialStock> stocks, Map<String, String> activeWarehouseNames) {
        List<RdmsSAPMaterialDto> rows = new ArrayList<>();
        if (stocks == null) {
            return rows;
        }
        for (RdmsMaterialStock stock : stocks) {
            if (!activeWarehouseNames.containsKey(stock.getWarehouseCode())
                    || stock.getQuantity() == null || stock.getQuantity() <= 0) {
                continue;
            }
            RdmsSAPMaterialDto dto = new RdmsSAPMaterialDto();
            dto.setMaterialNo(stock.getBomCode());
            dto.setMaterialName(stock.getName());
            dto.setMaterialSpcf(stock.getModel());
            dto.setWarehouseNo(stock.getWarehouseCode());
            dto.setWarehouseCode(stock.getWarehouseCode());
            dto.setWarehouseName(activeWarehouseNames.get(stock.getWarehouseCode()));
            dto.setUnit(stock.getUnit());
            dto.setNum(stock.getQuantity().intValue());
            dto.setPrice(stock.getUnitPrice() == null ? 0D : stock.getUnitPrice().doubleValue());
            rows.add(dto);
        }
        return rows;
    }

    /** 按行标识写入已有清单行或树节点前，核对该行原归属的特性或（子）项目属于当前机构；本机构内改挂不受限。 */
    private void assertStoredParentTenant(String characterId, String projectOrSubprojectId) {
        if (StringUtils.hasText(characterId)) {
            tenantParentGuard.requireCharacter(characterId);
        } else if (StringUtils.hasText(projectOrSubprojectId)) {
            tenantParentGuard.requireProjectOrSubproject(projectOrSubprojectId);
        }
    }

    private static List<RdmsBomTreeDto> flattenBomTree(List<RdmsBomTreeDto> treeList, List<RdmsBomTreeDto> collect) {
        for (RdmsBomTreeDto node : treeList) {
            if (node == null) {
                continue;
            }
            collect.add(node);
            if (!CollectionUtils.isEmpty(node.getChildren())) {
                flattenBomTree(node.getChildren(), collect);
            }
        }
        return collect;
    }

}
