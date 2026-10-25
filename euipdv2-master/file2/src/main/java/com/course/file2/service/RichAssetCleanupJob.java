/**
 * Author：周朔鹏
 * 编制时间：2026-10-05
 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */
package com.course.file2.service;

import com.course.server.domain.RdmsRichAsset;
import com.course.server.service.rdms.RdmsRichAssetService;
import com.course.server.storage.ObjectStorageService;
import com.course.server.storage.OssTargetResolver;
import com.course.server.storage.StorageTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 清理上传后超过保留时长仍未被正文引用（TEMP）的图片：先删记录，再删对象。
 */
@Component
public class RichAssetCleanupJob {

    private static final Logger LOG = LoggerFactory.getLogger(RichAssetCleanupJob.class);
    private static final int BATCH = 200;

    @Value("${rdms.rich.temp-retention-hours:24}")
    private long retentionHours;

    @Resource
    private RdmsRichAssetService rdmsRichAssetService;
    @Resource
    private OssTargetResolver ossTargetResolver;
    @Resource
    private ObjectStorageService objectStorageService;

    @Scheduled(initialDelayString = "${rdms.rich.cleanup-initial-delay-ms:600000}",
            fixedDelayString = "${rdms.rich.cleanup-delay-ms:3600000}")
    public void cleanup() {
        Date before = new Date(System.currentTimeMillis() - retentionHours * 3600_000L);
        Map<String, StorageTarget> targets = new HashMap<>();
        int removed = 0;
        int removedInBatch;
        List<RdmsRichAsset> batch;
        do {
            removedInBatch = 0;
            batch = rdmsRichAssetService.listTempBefore(before, BATCH);
            for (RdmsRichAsset asset : batch) {
                if (!rdmsRichAssetService.deleteIfTemp(asset)) {
                    continue;
                }
                removed++;
                removedInBatch++;
                try {
                    StorageTarget target = targets.computeIfAbsent(asset.getCustomerId(), ossTargetResolver::docTarget);
                    objectStorageService.deleteObject(target, asset.getPath());
                } catch (RuntimeException e) {
                    LOG.warn("rich asset object delete failed id={} key={}: {}", asset.getId(), asset.getPath(), e.getMessage());
                }
            }
        } while (batch.size() == BATCH && removedInBatch > 0);
        if (removed > 0) {
            LOG.info("rich asset cleanup removed {} TEMP assets", removed);
        }
    }
}
