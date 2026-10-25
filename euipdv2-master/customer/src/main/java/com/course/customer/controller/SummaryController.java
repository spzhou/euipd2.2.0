/**
 * Author：周朔鹏
 * 编制时间：2025-08-05

 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */

package com.course.customer.controller;

import com.alibaba.fastjson.JSON;
import com.course.server.dto.ResponseDto;
import com.course.server.dto.rdms.*;
import com.course.server.security.SecurityConstants;
import com.course.server.security.tenant.TenantAccess;
import com.course.server.service.rdms.*;
import com.course.server.util.RedisLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/summary")
public class SummaryController {
    private static final Logger LOG = LoggerFactory.getLogger(SummaryController.class);
    public static final String BUSINESS_NAME = "汇总数据";
    private static final long SUMMARY_LOCK_SECONDS = 300;

    @Autowired
    private RdmsPerformanceService rdmsPerformanceService;
    @Resource
    private RedisTemplate redisTemplate;

    /**
     * 机构大屏汇总，机构取自登录租户。
     * 当日缓存未命中时持锁生成一次；未获得锁说明他人正在生成，本次返回空内容。
     */
    @PostMapping("/getCustomerSummaryInfo")
    public ResponseDto<CustomerSummaryDto> getCustomerSummaryInfo() throws ParseException {
        String customerId = TenantAccess.resolveCustomerId(null);
        ResponseDto<CustomerSummaryDto> responseDto = new ResponseDto<>();
        if (ObjectUtils.isEmpty(customerId)) {
            return responseDto;
        }
        String customerDate = customerId + "-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        CustomerSummaryDto cached = readSummary(customerDate);
        if (cached == null) {
            String lockKey = SecurityConstants.REDIS_SUMMARY_LOCK_PREFIX + customerId;
            String token = RedisLock.tryLock(redisTemplate, lockKey, SUMMARY_LOCK_SECONDS);
            if (token != null) {
                try {
                    rdmsPerformanceService.getCustomerSummaryData_toRedis(customerId, customerDate);
                } finally {
                    RedisLock.unlock(redisTemplate, lockKey, token);
                }
                cached = readSummary(customerDate);
            }
        }
        responseDto.setContent(cached);
        return responseDto;
    }

    private CustomerSummaryDto readSummary(String customerDate) {
        Object raw = redisTemplate.opsForValue().get(RdmsPerformanceService.summaryKey(customerDate));
        if (!(raw instanceof String)) {
            return null;
        }
        CustomerSummaryDto dto = JSON.parseObject((String) raw, CustomerSummaryDto.class);
        return ObjectUtils.isEmpty(dto) ? null : dto;
    }


}
