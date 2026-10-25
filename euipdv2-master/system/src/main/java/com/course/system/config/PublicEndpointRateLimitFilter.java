/**
 * Author：周朔鹏
 * 编制时间：2026-10-10
 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */
package com.course.system.config;

import com.alibaba.fastjson.JSON;
import com.course.server.exception.BusinessExceptionCode;
import com.course.server.security.PublicRateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 登录前公开接口按“接口组 + 来源地址”限流，超限返回 HTTP 429。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 5)
public class PublicEndpointRateLimitFilter extends OncePerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(PublicEndpointRateLimitFilter.class);

    private static final Map<String, String> EXACT_GROUPS = new LinkedHashMap<>();
    private static final Map<String, String> PREFIX_GROUPS = new LinkedHashMap<>();

    static {
        EXACT_GROUPS.put("/auth/login", "login");
        EXACT_GROUPS.put("/auth/refresh", "refresh");
        EXACT_GROUPS.put("/auth/password/initial", "pwd-initial");
        EXACT_GROUPS.put("/rdms/user/reset-password", "reset-password");
        EXACT_GROUPS.put("/rdms/sms/send", "sms-send");
        EXACT_GROUPS.put("/auth2/getToken", "auth2-token");
        PREFIX_GROUPS.put("/rdms/user/login-salt/", "login-salt");
        PREFIX_GROUPS.put("/rdms/user/list-customer/", "list-customer");
        PREFIX_GROUPS.put("/rdms/kaptcha/", "kaptcha");
        PREFIX_GROUPS.put("/rdms/customer/get-customer-by-loginname/", "customer-name");
    }

    @Resource
    private PublicRateLimiter publicRateLimiter;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String group = resolveGroup(pathWithinApplication(request));
        if (group != null) {
            boolean allowed;
            try {
                allowed = publicRateLimiter.tryAcquire(group, clientIp(request));
            } catch (RuntimeException e) {
                LOG.warn("公开接口限流计数失败，放行 group={} err={}", group, e.getMessage());
                allowed = true;
            }
            if (!allowed) {
                writeTooManyRequests(response);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    static String resolveGroup(String path) {
        if (path == null) {
            return null;
        }
        String group = EXACT_GROUPS.get(path);
        if (group != null) {
            return group;
        }
        for (Map.Entry<String, String> entry : PREFIX_GROUPS.entrySet()) {
            if (path.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    static String pathWithinApplication(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri == null) {
            return null;
        }
        String contextPath = request.getContextPath();
        if (StringUtils.hasText(contextPath) && uri.startsWith(contextPath)) {
            uri = uri.substring(contextPath.length());
        }
        // 去掉矩阵参数与重复斜杠，避免变体路径绕过分组
        uri = uri.replaceAll(";[^/]*", "").replaceAll("/{2,}", "/");
        if (uri.length() > 1 && uri.endsWith("/") && EXACT_GROUPS.containsKey(uri.substring(0, uri.length() - 1))) {
            uri = uri.substring(0, uri.length() - 1);
        }
        return uri;
    }

    /**
     * 网关在 X-Forwarded-For 末尾追加其看到的来源地址；客户端自带的前段不可信。
     */
    static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) {
            String[] parts = forwarded.split(",");
            String last = parts[parts.length - 1].trim();
            if (StringUtils.hasText(last)) {
                return last;
            }
        }
        return request.getRemoteAddr();
    }

    private static void writeTooManyRequests(HttpServletResponse response) throws IOException {
        response.setStatus(429);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("code", BusinessExceptionCode.REQUEST_TOO_FREQUENT.name());
        body.put("message", BusinessExceptionCode.REQUEST_TOO_FREQUENT.getDesc());
        response.getWriter().write(JSON.toJSONString(body));
    }
}
