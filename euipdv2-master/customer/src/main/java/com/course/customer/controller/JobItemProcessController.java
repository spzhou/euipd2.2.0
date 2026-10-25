/**
 * Author：周朔鹏
 * 编制时间：2025-08-05

 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */

package com.course.customer.controller;

import com.course.server.domain.*;
import com.course.server.dto.ResponseDto;
import com.course.server.dto.rdms.RdmsCharacterDto;
import com.course.server.dto.rdms.RdmsCharacterProcessDto;
import com.course.server.dto.rdms.RdmsFileDto;
import com.course.server.dto.rdms.RdmsJobItemProcessDto;
import com.course.server.enums.rdms.*;
import com.course.server.exception.BusinessException;
import com.course.server.exception.BusinessExceptionCode;
import com.course.server.service.rdms.*;
import com.course.server.service.rdms.helper.TenantParentGuard;
import com.course.server.util.CopyUtil;
import com.course.server.util.ValidatorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/job-item-process")
public class JobItemProcessController {
    private static final Logger LOG = LoggerFactory.getLogger(JobItemProcessController.class);
    public static final String BUSINESS_NAME = "工单处理";

    @Resource
    private RdmsJobItemProcessService rdmsJobItemProcessService;
    @Resource
    private RdmsFileService rdmsFileService;
    @Resource
    private TenantParentGuard tenantParentGuard;

    /**
     * 保存客户信息
     */
    @PostMapping("/save")
    @Transactional
    public ResponseDto<RdmsJobItemProcessDto> save(@RequestBody RdmsJobItemProcessDto jobItemProcessDto) {
        ResponseDto<RdmsJobItemProcessDto> responseDto = new ResponseDto<>();

        ValidatorUtil.require(jobItemProcessDto.getJobItemId(), "工单ID");
        ValidatorUtil.require(jobItemProcessDto.getExecutorId(), "执行者ID");
        ValidatorUtil.require(jobItemProcessDto.getDeep(), "执行深度");
//        ValidatorUtil.require(jobItemProcessDto.getPlanSubmissionTime(), "计划提交时间");

        tenantParentGuard.requireJobItem(jobItemProcessDto.getJobItemId());
        if (!ObjectUtils.isEmpty(jobItemProcessDto.getId())) {
            RdmsJobItemProcess stored = rdmsJobItemProcessService.selectByPrimaryKey(jobItemProcessDto.getId());
            if (stored != null && !Objects.equals(stored.getJobItemId(), jobItemProcessDto.getJobItemId())) {
                throw new BusinessException(BusinessExceptionCode.NO_PERMISSION);
            }
        }

        RdmsJobItemProcess rdmsJobItemProcess = CopyUtil.copy(jobItemProcessDto, RdmsJobItemProcess.class);
        rdmsJobItemProcessService.save(rdmsJobItemProcess);

        responseDto.setContent(jobItemProcessDto);
        responseDto.setSuccess(true);
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    /**
     * 保存客户信息
     */
    /**
     * 遗留接口：空过程节点模型已废弃。请使用 /job-items/submit 或 submitJobitemProperty。
     */
    @PostMapping("/submit")
    @Transactional
    public ResponseDto<RdmsJobItemProcessDto> submit(@RequestBody RdmsJobItemProcessDto jobItemProcessDto) {
        throw new BusinessException(BusinessExceptionCode.DATA_RECORD_ERROR);
    }

    /**
     * 删除
     */
    @DeleteMapping("/del/{id}")
    public ResponseDto delete(@PathVariable String id) {
        ResponseDto responseDto = new ResponseDto();
        RdmsJobItemProcess stored = rdmsJobItemProcessService.selectByPrimaryKey(id);
        if (stored == null) {
            throw new BusinessException(BusinessExceptionCode.DB_RECORD_NOT_EXIST);
        }
        tenantParentGuard.requireJobItem(stored.getJobItemId());
        rdmsJobItemProcessService.delete(id);
        return responseDto;
    }

    /**
     *
     */
    @PostMapping("/get-jobitem-process-info")
    public ResponseDto<RdmsJobItemProcessDto> getJobItemProcessInfo(@RequestBody RdmsJobItemProcessDto processDto) {
        tenantParentGuard.requireJobItem(processDto.getJobItemId());
        ResponseDto<RdmsJobItemProcessDto> responseDto = new ResponseDto<>();
        RdmsJobItemProcess jobItemProcessInfo = rdmsJobItemProcessService.getJobItemProcessInfo(processDto.getJobItemId(), processDto.getExecutorId());
        RdmsJobItemProcessDto jobItemProcessDto = CopyUtil.copy(jobItemProcessInfo, RdmsJobItemProcessDto.class);
        responseDto.setContent(jobItemProcessDto);
        responseDto.setSuccess(true);
        responseDto.setMessage("查询成功");
        return responseDto;
    }

    /**
     *
     */
    @PostMapping("/get-jobitem-process-info-list/{jobItemId}")
    public ResponseDto<List<RdmsJobItemProcessDto>> getJobItemProcessInfoList(@PathVariable String jobItemId) {
        tenantParentGuard.requireJobItem(jobItemId);
        ResponseDto<List<RdmsJobItemProcessDto>> responseDto = new ResponseDto<>();
        List<RdmsJobItemProcessDto> jobItemProcessInfoList = rdmsJobItemProcessService.getJobItemProcessInfoList(jobItemId);
        for(RdmsJobItemProcessDto processDto : jobItemProcessInfoList){
            if(processDto.getFileList() != null && !processDto.getFileList().isEmpty()){
                for(RdmsFileDto fileDto : processDto.getFileList()){
                    if(!ObjectUtils.isEmpty(fileDto)){
                        RdmsFileDto fileSimpleRecordInfo = rdmsFileService.getFileSimpleRecordInfo(fileDto.getId());
                        fileDto.setAbsPath(fileSimpleRecordInfo.getAbsPath());
                    }
                }
            }
        }
        responseDto.setContent(jobItemProcessInfoList);
        responseDto.setSuccess(true);
        responseDto.setMessage("查询成功");
        return responseDto;
    }

}
