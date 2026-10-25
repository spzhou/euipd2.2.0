/**
 * Author：周朔鹏
 * 编制时间：2026-10-08
 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */
package com.course.customer.config;

import com.alibaba.fastjson.JSON;
import com.course.server.service.rdms.rich.RichDataMigrationService;
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
 * 富文本生产数据迁移（实现方案第 14 节）的一次性入口：rdms.rich.migration.enabled=true 时在启动后执行一次。
 * 缺省只演练（dry-run），核对报告后把 rdms.rich.migration.dry-run 设为 false 再执行；执行完毕应当关闭开关。
 */
@Component
@ConditionalOnProperty(name = "rdms.rich.migration.enabled", havingValue = "true")
public class RichDataMigrationRunner implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(RichDataMigrationRunner.class);

    @Resource
    private RichDataMigrationService richDataMigrationService;

    @Value("${rdms.rich.migration.dry-run:true}")
    private boolean dryRun;

    @Value("${rdms.rich.migration.customer-ids:}")
    private String customerIds;

    @Override
    public void run(ApplicationArguments args) {
        List<String> customers = Arrays.stream(customerIds.split(","))
                .map(String::trim).filter(StringUtils::hasText).collect(Collectors.toList());
        RichDataMigrationService.Report report = richDataMigrationService.migrate(customers, dryRun);
        LOG.info("rich data migration report: {}", JSON.toJSONString(report));
    }
}
