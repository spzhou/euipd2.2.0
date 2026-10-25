/**
 * Author：周朔鹏
 * 编制时间：2026-10-20
 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */
package com.course.customer.controller;

import com.course.server.domain.RdmsWorkCalendar;
import com.course.server.dto.ResponseDto;
import com.course.server.dto.rdms.WorkCalendarSetDto;
import com.course.server.enums.rdms.WorkDayTypeEnum;
import com.course.server.exception.BusinessException;
import com.course.server.exception.BusinessExceptionCode;
import com.course.server.security.SecurityConstants;
import com.course.server.security.tenant.TenantContext;
import com.course.server.service.calendar.WorkCalendarService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 机构工作日历（假日设置）。机构取自令牌，只有本机构管理员或最终管理人可以查看和修改。
 */
@RestController
@RequestMapping("/work-calendar")
public class WorkCalendarController {
    public static final String BUSINESS_NAME = "工作日历";
    private static final String DEFAULT = "DEFAULT";

    @Resource
    private WorkCalendarService workCalendarService;

    @GetMapping("/list/{year}")
    public ResponseDto<Map<String, Object>> list(@PathVariable int year) {
        String customerId = assertAllowed();
        if (year < 2000 || year > 2100) {
            throw new BusinessException(BusinessExceptionCode.INPUT_ERROR, "年份超出范围");
        }
        List<Map<String, Object>> days = new ArrayList<>();
        for (RdmsWorkCalendar row : workCalendarService.listYear(customerId, year)) {
            Map<String, Object> day = new LinkedHashMap<>();
            day.put("date", row.getCalendarDate().toString());
            day.put("dayType", row.getDayType());
            day.put("note", row.getNote());
            day.put("updatedAt", row.getUpdatedAt());
            days.add(day);
        }
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("year", year);
        content.put("days", days);
        ResponseDto<Map<String, Object>> dto = new ResponseDto<>();
        dto.setContent(content);
        return dto;
    }

    @PostMapping("/update")
    public ResponseDto<Integer> update(@RequestBody WorkCalendarSetDto body) {
        String customerId = assertAllowed();
        LocalDate from = parse(body.getStartDate());
        LocalDate to = StringUtils.hasText(body.getEndDate()) ? parse(body.getEndDate()) : from;
        ResponseDto<Integer> dto = new ResponseDto<>();
        if (DEFAULT.equals(body.getDayType())) {
            int count = workCalendarService.restore(customerId, from, to);
            dto.setContent(count);
            dto.setMessage("已恢复默认（周一至周五上班，周六周日放假）");
            return dto;
        }
        WorkDayTypeEnum type = WorkDayTypeEnum.getByType(body.getDayType());
        int count = workCalendarService.set(customerId, from, to, type, body.getNote(), operatorId());
        dto.setContent(count);
        dto.setMessage("已将 " + count + " 天设为" + (type == null ? "" : type.getName()));
        return dto;
    }

    private static LocalDate parse(String value) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(BusinessExceptionCode.INPUT_ERROR, "请填写日期");
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException e) {
            throw new BusinessException(BusinessExceptionCode.INPUT_ERROR, "日期格式应为 yyyy-MM-dd");
        }
    }

    private String assertAllowed() {
        String customerId = TenantContext.getTenantId();
        if (!StringUtils.hasText(customerId)) {
            throw new BusinessException(BusinessExceptionCode.NO_PERMISSION, "缺少机构身份");
        }
        if (!hasRole("ADMIN") && !hasRole("UltiMng")) {
            throw new BusinessException(BusinessExceptionCode.NO_PERMISSION, "只有本机构管理员或最终管理人可以设置假日");
        }
        return customerId;
    }

    private static boolean hasRole(String code) {
        HttpServletRequest request = currentRequest();
        Object raw = request == null ? null : request.getAttribute(SecurityConstants.ATTR_ROLES);
        if (!(raw instanceof Collection)) {
            return false;
        }
        for (Object item : (Collection<?>) raw) {
            if (item != null && code.equalsIgnoreCase(String.valueOf(item).trim())) {
                return true;
            }
        }
        return false;
    }

    private static String operatorId() {
        String membershipId = TenantContext.getMembershipId();
        return StringUtils.hasText(membershipId) ? membershipId : TenantContext.getUserId();
    }

    private static HttpServletRequest currentRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs == null ? null : attrs.getRequest();
    }
}
