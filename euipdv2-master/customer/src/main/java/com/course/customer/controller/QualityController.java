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
import com.course.server.enums.rdms.*;
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
@RequestMapping("/quality")
public class QualityController {
    @Resource
    private BizRichDocService bizRichDocService;
    private static final Logger LOG = LoggerFactory.getLogger(QualityController.class);
    public static final String BUSINESS_NAME = "质量管理";

    @Resource
    private RdmsQualityService rdmsQualityService;
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

    @PostMapping("/getQualityRecheckNumByUserId/{userId}")
    public ResponseDto<Long> getQualityRecheckNumByUserId(@PathVariable String userId) {
        userId = TenantAccess.resolveMemberId(userId);
        ResponseDto<Long> responseDto = new ResponseDto<>();
        long testManageNum = rdmsQualityService.getQualityRecheckNumByUserId(userId);
        responseDto.setContent(testManageNum);
        return responseDto;
    }

    @PostMapping("/getQualityDefineSubmitNumByPdm/{userId}")
    public ResponseDto<Long> getQualityDefineSubmitNumByPdm(@PathVariable String userId) {
        userId = TenantAccess.resolveMemberId(userId);
        ResponseDto<Long> responseDto = new ResponseDto<>();
        long qualityItemNum = rdmsQualityService.countQualityDefineSubmitNumByPdm(userId);
        responseDto.setContent(qualityItemNum);
        return responseDto;
    }

    @PostMapping("/getQualityAndSubmitJobitems/{qualityId}")
    public ResponseDto<RdmsQualityDto> getQualityAndSubmitJobitems(@PathVariable String qualityId) {
        ResponseDto<RdmsQualityDto> responseDto = new ResponseDto<>();
        RdmsQualityDto qualityAndSubmitJobitems = rdmsQualityService.getQualityAndSubmitJobitems(qualityId);
        responseDto.setContent(qualityAndSubmitJobitems);
        return responseDto;
    }

    @PostMapping("/getQualityCostListByProjectId/{projectId}")
    public ResponseDto<List<RdmsQualityDto>> getQualityCostListByProjectId(@PathVariable String projectId) {
        ResponseDto<List<RdmsQualityDto>> responseDto = new ResponseDto<>();
        List<RdmsQualityDto> qualityCostListByProjectId = rdmsQualityService.getQualityCostListByProjectId(projectId);
        responseDto.setContent(qualityCostListByProjectId);
        return responseDto;
    }

    @PostMapping("/getQualityHasListFlag/{userId}")
    public ResponseDto<Boolean> getQualityHasListFlag(@PathVariable String userId) {
        userId = TenantAccess.resolveMemberId(userId);
        ResponseDto<Boolean> responseDto = new ResponseDto<>();
        long l = rdmsQualityService.countQualityJobNumByPdm(userId);
        responseDto.setContent(l >0);
        return responseDto;
    }

    @PostMapping("/getQualityAndAnalysisJobList/{qualityId}")
    public ResponseDto<RdmsQualityDto> getCharacterAndJobitemList(@PathVariable String qualityId) {
        ResponseDto<RdmsQualityDto> responseDto = new ResponseDto<>();
        RdmsQualityDto qualityAndAnalysisJobList = rdmsQualityService.getQualityAndAnalysisJobList(qualityId);
        responseDto.setContent(qualityAndAnalysisJobList);
        return responseDto;
    }

    @PostMapping("/list")
    public ResponseDto<PageDto<RdmsQualityDto>> list(@RequestBody PageDto<RdmsQualityDto> pageDto) {
        TenantAccess.bindSelf(pageDto);
        ResponseDto<PageDto<RdmsQualityDto>> responseDto = new ResponseDto<>();
        rdmsQualityService.list(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

    @PostMapping("/listByProjectId")
    public ResponseDto<PageDto<RdmsQualityDto>> listByProjectId(@RequestBody PageDto<RdmsQualityDto> pageDto) {
        ResponseDto<PageDto<RdmsQualityDto>> responseDto = new ResponseDto<>();
        rdmsQualityService.listByProjectId(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

    @PostMapping("/listByProjectId_handling")
    public ResponseDto<PageDto<RdmsQualityDto>> listByProjectId_handling(@RequestBody PageDto<RdmsQualityDto> pageDto) {
        ResponseDto<PageDto<RdmsQualityDto>> responseDto = new ResponseDto<>();
        rdmsQualityService.listByProjectId_handling(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

    @PostMapping("/getQualityAndJobitemList/{qualityId}")
    public ResponseDto<RdmsQualityDto> getQualityAndJobitemList(@PathVariable String qualityId) {
        ResponseDto<RdmsQualityDto> responseDto = new ResponseDto<>();
        RdmsQualityDto qualityAndJobitemList = rdmsQualityService.getQualityAndJobitemList(qualityId);
        responseDto.setContent(qualityAndJobitemList);
        return responseDto;
    }


    @PostMapping("/listQualityByPjm")
    public ResponseDto<PageDto<RdmsQualityDto>> listQualityByPjm(@RequestBody PageDto<RdmsQualityDto> pageDto) {
        TenantAccess.bindSelf(pageDto);
        ResponseDto<PageDto<RdmsQualityDto>> responseDto = new ResponseDto<>();
        rdmsQualityService.listQualityByPjm(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

    @PostMapping("/listQualityByPdm")
    public ResponseDto<PageDto<RdmsQualityDto>> listQualityByPdm(@RequestBody PageDto<RdmsQualityDto> pageDto) {
        TenantAccess.bindSelf(pageDto);
        ResponseDto<PageDto<RdmsQualityDto>> responseDto = new ResponseDto<>();
        rdmsQualityService.listQualityByPdm(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

    @PostMapping("/listQualityByCharacterId")
    public ResponseDto<List<RdmsQualityDto>> listQualityByCharacterId(@RequestParam String characterId, String loginUserId) {
        ResponseDto<List<RdmsQualityDto>> responseDto = new ResponseDto<>();
        List<RdmsQualityDto> rdmsQualityDtos = rdmsQualityService.listQualityByCharacterId(characterId, loginUserId);
        responseDto.setContent(rdmsQualityDtos);
        return responseDto;
    }

    @PostMapping("/listByManager")
    public ResponseDto<PageDto<RdmsQualityDto>> listByManager(@RequestBody PageDto<RdmsQualityDto> pageDto) {
        TenantAccess.bindSelf(pageDto);
        ResponseDto<PageDto<RdmsQualityDto>> responseDto = new ResponseDto<>();
        rdmsQualityService.listByManager(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

    @PostMapping("/getProjectTreeListByCustomerId/{customerId}")
    public ResponseDto<List<RdmsHmiTree<RdmsHmiTreeInfoDto>>> getProjectTreeListByCustomerId(@PathVariable String customerId) {
        ResponseDto<List<RdmsHmiTree<RdmsHmiTreeInfoDto>>> responseDto = new ResponseDto<>();
        List<RdmsHmiTree<RdmsHmiTreeInfoDto>> projectTreeListByCustomerId = rdmsSubprojectService.getCompleteProjectTreeListByCustomerId(customerId);
        if(!CollectionUtils.isEmpty(projectTreeListByCustomerId)){
            for(RdmsHmiTree<RdmsHmiTreeInfoDto> hmiTree: projectTreeListByCustomerId){
                hmiTree.setValue(hmiTree.getId());
                hmiTree.setParent(customerId);
            }
        }
        responseDto.setContent(projectTreeListByCustomerId);
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

    @PostMapping("/getQualityDetailInfo")
    public ResponseDto<RdmsQualityDto> getQualityDetailInfo(@RequestParam String qualityId, String loginUserId) {
        ResponseDto<RdmsQualityDto> responseDto = new ResponseDto<>();
        RdmsQualityDto qualityDetailInfo = rdmsQualityService.getQualityDetailInfo(qualityId, loginUserId);
        responseDto.setContent(qualityDetailInfo);
        return responseDto;
    }

    @PostMapping("/save")
    @Transactional
    public ResponseDto<RdmsQualityDto> save(@RequestBody RdmsQualityDto qualityDto) {
        RichTextFields.sanitize(qualityDto);
        ResponseDto<RdmsQualityDto> responseDto = new ResponseDto<>();

        ValidatorUtil.require(qualityDto.getCustomerId(), "客户ID");
        ValidatorUtil.require(qualityDto.getWriterId(), "编制者");
        ValidatorUtil.require(qualityDto.getId(), "质量ID");
        ValidatorUtil.require(qualityDto.getSubprojectId(), "关联项目");
        ValidatorUtil.require(qualityDto.getName(), "质量标题");
        ValidatorUtil.require(qualityDto.getQualityDescription(), "问题描述");
//        ValidatorUtil.require(qualityDto.getAssociateCharacterId(), "问题描述");

        ValidatorUtil.richLength(qualityDto.getQualityDescription(), "问题描述", 10, 5000);
        ValidatorUtil.richLength(qualityDto.getTriggerCondition(), "触发条件/问题现象", 0, 5000);
        ValidatorUtil.length(qualityDto.getName(), "质量标题", 4, 50);

        RdmsQuality rdmsQuality = CopyUtil.copy(qualityDto, RdmsQuality.class);
        RdmsProjectSubproject subproject = this.requireSubproject(qualityDto.getSubprojectId());
        rdmsQuality.setProjectManagerId(subproject.getProjectManagerId());
        rdmsQuality.setProductManagerId(subproject.getProductManagerId());
        RdmsQuality existing = this.existingForWriter(qualityDto.getId(), rdmsQuality);
        String customerId = this.flowCustomerId(qualityDto.getCustomerId());
        String bizId = QualityFlowBridge.qualityBizId(qualityDto.getId());
        if (qualityFlowBridge.engineForNew(customerId) && !qualityFlowBridge.engineForExisting(customerId, bizId)) {
            rdmsQuality.setStatus(null);
            rdmsQualityService.save(rdmsQuality);
            qualityFlowBridge.openSave(rdmsQuality);
            responseDto.setContent(qualityDto);
            responseDto.setSuccess(true);
            responseDto.setMessage("保存成功");
            return responseDto;
        }
        if (qualityFlowBridge.engineForExisting(customerId, bizId)) {
            if (existing != null) {
                QualityDocGates.requireDraft(existing.getStatus());
            }
            rdmsQuality.setStatus(rdmsQualityService.selectByPrimaryKey(qualityDto.getId()).getStatus());
            rdmsQualityService.save(rdmsQuality);
            responseDto.setContent(qualityDto);
            responseDto.setSuccess(true);
            responseDto.setMessage("保存成功");
            return responseDto;
        }
        if (existing != null) {
            QualityDocGates.requireDraft(existing.getStatus());
        }
        rdmsQuality.setStatus(QualityStatusEnum.SAVE.getStatus());
        rdmsQualityService.save(rdmsQuality);


        responseDto.setContent(qualityDto);
        responseDto.setSuccess(true);
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    @PostMapping("/saveQualityFeedbackInfo")
    @Transactional
    public ResponseDto<String> saveQualityFeedbackInfo(@RequestBody RdmsQualityDto qualityDto) {
        RichTextFields.sanitize(qualityDto);
        ResponseDto<String> responseDto = new ResponseDto<>();

        ValidatorUtil.require(qualityDto.getId(), "质量ID");
        ValidatorUtil.require(qualityDto.getFeedbackDescription(), "反馈描述");
        ValidatorUtil.richLength(qualityDto.getFeedbackDescription(), "需求描述", 10, 5000);

        RdmsQuality stored = this.requireQuality(qualityDto.getId());
        jobActorGuard.assertActor(stored.getCustomerId(), null, stored.getProductManagerId());
        QualityDocGates.requireSource(stored.getStatus(), QualityDocGates.HANDLE_SOURCES);

        RdmsQuality quality = new RdmsQuality();
        quality.setId(qualityDto.getId());
        quality.setFeedbackDescription(qualityDto.getFeedbackDescription());
        quality.setFeedbackFileIdListStr(qualityDto.getFeedbackFileIdListStr());
        String id = rdmsQualityService.updateByPrimaryKeySelective(quality);



        responseDto.setContent(id);
        responseDto.setSuccess(true);
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    @PostMapping("/submitQualityFeedbackInfo")
    @Transactional
    public ResponseDto<String> submitQualityFeedbackInfo(@RequestBody RdmsQualityDto qualityDto) {
        RichTextFields.sanitize(qualityDto);
        ResponseDto<String> responseDto = new ResponseDto<>();

        ValidatorUtil.require(qualityDto.getId(), "质量ID");
        ValidatorUtil.require(qualityDto.getFeedbackDescription(), "反馈描述");
        ValidatorUtil.richLength(qualityDto.getFeedbackDescription(), "需求描述", 10, 5000);

        RdmsQuality quality = new RdmsQuality();
        quality.setId(qualityDto.getId());
        quality.setFeedbackDescription(qualityDto.getFeedbackDescription());
        quality.setFeedbackFileIdListStr(qualityDto.getFeedbackFileIdListStr());
        RdmsQuality storedFeedback = this.requireQuality(qualityDto.getId());
        jobActorGuard.assertActor(storedFeedback.getCustomerId(), null, storedFeedback.getProductManagerId());
        QualityDocGates.requireSource(storedFeedback.getStatus(), QualityDocGates.FEEDBACK_SOURCES);
        if (rdmsJobItemService.hasUnfinishedJobByQuality(storedFeedback.getId())) {
            throw new BusinessException(BusinessExceptionCode.QUALITY_JOB_NOT_COMPLETE);
        }
        if (qualityFlowBridge.engineForExisting(
                this.flowCustomerId(storedFeedback.getCustomerId()), QualityFlowBridge.qualityBizId(qualityDto.getId()))) {
            String id = rdmsQualityService.updateByPrimaryKeySelective(quality);
            bizRichDocService.pinSubmitted(RichDocConst.BIZ_QUALITY, id, RichDocConst.PIN_STAGE_SUBMITTED);
            qualityFlowBridge.completeFeedback(qualityDto.getId(), false, this.operatorOr(storedFeedback.getProductManagerId()));
            responseDto.setContent(id);
            responseDto.setSuccess(true);
            responseDto.setMessage("保存成功");
            return responseDto;
        }
        quality.setStatus(QualityStatusEnum.RECHECK.getStatus());
        String id = rdmsQualityService.updateByPrimaryKeySelective(quality);
        bizRichDocService.pinSubmitted(RichDocConst.BIZ_QUALITY, id, RichDocConst.PIN_STAGE_SUBMITTED);



        responseDto.setContent(id);
        responseDto.setSuccess(true);
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    @PostMapping("/submit")
    @Transactional
    public ResponseDto<RdmsQualityDto> submit(@RequestBody RdmsQualityDto qualityDto) {
        RichTextFields.sanitize(qualityDto);
        ResponseDto<RdmsQualityDto> responseDto = new ResponseDto<>();

        ValidatorUtil.require(qualityDto.getCustomerId(), "客户ID");
        ValidatorUtil.require(qualityDto.getWriterId(), "编制者");
        ValidatorUtil.require(qualityDto.getId(), "质量ID");
        ValidatorUtil.require(qualityDto.getSubprojectId(), "关联项目");
        ValidatorUtil.require(qualityDto.getName(), "质量标题");
        ValidatorUtil.require(qualityDto.getQualityDescription(), "问题描述");
//        ValidatorUtil.require(qualityDto.getAssociateCharacterId(), "关联功能");

        ValidatorUtil.richLength(qualityDto.getQualityDescription(), "问题描述", 10, 5000);
        ValidatorUtil.richLength(qualityDto.getTriggerCondition(), "触发条件/问题现象", 0, 5000);
        ValidatorUtil.length(qualityDto.getName(), "质量标题", 4, 50);

        RdmsQuality rdmsQuality = CopyUtil.copy(qualityDto, RdmsQuality.class);
        RdmsProjectSubproject subproject = this.requireSubproject(qualityDto.getSubprojectId());
        rdmsQuality.setProjectManagerId(subproject.getProjectManagerId());
        rdmsQuality.setProductManagerId(subproject.getProductManagerId());
        RdmsQuality existing = this.existingForWriter(qualityDto.getId(), rdmsQuality);
        String customerId = this.flowCustomerId(qualityDto.getCustomerId());
        String bizId = QualityFlowBridge.qualityBizId(qualityDto.getId());
        if (qualityFlowBridge.engineForExisting(customerId, bizId)) {
            if (existing != null) {
                QualityDocGates.requireDraft(existing.getStatus());
            }
            rdmsQuality.setStatus(rdmsQualityService.selectByPrimaryKey(qualityDto.getId()).getStatus());
            rdmsQualityService.save(rdmsQuality);
            qualityFlowBridge.completeSubmit(rdmsQuality);
            bizRichDocService.pinSubmitted(RichDocConst.BIZ_QUALITY, rdmsQuality.getId(), RichDocConst.PIN_STAGE_SUBMITTED);
            responseDto.setContent(qualityDto);
            responseDto.setSuccess(true);
            responseDto.setMessage("保存成功");
            return responseDto;
        }
        if (qualityFlowBridge.engineForNew(customerId)) {
            rdmsQuality.setStatus(null);
            rdmsQualityService.save(rdmsQuality);
            qualityFlowBridge.submitNew(rdmsQuality);
            bizRichDocService.pinSubmitted(RichDocConst.BIZ_QUALITY, rdmsQuality.getId(), RichDocConst.PIN_STAGE_SUBMITTED);
            responseDto.setContent(qualityDto);
            responseDto.setSuccess(true);
            responseDto.setMessage("保存成功");
            return responseDto;
        }
        if (existing != null) {
            QualityDocGates.requireDraft(existing.getStatus());
        }
        rdmsQuality.setStatus(QualityStatusEnum.SUBMIT.getStatus());
        rdmsQualityService.save(rdmsQuality);
        bizRichDocService.pinSubmitted(RichDocConst.BIZ_QUALITY, rdmsQuality.getId(), RichDocConst.PIN_STAGE_SUBMITTED);


        responseDto.setContent(qualityDto);
        responseDto.setSuccess(true);
        responseDto.setMessage("保存成功");
        return responseDto;
    }


    @PostMapping("/saveAnalysis")
    @Transactional
    public ResponseDto<RdmsQualityDto> saveAnalysis(@RequestBody RdmsQualityDto qualityDto) {
        RichTextFields.sanitize(qualityDto);
        ResponseDto<RdmsQualityDto> responseDto = new ResponseDto<>();

        ValidatorUtil.require(qualityDto.getCustomerId(), "客户ID");
        ValidatorUtil.require(qualityDto.getWriterId(), "编制者");
        ValidatorUtil.require(qualityDto.getId(), "质量ID");
        ValidatorUtil.require(qualityDto.getSubprojectId(), "关联项目");
        ValidatorUtil.require(qualityDto.getName(), "质量标题");
        ValidatorUtil.require(qualityDto.getQualityDescription(), "问题描述");
        ValidatorUtil.require(qualityDto.getAnalysisName(), "质量分析标题");
        ValidatorUtil.require(qualityDto.getEssentialAnalysis(), "质量问题本质分析");
        ValidatorUtil.require(qualityDto.getSolution(), "解决方案/处理措施");
        ValidatorUtil.require(qualityDto.getOutputIndicator(), "输出逻辑/交付指标");
        ValidatorUtil.require(qualityDto.getTestMethod(), "验证方法/测试方法");

        ValidatorUtil.richLength(qualityDto.getQualityDescription(), "需求描述", 10, 5000);
        ValidatorUtil.richLength(qualityDto.getTriggerCondition(), "触发条件/问题现象", 0, 5000);
        ValidatorUtil.length(qualityDto.getAnalysisName(), "质量分析标题", 4, 50);
        ValidatorUtil.richLength(qualityDto.getEssentialAnalysis(), "质量问题本质分析", 10, 5000);
        ValidatorUtil.richLength(qualityDto.getSolution(), "解决方案/处理措施", 10, 5000);
        ValidatorUtil.richLength(qualityDto.getOutputIndicator(), "输出逻辑/交付指标", 10, 5000);
        ValidatorUtil.richLength(qualityDto.getTestMethod(), "验证方法/测试方法", 10, 5000);
        ValidatorUtil.length(qualityDto.getName(), "质量标题", 4, 50);

        boolean submitFlag = QualityStatusEnum.JOB_SUBMIT.getStatus().equals(qualityDto.getStatus());
        if (!submitFlag && !QualityStatusEnum.SAVE_ANALYSIS.getStatus().equals(qualityDto.getStatus())) {
            throw new BusinessException(BusinessExceptionCode.STATUS_VALUE_INVALID);
        }

        RdmsQuality stored = this.requireQuality(qualityDto.getId());
        QualityDocGates.requireSource(stored.getStatus(), QualityDocGates.HANDLE_SOURCES);

        RdmsJobItem handlingJob = rdmsJobItemService.resolveHandlingJobForActor(
                qualityDto.getId(), qualityDto.getJobitemId(), this.operatorOr(qualityDto.getLoginUserId()), JobItemTypeEnum.QUALITY);
        jobActorGuard.assertActor(stored.getCustomerId(), qualityDto.getLoginUserId(),
                handlingJob.getExecutorId(), stored.getProductManagerId());
        String jobitemId = handlingJob.getId();
        qualityDto.setJobitemId(jobitemId);

        RdmsQuality rdmsQuality = CopyUtil.copy(qualityDto, RdmsQuality.class);
        rdmsQuality.setQualitySerial(stored.getQualitySerial());
        rdmsQuality.setCreateTime(stored.getCreateTime());
        rdmsQuality.setCustomerId(stored.getCustomerId());
        rdmsQuality.setWriterId(stored.getWriterId());
        rdmsQuality.setProjectId(stored.getProjectId());
        rdmsQuality.setSubprojectId(stored.getSubprojectId());
        rdmsQuality.setProductManagerId(stored.getProductManagerId());
        rdmsQuality.setProjectManagerId(stored.getProjectManagerId());
        String customerId = this.flowCustomerId(qualityDto.getCustomerId());
        String bizId = QualityFlowBridge.qualityBizId(qualityDto.getId());
        boolean engine = qualityFlowBridge.engineForExisting(customerId, bizId);
        if (engine) {
            rdmsQuality.setStatus(rdmsQualityService.selectByPrimaryKey(qualityDto.getId()).getStatus());
        } else {
            rdmsQuality.setStatus(submitFlag ? QualityStatusEnum.JOB_SUBMIT.getStatus() : QualityStatusEnum.SAVE_ANALYSIS.getStatus());
        }
        rdmsQualityService.save(rdmsQuality);
        if (submitFlag) {
            bizRichDocService.pinSubmitted(RichDocConst.BIZ_QUALITY, rdmsQuality.getId(), RichDocConst.PIN_STAGE_SUBMITTED);
        }

        boolean jobEngine = submitFlag && jobFlowPort != null && jobFlowPort.engineForExisting(customerId, jobitemId);
        if (jobEngine) {
            jobFlowPort.submitWithRoute(rdmsJobItemService.selectByPrimaryKey(jobitemId),
                    this.operatorOr(qualityDto.getLoginUserId()), "质量处理工单提交", "QUALITY");
        } else {
            RdmsJobItem jobItem = new RdmsJobItem();
            jobItem.setId(jobitemId);
            if (submitFlag) {
                jobItem.setStatus(JobItemStatusEnum.SUBMIT.getStatus());
                jobItem.setActualSubmissionTime(new Date());
                RdmsJobItem storedJob = rdmsJobItemService.selectByPrimaryKey(jobitemId);
                if (storedJob != null && StringUtils.hasText(storedJob.getProductManagerId())) {
                    jobItem.setNextNode(storedJob.getProductManagerId());
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
        jobItemProperty.setJobDescription("对质量问题进行分析说明");
        jobItemProperty.setFileListStr(qualityDto.getJobitemPropertyFileIdListStr());
        String propertyId = rdmsJobItemPropertyService.save(jobItemProperty);

        RdmsJobItem jobItem1 = new RdmsJobItem();
        jobItem1.setId(jobitemId);
        jobItem1.setPropertyId(propertyId);
        jobItem1.setUpdateTime(new Date());
        rdmsJobItemService.updateByPrimaryKeySelective(jobItem1);

        if (submitFlag && !jobEngine) {
            RdmsJobItem rdmsJobItem = rdmsJobItemService.selectByPrimaryKey(jobItem1.getId());
            jobItemProcessRecorder.record(jobitemId, JobItemProcessStatusEnum.SUBMIT, qualityDto.getLoginUserId(),
                    rdmsJobItem.getProductManagerId(), "质量处理工单提交", null, null, null);
        }
        if (engine) {
            String node = qualityFlowBridge.currentNode(customerId, bizId);
            if ("analyze".equals(node)) {
                qualityFlowBridge.completeAnalysis(qualityDto.getId(), false, this.operatorOr(qualityDto.getLoginUserId()),
                        submitFlag ? QualityStatusEnum.JOB_SUBMIT.getStatus() : QualityStatusEnum.SAVE_ANALYSIS.getStatus());
            } else if (submitFlag) {
                RdmsQuality statusOnly = new RdmsQuality();
                statusOnly.setId(qualityDto.getId());
                statusOnly.setStatus(QualityStatusEnum.JOB_SUBMIT.getStatus());
                statusOnly.setUpdateTime(new Date());
                rdmsQualityService.updateByPrimaryKeySelective(statusOnly);
            }
        }

        responseDto.setContent(qualityDto);
        responseDto.setSuccess(true);
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    @PostMapping("/accept/{qualityId}")
    @Transactional
    public ResponseDto<String> accept(@PathVariable String qualityId) {
        ResponseDto<String> responseDto = new ResponseDto<>();
        RdmsQuality stored = this.requireQuality(qualityId);
        jobActorGuard.assertActor(stored.getCustomerId(), null, stored.getWriterId());
        if (qualityFlowBridge.engineForExisting(
                this.flowCustomerId(stored.getCustomerId()), QualityFlowBridge.qualityBizId(qualityId))) {
            qualityFlowBridge.completeReview(qualityId, false, QualityStatusEnum.ARCHIVED.getStatus());
            responseDto.setContent(qualityId);
            responseDto.setSuccess(true);
            responseDto.setMessage("保存成功");
            return responseDto;
        }

        QualityDocGates.requireRecheck(stored.getStatus());
        RdmsQuality rdmsQuality = new RdmsQuality();
        rdmsQuality.setId(qualityId);
        rdmsQuality.setStatus(QualityStatusEnum.ARCHIVED.getStatus());
        rdmsQuality.setUpdateTime(new Date());
        rdmsQualityService.updateByPrimaryKeySelective(rdmsQuality);

        responseDto.setContent(qualityId);
        responseDto.setSuccess(true);
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    @PostMapping("/reject/{qualityId}")
    @Transactional
    public ResponseDto<String> reject(@PathVariable String qualityId) {
        ResponseDto<String> responseDto = new ResponseDto<>();
        RdmsQuality stored = this.requireQuality(qualityId);
        jobActorGuard.assertActor(stored.getCustomerId(), null, stored.getWriterId());
        if (qualityFlowBridge.engineForExisting(
                this.flowCustomerId(stored.getCustomerId()), QualityFlowBridge.qualityBizId(qualityId))) {
            qualityFlowBridge.completeReview(qualityId, false, QualityStatusEnum.REJECT.getStatus());
            responseDto.setContent(qualityId);
            responseDto.setSuccess(true);
            responseDto.setMessage("保存成功");
            return responseDto;
        }

        QualityDocGates.requireRecheck(stored.getStatus());
        RdmsQuality rdmsQuality = new RdmsQuality();
        rdmsQuality.setId(qualityId);
        rdmsQuality.setStatus(QualityStatusEnum.REJECT.getStatus());
        rdmsQuality.setUpdateTime(new Date());
        rdmsQualityService.updateByPrimaryKeySelective(rdmsQuality);

        responseDto.setContent(qualityId);
        responseDto.setSuccess(true);
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    private RdmsQuality requireQuality(String qualityId) {
        RdmsQuality stored = rdmsQualityService.selectByPrimaryKey(qualityId);
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
    private RdmsQuality existingForWriter(String qualityId, RdmsQuality target) {
        RdmsQuality existing = rdmsQualityService.selectByPrimaryKey(qualityId);
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
