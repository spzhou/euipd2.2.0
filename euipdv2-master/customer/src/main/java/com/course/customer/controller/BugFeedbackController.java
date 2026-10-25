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
import com.course.server.dto.ResponseDto;
import com.course.server.dto.rdms.*;
import com.course.server.enums.rdms.JobItemProcessStatusEnum;
import com.course.server.enums.rdms.JobItemStatusEnum;
import com.course.server.enums.rdms.JobItemTypeEnum;
import com.course.server.enums.rdms.QualityStatusEnum;
import com.course.server.exception.BusinessException;
import com.course.server.exception.BusinessExceptionCode;
import com.course.server.security.tenant.TenantAccess;
import com.course.server.security.tenant.TenantContext;
import com.course.server.service.rdms.*;
import com.course.server.service.rdms.rich.BizRichDocService;
import com.course.server.service.rdms.rich.RichDocConst;
import com.course.server.util.CopyUtil;
import com.course.server.util.ValidatorUtil;
import com.course.server.util.RichTextFields;
import com.course.customer.flow.JobFlowBridge;
import com.course.customer.flow.QualityFlowBridge;
import org.springframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/bugfeedback")
public class BugFeedbackController {
    @Resource
    private BizRichDocService bizRichDocService;
    private static final Logger LOG = LoggerFactory.getLogger(BugFeedbackController.class);
    public static final String BUSINESS_NAME = "缺陷管理";

    @Resource
    private RdmsBugFeedbackService rdmsBugFeedbackService;
    @Resource
    private JobActorGuard jobActorGuard;
    @Resource
    private QualityFlowBridge qualityFlowBridge;
    @Resource
    private JobFlowBridge jobFlowPort;
    @Resource
    private RdmsSubprojectService rdmsSubprojectService;
    @Resource
    private RdmsJobItemService rdmsJobItemService;
    @Resource
    private RdmsJobItemPropertyService rdmsJobItemPropertyService;
    @Resource
    private JobItemProcessRecorder jobItemProcessRecorder;
    @Resource
    private RdmsCharacterService rdmsCharacterService;
    @Autowired
    private RdmsBossService rdmsBossService;
    @Autowired
    private RdmsTgmService rdmsTgmService;

    @PostMapping("/getBugFeedbackNumByUserId/{userId}")
    public ResponseDto<Long> getBugFeedbackNumByUserId(@PathVariable String userId) {
        userId = TenantAccess.resolveMemberId(userId);
        ResponseDto<Long> responseDto = new ResponseDto<>();
        long testManageNum = rdmsBugFeedbackService.getBugFeedbackNumByUserId(userId);
        responseDto.setContent(testManageNum);
        return responseDto;
    }




    @PostMapping("/getBugRecheckNumByUserId/{userId}")
    public ResponseDto<Long> getBugRecheckNumByUserId(@PathVariable String userId) {
        userId = TenantAccess.resolveMemberId(userId);
        ResponseDto<Long> responseDto = new ResponseDto<>();
        long testManageNum = rdmsBugFeedbackService.getBugRecheckNumByUserId(userId);
        responseDto.setContent(testManageNum);
        return responseDto;
    }

    @PostMapping("/getBugDefineSubmitNumByPdm/{userId}")
    public ResponseDto<Long> getBugDefineSubmitNumByPdm(@PathVariable String userId) {
        userId = TenantAccess.resolveMemberId(userId);
        ResponseDto<Long> responseDto = new ResponseDto<>();
        long qualityItemNum = rdmsBugFeedbackService.countBugDefineSubmitNumByPdm(userId);
        responseDto.setContent(qualityItemNum);
        return responseDto;
    }

    @PostMapping("/getBugAndSubmitJobitems/{bugfeedbackId}")
    public ResponseDto<RdmsBugFeedbackDto> getBugAndSubmitJobitems(@PathVariable String bugfeedbackId) {
        ResponseDto<RdmsBugFeedbackDto> responseDto = new ResponseDto<>();
        RdmsBugFeedbackDto bugAndSubmitJobitems = rdmsBugFeedbackService.getBugAndSubmitJobitems(bugfeedbackId);
        responseDto.setContent(bugAndSubmitJobitems);
        return responseDto;
    }

    @PostMapping("/getBugCostListByProjectId/{projectId}")
    public ResponseDto<List<RdmsBugFeedbackDto>> getBugCostListByProjectId(@PathVariable String projectId) {
        ResponseDto<List<RdmsBugFeedbackDto>> responseDto = new ResponseDto<>();
        List<RdmsBugFeedbackDto> qualityCostListByProjectId = rdmsBugFeedbackService.getBugCostListByProjectId(projectId);
        responseDto.setContent(qualityCostListByProjectId);
        return responseDto;
    }

    @PostMapping("/getBugHasListFlag/{userId}")
    public ResponseDto<Boolean> getBugHasListFlag(@PathVariable String userId) {
        userId = TenantAccess.resolveMemberId(userId);
        ResponseDto<Boolean> responseDto = new ResponseDto<>();
        long l = rdmsBugFeedbackService.countBugJobNumByPdm(userId);
        responseDto.setContent(l >0);
        return responseDto;
    }

    @PostMapping("/getBugAndAnalysisJobList/{qualityId}")
    public ResponseDto<RdmsBugFeedbackDto> getCharacterAndJobitemList(@PathVariable String qualityId) {
        ResponseDto<RdmsBugFeedbackDto> responseDto = new ResponseDto<>();
        RdmsBugFeedbackDto qualityAndAnalysisJobList = rdmsBugFeedbackService.getBugAndAnalysisJobList(qualityId);
        responseDto.setContent(qualityAndAnalysisJobList);
        return responseDto;
    }

    @PostMapping("/list")
    public ResponseDto<PageDto<RdmsBugFeedbackDto>> list(@RequestBody PageDto<RdmsBugFeedbackDto> pageDto) {
        TenantAccess.bindSelf(pageDto);
        ResponseDto<PageDto<RdmsBugFeedbackDto>> responseDto = new ResponseDto<>();
        rdmsBugFeedbackService.list(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

    @PostMapping("/listByProjectId")
    public ResponseDto<PageDto<RdmsBugFeedbackDto>> listByProjectId(@RequestBody PageDto<RdmsBugFeedbackDto> pageDto) {
        ResponseDto<PageDto<RdmsBugFeedbackDto>> responseDto = new ResponseDto<>();
        rdmsBugFeedbackService.listByProjectId(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

    @PostMapping("/listByProjectId_handling")
    public ResponseDto<PageDto<RdmsBugFeedbackDto>> listByProjectId_handling(@RequestBody PageDto<RdmsBugFeedbackDto> pageDto) {
        ResponseDto<PageDto<RdmsBugFeedbackDto>> responseDto = new ResponseDto<>();
        rdmsBugFeedbackService.listByProjectId_handling(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

    @PostMapping("/getBugAndJobitemList/{bugId}")
    public ResponseDto<RdmsBugFeedbackDto> getBugAndJobitemList(@PathVariable String bugId) {
        ResponseDto<RdmsBugFeedbackDto> responseDto = new ResponseDto<>();
        RdmsBugFeedbackDto bugAndJobitemList = rdmsBugFeedbackService.getBugAndJobitemList(bugId);
        responseDto.setContent(bugAndJobitemList);
        return responseDto;
    }


    @PostMapping("/listBugByPjm")
    public ResponseDto<PageDto<RdmsBugFeedbackDto>> listBugByPjm(@RequestBody PageDto<RdmsBugFeedbackDto> pageDto) {
        TenantAccess.bindSelf(pageDto);
        ResponseDto<PageDto<RdmsBugFeedbackDto>> responseDto = new ResponseDto<>();
        rdmsBugFeedbackService.listBugByPjm(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

    @PostMapping("/listByTestManager")
    public ResponseDto<PageDto<RdmsBugFeedbackDto>> listByTestManager(@RequestBody PageDto<RdmsBugFeedbackDto> pageDto) {
        TenantAccess.bindSelf(pageDto);
        ResponseDto<PageDto<RdmsBugFeedbackDto>> responseDto = new ResponseDto<>();
        rdmsBugFeedbackService.listBugByTestManager(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

    @PostMapping("/getBuaArchiveListBySubprojectId")
    public ResponseDto<PageDto<RdmsBugFeedbackDto>> getBuaArchiveListBySubprojectId(@RequestBody PageDto<RdmsBugFeedbackDto> pageDto) {
        TenantAccess.bindSelf(pageDto);
        ResponseDto<PageDto<RdmsBugFeedbackDto>> responseDto = new ResponseDto<>();
        rdmsBugFeedbackService.getBuaArchiveListBySubprojectId(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

    @PostMapping("/listBugByCharacterId")
    public ResponseDto<List<RdmsBugFeedbackDto>> listBugByCharacterId(@RequestParam String characterId, String loginUserId) {
        ResponseDto<List<RdmsBugFeedbackDto>> responseDto = new ResponseDto<>();
        List<RdmsBugFeedbackDto> rdmsBugDtos = rdmsBugFeedbackService.listBugByCharacterId(characterId, loginUserId);
        responseDto.setContent(rdmsBugDtos);
        return responseDto;
    }

    @PostMapping("/listByManager")
    public ResponseDto<PageDto<RdmsBugFeedbackDto>> listByManager(@RequestBody PageDto<RdmsBugFeedbackDto> pageDto) {
        TenantAccess.bindSelf(pageDto);
        ResponseDto<PageDto<RdmsBugFeedbackDto>> responseDto = new ResponseDto<>();
        rdmsBugFeedbackService.listByManager(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

    @PostMapping("/getCharacterListIncludeNextLevelBySubprojectId/{subprojectId}")
    public ResponseDto<List<RdmsCharacterDto>> getCharacterListIncludeNextLevelBySubprojectId(@PathVariable String subprojectId) {
        ResponseDto<List<RdmsCharacterDto>> responseDto = new ResponseDto<>();
        List<String> subprojectIdList = rdmsSubprojectService.getSubProjectIdListIncludeNextLevel(subprojectId);
        List<RdmsCharacterDto> characterDtoList = new ArrayList<>();
        if(! CollectionUtils.isEmpty(subprojectIdList)){
            for(String subId: subprojectIdList){
                List<RdmsCharacterDto> characterList = rdmsCharacterService.getReleaseCharacterListBySubprojectId(subId);
                characterDtoList.addAll(characterList);
            }
        }
        List<RdmsCharacterDto> characterDtos = new ArrayList<>();
        if(!CollectionUtils.isEmpty(characterDtoList)){
            List<String> stringList = characterDtoList.stream().map(RdmsCharacterDto::getId).collect(Collectors.toList());
            List<String> stringList1 = stringList.stream().distinct().collect(Collectors.toList());
            characterDtos = characterDtoList.stream().filter(item -> stringList1.contains(item.getId())).collect(Collectors.toList());
        }
        responseDto.setContent(characterDtos);
        return responseDto;
    }

    @PostMapping("/getBugDetailInfo")
    public ResponseDto<RdmsBugFeedbackDto> getBugDetailInfo(@RequestParam String bugfeedbackId, @RequestParam String loginUserId) {
        ResponseDto<RdmsBugFeedbackDto> responseDto = new ResponseDto<>();
        RdmsBugFeedbackDto bugDetailInfo = rdmsBugFeedbackService.getBugDetailInfo(bugfeedbackId, loginUserId);
        responseDto.setContent(bugDetailInfo);
        return responseDto;
    }

    @PostMapping("/getProjectTreeListByCustomerId/{customerId}")
    public ResponseDto<List<RdmsHmiTree<RdmsHmiTreeInfoDto>>> getProjectTreeListByCustomerId(@PathVariable String customerId) {
        ResponseDto<List<RdmsHmiTree<RdmsHmiTreeInfoDto>>> responseDto = new ResponseDto<>();
        List<RdmsHmiTree<RdmsHmiTreeInfoDto>> projectTreeListByCustomerId = rdmsSubprojectService.getOnGoingProjectTreeListByCustomerId(customerId);
        if(!CollectionUtils.isEmpty(projectTreeListByCustomerId)){
            for(RdmsHmiTree<RdmsHmiTreeInfoDto> hmiTree: projectTreeListByCustomerId){
                hmiTree.setValue(hmiTree.getId());
                hmiTree.setParent(customerId);
            }
        }
        responseDto.setContent(projectTreeListByCustomerId);
        return responseDto;
    }

    @PostMapping("/save")
    @Transactional
    public ResponseDto<RdmsBugFeedbackDto> save(@RequestBody RdmsBugFeedbackDto bugFeedbackDto) {
        RichTextFields.sanitize(bugFeedbackDto);
        ResponseDto<RdmsBugFeedbackDto> responseDto = new ResponseDto<>();

        ValidatorUtil.require(bugFeedbackDto.getCustomerId(), "客户ID");
        ValidatorUtil.require(bugFeedbackDto.getWriterId(), "编制者");
        ValidatorUtil.require(bugFeedbackDto.getId(), "缺陷ID");
        ValidatorUtil.require(bugFeedbackDto.getSubprojectId(), "关联项目");
        ValidatorUtil.require(bugFeedbackDto.getName(), "缺陷标题");
        ValidatorUtil.require(bugFeedbackDto.getBugDescription(), "缺陷描述");
        ValidatorUtil.require(bugFeedbackDto.getPlanSubmissionTime(), "责成提交时间");

        ValidatorUtil.richLength(bugFeedbackDto.getBugDescription(), "缺陷描述", 10, 5000);
        ValidatorUtil.richLength(bugFeedbackDto.getTriggerCondition(), "触发条件/问题现象", 0, 5000);
        ValidatorUtil.length(bugFeedbackDto.getName(), "缺陷标题", 4, 50);

        RdmsBugFeedback rdmsBug = CopyUtil.copy(bugFeedbackDto, RdmsBugFeedback.class);
        RdmsProjectSubproject subproject = this.requireSubproject(bugFeedbackDto.getSubprojectId());
        rdmsBug.setProjectManagerId(subproject.getProjectManagerId());
        rdmsBug.setProductManagerId(subproject.getProductManagerId());
        rdmsBug.setTestManagerId(subproject.getTestManagerId());
        RdmsBugFeedback existing = this.existingForWriter(bugFeedbackDto.getId(), rdmsBug);
        String customerId = this.flowCustomerId(bugFeedbackDto.getCustomerId());
        String bizId = QualityFlowBridge.bugBizId(bugFeedbackDto.getId());
        if (qualityFlowBridge.engineForNew(customerId) && !qualityFlowBridge.engineForExisting(customerId, bizId)) {
            rdmsBug.setStatus(null);
            rdmsBugFeedbackService.save(rdmsBug);
            qualityFlowBridge.openSave(rdmsBug);
            responseDto.setContent(bugFeedbackDto);
            responseDto.setSuccess(true);
            responseDto.setMessage("保存成功");
            return responseDto;
        }
        if (qualityFlowBridge.engineForExisting(customerId, bizId)) {
            if (existing != null) {
                QualityDocGates.requireDraft(existing.getStatus());
            }
            rdmsBug.setStatus(rdmsBugFeedbackService.selectByPrimaryKey(bugFeedbackDto.getId()).getStatus());
            rdmsBugFeedbackService.save(rdmsBug);
            responseDto.setContent(bugFeedbackDto);
            responseDto.setSuccess(true);
            responseDto.setMessage("保存成功");
            return responseDto;
        }
        if (existing != null) {
            QualityDocGates.requireDraft(existing.getStatus());
        }
        rdmsBug.setStatus(QualityStatusEnum.SAVE.getStatus());
        rdmsBugFeedbackService.save(rdmsBug);


        responseDto.setContent(bugFeedbackDto);
        responseDto.setSuccess(true);
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    @PostMapping("/saveBugFeedbackInfo")
    @Transactional
    public ResponseDto<String> saveBugFeedbackInfo(@RequestBody RdmsBugFeedbackDto bugFeedbackDto) {
        RichTextFields.sanitize(bugFeedbackDto);
        ResponseDto<String> responseDto = new ResponseDto<>();

        ValidatorUtil.require(bugFeedbackDto.getId(), "缺陷ID");
        ValidatorUtil.require(bugFeedbackDto.getFeedbackDescription(), "反馈描述");
        ValidatorUtil.richLength(bugFeedbackDto.getFeedbackDescription(), "需求描述", 10, 5000);

        RdmsBugFeedback stored = this.requireBug(bugFeedbackDto.getId());
        jobActorGuard.assertActor(stored.getCustomerId(), null, stored.getTestManagerId());
        QualityDocGates.requireSource(stored.getStatus(), QualityDocGates.HANDLE_SOURCES);

        RdmsBugFeedback bugfeedback = new RdmsBugFeedback();
        bugfeedback.setId(bugFeedbackDto.getId());
        bugfeedback.setFeedbackDescription(bugFeedbackDto.getFeedbackDescription());
        bugfeedback.setFeedbackFileIdListStr(bugFeedbackDto.getFeedbackFileIdListStr());
        String id = rdmsBugFeedbackService.updateByPrimaryKeySelective(bugfeedback);



        responseDto.setContent(id);
        responseDto.setSuccess(true);
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    @PostMapping("/submitBugFeedbackInfo")
    @Transactional
    public ResponseDto<String> submitBugFeedbackInfo(@RequestBody RdmsBugFeedbackDto bugFeedbackDto) {
        RichTextFields.sanitize(bugFeedbackDto);
        ResponseDto<String> responseDto = new ResponseDto<>();

        ValidatorUtil.require(bugFeedbackDto.getId(), "缺陷ID");
        ValidatorUtil.require(bugFeedbackDto.getFeedbackDescription(), "反馈描述");
        ValidatorUtil.richLength(bugFeedbackDto.getFeedbackDescription(), "需求描述", 10, 5000);

        RdmsBugFeedback bugfeedback = new RdmsBugFeedback();
        bugfeedback.setId(bugFeedbackDto.getId());
        bugfeedback.setFeedbackDescription(bugFeedbackDto.getFeedbackDescription());
        bugfeedback.setFeedbackFileIdListStr(bugFeedbackDto.getFeedbackFileIdListStr());
        RdmsBugFeedback storedFeedback = this.requireBug(bugFeedbackDto.getId());
        jobActorGuard.assertActor(storedFeedback.getCustomerId(), null, storedFeedback.getTestManagerId());
        QualityDocGates.requireSource(storedFeedback.getStatus(), QualityDocGates.FEEDBACK_SOURCES);
        if (rdmsJobItemService.hasUnfinishedJobByQuality(storedFeedback.getId())) {
            throw new BusinessException(BusinessExceptionCode.QUALITY_JOB_NOT_COMPLETE);
        }
        if (qualityFlowBridge.engineForExisting(
                this.flowCustomerId(storedFeedback.getCustomerId()), QualityFlowBridge.bugBizId(bugFeedbackDto.getId()))) {
            String id = rdmsBugFeedbackService.updateByPrimaryKeySelective(bugfeedback);
            bizRichDocService.pinSubmitted(RichDocConst.BIZ_BUG, id, RichDocConst.PIN_STAGE_SUBMITTED);
            qualityFlowBridge.completeFeedback(bugFeedbackDto.getId(), true, this.operatorOr(storedFeedback.getTestManagerId()));
            responseDto.setContent(id);
            responseDto.setSuccess(true);
            responseDto.setMessage("保存成功");
            return responseDto;
        }
        bugfeedback.setStatus(QualityStatusEnum.RECHECK.getStatus());
        String id = rdmsBugFeedbackService.updateByPrimaryKeySelective(bugfeedback);
        bizRichDocService.pinSubmitted(RichDocConst.BIZ_BUG, id, RichDocConst.PIN_STAGE_SUBMITTED);



        responseDto.setContent(id);
        responseDto.setSuccess(true);
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    @PostMapping("/submit")
    @Transactional
    public ResponseDto<RdmsBugFeedbackDto> submit(@RequestBody RdmsBugFeedbackDto bugFeedbackDto) {
        RichTextFields.sanitize(bugFeedbackDto);
        ResponseDto<RdmsBugFeedbackDto> responseDto = new ResponseDto<>();

        ValidatorUtil.require(bugFeedbackDto.getCustomerId(), "客户ID");
        ValidatorUtil.require(bugFeedbackDto.getWriterId(), "编制者");
        ValidatorUtil.require(bugFeedbackDto.getId(), "缺陷ID");
        ValidatorUtil.require(bugFeedbackDto.getSubprojectId(), "关联项目");
        ValidatorUtil.require(bugFeedbackDto.getName(), "缺陷标题");
        ValidatorUtil.require(bugFeedbackDto.getBugDescription(), "缺陷描述");
        ValidatorUtil.require(bugFeedbackDto.getPlanSubmissionTime(), "责成提交时间");

        ValidatorUtil.richLength(bugFeedbackDto.getBugDescription(), "缺陷描述", 10, 5000);
        ValidatorUtil.richLength(bugFeedbackDto.getTriggerCondition(), "触发条件/问题现象", 0, 5000);
        ValidatorUtil.length(bugFeedbackDto.getName(), "缺陷标题", 4, 50);

        RdmsBugFeedback rdmsBug = CopyUtil.copy(bugFeedbackDto, RdmsBugFeedback.class);
        RdmsProjectSubproject subproject = this.requireSubproject(bugFeedbackDto.getSubprojectId());
        rdmsBug.setProjectManagerId(subproject.getProjectManagerId());
        rdmsBug.setProductManagerId(subproject.getProductManagerId());
        rdmsBug.setTestManagerId(subproject.getTestManagerId());
        RdmsBugFeedback existing = this.existingForWriter(bugFeedbackDto.getId(), rdmsBug);
        String customerId = this.flowCustomerId(bugFeedbackDto.getCustomerId());
        String bizId = QualityFlowBridge.bugBizId(bugFeedbackDto.getId());
        if (qualityFlowBridge.engineForExisting(customerId, bizId)) {
            if (existing != null) {
                QualityDocGates.requireDraft(existing.getStatus());
            }
            rdmsBug.setStatus(rdmsBugFeedbackService.selectByPrimaryKey(bugFeedbackDto.getId()).getStatus());
            rdmsBugFeedbackService.save(rdmsBug);
            qualityFlowBridge.completeSubmit(rdmsBug);
            bizRichDocService.pinSubmitted(RichDocConst.BIZ_BUG, rdmsBug.getId(), RichDocConst.PIN_STAGE_SUBMITTED);
            responseDto.setContent(bugFeedbackDto);
            responseDto.setSuccess(true);
            responseDto.setMessage("保存成功");
            return responseDto;
        }
        if (qualityFlowBridge.engineForNew(customerId)) {
            rdmsBug.setStatus(null);
            rdmsBugFeedbackService.save(rdmsBug);
            qualityFlowBridge.submitNew(rdmsBug);
            bizRichDocService.pinSubmitted(RichDocConst.BIZ_BUG, rdmsBug.getId(), RichDocConst.PIN_STAGE_SUBMITTED);
            responseDto.setContent(bugFeedbackDto);
            responseDto.setSuccess(true);
            responseDto.setMessage("保存成功");
            return responseDto;
        }
        if (existing != null) {
            QualityDocGates.requireDraft(existing.getStatus());
        }
        rdmsBug.setStatus(QualityStatusEnum.SUBMIT.getStatus());
        rdmsBugFeedbackService.save(rdmsBug);
        bizRichDocService.pinSubmitted(RichDocConst.BIZ_BUG, rdmsBug.getId(), RichDocConst.PIN_STAGE_SUBMITTED);


        responseDto.setContent(bugFeedbackDto);
        responseDto.setSuccess(true);
        responseDto.setMessage("保存成功");
        return responseDto;
    }


    @PostMapping("/saveAnalysis")
    @Transactional
    public ResponseDto<RdmsBugFeedbackDto> saveAnalysis(@RequestBody RdmsBugFeedbackDto bugFeedbackDto) {
        RichTextFields.sanitize(bugFeedbackDto);
        ResponseDto<RdmsBugFeedbackDto> responseDto = new ResponseDto<>();

        ValidatorUtil.require(bugFeedbackDto.getCustomerId(), "机构ID");
        ValidatorUtil.require(bugFeedbackDto.getWriterId(), "编制者");
        ValidatorUtil.require(bugFeedbackDto.getId(), "缺陷ID");
        ValidatorUtil.require(bugFeedbackDto.getSubprojectId(), "关联项目");
        ValidatorUtil.require(bugFeedbackDto.getName(), "缺陷标题");
        ValidatorUtil.require(bugFeedbackDto.getBugDescription(), "缺陷描述");
        ValidatorUtil.require(bugFeedbackDto.getAnalysisName(), "缺陷分析标题");
        ValidatorUtil.require(bugFeedbackDto.getEssentialAnalysis(), "缺陷问题本质分析");
        ValidatorUtil.require(bugFeedbackDto.getSolution(), "解决方案/处理措施");
        ValidatorUtil.require(bugFeedbackDto.getOutputIndicator(), "输出逻辑/交付指标");
        ValidatorUtil.require(bugFeedbackDto.getTestMethod(), "验证方法/测试方法");

        ValidatorUtil.richLength(bugFeedbackDto.getBugDescription(), "缺陷描述", 10, 5000);
        ValidatorUtil.richLength(bugFeedbackDto.getTriggerCondition(), "触发条件/问题现象", 0, 5000);
        ValidatorUtil.length(bugFeedbackDto.getAnalysisName(), "缺陷分析标题", 4, 50);
        ValidatorUtil.richLength(bugFeedbackDto.getEssentialAnalysis(), "缺陷问题本质分析", 10, 5000);
        ValidatorUtil.richLength(bugFeedbackDto.getSolution(), "解决方案/处理措施", 10, 5000);
        ValidatorUtil.richLength(bugFeedbackDto.getOutputIndicator(), "输出逻辑/交付指标", 10, 5000);
        ValidatorUtil.richLength(bugFeedbackDto.getTestMethod(), "验证方法/测试方法", 10, 5000);
        ValidatorUtil.length(bugFeedbackDto.getName(), "缺陷标题", 4, 50);

        boolean submitFlag = QualityStatusEnum.JOB_SUBMIT.getStatus().equals(bugFeedbackDto.getStatus());
        if (!submitFlag && !QualityStatusEnum.SAVE_ANALYSIS.getStatus().equals(bugFeedbackDto.getStatus())) {
            throw new BusinessException(BusinessExceptionCode.STATUS_VALUE_INVALID);
        }

        RdmsBugFeedback stored = this.requireBug(bugFeedbackDto.getId());
        QualityDocGates.requireSource(stored.getStatus(), QualityDocGates.HANDLE_SOURCES);

        RdmsJobItem handlingJob = rdmsJobItemService.resolveHandlingJobForActor(
                bugFeedbackDto.getId(), bugFeedbackDto.getJobitemId(), this.operatorOr(bugFeedbackDto.getLoginUserId()), JobItemTypeEnum.TASK_BUG);
        jobActorGuard.assertActor(stored.getCustomerId(), bugFeedbackDto.getLoginUserId(),
                handlingJob.getExecutorId(), stored.getTestManagerId());
        String jobitemId = handlingJob.getId();
        bugFeedbackDto.setJobitemId(jobitemId);

        RdmsBugFeedback rdmsBug = CopyUtil.copy(bugFeedbackDto, RdmsBugFeedback.class);
        rdmsBug.setBugSerial(stored.getBugSerial());
        rdmsBug.setCreateTime(stored.getCreateTime());
        rdmsBug.setCustomerId(stored.getCustomerId());
        rdmsBug.setWriterId(stored.getWriterId());
        rdmsBug.setProjectId(stored.getProjectId());
        rdmsBug.setSubprojectId(stored.getSubprojectId());
        rdmsBug.setProductManagerId(stored.getProductManagerId());
        rdmsBug.setProjectManagerId(stored.getProjectManagerId());
        rdmsBug.setTestManagerId(stored.getTestManagerId());
        String customerId = this.flowCustomerId(bugFeedbackDto.getCustomerId());
        String bizId = QualityFlowBridge.bugBizId(bugFeedbackDto.getId());
        boolean engine = qualityFlowBridge.engineForExisting(customerId, bizId);
        if (engine) {
            rdmsBug.setStatus(rdmsBugFeedbackService.selectByPrimaryKey(bugFeedbackDto.getId()).getStatus());
        } else {
            rdmsBug.setStatus(submitFlag ? QualityStatusEnum.JOB_SUBMIT.getStatus() : QualityStatusEnum.SAVE_ANALYSIS.getStatus());
        }
        rdmsBugFeedbackService.save(rdmsBug);
        if (submitFlag) {
            bizRichDocService.pinSubmitted(RichDocConst.BIZ_BUG, rdmsBug.getId(), RichDocConst.PIN_STAGE_SUBMITTED);
        }


        boolean jobEngine = submitFlag && jobFlowPort != null && jobFlowPort.engineForExisting(customerId, jobitemId);
        if (jobEngine) {
            jobFlowPort.submitWithRoute(rdmsJobItemService.selectByPrimaryKey(jobitemId),
                    this.operatorOr(bugFeedbackDto.getLoginUserId()), "缺陷处理工单提交", "BUG");
        } else {
            RdmsJobItem jobItem = new RdmsJobItem();
            jobItem.setId(jobitemId);
            if (submitFlag) {
                jobItem.setStatus(JobItemStatusEnum.SUBMIT.getStatus());
                jobItem.setActualSubmissionTime(new Date());
                RdmsJobItem storedJob = rdmsJobItemService.selectByPrimaryKey(jobitemId);
                if (storedJob != null && StringUtils.hasText(storedJob.getTestManagerId())) {
                    jobItem.setNextNode(storedJob.getTestManagerId());
                }
            } else {
                jobItem.setStatus(JobItemStatusEnum.HANDLING.getStatus());
            }
            jobItem.setUpdateTime(new Date());
            rdmsJobItemService.updateByPrimaryKeySelective(jobItem);
        }

        //保存附件等资产信息
        RdmsJobItemProperty jobItemProperty = new RdmsJobItemProperty();
        jobItemProperty.setJobItemId(jobitemId);
        jobItemProperty.setJobDescription("对缺陷问题进行分析说明");
        jobItemProperty.setFileListStr(bugFeedbackDto.getJobitemPropertyFileIdListStr());
        String propertyId = rdmsJobItemPropertyService.save(jobItemProperty);

        RdmsJobItem jobItem1 = new RdmsJobItem();
        jobItem1.setId(jobitemId);
        jobItem1.setPropertyId(propertyId);
        jobItem1.setUpdateTime(new Date());
        rdmsJobItemService.updateByPrimaryKeySelective(jobItem1);

        if (submitFlag && !jobEngine) {
            jobItemProcessRecorder.record(jobitemId, JobItemProcessStatusEnum.SUBMIT, bugFeedbackDto.getLoginUserId(),
                    stored.getTestManagerId(), "缺陷处理工单提交", null, null, null);
        }
        if (engine) {
            String node = qualityFlowBridge.currentNode(customerId, bizId);
            if ("analyze".equals(node)) {
                qualityFlowBridge.completeAnalysis(bugFeedbackDto.getId(), true, this.operatorOr(bugFeedbackDto.getLoginUserId()),
                        submitFlag ? QualityStatusEnum.JOB_SUBMIT.getStatus() : QualityStatusEnum.SAVE_ANALYSIS.getStatus());
            } else if (submitFlag) {
                RdmsBugFeedback statusOnly = new RdmsBugFeedback();
                statusOnly.setId(bugFeedbackDto.getId());
                statusOnly.setStatus(QualityStatusEnum.JOB_SUBMIT.getStatus());
                statusOnly.setUpdateTime(new Date());
                rdmsBugFeedbackService.updateByPrimaryKeySelective(statusOnly);
            }
        }

        responseDto.setContent(bugFeedbackDto);
        responseDto.setSuccess(true);
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    @PostMapping("/accept/{bugfeedbackId}")
    @Transactional
    public ResponseDto<String> accept(@PathVariable String bugfeedbackId) {
        ResponseDto<String> responseDto = new ResponseDto<>();
        RdmsBugFeedback stored = this.requireBug(bugfeedbackId);
        jobActorGuard.assertActor(stored.getCustomerId(), null, stored.getWriterId());
        if (qualityFlowBridge.engineForExisting(
                this.flowCustomerId(stored.getCustomerId()), QualityFlowBridge.bugBizId(bugfeedbackId))) {
            qualityFlowBridge.completeReview(bugfeedbackId, true, QualityStatusEnum.ARCHIVED.getStatus());
            responseDto.setContent(bugfeedbackId);
            responseDto.setSuccess(true);
            responseDto.setMessage("保存成功");
            return responseDto;
        }

        QualityDocGates.requireRecheck(stored.getStatus());
        RdmsBugFeedback rdmsBug = new RdmsBugFeedback();
        rdmsBug.setId(bugfeedbackId);
        rdmsBug.setStatus(QualityStatusEnum.ARCHIVED.getStatus());
        rdmsBug.setUpdateTime(new Date());
        rdmsBugFeedbackService.updateByPrimaryKeySelective(rdmsBug);

        responseDto.setContent(bugfeedbackId);
        responseDto.setSuccess(true);
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    @PostMapping("/reject/{bugfeedbackId}")
    @Transactional
    public ResponseDto<String> reject(@PathVariable String bugfeedbackId) {
        ResponseDto<String> responseDto = new ResponseDto<>();
        RdmsBugFeedback stored = this.requireBug(bugfeedbackId);
        jobActorGuard.assertActor(stored.getCustomerId(), null, stored.getWriterId());
        if (qualityFlowBridge.engineForExisting(
                this.flowCustomerId(stored.getCustomerId()), QualityFlowBridge.bugBizId(bugfeedbackId))) {
            qualityFlowBridge.completeReview(bugfeedbackId, true, QualityStatusEnum.REJECT.getStatus());
            responseDto.setContent(bugfeedbackId);
            responseDto.setSuccess(true);
            responseDto.setMessage("保存成功");
            return responseDto;
        }

        QualityDocGates.requireRecheck(stored.getStatus());
        RdmsBugFeedback rdmsBug = new RdmsBugFeedback();
        rdmsBug.setId(bugfeedbackId);
        rdmsBug.setStatus(QualityStatusEnum.REJECT.getStatus());
        rdmsBug.setUpdateTime(new Date());
        rdmsBugFeedbackService.updateByPrimaryKeySelective(rdmsBug);

        responseDto.setContent(bugfeedbackId);
        responseDto.setSuccess(true);
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    private RdmsBugFeedback requireBug(String bugfeedbackId) {
        RdmsBugFeedback stored = rdmsBugFeedbackService.selectByPrimaryKey(bugfeedbackId);
        if (stored == null || Integer.valueOf(1).equals(stored.getDeleted())) {
            throw new BusinessException(BusinessExceptionCode.DB_RECORD_NOT_EXIST);
        }
        TenantAccess.assertSameTenant(stored.getCustomerId());
        return stored;
    }

    private RdmsProjectSubproject requireSubproject(String subprojectId) {
        RdmsProjectSubproject subproject = rdmsSubprojectService.selectByPrimaryKey(subprojectId);
        if (subproject == null) {
            throw new BusinessException(BusinessExceptionCode.DB_RECORD_NOT_EXIST);
        }
        TenantAccess.assertSameTenant(subproject.getCustomerId());
        return subproject;
    }

    /** 已有单据只能由编制人再次保存或提交，编制人保持库中值 */
    private RdmsBugFeedback existingForWriter(String bugfeedbackId, RdmsBugFeedback target) {
        RdmsBugFeedback existing = rdmsBugFeedbackService.selectByPrimaryKey(bugfeedbackId);
        if (existing == null) {
            return null;
        }
        if (Integer.valueOf(1).equals(existing.getDeleted())) {
            throw new BusinessException(BusinessExceptionCode.DB_RECORD_NOT_EXIST);
        }
        TenantAccess.assertSameTenant(existing.getCustomerId());
        jobActorGuard.assertActor(existing.getCustomerId(), null, existing.getWriterId());
        target.setWriterId(existing.getWriterId());
        target.setCustomerId(existing.getCustomerId());
        return existing;
    }

    private String flowCustomerId(String fallback) {
        String customerId = TenantContext.getTenantId();
        return StringUtils.hasText(customerId) ? customerId : fallback;
    }

    private String operatorOr(String fallback) {
        String operatorId = TenantContext.getMembershipId();
        return StringUtils.hasText(operatorId) ? operatorId : fallback;
    }
}
