/**
 * Author：周朔鹏
 * 编制时间：2026-10-12
 * 电子邮件：391902958@qq.com
 * 项目官网：https://www.euipd.com
 */
package com.course.customer.controller;

import com.course.server.dto.PageDto;
import com.course.server.dto.ResponseDto;
import com.course.server.dto.rdms.statistics.DashboardOverviewDto;
import com.course.server.dto.rdms.statistics.DepartmentWorkloadNodeDto;
import com.course.server.dto.rdms.statistics.PersonWorkloadDto;
import com.course.server.dto.rdms.statistics.ProjectStatCardDto;
import com.course.server.dto.rdms.statistics.ProjectStatDetailDto;
import com.course.server.dto.rdms.statistics.ProjectTrendPointDto;
import com.course.server.dto.rdms.statistics.StatQueryDto;
import com.course.server.dto.rdms.statistics.StatScopeDto;
import com.course.server.dto.rdms.statistics.StatScopeViewDto;
import com.course.server.dto.rdms.statistics.WorkbenchSelfDto;
import com.course.server.dto.rdms.statistics.WorkloadRowDto;
import com.course.server.exception.BusinessException;
import com.course.server.exception.BusinessExceptionCode;
import com.course.server.service.rdms.statistics.DashboardStatisticsService;
import com.course.server.service.rdms.statistics.ProjectStatisticsService;
import com.course.server.service.rdms.statistics.StatMonthSnapshotService;
import com.course.server.service.rdms.statistics.StatPeriod;
import com.course.server.service.rdms.statistics.StatisticsCache;
import com.course.server.service.rdms.statistics.StatisticsScopeService;
import com.course.server.service.rdms.statistics.WorkloadStatisticsService;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 统计接口（《统计功能实现方案》第 3.10 节）。机构与人员一律取自令牌，数据边界由范围服务决定。
 */
@RestController
@RequestMapping("/statistics")
public class StatisticsController {
    public static final String BUSINESS_NAME = "统计";

    @Resource
    private StatisticsScopeService statisticsScopeService;
    @Resource
    private WorkloadStatisticsService workloadStatisticsService;
    @Resource
    private StatMonthSnapshotService statMonthSnapshotService;
    @Resource
    private StatisticsCache statisticsCache;
    @Resource
    private ProjectStatisticsService projectStatisticsService;
    @Resource
    private DashboardStatisticsService dashboardStatisticsService;

    @PostMapping("/scope/get")
    public ResponseDto<StatScopeViewDto> getScope() {
        ResponseDto<StatScopeViewDto> responseDto = new ResponseDto<>();
        responseDto.setContent(statisticsScopeService.describe(statisticsScopeService.resolve()));
        return responseDto;
    }

    @PostMapping("/workload/getDepartmentTree")
    public ResponseDto<DepartmentWorkloadNodeDto> getDepartmentTree(@RequestBody(required = false) StatQueryDto query) {
        StatQueryDto q = query == null ? new StatQueryDto() : query;
        StatScopeDto scope = statisticsScopeService.resolve();
        StatPeriod period = StatPeriod.of(q.getPeriodType(), q.getPeriodValue());
        statMonthSnapshotService.ensureOnce(scope.getCustomerId());
        ResponseDto<DepartmentWorkloadNodeDto> responseDto = new ResponseDto<>();
        responseDto.setContent(statisticsCache.get(scope.getCustomerId(), scope.getMemberId(), "workload-tree",
                Arrays.asList(period.key(), q.getProjectId()),
                new TypeReference<DepartmentWorkloadNodeDto>() { },
                () -> workloadStatisticsService.getDepartmentTree(scope, period, q.getProjectId())));
        return responseDto;
    }

    @PostMapping("/workload/listDepartmentMembers/{departmentId}")
    public ResponseDto<List<WorkloadRowDto>> listDepartmentMembers(@PathVariable String departmentId,
                                                                   @RequestBody(required = false) StatQueryDto query) {
        StatQueryDto q = query == null ? new StatQueryDto() : query;
        StatScopeDto scope = statisticsScopeService.resolve();
        StatPeriod period = StatPeriod.of(q.getPeriodType(), q.getPeriodValue());
        boolean includeDescendants = !Boolean.FALSE.equals(q.getIncludeDescendants());
        ResponseDto<List<WorkloadRowDto>> responseDto = new ResponseDto<>();
        responseDto.setContent(statisticsCache.get(scope.getCustomerId(), scope.getMemberId(), "workload-members",
                Arrays.asList(departmentId, includeDescendants, period.key(), q.getProjectId()),
                new TypeReference<List<WorkloadRowDto>>() { },
                () -> workloadStatisticsService.listDepartmentMembers(scope, departmentId, includeDescendants, period, q.getProjectId())));
        return responseDto;
    }

    @PostMapping("/workload/getPerson/{customerUserId}")
    public ResponseDto<PersonWorkloadDto> getPerson(@PathVariable String customerUserId,
                                                    @RequestBody(required = false) StatQueryDto query) {
        StatQueryDto q = query == null ? new StatQueryDto() : query;
        StatScopeDto scope = statisticsScopeService.resolve();
        statisticsScopeService.assertUserVisible(scope, customerUserId);
        StatPeriod period = StatPeriod.of(q.getPeriodType(), q.getPeriodValue());
        ResponseDto<PersonWorkloadDto> responseDto = new ResponseDto<>();
        responseDto.setContent(statisticsCache.get(scope.getCustomerId(), scope.getMemberId(), "workload-person",
                Arrays.asList(customerUserId, period.key()),
                new TypeReference<PersonWorkloadDto>() { },
                () -> workloadStatisticsService.getPerson(scope, customerUserId, period)));
        return responseDto;
    }

    @PostMapping("/project/list")
    public ResponseDto<PageDto<ProjectStatCardDto>> listProjects(@RequestBody(required = false) StatQueryDto query) {
        StatQueryDto q = query == null ? new StatQueryDto() : query;
        StatScopeDto scope = statisticsScopeService.resolve();
        ResponseDto<PageDto<ProjectStatCardDto>> responseDto = new ResponseDto<>();
        responseDto.setContent(statisticsCache.get(scope.getCustomerId(), scope.getMemberId(), "project-list",
                Arrays.asList(q.getStatus(), q.getHealth(), q.getManagerId(), q.getReleaseFrom(), q.getReleaseTo(), q.getKeyword(), q.getPage(), q.getSize()),
                new TypeReference<PageDto<ProjectStatCardDto>>() { },
                () -> projectStatisticsService.listCards(scope, q)));
        return responseDto;
    }

    @PostMapping("/project/listByIds")
    public ResponseDto<List<ProjectStatCardDto>> listProjectsByIds(@RequestBody(required = false) StatQueryDto query) {
        StatQueryDto q = query == null ? new StatQueryDto() : query;
        StatScopeDto scope = statisticsScopeService.resolve();
        ResponseDto<List<ProjectStatCardDto>> responseDto = new ResponseDto<>();
        responseDto.setContent(statisticsCache.get(scope.getCustomerId(), scope.getMemberId(), "project-by-ids",
                q.getProjectIds(),
                new TypeReference<List<ProjectStatCardDto>>() { },
                () -> projectStatisticsService.listCardsByIds(scope, q.getProjectIds())));
        return responseDto;
    }

    @PostMapping("/project/getDetail/{projectId}")
    public ResponseDto<ProjectStatDetailDto> getProjectDetail(@PathVariable String projectId,
                                                              @RequestBody(required = false) StatQueryDto query) {
        StatQueryDto q = query == null ? new StatQueryDto() : query;
        StatScopeDto scope = statisticsScopeService.resolve();
        statisticsScopeService.assertProjectVisible(scope, projectId);
        StatPeriod period = StatPeriod.of(q.getPeriodType(), q.getPeriodValue());
        ResponseDto<ProjectStatDetailDto> responseDto = new ResponseDto<>();
        responseDto.setContent(statisticsCache.get(scope.getCustomerId(), scope.getMemberId(), "project-detail",
                Arrays.asList(projectId, period.key()),
                new TypeReference<ProjectStatDetailDto>() { },
                () -> projectStatisticsService.getDetail(scope, projectId, period)));
        return responseDto;
    }

    @PostMapping("/project/getTrend/{projectId}")
    public ResponseDto<List<ProjectTrendPointDto>> getProjectTrend(@PathVariable String projectId,
                                                                   @RequestBody(required = false) StatQueryDto query) {
        StatQueryDto q = query == null ? new StatQueryDto() : query;
        StatScopeDto scope = statisticsScopeService.resolve();
        statisticsScopeService.assertProjectVisible(scope, projectId);
        statMonthSnapshotService.ensureOnce(scope.getCustomerId());
        ResponseDto<List<ProjectTrendPointDto>> responseDto = new ResponseDto<>();
        responseDto.setContent(statisticsCache.get(scope.getCustomerId(), scope.getMemberId(), "project-trend",
                Arrays.asList(projectId, q.getMonths()),
                new TypeReference<List<ProjectTrendPointDto>>() { },
                () -> projectStatisticsService.getTrend(scope, projectId, q.getMonths())));
        return responseDto;
    }

    /** 管理驾驶舱：部门级及以上 */
    @PostMapping("/dashboard/getOverview")
    public ResponseDto<DashboardOverviewDto> getOverview() {
        StatScopeDto scope = statisticsScopeService.resolve();
        if (!scope.isManagerial()) {
            throw new BusinessException(BusinessExceptionCode.NO_PERMISSION);
        }
        statMonthSnapshotService.ensureOnce(scope.getCustomerId());
        ResponseDto<DashboardOverviewDto> responseDto = new ResponseDto<>();
        responseDto.setContent(statisticsCache.get(scope.getCustomerId(), scope.getMemberId(), "dashboard-overview",
                Collections.singletonList(YearMonth.now().toString()),
                new TypeReference<DashboardOverviewDto>() { },
                () -> dashboardStatisticsService.getOverview(scope)));
        return responseDto;
    }

    /** 个人工作台：只看本人，不缓存 */
    @PostMapping("/workbench/getSelf")
    public ResponseDto<WorkbenchSelfDto> getWorkbenchSelf() {
        StatScopeDto scope = statisticsScopeService.resolve();
        ResponseDto<WorkbenchSelfDto> responseDto = new ResponseDto<>();
        responseDto.setContent(dashboardStatisticsService.getWorkbenchSelf(scope));
        return responseDto;
    }
}
