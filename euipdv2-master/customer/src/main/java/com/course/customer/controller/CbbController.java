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
import com.course.server.service.rdms.*;
import com.course.server.service.rdms.rich.CbbRichDocService;
import com.course.server.service.rdms.rich.CharacterRichDocService;
import com.course.server.util.CopyUtil;
import com.course.server.util.ValidatorUtil;
import com.course.server.util.RichTextFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cbb")
public class CbbController {
    private static final Logger LOG = LoggerFactory.getLogger(CbbController.class);
    public static final String BUSINESS_NAME = "特性管理";

    @Resource
    private RdmsCharacterService rdmsCharacterService;
    @Resource
    private RdmsCbbService rdmsCbbService;
    @Autowired
    private RdmsBossService rdmsBossService;
    @Autowired
    private RdmsJobItemService rdmsJobItemService;
    @Resource
    private CharacterRichDocService characterRichDocService;
    @Resource
    private CbbRichDocService cbbRichDocService;
    @Resource
    private JobActorGuard jobActorGuard;
    @Resource
    private RdmsCbbDefineReviewService rdmsCbbDefineReviewService;

    @PostMapping("/getRecordAndData")
    @Transactional
    public ResponseDto<RdmsCharacterDto> getRecordAndData(@RequestParam String cbbId, String loginUserId) {
        ResponseDto<RdmsCharacterDto> responseDto = new ResponseDto<>();
        RdmsCharacterDto recordAndData = new RdmsCharacterDto();
        RdmsCbb rdmsCbb = rdmsCbbService.selectByPrimaryKey(cbbId);
        if(cbbId != null && !ObjectUtils.isEmpty(rdmsCbb)){
            recordAndData = rdmsCharacterService.getRecordAndData(rdmsCbb.getCharacterId(), loginUserId);
        }
        responseDto.setContent(recordAndData);
        return responseDto;
    }

    @PostMapping("/list-module")
    public ResponseDto<PageDto<RdmsCbbDto>> listModule(@RequestBody PageDto<RdmsCbbDto> pageDto) {
        ResponseDto<PageDto<RdmsCbbDto>> responseDto = new ResponseDto<>();
        rdmsCbbService.list(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

    @PostMapping("/disable/{cbbId}")
    public ResponseDto<String> disable(@PathVariable String cbbId) {
        ResponseDto<String> responseDto = new ResponseDto<>();
        rdmsCbbService.disable(cbbId);
        responseDto.setContent(cbbId);
        responseDto.setMessage("已停用");
        return responseDto;
    }

    @PostMapping("/enable/{cbbId}")
    public ResponseDto<String> enable(@PathVariable String cbbId) {
        ResponseDto<String> responseDto = new ResponseDto<>();
        rdmsCbbService.enable(cbbId);
        responseDto.setContent(cbbId);
        responseDto.setMessage("已启用");
        return responseDto;
    }

    /** 废弃：请求体 reason 为废弃原因（10 至 500 字）。 */
    @PostMapping("/discard/{cbbId}")
    public ResponseDto<String> discard(@PathVariable String cbbId, @RequestBody Map<String, String> body) {
        ResponseDto<String> responseDto = new ResponseDto<>();
        rdmsCbbService.discard(cbbId, body == null ? null : body.get("reason"));
        responseDto.setContent(cbbId);
        responseDto.setMessage("已废弃");
        return responseDto;
    }

    @PostMapping("/tech-doc/get/{cbbId}")
    public ResponseDto<CbbTechDocDto> techDoc(@PathVariable String cbbId) {
        ResponseDto<CbbTechDocDto> responseDto = new ResponseDto<>();
        responseDto.setContent(rdmsCbbService.techDoc(cbbId, jobActorGuard.currentActor(null)));
        return responseDto;
    }

    @PostMapping("/change-owner/{cbbId}/{ownerId}")
    public ResponseDto<Integer> changeOwner(@PathVariable String cbbId, @PathVariable String ownerId) {
        ResponseDto<Integer> responseDto = new ResponseDto<>();
        responseDto.setContent(rdmsCbbService.changeOwner(cbbId, ownerId));
        responseDto.setMessage("负责人已变更");
        return responseDto;
    }

    @PostMapping("/where-used/{cbbId}")
    public ResponseDto<List<CbbWhereUsedDto>> whereUsed(@PathVariable String cbbId) {
        ResponseDto<List<CbbWhereUsedDto>> responseDto = new ResponseDto<>();
        responseDto.setContent(rdmsCbbService.whereUsed(cbbId));
        return responseDto;
    }

    @PostMapping("/define/approve/{jobitemId}")
    public ResponseDto<String> approveDefine(@PathVariable String jobitemId) {
        ResponseDto<String> responseDto = new ResponseDto<>();
        responseDto.setContent(rdmsCbbDefineReviewService.approve(jobitemId));
        responseDto.setMessage("审核通过");
        return responseDto;
    }

    @PostMapping("/define/reject/{jobitemId}")
    public ResponseDto<String> rejectDefine(@PathVariable String jobitemId, @RequestParam(required = false) String reason) {
        ResponseDto<String> responseDto = new ResponseDto<>();
        responseDto.setContent(rdmsCbbDefineReviewService.reject(jobitemId, reason));
        responseDto.setMessage("已退回");
        return responseDto;
    }

    /** 选用候选：已发布的 CBB，标签为“名称 发布版本”，带承载特性；同源 CBB 相邻。 */
    @PostMapping("/getCbbListByCustomerId/{customerId}")
    public ResponseDto<List<RdmsCbbCandidateDto>> getCbbListByCustomerId(@PathVariable String customerId) {
        ResponseDto<List<RdmsCbbCandidateDto>> responseDto = new ResponseDto<>();
        responseDto.setContent(rdmsCbbService.listCandidates(customerId));
        return responseDto;
    }

    @PostMapping("/getCbbDefineCandidates/{jobitemId}")
    public ResponseDto<List<RdmsCbbCandidateDto>> getCbbDefineCandidates(@PathVariable String jobitemId) {
        RdmsJobItem rdmsJobItem = rdmsJobItemService.selectByPrimaryKey(jobitemId);
        if (rdmsJobItem == null) {
            throw new BusinessException(BusinessExceptionCode.DB_RECORD_NOT_EXIST);
        }
        TenantAccess.assertSameTenant(rdmsJobItem.getCustomerId());
        ResponseDto<List<RdmsCbbCandidateDto>> responseDto = new ResponseDto<>();
        responseDto.setContent(rdmsCbbService.listCbbDefineCandidates(rdmsJobItem.getCustomerId(), rdmsJobItem.getSubprojectId()));
        return responseDto;
    }

    /**
     * 在保存的时候,现将数据存到character表中
     */
    @PostMapping("/saveRecordAndData")
    @Transactional
    public ResponseDto<RdmsCharacterDto> saveRecordAndData(@RequestBody RdmsCharacterDto characterDto) {
        RichTextFields.sanitize(characterDto);
        ResponseDto<RdmsCharacterDto> responseDto = new ResponseDto<>();

        ValidatorUtil.require(characterDto.getId(), "特性 ID");
        ValidatorUtil.require(characterDto.getCharacterName(), "特性名称");
        ValidatorUtil.require(characterDto.getFunctionDescription(), "特性描述");
        ValidatorUtil.require(characterDto.getWorkCondition(), "生效条件/应用范围");
        ValidatorUtil.require(characterDto.getInputLogicalDesc(), "输入逻辑/使用条件");
        ValidatorUtil.require(characterDto.getFunctionLogicalDesc(), "功能逻辑/功能要求");
        ValidatorUtil.require(characterDto.getOutputLogicalDesc(), "输出逻辑/交付指标");

        ValidatorUtil.require(characterDto.getCustomerId(), "客户ID");
        ValidatorUtil.require(characterDto.getProjectType(), "项目类型");
        ValidatorUtil.require(characterDto.getJobitemId(), "任务ID");
        ValidatorUtil.require(characterDto.getJobitemType(), "任务类型");
        ValidatorUtil.require(characterDto.getLoginUserId(), "当前用户");

        ValidatorUtil.length(characterDto.getCharacterName(), "特性名称", 4, 80);
        ValidatorUtil.richLength(characterDto.getFunctionDescription(), "特性描述", 10, 5000);
        ValidatorUtil.richLength(characterDto.getWorkCondition(), "生效条件/应用范围", 10, 5000);
        ValidatorUtil.richLength(characterDto.getInputLogicalDesc(), "输入逻辑/使用条件", 10, 5000);
        ValidatorUtil.richLength(characterDto.getFunctionLogicalDesc(), "功能逻辑/功能要求", 10, 5000);
        ValidatorUtil.richLength(characterDto.getOutputLogicalDesc(), "输出逻辑/交付指标", 10, 5000);
        ValidatorUtil.richLength(characterDto.getTestMethod(), "验证方法/测试方法", 10, 5000);

        String jsonString = JSON.toJSONString(characterDto.getFileIds());
        characterDto.setFileListStr(jsonString);

        // 定义记录提交时才写 ARCHIVED，保存期间不计入子项目特性统计
        characterDto.setStatus(CharacterStatusEnum.SAVED.getStatus());
        characterDto.setWriterId(characterDto.getLoginUserId());  //将编制人修改为自己

        RdmsJobItem rdmsJobItem = rdmsJobItemService.selectByPrimaryKey(characterDto.getJobitemId());
        if (rdmsJobItem == null) {
            throw new BusinessException(BusinessExceptionCode.DB_RECORD_NOT_EXIST);
        }
        TenantAccess.assertSameTenant(rdmsJobItem.getCustomerId());
        if (!JobItemStatusEnum.HANDLING.getStatus().equals(rdmsJobItem.getStatus())) {
            throw new BusinessException(BusinessExceptionCode.JOB_ITEM_STATUS_INVALID);
        }
        jobActorGuard.assertActor(rdmsJobItem.getCustomerId(), characterDto.getLoginUserId(), rdmsJobItem.getExecutorId());
        RdmsCharacter storedRecord = rdmsCharacterService.selectByPrimaryKey(characterDto.getId());
        rdmsCbbService.assertSelectableCbbs(rdmsJobItem.getCustomerId(),
                RdmsCbbService.parseModuleIds(characterDto.getModuleIdListStr()),
                RdmsCbbService.parseModuleIds(storedRecord == null ? null : storedRecord.getModuleIdListStr()),
                rdmsCbbService.cbbDefineScopeOfSubproject(rdmsJobItem.getSubprojectId()));
        characterDto.setCustomerId(rdmsJobItem.getCustomerId());
        if (RdmsCbbDefineReviewService.isSuggestRecord(storedRecord)) {
            throw new BusinessException(BusinessExceptionCode.JOB_TYPE_DEPRECATED);
        }
        if(!ObjectUtils.isEmpty(rdmsJobItem)){
            rdmsJobItem.setCharacterId(characterDto.getId());
            rdmsJobItemService.update(rdmsJobItem);

            characterDto.setCharacterSerial(characterDto.getId());
            characterDto.setDeep(0);
            characterDto.setIterationVersion(1);
            characterDto.setSubprojectId(rdmsJobItem.getSubprojectId());
            characterDto.setProjectId(rdmsJobItem.getProjectId());
            characterDto.setProductManagerId(rdmsJobItem.getProductManagerId());
            characterDto.setProjectManagerId(rdmsJobItem.getProjectManagerId());
            characterDto.setAuxStatus(CharacterStatusEnum.NOTSET.getStatus());
            characterDto.setJobitemType(JobItemTypeEnum.CBB_DEFINE.getType());
            characterDto.setJobitemId(characterDto.getJobitemId());
            characterDto.setProjectType(characterDto.getProjectType());
            characterDto.setStage(DocTypeEnum.PROJECT.getType());
            characterDto.setUpdateTime(new Date());
            characterDto.setCreateTime(new Date());
            characterDto.setSubmitTime(new Date());
            characterDto.setCbbProjectId(rdmsJobItem.getSubprojectId());
        }

        rdmsCbbService.saveRecordAndData(characterDto);

        responseDto.setContent(characterDto);
        responseDto.setSuccess(true);
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    @PostMapping("/getCbbData/{cbbProjectId}")
    @Transactional
    public ResponseDto<RdmsCharacterDto> getCbbData(@PathVariable String cbbProjectId) {
        ResponseDto<RdmsCharacterDto> responseDto = new ResponseDto<>();
        RdmsCharacterDto characterDto = new RdmsCharacterDto();
        List<RdmsCharacter> characterByCbbProjectId = rdmsCharacterService.getCharacterByCbbProjectId(cbbProjectId);
        if(!CollectionUtils.isEmpty(characterByCbbProjectId)){
            RdmsCharacter rdmsCharacter = characterByCbbProjectId.get(0);
            characterDto = CopyUtil.copy(rdmsCharacter, RdmsCharacterDto.class);
            characterRichDocService.fillDto(characterDto);
        }

        responseDto.setContent(characterDto);
        responseDto.setSuccess(true);
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    /** CBB 定义工单按工单所指记录读取定义内容；历史新版本定义工单指向迭代建议记录，只读。 */
    @PostMapping("/getCbbDefineData/{characterId}")
    @Transactional
    public ResponseDto<RdmsCharacterDto> getCbbDefineData(@PathVariable String characterId) {
        ResponseDto<RdmsCharacterDto> responseDto = new ResponseDto<>();
        RdmsCharacter record = rdmsCharacterService.selectByPrimaryKey(characterId);
        if (record == null || Integer.valueOf(1).equals(record.getDeleted())) {
            throw new BusinessException(BusinessExceptionCode.DB_RECORD_NOT_EXIST);
        }
        TenantAccess.assertSameTenant(record.getCustomerId());
        RdmsCharacterDto characterDto = CopyUtil.copy(record, RdmsCharacterDto.class);
        characterRichDocService.fillDto(characterDto);
        responseDto.setContent(characterDto);
        responseDto.setSuccess(true);
        responseDto.setMessage("查询成功");
        return responseDto;
    }

    @PostMapping("/getCbbSimpleInfo/{cbbId}")
    @Transactional
    public ResponseDto<RdmsCbb> getCbbSimpleInfo(@PathVariable String cbbId) {
        ResponseDto<RdmsCbb> responseDto = new ResponseDto<>();
        RdmsCbb rdmsCbb = rdmsCbbService.selectByPrimaryKey(cbbId);
        responseDto.setContent(rdmsCbb);
        responseDto.setSuccess(true);
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    @PostMapping("/getCbbSimpleInfoByCode/{cbbProjectId}")
    @Transactional
    public ResponseDto<RdmsCharacter> getCbbSimpleInfoByCode(@PathVariable String cbbProjectId) {
        ResponseDto<RdmsCharacter> responseDto = new ResponseDto<>();
        List<RdmsCharacter> characterByCbbProjectId = rdmsCharacterService.getCharacterByCbbProjectId(cbbProjectId);
        if(!CollectionUtils.isEmpty(characterByCbbProjectId)){
            RdmsCharacter rdmsCharacter = characterByCbbProjectId.get(0);
            responseDto.setContent(rdmsCharacter);
        }else{
            responseDto.setContent(null);
        }
        responseDto.setSuccess(true);
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    @PostMapping("/saveCharacterToCbb/{characterId}")
    @Transactional
    public ResponseDto<String> saveCharacterToCbb(@PathVariable String characterId) {
        ResponseDto<String> responseDto = new ResponseDto<>();
        RdmsCharacter record = rdmsCharacterService.selectByPrimaryKey(characterId);
        if (record == null || Integer.valueOf(1).equals(record.getDeleted())
                || !JobItemTypeEnum.CBB_DEFINE.getType().equals(record.getJobitemType())) {
            throw new BusinessException(BusinessExceptionCode.DB_RECORD_NOT_EXIST);
        }
        TenantAccess.assertSameTenant(record.getCustomerId());
        if (RdmsCbbDefineReviewService.isSuggestRecord(record)) {
            throw new BusinessException(BusinessExceptionCode.JOB_TYPE_DEPRECATED);
        }
        RdmsJobItem defineJob = ObjectUtils.isEmpty(record.getJobitemId())
                ? null : rdmsJobItemService.selectByPrimaryKey(record.getJobitemId());
        // 定义工单的执行人即评审组长；发布须先经产品经理审核通过（/define/approve）
        jobActorGuard.assertActor(record.getCustomerId(), null, record.getProductManagerId(),
                defineJob == null ? null : defineJob.getExecutorId());
        if (defineJob == null || !JobItemStatusEnum.COMPLETED.getStatus().equals(defineJob.getStatus())) {
            throw new BusinessException(BusinessExceptionCode.JOB_ITEM_STATUS_INVALID);
        }
        rdmsCbbService.saveCharacterToCbb(characterId, DocTypeEnum.PROJECT);
        responseDto.setContent(characterId);
        responseDto.setSuccess(true);
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    @PostMapping("/getCbbRecordDetailById")
    public ResponseDto<RdmsCbbDto> getCbbRecordDetailById(@RequestParam String cbbId, String loginUserId) {
        ResponseDto<RdmsCbbDto> responseDto = new ResponseDto<>();
        RdmsCbbDto cbbRecordDetailById = rdmsCbbService.getCbbRecordDetailById(cbbId, loginUserId);
        responseDto.setContent(cbbRecordDetailById);
        return responseDto;
    }
}
