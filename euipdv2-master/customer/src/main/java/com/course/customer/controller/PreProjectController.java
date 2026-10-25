/**
 * Author：周朔鹏
 * 编制时间：2025-08-05

 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */

package com.course.customer.controller;

import com.course.server.domain.RdmsCustomerUser;
import com.course.server.domain.RdmsPreProject;
import com.course.server.domain.RdmsProduct;
import com.course.server.dto.PageDto;
import com.course.server.dto.ResponseDto;
import com.course.server.dto.rdms.*;
import com.course.server.enums.rdms.BudgetTypeEnum;
import com.course.server.enums.rdms.JobItemStatusEnum;
import com.course.server.enums.rdms.JobItemTypeEnum;
import com.course.server.exception.BusinessException;
import com.course.server.exception.BusinessExceptionCode;
import com.course.server.security.tenant.TenantAccess;
import com.course.server.service.rdms.*;
import com.course.server.util.CopyUtil;
import com.course.server.util.ValidatorUtil;
import org.springframework.util.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/pre-project")
public class PreProjectController {
    private static final Logger LOG = LoggerFactory.getLogger(PreProjectController.class);
    public static final String BUSINESS_NAME = "预立项管理";

    @Resource
    private RdmsPreProjectService rdmsPreProjectService;
    @Resource
    private RdmsJobItemService rdmsJobItemService;
    @Resource
    private RdmsDemandService rdmsDemandService;
    @Resource
    private RdmsCustomerUserService rdmsCustomerUserService;
    @Autowired
    private RdmsBudgetService rdmsBudgetService;
    @Autowired
    private RdmsCustomerService rdmsCustomerService;
    @Autowired
    private RdmsBossService rdmsBossService;
    @Autowired
    private RdmsMaterialManageService rdmsMaterialManageService;
    @Autowired
    private RdmsFeeManageService rdmsFeeManageService;
    @Autowired
    private RdmsSysgmService rdmsSysgmService;
    @Resource
    private RdmsProductService rdmsProductService;

    @PostMapping("/list")
    public ResponseDto<PageDto<RdmsPreProjectDto>> list(@RequestBody PageDto<RdmsPreProjectDto> pageDto) {
        TenantAccess.bindSelf(pageDto);
        ResponseDto<PageDto<RdmsPreProjectDto>> responseDto = new ResponseDto<>();
        rdmsPreProjectService.list(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

    @PostMapping("/getPreProjectAndTaskJobitems_submit/{preProjectId}")
    public ResponseDto<RdmsHmiProjectAndJobItemMixDto> getPreProjectAndTaskJobitems_submit(@PathVariable String preProjectId) {
        ResponseDto<RdmsHmiProjectAndJobItemMixDto> responseDto = new ResponseDto<>();
        RdmsHmiProjectAndJobItemMixDto projectAndTaskJobitems = rdmsPreProjectService.getPreProjectAndTaskJobitems_submit(preProjectId);
        responseDto.setContent(projectAndTaskJobitems);
        return responseDto;
    }

    @PostMapping("/getPreProjectHasListFlag/{userId}")
    public ResponseDto<Boolean> getPreProjectHasListFlag(@PathVariable String userId) {
        userId = TenantAccess.resolveMemberId(userId);
        ResponseDto<Boolean> responseDto = new ResponseDto<>();
        Integer countOfPreProjectByPdm = rdmsPreProjectService.getCountOfPreProjectByPdm(userId);
        responseDto.setContent(countOfPreProjectByPdm >0);
        return responseDto;
    }

    @PostMapping("/getPreProjectListBySysgm/{customerId}")
    public ResponseDto<List<RdmsPreProjectDto>> getPreProjectListBySysgm(@PathVariable String customerId) {
        ResponseDto<List<RdmsPreProjectDto>> responseDto = new ResponseDto<>();
        List<RdmsPreProjectDto> preProjectListBySysgm = rdmsPreProjectService.getPreProjectListBySysgm(customerId);
        if(!CollectionUtils.isEmpty(preProjectListBySysgm)){
            RdmsBossDto boss = rdmsBossService.getBossByCustomerId(customerId);
            String bossId = boss == null ? null : boss.getBossId();
            RdmsCustomerUser bossUser = ObjectUtils.isEmpty(bossId) ? null : rdmsCustomerUserService.selectByPrimaryKey(bossId);
            RdmsSysgmDto sysgm = rdmsSysgmService.getSysgmByCustomerId(customerId);
            String sysgmId = sysgm == null || ObjectUtils.isEmpty(sysgm.getSysgmId()) ? bossId : sysgm.getSysgmId();
            for(RdmsPreProjectDto rdmsPreProjectDto : preProjectListBySysgm){
                rdmsPreProjectDto.setBossName(bossUser == null ? null : bossUser.getTrueName());
                rdmsPreProjectDto.setBossId(bossId);
                rdmsPreProjectDto.setSysgmId(sysgmId);

                String productManagerId = rdmsPreProjectDto.getProductManagerId();
                if(productManagerId != null){
                    RdmsCustomerUser productManager = rdmsCustomerUserService.selectByPrimaryKey(productManagerId);
                    rdmsPreProjectDto.setProductManagerName(productManager.getTrueName());
                }

                String systemManagerId = rdmsPreProjectDto.getSystemManagerId();
                if(systemManagerId != null){
                    RdmsCustomerUser systemManager = rdmsCustomerUserService.selectByPrimaryKey(systemManagerId);
                    rdmsPreProjectDto.setSystemManagerName(systemManager.getTrueName());
                }

                //获取submit的任务数量
                List<JobItemTypeEnum> jobitemTypeList = new ArrayList<>();
                jobitemTypeList.add(JobItemTypeEnum.TASK_FUNCTION);
                List<RdmsJobItemDto> jobitemList = rdmsJobItemService.getJobitemListByPreProjectIdAndStatusAndTypeList(rdmsPreProjectDto.getId(), JobItemStatusEnum.SUBMIT.getStatus(), jobitemTypeList);
                if(!CollectionUtils.isEmpty(jobitemList)){
                    rdmsPreProjectDto.setTaskSubmitJobNum(jobitemList.size());
                }

                Integer materialApplicationNum = rdmsMaterialManageService.getApplicationNumByPreProjectId(rdmsPreProjectDto.getId());
                rdmsPreProjectDto.setMaterialAppNum(materialApplicationNum);
                Integer feeApplicationNum = rdmsFeeManageService.getApplicationNumByPreProjectId(rdmsPreProjectDto.getId());
                rdmsPreProjectDto.setFeeAppNum(feeApplicationNum);
            }
        }
        responseDto.setContent(preProjectListBySysgm);
        return responseDto;
    }

    @PostMapping("/getPreProjectListByPdm")
    public ResponseDto<List<RdmsPreProjectDto>> getPreProjectListByPdm(@RequestParam String pdmId, String customerId) {
        ResponseDto<List<RdmsPreProjectDto>> responseDto = new ResponseDto<>();
        List<RdmsPreProjectDto> preProjectList = rdmsPreProjectService.getPreProjectListByPdm(pdmId, customerId);

        responseDto.setContent(preProjectList);
        return responseDto;
    }

    @PostMapping("/getPreProjectListBySm")
    public ResponseDto<List<RdmsPreProjectDto>> getPreProjectListBySm(@RequestParam String customerId, String loginUserId) {
        ResponseDto<List<RdmsPreProjectDto>> responseDto = new ResponseDto<>();
        List<RdmsPreProjectDto> preProjectListBySm = rdmsPreProjectService.getPreProjectListBySm(customerId, loginUserId);
        if(!CollectionUtils.isEmpty(preProjectListBySm)){
            for(RdmsPreProjectDto rdmsPreProjectDto : preProjectListBySm){
                String productManagerId = rdmsPreProjectDto.getProductManagerId();
                if(productManagerId != null){
                    RdmsCustomerUser productManager = rdmsCustomerUserService.selectByPrimaryKey(productManagerId);
                    rdmsPreProjectDto.setProductManagerName(productManager.getTrueName());
                }

                String systemManagerId = rdmsPreProjectDto.getSystemManagerId();
                if(systemManagerId != null){
                    RdmsCustomerUser systemManager = rdmsCustomerUserService.selectByPrimaryKey(systemManagerId);
                    rdmsPreProjectDto.setSystemManagerName(systemManager.getTrueName());
                }

                //获取submit的任务数量
                List<JobItemTypeEnum> jobitemTypeList = new ArrayList<>();
                jobitemTypeList.add(JobItemTypeEnum.TASK_FUNCTION);
                List<RdmsJobItemDto> jobitemList = rdmsJobItemService.getJobitemListByPreProjectIdAndStatusAndTypeList(rdmsPreProjectDto.getId(), JobItemStatusEnum.SUBMIT.getStatus(), jobitemTypeList);
                if(!CollectionUtils.isEmpty(jobitemList)){
                    rdmsPreProjectDto.setTaskSubmitJobNum(jobitemList.size());
                }
            }
        }
        responseDto.setContent(preProjectListBySm);
        return responseDto;
    }

    @PostMapping("/list/{customerId}")
    public ResponseDto<List<RdmsPreProjectDto>> listByCustomerId(@PathVariable String customerId) {
        ResponseDto<List<RdmsPreProjectDto>> responseDto = new ResponseDto<>();
        List<RdmsPreProjectDto> rdmsProjectPrepareDtos = rdmsPreProjectService.listByCustomerId(customerId);
        responseDto.setContent(rdmsProjectPrepareDtos);
        return responseDto;
    }

    @PostMapping("/getPreProjectSimpleInfo/{preProjectId}")
    public ResponseDto<RdmsPreProjectDto> getPreProjectSimpleInfo(@PathVariable String preProjectId) {
        ResponseDto<RdmsPreProjectDto> responseDto = new ResponseDto<>();
        RdmsPreProjectDto rdmsProjectPrepareDto = rdmsPreProjectService.getPreProjectSimpleInfo(preProjectId);
        responseDto.setContent(rdmsProjectPrepareDto);
        return responseDto;
    }

    @PostMapping("/getPreProjectAndTaskJobitems/{preProjectId}")
    public ResponseDto<RdmsHmiProjectAndJobItemMixDto> getPreProjectAndTaskJobitems(@PathVariable String preProjectId) {
        ResponseDto<RdmsHmiProjectAndJobItemMixDto> responseDto = new ResponseDto<>();
        RdmsHmiProjectAndJobItemMixDto projectAndTaskJobitems = rdmsPreProjectService.getPreProjectAndTaskJobitems(preProjectId);
        responseDto.setContent(projectAndTaskJobitems);
        return responseDto;
    }

    @PostMapping("/getModulePreProject/{customerId}")
    public ResponseDto<RdmsPreProjectDto> getModulePreProject(@PathVariable String customerId) {
        ResponseDto<RdmsPreProjectDto> responseDto = new ResponseDto<>();
        List<RdmsPreProject> modulePreProject = rdmsPreProjectService.getModulePreProject(customerId);
        if (!CollectionUtils.isEmpty(modulePreProject)) {
            RdmsPreProjectDto preProjectDto = CopyUtil.copy(modulePreProject.get(0), RdmsPreProjectDto.class);
            RdmsCustomerUser rdmsCustomerUser = rdmsCustomerUserService.selectByPrimaryKey(preProjectDto.getProductManagerId());
            preProjectDto.setProductManagerName(rdmsCustomerUser.getTrueName());
            responseDto.setContent(preProjectDto);
        }else{
            responseDto.setContent(null);
        }
        return responseDto;
    }

    @PostMapping("/getCharacterJobNotify")
    public ResponseDto<RdmsHmiNotifyDto> getCharacterJobNotify(@RequestBody RdmsHmiNotifyDto notifyDto) {
        notifyDto.setActorId(TenantAccess.resolveMemberId(notifyDto.getActorId()));
        ResponseDto<RdmsHmiNotifyDto> responseDto = new ResponseDto<>();
        RdmsHmiNotifyDto characterJobNotify = rdmsJobItemService.getCharacterJobNotify(notifyDto);
        responseDto.setContent(characterJobNotify);
        return responseDto;
    }

    @PostMapping("/getDemandJobNotify")
    public ResponseDto<RdmsHmiNotifyDto> getDemandJobNotify(@RequestBody RdmsHmiNotifyDto notifyDto) {
        notifyDto.setActorId(TenantAccess.resolveMemberId(notifyDto.getActorId()));
        ResponseDto<RdmsHmiNotifyDto> responseDto = new ResponseDto<>();
        RdmsHmiNotifyDto characterJobNotify = rdmsDemandService.getDemandJobNotify(notifyDto);
        responseDto.setContent(characterJobNotify);
        return responseDto;
    }

    /**
     * 保存客户信息
     */
    @PostMapping("/save")
    @Transactional
    public ResponseDto<RdmsPreProjectDto> save(@RequestBody RdmsPreProjectDto projectPrepareDto) {
        ResponseDto<RdmsPreProjectDto> responseDto = new ResponseDto<>();

        ValidatorUtil.require(projectPrepareDto.getPreProjectName(), "项目名称");
        ValidatorUtil.require(projectPrepareDto.getPreProjectIntroduce(), "项目描述");
        ValidatorUtil.require(projectPrepareDto.getDevVersion(), "开发版本");

        ValidatorUtil.length(projectPrepareDto.getPreProjectName(), "项目名称", 2, 50);
        ValidatorUtil.length(projectPrepareDto.getPreProjectIntroduce(), "项目描述", 5, 500);

        responseDto.setContent(rdmsPreProjectService.createWithBudget(projectPrepareDto));
        responseDto.setSuccess(true);
        responseDto.setMessage("保存成功");
        return responseDto;
    }

    /**
     * 删除（软删 → lifecycleStatus=CLOSED）
     */
    @DeleteMapping("/del/{id}")
    public ResponseDto delete(@PathVariable String id) {
        ResponseDto responseDto = new ResponseDto();
        rdmsPreProjectService.delete(id);
        return responseDto;
    }

    /**
     * 显式关闭预立项（有未关闭立项时拒绝）。cascade=true 时一并取消未完成工单并废弃未立项特性。
     */
    @PostMapping("/close/{id}")
    @Transactional
    public ResponseDto<String> close(@PathVariable String id,
                                     @RequestParam(required = false, defaultValue = "false") boolean cascade) {
        ResponseDto<String> responseDto = new ResponseDto<>();
        ValidatorUtil.require(id, "预立项ID");
        RdmsPreProject pre = rdmsPreProjectService.selectByPrimaryKey(id);
        if (ObjectUtils.isEmpty(pre)) {
            throw new BusinessException(BusinessExceptionCode.DB_RECORD_NOT_EXIST);
        }
        TenantAccess.assertSameTenant(pre.getCustomerId());
        rdmsPreProjectService.close(id, cascade);
        responseDto.setContent(id);
        responseDto.setSuccess(true);
        responseDto.setMessage("关闭成功");
        return responseDto;
    }

    /** 所属产品候选：本机构未删除、未停产的产品，供立项、预立项选择所属产品。 */
    @GetMapping("/product/list")
    public ResponseDto<List<RdmsProduct>> productOptions() {
        ResponseDto<List<RdmsProduct>> responseDto = new ResponseDto<>();
        responseDto.setContent(rdmsProductService.listSelectable(TenantAccess.requireTenantId()));
        return responseDto;
    }
}
