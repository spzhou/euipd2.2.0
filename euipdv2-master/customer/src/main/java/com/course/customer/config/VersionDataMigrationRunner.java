/**
 * Author：周朔鹏
 * 编制时间：2026-10-13
 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */
package com.course.customer.config;

import com.alibaba.fastjson.JSON;
import com.course.server.service.rdms.VersionDataMigrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 版本存量迁移（《版本管理统一修订计划》V2-20）的一次性入口：rdms.version.migration.enabled=true 时在启动后执行一次。
 * 缺省只试运行（dry-run），核对报告后把 rdms.version.migration.dry-run 设为 false 再执行；执行完毕应当关闭开关。
 * rdms.version.migration.exclude-project-ids 所列项目及其预立项、以其为来源的产品不迁移。
 */
@Component
@ConditionalOnProperty(name = "rdms.version.migration.enabled", havingValue = "true")
public class VersionDataMigrationRunner implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(VersionDataMigrationRunner.class);

    @Resource
    private VersionDataMigrationService versionDataMigrationService;

    @Value("${rdms.version.migration.dry-run:true}")
    private boolean dryRun;

    @Value("${rdms.version.migration.customer-ids:}")
    private String customerIds;

    @Value("${rdms.version.migration.exclude-project-ids:}")
    private String excludeProjectIds;

    @Override
    public void run(ApplicationArguments args) {
        VersionDataMigrationService.Report report = versionDataMigrationService.migrate(
                split(customerIds), split(excludeProjectIds), dryRun);
        LOG.info("version data migration report: {}", JSON.toJSONString(report));
    }

    private static List<String> split(String csv) {
        return Arrays.stream(csv.split(",")).map(String::trim).filter(StringUtils::hasText).collect(Collectors.toList());
    }
}
