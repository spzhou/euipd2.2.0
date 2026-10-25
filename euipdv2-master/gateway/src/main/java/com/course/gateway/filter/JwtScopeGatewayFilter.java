/**
 * Author：周朔鹏
 * 编制时间：2026-08-01
 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */
package com.course.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.course.gateway.security.GatewayJwtVerifier;
import com.course.gateway.security.GatewayModulePathMatcher;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JWT + Scope gateway auth. Does NOT use resource.json / requests for allowlisting.
 */
@Component
public class JwtScopeGatewayFilter implements GatewayFilter, Ordered {

    private static final Logger LOG = LoggerFactory.getLogger(JwtScopeGatewayFilter.class);
    private static final String JTI_BLACKLIST_PREFIX = "euipd:auth:jti:blacklist:";
    private static final String REVOKED_PREFIX = "euipd:auth:revoked:";
    private static final String TENANT_REVOKED_PREFIX = "euipd:auth:tenant-revoked:";
    /** 令牌只认 Authorization；旧客户端附带的同值 token 头转发会让下游请求头超出 Tomcat 上限 */
    private static final String LEGACY_TOKEN_HEADER = "token";
    private static final String[] IDENTITY_HEADERS = {
            "X-Tenant-Id", "X-User-Id", "X-Membership-Id", "X-User-Group", "X-Scopes"
    };

    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private GatewayJwtVerifier jwtVerifier;
    @Resource
    private GatewayModulePathMatcher pathMatcher;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        String method = exchange.getRequest().getMethod() != null
                ? exchange.getRequest().getMethod().name() : "GET";

        if (!pathMatcher.needIntercept(path) || pathMatcher.isWhitelisted(path)) {
            return chain.filter(withoutIdentityHeaders(exchange));
        }

        String rawToken = extractToken(exchange);
        if (!StringUtils.hasText(rawToken)) {
            return writeError(exchange, HttpStatus.UNAUTHORIZED, "TOKEN_MISSING", "未登录或登录凭证缺失");
        }

        Claims claims;
        try {
            claims = jwtVerifier.parse(rawToken);
        } catch (ExpiredJwtException e) {
            return writeError(exchange, HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED", "登录凭证已过期");
        } catch (Exception e) {
            LOG.debug("JWT verify failed: {}", e.getMessage());
            return writeError(exchange, HttpStatus.UNAUTHORIZED, "TOKEN_INVALID", "登录凭证无效或已过期");
        }
        if (isRevoked(claims)) {
            return writeError(exchange, HttpStatus.UNAUTHORIZED, "TOKEN_INVALID", "登录凭证无效或已过期");
        }
        List<String> scopes = GatewayJwtVerifier.scopes(claims);
        if (!authorizeScopes(scopes, path, method)) {
            LOG.warn("Scope denied path={} method={} scopes={}", path, method, scopes);
            return writeError(exchange, HttpStatus.FORBIDDEN, "NO_PERMISSION", "没有权限");
        }
        ServerHttpRequest mutated = exchange.getRequest().mutate()
                .headers(headers -> headers.remove(LEGACY_TOKEN_HEADER))
                .header("X-Tenant-Id", nullToEmpty(GatewayJwtVerifier.asString(claims.get("tid"))))
                .header("X-User-Id", nullToEmpty(claims.getSubject()))
                .header("X-Membership-Id", nullToEmpty(GatewayJwtVerifier.asString(claims.get("mid"))))
                .header("X-User-Group", nullToEmpty(GatewayJwtVerifier.asString(claims.get("ug"))))
                .header("X-Scopes", String.join(",", scopes))
                .build();
        return chain.filter(exchange.mutate().request(mutated).build());
    }

    /**
     * 一次读取 jti 黑名单、主体与机构的吊销时刻；签发时刻早于任一吊销时刻的令牌视为失效。
     * 键与值的序列化须与 TokenStoreService 一致。
     */
    @SuppressWarnings("unchecked")
    private boolean isRevoked(Claims claims) {
        String jti = claims.getId();
        String subject = claims.getSubject();
        String tenantId = GatewayJwtVerifier.asString(claims.get("tid"));
        List<Object> values = redisTemplate.opsForValue().multiGet(Arrays.asList(
                JTI_BLACKLIST_PREFIX + (jti == null ? "" : jti),
                REVOKED_PREFIX + (subject == null ? "" : subject),
                TENANT_REVOKED_PREFIX + (tenantId == null ? "" : tenantId)));
        if (values == null) {
            return false;
        }
        if (StringUtils.hasText(jti) && values.size() > 0 && values.get(0) != null) {
            return true;
        }
        long issuedAt = claims.getIssuedAt() == null ? 0L : claims.getIssuedAt().getTime() / 1000L;
        for (int i = 1; i < values.size(); i++) {
            long revokedAt = toLong(values.get(i));
            if (revokedAt > 0 && issuedAt < revokedAt) {
                return true;
            }
        }
        return false;
    }

    private static long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        try {
            return Long.parseLong(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private boolean authorizeScopes(List<String> scopes, String path, String method) {
        String module = pathMatcher.matchModule(path);
        if (module == null) {
            LOG.info("Unmapped module path (authenticated allow): {}", path);
            return true;
        }
        String operation = pathMatcher.inferOperation(method, path);
        return pathMatcher.hasScope(scopes, module, operation);
    }

    private String extractToken(ServerWebExchange exchange) {
        String auth = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (StringUtils.hasText(auth) && auth.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return auth.substring(7).trim();
        }
        return null;
    }

    private static ServerWebExchange withoutIdentityHeaders(ServerWebExchange exchange) {
        ServerHttpRequest stripped = exchange.getRequest().mutate()
                .headers(headers -> {
                    for (String name : IDENTITY_HEADERS) {
                        headers.remove(name);
                    }
                })
                .build();
        return exchange.mutate().request(stripped).build();
    }

    private static String nullToEmpty(String v) {
        return v == null ? "" : v;
    }

    /**
     * 与业务 ResponseDto 字段对齐的 JSON 错误体（HTTP 状态仍为 401/403）。
     */
    private Mono<Void> writeError(ServerWebExchange exchange, HttpStatus status, String code, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("code", code);
        body.put("message", message);
        body.put("content", null);
        byte[] bytes = JSON.toJSONString(body).getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
