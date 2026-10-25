/**
 * Author：周朔鹏
 * 编制时间：2025-08-05

 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */

package com.course.customer.controller;

import com.course.server.service.rdms.TenantRoleService;
import com.course.server.domain.RdmsBudgetResearch;
import com.course.server.domain.RdmsProject;
import com.course.server.domain.RdmsProjectSubproject;
import com.course.server.dto.PageDto;
import com.course.server.dto.RdmsBudgetResearchDto;
import com.course.server.dto.ResponseDto;
import com.course.server.enums.rdms.BudgetTypeEnum;
import com.course.server.enums.rdms.ProjectStatusEnum;
import com.course.server.enums.rdms.RoleEnum;
import com.course.server.exception.BusinessException;
import com.course.server.exception.BusinessExceptionCode;
import com.course.server.security.tenant.TenantAccess;
import com.course.server.security.tenant.TenantContext;
import com.course.server.service.rdms.RdmsBudgetResearchService;
import com.course.server.service.rdms.RdmsProjectService;
import com.course.server.service.rdms.RdmsSubprojectService;
import com.course.server.service.rdms.RdmsUserService;
import com.course.server.util.CopyUtil;
import com.course.server.util.ValidatorUtil;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/budget-direct")
public class BudgetResearchController {
    private static final Logger LOG = LoggerFactory.getLogger(BudgetResearchController.class);
    public static final String BUSINESS_NAME = "直接立项预算管理";

    @Resource
    private RdmsBudgetResearchService rdmsBudgetResearchService;
    @Resource
    private RdmsProjectService rdmsProjectService;
    @Resource
    private RdmsUserService rdmsUserService;
    @Resource
    private RdmsSubprojectService rdmsSubprojectService;

    @PostMapping("/list")
    public ResponseDto<PageDto<RdmsBudgetResearchDto>> list(@RequestBody PageDto<RdmsBudgetResearchDto> pageDto) {
        ResponseDto<PageDto<RdmsBudgetResearchDto>> responseDto = new ResponseDto<>();
        rdmsBudgetResearchService.list(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

    @PostMapping("/saveBudget")
    @Transactional
    public ResponseDto<String> saveBudget(@RequestBody RdmsBudgetResearchDto budgetData) {
        ResponseDto<String> responseDto = new ResponseDto<>();

        ValidatorUtil.require(budgetData.getProjectId(), "项目ID");
        ValidatorUtil.require(budgetData.getStage(), "预算阶段");
        ValidatorUtil.length(budgetData.getProjectId(), "项目ID", 8, 8);

        String actorId = resolveActorId(budgetData.getCreaterId());
        RdmsProject project = requireProject(budgetData.getProjectId());
        TenantAccess.assertSameTenant(project.getCustomerId());
        String tenantId = project.getCustomerId();
        assertCanEditBudget(project, actorId, tenantId);
        assertBudgetAmountsNonNegative(budgetData);

        RdmsBudgetResearchDto recordByProjectAndStage =
                rdmsBudgetResearchService.getRecordByProjectIdAndStage(budgetData.getProjectId(), BudgetTypeEnum.PROJECT);

        // 重复点击保存按钮时,更新数据
        if (!ObjectUtils.isEmpty(recordByProjectAndStage) && Boolean.TRUE.equals(recordByProjectAndStage.getHasRecord())) {
            RdmsBudgetResearch projectBudget = applyBudgetFeeWhitelist(recordByProjectAndStage, budgetData);
            projectBudget.setId(recordByProjectAndStage.getId());
            projectBudget.setCustomerId(tenantId);
            projectBudget.setProjectId(project.getId());
            projectBudget.setSubprojectId(null);
            projectBudget.setParentId(null);
            projectBudget.setStage(BudgetTypeEnum.PROJECT.getType());
            projectBudget.setIsRoot(1);
            projectBudget.setCreaterId(StringUtils.hasText(recordByProjectAndStage.getCreaterId())
                    ? recordByProjectAndStage.getCreaterId() : actorId);
            projectBudget.setDeleted(0);
            rdmsBudgetResearchService.transformBudgetToYuan(projectBudget);
            String s = rdmsBudgetResearchService.save(projectBudget);//保存主项目记录

            //作为子项目处理的主项目记录
            RdmsBudgetResearchDto dbRecord = rdmsBudgetResearchService.getRecordBySubProjectIdAndStage(
                    budgetData.getProjectId(), BudgetTypeEnum.SUB_PROJECT);
            if (!ObjectUtils.isEmpty(dbRecord) && Boolean.TRUE.equals(dbRecord.getHasRecord())) {
                RdmsBudgetResearch subCopy = CopyUtil.copy(projectBudget, RdmsBudgetResearch.class);
                subCopy.setId(dbRecord.getId());
                subCopy.setParentId(project.getId());
                subCopy.setSubprojectId(project.getId());
                subCopy.setStage(BudgetTypeEnum.SUB_PROJECT.getType());
                subCopy.setIsRoot(0);
                rdmsBudgetResearchService.update(subCopy); //保存作为子项目使用的copy
                responseDto.setContent(s);
            } else {
                RdmsBudgetResearch subCopy = CopyUtil.copy(projectBudget, RdmsBudgetResearch.class);
                subCopy.setId(null);
                subCopy.setParentId(project.getId());
                subCopy.setSubprojectId(project.getId());
                subCopy.setStage(BudgetTypeEnum.SUB_PROJECT.getType());
                subCopy.setIsRoot(0);
                rdmsBudgetResearchService.save(subCopy); //保存作为子项目使用的copy
                responseDto.setContent(s);
            }
        } else {
            RdmsBudgetResearch seed = applyBudgetFeeWhitelist(new RdmsBudgetResearchDto(), budgetData);
            seed.setCustomerId(tenantId);
            seed.setProjectId(project.getId());
            seed.setCreaterId(actorId);
            seed.setDeleted(0);

            RdmsBudgetResearch subCopy = CopyUtil.copy(seed, RdmsBudgetResearch.class);
            subCopy.setId(null);
            subCopy.setParentId(project.getId());
            subCopy.setSubprojectId(project.getId());
            subCopy.setStage(BudgetTypeEnum.SUB_PROJECT.getType());
            subCopy.setIsRoot(0);
            rdmsBudgetResearchService.transformBudgetToYuan(subCopy);
            String id = rdmsBudgetResearchService.save(subCopy); //保存作为子项目使用的记录

            //保存作为主项目的原始批准预算记录
            RdmsBudgetResearch projectBudget = CopyUtil.copy(subCopy, RdmsBudgetResearch.class);
            projectBudget.setId(null);
            projectBudget.setSubprojectId(null);
            projectBudget.setParentId(null);
            projectBudget.setStage(BudgetTypeEnum.PROJECT.getType());
            projectBudget.setIsRoot(1);
            rdmsBudgetResearchService.save(projectBudget); //保存主项目记录

            responseDto.setContent(id);
        }

        responseDto.setSuccess(true);
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    @PostMapping("/saveBudget_subproject")
    @Transactional
    public ResponseDto<String> saveBudget_subproject(@RequestBody RdmsBudgetResearchDto budgetData) {
        ResponseDto<String> responseDto = new ResponseDto<>();

        ValidatorUtil.require(budgetData.getCustomerId(), "客户ID");
        ValidatorUtil.require(budgetData.getSubprojectId(), "项目ID");

        ValidatorUtil.length(budgetData.getCustomerId(), "客户ID", 8, 8);
        ValidatorUtil.length(budgetData.getSubprojectId(), "项目ID", 8, 8);

        RdmsProjectSubproject subproject = rdmsSubprojectService.selectByPrimaryKey(budgetData.getSubprojectId());
        if (ObjectUtils.isEmpty(subproject) || Integer.valueOf(1).equals(subproject.getDeleted())) {
            throw new BusinessException(BusinessExceptionCode.DB_RECORD_NOT_EXIST);
        }
        RdmsProject project = requireProject(subproject.getProjectId());
        TenantAccess.assertSameTenant(project.getCustomerId());
        String tenantId = project.getCustomerId();
        String actorId = resolveActorId(budgetData.getModifierId());
        assertCanAdjustBudget(project, subproject, actorId, tenantId);
        assertAvailableAmountsNonNegative(budgetData);
        budgetData.setCustomerId(tenantId);
        budgetData.setModifierId(actorId);

        String newId = rdmsBudgetResearchService.adjustSubprojectBudget(budgetData);

        responseDto.setContent(newId);
        responseDto.setSuccess(true);
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    @Data
    public static class CustomerIdAndProjectId{
        String customerId;
        String projectId;
        String subprojectId;
    }

    @PostMapping("/getBudget")
    public ResponseDto<RdmsBudgetResearchDto> getBudget(@RequestParam String projectId, String stage) {
        ResponseDto<RdmsBudgetResearchDto> responseDto = new ResponseDto<>();

        ValidatorUtil.require(projectId, "项目ID");
        ValidatorUtil.require(stage, "预算阶段");

        RdmsProject project = requireProject(projectId);
        TenantAccess.assertSameTenant(project.getCustomerId());

        RdmsBudgetResearchDto rdmsBudgetResearch = rdmsBudgetResearchService.getRecordByProjectIdAndStage(
                projectId, Objects.requireNonNull(BudgetTypeEnum.getBudgetTypeEnumByType(stage)));
        if (Boolean.TRUE.equals(rdmsBudgetResearch.getHasRecord())) {
            rdmsBudgetResearchService.transformBudgetToWanYuan(rdmsBudgetResearch);
        }

        responseDto.setContent(rdmsBudgetResearch);
        return responseDto;
    }

    @PostMapping("/getBudget_subproject")
    @Transactional
    public ResponseDto<RdmsBudgetResearchDto> getBudget_subproject(@RequestBody CustomerIdAndProjectId budgetData) {
        ResponseDto<RdmsBudgetResearchDto> responseDto = new ResponseDto<>();

        ValidatorUtil.require(budgetData.getCustomerId(), "客户ID");
        ValidatorUtil.require(budgetData.getSubprojectId(), "项目ID");

        ValidatorUtil.length(budgetData.getCustomerId(), "客户ID", 8, 8);
        ValidatorUtil.length(budgetData.getSubprojectId(), "项目ID", 8, 8);

        String tenantId = TenantAccess.resolveCustomerId(budgetData.getCustomerId());
        TenantAccess.assertSameTenant(tenantId);

        RdmsBudgetResearchDto rdmsBudgetResearch = rdmsBudgetResearchService.getRecordByCustomerIdAndSubprojectId(
                tenantId, budgetData.getSubprojectId());
        if (rdmsBudgetResearch.getHasRecord()) {
            rdmsBudgetResearchService.transformBudgetToWanYuan(rdmsBudgetResearch);
        }

        responseDto.setContent(rdmsBudgetResearch);
        return responseDto;
    }

    @PostMapping("/getDirectSumBudget")
    public ResponseDto<Double> getDirectSumBudget(@RequestBody CustomerIdAndProjectId budgetData) {
        ResponseDto<Double> responseDto = new ResponseDto<>();

        ValidatorUtil.require(budgetData.getProjectId(), "项目ID");
        ValidatorUtil.length(budgetData.getProjectId(), "项目ID", 8, 8);

        RdmsProject project = requireProject(budgetData.getProjectId());
        TenantAccess.assertSameTenant(project.getCustomerId());

        double directSumBudgetByCustomerIdAndProjectId = rdmsBudgetResearchService
                .getDirectSumBudgetByCustomerIdAndProjectId(project.getCustomerId(), project.getId());
        responseDto.setContent(directSumBudgetByCustomerIdAndProjectId);
        return responseDto;
    }

    /**
     * 删除
     */
    @DeleteMapping("/del/{id}")
    public ResponseDto delete(@PathVariable String id) {
        ResponseDto responseDto = new ResponseDto();
        RdmsBudgetResearch record = rdmsBudgetResearchService.selectByPrimaryKey(id);
        if (ObjectUtils.isEmpty(record) || Integer.valueOf(1).equals(record.getDeleted())
                || !StringUtils.hasText(record.getProjectId())) {
            throw new BusinessException(BusinessExceptionCode.DB_RECORD_NOT_EXIST);
        }
        RdmsProject project = requireProject(record.getProjectId());
        TenantAccess.assertSameTenant(project.getCustomerId());
        assertCanEditBudget(project, resolveActorId(null), project.getCustomerId());
        rdmsBudgetResearchService.delete(id);
        return responseDto;
    }

    private String resolveActorId(String fallbackLoginUserId) {
        String actorId = TenantContext.getMembershipId();
        if (!StringUtils.hasText(actorId)) {
            actorId = fallbackLoginUserId;
        }
        ValidatorUtil.require(actorId, "操作者");
        return actorId;
    }

    private RdmsProject requireProject(String projectId) {
        RdmsProject project = rdmsProjectService.selectByPrimaryKey(projectId);
        if (ObjectUtils.isEmpty(project)) {
            throw new BusinessException(BusinessExceptionCode.INPUT_INFORMATION_ERROR);
        }
        return project;
    }

    private boolean hasFullAuthority(String tenantId, String actorId) {
        List<String> roles = rdmsUserService.getUserRole(tenantId, actorId);
        return TenantRoleService.hasFullAuthority(roles);
    }

    /**
     * 与立项编辑口径一致：SETUP_EDIT/SETUP，且创建者/监管/最终管理人。
     */
    private void assertCanEditBudget(RdmsProject db, String actorId, String tenantId) {
        String status = db.getStatus();
        if (!ProjectStatusEnum.SETUP_EDIT.getStatus().equals(status)
                && !ProjectStatusEnum.SETUP.getStatus().equals(status)) {
            throw new BusinessException(BusinessExceptionCode.NO_PERMISSION);
        }
        boolean allowed = actorId.equals(db.getCreaterId())
                || actorId.equals(db.getSupervise())
                || hasFullAuthority(tenantId, actorId);
        if (!allowed) {
            throw new BusinessException(BusinessExceptionCode.NO_PERMISSION);
        }
    }

    /**
     * 立项后调整子项目可用额：操作人与立项预算编制相同，主项目与子项目均未收口。
     */
    private void assertCanAdjustBudget(RdmsProject project, RdmsProjectSubproject subproject,
                                       String actorId, String tenantId) {
        if (ProjectStatusEnum.settledStatuses().contains(project.getStatus())
                || ProjectStatusEnum.settledStatuses().contains(subproject.getStatus())) {
            throw new BusinessException(BusinessExceptionCode.LIFECYCLE_TRANSITION_FORBIDDEN);
        }
        boolean allowed = actorId.equals(project.getCreaterId())
                || actorId.equals(project.getSupervise())
                || hasFullAuthority(tenantId, actorId);
        if (!allowed) {
            throw new BusinessException(BusinessExceptionCode.NO_PERMISSION);
        }
    }

    private void assertAvailableAmountsNonNegative(RdmsBudgetResearchDto dto) {
        for (BigDecimal value : Arrays.asList(
                dto.getEquipmentFeeBt(), dto.getEquipmentFeeZc(), dto.getTestFeeBt(), dto.getTestFeeZc(),
                dto.getMaterialFeeBt(), dto.getMaterialFeeZc(), dto.getPowerFeeBt(), dto.getPowerFeeZc(),
                dto.getConferenceFeeBt(), dto.getConferenceFeeZc(), dto.getBusinessFeeBt(), dto.getBusinessFeeZc(),
                dto.getCooperationFeeBt(), dto.getCooperationFeeZc(), dto.getPropertyFeeBt(), dto.getPropertyFeeZc(),
                dto.getLaborFeeBt(), dto.getLaborFeeZc(), dto.getStaffFeeBt(), dto.getStaffFeeZc(),
                dto.getConsultingFeeBt(), dto.getConsultingFeeZc(), dto.getManagementFeeBt(), dto.getManagementFeeZc(),
                dto.getOtherFeeBt(), dto.getOtherFeeZc(), dto.getPerformanceFeeBt(), dto.getPerformanceFeeZc(),
                dto.getInfrastructureFeeBt(), dto.getInfrastructureFeeZc())) {
            requireNonNegative(value, null);
        }
    }

    private void assertBudgetAmountsNonNegative(RdmsBudgetResearchDto dto) {
        requireNonNegative(dto.getEquipmentFeeApprovedBt(), "设备费国拨");
        requireNonNegative(dto.getEquipmentFeeApprovedZc(), "设备费自筹");
        requireNonNegative(dto.getTestFeeApprovedBt(), "测试化验加工费国拨");
        requireNonNegative(dto.getTestFeeApprovedZc(), "测试化验加工费自筹");
        requireNonNegative(dto.getMaterialFeeApprovedBt(), "材料费国拨");
        requireNonNegative(dto.getMaterialFeeApprovedZc(), "材料费自筹");
        requireNonNegative(dto.getPowerFeeApprovedBt(), "燃料动力费国拨");
        requireNonNegative(dto.getPowerFeeApprovedZc(), "燃料动力费自筹");
        requireNonNegative(dto.getConferenceFeeApprovedBt(), "会议费国拨");
        requireNonNegative(dto.getConferenceFeeApprovedZc(), "会议费自筹");
        requireNonNegative(dto.getBusinessFeeApprovedBt(), "差旅费国拨");
        requireNonNegative(dto.getBusinessFeeApprovedZc(), "差旅费自筹");
        requireNonNegative(dto.getCooperationFeeApprovedBt(), "合作交流费国拨");
        requireNonNegative(dto.getCooperationFeeApprovedZc(), "合作交流费自筹");
        requireNonNegative(dto.getPropertyFeeApprovedBt(), "知识产权费国拨");
        requireNonNegative(dto.getPropertyFeeApprovedZc(), "知识产权费自筹");
        requireNonNegative(dto.getLaborFeeApprovedBt(), "劳务费国拨");
        requireNonNegative(dto.getLaborFeeApprovedZc(), "劳务费自筹");
        requireNonNegative(dto.getStaffFeeApprovedBt(), "人员费国拨");
        requireNonNegative(dto.getStaffFeeApprovedZc(), "人员费自筹");
        requireNonNegative(dto.getConsultingFeeApprovedBt(), "专家咨询费国拨");
        requireNonNegative(dto.getConsultingFeeApprovedZc(), "专家咨询费自筹");
        requireNonNegative(dto.getManagementFeeApprovedBt(), "管理费国拨");
        requireNonNegative(dto.getManagementFeeApprovedZc(), "管理费自筹");
        requireNonNegative(dto.getOtherFeeApprovedBt(), "其他费用国拨");
        requireNonNegative(dto.getOtherFeeApprovedZc(), "其他费用自筹");
        requireNonNegative(dto.getPerformanceFeeApprovedBt(), "绩效费国拨");
        requireNonNegative(dto.getPerformanceFeeApprovedZc(), "绩效费自筹");
        requireNonNegative(dto.getInfrastructureFeeApprovedBt(), "基建费国拨");
        requireNonNegative(dto.getInfrastructureFeeApprovedZc(), "基建费自筹");
    }

    private void requireNonNegative(BigDecimal value, String fieldName) {
        if (value != null && value.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(BusinessExceptionCode.INPUT_INFORMATION_ERROR);
        }
    }

    /**
     * 仅覆盖科目金额；结构字段由调用方设置。批准金额同步写入可用额度（保持现有业务语义）。
     */
    private RdmsBudgetResearch applyBudgetFeeWhitelist(RdmsBudgetResearchDto base, RdmsBudgetResearchDto dto) {
        RdmsBudgetResearch entity = ObjectUtils.isEmpty(base)
                ? new RdmsBudgetResearch()
                : CopyUtil.copy(base, RdmsBudgetResearch.class);

        BigDecimal equipmentBt = nz(dto.getEquipmentFeeApprovedBt());
        BigDecimal equipmentZc = nz(dto.getEquipmentFeeApprovedZc());
        entity.setEquipmentFeeApprovedBt(equipmentBt);
        entity.setEquipmentFeeApprovedZc(equipmentZc);
        entity.setEquipmentFeeBt(equipmentBt);
        entity.setEquipmentFeeZc(equipmentZc);

        BigDecimal testBt = nz(dto.getTestFeeApprovedBt());
        BigDecimal testZc = nz(dto.getTestFeeApprovedZc());
        entity.setTestFeeApprovedBt(testBt);
        entity.setTestFeeApprovedZc(testZc);
        entity.setTestFeeBt(testBt);
        entity.setTestFeeZc(testZc);

        BigDecimal materialBt = nz(dto.getMaterialFeeApprovedBt());
        BigDecimal materialZc = nz(dto.getMaterialFeeApprovedZc());
        entity.setMaterialFeeApprovedBt(materialBt);
        entity.setMaterialFeeApprovedZc(materialZc);
        entity.setMaterialFeeBt(materialBt);
        entity.setMaterialFeeZc(materialZc);

        BigDecimal powerBt = nz(dto.getPowerFeeApprovedBt());
        BigDecimal powerZc = nz(dto.getPowerFeeApprovedZc());
        entity.setPowerFeeApprovedBt(powerBt);
        entity.setPowerFeeApprovedZc(powerZc);
        entity.setPowerFeeBt(powerBt);
        entity.setPowerFeeZc(powerZc);

        BigDecimal conferenceBt = nz(dto.getConferenceFeeApprovedBt());
        BigDecimal conferenceZc = nz(dto.getConferenceFeeApprovedZc());
        entity.setConferenceFeeApprovedBt(conferenceBt);
        entity.setConferenceFeeApprovedZc(conferenceZc);
        entity.setConferenceFeeBt(conferenceBt);
        entity.setConferenceFeeZc(conferenceZc);

        BigDecimal businessBt = nz(dto.getBusinessFeeApprovedBt());
        BigDecimal businessZc = nz(dto.getBusinessFeeApprovedZc());
        entity.setBusinessFeeApprovedBt(businessBt);
        entity.setBusinessFeeApprovedZc(businessZc);
        entity.setBusinessFeeBt(businessBt);
        entity.setBusinessFeeZc(businessZc);

        BigDecimal cooperationBt = nz(dto.getCooperationFeeApprovedBt());
        BigDecimal cooperationZc = nz(dto.getCooperationFeeApprovedZc());
        entity.setCooperationFeeApprovedBt(cooperationBt);
        entity.setCooperationFeeApprovedZc(cooperationZc);
        entity.setCooperationFeeBt(cooperationBt);
        entity.setCooperationFeeZc(cooperationZc);

        BigDecimal propertyBt = nz(dto.getPropertyFeeApprovedBt());
        BigDecimal propertyZc = nz(dto.getPropertyFeeApprovedZc());
        entity.setPropertyFeeApprovedBt(propertyBt);
        entity.setPropertyFeeApprovedZc(propertyZc);
        entity.setPropertyFeeBt(propertyBt);
        entity.setPropertyFeeZc(propertyZc);

        BigDecimal laborBt = nz(dto.getLaborFeeApprovedBt());
        BigDecimal laborZc = nz(dto.getLaborFeeApprovedZc());
        entity.setLaborFeeApprovedBt(laborBt);
        entity.setLaborFeeApprovedZc(laborZc);
        entity.setLaborFeeBt(laborBt);
        entity.setLaborFeeZc(laborZc);

        BigDecimal staffBt = nz(dto.getStaffFeeApprovedBt());
        BigDecimal staffZc = nz(dto.getStaffFeeApprovedZc());
        entity.setStaffFeeApprovedBt(staffBt);
        entity.setStaffFeeApprovedZc(staffZc);
        entity.setStaffFeeBt(staffBt);
        entity.setStaffFeeZc(staffZc);

        BigDecimal consultingBt = nz(dto.getConsultingFeeApprovedBt());
        BigDecimal consultingZc = nz(dto.getConsultingFeeApprovedZc());
        entity.setConsultingFeeApprovedBt(consultingBt);
        entity.setConsultingFeeApprovedZc(consultingZc);
        entity.setConsultingFeeBt(consultingBt);
        entity.setConsultingFeeZc(consultingZc);

        BigDecimal managementBt = nz(dto.getManagementFeeApprovedBt());
        BigDecimal managementZc = nz(dto.getManagementFeeApprovedZc());
        entity.setManagementFeeApprovedBt(managementBt);
        entity.setManagementFeeApprovedZc(managementZc);
        entity.setManagementFeeBt(managementBt);
        entity.setManagementFeeZc(managementZc);

        BigDecimal otherBt = nz(dto.getOtherFeeApprovedBt());
        BigDecimal otherZc = nz(dto.getOtherFeeApprovedZc());
        entity.setOtherFeeApprovedBt(otherBt);
        entity.setOtherFeeApprovedZc(otherZc);
        entity.setOtherFeeBt(otherBt);
        entity.setOtherFeeZc(otherZc);

        BigDecimal performanceBt = nz(dto.getPerformanceFeeApprovedBt());
        BigDecimal performanceZc = nz(dto.getPerformanceFeeApprovedZc());
        entity.setPerformanceFeeApprovedBt(performanceBt);
        entity.setPerformanceFeeApprovedZc(performanceZc);
        entity.setPerformanceFeeBt(performanceBt);
        entity.setPerformanceFeeZc(performanceZc);

        BigDecimal infrastructureBt = nz(dto.getInfrastructureFeeApprovedBt());
        BigDecimal infrastructureZc = nz(dto.getInfrastructureFeeApprovedZc());
        entity.setInfrastructureFeeApprovedBt(infrastructureBt);
        entity.setInfrastructureFeeApprovedZc(infrastructureZc);
        entity.setInfrastructureFeeBt(infrastructureBt);
        entity.setInfrastructureFeeZc(infrastructureZc);

        return entity;
    }

    private BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

}
