/**
 * Author：周朔鹏
 * 编制时间：2025-08-09

 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */

package com.course.system.controller.rdms;

import com.course.server.constants.Constants;
import com.course.server.exception.BusinessExceptionCode;
import com.course.server.exception.BusinessException;
import com.course.server.domain.RdmsBoss;
import com.course.server.domain.RdmsCustomer;
import com.course.server.domain.RdmsCustomerUser;
import com.course.server.domain.RdmsDepartment;
import com.course.server.dto.*;
import com.course.server.dto.rdms.*;
import com.course.server.service.rdms.*;
import com.course.server.security.tenant.TenantAccess;
import com.course.server.security.tenant.TenantContext;
import com.course.server.security.tenant.TenantScope;
import com.course.server.util.CopyUtil;
import com.course.server.util.ValidatorUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/rdms/customer")
public class RdmsCustomerController {

    private static final Logger LOG = LoggerFactory.getLogger(RdmsCustomerController.class);
    public static final String BUSINESS_NAME = "企业客户";

    @Resource
    private RdmsCustomerService rdmsCustomerService;
    @Resource
    private com.course.server.security.TokenStoreService tokenStoreService;
    @Resource
    private com.course.server.security.LoginAttemptService loginAttemptService;
    @Resource
    private RdmsCustomerUserService rdmsCustomerUserService;
    @Resource
    private com.course.system.service.CustomerDeleteConfirmService customerDeleteConfirmService;
    @Autowired
    private RdmsBossService rdmsBossService;
    @Autowired
    private RdmsDepartmentService rdmsDepartmentService;

    /**
     * 保存客户信息
     */
    @PostMapping("/save")
    @Transactional
    public ResponseDto save(@RequestBody RdmsCustomerDto customerDto) throws Exception {
        ResponseDto responseDto = new ResponseDto();
        // 保存校验
        ValidatorUtil.require(customerDto.getLoginName(), "登录名称");
        ValidatorUtil.require(customerDto.getCustomerName(), "机构名称");
        ValidatorUtil.require(customerDto.getAdminName(), "管理员姓名");
        ValidatorUtil.require(customerDto.getContactPhone(), "联系电话");
        ValidatorUtil.require(customerDto.getStatus(), "客户状态");
        ValidatorUtil.require(customerDto.getCustomerType(), "客户类型");

        ValidatorUtil.length(customerDto.getLoginName(), "登录名", 2, 6);
        ValidatorUtil.length(customerDto.getCustomerName(), "公司名称", 5, 50);
        ValidatorUtil.length(customerDto.getAdminName(), "管理员姓名", 2, 10);
        ValidatorUtil.length(customerDto.getContactPhone(), "联系电话", 6, 15);

        rdmsCustomerService.save(customerDto);

        responseDto.setContent(customerDto);
        return responseDto;
    }

    /**
     * 修改客户信息
     */
    @PostMapping("/modify")
    @Transactional
    public ResponseDto modify(@RequestBody RdmsCustomerDto customerDto) throws Exception {
        ResponseDto responseDto = new ResponseDto();
        // 保存校验
        ValidatorUtil.require(customerDto.getLoginName(), "登录名称");
        ValidatorUtil.require(customerDto.getAdminName(), "管理员姓名");
        ValidatorUtil.require(customerDto.getContactPhone(), "联系电话");
        ValidatorUtil.require(customerDto.getStatus(), "客户状态");

        ValidatorUtil.length(customerDto.getLoginName(), "登录名", 1, 20);
        ValidatorUtil.length(customerDto.getAdminName(), "管理员姓名", 2, 10);
        ValidatorUtil.length(customerDto.getContactPhone(), "联系电话", 6, 15);

        rdmsCustomerService.modify(customerDto);

        responseDto.setContent(customerDto);
        return responseDto;
    }

    /**
     * 修改客户管理员后需要提示重新设置密码
     * @param adminDto
     * @return
     */
    @PostMapping("/changeAdmin")
    @Transactional
    public ResponseDto changeAdmin(@RequestBody RdmsHmiAdminDto adminDto){
        ResponseDto responseDto = new ResponseDto();
        // 保存校验
        ValidatorUtil.require(adminDto.getAdminId(), "管理员");
        ValidatorUtil.require(adminDto.getCustomerId(), "机构");
        String target = TenantAccess.requireManagedTenant(adminDto.getCustomerId());
        String customerId = TenantScope.call(target, () -> rdmsCustomerService.changeAdmin(adminDto));
        responseDto.setContent(customerId);
        return responseDto;
    }

    /**
     * 保存客户关联的最终管理人信息
     * 为客户指定对应的最终管理人，建立客户与最终管理人的关联关系
     * 
     * @param customerId 客户ID
     * @param bossId 最终管理人 ID
     * @return 返回保存后的最终管理人信息
     */
    @PostMapping("/saveBoss")
    @Transactional
    public ResponseDto<RdmsBossDto> saveBoss(@RequestParam String customerId, String bossId){
        ResponseDto<RdmsBossDto> responseDto = new ResponseDto<>();
        // 保存校验
        ValidatorUtil.require(bossId, "最终管理人");
        ValidatorUtil.require(customerId, "机构");
        String target = TenantAccess.requireManagedTenant(customerId);
        RdmsBossDto boss = TenantScope.call(target, () -> rdmsCustomerService.saveBoss(target, bossId));
        responseDto.setContent(boss);
        return responseDto;
    }

    /**
     * 修改客户管理员密码
     * 更新指定客户的管理员登录密码
     * 
     * @param adminDto 包含客户ID和新密码的管理员信息对象
     * @return 返回操作结果
     */
    @PostMapping("/changeAdminPassword")
    @Transactional
    public ResponseDto changeAdminPassword(@RequestBody RdmsHmiAdminDto adminDto){
        ResponseDto responseDto = new ResponseDto();
        ValidatorUtil.require(adminDto.getPassword(), "管理员密码");
        ValidatorUtil.require(adminDto.getCustomerId(), "机构");

        String customerId = rdmsCustomerService.savePassword(adminDto.getCustomerId(), adminDto.getPassword());
        tokenStoreService.revokeSubject(adminDto.getCustomerId());
        unlockAdminLogin(adminDto.getCustomerId());
        responseDto.setContent(customerId);
        return responseDto;
    }

    private void unlockAdminLogin(String customerId) {
        RdmsCustomer customer = rdmsCustomerService.selectByPrimaryKey(customerId);
        if (customer != null) {
            loginAttemptService.clear(customer.getContactPhone());
        }
    }

    /**
     * 根据客户ID获取客户详细信息
     * 查询客户基本信息并关联查询管理员信息
     * 
     * @param customerId 客户ID
     * @return 返回包含管理员信息的客户详细信息
     */
    @PostMapping("/get-customer-info/{customerId}")
    public ResponseDto<RdmsCustomerDto> getCustomerInfo(@PathVariable String customerId){
        ResponseDto<RdmsCustomerDto> responseDto = new ResponseDto<>();
        RdmsCustomer rdmsCustomer = rdmsCustomerService.selectByPrimaryKey(customerId);
        RdmsCustomerDto customerDto = CopyUtil.copy(rdmsCustomer, RdmsCustomerDto.class);
        String target = TenantAccess.requireManagedTenant(customerId);
        RdmsCustomerUser customerUserByCustomerIdAndLoginName = TenantScope.call(target,
                () -> rdmsCustomerUserService.getCustomerUserByCustomerIdAndLoginName(target, customerDto.getContactPhone()));
        customerDto.setSuperManagerId(customerUserByCustomerIdAndLoginName == null ? null : customerUserByCustomerIdAndLoginName.getId());
        stripSecrets(customerDto);
        responseDto.setContent(customerDto);
        return responseDto;
    }

    /**
     * 根据登录名称获取客户信息
     * 通过登录名称查询对应的客户详细信息
     * 
     * @param loginname 登录名称
     * @return 返回对应的客户信息
     */
    @PostMapping("/get-customer-by-loginname/{loginname}")
    public ResponseDto<RdmsCustomerDto> getCustomerByLoginName(@PathVariable String loginname){
        ResponseDto<RdmsCustomerDto> responseDto = new ResponseDto<>();
        RdmsCustomer rdmsCustomer = rdmsCustomerService.getCustomerByLoginName(loginname);
        RdmsCustomerDto customerDto = null;
        if (rdmsCustomer != null) {
            customerDto = new RdmsCustomerDto();
            customerDto.setId(rdmsCustomer.getId());
            customerDto.setLoginName(rdmsCustomer.getLoginName());
        }
        responseDto.setContent(customerDto);
        return responseDto;
    }

    /**
     * 根据登录名称进行查询
     */
    @PostMapping("/login-search")
    @Transactional
    public ResponseDto loginSearch(@RequestBody RdmsCustomerDto customerDto) {
        ValidatorUtil.require(customerDto.getLoginName(), "登录名称");
        ResponseDto responseDto = new ResponseDto();

        //判断用户是否存在
        List<RdmsCustomer> customers = rdmsCustomerService.searchCustomerByLoginName(customerDto.getLoginName().trim());
        List<RdmsCustomerDto> customerDtos = new ArrayList<>();
        if (customers.size()>0) {
            customerDtos = CopyUtil.copyList(customers, RdmsCustomerDto.class);
            customerDtos.forEach(RdmsCustomerController::stripSecrets);
            responseDto.setContent(customerDtos);
            return responseDto;
        }
        throw new BusinessException(BusinessExceptionCode.DB_RECORD_NOT_EXIST, "记录不存在");
    }

    /**
     * 根据公司名称进行查询
     */
    @PostMapping("/inst-search")
    @Transactional
    public ResponseDto instSearch(@RequestBody RdmsCustomerDto customerDto) {
        ValidatorUtil.require(customerDto.getCustomerName(), "公司名称");
        ResponseDto responseDto = new ResponseDto();

        //判断用户是否存在
        List<RdmsCustomer> customers = rdmsCustomerService.searchCustomerByCustomerName(customerDto.getCustomerName().trim());
        List<RdmsCustomerDto> customerDtos = new ArrayList<>();
        if (customers.size()>0) {
            customerDtos = CopyUtil.copyList(customers, RdmsCustomerDto.class);
            customerDtos.forEach(RdmsCustomerController::stripSecrets);
            responseDto.setContent(customerDtos);
            return responseDto;
        }
        throw new BusinessException(BusinessExceptionCode.DB_RECORD_NOT_EXIST, "记录不存在");
    }

     /**
     * 根据联系电话进行查询
     */
    @PostMapping("/contact-search")
    @Transactional
    public ResponseDto contactSearch(@RequestBody RdmsCustomerDto customerDto) {
        ValidatorUtil.require(customerDto.getContactPhone(), "联系电话");
        ResponseDto responseDto = new ResponseDto();

        //判断用户是否存在
        List<RdmsCustomer> customers = rdmsCustomerService.searchCustomerByContactPhone(customerDto.getContactPhone().trim());
        List<RdmsCustomerDto> customerDtos = new ArrayList<>();
        if (customers.size()>0) {
            customerDtos = CopyUtil.copyList(customers, RdmsCustomerDto.class);
            customerDtos.forEach(RdmsCustomerController::stripSecrets);
            responseDto.setContent(customerDtos);
            return responseDto;
        }
        throw new BusinessException(BusinessExceptionCode.DB_RECORD_NOT_EXIST, "记录不存在");
    }

    /**
     * 列表查询
     */
    @PostMapping("/list")
    public ResponseDto list(@RequestBody PageDto pageDto) {
        ResponseDto responseDto = new ResponseDto();
        rdmsCustomerService.list(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

    /**
     * 列表查询
     */
    @PostMapping("/customerList")
    public ResponseDto customerList(@RequestBody PageDto pageDto) {
        ResponseDto responseDto = new ResponseDto();
        rdmsCustomerService.customerList(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

    @PostMapping("/getOutSourcingCustomerList/{customerId}")
    public ResponseDto<List<RdmsCustomerDto>> getOutSourcingCustomerList(@PathVariable String customerId) {
        ResponseDto responseDto = new ResponseDto();
        List<RdmsCustomerDto> outSourcingCustomerList = rdmsCustomerService.getOutSourcingCustomerList(customerId);
        outSourcingCustomerList.forEach(RdmsCustomerController::stripSecrets);
        responseDto.setContent(outSourcingCustomerList);
        return responseDto;
    }


    private static void stripSecrets(RdmsCustomerDto dto) {
        dto.setPassword(null);
        dto.setSmsCode(null);
    }

    /**
     * 删除前核对：体为 id、customerName（手工输入的机构名称）、password（md5(超管明文 + 超管盐)）。
     */
    @PostMapping("/delete-check")
    @com.course.server.security.scope.RequiresScope({"CUSTOMER:DELETE", "CUSTOMER:*", "PLATFORM:*"})
    public ResponseDto deleteCheck(@RequestBody RdmsCustomerDto customerDto) {
        customerDeleteConfirmService.verify(customerDto.getId(), customerDto.getCustomerName(), customerDto.getPassword());
        return new ResponseDto();
    }

    /**
     * 删除机构：体同 delete-check，服务端再次核对后置删除标记，该机构会话随即失效。
     */
    @PostMapping("/delete")
    @com.course.server.security.scope.RequiresScope({"CUSTOMER:DELETE", "CUSTOMER:*", "PLATFORM:*"})
    public ResponseDto delete(@RequestBody RdmsCustomerDto customerDto) {
        RdmsCustomer customer = customerDeleteConfirmService.verify(
                customerDto.getId(), customerDto.getCustomerName(), customerDto.getPassword());
        rdmsCustomerService.delete(customer.getId());
        LOG.info("机构已删除：{}（{}）", customer.getCustomerName(), customer.getId());
        return new ResponseDto();
    }

    /**
     * 设置机构侧密码。password 为 md5(明文 + 盐)；该机构已签发的会话随即失效。
     */
    @PostMapping("/save-password")
    @com.course.server.security.scope.RequiresScope({"CUSTOMER:UPDATE", "USER:UPDATE", "PLATFORM:*"})
    public ResponseDto savePassword(@RequestBody RdmsCustomerDto customerDto) {
        ValidatorUtil.require(customerDto.getId(), "机构");
        ValidatorUtil.require(customerDto.getPassword(), "密码");
        ResponseDto responseDto = new ResponseDto();
        rdmsCustomerService.savePassword(customerDto.getId(), customerDto.getPassword());
        tokenStoreService.revokeSubject(customerDto.getId());
        unlockAdminLogin(customerDto.getId());
        stripSecrets(customerDto);
        responseDto.setContent(customerDto);
        return responseDto;
    }

    /** 补数：遍历全部机构写入默认部门与默认职级，限超管调用 */
    @GetMapping("/initDefaultDepartment")
    public ResponseDto initDefaultDepartment() {
       ResponseDto responseDto = new ResponseDto();
        if (!Constants.SUPER_CUSTOMER_ID.equals(TenantContext.getTenantId())) {
            throw new BusinessException(BusinessExceptionCode.NO_PERMISSION);
        }
        List<RdmsCustomer> allCustomerList = rdmsCustomerService.getAllCustomerList();
        if(!CollectionUtils.isEmpty(allCustomerList)){
            for(RdmsCustomer customer: allCustomerList){
                if(!ObjectUtils.isEmpty(customer)){
                    TenantScope.run(customer.getId(), () -> initDefaults(customer.getId()));
                }
            }
       }
       return responseDto;
    }

    private void initDefaults(String customerId) {
        RdmsBossDto bossByCustomer = rdmsBossService.getBossByCustomerId(customerId);
        RdmsDepartment defaultDepartment = rdmsDepartmentService.getDefaultDepartment(customerId);
        if(!ObjectUtils.isEmpty(bossByCustomer) && !ObjectUtils.isEmpty(defaultDepartment)){
            rdmsCustomerService.addDefaultDepartmentInfo(customerId, bossByCustomer.getBossId(), defaultDepartment.getId());
        }
        rdmsCustomerService.addDefaultJobLevel(customerId);
    }

}
