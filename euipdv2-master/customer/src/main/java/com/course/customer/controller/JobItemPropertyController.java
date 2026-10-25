/**
 * Author：周朔鹏
 * 编制时间：2025-08-05

 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */

package com.course.customer.controller;

import com.alibaba.fastjson.JSON;
import com.course.server.domain.*;
import com.course.server.dto.ResponseDto;
import com.course.server.dto.rdms.*;
import com.course.server.enums.rdms.*;
import com.course.server.exception.BusinessException;
import com.course.server.exception.BusinessExceptionCode;
import com.course.server.exception.ValidatorException;
import com.course.server.service.rdms.*;
import com.course.server.service.rdms.flow.JobFlowPort;
import com.course.server.service.rdms.helper.TenantParentGuard;
import com.course.server.service.rdms.rich.BizRichDocService;
import com.course.server.service.rdms.rich.RichDocConst;
import com.course.server.util.CopyUtil;
import com.course.server.util.ValidatorUtil;
import com.course.server.util.RichTextFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/jobitem-property")
public class JobItemPropertyController {
    @Resource
    private BizRichDocService bizRichDocService;
    private static final Logger LOG = LoggerFactory.getLogger(JobItemPropertyController.class);
    public static final String BUSINESS_NAME = "工单资产";


    @Resource
    private RdmsJobItemPropertyService rdmsJobItemPropertyService;
    @Resource
    private RdmsFileService rdmsFileService;
    @Resource
    private RdmsJobItemService rdmsJobItemService;
    @Resource
    private JobItemProcessRecorder jobItemProcessRecorder;
    @Resource
    private RdmsJobItemReviewService rdmsJobItemReviewService;
    @Resource
    private RdmsJobItemFlowService rdmsJobItemFlowService;
    @Resource
    private JobActorGuard jobActorGuard;
    @Resource
    private JobFlowPort jobFlowPort;
    @Resource
    private RdmsSubprojectService rdmsSubprojectService;
    @Resource
    private RdmsFileAuthService rdmsFileAuthService;
    @Autowired
    private RdmsJobitemAssociateService rdmsJobitemAssociateService;
    @Autowired
    private RdmsBossService rdmsBossService;
    @Autowired
    private RdmsPreProjectService rdmsPreProjectService;
    @Autowired
    private RdmsTgmService rdmsTgmService;
    @Autowired
    private RdmsSysgmService rdmsSysgmService;
    @Resource
    private TenantParentGuard tenantParentGuard;

    /**
     *
     */
    @PostMapping("/getJobItemProperty")
    public ResponseDto<RdmsJobItemPropertyDto> getJobItemProperty(@RequestParam String jobitemId, String loginUserId) {
        tenantParentGuard.requireJobItem(jobitemId);
        ResponseDto<RdmsJobItemPropertyDto> responseDto = new ResponseDto<>();
        List<RdmsJobItemProperty> jobItemPropertyByJobItemId = rdmsJobItemPropertyService.getJobItemPropertyByJobItemId(jobitemId);
        if (!CollectionUtils.isEmpty(jobItemPropertyByJobItemId)) {
            List<RdmsJobItemPropertyDto> rdmsJobItemPropertyDtos = CopyUtil.copyList(jobItemPropertyByJobItemId, RdmsJobItemPropertyDto.class);
            if (!CollectionUtils.isEmpty(rdmsJobItemPropertyDtos)) {
                RdmsJobItemPropertyDto propertyDto = rdmsJobItemPropertyDtos.get(0);
                if (!ObjectUtils.isEmpty(propertyDto)) {
                    {
                        if (!ObjectUtils.isEmpty(propertyDto.getFileListStr())) {
                            List<String> stringList = JSON.parseArray(propertyDto.getFileListStr(), String.class);
                            if (!CollectionUtils.isEmpty(stringList)) {
                                List<RdmsFileDto> fileDtos = new ArrayList<>();
                                for (String id : stringList) {
                                    RdmsFileDto fileDto = rdmsFileService.getFileSimpleRecordInfo(id);
                                    //读取访问权限
                                    if (!ObjectUtils.isEmpty(fileDto)) {
                                        rdmsFileAuthService.fillAuthStatus(fileDto, loginUserId);
                                    }
                                    fileDtos.add(fileDto);
                                }
                                propertyDto.setFileItems(fileDtos);
                            }
                        }
                    }
                    {
                        if (!ObjectUtils.isEmpty(propertyDto.getReferenceFileListStr())) {
                            List<String> stringList = JSON.parseArray(propertyDto.getReferenceFileListStr(), String.class);
                            if (!CollectionUtils.isEmpty(stringList)) {
                                List<RdmsFileDto> fileDtos = new ArrayList<>();
                                for (String id : stringList) {
                                    RdmsFileDto fileDto = rdmsFileService.getFileSimpleRecordInfo(id);
                                    //读取访问权限
                                    if (!ObjectUtils.isEmpty(fileDto)) {
                                        rdmsFileAuthService.fillAuthStatus(fileDto, loginUserId);
                                    }
                                    fileDtos.add(fileDto);
                                }
                                propertyDto.setReferenceFileList(fileDtos);
                            }
                        }
                    }
                }
                responseDto.setContent(propertyDto);
            }
            return responseDto;
        }
        responseDto.setContent(null);
        return responseDto;
    }


    /**
     * 保存客户信息
     */
    @PostMapping("/save")
    @Transactional
    public ResponseDto<RdmsJobItemPropertyDto> save(@RequestBody RdmsJobItemPropertyDto propertyDto) {
        RichTextFields.sanitize(propertyDto);
        ResponseDto<RdmsJobItemPropertyDto> responseDto = new ResponseDto<>();

        ValidatorUtil.require(propertyDto.getJobItemId(), "工单ID");
        ValidatorUtil.require(propertyDto.getJobDescription(), "工作描述");
        ValidatorUtil.require(propertyDto.getTestKeyPoints(), "测试关键点说明");

        ValidatorUtil.richLength(propertyDto.getJobDescription(), "工作描述", 10, 5000);
        ValidatorUtil.richLength(propertyDto.getTestKeyPoints(), "测试关键点说明", 10, 5000);

        RdmsJobItem jobItem = tenantParentGuard.requireJobItem(propertyDto.getJobItemId());
        jobActorGuard.assertActor(jobItem.getCustomerId(), propertyDto.getLoginUserId(), jobItem.getExecutorId());

        RdmsJobItemProperty jobItemProperty = CopyUtil.copy(propertyDto, RdmsJobItemProperty.class);

        List<String> strings = JSON.parseArray(jobItemProperty.getFileListStr(), String.class);
        if (strings == null) {
            strings = new ArrayList<>();
        }
        HashSet<String> objects = new HashSet<>(strings);
        List<String> list = new ArrayList<String>(objects);
        String jsonString = JSON.toJSONString(list);
        jobItemProperty.setFileListStr(jsonString);

        rdmsJobItemPropertyService.save(jobItemProperty);

        responseDto.setContent(propertyDto);
        responseDto.setSuccess(true);
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    /**
     * 保存客户信息
     */
    @PostMapping("/saveJobitemProperty")
    @Transactional
    public ResponseDto<RdmsJobItemPropertyDto> saveJobitemProperty(@RequestBody RdmsJobItemPropertyDto propertyDto) {
        RichTextFields.sanitize(propertyDto);
        ResponseDto<RdmsJobItemPropertyDto> responseDto = new ResponseDto<>();

        ValidatorUtil.require(propertyDto.getJobItemId(), "工单ID");
        ValidatorUtil.require(propertyDto.getJobDescription(), "工作描述");
        ValidatorUtil.richLength(propertyDto.getJobDescription(), "工作描述", 10, 5000);

        RdmsJobItem jobItem = rdmsJobItemService.selectByPrimaryKey(propertyDto.getJobItemId());
        if (ObjectUtils.isEmpty(jobItem)) {
            throw new BusinessException(BusinessExceptionCode.DB_RECORD_NOT_EXIST);
        }
        LifecycleTransition.requireSource(jobItem.getStatus(), JobItemStatusEnum.propertySubmitSources(),
                BusinessExceptionCode.JOB_ITEM_STATUS_INVALID);
        jobActorGuard.assertActor(jobItem.getCustomerId(), propertyDto.getLoginUserId(), jobItem.getExecutorId());
        {
            if (!(jobItem.getType().equals(JobItemTypeEnum.REVIEW.getType())
                    || jobItem.getType().equals(JobItemTypeEnum.REVIEW_COO.getType())
                    || jobItem.getType().equals(JobItemTypeEnum.REVIEW_SUBP.getType())
                    || jobItem.getType().equals(JobItemTypeEnum.REVIEW_MILESTONE.getType())
                    || jobItem.getType().equals(JobItemTypeEnum.REVIEW_PRO.getType()))) {
                ValidatorUtil.require(propertyDto.getTestKeyPoints(), "测试关键点说明");
                ValidatorUtil.richLength(propertyDto.getTestKeyPoints(), "测试关键点说明", 10, 5000);
            }
        }

        RdmsJobItemProperty jobItemProperty = CopyUtil.copy(propertyDto, RdmsJobItemProperty.class);
        String propertyId = rdmsJobItemPropertyService.save(jobItemProperty);

        jobItem.setPropertyId(propertyId);
        rdmsJobItemService.update(jobItem);

        responseDto.setContent(propertyDto);
        responseDto.setSuccess(true);
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    /**
     * 保存客户信息
     */
    @PostMapping("/submitJobitemProperty")
    @Transactional
    public ResponseDto submitJobitemProperty(@RequestBody RdmsJobItemPropertyDto propertyDto) {
        RichTextFields.sanitize(propertyDto);
        ResponseDto responseDto = new ResponseDto<>();

        ValidatorUtil.require(propertyDto.getJobItemId(), "工单ID");
        ValidatorUtil.require(propertyDto.getJobDescription(), "工作描述");
        ValidatorUtil.richLength(propertyDto.getJobDescription(), "工作描述", 10, 5000);

        rdmsJobItemFlowService.submitProperty(propertyDto);
        bizRichDocService.pinSubmitted(RichDocConst.BIZ_JOBITEM_PROPERTY, propertyDto.getJobItemId(), RichDocConst.PIN_SUBMITTED);

        responseDto.setSuccess(true);
        responseDto.setMessage("提交成功");
        return responseDto;
    }


    /**
     * 提交评审结果
     * <p>
     * 执行人与NextNode
     * 1. 工单
     * 工单的执行人(executor)就是工单的提交(submit)人,也就是说谁提交谁就是执行人. 这个概念在评审工单的处理中,评审工单的最初状态就是 submit 所以,executor是当前的项目经理
     * nextNode是下一步的执行人,这里就是评审组长
     * <p>
     * 评审组长：
     * 1) 里程碑评审 	    组长：主项目经理 项目进度类评审
     * 2) 子项目评审	    组长：产品经理 可以协同系统工程部门一同参会
     * 3) 特性评审		组长：产品经理 可以协调系统工程部门一同参会
     * 4) 主项目评审	    组长：监管委员 最终验收
     * <p>
     * 2. 工单过程
     * 工单过程执行人是 当前的登录用户
     * 工单过程的nextNode和对应的工单的nextNode相同
     * <p>
     * 业务实现见 {@link com.course.server.service.rdms.RdmsJobItemReviewService#submitReviewResult}
     */
    @PostMapping("/submitReviewResult")
    @Transactional
    public ResponseDto submitReviewResult(@RequestBody RdmsJobItemPropertyDto propertyDto) {
        RichTextFields.sanitize(propertyDto);
        ResponseDto responseDto = new ResponseDto<>();
        rdmsJobItemReviewService.submitReviewResult(propertyDto);
        responseDto.setSuccess(true);
        responseDto.setMessage("提交成功");
        return responseDto;
    }

    @PostMapping("/submitJobitemTestResult")
    @Transactional
    public ResponseDto submitJobitemTestResult(@RequestBody RdmsJobItemPropertyDto propertyDto) {
        RichTextFields.sanitize(propertyDto);
        ResponseDto responseDto = new ResponseDto<>();

        ValidatorUtil.require(propertyDto.getJobItemId(), "工单ID");
        ValidatorUtil.require(propertyDto.getJobDescription(), "工作描述");
        ValidatorUtil.require(propertyDto.getTestKeyPoints(), "测试关键点说明");
        ValidatorUtil.require(propertyDto.getJobEvaluate(), "测试评价");
        ValidatorUtil.require(propertyDto.getJobEvaluate().getEvaluateScore(), "测试评分");
        ValidatorUtil.require(propertyDto.getJobEvaluate().getEvaluateDesc(), "测试评价");

        ValidatorUtil.richLength(propertyDto.getJobDescription(), "工作描述", 10, 5000);
        ValidatorUtil.richLength(propertyDto.getTestKeyPoints(), "测试关键点说明", 10, 5000);
        ValidatorUtil.length(propertyDto.getJobEvaluate().getEvaluateDesc(), "测试评价", 2, 500);
        if (propertyDto.getJobEvaluate().getEvaluateScore() < 1 || propertyDto.getJobEvaluate().getEvaluateScore() > 10) {
            throw new ValidatorException("测试评分须在 1 至 10 之间");
        }

        RdmsJobItem currentTestJob = rdmsJobItemService.selectByPrimaryKey(propertyDto.getJobItemId());
        if (ObjectUtils.isEmpty(currentTestJob)) {
            throw new BusinessException(BusinessExceptionCode.DB_RECORD_NOT_EXIST);
        }
        if (!JobItemStatusEnum.HANDLING.getStatus().equals(currentTestJob.getStatus())) {
            throw new BusinessException(BusinessExceptionCode.LIFECYCLE_TRANSITION_FORBIDDEN);
        }
        jobActorGuard.assertActor(currentTestJob.getCustomerId(), propertyDto.getLoginUserId(), currentTestJob.getExecutorId());
        List<RdmsJobItemDto> bugJobs = rdmsJobItemService.getJobitemByParentJobitemId(currentTestJob.getId(), JobItemTypeEnum.BUG);
        boolean bugAllCompleted = bugJobs.stream()
                .allMatch(item -> JobItemStatusEnum.closedStatuses().contains(item.getStatus()));
        if (!bugAllCompleted) {
            throw new BusinessException(BusinessExceptionCode.TEST_BUG_NOT_COMPLETE);
        }

        //将测试评价写入原工单
        RdmsHmiJobEvaluateDto jobEvaluateDto = propertyDto.getJobEvaluate();
        RdmsJobItem testedJobItem = ObjectUtils.isEmpty(currentTestJob.getTestedJobitemId())
                ? null : rdmsJobItemService.selectByPrimaryKey(currentTestJob.getTestedJobitemId());
        if (ObjectUtils.isEmpty(testedJobItem)) {
            throw new BusinessException(BusinessExceptionCode.DB_RECORD_NOT_EXIST);
        }
        if (JobItemStatusEnum.SUBMIT.getStatus().equals(testedJobItem.getStatus())
                || JobItemStatusEnum.TESTING.getStatus().equals(testedJobItem.getStatus())) {
            testedJobItem.setEvaluateScore(jobEvaluateDto.getEvaluateScore());
            testedJobItem.setEvaluateDesc(jobEvaluateDto.getEvaluateDesc());
            rdmsJobItemService.update(testedJobItem); //此处外产生过程变更, 所以没有过程记录
        }

        //处理当前测试工单
        RdmsJobItemProperty jobItemProperty = CopyUtil.copy(propertyDto, RdmsJobItemProperty.class);
        String propertyId = rdmsJobItemPropertyService.save(jobItemProperty);

        RdmsJobItem jobItem = rdmsJobItemService.selectByPrimaryKey(propertyDto.getJobItemId());
        jobItem.setPropertyId(propertyId);

        if (ObjectUtils.isEmpty(jobItem.getSubprojectId())) {
            jobItem.setSubprojectId(testedJobItem.getSubprojectId());
        }

        RdmsProjectSubproject subproject = ObjectUtils.isEmpty(jobItem.getSubprojectId())
                ? null : rdmsSubprojectService.selectByPrimaryKey(jobItem.getSubprojectId());
        if (subproject == null) {
            throw new BusinessException(BusinessExceptionCode.DB_RECORD_NOT_EXIST);
        }
        if (jobFlowPort != null && jobFlowPort.engineForExisting(jobItem.getCustomerId(), jobItem.getId())) {
            jobItem.setTestManagerId(subproject.getTestManagerId());
            rdmsJobItemService.update(jobItem);
            jobFlowPort.submitTest(jobItem, jobItem.getExecutorId());
            bizRichDocService.pinSubmitted(RichDocConst.BIZ_JOBITEM_PROPERTY, jobItem.getId(), RichDocConst.PIN_SUBMITTED);
            responseDto.setSuccess(true);
            responseDto.setMessage("保存成功");
            return responseDto;
        }
        jobItem.setNextNode(subproject.getTestManagerId());
        jobItem.setStatus(JobItemStatusEnum.SUBMIT.getStatus());
        jobItem.setActualSubmissionTime(new Date());
        String jobitemId = rdmsJobItemService.update(jobItem);

        jobItemProcessRecorder.record(jobitemId, JobItemProcessStatusEnum.SUBMIT, jobItem.getExecutorId(), subproject.getTestManagerId(),
                "提交测试任务", null, jobItem.getActualSubmissionTime(), null);
        bizRichDocService.pinSubmitted(RichDocConst.BIZ_JOBITEM_PROPERTY, jobitemId, RichDocConst.PIN_SUBMITTED);
        responseDto.setSuccess(true);
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    /**
     * 删除
     */
    @DeleteMapping("/del/{id}")
    public ResponseDto delete(@PathVariable String id) {
        ResponseDto responseDto = new ResponseDto();
        RdmsJobItemProperty stored = rdmsJobItemPropertyService.selectByPrimaryKey(id);
        if (stored == null) {
            throw new BusinessException(BusinessExceptionCode.DB_RECORD_NOT_EXIST);
        }
        tenantParentGuard.requireJobItem(stored.getJobItemId());
        rdmsJobItemPropertyService.delete(id);
        return responseDto;
    }

}
