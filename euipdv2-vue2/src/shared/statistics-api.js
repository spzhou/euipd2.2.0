/**
 * 统计接口。数据范围由服务端按登录成员判定，前端不传本人标识。
 * query 字段：periodType（MONTH、QUARTER、YEAR、CUSTOM）、periodValue、projectId、includeDescendants 等，见 StatQueryDto。
 */
import { post } from './http';

const BASE = '/customer/statistics';

export function getScope() {
  return post(BASE + '/scope/get', {});
}

export function getDepartmentTree(query) {
  return post(BASE + '/workload/getDepartmentTree', query || {});
}

export function listDepartmentMembers(departmentId, query) {
  return post(BASE + '/workload/listDepartmentMembers/' + encodeURIComponent(departmentId), query || {});
}

export function getPersonWorkload(customerUserId, query) {
  return post(BASE + '/workload/getPerson/' + encodeURIComponent(customerUserId), query || {});
}

/** query：status、managerId、releaseFrom、releaseTo（yyyy-MM-dd）、keyword、page、size */
export function listProjectCards(query) {
  return post(BASE + '/project/list', query || {});
}

/** 编号可为主项目或子项目，范围外编号不返回 */
export function listProjectCardsByIds(projectIds) {
  return post(BASE + '/project/listByIds', { projectIds: projectIds || [] });
}

export function getProjectDetail(projectId, query) {
  return post(BASE + '/project/getDetail/' + encodeURIComponent(projectId), query || {});
}

export function getProjectTrend(projectId, months) {
  return post(BASE + '/project/getTrend/' + encodeURIComponent(projectId), { months: months || 12 });
}

/** 管理驾驶舱，限机构级、部门级 */
export function getDashboardOverview() {
  return post(BASE + '/dashboard/getOverview', {});
}

/** 个人工作台：本人工单计数、最近到期工单与近 6 个月交付 */
export function getWorkbenchSelf() {
  return post(BASE + '/workbench/getSelf', {});
}

const PERFORMANCE = '/customer/performance';

/** 当月绩效计入工单明细：本人或范围内人员；查看他人且非机构级、部门级时金额为空 */
export function getPerformanceDetail(customerUserId) {
  return post(PERFORMANCE + '/getCurrentDetail/' + encodeURIComponent(customerUserId), {});
}

/** 绩效月报：yearMonth 为 yyyy-MM，返回明细、部门小计（含吸收率）与总计 */
export function getPerformanceMonthlyReport(yearMonth) {
  return post(PERFORMANCE + '/getMonthlyReport', { yearMonth: yearMonth });
}

