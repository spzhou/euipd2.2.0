/**
 * Author：周朔鹏
 * 编制时间：2026-10-06
 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */
package com.course.file2.rich;

import com.course.server.exception.BusinessException;
import com.course.server.exception.BusinessExceptionCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

/**
 * WOPI 一次性查看凭证（实现方案第 10.3 节）：10 分钟有效，绑定成员、机构与文件；
 * Collabora 取文件内容（GetFile）后即作废，CheckFileInfo 不消耗凭证。
 */
@Service
public class WopiTokenService {

    private static final String KEY_PREFIX = "rdms:wopi:token:";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final ObjectMapper JSON = new ObjectMapper();

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private RichRenderProperties properties;

    @Data
    public static class Grant {
        private String customerId;
        private String userId;
        private String memberId;
        private String userName;
        /** 每次打开单独的文件标识，避免不同查看者共用同一 Collabora 会话。 */
        private String fileId;
        private String renderId;
        private String docId;
        private String versionId;
        private String fileName;
        private long size;
        private String sha256;
    }

    public String issue(Grant grant) {
        byte[] raw = new byte[32];
        RANDOM.nextBytes(raw);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        try {
            stringRedisTemplate.opsForValue().set(KEY_PREFIX + token, JSON.writeValueAsString(grant),
                    properties.getWopiTokenTtlSeconds(), TimeUnit.SECONDS);
        } catch (IOException e) {
            throw new BusinessException(BusinessExceptionCode.RICH_RENDER_FAILED);
        }
        return token;
    }

    /** 校验凭证与文件绑定；consume 为真时校验通过即删除。 */
    public Grant check(String token, String fileId, boolean consume) {
        if (!StringUtils.hasText(token) || !StringUtils.hasText(fileId) || token.length() > 128) {
            throw new BusinessException(BusinessExceptionCode.RICH_WOPI_TOKEN_INVALID);
        }
        String key = KEY_PREFIX + token;
        String value = stringRedisTemplate.opsForValue().get(key);
        if (value == null) {
            throw new BusinessException(BusinessExceptionCode.RICH_WOPI_TOKEN_INVALID);
        }
        Grant grant;
        try {
            grant = JSON.readValue(value, Grant.class);
        } catch (IOException e) {
            throw new BusinessException(BusinessExceptionCode.RICH_WOPI_TOKEN_INVALID);
        }
        if (!fileId.equals(grant.getFileId())) {
            throw new BusinessException(BusinessExceptionCode.RICH_WOPI_TOKEN_INVALID);
        }
        if (consume && !Boolean.TRUE.equals(stringRedisTemplate.delete(key))) {
            throw new BusinessException(BusinessExceptionCode.RICH_WOPI_TOKEN_INVALID);
        }
        return grant;
    }
}
