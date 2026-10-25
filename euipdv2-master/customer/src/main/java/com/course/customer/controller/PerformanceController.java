/**
 * Author：周朔鹏
 * 编制时间：2025-08-05

 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */

package com.course.customer.controller;

import com.course.server.dto.PageDto;
import com.course.server.security.tenant.TenantAccess;
import com.course.server.security.tenant.TenantContext;
import com.course.server.dto.ResponseDto;
import com.course.server.dto.rdms.PerformancePageDto;
import com.course.server.dto.rdms.RdmsPerformanceDto;
import com.course.server.dto.rdms.statistics.PerformanceDetailDto;
import com.course.server.dto.rdms.statistics.PerformanceMonthlyReportDto;
import com.course.server.service.rdms.RdmsPerformanceService;
import com.course.server.service.rdms.RdmsPerformanceViewService;
import com.course.server.util.CopyUtil;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/performance")
public class PerformanceController {
    public static final String BUSINESS_NAME = "绩效管理";

    @Resource
    private RdmsPerformanceService rdmsPerformanceService;
    @Resource
    private RdmsPerformanceViewService rdmsPerformanceViewService;

    @PostMapping("/list")
    public ResponseDto<PageDto<RdmsPerformanceDto>> list(@RequestBody PerformancePageDto pageDto) throws ParseException {
        pageDto.setCustomerId(TenantAccess.resolveCustomerId(pageDto.getCustomerId()));
        ResponseDto<PageDto<RdmsPerformanceDto>> responseDto = new ResponseDto<>();
        responseDto.setContent(rdmsPerformanceViewService.listCurrentPage(pageDto, TenantContext.getMembershipId(),
                pageDto.getDepartmentId()));
        return responseDto;
    }

    @PostMapping("/getCurrentPerformanceByLoginUser/{customerUserId}")
    public ResponseDto<RdmsPerformanceDto> getCurrentPerformanceByLoginUser(@PathVariable String customerUserId) throws ParseException {
        customerUserId = TenantAccess.resolveMemberId(customerUserId);
        ResponseDto<RdmsPerformanceDto> responseDto = new ResponseDto<>();
        responseDto.setContent(rdmsPerformanceViewService.currentOf(customerUserId));
        return responseDto;
    }

    /** 当月绩效计入工单明细：本人或范围内人员 */
    @PostMapping("/getCurrentDetail/{customerUserId}")
    public ResponseDto<PerformanceDetailDto> getCurrentDetail(@PathVariable String customerUserId) {
        ResponseDto<PerformanceDetailDto> responseDto = new ResponseDto<>();
        responseDto.setContent(rdmsPerformanceViewService.currentDetail(TenantContext.getMembershipId(), customerUserId));
        return responseDto;
    }

    @PostMapping("/listPerformance")
    public ResponseDto<List<RdmsPerformanceDto>> listPerformance(@RequestParam(required = false) String customerUserId, String customerId) throws ParseException {
        customerUserId = TenantAccess.resolveMemberId(customerUserId);
        customerId = TenantAccess.resolveCustomerId(customerId);
        ResponseDto<List<RdmsPerformanceDto>> responseDto = new ResponseDto<>();
        responseDto.setContent(rdmsPerformanceViewService.listVisibleCurrent(customerUserId, customerId));
        return responseDto;
    }

    /** 绩效月报：body.yearMonth 为 yyyy-MM；返回明细、部门小计（含吸收率）与总计 */
    @PostMapping("/getMonthlyReport")
    public ResponseDto<PerformanceMonthlyReportDto> getMonthlyReport(@RequestBody(required = false) Map<String, String> body) {
        String customerId = TenantAccess.resolveCustomerId(null);
        ResponseDto<PerformanceMonthlyReportDto> responseDto = new ResponseDto<>();
        responseDto.setContent(rdmsPerformanceViewService.monthlyReport(body == null ? null : body.get("yearMonth"), customerId,
                TenantContext.getMembershipId()));
        return responseDto;
    }

    @PostMapping("/createPerformanceRecord/{customerUserId}")
    public ResponseDto<RdmsPerformanceDto> createPerformanceRecord(@PathVariable String customerUserId) throws ParseException {
        customerUserId = TenantAccess.resolveMemberId(customerUserId);
        ResponseDto<RdmsPerformanceDto> responseDto = new ResponseDto<>();
        String dateFormatStr = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        responseDto.setContent(CopyUtil.copy(
                rdmsPerformanceService.previewWorkerLastMonthPerformanceItem(customerUserId, dateFormatStr),
                RdmsPerformanceDto.class));
        return responseDto;
    }

    @PostMapping("/getPerformanceListByUserId/{customerUserId}")
    public ResponseDto<List<RdmsPerformanceDto>> getPerformanceListByCustomerUserId(@PathVariable String customerUserId) {
        rdmsPerformanceViewService.assertVisible(TenantContext.getMembershipId(), customerUserId);
        ResponseDto<List<RdmsPerformanceDto>> responseDto = new ResponseDto<>();
        responseDto.setContent(rdmsPerformanceService.getPerformanceListByCustomerUserId(customerUserId));
        return responseDto;
    }
}
