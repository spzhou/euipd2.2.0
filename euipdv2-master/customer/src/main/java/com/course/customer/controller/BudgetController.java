/**
 * Author：周朔鹏
 * 编制时间：2025-08-05

 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */

package com.course.customer.controller;

import com.course.server.domain.*;
import com.course.server.security.tenant.TenantAccess;
import com.course.server.dto.PageDto;
import com.course.server.dto.ResponseDto;
import com.course.server.dto.rdms.RdmsBudgetAdjustDto;
import com.course.server.dto.rdms.RdmsBudgetDto;
import com.course.server.enums.rdms.*;
import com.course.server.service.rdms.RdmsBudgetAdjustService;
import com.course.server.service.rdms.RdmsBudgetApplicationService;
import com.course.server.service.rdms.RdmsBudgetService;
import com.course.server.service.rdms.RdmsCustomerUserService;
import com.course.server.util.CopyUtil;
import com.course.server.util.ValidatorUtil;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/budget-mng")
public class BudgetController {
    public static final String BUSINESS_NAME = "预算管理";

    @Resource
    private RdmsBudgetService rdmsBudgetService;
    @Resource
    private RdmsBudgetApplicationService rdmsBudgetApplicationService;
    @Resource
    private RdmsBudgetAdjustService rdmsBudgetAdjustService;
    @Resource
    private RdmsCustomerUserService rdmsCustomerUserService;

    @PostMapping("/list")
    public ResponseDto<PageDto<RdmsBudgetDto>> list(@RequestBody PageDto<RdmsBudgetDto> pageDto) {
        ResponseDto<PageDto<RdmsBudgetDto>> responseDto = new ResponseDto<>();

        ValidatorUtil.require(pageDto.getPage(), "页码");
        ValidatorUtil.require(pageDto.getSize(), "条目数");
        ValidatorUtil.require(pageDto.getCustomerId(), "机构ID");
        ValidatorUtil.require(pageDto.getLoginUserId(), "登录用户");

        ValidatorUtil.length(pageDto.getCustomerId(), "需求名称", 8, 8);

        rdmsBudgetService.list(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

    @PostMapping("/getBudgetManageJobNumByUserId/{userId}")
    public ResponseDto<Long> getBudgetManageJobNumByUserId(@PathVariable String userId) {
        userId = TenantAccess.resolveMemberId(userId);
        ResponseDto<Long> responseDto = new ResponseDto<>();
        long testManageNum = rdmsBudgetService.getBudgetManageJobNumByUserId(userId);
        responseDto.setContent(testManageNum);
        return responseDto;
    }

    @PostMapping("/getProjectAndApplicationBudgetItems/{budgetId}")
    public ResponseDto<RdmsBudgetDto> getProjectAndApplicationBudgetItems(@PathVariable String budgetId) {
        ResponseDto<RdmsBudgetDto> responseDto = new ResponseDto<>();
        RdmsBudgetDto projectAndBudgetList = rdmsBudgetService.getProjectAndApplicationBudgetItems(budgetId);
        responseDto.setContent(projectAndBudgetList);
        return responseDto;
    }

    @PostMapping("/getProjectAndApplicationCompleteBudgetItems/{budgetId}")
    public ResponseDto<RdmsBudgetDto> getProjectAndApplicationCompleteBudgetItems(@PathVariable String budgetId) {
        ResponseDto<RdmsBudgetDto> responseDto = new ResponseDto<>();
        RdmsBudgetDto projectAndBudgetList = rdmsBudgetService.getProjectAndApplicationCompleteBudgetItems(budgetId);
        responseDto.setContent(projectAndBudgetList);
        return responseDto;
    }

    @PostMapping("/saveBudgetApplication")
    public ResponseDto<String> saveBudgetApplication(@RequestBody RdmsBudgetAdjustDto budgetAdjustDto) {
        ResponseDto<String> responseDto = new ResponseDto<>();
        responseDto.setContent(rdmsBudgetApplicationService.saveApplication(budgetAdjustDto));
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    @PostMapping("/saveBudgetDistribution")
    public ResponseDto<String> saveBudgetDistribution(@RequestBody RdmsBudgetAdjustDto budgetAdjustDto) {
        ResponseDto<String> responseDto = new ResponseDto<>();
        responseDto.setContent(rdmsBudgetApplicationService.saveDistribution(budgetAdjustDto));
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    @PostMapping("/getExamineNum/{projectId}")
    public ResponseDto<Integer> getExamineNum(@PathVariable String projectId) {
        ResponseDto<Integer> responseDto = new ResponseDto<>();
        List<BudgetApplicantStatusEnum> statusEnumList = new ArrayList<>();
        statusEnumList.add(BudgetApplicantStatusEnum.SUBMIT);
        Integer examineNum = rdmsBudgetAdjustService.getBudgetAdjustListByProjectIdAndStatusList(projectId, statusEnumList);
        responseDto.setContent(examineNum);
        return responseDto;
    }

    @PostMapping("/getBudgetSimpleInfo/{budgetId}")
    public ResponseDto<RdmsBudgetDto> getBudgetSimpleInfo(@PathVariable String budgetId) {
        ResponseDto<RdmsBudgetDto> responseDto = new ResponseDto<>();
        RdmsBudget rdmsBudget = rdmsBudgetService.selectByPrimaryKey(budgetId);
        RdmsBudgetDto budgetDto = CopyUtil.copy(rdmsBudget, RdmsBudgetDto.class);
        responseDto.setContent(budgetDto);
        return responseDto;
    }

    @PostMapping("/getBudgetAdjustSimpleInfo/{budgetAdjustId}")
    public ResponseDto<RdmsBudgetAdjustDto> getBudgetAdjustSimpleInfo(@PathVariable String budgetAdjustId) {
        ResponseDto<RdmsBudgetAdjustDto> responseDto = new ResponseDto<>();
        RdmsBudgetAdjust budgetAdjust = rdmsBudgetAdjustService.selectByPrimaryKey(budgetAdjustId);
        RdmsBudgetAdjustDto budgetAdjustDto = CopyUtil.copy(budgetAdjust, RdmsBudgetAdjustDto.class);
        if (!ObjectUtils.isEmpty(budgetAdjustDto)) {
            RdmsCustomerUser rdmsCustomerUser = rdmsCustomerUserService.selectByPrimaryKey(budgetAdjustDto.getApplicantId());
            budgetAdjustDto.setApplicantName(rdmsCustomerUser.getTrueName());
            RdmsCustomerUser rdmsCustomerUser1 = rdmsCustomerUserService.selectByPrimaryKey(budgetAdjustDto.getApproverId());
            budgetAdjustDto.setApproverName(rdmsCustomerUser1.getTrueName());
        }
        responseDto.setContent(budgetAdjustDto);
        return responseDto;
    }

    @PostMapping("/approvalPass")
    public ResponseDto<String> approvalPass(@RequestBody RdmsBudgetAdjustDto budgetAdjustDto) {
        ResponseDto<String> responseDto = new ResponseDto<>();
        responseDto.setContent(rdmsBudgetApplicationService.approve(budgetAdjustDto));
        return responseDto;
    }

    @PostMapping("/approvalReject")
    public ResponseDto<String> approvalReject(@RequestBody RdmsBudgetAdjustDto budgetAdjustDto) {
        ResponseDto<String> responseDto = new ResponseDto<>();
        responseDto.setContent(rdmsBudgetApplicationService.reject(budgetAdjustDto));
        return responseDto;
    }

}
