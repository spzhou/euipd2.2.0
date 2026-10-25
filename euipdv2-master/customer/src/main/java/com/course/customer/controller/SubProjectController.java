/**
 * Author：周朔鹏
 * 编制时间：2025-08-05

 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */

package com.course.customer.controller;

import com.alibaba.fastjson.JSON;
import com.course.server.domain.*;
import com.course.server.dto.PageDto;
import com.course.server.dto.RdmsBudgetResearchDto;
import com.course.server.dto.ResponseDto;
import com.course.server.dto.rdms.*;
import com.course.server.dto.rdms.RdmsProjectSubprojectDto;
import com.course.server.enums.rdms.*;
import com.course.server.exception.BusinessException;
import com.course.server.exception.BusinessExceptionCode;
import com.course.server.exception.ValidatorException;
import com.course.server.security.tenant.TenantAccess;
import com.course.server.service.calculate.CalBudgetService;
import com.course.server.service.rdms.*;
import com.course.server.service.rdms.helper.BudgetExeStatisticsBuilder;
import com.course.server.service.rdms.helper.TenantParentGuard;
import com.course.server.service.util.DateUtil;
import com.course.server.util.CopyUtil;
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
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/sub-project")
public class SubProjectController {
    private static final Logger LOG = LoggerFactory.getLogger(SubProjectController.class);
    public static final String BUSINESS_NAME = "子项目";

    @Resource
    private JobActorGuard jobActorGuard;
    @Resource
    private RdmsSubprojectService rdmsSubprojectService;
    @Resource
    private RdmsProjectCloseService rdmsProjectCloseService;
    @Resource
    private RdmsCustomerUserService rdmsCustomerUserService;
    @Resource
    private RdmsCharacterService rdmsCharacterService;
    @Resource
    private RdmsProjectService rdmsProjectService;
    @Resource
    private RdmsCharacterProcessService rdmsCharacterProcessService;
    @Resource
    private RdmsJobItemService rdmsJobItemService;
    @Resource
    private RdmsManhourService rdmsManhourService;
    @Resource
    private RdmsBudgetExeService rdmsBudgetExeService;
    @Resource
    private RdmsMaterialManageService rdmsMaterialManageService;
    @Resource
    private RdmsFeeManageService rdmsFeeManageService;
    @Resource
    private RdmsBudgetResearchService rdmsBudgetResearchService;
    @Resource
    private RdmsProjectSecLevelService rdmsProjectSecLevelService;
    @Resource
    private RdmsCharacterSecLevelService rdmsCharacterSecLevelService;
    @Autowired
    private RdmsBossService rdmsBossService;
    @Autowired
    private RdmsGanttService rdmsGanttService;
    @Autowired
    private RdmsCbbService rdmsCbbService;
    @Resource
    private RdmsMilestoneService rdmsMilestoneService;
    @Resource
    private CalBudgetService calBudgetService;
    @Resource
    private RdmsSubprojectDecomposeService rdmsSubprojectDecomposeService;
    @Resource
    private TenantParentGuard tenantParentGuard;

    @PostMapping("/save")
    public ResponseDto<RdmsProjectSubprojectDto> save(@RequestBody RdmsProjectSubprojectDto subprojectDto) {
        if (subprojectDto == null) {
            throw new BusinessException(BusinessExceptionCode.INPUT_INFORMATION_ERROR);
        }

        ValidatorUtil.require(subprojectDto.getCustomerId(), "公司ID");
        ValidatorUtil.require(subprojectDto.getProjectId(), "项目ID");
        ValidatorUtil.require(subprojectDto.getParent(), "上级项目ID");

        ValidatorUtil.require(subprojectDto.getLabel(), "子项目名称");
        ValidatorUtil.require(subprojectDto.getIntroduce(), "子项目概要");
        ValidatorUtil.require(subprojectDto.getIncentivePolicy(), "激励政策");
        ValidatorUtil.require(subprojectDto.getProjectManagerId(), "子项目经理");
        ValidatorUtil.require(subprojectDto.getTestManagerId(), "测试主管/经理");
        ValidatorUtil.require(subprojectDto.getSystemManagerId(), "系统工程师");
        ValidatorUtil.require(subprojectDto.getTargetMilestoneId(), "目标里程碑");
        ValidatorUtil.require(subprojectDto.getDeep(), "Deep值");
        ValidatorUtil.require(subprojectDto.getCreatorId(), "创建者");
        ValidatorUtil.require(subprojectDto.getProjectCode(), "项目Code");
        ValidatorUtil.require(subprojectDto.getReleaseTime(), "评审时间");
        ValidatorUtil.require(subprojectDto.getBudgetData(), "预算表");

        ValidatorUtil.length(subprojectDto.getLabel(), "子项目名称", 2, 30);
        ValidatorUtil.length(subprojectDto.getIntroduce(), "子项目概要", 5, 1500);
        ValidatorUtil.length(subprojectDto.getIncentivePolicy(), "激励政策", 2, 500);
        ValidatorUtil.length(subprojectDto.getProjectCode(), "项目Code", 5, 50);

        ResponseDto<RdmsProjectSubprojectDto> responseDto = new ResponseDto<>();
        responseDto.setContent(rdmsSubprojectDecomposeService.createChild(subprojectDto));
        responseDto.setSuccess(true);
        responseDto.setMessage("创建成功");
        return responseDto;
    }

    @PostMapping("/save-module-subproject")
    @Transactional
    public ResponseDto<RdmsProjectSubprojectDto> saveModuleSubproject(@RequestBody RdmsProjectSubprojectDto subprojectDto) {

        ValidatorUtil.require(subprojectDto.getCustomerId(), "公司ID");
        ValidatorUtil.require(subprojectDto.getProjectId(), "项目ID");
        ValidatorUtil.require(subprojectDto.getParent(), "上级项目ID");

        ValidatorUtil.require(subprojectDto.getLabel(), "子项目名称");
        ValidatorUtil.require(subprojectDto.getIntroduce(), "子项目概要");
        ValidatorUtil.require(subprojectDto.getProjectManagerId(), "子项目经理");
        ValidatorUtil.require(subprojectDto.getDeep(), "Deep值");
        ValidatorUtil.require(subprojectDto.getCreatorId(), "创建者");
        ValidatorUtil.require(subprojectDto.getProjectCode(), "项目Code");

        ResponseDto<RdmsProjectSubprojectDto> responseDto = new ResponseDto<>();
        responseDto.setContent(rdmsSubprojectDecomposeService.createModuleChild(subprojectDto));
        responseDto.setSuccess(true);
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    /**
     * 根据后台数据,计算实时的项目执行情况数据
     *
     * @param subprojectId
     * @return
     */
    @PostMapping("/calSubprojectSelfBudgetActiveExeInfo/{subprojectId}")
    public ResponseDto<RdmsBudgetExeSummaryDto> getBudgetActiveExeSummaryBySubprojectId(@PathVariable String subprojectId) {
        ResponseDto<RdmsBudgetExeSummaryDto> responseDto = new ResponseDto<>();
        RdmsBudgetExeSummaryDto budgetExeSummaryBySubprojectId = rdmsManhourService.calSubprojectSelfBudgetActiveExeInfo(subprojectId);
        responseDto.setContent(budgetExeSummaryBySubprojectId);
        return responseDto;
    }

    /**
     * 根据后台数据,计算完工的项目执行情况数据
     *
     * @param subprojectId
     * @return
     */
    @PostMapping("/getBudgetExeSummaryBySubprojectId/{subprojectId}")
    public ResponseDto<RdmsBudgetExeSummaryDto> getCharacterBudgetExeSummary(@PathVariable String subprojectId) {
        tenantParentGuard.requireSubproject(subprojectId);
        ResponseDto<RdmsBudgetExeSummaryDto> responseDto = new ResponseDto<>();
        RdmsBudgetExeSummaryDto budgetExeSummaryBySubprojectId = rdmsManhourService.getBudgetExeSummaryBySubprojectId(subprojectId);
        responseDto.setContent(budgetExeSummaryBySubprojectId);
        return responseDto;
    }

    @PostMapping("/getTakePartInSubprojectList/{customerUserId}")
    public ResponseDto<List<RdmsProjectSubprojectDto>> getTakePartInSubprojectList(@PathVariable String customerUserId) {
        customerUserId = TenantAccess.resolveMemberId(customerUserId);
        ResponseDto<List<RdmsProjectSubprojectDto>> responseDto = new ResponseDto<>();
        List<RdmsProjectSubprojectDto> takePartInSubprojectList = rdmsSubprojectService.getTakePartInSubprojectList(customerUserId);
        responseDto.setContent(takePartInSubprojectList);
        return responseDto;
    }

    @PostMapping("/getCanBeOutsourcingFlag/{subproject}")
    public ResponseDto<Boolean> getCanBeOutsourcingFlag(@PathVariable String subproject) {
        ResponseDto<Boolean> responseDto = new ResponseDto<>();
        Boolean canBeOutsourcingFlag = rdmsSubprojectService.getCanBeOutsourcingFlag(subproject);
        responseDto.setContent(canBeOutsourcingFlag);
        return responseDto;
    }

    /**
     * 从数据库项目评审记录中读取相应的项目执行数据
     *
     * @param subprojectId
     * @return
     */
    @PostMapping("/getSubprojectBudgetExeInfoList/{subprojectId}")
    public ResponseDto<RdmsBudgetExeDto> getSubprojectBudgetExeInfoList(@PathVariable String subprojectId) {
        tenantParentGuard.requireSubproject(subprojectId);
        ResponseDto<RdmsBudgetExeDto> responseDto = new ResponseDto<>();
        RdmsBudgetExeDto subprojectBudgetExeSummary = rdmsManhourService.getSubprojectBudgetExeSummary(subprojectId);
        responseDto.setContent(subprojectBudgetExeSummary);
        return responseDto;
    }

    @PostMapping("/getSubprojectBudgetExeSummary_researchExe/{subprojectId}")
    public ResponseDto<RdmsBudgetExeDto> getSubprojectBudgetExeSummary_researchExe(@PathVariable String subprojectId) {
        tenantParentGuard.requireSubproject(subprojectId);
        ResponseDto<RdmsBudgetExeDto> responseDto = new ResponseDto<>();
        RdmsBudgetExeDto subprojectBudgetExeSummary = rdmsManhourService.getSubprojectBudgetExeSummary_researchExe(subprojectId);
        responseDto.setContent(subprojectBudgetExeSummary);
        return responseDto;
    }

    @PostMapping("/getSubprojectBoardInfo/{subprojectId}")
    public ResponseDto<RdmsBoardSubprojectDto> getSubprojectBoardInfo(@PathVariable String subprojectId) {
        ResponseDto<RdmsBoardSubprojectDto> responseDto = new ResponseDto<>();
        ValidatorUtil.require(subprojectId, "子项目ID");
        RdmsProjectSubproject subproject = rdmsSubprojectService.selectByPrimaryKey(subprojectId);
        if (ObjectUtils.isEmpty(subproject)) {
            throw new BusinessException(BusinessExceptionCode.DB_RECORD_NOT_EXIST);
        }
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
        RdmsBoardSubprojectDto subprojectBoardInfo = rdmsSubprojectService.getSubprojectBoardInfo_direct(subprojectId);
        responseDto.setContent(subprojectBoardInfo);
        return responseDto;
    }

    @PostMapping("/saveOutSourcing")
    public ResponseDto saveOutSourcing(@RequestBody RdmsSubprojectService.OutsourceParameter parameter) {
        ResponseDto responseDto = new ResponseDto<>();
        rdmsSubprojectService.saveOutSourcing(parameter);
        responseDto.setContent("");
        return responseDto;
    }

    /**
     * 根据后台数据, 计算项目执行情况的简要关键信息
     *
     * @param subprojectId
     * @return
     */
    @PostMapping("/getSubprojectAndChildrenExeSimpleInfo")
    public ResponseDto<List<RdmsSubprojectExeSimpleInfoDto>> getSubprojectAndChildrenExeSimpleInfo(@RequestParam String subprojectId, String loginUserId) {
        ResponseDto<List<RdmsSubprojectExeSimpleInfoDto>> responseDto = new ResponseDto<>();
        List<RdmsSubprojectExeSimpleInfoDto> subprojectExeSimpleInfoDtoList = new ArrayList<>();
        List<String> allChildrenSubprojectIdList = rdmsSubprojectService.getAllChildrenSubprojectId(subprojectId);

        for (String itemId : allChildrenSubprojectIdList) {
            RdmsSubprojectExeSimpleInfoDto childSubproject = rdmsSubprojectService.getSubprojectExeSimpleInfo(itemId, loginUserId);
            subprojectExeSimpleInfoDtoList.add(childSubproject);
        }
        responseDto.setContent(subprojectExeSimpleInfoDtoList);
        return responseDto;
    }

    @PostMapping("/getProjectAllReviewInfoList/{subprojectId}")
    public ResponseDto<List<RdmsBudgetExeDto>> getProjectAllReviewInfoList(@PathVariable String subprojectId) {
        tenantParentGuard.requireProjectOrSubproject(subprojectId);
        ResponseDto<List<RdmsBudgetExeDto>> responseDto = new ResponseDto<>();
        List<RdmsBudgetExe> childrenList = rdmsBudgetExeService.getChildrenListByParentId(subprojectId);
        List<RdmsBudgetExeDto> rdmsBudgetExeDtos = CopyUtil.copyList(childrenList, RdmsBudgetExeDto.class);
        List<RdmsBudgetExeDto> collect = rdmsBudgetExeDtos.stream().sorted(Comparator.comparing(RdmsBudgetExeDto::getCreateTime).reversed()).collect(Collectors.toList());
        responseDto.setContent(collect);
        return responseDto;
    }

    @PostMapping("/getReviewJobInfo")
    public ResponseDto<RdmsHmiJobItemDocPageDto> getLastReviewInfo(@RequestParam String subprojectId, String loginUserId) {
        tenantParentGuard.requireSubproject(subprojectId);
        ResponseDto<RdmsHmiJobItemDocPageDto> responseDto = new ResponseDto<>();
        RdmsHmiJobItemDocPageDto lastReviewInfo = rdmsSubprojectService.getReviewJobInfo(subprojectId, loginUserId);
        responseDto.setContent(lastReviewInfo);
        return responseDto;
    }

    @PostMapping("/listProjectTaskJobChargeBySubProjectId/{subprojectId}")
    public ResponseDto<BigDecimal> listProjectTaskJobChargeBySubProjectId(@PathVariable String subprojectId) {
        ResponseDto<BigDecimal> responseDto = new ResponseDto<>();
        if (ObjectUtils.isEmpty(subprojectId)) {
            return responseDto;
        }

        BigDecimal subprojectSelfActiveAccountCharge = rdmsManhourService.getSubprojectTaskActiveAccountCharge(subprojectId);
        responseDto.setContent(subprojectSelfActiveAccountCharge);
        return responseDto;
    }

    @PostMapping("/getCharacterListBySubProjectReviewJobitem/{reviewJobitemId}")
    public ResponseDto<List<RdmsBudgetExe>> getCharacterListBySubProjectReviewJobitem(@PathVariable String reviewJobitemId) {
        ResponseDto<List<RdmsBudgetExe>> responseDto = new ResponseDto<>();

        RdmsJobItem reviewJobitem = tenantParentGuard.requireJobItem(reviewJobitemId);
        List<RdmsBudgetExe> characterBudgetList = new ArrayList<>();

        List<String> subprojectIdCollect = rdmsSubprojectService.getAllChildrenSubprojectId(reviewJobitem.getSubprojectId());

        for (String subprojectId : subprojectIdCollect) {
            List<RdmsBudgetExe> characterBudgetExeInfoList = rdmsBudgetExeService.getItemListByParentAndeDocType(subprojectId, DocTypeEnum.CHARACTER);
            characterBudgetList.addAll(characterBudgetExeInfoList);
        }
        responseDto.setContent(characterBudgetList);
        return responseDto;
    }

    @PostMapping("/getCharacterListBySubprojectId/{subprojectId}")
    public ResponseDto<List<RdmsBudgetExe>> getCharacterListBySubprojectId(@PathVariable String subprojectId) {
        tenantParentGuard.requireSubproject(subprojectId);
        ResponseDto<List<RdmsBudgetExe>> responseDto = new ResponseDto<>();
        List<RdmsBudgetExe> characterBudgetList = new ArrayList<>();
        List<RdmsBudgetExe> characterBudgetExeInfoList = rdmsBudgetExeService.getItemListByParentAndeDocType(subprojectId, DocTypeEnum.CHARACTER);
        characterBudgetList.addAll(characterBudgetExeInfoList);
        responseDto.setContent(characterBudgetList);
        return responseDto;
    }

    @PostMapping("/getSubProjectCbbList/{subprojectId}")
    public ResponseDto<RdmsProjectSubprojectDto> getSubProjectCbbList(@PathVariable String subprojectId) {
        ResponseDto<RdmsProjectSubprojectDto> responseDto = new ResponseDto<>();
        RdmsProjectSubproject rdmsProjectSubproject = rdmsSubprojectService.selectByPrimaryKey(subprojectId);
        RdmsProjectSubprojectDto rdmsProjectSubprojectDto = CopyUtil.copy(rdmsProjectSubproject, RdmsProjectSubprojectDto.class);
        if(!ObjectUtils.isEmpty(rdmsProjectSubproject) && !ObjectUtils.isEmpty(rdmsProjectSubproject.getModuleIdListStr())){
            List<String> cbbIdList = JSON.parseArray(rdmsProjectSubproject.getModuleIdListStr(), String.class);
            if (!CollectionUtils.isEmpty(cbbIdList)) {
                try {
                    List<RdmsCbbDto> cbbListByIdList = rdmsCbbService.getCbbListByIdList(rdmsProjectSubproject.getCustomerId(), cbbIdList);
                    rdmsProjectSubprojectDto.setCbbList(cbbListByIdList);
                } catch (Exception e) {
                    // 记录异常日志或进行其他异常处理
                    LOG.info("Error occurred while fetching character list: {}", e.getMessage());
                }
            }
        }
        responseDto.setContent(rdmsProjectSubprojectDto);
        return responseDto;
    }

    @PostMapping("/editSubProjectInfo")
    @Transactional
    public ResponseDto editSubProjectInfo(@RequestBody RdmsProjectSubprojectDto subprojectDto) {

        ValidatorUtil.require(subprojectDto.getCustomerId(), "公司ID");
        ValidatorUtil.require(subprojectDto.getProjectId(), "项目ID");
        ValidatorUtil.require(subprojectDto.getLabel(), "子项目名称");
        ValidatorUtil.require(subprojectDto.getIntroduce(), "子项目概要");
        ValidatorUtil.require(subprojectDto.getProjectManagerId(), "子项目经理");
        ValidatorUtil.require(subprojectDto.getTestManagerId(), "测试主管");
        ValidatorUtil.require(subprojectDto.getSystemManagerId(), "系统工程师");

        ValidatorUtil.require(subprojectDto.getId(), "子项目ID");
        RdmsProjectSubproject dbSubproject = rdmsSubprojectService.selectByPrimaryKey(subprojectDto.getId());
        if (dbSubproject == null || Integer.valueOf(1).equals(dbSubproject.getDeleted())) {
            throw new BusinessException(BusinessExceptionCode.DB_RECORD_NOT_EXIST);
        }
        TenantAccess.assertSameTenant(dbSubproject.getCustomerId());
        RdmsProject mainProject = rdmsProjectService.selectByPrimaryKey(dbSubproject.getProjectId());
        jobActorGuard.assertActor(dbSubproject.getCustomerId(), null,
                dbSubproject.getProjectManagerId(), dbSubproject.getCreatorId(), dbSubproject.getSupervise(),
                mainProject == null ? null : mainProject.getProjectManagerId());
        // 原负责人以库为准；改派只在本子项内进行
        subprojectDto.setOriginProjectManagerId(dbSubproject.getProjectManagerId());
        subprojectDto.setOriginTestManagerId(dbSubproject.getTestManagerId());
        String scopeId = dbSubproject.getId();
        if (subprojectDto.getModuleIdListStr() != null) {
            List<String> moduleScope = RdmsCbbService.isRootSubproject(dbSubproject)
                    ? RdmsCbbService.parseModuleIds(mainProject == null ? null : mainProject.getModuleIdListStr())
                    : rdmsCbbService.moduleScopeOfSubproject(dbSubproject.getParent());
            rdmsCbbService.assertSelectableCbbs(dbSubproject.getCustomerId(),
                    RdmsCbbService.parseModuleIds(subprojectDto.getModuleIdListStr()),
                    RdmsCbbService.parseModuleIds(dbSubproject.getModuleIdListStr()), moduleScope);
        }

        ResponseDto responseDto = new ResponseDto<>();
        RdmsProjectSubproject projectSubproject = CopyUtil.copy(subprojectDto, RdmsProjectSubproject.class);
        // 状态、预算、归属与产品经理、监管委员不经此入口修改
        projectSubproject.setProductManagerId(null);
        projectSubproject.setSupervise(null);
        projectSubproject.setStatus(null);
        projectSubproject.setBudget(null);
        projectSubproject.setAddCharge(null);
        projectSubproject.setChargeRate(null);
        projectSubproject.setCustomerId(null);
        projectSubproject.setProjectId(null);
        projectSubproject.setParent(null);
        projectSubproject.setType(null);
        projectSubproject.setRdType(null);
        projectSubproject.setDeleted(null);
        //更新子项目
        rdmsSubprojectService.updateByPrimaryKeySelective(projectSubproject);

        {//如果项目经理发生了变化
            if (!subprojectDto.getProjectManagerId().equals(subprojectDto.getOriginProjectManagerId())) {
                //修改直接下级子项目的creatorId
                List<RdmsProjectSubproject> subprojectListByParentId = rdmsSubprojectService.getSubprojectListByParentId(subprojectDto.getId());
                if(!CollectionUtils.isEmpty(subprojectListByParentId)){
                    for(RdmsProjectSubproject subproject: subprojectListByParentId){
                        subproject.setCreatorId(subprojectDto.getProjectManagerId());
                        rdmsSubprojectService.updateByPrimaryKeySelective(subproject);
                    }
                }

                //该项目项下的所有特性开发的执行人
                //通过原始子项目ID和原始项目经理ID获取相应的功能条目
                List<RdmsCharacterDto> characterList = rdmsCharacterService.getCharacterListBySubprojectIdAndNextNodeId(subprojectDto.getId(), subprojectDto.getOriginProjectManagerId());
                if(!CollectionUtils.isEmpty(characterList)){
                    for (RdmsCharacterDto characterDto : characterList) {
                        characterDto.setNextNode(subprojectDto.getProjectManagerId());
                        rdmsCharacterService.updateByPrimaryKeySelective(characterDto);
                    }
                }

                //将该项目项下所有任务的项目经理更新为新的项目经理;
                List<RdmsJobItemDto> jobitemList = rdmsJobItemService.getJobitemListBySubprojectId(subprojectDto.getId(), null, null);
                if (!CollectionUtils.isEmpty(jobitemList)) {
                    RdmsProjectSubproject rdmsProjectSubproject = rdmsSubprojectService.selectByPrimaryKey(subprojectDto.getId());
                    for (RdmsJobItemDto jobItemDto : jobitemList) {
                        //修改项目经理
                        jobItemDto.setProjectManagerId(rdmsProjectSubproject.getProjectManagerId());
                        rdmsJobItemService.updateByPrimaryKeySelective(jobItemDto);

                    }
                }
                //处理未完成的任务
                List<JobItemStatusEnum> statusEnumList = new ArrayList<>();
                statusEnumList.add(JobItemStatusEnum.PLAN);
                statusEnumList.add(JobItemStatusEnum.HANDLING);
                statusEnumList.add(JobItemStatusEnum.SUBMIT);
                statusEnumList.add(JobItemStatusEnum.TESTING);
                statusEnumList.add(JobItemStatusEnum.CHA_RECHECK);
                statusEnumList.add(JobItemStatusEnum.QUA_RECHECK);
                statusEnumList.add(JobItemStatusEnum.SUB_RECHECK);
                statusEnumList.add(JobItemStatusEnum.EVALUATE);
                statusEnumList.add(JobItemStatusEnum.APPROVED);
                List<RdmsJobItem> jobitemListByNextNodeId = rdmsJobItemService.getJobitemListByNextNodeId(subprojectDto.getOriginProjectManagerId(), statusEnumList);
                if (!CollectionUtils.isEmpty(jobitemListByNextNodeId)) {
                    for (RdmsJobItem jobItem : jobitemListByNextNodeId) {
                        if (!scopeId.equals(jobItem.getSubprojectId())) {
                            continue;
                        }
                        jobItem.setNextNode(subprojectDto.getProjectManagerId());
                        rdmsJobItemService.updateByPrimaryKeySelective(jobItem);
                    }
                }
                //处理未完成的物料申请
                {
                    List<RdmsMaterialManageDto> listByNextNodeId = rdmsMaterialManageService.getListByNextNodeIdForApprover(subprojectDto.getOriginProjectManagerId());
                    if(!CollectionUtils.isEmpty(listByNextNodeId)){
                        for(RdmsMaterialManageDto materialManageDto: listByNextNodeId){
                            if (!scopeId.equals(materialManageDto.getSubprojectId())) {
                                continue;
                            }
                            RdmsMaterialManage materialManage = CopyUtil.copy(materialManageDto, RdmsMaterialManage.class);
                            materialManage.setNextNode(subprojectDto.getProjectManagerId());
                            rdmsMaterialManageService.updateByPrimaryKeySelective(materialManage);
                        }
                    }
                }
                //处理未完成的费用申请
                {
                    List<RdmsFeeManage> listByNextNodeId = rdmsFeeManageService.getListByNextNodeId(subprojectDto.getOriginProjectManagerId());
                    if(!CollectionUtils.isEmpty(listByNextNodeId)){
                        for(RdmsFeeManage feeManageDto: listByNextNodeId){
                            if (!scopeId.equals(feeManageDto.getSubprojectId())) {
                                continue;
                            }
                            feeManageDto.setNextNode(subprojectDto.getProjectManagerId());
                            rdmsFeeManageService.updateByPrimaryKeySelective(feeManageDto);
                        }
                    }
                }

            }
        }

        {//如果测试主管发生了变化
            if (!subprojectDto.getTestManagerId().equals(subprojectDto.getOriginTestManagerId())) {
                //将该项目项下所有任务的测试主管更新为新的测试主管;
                List<RdmsJobItemDto> jobitemList = rdmsJobItemService.getJobitemListBySubprojectId(subprojectDto.getId(), null, null);
                RdmsProjectSubproject rdmsProjectSubproject = rdmsSubprojectService.selectByPrimaryKey(subprojectDto.getId());
                if (!CollectionUtils.isEmpty(jobitemList)) {
                    for (RdmsJobItemDto jobItemDto : jobitemList) {
                        //修改测试主管
                        jobItemDto.setTestManagerId(rdmsProjectSubproject.getTestManagerId());
                        rdmsJobItemService.updateByPrimaryKeySelective(jobItemDto);
                    }
                }
                //处理未完成的任务
                List<JobItemStatusEnum> statusEnumList = new ArrayList<>();
                statusEnumList.add(JobItemStatusEnum.PLAN);
                statusEnumList.add(JobItemStatusEnum.HANDLING);
                statusEnumList.add(JobItemStatusEnum.SUBMIT);
                statusEnumList.add(JobItemStatusEnum.TESTING);
                statusEnumList.add(JobItemStatusEnum.CHA_RECHECK);
                statusEnumList.add(JobItemStatusEnum.QUA_RECHECK);
                statusEnumList.add(JobItemStatusEnum.SUB_RECHECK);
                statusEnumList.add(JobItemStatusEnum.EVALUATE);
                statusEnumList.add(JobItemStatusEnum.APPROVED);
                List<RdmsJobItem> jobitemListByNextNodeId = rdmsJobItemService.getJobitemListByNextNodeId(subprojectDto.getOriginTestManagerId(), statusEnumList);
                if (!CollectionUtils.isEmpty(jobitemListByNextNodeId)) {
                    for (RdmsJobItem jobItem : jobitemListByNextNodeId) {
                        if (!scopeId.equals(jobItem.getSubprojectId())) {
                            continue;
                        }
                        jobItem.setNextNode(subprojectDto.getTestManagerId());
                        rdmsJobItemService.updateByPrimaryKeySelective(jobItem);
                    }
                }
                //处理未完成的物料申请
                {
                    List<RdmsMaterialManageDto> listByNextNodeId = rdmsMaterialManageService.getListByNextNodeIdForApprover(subprojectDto.getOriginTestManagerId());
                    if(!CollectionUtils.isEmpty(listByNextNodeId)){
                        for(RdmsMaterialManageDto materialManageDto: listByNextNodeId){
                            if (!scopeId.equals(materialManageDto.getSubprojectId())) {
                                continue;
                            }
                            RdmsMaterialManage materialManage = CopyUtil.copy(materialManageDto, RdmsMaterialManage.class);
                            materialManage.setNextNode(subprojectDto.getTestManagerId());
                            rdmsMaterialManageService.updateByPrimaryKeySelective(materialManage);
                        }
                    }
                }
                //处理未完成的费用申请
                {
                    List<RdmsFeeManage> listByNextNodeId = rdmsFeeManageService.getListByNextNodeId(subprojectDto.getOriginTestManagerId());
                    if(!CollectionUtils.isEmpty(listByNextNodeId)){
                        for(RdmsFeeManage feeManageDto: listByNextNodeId){
                            if (!scopeId.equals(feeManageDto.getSubprojectId())) {
                                continue;
                            }
                            feeManageDto.setNextNode(subprojectDto.getTestManagerId());
                            rdmsFeeManageService.updateByPrimaryKeySelective(feeManageDto);
                        }
                    }
                }
            }
        }

        responseDto.setSuccess(true);
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    /**
     * 列表查询申请立项的项目
     */
    @PostMapping("/getSubProjectForPjm")
    public ResponseDto<List<RdmsProjectSubprojectDto>> getSubProjectForPjm(@RequestBody RdmsHmiIdsDto idsDto) {
        ResponseDto<List<RdmsProjectSubprojectDto>> responseDto = new ResponseDto<>();
        List<ProjectStatusEnum> projectStatusEnums = new ArrayList<>();
        projectStatusEnums.add(ProjectStatusEnum.SETUPED);
        projectStatusEnums.add(ProjectStatusEnum.ONGOING);
        projectStatusEnums.add(ProjectStatusEnum.SUSPEND);
        projectStatusEnums.add(ProjectStatusEnum.DEV_COMPLETE);
        projectStatusEnums.add(ProjectStatusEnum.INTEGRATION);
        projectStatusEnums.add(ProjectStatusEnum.REVIEW_SUBP);
        projectStatusEnums.add(ProjectStatusEnum.REVIEW_PRO);
        projectStatusEnums.add(ProjectStatusEnum.COMPLETE);
//        projectStatusEnums.add(ProjectStatusEnum.CLOSE);
        projectStatusEnums.add(ProjectStatusEnum.OUT_SOURCE);

        List<RdmsProjectSubprojectDto> projectList = rdmsSubprojectService.getMixProjectListByPjmAndStatusList(idsDto.getCustomerId(), idsDto.getProjectManagerId(), projectStatusEnums);
        enrichSubprojectListMetrics(projectList);
        responseDto.setContent(projectList);
        return responseDto;
    }

   @PostMapping("/getSubprojectExeData")
    public ResponseDto<List<RdmsProjectSubprojectDto>> getSubprojectExeDataByPdm(@RequestBody RdmsHmiIdsDto idsDto) {
        idsDto.setProductManagerId(TenantAccess.resolveMemberId(idsDto.getProductManagerId()));
        ResponseDto<List<RdmsProjectSubprojectDto>> responseDto = new ResponseDto<>();
        List<ProjectStatusEnum> projectStatusEnums = new ArrayList<>();
        projectStatusEnums.add(ProjectStatusEnum.SETUPED);
        projectStatusEnums.add(ProjectStatusEnum.ONGOING);
        projectStatusEnums.add(ProjectStatusEnum.SUSPEND);
        projectStatusEnums.add(ProjectStatusEnum.DEV_COMPLETE);
        projectStatusEnums.add(ProjectStatusEnum.INTEGRATION);
        projectStatusEnums.add(ProjectStatusEnum.REVIEW_SUBP);
        projectStatusEnums.add(ProjectStatusEnum.REVIEW_PRO);
        projectStatusEnums.add(ProjectStatusEnum.COMPLETE);
//        projectStatusEnums.add(ProjectStatusEnum.CLOSE);
        projectStatusEnums.add(ProjectStatusEnum.OUT_SOURCE);

        List<RdmsProjectSubprojectDto> projectList = rdmsSubprojectService.getMixProjectListByPdmAndStatusList(idsDto.getCustomerId(), idsDto.getProductManagerId(), projectStatusEnums);
        enrichSubprojectListMetrics(projectList);
        responseDto.setContent(projectList);
        return responseDto;
    }

    @PostMapping("/getArchiveSubProjectForPjm")
    public ResponseDto<List<RdmsProjectSubprojectDto>> getArchiveSubProjectForPjm(@RequestBody RdmsHmiIdsDto idsDto) {
        idsDto.setProjectManagerId(TenantAccess.resolveMemberId(idsDto.getProjectManagerId()));
        ResponseDto<List<RdmsProjectSubprojectDto>> responseDto = new ResponseDto<>();
        List<ProjectStatusEnum> projectStatusEnums = new ArrayList<>();
        projectStatusEnums.add(ProjectStatusEnum.ARCHIVED);
        List<RdmsProjectSubprojectDto> projectList = rdmsSubprojectService.getMixProjectListByPjmAndStatusList(idsDto.getCustomerId(), idsDto.getProjectManagerId(), projectStatusEnums);
        responseDto.setContent(projectList);
        return responseDto;
    }

    /**
     *
     */
    @PostMapping("/ListSubprojectByIds")
    public ResponseDto<List<RdmsProjectSubprojectDto>> ListSubprojectByIds(@RequestBody RdmsHmiIdsDto idsDto) {
        ResponseDto<List<RdmsProjectSubprojectDto>> responseDto = new ResponseDto<>();
        List<RdmsProjectSubprojectDto> rdmsProjectSubprojectDtos = rdmsSubprojectService.ListSubprojectByIds(idsDto);
        responseDto.setContent(rdmsProjectSubprojectDtos);
        return responseDto;
    }

    /**
     * 列表查询
     */
    @PostMapping("/getSubProjectInfo")
    public ResponseDto<RdmsProjectSubprojectDto> getSubProjectInfo(@RequestParam String subProjectId, String loginUserId) {
        ResponseDto<RdmsProjectSubprojectDto> responseDto = new ResponseDto<>();

        RdmsProjectSubproject subProjectInfo = rdmsSubprojectService.selectByPrimaryKey(subProjectId);
        if (!ObjectUtils.isEmpty(subProjectInfo)) {
            TenantAccess.assertSameTenant(subProjectInfo.getCustomerId());
            RdmsProjectSubprojectDto projectSubprojectDto = CopyUtil.copy(subProjectInfo, RdmsProjectSubprojectDto.class);
            //当前项目保密等级
            List<RdmsProjectSecLevel> rdmsProjectSecLevels = rdmsProjectSecLevelService.getBySubProjectId(subProjectId);
            if(!CollectionUtils.isEmpty(rdmsProjectSecLevels)){
                projectSubprojectDto.setSecretLevel(rdmsProjectSecLevels.get(0).getLevel());
            }else{
                projectSubprojectDto.setSecretLevel(0);
            }
            //父级项目保密等级
            List<RdmsProjectSecLevel> byProjectId = rdmsProjectSecLevelService.getByProjectId(subProjectInfo.getProjectId());
            if(!CollectionUtils.isEmpty(byProjectId)){
                projectSubprojectDto.setMainSecretLevel(byProjectId.get(0).getLevel());
            }else{
                projectSubprojectDto.setMainSecretLevel(0);
            }

            List<CharacterStatusEnum> statusList = new ArrayList<>();
            statusList.add(CharacterStatusEnum.SETUPED);
            statusList.add(CharacterStatusEnum.DECOMPOSED);
            statusList.add(CharacterStatusEnum.ITERATING);
            statusList.add(CharacterStatusEnum.DEVELOPING);
            statusList.add(CharacterStatusEnum.INTEGRATION);
            statusList.add(CharacterStatusEnum.DEV_COMPLETE);
            List<RdmsCharacterDto> characterList = rdmsCharacterService.getCharacterListBySubprojectIdAndStatusList(subProjectId, statusList);
            if (!CollectionUtils.isEmpty(characterList)) {
                projectSubprojectDto.setCharacters(characterList);
            }
            rdmsSubprojectService.appendSubProjectInfo(subProjectInfo, projectSubprojectDto, loginUserId);

            /*List<RdmsMilestoneDto> milestoneList = rdmsSubprojectService.getSubprojectMilestoneList(subProjectId);
            projectSubprojectDto.setMilestoneList(milestoneList);*/
            responseDto.setContent(projectSubprojectDto);
        } else {
            responseDto.setContent(null);
        }
        return responseDto;
    }

    @PostMapping("/getSubProjectTree/{subProjectId}")
    public ResponseDto<List<RdmsHmiTree<RdmsHmiTreeInfoDto>>> getSubProjectTree(@PathVariable String subProjectId) {
        ResponseDto<List<RdmsHmiTree<RdmsHmiTreeInfoDto>>> responseDto = new ResponseDto<>();
        List<RdmsHmiTree<RdmsHmiTreeInfoDto>> projectTreeList = rdmsSubprojectService.getSubProjectTreeList(subProjectId);
        responseDto.setContent(projectTreeList);
        return responseDto;
    }

    @PostMapping("/getNextLevelSubproject/{subProjectId}")
    public ResponseDto<List<RdmsProjectSubprojectDto>> getNextLevelSubproject(@PathVariable String subProjectId) {
        ResponseDto<List<RdmsProjectSubprojectDto>> responseDto = new ResponseDto<>();
        List<RdmsProjectSubprojectDto> nextLevelSubproject = rdmsSubprojectService.getNextLevelSubproject(subProjectId);
        responseDto.setContent(nextLevelSubproject);
        return responseDto;
    }

    @PostMapping("/getSubProjectDetailInfo/{subprojectId}")
    public ResponseDto<RdmsProjectSubprojectDto> getSubProjectDetailInfo(@PathVariable String subprojectId) {
        ResponseDto<RdmsProjectSubprojectDto> responseDto = new ResponseDto<>();
        RdmsProjectSubproject subproject = rdmsSubprojectService.selectByPrimaryKey(subprojectId);
        RdmsProjectSubprojectDto subprojectInfoDto = CopyUtil.copy(subproject, RdmsProjectSubprojectDto.class);
        if (subprojectInfoDto.getProjectManagerId() != null) {
            RdmsCustomerUser projectManager = rdmsCustomerUserService.selectByPrimaryKey(subprojectInfoDto.getProjectManagerId());
            subprojectInfoDto.setProjectManagerName(projectManager.getTrueName());
        }
        if (subprojectInfoDto.getProductManagerId() != null) {
            RdmsCustomerUser productManager = rdmsCustomerUserService.selectByPrimaryKey(subprojectInfoDto.getProductManagerId());
            subprojectInfoDto.setProductManagerName(productManager.getTrueName());
        }
        if (subprojectInfoDto.getSupervise() != null) {
            RdmsCustomerUser supervise = rdmsCustomerUserService.selectByPrimaryKey(subprojectInfoDto.getSupervise());
            subprojectInfoDto.setSuperviseName(supervise.getTrueName());
        }
        subprojectInfoDto.setTestManagerName(trueNameOf(subprojectInfoDto.getTestManagerId()));
        subprojectInfoDto.setSystemManagerName(trueNameOf(subprojectInfoDto.getSystemManagerId()));

        String keyMemberListStr = subproject.getKeyMemberListStr();
        List<String> keyMemberList = JSON.parseArray(keyMemberListStr, String.class);
        List<RdmsHmiNameAndIdDto> keyMemberNameIdList = new ArrayList<>();
        StringBuilder keyMemberNameListStr = new StringBuilder();
        for (String userId : keyMemberList) {
            RdmsCustomerUser rdmsCustomerUser = rdmsCustomerUserService.selectByPrimaryKey(userId);
            RdmsHmiNameAndIdDto nameAndIdDto = new RdmsHmiNameAndIdDto(rdmsCustomerUser.getId(), rdmsCustomerUser.getTrueName());
            keyMemberNameIdList.add(nameAndIdDto);

            keyMemberNameListStr.append(nameAndIdDto.getName()).append(" ");
        }
        subprojectInfoDto.setKeyMemberList(keyMemberNameIdList);
        subprojectInfoDto.setKeyMemberListStr(keyMemberNameListStr.toString());

        responseDto.setContent(subprojectInfoDto);
        return responseDto;
    }

    private String trueNameOf(String customerUserId) {
        if (ObjectUtils.isEmpty(customerUserId)) {
            return null;
        }
        RdmsCustomerUser user = rdmsCustomerUserService.selectByPrimaryKey(customerUserId);
        return user == null ? null : user.getTrueName();
    }

    /**
     * 列表查询
     */
    @PostMapping("/getSubProjectSimpleInfo/{subProjectId}")
    public ResponseDto<RdmsProjectSubprojectDto> getSubProjectSimpleInfo(@PathVariable String subProjectId) {
        ResponseDto<RdmsProjectSubprojectDto> responseDto = new ResponseDto<>();
        RdmsProjectSubproject subProjectInfo = rdmsSubprojectService.getSubProjectInfo(subProjectId);
        if (!ObjectUtils.isEmpty(subProjectInfo)) {
            RdmsProjectSubprojectDto projectSubprojectDto = CopyUtil.copy(subProjectInfo, RdmsProjectSubprojectDto.class);
            if (!ObjectUtils.isEmpty(projectSubprojectDto)) {
                RdmsProject rdmsProject = rdmsProjectService.selectByPrimaryKey(projectSubprojectDto.getProjectId());
                projectSubprojectDto.setProductManagerId(rdmsProject.getProductManagerId());
                RdmsCustomerUser rdmsCustomerUser = rdmsCustomerUserService.selectByPrimaryKey(rdmsProject.getProductManagerId());
                projectSubprojectDto.setProductManagerName(rdmsCustomerUser.getTrueName());

                RdmsCustomerUser rdmsCustomerUser1 = rdmsCustomerUserService.selectByPrimaryKey(projectSubprojectDto.getProjectManagerId());
                projectSubprojectDto.setProjectManagerName(rdmsCustomerUser1.getTrueName());

                projectSubprojectDto.setTestManagerName(trueNameOf(projectSubprojectDto.getTestManagerId()));
                projectSubprojectDto.setSystemManagerName(trueNameOf(projectSubprojectDto.getSystemManagerId()));
            }
            responseDto.setContent(projectSubprojectDto);
        } else {
            responseDto.setContent(null);
        }
        return responseDto;
    }

    @PostMapping("/getModuleSubProjectInfo/{customerId}")
    public ResponseDto<RdmsProjectSubproject> getModuleSubProjectInfo(@PathVariable String customerId) {
        ResponseDto<RdmsProjectSubproject> responseDto = new ResponseDto<>();
        RdmsProjectSubproject moduleSubProject = rdmsSubprojectService.getModuleSubProject(customerId);
        if (!ObjectUtils.isEmpty(moduleSubProject)) {
            responseDto.setContent(moduleSubProject);
        } else {
            responseDto.setContent(null);
        }
        return responseDto;
    }

    @PostMapping("/getSubprojectSelfRemainBudget/{subProjectId}")
    public ResponseDto<BigDecimal> getSubprojectSelfRemainBudget(@PathVariable String subProjectId) {
        ResponseDto<BigDecimal> responseDto = new ResponseDto<>();
        BigDecimal subprojectSelfRemainBudget = rdmsManhourService.getSubprojectSelfRemainBudget(subProjectId);
        responseDto.setContent(subprojectSelfRemainBudget);
        return responseDto;
    }

    @Transactional
    @PostMapping("/getSubprojectDevelopCompleteStatus/{subprojectId}")
    public ResponseDto<Boolean> getSubprojectDevelopCompleteStatus(@PathVariable String subprojectId) {
        ResponseDto<Boolean> responseDto = new ResponseDto<>();
        boolean devComplateStatusFlag = rdmsSubprojectService.getDevComplateStatusFlag(subprojectId);
        responseDto.setContent(devComplateStatusFlag);
        return responseDto;
    }

    @Transactional
    @PostMapping("/getSubprojectIntCompleteStatus/{subprojectId}")
    public ResponseDto<Boolean> getSubprojectIntCompleteStatus(@PathVariable String subprojectId) {
        ResponseDto<Boolean> responseDto = new ResponseDto<>();
        boolean devComplateFlag = rdmsSubprojectService.getDevComplateStatusFlag(subprojectId);
        //BOM编辑完成
        RdmsProjectSubproject subproject = rdmsSubprojectService.selectByPrimaryKey(subprojectId);
        boolean intComplateFlag = false;
        if (!ObjectUtils.isEmpty(subproject) && !ObjectUtils.isEmpty(subproject.getBomStatus()) && subproject.getBomStatus().equals(CharacterBomStatusEnum.SUBMIT.getStatus())) {
            intComplateFlag = true;
        }

        responseDto.setContent(devComplateFlag && intComplateFlag);
        return responseDto;
    }

    /**
     * 查询依据CharacterId创建的工单, 并且这些工单处于submit状态
     */
    @PostMapping("/getNumOfReviewSubprojectByNextNode/{nextNode}")
    public ResponseDto<Integer> getNumOfReviewSubprojectByNextNode(@PathVariable String nextNode) {
        nextNode = TenantAccess.resolveMemberId(nextNode);
        ResponseDto<Integer> responseDto = new ResponseDto<>();
        Integer reviewNum = rdmsJobItemService.countPendingReviewMainByNextNode(
                nextNode, JobItemTypeEnum.REVIEW_SUBP, JobItemTypeEnum.REVIEW_PRO);
        responseDto.setContent(reviewNum);
        return responseDto;
    }

    /**
     * 列表查询
     */
    @Transactional
    @PostMapping("/getSubprojectReviewInfoList")
    public ResponseDto<List<RdmsJobItemDto>> getSubprojectReviewInfoList(@RequestParam String subProjectId, String loginUserId) {
        ResponseDto<List<RdmsJobItemDto>> responseDto = new ResponseDto<>();
        List<JobItemStatusEnum> statusList = new ArrayList<>();
        statusList.add(JobItemStatusEnum.COMPLETED);
        statusList.add(JobItemStatusEnum.ARCHIVED);

        List<JobItemTypeEnum> typeList = new ArrayList<>();
        typeList.add(JobItemTypeEnum.REVIEW);

        List<RdmsJobItemDto> reviewJobitemDetailList = new ArrayList<>();
        List<RdmsJobItemDto> reviewJobitemList = rdmsJobItemService.getJobitemListBySubProjectId(subProjectId, statusList, typeList);
        if (!CollectionUtils.isEmpty(reviewJobitemList)) {
            List<RdmsJobItemDto> jobItemDtos = reviewJobitemList.stream().sorted(Comparator.comparing(RdmsJobItemDto::getCreateTime).reversed()).collect(Collectors.toList());
            for (RdmsJobItemDto jobItemDto : jobItemDtos) {
                RdmsJobItemDto jobitemDetailInfo = rdmsJobItemService.getReviewJobitemDetailInfo(jobItemDto.getId(), loginUserId);
                reviewJobitemDetailList.add(jobitemDetailInfo);
            }
        }
        responseDto.setContent(reviewJobitemDetailList);
        return responseDto;
    }

    @PostMapping("/list")
    public ResponseDto<PageDto<RdmsProjectSubprojectDto>> list(@RequestBody PageDto<RdmsProjectSubprojectDto> pageDto) {
        ResponseDto<PageDto<RdmsProjectSubprojectDto>> responseDto = new ResponseDto<>();
        rdmsSubprojectService.customerSubProjectList(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

    @PostMapping("/listSubProjectByPJM")
    public ResponseDto<PageDto<RdmsProjectSubprojectDto>> listSubProjectByPJM(@RequestBody PageDto<RdmsProjectSubprojectDto> pageDto) {
        TenantAccess.bindSelf(pageDto);
        ResponseDto<PageDto<RdmsProjectSubprojectDto>> responseDto = new ResponseDto<>();
        rdmsSubprojectService.listSubProjectByPJM(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

    /**
     * 对jobitem表进行"简单"查询
     */
    @PostMapping("/listSubprojectsAndCharacterList")
    public ResponseDto<PageDto<RdmsHmiSubprojectAndCharacterMixDto>> listSubprojectsAndCharacterList(@RequestBody PageDto<RdmsHmiSubprojectAndCharacterMixDto> pageDto) {
        TenantAccess.bindSelf(pageDto);
        ResponseDto<PageDto<RdmsHmiSubprojectAndCharacterMixDto>> responseDto = new ResponseDto<>();
        rdmsProjectService.listSubprojectsAndCharacterList(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }


    @PostMapping("/listSubProjectByTM")
    public ResponseDto<PageDto<RdmsProjectSubprojectDto>> listSubProjectByTM(@RequestBody PageDto<RdmsProjectSubprojectDto> pageDto) {
        TenantAccess.bindSelf(pageDto);
        ResponseDto<PageDto<RdmsProjectSubprojectDto>> responseDto = new ResponseDto<>();
        rdmsSubprojectService.listSubProjectByTM(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

    @PostMapping("/archive/{subprojectId}")
    public ResponseDto archive(@PathVariable String subprojectId) {
        ResponseDto responseDto = new ResponseDto();
        rdmsProjectCloseService.archiveSubproject(subprojectId);
        return responseDto;
    }

    /**
     * 列表页补充汇总预算与物料/费用申请角标；预算字段允许为空。
     */
    private void enrichSubprojectListMetrics(List<RdmsProjectSubprojectDto> projectList) {
        if (CollectionUtils.isEmpty(projectList)) {
            return;
        }
        for (RdmsProjectSubprojectDto subprojectDto : projectList) {
            if (!RdTypeEnum.MODULE.getType().equals(subprojectDto.getRdType())) {
                BigDecimal budget = subprojectDto.getBudget() == null ? BigDecimal.ZERO : subprojectDto.getBudget();
                BigDecimal addCharge = subprojectDto.getAddCharge() == null ? BigDecimal.ZERO : subprojectDto.getAddCharge();
                subprojectDto.setSumBudget(budget.add(addCharge));
            }
            subprojectDto.setMaterialAppNum(
                    rdmsMaterialManageService.getApplicationNumBySubprojectId(subprojectDto.getId()));
            subprojectDto.setFeeAppNum(
                    rdmsFeeManageService.getApplicationNumBySubprojectId(subprojectDto.getId()));
        }
    }

    @PostMapping("/getTestSubprojectListByTM/{testManagerId}")
    public ResponseDto<List<RdmsProjectSubprojectDto>> getTestSubprojectListByTM(@PathVariable String testManagerId) {
        testManagerId = TenantAccess.resolveMemberId(testManagerId);
        ResponseDto<List<RdmsProjectSubprojectDto>> responseDto = new ResponseDto();
        List<RdmsProjectSubprojectDto> testSubprojectListByTM = rdmsSubprojectService.getTestSubprojectListByTM(testManagerId);
        responseDto.setContent(testSubprojectListByTM);
        return responseDto;
    }

    @PostMapping("/getSubprojectTaskList/{subprojectId}")
    public ResponseDto<List<RdmsJobItemDto>> getSubprojectTaskList(@PathVariable String subprojectId) {
        ResponseDto<List<RdmsJobItemDto>> responseDto = new ResponseDto();
        List<RdmsJobItemDto> subprojectTaskList = rdmsSubprojectService.getSubprojectTaskList(subprojectId);
        responseDto.setContent(subprojectTaskList);
        return responseDto;
    }

    @PostMapping("/getTestFlagSubprojectListByTM/{testManagerId}")
    public ResponseDto<Boolean> getTestFlagSubprojectListByTM(@PathVariable String testManagerId) {
        testManagerId = TenantAccess.resolveMemberId(testManagerId);
        ResponseDto<Boolean> responseDto = new ResponseDto<>();
        responseDto.setContent(rdmsSubprojectService.hasTestSubprojectByTM(testManagerId));
        return responseDto;
    }

    /**
     * 删除
     */
    /**
     * 删除子项目（现无页面入口）：只允许删除空的非根子项——无特性、无下级子项、未划入预算。
     * 已划转预算的子项删除时如何退回预算尚无规则，一律拒绝。
     */
    @DeleteMapping("/del/{id}")
    @Transactional
    public ResponseDto delete(@PathVariable String id) {
        ResponseDto responseDto = new ResponseDto();
        RdmsProjectSubproject subproject = rdmsSubprojectService.selectByPrimaryKey(id);
        if (subproject == null || Integer.valueOf(1).equals(subproject.getDeleted())) {
            throw new BusinessException(BusinessExceptionCode.DB_RECORD_NOT_EXIST);
        }
        TenantAccess.assertSameTenant(subproject.getCustomerId());
        RdmsProject mainProject = rdmsProjectService.selectByPrimaryKey(subproject.getProjectId());
        jobActorGuard.assertActor(subproject.getCustomerId(), null,
                subproject.getCreatorId(), subproject.getProjectManagerId(),
                mainProject == null ? null : mainProject.getProjectManagerId());
        boolean empty = !id.equals(subproject.getProjectId())
                && CollectionUtils.isEmpty(rdmsCharacterService.getCharacterListBySubprojectId(id, null))
                && CollectionUtils.isEmpty(rdmsSubprojectService.getSubprojectListByParentId(id))
                && (subproject.getBudget() == null || subproject.getBudget().signum() == 0)
                && (subproject.getAddCharge() == null || subproject.getAddCharge().signum() == 0);
        if (!empty) {
            throw new BusinessException(BusinessExceptionCode.LIFECYCLE_TRANSITION_FORBIDDEN);
        }
        rdmsSubprojectService.delete(id);
        responseDto.setSuccess(true);
        return responseDto;
    }

}
