/**
 * Author：周朔鹏
 * 编制时间：2025-08-05

 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */

package com.course.customer.controller;

import com.course.server.domain.RdmsJobItem;
import com.course.server.domain.RdmsManhourDeclare;
import com.course.server.dto.ResponseDto;
import com.course.server.dto.rdms.RdmsManhourDeclareDto;
import com.course.server.enums.rdms.JobItemStatusEnum;
import com.course.server.exception.BusinessException;
import com.course.server.exception.BusinessExceptionCode;
import com.course.server.exception.ValidatorException;
import com.course.server.security.tenant.TenantAccess;
import com.course.server.service.rdms.JobActorGuard;
import com.course.server.service.rdms.LifecycleTransition;
import com.course.server.service.rdms.RdmsJobItemService;
import com.course.server.service.rdms.RdmsManhourDeclareService;
import com.course.server.util.CopyUtil;
import com.course.server.util.ValidatorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/manhour-declare")
public class ManhourDeclareController {
    private static final Logger LOG = LoggerFactory.getLogger(ManhourDeclareController.class);
    public static final String BUSINESS_NAME = "MANHOUR_DECLARE";

    @Resource
    private RdmsManhourDeclareService rdmsManhourDeclareService;
    @Resource
    private RdmsJobItemService rdmsJobItemService;
    @Resource
    private JobActorGuard jobActorGuard;

    /**
     * 保存客户信息
     */
    @PostMapping("/save")
    @Transactional
    public ResponseDto<String> save(@RequestBody RdmsManhourDeclareDto manhourDeclareDto) {
        ResponseDto<String> responseDto = new ResponseDto<>();

        ValidatorUtil.require(manhourDeclareDto.getJobitemId(), "任务ID");
        ValidatorUtil.require(manhourDeclareDto.getManhour(), "申报工时");
        ValidatorUtil.require(manhourDeclareDto.getReason(), "申报原因");
        ValidatorUtil.length(manhourDeclareDto.getReason(), "申报原因", 4, 200);
        if (manhourDeclareDto.getManhour() < 0) {
            throw new ValidatorException("申报工时不能小于 0");
        }
        RdmsJobItem jobItem = rdmsJobItemService.selectByPrimaryKey(manhourDeclareDto.getJobitemId());
        if (jobItem == null) {
            throw new BusinessException(BusinessExceptionCode.DB_RECORD_NOT_EXIST);
        }
        TenantAccess.assertSameTenant(jobItem.getCustomerId());
        LifecycleTransition.requireSource(jobItem.getStatus(), JobItemStatusEnum.propertySubmitSources(),
                BusinessExceptionCode.JOB_ITEM_STATUS_INVALID);
        jobActorGuard.assertActor(jobItem.getCustomerId(), null, jobItem.getExecutorId());

        RdmsManhourDeclare manhourDeclare = CopyUtil.copy(manhourDeclareDto, RdmsManhourDeclare.class);
        manhourDeclare.setId(manhourDeclareDto.getJobitemId());
        String save = rdmsManhourDeclareService.save(manhourDeclare);

        responseDto.setContent(save);
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
        RdmsJobItem jobItem = rdmsJobItemService.selectByPrimaryKey(id);
        if (jobItem == null) {
            throw new BusinessException(BusinessExceptionCode.DB_RECORD_NOT_EXIST);
        }
        TenantAccess.assertSameTenant(jobItem.getCustomerId());
        jobActorGuard.assertActor(jobItem.getCustomerId(), null, jobItem.getExecutorId());
        rdmsManhourDeclareService.delete(id);
        return responseDto;
    }

}
