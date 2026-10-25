/**
 * Author：周朔鹏
 * 编制时间：2026-10-06
 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */
package com.course.file2.rich;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件生成与在线查看配置（实现方案第 10 节）。
 */
@Getter
@Component
public class RichRenderProperties {

    /** file2 访问 Collabora 的内部地址，如 http://collabora:9980 。 */
    @Value("${rdms.rich.collabora.url:http://127.0.0.1:18980}")
    private String collaboraUrl;

    /** 浏览器访问 Collabora 的地址（经网关或反向代理），用于拼接查看页。为空时取 collaboraUrl。 */
    @Value("${rdms.rich.collabora.public-url:}")
    private String collaboraPublicUrl;

    /** Collabora 回调 WOPI 的 file2 地址，如 http://host.docker.internal:18003/file/wopi/files 。 */
    @Value("${rdms.rich.wopi.base-url:http://host.docker.internal:18003/file/wopi/files}")
    private String wopiBaseUrl;

    /** 允许调用 WOPI 接口的来源地址（逗号分隔，支持前缀匹配，如 172.16.）。为空时不校验来源。 */
    @Value("${rdms.rich.wopi.allowed-hosts:}")
    private String wopiAllowedHosts;

    @Value("${rdms.rich.wopi.token-ttl-seconds:600}")
    private long wopiTokenTtlSeconds;

    @Value("${rdms.rich.render.template-rev:gbt9704-r3}")
    private String templateRev;

    @Value("${rdms.rich.render.watermark:}")
    private String watermark;

    @Value("${rdms.rich.render.timeout-seconds:60}")
    private long timeoutSeconds;

    @Value("${rdms.rich.render.retention-days:30}")
    private long retentionDays;

    @Value("${rdms.rich.render.signed-url-seconds:300}")
    private long signedUrlSeconds;

    @Value("${rdms.rich.import.max-bytes:20971520}")
    private long importMaxBytes;

    public String publicUrl() {
        return StringUtils.hasText(collaboraPublicUrl) ? collaboraPublicUrl : collaboraUrl;
    }

    public List<String> allowedHosts() {
        if (!StringUtils.hasText(wopiAllowedHosts)) {
            return Collections.emptyList();
        }
        return Arrays.stream(wopiAllowedHosts.split(",")).map(String::trim)
                .filter(StringUtils::hasText).collect(Collectors.toList());
    }
}
