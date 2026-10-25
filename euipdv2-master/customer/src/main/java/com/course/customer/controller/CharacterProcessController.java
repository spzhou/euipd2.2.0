/**
 * Author：周朔鹏
 * 编制时间：2025-08-05

 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */

package com.course.customer.controller;

import com.course.server.domain.RdmsCharacterProcess;
import com.course.server.domain.RdmsCharacter;
import com.course.server.dto.ResponseDto;
import com.course.server.dto.rdms.RdmsCharacterProcessDto;
import com.course.server.dto.rdms.RdmsHmiCharacterProcessDto;
import com.course.server.enums.rdms.CharacterStatusEnum;
import com.course.server.enums.rdms.JobItemTypeEnum;
import com.course.server.enums.rdms.ProjectTypeEnum;
import com.course.server.exception.BusinessException;
import com.course.server.exception.BusinessExceptionCode;
import com.course.server.security.tenant.TenantAccess;
import com.course.server.service.rdms.JobActorGuard;
import com.course.server.service.rdms.LifecycleTransition;
import com.course.server.service.rdms.RdmsCharacterProcessService;
import com.course.server.service.rdms.RdmsCharacterService;
import com.course.server.service.rdms.helper.TenantParentGuard;
import com.course.server.util.CopyUtil;
import com.course.server.util.ValidatorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/character-process")
public class CharacterProcessController {
    private static final Logger LOG = LoggerFactory.getLogger(CharacterProcessController.class);
    public static final String BUSINESS_NAME = "特性处理";

    @Resource
    private RdmsCharacterProcessService rdmsCharacterProcessService;
    @Resource
    private RdmsCharacterService rdmsCharacterService;
    @Resource
    private JobActorGuard jobActorGuard;
    @Resource
    private TenantParentGuard tenantParentGuard;


    /**
     * 保存客户信息
     */
    @PostMapping("/submit")
    @Transactional
    public ResponseDto<RdmsCharacterProcessDto> submit(@RequestBody RdmsCharacterProcessDto characterProcessDto) {
        ResponseDto<RdmsCharacterProcessDto> responseDto = new ResponseDto<>();

        ValidatorUtil.require(characterProcessDto.getCharacterId(), "特性ID");
        ValidatorUtil.require(characterProcessDto.getExecutorId(), "执行者ID");
        ValidatorUtil.require(characterProcessDto.getJobDescription(), "工作描述");
        ValidatorUtil.require(characterProcessDto.getDeep(), "执行深度");

        RdmsCharacter character = requireCharacter(characterProcessDto.getCharacterId(),
                CharacterStatusEnum.definitionEditSources(), BusinessExceptionCode.CHARACTER_EDIT_STATUS_INVALID);
        // 修订编辑记录只经 saveIterationCharacter 提交，以便工单同步
        if (JobItemTypeEnum.ITERATION.getType().equals(character.getJobitemType())) {
            throw new BusinessException(BusinessExceptionCode.CHARACTER_REVISION_FLOW_INVALID);
        }
        jobActorGuard.assertActor(character.getCustomerId(), null,
                character.getWriterId(), character.getProductManagerId());
        characterProcessDto.setExecutorId(jobActorGuard.currentActor(characterProcessDto.getExecutorId()));

        RdmsCharacterProcess rdmsCharacterProcess = CopyUtil.copy(characterProcessDto, RdmsCharacterProcess.class);
        rdmsCharacterProcessService.submit(rdmsCharacterProcess);

        responseDto.setContent(characterProcessDto);
        responseDto.setSuccess(true);
        responseDto.setMessage("保存成功");
        return responseDto;
    }


    @PostMapping("/reject")
    @Transactional
    public ResponseDto<RdmsCharacterProcessDto> reject(@RequestBody RdmsCharacterProcessDto characterProcessDto) {
        ResponseDto<RdmsCharacterProcessDto> responseDto = new ResponseDto<>();

        ValidatorUtil.require(characterProcessDto.getCharacterId(), "特性ID");
        ValidatorUtil.require(characterProcessDto.getExecutorId(), "执行者ID");
        ValidatorUtil.require(characterProcessDto.getJobDescription(), "工作描述");
        ValidatorUtil.require(characterProcessDto.getDeep(), "执行深度");
//        ValidatorUtil.require(characterProcessDto.getPlanSubmissionTime(), "计划提交时间");

        RdmsCharacter character = requireCharacter(characterProcessDto.getCharacterId(),
                CharacterStatusEnum.definitionApproveSources(), BusinessExceptionCode.CHARACTER_APPROVE_STATUS_INVALID);
        // 开发域修订由派发人确认或退回，不走特性审批驳回
        if (JobItemTypeEnum.ITERATION.getType().equals(character.getJobitemType())
                && !ProjectTypeEnum.PRE_PROJECT.getType().equals(character.getProjectType())) {
            throw new BusinessException(BusinessExceptionCode.CHARACTER_REVISION_FLOW_INVALID);
        }
        jobActorGuard.assertActor(character.getCustomerId(), null,
                character.getNextNode(), character.getProductManagerId());
        characterProcessDto.setExecutorId(jobActorGuard.currentActor(characterProcessDto.getExecutorId()));

        RdmsCharacterProcess rdmsCharacterProcess = CopyUtil.copy(characterProcessDto, RdmsCharacterProcess.class);
        rdmsCharacterProcessService.reject(rdmsCharacterProcess);

        responseDto.setContent(characterProcessDto);
        responseDto.setSuccess(true);
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    private RdmsCharacter requireCharacter(String characterId, Set<String> sources, BusinessExceptionCode statusCode) {
        RdmsCharacter character = rdmsCharacterService.selectByPrimaryKey(characterId);
        if (character == null || Integer.valueOf(1).equals(character.getDeleted())) {
            throw new BusinessException(BusinessExceptionCode.DB_RECORD_NOT_EXIST);
        }
        TenantAccess.assertSameTenant(character.getCustomerId());
        LifecycleTransition.requireSource(character.getStatus(), sources, statusCode);
        return character;
    }

    @PostMapping("/list-by-characterId/{id}")
    public ResponseDto<List<RdmsCharacterProcessDto>> listByCharacterId(@PathVariable String id){
        tenantParentGuard.requireCharacter(id);
        ResponseDto<List<RdmsCharacterProcessDto>> responseDto = new ResponseDto<>();
        List<RdmsCharacterProcessDto> rdmsCharacterProcessDtos = rdmsCharacterProcessService.listByCharacterId(id);
        responseDto.setContent(rdmsCharacterProcessDtos);
        responseDto.setSuccess(true);
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    @PostMapping("/listProcessAllInfoByCharacterId/{id}")
    public ResponseDto<RdmsHmiCharacterProcessDto> listProcessAllInfoByCharacterId(@PathVariable String id){
        tenantParentGuard.requireCharacter(id);
        ResponseDto<RdmsHmiCharacterProcessDto> responseDto = new ResponseDto<>();
        RdmsHmiCharacterProcessDto hmiCharacterProcessDto = rdmsCharacterProcessService.listProcessAllInfoByCharacterId(id);
        responseDto.setContent(hmiCharacterProcessDto);
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
        RdmsCharacterProcess process = rdmsCharacterProcessService.getCharacterProcessById(id);
        if (process == null) {
            throw new BusinessException(BusinessExceptionCode.DB_RECORD_NOT_EXIST);
        }
        tenantParentGuard.requireCharacter(process.getCharacterId());
        rdmsCharacterProcessService.delete(id);
        return responseDto;
    }

}
