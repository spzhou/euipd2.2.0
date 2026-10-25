/**
 * Author：周朔鹏
 * 编制时间：2025-08-05

 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */

package com.course.customer.controller;

import com.course.server.domain.RdmsBudgetResearchExe;
import com.course.server.domain.RdmsCustomerUser;
import com.course.server.domain.RdmsProject;
import com.course.server.domain.RdmsProjectSubproject;
import com.course.server.dto.PageDto;
import com.course.server.dto.RdmsBudgetResearchExeDto;
import com.course.server.dto.ResponseDto;
import com.course.server.dto.rdms.RdmsHmiBudgeStatisticsDto;
import com.course.server.enums.rdms.BudgetClassifyStdEnum;
import com.course.server.enums.rdms.BudgetTypeEnum;
import com.course.server.enums.rdms.CapitalSourceEnum;
import com.course.server.enums.rdms.PayClassifyEnum;
import com.course.server.enums.rdms.PaymentStatusEnum;
import com.course.server.exception.BusinessException;
import com.course.server.exception.BusinessExceptionCode;
import com.course.server.security.tenant.TenantAccess;
import com.course.server.security.tenant.TenantContext;
import com.course.server.dto.rdms.statistics.StatScopeDto;
import com.course.server.service.rdms.statistics.StatisticsScopeService;
import com.course.server.service.rdms.*;
import com.course.server.util.ValidatorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/budget-direct-exe")
public class BudgetResearchExeController {
    private static final Logger LOG = LoggerFactory.getLogger(BudgetResearchExeController.class);
    public static final String BUSINESS_NAME = "直接立项预算执行管理";

    @Resource
    private RdmsBudgetResearchExeService rdmsBudgetResearchExeService;
    @Resource
    private StatisticsScopeService statisticsScopeService;
    @Resource
    private RdmsCustomerUserService rdmsCustomerUserService;

    @Resource
    private RdmsIpmtService rdmsIpmtService;
    @Autowired
    private RdmsProjectService rdmsProjectService;
    @Resource
    private RdmsSubprojectService rdmsSubprojectService;
    @Resource
    private JobActorGuard jobActorGuard;

    @PostMapping("/listByKeywordIsSubprojectId")
    public ResponseDto<PageDto<RdmsBudgetResearchExeDto>> listByKeywordIsSubprojectId(@RequestBody PageDto<RdmsBudgetResearchExeDto> pageDto) {
        ResponseDto<PageDto<RdmsBudgetResearchExeDto>> responseDto = new ResponseDto<>();
        ValidatorUtil.require(pageDto.getKeyWord(), "子项目ID");
        assertSubprojectTenant(pageDto.getKeyWord());
        rdmsBudgetResearchExeService.listByKeywordIsSubprojectId(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

    @PostMapping("/listByKeywordIsProjectId")
    public ResponseDto<PageDto<RdmsBudgetResearchExeDto>> listByKeywordIsProjectId(@RequestBody PageDto<RdmsBudgetResearchExeDto> pageDto) {
        ResponseDto<PageDto<RdmsBudgetResearchExeDto>> responseDto = new ResponseDto<>();
        ValidatorUtil.require(pageDto.getKeyWord(), "项目ID");
        assertProjectTenant(pageDto.getKeyWord());
        rdmsBudgetResearchExeService.listByKeywordIsProjectId(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

    /**
     * 保存客户信息
     */
    @PostMapping("/saveBudgetExeList")
    @Transactional
    public ResponseDto<List<String>> saveBudgetExeList(@RequestBody List<RdmsBudgetResearchExeDto> budgetExeList) {
        ResponseDto<List<String>> responseDto = new ResponseDto<>();
        ValidatorUtil.listNotEmpty(budgetExeList, "预算执行清单");
        for (RdmsBudgetResearchExeDto item : budgetExeList) {
            RdmsProjectSubproject subproject = requireSubproject(item.getSubprojectId());
            String tenantId = assertSubprojectTenant(item.getSubprojectId());
            ValidatorUtil.require(item.getSerialNo(), "支出序号");
            ValidatorUtil.length(item.getSerialNo(), "支出序号", 2, 50);
            if (BudgetClassifyStdEnum.getBudgetClassifyEnumByClassify(item.getClassify()) == null
                    || CapitalSourceEnum.getCapitalSourceEnumBySource(item.getCapitalSource()) == null
                    || PaymentStatusEnum.getPaymentStatusEnumByStatus(item.getPaymentStatue()) == null) {
                throw new BusinessException(BusinessExceptionCode.INPUT_ERROR, "费用科目、资金来源或支付状态取值错误");
            }
            if (item.getAmount() == null || item.getAmount().signum() <= 0) {
                throw new BusinessException(BusinessExceptionCode.INPUT_ERROR, "支出数额须大于 0");
            }
            item.setId(null);
            item.setCustomerId(tenantId);
            item.setProjectId(subproject.getProjectId());
            item.setPreprojectId(null);
            item.setPaymentTime(PaymentStatusEnum.PAID.getStatus().equals(item.getPaymentStatue()) ? new Date() : null);
        }
        List<String> strings = rdmsBudgetResearchExeService.saveBudgetExeList(budgetExeList);
        responseDto.setContent(strings);
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    /**
     * 只改手工录入流水的支付状态，且只允许应付未付转已支付；系统流水由各自的审批、评价流程转已支付。
     */
    @PostMapping("/updatePaymentStatus/{id}")
    @Transactional
    public ResponseDto<String> updatePaymentStatus(@PathVariable String id) {
        ResponseDto<String> responseDto = new ResponseDto<>();
        RdmsBudgetResearchExe exe = requireManualExe(id);
        if (!PaymentStatusEnum.UNPAID.getStatus().equals(exe.getPaymentStatue())) {
            throw new BusinessException(BusinessExceptionCode.LIFECYCLE_TRANSITION_FORBIDDEN);
        }
        exe.setPaymentStatue(PaymentStatusEnum.PAID.getStatus());
        exe.setPaymentTime(new Date());
        responseDto.setContent(rdmsBudgetResearchExeService.update(exe));
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    @PostMapping("/getSumBudgetBySubprojectId/{subprojectId}")
    @Transactional
    public ResponseDto<BigDecimal> getSumBudgetBySubprojectId(@PathVariable String subprojectId) {
        ResponseDto<BigDecimal> responseDto = new ResponseDto<>();
        assertSubprojectTenant(subprojectId);
        BigDecimal sumBudget = rdmsBudgetResearchExeService.getActiveBudgetBySubprojectId(subprojectId);
        responseDto.setContent(sumBudget);
        return responseDto;
    }

    @PostMapping("/getBudgetExecutionSummary/{subprojectId}")
    @Transactional
    public ResponseDto<RdmsHmiBudgeStatisticsDto> getBudgetExecutionSummary(@PathVariable String subprojectId) {
        ResponseDto<RdmsHmiBudgeStatisticsDto> responseDto = new ResponseDto<>();
        assertSubprojectTenant(subprojectId);
        RdmsHmiBudgeStatisticsDto budgetExecutionSummary = rdmsBudgetResearchExeService.getBudgetExecutionSummary(subprojectId);
        responseDto.setContent(budgetExecutionSummary);
        return responseDto;
    }

    @PostMapping("/getBudgetExecutionSummary_preproject/{preprojectId}")
    @Transactional
    public ResponseDto<RdmsHmiBudgeStatisticsDto> getBudgetExecutionSummary_preproject(@PathVariable String preprojectId) {
        ResponseDto<RdmsHmiBudgeStatisticsDto> responseDto = new ResponseDto<>();
        // 预立项租户在服务层按记录校验；此处仅要求 ID
        ValidatorUtil.require(preprojectId, "预立项ID");
        RdmsHmiBudgeStatisticsDto budgetExecutionSummary = rdmsBudgetResearchExeService.getBudgetExecutionSummary_preproject(preprojectId);
        responseDto.setContent(budgetExecutionSummary);
        return responseDto;
    }

    @PostMapping("/getBudgetExecutionSummary_mainProject/{projectId}")
    @Transactional
    public ResponseDto<RdmsHmiBudgeStatisticsDto> getBudgetExecutionSummary_mainProject(@PathVariable String projectId) {
        ResponseDto<RdmsHmiBudgeStatisticsDto> responseDto = new ResponseDto<>();
        assertProjectTenant(projectId);
        RdmsHmiBudgeStatisticsDto budgetExecutionSummary = rdmsBudgetResearchExeService.getBudgetExecutionSummary_mainProject(projectId);
        responseDto.setContent(budgetExecutionSummary);
        return responseDto;
    }

    @PostMapping("/getBudgetExecutionSummary_product/{projectId}")
    @Transactional
    public ResponseDto<RdmsHmiBudgeStatisticsDto> getBudgetExecutionSummary_product(@PathVariable String projectId) {
        ResponseDto<RdmsHmiBudgeStatisticsDto> responseDto = new ResponseDto<>();
        assertProjectTenant(projectId);
        RdmsHmiBudgeStatisticsDto budgetExecutionSummary = rdmsBudgetResearchExeService.getBudgetExecutionSummary_product(projectId);
        responseDto.setContent(budgetExecutionSummary);
        return responseDto;
    }

    @PostMapping("/getMainProjectBudgetExecutionSummaryList/{customerId}")
    @Transactional
    public ResponseDto<List<RdmsHmiBudgeStatisticsDto>> getMainProjectBudgetExecutionSummaryList(@PathVariable String customerId) {
        ResponseDto<List<RdmsHmiBudgeStatisticsDto>> responseDto = new ResponseDto<>();
        List<RdmsHmiBudgeStatisticsDto> mainProjectBudgetExecutionSummaryList = rdmsBudgetResearchExeService.getMainProjectBudgetExecutionSummaryList(customerId);
        responseDto.setContent(mainProjectBudgetExecutionSummaryList);
        return responseDto;
    }

    @PostMapping("/getBudgetStageExecutionSummary_project/{customerId}")
    @Transactional
    public ResponseDto<RdmsHmiBudgeStatisticsDto> getBudgetStageExecutionSummary_project(@PathVariable String customerId) {
        ResponseDto<RdmsHmiBudgeStatisticsDto> responseDto = new ResponseDto<>();
        RdmsHmiBudgeStatisticsDto budgetExecutionSummary = rdmsBudgetResearchExeService.getBudgetStageExecutionSummary(customerId, BudgetTypeEnum.PROJECT);
        responseDto.setContent(budgetExecutionSummary);
        return responseDto;
    }

    @PostMapping("/getBudgetStageExecutionSummary_product/{customerId}")
    @Transactional
    public ResponseDto<RdmsHmiBudgeStatisticsDto> getBudgetStageExecutionSummary_product(@PathVariable String customerId) {
        ResponseDto<RdmsHmiBudgeStatisticsDto> responseDto = new ResponseDto<>();
        RdmsHmiBudgeStatisticsDto budgetExecutionSummary = rdmsBudgetResearchExeService.getBudgetStageExecutionSummary(customerId, BudgetTypeEnum.PRODUCT);
        responseDto.setContent(budgetExecutionSummary);
        return responseDto;
    }

    @PostMapping("/getBudgetStageExecutionSummary_preProject/{customerId}")
    @Transactional
    public ResponseDto<RdmsHmiBudgeStatisticsDto> getBudgetStageExecutionSummary_preProject(@PathVariable String customerId) {
        ResponseDto<RdmsHmiBudgeStatisticsDto> responseDto = new ResponseDto<>();
        RdmsHmiBudgeStatisticsDto budgetExecutionSummary = rdmsBudgetResearchExeService.getBudgetStageExecutionSummary(customerId, BudgetTypeEnum.PRE_PROJECT);
        responseDto.setContent(budgetExecutionSummary);
        return responseDto;
    }

    @PostMapping("/getBudgetExecutionSummary_customer/{customerId}")
    @Transactional
    public ResponseDto<RdmsHmiBudgeStatisticsDto> getBudgetExecutionSummary_customer(@PathVariable String customerId) {
        ResponseDto<RdmsHmiBudgeStatisticsDto> responseDto = new ResponseDto<>();
        RdmsHmiBudgeStatisticsDto budgetExecutionSummary = rdmsBudgetResearchExeService.getCustomerBudgetExecutionSummary(customerId);
        responseDto.setContent(budgetExecutionSummary);
        return responseDto;
    }

    @PostMapping("/getMainProjectBudgetExecutionSummary/{projectId}")
    @Transactional
    public ResponseDto<RdmsHmiBudgeStatisticsDto> getMainProjectBudgetExecutionSummary(@PathVariable String projectId) {
        ResponseDto<RdmsHmiBudgeStatisticsDto> responseDto = new ResponseDto<>();
        RdmsHmiBudgeStatisticsDto mainProjectBudgetExecutionSummary = rdmsBudgetResearchExeService.getMainProjectBudgetExecutionSummary(projectId);
        responseDto.setContent(mainProjectBudgetExecutionSummary);
        return responseDto;
    }

    /** 范围内已立项主项目的预算执行汇总；人员与机构取自登录身份 */
    @PostMapping("/getUnderlineProjectBudgetExecutionSummaryList")
    public ResponseDto<List<RdmsHmiBudgeStatisticsDto>> getUnderlineProjectBudgetExecutionSummaryList(@RequestParam(required = false) String customerUserId, String customerId) {
        ResponseDto<List<RdmsHmiBudgeStatisticsDto>> responseDto = new ResponseDto<>();
        StatScopeDto scope = statisticsScopeService.resolve();
        responseDto.setContent(rdmsBudgetResearchExeService.getSetupedProjectBudgetExecutionSummaryList(scope.getProjectIds()));
        return responseDto;
    }

    /**
     * 删除
     */
    @DeleteMapping("/del/{id}")
    @Transactional
    public ResponseDto delete(@PathVariable String id) {
        ResponseDto responseDto = new ResponseDto();
        requireManualExe(id);
        rdmsBudgetResearchExeService.delete(id);
        return responseDto;
    }

    /** 手工录入流水（payClassify=SUB_PROJECT）且操作人为本租户最终管理人 */
    private RdmsBudgetResearchExe requireManualExe(String id) {
        ValidatorUtil.require(id, "执行流水ID");
        RdmsBudgetResearchExe exe = rdmsBudgetResearchExeService.selectByPrimaryKey(id);
        if (ObjectUtils.isEmpty(exe) || Integer.valueOf(1).equals(exe.getDeleted())) {
            throw new BusinessException(BusinessExceptionCode.DB_RECORD_NOT_EXIST);
        }
        String tenantId = StringUtils.hasText(exe.getCustomerId())
                ? exe.getCustomerId() : assertSubprojectTenant(exe.getSubprojectId());
        TenantAccess.assertSameTenant(tenantId);
        if (!PayClassifyEnum.SUB_PROJECT.getClassify().equals(exe.getPayClassify())) {
            throw new BusinessException(BusinessExceptionCode.LIFECYCLE_TRANSITION_FORBIDDEN);
        }
        String operatorId = TenantContext.getMembershipId();
        if (!jobActorGuard.hasFullAuthority(tenantId, operatorId)) {
            throw new BusinessException(BusinessExceptionCode.NO_PERMISSION);
        }
        return exe;
    }

    private RdmsProjectSubproject requireSubproject(String subprojectId) {
        ValidatorUtil.require(subprojectId, "子项目ID");
        RdmsProjectSubproject subproject = rdmsSubprojectService.selectByPrimaryKey(subprojectId);
        if (ObjectUtils.isEmpty(subproject)) {
            throw new BusinessException(BusinessExceptionCode.DB_RECORD_NOT_EXIST);
        }
        return subproject;
    }

    /** 校验子项目存在且属于当前租户，返回租户标识 */
    private String assertSubprojectTenant(String subprojectId) {
        RdmsProjectSubproject subproject = requireSubproject(subprojectId);
        String tenantId = subproject.getCustomerId();
        if (!StringUtils.hasText(tenantId) && StringUtils.hasText(subproject.getProjectId())) {
            RdmsProject project = rdmsProjectService.selectByPrimaryKey(subproject.getProjectId());
            if (ObjectUtils.isEmpty(project)) {
                throw new BusinessException(BusinessExceptionCode.DB_RECORD_NOT_EXIST);
            }
            tenantId = project.getCustomerId();
        }
        if (!StringUtils.hasText(tenantId)) {
            throw new BusinessException(BusinessExceptionCode.INPUT_INFORMATION_ERROR);
        }
        TenantAccess.assertSameTenant(tenantId);
        return tenantId;
    }

    /** 校验主项目存在且属于当前租户 */
    private void assertProjectTenant(String projectId) {
        ValidatorUtil.require(projectId, "项目ID");
        RdmsProject project = rdmsProjectService.selectByPrimaryKey(projectId);
        if (ObjectUtils.isEmpty(project) || !StringUtils.hasText(project.getCustomerId())) {
            throw new BusinessException(BusinessExceptionCode.DB_RECORD_NOT_EXIST);
        }
        TenantAccess.assertSameTenant(project.getCustomerId());
    }

}
