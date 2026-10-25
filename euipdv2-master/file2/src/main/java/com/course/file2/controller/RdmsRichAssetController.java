/**
 * Author：周朔鹏
 * 编制时间：2026-10-05
 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */
package com.course.file2.controller;

import com.course.server.storage.OssTargetResolver;
import com.course.server.domain.RdmsRichAsset;
import com.course.server.dto.ResponseDto;
import com.course.server.exception.BusinessException;
import com.course.server.exception.BusinessExceptionCode;
import com.course.server.security.tenant.TenantContext;
import com.course.server.service.rdms.RdmsRichAssetService;
import com.course.server.storage.ObjectStorageService;
import com.course.server.storage.StorageTarget;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 富文本正文图片：上传与批量换取签名地址。机构只取验签后的上下文。
 */
@RestController
@RequestMapping("/rdms/rich-asset")
public class RdmsRichAssetController {

    public static final String BUSINESS_NAME = "富文本图片";
    private static final int MAX_BATCH = 200;

    @Resource
    private RdmsRichAssetService rdmsRichAssetService;
    @Resource
    private OssTargetResolver ossTargetResolver;
    @Resource
    private ObjectStorageService objectStorageService;

    @PostMapping("/upload")
    public ResponseDto<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) throws IOException {
        String customerId = requireTenant();
        String uploaderId = StringUtils.hasText(TenantContext.getMembershipId())
                ? TenantContext.getMembershipId() : TenantContext.getUserId();
        if (file == null || file.isEmpty()) {
            throw new BusinessException(BusinessExceptionCode.RICH_ASSET_TYPE_INVALID);
        }
        if (file.getSize() > RdmsRichAssetService.MAX_BYTES) {
            throw new BusinessException(BusinessExceptionCode.RICH_ASSET_TOO_LARGE);
        }
        StorageTarget target = ossTargetResolver.docTarget(customerId);
        RdmsRichAsset asset = rdmsRichAssetService.upload(target, customerId, uploaderId, file.getBytes());

        Map<String, Object> content = new LinkedHashMap<>();
        content.put("assetId", asset.getId());
        content.put("mime", asset.getMime());
        content.put("width", asset.getWidth());
        content.put("height", asset.getHeight());
        content.put("url", objectStorageService.presignGetUrl(target, asset.getPath()));
        content.put("expiresIn", objectStorageService.getSignedUrlTtl().getSeconds());
        ResponseDto<Map<String, Object>> responseDto = new ResponseDto<>();
        responseDto.setContent(content);
        return responseDto;
    }

    /**
     * 请求体 {"assetIds": [...]}；逐个按当前机构校验，只返回本机构存在的图片，
     * 其他机构的或已清理的不出现在结果中，也不影响同批其余图片。
     */
    @PostMapping("/signed-url")
    public ResponseDto<Map<String, Object>> signedUrl(@RequestBody Map<String, List<String>> body) {
        String customerId = requireTenant();
        List<String> requested = body == null ? null : body.get("assetIds");
        Set<String> ids = new LinkedHashSet<>();
        if (!CollectionUtils.isEmpty(requested)) {
            for (String id : requested) {
                if (StringUtils.hasText(id)) {
                    ids.add(id);
                }
            }
        }
        if (ids.size() > MAX_BATCH) {
            throw new BusinessException(BusinessExceptionCode.INPUT_ERROR);
        }
        Map<String, String> urls = new LinkedHashMap<>();
        if (!ids.isEmpty()) {
            List<RdmsRichAsset> owned = rdmsRichAssetService.listOwned(customerId, ids);
            if (!owned.isEmpty()) {
                StorageTarget target = ossTargetResolver.docTarget(customerId);
                Map<String, String> byKey = objectStorageService.presignGetUrls(target,
                        owned.stream().map(RdmsRichAsset::getPath).collect(Collectors.toList()));
                for (RdmsRichAsset asset : owned) {
                    urls.put(asset.getId(), byKey.get(asset.getPath()));
                }
            }
        }
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("urls", urls);
        content.put("expiresIn", objectStorageService.getSignedUrlTtl().getSeconds());
        ResponseDto<Map<String, Object>> responseDto = new ResponseDto<>();
        responseDto.setContent(content);
        return responseDto;
    }

    private static String requireTenant() {
        String customerId = TenantContext.getTenantId();
        if (!StringUtils.hasText(customerId)) {
            throw new BusinessException(BusinessExceptionCode.NO_PERMISSION);
        }
        return customerId;
    }
}
