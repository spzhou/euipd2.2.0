/**
 * Author：周朔鹏
 * 编制时间：2026-10-06
 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */
package com.course.file2.rich;

import com.course.server.exception.BusinessException;
import com.course.server.exception.BusinessExceptionCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

/**
 * 调用 Collabora CODE 的 /cool/convert-to 进行格式转换（docx→pdf、doc/wps/odt/rtf→docx），超时 60 秒。
 */
@Component
public class CollaboraClient {

    private static final Logger LOG = LoggerFactory.getLogger(CollaboraClient.class);

    @Resource
    private RichRenderProperties properties;

    private volatile HttpClient client;
    private volatile String viewerPrefix;

    public byte[] convert(byte[] data, String fileName, String targetFormat) {
        String boundary = "----euipd" + UUID.randomUUID().toString().replace("-", "");
        byte[] body;
        try {
            body = multipart(boundary, fileName, data);
        } catch (IOException e) {
            throw new BusinessException(BusinessExceptionCode.RICH_CONVERT_FAILED);
        }
        Duration timeout = Duration.ofSeconds(properties.getTimeoutSeconds());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(trimSlash(properties.getCollaboraUrl()) + "/cool/convert-to/" + targetFormat))
                .timeout(timeout)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();
        try {
            HttpResponse<byte[]> response = client(timeout).send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() != 200 || response.body() == null || response.body().length == 0) {
                LOG.warn("collabora convert-to {} failed status={}", targetFormat, response.statusCode());
                throw new BusinessException(BusinessExceptionCode.RICH_CONVERT_FAILED);
            }
            return response.body();
        } catch (ConnectException e) {
            LOG.warn("collabora unreachable for convert-to {}: {}", targetFormat, e.getMessage());
            throw new BusinessException(BusinessExceptionCode.RICH_CONVERTER_UNAVAILABLE);
        } catch (IOException e) {
            LOG.warn("collabora convert-to {} failed: {}", targetFormat, e.getMessage());
            throw new BusinessException(BusinessExceptionCode.RICH_CONVERT_FAILED);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(BusinessExceptionCode.RICH_CONVERT_FAILED);
        }
    }

    /**
     * 查看页地址前缀（以 ? 或 & 结尾）：取 /hosting/discovery 中 docx 的 view 动作，并把主机替换为浏览器可访问的地址。
     * 取不到 discovery 时 Collabora 未启用或未就绪，此时拼出的查看页只会得到 502，因此直接报错。
     */
    public String viewerUrlPrefix() {
        String cached = viewerPrefix;
        if (cached != null) {
            return cached;
        }
        String prefix = trimSlash(properties.publicUrl()) + "/browser/dist/cool.html?";
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(trimSlash(properties.getCollaboraUrl()) + "/hosting/discovery"))
                    .timeout(Duration.ofSeconds(10)).GET().build();
            HttpResponse<String> response = client(Duration.ofSeconds(10)).send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                LOG.warn("collabora discovery failed status={}", response.statusCode());
                throw new BusinessException(BusinessExceptionCode.RICH_VIEWER_UNAVAILABLE);
            }
            java.util.regex.Matcher m = java.util.regex.Pattern
                    .compile("<action[^>]*ext=\"docx\"[^>]*name=\"view\"[^>]*urlsrc=\"([^\"]+)\"|"
                            + "<action[^>]*name=\"view\"[^>]*ext=\"docx\"[^>]*urlsrc=\"([^\"]+)\"")
                    .matcher(response.body());
            if (m.find()) {
                String src = m.group(1) != null ? m.group(1) : m.group(2);
                URI u = URI.create(src);
                prefix = trimSlash(properties.publicUrl()) + u.getRawPath() + "?";
                viewerPrefix = prefix;
            }
        } catch (IOException e) {
            LOG.warn("collabora discovery failed: {}", e.getMessage());
            throw new BusinessException(BusinessExceptionCode.RICH_VIEWER_UNAVAILABLE);
        } catch (IllegalArgumentException e) {
            LOG.warn("collabora discovery urlsrc invalid: {}", e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(BusinessExceptionCode.RICH_VIEWER_UNAVAILABLE);
        }
        return prefix;
    }

    private HttpClient client(Duration timeout) {
        if (client == null) {
            synchronized (this) {
                if (client == null) {
                    client = HttpClient.newBuilder().connectTimeout(timeout)
                            .version(HttpClient.Version.HTTP_1_1).build();
                }
            }
        }
        return client;
    }

    private static byte[] multipart(String boundary, String fileName, byte[] data) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(data.length + 512);
        String safeName = fileName.replaceAll("[\"\\r\\n]", "_");
        out.write(("--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"data\"; filename=\"" + safeName + "\"\r\n"
                + "Content-Type: application/octet-stream\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        out.write(data);
        out.write(("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
        return out.toByteArray();
    }

    static String trimSlash(String url) {
        String u = url == null ? "" : url.trim();
        while (u.endsWith("/")) {
            u = u.substring(0, u.length() - 1);
        }
        return u;
    }
}
