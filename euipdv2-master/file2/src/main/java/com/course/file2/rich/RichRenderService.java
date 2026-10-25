/**
 * Author：周朔鹏
 * 编制时间：2026-10-06
 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */
package com.course.file2.rich;

import com.course.server.domain.RdmsFile;
import com.course.server.domain.RdmsRichAccessLog;
import com.course.server.domain.RdmsRichAsset;
import com.course.server.domain.RdmsRichRender;
import com.course.server.domain.RdmsRichRenderExample;
import com.course.server.enums.FileGroupingEnum;
import com.course.server.exception.BusinessException;
import com.course.server.exception.BusinessExceptionCode;
import com.course.server.mapper.RdmsRichAccessLogMapper;
import com.course.server.mapper.RdmsRichRenderMapper;
import com.course.server.security.tenant.TenantContext;
import com.course.server.service.rdms.RdmsFileService;
import com.course.server.service.rdms.RdmsRichAssetService;
import com.course.server.service.rdms.rich.RichRenderModelService;
import com.course.server.storage.ObjectStorageService;
import com.course.server.storage.OssTargetResolver;
import com.course.server.storage.StorageTarget;
import com.course.server.util.UuidUtil;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 按版本生成正式文件并缓存（实现方案第 10.1、10.2 节）：
 * docx 由 POI 按公文版式生成，pdf 由 Collabora 从 docx 转换；同一版本、格式、模板修订号与选项只生成一次，
 * 对象键为 {机构ID}/rich-render/{versionId}/{template_rev}-{options}.{format}。
 */
@Service
public class RichRenderService {

    private static final Logger LOG = LoggerFactory.getLogger(RichRenderService.class);

    public static final String FORMAT_PDF = "pdf";
    public static final String FORMAT_DOCX = "docx";
    public static final String OPTIONS_MONO = "mono";
    public static final String OPTIONS_COLOR = "color";

    public static final String ACTION_OPEN_PDF = "OPEN_PDF";
    public static final String ACTION_OPEN_WORD = "OPEN_WORD";
    public static final String ACTION_DOWNLOAD_PDF = "DOWNLOAD_PDF";
    public static final String ACTION_DOWNLOAD_WORD = "DOWNLOAD_WORD";

    @Resource
    private RichRenderModelService richRenderModelService;
    @Resource
    private RdmsRichRenderMapper rdmsRichRenderMapper;
    @Resource
    private RdmsRichAccessLogMapper rdmsRichAccessLogMapper;
    @Resource
    private RdmsRichAssetService rdmsRichAssetService;
    @Resource
    private ObjectStorageService objectStorageService;
    @Resource
    private OssTargetResolver ossTargetResolver;
    @Resource
    private CollaboraClient collaboraClient;
    @Resource
    private RichRenderProperties properties;
    @Resource
    private RdmsFileService rdmsFileService;

    @Data
    public static class Rendered {
        private RdmsRichRender render;
        private RichRenderModelService.Model model;
        private StorageTarget target;
        private String fileName;
    }

    public static String normalizeFormat(String format) {
        String f = format == null ? FORMAT_PDF : format.trim().toLowerCase(Locale.ROOT);
        if ("word".equals(f) || "doc".equals(f)) {
            f = FORMAT_DOCX;
        }
        if (!FORMAT_PDF.equals(f) && !FORMAT_DOCX.equals(f)) {
            throw new BusinessException(BusinessExceptionCode.INPUT_ERROR);
        }
        return f;
    }

    /** 取缓存或生成；版本须属于当前机构，且当前用户具备所属模块的查看权限。 */
    public Rendered render(String versionId, String format, boolean keepColor) {
        String f = normalizeFormat(format);
        String options = keepColor ? OPTIONS_COLOR : OPTIONS_MONO;
        RichRenderModelService.Model model = richRenderModelService.build(versionId);
        StorageTarget target = ossTargetResolver.docTarget(model.getCustomerId());
        Rendered out = new Rendered();
        out.setModel(model);
        out.setTarget(target);
        out.setFileName(model.fileName(f));

        RdmsRichRender cached = find(versionId, f, options);
        if (cached != null && objectStorageService.headObject(target, cached.getPath())) {
            out.setRender(cached);
            return out;
        }
        if (cached != null) {
            rdmsRichRenderMapper.deleteByPrimaryKey(cached.getId());
        }

        byte[] bytes;
        try {
            byte[] docx = new GbtDocxWriter(keepColor, assetId -> loadAsset(model.getCustomerId(), target, assetId),
                    properties.getWatermark()).write(model);
            bytes = FORMAT_PDF.equals(f) ? collaboraClient.convert(docx, "render.docx", FORMAT_PDF) : docx;
        } catch (Exception e) {
            LOG.warn("rich render failed version={} format={}: {}", versionId, f, e.getMessage());
            throw new BusinessException(BusinessExceptionCode.RICH_RENDER_FAILED);
        }
        String path = model.getCustomerId() + "/rich-render/" + versionId + "/"
                + properties.getTemplateRev() + "-" + options + "." + f;
        objectStorageService.putObject(target, path, bytes, contentType(f));

        RdmsRichRender row = new RdmsRichRender();
        row.setId(UuidUtil.getShortUuid());
        row.setCustomerId(model.getCustomerId());
        row.setVersionId(versionId);
        row.setFormat(f);
        row.setTemplateRev(properties.getTemplateRev());
        row.setOptions(options);
        row.setPath(path);
        row.setSize((long) bytes.length);
        row.setSha256(sha256(bytes));
        row.setCreateTime(new Date());
        try {
            rdmsRichRenderMapper.insert(row);
        } catch (DuplicateKeyException e) {
            RdmsRichRender again = find(versionId, f, options);
            if (again != null) {
                row = again;
            }
        }
        out.setRender(row);
        return out;
    }

    public byte[] bytes(Rendered r) {
        return objectStorageService.getObject(r.getTarget(), r.getRender().getPath());
    }

    /** 复制一份渲染结果为邮件附件文件（use=mail，上传人为本人），与渲染缓存互不影响。 */
    public RdmsFile saveAsMailFile(Rendered r, String memberId) {
        byte[] bytes = bytes(r);
        String key = UuidUtil.getShortUuid();
        String format = r.getRender().getFormat();
        String path = r.getModel().getCustomerId() + "/" + FileGroupingEnum.MAIL.getCode() + "/" + key + "-" + r.getFileName();
        objectStorageService.putObject(r.getTarget(), path, bytes, contentType(format));
        RdmsFile file = new RdmsFile();
        file.setCustomerId(r.getModel().getCustomerId());
        file.setUse(FileGroupingEnum.MAIL.getCode());
        file.setOperatorId(memberId);
        file.setKey(key);
        file.setPath(path);
        file.setName(r.getFileName());
        file.setSize(bytes.length);
        file.setSuffix(format);
        file.setSourceType(RdmsFileService.SOURCE_RICH_VERSION);
        file.setSourceId(r.getModel().getVersionId());
        file.setId(rdmsFileService.save(file));
        return file;
    }

    public String signedUrl(Rendered r) {
        return objectStorageService.presignAttachmentUrl(r.getTarget(), r.getRender().getPath(), r.getFileName(),
                java.time.Duration.ofSeconds(properties.getSignedUrlSeconds()));
    }

    public RdmsRichRender getRender(String renderId) {
        return rdmsRichRenderMapper.selectByPrimaryKey(renderId);
    }

    public byte[] bytesOf(RdmsRichRender render) {
        return objectStorageService.getObject(ossTargetResolver.docTarget(render.getCustomerId()), render.getPath());
    }

    public void log(RichRenderModelService.Model model, String action, String ip) {
        try {
            RdmsRichAccessLog log = new RdmsRichAccessLog();
            log.setId(UuidUtil.getShortUuid());
            log.setCustomerId(model.getCustomerId());
            log.setDocId(model.getDocId());
            log.setVersionId(model.getVersionId());
            log.setAction(action);
            String member = TenantContext.getMembershipId();
            log.setUserId(StringUtils.hasText(member) ? member : TenantContext.getUserId());
            log.setIp(ip == null ? null : ip.length() > 64 ? ip.substring(0, 64) : ip);
            log.setCreateTime(new Date());
            rdmsRichAccessLogMapper.insert(log);
        } catch (RuntimeException e) {
            LOG.warn("rich access log failed version={} action={}: {}", model.getVersionId(), action, e.getMessage());
        }
    }

    /** 清理超过保留天数的缓存文件：先删记录，再删对象；再次访问时重新生成。 */
    public int cleanupBefore(Date before, int limit) {
        RdmsRichRenderExample example = new RdmsRichRenderExample();
        example.createCriteria().andCreateTimeLessThan(before);
        example.setOrderByClause("create_time asc limit " + limit);
        List<RdmsRichRender> rows = rdmsRichRenderMapper.selectByExample(example);
        int removed = 0;
        for (RdmsRichRender row : rows) {
            if (rdmsRichRenderMapper.deleteByPrimaryKey(row.getId()) == 0) {
                continue;
            }
            removed++;
            try {
                objectStorageService.deleteObject(ossTargetResolver.docTarget(row.getCustomerId()), row.getPath());
            } catch (RuntimeException e) {
                LOG.warn("rich render object delete failed id={} key={}: {}", row.getId(), row.getPath(), e.getMessage());
            }
        }
        return removed;
    }

    private RdmsRichRender find(String versionId, String format, String options) {
        RdmsRichRenderExample example = new RdmsRichRenderExample();
        example.createCriteria().andVersionIdEqualTo(versionId).andFormatEqualTo(format)
                .andTemplateRevEqualTo(properties.getTemplateRev()).andOptionsEqualTo(options);
        List<RdmsRichRender> rows = rdmsRichRenderMapper.selectByExample(example);
        return rows.isEmpty() ? null : rows.get(0);
    }

    private byte[] loadAsset(String customerId, StorageTarget target, String assetId) {
        List<RdmsRichAsset> owned = rdmsRichAssetService.listOwned(customerId, Collections.singleton(assetId));
        if (owned.isEmpty()) {
            return null;
        }
        try {
            return objectStorageService.getObject(target, owned.get(0).getPath());
        } catch (RuntimeException e) {
            LOG.warn("rich render image load failed asset={}: {}", assetId, e.getMessage());
            return null;
        }
    }

    public static String contentType(String format) {
        return FORMAT_PDF.equals(format) ? "application/pdf"
                : "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    }

    private static String sha256(byte[] data) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-256").digest(data);
            StringBuilder sb = new StringBuilder();
            for (byte b : d) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return DigestUtils.md5DigestAsHex(data);
        }
    }
}
