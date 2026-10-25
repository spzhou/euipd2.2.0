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
import com.course.server.enums.FileGroupingEnum;
import com.course.server.enums.rdms.*;
import com.course.server.exception.BusinessException;
import com.course.server.exception.BusinessExceptionCode;
import com.course.server.mapper.RdmsProjectSubprojectMapper;
import com.course.server.security.tenant.TenantAccess;
import com.course.server.security.tenant.CrossTenantPath;
import com.course.server.security.tenant.TenantContext;
import com.course.server.security.tenant.TenantScope;
import com.course.server.service.calculate.CalBudgetService;
import com.course.server.service.rdms.*;
import com.course.server.service.rdms.helper.TenantParentGuard;
import com.course.server.util.CopyUtil;
import com.course.server.util.DevVersionUtil;
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
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/project")
public class ProjectController {
    private static final Logger LOG = LoggerFactory.getLogger(ProjectController.class);
    public static final String BUSINESS_NAME = "项目管理";

    @Resource
    private JobActorGuard jobActorGuard;
    @Resource
    private RdmsProjectService rdmsProjectService;
    @Resource
    private RdmsProjectSetupService rdmsProjectSetupService;
    @Resource
    private RdmsProductService rdmsProductService;
    @Resource
    private RdmsProjectCloseService rdmsProjectCloseService;
    @Resource
    private RdmsIpmtService rdmsIpmtService;
    @Resource
    private RdmsProductManagerService rdmsProductManagerService;
    @Resource
    private RdmsProjectManagerService rdmsProjectManagerService;
    @Resource
    private RdmsCustomerUserService rdmsCustomerUserService;
    @Resource
    private RdmsCharacterService rdmsCharacterService;
    @Resource
    private RdmsMilestoneService rdmsMilestoneService;
    @Resource
    private RdmsCharacterProcessService rdmsCharacterProcessService;
    @Resource
    private RdmsSubprojectService rdmsSubprojectService;
    @Resource
    private RdmsJobItemService rdmsJobItemService;
    @Resource
    private CalBudgetService calBudgetService;
    @Resource
    private RdmsFileService rdmsFileService;
    @Resource
    private RdmsBomConfigService rdmsBomConfigService;
    @Resource
    private RdmsBudgetResearchService rdmsBudgetResearchService;
    @Resource
    private RdmsManhourService rdmsManhourService;
    @Resource
    private RdmsPreProjectService rdmsPreProjectService;
    @Resource
    private RdmsCustomerService rdmsCustomerService;
    @Resource
    private RdmsProjectSecLevelService rdmsProjectSecLevelService;
    @Resource
    private RdmsCharacterSecLevelService rdmsCharacterSecLevelService;
    @Resource
    private RdmsBudgetService rdmsBudgetService;
    @Resource
    private RdmsBossService rdmsBossService;
    @Autowired
    private RdmsBudgetAdjustService rdmsBudgetAdjustService;
    @Autowired
    private RdmsCategoryProjectService rdmsCategoryProjectService;
    @Autowired
    private RdmsSystemManagerService rdmsSystemManagerService;
    @Autowired
    private RdmsGanttService rdmsGanttService;
    @Autowired
    private RdmsMaterialManageService rdmsMaterialManageService;
    @Autowired
    private RdmsFeeManageService rdmsFeeManageService;
    @Autowired
    private RdmsCbbService rdmsCbbService;
    @Resource
    private RdmsUserService rdmsUserService;
    @Resource
    private ProjectLifecycleGuard projectLifecycleGuard;
    @Resource
    private TenantParentGuard tenantParentGuard;

    /**
     * 列表查询已经立项的项目
     */
    @PostMapping("/list")
    public ResponseDto<PageDto<RdmsProjectDto>> list(@RequestBody PageDto<RdmsProjectDto> pageDto) {
        ResponseDto<PageDto<RdmsProjectDto>> responseDto = new ResponseDto<>();
        rdmsProjectService.list(pageDto, null, ActorTypeEnum.ALL.getStatus());
        responseDto.setContent(pageDto);
        return responseDto;
    }

    /**
     * 列表查询申请立项的项目
     */
    @PostMapping("/list-setup-project")
    public ResponseDto<List<RdmsProjectDto>> listSetupProject(@RequestParam String customerId, String loginUserId) {
        ResponseDto<List<RdmsProjectDto>> responseDto = new ResponseDto<>();

        RdmsCustomerUser rdmsCustomerUser = rdmsCustomerUserService.selectByPrimaryKey(loginUserId);
        RdmsCustomer rdmsCustomer = rdmsCustomerService.selectByPrimaryKey(customerId);

        List<RdmsProjectDto> projectList = new ArrayList<>();
        //判断是否是最终管理人
        if(Boolean.TRUE.equals(rdmsCustomerUserService.isBoss(rdmsCustomer.getId(), rdmsCustomerUser.getId()))){
            List<RdmsProjectDto> setupProjectList = rdmsProjectService.getSetupProjectList(customerId);
            projectList.addAll(setupProjectList);
        }
        //如果是监管委员
        List<RdmsCustomerUser> ipmtList = rdmsIpmtService.listIpmtByCustomerId(rdmsCustomer.getId());
        if(!CollectionUtils.isEmpty(ipmtList)) {
            List<String> ipmtIdList = ipmtList.stream().map(RdmsCustomerUser::getId).collect(Collectors.toList());
            if(ipmtIdList.contains(loginUserId)){
                List<RdmsProjectDto> setupProjectListByIpmt = rdmsProjectService.getSetupProjectListByIpmt(customerId, loginUserId);
                projectList.addAll(setupProjectListByIpmt);
            }
        }

        //创建者
        if(!ObjectUtils.isEmpty(loginUserId)){
            List<RdmsProjectDto> setupEditProjectListByCreater = rdmsProjectService.getSetupEditProjectListByCreater(customerId, loginUserId);
            projectList.addAll(setupEditProjectListByCreater);
        }

        List<RdmsProjectDto> projectDtoList = new ArrayList<>();
        List<String> projectIdList = projectList.stream().map(RdmsProjectDto::getId).distinct().collect(Collectors.toList());
        if(!CollectionUtils.isEmpty(projectIdList)){
            projectIdList.forEach(projectId -> {
                for(RdmsProjectDto projectDto: projectList){
                    if(projectId.equals(projectDto.getId())){
                        projectDtoList.add(projectDto);
                        break;
                    }
                }
            });
        }

        //附加处理: 检查项目分类中是否存在"全部项目"分类, 如果不存在, 则创建默认分类
        rdmsCategoryProjectService.initCategoryByCustomerId(customerId);

        List<RdmsProjectDto> projectListSorted = new ArrayList<>();
        if(!CollectionUtils.isEmpty(projectDtoList)){
            projectListSorted = projectDtoList.stream().sorted(Comparator.comparing(RdmsProjectDto::getCreateTime).reversed()).collect(Collectors.toList());
        }
        responseDto.setContent(projectListSorted);
        return responseDto;
    }

    /**
     * 列表查询申请立项的项目
     */
    @PostMapping("/list-setuped-project")
    public ResponseDto<PageDto<RdmsProjectDto>> listSetupedProject(@RequestBody PageDto<RdmsProjectDto> pageDto) {
        TenantAccess.bindSelf(pageDto);
        ResponseDto<PageDto<RdmsProjectDto>> responseDto = new ResponseDto<>();
        List<ProjectStatusEnum> statusEnumList = new ArrayList<>();
        statusEnumList.add(ProjectStatusEnum.SETUPED);
        statusEnumList.add(ProjectStatusEnum.ONGOING);
        statusEnumList.add(ProjectStatusEnum.SUSPEND);
        statusEnumList.add(ProjectStatusEnum.DEV_COMPLETE);
        statusEnumList.add(ProjectStatusEnum.INTEGRATION);
        statusEnumList.add(ProjectStatusEnum.REVIEW_SUBP);
        statusEnumList.add(ProjectStatusEnum.REVIEW_PRO);
        rdmsProjectService.getSetupedProjectList(pageDto, statusEnumList);
        responseDto.setContent(pageDto);
        return responseDto;
    }

    @PostMapping("/getSetupedProjectListByTm")
    public ResponseDto<List<RdmsProjectDto>> getSetupedProjectListByTm(@RequestParam String customerId, String loginUserId) {
        ResponseDto<List<RdmsProjectDto>> responseDto = new ResponseDto<>();
        List<RdmsProject> setupedProjectListByTm = rdmsProjectService.getSetupedProjectListByTm(customerId, loginUserId);
        List<RdmsProjectDto> projectDtoList = CopyUtil.copyList(setupedProjectListByTm, RdmsProjectDto.class);
        int submitTestTaskNum = 0;
        int submitTestTaskNum_testTask = 0;
        if(!CollectionUtils.isEmpty(projectDtoList)){
            for(RdmsProjectDto rdmsProjectDto : projectDtoList){
                if(rdmsProjectDto.getSupervise() != null){
                    RdmsCustomerUser rdmsCustomerUser = rdmsCustomerUserService.selectByPrimaryKey(rdmsProjectDto.getSupervise());
                    rdmsProjectDto.setSuperviseName(rdmsCustomerUser.getTrueName());
                }
                if(rdmsProjectDto.getProductManagerId() != null){
                    RdmsCustomerUser rdmsCustomerUser = rdmsCustomerUserService.selectByPrimaryKey(rdmsProjectDto.getProductManagerId());
                    rdmsProjectDto.setProductManagerName(rdmsCustomerUser.getTrueName());
                }
                if(rdmsProjectDto.getProjectManagerId() != null){
                    RdmsCustomerUser rdmsCustomerUser = rdmsCustomerUserService.selectByPrimaryKey(rdmsProjectDto.getProjectManagerId());
                    rdmsProjectDto.setProjectManagerName(rdmsCustomerUser.getTrueName());
                }
                if(rdmsProjectDto.getTestManagerId() != null){
                    RdmsCustomerUser rdmsCustomerUser = rdmsCustomerUserService.selectByPrimaryKey(rdmsProjectDto.getTestManagerId());
                    rdmsProjectDto.setTestManagerName(rdmsCustomerUser.getTrueName());
                }

                //查询相应项目的功能单元
                List<RdmsCharacter> characterListByProjectId = rdmsCharacterService.getCharacterListByProjectId(rdmsProjectDto.getId());
                if(!CollectionUtils.isEmpty(characterListByProjectId)){
                    for(RdmsCharacter rdmsCharacter : characterListByProjectId){

                        List<JobItemStatusEnum> statusList = new ArrayList<>();
                        statusList.add(JobItemStatusEnum.SUBMIT);
                        Integer submitJobNun = rdmsJobItemService.getTestSchemeJobitemNumberByCharacterAndStatus_notAssist(rdmsCharacter.getId(), statusList);
                        if(submitJobNun > 0){
                            submitTestTaskNum += submitJobNun;
                        }
                        Integer submitJobNun_testTask = rdmsJobItemService.getTaskTestJobitemNumberByCharacterAndStatus(rdmsCharacter.getId(), statusList);
                        if(submitJobNun_testTask > 0){
                            submitTestTaskNum_testTask += submitJobNun_testTask;
                        }

                    }
                }
                rdmsProjectDto.setSubmitJobNum(submitTestTaskNum);
                submitTestTaskNum = 0;
                rdmsProjectDto.setSubmitJobNum_testTask(submitTestTaskNum_testTask);
                submitTestTaskNum_testTask = 0;
            }
        }
        responseDto.setContent(projectDtoList);
        return responseDto;
    }

    @PostMapping("/getTestedSubprojectListByTgm")
    public ResponseDto<List<RdmsProjectSubprojectDto>> getTestedSubprojectListByTgm(@RequestParam String customerId, String loginUserId) {
        ResponseDto<List<RdmsProjectSubprojectDto>> responseDto = new ResponseDto<>();
        List<RdmsProjectSubprojectDto> testedProjectListByTgm = rdmsProjectService.getTestedSubprojectListByTgm(customerId, loginUserId);
        responseDto.setContent(testedProjectListByTgm);
        return responseDto;
    }

    @PostMapping("/getTestedSubprojectListByTm")
    public ResponseDto<PageDto<RdmsProjectSubprojectDto>> getTestedSubprojectListByTm(@RequestBody PageDto<RdmsProjectSubprojectDto> pageDto) {
        TenantAccess.bindSelf(pageDto);
        ResponseDto<PageDto<RdmsProjectSubprojectDto>> responseDto = new ResponseDto<>();
        rdmsProjectService.getTestedSubprojectListByTestManagerId(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

    @PostMapping("/getCompleteProjectListByCustomerId/{customerId}")
    public ResponseDto<List<RdmsProjectDto>> getCompleteProjectListByCustomerId(@PathVariable String customerId) {
        ResponseDto<List<RdmsProjectDto>> responseDto = new ResponseDto<>();
        List<RdmsProjectDto> rdmsProjectInfoDtos = rdmsProjectService.getCompleteProjectListByCustomerId(customerId);
        responseDto.setContent(rdmsProjectInfoDtos);
        return responseDto;
    }

    /**
     * 列表查询
     */
    @PostMapping("/countSetupProjectBySupervise/{userId}")
    public ResponseDto<Integer> countSetupProjectBySupervise(@PathVariable String userId) {
        userId = TenantAccess.resolveMemberId(userId);
        ResponseDto<Integer> responseDto = new ResponseDto<>();
        Integer i = rdmsProjectService.countSetupProjectBySupervise(userId);
        Integer j = rdmsProjectService.countSetupEditProjectByCreater(userId);
        responseDto.setContent(i + j);
        return responseDto;
    }

    @PostMapping("/getSubprojectListAndAllTaskJobitems/{projectId}")
    public ResponseDto<List<RdmsHmiSubprojectAndJobItemMixDto>> getSubprojectListAndAllTaskJobitems(@PathVariable String projectId) {
        ResponseDto<List<RdmsHmiSubprojectAndJobItemMixDto>> responseDto = new ResponseDto<>();
        List<RdmsHmiSubprojectAndJobItemMixDto> subprojectListAndAllTaskJobitems = rdmsProjectService.getSubprojectListAndAllTaskJobitems(projectId);
        responseDto.setContent(subprojectListAndAllTaskJobitems);
        return responseDto;
    }

    @PostMapping("/getProjectTeamIdsByProjectId/{projectId}")
    public ResponseDto<RdmProjectTeamInfoDto> getProjectTeamIdsByProjectId(@PathVariable String projectId) {
        ResponseDto<RdmProjectTeamInfoDto> responseDto = new ResponseDto<>();
        tenantParentGuard.requireProject(projectId);
        RdmProjectTeamInfoDto teamInfoDto = new RdmProjectTeamInfoDto();
        List<RdmsProjectSecLevel> rdmsProjectSecLevels = rdmsProjectSecLevelService.getByProjectId(projectId);
        if(!CollectionUtils.isEmpty(rdmsProjectSecLevels)){
            teamInfoDto.setProjectSecLevel(rdmsProjectSecLevels.get(0).getLevel());
        }else{
            teamInfoDto.setProjectSecLevel(0);
        }
        List<String> projectTeamIds = rdmsProjectSecLevelService.getProjectTeamIdsByProjectId(projectId);
        teamInfoDto.setTeamIdList(projectTeamIds);
        responseDto.setContent(teamInfoDto);
        return responseDto;
    }

    @PostMapping("/getSubProjectByPrimaryKey/{subprojectId}")
    public ResponseDto<RdmsProjectSubprojectDto> getSubProjectByPrimaryKey(@PathVariable String subprojectId) {
        ResponseDto<RdmsProjectSubprojectDto> responseDto = new ResponseDto<>();
        RdmsProjectSubproject subproject = rdmsSubprojectService.selectByPrimaryKey(subprojectId);
        responseDto.setContent(CopyUtil.copy(subproject, RdmsProjectSubprojectDto.class));
        return responseDto;
    }

    /**
     * 对jobitem表进行"简单"查询
     */
    @PostMapping("/getProjectAndTaskJobitems/{projectId}")
    public ResponseDto<RdmsHmiProjectAndJobItemMixDto> getProjectAndTaskJobitems(@PathVariable String projectId) {
        ResponseDto<RdmsHmiProjectAndJobItemMixDto> responseDto = new ResponseDto<>();
        RdmsHmiProjectAndJobItemMixDto projectAndTaskJobitems = rdmsProjectService.getProjectAndTaskJobitems(projectId);
        responseDto.setContent(projectAndTaskJobitems);
        return responseDto;
    }

    /**
     * 对jobitem表进行"简单"查询
     */
    @PostMapping("/getSubProjectAndTaskJobitems/{subProjectId}")
    public ResponseDto<RdmsHmiProjectAndJobItemMixDto> getSubProjectAndTaskJobitems(@PathVariable String subProjectId) {
        ResponseDto<RdmsHmiProjectAndJobItemMixDto> responseDto = new ResponseDto<>();
        RdmsHmiProjectAndJobItemMixDto projectAndTaskJobitems = rdmsProjectService.getSubProjectAndTaskJobitems(subProjectId);
        responseDto.setContent(projectAndTaskJobitems);
        return responseDto;
    }

    /**
     * 根据SubprojectId进行列表查询
     */
/*
    @PostMapping("/getSubProjectReviewDetailInfo/{subprojectId}")
    public ResponseDto<RdmsHmiSubProjectReviewDetailDto> getSubProjectReviewDetailInfo(@PathVariable String subprojectId) {
        ResponseDto<RdmsHmiSubProjectReviewDetailDto> responseDto = new ResponseDto<>();
        RdmsHmiSubProjectReviewDetailDto subProjectReviewDetailInfo = rdmsProjectService.getSubProjectReviewDetailInfo(subprojectId);
        responseDto.setContent(subProjectReviewDetailInfo);
        return responseDto;
    }
*/


    /**
     * 根据SubprojectId进行列表查询
     */
    @PostMapping("/getProjectReviewDetailInfo")
    public ResponseDto<RdmsHmiProjectReviewDetailDto> getProjectReviewDetailInfo(@RequestParam String projectId, String loginUserId) {
        ResponseDto<RdmsHmiProjectReviewDetailDto> responseDto = new ResponseDto<>();
        RdmsHmiProjectReviewDetailDto projectReviewDetailInfo = rdmsProjectService.getProjectReviewDetailInfo(projectId, loginUserId);
        responseDto.setContent(projectReviewDetailInfo);
        return responseDto;
    }


    /**
     * 对jobitem表进行"简单"查询
     */
    @PostMapping("/getSubProjectAndTestJobitems/{subprojectId}")
    public ResponseDto<RdmsHmiProjectAndJobItemMixDto> getSubProjectAndTestJobitems(@PathVariable String subprojectId) {
        ResponseDto<RdmsHmiProjectAndJobItemMixDto> responseDto = new ResponseDto<>();
        RdmsHmiProjectAndJobItemMixDto projectAndTaskJobitems = rdmsProjectService.getSubProjectAndTestJobitems(subprojectId);
        responseDto.setContent(projectAndTaskJobitems);
        return responseDto;
    }


    /**
     * 对jobitem表进行"简单"查询
     */
    @PostMapping("/getSubProjectAndTestJobitems_submit/{subprojectId}")
    public ResponseDto<RdmsHmiProjectAndJobItemMixDto> getSubProjectAndTestJobitems_submit(@PathVariable String subprojectId) {
        ResponseDto<RdmsHmiProjectAndJobItemMixDto> responseDto = new ResponseDto<>();
        RdmsHmiProjectAndJobItemMixDto projectAndTaskJobitems = rdmsProjectService.getSubProjectAndTestJobitems_submit(subprojectId);
        responseDto.setContent(projectAndTaskJobitems);
        return responseDto;
    }


    /**
     * 对jobitem表进行"简单"查询
     */
    @PostMapping("/getProjectAndTaskJobitems_submit/{projectId}")
    public ResponseDto<RdmsHmiProjectAndJobItemMixDto> getProjectAndTaskJobitems_submit(@PathVariable String projectId) {
        ResponseDto<RdmsHmiProjectAndJobItemMixDto> responseDto = new ResponseDto<>();
        RdmsHmiProjectAndJobItemMixDto projectAndTaskJobitems = rdmsProjectService.getProjectAndTaskJobitems_submit(projectId);
        responseDto.setContent(projectAndTaskJobitems);
        return responseDto;
    }

    @PostMapping("/getProjectHasListFlag/{userId}")
    public ResponseDto<Boolean> getProjectHasListFlag(@PathVariable String userId) {
        userId = TenantAccess.resolveMemberId(userId);
        ResponseDto<Boolean> responseDto = new ResponseDto<>();
        Integer countOfSetupedProjectByPjm = rdmsProjectService.getCountOfSetupedProjectByPjm(userId);
        responseDto.setContent(countOfSetupedProjectByPjm >0);
        return responseDto;
    }

    @PostMapping("/getProductHasListFlag/{userId}")
    public ResponseDto<Boolean> getProductHasListFlag(@PathVariable String userId) {
        userId = TenantAccess.resolveMemberId(userId);
        ResponseDto<Boolean> responseDto = new ResponseDto<>();
        Integer countOfSetupedProjectByPdm = rdmsProjectService.getCountOfArchivedProjectByPdm(userId);
        responseDto.setContent(countOfSetupedProjectByPdm >0);
        return responseDto;
    }

    @PostMapping("/getSetupProjectHasListFlag/{userId}")
    public ResponseDto<Boolean> getSetupProjectHasListFlag(@PathVariable String userId) {
        userId = TenantAccess.resolveMemberId(userId);
        ResponseDto<Boolean> responseDto = new ResponseDto<>();
        RdmsCustomerUser rdmsCustomerUser = rdmsCustomerUserService.selectByPrimaryKey(userId);
        RdmsCustomer rdmsCustomer = rdmsCustomerService.selectByPrimaryKey(rdmsCustomerUser.getCustomerId());

        int countOfSetupProjectBySuper = 0;
        if(rdmsCustomerUserService.isBoss(rdmsCustomer.getId(), userId)){
            countOfSetupProjectBySuper = rdmsProjectService.countSetupProjectByCustomerId(rdmsCustomer.getId());
        }else{
            countOfSetupProjectBySuper = rdmsProjectService.countSetupProjectBySupervise(userId);
        }

        responseDto.setContent(countOfSetupProjectBySuper >0);
        return responseDto;
    }

    /**
     * 对jobitem表进行"简单"查询
     */
    @PostMapping("/getSubProjectAndTaskJobitems_submit/{subProjectId}")
    public ResponseDto<RdmsHmiProjectAndJobItemMixDto> getSubProjectAndTaskJobitems_submit(@PathVariable String subProjectId) {
        ResponseDto<RdmsHmiProjectAndJobItemMixDto> responseDto = new ResponseDto<>();
        RdmsHmiProjectAndJobItemMixDto projectAndTaskJobitems = rdmsProjectService.getSubProjectAndTaskJobitems_submit(subProjectId);
        responseDto.setContent(projectAndTaskJobitems);
        return responseDto;
    }

    @PostMapping("/getSubprojectListByProjectId/{projectId}")
    public ResponseDto<List<RdmsProjectSubprojectDto>> getSubprojectListByProjectId(@PathVariable String projectId) {
        ResponseDto<List<RdmsProjectSubprojectDto>> responseDto = new ResponseDto<>();
        List<RdmsProjectSubprojectDto> subprojectListByProjectId = rdmsProjectService.getSubprojectListByProjectId(projectId);
        responseDto.setContent(subprojectListByProjectId);
        return responseDto;
    }

    @Transactional
    @PostMapping("/getProjectDevelopCompleteStatus/{projectId}")
    public ResponseDto<Boolean> getProjectDevelopCompleteStatus(@PathVariable String projectId) {
        ResponseDto<Boolean> responseDto = new ResponseDto<>();
        Boolean flag = false;

        RdmsProject project = rdmsProjectService.selectByPrimaryKey(projectId);
        if( !(project.getStatus().equals(ProjectStatusEnum.ARCHIVED.getStatus()) || project.getStatus().equals(ProjectStatusEnum.COMPLETE.getStatus()))){
            //已经完成的工单和工单数
            List<JobItemTypeEnum> jobitemTypeList = new ArrayList<>();
            jobitemTypeList.add(JobItemTypeEnum.TASK_SUBP);
            jobitemTypeList.add(JobItemTypeEnum.SUBPROJECT_INT);
            jobitemTypeList.add(JobItemTypeEnum.REVIEW_SUBP);

            List<JobItemStatusEnum> jobitemStatusList = new ArrayList<>();
            jobitemStatusList.add(JobItemStatusEnum.COMPLETED);
            jobitemStatusList.add(JobItemStatusEnum.ARCHIVED);

            List<RdmsJobItemDto> completeJobList = rdmsJobItemService.getJobitemListByProjectIdAndStatusListTypeList(projectId, jobitemStatusList, jobitemTypeList);
            List<RdmsJobItemDto> allJobList = rdmsJobItemService.getJobitemListByProjectIdAndStatusListTypeList(projectId, null, jobitemTypeList);
            if (!CollectionUtils.isEmpty(completeJobList) && !CollectionUtils.isEmpty(allJobList)) {
                if (completeJobList.size() == allJobList.size()) {
                    //判断项目项下所有子项目评审是否全部完成
                    List<RdmsProjectSubprojectDto> allSubprojects = rdmsProjectService.getSubprojectListByProjectId(projectId);
                    List<ProjectStatusEnum> statusEnumList =new ArrayList<>();
                    statusEnumList.add(ProjectStatusEnum.ARCHIVED);
                    statusEnumList.add(ProjectStatusEnum.COMPLETE);
                    List<RdmsProjectSubprojectDto> completeSubprojects = rdmsProjectService.getSubprojectListByProjectIdAndStatus(projectId, statusEnumList);

                    for (RdmsJobItemDto jobItemDto : completeJobList) {
                        if (jobItemDto.getType().equals(JobItemTypeEnum.PROJECT_INT.getType()) && jobItemDto.getStatus().equals(JobItemStatusEnum.COMPLETED.getStatus()) && allSubprojects.size() == completeSubprojects.size()) {
                            flag = true;
                        }
                    }
                }
            }
        }
        responseDto.setContent(flag);
        return responseDto;
    }

    /**
     * 这里的projectID是个通用的说法, 需要通过projectType确定到底是什么类型的项目id
     */
    @PostMapping("/getIdsDtoByProjectIdAndType")
    public ResponseDto<RdmsHmiIdsDto> getIdsDtoByProjectIdAndType(@RequestBody RdmsHmiProjectIdAndTypeDto projectIdAndType) {
        ResponseDto<RdmsHmiIdsDto> responseDto = new ResponseDto<>();
        RdmsHmiIdsDto idsDto = rdmsProjectService.getIdsDtoByProjectIdAndType(projectIdAndType.getProjectType(), projectIdAndType.getAnyProjectId());
        responseDto.setContent(idsDto);
        return responseDto;
    }

    @PostMapping("/joinToMainProject")
    public ResponseDto<String> joinToMainProject(@RequestBody RdmsProjectDto projectDto) {
        ResponseDto<String> responseDto = new ResponseDto<>();
        List<String> joinProjectIdList = projectDto.getJoinProjectIdList();
        String jsonString = JSON.toJSONString(joinProjectIdList);
        RdmsProject project = new RdmsProject();
        project.setId(projectDto.getId());
        project.setJoinedProjectIdsStr(jsonString);
        rdmsProjectService.updateByPrimaryKeySelective(project);
        responseDto.setContent(jsonString);
        return responseDto;
    }

    @PostMapping("/getCharacterTreeByProjectIdWithLink/{projectId}")
    public ResponseDto<List<RdmsHmiTree<RdmsCharacterSimpleDto>>> getCharacterTreeByProjectIdWithLink(@PathVariable String projectId) {
        ResponseDto<List<RdmsHmiTree<RdmsCharacterSimpleDto>>> responseDto = new ResponseDto<>();
        List<RdmsHmiTree<RdmsCharacterSimpleDto>> treeList = new ArrayList<>();
        RdmsProject rdmsProject = rdmsProjectService.selectByPrimaryKey(projectId);

        //构建主项目树
        RdmsHmiTree<RdmsCharacterSimpleDto> characterTree = rdmsCharacterService.getCharacterTreeByProjectId(projectId);
        //添加树根节点信息
        characterTree.setId(rdmsProject.getId());
        characterTree.setDeep(-1);
        characterTree.setLabel("项目: "+rdmsProject.getProjectName());
        characterTree.setParent(rdmsProject.getId());
        characterTree.setStatus(rdmsProject.getStatus());
        treeList.add(characterTree);

        //添加link项目树
        if(! ObjectUtils.isEmpty(rdmsProject.getJoinedProjectIdsStr()) && rdmsProject.getJoinedProjectIdsStr().length()>6){
            List<String> linkProjectIds = JSON.parseArray(rdmsProject.getJoinedProjectIdsStr(), String.class);
            for(String id: linkProjectIds){
                RdmsProject linkProject = rdmsProjectService.selectByPrimaryKey(id);
                RdmsHmiTree<RdmsCharacterSimpleDto> linkProjectCharacterTree = rdmsCharacterService.getCharacterTreeByProjectId(id);
                //添加树根节点信息
                linkProjectCharacterTree.setId(linkProject.getId());
                linkProjectCharacterTree.setDeep(-1);
                linkProjectCharacterTree.setLabel("项目: "+linkProject.getProjectName());
                linkProjectCharacterTree.setParent(linkProject.getId());
                linkProjectCharacterTree.setStatus(linkProject.getStatus());
                treeList.add(linkProjectCharacterTree);
            }
        }

        responseDto.setContent(treeList);
        return responseDto;
    }

    @PostMapping("/getArchivedProjectList")
    public ResponseDto<List<RdmsProjectDto>> getArchivedProjectList(@RequestBody RdmsRequestDto requestDto) {
        requestDto.setCustomerUserId(TenantAccess.resolveMemberId(requestDto.getCustomerUserId()));
        ResponseDto<List<RdmsProjectDto>> responseDto = new ResponseDto<>();
        List<RdmsProjectDto> archivedProjects = rdmsProjectService.getArchivedProjectsByCustomerAndPdmId(requestDto.getCustomerId(), requestDto.getCustomerUserId());
        responseDto.setContent(archivedProjects);
        return responseDto;
    }

    @PostMapping("/getAllProjectArchivedList/{loginCustomerId}")
    public ResponseDto<List<RdmsProjectDto>> getAllProjectArchivedList(@PathVariable String loginCustomerId) {
        ResponseDto<List<RdmsProjectDto>> responseDto = new ResponseDto<>();
        List<RdmsProjectDto> archivedProjects = rdmsProjectService.getAllProjectArchivedList(loginCustomerId);
        responseDto.setContent(archivedProjects);
        return responseDto;
    }

    @PostMapping("/getAllProjectArchivedListByStaff/{loginCustomerId}")
    public ResponseDto<List<RdmsProjectDto>> getAllProjectArchivedListByStaff(@PathVariable String loginCustomerId) {
        ResponseDto<List<RdmsProjectDto>> responseDto = new ResponseDto<>();
        List<RdmsProjectDto> archivedProjects = rdmsProjectService.getAllProjectArchivedListByStaff(loginCustomerId);
        responseDto.setContent(archivedProjects);
        return responseDto;
    }

    @PostMapping("/getDevelopingProjectList")
    public ResponseDto<List<RdmsProjectDto>> getDevelopingProjectList(@RequestBody RdmsRequestDto requestDto) {
        requestDto.setCustomerUserId(TenantAccess.resolveMemberId(requestDto.getCustomerUserId()));
        ResponseDto<List<RdmsProjectDto>> responseDto = new ResponseDto<>();
        List<RdmsProjectDto> developingProjects = rdmsProjectService.getDevelopingProjectsByCustomerAndPdmId(requestDto.getCustomerId(), requestDto.getCustomerUserId());
        responseDto.setContent(developingProjects);
        return responseDto;
    }

    /**
     * 列表查询
     */
    @PostMapping("/getProjectInfo/{projectId}")
    public ResponseDto<RdmsProjectDto> getProjectInfo(@PathVariable String projectId) {
        ResponseDto<RdmsProjectDto> responseDto = new ResponseDto<>();
        tenantParentGuard.requireProject(projectId);
        RdmsProjectDto projectInfoDto = rdmsProjectService.getProjectRecordInfo(projectId);
        List<RdmsProjectSecLevel> rdmsProjectSecLevels = rdmsProjectSecLevelService.getBySubProjectId(projectId);
        if(!CollectionUtils.isEmpty(rdmsProjectSecLevels)){
            projectInfoDto.setSecretLevel(rdmsProjectSecLevels.get(0).getLevel());
        }
        responseDto.setContent(projectInfoDto);
        return responseDto;
    }

    @PostMapping("/getProjectSimpleInfo/{projectId}")
    public ResponseDto<RdmsProjectDto> getProjectSimpleInfo(@PathVariable String projectId) {
        ResponseDto<RdmsProjectDto> responseDto = new ResponseDto<>();
        tenantParentGuard.requireProject(projectId);
        RdmsProject rdmsProjectInfo = rdmsProjectService.selectByPrimaryKey(projectId);
        RdmsProjectDto projectInfoDto = CopyUtil.copy(rdmsProjectInfo, RdmsProjectDto.class);
        if(!ObjectUtils.isEmpty(projectInfoDto)){
            RdmsCustomerUser rdmsCustomerUser = rdmsCustomerUserService.selectByPrimaryKey(projectInfoDto.getSupervise());
            projectInfoDto.setSuperviseName(rdmsCustomerUser.getTrueName());
            String bossId = rdmsBossService.requireBossId(projectInfoDto.getCustomerId());
            projectInfoDto.setBossId(bossId);
            RdmsCustomerUser rdmsCustomerUser1 = rdmsCustomerUserService.selectByPrimaryKey(bossId);
            projectInfoDto.setBossName(rdmsCustomerUser1 == null ? null : rdmsCustomerUser1.getTrueName());
            if (!ObjectUtils.isEmpty(projectInfoDto.getProductManagerId())) {
                RdmsCustomerUser productManager = rdmsCustomerUserService.selectByPrimaryKey(projectInfoDto.getProductManagerId());
                if (!ObjectUtils.isEmpty(productManager)) {
                    projectInfoDto.setProductManagerName(productManager.getTrueName());
                }
            }
        }

        List<RdmsProjectSecLevel> rdmsProjectSecLevels = rdmsProjectSecLevelService.getBySubProjectId(projectId);
        if(!CollectionUtils.isEmpty(rdmsProjectSecLevels)){
            projectInfoDto.setSecretLevel(rdmsProjectSecLevels.get(0).getLevel());
        }
        responseDto.setContent(projectInfoDto);
        return responseDto;
    }

    /**
     * 列表查询
     */
    @PostMapping("/getProjectDetailInfo")
    public ResponseDto<RdmsProjectDto> getProjectDetailInfo(@RequestParam String projectId, String loginUserId) {
        ResponseDto<RdmsProjectDto> responseDto = new ResponseDto<>();
        tenantParentGuard.requireProject(projectId);
        RdmsProject rdmsProjectInfo = rdmsProjectService.selectByPrimaryKey(projectId);
        RdmsProjectDto projectInfoDto = CopyUtil.copy(rdmsProjectInfo, RdmsProjectDto.class);
        List<RdmsProjectSecLevel> rdmsProjectSecLevels = rdmsProjectSecLevelService.getBySubProjectId(projectId);
        if(!CollectionUtils.isEmpty(rdmsProjectSecLevels)){
            projectInfoDto.setSecretLevel(rdmsProjectSecLevels.get(0).getLevel());
        }

        if(projectInfoDto.getProjectManagerId() != null){
            RdmsCustomerUser projectManager = rdmsCustomerUserService.selectByPrimaryKey(projectInfoDto.getProjectManagerId());
            projectInfoDto.setProjectManagerName(projectManager.getTrueName());
        }
        if(projectInfoDto.getProductManagerId() != null){
            RdmsCustomerUser productManager = rdmsCustomerUserService.selectByPrimaryKey(projectInfoDto.getProductManagerId());
            projectInfoDto.setProductManagerName(productManager.getTrueName());
        }
        if(projectInfoDto.getSupervise() != null){
            RdmsCustomerUser supervise = rdmsCustomerUserService.selectByPrimaryKey(projectInfoDto.getSupervise());
            projectInfoDto.setSuperviseName(supervise.getTrueName());
        }

        String keyMemberListStr = rdmsProjectInfo.getKeyMemberListStr();
        List<String> keyMemberList = JSON.parseArray(keyMemberListStr, String.class);
        projectInfoDto.setKeyMemberList(keyMemberList);
        String keyMemberNameStr = "";
        if(! CollectionUtils.isEmpty(keyMemberList)){
            for(String id: keyMemberList){
                RdmsCustomerUser rdmsCustomerUser = rdmsCustomerUserService.selectByPrimaryKey(id);
                keyMemberNameStr += rdmsCustomerUser.getTrueName()+ " ";
            }
        }
        projectInfoDto.setKeyMemberListStr(keyMemberNameStr);

        String moduleIdListStr = rdmsProjectInfo.getModuleIdListStr();
        if(!ObjectUtils.isEmpty(moduleIdListStr) && moduleIdListStr.length() > 6){
            List<String> moduleIdList = JSON.parseArray(moduleIdListStr, String.class);
            List<RdmsCbbDto> cbbListByIdList = rdmsCbbService.getCbbListByIdList(projectInfoDto.getCustomerId(), moduleIdList);
            projectInfoDto.setCbbList(cbbListByIdList);
        }

        if(projectInfoDto.getReleaseTime() != null){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            projectInfoDto.setReleaseTimeStr(sdf.format(projectInfoDto.getReleaseTime()));
        }

        List<RdmsMilestoneDto> rdmsMilestoneDtos = rdmsMilestoneService.listByProjectId(projectId);
        if(!CollectionUtils.isEmpty(rdmsMilestoneDtos)){
            projectInfoDto.setMilestoneList(rdmsMilestoneDtos);
        }
        List<RdmsHmiTree<RdmsHmiTreeInfoDto>> projectTreeList = rdmsSubprojectService.getProjectTree(projectId);
        if(! CollectionUtils.isEmpty(projectTreeList)){
            projectInfoDto.setProjectTreeList(projectTreeList);
        }

        List<RdmsCustomerUser> customerUsers = rdmsCustomerUserService.listAllWorkersByProjectId(projectId);
        List<RdmsUserOutlineInfoDto> rdmsUserOutlineList = CopyUtil.copyList(customerUsers, RdmsUserOutlineInfoDto.class);
        if(!CollectionUtils.isEmpty(rdmsUserOutlineList)){
            projectInfoDto.setWorkerList(rdmsUserOutlineList);
        }

        //是否存在评审工单  用于要求补充材料的评审场景
        List<RdmsJobItem> jobitemList = rdmsJobItemService.getJobitemListByProjectIdAndStatusAndJobType(projectId, JobItemStatusEnum.SUBMIT.getStatus(), JobItemTypeEnum.REVIEW_PRO.getType());
        if(!CollectionUtils.isEmpty(jobitemList)){
            projectInfoDto.setHasReviewJobitem(true);
        }else{
            projectInfoDto.setHasReviewJobitem(false);
        }

        //添加项目评审信息  主项目号等于子项目号的 Review_subp 类型工单
        List<JobItemStatusEnum> statusEnumList = new ArrayList<>();
        statusEnumList.add(JobItemStatusEnum.ARCHIVED);
        List<JobItemTypeEnum> typeEnumList = new ArrayList<>();
        typeEnumList.add(JobItemTypeEnum.REVIEW_SUBP);
        List<RdmsJobItemDto> reviewJobitems = rdmsJobItemService.getJobitemListBySubprojectIdAndStatusListTypeList(projectId, statusEnumList, typeEnumList);
        List<RdmsJobItemDto> collect = reviewJobitems.stream().sorted(Comparator.comparing(RdmsJobItemDto::getCreateTime).reversed()).collect(Collectors.toList());
        List<RdmsJobItemDto> jobItemDtoList = new ArrayList<>();
        if(! CollectionUtils.isEmpty(reviewJobitems)){
            for(RdmsJobItemDto jobItemDto: collect){
                RdmsJobItemDto jobitemDetailInfo = rdmsJobItemService.getJobitemDetailInfo(jobItemDto.getId(), loginUserId);
                if(! ObjectUtils.isEmpty(jobitemDetailInfo.getNextNode())){
                    RdmsCustomerUser rdmsCustomerUser = rdmsCustomerUserService.selectByPrimaryKey(jobitemDetailInfo.getNextNode());
                    jobitemDetailInfo.setNextNodeName(rdmsCustomerUser.getTrueName());
                }

                List<String> workerIds = JSON.parseArray(jobitemDetailInfo.getReviewWorkerIdStr(), String.class);
                StringBuilder workerNameListStr = new StringBuilder();
                if(! CollectionUtils.isEmpty(workerIds)){
                    for(String id : workerIds){
                        RdmsCustomerUser rdmsCustomerUser1 = rdmsCustomerUserService.selectByPrimaryKey(id);
                        workerNameListStr.append(rdmsCustomerUser1.getTrueName()).append(" ");
                    }
                }
                jobitemDetailInfo.setReviewWorkerIdStr(workerNameListStr.toString());
                jobItemDtoList.add(jobitemDetailInfo);
            }
        }
        projectInfoDto.setReviewJobDetail(jobItemDtoList);

        responseDto.setContent(projectInfoDto);
        return responseDto;
    }

    /**
     * 列表查询
     */
    @PostMapping("/getProjectAndArchivedCharacters/{projectId}")
    public ResponseDto<RdmsProjectDto> getProjectAndArchivedCharacters(@PathVariable String projectId) {
        ResponseDto<RdmsProjectDto> responseDto = new ResponseDto<>();
        List<CharacterStatusEnum> statusEnumList = new ArrayList<>();
        statusEnumList.add(CharacterStatusEnum.ARCHIVED);
        RdmsProjectDto projectAndCharacterList = rdmsProjectService.getProjectAndCharacterList(projectId, statusEnumList);
        responseDto.setContent(projectAndCharacterList);
        return responseDto;
    }

    @PostMapping("/getBomConfigListByProjectId/{projectId}")
    public ResponseDto<List<RdmsBomConfigDto>> getBomConfigListByProjectId(@PathVariable String projectId) {
        ResponseDto<List<RdmsBomConfigDto>> responseDto = new ResponseDto<>();
        tenantParentGuard.requireProject(projectId);
        List<RdmsBomConfigDto> bomConfigListByProjectId = rdmsProjectService.getBomConfigListByProjectId(projectId);
        responseDto.setContent(bomConfigListByProjectId);
        return responseDto;
    }

    /**
     * 列表查询
     */
    @PostMapping("/getProjectInfoBySubprojectId/{subprojectId}")
    public ResponseDto<RdmsProjectDto> getProjectInfoBySubprojectId(@PathVariable String subprojectId) {
        ResponseDto<RdmsProjectDto> responseDto = new ResponseDto<>();
        tenantParentGuard.requireSubproject(subprojectId);
        RdmsProject project = rdmsProjectService.getProjectBySubprojectId(subprojectId);
        RdmsProjectDto projectInfoDto = CopyUtil.copy(project, RdmsProjectDto.class);
        if(projectInfoDto.getProjectManagerId() != null){
            RdmsCustomerUser projectManager = rdmsCustomerUserService.selectByPrimaryKey(projectInfoDto.getProjectManagerId());
            projectInfoDto.setProjectManagerName(projectManager.getTrueName());
        }
        if(projectInfoDto.getProductManagerId() != null){
            RdmsCustomerUser productManager = rdmsCustomerUserService.selectByPrimaryKey(projectInfoDto.getProductManagerId());
            projectInfoDto.setProductManagerName(productManager.getTrueName());
        }
        if(projectInfoDto.getSupervise() != null){
            RdmsCustomerUser supervise = rdmsCustomerUserService.selectByPrimaryKey(projectInfoDto.getSupervise());
            projectInfoDto.setSuperviseName(supervise.getTrueName());
        }

        String keyMemberListStr = project.getKeyMemberListStr();
        List<String> keyMemberList = JSON.parseArray(keyMemberListStr, String.class);
        projectInfoDto.setKeyMemberList(keyMemberList);

        if(projectInfoDto.getReleaseTime() != null){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            projectInfoDto.setReleaseTimeStr(sdf.format(projectInfoDto.getReleaseTime()));
        }

        List<RdmsMilestoneDto> rdmsMilestoneDtos = rdmsMilestoneService.listByProjectId(project.getId());
        if(!CollectionUtils.isEmpty(rdmsMilestoneDtos)){
            projectInfoDto.setMilestoneList(rdmsMilestoneDtos);
        }

        responseDto.setContent(projectInfoDto);
        return responseDto;
    }


    /**
     * 列表查询
     */
    @PostMapping("/listIpmtByCustomerId/{customerId}")
    @CrossTenantPath
    public ResponseDto<List<RdmsCustomerUser>> listIpmtByCustomerId(@PathVariable String customerId) {
        ResponseDto<List<RdmsCustomerUser>> responseDto = new ResponseDto<>();
        // 外包时选择对方机构的监管委员
        rdmsCustomerService.assertOutsourcingTarget(customerId);
        List<RdmsCustomerUser> rdmsCustomerUsers = TenantScope.call(customerId, () -> rdmsIpmtService.listIpmtByCustomerId(customerId));
        responseDto.setContent(rdmsCustomerUsers);
        return responseDto;
    }

    /**
     * 列表查询
     */
    @PostMapping("/listProductManagerByCustomerId/{customerId}")
    public ResponseDto<List<RdmsCustomerUser>> listProductManagerByCustomerId(@PathVariable String customerId) {
        ResponseDto<List<RdmsCustomerUser>> responseDto = new ResponseDto<>();
        List<RdmsCustomerUser> rdmsCustomerUsers = rdmsProductManagerService.listProductManagerByCustomerId(customerId);
        responseDto.setContent(rdmsCustomerUsers);
        return responseDto;
    }

    /**
     * 列表查询
     */
    @PostMapping("/listProjectManagerByCustomerId/{customerId}")
    public ResponseDto<List<RdmsCustomerUser>> listProjectManagerByCustomerId(@PathVariable String customerId) {
        ResponseDto<List<RdmsCustomerUser>> responseDto = new ResponseDto<>();
        List<RdmsCustomerUser> rdmsCustomerUsers = rdmsProjectManagerService.getProjectManagerListByCustomerId(customerId);
        responseDto.setContent(rdmsCustomerUsers);
        return responseDto;
    }

    @PostMapping("/listSystemManagerByCustomerId/{customerId}")
    public ResponseDto<List<RdmsCustomerUser>> listSystemManagerByCustomerId(@PathVariable String customerId) {
        ResponseDto<List<RdmsCustomerUser>> responseDto = new ResponseDto<>();
        List<RdmsCustomerUser> rdmsCustomerUsers = rdmsSystemManagerService.listSystemManagerByCustomerId(customerId);
        responseDto.setContent(rdmsCustomerUsers);
        return responseDto;
    }

    /**
     * 列表查询
     */
    @PostMapping("/findKeyMemberIdListByProjectId/{projectId}")
    public ResponseDto<List<String>> findKeyMemberIdListByProjectId(@PathVariable String projectId) {
        ResponseDto<List<String>> responseDto = new ResponseDto<>();
        List<String> keyMemberIdListByProjectId = rdmsProjectService.findKeyMemberIdListByProjectId(projectId);
        responseDto.setContent(keyMemberIdListByProjectId);
        return responseDto;
    }

    /**
     * 保存项目信息
     */
    @PostMapping("/save")
    @Transactional
    public ResponseDto<RdmsProjectDto> save(@RequestBody RdmsProjectDto projectInfoDto) {
        ResponseDto<RdmsProjectDto> responseDto = new ResponseDto<>();

        ValidatorUtil.require(projectInfoDto.getCustomerId(), "客户ID");
        ValidatorUtil.require(projectInfoDto.getProjectName(), "项目名称");

        ValidatorUtil.length(projectInfoDto.getProjectName(), "项目名称", 2, 50);

        RdmsProject projectInfo = CopyUtil.copy(projectInfoDto, RdmsProject.class);
        String jsonString = JSON.toJSONString(projectInfoDto.getKeyMemberList());
        projectInfo.setKeyMemberListStr(jsonString);
        RdmsProject existingProject = ObjectUtils.isEmpty(projectInfo.getId())
                ? null : rdmsProjectService.selectByPrimaryKey(projectInfo.getId());
        if (existingProject != null) {
            TenantAccess.assertSameTenant(existingProject.getCustomerId());
            projectInfo.setStatus(existingProject.getStatus());
            projectInfo.setDevVersion(existingProject.getDevVersion());
        } else {
            projectInfo.setStatus(ProjectStatusEnum.SETUP.getStatus());
            projectInfo.setDevVersion(DevVersionUtil.requireCanonical(projectInfoDto.getDevVersion()));
        }
        rdmsCbbService.assertSelectableModuleList(
                existingProject == null ? projectInfoDto.getCustomerId() : existingProject.getCustomerId(),
                projectInfoDto.getModuleIdListStr(),
                existingProject == null ? null : existingProject.getModuleIdListStr(), null);
        rdmsProjectService.save(projectInfo);

        responseDto.setContent(projectInfoDto);
        responseDto.setSuccess(true);
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    @PostMapping("/saveBomConfig")
    @Transactional
    public ResponseDto<String> saveBomConfig(@RequestBody RdmsBomConfigDto bomConfigDto) {
        ResponseDto<String> responseDto = new ResponseDto<>();

        ValidatorUtil.require(bomConfigDto.getProjectId(), "项目ID");
        ValidatorUtil.require(bomConfigDto.getCharacterIdList(), "功能列表");
        tenantParentGuard.requireProject(bomConfigDto.getProjectId());
        if (StringUtils.hasText(bomConfigDto.getId())) {
            RdmsBomConfig existingConfig = rdmsBomConfigService.selectByPrimaryKey(bomConfigDto.getId());
            if (existingConfig != null && !bomConfigDto.getProjectId().equals(existingConfig.getProjectId())) {
                throw new BusinessException(BusinessExceptionCode.NO_PERMISSION);
            }
        }

        RdmsBomConfig bomConfig = CopyUtil.copy(bomConfigDto, RdmsBomConfig.class);
        String jsonString = JSON.toJSONString(bomConfigDto.getCharacterIdList());
        bomConfig.setCharacterIdListStr(jsonString);
        if(!ObjectUtils.isEmpty(bomConfigDto.getIsActive()) && bomConfigDto.getIsActive() == 1){
            rdmsBomConfigService.resetActiveFlagByProjectId(bomConfigDto.getProjectId());
            bomConfig.setIsActive(1);
        }
        rdmsBomConfigService.save(bomConfig);

        responseDto.setContent(bomConfig.getId());
        responseDto.setSuccess(true);
        responseDto.setMessage("保存成功");
        return responseDto;
    }
    @PostMapping("/setup")
    public ResponseDto<RdmsProjectSetupDto> setup(@RequestBody RdmsProjectSetupDto projectSetupDto) {
        return rdmsProjectSetupService.setup(projectSetupDto);
    }
    @PostMapping("/save-setup-info")
    public ResponseDto<RdmsProjectSetupDto> saveSetupInfo(@RequestBody RdmsProjectSetupDto projectSetupDto) {
        return rdmsProjectSetupService.saveSetupInfo(projectSetupDto);
    }

    @PostMapping("/save-product")
    @Transactional
    public ResponseDto<RdmsProjectSetupDto> saveProduct(@RequestBody RdmsProjectSetupDto projectSetupDto) {
        ResponseDto<RdmsProjectSetupDto> responseDto = new ResponseDto<>();

        ValidatorUtil.require(projectSetupDto.getCustomerId(), "客户ID");
        ValidatorUtil.require(projectSetupDto.getProjectName(), "项目名称");
        ValidatorUtil.require(projectSetupDto.getSupervise(), "监管委员");
        ValidatorUtil.require(projectSetupDto.getProjectManagerId(), "项目经理");
        ValidatorUtil.require(projectSetupDto.getProductManagerId(), "产品经理");
        ValidatorUtil.require(projectSetupDto.getTestManagerId(), "测试主管");
        ValidatorUtil.require(projectSetupDto.getProjectCode(), "项目代码");

        ValidatorUtil.length(projectSetupDto.getProjectName(), "项目名称", 2, 50);
        ValidatorUtil.length(projectSetupDto.getIncentivePolicy(), "激励政策", 2, 500);
        TenantAccess.assertSameTenant(projectSetupDto.getCustomerId());
        jobActorGuard.assertActor(projectSetupDto.getCustomerId(), null, projectSetupDto.getProductManagerId());

        //处理项目信息
        RdmsProject projectInfo = CopyUtil.copy(projectSetupDto, RdmsProject.class);
        //从数据库中读
        RdmsProject projectInfoDb = rdmsProjectService.selectByPrimaryKey(projectSetupDto.getId());
        if (!ObjectUtils.isEmpty(projectInfoDb)) {
            throw new BusinessException(BusinessExceptionCode.DB_RECORD_EXIST);
        }
        projectInfo.setDevVersion(DevVersionUtil.requireCanonical(projectSetupDto.getDevVersion()));
        String jsonString = JSON.toJSONString(projectSetupDto.getKeyMemberList());
        projectInfo.setKeyMemberListStr(jsonString);
        projectInfo.setStatus(ProjectStatusEnum.ARCHIVED.getStatus());
        projectInfo.setRdType(RdTypeEnum.PRODUCT.getType());
        projectInfo.setBudget(BigDecimal.ZERO);
        projectInfo.setAddCharge(BigDecimal.ZERO);
        projectInfo.setChargeRate(BigDecimal.ONE);
        String projectId = rdmsProjectService.save(projectInfo);

        RdmsProjectSubproject projectSubproject = CopyUtil.copy(projectInfo, RdmsProjectSubproject.class);
        projectSubproject.setParent(projectId);
        projectSubproject.setDeep(0);
        projectSubproject.setProjectId(projectId);
        projectSubproject.setLabel(projectInfo.getProjectName());
        projectSubproject.setIntroduce(projectInfo.getProjectIntroduce());
        projectSubproject.setCreatorId(projectInfo.getCreaterId());
        projectSubproject.setSetupedTime(projectInfo.getCreateTime());
        projectSubproject.setType(ProjectTypeEnum.PRODUCT.getType());
        rdmsSubprojectService.save(projectSubproject);

        RdmsProject rdmsProject = rdmsProjectService.selectByPrimaryKey(projectId);
        rdmsBudgetService.initProductBudgetSheetItem(projectSetupDto.getProductManagerId(), rdmsProject);
        responseDto.setContent(projectSetupDto);
        responseDto.setSuccess(true);
        responseDto.setMessage("保存成功");
        return responseDto;
    }
    @PostMapping("/setup-module-project")
    public ResponseDto<RdmsProjectSetupDto> setupModuleProject(@RequestBody RdmsProjectSetupDto projectSetupDto) {
        return rdmsProjectSetupService.setupModuleProject(projectSetupDto);
    }

     /**
     * 立项
     */
    @PostMapping("/edit")
    @Transactional
    public ResponseDto<RdmsProjectDto> edit(@RequestBody RdmsProjectDto projectInfoDto) {
        ResponseDto<RdmsProjectDto> responseDto = new ResponseDto<>();

        ValidatorUtil.require(projectInfoDto.getCustomerId(), "客户ID");
        ValidatorUtil.require(projectInfoDto.getProjectName(), "项目名称");

        ValidatorUtil.length(projectInfoDto.getProjectName(), "项目名称", 2, 50);

        ValidatorUtil.require(projectInfoDto.getId(), "项目ID");
        RdmsProject editedProject = rdmsProjectService.selectByPrimaryKey(projectInfoDto.getId());
        if (editedProject == null || Integer.valueOf(1).equals(editedProject.getDeleted())) {
            throw new BusinessException(BusinessExceptionCode.DB_RECORD_NOT_EXIST);
        }
        TenantAccess.assertSameTenant(editedProject.getCustomerId());
        jobActorGuard.assertActor(editedProject.getCustomerId(), null,
                editedProject.getProjectManagerId(), editedProject.getProductManagerId(),
                editedProject.getCreaterId(), editedProject.getSupervise());
        // 原负责人以库为准；改派只在本项目根子项内进行
        projectInfoDto.setOriginProjectManagerId(editedProject.getProjectManagerId());
        projectInfoDto.setOriginSystemManagerId(editedProject.getSystemManagerId());
        projectInfoDto.setOriginProductManagerId(editedProject.getProductManagerId());
        projectInfoDto.setOriginTestManagerId(editedProject.getTestManagerId());
        projectInfoDto.setOriginIpmtId(editedProject.getSupervise());
        String scopeId = editedProject.getId();
        rdmsCbbService.assertSelectableModuleList(editedProject.getCustomerId(), projectInfoDto.getModuleIdListStr(),
                editedProject.getModuleIdListStr(), null);

        //处理项目信息
        RdmsProject projectInfo = CopyUtil.copy(projectInfoDto, RdmsProject.class);
        String jsonString = JSON.toJSONString(projectInfoDto.getKeyMemberList());
        projectInfo.setKeyMemberListStr(jsonString);
        projectInfo.setStatus(null);
        projectInfo.setCustomerId(null);
        projectInfo.setCreaterId(null);
        projectInfo.setPreProjectId(null);
        projectInfo.setRdType(null);
        projectInfo.setBudget(null);
        projectInfo.setAddCharge(null);
        projectInfo.setChargeRate(null);
        projectInfo.setDeleted(null);
        projectInfo.setDevVersion(null);
        rdmsProjectService.updateByPrimaryKeySelective(projectInfo);

        //修改作为子项目的主项目
        RdmsProjectSubproject rdmsProjectSubproject = new RdmsProjectSubproject();
        rdmsProjectSubproject.setId(projectInfo.getId());
        rdmsProjectSubproject.setLabel(projectInfo.getProjectName());
        rdmsProjectSubproject.setSupervise(projectInfo.getSupervise());
        rdmsProjectSubproject.setProductManagerId(projectInfo.getProductManagerId());
        rdmsProjectSubproject.setProjectManagerId(projectInfo.getProjectManagerId());
//        rdmsProjectSubproject.setSystemManagerId(projectInfo.getSystemManagerId());  //子项目中不保存系统工程信息, 统一到项目记录中读取
        rdmsProjectSubproject.setTestManagerId(projectInfo.getTestManagerId());
        rdmsProjectSubproject.setKeyMemberListStr(projectInfo.getKeyMemberListStr());
        rdmsSubprojectService.updateByPrimaryKeySelective(rdmsProjectSubproject);

        {//如果项目经理发生了变化
            if (StringUtils.hasText(projectInfoDto.getProjectManagerId()) && !projectInfoDto.getProjectManagerId().equals(projectInfoDto.getOriginProjectManagerId())) {
                //修改直接下级子项目的creatorId
                List<RdmsProjectSubproject> subprojectListByParentId = rdmsSubprojectService.getSubprojectListByParentId(projectInfo.getId());
                if(!CollectionUtils.isEmpty(subprojectListByParentId)){
                    for(RdmsProjectSubproject subproject: subprojectListByParentId){
                        subproject.setCreatorId(projectInfoDto.getProjectManagerId());
                        rdmsSubprojectService.updateByPrimaryKeySelective(subproject);
                    }
                }

                //将该项目项下所有任务的项目经理更新为新的项目经理;
                List<RdmsJobItemDto> jobitemList = rdmsJobItemService.getJobitemListBySubprojectId(projectInfo.getId(), null, null);
                if (!CollectionUtils.isEmpty(jobitemList)) {
                    for (RdmsJobItemDto jobItemDto : jobitemList) {
                        //修改项目经理
                        if (jobItemDto.getProjectManagerId().equals(projectInfoDto.getOriginProjectManagerId())) {
                            jobItemDto.setProjectManagerId(projectInfoDto.getProjectManagerId());
                            rdmsJobItemService.updateByPrimaryKeySelective(jobItemDto);
                        }
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
                List<RdmsJobItem> jobitemListByNextNodeId = rdmsJobItemService.getJobitemListByNextNodeId(projectInfoDto.getOriginProjectManagerId(), statusEnumList);
                if (!CollectionUtils.isEmpty(jobitemListByNextNodeId)) {
                    for (RdmsJobItem jobItem : jobitemListByNextNodeId) {
                        if (!scopeId.equals(jobItem.getSubprojectId())) {
                            continue;
                        }
                        jobItem.setNextNode(projectInfoDto.getProjectManagerId());
                        rdmsJobItemService.updateByPrimaryKeySelective(jobItem);
                    }
                }

                //处理未完成的物料申请
                {
                    List<RdmsMaterialManageDto> listByNextNodeId = rdmsMaterialManageService.getListByNextNodeIdForApprover(projectInfoDto.getOriginProjectManagerId());
                    if(!CollectionUtils.isEmpty(listByNextNodeId)){
                        for(RdmsMaterialManageDto materialManageDto: listByNextNodeId){
                            if (!inScope(scopeId, materialManageDto.getProjectId(), materialManageDto.getSubprojectId())) {
                                continue;
                            }
                            RdmsMaterialManage materialManage = CopyUtil.copy(materialManageDto, RdmsMaterialManage.class);
                            materialManage.setNextNode(projectInfoDto.getProjectManagerId());
                            rdmsMaterialManageService.updateByPrimaryKeySelective(materialManage);
                        }
                    }
                }
                //处理未完成的费用申请
                {
                    List<RdmsFeeManage> listByNextNodeId = rdmsFeeManageService.getListByNextNodeId(projectInfoDto.getOriginProjectManagerId());
                    if(!CollectionUtils.isEmpty(listByNextNodeId)){
                        for(RdmsFeeManage feeManageDto: listByNextNodeId){
                            if (!inScope(scopeId, feeManageDto.getProjectId(), feeManageDto.getSubprojectId())) {
                                continue;
                            }
                            feeManageDto.setNextNode(projectInfoDto.getProjectManagerId());
                            rdmsFeeManageService.updateByPrimaryKeySelective(feeManageDto);
                        }
                    }
                }
            }
        }

        {//如果系统工程师发生了变化
            if (StringUtils.hasText(projectInfoDto.getSystemManagerId()) && !projectInfoDto.getSystemManagerId().equals(projectInfoDto.getOriginSystemManagerId())) {
                //将该项目项下所有任务的项目经理更新为新的项目经理;
                List<RdmsJobItemDto> jobitemList = rdmsJobItemService.getJobitemListBySubprojectId(projectInfo.getId(), null, null);
                if (!CollectionUtils.isEmpty(jobitemList)) {
                    for (RdmsJobItemDto jobItemDto : jobitemList) {
                        //修改执行人
                        if (jobItemDto.getExecutorId().equals(projectInfoDto.getOriginSystemManagerId())) {
                            jobItemDto.setExecutorId(projectInfoDto.getSystemManagerId());
                            rdmsJobItemService.updateByPrimaryKeySelective(jobItemDto);
                        }
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
                List<RdmsJobItem> jobitemListByNextNodeId = rdmsJobItemService.getJobitemListByNextNodeId(projectInfoDto.getOriginSystemManagerId(), statusEnumList);
                if (!CollectionUtils.isEmpty(jobitemListByNextNodeId)) {
                    for (RdmsJobItem jobItem : jobitemListByNextNodeId) {
                        if (!scopeId.equals(jobItem.getSubprojectId())) {
                            continue;
                        }
                        jobItem.setNextNode(projectInfoDto.getSystemManagerId());
                        rdmsJobItemService.updateByPrimaryKeySelective(jobItem);
                    }
                }
                //处理未完成的物料申请
                {
                    List<RdmsMaterialManageDto> listByNextNodeId = rdmsMaterialManageService.getListByNextNodeIdForApprover(projectInfoDto.getOriginSystemManagerId());
                    if(!CollectionUtils.isEmpty(listByNextNodeId)){
                        for(RdmsMaterialManageDto materialManageDto: listByNextNodeId){
                            if (!inScope(scopeId, materialManageDto.getProjectId(), materialManageDto.getSubprojectId())) {
                                continue;
                            }
                            RdmsMaterialManage materialManage = CopyUtil.copy(materialManageDto, RdmsMaterialManage.class);
                            materialManage.setNextNode(projectInfoDto.getSystemManagerId());
                            rdmsMaterialManageService.updateByPrimaryKeySelective(materialManage);
                        }
                    }
                }
                //处理未完成的费用申请
                {
                    List<RdmsFeeManage> listByNextNodeId = rdmsFeeManageService.getListByNextNodeId(projectInfoDto.getOriginSystemManagerId());
                    if(!CollectionUtils.isEmpty(listByNextNodeId)){
                        for(RdmsFeeManage feeManageDto: listByNextNodeId){
                            if (!inScope(scopeId, feeManageDto.getProjectId(), feeManageDto.getSubprojectId())) {
                                continue;
                            }
                            feeManageDto.setNextNode(projectInfoDto.getSystemManagerId());
                            rdmsFeeManageService.updateByPrimaryKeySelective(feeManageDto);
                        }
                    }
                }
            }
        }

        {//如果产品经理发生了变化
            if (StringUtils.hasText(projectInfoDto.getProductManagerId()) && !projectInfoDto.getProductManagerId().equals(projectInfoDto.getOriginProductManagerId())) {
                //该项目的子项目的的上一级产品经理需要改成新的产品经理
                List<RdmsProjectSubprojectDto> subprojectListByProjectId = rdmsProjectService.getSubprojectListByProjectId(projectInfo.getId());
                for (RdmsProjectSubproject subproject : subprojectListByProjectId) {
                    subproject.setProductManagerId(projectInfoDto.getProductManagerId());
                    rdmsSubprojectService.updateByPrimaryKeySelective(subproject);
                }

                //将该项目项下所有任务的产品经理更新为新的产品经理;
                List<RdmsJobItemDto> jobitemList = rdmsJobItemService.getJobitemListByProjectId(projectInfo.getId(), null, null);
                if (!CollectionUtils.isEmpty(jobitemList)) {
                    for (RdmsJobItemDto jobItemDto : jobitemList) {
                        //修改产品经理
                        if (jobItemDto.getProductManagerId().equals(projectInfoDto.getOriginProductManagerId())) {
                            jobItemDto.setProductManagerId(projectInfoDto.getProductManagerId());
                            rdmsJobItemService.updateByPrimaryKeySelective(jobItemDto);
                        }
                    }
                }
                //将公司所有功能定义的产品经理改为新的产品经理;
                List<RdmsCharacter> characterList = rdmsCharacterService.getCharacterListByProjectId(projectInfo.getId());
                if (!CollectionUtils.isEmpty(characterList)) {
                    for (RdmsCharacter character : characterList) {
                        //修改产品经理
                        if (character.getProductManagerId().equals(projectInfoDto.getOriginProductManagerId())) {
                            character.setProductManagerId(projectInfoDto.getProductManagerId());
                            rdmsCharacterService.updateByPrimaryKeySelective(character);
                        }
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
                List<RdmsJobItem> jobitemListByNextNodeId = rdmsJobItemService.getJobitemListByNextNodeId(projectInfoDto.getOriginProductManagerId(), statusEnumList);
                if (!CollectionUtils.isEmpty(jobitemListByNextNodeId)) {
                    for (RdmsJobItem jobItem : jobitemListByNextNodeId) {
                        if (!scopeId.equals(jobItem.getSubprojectId())) {
                            continue;
                        }
                        jobItem.setNextNode(projectInfoDto.getProductManagerId());
                        rdmsJobItemService.updateByPrimaryKeySelective(jobItem);
                    }
                }
                //处理未完成的物料申请
                {
                    List<RdmsMaterialManageDto> listByNextNodeId = rdmsMaterialManageService.getListByNextNodeIdForApprover(projectInfoDto.getOriginProductManagerId());
                    if(!CollectionUtils.isEmpty(listByNextNodeId)){
                        for(RdmsMaterialManageDto materialManageDto: listByNextNodeId){
                            if (!inScope(scopeId, materialManageDto.getProjectId(), materialManageDto.getSubprojectId())) {
                                continue;
                            }
                            RdmsMaterialManage materialManage = CopyUtil.copy(materialManageDto, RdmsMaterialManage.class);
                            materialManage.setNextNode(projectInfoDto.getProductManagerId());
                            rdmsMaterialManageService.updateByPrimaryKeySelective(materialManage);
                        }
                    }
                }
                //处理未完成的费用申请
                {
                    List<RdmsFeeManage> listByNextNodeId = rdmsFeeManageService.getListByNextNodeId(projectInfoDto.getOriginProductManagerId());
                    if(!CollectionUtils.isEmpty(listByNextNodeId)){
                        for(RdmsFeeManage feeManageDto: listByNextNodeId){
                            if (!inScope(scopeId, feeManageDto.getProjectId(), feeManageDto.getSubprojectId())) {
                                continue;
                            }
                            feeManageDto.setNextNode(projectInfoDto.getProductManagerId());
                            rdmsFeeManageService.updateByPrimaryKeySelective(feeManageDto);
                        }
                    }
                }

            }
        }

        {//如果测试主管发生了变化
            if (StringUtils.hasText(projectInfoDto.getTestManagerId()) && !projectInfoDto.getTestManagerId().equals(projectInfoDto.getOriginTestManagerId())) {
                //将该项目项下所有任务的测试主管更新为新的测试主管;
                List<RdmsJobItemDto> jobitemList = rdmsJobItemService.getJobitemListBySubprojectId(projectInfo.getId(), null, null);
                if (!CollectionUtils.isEmpty(jobitemList)) {
                    for (RdmsJobItemDto jobItemDto : jobitemList) {
                        //修改测试主管
                        if (jobItemDto.getTestManagerId().equals(projectInfoDto.getOriginTestManagerId())) {
                            jobItemDto.setTestManagerId(projectInfoDto.getTestManagerId());
                            rdmsJobItemService.updateByPrimaryKeySelective(jobItemDto);
                        }
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
                List<RdmsJobItem> jobitemListByNextNodeId = rdmsJobItemService.getJobitemListByNextNodeId(projectInfoDto.getOriginTestManagerId(), statusEnumList);
                if (!CollectionUtils.isEmpty(jobitemListByNextNodeId)) {
                    for (RdmsJobItem jobItem : jobitemListByNextNodeId) {
                        if (!scopeId.equals(jobItem.getSubprojectId())) {
                            continue;
                        }
                        jobItem.setNextNode(projectInfoDto.getTestManagerId());
                        rdmsJobItemService.updateByPrimaryKeySelective(jobItem);
                    }
                }
                //处理未完成的物料申请
                {
                    List<RdmsMaterialManageDto> listByNextNodeId = rdmsMaterialManageService.getListByNextNodeIdForApprover(projectInfoDto.getOriginTestManagerId());
                    if(!CollectionUtils.isEmpty(listByNextNodeId)){
                        for(RdmsMaterialManageDto materialManageDto: listByNextNodeId){
                            if (!inScope(scopeId, materialManageDto.getProjectId(), materialManageDto.getSubprojectId())) {
                                continue;
                            }
                            RdmsMaterialManage materialManage = CopyUtil.copy(materialManageDto, RdmsMaterialManage.class);
                            materialManage.setNextNode(projectInfoDto.getTestManagerId());
                            rdmsMaterialManageService.updateByPrimaryKeySelective(materialManage);
                        }
                    }
                }
                //处理未完成的费用申请
                {
                    List<RdmsFeeManage> listByNextNodeId = rdmsFeeManageService.getListByNextNodeId(projectInfoDto.getOriginTestManagerId());
                    if(!CollectionUtils.isEmpty(listByNextNodeId)){
                        for(RdmsFeeManage feeManageDto: listByNextNodeId){
                            if (!inScope(scopeId, feeManageDto.getProjectId(), feeManageDto.getSubprojectId())) {
                                continue;
                            }
                            feeManageDto.setNextNode(projectInfoDto.getTestManagerId());
                            rdmsFeeManageService.updateByPrimaryKeySelective(feeManageDto);
                        }
                    }
                }
            }
        }

        {//如果IPMT发生了变化
            if (StringUtils.hasText(projectInfoDto.getSupervise()) && !projectInfoDto.getSupervise().equals(projectInfoDto.getOriginIpmtId())) {
                //该项目的子项目的的上一级IPMT需要改成新的IPMT
                List<RdmsProjectSubproject> subprojectListByParentId = rdmsSubprojectService.getSubprojectListByParentId(projectInfo.getId());
                for (RdmsProjectSubproject subproject : subprojectListByParentId) {
                    subproject.setSupervise(projectInfoDto.getSupervise());
                    rdmsSubprojectService.updateByPrimaryKeySelective(subproject);
                }
            }
        }

        responseDto.setContent(null);
        responseDto.setSuccess(true);
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    /** 改派范围：挂在本子项上，或未分子项而属于本项目（根子项标识与项目相同）。 */
    private static boolean inScope(String scopeId, String projectId, String subprojectId) {
        return scopeId.equals(subprojectId) || (!StringUtils.hasText(subprojectId) && scopeId.equals(projectId));
    }

    /**
     * 立项管理“追加”：待批立项申请中的特性并入已立项项目，模块项目申请由 moduleSetupProjectId 指定
     */
    @PostMapping("/append")
    public ResponseDto<RdmsProjectSetupDto> append(@RequestBody RdmsProjectSetupDto projectSetupDto) {
        ResponseDto<RdmsProjectSetupDto> responseDto = new ResponseDto<>();
        String applicationId = ObjectUtils.isEmpty(projectSetupDto.getModuleSetupProjectId())
                ? projectSetupDto.getId() : projectSetupDto.getModuleSetupProjectId();
        ValidatorUtil.require(applicationId, "立项申请ID");
        ValidatorUtil.require(projectSetupDto.getTargetProjectId(), "目标项目ID");
        rdmsProjectSetupService.appendApplication(applicationId, projectSetupDto.getTargetProjectId(),
                projectSetupDto.getCharacterList(), projectSetupDto.getLoginUserId());
        responseDto.setContent(projectSetupDto);
        responseDto.setSuccess(true);
        responseDto.setMessage("保存成功");
        return responseDto;
    }
    @PostMapping("/setup-application")
    public ResponseDto<RdmsProjectSetupDto> setupApplication(@RequestBody RdmsProjectSetupDto projectSetupDto) {
        return rdmsProjectSetupService.setupApplication(projectSetupDto);
    }
    @PostMapping("/direct-setup")
    public ResponseDto<RdmsProjectSetupDto> directSetup(@RequestBody RdmsProjectSetupDto projectSetupDto) {
        return rdmsProjectSetupService.directSetup(projectSetupDto);
    }
    @PostMapping("/module-setup")
    public ResponseDto<RdmsProjectSetupDto> moduleSetup(@RequestBody RdmsProjectSetupDto projectSetupDto) {
        return rdmsProjectSetupService.moduleSetup(projectSetupDto);
    }
    @PostMapping("/reject-application")
    public ResponseDto<RdmsProjectSetupDto> rejectApplication(@RequestBody RdmsProjectSetupDto projectSetupDto) {
        return rdmsProjectSetupService.rejectApplication(projectSetupDto);
    }
    @PostMapping("/withdrawProject/{projectId}")
    public ResponseDto<String> withdrawProject(@PathVariable String projectId) {
        return rdmsProjectSetupService.withdrawProject(projectId);
    }

    /**
     * 保存客户信息
     */
    @PostMapping("/append-characters")
    @Transactional
    public ResponseDto<RdmsProjectSetupDto> appendCharacters(@RequestBody RdmsProjectSetupDto projectSetupDto) {
        ResponseDto<RdmsProjectSetupDto> responseDto = new ResponseDto<>();

        ValidatorUtil.require(projectSetupDto.getId(), "项目ID");
        RdmsProject targetProject = rdmsProjectSetupService.requireProject(projectSetupDto.getId());
        TenantAccess.assertSameTenant(targetProject.getCustomerId());
        LifecycleTransition.requireSource(targetProject.getStatus(), ProjectStatusEnum.appendCharacterSources(),
                BusinessExceptionCode.LIFECYCLE_TRANSITION_FORBIDDEN);

        List<RdmsCharacterDto> characterDtoList = projectSetupDto.getCharacterList();
        rdmsProjectSetupService.appendCharacters(targetProject,
                characterDtoList == null ? new ArrayList<>() : characterDtoList, projectSetupDto.getLoginUserId());

        responseDto.setContent(projectSetupDto);
        responseDto.setSuccess(true);
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    /**
     * 列表查询所有预立项项目项下经过审批的特性特性清单
     */
    @PostMapping("/listProjectsByPreProjectId/{preProjectId}")
    public ResponseDto<List<RdmsProjectDto>> listProjectsByPreProjectId(@PathVariable String preProjectId) {
        ResponseDto<List<RdmsProjectDto>> responseDto = new ResponseDto<>();
        List<RdmsProjectDto> projectDtos = rdmsProjectService.listProjectsByPreProjectId(preProjectId);
        responseDto.setContent(projectDtos);
        return responseDto;
    }

    @PostMapping("/getCbbList/{projectId}")
    public ResponseDto<List<RdmsCharacterDto>> getCbbList(@PathVariable String projectId) {
        ResponseDto<List<RdmsCharacterDto>> responseDto = new ResponseDto<>();
        List<RdmsCharacterDto> cbbList = rdmsProjectService.getCbbList(projectId);
        responseDto.setContent(cbbList);
        return responseDto;
    }

    /**
     * 列表查询所有预立项项目项下经过审批的特性特性清单
     */
    @PostMapping("/list-setuped-projects/{customerId}")
    public ResponseDto<List<RdmsProjectDto>> listSetupedProjects(@PathVariable String customerId) {
        ResponseDto<List<RdmsProjectDto>> responseDto = new ResponseDto<>();
        List<RdmsProjectDto> projectDtos = rdmsProjectService.listSetupedProjects(customerId);
        responseDto.setContent(projectDtos);
        return responseDto;
    }

    @PostMapping("/abandonProject/{projectId}")
    public ResponseDto<String> abandonProject(@PathVariable String projectId) {
        ResponseDto<String> responseDto = new ResponseDto<>();
        responseDto.setContent(projectId);
        responseDto.setSuccess(true);
        responseDto.setMessage(rdmsProjectCloseService.abandon(projectId));
        return responseDto;
    }

    /**
     * 删除
     */
    @DeleteMapping("/del/{id}")
    public ResponseDto delete(@PathVariable String id) {
        ResponseDto responseDto = new ResponseDto();
        ValidatorUtil.require(id, "项目ID");
        String actorId = rdmsProjectSetupService.resolveSetupActorId(null);
        RdmsProject project = rdmsProjectSetupService.requireProject(id);
        TenantAccess.assertSameTenant(project.getCustomerId());
        rdmsProjectSetupService.assertCanDeleteDirectSetup(project, actorId);
        rdmsProjectService.delete(id);
        return responseDto;
    }

    /** 存量项目补选所属产品：body 为 id、productId，或 newProductName、newProductCode 新建产品。 */
    @PostMapping("/product/update")
    public ResponseDto<RdmsProject> fillProduct(@RequestBody RdmsProjectSetupDto dto) {
        ResponseDto<RdmsProject> responseDto = new ResponseDto<>();
        responseDto.setContent(rdmsProductService.fillProjectProduct(dto.getId(), dto.getProductId(),
                dto.getNewProductName(), dto.getNewProductCode()));
        responseDto.setMessage("所属产品已登记");
        return responseDto;
    }
}
