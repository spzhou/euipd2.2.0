/**
 * Author：周朔鹏
 * 编制时间：2026-10-06
 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */
package com.course.file2.rich;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

/**
 * 清理生成超过保留天数（默认 30 天）的正式文件缓存，再次访问时重新生成。
 */
@Component
public class RichRenderCleanupJob {

    private static final Logger LOG = LoggerFactory.getLogger(RichRenderCleanupJob.class);
    private static final int BATCH = 200;

    @Resource
    private RichRenderService richRenderService;
    @Resource
    private RichRenderProperties properties;

    @Scheduled(initialDelayString = "${rdms.rich.render.cleanup-initial-delay-ms:900000}",
            fixedDelayString = "${rdms.rich.render.cleanup-delay-ms:86400000}")
    public void cleanup() {
        Date before = new Date(System.currentTimeMillis() - properties.getRetentionDays() * 86_400_000L);
        int total = 0;
        int removed;
        do {
            removed = richRenderService.cleanupBefore(before, BATCH);
            total += removed;
        } while (removed == BATCH);
        if (total > 0) {
            LOG.info("rich render cleanup removed {} cached files", total);
        }
    }
}
