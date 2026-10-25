/**
 * Author：周朔鹏
 * 编制时间：2026-08-01
 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */
package com.course.system.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.course.server.constants.Constants;
import com.course.server.domain.RdmsCustomer;
import com.course.server.domain.RdmsCustomerUser;
import com.course.server.domain.RdmsUser;
import com.course.server.dto.LoginUserDto;
import com.course.server.dto.rdms.RdmsCustomerDto;
import com.course.server.dto.rdms.RdmsCustomerUserDto;
import com.course.server.dto.rdms.RdmsUserDto;
import com.course.server.enums.rdms.CustomerStatusEnum;
import com.course.server.enums.rdms.LoginUserGroupEnum;
import com.course.server.enums.rdms.WorkerStatusEnum;
import com.course.server.exception.BusinessException;
import com.course.server.exception.BusinessExceptionCode;
import com.course.server.security.AuthPolicyProperties;
import com.course.server.security.AuthTokenIssuer;
import com.course.server.security.LoginAttemptService;
import com.course.server.security.PasswordService;
import com.course.server.security.SecurityConstants;
import com.course.server.security.TokenStoreService;
import com.course.server.security.jwt.JwtClaims;
import com.course.server.security.jwt.JwtService;
import com.course.server.service.rdms.RdmsCustomerService;
import com.course.server.service.rdms.RdmsCustomerUserService;
import com.course.server.service.rdms.RdmsUserService;
import com.course.server.service.util.IpUtils;
import com.course.server.util.CopyUtil;
import com.course.server.util.ValidatorUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPublicKey;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class AuthApplicationService {

    private static final String SALT_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private RdmsUserService rdmsUserService;
    @Resource
    private RdmsCustomerService rdmsCustomerService;
    @Resource
    private RdmsCustomerUserService rdmsCustomerUserService;
    @Resource
    private AuthTokenIssuer authTokenIssuer;
    @Resource
    private TokenStoreService tokenStoreService;
    @Resource
    private JwtService jwtService;
    @Resource
    private PasswordService passwordService;
    @Resource
    private LoginAttemptService loginAttemptService;
    @Resource
    private AuthPolicyProperties policy;
    @Resource
    private IpUtils ipUtils;

    /**
     * 登录：图形码 → 锁定检查 → 机构 → 口令 → 状态。以初始口令登录时不签发令牌，返回改口令凭证。
     */
    public LoginUserDto login(RdmsUserDto userDto, HttpServletRequest request) {
        ValidatorUtil.require(userDto.getLoginName(), "登录名");
        ValidatorUtil.require(userDto.getPassword(), "密码");
        ValidatorUtil.require(userDto.getSelectedCustomerId(), "机构");
        ValidatorUtil.require(userDto.getImageCode(), "图形验证码");
        ValidatorUtil.require(userDto.getImageCodeToken(), "图形验证码令牌");
        String loginName = userDto.getLoginName().trim();
        userDto.setLoginName(loginName);

        String imageCodeKey = SecurityConstants.REDIS_KAPTCHA_PREFIX + userDto.getImageCodeToken();
        Object imageCode = redisTemplate.opsForValue().getAndDelete(imageCodeKey);
        if (ObjectUtils.isEmpty(imageCode) || !String.valueOf(imageCode).equalsIgnoreCase(userDto.getImageCode())) {
            throw new BusinessException(BusinessExceptionCode.INPUT_INFORMATION_ERROR);
        }
        assertNotLocked(loginName);

        RdmsCustomer selectedCustomer = rdmsCustomerService.selectByPrimaryKey(userDto.getSelectedCustomerId());
        if (ObjectUtils.isEmpty(selectedCustomer)) {
            throw new BusinessException(BusinessExceptionCode.CUSTOMER_NOT_EXIST);
        }

        String clientDigest = userDto.getPassword();
        userDto.setPassword(passwordService.candidateFromClientDigest(clientDigest));
        boolean adminPath = loginName.equals(selectedCustomer.getContactPhone());

        LoginUserDto loginUserDto;
        try {
            loginUserDto = adminPath
                    ? rdmsCustomerService.login(CopyUtil.copy(userDto, RdmsCustomerDto.class), selectedCustomer.getId())
                    : rdmsUserService.login(userDto);
        } catch (BusinessException e) {
            if (e.getCode() == BusinessExceptionCode.LOGIN_USER_ERROR && loginAttemptService.recordFailure(loginName)) {
                throw lockedException();
            }
            throw e;
        }
        loginAttemptService.clear(loginName);
        assertTenantUsable(selectedCustomer, null);

        String tenantId;
        String membershipId = null;
        String subjectId = loginUserDto.getId();
        if (adminPath) {
            loginUserDto.setUserGroup(LoginUserGroupEnum.CUSTOMER.getStatus());
            tenantId = subjectId;
            RdmsCustomerUser customerUser = rdmsCustomerUserService
                    .getCustomerUserByCustomerIdAndLoginName(tenantId, loginUserDto.getContactPhone());
            if (customerUser != null) {
                RdmsCustomerUserDto customerUserDto = CopyUtil.copy(customerUser, RdmsCustomerUserDto.class);
                customerUserDto.setCustomerUserId(customerUser.getId());
                loginUserDto.setAdminUser(customerUserDto);
                membershipId = customerUser.getId();
                loginUserDto.setCustomerUserId(customerUser.getId());
                loginUserDto.setRoleList(rdmsUserService.getUserRole(tenantId, membershipId));
            }
            RdmsCustomer patch = new RdmsCustomer();
            patch.setId(subjectId);
            patch.setLoginIp(ipUtils.getIpAddress(request));
            rdmsCustomerService.updateByPrimaryKeySelective(patch);
            loginUserDto.setLoginName(loginName);
        } else {
            RdmsUser user = rdmsUserService.selectByPrimaryKey(subjectId);
            if (user == null || Integer.valueOf(1).equals(user.getDeleted())) {
                throw new BusinessException(BusinessExceptionCode.LOGIN_USER_ERROR);
            }
            loginUserDto.setUserGroup(LoginUserGroupEnum.DEVELOPER.getStatus());
            loginUserDto.setCustomer(selectedCustomer);
            tenantId = selectedCustomer.getId();
            RdmsCustomerUser customerUser = rdmsCustomerUserService
                    .getCustomerUserByCustomerIdAndLoginName(tenantId, loginName);
            if (customerUser == null) {
                throw new BusinessException(BusinessExceptionCode.NO_PERMISSION);
            }
            membershipId = customerUser.getId();
            loginUserDto.setRoleList(rdmsUserService.getUserRole(tenantId, membershipId));

            RdmsUser patch = new RdmsUser();
            patch.setId(subjectId);
            patch.setLoginIp(ipUtils.getIpAddress(request));
            rdmsUserService.updateByPrimaryKeySelective(patch);
        }
        assertMembershipActive(membershipId, BusinessExceptionCode.NO_PERMISSION);

        String salt = rdmsUserService.findSaltByLoginName(loginName);
        if (!isSuperCustomer(subjectId) && !isSuperLogin(loginName)
                && passwordService.isInitialPassword(clientDigest, subjectId, salt)) {
            return passwordChangeRequired(subjectId, tenantId, membershipId, loginName);
        }
        return authTokenIssuer.attachTokens(loginUserDto, tenantId, membershipId, TokenStoreService.nowSeconds());
    }

    private LoginUserDto passwordChangeRequired(String subjectId, String tenantId, String membershipId, String loginName) {
        String ticket = TokenStoreService.randomHex(24);
        JSONObject payload = new JSONObject();
        payload.put("userId", subjectId);
        payload.put("tenantId", tenantId);
        payload.put("membershipId", membershipId == null ? "" : membershipId);
        payload.put("loginName", loginName);
        redisTemplate.opsForValue().set(SecurityConstants.REDIS_PWD_TICKET_PREFIX + ticket, payload.toJSONString(),
                policy.getPasswordTicketTtlSeconds(), TimeUnit.SECONDS);
        LoginUserDto dto = new LoginUserDto();
        dto.setLoginName(loginName);
        dto.setPasswordChangeRequired(Boolean.TRUE);
        dto.setPasswordChangeTicket(ticket);
        return dto;
    }

    /**
     * 以初始口令登录后设置新口令，完成登录并签发令牌。
     */
    public LoginUserDto completeInitialPassword(String ticket, String newPassword) {
        ValidatorUtil.require(ticket, "设置密码凭证");
        ValidatorUtil.require(newPassword, "新密码");
        String key = SecurityConstants.REDIS_PWD_TICKET_PREFIX + ticket;
        Object raw = redisTemplate.opsForValue().get(key);
        if (raw == null) {
            throw new BusinessException(BusinessExceptionCode.PASSWORD_TICKET_INVALID);
        }
        JSONObject payload = JSON.parseObject(String.valueOf(raw));
        String loginName = payload.getString("loginName");
        if (isInitialPasswordFor(loginName, newPassword)) {
            throw new BusinessException(BusinessExceptionCode.PASSWORD_NOT_ALLOWED);
        }
        if (redisTemplate.opsForValue().getAndDelete(key) == null) {
            throw new BusinessException(BusinessExceptionCode.PASSWORD_TICKET_INVALID);
        }
        applyNewPassword(loginName, newPassword);
        String membershipId = payload.getString("membershipId");
        SessionContext ctx = rebuildSession(payload.getString("userId"), payload.getString("tenantId"),
                StringUtils.hasText(membershipId) ? membershipId : null, BusinessExceptionCode.PASSWORD_TICKET_INVALID);
        return authTokenIssuer.attachTokens(ctx.dto, ctx.tenantId, ctx.membershipId, TokenStoreService.nowSeconds());
    }

    /**
     * 本人修改口令。成功后该手机号的全部会话失效，须重新登录。
     */
    public void changePassword(String authorization, String oldPassword, String newPassword) {
        ValidatorUtil.require(oldPassword, "原密码");
        ValidatorUtil.require(newPassword, "新密码");
        JwtClaims claims = verifyAccess(authorization);
        String subject = claims.getSubject();
        String loginName;
        String stored;
        if (LoginUserGroupEnum.CUSTOMER.getStatus().equals(claims.getUserGroup())) {
            RdmsCustomer customer = rdmsCustomerService.selectByPrimaryKey(subject);
            if (customer == null) {
                throw new BusinessException(BusinessExceptionCode.TOKEN_INVALID);
            }
            loginName = customer.getContactPhone();
            stored = customer.getPassword();
        } else {
            RdmsUser user = rdmsUserService.selectByPrimaryKey(subject);
            if (user == null) {
                throw new BusinessException(BusinessExceptionCode.TOKEN_INVALID);
            }
            loginName = user.getLoginName();
            stored = user.getPassword();
        }
        assertNotLocked(loginName);
        if (!passwordService.matchesClientDigest(oldPassword, stored)) {
            if (loginAttemptService.recordFailure(loginName)) {
                throw lockedException();
            }
            throw new BusinessException(BusinessExceptionCode.PASSWORD_OLD_ERROR);
        }
        loginAttemptService.clear(loginName);
        if (oldPassword.equalsIgnoreCase(newPassword) || isInitialPasswordFor(loginName, newPassword)) {
            throw new BusinessException(BusinessExceptionCode.PASSWORD_NOT_ALLOWED);
        }
        applyNewPassword(loginName, newPassword);
    }

    /**
     * 把该手机号的成员口令与以其为联系电话的机构侧口令改为同一新口令，并吊销相应主体的全部会话。
     * 新口令为客户端摘要 md5(明文 + 盐)。
     */
    public void applyNewPassword(String loginName, String clientDigest) {
        String encoded = passwordService.encodeClientDigest(clientDigest);
        String userId = rdmsUserService.updatePasswordHashByLoginName(loginName, encoded);
        List<String> customerIds = rdmsCustomerService.updatePasswordHashByContactPhone(loginName, encoded);
        tokenStoreService.revokeSubject(userId);
        for (String customerId : customerIds) {
            tokenStoreService.revokeSubject(customerId);
        }
        loginAttemptService.clear(loginName);
    }

    private boolean isInitialPasswordFor(String loginName, String clientDigest) {
        List<String> customerIds = rdmsCustomerService.findIdsByContactPhone(loginName);
        if (customerIds.stream().anyMatch(this::isSuperCustomer)) {
            return false;
        }
        String salt = rdmsUserService.findSaltByLoginName(loginName);
        RdmsUser user = rdmsUserService.selectByLoginName(loginName);
        if (passwordService.isInitialPassword(clientDigest, user == null ? null : user.getId(), salt)) {
            return true;
        }
        for (String customerId : customerIds) {
            if (passwordService.isInitialPassword(clientDigest, customerId, salt)) {
                return true;
            }
        }
        return false;
    }

    /** 超管不受初始口令规则约束：部署版超管缺省口令即为平台机构标识 */
    private boolean isSuperCustomer(String customerId) {
        return Constants.SUPER_CUSTOMER_ID.equals(customerId);
    }

    /** 超管手机号即平台机构联系电话 */
    private boolean isSuperLogin(String loginName) {
        return rdmsCustomerService.findIdsByContactPhone(loginName).stream().anyMatch(this::isSuperCustomer);
    }

    /**
     * 登录前取盐。未登记的手机号返回由服务端密钥派生的固定伪盐，响应不暴露是否登记。
     */
    public String loginSalt(String loginName) {
        String salt = rdmsUserService.findSaltByLoginName(loginName);
        return StringUtils.hasText(salt) ? salt : pseudoSalt(loginName);
    }

    private String pseudoSalt(String loginName) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(jwtService.serverSecretMaterial(), "HmacSHA256"));
            byte[] h = mac.doFinal(("login-salt:" + (loginName == null ? "" : loginName.trim()))
                    .getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(8);
            for (int i = 0; i < 8; i++) {
                sb.append(SALT_CHARS.charAt((h[i] & 0xff) % SALT_CHARS.length()));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private void assertNotLocked(String loginName) {
        if (loginAttemptService.isLocked(loginName)) {
            throw lockedException();
        }
    }

    private BusinessException lockedException() {
        return new BusinessException(BusinessExceptionCode.ACCOUNT_LOCKED,
                "登录失败次数过多，请" + loginAttemptService.lockMinutes() + "分钟后再试");
    }

    /**
     * 成员已删除或状态为辞职、辞退时拒绝签发；平台超管及无成员记录的机构联系人不受此限。
     */
    private void assertMembershipActive(String membershipId, BusinessExceptionCode denied) {
        if (!StringUtils.hasText(membershipId)) {
            return;
        }
        RdmsCustomerUser membership = rdmsCustomerUserService.selectByPrimaryKey(membershipId);
        if (membership == null || Integer.valueOf(1).equals(membership.getDeleted())) {
            throw new BusinessException(denied);
        }
        String status = membership.getStatus();
        if (WorkerStatusEnum.WORKER_DISMISS.getStatus().equals(status)
                || WorkerStatusEnum.WORKER_VACATE.getStatus().equals(status)) {
            throw new BusinessException(denied);
        }
    }

    /**
     * 机构须存在、未删除、状态在业且未过期。{@code denied} 为空时按具体原因返回错误码（登录用），否则统一返回该码。
     */
    private void assertTenantUsable(RdmsCustomer tenant, BusinessExceptionCode denied) {
        BusinessExceptionCode reason = null;
        if (tenant == null || Integer.valueOf(1).equals(tenant.getDeleted())) {
            reason = BusinessExceptionCode.CUSTOMER_NOT_EXIST;
        } else if (CustomerStatusEnum.CUSTOMER_REGISTER.getStatus().equals(tenant.getStatus())) {
            reason = BusinessExceptionCode.CUSTOMER_REGISTER_RECHECK;
        } else if (CustomerStatusEnum.CUSTOMER_FROZEN.getStatus().equals(tenant.getStatus())) {
            reason = BusinessExceptionCode.CUSTOMER_FROZEN;
        } else if (CustomerStatusEnum.CUSTOMER_STOP.getStatus().equals(tenant.getStatus())) {
            reason = BusinessExceptionCode.CUSTOMER_STOP;
        } else if (!Constants.SUPER_CUSTOMER_ID.equals(tenant.getId()) && tenant.getExpirationTime() != null
                && tenant.getExpirationTime().getTime() < System.currentTimeMillis()) {
            reason = BusinessExceptionCode.SELECTED_CUSTOMER_OVERDUE_ERROR;
        }
        if (reason != null) {
            throw new BusinessException(denied == null ? reason : denied);
        }
    }

    private static String resolveUserGroup(RdmsCustomer tenant, String loginName) {
        if (tenant != null && loginName != null && loginName.equals(tenant.getContactPhone())) {
            return LoginUserGroupEnum.CUSTOMER.getStatus();
        }
        return LoginUserGroupEnum.DEVELOPER.getStatus();
    }

    public LoginUserDto refresh(String refreshToken) {
        TokenStoreService.RefreshSession session = tokenStoreService.consumeRefreshToken(refreshToken);
        if (session == null) {
            throw new BusinessException(BusinessExceptionCode.TOKEN_INVALID);
        }
        SessionContext ctx = rebuildSession(session.getUserId(), session.getTenantId(), session.getMembershipId(),
                BusinessExceptionCode.TOKEN_INVALID);
        return authTokenIssuer.attachTokens(ctx.dto, ctx.tenantId, ctx.membershipId, session.getAuthTime());
    }

    private static final class SessionContext {
        LoginUserDto dto;
        String tenantId;
        String membershipId;
    }

    /**
     * 按主体、机构、成员重建登录载荷，并复查用户、机构与成员状态。用于刷新与首次改口令后的签发。
     */
    private SessionContext rebuildSession(String userId, String tenantId, String membershipId,
                                          BusinessExceptionCode denied) {
        SessionContext ctx = new SessionContext();
        RdmsUser user = rdmsUserService.selectByPrimaryKey(userId);
        LoginUserDto loginUserDto;
        if (user != null) {
            if (Integer.valueOf(1).equals(user.getDeleted()) || !StringUtils.hasText(tenantId)) {
                throw new BusinessException(denied);
            }
            RdmsCustomer tenant = rdmsCustomerService.selectByPrimaryKey(tenantId);
            assertTenantUsable(tenant, denied);
            RdmsCustomerUser membership = rdmsCustomerUserService
                    .getCustomerUserByCustomerIdAndLoginName(tenantId, user.getLoginName());
            if (membership == null) {
                throw new BusinessException(denied);
            }
            loginUserDto = rdmsUserService.reloadAuthForUser(user.getLoginName(), tenantId);
            if (loginUserDto == null) {
                throw new BusinessException(denied);
            }
            loginUserDto.setUserGroup(LoginUserGroupEnum.DEVELOPER.getStatus());
            loginUserDto.setCustomer(tenant);
            if (!StringUtils.hasText(membershipId)) {
                membershipId = membership.getId();
            }
            if (!StringUtils.hasText(loginUserDto.getCustomerUserId())) {
                loginUserDto.setCustomerUserId(membershipId);
            }
        } else {
            RdmsCustomer customer = rdmsCustomerService.selectByPrimaryKey(userId);
            assertTenantUsable(customer, denied);
            loginUserDto = rdmsCustomerService.reloadAuthForCustomer(customer.getId());
            if (loginUserDto == null) {
                throw new BusinessException(denied);
            }
            loginUserDto.setUserGroup(LoginUserGroupEnum.CUSTOMER.getStatus());
            loginUserDto.setLoginName(customer.getContactPhone());
            loginUserDto.setContactPhone(customer.getContactPhone());
            tenantId = customer.getId();
            attachCustomerAdminUser(loginUserDto, tenantId, membershipId);
            if (loginUserDto.getAdminUser() != null && StringUtils.hasText(loginUserDto.getAdminUser().getCustomerUserId())) {
                membershipId = loginUserDto.getAdminUser().getCustomerUserId();
            }
        }
        assertMembershipActive(membershipId, denied);
        ctx.dto = loginUserDto;
        ctx.tenantId = tenantId;
        ctx.membershipId = membershipId;
        return ctx;
    }

    /**
     * 机构客户会话补齐 adminUser（含 customerUserId），供前端业务页读取操作人。
     */
    private void attachCustomerAdminUser(LoginUserDto loginUserDto, String tenantId, String membershipId) {
        if (loginUserDto == null || !StringUtils.hasText(tenantId)) {
            return;
        }
        RdmsCustomerUser customerUser = null;
        if (StringUtils.hasText(membershipId)) {
            customerUser = rdmsCustomerUserService.selectByPrimaryKey(membershipId);
            if (customerUser != null && !tenantId.equals(customerUser.getCustomerId())) {
                customerUser = null;
            }
        }
        if (customerUser == null) {
            String loginName = StringUtils.hasText(loginUserDto.getContactPhone())
                    ? loginUserDto.getContactPhone()
                    : loginUserDto.getLoginName();
            if (StringUtils.hasText(loginName)) {
                customerUser = rdmsCustomerUserService.getCustomerUserByCustomerIdAndLoginName(tenantId, loginName);
            }
        }
        if (customerUser == null) {
            return;
        }
        RdmsCustomerUserDto customerUserDto = CopyUtil.copy(customerUser, RdmsCustomerUserDto.class);
        customerUserDto.setCustomerUserId(customerUser.getId());
        loginUserDto.setAdminUser(customerUserDto);
        loginUserDto.setCustomerUserId(customerUser.getId());
        if (CollectionUtils.isEmpty(loginUserDto.getRoleList())) {
            loginUserDto.setRoleList(rdmsUserService.getUserRole(tenantId, customerUser.getId()));
        }
    }

    /**
     * 删除提交的 refresh；Authorization 为有效 JWT 时拉黑其 jti。不按请求值删除其他任何键。
     */
    public void logout(String authorization, String refreshToken) {
        tokenStoreService.deleteRefreshToken(refreshToken);
        String accessToken = stripBearer(authorization);
        if (!StringUtils.hasText(accessToken)) {
            return;
        }
        try {
            JwtClaims claims = jwtService.parseAndValidate(accessToken);
            tokenStoreService.blacklistJti(claims.getJti(), secondsUntilExpiry(claims));
        } catch (Exception ignored) {
            // 已失效的令牌无需拉黑
        }
    }

    public LoginUserDto switchTenant(String authorization, String targetTenantId, HttpServletRequest request) {
        ValidatorUtil.require(targetTenantId, "目标机构");
        JwtClaims claims = verifyAccess(authorization);
        String loginName = resolveLoginName(claims.getSubject());
        List<RdmsCustomerUser> list = rdmsCustomerUserService
                .findCustomerUserByCustomerIdAndLoginName(targetTenantId, loginName);
        RdmsCustomer targetTenant = rdmsCustomerService.selectByPrimaryKey(targetTenantId);
        if (targetTenant == null) {
            throw new BusinessException(BusinessExceptionCode.CUSTOMER_NOT_EXIST);
        }
        assertTenantUsable(targetTenant, BusinessExceptionCode.NO_PERMISSION);
        if (CollectionUtils.isEmpty(list) && !Constants.SUPER_CUSTOMER_ID.equals(claims.getSubject())
                && !loginName.equals(targetTenant.getContactPhone())) {
            throw new BusinessException(BusinessExceptionCode.NO_PERMISSION);
        }

        LoginUserDto loginUserDto = CollectionUtils.isEmpty(list)
                ? rdmsCustomerService.reloadAuthForCustomer(targetTenantId)
                : rdmsUserService.reloadAuthForUser(loginName, targetTenantId);
        if (loginUserDto == null) {
            throw new BusinessException(BusinessExceptionCode.NO_PERMISSION);
        }
        String membershipId = loginUserDto.getCustomerUserId();
        if (!StringUtils.hasText(membershipId) && !CollectionUtils.isEmpty(list)) {
            membershipId = list.get(0).getId();
        }
        assertMembershipActive(membershipId, BusinessExceptionCode.NO_PERMISSION);
        loginUserDto.setUserGroup(resolveUserGroup(targetTenant, loginName));
        loginUserDto.setCustomer(targetTenant);
        tokenStoreService.blacklistJti(claims.getJti(), secondsUntilExpiry(claims));
        return authTokenIssuer.attachTokens(loginUserDto, targetTenantId, membershipId, claims.getAuthTime());
    }

    /**
     * 白名单接口自行验证 access：验签、未过期、未拉黑、未被吊销。
     */
    private JwtClaims verifyAccess(String authorization) {
        String accessToken = stripBearer(authorization);
        if (!StringUtils.hasText(accessToken)) {
            throw new BusinessException(BusinessExceptionCode.TOKEN_INVALID);
        }
        JwtClaims claims;
        try {
            claims = jwtService.parseAndValidate(accessToken);
        } catch (Exception e) {
            throw new BusinessException(BusinessExceptionCode.TOKEN_INVALID);
        }
        if (tokenStoreService.isAccessRevoked(claims)) {
            throw new BusinessException(BusinessExceptionCode.TOKEN_INVALID);
        }
        return claims;
    }

    private static String stripBearer(String authorization) {
        if (authorization == null) {
            return null;
        }
        String v = authorization.trim();
        if (v.regionMatches(true, 0, SecurityConstants.BEARER_PREFIX, 0, SecurityConstants.BEARER_PREFIX.length())) {
            v = v.substring(SecurityConstants.BEARER_PREFIX.length()).trim();
        }
        return v;
    }

    private static long secondsUntilExpiry(JwtClaims claims) {
        return claims.getExpiresAt() == null ? 0
                : (claims.getExpiresAt().getTime() - System.currentTimeMillis()) / 1000;
    }

    public Map<String, Object> jwks() {
        RSAPublicKey key = jwtService.getPublicKey();
        Map<String, Object> jwk = new LinkedHashMap<>();
        jwk.put("kty", "RSA");
        jwk.put("use", "sig");
        jwk.put("alg", "RS256");
        jwk.put("kid", "euipd-1");
        jwk.put("n", Base64.getUrlEncoder().withoutPadding().encodeToString(unsigned(key.getModulus().toByteArray())));
        jwk.put("e", Base64.getUrlEncoder().withoutPadding().encodeToString(unsigned(key.getPublicExponent().toByteArray())));
        Map<String, Object> body = new HashMap<>();
        body.put("keys", Collections.singletonList(jwk));
        return body;
    }

    private static byte[] unsigned(byte[] bytes) {
        if (bytes.length > 1 && bytes[0] == 0) {
            return Arrays.copyOfRange(bytes, 1, bytes.length);
        }
        return bytes;
    }

    private String resolveLoginName(String userId) {
        RdmsUser user = rdmsUserService.selectByPrimaryKey(userId);
        if (user != null) {
            return user.getLoginName();
        }
        RdmsCustomer customer = rdmsCustomerService.selectByPrimaryKey(userId);
        if (customer != null) {
            return customer.getContactPhone();
        }
        throw new BusinessException(BusinessExceptionCode.USER_NOT_EXIST_ERROR);
    }
}
