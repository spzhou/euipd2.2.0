/**
 * Author：周朔鹏
 * 编制时间：2026-08-06
 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */
package com.course.customer.controller;

import com.course.server.dto.ResponseDto;
import com.course.server.security.tenant.TenantAccess;
import com.course.server.dto.rdms.RdmsBadgeSummaryDto;
import com.course.server.service.rdms.RdmsNotifyBadgeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 通知 / 侧栏角标聚合。
 */
@RestController
@RequestMapping("/notify")
public class NotifyController {

    public static final String BUSINESS_NAME = "通知角标";

    @Resource
    private RdmsNotifyBadgeService rdmsNotifyBadgeService;

    /**
     * 侧栏角标一次聚合：字段名对齐 admin-v2 taskCounts / taskFlags。
     */
    @GetMapping("/badge-summary/{userId}")
    public ResponseDto<RdmsBadgeSummaryDto> badgeSummary(@PathVariable String userId) {
        userId = TenantAccess.resolveMemberId(userId);
        ResponseDto<RdmsBadgeSummaryDto> responseDto = new ResponseDto<>();
        responseDto.setContent(rdmsNotifyBadgeService.getBadgeSummary(userId));
        return responseDto;
    }
}
