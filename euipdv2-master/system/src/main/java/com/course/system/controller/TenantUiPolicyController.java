/**
 * Author：周朔鹏
 * 编制时间：2026-10-03
 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */
package com.course.system.controller;

import com.course.server.exception.BusinessException;
import com.course.server.exception.BusinessExceptionCode;
import com.course.server.dto.ResponseDto;
import com.course.server.security.SecurityConstants;
import com.course.server.security.permission.TenantUiPolicyService;
import com.course.server.security.tenant.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 本机构菜单与按钮复选框。机构标识只取令牌。超管不在此改策略。
 */
@RestController
@RequestMapping("/tenant-ui-policy")
public class TenantUiPolicyController {

    private static final Logger LOG = LoggerFactory.getLogger(TenantUiPolicyController.class);
    public static final String BUSINESS_NAME = "机构权限策略";
    private static final String SUPER_CUSTOMER_ID = "fm4ek0Zy";

    @Resource
    private TenantUiPolicyService tenantUiPolicyService;

    @GetMapping
    public ResponseDto<Map<String, Object>> get() {
        ResponseDto<Map<String, Object>> dto = new ResponseDto<>();
        assertAllowed();
        dto.setContent(tenantUiPolicyService.view(TenantContext.getTenantId()));
        return dto;
    }

    /** 平台模板相对本机构保存时基准的差异。 */
    @GetMapping("/diff")
    public ResponseDto<Map<String, Object>> diff() {
        ResponseDto<Map<String, Object>> dto = new ResponseDto<>();
        assertAllowed();
        dto.setContent(tenantUiPolicyService.diff(TenantContext.getTenantId()));
        return dto;
    }

    @PutMapping
    public ResponseDto<Void> save(@RequestBody Map<String, Object> body) {
        ResponseDto<Void> dto = new ResponseDto<>();
        assertAllowed();
        int saved = tenantUiPolicyService.save(TenantContext.getTenantId(), operatorId(), rolesOf(body));
        dto.setMessage(saved == 0
                ? "各角色与平台默认一致，已清除本机构调整。相关用户重新登录后生效"
                : "已保存 " + saved + " 个角色的调整。相关用户重新登录后生效");
        return dto;
    }

    @PostMapping("/reset")
    public ResponseDto<Void> reset() {
        ResponseDto<Void> dto = new ResponseDto<>();
        assertAllowed();
        tenantUiPolicyService.reset(TenantContext.getTenantId(), operatorId());
        dto.setMessage("已恢复平台默认。相关用户重新登录后生效");
        return dto;
    }

    private void assertAllowed() {
        String deny = denyReason();
        if (deny != null) {
            throw new BusinessException(BusinessExceptionCode.NO_PERMISSION, deny);
        }
    }

    private String denyReason() {
        String tenantId = TenantContext.getTenantId();
        if (!StringUtils.hasText(tenantId)) {
            return "缺少机构身份";
        }
        if (SUPER_CUSTOMER_ID.equals(tenantId) || hasRole("SUPER")) {
            return "平台超管不在此配置机构权限";
        }
        if (!hasRole("UltiMng")) {
            return "只有本机构最终管理人可以配置";
        }
        return null;
    }

    private boolean hasRole(String code) {
        HttpServletRequest request = currentRequest();
        if (request == null) {
            return false;
        }
        Object raw = request.getAttribute(SecurityConstants.ATTR_ROLES);
        if (!(raw instanceof Collection)) {
            return false;
        }
        for (Object item : (Collection<?>) raw) {
            if (item != null && code.equalsIgnoreCase(String.valueOf(item).trim())) {
                return true;
            }
        }
        return false;
    }

    private String operatorId() {
        HttpServletRequest request = currentRequest();
        if (request == null) {
            return "";
        }
        Object raw = request.getAttribute(SecurityConstants.ATTR_MEMBERSHIP_ID);
        if (raw != null && StringUtils.hasText(String.valueOf(raw))) {
            return String.valueOf(raw);
        }
        raw = request.getAttribute(SecurityConstants.ATTR_USER_ID);
        return raw == null ? "" : String.valueOf(raw);
    }

    @SuppressWarnings("unchecked")
    private Map<String, List<String>> rolesOf(Map<String, Object> body) {
        if (body == null || !(body.get("roles") instanceof Map)) {
            throw new BusinessException(BusinessExceptionCode.TENANT_POLICY_INVALID, "缺少角色资源号");
        }
        Map<String, Object> raw = (Map<String, Object>) body.get("roles");
        Map<String, List<String>> roles = new java.util.LinkedHashMap<>();
        for (Map.Entry<String, Object> e : raw.entrySet()) {
            if (e.getKey() == null) {
                continue;
            }
            List<String> ids = new java.util.ArrayList<>();
            if (e.getValue() instanceof List) {
                for (Object id : (List<?>) e.getValue()) {
                    if (id != null && StringUtils.hasText(String.valueOf(id))) {
                        ids.add(String.valueOf(id).trim().toUpperCase(Locale.ROOT));
                    }
                }
            }
            roles.put(e.getKey(), ids);
        }
        return roles;
    }

    private HttpServletRequest currentRequest() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes)) {
            return null;
        }
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }
}
