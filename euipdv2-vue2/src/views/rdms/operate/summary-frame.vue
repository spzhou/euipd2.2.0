<!--
 * Author: 周朔鹏
 * Date: 2025-02-25
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <!--Tab 卡片-->
    <div class="row">
      <div class="col-md-12">
        <div class="tabbable">
          <ul class="nav nav-tabs" id="myTab">
            <li v-permission="'520002'" :class="{active : activePageId==='dashboard'}">
              <a @click="switchTab('dashboard')">
                管理驾驶舱
              </a>
            </li>
            <li v-permission="'520003'" :class="{active : activePageId==='project-board'}">
              <a @click="switchTab('project-board')">
                项目仪表盘
              </a>
            </li>
            <li v-permission="'520004'" :class="{active : activePageId==='org-workload'}">
              <a @click="switchTab('org-workload')">
                组织工作量
              </a>
            </li>
            <li :class="{active : activePageId==='task-performance'}">
              <a @click="tabPerformanceClick">
                任务与绩效
              </a>
            </li>
            <li v-permission="'520001'" :class="{active : activePageId==='material'}">
              <a @click="tabMaterialClick">
                物料盘点表
              </a>
            </li>

          </ul>

          <div class="tab-content">
            <div v-permission="'520002'" id="dashboard" class="tab-pane fade" :class="{active : activePageId === 'dashboard', in : activePageId === 'dashboard' }">
              <p>管理驾驶舱: 所辖范围的在研项目、工单、交付工时、人员费与预算执行，项目健康分布、部门负荷、绩效排行与近 6 个月趋势.</p>
              <div class="hr hr-8 dotted"></div>
              <DashboardOverview
                  v-if="activePageId === 'dashboard'"
                  @go-projects="goProjects"
                  @go-department="goDepartment"
              ></DashboardOverview>
            </div>
            <div v-permission="'520003'" id="project-board" class="tab-pane fade" :class="{active : activePageId === 'project-board', in : activePageId === 'project-board' }">
              <p>项目仪表盘: 按项目汇总工单进度、逾期、预算执行与里程碑，并给出健康度；点击项目卡片查看子项目、成员与趋势.</p>
              <div class="hr hr-8 dotted"></div>
              <ProjectStatBoard
                  v-if="activePageId === 'project-board'"
                  :preset="boardPreset"
              ></ProjectStatBoard>
            </div>
            <div id="task-performance" class="tab-pane fade" :class="{active : activePageId === 'task-performance', in : activePageId === 'task-performance' }">
              <p>下属任务信息: 您可以在此页面查看自己下属的工作任务情况以及当月的绩效情况.</p>
              <div class="hr hr-18 dotted hr-double"></div>
              <TaskPerformance
                  v-if="loginUser && loginUser.customerUserId"
                  :key="loginUser.customerUserId"
              ></TaskPerformance>
            </div>
            <div v-permission="'520004'" id="org-workload" class="tab-pane fade" :class="{active : activePageId === 'org-workload', in : activePageId === 'org-workload' }">
              <p>组织工作量: 按部门汇总成员的工单、工时负荷、延期、人员费与绩效，数据范围随登录成员的管理范围.</p>
              <div class="hr hr-8 dotted"></div>
              <OrgWorkload
                  v-if="activePageId === 'org-workload'"
                  :focus-department-id="focusDepartmentId"
                  @go-performance="activePageId = 'task-performance'"
              ></OrgWorkload>
            </div>
            <div v-permission="'520001'" id="material" class="tab-pane fade" :class="{active : activePageId === 'material', in : activePageId === 'material' }">
              <p>物料盘点表: 根据物料的使用属性分别进行盘点汇总.</p>
              <div class="hr hr-8 dotted"></div>
              <el-tabs v-model="activeName" @tab-click="handleClick">
                <el-tab-pane label="借用物料盘点表" name="first">
                  <div v-if="activeName === 'first'">
                    <MaterialCheckListCustomer
                        :key="'borrow'"
                        :application-type="MATERIAL_USAGE_MODE.BORROW.key"
                    ></MaterialCheckListCustomer>
                  </div>
                </el-tab-pane>
                <el-tab-pane label="费用化物料盘点表" name="second">
                  <div v-if="activeName === 'second'">
                    <MaterialCheckListCustomer
                        :key="'expenditure'"
                        :application-type="MATERIAL_USAGE_MODE.EXPENDITURE.key"
                    ></MaterialCheckListCustomer>
                  </div>
                </el-tab-pane>
                <el-tab-pane label="资产化物料盘点表" name="third">
                  <div v-if="activeName === 'third'">
                    <MaterialCheckListCustomer
                        :key="'capitalization'"
                        :application-type="MATERIAL_USAGE_MODE.CAPITALIZATION.key"
                    ></MaterialCheckListCustomer>
                  </div>
                </el-tab-pane>
              </el-tabs>
            </div>

          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import TaskPerformance from "@/components/rdms/task-performance.vue";
import MaterialCheckListCustomer from "@/components/std/material-check-list-customer.vue";

export default {
  components: {
    TaskPerformance,
    MaterialCheckListCustomer,
    OrgWorkload: () => import(/* webpackChunkName: "statistics" */ "@/components/statistics/org-workload.vue"),
    ProjectStatBoard: () => import(/* webpackChunkName: "statistics" */ "@/components/statistics/project-stat-board.vue"),
    DashboardOverview: () => import(/* webpackChunkName: "statistics" */ "@/components/statistics/dashboard-overview.vue"),
  },
  name: "summary-frame",

  data: function() {
    return {
      loginUser: {},
      loginCustomer: {},
      activePageId: 'task-performance',
      activeName: 'first',
      MATERIAL_USAGE_MODE: MATERIAL_USAGE_MODE,
      boardPreset: null,
      focusDepartmentId: '',
    }
  },

  mounted: function() {
    let _this = this;
    _this.getLoginUser();
    _this.activePageId = _this.defaultTab();
  },

  computed: {
  },

  methods: {
    getLoginUser(){
      let _this = this;
      _this.loginUser = Tool.getActorUser();
      _this.loginCustomer = Tool.getLoginCustomer();
    },

    // 默认打开有权限的第一个统计页签
    defaultTab() {
      const tabs = [['520002', 'dashboard'], ['520003', 'project-board'], ['520004', 'org-workload']];
      const hit = tabs.find(t => Tool.hasButtonPermission && Tool.hasButtonPermission(t[0]));
      return hit ? hit[1] : 'task-performance';
    },

    // 手动切换页签时清除驾驶舱带入的筛选
    switchTab(pageId) {
      this.boardPreset = null;
      this.focusDepartmentId = '';
      this.activePageId = pageId;
    },

    goProjects(preset) {
      this.focusDepartmentId = '';
      this.boardPreset = preset;
      this.activePageId = 'project-board';
    },

    goDepartment(departmentId) {
      this.boardPreset = null;
      this.focusDepartmentId = departmentId || '';
      this.activePageId = 'org-workload';
    },

    tabPerformanceClick(){
      this.switchTab('task-performance');
    },
    tabMaterialClick(){
      this.switchTab('material');
    },
    handleClick(tab, event) {
      console.log(tab, event);
    }

  }
}
</script>

