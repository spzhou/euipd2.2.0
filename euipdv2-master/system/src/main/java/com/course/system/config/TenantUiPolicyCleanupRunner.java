/**
 * Author：周朔鹏
 * 编制时间：2026-10-13
 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */
package com.course.system.config;

import com.course.server.security.permission.TenantUiPolicyCleaner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 启动时清理机构覆盖中平台已删除的资源号。失败只记日志，不影响启动。
 */
@Component
public class TenantUiPolicyCleanupRunner implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(TenantUiPolicyCleanupRunner.class);

    @Resource
    private TenantUiPolicyCleaner tenantUiPolicyCleaner;

    @Override
    public void run(ApplicationArguments args) {
        try {
            int changed = tenantUiPolicyCleaner.cleanAll();
            LOG.info("Tenant UI policy cleanup finished, changed={}", changed);
        } catch (RuntimeException e) {
            LOG.warn("Tenant UI policy cleanup failed: {}", e.getMessage());
        }
    }
}
