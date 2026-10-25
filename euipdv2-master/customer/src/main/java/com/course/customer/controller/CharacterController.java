/**
 * Author：周朔鹏
 * 编制时间：2025-08-05

 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */

package com.course.customer.controller;

import com.alibaba.fastjson.JSON;
import com.course.server.domain.*;
import com.course.server.domain.rdms.MyCharacter;
import com.course.server.dto.PageDto;
import com.course.server.dto.RdmsBudgetResearchDto;
import com.course.server.dto.ResponseDto;
import com.course.server.dto.rdms.*;
import com.course.server.enums.rdms.*;
import com.course.server.exception.BusinessException;
import com.course.server.exception.BusinessExceptionCode;
import com.course.server.exception.ValidatorException;
import com.course.server.security.tenant.TenantAccess;
import com.course.server.service.rdms.*;
import com.course.server.service.rdms.helper.TenantParentGuard;
import com.course.server.service.rdms.revision.CharacterRevisionFlowService;
import com.course.server.service.rdms.rich.CharacterRichDocService;
import com.course.server.util.CopyUtil;
import com.course.server.util.ValidatorUtil;
import com.course.server.util.RichTextFields;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/character")
public class CharacterController {
    private static final Logger LOG = LoggerFactory.getLogger(CharacterController.class);
    public static final String BUSINESS_NAME = "特性定义";

    @Resource
    private RdmsCharacterService rdmsCharacterService;
    @Resource
    private CharacterRevisionFlowService characterRevisionFlowService;
    @Resource
    private CharacterRichDocService characterRichDocService;
    @Resource
    private RdmsCharacterRevisionService rdmsCharacterRevisionService;
    @Resource
    private RdmsCharacterTerminateService rdmsCharacterTerminateService;
    @Resource
    private CharacterReviewOpinionService characterReviewOpinionService;
    @Resource
    private RdmsCharacterProcessService rdmsCharacterProcessService;
    @Resource
    private RdmsProjectService rdmsProjectService;
    @Resource
    private RdmsDemandService rdmsDemandService;
    @Resource
    private RdmsDemandLifecycleService rdmsDemandLifecycleService;
    @Resource
    private RdmsCharacterDefinitionService rdmsCharacterDefinitionService;
    @Resource
    private RdmsResearchStartService rdmsResearchStartService;
    @Resource
    private RdmsSubprojectDecomposeService rdmsSubprojectDecomposeService;
    @Resource
    private RdmsManhourService rdmsManhourService;
    @Resource
    private RdmsJobItemService rdmsJobItemsService;
    @Resource
    private JobItemProcessRecorder jobItemProcessRecorder;
    @Resource
    private RdmsPreProjectService rdmsPreProjectService;
    @Resource
    private RdmsCustomerUserService rdmsCustomerUserService;
    @Resource
    private RdmsSubprojectService rdmsSubprojectService;
    @Resource
    private RdmsMaterialManageService rdmsMaterialManageService;
    @Resource
    private RdmsBudgetResearchService rdmsBudgetResearchService;
    @Resource
    private RdmsMilestoneService rdmsMilestoneService;
    @Resource
    private RdmsProjectSetupService rdmsProjectSetupService;
    @Resource
    private RdmsFeeManageService rdmsFeeManageService;
    @Resource
    private RdmsProjectSecLevelService rdmsProjectSecLevelService;
    @Resource
    private CharacterVersionService characterVersionService;
    @Resource
    private RdmsBossService rdmsBossService;
    @Resource
    private RdmsSysgmService rdmsSysgmService;
    @Resource
    private JobActorGuard jobActorGuard;
    @Resource
    private TenantParentGuard tenantParentGuard;
    @Autowired
    private RdmsBugFeedbackService rdmsBugFeedbackService;
    @Autowired
    private RdmsCbbService rdmsCbbService;


    @PostMapping("/listSuggestItems")
    public ResponseDto listSuggestItems(@RequestBody PageDto pageDto) {
        TenantAccess.bindSelf(pageDto);
        ResponseDto responseDto = new ResponseDto();
        rdmsCharacterService.listSuggestItems(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

    @PostMapping("/checkoutAbandon/{characterId}")
    @Transactional
    public ResponseDto<String> checkoutAbandon(@PathVariable String characterId) {
        ResponseDto<String> responseDto = new ResponseDto<>();
        responseDto.setContent(rdmsCharacterService.checkoutAbandon(characterId));
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    /**
     * 根据customerId进行列表查询
     */
    @PostMapping("/listByCustomerId")
    public ResponseDto<PageDto<RdmsCharacterDto>> listByCustomerId(@RequestBody PageDto<RdmsCharacterDto> pageDto) {
        ValidatorUtil.require(pageDto.getCustomerId(), "机构ID");
        ResponseDto<PageDto<RdmsCharacterDto>> responseDto = new ResponseDto<>();
        rdmsCharacterService.listByCustomerId(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

    /**
     * 根据customerId进行列表查询
     */
    @PostMapping("/listDevelopingCharactersByPjm")
    public ResponseDto<PageDto<RdmsCharacterDto>> listDevelopingCharactersByPjm(@RequestBody PageDto<RdmsCharacterDto> pageDto) {
        TenantAccess.bindSelf(pageDto);
        ResponseDto<PageDto<RdmsCharacterDto>> responseDto = new ResponseDto<>();
        rdmsCharacterService.listDevelopingCharactersByPjm(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

    @PostMapping("/listTestCharactersByTm")
    public ResponseDto<PageDto<RdmsCharacterDto>> listTestCharactersByTm(@RequestBody PageDto<RdmsCharacterDto> pageDto) {
        TenantAccess.bindSelf(pageDto);
        ResponseDto<PageDto<RdmsCharacterDto>> responseDto = new ResponseDto<>();
        rdmsCharacterService.listTestCharactersByTm(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

    /**
     * 根据PreProjectId进行列表查询
     */
    @PostMapping("/listByPreProjectId")
    public ResponseDto<PageDto<RdmsCharacterDto>> listByPreProjectId(
            @RequestBody PageDto<RdmsCharacterDto> pageDto
    ) {
        TenantAccess.bindSelf(pageDto);
        ResponseDto<PageDto<RdmsCharacterDto>> responseDto = new ResponseDto<>();
        rdmsCharacterService.listByPreProjectId(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

    /**
     * 列表查询所有预立项项目项下经过审批的特性特性清单
     */
    @PostMapping("/listAllApprovedByPreProjectId")
    public ResponseDto<List<RdmsCharacterDto>> listAllApprovedByPreProjectId(@RequestParam String preProjectId, String loginUserId) {
        ResponseDto<List<RdmsCharacterDto>> responseDto = new ResponseDto<>();
        List<RdmsCharacterDto> characterDtos = rdmsCharacterService.listAllApprovedByPreProjectId(preProjectId, loginUserId);
        responseDto.setContent(characterDtos);
        return responseDto;
    }

    @PostMapping("/getDevelopCharacterHasListFlag/{userId}")
    public ResponseDto<Boolean> getDevelopCharacterHasListFlag(@PathVariable String userId) {
        userId = TenantAccess.resolveMemberId(userId);
        ResponseDto<Boolean> responseDto = new ResponseDto<>();
        Integer countOfDevelopingCharacterByPjm = rdmsCharacterService.getCountOfDevelopingCharacterByPjm(userId);
        responseDto.setContent(countOfDevelopingCharacterByPjm >0);
        return responseDto;
    }

    /**
     * 根据ProjectId进行列表查询
     */
    @PostMapping("/listByProjectId/{projectId}")
    public ResponseDto<List<RdmsCharacterDto>> listByProjectId(@PathVariable String projectId) {
        ResponseDto<List<RdmsCharacterDto>> responseDto = new ResponseDto<>();
        List<RdmsCharacterDto> characterDtos = rdmsCharacterService.listByProjectId(projectId);
        responseDto.setContent(characterDtos);
        return responseDto;
    }

    /**
     * 根据ProjectId进行列表查询
     */
    @PostMapping("/getSetupedCharacterListByProjectId")
    public ResponseDto<List<RdmsCharacterDto>> getSetupedCharacterListByProjectId(@RequestParam String projectId, String loginUserId) {
        ResponseDto<List<RdmsCharacterDto>> responseDto = new ResponseDto<>();
        List<RdmsCharacterDto> characterDtos = rdmsCharacterService.getSetupedCharacterListByProjectId(projectId, loginUserId);
        characterRichDocService.fillLockedFeatureNos(characterDtos);
        responseDto.setContent(characterDtos);
        return responseDto;
    }

    /**
     * 根据ProjectId进行列表查询
     */
    @PostMapping("/getSetupCharacterListByProjectId")
    public ResponseDto<List<RdmsCharacterDto>> getSetupCharacterListByProjectId(@RequestParam String projectId, String loginUserId) {
        ResponseDto<List<RdmsCharacterDto>> responseDto = new ResponseDto<>();
        List<RdmsCharacterDto> characterDtos = rdmsCharacterService.getSetupCharacterListByProjectId(projectId, loginUserId);
        characterRichDocService.fillLockedFeatureNos(characterDtos);
        responseDto.setContent(characterDtos);
        return responseDto;
    }

    /**
     * 根据subprojectId进行列表查询
     */
    @PostMapping("/getSetupedCharacterListBySubProjectId")
    public ResponseDto<List<RdmsCharacterDto>> getSetupedCharacterListBySubProjectId(@RequestParam String subprojectId, String loginUserId) {
        ResponseDto<List<RdmsCharacterDto>> responseDto = new ResponseDto<>();
        List<RdmsCharacterDto> characterDtos = rdmsCharacterService.getSetupedCharacterListBySubProjectId(subprojectId, loginUserId);
        characterRichDocService.fillLockedFeatureNos(characterDtos);
        responseDto.setContent(characterDtos);
        return responseDto;
    }

    @PostMapping("/restartSubprojectResearch/{subprojectId}")
    public ResponseDto restartSubprojectResearch(@PathVariable String subprojectId) {
        ResponseDto responseDto = new ResponseDto();
        rdmsResearchStartService.restartSubproject(subprojectId);
        return responseDto;
    }

    @PostMapping("/startResearchAndDevelopment")
    public ResponseDto<String> startResearchAndDevelopment(@RequestBody RdmsCharacterService.CbbListStr cbbListStr) {
        ResponseDto<String> responseDto = new ResponseDto<>();
        ValidatorUtil.require(cbbListStr == null ? null : cbbListStr.getCharacterId(), "特性ID");
        responseDto.setContent(rdmsResearchStartService.start(cbbListStr));
        responseDto.setSuccess(true);
        responseDto.setMessage("启动成功");
        return responseDto;
    }

    /**
     * 根据SubprojectId进行列表查询
     */
    @PostMapping("/getCharacterSimpleInfo/{characterId}")
    public ResponseDto<RdmsCharacterDto> getCharacterSimpleInfo(@PathVariable String characterId) {
        ResponseDto<RdmsCharacterDto> responseDto = new ResponseDto<>();
        RdmsCharacterDto characterById = rdmsCharacterService.getCharacterSimpleInfo(characterId);
        if(!ObjectUtils.isEmpty(characterById)){
            List<RdmsCharacterDto> characterList = rdmsCharacterService.referenceCharactersOf(
                    characterById.getCustomerId(), characterById.getModuleIdListStr());
            if(!CollectionUtils.isEmpty(characterList)){
                characterById.setReferenceCharacterList(characterList);
            }
        }
        responseDto.setContent(characterById);
        return responseDto;
    }

    /**
     * 根据功能的ID List查询功能名称List
     */
    @PostMapping("/getCharacterNameListByIdList")
    public ResponseDto<RdmsHmiMergeCharactersDto> getCharacterNameListByIdList(@RequestBody RdmsHmiMergeCharactersDto characterNameListDto) {
        ResponseDto<RdmsHmiMergeCharactersDto> responseDto = new ResponseDto<>();
        List<String> characterNameListByIdList = rdmsCharacterService.getCharacterNameListByIdList(characterNameListDto.getCustomerId(), characterNameListDto.getCharacterIdList());
        RdmsHmiMergeCharactersDto nameListDto = new RdmsHmiMergeCharactersDto();
        nameListDto.setCharacterNameList(characterNameListByIdList);
        RdmsCharacter rdmsCharacter = rdmsCharacterService.selectByPrimaryKey(characterNameListDto.getCharacterIdList().get(0));
        if(!ObjectUtils.isEmpty(rdmsCharacter) && rdmsCharacter.getPreProjectId() != null){
            nameListDto.setPreProjectId(rdmsCharacter.getPreProjectId());
            RdmsPreProject rdmsPreProject = rdmsPreProjectService.selectByPrimaryKey(rdmsCharacter.getPreProjectId());
            if(rdmsPreProject.getProductManagerId() != null){
                RdmsCustomerUser rdmsCustomerUser = rdmsCustomerUserService.selectByPrimaryKey(rdmsPreProject.getProductManagerId());
                nameListDto.setProductManagerId(rdmsCustomerUser.getId());
                nameListDto.setProductManagerName(rdmsCustomerUser.getTrueName());
            }
        }
        responseDto.setContent(nameListDto);
        return responseDto;
    }

    /**
     * 根据SubprojectId进行列表查询
     */
    @PostMapping("/getCharacterTreeByProjectId/{subProjectId}")
    public ResponseDto<List<RdmsHmiTree<RdmsCharacterSimpleDto>>> getCharacterTreeByProjectId(@PathVariable String subProjectId) {
        ResponseDto<List<RdmsHmiTree<RdmsCharacterSimpleDto>>> responseDto = new ResponseDto<>();
        RdmsHmiTree<RdmsCharacterSimpleDto> characterTree = rdmsCharacterService.getCharacterTreeByProjectId(subProjectId);
        //添加跟节点信息
        RdmsProjectSubproject subproject = rdmsSubprojectService.selectByPrimaryKey(subProjectId);
        characterTree.setId(subproject.getId());
        characterTree.setDeep(-1);
        characterTree.setLabel("产品: "+subproject.getLabel());
        characterTree.setParent(subproject.getId());
        characterTree.setStatus(subproject.getStatus());
        List<RdmsHmiTree<RdmsCharacterSimpleDto>> treeList = new ArrayList<>();
        treeList.add(characterTree);
        responseDto.setContent(treeList);
        return responseDto;
    }

    @PostMapping("/getDocTreeByCharacterId")
    public ResponseDto<List<RdmsHmiTree<RdmsFileDto>>> getDocTreeByCharacterId(@RequestParam String characterId, String loginUserId) {
        ResponseDto<List<RdmsHmiTree<RdmsFileDto>>> responseDto = new ResponseDto<>();
        RdmsHmiTree<RdmsFileDto> docTreeByCharacterId = rdmsCharacterService.getDocTreeByCharacterId(characterId, loginUserId);
        List<RdmsHmiTree<RdmsFileDto>> rdmsHmiTreeList = new ArrayList<>();
        rdmsHmiTreeList.add(docTreeByCharacterId);
        responseDto.setContent(rdmsHmiTreeList);
        return responseDto;
    }

    @PostMapping("/getCharacterReviewDetailInfo")
    public ResponseDto<RdmsHmiCharacterReviewDetailDto> getCharacterReviewDetailInfo(@RequestParam String characterId, String loginUserId) {
        ResponseDto<RdmsHmiCharacterReviewDetailDto> responseDto = new ResponseDto<>();
        RdmsHmiCharacterReviewDetailDto characterReviewDetailInfo = rdmsCharacterService.getCharacterReviewDetailInfo(characterId, loginUserId);
        responseDto.setContent(characterReviewDetailInfo);
        return responseDto;
    }

    @PostMapping("/getCharacterDocPageInfo")
    public ResponseDto<RdmsHmiCharacterDocPageInfoDto> getCharacterDocPageInfo(@RequestParam String characterId, String loginUserId) {
        tenantParentGuard.requireCharacter(characterId);
        ResponseDto<RdmsHmiCharacterDocPageInfoDto> responseDto = new ResponseDto<>();
        RdmsHmiCharacterDocPageInfoDto characterDocPageInfo = rdmsCharacterService.getCharacterDocPageInfo(characterId, loginUserId);
        responseDto.setContent(characterDocPageInfo);
        return responseDto;
    }

    @PostMapping("/getCharacterArchiveDocs")
    public ResponseDto<RdmsHmiCharacterArchiveDocsDto> getCharacterArchiveDocs(@RequestParam String characterId, String loginUserId) {
        ResponseDto<RdmsHmiCharacterArchiveDocsDto> responseDto = new ResponseDto<>();
        RdmsHmiCharacterArchiveDocsDto characterArchiveDocs = rdmsCharacterService.getCharacterArchiveDocs(characterId, loginUserId);
        responseDto.setContent(characterArchiveDocs);
        return responseDto;
    }

    @PostMapping("/getCharacterManhourExeInfoList/{characterId}")
    public ResponseDto<List<RdmsBudgeExeInfoDto>> getCharacterBudgetExeInfoList(@PathVariable String characterId) {
        ResponseDto<List<RdmsBudgeExeInfoDto>> responseDto = new ResponseDto<>();
        List<RdmsBudgeExeInfoDto> characterManhourExeInfoList = rdmsManhourService.getCharacterBudgetExeSummaryList(characterId);

        //另一种统计方法: 将assist工单作为开发工单统计; test工单全部作为测试费统计. 结果是一样的
        //RdmsBudgetExeSummaryDto budgetExeSummaryByCharacterId = rdmsManhourService.getBudgetExeSummaryByCharacterId(characterId);
        responseDto.setContent(characterManhourExeInfoList);
        return responseDto;
    }

    @PostMapping("/getCharacterBudgetExeSummary/{characterId}")
    public ResponseDto<RdmsBudgetSummaryDto> getCharacterBudgetExeSummary(@PathVariable String characterId) {
        ResponseDto<RdmsBudgetSummaryDto> responseDto = new ResponseDto<>();
        RdmsBudgetSummaryDto characterBudgetExeSummary = rdmsManhourService.getCharacterBudgetExeSummary(characterId);
        responseDto.setContent(characterBudgetExeSummary);
        return responseDto;
    }

    /**
     * 根据SubprojectId进行列表查询
     */
    @PostMapping("/getCharacterRecordDetailById")
    public ResponseDto<RdmsCharacterDto> getCharacterRecordDetailById(@RequestParam String characterId, String loginUserId) {
        ResponseDto<RdmsCharacterDto> responseDto = new ResponseDto<>();
        RdmsCharacterDto characterById = rdmsCharacterService.getCharacterRecordDetailById(characterId, loginUserId);
        responseDto.setContent(characterById);
        return responseDto;
    }

    /**
     * 根据SubprojectId进行列表查询
     */
    @PostMapping("/getCharacterDocDetailById")
    public ResponseDto<RdmsCharacterDto> getCharacterDocDetailById(@RequestParam String characterId, String loginUserId) {
        ResponseDto<RdmsCharacterDto> responseDto = new ResponseDto<>();
        RdmsCharacter rdmsCharacter = rdmsCharacterService.selectByPrimaryKey(characterId);
        if(!ObjectUtils.isEmpty(rdmsCharacter)){
            if(rdmsCharacter.getAuxStatus().equals(CharacterStatusEnum.UPDATE.getStatus())){
                characterId = rdmsCharacter.getParent();
            }
            RdmsCharacterDto characterById = rdmsCharacterService.getCharacterRecordDetailById(characterId, loginUserId);
            responseDto.setContent(characterById);
        }

        return responseDto;
    }

    /**
     * 根据工单号获取工单记录的CharacterList 列表
     */
    @PostMapping("/getCharacterListByJobitemId")
    public ResponseDto<List<RdmsCharacterDto>> getCharacterListByJobitemId(@RequestParam String jobitemId, String loginUserId) {
        ResponseDto<List<RdmsCharacterDto>> responseDto = new ResponseDto<>();
        List<RdmsCharacterDto> characterListByJobitemId = rdmsCharacterService.getCharacterListByJobitemId(jobitemId, loginUserId);
        responseDto.setContent(characterListByJobitemId);
        return responseDto;
    }

    /**
     * 根据工单号获取迭代功能记录
     */
    @PostMapping("/getIterationCharacterByJobitemId")
    public ResponseDto<RdmsCharacterDto> getIterationCharacterByJobitemId(@RequestParam String jobitemId, String loginUserId) {
        ResponseDto<RdmsCharacterDto> responseDto = new ResponseDto<>();
        RdmsCharacterDto character = rdmsCharacterService.getIterationCharacterByJobitemId(jobitemId, loginUserId);
        responseDto.setContent(character);
        return responseDto;
    }

    /**
     * 根据工单号获取迭代功能记录
     */
    @PostMapping("/getMergedCharacterByJobitemId")
    public ResponseDto<RdmsCharacterDto> getMergedCharacterByJobitemId(@RequestParam String jobitemId, String loginUserId) {
        ResponseDto<RdmsCharacterDto> responseDto = new ResponseDto<>();
        RdmsCharacterDto character = rdmsCharacterService.getMergedCharacterByJobitemId(jobitemId, loginUserId);
        responseDto.setContent(character);
        return responseDto;
    }

    @PostMapping("/getProjectTeamIdsByCharacterId/{characterId}")
    public ResponseDto<RdmProjectTeamInfoDto> getProjectTeamIdsByCharacterId(@PathVariable String characterId) {
        tenantParentGuard.requireCharacter(characterId);
        ResponseDto<RdmProjectTeamInfoDto> responseDto = new ResponseDto<>();
        RdmProjectTeamInfoDto teamInfoDto = new RdmProjectTeamInfoDto();
        List<RdmsProjectSecLevel> rdmsProjectSecLevels = rdmsProjectSecLevelService.getByCharacterId(characterId);
        if(!CollectionUtils.isEmpty(rdmsProjectSecLevels)){
            teamInfoDto.setProjectSecLevel(rdmsProjectSecLevels.get(0).getLevel());
        }else{
            teamInfoDto.setProjectSecLevel(0);
        }
        List<String> projectTeamIds = rdmsProjectSecLevelService.getProjectTeamIdsByCharacterId(characterId);
        teamInfoDto.setTeamIdList(projectTeamIds);
        responseDto.setContent(teamInfoDto);
        return responseDto;
    }

    @PostMapping("/getMergedCharacter/{characterId}")
    public ResponseDto<List<MyCharacter>> getMergedCharacter(@PathVariable String characterId) {
        ResponseDto<List<MyCharacter>> responseDto = new ResponseDto<>();
        List<MyCharacter> mergedCharacter = rdmsCharacterService.getMergedCharacter(characterId);
        responseDto.setContent(mergedCharacter);
        return responseDto;
    }

    @PostMapping("/getUpdateCharacterList")
    public ResponseDto<List<RdmsCharacterDto>> getUpdateCharacterList(@RequestParam String preProjectId, String loginUserId) {
        ResponseDto<List<RdmsCharacterDto>> responseDto = new ResponseDto<>();
        List<RdmsCharacterDto> updateCharacterList = rdmsCharacterService.getUpdateCharacterList(preProjectId, loginUserId);
        responseDto.setContent(updateCharacterList);
        return responseDto;
    }

    @PostMapping("/list-approved")
    public ResponseDto<PageDto<RdmsCharacterDto>> listApproved(
            @RequestBody PageDto<RdmsCharacterDto> pageDto) {
        TenantAccess.bindSelf(pageDto);
        ResponseDto<PageDto<RdmsCharacterDto>> responseDto = new ResponseDto<>();
        rdmsCharacterService.listApproved(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

    @PostMapping("/list-module-super")
    public ResponseDto<PageDto<RdmsCharacterDto>> listModuleBySuper(
            @RequestBody PageDto<RdmsCharacterDto> pageDto) {
        TenantAccess.bindSelf(pageDto);
        ResponseDto<PageDto<RdmsCharacterDto>> responseDto = new ResponseDto<>();
        rdmsCharacterService.listModuleBySuper(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

    /**
     */
    @PostMapping("/getSetupListByProjectId")
    public ResponseDto<List<RdmsCharacterDto>> getSetupListByProjectId(@RequestParam String projectId, String loginUserId) {
        ResponseDto<List<RdmsCharacterDto>> responseDto = new ResponseDto<>();
        List<RdmsCharacterDto> rdmsProductCharacterDtos = rdmsCharacterService.getSetupListByProjectId(projectId, loginUserId);
        responseDto.setContent(rdmsProductCharacterDtos);
        return responseDto;
    }

    /**
     * 获取一个Character的全部信息
     */
    @PostMapping("/getHmiCharacterDocDto")
    public ResponseDto<RdmsHmiCharacterDocDto> getHmiCharacterDocDto(@RequestParam String characterId, String loginUserId) {
        ResponseDto<RdmsHmiCharacterDocDto> responseDto = new ResponseDto<>();
        RdmsHmiCharacterDocDto hmiCharacterDocDto = rdmsCharacterService.getHmiCharacterDocDto(characterId, loginUserId);
        responseDto.setContent(hmiCharacterDocDto);
        return responseDto;
    }

    /**
     *
     */
    @PostMapping("/getHmiAllAttachmentsOfCharacter/{characterId}")
    public ResponseDto<List<RdmsFileDto>> getHmiAllAttachmentsOfCharacter(@PathVariable String characterId) {
        ResponseDto<List<RdmsFileDto>> responseDto = new ResponseDto<>();
        List<RdmsFileDto> hmiAllAttachmentsOfCharacterDto = rdmsCharacterService.getHmiAllAttachmentsOfCharacter(characterId);
        responseDto.setContent(hmiAllAttachmentsOfCharacterDto);
        return responseDto;
    }

    /**
     *功能集成工单全部附件读取
     */
    @PostMapping("/getHmiAllAttachmentsOfIntCharacter/{characterId}")
    public ResponseDto<List<RdmsFileDto>> getHmiAllAttachmentsOfIntCharacter(@PathVariable String characterId) {
        ResponseDto<List<RdmsFileDto>> responseDto = new ResponseDto<>();
        List<RdmsFileDto> hmiAllAttachmentsOfIntCharacter = rdmsCharacterService.getHmiAllAttachmentsOfIntCharacter(characterId);
        responseDto.setContent(hmiAllAttachmentsOfIntCharacter);
        return responseDto;
    }


    @PostMapping("/listAllCharactersByProjectId")
    public ResponseDto<List<RdmsCharacterDto>> listAllCharactersByProjectId(@RequestParam String projectId, String loginUserId) {
        ResponseDto<List<RdmsCharacterDto>> responseDto = new ResponseDto<>();
        List<RdmsCharacterDto> rdmsProductCharacterDtos = rdmsCharacterService.listAllCharactersByProjectId(projectId, loginUserId);
        characterRichDocService.fillLockedFeatureNos(rdmsProductCharacterDtos);
        responseDto.setContent(rdmsProductCharacterDtos);
        return responseDto;
    }

    @PostMapping("/listAllCharactersBySubProjectId")
    public ResponseDto<List<RdmsCharacterDto>> listAllCharactersBySubProjectId(@RequestParam String subprojectId, String loginUserId) {
        ResponseDto<List<RdmsCharacterDto>> responseDto = new ResponseDto<>();
        List<RdmsCharacterDto> rdmsProductCharacterDtos = rdmsCharacterService.listAllCharactersBySubProjectId(subprojectId, loginUserId);
        characterRichDocService.fillLockedFeatureNos(rdmsProductCharacterDtos);
        responseDto.setContent(rdmsProductCharacterDtos);
        return responseDto;
    }

/*    @PostMapping("/listCharactersBySubprojectId/{subprojectId}")
    public ResponseDto<List<RdmsCharacterDto>> listCharactersBySubprojectId(@PathVariable String subprojectId) {
        ResponseDto<List<RdmsCharacterDto>> responseDto = new ResponseDto<>();
        List<RdmsCharacterDto> rdmsProductCharacterDtos = rdmsCharacterService.listCharactersBySubprojectId(subprojectId);
        responseDto.setContent(rdmsProductCharacterDtos);
        return responseDto;
    }*/

    @PostMapping("/getDecomposedChildrenCharacters")
    public ResponseDto<List<RdmsCharacterDto>> getDecomposedChildrenCharacters(@RequestParam String parentCharacterId, String loginUserId) {
        ResponseDto<List<RdmsCharacterDto>> responseDto = new ResponseDto<>();
        List<RdmsCharacterDto> rdmsProductCharacterDtos = rdmsCharacterService.getDecomposedChildrenCharacters(parentCharacterId, CharacterStatusEnum.SAVED.getStatus(), loginUserId);
        responseDto.setContent(rdmsProductCharacterDtos);
        return responseDto;
    }

    /**
     * 对jobitem表进行"简单"查询
     */
    @PostMapping("/getCharacterListAndAllDevelopJobitem")
    public ResponseDto<PageDto<RdmsCharacterDto>> getCharacterListAndAllDevelopJobitem(@RequestBody PageDto<RdmsCharacterDto> pageDto) {
        TenantAccess.bindSelf(pageDto);
        ResponseDto<PageDto<RdmsCharacterDto>> responseDto = new ResponseDto<>();
        rdmsCharacterService.getCharacterListAndAllDevelopJobitem(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

    @PostMapping("/getCharacterAndJobitemList/{characterId}")
    public ResponseDto<RdmsCharacterDto> getCharacterAndJobitemList(@PathVariable String characterId) {
        ResponseDto<RdmsCharacterDto> responseDto = new ResponseDto<>();
        RdmsCharacterDto characterAndJobitemList = rdmsCharacterService.getCharacterAndJobitemList(characterId);
        responseDto.setContent(characterAndJobitemList);
        return responseDto;
    }

    @PostMapping("/getCharacterAndTestTaskJobitemList/{characterId}")
    public ResponseDto<RdmsCharacterDto> getCharacterAndTestTaskJobitemList(@PathVariable String characterId) {
        ResponseDto<RdmsCharacterDto> responseDto = new ResponseDto<>();
        RdmsCharacterDto characterAndJobitemList = rdmsCharacterService.getCharacterAndTestTaskJobitemList(characterId);
        responseDto.setContent(characterAndJobitemList);
        return responseDto;
    }

    @PostMapping("/getCharacterListBySubprojectId/{subprojectId}")
    public ResponseDto<List<RdmsHmiCharacterPlainDto>> getCharacterListBySubprojectId(@PathVariable String subprojectId) {
        ResponseDto<List<RdmsHmiCharacterPlainDto>> responseDto = new ResponseDto<>();
        List<RdmsCharacterDto> characterList = rdmsCharacterService.getCharacterListBySubprojectId(subprojectId, null);
        if(!CollectionUtils.isEmpty(characterList)){
            List<RdmsHmiCharacterPlainDto> characterPlainList = characterList.stream().map(item -> new RdmsHmiCharacterPlainDto(item.getId(), item.getCharacterName())).collect(Collectors.toList());
            responseDto.setContent(characterPlainList);
        }
        return responseDto;
    }

    @PostMapping("/getArchivedCharacterListByCustomerId/{customerId}")
    public ResponseDto<List<RdmsHmiCharacterPlainDto>> getArchivedCharacterListByCustomerId(@PathVariable String customerId) {
        ResponseDto<List<RdmsHmiCharacterPlainDto>> responseDto = new ResponseDto<>();
        List<RdmsCharacterDto> characterList = rdmsCharacterService.getArchivedCharacterListByCustomerId(customerId);
        if(!CollectionUtils.isEmpty(characterList)){
            List<RdmsHmiCharacterPlainDto> characterPlainList = characterList.stream().map(item -> new RdmsHmiCharacterPlainDto(item.getId(), item.getCharacterName())).collect(Collectors.toList());
            responseDto.setContent(characterPlainList);
        }
        return responseDto;
    }

    @PostMapping("/getLastReviewInfo")
    public ResponseDto<RdmsHmiJobItemDocPageDto> getLastReviewInfo(@RequestParam String characterId, String loginUserId) {
        tenantParentGuard.requireCharacter(characterId);
        ResponseDto<RdmsHmiJobItemDocPageDto> responseDto = new ResponseDto<>();
        RdmsHmiJobItemDocPageDto lastReviewInfo = rdmsCharacterService.getLastReviewInfo(characterId, loginUserId);
        responseDto.setContent(lastReviewInfo);
        return responseDto;
    }

    @PostMapping("/getCharacterMaterialBudgetExeInfoList/{pjmId}")
    public ResponseDto<List<RdmsCharacterBudgeExeInfoDto>> getCharacterListByPjm(@PathVariable String pjmId) {
        pjmId = TenantAccess.resolveMemberId(pjmId);
        ResponseDto<List<RdmsCharacterBudgeExeInfoDto>> responseDto = new ResponseDto<>();
        List<RdmsCharacterDto> characterList = rdmsCharacterService.getCharacterListByPjmByMmStatus(pjmId, CharacterStatusEnum.NOTSET);
        List<RdmsCharacterBudgeExeInfoDto> characterBudgeExeInfoList = new ArrayList<>();
        if(!CollectionUtils.isEmpty(characterList)){
            for(RdmsCharacterDto characterDto: characterList){
                RdmsCharacterBudgeExeInfoDto characterMaterialBudgetExeInfo = rdmsManhourService.getCharacterMaterialBudgetExeInfo(characterDto.getId());
                characterBudgeExeInfoList.add(characterMaterialBudgetExeInfo);
            }
        }
        responseDto.setContent(characterBudgeExeInfoList);
        return responseDto;
    }

    @PostMapping("/getMaterialHasListFlag/{pjmId}")
    public ResponseDto<Boolean> getMaterialHasListFlag(@PathVariable String pjmId) {
        pjmId = TenantAccess.resolveMemberId(pjmId);
        ResponseDto<Boolean> responseDto = new ResponseDto<>();
        responseDto.setContent(rdmsMaterialManageService.hasListByNextNodeIdForApprover(pjmId));
        return responseDto;
    }

    @PostMapping("/getCharacterAndSubmitJobitems/{characterId}")
    public ResponseDto<RdmsCharacterDto> getCharacterAndSubmitJobitems(@PathVariable String characterId) {
        ResponseDto<RdmsCharacterDto> responseDto = new ResponseDto<>();
        RdmsCharacterDto characterAndJobitemList = rdmsCharacterService.getCharacterAndSubmitJobitems(characterId);
        responseDto.setContent(characterAndJobitemList);
        return responseDto;
    }

    @PostMapping("/getCharacterAndTestJobSubmitJobitems/{characterId}")
    public ResponseDto<RdmsCharacterDto> getCharacterAndTestJobSubmitJobitems(@PathVariable String characterId) {
        ResponseDto<RdmsCharacterDto> responseDto = new ResponseDto<>();
        RdmsCharacterDto characterAndJobitemList = rdmsCharacterService.getCharacterAndTestJobSubmitJobitems(characterId);
        responseDto.setContent(characterAndJobitemList);
        return responseDto;
    }

    @PostMapping("/getCharacterDevelopCompleteStatus/{characterId}")
    public ResponseDto<RdmsHmiCharacterCompleteStatusDto> getCharacterDevelopCompleteStatus(@PathVariable String characterId) {
        ResponseDto<RdmsHmiCharacterCompleteStatusDto> responseDto = new ResponseDto<>();
        RdmsHmiCharacterCompleteStatusDto characterDevelopCompleteInfoAndStatus = rdmsCharacterService.getCharacterDevelopCompleteInfoAndStatus(characterId);
        responseDto.setContent(characterDevelopCompleteInfoAndStatus);
        return responseDto;
    }

    /**
     * 查询依据CharacterId创建的工单, 并且这些工单处于submit状态
     */
    @PostMapping("/getNumOfReviewCharactersByNextNode/{nextNode}")
    public ResponseDto<Integer> getNumOfReviewCharactersByNextNode(@PathVariable String nextNode) {
        nextNode = TenantAccess.resolveMemberId(nextNode);
        ResponseDto<Integer> responseDto = new ResponseDto<>();
        Integer reviewJobNum = rdmsJobItemsService.countPendingReviewMainByNextNode(
                nextNode, JobItemTypeEnum.REVIEW, JobItemTypeEnum.REVIEW_CHANGE);
        responseDto.setContent(reviewJobNum);
        return responseDto;
    }


    /**
     * 查询依据CharacterId创建的工单, 并且这些工单处于submit状态
     */
    @PostMapping("/getUpdateCharacterNumByUserId/{userId}")
    public ResponseDto<Integer> getUpdateCharacterNumByUserId(@PathVariable String userId) {
        userId = TenantAccess.resolveMemberId(userId);
        ResponseDto<Integer> responseDto = new ResponseDto<>();
        List<CharacterStatusEnum> statusEnumList = new ArrayList<>();
        statusEnumList.add(CharacterStatusEnum.REVIEW);
        Integer updateCharacterNum = rdmsCharacterService.getNumOfUpdateCharactersByUserId(userId);
        responseDto.setContent(updateCharacterNum);
        return responseDto;
    }


    /**
     * 对jobitem表进行"简单"查询
     */
    @PostMapping("/getCharacterListAndAllTestJobitem")
    public ResponseDto<PageDto<RdmsCharacterDto>> getCharacterListAndAllTestJobitem(@RequestBody PageDto<RdmsCharacterDto> pageDto) {
        ResponseDto<PageDto<RdmsCharacterDto>> responseDto = new ResponseDto<>();
        List<JobItemTypeEnum> jobItemTypeEnumList = new ArrayList<>();
        jobItemTypeEnumList.add(JobItemTypeEnum.TEST);

        rdmsCharacterService.getDevelopCharacterAndJobitemListByMyCharacter(pageDto, JobItemStatusEnum.HANDLING.getStatus(), jobItemTypeEnumList);
        responseDto.setContent(pageDto);
        return responseDto;
    }
     /**
     * 对jobitem表进行"简单"查询
     */
    @PostMapping("/getCharacterListAndAllSubmitTestJobitem")
    public ResponseDto<PageDto<RdmsCharacterDto>> getCharacterListAndAllSubmitTestJobitem(@RequestBody PageDto<RdmsCharacterDto> pageDto) {
        ResponseDto<PageDto<RdmsCharacterDto>> responseDto = new ResponseDto<>();
        List<JobItemTypeEnum> jobItemTypeEnumList = new ArrayList<>();
        jobItemTypeEnumList.add(JobItemTypeEnum.TEST);
        rdmsCharacterService.getDevelopCharacterAndJobitemListByMyCharacter(pageDto, JobItemStatusEnum.SUBMIT.getStatus(), jobItemTypeEnumList);
        responseDto.setContent(pageDto);
        return responseDto;
    }
     /**
     * 对jobitem表进行"简单"查询
     */
    @PostMapping("/getDevelopingCharacterList")
    public ResponseDto<PageDto<RdmsCharacterDto>> getDevelopingCharacterList(@RequestBody PageDto<RdmsCharacterDto> pageDto) {
        TenantAccess.bindSelf(pageDto);
        ResponseDto<PageDto<RdmsCharacterDto>> responseDto = new ResponseDto<>();
        rdmsCharacterService.getDevelopingCharacterList(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }
    /**
     * 对jobitem表进行"简单"查询
     */
    @PostMapping("/getEvaluateJobitemList")
    public ResponseDto<PageDto<RdmsCharacterDto>> getEvaluateJobitemList(@RequestBody PageDto<RdmsCharacterDto> pageDto) {
        TenantAccess.bindSelf(pageDto);
        ResponseDto<PageDto<RdmsCharacterDto>> responseDto = new ResponseDto<>();
        rdmsCharacterService.getEvaluateJobitemList(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }
    /**
     * 对jobitem表进行"简单"查询
     */
    @PostMapping("/getTestingJobitemList")
    public ResponseDto<PageDto<RdmsCharacterDto>> getTestingJobitemList(@RequestBody PageDto<RdmsCharacterDto> pageDto) {
        TenantAccess.bindSelf(pageDto);
        ResponseDto<PageDto<RdmsCharacterDto>> responseDto = new ResponseDto<>();
        rdmsCharacterService.getTestingJobitemList(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }
    /**
     */
    @PostMapping("/getCharacterIntegrationJobList")
    public ResponseDto<PageDto<RdmsCharacterDto>> getCharacterIntegrationJobList(@RequestBody PageDto<RdmsCharacterDto> pageDto) {
        TenantAccess.bindSelf(pageDto);
        ResponseDto<PageDto<RdmsCharacterDto>> responseDto = new ResponseDto<>();
        rdmsCharacterService.getCharacterIntegrationJobList(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }
    /**
     */
    @PostMapping("/getCharacterIntSubmitOriginInfo/{characterId}")
    public ResponseDto<RdmsHmiCharacterIntOriginDto> getCharacterIntSubmitOriginInfo(@PathVariable String characterId) {
        ResponseDto<RdmsHmiCharacterIntOriginDto> responseDto = new ResponseDto<>();
        RdmsHmiCharacterIntOriginDto submitOriginDto = rdmsCharacterService.getCharacterIntSubmitOriginInfo(characterId);
        responseDto.setContent(submitOriginDto);
        return responseDto;
    }
    @PostMapping("/getIntegrationJobitemInfo/{characterId}")
    public ResponseDto<RdmsJobItemDto> getIntegrationJobitemInfo(@PathVariable String characterId) {
        ResponseDto<RdmsJobItemDto> responseDto = new ResponseDto<>();
        RdmsJobItemDto integrationJobitemInfo = rdmsCharacterService.getIntegrationJobitemInfo(characterId);
        responseDto.setContent(integrationJobitemInfo);
        return responseDto;
    }

    /**
     * 对jobitem表进行"简单"查询
     */
    @PostMapping("/getApprovedJobitemList")
    public ResponseDto<PageDto<RdmsCharacterDto>> getApprovedJobitemList(@RequestBody PageDto<RdmsCharacterDto> pageDto) {
        TenantAccess.bindSelf(pageDto);
        ResponseDto<PageDto<RdmsCharacterDto>> responseDto = new ResponseDto<>();
        rdmsCharacterService.getApprovedJobitemList(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }
    /**
     * 保存特性信息
     */
    @PostMapping("/save")
    @Transactional
    public ResponseDto<RdmsCharacterDto> save(@RequestBody RdmsCharacterDto characterDto) {
        RichTextFields.sanitize(characterDto);
        ResponseDto<RdmsCharacterDto> responseDto = new ResponseDto<>();

        ValidatorUtil.require(characterDto.getCustomerId(), "客户ID");
        ValidatorUtil.require(characterDto.getProductManagerId(), "产品经理");
        ValidatorUtil.require(characterDto.getCharacterName(), "特性名称");
        ValidatorUtil.require(characterDto.getFunctionDescription(), "特性描述");
        ValidatorUtil.require(characterDto.getWorkCondition(), "生效条件/应用范围");
        ValidatorUtil.require(characterDto.getInputLogicalDesc(), "输入逻辑/使用条件");
        ValidatorUtil.require(characterDto.getFunctionLogicalDesc(), "功能逻辑/功能要求");
        ValidatorUtil.require(characterDto.getOutputLogicalDesc(), "输出逻辑/交付指标");
        ValidatorUtil.require(characterDto.getWriterId(), "编制者");
        ValidatorUtil.require(characterDto.getProjectType(), "项目类型");

        ValidatorUtil.length(characterDto.getCharacterName(), "特性名称", 4, 80);
        ValidatorUtil.richLength(characterDto.getFunctionDescription(), "特性描述", 10, 5000);
        ValidatorUtil.richLength(characterDto.getWorkCondition(), "生效条件/应用范围", 10, 5000);
        ValidatorUtil.richLength(characterDto.getInputLogicalDesc(), "输入逻辑/使用条件", 10, 5000);
        ValidatorUtil.richLength(characterDto.getFunctionLogicalDesc(), "功能逻辑/功能要求", 10, 5000);
        ValidatorUtil.richLength(characterDto.getOutputLogicalDesc(), "输出逻辑/交付指标", 10, 5000);
        ValidatorUtil.richLength(characterDto.getTestMethod(), "验证方法/测试方法", 10, 5000);

        rdmsCharacterDefinitionService.save(characterDto);

        responseDto.setContent(characterDto);
        responseDto.setSuccess(true);
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    @PostMapping("/saveSuggestCharacterToSetuped")
    @Transactional
    public ResponseDto<RdmsCharacterDto> saveSuggestCharacterToSetuped(@RequestBody RdmsCharacterDto characterDto) {
        RichTextFields.sanitize(characterDto);
        ResponseDto<RdmsCharacterDto> responseDto = new ResponseDto<>();

        ValidatorUtil.require(characterDto.getId(), "特性建议ID");
        ValidatorUtil.require(characterDto.getMilestoneId(), "里程碑");

        RdmsCharacter character = rdmsProjectSetupService.appendSuggested(characterDto, characterDto.getLoginUserId());
        String characterId = character.getId();

        //将特性建议发起任务单标记为完成
        List<RdmsJobItem> suggestOriginJobitem = rdmsJobItemsService.getSuggestOriginJobitem(characterId);
        if(!CollectionUtils.isEmpty(suggestOriginJobitem)){
            for(RdmsJobItem jobItem: suggestOriginJobitem){
                if (JobItemStatusEnum.closedStatuses().contains(jobItem.getStatus())) {
                    continue;
                }
                jobItem.setStatus(JobItemStatusEnum.COMPLETED.getStatus());
                rdmsJobItemsService.update(jobItem);
            }
        }

        responseDto.setContent(characterDto);
        responseDto.setSuccess(true);
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    /**
     * 保存特性信息
     */
    @PostMapping("/saveRecordAndData")
    @Transactional
    public ResponseDto<RdmsCharacterDto> saveRecordAndData(@RequestBody RdmsCharacterDto characterDto) {
        RichTextFields.sanitize(characterDto);
        ResponseDto<RdmsCharacterDto> responseDto = new ResponseDto<>();
        if (JobItemTypeEnum.SUGGEST_UPDATE.getType().equals(characterDto.getJobitemType())) {
            throw new BusinessException(BusinessExceptionCode.JOB_TYPE_DEPRECATED);
        }

        ValidatorUtil.require(characterDto.getCustomerId(), "客户ID");
        ValidatorUtil.require(characterDto.getCharacterName(), "特性名称");
        ValidatorUtil.require(characterDto.getFunctionDescription(), "特性描述");
        ValidatorUtil.require(characterDto.getWorkCondition(), "生效条件/应用范围");
        ValidatorUtil.require(characterDto.getInputLogicalDesc(), "输入逻辑/使用条件");
        ValidatorUtil.require(characterDto.getFunctionLogicalDesc(), "功能逻辑/功能要求");
        ValidatorUtil.require(characterDto.getOutputLogicalDesc(), "输出逻辑/交付指标");
        ValidatorUtil.require(characterDto.getWriterId(), "编制者");
        ValidatorUtil.require(characterDto.getProjectType(), "项目类型");
        if(!JobItemTypeEnum.SUGGEST.getType().equals(characterDto.getJobitemType())){
            ValidatorUtil.require(characterDto.getProductManagerId(), "产品经理");
        }

        ValidatorUtil.length(characterDto.getCharacterName(), "特性名称", 4, 80);
        ValidatorUtil.richLength(characterDto.getFunctionDescription(), "特性描述", 10, 5000);
        ValidatorUtil.richLength(characterDto.getWorkCondition(), "生效条件/应用范围", 10, 5000);
        ValidatorUtil.richLength(characterDto.getInputLogicalDesc(), "输入逻辑/使用条件", 10, 5000);
        ValidatorUtil.richLength(characterDto.getFunctionLogicalDesc(), "功能逻辑/功能要求", 10, 5000);
        ValidatorUtil.richLength(characterDto.getOutputLogicalDesc(), "输出逻辑/交付指标", 10, 5000);
        ValidatorUtil.richLength(characterDto.getTestMethod(), "验证方法/测试方法", 10, 5000);

        String jsonString = JSON.toJSONString(characterDto.getFileIds());
        characterDto.setFileListStr(jsonString);

        List<RdmsDemandDto> demandList = characterDto.getDemandList();
        if(demandList != null){
            List<String> stringList = demandList.stream().map(RdmsDemandDto::getId).collect(Collectors.toList());
            String demandIdStr = JSON.toJSONString(stringList);
            characterDto.setDemandListStr(demandIdStr);
        }

        characterDto.setStatus(CharacterStatusEnum.SAVED.getStatus());
        characterDto.setNextNode(characterDto.getWriterId());  //保存的时候,下一节点是自己
        characterDto.setAuxStatus(CharacterStatusEnum.NOTSET.getStatus());  //保存的时候,下一节点是自己
        characterDto.setWriterId(characterDto.getLoginUserId());  //将编制人修改为自己

        if((!ObjectUtils.isEmpty(characterDto.getId())) && ObjectUtils.isEmpty(characterDto.getCharacterSerial())){
            characterDto.setCharacterSerial(characterDto.getId());
        }
        RdmsCharacter storedCharacter = ObjectUtils.isEmpty(characterDto.getId())
                ? null : rdmsCharacterService.selectByPrimaryKey(characterDto.getId());
        if (storedCharacter != null && JobItemTypeEnum.UPDATE.getType().equals(storedCharacter.getJobitemType())) {
            bindUpdateRecord(characterDto, storedCharacter);
        }
        rdmsCbbService.assertSelectableCbbs(characterDto.getCustomerId(),
                RdmsCbbService.parseModuleIds(characterDto.getModuleIdListStr()),
                RdmsCbbService.parseModuleIds(storedCharacter == null ? null : storedCharacter.getModuleIdListStr()),
                ObjectUtils.isEmpty(characterDto.getSubprojectId())
                        ? null : rdmsCbbService.moduleScopeOfSubproject(characterDto.getSubprojectId()));
        String savedCharacterId = rdmsCharacterService.saveRecordAndData(characterDto);


        if(! CollectionUtils.isEmpty(demandList)){
            for(RdmsDemandDto demandDto : demandList){
                rdmsDemandLifecycleService.completeAttached(demandDto.getId(), savedCharacterId);
            }
        } else if (!ObjectUtils.isEmpty(characterDto.getDemandListStr())) {
            // 由需求工单定义特性的页面只带 demandListStr
            List<String> demandIds = JSON.parseArray(characterDto.getDemandListStr(), String.class);
            if (demandIds != null) {
                for (String demandId : demandIds) {
                    rdmsDemandLifecycleService.completeAttachedIfOpen(demandId, savedCharacterId);
                }
            }
        }

        responseDto.setContent(characterDto);
        responseDto.setSuccess(true);
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    /** 迭代定义记录随迭代定义工单保存：工单须在执行人手中，归属与审批人以库中记录为准，请求体只承载可编辑字段。 */
    private void bindUpdateRecord(RdmsCharacterDto characterDto, RdmsCharacter stored) {
        TenantAccess.assertSameTenant(stored.getCustomerId());
        RdmsJobItem jobItem = ObjectUtils.isEmpty(stored.getJobitemId()) ? null
                : rdmsJobItemsService.selectByPrimaryKey(stored.getJobitemId());
        if (jobItem == null || !JobItemTypeEnum.UPDATE.getType().equals(jobItem.getType())) {
            throw new BusinessException(BusinessExceptionCode.DB_RECORD_NOT_EXIST);
        }
        if (!JobItemStatusEnum.HANDLING.getStatus().equals(jobItem.getStatus())) {
            throw new BusinessException(BusinessExceptionCode.JOB_ITEM_STATUS_INVALID);
        }
        jobActorGuard.assertActor(stored.getCustomerId(), characterDto.getLoginUserId(),
                jobItem.getExecutorId(), stored.getProductManagerId());
        String actorId = jobActorGuard.currentActor(characterDto.getLoginUserId());
        characterDto.setCustomerId(stored.getCustomerId());
        characterDto.setJobitemId(stored.getJobitemId());
        characterDto.setJobitemType(stored.getJobitemType());
        characterDto.setParent(stored.getParent());
        characterDto.setCharacterSerial(stored.getCharacterSerial());
        characterDto.setDeep(stored.getDeep());
        characterDto.setPreProjectId(stored.getPreProjectId());
        characterDto.setProductManagerId(stored.getProductManagerId());
        characterDto.setProjectId(stored.getProjectId());
        characterDto.setSubprojectId(stored.getSubprojectId());
        characterDto.setProjectType(stored.getProjectType());
        characterDto.setIterationVersion(stored.getIterationVersion());
        characterDto.setAuxStatus(stored.getAuxStatus());
        characterDto.setWriterId(actorId);
        characterDto.setNextNode(actorId);
    }

    @PostMapping("/getRecordAndData")
    @Transactional
    public ResponseDto<RdmsCharacterDto> getRecordAndData(@RequestParam String characterId, String loginUserId) {
        ResponseDto<RdmsCharacterDto> responseDto = new ResponseDto<>();
        RdmsCharacterDto recordAndData = new RdmsCharacterDto();
        if(characterId != null){
            recordAndData = rdmsCharacterService.getRecordAndData(characterId, loginUserId);
        }
        responseDto.setContent(recordAndData);
        return responseDto;
    }

    @PostMapping("/get-approved-items-num")
    public ResponseDto<Long> getApprovedItemsNum(@RequestBody RdmsRequestDto requestDto) {
        requestDto.setCustomerUserId(TenantAccess.resolveMemberId(requestDto.getCustomerUserId()));
        ResponseDto<Long> responseDto = new ResponseDto<>();
        long examineItemsNum = rdmsCharacterService.getApprovedItemsNum(requestDto.getCustomerId(), requestDto.getPreProjectId(), requestDto.getCustomerUserId());
        responseDto.setContent(examineItemsNum);
        return responseDto;
    }

    @Data
    private static class RespSuggestNum{
        private long suggestNum;
        private long materialAppNum;
        private long feeAppNum;
        private long bugfeedbackNum;
    }
    @PostMapping("/get-suggest-items-num/{customerUserId}")
    public ResponseDto<RespSuggestNum> getSuggestItemsNum(@PathVariable String customerUserId) {
        customerUserId = TenantAccess.resolveMemberId(customerUserId);
        ResponseDto<RespSuggestNum> responseDto = new ResponseDto<>();
        RespSuggestNum suggestNum = new RespSuggestNum();
        suggestNum.setSuggestNum(rdmsCharacterService.getSuggestNum(customerUserId));
        List<RdmsMaterialManageDto> materialApplicationList = rdmsMaterialManageService.getMaterialApplicationListByWriterId(customerUserId);
        suggestNum.setMaterialAppNum(materialApplicationList.size());
        List<RdmsFeeManageDto> feeApplicationList = rdmsFeeManageService.getFeeApplicationListByWriterId(customerUserId);
        suggestNum.setFeeAppNum(feeApplicationList.size());
        long bugRecheckNumByUserId = rdmsBugFeedbackService.getBugRecheckNumByUserId(customerUserId);
        suggestNum.setBugfeedbackNum(bugRecheckNumByUserId);

        responseDto.setContent(suggestNum);
        return responseDto;
    }

    /**
     * 保存客户信息
     */
    @PostMapping("/approve")
    @Transactional
    public ResponseDto<String> approve(@RequestBody RdmsCharacterProcessDto characterProcessDto) {
        ResponseDto<String> responseDto = new ResponseDto<>();

        ValidatorUtil.require(characterProcessDto.getCharacterId(), "特性ID");
        ValidatorUtil.require(characterProcessDto.getExecutorId(), "执行者ID");
        ValidatorUtil.require(characterProcessDto.getJobDescription(), "工作描述");
        ValidatorUtil.require(characterProcessDto.getCharacterBudget(), "审批工时");

        String respStr = rdmsCharacterService.approve(characterProcessDto);

        responseDto.setContent(respStr);
        responseDto.setSuccess(true);
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    @PostMapping("/close/{characterId}")
    @Transactional
    public ResponseDto<String> close(@PathVariable String characterId) {
        ResponseDto<String> responseDto = new ResponseDto<>();
        String respStr = rdmsCharacterService.close(characterId);
        responseDto.setContent(respStr);
        responseDto.setSuccess(true);
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    @PostMapping("/modifyCharactersToApprovedStatus")
    @Transactional
    public ResponseDto modifyCharactersToApprovedStatus(@RequestBody List<RdmsCharacterDto> characterDtos) {
        ResponseDto responseDto = new ResponseDto<>();

        if(! CollectionUtils.isEmpty(characterDtos)){
            for(RdmsCharacterDto characterDto: characterDtos){
                if (characterDto == null || ObjectUtils.isEmpty(characterDto.getId())) {
                    continue;
                }
                RdmsCharacter dbCharacter = rdmsCharacterService.selectByPrimaryKey(characterDto.getId());
                if (ObjectUtils.isEmpty(dbCharacter) || Integer.valueOf(1).equals(dbCharacter.getDeleted())) {
                    continue;
                }
                TenantAccess.assertSameTenant(dbCharacter.getCustomerId());
                if (!CharacterStatusEnum.SETUP.getStatus().equals(dbCharacter.getStatus())) {
                    continue;
                }
                RdmsProject ownerProject = ObjectUtils.isEmpty(dbCharacter.getProjectId())
                        ? null : rdmsProjectService.selectByPrimaryKey(dbCharacter.getProjectId());
                if (ownerProject != null && !ProjectStatusEnum.setupDetachSources().contains(ownerProject.getStatus())) {
                    continue;
                }
                // 退回立项中的特性限立项申请人与监管委员
                jobActorGuard.assertActor(dbCharacter.getCustomerId(), characterDto.getLoginUserId(),
                        ownerProject == null ? null : ownerProject.getCreaterId(),
                        ownerProject == null ? null : ownerProject.getSupervise());
                dbCharacter.setStatus(CharacterStatusEnum.APPROVED.getStatus());
                dbCharacter.setProjectId(null);
                dbCharacter.setMilestoneId(null);
                dbCharacter.setModuleIdListStr(null);
                rdmsCharacterService.update(dbCharacter);

                //添加一条CharacterProcess记录
                RdmsCharacterProcess characterProcess = new RdmsCharacterProcess();
                characterProcess.setCharacterId(dbCharacter.getId());
                characterProcess.setExecutorId(dbCharacter.getProductManagerId());  //理论上是监管委员, 但是,在没有进入正式立项前, 监管委员是没有的
                characterProcess.setNextNode(dbCharacter.getProductManagerId());
                long characterProcessCount = rdmsCharacterProcessService.getCharacterProcessCount(dbCharacter.getId());
                characterProcess.setDeep((int)characterProcessCount);
                characterProcess.setJobDescription("立项过程中退回产品经理");
                characterProcess.setProcessStatus(CharacterProcessStatusEnum.EXAMINE.getStatus());
                rdmsCharacterProcessService.save(characterProcess);
            }
        }
        responseDto.setSuccess(true);
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    /**
     * 保存客户信息
     */
    @PostMapping("/saveDecomposeCharacter")
    @Transactional
    public ResponseDto<RdmsCharacterDto> saveDecomposeCharacter(@RequestBody RdmsCharacterDto characterDto) {
        RichTextFields.sanitize(characterDto);
        ResponseDto<RdmsCharacterDto> responseDto = new ResponseDto<>();

        ValidatorUtil.require(characterDto.getCustomerId(), "客户ID");
        ValidatorUtil.require(characterDto.getProductManagerId(), "产品经理");
        ValidatorUtil.require(characterDto.getCharacterName(), "特性名称");
        ValidatorUtil.require(characterDto.getFunctionDescription(), "特性描述");
        ValidatorUtil.require(characterDto.getWorkCondition(), "生效条件/应用范围");
        ValidatorUtil.require(characterDto.getInputLogicalDesc(), "输入逻辑/使用条件");
        ValidatorUtil.require(characterDto.getFunctionLogicalDesc(), "功能逻辑/功能要求");
        ValidatorUtil.require(characterDto.getOutputLogicalDesc(), "输出逻辑/交付指标");
        ValidatorUtil.require(characterDto.getWriterId(), "编制者");
        ValidatorUtil.require(characterDto.getProjectType(), "项目类型");
        ValidatorUtil.require(characterDto.getParent(), "父级功能");

        ValidatorUtil.length(characterDto.getCharacterName(), "特性名称", 4, 80);
        ValidatorUtil.richLength(characterDto.getFunctionDescription(), "特性描述", 10, 5000);
        ValidatorUtil.richLength(characterDto.getWorkCondition(), "生效条件/应用范围", 10, 5000);
        ValidatorUtil.richLength(characterDto.getInputLogicalDesc(), "输入逻辑/使用条件", 10, 5000);
        ValidatorUtil.richLength(characterDto.getFunctionLogicalDesc(), "功能逻辑/功能要求", 10, 5000);
        ValidatorUtil.richLength(characterDto.getOutputLogicalDesc(), "输出逻辑/交付指标", 10, 5000);
        ValidatorUtil.richLength(characterDto.getTestMethod(), "验证方法/测试方法", 10, 5000);
        assertDecomposeEditable(characterDto);

        RdmsCharacter character = CopyUtil.copy(characterDto, RdmsCharacter.class);
        String jsonString = JSON.toJSONString(characterDto.getFileIds());
        character.setFileListStr(jsonString);

        List<RdmsDemandDto> demandList = characterDto.getDemandList();
        if(demandList != null){
            List<String> stringList = demandList.stream().map(RdmsDemandDto::getId).collect(Collectors.toList());
            String demandIdStr = JSON.toJSONString(stringList);
            character.setDemandListStr(demandIdStr);
        }

        character.setStatus(CharacterStatusEnum.SAVED.getStatus());
        character.setNextNode(characterDto.getLoginUserId());  //保存的时候,下一节点是自己
        character.setJobitemType(JobItemTypeEnum.DECOMPOSE.getType());
        //在审批之前,暂时填充审批预算字段
        character.setDevManhourApproved(character.getDevManhour());
        character.setTestManhourApproved(character.getTestManhour());
        character.setMaterialFeeApproved(character.getMaterialFee());
        character.setTestFee(character.getTestFee());
        character.setPowerFee(character.getPowerFee());
        character.setConferenceFee(character.getConferenceFee());
        character.setBusinessFee(character.getBusinessFee());
        character.setCooperationFee(character.getCooperationFee());
        character.setPropertyFee(character.getPropertyFee());
        character.setLaborFee(character.getLaborFee());
        character.setConsultingFee(character.getConsultingFee());
        character.setManagementFee(character.getManagementFee());

        RdmsManhourStandard standardManhour = rdmsManhourService.getStandardManhourByCustomerId(characterDto.getCustomerId());

        if(characterDto.getProjectType().equals(ProjectTypeEnum.DEV_PROJECT.getType())){
            double sumOtherFee = character.getTestFee().floatValue()
                    + character.getPowerFee().floatValue()
                    + character.getConferenceFee().floatValue()
                    + character.getBusinessFee().floatValue()
                    + character.getCooperationFee().floatValue()
                    + character.getPropertyFee().floatValue()
                    + character.getLaborFee().floatValue()
                    + character.getConsultingFee().floatValue()
                    + character.getManagementFee().floatValue();
            character.setSumOtherFee(BigDecimal.valueOf(sumOtherFee));

            double budget = character.getDevManhourApproved() * standardManhour.getDevManhourFee().floatValue()
                    + character.getTestManhourApproved() * standardManhour.getTestManhourFee().floatValue()
                    + character.getMaterialFeeApproved().floatValue()
                    + sumOtherFee;

            character.setBudget(BigDecimal.valueOf(budget));
        }

        RdmsCharacter parentCharacter = rdmsCharacterService.selectByPrimaryKey(characterDto.getParent());
        character.setPlanCompleteTime(parentCharacter.getPlanCompleteTime());
        character.setModuleIdListStr(parentCharacter.getModuleIdListStr());

        String childId = rdmsCharacterService.saveDecomposeCharacter(character, characterDto);
        characterDto.setId(childId);

        responseDto.setContent(characterDto);
        responseDto.setSuccess(true);
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    /** 分解子特性只能由分解工单执行人在工单处理中编辑；已有子特性须属于同一工单。 */
    private void assertDecomposeEditable(RdmsCharacterDto characterDto) {
        ValidatorUtil.require(characterDto.getJobitemId(), "分解工单ID");
        RdmsJobItem jobItem = rdmsJobItemsService.selectByPrimaryKey(characterDto.getJobitemId());
        if (jobItem == null || !JobItemTypeEnum.DECOMPOSE.getType().equals(jobItem.getType())) {
            throw new BusinessException(BusinessExceptionCode.DB_RECORD_NOT_EXIST);
        }
        TenantAccess.assertSameTenant(jobItem.getCustomerId());
        if (!JobItemStatusEnum.HANDLING.getStatus().equals(jobItem.getStatus())) {
            throw new BusinessException(BusinessExceptionCode.JOB_ITEM_STATUS_INVALID);
        }
        jobActorGuard.assertActor(jobItem.getCustomerId(), characterDto.getLoginUserId(), jobItem.getExecutorId());
        if (!ObjectUtils.isEmpty(characterDto.getId())) {
            RdmsCharacter stored = rdmsCharacterService.selectByPrimaryKey(characterDto.getId());
            if (stored != null && !jobItem.getId().equals(stored.getJobitemId())) {
                throw new BusinessException(BusinessExceptionCode.NO_PERMISSION);
            }
        }
        RdmsCharacter parent = rdmsCharacterService.selectByPrimaryKey(characterDto.getParent());
        if (parent == null || !jobItem.getCustomerId().equals(parent.getCustomerId())) {
            throw new BusinessException(BusinessExceptionCode.DB_RECORD_NOT_EXIST);
        }
        characterDto.setCustomerId(jobItem.getCustomerId());
    }


    /**
     * 保存或提交修订编辑记录 E：正文写入修订分支，E 只承载非正文字段（实现方案第 8.5.7、8.5.8 节）。
     * 提交后开发域由派发人修订确认（不发起变更评审），确认后另行纳入项目；预立项进入修订审批。
     */
    @PostMapping("/saveIterationCharacter")
    @Transactional
    public ResponseDto<RdmsCharacterDto> saveIterationCharacter(@RequestBody RdmsCharacterDto characterDto) {
        RichTextFields.sanitize(characterDto);
        ResponseDto<RdmsCharacterDto> responseDto = new ResponseDto<>();

        ValidatorUtil.require(characterDto.getCustomerId(), "客户ID");
        ValidatorUtil.require(characterDto.getProductManagerId(), "产品经理");
        ValidatorUtil.require(characterDto.getCharacterName(), "特性名称");
        ValidatorUtil.require(characterDto.getFunctionDescription(), "特性描述");
        ValidatorUtil.require(characterDto.getWorkCondition(), "生效条件/应用范围");
        ValidatorUtil.require(characterDto.getInputLogicalDesc(), "输入逻辑/使用条件");
        ValidatorUtil.require(characterDto.getFunctionLogicalDesc(), "功能逻辑/功能要求");
        ValidatorUtil.require(characterDto.getOutputLogicalDesc(), "输出逻辑/交付指标");
        ValidatorUtil.require(characterDto.getWriterId(), "编制者");
        ValidatorUtil.require(characterDto.getStatus(), "状态");
        ValidatorUtil.require(characterDto.getProjectType(), "项目类型");

        ValidatorUtil.length(characterDto.getCharacterName(), "特性名称", 4, 80);
        ValidatorUtil.richLength(characterDto.getFunctionDescription(), "特性描述", 10, 5000);
        ValidatorUtil.richLength(characterDto.getWorkCondition(), "生效条件/应用范围", 10, 5000);
        ValidatorUtil.richLength(characterDto.getInputLogicalDesc(), "输入逻辑/使用条件", 10, 5000);
        ValidatorUtil.richLength(characterDto.getFunctionLogicalDesc(), "功能逻辑/功能要求", 10, 5000);
        ValidatorUtil.richLength(characterDto.getOutputLogicalDesc(), "输出逻辑/交付指标", 10, 5000);
        ValidatorUtil.richLength(characterDto.getTestMethod(), "验证方法/测试方法", 10, 5000);

//        RdmsCharacter character = CopyUtil.copy(characterDto, RdmsCharacter.class);
        String jsonString = JSON.toJSONString(characterDto.getFileIds());
        characterDto.setFileListStr(jsonString);

        List<RdmsDemandDto> demandList = characterDto.getDemandList();
        if(demandList != null){
            List<String> stringList = demandList.stream().map(RdmsDemandDto::getId).collect(Collectors.toList());
            String demandIdStr = JSON.toJSONString(stringList);
            characterDto.setDemandListStr(demandIdStr);
        }

        ValidatorUtil.require(characterDto.getId(), "编辑记录ID");
        RdmsCharacter stored = rdmsCharacterService.selectByPrimaryKey(characterDto.getId());
        if (stored == null || Integer.valueOf(1).equals(stored.getDeleted())
                || !JobItemTypeEnum.ITERATION.getType().equals(stored.getJobitemType())) {
            throw new BusinessException(BusinessExceptionCode.DB_RECORD_NOT_EXIST);
        }
        TenantAccess.assertSameTenant(stored.getCustomerId());
        RdmsJobItem jobItem = ObjectUtils.isEmpty(stored.getJobitemId()) ? null
                : rdmsJobItemsService.selectByPrimaryKey(stored.getJobitemId());
        RdmsCharacter original = ObjectUtils.isEmpty(stored.getParent()) ? null
                : rdmsCharacterService.selectByPrimaryKey(stored.getParent());
        if (jobItem == null || original == null) {
            throw new BusinessException(BusinessExceptionCode.DB_RECORD_NOT_EXIST);
        }
        // E 与修订工单同进同退：工单不在执行人手中时不得保存或提交
        if (!JobItemStatusEnum.HANDLING.getStatus().equals(jobItem.getStatus())) {
            throw new BusinessException(BusinessExceptionCode.JOB_ITEM_STATUS_INVALID);
        }
        String subprojectManagerId = characterRichDocService.subprojectManager(original);
        jobActorGuard.assertActor(stored.getCustomerId(), characterDto.getLoginUserId(),
                jobItem.getExecutorId(), stored.getProductManagerId(), subprojectManagerId);
        String actorId = jobActorGuard.currentActor(characterDto.getLoginUserId());
        // 归属、审批人与工单关联以库中编辑记录为准，请求体只承载可编辑字段
        characterDto.setCustomerId(stored.getCustomerId());
        characterDto.setJobitemId(stored.getJobitemId());
        characterDto.setJobitemType(stored.getJobitemType());
        characterDto.setParent(stored.getParent());
        characterDto.setCharacterSerial(stored.getCharacterSerial());
        characterDto.setDeep(stored.getDeep());
        characterDto.setPreProjectId(stored.getPreProjectId());
        characterDto.setProductManagerId(stored.getProductManagerId());
        characterDto.setProjectId(stored.getProjectId());
        characterDto.setSubprojectId(stored.getSubprojectId());
        characterDto.setProjectType(stored.getProjectType());
        characterDto.setWriterId(stored.getWriterId());
        characterDto.setIterationVersion(stored.getIterationVersion());

        boolean preProject = ProjectTypeEnum.PRE_PROJECT.getType().equals(jobItem.getProjectType());
        if(characterDto.getStatus().equals(CharacterStatusEnum.SUBMIT.getStatus())){
            if (preProject) {
                RdmsPreProject rdmsPreProject = ObjectUtils.isEmpty(stored.getPreProjectId()) ? null
                        : rdmsPreProjectService.selectByPrimaryKey(stored.getPreProjectId());
                if (rdmsPreProject != null) {
                    characterDto.setNextNode(rdmsPreProject.getSystemManagerId());
                } else {
                    RdmsSysgmDto sysgmByCustomerId = rdmsSysgmService.getSysgmByCustomerId(stored.getCustomerId());
                    characterDto.setNextNode(sysgmByCustomerId == null ? null : sysgmByCustomerId.getSysgmId());
                }
            } else {
                characterDto.setNextNode(revisionConfirmer(jobItem.getId(), subprojectManagerId));
            }

            characterDto.setSubmitTime(new Date());

        }else{
            characterDto.setStatus(CharacterStatusEnum.SAVED.getStatus());
            characterDto.setNextNode(actorId);  //保存的时候,下一节点是自己
        }

        String characterId = rdmsCharacterService.saveRecordAndData(characterDto);

        if(characterDto.getStatus().equals(CharacterStatusEnum.SUBMIT.getStatus())){
            //创建CharacterProcess记录
            RdmsCharacterProcess characterProcess = new RdmsCharacterProcess();
            characterProcess.setCharacterId(characterId);
            characterProcess.setExecutorId(actorId);
            long characterProcessCount = rdmsCharacterProcessService.getCharacterProcessCount(characterId);
            characterProcess.setDeep((int)characterProcessCount);
            characterProcess.setJobDescription("提交特性定义表");
            characterProcess.setProcessStatus(CharacterProcessStatusEnum.SUBMIT.getStatus());
            characterProcess.setNextNode(characterDto.getNextNode());

            rdmsCharacterProcessService.save(characterProcess);

            //将工单的状态标记为提交submit
            jobItem.setStatus(JobItemStatusEnum.SUBMIT.getStatus());
            jobItem.setActualSubmissionTime(new Date());
            if (preProject) {
                RdmsPreProject rdmsPreProject = ObjectUtils.isEmpty(jobItem.getPreProjectId()) ? null
                        : rdmsPreProjectService.selectByPrimaryKey(jobItem.getPreProjectId());
                if (rdmsPreProject != null) {
                    jobItem.setProductManagerId(rdmsPreProject.getProductManagerId());
                    jobItem.setNextNode(rdmsPreProject.getSystemManagerId());
                }
            } else {
                RdmsProjectSubproject subproject = ObjectUtils.isEmpty(original.getSubprojectId()) ? null
                        : rdmsSubprojectService.selectByPrimaryKey(original.getSubprojectId());
                if (subproject != null) {
                    jobItem.setProductManagerId(subproject.getProductManagerId());
                    jobItem.setProjectManagerId(subproject.getProjectManagerId());
                }
                jobItem.setNextNode(characterDto.getNextNode());
            }
            String jobitemId = rdmsJobItemsService.update(jobItem);

            //添加工单过程记录
            List<String> characterIdList = new ArrayList<>();
            characterIdList.add(characterId);
            String characterIdStr = JSON.toJSONString(characterIdList);
            jobItemProcessRecorder.record(jobitemId, JobItemProcessStatusEnum.SUBMIT, actorId, jobItem.getExecutorId(),
                    "提交特性修订工单", null, jobItem.getActualSubmissionTime(), characterIdStr);
            characterRevisionFlowService.onSubmitted(characterId);
        }

        responseDto.setContent(characterDto);
        responseDto.setSuccess(true);
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    /** 开发域修订提交后由派发人确认；工单没有派发人时由子项目经理确认。 */
    private String revisionConfirmer(String jobitemId, String subprojectManagerId) {
        RdmsJobItem job = ObjectUtils.isEmpty(jobitemId) ? null : rdmsJobItemsService.selectByPrimaryKey(jobitemId);
        return job != null && !ObjectUtils.isEmpty(job.getCreaterId()) ? job.getCreaterId() : subprojectManagerId;
    }


    /**
     */
    @PostMapping("/saveMergeCharacter")
    @Transactional
    public ResponseDto<RdmsCharacterDto> saveMergeCharacter(@RequestBody RdmsCharacterDto characterDto) {
        RichTextFields.sanitize(characterDto);
        ResponseDto<RdmsCharacterDto> responseDto = new ResponseDto<>();

        ValidatorUtil.require(characterDto.getCustomerId(), "客户ID");
//        ValidatorUtil.require(characterDto.getProductManagerId(), "产品经理");
        ValidatorUtil.require(characterDto.getCharacterName(), "特性名称");
        ValidatorUtil.require(characterDto.getFunctionDescription(), "特性描述");
        ValidatorUtil.require(characterDto.getWorkCondition(), "生效条件/应用范围");
        ValidatorUtil.require(characterDto.getInputLogicalDesc(), "输入逻辑/使用条件");
        ValidatorUtil.require(characterDto.getFunctionLogicalDesc(), "功能逻辑/功能要求");
        ValidatorUtil.require(characterDto.getOutputLogicalDesc(), "输出逻辑/交付指标");
//        ValidatorUtil.require(characterDto.getWriterId(), "编制者");
        ValidatorUtil.require(characterDto.getStatus(), "状态");
        ValidatorUtil.require(characterDto.getProjectType(), "项目类型");


        ValidatorUtil.length(characterDto.getCharacterName(), "特性名称", 4, 80);
        ValidatorUtil.richLength(characterDto.getFunctionDescription(), "特性描述", 10, 5000);
        ValidatorUtil.richLength(characterDto.getWorkCondition(), "生效条件/应用范围", 10, 5000);
        ValidatorUtil.richLength(characterDto.getInputLogicalDesc(), "输入逻辑/使用条件", 10, 5000);
        ValidatorUtil.richLength(characterDto.getFunctionLogicalDesc(), "功能逻辑/功能要求", 10, 5000);
        ValidatorUtil.richLength(characterDto.getOutputLogicalDesc(), "输出逻辑/交付指标", 10, 5000);
        ValidatorUtil.richLength(characterDto.getTestMethod(), "验证方法/测试方法", 10, 5000);

        String jsonString = JSON.toJSONString(characterDto.getFileIds());
        characterDto.setFileListStr(jsonString);

        List<RdmsDemandDto> demandList = characterDto.getDemandList();
        if(demandList != null){
            List<String> stringList = demandList.stream().map(RdmsDemandDto::getId).collect(Collectors.toList());
            String demandIdStr = JSON.toJSONString(stringList);
            characterDto.setDemandListStr(demandIdStr);
        }

        ValidatorUtil.require(characterDto.getId(), "合并记录ID");
        RdmsCharacter stored = rdmsCharacterService.selectByPrimaryKey(characterDto.getId());
        if (stored == null || Integer.valueOf(1).equals(stored.getDeleted())
                || !JobItemTypeEnum.MERGE.getType().equals(stored.getJobitemType())) {
            throw new BusinessException(BusinessExceptionCode.DB_RECORD_NOT_EXIST);
        }
        TenantAccess.assertSameTenant(stored.getCustomerId());
        RdmsJobItem jobItem = ObjectUtils.isEmpty(stored.getJobitemId()) ? null
                : rdmsJobItemsService.selectByPrimaryKey(stored.getJobitemId());
        if (jobItem == null) {
            throw new BusinessException(BusinessExceptionCode.DB_RECORD_NOT_EXIST);
        }
        if (!JobItemStatusEnum.HANDLING.getStatus().equals(jobItem.getStatus())) {
            throw new BusinessException(BusinessExceptionCode.JOB_ITEM_STATUS_INVALID);
        }
        jobActorGuard.assertActor(stored.getCustomerId(), characterDto.getLoginUserId(), jobItem.getExecutorId());
        String actorId = jobActorGuard.currentActor(characterDto.getLoginUserId());
        // 归属、审批人、合并清单与工单关联以库中合并记录为准，请求体只承载可编辑字段
        characterDto.setCustomerId(stored.getCustomerId());
        characterDto.setJobitemId(stored.getJobitemId());
        characterDto.setJobitemType(stored.getJobitemType());
        characterDto.setParent(stored.getParent());
        characterDto.setCharacterSerial(stored.getCharacterSerial());
        characterDto.setDeep(stored.getDeep());
        characterDto.setPreProjectId(stored.getPreProjectId());
        characterDto.setProjectId(stored.getProjectId());
        characterDto.setSubprojectId(stored.getSubprojectId());
        characterDto.setProjectType(stored.getProjectType());
        characterDto.setIterationVersion(stored.getIterationVersion());
        characterDto.setMergedCharacterIdStr(stored.getMergedCharacterIdStr());
        characterDto.setWriterId(ObjectUtils.isEmpty(stored.getWriterId()) ? jobItem.getExecutorId() : stored.getWriterId());
        String productManagerId = stored.getProductManagerId();
        if (ObjectUtils.isEmpty(productManagerId) && !ObjectUtils.isEmpty(stored.getPreProjectId())) {
            RdmsPreProject preProject = rdmsPreProjectService.selectByPrimaryKey(stored.getPreProjectId());
            productManagerId = preProject == null ? null : preProject.getProductManagerId();
        }
        characterDto.setProductManagerId(productManagerId);

        boolean submit = CharacterStatusEnum.SUBMIT.getStatus().equals(characterDto.getStatus());
        if (submit) {
            ValidatorUtil.require(productManagerId, "产品经理");
            characterDto.setNextNode(productManagerId);
            characterDto.setSubmitTime(new Date());
        } else {
            characterDto.setStatus(CharacterStatusEnum.SAVED.getStatus());
            characterDto.setNextNode(actorId);
        }
        String characterId = rdmsCharacterService.saveRecordAndData(characterDto);

        if (submit) {
            RdmsCharacterProcess characterProcess = new RdmsCharacterProcess();
            characterProcess.setCharacterId(characterId);
            characterProcess.setExecutorId(actorId);
            long characterProcessCount = rdmsCharacterProcessService.getCharacterProcessCount(characterId);
            characterProcess.setDeep((int)characterProcessCount);
            characterProcess.setJobDescription("提交特性定义表");
            characterProcess.setProcessStatus(CharacterProcessStatusEnum.SUBMIT.getStatus());
            characterProcess.setNextNode(productManagerId);
            rdmsCharacterProcessService.save(characterProcess);

            jobItem.setStatus(JobItemStatusEnum.SUBMIT.getStatus());
            jobItem.setNextNode(productManagerId);
            jobItem.setProductManagerId(productManagerId);
            jobItem.setActualSubmissionTime(new Date());
            String jobitemId = rdmsJobItemsService.update(jobItem);

            List<String> characterIdList = new ArrayList<>();
            characterIdList.add(characterId);
            String characterIdStr = JSON.toJSONString(characterIdList);
            jobItemProcessRecorder.record(jobitemId, JobItemProcessStatusEnum.SUBMIT, actorId, productManagerId,
                    "提交特性合并工单", null, jobItem.getActualSubmissionTime(), characterIdStr);
        }

        responseDto.setContent(characterDto);
        responseDto.setSuccess(true);
        responseDto.setMessage("保存成功");
        return responseDto;
    }


    /**
     * 在立项审批时,驳回某个特性定义给审批人,重新处理
     */
    @PostMapping("/reject/{characterId}")
    @Transactional
    public ResponseDto reject(@PathVariable String characterId,
                              @RequestBody(required = false) RejectReasonDto rejectReasonDto) {
        ResponseDto responseDto = new ResponseDto<>();
        String rejectReason = RejectReasonDto.reasonOf(rejectReasonDto);
        ValidatorUtil.length(rejectReason, "驳回理由", 0, RejectReasonDto.MAX_LENGTH);
        rdmsCharacterDefinitionService.rejectDuringSetup(characterId, rejectReason);
        responseDto.setSuccess(true);
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    /**
     * 保存客户信息
     */
    @PostMapping("/submit")
    @Transactional
    public ResponseDto<RdmsCharacterDto> submit(@RequestBody RdmsCharacterDto characterDto) {
        RichTextFields.sanitize(characterDto);
        ResponseDto<RdmsCharacterDto> responseDto = new ResponseDto<>();

        ValidatorUtil.require(characterDto.getCustomerId(), "客户ID");
        ValidatorUtil.require(characterDto.getProductManagerId(), "产品经理");
        ValidatorUtil.require(characterDto.getCharacterName(), "特性名称");
        ValidatorUtil.require(characterDto.getFunctionDescription(), "特性描述");
        ValidatorUtil.require(characterDto.getWorkCondition(), "生效条件/应用范围");
        ValidatorUtil.require(characterDto.getInputLogicalDesc(), "输入逻辑/使用条件");
        ValidatorUtil.require(characterDto.getFunctionLogicalDesc(), "功能逻辑/功能要求");
        ValidatorUtil.require(characterDto.getOutputLogicalDesc(), "输出逻辑/交付指标");
        ValidatorUtil.require(characterDto.getWriterId(), "编制者");

        ValidatorUtil.length(characterDto.getCharacterName(), "特性名称", 4, 80);
        ValidatorUtil.richLength(characterDto.getFunctionDescription(), "特性描述", 10, 5000);
        ValidatorUtil.richLength(characterDto.getWorkCondition(), "生效条件/应用范围", 10, 5000);
        ValidatorUtil.richLength(characterDto.getInputLogicalDesc(), "输入逻辑/使用条件", 10, 5000);
        ValidatorUtil.richLength(characterDto.getFunctionLogicalDesc(), "功能逻辑/功能要求", 10, 5000);
        ValidatorUtil.richLength(characterDto.getOutputLogicalDesc(), "输出逻辑/交付指标", 10, 5000);
        ValidatorUtil.richLength(characterDto.getTestMethod(), "验证方法/测试方法", 10, 5000);

        rdmsCharacterDefinitionService.submit(characterDto);

        responseDto.setContent(characterDto);
        responseDto.setSuccess(true);
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    /**
     * 保存客户信息
     */
    @PostMapping("/saveDistribution")
    public ResponseDto<RdmsCharacterDto> saveDistribution(@RequestBody RdmsCharacterDto characterDto) {
        ResponseDto<RdmsCharacterDto> responseDto = new ResponseDto<>();
        rdmsSubprojectDecomposeService.distribute(characterDto);
        responseDto.setContent(characterDto);
        responseDto.setSuccess(true);
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    @PostMapping("/getCharacterTree/{characterId}")
    @Transactional
    public ResponseDto<RdmsCharacterInheritTreeDto> getCharacterTree(@PathVariable String characterId) {
        ResponseDto<RdmsCharacterInheritTreeDto> responseDto = new ResponseDto<>();
        RdmsCharacterInheritTreeDto characterInheritTree = rdmsCharacterService.getCharacterInheritTree(characterId);
        responseDto.setContent(characterInheritTree);
        responseDto.setSuccess(true);
        return responseDto;
    }

    /**
     * 保存客户信息
     */
    @PostMapping("/saveIterationCharacterRecord")
    public ResponseDto<String> saveIterationCharacterRecord(@RequestBody RdmsRequestDto requestDto) {
        ResponseDto<String> responseDto = new ResponseDto<>();
        responseDto.setContent(rdmsCharacterRevisionService.saveIterationCharacterRecord(
                requestDto.getCharacterId(), requestDto.getPreProjectId()));
        return responseDto;
    }

    /**
     * 项目级版本迭代：新预立项，旧项目记版本更新。
     */
    @PostMapping("/saveProjectUpdate")
    public ResponseDto<String> saveProjectUpdate(@RequestBody RdmsPreProjectDto preProjectDto) {
        ResponseDto<String> responseDto = new ResponseDto<>();
        responseDto.setContent(rdmsCharacterRevisionService.saveProjectUpdate(preProjectDto));
        return responseDto;
    }

    @PostMapping("/terminate/apply/{characterId}")
    public ResponseDto<String> applyTerminate(@PathVariable String characterId, @RequestParam String reason) {
        ResponseDto<String> responseDto = new ResponseDto<>();
        responseDto.setContent(rdmsCharacterTerminateService.apply(characterId, reason));
        responseDto.setMessage("终止申请已提交产品经理审批");
        return responseDto;
    }

    @PostMapping("/terminate/approve/{jobitemId}")
    public ResponseDto<String> approveTerminate(@PathVariable String jobitemId) {
        ResponseDto<String> responseDto = new ResponseDto<>();
        responseDto.setContent(rdmsCharacterTerminateService.approve(jobitemId));
        responseDto.setMessage("特性已终止");
        return responseDto;
    }

    @PostMapping("/terminate/reject/{jobitemId}")
    public ResponseDto<String> rejectTerminate(@PathVariable String jobitemId,
                                               @RequestParam(required = false) String reason) {
        ResponseDto<String> responseDto = new ResponseDto<>();
        responseDto.setContent(rdmsCharacterTerminateService.reject(jobitemId, reason));
        responseDto.setMessage("终止申请未批准");
        return responseDto;
    }

    @PostMapping("/review-opinion/{characterId}")
    public ResponseDto<String> reviewOpinion(@PathVariable String characterId, @RequestParam String opinion) {
        ResponseDto<String> responseDto = new ResponseDto<>();
        characterReviewOpinionService.submit(characterId, opinion);
        responseDto.setContent(characterId);
        responseDto.setMessage("审核意见已记录");
        return responseDto;
    }

    /**
     * 保存核定工时
     */
    @PostMapping("/save-confirm-manhour")
    @Transactional
    public ResponseDto<RdmsCharacterDto> saveConfirmManhour(@RequestBody RdmsCharacterDto characterDto) {
        ResponseDto<RdmsCharacterDto> responseDto = new ResponseDto<>();
        ValidatorUtil.require(characterDto.getId(), "特性ID");
        ValidatorUtil.require(characterDto.getDevManhourApproved(), "核定开发工时");
        ValidatorUtil.require(characterDto.getTestManhourApproved(), "核定测试工时");
        RdmsCharacter character = rdmsCharacterService.selectByPrimaryKey(characterDto.getId());
        if (character == null || Integer.valueOf(1).equals(character.getDeleted())) {
            throw new BusinessException(BusinessExceptionCode.DB_RECORD_NOT_EXIST);
        }
        TenantAccess.assertSameTenant(character.getCustomerId());
        RdmsProjectSubproject ownerSubproject = ObjectUtils.isEmpty(character.getSubprojectId())
                ? null : rdmsSubprojectService.selectByPrimaryKey(character.getSubprojectId());
        // 立项阶段特性尚无子项目，由项目监管委员核定预算
        RdmsProject ownerProject = ObjectUtils.isEmpty(character.getProjectId())
                ? null : rdmsProjectService.selectByPrimaryKey(character.getProjectId());
        jobActorGuard.assertActor(character.getCustomerId(), characterDto.getLoginUserId(),
                ownerSubproject == null ? null : ownerSubproject.getProjectManagerId(),
                ownerProject == null ? null : ownerProject.getSupervise());
        zeroNullFees(characterDto);
        character.setDevManhourApproved(characterDto.getDevManhourApproved());
        character.setTestManhourApproved(characterDto.getTestManhourApproved());
        character.setMaterialFeeApproved(characterDto.getMaterialFeeApproved());
        character.setTestFee(characterDto.getTestFee());
        character.setPowerFee(characterDto.getPowerFee());
        character.setConferenceFee(characterDto.getConferenceFee());
        character.setBusinessFee(characterDto.getBusinessFee());
        character.setCooperationFee(characterDto.getCooperationFee());
        character.setPropertyFee(characterDto.getPropertyFee());
        character.setLaborFee(characterDto.getLaborFee());
        character.setConsultingFee(characterDto.getConsultingFee());
        character.setManagementFee(characterDto.getManagementFee());

        RdmsManhourStandard manhourByCustomerId = rdmsManhourService.getStandardManhourByCustomerId(character.getCustomerId());
        if (manhourByCustomerId == null || manhourByCustomerId.getDevManhourFee() == null
                || manhourByCustomerId.getTestManhourFee() == null) {
            throw new ValidatorException("工时标准未配置");
        }

        double sumOtherFee = character.getTestFee().floatValue()
                + character.getPowerFee().floatValue()
                + character.getConferenceFee().floatValue()
                + character.getBusinessFee().floatValue()
                + character.getCooperationFee().floatValue()
                + character.getPropertyFee().floatValue()
                + character.getLaborFee().floatValue()
                + character.getConsultingFee().floatValue()
                + character.getManagementFee().floatValue();
        character.setSumOtherFee(BigDecimal.valueOf(sumOtherFee));

        double budget = character.getDevManhourApproved() * manhourByCustomerId.getDevManhourFee().floatValue()
                + character.getTestManhourApproved() * manhourByCustomerId.getTestManhourFee().floatValue()
                + character.getMaterialFeeApproved().floatValue()
                + sumOtherFee;

        character.setBudget(BigDecimal.valueOf(budget));

        rdmsCharacterService.update(character);

        responseDto.setContent(characterDto);
        responseDto.setSuccess(true);
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    private static void zeroNullFees(RdmsCharacterDto dto) {
        if (dto.getMaterialFeeApproved() == null) dto.setMaterialFeeApproved(BigDecimal.ZERO);
        if (dto.getTestFee() == null) dto.setTestFee(BigDecimal.ZERO);
        if (dto.getPowerFee() == null) dto.setPowerFee(BigDecimal.ZERO);
        if (dto.getConferenceFee() == null) dto.setConferenceFee(BigDecimal.ZERO);
        if (dto.getBusinessFee() == null) dto.setBusinessFee(BigDecimal.ZERO);
        if (dto.getCooperationFee() == null) dto.setCooperationFee(BigDecimal.ZERO);
        if (dto.getPropertyFee() == null) dto.setPropertyFee(BigDecimal.ZERO);
        if (dto.getLaborFee() == null) dto.setLaborFee(BigDecimal.ZERO);
        if (dto.getConsultingFee() == null) dto.setConsultingFee(BigDecimal.ZERO);
        if (dto.getManagementFee() == null) dto.setManagementFee(BigDecimal.ZERO);
    }


    /**
     * 删除
     */
    @DeleteMapping("/del/{id}")
    public ResponseDto delete(@PathVariable String id) {
        ResponseDto responseDto = new ResponseDto();
        rdmsCharacterService.delete(id);
        return responseDto;
    }

    /** 按序列号列出特性版本：版本、内容版本、产生方式、时间、操作人、是否项目锁定。 */
    @GetMapping("/version/list/{characterId}")
    public ResponseDto<List<Map<String, Object>>> versionList(@PathVariable String characterId) {
        ResponseDto<List<Map<String, Object>>> responseDto = new ResponseDto<>();
        responseDto.setContent(characterVersionService.listForCharacter(characterId));
        return responseDto;
    }
}
