/**
 * Author：周朔鹏
 * 编制时间：2026-10-06
 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */
package com.course.file2.rich;

import com.course.server.domain.RdmsFile;
import com.course.server.dto.ResponseDto;
import com.course.server.exception.BusinessException;
import com.course.server.exception.BusinessExceptionCode;
import com.course.server.security.tenant.TenantContext;
import com.course.server.service.rdms.mail.MailActor;
import com.course.server.service.rdms.mail.MailAttachableService;
import com.course.server.service.rdms.rich.RichSimpleDocService;
import lombok.Data;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 正式文件的查看与下载（实现方案第 10.2 节）：PDF 以字节流返回给页面内查看器，Word 经 WOPI 在 Collabora 只读打开，
 * 下载取 5 分钟有效的附件地址。每次访问记入 rdms_rich_access_log。
 */
@RestController
@RequestMapping("/rdms/rich-render")
public class RdmsRichRenderController {

    @Resource
    private RichRenderService richRenderService;
    @Resource
    private WopiTokenService wopiTokenService;
    @Resource
    private CollaboraClient collaboraClient;
    @Resource
    private RichRenderProperties properties;
    @Resource
    private RichSimpleDocService richSimpleDocService;
    @Resource
    private MailAttachableService mailAttachableService;

    @Data
    public static class RenderRequest {
        private String versionId;
        /** pdf 或 docx，默认 pdf。 */
        private String format;
        /** 保留原文字颜色，默认按公文要求统一为黑色。 */
        private Boolean keepColor;

        boolean color() {
            return Boolean.TRUE.equals(keepColor);
        }
    }

    @PostMapping("/get-content")
    public ResponseEntity<byte[]> getContent(@RequestBody RenderRequest req, HttpServletRequest http) {
        requireTenant();
        RichRenderService.Rendered r = richRenderService.render(versionIdOf(req), RichRenderService.FORMAT_PDF, req.color());
        byte[] bytes = richRenderService.bytes(r);
        richRenderService.log(r.getModel(), RichRenderService.ACTION_OPEN_PDF, clientIp(http));
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename*=UTF-8''" + encode(r.getFileName()))
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(bytes);
    }

    @PostMapping("/get-viewer")
    public ResponseDto<Map<String, Object>> getViewer(@RequestBody RenderRequest req, HttpServletRequest http) {
        String customerId = requireTenant();
        String viewerPrefix = collaboraClient.viewerUrlPrefix();
        RichRenderService.Rendered r = richRenderService.render(versionIdOf(req), RichRenderService.FORMAT_DOCX, req.color());
        WopiTokenService.Grant grant = new WopiTokenService.Grant();
        grant.setCustomerId(customerId);
        grant.setUserId(TenantContext.getUserId());
        grant.setMemberId(TenantContext.getMembershipId());
        grant.setUserName(richSimpleDocService.userName(TenantContext.getMembershipId()));
        grant.setRenderId(r.getRender().getId());
        grant.setFileId(r.getRender().getId() + "_" + Long.toString(System.nanoTime(), 36));
        grant.setDocId(r.getModel().getDocId());
        grant.setVersionId(r.getModel().getVersionId());
        grant.setFileName(r.getFileName());
        grant.setSize(r.getRender().getSize() == null ? 0 : r.getRender().getSize());
        grant.setSha256(r.getRender().getSha256());
        String token = wopiTokenService.issue(grant);
        richRenderService.log(r.getModel(), RichRenderService.ACTION_OPEN_WORD, clientIp(http));

        String wopiSrc = CollaboraClient.trimSlash(properties.getWopiBaseUrl()) + "/" + grant.getFileId();
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("url", viewerPrefix + "WOPISrc=" + encode(wopiSrc) + "&lang=zh-CN&permission=readonly");
        content.put("accessToken", token);
        // 凭证在 GetFile 后即作废，只读页不再回取；传 0 时 Collabora 不弹会话到期提醒
        content.put("accessTokenTtl", 0);
        content.put("fileName", r.getFileName());
        ResponseDto<Map<String, Object>> dto = new ResponseDto<>();
        dto.setContent(content);
        return dto;
    }

    @PostMapping("/signed-url")
    public ResponseDto<Map<String, Object>> signedUrl(@RequestBody RenderRequest req, HttpServletRequest http) {
        requireTenant();
        String format = RichRenderService.normalizeFormat(req.getFormat());
        RichRenderService.Rendered r = richRenderService.render(versionIdOf(req), format, req.color());
        String url = richRenderService.signedUrl(r);
        richRenderService.log(r.getModel(), RichRenderService.FORMAT_PDF.equals(format)
                ? RichRenderService.ACTION_DOWNLOAD_PDF : RichRenderService.ACTION_DOWNLOAD_WORD, clientIp(http));
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("url", url);
        content.put("fileName", r.getFileName());
        content.put("expiresIn", properties.getSignedUrlSeconds());
        ResponseDto<Map<String, Object>> dto = new ResponseDto<>();
        dto.setContent(content);
        return dto;
    }

    /** 本人编辑保存的版本导出为邮件附件：生成独立的文件记录，返回文件编号与快照信息。 */
    @PostMapping("/save-as-file")
    public ResponseDto<Map<String, Object>> saveAsFile(@RequestBody RenderRequest req, HttpServletRequest http) {
        MailActor actor = MailActor.current();
        String versionId = versionIdOf(req);
        mailAttachableService.requireOwnVersion(actor, versionId);
        String format = RichRenderService.normalizeFormat(req.getFormat());
        RichRenderService.Rendered r = richRenderService.render(versionId, format, req.color());
        RdmsFile file = richRenderService.saveAsMailFile(r, actor.getMemberId());
        richRenderService.log(r.getModel(), RichRenderService.FORMAT_PDF.equals(format)
                ? RichRenderService.ACTION_DOWNLOAD_PDF : RichRenderService.ACTION_DOWNLOAD_WORD, clientIp(http));
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("id", file.getId());
        content.put("name", file.getName());
        content.put("size", file.getSize());
        content.put("suffix", file.getSuffix());
        ResponseDto<Map<String, Object>> dto = new ResponseDto<>();
        dto.setContent(content);
        return dto;
    }

    private static String versionIdOf(RenderRequest req) {
        if (req == null || !StringUtils.hasText(req.getVersionId())) {
            throw new BusinessException(BusinessExceptionCode.INPUT_ERROR);
        }
        return req.getVersionId();
    }

    static String requireTenant() {
        String customerId = TenantContext.getTenantId();
        if (!StringUtils.hasText(customerId)) {
            throw new BusinessException(BusinessExceptionCode.NO_PERMISSION);
        }
        return customerId;
    }

    /** 用户来源地址：经网关时取 X-Forwarded-For 的第一个地址。 */
    static String clientIp(HttpServletRequest http) {
        String xff = http.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xff)) {
            return xff.split(",")[0].trim();
        }
        return http.getRemoteAddr();
    }

    static String encode(String s) {
        try {
            return URLEncoder.encode(s, StandardCharsets.UTF_8.name()).replace("+", "%20");
        } catch (java.io.UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }
}
