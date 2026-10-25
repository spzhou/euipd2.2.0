/**
 * Author：周朔鹏
 * 编制时间：2026-10-06
 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */
package com.course.file2.rich;

import com.course.server.domain.RdmsRichRender;
import com.course.server.exception.BusinessException;
import com.course.server.security.tenant.TenantScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.function.Supplier;

/**
 * WOPI 宿主（实现方案第 10.3 节），只供 Collabora 回调，只实现 CheckFileInfo 与 GetFile，文件只读。
 * 路径在网关与服务的免鉴权前缀中，凭一次性查看凭证访问，并校验来源地址。
 */
@RestController
@RequestMapping("/wopi/files")
public class WopiController {

    private static final Logger LOG = LoggerFactory.getLogger(WopiController.class);

    @Resource
    private WopiTokenService wopiTokenService;
    @Resource
    private RichRenderService richRenderService;
    @Resource
    private RichRenderProperties properties;

    @GetMapping(value = "/{fileId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> checkFileInfo(@PathVariable String fileId,
                                           @RequestParam(value = "access_token", required = false) String token,
                                           HttpServletRequest http) {
        return guarded(http, () -> {
            WopiTokenService.Grant g = wopiTokenService.check(token, fileId, false);
            Map<String, Object> info = new LinkedHashMap<>();
            info.put("BaseFileName", g.getFileName());
            info.put("Size", g.getSize());
            info.put("Version", StringUtils.hasText(g.getSha256()) ? g.getSha256() : g.getRenderId());
            info.put("OwnerId", g.getCustomerId());
            info.put("UserId", StringUtils.hasText(g.getMemberId()) ? g.getMemberId() : g.getUserId());
            info.put("UserFriendlyName", StringUtils.hasText(g.getUserName()) ? g.getUserName() : "查看者");
            info.put("UserCanWrite", false);
            info.put("ReadOnly", true);
            info.put("UserCanNotWriteRelative", true);
            info.put("SupportsUpdate", false);
            info.put("SupportsLocks", false);
            info.put("HideSaveOption", true);
            info.put("LastModifiedTime", iso(new Date()));
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(info);
        });
    }

    @GetMapping("/{fileId}/contents")
    public ResponseEntity<?> getFile(@PathVariable String fileId,
                                     @RequestParam(value = "access_token", required = false) String token,
                                     HttpServletRequest http) {
        return guarded(http, () -> {
            WopiTokenService.Grant g = wopiTokenService.check(token, fileId, true);
            return TenantScope.callAs(g.getCustomerId(), g.getUserId(), g.getMemberId(), () -> {
                RdmsRichRender render = richRenderService.getRender(g.getRenderId());
                if (render == null || !g.getCustomerId().equals(render.getCustomerId())) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                }
                byte[] bytes = richRenderService.bytesOf(render);
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .header("X-WOPI-ItemVersion", StringUtils.hasText(render.getSha256()) ? render.getSha256() : render.getId())
                        .body(bytes);
            });
        });
    }

    private ResponseEntity<?> guarded(HttpServletRequest http, Supplier<ResponseEntity<?>> action) {
        String source = sourceAddress(http);
        if (!allowed(source, properties.allowedHosts())) {
            LOG.warn("wopi request rejected from {}", source);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            return action.get();
        } catch (BusinessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (RuntimeException e) {
            LOG.warn("wopi request failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /** 直连时取对端地址；经网关转发时取 X-Forwarded-For 的最后一个地址（由最近一跳代理写入，不可由调用方伪造）。 */
    static String sourceAddress(HttpServletRequest http) {
        String xff = http.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xff)) {
            String[] parts = xff.split(",");
            return parts[parts.length - 1].trim();
        }
        return http.getRemoteAddr();
    }

    static boolean allowed(String source, List<String> allowedHosts) {
        if (allowedHosts.isEmpty()) {
            return true;
        }
        if (!StringUtils.hasText(source)) {
            return false;
        }
        for (String rule : allowedHosts) {
            if (rule.endsWith(".") || rule.endsWith(":") ? source.startsWith(rule) : source.equals(rule)) {
                return true;
            }
        }
        return false;
    }

    private static String iso(Date d) {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        f.setTimeZone(TimeZone.getTimeZone("UTC"));
        return f.format(d);
    }
}
