/**
 * Author：周朔鹏
 * 编制时间：2025-08-05

 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */

package com.course.customer;

import com.course.server.config.RdmsMilestoneProperties;
import com.course.server.domain.RdmsCustomer;
import com.course.server.dto.rdms.GanttIssueResultDto;
import com.course.server.security.SecurityConstants;
import com.course.server.security.tenant.TenantScope;
import com.course.server.util.RedisLock;
import com.course.server.service.rdms.RdmsCustomerService;
import com.course.server.service.rdms.RdmsJobItemGanttService;
import com.course.server.service.rdms.RdmsMilestoneService;
import com.course.server.service.rdms.RdmsPerformanceService;
import com.course.server.service.rdms.mail.MailCleanupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component("timerTaskJob")
public class TimerTaskJob {
    private static final Logger LOG = LoggerFactory.getLogger(TimerTaskJob.class);
    private static final long MONTH_ACCOUNT_LOCK_SECONDS = 7200;
    private static final long SUMMARY_LOCK_SECONDS = 300;
    private static final String MAIL_CLEANUP_LOCK = "euipd:lock:mail-cleanup";
    private static final long MAIL_CLEANUP_LOCK_SECONDS = 1800;
    public static final String BUSINESS_NAME = "定时任务";

    @Resource
    private RdmsPerformanceService rdmsPerformanceService;
    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private RdmsCustomerService rdmsCustomerService;

    @Value("${rdms.timer.month-account.enabled:true}")
    private boolean monthAccountEnabled;

    @Value("${rdms.timer.plan-task-issue.enabled:true}")
    private boolean planTaskIssueEnabled;

    @Autowired
    private RdmsJobItemGanttService rdmsJobItemGanttService;
    @Resource
    private RdmsMilestoneService rdmsMilestoneService;
    @Resource
    private RdmsMilestoneProperties rdmsMilestoneProperties;
    @Resource
    private MailCleanupService mailCleanupService;

    @Value("${rdms.timer.mail-cleanup.enabled:true}")
    private boolean mailCleanupEnabled;

    /**
     * 如果是在Spring Boot项目中，需要在启动类上添加@EnableScheduling来开启定时任务
     * fixedRate有一个时刻表的概念，在任务启动时，T1、T2、T3就已经排好了执行的时刻，比如1分、2分、3分，当T1的执行时间大于1分钟时，就会造成T2晚点，当T1执行完时T2立即执行
     */
    /**
     * 定时任务3：月度账户统计
     * 每2小时执行一次，计算并更新月度账户统计信息
     * 
     * @throws ParseException 解析异常
     */
    @Scheduled(fixedDelay = 2 * 60 * 60 * 1000L)
    public void job3() throws ParseException {
        if (!monthAccountEnabled) {
            return;
        }
        String token = RedisLock.tryLock(redisTemplate, SecurityConstants.REDIS_MONTH_ACCOUNT_LOCK, MONTH_ACCOUNT_LOCK_SECONDS);
        if (token == null) {
            LOG.info("job3月度结算由其他实例执行，本次跳过");
            return;
        }
        try {
            rdmsPerformanceService.monthAccount();
        } finally {
            RedisLock.unlock(redisTemplate, SecurityConstants.REDIS_MONTH_ACCOUNT_LOCK, token);
        }
    }

    /**
     * 定时任务5：每日汇总数据更新
     * 每天零点零一分执行，计算大屏幕展示的汇总信息并缓存到Redis
     * 
     * @throws ParseException 解析异常
     */
    @Scheduled(cron = "0 1 0 * * ?")
    public void job5() throws ParseException {
        LOG.info("job5定时任务被执行 - 每天零点零一分");
        //计算大屏幕展示的汇总信息
        // 获取当前日期
        LocalDate currentDate = LocalDate.now();
        // 定义日期格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        // 将日期格式化为字符串
        List<RdmsCustomer> customers = rdmsCustomerService.getAllCustomerList();
        if (CollectionUtils.isEmpty(customers)) {
            return;
        }
        for (RdmsCustomer customer : customers) {
            if (customer == null || ObjectUtils.isEmpty(customer.getId())) {
                continue;
            }
            String customerId = customer.getId();
            TenantScope.run(customerId, () -> refreshSummary(customerId, customerId + "-" + currentDate.format(formatter)));
        }
    }

    private void refreshSummary(String customerId, String customerDate) {
        String lockKey = SecurityConstants.REDIS_SUMMARY_LOCK_PREFIX + customerId;
        String lockToken = null;
        try {
            Object radisData = redisTemplate.opsForValue().get(RdmsPerformanceService.summaryKey(customerDate));
            if (ObjectUtils.isEmpty(radisData)) {
                lockToken = RedisLock.tryLock(redisTemplate, lockKey, SUMMARY_LOCK_SECONDS);
                if (lockToken != null) {
                    rdmsPerformanceService.getCustomerSummaryData_toRedis(customerId, customerDate);
                }
            }
        } catch (Exception e) {
            LOG.error("汇总数据生成失败, customerId={}", customerId, e);
        } finally {
            RedisLock.unlock(redisTemplate, lockKey, lockToken);
        }
    }

    /**
     * 定时任务6：计划任务下发
     * 每天8:30执行，检查计划任务中是否有今天下发的任务，如果有则下发这些计划任务
     * 
     * @throws ParseException 解析异常
     */
    @Scheduled(cron = "0 30 8 * * ?")
    public void job6() throws ParseException {
        if (!planTaskIssueEnabled) {
            return;
        }
        LOG.info("job6定时任务被执行 - 每天8:30，多租户计划任务下发");
        // 对全部正常客户检查并下发到期计划任务
        GanttIssueResultDto result = rdmsJobItemGanttService.issueAllPlanTasks();
        LOG.info("job6计划任务下发完成：成功 {} 条，失败 {} 条，取消 {} 条",
                result.getIssued(), result.getFailed(), result.getCancelled());
    }

    /**
     * 里程碑周期任务（默认关闭；rdms.milestone.cycle-task.enabled=true 时启用）。
     */
    @Scheduled(cron = "0 15 7 * * ?")
    public void milestoneCycleTask() {
        if (rdmsMilestoneProperties == null || !rdmsMilestoneProperties.isCycleTaskEnabled()) {
            return;
        }
        LOG.info("milestoneCycleTask 启用，开始调度");
        List<RdmsCustomer> customers = rdmsCustomerService.getAllCustomerList();
        if (CollectionUtils.isEmpty(customers)) {
            return;
        }
        for (RdmsCustomer customer : customers) {
            if (customer == null || ObjectUtils.isEmpty(customer.getId())) {
                continue;
            }
            String customerId = customer.getId();
            TenantScope.run(customerId, () -> runMilestoneCycleTask(customerId));
        }
    }

    private void runMilestoneCycleTask(String customerId) {
        try {
            rdmsMilestoneService.milestoneCycleTask_jobTask(customerId);
            rdmsMilestoneService.milestoneCycleTask_reviewTask(customerId);
        } catch (Exception e) {
            LOG.error("milestoneCycleTask 执行失败, customerId={}", customerId, e);
        }
    }

    /**
     * 内部邮件清理：已删除文件夹到期条目、无人持有的邮件、写信时上传但未发出的文件。每天 3:40 执行。
     */
    @Scheduled(cron = "0 40 3 * * ?")
    public void mailCleanup() {
        if (!mailCleanupEnabled) {
            return;
        }
        String token = RedisLock.tryLock(redisTemplate, MAIL_CLEANUP_LOCK, MAIL_CLEANUP_LOCK_SECONDS);
        if (token == null) {
            LOG.info("邮件清理由其他实例执行，本次跳过");
            return;
        }
        try {
            mailCleanupService.run();
        } catch (Exception e) {
            LOG.error("邮件清理失败", e);
        } finally {
            RedisLock.unlock(redisTemplate, MAIL_CLEANUP_LOCK, token);
        }
    }



}

