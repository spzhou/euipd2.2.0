/**
 * Author：周朔鹏
 * 编制时间：2026-10-06
 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */
package com.course.file2.rich;

import com.course.server.domain.RdmsRichAsset;
import com.course.server.dto.ResponseDto;
import com.course.server.exception.BusinessException;
import com.course.server.exception.BusinessExceptionCode;
import com.course.server.security.tenant.TenantContext;
import com.course.server.service.rdms.RdmsRichAssetService;
import com.course.server.service.rdms.rich.RichDocSchemaRegistry;
import com.course.server.storage.OssTargetResolver;
import com.course.server.storage.StorageTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Word 导入解析（实现方案第 10.4 节）：.docx 直接由 POI 解析，.doc/.wps/.odt/.rtf 先经 Collabora 转为 .docx；
 * 按一级标题与章节别名拆分，图片上传为 TEMP 图片。返回结果只用于填充编辑器，用户保存时来源记为 IMPORT。
 */
@RestController
@RequestMapping("/rdms/rich-doc")
public class RdmsRichDocImportController {

    private static final Logger LOG = LoggerFactory.getLogger(RdmsRichDocImportController.class);
    static final List<String> EXTENSIONS = Arrays.asList("docx", "doc", "wps", "odt", "rtf");

    @Resource
    private RichRenderProperties properties;
    @Resource
    private CollaboraClient collaboraClient;
    @Resource
    private RdmsRichAssetService rdmsRichAssetService;
    @Resource
    private OssTargetResolver ossTargetResolver;

    @PostMapping("/word-parse")
    public ResponseDto<Map<String, Object>> wordParse(@RequestParam("file") MultipartFile file,
                                                      @RequestParam("bizType") String bizType) throws IOException {
        String customerId = RdmsRichRenderController.requireTenant();
        if (!RichDocSchemaRegistry.known(bizType)) {
            throw new BusinessException(BusinessExceptionCode.INPUT_ERROR);
        }
        String ext = extension(file == null ? null : file.getOriginalFilename());
        if (file == null || file.isEmpty() || file.getSize() > properties.getImportMaxBytes() || !EXTENSIONS.contains(ext)) {
            throw new BusinessException(BusinessExceptionCode.RICH_IMPORT_FORMAT_INVALID);
        }
        byte[] data = file.getBytes();
        if (!signatureMatches(ext, data)) {
            throw new BusinessException(BusinessExceptionCode.RICH_IMPORT_FORMAT_INVALID);
        }
        byte[] docx = "docx".equals(ext) ? data : collaboraClient.convert(data, "import." + ext, "docx");

        String uploaderId = StringUtils.hasText(TenantContext.getMembershipId())
                ? TenantContext.getMembershipId() : TenantContext.getUserId();
        StorageTarget target = ossTargetResolver.docTarget(customerId);
        RichWordParser.Result parsed;
        try {
            parsed = new RichWordParser(bizType, bytes -> {
                if (bytes == null || bytes.length > RdmsRichAssetService.MAX_BYTES) {
                    return null;
                }
                RdmsRichAsset asset = rdmsRichAssetService.upload(target, customerId, uploaderId, bytes);
                return asset.getId();
            }).parse(docx);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            LOG.warn("word parse failed ext={}: {}", ext, e.getMessage());
            throw new BusinessException(BusinessExceptionCode.RICH_IMPORT_FORMAT_INVALID);
        }

        Map<String, Object> content = new LinkedHashMap<>();
        content.put("sections", parsed.getSections());
        content.put("unassigned", parsed.getUnassigned());
        content.put("matchedHeadings", parsed.getMatchedHeadings());
        content.put("warnings", parsed.getWarnings());
        content.put("images", parsed.getImages());
        content.put("source", "IMPORT");
        ResponseDto<Map<String, Object>> dto = new ResponseDto<>();
        dto.setContent(content);
        return dto;
    }

    static String extension(String name) {
        if (name == null) {
            return "";
        }
        int i = name.lastIndexOf('.');
        return i < 0 ? "" : name.substring(i + 1).toLowerCase(Locale.ROOT);
    }

    /** 文件头与扩展名一致：docx/odt 为 ZIP，doc/wps 为 OLE 复合文档，rtf 以 {\rtf 开头。 */
    static boolean signatureMatches(String ext, byte[] d) {
        if (d == null || d.length < 8) {
            return false;
        }
        switch (ext) {
            case "docx":
            case "odt":
                return d[0] == 'P' && d[1] == 'K' && d[2] == 3 && d[3] == 4;
            case "doc":
            case "wps":
                return (d[0] & 0xff) == 0xD0 && (d[1] & 0xff) == 0xCF && (d[2] & 0xff) == 0x11 && (d[3] & 0xff) == 0xE0;
            case "rtf":
                return d[0] == '{' && d[1] == '\\' && d[2] == 'r' && d[3] == 't' && d[4] == 'f';
            default:
                return false;
        }
    }
}
