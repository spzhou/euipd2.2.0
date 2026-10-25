/**
 * Author：周朔鹏
 * 编制时间：2026-10-13
 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */
package com.course.file2.service;

import com.course.server.domain.RdmsFile;
import com.course.server.mapper.RdmsFilePurgeMapper;
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
 * 软删附件对象清理（VM-D6）：每日删除软删满保留期且无引用的附件对象，记录保留，标注清理时间。
 * 缺省关闭；开启后缺省试运行，只记待删对象，不删对象也不标注。
 */
@Component
public class FilePurgeJob {

    private static final Logger LOG = LoggerFactory.getLogger(FilePurgeJob.class);
    static final int BATCH = 200;
    static final int MAX_PER_RUN = 5000;

    @Value("${rdms.file.purge.enabled:false}")
    boolean enabled;
    @Value("${rdms.file.purge.retention-days:180}")
    int retentionDays = 180;
    @Value("${rdms.file.purge.dry-run:true}")
    boolean dryRun = true;

    @Resource
    RdmsFilePurgeMapper rdmsFilePurgeMapper;
    @Resource
    OssTargetResolver ossTargetResolver;
    @Resource
    ObjectStorageService objectStorageService;

    @Scheduled(cron = "${rdms.file.purge.cron:0 30 3 * * ?}")
    public void scheduled() {
        if (!enabled) {
            return;
        }
        purge(new Date());
    }

    /** 返回本次删除（试运行时为列出）的对象数。 */
    public int purge(Date now) {
        Date before = new Date(now.getTime() - Math.max(retentionDays, 1) * 86_400_000L);
        if (dryRun) {
            List<RdmsFile> candidates = rdmsFilePurgeMapper.selectPurgeCandidates(before, MAX_PER_RUN);
            for (RdmsFile file : candidates) {
                LOG.info("file purge dry-run id={} customer={} key={} deletedAt={}",
                        file.getId(), file.getCustomerId(), file.getPath(), file.getUpdateTime());
            }
            LOG.info("file purge dry-run listed {} objects, retentionDays={}", candidates.size(), retentionDays);
            return candidates.size();
        }
        Map<String, StorageTarget> targets = new HashMap<>();
        int purged = 0;
        int failed = 0;
        List<RdmsFile> batch;
        int purgedInBatch;
        do {
            purgedInBatch = 0;
            batch = rdmsFilePurgeMapper.selectPurgeCandidates(before, BATCH);
            for (RdmsFile file : batch) {
                try {
                    StorageTarget target = targets.computeIfAbsent(file.getCustomerId(), ossTargetResolver::docTarget);
                    objectStorageService.deleteObject(target, file.getPath());
                } catch (RuntimeException e) {
                    failed++;
                    LOG.warn("file purge object delete failed id={} key={}: {}", file.getId(), file.getPath(), e.getMessage());
                    continue;
                }
                if (rdmsFilePurgeMapper.markPurged(file.getId(), now) > 0) {
                    purged++;
                    purgedInBatch++;
                    LOG.info("file purge removed id={} customer={} key={}", file.getId(), file.getCustomerId(), file.getPath());
                }
            }
        } while (batch.size() == BATCH && purgedInBatch > 0 && purged < MAX_PER_RUN);
        LOG.info("file purge removed {} objects, failed {}, retentionDays={}", purged, failed, retentionDays);
        return purged;
    }
}
