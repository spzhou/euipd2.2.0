/**
 * Author：周朔鹏
 * 编制时间：2025-08-09

 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */

package com.course.system.controller.rdms;

import com.course.server.domain.RdmsCustomer;
import com.course.server.domain.RdmsCustomerUser;
import com.course.server.domain.RdmsUser;
import com.course.server.dto.LoginUserDto;
import com.course.server.dto.PageDto;
import com.course.server.dto.ResponseDto;
import com.course.server.dto.rdms.*;
import com.course.server.enums.SmsUseEnum;
import com.course.server.enums.rdms.CustomerStatusEnum;
import com.course.server.exception.BusinessException;
import com.course.server.exception.BusinessExceptionCode;
import com.course.server.security.LoginAttemptService;
import com.course.server.security.PasswordService;
import com.course.server.security.TokenStoreService;
import com.course.server.service.rdms.*;
import com.course.server.util.CopyUtil;
import com.course.server.util.UuidUtil;
import com.course.server.util.ValidatorUtil;
import com.course.system.service.AuthApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/rdms/user")
public class RdmsUserController {

    private static final Logger LOG = LoggerFactory.getLogger(RdmsUserController.class);
    public static final String BUSINESS_NAME = "开发者";

    @Resource
    private RdmsUserService rdmsUserService;
    @Resource
    private RdmsSmsService rdmsSmsService;
    @Resource
    private RdmsCustomerService rdmsCustomerService;
    @Resource
    private RdmsCustomerUserService rdmsCustomerUserService;
    @Resource
    private PasswordService passwordService;
    @Resource
    private TokenStoreService tokenStoreService;
    @Resource
    private LoginAttemptService loginAttemptService;
    @Resource
    private AuthApplicationService authApplicationService;

    /**
     * 保存客户信息
     */
    @PostMapping("/save")
    @Transactional
    public ResponseDto save(@RequestBody RdmsUserDto userDto) throws Exception {
        ResponseDto responseDto = new ResponseDto();

        ValidatorUtil.require(userDto.getLoginName(), "登录名称");
        ValidatorUtil.require(userDto.getTrueName(), "真实姓名");
        ValidatorUtil.require(userDto.getDescription(), "能力描述");

        ValidatorUtil.length(userDto.getLoginName(), "登录名称", 11, 11);
        ValidatorUtil.length(userDto.getTrueName(), "真实姓名", 2, 4);

        ValidatorUtil.length(userDto.getDescription(), "能力描述", 10, 500);

        //判断用户是否存在
        RdmsUser rdmsUser = rdmsUserService.selectByLoginName(userDto.getLoginName());
        if(rdmsUser==null){
            userDto.setId(UuidUtil.getShortUuid());
            rdmsUserService.save(userDto);
            return responseDto;
        }else{
            throw new BusinessException(BusinessExceptionCode.CUSTOMER_LOGIN_NAME_EXIST);
        }

    }

    /**
     * 用户信息修改
     */
    @PostMapping("/modify")
    @Transactional
    public ResponseDto modify(@RequestBody RdmsUserDto userDto) throws Exception {
        ResponseDto responseDto = new ResponseDto();
        ValidatorUtil.require(userDto.getLoginName(), "登录名称");
        ValidatorUtil.require(userDto.getTrueName(), "真实姓名");
        ValidatorUtil.require(userDto.getDescription(), "能力描述");

        ValidatorUtil.length(userDto.getLoginName(), "登录名称", 11, 11);
        ValidatorUtil.length(userDto.getTrueName(), "真实姓名", 2, 4);
        ValidatorUtil.length(userDto.getDescription(), "能力描述", 10, 500);

        //判断用户是否存在
        RdmsUser rdmsUser = rdmsUserService.selectByPrimaryKey(userDto.getId());
        if(rdmsUser!=null){
            rdmsUserService.modify(userDto);
            return responseDto;
        }else{
            throw new BusinessException(BusinessExceptionCode.MEMBER_NOT_EXIST);
        }

    }


    /**
     * 根据登录名称进行查询
     */
    @PostMapping("/login-search")
    @Transactional
    public ResponseDto loginSearch(@RequestBody RdmsUserDto userDto) {
        ValidatorUtil.require(userDto.getLoginName(), "登录名称");
        ResponseDto responseDto = new ResponseDto();

        //判断用户是否存在
        List<RdmsUser> users = rdmsUserService.searchUserByloginName(userDto.getLoginName());
        List<RdmsUserDto> userDtos = new ArrayList<>();
        if (users.size()>0) {
            userDtos = CopyUtil.copyList(users, RdmsUserDto.class);
            userDtos.forEach(RdmsUserController::stripSecrets);
            responseDto.setContent(userDtos);
            return responseDto;
        }
        throw new BusinessException(BusinessExceptionCode.DB_RECORD_NOT_EXIST, "记录不存在");
    }

    /**
     * 根据公司名称进行查询
     */
    @PostMapping("/name-search")
    @Transactional
    public ResponseDto nameSearch(@RequestBody RdmsUserDto userDto) {
        ValidatorUtil.require(userDto.getTrueName(), "真实姓名");
        ResponseDto responseDto = new ResponseDto();

        //判断用户是否存在
        List<RdmsUser> users = rdmsUserService.searchUserByTrueName(userDto.getTrueName());
        List<RdmsUserDto> userDtos = new ArrayList<>();
        if (users.size()>0) {
            userDtos = CopyUtil.copyList(users, RdmsUserDto.class);
            userDtos.forEach(RdmsUserController::stripSecrets);
            responseDto.setContent(userDtos);
            return responseDto;
        }
        throw new BusinessException(BusinessExceptionCode.DB_RECORD_NOT_EXIST, "记录不存在");
    }

    private static void stripSecrets(RdmsUserDto dto) {
        dto.setPassword(null);
        dto.setSmsCode(null);
        dto.setToken(null);
    }

    /**
     * 列表查询
     */
    @PostMapping("/list")
    public ResponseDto list(@RequestBody PageDto pageDto) {
        ResponseDto responseDto = new ResponseDto();
        rdmsUserService.list(pageDto);
        stripPageSecrets(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

    private static void stripPageSecrets(PageDto<?> pageDto) {
        if (pageDto == null || pageDto.getList() == null) {
            return;
        }
        for (Object item : pageDto.getList()) {
            if (item instanceof RdmsUserDto) {
                stripSecrets((RdmsUserDto) item);
            }
        }
    }

    /**
     * 列表查询
     */
    @PostMapping("/searchList")
    public ResponseDto<PageDto<RdmsUserDto>> searchList(@RequestBody PageDto<RdmsUserDto> pageDto) {
        ResponseDto<PageDto<RdmsUserDto>> responseDto = new ResponseDto<>();
        rdmsUserService.searchList(pageDto);
        stripPageSecrets(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

/**
     * 列表查询
     */
    @PostMapping("/searchUserByLoginName/{loginName}")
    public ResponseDto searchUserByLoginName(@PathVariable String loginName) {
        ResponseDto responseDto = new ResponseDto();
        LoginUserDto byLoginName = rdmsUserService.findByLoginName(loginName);
        responseDto.setContent(byLoginName);
        return responseDto;
    }

    /**
     * 删除
     */
    @DeleteMapping("/del/{id}")
    public ResponseDto delete(@PathVariable String id) {
        ResponseDto responseDto = new ResponseDto();
        rdmsUserService.delete(id);
        tokenStoreService.revokeSubject(id);
        return responseDto;
    }

    /**
     * 管理员设置成员密码。password 为 md5(明文 + 盐)；该成员已签发的会话随即失效，登录锁定随即解除。
     */
    @PostMapping("/save-password")
    @com.course.server.security.scope.RequiresScope({"USER:UPDATE", "USER:*", "PLATFORM:*"})
    public ResponseDto savePassword(@RequestBody RdmsUserDto userDto) {
        ValidatorUtil.require(userDto.getId(), "用户");
        ValidatorUtil.require(userDto.getPassword(), "密码");
        userDto.setPassword(passwordService.candidateFromClientDigest(userDto.getPassword()));
        ResponseDto responseDto = new ResponseDto();
        rdmsUserService.savePassword(userDto);
        tokenStoreService.revokeSubject(userDto.getId());
        RdmsUser target = rdmsUserService.selectByPrimaryKey(userDto.getId());
        if (target != null) {
            loginAttemptService.clear(target.getLoginName());
        }
        stripSecrets(userDto);
        responseDto.setContent(userDto);
        return responseDto;
    }

    /**
     * 校验登录名是否存在
     * content 为 true 表示存在，false 表示不存在
     */
    @GetMapping(value = "/is-mobile-exist/{loginName}")
    public ResponseDto isMobileExist(@PathVariable(value = "loginName") String loginName) throws BusinessException {
        ResponseDto responseDto = new ResponseDto();
        LoginUserDto loginUserDto = rdmsUserService.findByLoginName(loginName);
        LoginUserDto loginCustomerDto = rdmsCustomerService.findByContactPhone(loginName);
        responseDto.setContent(loginUserDto != null || loginCustomerDto != null);
        return responseDto;
    }

    /**
     * 短信验证后重置密码。体为 loginName、password（md5(明文 + 盐)）、smsCode。
     * 成员口令与以该手机号为联系电话的机构侧口令一并重置，相应会话随即失效。
     */
    @PostMapping("/reset-password")
    public ResponseDto resetPassword(@RequestBody RdmsUserDto userDto) throws BusinessException {
        ValidatorUtil.require(userDto.getLoginName(), "手机号");
        ValidatorUtil.require(userDto.getPassword(), "新密码");
        ValidatorUtil.require(userDto.getSmsCode(), "短信验证码");
        String loginName = userDto.getLoginName().trim();
        RdmsSmsDto rdmsSmsDto = new RdmsSmsDto();
        rdmsSmsDto.setMobile(loginName);
        rdmsSmsDto.setCode(userDto.getSmsCode());
        rdmsSmsDto.setUse(SmsUseEnum.FORGET.getCode());
        rdmsSmsService.validCode(rdmsSmsDto);
        authApplicationService.applyNewPassword(loginName, userDto.getPassword());
        return new ResponseDto();
    }

    /**
     * 登录前按手机号读取密码盐，前端用它计算 md5(明文 + 盐)。未登记的手机号返回固定伪盐。
     */
    @GetMapping("/login-salt/{loginName}")
    public ResponseDto loginSalt(@PathVariable String loginName) {
        ResponseDto responseDto = new ResponseDto();
        responseDto.setContent(authApplicationService.loginSalt(loginName));
        return responseDto;
    }

    /**
     * 登录前列出该手机号所属的在业且未删除的机构，只返回机构标识与名称。
     */
    @GetMapping("/list-customer/{loginName}")
    public ResponseDto<List<Map<String, String>>> listCustomer(@PathVariable String loginName){
        ResponseDto<List<Map<String, String>>> responseDto = new ResponseDto<>();
        List<RdmsCustomerUser> memberships = rdmsCustomerUserService.findCustomerUserByLoginName(loginName);
        List<Map<String, String>> customers = new ArrayList<>();
        for (RdmsCustomerUser membership : memberships) {
            RdmsCustomer rdmsCustomer = rdmsCustomerService.selectByPrimaryKey(membership.getCustomerId());
            if (!ObjectUtils.isEmpty(rdmsCustomer) && !Integer.valueOf(1).equals(rdmsCustomer.getDeleted())
                    && CustomerStatusEnum.CUSTOMER_NORMAL.getStatus().equals(rdmsCustomer.getStatus())) {
                Map<String, String> item = new HashMap<>();
                item.put("id", rdmsCustomer.getId());
                item.put("customerName", rdmsCustomer.getCustomerName());
                customers.add(item);
            }
        }
        responseDto.setContent(customers);
        return responseDto;
    }
}
