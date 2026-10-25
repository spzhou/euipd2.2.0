/**
 * Author：周朔鹏
 * 编制时间：2025-08-05

 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */

package com.course.customer.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.course.server.domain.*;
import com.course.server.dto.PageDto;
import com.course.server.dto.ResponseDto;
import com.course.server.dto.rdms.*;
import com.course.server.enums.FileGroupingEnum;
import com.course.server.enums.rdms.*;
import com.course.server.exception.BusinessException;
import com.course.server.exception.BusinessExceptionCode;
import com.course.server.security.tenant.TenantAccess;
import com.course.server.service.rdms.rich.CharacterRichDocService;
import com.course.server.service.rdms.*;
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
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/job-items")
public class JobItemController {
    private static final Logger LOG = LoggerFactory.getLogger(JobItemController.class);
    public static final String BUSINESS_NAME = "工单";

    @Resource
    private RdmsJobItemService rdmsJobItemService;
    @Resource
    private TenantParentGuard tenantParentGuard;
    @Resource
    private RdmsJobItemSubmitService rdmsJobItemSubmitService;
    @Resource
    private RdmsJobItemRefuseService rdmsJobItemRefuseService;
    @Resource
    private RdmsJobItemFlowService rdmsJobItemFlowService;
    @Resource
    private JobActorGuard jobActorGuard;
    @Resource
    private RdmsJobItemEvaluateService rdmsJobItemEvaluateService;
    @Resource
    private JobItemProcessRecorder jobItemProcessRecorder;
    @Resource
    private RdmsJobItemReviewService rdmsJobItemReviewService;
    @Resource
    private RdmsFileService rdmsFileService;
    @Resource
    private RdmsCharacterService rdmsCharacterService;
    @Resource
    private RdmsCharacterProcessService rdmsCharacterProcessService;
    @Resource
    private RdmsProjectService rdmsProjectService;
    @Resource
    private RdmsPreProjectService rdmsPreProjectService;
    @Resource
    private RdmsCustomerUserService rdmsCustomerUserService;
    @Resource
    private RdmsDemandService rdmsDemandService;
    @Resource
    private RdmsSubprojectService rdmsSubprojectService;
    @Resource
    private RdmsManhourService rdmsManhourService;
    @Resource
    private RdmsMaterialManageService rdmsMaterialManageService;
    @Resource
    private RdmsFeeManageService rdmsFeeManageService;
    @Resource
    private RdmsQualityService rdmsQualityService;
    @Resource
    private RdmsCategoryFileService rdmsCategoryFileService;
    @Resource
    private RdmsJobItemGanttService rdmsJobItemGanttService;
    @Resource
    private JobIssueWindowGuard jobIssueWindowGuard;
    @Autowired
    private RdmsBugFeedbackService rdmsBugFeedbackService;
    @Resource
    private RdmsCbbService rdmsCbbService;
    @Resource
    private ReviewStatusQuery reviewStatusQuery;

    /**
     * 根据下一节点和状态获取里程碑评审数量
     * 统计指定下一节点下处于提交状态的里程碑评审工单数量
     * 
     * @param nextNode 下一节点标识
     * @return 返回评审工单数量
     */
    @PostMapping("/getNumOfMilestoneReviewByNextNodeAndStatus/{nextNode}")
    public ResponseDto<Integer> getNumOfMilestoneReviewByNextNodeAndStatus(@PathVariable String nextNode) {
        nextNode = TenantAccess.resolveMemberId(nextNode);
        ResponseDto<Integer> responseDto = new ResponseDto<>();
        Integer reviewJobNum = rdmsJobItemService.countPendingReviewMainByNextNode(nextNode, JobItemTypeEnum.REVIEW_MILESTONE);
        responseDto.setContent(reviewJobNum);
        return responseDto;
    }

    /**
     * 根据评审工单获取开发工单列表
     * 查询与评审工单关联的开发工单和集成工单，包括协作工单信息
     * 
     * @param jobitemId 评审工单ID
     * @param loginUserId 登录用户ID
     * @return 返回开发工单列表
     */
    @PostMapping("/getDevelopJobitemListByReviewJobitem")
    public ResponseDto<List<RdmsJobItemDto>> getDevelopJobitemListByReviewJobitem(@RequestParam String jobitemId, String loginUserId) {
        ResponseDto<List<RdmsJobItemDto>> responseDto = new ResponseDto<>();
        RdmsJobItem reviewJobItem = tenantParentGuard.requireJobItem(jobitemId);
        List<JobItemStatusEnum> statusEnumList = new ArrayList<>();
        statusEnumList.add(JobItemStatusEnum.COMPLETED);
        statusEnumList.add(JobItemStatusEnum.ARCHIVED);

        List<JobItemTypeEnum> typeEnumList = new ArrayList<>();
        typeEnumList.add(JobItemTypeEnum.DEVELOP);
        typeEnumList.add(JobItemTypeEnum.CHARACTER_INT);
        List<RdmsJobItemDto> jobitemList = rdmsJobItemService.getJobitemListByCharacterIdAndStatusListTypeList(reviewJobItem.getCharacterId(), statusEnumList, typeEnumList);
        //为工单添加辅助信息
        if(! CollectionUtils.isEmpty(jobitemList)){
            for(RdmsJobItemDto jobItemDto : jobitemList){
                rdmsJobItemService.appendRecordSimpleInfo(jobItemDto, loginUserId);
            }
        }

        //读出集成工单的协作工单
        if(! CollectionUtils.isEmpty(jobitemList)){
            for(RdmsJobItemDto jobItemDto: jobitemList){
                if(jobItemDto.getType().equals(JobItemTypeEnum.CHARACTER_INT.getType())){
                    List<RdmsJobItemDto> assistJobListDto = rdmsJobItemService.getJobitemByParentJobitemId(jobItemDto.getId(), JobItemTypeEnum.ASSIST);
                    if(! CollectionUtils.isEmpty(assistJobListDto)){
                        for(RdmsJobItemDto assistJobDto: assistJobListDto){
                            rdmsJobItemService.appendRecordSimpleInfo(assistJobDto, loginUserId);
                        }
                    }
                    jobItemDto.setAssistJobitemList(assistJobListDto);
                }
            }
        }
        responseDto.setContent(jobitemList);
        return responseDto;
    }

    /**
     * 根据评审工单获取测试工单列表
     * 查询与评审工单关联的测试工单，为工单添加辅助信息
     * 
     * @param jobitemId 评审工单ID
     * @param loginUserId 登录用户ID
     * @return 返回测试工单列表
     */
    @PostMapping("/getTestJobitemListByReviewJobitem")
    public ResponseDto<List<RdmsJobItemDto>> getTestJobitemListByReviewJobitem(@RequestParam String jobitemId, String loginUserId) {
        ResponseDto<List<RdmsJobItemDto>> responseDto = new ResponseDto<>();
        RdmsJobItem reviewJobItem = rdmsJobItemService.selectByPrimaryKey(jobitemId);
        List<JobItemStatusEnum> statusEnumList = new ArrayList<>();
        statusEnumList.add(JobItemStatusEnum.COMPLETED);
        statusEnumList.add(JobItemStatusEnum.ARCHIVED);

        List<JobItemTypeEnum> typeEnumList = new ArrayList<>();
        typeEnumList.add(JobItemTypeEnum.TEST);

        /*List<JobItemTypeEnum> notInAuxTypeList = new ArrayList<>();
        notInAuxTypeList.add(JobItemTypeEnum.CHARACTER_INT);
        notInAuxTypeList.add(JobItemTypeEnum.TASK_SUBP);
        List<RdmsJobItemDto> jobitemList = rdmsJobItemService.getJobitemListByCharacterIdAndStatusListTypeListNotInAuxTypeList(reviewJobItem.getCharacterId(), statusEnumList, typeEnumList, notInAuxTypeList);
        */
        List<RdmsJobItemDto> jobitemList = rdmsJobItemService.getJobitemListByCharacterIdAndStatusListTypeList(reviewJobItem.getCharacterId(), statusEnumList, typeEnumList);
        //为工单添加辅助信息
        if(! CollectionUtils.isEmpty(jobitemList)){
            for(RdmsJobItemDto jobItemDto : jobitemList){
                rdmsJobItemService.appendRecordSimpleInfo(jobItemDto, loginUserId);
            }
        }
        responseDto.setContent(jobitemList);
        return responseDto;
    }

    /**
     * 根据评审工单获取物料列表
     * 查询与评审工单关联的物料管理信息
     * 
     * @param jobitemId 评审工单ID
     * @return 返回物料管理列表
     */
    @PostMapping("/getMaterialListByReviewJobitem/{jobitemId}")
    public ResponseDto<List<RdmsMaterialManageDto>> getMaterialListByReviewJobitem(@PathVariable String jobitemId) {
        ResponseDto<List<RdmsMaterialManageDto>> responseDto = new ResponseDto<>();
        RdmsJobItem reviewJobItem = rdmsJobItemService.selectByPrimaryKey(jobitemId);
        List<RdmsMaterialManageDto> materialManageList = rdmsMaterialManageService.getListByCharacterIdForStatistics(reviewJobItem.getCharacterId());

        //需要展示物料的签字表格等信息 TODO 提交评审的时候, 要对资产化和借用的物料进行盘点
        responseDto.setContent(materialManageList);
        return responseDto;
    }

    /**
     * 根据评审工单获取费用列表
     * 查询与评审工单关联的费用管理信息
     * 
     * @param jobitemId 评审工单ID
     * @return 返回费用管理列表
     */
     @PostMapping("/getFeeListByReviewJobitem/{jobitemId}")
    public ResponseDto<List<RdmsFeeManageDto>> getFeeListByReviewJobitem(@PathVariable String jobitemId) {
        ResponseDto<List<RdmsFeeManageDto>> responseDto = new ResponseDto<>();
        RdmsJobItem reviewJobItem = rdmsJobItemService.selectByPrimaryKey(jobitemId);
        List<RdmsFeeManageDto> feeManageList = rdmsFeeManageService.getListByCharacterIdForStatistics(reviewJobItem.getCharacterId());
        responseDto.setContent(feeManageList);
        return responseDto;
    }

    /**
     * 获取开发角色待审核工单数量
     * 统计指定角色下处于提交状态的开发工单数量（不包括辅助工单）
     * 
     * @param characterId 角色ID
     * @return 返回待审核的开发工单数量
     */
    @PostMapping("/getExamineNum_develop/{characterId}")
    public ResponseDto<Integer> getExamineNum_develop(@PathVariable String characterId) {
        ResponseDto<Integer> responseDto = new ResponseDto<>();
        List<JobItemStatusEnum> statusEnumList = new ArrayList<>();
        statusEnumList.add(JobItemStatusEnum.SUBMIT);
        Integer examineNum = rdmsJobItemService.getDevelopJobitemNumberByCharacterAndStatus_notAssist(characterId, statusEnumList);
        responseDto.setContent(examineNum);
        return responseDto;
    }

    /**
     * 获取测试角色待审核工单数量
     * 统计指定角色下处于提交状态的测试工单数量
     * 
     * @param characterId 角色ID
     * @return 返回待审核的测试工单数量
     */
    @PostMapping("/getExamineNum_character_test/{characterId}")
    public ResponseDto<Integer> getExamineNum_character_test(@PathVariable String characterId) {
        ResponseDto<Integer> responseDto = new ResponseDto<>();
        List<JobItemStatusEnum> statusEnumList = new ArrayList<>();
        statusEnumList.add(JobItemStatusEnum.SUBMIT);
        Integer examineNum = rdmsJobItemService.getTaskTestJobitemNumberByCharacterAndStatus(characterId, statusEnumList);
        responseDto.setContent(examineNum);
        return responseDto;
    }

    /**
     * 获取质量角色待审核工单数量
     * 统计指定质量角色下处于提交状态的工单数量
     * 
     * @param qualityId 质量角色ID
     * @return 返回待审核的工单数量
     */
    @PostMapping("/getExamineNum_quality/{qualityId}")
    public ResponseDto<Integer> getExamineNum_quality(@PathVariable String qualityId) {
        ResponseDto<Integer> responseDto = new ResponseDto<>();
        List<JobItemStatusEnum> statusEnumList = new ArrayList<>();
        statusEnumList.add(JobItemStatusEnum.SUBMIT);
        Integer examineNum = rdmsJobItemService.getJobitemNumberByQualityAndStatus(qualityId, statusEnumList);
        responseDto.setContent(examineNum);
        return responseDto;
    }

    /**
     * 获取Bug反馈待审核工单数量
     * 统计指定Bug反馈下处于提交状态的工单数量
     * 
     * @param bugId Bug反馈ID
     * @return 返回待审核的工单数量
     */
    @PostMapping("/getExamineNum_bugfeedback/{bugId}")
    public ResponseDto<Integer> getExamineNum_bugfeedback(@PathVariable String bugId) {
        ResponseDto<Integer> responseDto = new ResponseDto<>();
        List<JobItemStatusEnum> statusEnumList = new ArrayList<>();
        statusEnumList.add(JobItemStatusEnum.SUBMIT);
        Integer examineNum = rdmsJobItemService.getJobitemNumberByBugFeedbackAndStatus(bugId, statusEnumList);
        responseDto.setContent(examineNum);
        return responseDto;
    }

    /**
     * 获取任务待审核辅助工单数量
     * 统计指定任务下处于提交状态的辅助工单数量
     * 
     * @param jobitemId 工单ID
     * @return 返回待审核的辅助工单数量
     */
    @PostMapping("/getExamineNum_task/{jobitemId}")
    public ResponseDto<Integer> getExamineNum_task(@PathVariable String jobitemId) {
        ResponseDto<Integer> responseDto = new ResponseDto<>();
        tenantParentGuard.requireJobItem(jobitemId);
        List<JobItemStatusEnum> statusEnumList = new ArrayList<>();
        statusEnumList.add(JobItemStatusEnum.SUBMIT);
        List<RdmsJobItemDto> assistJobitemListByJobitemIdAndStatus = rdmsJobItemService.getJobitemListByJobitemIdAndStatus(jobitemId, JobItemStatusEnum.SUBMIT.getStatus(), JobItemTypeEnum.ASSIST.getType());
        responseDto.setContent(assistJobitemListByJobitemIdAndStatus.size());
        return responseDto;
    }

    /**
     * 获取测试待审核Bug工单数量
     * 统计指定工单下处于提交状态的Bug工单数量
     * 
     * @param jobitemId 工单ID
     * @return 返回待审核的Bug工单数量
     */
    @PostMapping("/getExamineNum_test/{jobitemId}")
    public ResponseDto<Integer> getExamineNum_test(@PathVariable String jobitemId) {
        ResponseDto<Integer> responseDto = new ResponseDto<>();
        tenantParentGuard.requireJobItem(jobitemId);
        List<JobItemStatusEnum> statusEnumList = new ArrayList<>();
        statusEnumList.add(JobItemStatusEnum.SUBMIT);
        List<RdmsJobItemDto> assistJobitemListByJobitemIdAndStatus = rdmsJobItemService.getJobitemListByJobitemIdAndStatus(jobitemId, JobItemStatusEnum.SUBMIT.getStatus(), JobItemTypeEnum.BUG.getType());
        responseDto.setContent(assistJobitemListByJobitemIdAndStatus.size());
        return responseDto;
    }

    /**
     * 查询依据CharacterId创建的工单, 并且这些工单处于submit状态
     */
    @Transactional
    @PostMapping("/getNotifyDevelopNum/{nextNode}")
    public ResponseDto<Integer> getNotifyDevelopNum(@PathVariable String nextNode) {
        nextNode = TenantAccess.resolveMemberId(nextNode);
        ResponseDto<Integer> responseDto = new ResponseDto<>();
        List<JobItemStatusEnum> statusEnumList = new ArrayList<>();
        statusEnumList.add(JobItemStatusEnum.SUBMIT);
        Integer examineNum = rdmsJobItemService.getJobitemNumOfDevelopByNextNodeAndStatus(nextNode, statusEnumList);
        int waitingMerge = rdmsJobItemService.countApprovedIterationByNextNode(nextNode);
        responseDto.setContent(examineNum + waitingMerge);
        return responseDto;
    }

    @Transactional
    @PostMapping("/countQualityJobFlag/{nextNode}")
    public ResponseDto<Integer> countQualityJobFlag(@PathVariable String nextNode) {
        nextNode = TenantAccess.resolveMemberId(nextNode);
        ResponseDto<Integer> responseDto = new ResponseDto<>();
        List<JobItemStatusEnum> statusEnumList = new ArrayList<>();
        statusEnumList.add(JobItemStatusEnum.SUBMIT);
        Integer examineNum = rdmsJobItemService.getJobitemOfQualityByNextNodeAndStatus(nextNode, statusEnumList);
        responseDto.setContent(examineNum);
        return responseDto;
    }

    @Transactional
    @PostMapping("/getRefuseNumByUserId/{nextNode}")
    public ResponseDto<Integer> getRefuseNumByUserId(@PathVariable String nextNode) {
        nextNode = TenantAccess.resolveMemberId(nextNode);
        ResponseDto<Integer> responseDto = new ResponseDto<>();
        Integer refuseNum = rdmsJobItemService.getRefuseNumByUserId(nextNode);
        responseDto.setContent(refuseNum);
        return responseDto;
    }

    @Transactional
    @PostMapping("/getSubmitSubTaskJobitemNum/{userId}")
    public ResponseDto<Integer> getSubmitSubTaskJobitemNum(@PathVariable String userId) {
        userId = TenantAccess.resolveMemberId(userId);
        ResponseDto<Integer> responseDto = new ResponseDto<>();
        Integer examineNum = rdmsJobItemService.getSubmitSubTaskJobitemNumByPjm(userId);
        responseDto.setContent(examineNum);
        return responseDto;
    }

    @Transactional
    @PostMapping("/getSubmitSubTaskJobitemNumByPjmAndSubprojectId")
    public ResponseDto<Integer> getSubmitSubTaskJobitemNumByPjmAndSubprojectId(@RequestBody RdmsHmiIdsDto idsDto) {
        ResponseDto<Integer> responseDto = new ResponseDto<>();
        Integer examineNum = rdmsJobItemService.getSubmitSubTaskJobitemNumByPjmAndSubprojectId(idsDto);
        responseDto.setContent(examineNum);
        return responseDto;
    }

    @Transactional
    @PostMapping("/getSubmitPreTaskJobitemNumBySysgmAndPreprojectId")
    public ResponseDto<Integer> getSubmitPreTaskJobitemNumBySysgmAndPreprojectId(@RequestBody RdmsHmiIdsDto idsDto) {
        ResponseDto<Integer> responseDto = new ResponseDto<>();
        Integer examineNum = rdmsJobItemService.getSubmitPreTaskJobitemNumBySysgmAndPreprojectId(idsDto);
        responseDto.setContent(examineNum);
        return responseDto;
    }

    @Transactional
    @PostMapping("/getSubmitTaskJobitemNumByPjmAndProjectId")
    public ResponseDto<Integer> getSubmitTaskJobitemNumByPjmAndProjectId(@RequestBody RdmsHmiIdsDto idsDto) {
        ResponseDto<Integer> responseDto = new ResponseDto<>();
        Integer examineNum = rdmsJobItemService.getSubmitTaskJobitemNumByPjmAndProjectId(idsDto);
        responseDto.setContent(examineNum);
        return responseDto;
    }

    /**
     * 根据subprojectId进行列表查询
     */
    @PostMapping("/listBySubprojectId")
    public ResponseDto<PageDto<RdmsJobItemDto>> listBySubprojectId(@RequestBody PageDto<RdmsJobItemDto> pageDto) {
        ResponseDto<PageDto<RdmsJobItemDto>> responseDto = new ResponseDto<>();
        rdmsJobItemService.listBySubprojectId(pageDto);
        for(RdmsJobItemDto jobItemsDto : pageDto.getList()){
            if(jobItemsDto.getFileList() != null){
                for(RdmsFileDto fileDto : jobItemsDto.getFileList()){
                    RdmsFileDto fileSimpleRecordInfo = rdmsFileService.getFileSimpleRecordInfo(fileDto.getId());
                    fileDto.setAbsPath(fileSimpleRecordInfo.getAbsPath());
                }
            }
        }
        responseDto.setContent(pageDto);
        return responseDto;
    }

    /**
     * 根据projectId进行列表查询
     */
    @PostMapping("/listByProjectId")
    public ResponseDto<PageDto<RdmsJobItemDto>> listByProjectId(@RequestBody PageDto<RdmsJobItemDto> pageDto) {
        ResponseDto<PageDto<RdmsJobItemDto>> responseDto = new ResponseDto<>();
        rdmsJobItemService.listByProjectId(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

    /**
     * 根据projectId进行列表查询
     */
    @PostMapping("/listJobitemListBySubProjectId")
    public ResponseDto<PageDto<RdmsJobItemDto>> listJobitemListBySubProjectId(@RequestBody PageDto<RdmsJobItemDto> pageDto) {
        ResponseDto<PageDto<RdmsJobItemDto>> responseDto = new ResponseDto<>();
        rdmsJobItemService.listJobitemListBySubProjectId(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

   @PostMapping("/listJobitemListByPreProjectId")
    public ResponseDto<PageDto<RdmsJobItemDto>> listJobitemListByPreProjectId(@RequestBody PageDto<RdmsJobItemDto> pageDto) {
        ResponseDto<PageDto<RdmsJobItemDto>> responseDto = new ResponseDto<>();
        rdmsJobItemService.listJobitemListByPreProjectId(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

    /**
     * 根据给定的工单状态查询某个project下的工单列表
     */
    @PostMapping("/listJobItem")
    public ResponseDto<PageDto<RdmsJobItemDto>> listJobItemByProjectIdAndStatus(@RequestBody PageDto<RdmsJobItemDto> pageDto) {
        ResponseDto<PageDto<RdmsJobItemDto>> responseDto = new ResponseDto<>();
        rdmsJobItemService.listJobItemByProjectIdAndStatus(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

    @PostMapping("/getReviewHasListFlag/{userId}")
    public ResponseDto<Boolean> getReviewHasListFlag(@PathVariable String userId) {
        userId = TenantAccess.resolveMemberId(userId);
        ResponseDto<Boolean> responseDto = new ResponseDto<>();
        Integer reviewHasListFlag = rdmsJobItemService.getReviewHasListFlag(userId);
        responseDto.setContent(reviewHasListFlag >0);
        return responseDto;
    }

    /**
     * 根据给定的工单状态查询某个project下的工单列表
     */
    @PostMapping("/listReviewJobitems")
    public ResponseDto<PageDto<RdmsJobItemDto>> listReviewJobitems(@RequestBody PageDto<RdmsJobItemDto> pageDto) {
        TenantAccess.bindSelf(pageDto);
        ResponseDto<PageDto<RdmsJobItemDto>> responseDto = new ResponseDto<>();
        rdmsJobItemService.listReviewJobitems(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

    /**
     * 根据customerId进行列表查询
     */
    @PostMapping("/listByCustomerId")
    public ResponseDto<PageDto<RdmsJobItemDto>> listByCustomerId(@RequestBody PageDto<RdmsJobItemDto> pageDto) {
        ResponseDto<PageDto<RdmsJobItemDto>> responseDto = new ResponseDto<>();
        rdmsJobItemService.listByCustomerId(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

    /**
     * 根据loginUserId进行列表查询
     */
    @PostMapping("/listByLoginUserId")
    public ResponseDto<PageDto<RdmsJobItemDto>> listByLoginUserId(@RequestBody PageDto<RdmsJobItemDto> pageDto) {
        TenantAccess.bindSelf(pageDto);
        ResponseDto<PageDto<RdmsJobItemDto>> responseDto = new ResponseDto<>();
        rdmsJobItemService.listByLoginUserId(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

    @PostMapping("/getTestJobListBySubproject")
    public ResponseDto<PageDto<RdmsJobItemDto>> getTestJobListBySubproject(@RequestBody PageDto<RdmsJobItemDto> pageDto) {
        ResponseDto<PageDto<RdmsJobItemDto>> responseDto = new ResponseDto<>();
        rdmsJobItemService.getTestJobListBySubproject(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

    @PostMapping("/getBugJobListBySubproject")
    public ResponseDto<PageDto<RdmsJobItemDto>> getBugJobListBySubproject(@RequestBody PageDto<RdmsJobItemDto> pageDto) {
        ResponseDto<PageDto<RdmsJobItemDto>> responseDto = new ResponseDto<>();
        rdmsJobItemService.getBugJobListBySubproject(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

    /**
     * 根据loginUserId进行列表查询
     */
    @PostMapping("/listReviewsByLoginUserId")
    public ResponseDto<PageDto<RdmsJobItemDto>> listReviewsByLoginUserId(@RequestBody PageDto<RdmsJobItemDto> pageDto) {
        ResponseDto<PageDto<RdmsJobItemDto>> responseDto = new ResponseDto<>();
        rdmsJobItemService.listReviewsByLoginUserId(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

    /**
     * 根据loginUserId进行列表查询
     */
    @PostMapping("/getReviewJobitemsByCharacterId/{characterId}")
    public ResponseDto<List<RdmsJobItemDto>> getReviewJobitemsByCharacterId(@PathVariable String characterId) {
        ResponseDto<List<RdmsJobItemDto>> responseDto = new ResponseDto<>();
        List<RdmsJobItemDto> reviewJobitemsByCharacterId = rdmsJobItemService.getReviewJobitemsByCharacterId(characterId);
        responseDto.setContent(reviewJobitemsByCharacterId);
        return responseDto;
    }

    @PostMapping("/jobListByLoginUserId/{workerId}")
    public ResponseDto<List<RdmsJobItemDto>> jobListByLoginUserId(@PathVariable String workerId) {
        ResponseDto<List<RdmsJobItemDto>> responseDto = new ResponseDto<>();
        List<RdmsJobItemDto> reviewJobitems = rdmsJobItemService.jobListByLoginUserId(workerId);
        responseDto.setContent(reviewJobitems);
        return responseDto;
    }

    @PostMapping("/getCompleteJobitemListBySubprojectId/{subprojectId}")
    public ResponseDto<List<RdmsJobItemSimpleDto>> getCompleteJobitemListBySubprojectId(@PathVariable String subprojectId) {
        ResponseDto<List<RdmsJobItemSimpleDto>> responseDto = new ResponseDto<>();
        List<JobItemStatusEnum> statusList = new ArrayList<>();
        statusList.add(JobItemStatusEnum.COMPLETED);
        statusList.add(JobItemStatusEnum.ARCHIVED);
        List<RdmsJobItemDto> completeJobList = rdmsJobItemService.getJobitemListBySubprojectIdAndStatusListTypeList(subprojectId, statusList, null);
        List<RdmsJobItemSimpleDto> rdmsJobItemSimpleDtos = CopyUtil.copyList(completeJobList, RdmsJobItemSimpleDto.class);
        responseDto.setContent(rdmsJobItemSimpleDtos);
        return responseDto;
    }

    /**
     * 根据loginUserId进行列表查询
     */
    @PostMapping("/countJobitemsByLoginUserId/{userId}")
    public ResponseDto<Integer> countJobitemsByLoginUserId(@PathVariable String userId) {
        userId = TenantAccess.resolveMemberId(userId);
        ResponseDto<Integer> responseDto = new ResponseDto<>();
        Integer i = rdmsJobItemService.countJobitemsByLoginUserId(userId);
        responseDto.setContent(i);
        return responseDto;
    }

    /**
     * 根据loginUserId进行列表查询
     */
    @PostMapping("/getReviewJobitemsByProjectId/{projectId}")
    public ResponseDto<List<RdmsJobItemDto>> getReviewJobitemsByProjectId(@PathVariable String projectId) {
        ResponseDto<List<RdmsJobItemDto>> responseDto = new ResponseDto<>();
        List<RdmsJobItemDto> reviewJobitemsByProjectId = rdmsJobItemService.getReviewJobitemsByProjectId(projectId);
        responseDto.setContent(reviewJobitemsByProjectId);
        return responseDto;
    }

    /**
     * 对jobitem表进行"简单"查询
     */
    @PostMapping("/getJobitemHandlingList")
    public ResponseDto<PageDto<RdmsJobItemDto>> getJobitemHandlingList(@RequestBody PageDto<RdmsJobItemDto> pageDto) {
        ResponseDto<PageDto<RdmsJobItemDto>> responseDto = new ResponseDto<>();
        rdmsJobItemService.getJobitemHandlingList(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

    /**
     * 对jobitem表进行"简单"查询
     */
    @PostMapping("/getSystemJobitemHandlingList")
    public ResponseDto<PageDto<RdmsJobItemDto>> getSystemJobitemHandlingList(@RequestBody PageDto<RdmsJobItemDto> pageDto) {
        TenantAccess.bindSelf(pageDto);
        ResponseDto<PageDto<RdmsJobItemDto>> responseDto = new ResponseDto<>();
        rdmsJobItemService.getSystemJobitemHandlingList(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

    /**
     * 对jobitem表进行"简单"查询
     */
    @PostMapping("/getDevelopJobitemHandlingList")
    public ResponseDto<PageDto<RdmsJobItemDto>> getDevelopJobitemHandlingList(@RequestBody PageDto<RdmsJobItemDto> pageDto) {
        TenantAccess.bindSelf(pageDto);
        ResponseDto<PageDto<RdmsJobItemDto>> responseDto = new ResponseDto<>();
        rdmsJobItemService.getDevelopJobitemHandlingList(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

    /**
     * 对jobitem表进行"简单"查询
     */
    @PostMapping("/getJobitemExamineList")
    public ResponseDto<PageDto<RdmsJobItemDto>> getJobitemExamineList(@RequestBody PageDto<RdmsJobItemDto> pageDto) {
        ResponseDto<PageDto<RdmsJobItemDto>> responseDto = new ResponseDto<>();
        rdmsJobItemService.getJobitemExamineList(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

    /**
     * 对jobitem表进行"简单"查询
     */
    @PostMapping("/getReCheckCharactersByJobitemId/{jobitemId}")
    public ResponseDto<List<RdmsCharacterDto>> getReCheckCharactersByJobitemId(@PathVariable String jobitemId) {
        ResponseDto<List<RdmsCharacterDto>> responseDto = new ResponseDto<>();
        List<RdmsCharacterDto> characterDtos = new ArrayList<>();
        RdmsCharacterDto charactersByJobitemId = rdmsJobItemService.getReCheckCharactersByJobitemId(jobitemId);
        characterDtos.add(charactersByJobitemId);
        responseDto.setContent(characterDtos);
        return responseDto;
    }

    /**
     * 对jobitem表进行"简单"查询
     */
    @PostMapping("/getSystemJobitemExamineList")
    public ResponseDto<PageDto<RdmsJobItemDto>> getSystemJobitemExamineList(@RequestBody PageDto<RdmsJobItemDto> pageDto) {
        TenantAccess.bindSelf(pageDto);
        ResponseDto<PageDto<RdmsJobItemDto>> responseDto = new ResponseDto<>();
        rdmsJobItemService.getSystemJobitemExamineList(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

    /**
     * 对jobitem表进行"简单"查询
     */
    @PostMapping("/getJobitemEvaluateList")
    public ResponseDto<PageDto<RdmsJobItemDto>> getJobitemEvaluateList(@RequestBody PageDto<RdmsJobItemDto> pageDto) {
        TenantAccess.bindSelf(pageDto);
        ResponseDto<PageDto<RdmsJobItemDto>> responseDto = new ResponseDto<>();
        rdmsJobItemService.getJobitemEvaluateList(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

    /**
     * 对jobitem表进行"简单"查询
     */
    @PostMapping("/getSystemJobitemEvaluateList")
    public ResponseDto<PageDto<RdmsJobItemDto>> getSystemJobitemEvaluateList(@RequestBody PageDto<RdmsJobItemDto> pageDto) {
        TenantAccess.bindSelf(pageDto);
        ResponseDto<PageDto<RdmsJobItemDto>> responseDto = new ResponseDto<>();
        rdmsJobItemService.getSystemJobitemEvaluateList(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

    /**
     * 对jobitem表进行"简单"查询
     */
    @PostMapping("/getDevelopJobitemEvaluateList")
    public ResponseDto<PageDto<RdmsJobItemDto>> getDevelopJobitemEvaluateList(@RequestBody PageDto<RdmsJobItemDto> pageDto) {
        TenantAccess.bindSelf(pageDto);
        ResponseDto<PageDto<RdmsJobItemDto>> responseDto = new ResponseDto<>();
        rdmsJobItemService.getDevelopJobitemEvaluateList(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

    /**
     * 对jobitem表进行"简单"查询
     */
    @PostMapping("/getDevelopJobitemSubmitList")
    public ResponseDto<PageDto<RdmsJobItemDto>> getDevelopJobitemSubmitList(@RequestBody PageDto<RdmsJobItemDto> pageDto) {
        TenantAccess.bindSelf(pageDto);
        ResponseDto<PageDto<RdmsJobItemDto>> responseDto = new ResponseDto<>();
        rdmsJobItemService.getDevelopJobitemSubmitList(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

    /**
     * 对jobitem表进行"简单"查询
     */
    @PostMapping("/getDevelopApprovedJobItemList")
    public ResponseDto<PageDto<RdmsJobItemDto>> getDevelopApprovedJobItemList(@RequestBody PageDto<RdmsJobItemDto> pageDto) {
        TenantAccess.bindSelf(pageDto);
        ResponseDto<PageDto<RdmsJobItemDto>> responseDto = new ResponseDto<>();
        rdmsJobItemService.getDevelopApprovedJobItemList(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

    /**
     * 对jobitem表进行"简单"查询
     */
    @PostMapping("/getJobitemAndAllAssistJobitems/{jobitemId}")
    public ResponseDto<List<RdmsJobItemDto>> getJobitemAndAllAssistJobitems(@PathVariable String jobitemId) {
        ResponseDto<List<RdmsJobItemDto>> responseDto = new ResponseDto<>();
        RdmsHmiJobItemAndChildrenItemsDto jobitemAndAllAssistJobitems = rdmsJobItemService.getJobitemAndAllAssistJobitems(jobitemId);
        List<RdmsJobItemDto> hmiJobitemDtoList = new ArrayList<>();
        if(!ObjectUtils.isEmpty(jobitemAndAllAssistJobitems) && ! ObjectUtils.isEmpty(jobitemAndAllAssistJobitems.getParentJobitem())){
            RdmsJobItemDto parentJobitemDto = jobitemAndAllAssistJobitems.getParentJobitem();
            parentJobitemDto.setParentOrChildren(ParentOrChildrenEnum.PARENT.getKey());
            hmiJobitemDtoList.add(parentJobitemDto);
        }

        if(!ObjectUtils.isEmpty(jobitemAndAllAssistJobitems) && ! ObjectUtils.isEmpty(jobitemAndAllAssistJobitems.getChildJobitemList())){
            for(RdmsJobItemDto assistJobitem: jobitemAndAllAssistJobitems.getChildJobitemList()){
                assistJobitem.setParentOrChildren(ParentOrChildrenEnum.CHILDREN.getKey());
                assistJobitem.setShowFlag("children");
                hmiJobitemDtoList.add(assistJobitem);
            }
        }
        responseDto.setContent(hmiJobitemDtoList);
        return responseDto;
    }

     /**
     * 对jobitem表进行"简单"查询
     */
    @PostMapping("/getReviewRelatedJobitems_handling/{jobitemId}")
    public ResponseDto<RdmsHmiJobItemAndChildrenItemsDto> getReviewRelatedJobitems_handling(@PathVariable String jobitemId) {
        ResponseDto<RdmsHmiJobItemAndChildrenItemsDto> responseDto = new ResponseDto<>();
        List<ReviewResultEnum> reviewResultEnumList = new ArrayList<>();
        reviewResultEnumList.add(ReviewResultEnum.CONDITIONAL);
        reviewResultEnumList.add(ReviewResultEnum.SUPPLEMENT);
        reviewResultEnumList.add(ReviewResultEnum.REJECT);
        reviewResultEnumList.add(ReviewResultEnum.PASS);
        RdmsHmiJobItemAndChildrenItemsDto reviewJobitemAndRelatedJobitems = rdmsJobItemService.getReviewJobitemAndRelatedJobitems(jobitemId, JobItemStatusEnum.HANDLING, reviewResultEnumList);

        responseDto.setContent(reviewJobitemAndRelatedJobitems);
        return responseDto;
    }

    /**
     * 对jobitem表进行"简单"查询
     */
    @PostMapping("/getReviewRelatedJobitems_submit/{jobitemId}")
    public ResponseDto<List<RdmsJobItemDto>> getReviewRelatedJobitems_submit(@PathVariable String jobitemId) {
        ResponseDto<List<RdmsJobItemDto>> responseDto = new ResponseDto<>();
        List<ReviewResultEnum> reviewResultEnumList = new ArrayList<>();
        reviewResultEnumList.add(ReviewResultEnum.CONDITIONAL);
        reviewResultEnumList.add(ReviewResultEnum.SUPPLEMENT);
        RdmsHmiJobItemAndChildrenItemsDto reviewJobitemAndRelatedJobitems = rdmsJobItemService.getReviewJobitemAndRelatedJobitems(jobitemId, JobItemStatusEnum.SUBMIT, reviewResultEnumList);

        List<RdmsJobItemDto> hmiJobitemDtoList = new ArrayList<>();
        if(!ObjectUtils.isEmpty(reviewJobitemAndRelatedJobitems) && ! ObjectUtils.isEmpty(reviewJobitemAndRelatedJobitems.getParentJobitem())){
            RdmsJobItemDto parentJobitemDto = reviewJobitemAndRelatedJobitems.getParentJobitem();
            parentJobitemDto.setParentOrChildren(ParentOrChildrenEnum.PARENT.getKey());
            hmiJobitemDtoList.add(parentJobitemDto);
        }

        if(!ObjectUtils.isEmpty(reviewJobitemAndRelatedJobitems) && ! ObjectUtils.isEmpty(reviewJobitemAndRelatedJobitems.getChildJobitemList())){
            for(RdmsJobItemDto childJobitem: reviewJobitemAndRelatedJobitems.getChildJobitemList()){
                childJobitem.setParentOrChildren(ParentOrChildrenEnum.CHILDREN.getKey());
                childJobitem.setShowFlag("children");
                hmiJobitemDtoList.add(childJobitem);
            }
        }
        responseDto.setContent(hmiJobitemDtoList);
        return responseDto;
    }

    @PostMapping("/getReviewRelatedSubmitJobNum/{reviewJobitemId}")
    public ResponseDto<Integer> getReviewRelatedSubmitJobNumByReviewJobitemId(@PathVariable String reviewJobitemId) {
        ResponseDto<Integer> responseDto = new ResponseDto<>();
        List<ReviewResultEnum> reviewResultEnumList = new ArrayList<>();
        reviewResultEnumList.add(ReviewResultEnum.CONDITIONAL);
        reviewResultEnumList.add(ReviewResultEnum.SUPPLEMENT);
        List<RdmsJobItemDto> reviewJobitemRelatedSubmitJobitems = rdmsJobItemService.getReviewJobitemRelatedSubmitJobitems(reviewJobitemId, reviewResultEnumList);

        responseDto.setContent(reviewJobitemRelatedSubmitJobitems.size());
        return responseDto;
    }

    @PostMapping("/getReviewRelatedSubmitJobNumByUserId/{userId}")
    public ResponseDto<Integer> getReviewRelatedSubmitJobNumByUserId(@PathVariable String userId) {
        userId = TenantAccess.resolveMemberId(userId);
        ResponseDto<Integer> responseDto = new ResponseDto<>();
        List<ReviewResultEnum> reviewResultEnumList = new ArrayList<>();
        reviewResultEnumList.add(ReviewResultEnum.CONDITIONAL);
        reviewResultEnumList.add(ReviewResultEnum.SUPPLEMENT);
        responseDto.setContent(
                rdmsJobItemService.countReviewJobitemRelatedSubmitJobitemsByNextNode(userId, reviewResultEnumList));
        return responseDto;
    }

    /**
     * 对jobitem表进行"简单"查询
     */
    @PostMapping("/getJobitemAndAllAssistJobitems_submit/{jobitemId}")
    public ResponseDto<List<RdmsJobItemDto>> getJobitemAndAllAssistJobitems_submit(@PathVariable String jobitemId) {
        ResponseDto<List<RdmsJobItemDto>> responseDto = new ResponseDto<>();
        RdmsHmiJobItemAndChildrenItemsDto jobitemAndAllAssistJobitems = rdmsJobItemService.getJobitemAndAllAssistJobitems_submit(jobitemId);
        List<RdmsJobItemDto> hmiJobitemDtoList = new ArrayList<>();
        if(!ObjectUtils.isEmpty(jobitemAndAllAssistJobitems) && ! ObjectUtils.isEmpty(jobitemAndAllAssistJobitems.getParentJobitem())){
            RdmsJobItemDto parentJobitemDto = jobitemAndAllAssistJobitems.getParentJobitem();
            parentJobitemDto.setParentOrChildren(ParentOrChildrenEnum.PARENT.getKey());
            hmiJobitemDtoList.add(parentJobitemDto);
        }

        if(!ObjectUtils.isEmpty(jobitemAndAllAssistJobitems) && ! ObjectUtils.isEmpty(jobitemAndAllAssistJobitems.getChildJobitemList())){
            for(RdmsJobItemDto assistJobitem: jobitemAndAllAssistJobitems.getChildJobitemList()){
                assistJobitem.setParentOrChildren(ParentOrChildrenEnum.CHILDREN.getKey());
                assistJobitem.setShowFlag("children");
                hmiJobitemDtoList.add(assistJobitem);
            }
        }
        responseDto.setContent(hmiJobitemDtoList);
        return responseDto;
    }

    /**
     * 对jobitem表进行"简单"查询
     */
    @PostMapping("/getJobitemAndAllEvaluateJobitems/{jobitemId}")
    public ResponseDto<List<RdmsJobItemDto>> getJobitemAndAllEvaluateJobitems(@PathVariable String jobitemId) {
        ResponseDto<List<RdmsJobItemDto>> responseDto = new ResponseDto<>();
        RdmsHmiJobItemAndChildrenItemsDto jobItemAndChildrenItemsDto = rdmsJobItemService.getJobitemAndAllEvaluateJobitems(jobitemId);
        List<RdmsJobItemDto> hmiJobitemDtoList = new ArrayList<>();
        if(!ObjectUtils.isEmpty(jobItemAndChildrenItemsDto) && ! ObjectUtils.isEmpty(jobItemAndChildrenItemsDto.getParentJobitem())){
            RdmsJobItemDto parentJobitemDto = jobItemAndChildrenItemsDto.getParentJobitem();
            parentJobitemDto.setParentOrChildren(ParentOrChildrenEnum.PARENT.getKey());
            hmiJobitemDtoList.add(parentJobitemDto);
        }

        if(!ObjectUtils.isEmpty(jobItemAndChildrenItemsDto) && ! ObjectUtils.isEmpty(jobItemAndChildrenItemsDto.getChildJobitemList())){
            for(RdmsJobItemDto evaluateJobitem: jobItemAndChildrenItemsDto.getChildJobitemList()){
                evaluateJobitem.setParentOrChildren(ParentOrChildrenEnum.CHILDREN.getKey());
                evaluateJobitem.setShowFlag("children");
                hmiJobitemDtoList.add(evaluateJobitem);
            }
        }
        responseDto.setContent(hmiJobitemDtoList);
        return responseDto;
    }

    @PostMapping("/get-examine-items-num")
    public ResponseDto<Long> getExamineItemsNum(@RequestBody RdmsRequestDto requestDto) {
        requestDto.setCustomerUserId(TenantAccess.resolveMemberId(requestDto.getCustomerUserId()));
        ResponseDto<Long> responseDto = new ResponseDto<>();
        long examineItemsNum = rdmsJobItemService.getExamineNumByPreProjectId(requestDto.getCustomerId(),
                requestDto.getPreProjectId(), RdmsJobItemService.PRE_PROJECT_EXAMINE_TYPES);
        responseDto.setContent(examineItemsNum);

        return responseDto;
    }

    @PostMapping("/get-system-examine-items-num")
    public ResponseDto<Long> getSystemExamineItemsNum(@RequestBody RdmsRequestDto requestDto) {
        requestDto.setCustomerUserId(TenantAccess.resolveMemberId(requestDto.getCustomerUserId()));
        ResponseDto<Long> responseDto = new ResponseDto<>();
        long examineItemsNum = rdmsJobItemService.getSystemExamineItemsNum(requestDto.getCustomerId(), requestDto.getProjectId(), requestDto.getCustomerUserId());
        responseDto.setContent(examineItemsNum);
        return responseDto;
    }

    @PostMapping("/get-evaluate-items-num")
    public ResponseDto<Long> getEvaluateItemsNum(@RequestBody RdmsRequestDto requestDto) {
        requestDto.setCustomerUserId(TenantAccess.resolveMemberId(requestDto.getCustomerUserId()));
        ResponseDto<Long> responseDto = new ResponseDto<>();
        long characterItemsNum = rdmsJobItemService.getEvaluateItemsNum(requestDto.getCustomerId(), requestDto.getPreProjectId(), requestDto.getCustomerUserId());
        responseDto.setContent(characterItemsNum);
        return responseDto;
    }

    @PostMapping("/getRefuseNum")
    public ResponseDto<Long> getRefuseNum(@RequestBody RdmsRequestDto requestDto) {
        requestDto.setCustomerUserId(TenantAccess.resolveMemberId(requestDto.getCustomerUserId()));
        ResponseDto<Long> responseDto = new ResponseDto<>();
        long characterItemsNum = rdmsJobItemService.getRefuseNum(requestDto.getCustomerId(), requestDto.getPreProjectId(), requestDto.getCustomerUserId());
        responseDto.setContent(characterItemsNum);
        return responseDto;
    }

    @PostMapping("/getEvaluateNumBySubprojectId")
    public ResponseDto<Long> getEvaluateNumBySubprojectId(@RequestBody RdmsRequestDto requestDto) {
        ResponseDto<Long> responseDto = new ResponseDto<>();
        long evaluateItemsNum = rdmsJobItemService.getEvaluateNumBySubprojectId(requestDto.getCustomerId(), requestDto.getSubprojectId(), requestDto.getStatus(), requestDto.getType());
        responseDto.setContent(evaluateItemsNum);
        return responseDto;
    }

    @PostMapping("/getHandlingByPreProjectId")
    public ResponseDto<Long> getHandlingByPreProjectId(@RequestBody RdmsRequestDto requestDto) {
        ResponseDto<Long> responseDto = new ResponseDto<>();
        List<JobItemTypeEnum> jobitemTypeList = new ArrayList<>();
        long evaluateItemsNum = 0L;
        if(requestDto.getStatus().equals(JobItemStatusEnum.HANDLING.getStatus())){
            jobitemTypeList.add(JobItemTypeEnum.DEMAND);
            evaluateItemsNum = rdmsJobItemService.getHandlingByPreProjectId(requestDto.getCustomerId(), requestDto.getPreProjectId(), jobitemTypeList);
            responseDto.setContent(evaluateItemsNum);
        }
        responseDto.setContent(evaluateItemsNum);
        return responseDto;
    }

    @PostMapping("/getExamineNumByPreProjectId")
    public ResponseDto<Long> getExamineNumByPreProjectId(@RequestBody RdmsRequestDto requestDto) {
        requestDto.setUserId(TenantAccess.resolveMemberId(requestDto.getUserId()));
        ResponseDto<Long> responseDto = new ResponseDto<>();
        long examineItemsNum = 0L;
        if (JobItemStatusEnum.SUBMIT.getStatus().equals(requestDto.getStatus())) {
            examineItemsNum = rdmsJobItemService.getExamineNumByPreProjectId(requestDto.getCustomerId(),
                    requestDto.getPreProjectId(), RdmsJobItemService.PRE_PROJECT_EXAMINE_TYPES, requestDto.getUserId());
        }
        responseDto.setContent(examineItemsNum);
        return responseDto;
    }

    @PostMapping("/getEvaluateNumByPreProjectId")
    public ResponseDto<Long> getEvaluateNumByPreProjectId(@RequestBody RdmsRequestDto requestDto) {
        requestDto.setUserId(TenantAccess.resolveMemberId(requestDto.getUserId()));
        ResponseDto<Long> responseDto = new ResponseDto<>();
        List<JobItemTypeEnum> jobitemTypeList = new ArrayList<>();
        long evaluateItemsNum = 0L;
        if(requestDto.getStatus().equals(JobItemStatusEnum.EVALUATE.getStatus())){
            jobitemTypeList.add(JobItemTypeEnum.DEMAND);
            jobitemTypeList.add(JobItemTypeEnum.ITERATION);
            jobitemTypeList.add(JobItemTypeEnum.DECOMPOSE);
            jobitemTypeList.add(JobItemTypeEnum.MERGE);
            evaluateItemsNum = rdmsJobItemService.getEvaluateNumByPreProjectId(requestDto.getCustomerId(), requestDto.getPreProjectId(), requestDto.getUserId(), requestDto.getStatus(), jobitemTypeList);
            responseDto.setContent(evaluateItemsNum);
        }
        responseDto.setContent(evaluateItemsNum);
        return responseDto;
    }

    @PostMapping("/getTestManageJobNumByUserId/{userId}")
    public ResponseDto<Long> getTestManageJobNumByUserId(@PathVariable String userId) {
        userId = TenantAccess.resolveMemberId(userId);
        ResponseDto<Long> responseDto = new ResponseDto<>();
        long testManageNum = rdmsJobItemService.getTestManageJobNumByUserId(userId);
        responseDto.setContent(testManageNum);
        return responseDto;
    }

    @PostMapping("/getTestJobNumByUserId/{userId}")
    public ResponseDto<Long> getTestJobNumByUserId(@PathVariable String userId) {
        userId = TenantAccess.resolveMemberId(userId);
        ResponseDto<Long> responseDto = new ResponseDto<>();
        long testManageNum = rdmsJobItemService.getTestJobNumByUserId(userId);
        responseDto.setContent(testManageNum);
        return responseDto;
    }

    @PostMapping("/getFunctionManageJobNumByUserId/{userId}")
    public ResponseDto<Long> getFunctionManageJobNumByUserId(@PathVariable String userId) {
        userId = TenantAccess.resolveMemberId(userId);
        ResponseDto<Long> responseDto = new ResponseDto<>();
        long testManageNum = rdmsJobItemService.getFunctionManageJobNumByUserId(userId);
        responseDto.setContent(testManageNum);
        return responseDto;
    }

    @PostMapping("/get-system-evaluate-items-num")
    public ResponseDto<Long> getSystemEvaluateItemsNum(@RequestBody RdmsRequestDto requestDto) {
        requestDto.setCustomerUserId(TenantAccess.resolveMemberId(requestDto.getCustomerUserId()));
        ResponseDto<Long> responseDto = new ResponseDto<>();
        long characterItemsNum = rdmsJobItemService.getSystemEvaluateItemsNum(requestDto.getCustomerId(), requestDto.getProjectId(), requestDto.getCustomerUserId());
        responseDto.setContent(characterItemsNum);
        return responseDto;
    }

    @Transactional
    @PostMapping("/getJobItemCreateDlgInfo/{characterId}")
    public ResponseDto<RdmsHmiJobItemCreateDto> getJobItemCreateDlgInfo(@PathVariable String characterId) {
        ResponseDto<RdmsHmiJobItemCreateDto> responseDto = new ResponseDto<>();
        RdmsHmiJobItemCreateDto jobItemCreateDto = new RdmsHmiJobItemCreateDto();

        RdmsCharacter character = rdmsCharacterService.selectByPrimaryKey(characterId);
        jobItemCreateDto.setCharacterId(character.getId());
        jobItemCreateDto.setCharacterName(character.getCharacterName());
        jobItemCreateDto.setCreateCharacterJobitemId(character.getJobitemId());
        jobItemCreateDto.setPlanCompleteTime(character.getPlanCompleteTime());

        RdmsJobItem jobItem = rdmsJobItemService.selectByPrimaryKey(character.getJobitemId());
        if(! ObjectUtils.isEmpty(jobItem) && ! ObjectUtils.isEmpty(jobItem.getPreProjectId())){  //PreProject阶段是现有工单,后有Character
            jobItemCreateDto.setPreProjectId(jobItem.getPreProjectId());
        }
        if(! ObjectUtils.isEmpty(character.getProjectId())){  //在Project阶段是根据Character信息创建工单
            jobItemCreateDto.setProjectId(character.getProjectId());
            RdmsProject rdmsProject = rdmsProjectService.selectByPrimaryKey(character.getProjectId());
            if(!ObjectUtils.isEmpty(rdmsProject)){
                jobItemCreateDto.setRdType(rdmsProject.getRdType());
            }
        }
        if(! ObjectUtils.isEmpty(character.getSubprojectId())){
            jobItemCreateDto.setSubprojectId(character.getSubprojectId());
        }
        if(!ObjectUtils.isEmpty(character.getBudget())){
            RdmsBudgetSummaryDto budgetDto = new RdmsBudgetSummaryDto();
            BigDecimal approvedDevManhourFee = rdmsManhourService.getApprovedDevManhourFee(characterId);
            BigDecimal remainDevManhourFee = rdmsManhourService.getCharacterRemainDevManhourFee(characterId);
            budgetDto.setDevManhourFeeApproved(approvedDevManhourFee);
            budgetDto.setDevManhourFeeRemain(remainDevManhourFee);
            budgetDto.setBudget(character.getBudget());

            BigDecimal characterRemainManhourFee = rdmsManhourService.getCharacterRemainBudgetFee(characterId);
            budgetDto.setRemainBudget(characterRemainManhourFee);
//            jobItemCreateDto.setCharacterBudget(budgetDto);

            RdmsManhourStandard standardManhour = rdmsManhourService.getStandardManhourByCustomerId(jobItem.getCustomerId());
            if(!ObjectUtils.isEmpty(standardManhour)){
                budgetDto.setStandDevManhourFee(standardManhour.getDevManhourFee());
                budgetDto.setStandTestManhourFee(standardManhour.getTestManhourFee());
            }
            jobItemCreateDto.setBudgetExeInfo(budgetDto);
        }

        List<RdmsCustomerUser> customerUserList = rdmsCustomerUserService.listAllNormalWorkers(character.getCustomerId());
        //重要代码
        List<RdmsHmiExecutorPlainDto> executorPlainDtos = customerUserList.stream().map(e -> new RdmsHmiExecutorPlainDto(e.getId(), e.getTrueName())).collect(Collectors.toList());
        jobItemCreateDto.setExecutors(executorPlainDtos);

        RdmsCustomerUser customerUser = rdmsCustomerUserService.selectByPrimaryKey(character.getProductManagerId());
        jobItemCreateDto.setProductManagerId(customerUser.getId());
        jobItemCreateDto.setProductManagerName(customerUser.getTrueName());

        RdmsProject rdmsProject = rdmsProjectService.selectByPrimaryKey(character.getProjectId());
        if(!ObjectUtils.isEmpty(rdmsProject) && !ObjectUtils.isEmpty(rdmsProject.getTestManagerId())){
            RdmsCustomerUser customerUser1 = rdmsCustomerUserService.selectByPrimaryKey(rdmsProject.getTestManagerId());
            jobItemCreateDto.setTestManagerId(rdmsProject.getTestManagerId());
            jobItemCreateDto.setTestManagerName(customerUser1.getTrueName());
        }

        responseDto.setContent(jobItemCreateDto);
        return responseDto;
    }

    @Transactional
    @PostMapping("/getJobItemCreateDlgInfoByQualityId/{qualityId}")
    public ResponseDto<RdmsHmiJobItemCreateDto> getJobItemCreateDlgInfoByQualityId(@PathVariable String qualityId) {
        ResponseDto<RdmsHmiJobItemCreateDto> responseDto = new ResponseDto<>();
        RdmsHmiJobItemCreateDto jobItemCreateDto = new RdmsHmiJobItemCreateDto();

        RdmsQuality rdmsQuality = rdmsQualityService.selectByPrimaryKey(qualityId);
        jobItemCreateDto.setQualityId(rdmsQuality.getId());
        jobItemCreateDto.setQualityName(rdmsQuality.getName());

        jobItemCreateDto.setProjectId(rdmsQuality.getProjectId());
        jobItemCreateDto.setSubprojectId(rdmsQuality.getSubprojectId());

        List<RdmsCustomerUser> customerUserList = rdmsCustomerUserService.listAllNormalWorkers(rdmsQuality.getCustomerId());
        List<RdmsHmiExecutorPlainDto> executorPlainDtos = customerUserList.stream().map(e -> new RdmsHmiExecutorPlainDto(e.getId(), e.getTrueName())).collect(Collectors.toList());
        jobItemCreateDto.setExecutors(executorPlainDtos);

        RdmsCustomerUser customerUser = rdmsCustomerUserService.selectByPrimaryKey(rdmsQuality.getProductManagerId());
        jobItemCreateDto.setProductManagerId(customerUser.getId());
        jobItemCreateDto.setProductManagerName(customerUser.getTrueName());

        responseDto.setContent(jobItemCreateDto);
        return responseDto;
    }

    @Transactional
    @PostMapping("/getJobItemCreateDlgInfoByBugfeedbackId/{bugfeedbackId}")
    public ResponseDto<RdmsHmiJobItemCreateDto> getJobItemCreateDlgInfoByBugfeedbackId(@PathVariable String bugfeedbackId) {
        ResponseDto<RdmsHmiJobItemCreateDto> responseDto = new ResponseDto<>();
        RdmsHmiJobItemCreateDto jobItemCreateDto = new RdmsHmiJobItemCreateDto();

        RdmsBugFeedback rdmsBugFeedback = rdmsBugFeedbackService.selectByPrimaryKey(bugfeedbackId);
        jobItemCreateDto.setBugfeedbackId(rdmsBugFeedback.getId());
        jobItemCreateDto.setBugfeedbackName(rdmsBugFeedback.getName());

        jobItemCreateDto.setProjectId(rdmsBugFeedback.getProjectId());
        jobItemCreateDto.setSubprojectId(rdmsBugFeedback.getSubprojectId());

        List<RdmsCustomerUser> customerUserList = rdmsCustomerUserService.listAllNormalWorkers(rdmsBugFeedback.getCustomerId());
        List<RdmsHmiExecutorPlainDto> executorPlainDtos = customerUserList.stream().map(e -> new RdmsHmiExecutorPlainDto(e.getId(), e.getTrueName())).collect(Collectors.toList());
        jobItemCreateDto.setExecutors(executorPlainDtos);

        RdmsCustomerUser customerUser = rdmsCustomerUserService.selectByPrimaryKey(rdmsBugFeedback.getTestManagerId());
        jobItemCreateDto.setTestManagerId(customerUser.getId());
        jobItemCreateDto.setTestManagerName(customerUser.getTrueName());

        responseDto.setContent(jobItemCreateDto);
        return responseDto;
    }

    @PostMapping("/getJobitemByCharacterId/{characterId}")
    public ResponseDto<RdmsJobItemDto> getJobitemByCharacterId(@PathVariable String characterId) {
        ResponseDto<RdmsJobItemDto> responseDto = new ResponseDto<>();
        RdmsJobItemDto jobitemByCharacterId = rdmsJobItemService.getOriginJobitemByCharacterId(characterId);
        responseDto.setContent(jobitemByCharacterId);
        return responseDto;
    }

    @PostMapping("/getJobitemListByPjm/{customerUserId}")
    public ResponseDto<List<RdmsJobItemDto>> getJobitemListByPjm(@PathVariable String customerUserId) {
        customerUserId = TenantAccess.resolveMemberId(customerUserId);
        ResponseDto<List<RdmsJobItemDto>> responseDto = new ResponseDto<>();
        List<RdmsJobItemDto> jobitemListByPjm = rdmsJobItemService.getJobitemListByPjm(customerUserId);
        responseDto.setContent(jobitemListByPjm);
        return responseDto;
    }

    @PostMapping("/listJobitemListByPjm")
    public ResponseDto<PageDto<RdmsJobItemDto>> listJobitemListByPjm(@RequestBody PageDto<RdmsJobItemDto> pageDto) {
        TenantAccess.bindSelf(pageDto);
        ResponseDto<PageDto<RdmsJobItemDto>> responseDto = new ResponseDto<>();
        rdmsJobItemService.getJobitemListByPjm(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

    @PostMapping("/getJobitemListByCreater")
    public ResponseDto<PageDto<RdmsJobItemDto>> getJobitemListByCreater(@RequestBody PageDto<RdmsJobItemDto> pageDto) {
        TenantAccess.bindSelf(pageDto);
        ResponseDto<PageDto<RdmsJobItemDto>> responseDto = new ResponseDto<>();
        rdmsJobItemService.getJobitemListByCreater(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

    @PostMapping("/listJobsByPdm")
    public ResponseDto<PageDto<RdmsJobItemDto>> listJobsByPdm(@RequestBody PageDto<RdmsJobItemDto> pageDto) {
        TenantAccess.bindSelf(pageDto);
        ResponseDto<PageDto<RdmsJobItemDto>> responseDto = new ResponseDto<>();
        rdmsJobItemService.listJobsByPdm(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

    @PostMapping("/getJobitemHasListFlag/{userId}")
    public ResponseDto<Boolean> getJobitemHasListFlag(@PathVariable String userId) {
        userId = TenantAccess.resolveMemberId(userId);
        ResponseDto<Boolean> responseDto = new ResponseDto<>();
        Boolean jobitemHasListFlag = rdmsJobItemService.getJobitemHasListFlag(userId);
        responseDto.setContent(jobitemHasListFlag);
        return responseDto;
    }

    @PostMapping("/listJobitemListByPjm_complete")
    public ResponseDto<PageDto<RdmsJobItemDto>> listJobitemListByPjm_complete(@RequestBody PageDto<RdmsJobItemDto> pageDto) {
        TenantAccess.bindSelf(pageDto);
        ResponseDto<PageDto<RdmsJobItemDto>> responseDto = new ResponseDto<>();
        rdmsJobItemService.getJobitemListByCreater(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

    @PostMapping("/listJobsByPdm_complete")
    public ResponseDto<PageDto<RdmsJobItemDto>> listJobsByPdm_complete(@RequestBody PageDto<RdmsJobItemDto> pageDto) {
        TenantAccess.bindSelf(pageDto);
        ResponseDto<PageDto<RdmsJobItemDto>> responseDto = new ResponseDto<>();
        rdmsJobItemService.listJobsByPdm(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

    @Transactional
    @PostMapping("/getCompletedJobitemList/{characterId}")
    public ResponseDto<RdmsHmiCompleteStatusInfoDto> getCompletedJobitemList(@PathVariable String characterId) {
        ResponseDto<RdmsHmiCompleteStatusInfoDto> responseDto = new ResponseDto<>();
        RdmsHmiCompleteStatusInfoDto completeStatusInfoDto = new RdmsHmiCompleteStatusInfoDto();
        {
            {
                //已经完成的工单和工单数
                List<JobItemTypeEnum> jobitemTypeList = new ArrayList<>();
                jobitemTypeList.add(JobItemTypeEnum.DEVELOP);
                jobitemTypeList.add(JobItemTypeEnum.CHARACTER_INT);
                jobitemTypeList.add(JobItemTypeEnum.TASK);

                List<RdmsJobItemDto> completeJobList = rdmsJobItemService.getJobitemListByCharacterIdAndStatusTypeList(characterId, JobItemStatusEnum.COMPLETED.getStatus(), jobitemTypeList);
                completeStatusInfoDto.setCompleteJobitemList(completeJobList);
                completeStatusInfoDto.setCompleteJobNum(completeJobList.size());

                List<RdmsJobItemDto> sumJobList = rdmsJobItemService.getJobitemListByCharacterIdAndStatusTypeList(characterId, null, jobitemTypeList);
                sumJobList.removeIf(job -> JobItemStatusEnum.CANCEL.getStatus().equals(job.getStatus()));
                completeStatusInfoDto.setSumJobNum(sumJobList.size());
            }
            {
                //是否所有集成工单都完成了
                List<JobItemTypeEnum> jobitemTypeList = new ArrayList<>();
                jobitemTypeList.add(JobItemTypeEnum.CHARACTER_INT);

                List<RdmsJobItemDto> intJobList = rdmsJobItemService.getJobitemListByCharacterIdAndStatusTypeList(characterId, null, jobitemTypeList);
                intJobList.removeIf(job -> JobItemStatusEnum.CANCEL.getStatus().equals(job.getStatus()));
                if(!CollectionUtils.isEmpty(intJobList)){
                    boolean completeFlag = true;
                    for(RdmsJobItemDto jobItemDto: intJobList){
                        if(!jobItemDto.getStatus().equals(JobItemStatusEnum.COMPLETED.getStatus())){
                            completeFlag = false;
                            break;
                        }
                    }
                    completeStatusInfoDto.setCompleteFlag(completeFlag);
                }else{
                    completeStatusInfoDto.setCompleteFlag(false);
                }
            }
        }

        responseDto.setContent(completeStatusInfoDto);
        return responseDto;
    }

    @Transactional
    @PostMapping("/getCompletedJobitemList_testTask/{characterId}")
    public ResponseDto<RdmsHmiCompleteStatusInfoDto> getCompletedJobitemList_testTask(@PathVariable String characterId) {
        ResponseDto<RdmsHmiCompleteStatusInfoDto> responseDto = new ResponseDto<>();
        RdmsHmiCompleteStatusInfoDto completeStatusInfoDto = new RdmsHmiCompleteStatusInfoDto();
        {
            //已经完成的工单和工单数
            List<JobItemTypeEnum> jobitemTypeList = new ArrayList<>();
            jobitemTypeList.add(JobItemTypeEnum.TASK_TEST);

            List<RdmsJobItemDto> completeJobList = rdmsJobItemService.getJobitemListByCharacterIdAndStatusTypeList(characterId, JobItemStatusEnum.COMPLETED.getStatus(), jobitemTypeList);
            completeStatusInfoDto.setCompleteJobitemList(completeJobList);
            completeStatusInfoDto.setCompleteJobNum(completeJobList.size());

            List<RdmsJobItemDto> sumJobList = rdmsJobItemService.getJobitemListByCharacterIdAndStatusTypeList(characterId, null, jobitemTypeList);
            sumJobList.removeIf(job -> JobItemStatusEnum.CANCEL.getStatus().equals(job.getStatus()));
            completeStatusInfoDto.setSumJobNum(sumJobList.size());
        }

        responseDto.setContent(completeStatusInfoDto);
        return responseDto;
    }

    @Transactional
    @PostMapping("/getCompletedJobitemListByQualityId/{qualityId}")
    public ResponseDto<RdmsHmiCompleteStatusInfoDto> getCompletedJobitemListByQualityId(@PathVariable String qualityId) {
        ResponseDto<RdmsHmiCompleteStatusInfoDto> responseDto = new ResponseDto<>();
        RdmsHmiCompleteStatusInfoDto completeStatusInfoDto = new RdmsHmiCompleteStatusInfoDto();
        {
            {
                //已经完成的工单和工单数
                List<JobItemTypeEnum> jobitemTypeList = new ArrayList<>();
                jobitemTypeList.add(JobItemTypeEnum.QUALITY);

                List<RdmsJobItemDto> completeJobList = rdmsJobItemService.getJobitemListByQualityIdAndStatusTypeList(qualityId, JobItemStatusEnum.COMPLETED.getStatus(), jobitemTypeList);
                completeStatusInfoDto.setCompleteJobitemList(completeJobList);
                completeStatusInfoDto.setCompleteJobNum(completeJobList.size());

                List<RdmsJobItemDto> sumJobList = rdmsJobItemService.getJobitemListByQualityIdAndStatusTypeList(qualityId, null, jobitemTypeList);
                completeStatusInfoDto.setSumJobNum(sumJobList.size());
            }
        }

        responseDto.setContent(completeStatusInfoDto);
        return responseDto;
    }

    @Transactional
    @PostMapping("/getCompletedJobitemListByBugfeedbackId/{bugfeedbackId}")
    public ResponseDto<RdmsHmiCompleteStatusInfoDto> getCompletedJobitemListByBugfeedbackId(@PathVariable String bugfeedbackId) {
        ResponseDto<RdmsHmiCompleteStatusInfoDto> responseDto = new ResponseDto<>();
        RdmsHmiCompleteStatusInfoDto completeStatusInfoDto = new RdmsHmiCompleteStatusInfoDto();
        {
            {
                //已经完成的工单和工单数
                List<JobItemTypeEnum> jobitemTypeList = new ArrayList<>();
                jobitemTypeList.add(JobItemTypeEnum.TASK_BUG);

                List<RdmsJobItemDto> completeJobList = rdmsJobItemService.getJobitemListByQualityIdAndStatusTypeList(bugfeedbackId, JobItemStatusEnum.COMPLETED.getStatus(), jobitemTypeList);
                completeStatusInfoDto.setCompleteJobitemList(completeJobList);
                completeStatusInfoDto.setCompleteJobNum(completeJobList.size());

                List<RdmsJobItemDto> sumJobList = rdmsJobItemService.getJobitemListByQualityIdAndStatusTypeList(bugfeedbackId, null, jobitemTypeList);
                completeStatusInfoDto.setSumJobNum(sumJobList.size());
            }
        }

        responseDto.setContent(completeStatusInfoDto);
        return responseDto;
    }

    @Transactional
    @PostMapping("/getCompletedJobitemListBySubProjectId/{subprojectId}")
    public ResponseDto<RdmsHmiCompleteStatusInfoDto> getCompletedJobitemListBySubProjectId(@PathVariable String subprojectId) {
        ResponseDto<RdmsHmiCompleteStatusInfoDto> responseDto = new ResponseDto<>();
        RdmsHmiCompleteStatusInfoDto completeStatusInfoDto = new RdmsHmiCompleteStatusInfoDto();

        {
            //已经完成的工单和工单数
            List<JobItemTypeEnum> jobitemTypeList = new ArrayList<>();
            jobitemTypeList.add(JobItemTypeEnum.TASK_SUBP);
            jobitemTypeList.add(JobItemTypeEnum.SUBPROJECT_INT);

            List<RdmsJobItemDto> completeJobList = rdmsJobItemService.getJobitemListBySubProjectIdAndStatus(subprojectId, JobItemStatusEnum.COMPLETED.getStatus(), jobitemTypeList);
            completeStatusInfoDto.setCompleteJobitemList(completeJobList);
            completeStatusInfoDto.setCompleteJobNum(completeJobList.size());

            //读取所有的工单数 开发工单 集成工单 任务工单工单总数
            List<RdmsJobItemDto> sumJobList = rdmsJobItemService.getJobitemListBySubProjectIdAndStatus(subprojectId, null, jobitemTypeList);
            completeStatusInfoDto.setSumJobNum(sumJobList.size());
        }

        //处理里程碑任务
        RdmsProjectSubproject subproject = rdmsSubprojectService.selectByPrimaryKey(subprojectId);
        if(subproject.getId().equals(subproject.getProjectId())){
            //已经完成的工单和工单数
            List<JobItemTypeEnum> jobitemTypeList = new ArrayList<>();
            jobitemTypeList.add(JobItemTypeEnum.TASK_MILESTONE);

            List<RdmsJobItemDto> completeJobList = rdmsJobItemService.getJobitemListByProjectIdAndStatus(subproject.getProjectId(), JobItemStatusEnum.COMPLETED.getStatus(), jobitemTypeList);
            completeStatusInfoDto.getCompleteJobitemList().addAll(completeJobList);
            completeStatusInfoDto.setCompleteJobNum(completeStatusInfoDto.getCompleteJobNum() + completeJobList.size());

            //读取所有的工单数 开发工单 集成工单 任务工单工单总数
            List<RdmsJobItemDto> sumJobList = rdmsJobItemService.getJobitemListByProjectIdAndStatus(subproject.getProjectId(), null, jobitemTypeList);
            completeStatusInfoDto.setSumJobNum(completeStatusInfoDto.getSumJobNum() + sumJobList.size());
        }

        {
            List<CharacterStatusEnum> statusList = new ArrayList<>();
            statusList.add(CharacterStatusEnum.ARCHIVED);
            List<RdmsCharacterDto> completeCharacters = rdmsCharacterService.getCharacterListBySubprojectIdAndStatusList(subprojectId, statusList);
            completeStatusInfoDto.setCompleteCharacterNum(completeCharacters.size());
            List<RdmsCharacterDto> allCharacters = rdmsCharacterService.getCharacterListBySubprojectIdAndStatusList(subprojectId, null);
            completeStatusInfoDto.setSumCharacterNum(allCharacters.size());
        }
        {
            //是否所有集成工单都完成了
            List<JobItemTypeEnum> jobitemTypeList = new ArrayList<>();
            jobitemTypeList.add(JobItemTypeEnum.SUBPROJECT_INT);

            List<RdmsJobItemDto> intJobList = rdmsJobItemService.getJobitemListBySubProjectIdAndStatus(subprojectId, null, jobitemTypeList);
            if(!CollectionUtils.isEmpty(intJobList)){
                boolean completeFlag = true;
                for(RdmsJobItemDto jobItemDto: intJobList){
                    if(!jobItemDto.getStatus().equals(JobItemStatusEnum.COMPLETED.getStatus())){
                        completeFlag = false;
                        break;
                    }
                }
                completeStatusInfoDto.setCompleteFlag(completeFlag);
            }else{
                completeStatusInfoDto.setCompleteFlag(false);
            }
        }

        {
            // 统一走 ReviewStatusQuery（说明书 §2.5）
            completeStatusInfoDto.setIsReviewing(reviewStatusQuery.isSubprojectReviewing(subprojectId));
        }

        responseDto.setContent(completeStatusInfoDto);
        return responseDto;
    }

    @Transactional
    @PostMapping("/getCompletedJobitemListByWorkerId")
    public ResponseDto<List<RdmsJobItemDto>> getCompletedJobitemListByWorkerId(
            @RequestParam("month") Date month,
            @RequestParam("customerId") String customerId,
            @RequestParam("workerId") String workerId

    ) throws ParseException {
        ResponseDto<List<RdmsJobItemDto>> responseDto = new ResponseDto<>();
        //根据month的日期获得month所在月份的第一天和最后一天的日期
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(month);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        Date monthStart = calendar.getTime();
        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        Date monthEnd = calendar.getTime();

        List<JobItemStatusEnum> statusEnumList = new ArrayList<>();
        statusEnumList.add(JobItemStatusEnum.COMPLETED);
        statusEnumList.add(JobItemStatusEnum.ARCHIVED);
        List<RdmsJobItemDto> jobitemListByExecutorId = rdmsJobItemService.getJobitemListByExecutorIdByTimeInterval(workerId, statusEnumList, monthStart, monthEnd);
        if(!CollectionUtils.isEmpty(jobitemListByExecutorId)){
            for(RdmsJobItemDto jobItemDto: jobitemListByExecutorId){
                RdmsCustomerUser executor = rdmsCustomerUserService.selectByPrimaryKey(jobItemDto.getExecutorId());
                jobItemDto.setExecutorName(executor != null? executor.getTrueName() : "");
                if(!ObjectUtils.isEmpty(jobItemDto.getSubprojectId())){
                    RdmsProjectSubproject rdmsProjectSubproject = rdmsSubprojectService.selectByPrimaryKey(jobItemDto.getSubprojectId());
                    jobItemDto.setSubprojectName(rdmsProjectSubproject != null? rdmsProjectSubproject.getLabel() : "");
                }
                if(!ObjectUtils.isEmpty(jobItemDto.getPreProjectId())){
                    RdmsPreProject rdmsPreProject = rdmsPreProjectService.selectByPrimaryKey(jobItemDto.getPreProjectId());
                    jobItemDto.setSubprojectName(rdmsPreProject != null? rdmsPreProject.getPreProjectName() : "");
                }
            }
        }

        responseDto.setContent(jobitemListByExecutorId);
        return responseDto;
    }

    @Transactional
    @PostMapping("/getCompletedJobitemListByPreProjectId/{preprojectId}")
    public ResponseDto<RdmsHmiCompleteStatusInfoDto> getCompletedJobitemListByPreProjectId(@PathVariable String preprojectId) {
        ResponseDto<RdmsHmiCompleteStatusInfoDto> responseDto = new ResponseDto<>();
        RdmsHmiCompleteStatusInfoDto completeStatusInfoDto = new RdmsHmiCompleteStatusInfoDto();

        {
            //已经完成的工单和工单数
            List<JobItemTypeEnum> jobitemTypeList = new ArrayList<>();
            jobitemTypeList.add(JobItemTypeEnum.TASK_FUNCTION);

            List<RdmsJobItemDto> completeJobList = rdmsJobItemService.getJobitemListByPreProjectIdAndStatusAndTypeList(preprojectId, JobItemStatusEnum.COMPLETED.getStatus(), jobitemTypeList);
            completeStatusInfoDto.setCompleteJobitemList(completeJobList);
            completeStatusInfoDto.setCompleteJobNum(completeJobList.size());

            //读取所有的工单数
            List<RdmsJobItemDto> sumJobList = rdmsJobItemService.getJobitemListByPreProjectIdAndStatusAndTypeList(preprojectId, null, jobitemTypeList);
            completeStatusInfoDto.setSumJobNum(sumJobList.size());
        }
        responseDto.setContent(completeStatusInfoDto);
        return responseDto;
    }

    @Transactional
    @PostMapping("/getCompletedJobitemListByProjectId/{projectId}")
    public ResponseDto<RdmsHmiCompleteStatusInfoDto> getCompletedJobitemListByProjectId(@PathVariable String projectId) {
        ResponseDto<RdmsHmiCompleteStatusInfoDto> responseDto = new ResponseDto<>();
        RdmsHmiCompleteStatusInfoDto completeStatusInfoDto = new RdmsHmiCompleteStatusInfoDto();
        {
            //已经完成的工单和工单数
            List<JobItemTypeEnum> jobitemTypeList = new ArrayList<>();
            jobitemTypeList.add(JobItemTypeEnum.TASK_PRODUCT);

            List<RdmsJobItemDto> completeJobList = rdmsJobItemService.getJobitemListByProjectIdAndStatus(projectId, JobItemStatusEnum.COMPLETED.getStatus(), jobitemTypeList);
            completeStatusInfoDto.setCompleteJobitemList(completeJobList);
            completeStatusInfoDto.setCompleteJobNum(completeJobList.size());

            //读取所有的工单数 开发工单 集成工单 任务工单工单总数
            List<RdmsJobItemDto> sumJobList = rdmsJobItemService.getJobitemListByProjectIdAndStatus(projectId, null, jobitemTypeList);
            completeStatusInfoDto.setSumJobNum(sumJobList.size());
        }
        responseDto.setContent(completeStatusInfoDto);
        return responseDto;
    }

    /**
     * 对jobitem表进行"简单"查询
     */
    @PostMapping("/getIdsDtoByJobitemId/{jobitemId}")
    public ResponseDto<RdmsHmiIdsDto> getIdsDtoByJobitemId(@PathVariable String jobitemId) {
        ResponseDto<RdmsHmiIdsDto> responseDto = new ResponseDto<>();
        RdmsHmiIdsDto idsDto = rdmsJobItemService.getIdsDtoByJobitemId(jobitemId);
        responseDto.setContent(idsDto);
        return responseDto;
    }

    @Transactional
    @PostMapping("/getCompletedAssistJobitemList/{jobitemId}")
    public ResponseDto<RdmsHmiCompleteStatusInfoDto> getCompletedAssistJobitemList(@PathVariable String jobitemId) {
        ResponseDto<RdmsHmiCompleteStatusInfoDto> responseDto = new ResponseDto<>();
        tenantParentGuard.requireJobItem(jobitemId);
        List<RdmsJobItemDto> assistJobitemList = rdmsJobItemService.getJobitemListByJobitemIdAndStatus(jobitemId, JobItemStatusEnum.COMPLETED.getStatus(), JobItemTypeEnum.ASSIST.getType());
        RdmsHmiCompleteStatusInfoDto completeStatusInfoDto = new RdmsHmiCompleteStatusInfoDto();
        completeStatusInfoDto.setCompleteJobitemList(assistJobitemList);
        completeStatusInfoDto.setCompleteJobNum(assistJobitemList.size());
        long sumJobNumByParentJobitemId = rdmsJobItemService.getSumJobNumByParentJobitemId(jobitemId, JobItemTypeEnum.ASSIST.getType());
        completeStatusInfoDto.setSumJobNum((int)sumJobNumByParentJobitemId);
        responseDto.setContent(completeStatusInfoDto);
        return responseDto;
    }

    @Transactional
    @PostMapping("/getCompletedBugJobitemList/{jobitemId}")
    public ResponseDto<RdmsHmiCompleteStatusInfoDto> getCompletedBugJobitemList(@PathVariable String jobitemId) {
        ResponseDto<RdmsHmiCompleteStatusInfoDto> responseDto = new ResponseDto<>();
        tenantParentGuard.requireJobItem(jobitemId);
        List<RdmsJobItemDto> bugJobitemList = rdmsJobItemService.getJobitemListByJobitemIdAndStatus(jobitemId, JobItemStatusEnum.COMPLETED.getStatus(), JobItemTypeEnum.BUG.getType());
        RdmsHmiCompleteStatusInfoDto completeStatusInfoDto = new RdmsHmiCompleteStatusInfoDto();
        completeStatusInfoDto.setCompleteJobitemList(bugJobitemList);
        completeStatusInfoDto.setCompleteJobNum(bugJobitemList.size());
        long sumJobNumByParentJobitemId = rdmsJobItemService.getSumJobNumByParentJobitemId(jobitemId, JobItemTypeEnum.BUG.getType());
        completeStatusInfoDto.setSumJobNum((int)sumJobNumByParentJobitemId);
        responseDto.setContent(completeStatusInfoDto);
        return responseDto;
    }

    @Transactional
    @PostMapping("/getCharactersHandlingStatusOfJobitem")
    public ResponseDto<Boolean> getCharactersHandlingStatusOfJobitem(@RequestParam String characterId, String loginUserId) {
        ResponseDto<Boolean> responseDto = new ResponseDto<>();
        RdmsCharacter rdmsCharacter = rdmsCharacterService.selectByPrimaryKey(characterId);
        List<RdmsCharacterDto> characterList = rdmsCharacterService.getCharacterListByJobitemId(rdmsCharacter.getJobitemId(), loginUserId);
       Boolean isHandling = false;
       for(int i=0; i< characterList.size(); i++){
           if(! characterList.get(i).getStatus().equals(CharacterStatusEnum.SUBMIT.getStatus())){
               isHandling = true;
               break;
           }
       }
        responseDto.setContent(isHandling);
        return responseDto;
    }

    @Transactional
    @PostMapping("/getJobItemCreateDlgInfo-demand")
    public ResponseDto<RdmsHmiJobItemCreateDto> getJobItemCreateDlgInfo_demand(@RequestBody RdmsHmiInCreateJobitemByMulDemandDto createJobitemByMulDemandDto) {
        ResponseDto<RdmsHmiJobItemCreateDto> responseDto = new ResponseDto<>();
        RdmsHmiJobItemCreateDto jobItemCreateDto = new RdmsHmiJobItemCreateDto();

        List<RdmsDemand> demandList = rdmsDemandService.getDemandListByIdList(createJobitemByMulDemandDto.getDemandIdList());
        List<RdmsHmiDemandPlainDto> rdmsHmiDemandPlainDtos = demandList.stream().map(e -> new RdmsHmiDemandPlainDto(e.getId(), e.getDemandName())).collect(Collectors.toList());
        jobItemCreateDto.setDemandPlains(rdmsHmiDemandPlainDtos);

        RdmsPreProject rdmsPreProject = rdmsPreProjectService.selectByPrimaryKey(createJobitemByMulDemandDto.getPreProjectId());
        jobItemCreateDto.setPreProjectId(rdmsPreProject.getId());

        List<RdmsCustomerUser> customerUserList = rdmsCustomerUserService.listAllNormalWorkers(rdmsPreProject.getCustomerId());
        //重要代码
        List<RdmsHmiExecutorPlainDto> executorPlainDtos = customerUserList.stream().map(e -> new RdmsHmiExecutorPlainDto(e.getId(), e.getTrueName())).collect(Collectors.toList());
        jobItemCreateDto.setExecutors(executorPlainDtos);

        RdmsCustomerUser customerUser = rdmsCustomerUserService.selectByPrimaryKey(rdmsPreProject.getProductManagerId());
        jobItemCreateDto.setProductManagerId(customerUser.getId());
        jobItemCreateDto.setProductManagerName(customerUser.getTrueName());

        responseDto.setContent(jobItemCreateDto);
        return responseDto;
    }

    @Transactional
    @PostMapping("/getJobItemCreateDlgInfo-develop/{jobitemId}")
    public ResponseDto<RdmsHmiJobItemCreateDto> getJobItemCreateDlgInfo_develop (@PathVariable String jobitemId) {
        ResponseDto<RdmsHmiJobItemCreateDto> responseDto = new ResponseDto<>();
        RdmsJobItem jobItem = rdmsJobItemService.selectByPrimaryKey(jobitemId);
        RdmsHmiJobItemCreateDto jobItemCreateDto = new RdmsHmiJobItemCreateDto();
        jobItemCreateDto.setJobName(jobItem.getJobName());
        jobItemCreateDto.setTestJobitemId(jobItem.getId());  //被测试工单
        RdmsProject rdmsProject = rdmsProjectService.selectByPrimaryKey(jobItem.getProjectId());
        jobItemCreateDto.setProjectId(rdmsProject.getId());
        jobItemCreateDto.setSubprojectId(jobItem.getSubprojectId());
        jobItemCreateDto.setCharacterId(jobItem.getCharacterId());
        jobItemCreateDto.setRdType(rdmsProject.getRdType());

        List<RdmsCustomerUser> customerUserList = rdmsCustomerUserService.listAllNormalWorkers(jobItem.getCustomerId());
        //重要代码
        List<RdmsHmiExecutorPlainDto> executorPlainDtos = customerUserList.stream().map(e -> new RdmsHmiExecutorPlainDto(e.getId(), e.getTrueName())).collect(Collectors.toList());
        jobItemCreateDto.setExecutors(executorPlainDtos);

        if(! ObjectUtils.isEmpty(jobItem.getProductManagerId())){
            RdmsCustomerUser customerUser = rdmsCustomerUserService.selectByPrimaryKey(jobItem.getProductManagerId());
            jobItemCreateDto.setProductManagerId(customerUser.getId());
            jobItemCreateDto.setProductManagerName(customerUser.getTrueName());
        }
        if(!ObjectUtils.isEmpty(jobItem.getCharacterId())){
            RdmsBudgetSummaryDto budgetDto = new RdmsBudgetSummaryDto();
            BigDecimal approvedTestManhourFee = rdmsManhourService.getApprovedTestManhourFee(jobitemId);
            BigDecimal remainTestManhourFee = rdmsManhourService.getCharacterRemainTestManhourFeeByJobitemId(jobitemId);
            budgetDto.setTestManhourFeeApproved(approvedTestManhourFee);
            budgetDto.setTestManhourFeeRemain(remainTestManhourFee);
            RdmsCharacter rdmsCharacter = rdmsCharacterService.selectByPrimaryKey(jobItem.getCharacterId());
            budgetDto.setBudget(rdmsCharacter.getBudget());
            BigDecimal characterRemainManhourFee = rdmsManhourService.getCharacterRemainBudgetFee(jobItem.getCharacterId());
            budgetDto.setRemainBudget(characterRemainManhourFee);
//            jobItemCreateDto.setCharacterBudget(budgetDto);

            RdmsManhourStandard standardManhour = rdmsManhourService.getStandardManhourByCustomerId(jobItem.getCustomerId());
            if(!ObjectUtils.isEmpty(standardManhour)){
                budgetDto.setStandDevManhourFee(standardManhour.getDevManhourFee());
                budgetDto.setStandTestManhourFee(standardManhour.getTestManhourFee());
            }
            jobItemCreateDto.setBudgetExeInfo(budgetDto);
        }

        responseDto.setContent(jobItemCreateDto);
        return responseDto;
    }

    /**
     * 功能集成或者项目任务的协作任务
     * @param parentJobitemId
     * @return
     */
    @Transactional
    @PostMapping("/getJobItemCreateDlgInfo-assist/{parentJobitemId}")
    public ResponseDto<RdmsHmiJobItemCreateDto> getJobItemCreateDlgInfo_assist (@PathVariable String parentJobitemId) {
        ResponseDto<RdmsHmiJobItemCreateDto> responseDto = new ResponseDto<>();
        RdmsJobItem parentJobItem = rdmsJobItemService.selectByPrimaryKey(parentJobitemId);
        RdmsHmiJobItemCreateDto jobItemCreateDto = new RdmsHmiJobItemCreateDto();
        jobItemCreateDto.setJobName(parentJobItem.getJobName());
        jobItemCreateDto.setParentJobitemId(parentJobItem.getId());  //集成工单或者任务工单
        RdmsProject rdmsProject = rdmsProjectService.selectByPrimaryKey(parentJobItem.getProjectId());
        jobItemCreateDto.setProjectId(rdmsProject.getId());
        jobItemCreateDto.setSubprojectId(parentJobItem.getSubprojectId());
        jobItemCreateDto.setCharacterId(parentJobItem.getCharacterId());
        jobItemCreateDto.setRdType(rdmsProject.getRdType());

        List<RdmsCustomerUser> customerUserList = rdmsCustomerUserService.listAllNormalWorkers(parentJobItem.getCustomerId());
        //重要代码
        List<RdmsHmiExecutorPlainDto> executorPlainDtos = customerUserList.stream().map(e -> new RdmsHmiExecutorPlainDto(e.getId(), e.getTrueName())).collect(Collectors.toList());
        jobItemCreateDto.setExecutors(executorPlainDtos);

        if(! ObjectUtils.isEmpty(parentJobItem.getProductManagerId())){
            RdmsCustomerUser customerUser = rdmsCustomerUserService.selectByPrimaryKey(parentJobItem.getProductManagerId());
            jobItemCreateDto.setProductManagerId(customerUser.getId());
            jobItemCreateDto.setProductManagerName(customerUser.getTrueName());
        }
        if(!ObjectUtils.isEmpty(parentJobItem.getCharacterId())){
            RdmsBudgetSummaryDto budgetDto = new RdmsBudgetSummaryDto();
            RdmsBudgetSummaryDto jobitemBudgetExeInfo = rdmsManhourService.getFirstLevelJobitemBudgetExeInfo(parentJobitemId);
            budgetDto.setRemainBudget(jobitemBudgetExeInfo.getRemainBudget());
//            jobItemCreateDto.setCharacterBudget(budgetDto);

            RdmsManhourStandard standardManhour = rdmsManhourService.getStandardManhourByCustomerId(parentJobItem.getCustomerId());
            if(!ObjectUtils.isEmpty(standardManhour)){
                budgetDto.setStandDevManhourFee(standardManhour.getDevManhourFee());
                budgetDto.setStandTestManhourFee(standardManhour.getTestManhourFee());
            }
            jobItemCreateDto.setBudgetExeInfo(budgetDto);
        }

        responseDto.setContent(jobItemCreateDto);
        return responseDto;
    }

    @Transactional
    @PostMapping("/getJobItemCreateDlgInfo-function-assist/{parentJobitemId}")
    public ResponseDto<RdmsHmiJobItemCreateDto> getJobItemCreateDlgInfo_function_assist (@PathVariable String parentJobitemId) {
        ResponseDto<RdmsHmiJobItemCreateDto> responseDto = new ResponseDto<>();
        RdmsJobItem parentJobItem = rdmsJobItemService.selectByPrimaryKey(parentJobitemId);
        RdmsHmiJobItemCreateDto jobItemCreateDto = new RdmsHmiJobItemCreateDto();
        jobItemCreateDto.setJobName(parentJobItem.getJobName());
        jobItemCreateDto.setParentJobitemId(parentJobItem.getId());  //集成工单或者任务工单
        RdmsPreProject rdmsPreProject = rdmsPreProjectService.selectByPrimaryKey(parentJobItem.getPreProjectId());
        jobItemCreateDto.setPreProjectId(rdmsPreProject.getId());
        jobItemCreateDto.setProjectId(null);
        jobItemCreateDto.setSubprojectId(null);
        jobItemCreateDto.setCharacterId(null);
        jobItemCreateDto.setRdType(null);

        List<RdmsCustomerUser> customerUserList = rdmsCustomerUserService.listAllNormalWorkers(parentJobItem.getCustomerId());
        //重要代码
        List<RdmsHmiExecutorPlainDto> executorPlainDtos = customerUserList.stream().map(e -> new RdmsHmiExecutorPlainDto(e.getId(), e.getTrueName())).collect(Collectors.toList());
        jobItemCreateDto.setExecutors(executorPlainDtos);

        if(! ObjectUtils.isEmpty(parentJobItem.getProductManagerId())){
            RdmsCustomerUser customerUser = rdmsCustomerUserService.selectByPrimaryKey(parentJobItem.getProductManagerId());
            jobItemCreateDto.setProductManagerId(customerUser.getId());
            jobItemCreateDto.setProductManagerName(customerUser.getTrueName());
        }

        responseDto.setContent(jobItemCreateDto);
        return responseDto;
    }

    @Transactional
    @PostMapping("/getJobItemCreateDlgInfo-taskSubpAssist/{parentJobitemId}")
    public ResponseDto<RdmsHmiJobItemCreateDto> getJobItemCreateDlgInfo_taskSubpAssist (@PathVariable String parentJobitemId) {
        ResponseDto<RdmsHmiJobItemCreateDto> responseDto = new ResponseDto<>();
        RdmsJobItem parentJobItem = rdmsJobItemService.selectByPrimaryKey(parentJobitemId);
        RdmsHmiJobItemCreateDto jobItemCreateDto = new RdmsHmiJobItemCreateDto();
        jobItemCreateDto.setJobName(parentJobItem.getJobName());
        jobItemCreateDto.setParentJobitemId(parentJobItem.getId());  //集成工单或者任务工单
        RdmsProject rdmsProject = rdmsProjectService.selectByPrimaryKey(parentJobItem.getProjectId());
        jobItemCreateDto.setProjectId(rdmsProject.getId());
        jobItemCreateDto.setSubprojectId(parentJobItem.getSubprojectId());
        jobItemCreateDto.setCharacterId(parentJobItem.getCharacterId());
        jobItemCreateDto.setRdType(rdmsProject.getRdType());

        List<RdmsCustomerUser> customerUserList = rdmsCustomerUserService.listAllNormalWorkers(parentJobItem.getCustomerId());
        //重要代码
        List<RdmsHmiExecutorPlainDto> executorPlainDtos = customerUserList.stream().map(e -> new RdmsHmiExecutorPlainDto(e.getId(), e.getTrueName())).collect(Collectors.toList());
        jobItemCreateDto.setExecutors(executorPlainDtos);

        if(! ObjectUtils.isEmpty(parentJobItem.getProductManagerId())){
            RdmsCustomerUser customerUser = rdmsCustomerUserService.selectByPrimaryKey(parentJobItem.getProductManagerId());
            jobItemCreateDto.setProductManagerId(customerUser.getId());
            jobItemCreateDto.setProductManagerName(customerUser.getTrueName());
        }
        RdmsBudgetSummaryDto budgetDto = new RdmsBudgetSummaryDto();
        RdmsBudgetSummaryDto jobitemBudgetExeInfo = rdmsManhourService.getFirstLevelJobitemBudgetExeInfo(parentJobitemId);
        budgetDto.setRemainBudget(jobitemBudgetExeInfo.getRemainBudget());
        RdmsManhourStandard standardManhour = rdmsManhourService.getStandardManhourByCustomerId(parentJobItem.getCustomerId());
        if(!ObjectUtils.isEmpty(standardManhour)){
            budgetDto.setStandDevManhourFee(standardManhour.getDevManhourFee());
            budgetDto.setStandTestManhourFee(standardManhour.getTestManhourFee());
        }
        jobItemCreateDto.setBudgetExeInfo(budgetDto);

        responseDto.setContent(jobItemCreateDto);
        return responseDto;
    }

    @Transactional
    @PostMapping("/getJobItemCreateDlgInfo-quality-assist/{parentJobitemId}")
    public ResponseDto<RdmsHmiJobItemCreateDto> getJobItemCreateDlgInfo_qualityAssist (@PathVariable String parentJobitemId) {
        ResponseDto<RdmsHmiJobItemCreateDto> responseDto = new ResponseDto<>();
        RdmsJobItem parentJobItem = rdmsJobItemService.selectByPrimaryKey(parentJobitemId);
        RdmsHmiJobItemCreateDto jobItemCreateDto = new RdmsHmiJobItemCreateDto();
        jobItemCreateDto.setJobName(parentJobItem.getJobName());
        jobItemCreateDto.setQualityId(parentJobItem.getQualityId());
        jobItemCreateDto.setTestManagerId(parentJobItem.getTestManagerId());
        jobItemCreateDto.setProjectManagerId(parentJobItem.getProjectManagerId());
        jobItemCreateDto.setProductManagerId(parentJobItem.getProductManagerId());
        jobItemCreateDto.setParentJobitemId(parentJobItem.getId());  //质量工单
        RdmsProject rdmsProject = rdmsProjectService.selectByPrimaryKey(parentJobItem.getProjectId());
        jobItemCreateDto.setProjectId(rdmsProject.getId());
        jobItemCreateDto.setSubprojectId(parentJobItem.getSubprojectId());
        jobItemCreateDto.setRdType(rdmsProject.getRdType());

        List<RdmsCustomerUser> customerUserList = rdmsCustomerUserService.listAllNormalWorkers(parentJobItem.getCustomerId());
        List<RdmsHmiExecutorPlainDto> executorPlainDtos = customerUserList.stream().map(e -> new RdmsHmiExecutorPlainDto(e.getId(), e.getTrueName())).collect(Collectors.toList());
        jobItemCreateDto.setExecutors(executorPlainDtos);

        if(!ObjectUtils.isEmpty(parentJobItem.getQualityId())){
            RdmsBudgetSummaryDto budgetDto = new RdmsBudgetSummaryDto();
            RdmsBudgetSummaryDto jobitemBudgetExeInfo = rdmsManhourService.getFirstLevelJobitemBudgetExeInfo(parentJobitemId);
            budgetDto.setRemainBudget(jobitemBudgetExeInfo.getRemainBudget());

            RdmsManhourStandard standardManhour = rdmsManhourService.getStandardManhourByCustomerId(parentJobItem.getCustomerId());
            if(!ObjectUtils.isEmpty(standardManhour)){
                budgetDto.setStandDevManhourFee(standardManhour.getDevManhourFee());
                budgetDto.setStandTestManhourFee(standardManhour.getTestManhourFee());
            }
            jobItemCreateDto.setBudgetExeInfo(budgetDto);
        }

        responseDto.setContent(jobItemCreateDto);
        return responseDto;
    }

    @Transactional
    @PostMapping("/getJobItemCreateDlgInfo-assist-test/{testedAssistJobId}")
    public ResponseDto<RdmsHmiJobItemCreateDto> getJobItemCreateDlgInfo_assistTest (@PathVariable String testedAssistJobId) {
        ResponseDto<RdmsHmiJobItemCreateDto> responseDto = new ResponseDto<>();
        RdmsJobItem testedJobItem = rdmsJobItemService.selectByPrimaryKey(testedAssistJobId);
        RdmsHmiJobItemCreateDto jobItemCreateDto = new RdmsHmiJobItemCreateDto();
        jobItemCreateDto.setJobName(testedJobItem.getJobName());
        jobItemCreateDto.setTestJobitemId(testedJobItem.getId());  //被测试协作工单
        RdmsProject rdmsProject = rdmsProjectService.selectByPrimaryKey(testedJobItem.getProjectId());
        jobItemCreateDto.setProjectId(rdmsProject.getId());
        jobItemCreateDto.setSubprojectId(testedJobItem.getSubprojectId());
        jobItemCreateDto.setCharacterId(testedJobItem.getCharacterId());
        jobItemCreateDto.setRdType(rdmsProject.getRdType());

        List<RdmsCustomerUser> customerUserList = rdmsCustomerUserService.listAllNormalWorkers(testedJobItem.getCustomerId());
        //重要代码
        List<RdmsHmiExecutorPlainDto> executorPlainDtos = customerUserList.stream().map(e -> new RdmsHmiExecutorPlainDto(e.getId(), e.getTrueName())).collect(Collectors.toList());
        jobItemCreateDto.setExecutors(executorPlainDtos);

        if(! ObjectUtils.isEmpty(testedJobItem.getProductManagerId())){
            RdmsCustomerUser customerUser = rdmsCustomerUserService.selectByPrimaryKey(testedJobItem.getProductManagerId());
            jobItemCreateDto.setProductManagerId(customerUser.getId());
            jobItemCreateDto.setProductManagerName(customerUser.getTrueName());
        }

        if(testedJobItem.getType().equals(JobItemTypeEnum.TASK_SUBP.getType()) || testedJobItem.getType().equals(JobItemTypeEnum.QUALITY.getType())) {  //对于子项目任务工单执行测试任务, 测试费用走工单费用. 原因: 1. 测试费是给特性开发用的; 2. 不应该通过任务工单进行研发; 3. 避免预算乱用;
            RdmsBudgetSummaryDto budgetDto = new RdmsBudgetSummaryDto();
            RdmsBudgetSummaryDto jobitemBudgetExeInfo = rdmsManhourService.getFirstLevelJobitemBudgetExeInfo(testedJobItem.getId());
            budgetDto.setRemainBudget(jobitemBudgetExeInfo.getRemainBudget());

            RdmsManhourStandard standardManhour = rdmsManhourService.getStandardManhourByCustomerId(testedJobItem.getCustomerId());
            if(!ObjectUtils.isEmpty(standardManhour)){
                budgetDto.setStandDevManhourFee(standardManhour.getDevManhourFee());
                budgetDto.setStandTestManhourFee(standardManhour.getTestManhourFee());
            }
            jobItemCreateDto.setBudgetExeInfo(budgetDto);
        }else{
            if(!ObjectUtils.isEmpty(testedJobItem.getParentJobitemId())){ //注意: 这里和其他情况有区别
                RdmsBudgetSummaryDto budgetDto = new RdmsBudgetSummaryDto();

                RdmsBudgetSummaryDto jobitemBudgetExeInfo = rdmsManhourService.getFirstLevelJobitemBudgetExeInfo(testedJobItem.getParentJobitemId());
                budgetDto.setRemainBudget(jobitemBudgetExeInfo.getRemainBudget());

                RdmsManhourStandard standardManhour = rdmsManhourService.getStandardManhourByCustomerId(testedJobItem.getCustomerId());
                if(!ObjectUtils.isEmpty(standardManhour)){
                    budgetDto.setStandDevManhourFee(standardManhour.getDevManhourFee());
                    budgetDto.setStandTestManhourFee(standardManhour.getTestManhourFee());
                }
                jobItemCreateDto.setBudgetExeInfo(budgetDto);
            }
        }

        responseDto.setContent(jobItemCreateDto);
        return responseDto;
    }

    @Transactional
    @PostMapping("/getJobItemCreateDlgInfo-function-assist-test/{testedAssistJobId}")
    public ResponseDto<RdmsHmiJobItemCreateDto> getJobItemCreateDlgInfo_functionAssistTest (@PathVariable String testedAssistJobId) {
        ResponseDto<RdmsHmiJobItemCreateDto> responseDto = new ResponseDto<>();
        RdmsJobItem testedJobItem = rdmsJobItemService.selectByPrimaryKey(testedAssistJobId);
        RdmsHmiJobItemCreateDto jobItemCreateDto = new RdmsHmiJobItemCreateDto();
        jobItemCreateDto.setJobName(testedJobItem.getJobName());
        jobItemCreateDto.setTestJobitemId(testedJobItem.getId());  //被测试协作工单
        RdmsPreProject rdmsPreProject = rdmsPreProjectService.selectByPrimaryKey(testedJobItem.getPreProjectId());
        jobItemCreateDto.setProjectId(null);
        jobItemCreateDto.setPreProjectId(rdmsPreProject.getId());
        jobItemCreateDto.setSubprojectId(null);
        jobItemCreateDto.setCharacterId(null);
        jobItemCreateDto.setRdType(null);

        List<RdmsCustomerUser> customerUserList = rdmsCustomerUserService.listAllNormalWorkers(testedJobItem.getCustomerId());
        //重要代码
        List<RdmsHmiExecutorPlainDto> executorPlainDtos = customerUserList.stream().map(e -> new RdmsHmiExecutorPlainDto(e.getId(), e.getTrueName())).collect(Collectors.toList());
        jobItemCreateDto.setExecutors(executorPlainDtos);

        if(! ObjectUtils.isEmpty(testedJobItem.getProductManagerId())){
            jobItemCreateDto.setProductManagerName(testedJobItem.getProductManagerId());
        }

        if(testedJobItem.getType().equals(JobItemTypeEnum.TASK_FUNCTION.getType())) {  //对于子项目任务工单执行测试任务, 测试费用走工单费用. 原因: 1. 测试费是给特性开发用的; 2. 不应该通过任务工单进行研发; 3. 避免预算乱用;
            RdmsBudgetSummaryDto budgetDto = new RdmsBudgetSummaryDto();
            RdmsBudgetSummaryDto jobitemBudgetExeInfo = rdmsManhourService.getFirstLevelJobitemBudgetExeInfo(testedJobItem.getId());
            budgetDto.setRemainBudget(jobitemBudgetExeInfo.getRemainBudget());

            RdmsManhourStandard standardManhour = rdmsManhourService.getStandardManhourByCustomerId(testedJobItem.getCustomerId());
            if(!ObjectUtils.isEmpty(standardManhour)){
                budgetDto.setStandDevManhourFee(standardManhour.getDevManhourFee());
                budgetDto.setStandTestManhourFee(standardManhour.getTestManhourFee());
            }
            jobItemCreateDto.setBudgetExeInfo(budgetDto);
        }

        responseDto.setContent(jobItemCreateDto);
        return responseDto;
    }

    /**
     * 根据loginUserId进行列表查询
     */
    @Transactional
    @PostMapping("/getJobitemDetailInfo")
    public ResponseDto<RdmsJobItemDto> getJobitemDetailInfo(@RequestParam String jobitemId, String loginUserId) {
        ResponseDto<RdmsJobItemDto> responseDto = new ResponseDto<>();
        RdmsJobItemDto jobitemDetailInfo = rdmsJobItemService.getJobitemDetailInfo(jobitemId, loginUserId);
        if (jobitemDetailInfo != null) {
            jobitemDetailInfo.setFromPlan(rdmsJobItemGanttService.isIssuedFromPlan(jobitemDetailInfo.getId()));
            jobitemDetailInfo.setTimeEditable(rdmsJobItemGanttService.canAdjustJobTime(jobitemDetailInfo, loginUserId));
        }
        responseDto.setContent(jobitemDetailInfo);
        return responseDto;
    }

    @Transactional
    @PostMapping("/getTestJobitemAuxPlainInfo")
    public ResponseDto<RdmsHmiTestJobitemAuxPlainDto> getTestJobitemAuxPlainInfo(@RequestParam String jobitemId, String loginUserId) {
        ResponseDto<RdmsHmiTestJobitemAuxPlainDto> responseDto = new ResponseDto<>();
        RdmsHmiTestJobitemAuxPlainDto testJobitemAuxPlainInfo = rdmsJobItemService.getTestJobitemAuxPlainInfo(jobitemId, loginUserId);
        responseDto.setContent(testJobitemAuxPlainInfo);
        return responseDto;
    }

    /**
     * 根据loginUserId进行列表查询
     */
    @Transactional
    @PostMapping("/getJobitemRecordDetailInfo")
    public ResponseDto<RdmsJobItemDto> getJobitemRecordDetailInfo(@RequestParam String jobitemId, String loginUserId) {
        ResponseDto<RdmsJobItemDto> responseDto = new ResponseDto<>();
        RdmsJobItemDto jobitemDetailInfo = rdmsJobItemService.getJobitemRecordDetailInfo(jobitemId, loginUserId);
        responseDto.setContent(jobitemDetailInfo);
        return responseDto;
    }

    /**
     * 根据loginUserId进行列表查询
     */
    @Transactional
    @PostMapping("/getJobitemSimpleInfo")
    public ResponseDto<RdmsJobItemDto> getJobitemSimpleInfo(@RequestParam String jobitemId, String loginUserId) {
        ResponseDto<RdmsJobItemDto> responseDto = new ResponseDto<>();
        RdmsJobItemDto jobitemSimpleInfo = rdmsJobItemService.getJobitemSimpleInfo(jobitemId, loginUserId);
        if (jobitemSimpleInfo != null) {
            jobitemSimpleInfo.setFromPlan(rdmsJobItemGanttService.isIssuedFromPlan(jobitemSimpleInfo.getId()));
        }
        responseDto.setContent(jobitemSimpleInfo);
        return responseDto;
    }

    /**
     * 获得里程碑任务列表
     */
    @Transactional
    @PostMapping("/getMilestoneTaskJobList/{milestoneId}")
    public ResponseDto<List<RdmsJobItemDto>> getMilestoneTaskJobList(@PathVariable String milestoneId) {
        ResponseDto<List<RdmsJobItemDto>> responseDto = new ResponseDto<>();
        List<JobItemStatusEnum> statusEnumList = new ArrayList<>();
        statusEnumList.add(JobItemStatusEnum.HANDLING);
        statusEnumList.add(JobItemStatusEnum.SUBMIT);
        statusEnumList.add(JobItemStatusEnum.TESTING);
        statusEnumList.add(JobItemStatusEnum.EVALUATE);
        statusEnumList.add(JobItemStatusEnum.APPROVED);
        statusEnumList.add(JobItemStatusEnum.COMPLETED);
        statusEnumList.add(JobItemStatusEnum.ARCHIVED);
        List<RdmsJobItem> milestoneTaskJobList = rdmsJobItemService.getMilestoneTaskJobList(milestoneId, statusEnumList);
        List<RdmsJobItemDto> jobItemDtos = CopyUtil.copyList(milestoneTaskJobList, RdmsJobItemDto.class);
        if(!CollectionUtils.isEmpty(jobItemDtos)){
            for(RdmsJobItemDto jobItemDto: jobItemDtos){
                RdmsCustomerUser rdmsCustomerUser = rdmsCustomerUserService.selectByPrimaryKey(jobItemDto.getExecutorId());
                jobItemDto.setExecutorName(rdmsCustomerUser.getTrueName());
            }

        }
        responseDto.setContent(jobItemDtos);
        return responseDto;
    }

    /**
     * 根据loginUserId进行列表查询
     */
    @Transactional
    @PostMapping("/getIterationOriginCharacterIdByJobitemId/{jobItemId}")
    public ResponseDto<String> getIterationOriginCharacterIdByJobitemId(@PathVariable String jobItemId) {
        ResponseDto<String> responseDto = new ResponseDto<>();

        List<JobItemTypeEnum> jobItemTypeEnumList = new ArrayList<>();
        jobItemTypeEnumList.add(JobItemTypeEnum.UPDATE);
        jobItemTypeEnumList.add(JobItemTypeEnum.SUGGEST_UPDATE);
        List<RdmsCharacterDto> updateCharacters = rdmsCharacterService.getCharacterListByJobitemIdAndStatusTypeList(jobItemId, null, jobItemTypeEnumList);
        if(! CollectionUtils.isEmpty(updateCharacters)){
            if(updateCharacters.get(0).getJobitemType().equals(JobItemTypeEnum.UPDATE.getType())){
                responseDto.setContent(updateCharacters.get(0).getParent());
                return responseDto;
            }else{
                RdmsJobItem rdmsJobItem = rdmsJobItemService.selectByPrimaryKey(jobItemId);
                responseDto.setContent(rdmsJobItem.getCharacterId());  //很简单, 就是工单记录的CharacterId
                return responseDto;
            }
        }
        throw new BusinessException(BusinessExceptionCode.DB_RECORD_NOT_EXIST, "原始特性不存在");
    }

    /**
     * 根据loginUserId进行列表查询
     */
    @Transactional
    @PostMapping("/getReviewJobitemListDetail")
    public ResponseDto<RdmsHmiJobItemAndOtherReviewJobItemsDto> getReviewJobitemListDetail(@RequestParam String jobitemId, String loginUserId) {
        ResponseDto<RdmsHmiJobItemAndOtherReviewJobItemsDto> responseDto = new ResponseDto<>();

        RdmsHmiJobItemAndOtherReviewJobItemsDto reviewJobItemsDto = new RdmsHmiJobItemAndOtherReviewJobItemsDto();
        RdmsJobItemDto jobitemDetailInfo = rdmsJobItemService.getJobitemDetailInfo(jobitemId, loginUserId);
        reviewJobItemsDto.setJobitem(jobitemDetailInfo);

        if(jobitemDetailInfo.getType().equals(JobItemTypeEnum.REVIEW_SUBP.getType())){
            RdmsProjectSubproject subproject = rdmsSubprojectService.selectByPrimaryKey(jobitemDetailInfo.getSubprojectId());
            reviewJobItemsDto.setOutSourceStatus(subproject.getOutSourceStatus());
        }

        List<RdmsJobItemDto> otherReviewJobitems = rdmsJobItemService.getOtherReviewJobitems(jobitemId, loginUserId);
        reviewJobItemsDto.setOtherReviewJobitemList(otherReviewJobitems);

        List<RdmsJobItemDto> supplementJobitems = rdmsJobItemService.getSupplementJobitems(jobitemId, loginUserId);
        reviewJobItemsDto.setSupplementJobitemList(supplementJobitems);

        Boolean allChildrenJobitemComplateStatus = rdmsJobItemService.getAllChildrenJobitemComplateStatus(jobitemId);
        reviewJobItemsDto.setChildrenJobComplateFlag(allChildrenJobitemComplateStatus);

        responseDto.setContent(reviewJobItemsDto);
        return responseDto;
    }

    /**
     * 根据loginUserId进行列表查询
     */
    @Transactional
    @PostMapping("/getReviewNotifyInfo")
    public ResponseDto<RdmsHmiJobItemAndReviewResultDto> getReviewNotifyInfo(@RequestParam String jobitemId, String loginUserId) {
        ResponseDto<RdmsHmiJobItemAndReviewResultDto> responseDto = new ResponseDto<>();
        RdmsHmiJobItemAndReviewResultDto jobItemAndReviewNotifyDto = new RdmsHmiJobItemAndReviewResultDto();
        RdmsJobItemDto jobItemDto = rdmsJobItemService.getJobitemDetailInfo(jobitemId, loginUserId);
        jobItemAndReviewNotifyDto.setJobitem(jobItemDto);

        RdmsJobItemDto parentJobitem = new RdmsJobItemDto();
        if(!ObjectUtils.isEmpty(jobItemDto) && !ObjectUtils.isEmpty(jobItemDto.getParentJobitemId())){    //getParentJobitemId 是产生notify工单的评审工单
            parentJobitem = rdmsJobItemService.getJobitemDetailInfo(jobItemDto.getParentJobitemId(), loginUserId);
        }
        RdmsHmiJobItemAndOtherReviewJobItemsDto reviewJobItemsDto = new RdmsHmiJobItemAndOtherReviewJobItemsDto();
        reviewJobItemsDto.setJobitem(parentJobitem);
        List<RdmsJobItemDto> otherReviewJobitems = rdmsJobItemService.getOtherReviewJobitems(parentJobitem.getId(), loginUserId);
        reviewJobItemsDto.setOtherReviewJobitemList(otherReviewJobitems);

        jobItemAndReviewNotifyDto.setReviewJobitemInfo(reviewJobItemsDto);
//        parentJobitem.getReviewResult();
        ReviewResultEnum reviewResultEnumByStatus = ReviewResultEnum.getReviewResultEnumByStatus(parentJobitem.getReviewResult());
        jobItemAndReviewNotifyDto.setReviewResult(reviewResultEnumByStatus.getStatusNum());
        responseDto.setContent(jobItemAndReviewNotifyDto);
        return responseDto;
    }

    @Transactional
    @PostMapping("/confirmMilestoneNotify/{jobItemId}")
    public ResponseDto<String> confirmMilestoneNotify(@PathVariable String jobItemId) {
        return confirmNotify(jobItemId, JobItemTypeEnum.MILESTONE_NOTIFY);
    }

    @Transactional
    /** 定义更新通知确认：只能由接收人确认，确认后关闭。 */
    @PostMapping("/confirmDefineUpdateNotify/{jobItemId}")
    public ResponseDto<String> confirmDefineUpdateNotify(@PathVariable String jobItemId) {
        return confirmNotify(jobItemId, JobItemTypeEnum.DEFINE_UPDATE_NOTIFY);
    }

    @Transactional
    @PostMapping("/confirmCbbUpdateNotify/{jobItemId}")
    public ResponseDto<String> confirmCbbUpdateNotify(@PathVariable String jobItemId) {
        return confirmNotify(jobItemId, JobItemTypeEnum.CBB_UPDATE_NOTIFY);
    }

    @Transactional
    @PostMapping("/confirmPlanIssueNotify/{jobItemId}")
    public ResponseDto<String> confirmPlanIssueNotify(@PathVariable String jobItemId) {
        return confirmNotify(jobItemId, JobItemTypeEnum.PLAN_ISSUE_NOTIFY);
    }

    @Transactional
    @PostMapping("/confirmBudgetAdjustNotify/{jobItemId}")
    public ResponseDto<String> confirmBudgetAdjustNotify(@PathVariable String jobItemId) {
        return confirmNotify(jobItemId, JobItemTypeEnum.BUDGET_ADJUST_NOTIFY);
    }

    /** 通知类工单确认：类型须匹配，同租户，只能由接收人（执行人）确认；已完成幂等。 */
    private ResponseDto<String> confirmNotify(String jobItemId, JobItemTypeEnum notifyType) {
        ResponseDto<String> responseDto = new ResponseDto<>();
        RdmsJobItem rdmsJobItem = rdmsJobItemService.selectByPrimaryKey(jobItemId);
        if (rdmsJobItem == null || !notifyType.getType().equals(rdmsJobItem.getType())) {
            throw new BusinessException(BusinessExceptionCode.DB_RECORD_NOT_EXIST);
        }
        TenantAccess.assertSameTenant(rdmsJobItem.getCustomerId());
        String me = CharacterRichDocService.memberId();
        if (me == null || !me.equals(rdmsJobItem.getExecutorId())) {
            throw new BusinessException(BusinessExceptionCode.NO_PERMISSION);
        }
        if (!JobItemStatusEnum.closedStatuses().contains(rdmsJobItem.getStatus())) {
            rdmsJobItem.setStatus(JobItemStatusEnum.COMPLETED.getStatus());
            rdmsJobItem.setCompleteTime(new Date());
            rdmsJobItemService.update(rdmsJobItem);
        }
        responseDto.setContent(jobItemId);
        return responseDto;
    }

    /**
     * 根据loginUserId进行列表查询
     */
    @Transactional
    @PostMapping("/getReviewResultInfo")
    public ResponseDto<RdmsHmiJobItemAndReviewResultDto> getReviewResultInfo(@RequestParam String jobitemId, String loginUserId) {
        ResponseDto<RdmsHmiJobItemAndReviewResultDto> responseDto = new ResponseDto<>();
        RdmsHmiJobItemAndReviewResultDto jobItemAndReviewResultDto = new RdmsHmiJobItemAndReviewResultDto();

        RdmsJobItemDto jobItemDto = rdmsJobItemService.getJobitemDetailInfo(jobitemId, loginUserId);
        jobItemAndReviewResultDto.setJobitem(jobItemDto);

        RdmsHmiJobItemAndOtherReviewJobItemsDto reviewJobItemsDto = new RdmsHmiJobItemAndOtherReviewJobItemsDto();
        reviewJobItemsDto.setJobitem(jobItemDto);
        List<RdmsJobItemDto> otherReviewJobitems = rdmsJobItemService.getOtherReviewJobitems(jobItemDto.getId(), loginUserId);
        reviewJobItemsDto.setOtherReviewJobitemList(otherReviewJobitems);

        jobItemAndReviewResultDto.setReviewJobitemInfo(reviewJobItemsDto);
        ReviewResultEnum reviewResultEnumByStatus = ReviewResultEnum.getReviewResultEnumByStatus(jobItemDto.getReviewResult());
        jobItemAndReviewResultDto.setReviewResult(reviewResultEnumByStatus.getStatusNum());  //对应前端的 数字
        responseDto.setContent(jobItemAndReviewResultDto);
        return responseDto;
    }

    /**
     * 保存客户信息
     */
    @PostMapping("/confirmReviewResult/{jobItemId}")
    @Transactional
    public ResponseDto confirmReviewResult(@PathVariable String jobItemId) {
        ResponseDto responseDto = new ResponseDto<>();
        rdmsJobItemReviewService.confirmReviewResult(jobItemId);
        responseDto.setSuccess(true);
        responseDto.setMessage("已完成");
        return responseDto;
    }

    /**
     * 获取集成工单和所有协作工单信息
     */
    @Transactional
    @PostMapping("/getIntegrationJobitemDetailInfo")
    public ResponseDto<RdmsJobItemDto> getIntegrationJobitemDetailInfo(@RequestParam String jobitemId, String loginUserId) {
        ResponseDto<RdmsJobItemDto> responseDto = new ResponseDto<>();
        RdmsJobItemDto intJobitem = rdmsJobItemService.getJobitemDetailInfo(jobitemId, loginUserId);
        List<RdmsJobItemDto> assistJobitemList = rdmsJobItemService.getJobitemListByParentJobitemId(intJobitem.getId(), null, JobItemTypeEnum.ASSIST.getType());
        intJobitem.setAssistJobitemList(assistJobitemList);
        responseDto.setContent(intJobitem);
        return responseDto;
    }

    /**
     * 复核工单执行人完成复核
     */
    @PostMapping("/completeReCheckJobitem/{jobItemId}")
    public ResponseDto<Boolean> completeReCheckJobitem(@PathVariable String jobItemId, String loginUserId) {
        ResponseDto<Boolean> responseDto = new ResponseDto<>();
        rdmsJobItemFlowService.completeRecheck(jobItemId, loginUserId);
        responseDto.setContent(true);
        return responseDto;
    }

    /**
     * 获取一个工单的所有相关的附件信息
     * 1. 工单附件
     * 2. 提交附件
     * 3. 流转附件
     * 4. 测试附件 (含评审类附件)
     * 5. 协作工单提交附件 (协作工单没有测试附件)
     * 6. 联合评审人员的提交附件
     */
    @Transactional
    @PostMapping("/getJobitemAllAttachments/{jobItemId}")
    public ResponseDto<List<RdmsFileDto>> getJobitemAllAttachments(@PathVariable String jobItemId) {
        ResponseDto<List<RdmsFileDto>> responseDto = new ResponseDto<>();
        List<RdmsFileDto> fileDtoList = rdmsJobItemService.getJobitemAllAttachments(jobItemId);
        responseDto.setContent(fileDtoList);
        return responseDto;
    }

    @Transactional
    @PostMapping("/getAllChildrenJobitemAttachments/{jobItemId}")
    public ResponseDto<List<RdmsFileDto>> getAllChildrenJobitemAttachments(@PathVariable String jobItemId) {
        ResponseDto<List<RdmsFileDto>> responseDto = new ResponseDto<>();
        List<RdmsFileDto> fileDtoList = rdmsJobItemService.getAllChildrenJobitemAttachments(jobItemId);
        responseDto.setContent(fileDtoList);
        return responseDto;
    }

    @Transactional
    @PostMapping("/getChildrenJobitemNotCompleteNum/{parentJobitem}")
    public ResponseDto<Long> getChildrenJobitemNotCompleteNum(@PathVariable String parentJobitem) {
        ResponseDto<Long> responseDto = new ResponseDto<>();
        Long childrenJobitemNotCompleteNum = rdmsJobItemService.getChildrenJobitemNotCompleteNum(parentJobitem);
        responseDto.setContent(childrenJobitemNotCompleteNum);
        return responseDto;
    }

    @PostMapping("/refuseJobitem")
    public ResponseDto refuseJobitem(@RequestBody RdmsJobItemProcessDto processDto) {
        ResponseDto responseDto = new ResponseDto<>();
        rdmsJobItemRefuseService.refuse(processDto);
        responseDto.setSuccess(true);
        responseDto.setMessage("提交成功");
        return responseDto;
    }

    @PostMapping("/cancelJobitem")
    public ResponseDto cancelJobitem(@RequestParam String jobitemId, String loginUserId) {
        ResponseDto responseDto = new ResponseDto<>();
        ValidatorUtil.require(jobitemId, "工单ID");
        rdmsJobItemFlowService.cancel(jobitemId, loginUserId);
        responseDto.setSuccess(true);
        responseDto.setMessage("任务取消成功");
        return responseDto;
    }


    @PostMapping("/reIssueJobitem")
    public ResponseDto<String> reIssueJobitem(@RequestBody RdmsJobItemDto jobItemsDto) {
        ResponseDto<String> responseDto = new ResponseDto<>();

        ValidatorUtil.require(jobItemsDto.getId(), "工单ID");
        ValidatorUtil.require(jobItemsDto.getCustomerId(), "客户ID");
        ValidatorUtil.require(jobItemsDto.getJobName(), "工作项名称");
        ValidatorUtil.require(jobItemsDto.getTaskDescription(), "任务描述");
        ValidatorUtil.require(jobItemsDto.getPlanSubmissionTime(), "计划提交时间");
        ValidatorUtil.require(jobItemsDto.getExecutorId(), "执行人");
        ValidatorUtil.require(jobItemsDto.getProjectType(), "项目类型");
        ValidatorUtil.require(jobItemsDto.getType(), "工单类型");

        ValidatorUtil.length(jobItemsDto.getJobName(), "项目名称", 2, 50);
        ValidatorUtil.length(jobItemsDto.getTaskDescription(), "任务描述", 10, 1000);

        responseDto.setContent(rdmsJobItemFlowService.reIssue(jobItemsDto));
        return responseDto;
    }
    @PostMapping("/modifyTimeLimit")
    @Transactional
    public ResponseDto<String> modifyTimeLimit(@RequestBody RdmsJobItemDto jobItemsDto) throws ParseException {
        ResponseDto<String> responseDto = new ResponseDto<>();

        ValidatorUtil.require(jobItemsDto.getId(), "工单ID");
        ValidatorUtil.require(jobItemsDto.getPlanSubmissionTime(), "计划提交时间");

        RdmsJobItem dbJobItem = rdmsJobItemService.selectByPrimaryKey(jobItemsDto.getId());
        if (dbJobItem == null) {
            throw new BusinessException(BusinessExceptionCode.DB_RECORD_NOT_EXIST);
        }
        TenantAccess.assertSameTenant(dbJobItem.getCustomerId());
        if (JobItemStatusEnum.closedStatuses().contains(dbJobItem.getStatus())) {
            throw new BusinessException(BusinessExceptionCode.JOB_ITEM_STATUS_INVALID);
        }
        jobActorGuard.assertActor(dbJobItem.getCustomerId(), jobItemsDto.getLoginUserId(),
                dbJobItem.getCreaterId(), dbJobItem.getProjectManagerId(), dbJobItem.getProductManagerId());

        if (!rdmsJobItemGanttService.isIssuedFromPlan(dbJobItem.getId())) {
            jobIssueWindowGuard.assertWithin(dbJobItem.getType(), jobItemsDto.getPlanSubmissionTime());
        }

        jobItemsDto.setPlanSubmissionTime(DateUtil.afternoon23_59pm(jobItemsDto.getPlanSubmissionTime()));

        RdmsJobItem updateJobItem = new RdmsJobItem();
        updateJobItem.setId(jobItemsDto.getId());
        updateJobItem.setUpdateTime(new Date());
        updateJobItem.setPlanSubmissionTime(jobItemsDto.getPlanSubmissionTime());
        rdmsJobItemService.updateByPrimaryKeySelective(updateJobItem);

        responseDto.setContent(updateJobItem.getId());
        return responseDto;
    }

    @PostMapping("/modifyManhour")
    @Transactional
    public ResponseDto<String> modifyManhour(@RequestBody RdmsJobItemDto jobItemsDto) {
        ResponseDto<String> responseDto = new ResponseDto<>();

        ValidatorUtil.require(jobItemsDto.getId(), "工单ID");
        ValidatorUtil.require(jobItemsDto.getManhour(), "工单工时");

        RdmsJobItem dbJobItem = rdmsJobItemService.selectByPrimaryKey(jobItemsDto.getId());
        if (dbJobItem == null) {
            throw new BusinessException(BusinessExceptionCode.DB_RECORD_NOT_EXIST);
        }
        TenantAccess.assertSameTenant(dbJobItem.getCustomerId());
        if (JobItemStatusEnum.closedStatuses().contains(dbJobItem.getStatus())) {
            throw new BusinessException(BusinessExceptionCode.JOB_ITEM_STATUS_INVALID);
        }
        jobActorGuard.assertActor(dbJobItem.getCustomerId(), jobItemsDto.getLoginUserId(),
                dbJobItem.getCreaterId(), dbJobItem.getProjectManagerId(), dbJobItem.getProductManagerId());

        RdmsJobItem updateJobItem = new RdmsJobItem();
        updateJobItem.setId(jobItemsDto.getId());
        updateJobItem.setUpdateTime(new Date());

        OperateTypeEnum opeType = JobItemTypeEnum.TEST.getType().equals(dbJobItem.getType())
                ? OperateTypeEnum.TEST : OperateTypeEnum.DEVELOP;
        Double standManhour = rdmsManhourService.transformToStandManhour(jobItemsDto.getManhour(),
                dbJobItem.getExecutorId(), dbJobItem.getCustomerId(), opeType);
        updateJobItem.setManhour(standManhour);
        updateJobItem.setStandManhourFee(rdmsManhourService.getStandManhourFee(dbJobItem.getCustomerId(), opeType));

        rdmsJobItemService.updateByPrimaryKeySelective(updateJobItem);

        responseDto.setContent(updateJobItem.getId());
        return responseDto;
    }

    /**
     * 特性管理阶段对工单的保存处理
     */
    @PostMapping("/save")
    @Transactional
    public ResponseDto<RdmsJobItemDto> save(@RequestBody RdmsJobItemDto jobItemsDto) throws ParseException {
        ResponseDto<RdmsJobItemDto> responseDto = new ResponseDto<>();
        jobIssueWindowGuard.assertWithin(jobItemsDto.getType(), jobItemsDto.getPlanSubmissionTime());
        String overStatus = rdmsJobItemService.saveJobitem(jobItemsDto);
        if(!ObjectUtils.isEmpty(overStatus) && overStatus.equals(OverLoadStatusEnum.OVERLOAD.getStatus())){
            throw new BusinessException(BusinessExceptionCode.JOB_OVERLOAD, OverLoadStatusEnum.OVERLOAD.getName());
        }
        responseDto.setContent(jobItemsDto);
        return responseDto;
    }

    /**
     * 保存客户信息
     */
    @PostMapping("/saveEvaluate")
    @Transactional
    public ResponseDto<RdmsJobItemDto> saveEvaluate(@RequestBody RdmsJobItemDto jobItemsDto) {
        ResponseDto<RdmsJobItemDto> responseDto = new ResponseDto<>();

        rdmsJobItemService.saveEvaluate(jobItemsDto);
        rdmsJobItemEvaluateService.archiveProductTaskFiles(jobItemsDto);

        responseDto.setContent(jobItemsDto);
        responseDto.setSuccess(true);
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    /**
     * 保存客户信息
     */
    @PostMapping("/submit")
    public ResponseDto<RdmsJobItemDto> submit(@RequestBody RdmsJobItemDto jobItemsDto) {
        ResponseDto<RdmsJobItemDto> responseDto = new ResponseDto<>();
        rdmsJobItemSubmitService.submit(jobItemsDto);
        responseDto.setContent(jobItemsDto);
        responseDto.setSuccess(true);
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    @PostMapping("/reject")
    public ResponseDto reject(@RequestBody RdmsJobItemProcessDto jobItemProcessDto) {
        ResponseDto responseDto = new ResponseDto<>();
        ValidatorUtil.require(jobItemProcessDto.getJobItemId(), "工单ID");
        rdmsJobItemFlowService.reject(jobItemProcessDto);
        responseDto.setSuccess(true);
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    /**
     *
     * @param jobItemProcessDto 复核工单的信息
     * @return
     */
    @PostMapping("/rechecked")
    public ResponseDto rechecked(@RequestBody RdmsJobItemProcessDto jobItemProcessDto) {
        ResponseDto responseDto = new ResponseDto<>();
        ValidatorUtil.require(jobItemProcessDto.getJobItemId(), "工单ID");
        rdmsJobItemFlowService.rechecked(jobItemProcessDto);
        responseDto.setSuccess(true);
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    /** 分解复核不通过：jobDescription 为复核意见。 */
    @PostMapping("/recheck-reject")
    public ResponseDto recheckReject(@RequestBody RdmsJobItemProcessDto jobItemProcessDto) {
        ResponseDto responseDto = new ResponseDto<>();
        ValidatorUtil.require(jobItemProcessDto.getJobItemId(), "工单ID");
        rdmsJobItemFlowService.recheckReject(jobItemProcessDto);
        responseDto.setSuccess(true);
        responseDto.setMessage("已退回分解执行人");
        return responseDto;
    }

    /**
     * 关闭工作
     */
    @PostMapping("/closeJobitem/{jobId}")
    public ResponseDto closeJobitem(@PathVariable String jobId) {
        ResponseDto responseDto = new ResponseDto();
        rdmsJobItemService.closeJobitem(jobId);
        responseDto.setContent(true);
        return responseDto;
    }

    /**
     * 删除
     */
    @PostMapping("/archive")
    public ResponseDto archive(@RequestBody RdmsHmiIdsDto idsDto) {
        ResponseDto responseDto = new ResponseDto();
        rdmsJobItemService.archive(idsDto);
        return responseDto;
    }

    /**
     * 删除
     */
    @Transactional
    @PostMapping("/getPjmProjectJobNotifyNum")
    public ResponseDto<Integer> getPjmProjectJobNotifyNum(@RequestBody RdmsHmiIdsDto idsDto) {
        idsDto.setProjectManagerId(TenantAccess.resolveMemberId(idsDto.getProjectManagerId()));
        ResponseDto<Integer> responseDto = new ResponseDto<>();
        List<JobItemStatusEnum> statusList = new ArrayList<>();
        statusList.add(JobItemStatusEnum.SUBMIT);
        statusList.add(JobItemStatusEnum.EVALUATE);
        List<RdmsJobItemDto> notifyJobList = rdmsJobItemService.getJobitemListByCustomerIdPjmIdAndStatus(idsDto.getCustomerId(), idsDto.getProjectManagerId(), statusList, ProjectTypeEnum.DEV_PROJECT);
        List<RdmsJobItemDto> notifyJobList_task = rdmsJobItemService.getTaskJobitemListByCustomerIdPjmIdAndStatus(idsDto.getCustomerId(), idsDto.getProjectManagerId(), statusList, ProjectTypeEnum.SUB_PROJECT);
        responseDto.setContent(notifyJobList.size() + notifyJobList_task.size());
        return responseDto;
    }

    @Transactional
    @PostMapping("/getPdmProjectJobNotifyNum")
    public ResponseDto<Integer> getPdmProjectJobNotifyNum(@RequestBody RdmsHmiIdsDto idsDto) {
        idsDto.setProductManagerId(TenantAccess.resolveMemberId(idsDto.getProductManagerId()));
        ResponseDto<Integer> responseDto = new ResponseDto<>();
        List<JobItemStatusEnum> statusList = new ArrayList<>();
        statusList.add(JobItemStatusEnum.SUBMIT);
        List<RdmsJobItemDto> notifyJobList = rdmsJobItemService.getJobitemListByCustomerIdPdmIdAndStatus(idsDto.getCustomerId(), idsDto.getProductManagerId(), statusList, ProjectTypeEnum.PROJECT);
        Integer materialAndFeeApplicationNum = rdmsProjectService.getMaterialAndFeeApplicationNum(idsDto.getProductManagerId());

        if(!CollectionUtils.isEmpty(notifyJobList)){
            List<RdmsJobItemDto> collect = notifyJobList.stream().filter(item -> item.getType().equals(JobItemTypeEnum.TASK_PRODUCT.getType())).collect(Collectors.toList());
            responseDto.setContent(collect.size() + materialAndFeeApplicationNum);
        }else {
            responseDto.setContent(materialAndFeeApplicationNum);
        }
        return responseDto;
    }

}
