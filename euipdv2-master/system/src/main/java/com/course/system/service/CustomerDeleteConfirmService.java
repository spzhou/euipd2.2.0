/**
 * Author：周朔鹏
 * 编制时间：2026-10-17
 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */
package com.course.system.service;

import com.course.server.constants.Constants;
import com.course.server.domain.RdmsCustomer;
import com.course.server.exception.BusinessException;
import com.course.server.exception.BusinessExceptionCode;
import com.course.server.security.LoginAttemptService;
import com.course.server.security.PasswordService;
import com.course.server.security.tenant.TenantContext;
import com.course.server.service.rdms.RdmsCustomerService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

/**
 * 删除机构前的确认：操作人须为超管，手工输入的机构名称须与库中一致，超管密码须正确。
 * 密码错误按超管登录名计入登录失败次数，达到上限即锁定。
 */
@Service
public class CustomerDeleteConfirmService {

    @Resource
    private RdmsCustomerService rdmsCustomerService;
    @Resource
    private PasswordService passwordService;
    @Resource
    private LoginAttemptService loginAttemptService;

    /**
     * @param superPasswordDigest md5(超管明文密码 + 超管盐)
     * @return 待删除的机构
     */
    public RdmsCustomer verify(String customerId, String customerName, String superPasswordDigest) {
        if (!Constants.SUPER_CUSTOMER_ID.equals(TenantContext.getTenantId())) {
            throw new BusinessException(BusinessExceptionCode.NO_PERMISSION);
        }
        if (!StringUtils.hasText(customerId) || Constants.SUPER_CUSTOMER_ID.equals(customerId)) {
            throw new BusinessException(BusinessExceptionCode.NO_PERMISSION);
        }
        RdmsCustomer customer = rdmsCustomerService.selectByPrimaryKey(customerId);
        if (customer == null || Integer.valueOf(1).equals(customer.getDeleted())) {
            throw new BusinessException(BusinessExceptionCode.CUSTOMER_NOT_EXIST);
        }
        String typed = customerName == null ? "" : customerName.trim();
        if (!typed.equals(customer.getCustomerName())) {
            throw new BusinessException(BusinessExceptionCode.INPUT_INFORMATION_ERROR, "机构名称不一致");
        }

        RdmsCustomer superCustomer = rdmsCustomerService.selectByPrimaryKey(Constants.SUPER_CUSTOMER_ID);
        if (superCustomer == null) {
            throw new BusinessException(BusinessExceptionCode.NO_PERMISSION);
        }
        String loginName = superCustomer.getContactPhone();
        if (loginAttemptService.isLocked(loginName)) {
            throw locked();
        }
        if (!StringUtils.hasText(superPasswordDigest)
                || !passwordService.matchesClientDigest(superPasswordDigest, superCustomer.getPassword())) {
            if (loginAttemptService.recordFailure(loginName)) {
                throw locked();
            }
            throw new BusinessException(BusinessExceptionCode.PASSWORD_OLD_ERROR, "超管密码错误");
        }
        loginAttemptService.clear(loginName);
        return customer;
    }

    private BusinessException locked() {
        return new BusinessException(BusinessExceptionCode.ACCOUNT_LOCKED,
                "密码错误次数过多，请" + loginAttemptService.lockMinutes() + "分钟后再试");
    }
}
