/**
 * Author：周朔鹏
 * 编制时间：2025-08-05

 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */

package com.course.customer.controller;

import com.course.customer.flow.FeeFlowBridge;
import com.course.server.security.tenant.TenantAccess;
import com.course.customer.flow.FlowDiagramService;
import com.course.server.domain.*;
import com.course.server.dto.PageDto;
import com.course.server.dto.ResponseDto;
import com.course.server.dto.rdms.FlowDiagramDto;
import com.course.server.dto.rdms.RdmsFeeManageDto;
import com.course.server.enums.rdms.*;
import com.course.server.exception.BusinessException;
import com.course.server.exception.BusinessExceptionCode;
import com.course.server.security.tenant.TenantContext;
import com.course.server.service.rdms.*;
import com.course.server.service.rdms.flow.FeeApproverClassifier;
import com.course.server.service.rdms.flow.FlowBizCode;
import com.course.server.util.CopyUtil;
import com.course.server.util.ValidatorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/fee")
public class FeeController {
    private static final Logger LOG = LoggerFactory.getLogger(FeeController.class);
    public static final String BUSINESS_NAME = "费用管理";

    @Resource
    private RdmsFeeManageService rdmsFeeManageService;
    @Resource
    private RdmsFeeProcessService rdmsFeeProcessService;
    @Resource
    private RdmsCustomerUserService rdmsCustomerUserService;
    @Resource
    private RdmsSubprojectService rdmsSubprojectService;
    @Resource
    private RdmsCharacterService rdmsCharacterService;
    @Resource
    private RdmsJobItemService rdmsJobItemService;
    @Resource
    private ApplicationBudgetExeWriter applicationBudgetExeWriter;
    @Resource
    private RdmsManhourService rdmsManhourService;
    @Resource
    private FeeApproverClassifier feeApproverClassifier;
    @Resource
    private FeeFlowBridge feeFlowBridge;
    @Resource
    private JobActorGuard jobActorGuard;
    @Resource
    private FlowDiagramService flowDiagramService;

    @PostMapping("/getQualityFeeApplicationNumByUserId/{userId}")
    public ResponseDto<Long> getQualityFeeApplicationNumByUserId(@PathVariable String userId) {
        userId = TenantAccess.resolveMemberId(userId);
        ResponseDto<Long> responseDto = new ResponseDto<>();
        Long applicationNumByUserId = rdmsFeeManageService.getApplicationNumByUserId(userId);
        responseDto.setContent(applicationNumByUserId);
        return responseDto;
    }

    /**
     * 保存客户信息
     */
    @PostMapping("/saveFeeApplication")
    @Transactional
    public ResponseDto<String> saveFeeApplication(@RequestBody RdmsFeeManageDto feeManageDto) {
        ResponseDto<String> responseDto = new ResponseDto<>();

        ValidatorUtil.require(feeManageDto.getJobitemId(), "工单Id");
        RdmsJobItem rdmsJobItem = rdmsJobItemService.selectByPrimaryKey(feeManageDto.getJobitemId());
        if(ObjectUtils.isEmpty(rdmsJobItem)){
            throw new BusinessException(BusinessExceptionCode.DB_RECORD_NOT_EXIST);
        }
        TenantAccess.assertSameTenant(rdmsJobItem.getCustomerId());
        this.resolveFeeApprover(feeManageDto, rdmsJobItem);
        feeManageDto.setId(null);
        feeManageDto.setWriterId(this.resolveOperatorId(feeManageDto.getWriterId()));
        if (!ObjectUtils.isEmpty(feeManageDto.getCode()) && rdmsFeeManageService.codeExists(feeManageDto.getCode())) {
            throw new BusinessException(BusinessExceptionCode.DB_RECORD_EXIST);
        }

        ValidatorUtil.require(feeManageDto.getProjectType(), "项目类型");
        if(feeManageDto.getProjectType().equals(ProjectTypeEnum.PRE_PROJECT.getType())){
            ValidatorUtil.require(feeManageDto.getPreProjectId(), "项目Id");
        }else{
            ValidatorUtil.require(feeManageDto.getProjectId(), "项目Id");
            ValidatorUtil.require(feeManageDto.getSubprojectId(), "项目Id");
        }

        ValidatorUtil.require(feeManageDto.getCode(), "申请单号");
        ValidatorUtil.require(feeManageDto.getName(), "申请标题");
        ValidatorUtil.require(feeManageDto.getCustomerId(), "公司Id");
        ValidatorUtil.require(feeManageDto.getAuxStatus(), "费用归类");
        if(!feeManageDto.getAuxStatus().equals(DocTypeEnum.PRE_PROJECT.getType())){
            ValidatorUtil.require(feeManageDto.getProjectId(), "项目Id");
            ValidatorUtil.require(feeManageDto.getSubprojectId(), "项目Id");
        }
        ValidatorUtil.require(feeManageDto.getAppliedAmount(), "申请金额");
        ValidatorUtil.require(feeManageDto.getReason(), "领用事由");
        ValidatorUtil.require(feeManageDto.getWriterId(), "填表人");
        ValidatorUtil.require(feeManageDto.getNextNode(), "下一节点");
        ValidatorUtil.require(feeManageDto.getType(), "费用科目");

        ValidatorUtil.length(feeManageDto.getName(), "申请标题", 2, 30);
        ValidatorUtil.length(feeManageDto.getReason(), "领用事由", 6, 800);
        ValidatorUtil.length(feeManageDto.getType(), "费用科目", 2, 20);
        if(!BudgetClassifyStdEnum.isFeeSubject(feeManageDto.getType())){
            throw new BusinessException(BusinessExceptionCode.INPUT_ERROR, "费用科目取值错误");
        }
        if(ObjectUtils.isEmpty(feeManageDto.getAppliedAmount()) || feeManageDto.getAppliedAmount().signum() <= 0){
            throw new BusinessException(BusinessExceptionCode.INPUT_ERROR, "申请金额须大于 0");
        }

        if(!(rdmsJobItem.getType().equals(JobItemTypeEnum.QUALITY.getType())
                || rdmsJobItem.getType().equals(JobItemTypeEnum.TASK_BUG.getType()) || JobItemTypeEnum.TASK_BUG.getType().equals(rdmsJobItem.getAuxType())
                || rdmsJobItem.getType().equals(JobItemTypeEnum.TASK_SUBP.getType())
                || rdmsJobItem.getType().equals(JobItemTypeEnum.ASSIST.getType())
                || rdmsJobItem.getType().equals(JobItemTypeEnum.TEST.getType())
                || rdmsJobItem.getType().equals(JobItemTypeEnum.TASK_FUNCTION.getType())
                || rdmsJobItem.getType().equals(JobItemTypeEnum.TASK_PRODUCT.getType())
        )){
            ValidatorUtil.require(feeManageDto.getCharacterId(), "功能特性Id");
        }
        if (feeFlowBridge.engineForNew(this.flowCustomerId(feeManageDto))) {
            String id = feeFlowBridge.submitApplication(feeManageDto, rdmsJobItem);
            responseDto.setContent(id);
            responseDto.setMessage("保存成功");
            return responseDto;
        }
        //保存申请表
        RdmsFeeManage feeManage = CopyUtil.copy(feeManageDto, RdmsFeeManage.class);
        feeManage.setStatus(ApplicationStatusEnum.APPLICATION.getStatus());
        feeManage.setJobType(rdmsJobItem.getType());
        feeManage.setApproverId(feeManageDto.getNextNode());  //记录审批人,是为了在后续单据上正确显示审批人
        feeManage.setAccountCost(feeManage.getAppliedAmount());  //使用申请金额进行临时记账
        this.checkFeeBudget(feeManage, feeManage.getAppliedAmount());
        rdmsFeeManageService.save(feeManage);

        //填写一条过程记录
        RdmsFeeProcess feeProcess = new RdmsFeeProcess();
        feeProcess.setFeeId(feeManage.getId());
        feeProcess.setExecutorId(feeManageDto.getWriterId());
        feeProcess.setDeep(1);
        feeProcess.setDescription("提交费用申请");
        feeProcess.setNextNode(feeManage.getNextNode());
        feeProcess.setSubmitTime(new Date());
        feeProcess.setStatus(ApplicationStatusEnum.APPLICATION.getStatus());
        rdmsFeeProcessService.save(feeProcess);

        responseDto.setContent(feeManage.getId());
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    /**
     * 保存客户信息
     */
    @PostMapping("/approveFeeApplication")
    @Transactional
    public ResponseDto<String> approveFeeApplication(@RequestBody RdmsFeeManageDto feeManageDto) {
        ResponseDto<String> responseDto = new ResponseDto<>();
        ValidatorUtil.require(feeManageDto.getCode(), "申请单号");
        ValidatorUtil.require(feeManageDto.getApproveDescription(), "审批意见");
        ValidatorUtil.require(feeManageDto.getCapitalSource(), "资金来源");
        if(CapitalSourceEnum.getCapitalSourceEnumBySource(feeManageDto.getCapitalSource()) == null){
            throw new BusinessException(BusinessExceptionCode.INPUT_ERROR, "资金来源取值错误");
        }
        ValidatorUtil.length(feeManageDto.getApproveDescription(), "审批描述", 2, 500);
        ValidatorUtil.require(feeManageDto.getApproveStatus(), "审批结论");

        String approveStatus = feeManageDto.getApproveStatus();
        String requiredStatus;
        if(ApplicationStatusEnum.APPROVED.getStatus().equals(approveStatus) || ApplicationStatusEnum.REJECT.getStatus().equals(approveStatus)){
            requiredStatus = ApplicationStatusEnum.APPLICATION.getStatus();
        }else if(ApplicationStatusEnum.PRE_COMPLETE.getStatus().equals(approveStatus) || ApplicationStatusEnum.SUPPLEMENT.getStatus().equals(approveStatus)){
            requiredStatus = ApplicationStatusEnum.SUBMIT.getStatus();
        }else{
            throw new BusinessException(BusinessExceptionCode.STATUS_VALUE_INVALID);
        }
        RdmsFeeManageDto current = rdmsFeeManageService.getFeeManageSimpleInfoByCode(feeManageDto.getCode());
        if(!requiredStatus.equals(current.getStatus())){
            throw new BusinessException(BusinessExceptionCode.LIFECYCLE_TRANSITION_FORBIDDEN);
        }
        String operatorId = this.resolveOperatorId(feeManageDto.getLoginUserId());
        if (feeFlowBridge.engineForExisting(this.flowCustomerId(current), current.getCode())) {
            feeFlowBridge.approve(feeManageDto, current, operatorId);
            responseDto.setContent(current.getId());
            responseDto.setMessage("保存成功");
            return responseDto;
        }
        String expectedOperatorId = current.getNextNode();
        if(ApplicationStatusEnum.SUBMIT.getStatus().equals(current.getStatus()) && Objects.equals(expectedOperatorId, current.getWriterId())){
            //提交报销改写下一节点之前形成的 SUBMIT 单据，下一节点仍是申请人，由原审批人复核
            expectedOperatorId = current.getApproverId();
        }
        if(!operatorId.equals(expectedOperatorId)){
            throw new BusinessException(BusinessExceptionCode.NO_PERMISSION);
        }

        //保存申请表: 请求只取审批意见与资金来源, 其余以库内记录为准
        RdmsFeeManage feeManage = CopyUtil.copy(current, RdmsFeeManage.class);
        feeManage.setApproveDescription(feeManageDto.getApproveDescription());
        feeManage.setCapitalSource(feeManageDto.getCapitalSource());
        feeManage.setNextNode(current.getWriterId());  //提交之后, 出现在谁的页面, 那么, 下一节点就是谁
        feeManage.setStatus(approveStatus);
        if(ApplicationStatusEnum.PRE_COMPLETE.getStatus().equals(approveStatus) && !ObjectUtils.isEmpty(current.getActualCost())){
            feeManage.setAccountCost(current.getActualCost());  //复核通过才按实际费用记账；退回增补保留原记账金额
        }
        feeManage.setApproveTime(new Date());
        if(ApplicationStatusEnum.APPROVED.getStatus().equals(approveStatus)){
            this.checkFeeBudget(feeManage, current.getAppliedAmount());
        }else if(ApplicationStatusEnum.PRE_COMPLETE.getStatus().equals(approveStatus) && !ObjectUtils.isEmpty(current.getActualCost())){
            //SUBMIT 单据已按原记账金额计入余额，只校验核定金额的增量
            BigDecimal booked = ObjectUtils.isEmpty(current.getAccountCost()) ? BigDecimal.ZERO : current.getAccountCost();
            this.checkFeeBudget(feeManage, current.getActualCost().subtract(booked));
        }
        rdmsFeeManageService.updateByPrimaryKeySelective(feeManage);

        //项目费用 → 统一写入 research_exe; 驳回不形成执行流水
        if(ApplicationStatusEnum.REJECT.getStatus().equals(approveStatus)){
            applicationBudgetExeWriter.removeFeeUnpaidExe(feeManage.getCode());
        }else{
            RdmsFeeManageDto exeDto = CopyUtil.copy(feeManage, RdmsFeeManageDto.class);
            exeDto.setApproveStatus(approveStatus);
            applicationBudgetExeWriter.writeFeeApprove(exeDto, feeManage);
        }

        //填写一条过程记录
        RdmsFeeProcess feeProcess = new RdmsFeeProcess();
        feeProcess.setFeeId(feeManage.getId());
        feeProcess.setExecutorId(operatorId);
        long feeProcessCount = rdmsFeeProcessService.getFeeProcessCount(feeManage.getId());
        feeProcess.setDeep((int)feeProcessCount);
        feeProcess.setDescription("批准申请");
        feeProcess.setNextNode(feeManage.getNextNode());
        feeProcess.setSubmitTime(new Date());
        feeProcess.setStatus(approveStatus);
        rdmsFeeProcessService.save(feeProcess);

        responseDto.setContent(feeManage.getId());
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    @PostMapping("/listFeesByCharacterId")
    public ResponseDto<PageDto<RdmsFeeManageDto>> listFeesByCharacterId(@RequestBody PageDto<RdmsFeeManageDto> pageDto) {
        ResponseDto<PageDto<RdmsFeeManageDto>> responseDto = new ResponseDto<>();
        rdmsFeeManageService.listByCharacterId(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

    @PostMapping("/getListByCharacterId/{characterId}")
    public ResponseDto<List<RdmsFeeManageDto>> getListByCharacterId(@PathVariable String characterId) {
        ResponseDto<List<RdmsFeeManageDto>> responseDto = new ResponseDto<>();
        List<RdmsFeeManageDto> listByCharacterId = rdmsFeeManageService.getListByCharacterId(characterId);
        responseDto.setContent(listByCharacterId);
        return responseDto;
    }

    @PostMapping("/getListBySubprojectId/{subprojectId}")
    public ResponseDto<List<RdmsFeeManageDto>> getListBySubprojectId(@PathVariable String subprojectId) {
        ResponseDto<List<RdmsFeeManageDto>> responseDto = new ResponseDto<>();
        List<RdmsFeeManageDto> listBySubprojectId = rdmsFeeManageService.getListBySubprojectId(subprojectId);
        responseDto.setContent(listBySubprojectId);
        return responseDto;
    }

    @PostMapping("/getListByPreProjectId/{preprojectId}")
    public ResponseDto<List<RdmsFeeManageDto>> getListByPreProjectId(@PathVariable String preprojectId) {
        ResponseDto<List<RdmsFeeManageDto>> responseDto = new ResponseDto<>();
        List<RdmsFeeManageDto> listByPreprojectId = rdmsFeeManageService.getListByPreProjectId(preprojectId);
        responseDto.setContent(listByPreprojectId);
        return responseDto;
    }

    @PostMapping("/getListByProjectId/{projectId}")
    public ResponseDto<List<RdmsFeeManageDto>> getListByProjectId(@PathVariable String projectId) {
        ResponseDto<List<RdmsFeeManageDto>> responseDto = new ResponseDto<>();
        List<RdmsFeeManageDto> listByPreprojectId = rdmsFeeManageService.getListByProjectId(projectId);
        responseDto.setContent(listByPreprojectId);
        return responseDto;
    }

    @PostMapping("/getListByQualityId/{qualityId}")
    public ResponseDto<List<RdmsFeeManageDto>> getListByQualityId(@PathVariable String qualityId) {
        ResponseDto<List<RdmsFeeManageDto>> responseDto = new ResponseDto<>();
        List<RdmsFeeManageDto> listByJobitemId = rdmsFeeManageService.getListByQualityId(qualityId);
        responseDto.setContent(listByJobitemId);

        return responseDto;
    }


    @PostMapping("/getFeeApplicationListByWriterId/{writerId}")
    public ResponseDto<List<RdmsFeeManageDto>> getFeeApplicationListByWriterId(@PathVariable String writerId) {
        writerId = TenantAccess.resolveMemberId(writerId);
        ResponseDto<List<RdmsFeeManageDto>> responseDto = new ResponseDto<>();
        List<RdmsFeeManageDto> listByWriterId = rdmsFeeManageService.getFeeApplicationListByWriterId(writerId);
        responseDto.setContent(listByWriterId);
        return responseDto;
    }

    @PostMapping("/getFeeApplicationNum/{userId}")
    public ResponseDto<Integer> getFeeApplicationNum(@PathVariable String userId) {
        userId = TenantAccess.resolveMemberId(userId);
        ResponseDto<Integer> responseDto = new ResponseDto<>();
        Integer feeApplicationNum = rdmsFeeManageService.getFeeApplicationNum(userId);
        responseDto.setContent(feeApplicationNum);
        return responseDto;
    }

    @PostMapping("/getFeeApplicationNum_project/{userId}")
    public ResponseDto<Integer> getFeeApplicationNum_project(@PathVariable String userId) {
        userId = TenantAccess.resolveMemberId(userId);
        ResponseDto<Integer> responseDto = new ResponseDto<>();
        Integer feeApplicationNum = rdmsFeeManageService.getFeeApplicationNum_project(userId);
        responseDto.setContent(feeApplicationNum);
        return responseDto;
    }

    @PostMapping("/getFeeApplicationByCode/{code}")
    public ResponseDto<RdmsFeeManageDto> getFeeApplicationByCode(@PathVariable String code) {
        ResponseDto<RdmsFeeManageDto> responseDto = new ResponseDto<>();
        RdmsFeeManageDto feeApplicationByCode = rdmsFeeManageService.getFeeApplicationByCode(code);
        RdmsCustomerUser rdmsCustomerUser = rdmsCustomerUserService.selectByPrimaryKey(feeApplicationByCode.getWriterId());
        feeApplicationByCode.setWriterName(rdmsCustomerUser.getTrueName());

        if(!ObjectUtils.isEmpty(feeApplicationByCode.getNextNode())){
            RdmsCustomerUser rdmsCustomerUser1 = rdmsCustomerUserService.selectByPrimaryKey(feeApplicationByCode.getNextNode());
            feeApplicationByCode.setNextNodeName(rdmsCustomerUser1.getTrueName());
        }
        if(!ObjectUtils.isEmpty(feeApplicationByCode.getApproverId())){
            RdmsCustomerUser rdmsCustomerUser2 = rdmsCustomerUserService.selectByPrimaryKey(feeApplicationByCode.getApproverId());
            feeApplicationByCode.setApproverName(rdmsCustomerUser2.getTrueName());  //项目经理
        }

        if(!ObjectUtils.isEmpty(feeApplicationByCode.getCharacterId())){
            RdmsCharacter rdmsCharacter = rdmsCharacterService.selectByPrimaryKey(feeApplicationByCode.getCharacterId());
            feeApplicationByCode.setCharacterName(rdmsCharacter.getCharacterName());
        }
        if(!ObjectUtils.isEmpty(feeApplicationByCode.getJobitemId())){
            RdmsJobItem rdmsJobItem = rdmsJobItemService.selectByPrimaryKey(feeApplicationByCode.getJobitemId());
            feeApplicationByCode.setJobitemName(rdmsJobItem.getJobName());
        }
        responseDto.setContent(feeApplicationByCode);
        return responseDto;
    }

    @PostMapping("/confirmCancel/{code}")
    @Transactional
    public ResponseDto<String> confirmCancel(@PathVariable String code) {
        ResponseDto<String> responseDto = new ResponseDto<>();
        RdmsFeeManageDto feeApplication = rdmsFeeManageService.getFeeManageSimpleInfoByCode(code);
        if(ApplicationStatusEnum.CANCEL.getStatus().equals(feeApplication.getStatus())){
            responseDto.setContent(feeApplication.getId());
            return responseDto;
        }
        String cancelOperatorId = this.resolveOperatorId(feeApplication.getWriterId());
        if (feeFlowBridge.engineForExisting(this.flowCustomerId(feeApplication), feeApplication.getCode())) {
            boolean proxy = this.assertWriterOrProxy(feeApplication, cancelOperatorId);
            feeFlowBridge.cancel(feeApplication, cancelOperatorId, proxy);
            responseDto.setContent(feeApplication.getId());
            return responseDto;
        }
        this.assertWriterAction(feeApplication, ApplicationStatusEnum.REJECT);
        feeApplication.setStatus(ApplicationStatusEnum.CANCEL.getStatus());
        RdmsFeeManage copy = CopyUtil.copy(feeApplication, RdmsFeeManage.class);
        String s = rdmsFeeManageService.update(copy);
        applicationBudgetExeWriter.removeFeeUnpaidExe(feeApplication.getCode());

        //填写一条过程记录
        RdmsFeeProcess feeProcess = new RdmsFeeProcess();
        feeProcess.setFeeId(feeApplication.getId());
        feeProcess.setExecutorId(cancelOperatorId);
        long feeProcessCount = rdmsFeeProcessService.getFeeProcessCount(feeApplication.getId());
        feeProcess.setDeep((int)feeProcessCount);
        feeProcess.setDescription("费用申请取消");
        feeProcess.setNextNode(null);
        feeProcess.setSubmitTime(new Date());
        feeProcess.setStatus(ApplicationStatusEnum.CANCEL.getStatus());
        rdmsFeeProcessService.save(feeProcess);

        responseDto.setContent(s);
        return responseDto;
    }
    @PostMapping("/updateFeeApproveFileListStr")
    public ResponseDto<String> updateFeeApproveFileListStr(@RequestBody RdmsFeeManageDto feeManageDto) {
        ResponseDto<String> responseDto = new ResponseDto<>();
        if(!ObjectUtils.isEmpty(feeManageDto.getApproveFileListStr())){
            RdmsFeeManage copy = CopyUtil.copy(feeManageDto, RdmsFeeManage.class);
            String s = rdmsFeeManageService.updateFeeApproveFileListStr(copy);
            responseDto.setContent(s);
        }else{
            responseDto.setContent(null);
        }
        return responseDto;
    }
    @PostMapping("/updateFeeDealWithFileListStr")
    public ResponseDto<String> updateFeeDealWithFileListStr(@RequestBody RdmsFeeManageDto feeManageDto) {
        ResponseDto<String> responseDto = new ResponseDto<>();
        if(!ObjectUtils.isEmpty(feeManageDto.getApproveFileListStr())){
            RdmsFeeManage copy = CopyUtil.copy(feeManageDto, RdmsFeeManage.class);
            String s = rdmsFeeManageService.updateFeeDealWithFileListStr(copy);
            responseDto.setContent(s);
        }else{
            responseDto.setContent(null);
        }
        return responseDto;
    }
    @PostMapping("/updateFeeCompleteFileListStr")
    public ResponseDto<String> updateFeeCompleteFileListStr(@RequestBody RdmsFeeManageDto feeManageDto) {
        ResponseDto<String> responseDto = new ResponseDto<>();
        if(!ObjectUtils.isEmpty(feeManageDto.getApproveFileListStr())){
            RdmsFeeManage copy = CopyUtil.copy(feeManageDto, RdmsFeeManage.class);
            String s = rdmsFeeManageService.updateFeeCompleteFileListStr(copy);
            responseDto.setContent(s);
        }else{
            responseDto.setContent(null);
        }
        return responseDto;
    }
    @PostMapping("/submit")
    @Transactional
    public ResponseDto<String> submit(@RequestBody RdmsFeeManageDto feeManageDto) {
        ValidatorUtil.require(feeManageDto.getCode(), "申请单号");
        ValidatorUtil.require(feeManageDto.getLoginUserId(), "当前用户");
        ValidatorUtil.require(feeManageDto.getSubmitDescription(), "提交说明");
        ValidatorUtil.require(feeManageDto.getActualCost(), "实际费用");
        ValidatorUtil.length(feeManageDto.getType(), "费用类型", 2, 20);
        if (feeManageDto.getActualCost().signum() < 0) {
            throw new BusinessException(BusinessExceptionCode.INPUT_ERROR);
        }

        ResponseDto<String> responseDto = new ResponseDto<>();
        RdmsFeeManageDto feeApplication = rdmsFeeManageService.getFeeApplicationByCode(feeManageDto.getCode());
        if(!(ApplicationStatusEnum.APPROVED.getStatus().equals(feeApplication.getStatus())
                || ApplicationStatusEnum.SUPPLEMENT.getStatus().equals(feeApplication.getStatus())
                || ApplicationStatusEnum.SUBMIT.getStatus().equals(feeApplication.getStatus()))){
            throw new BusinessException(BusinessExceptionCode.LIFECYCLE_TRANSITION_FORBIDDEN);
        }
        String operatorId = this.resolveOperatorId(feeManageDto.getLoginUserId());
        boolean proxy = this.assertWriterOrProxy(feeApplication, operatorId);
        if (feeFlowBridge.engineForExisting(this.flowCustomerId(feeApplication), feeApplication.getCode())) {
            feeFlowBridge.submit(feeManageDto, feeApplication, operatorId, proxy);
            responseDto.setContent(feeApplication.getCode());
            return responseDto;
        }
        feeApplication.setStatus(ApplicationStatusEnum.SUBMIT.getStatus());
        if(!ObjectUtils.isEmpty(feeApplication.getApproverId())){
            feeApplication.setNextNode(feeApplication.getApproverId());  //待复核单据出现在审批人页面
        }
        feeApplication.setActualCost(feeManageDto.getActualCost());
        feeApplication.setSubmitDescription(feeManageDto.getSubmitDescription());
        RdmsFeeManage copy = CopyUtil.copy(feeApplication, RdmsFeeManage.class);
        rdmsFeeManageService.update(copy);

        //填写一条过程记录
        RdmsFeeProcess feeProcess = new RdmsFeeProcess();
        feeProcess.setFeeId(feeApplication.getId());
        feeProcess.setExecutorId(operatorId);
        long feeProcessCount = rdmsFeeProcessService.getFeeProcessCount(feeApplication.getId());
        feeProcess.setDeep((int)feeProcessCount);
        feeProcess.setDescription("费用处理完成, 提交审批");
//        RdmsProjectSubproject subproject = rdmsSubprojectService.selectByPrimaryKey(feeApplication.getSubprojectId());
        feeProcess.setNextNode(feeApplication.getNextNode());
        feeProcess.setSubmitTime(new Date());
        feeProcess.setStatus(ApplicationStatusEnum.SUBMIT.getStatus());
        rdmsFeeProcessService.save(feeProcess);

        responseDto.setContent(feeApplication.getCode());
        return responseDto;
    }

    @PostMapping("/complete/{code}")
    @Transactional
    public ResponseDto<String> complete(@PathVariable String code) {
        ValidatorUtil.require(code, "申请单号");

        ResponseDto<String> responseDto = new ResponseDto<>();
        RdmsFeeManageDto feeApplication = rdmsFeeManageService.getFeeApplicationByCode(code);
        if(ApplicationStatusEnum.COMPLETE.getStatus().equals(feeApplication.getStatus())){
            responseDto.setContent(feeApplication.getCode());
            return responseDto;
        }
        String archiveOperatorId = this.resolveOperatorId(feeApplication.getWriterId());
        if (feeFlowBridge.engineForExisting(this.flowCustomerId(feeApplication), feeApplication.getCode())) {
            boolean proxy = this.assertWriterOrProxy(feeApplication, archiveOperatorId);
            feeFlowBridge.archive(feeApplication, archiveOperatorId, proxy);
            responseDto.setContent(feeApplication.getCode());
            return responseDto;
        }
        this.assertWriterAction(feeApplication, ApplicationStatusEnum.PRE_COMPLETE);
        feeApplication.setStatus(ApplicationStatusEnum.COMPLETE.getStatus());
        RdmsFeeManage copy = CopyUtil.copy(feeApplication, RdmsFeeManage.class);
        rdmsFeeManageService.update(copy);

        //填写一条过程记录
        RdmsFeeProcess feeProcess = new RdmsFeeProcess();
        feeProcess.setFeeId(feeApplication.getId());
        feeProcess.setExecutorId(archiveOperatorId);
        long feeProcessCount = rdmsFeeProcessService.getFeeProcessCount(feeApplication.getId());
        feeProcess.setDeep((int)feeProcessCount);
        feeProcess.setDescription("归档");
        feeProcess.setNextNode(null);
        feeProcess.setSubmitTime(new Date());
        feeProcess.setStatus(ApplicationStatusEnum.COMPLETE.getStatus());
        rdmsFeeProcessService.save(feeProcess);

        responseDto.setContent(feeApplication.getCode());
        return responseDto;
    }

    /**
     * 本申请单的流程图。路径含 /diagram，按现有推断规则归费用模块的查看，
     * 申请人不必具备流程配置权限。
     */
    @PostMapping("/diagram")
    public ResponseDto<FlowDiagramDto> diagram(@RequestBody RdmsFeeManageDto feeManageDto) {
        ValidatorUtil.require(feeManageDto.getCode(), "申请单号");
        ResponseDto<FlowDiagramDto> responseDto = new ResponseDto<>();
        responseDto.setContent(flowDiagramService.diagram(
                this.flowCustomerId(feeManageDto), FlowBizCode.FEE_APPLY.getCode(), feeManageDto.getCode()));
        return responseDto;
    }

    /** 本申请单的办理记录。路径含 /history，归费用模块的查看。 */
    @PostMapping("/history")
    public ResponseDto<List<FlowDiagramDto.FlowNodeDetailDto>> history(@RequestBody RdmsFeeManageDto feeManageDto) {
        ValidatorUtil.require(feeManageDto.getCode(), "申请单号");
        ResponseDto<List<FlowDiagramDto.FlowNodeDetailDto>> responseDto = new ResponseDto<>();
        responseDto.setContent(flowDiagramService.diagram(
                this.flowCustomerId(feeManageDto), FlowBizCode.FEE_APPLY.getCode(), feeManageDto.getCode()).getNodeDetails());
        return responseDto;
    }

    /**
     * 审批人预览。路径含 /query，按现有推断规则归为查看，申请人无需审批权限。
     * 判定与保存接口相同，页面只展示，不采用请求里自带的审批人。
     */
    @PostMapping("/previewHandler/query")
    public ResponseDto<RdmsFeeManageDto> previewHandler(@RequestBody RdmsFeeManageDto feeManageDto) {
        ValidatorUtil.require(feeManageDto.getJobitemId(), "工单Id");
        RdmsJobItem jobItem = rdmsJobItemService.selectByPrimaryKey(feeManageDto.getJobitemId());
        if (ObjectUtils.isEmpty(jobItem)) {
            throw new BusinessException(BusinessExceptionCode.DB_RECORD_NOT_EXIST);
        }
        RdmsFeeManageDto preview = new RdmsFeeManageDto();
        feeApproverClassifier.apply(preview, jobItem);
        RdmsCustomerUser approver = rdmsCustomerUserService.selectByPrimaryKey(preview.getApproverId());
        if (approver != null) {
            preview.setApproverName(approver.getTrueName());
        }
        ResponseDto<RdmsFeeManageDto> responseDto = new ResponseDto<>();
        responseDto.setContent(preview);
        return responseDto;
    }

    /**
     * 审批人与费用归类只在 {@link FeeApproverClassifier} 判定，覆盖请求值。
     */
    private void resolveFeeApprover(RdmsFeeManageDto feeManageDto, RdmsJobItem jobItem){
        feeApproverClassifier.apply(feeManageDto, jobItem);
    }

    /** 网关请求以租户机构为准，本地直连时退回单据上的机构标识。 */
    private String flowCustomerId(RdmsFeeManageDto feeManageDto) {
        String customerId = TenantContext.getTenantId();
        if (!StringUtils.hasText(customerId) && feeManageDto != null) {
            customerId = feeManageDto.getCustomerId();
        }
        return customerId;
    }

    /**
     * 余额口径与审批页显示一致：挂特性的取特性其他费用余额，否则取子项目可用额合计减执行流水（已执行与进行中）；
     * 预研、产品级与缺陷费用没有对应的余额方法，不做拦截
     */
    private void checkFeeBudget(RdmsFeeManage feeManage, BigDecimal increment){
        if(ObjectUtils.isEmpty(increment) || increment.compareTo(BigDecimal.ZERO) <= 0){
            return;
        }
        if(!DocTypeEnum.SUB_PROJECT.getType().equals(feeManage.getAuxStatus()) || !ObjectUtils.isEmpty(feeManage.getQualityId())){
            return;
        }
        BigDecimal remain;
        if(!ObjectUtils.isEmpty(feeManage.getCharacterId())){
            remain = rdmsManhourService.getCharacterBudgetExeSummary(feeManage.getCharacterId()).getOtherFeeRemain();
        }else if(!ObjectUtils.isEmpty(feeManage.getSubprojectId())){
            remain = rdmsManhourService.getSubprojectSelfRemainBudget(feeManage.getSubprojectId());
        }else{
            return;
        }
        if(!ObjectUtils.isEmpty(remain) && increment.compareTo(remain) > 0){
            throw new BusinessException(BusinessExceptionCode.FEE_BUDGET_OVERFLOW);
        }
    }

    /**
     * 网关验签后以租户上下文中的成员身份为准，本地直连时退回请求中的 loginUserId
     */
    private String resolveOperatorId(String fallbackLoginUserId){
        String operatorId = TenantContext.getMembershipId();
        if(!StringUtils.hasText(operatorId)){
            operatorId = fallbackLoginUserId;
        }
        ValidatorUtil.require(operatorId, "操作者");
        return operatorId;
    }

    private void assertWriterAction(RdmsFeeManageDto current, ApplicationStatusEnum requiredStatus){
        if(!requiredStatus.getStatus().equals(current.getStatus())){
            throw new BusinessException(BusinessExceptionCode.LIFECYCLE_TRANSITION_FORBIDDEN);
        }
        this.assertWriterOrProxy(current, this.resolveOperatorId(null));
    }

    /** 填表人本人或最终管理人代办；返回是否为代办 */
    private boolean assertWriterOrProxy(RdmsFeeManageDto current, String operatorId){
        if(operatorId.equals(current.getWriterId())){
            return false;
        }
        if(jobActorGuard.hasFullAuthority(current.getCustomerId(), operatorId)){
            return true;
        }
        throw new BusinessException(BusinessExceptionCode.NO_PERMISSION);
    }


}
