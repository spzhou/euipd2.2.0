<!--
 * Author: 周朔鹏
 * Date: 2025-02-25
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div style="height: 100%;">
    <div class="paper-height">
      <div style="margin-left: 10px;">
        <el-tabs
            v-model="activeName"
            @tab-click="handleClick">
          <el-tab-pane label="评审会签表" name="review_sheet">
            <div v-if="activeName === 'review_sheet'">
              <ReviewSheetPage
                  :key="jobitemId"
                  :jobitem-id="jobitemId"
                  :job-type="jobType"
                  :project-type="projectType"
                  :index="index"
                  :entrance="entrance">
              </ReviewSheetPage>
            </div>
          </el-tab-pane>
          <el-tab-pane label="特性清单" name="develop_jobitem">
            <div v-if="activeName === 'develop_jobitem'">
              <ReviewCharacterListPage
                  :key="jobitemId"
                  :jobitem-id="jobitemId"
                  :job-type="jobType"
                  :project-type="projectType"
                  :index="index"
                  :entrance="entrance">
              </ReviewCharacterListPage>
            </div>
          </el-tab-pane>
          <el-tab-pane label="BOM清单" name="bom_list">
            <div v-if="activeName === 'bom_list'">
              <BomListPage
                  :key="jobitemId"
                  :jobitem-id="jobitemId"
                  :job-type="jobType"
                  :project-type="projectType"
                  :index="index"
                  :entrance="entrance">
              </BomListPage>
            </div>
          </el-tab-pane>
          <el-tab-pane label="文档汇总" name="archive-true">
            <div v-if="activeName === 'archive-true'">
              <SubprojectArchiveTree
                  :key="jobItem.subprojectId"
                  :index="index"
                  :project-id="jobItem.subprojectId"
                  :project-type="projectType"
                  :entrance="entrance">
              </SubprojectArchiveTree>
            </div>
          </el-tab-pane>
          <el-tab-pane label="预算执行统计表" name="budget-execution">
            <div v-if="activeName === 'budget-execution'">
              <BudgetSubprojectSummaryPage
                  :key="((jobitemId) + '-subproject')"
                  :jobitem-id="jobitemId"
                  :job-type="jobType"
                  :project-type="projectType"
                  :index="index"
                  :entrance="entrance">
              </BudgetSubprojectSummaryPage>
            </div>
          </el-tab-pane>
          <el-tab-pane label="费用清单" name="fee-sheet">
            <div v-if="activeName === 'fee-sheet'">
              <SubprojectFeeSummaryPage
                  :key="((jobItem.subprojectId) + '-' + (projectType))"
                  :subproject-id="jobItem.subprojectId"
                  :project-type="projectType"
              ></SubprojectFeeSummaryPage>
            </div>
          </el-tab-pane>
          <el-tab-pane label="综合统计表" name="subproject-summary">
            <div v-if="activeName === 'subproject-summary'">
              <ReviewSubprojectSummaryPage
                  :key="jobitemId"
                  :jobitem-id="jobitemId"
                  :job-type="jobType"
                  :project-type="projectType"
                  :index="index"
                  :entrance="entrance">
              </ReviewSubprojectSummaryPage>
            </div>
          </el-tab-pane>
          <el-tab-pane v-if="jobItem.projectId === jobItem.subprojectId" label="完整项目预算执行表" name="project-budget-execution">
            <div v-if="activeName === 'project-budget-execution'">
              <BudgetProjectSummaryPage
                  :key="((jobItem.id) + '-project')"
                  :jobitem-id="jobItem.id"
                  :job-type="jobType"
                  :project-type="PROJECT_TYPE.PROJECT.key"
                  :index="index"
                  :entrance="entrance">
              </BudgetProjectSummaryPage>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
</template>

<script>
import ReviewSheetPage from "@/components/std/review-sheet-page.vue";
import ReviewCharacterListPage from "@/components/std/review-character-list-page.vue";
import BomListPage from "@/components/std/bom-list-page.vue";

import ReviewDevelopPage from "@/components/std/review-develop-page.vue";
import ReviewTestPage from "@/components/std/review-test-page.vue";
import ReviewFeePage from "@/components/std/review-fee-page.vue";
import ReviewSubprojectSummaryPage from "@/components/std/review-subproject-summary-page.vue";
import ReviewBomList from "@/components/std/review-bom-list.vue";
import ReviewManhourPage from "@/components/std/review-manhour-page.vue";
import SubprojectArchiveTree from "@/components/rdms/subproject-archive-tree.vue";
import Swal from "sweetalert2";
import BudgetSubprojectSummaryPage from "@/components/std/budget-subproject-summary-page.vue";
import BudgetProjectSummaryPage from "@/components/std/budget-project-summary-page.vue";
import SubprojectFeeSummaryPage from "@/components/std/subproject-fee-summary-page.vue";

export default {
  components: {
    SubprojectFeeSummaryPage,
    BudgetProjectSummaryPage,
    BudgetSubprojectSummaryPage,
    SubprojectArchiveTree,
    ReviewSheetPage,
    ReviewCharacterListPage,

    ReviewBomList,
    ReviewDevelopPage,
    ReviewTestPage,
    ReviewFeePage,
    ReviewSubprojectSummaryPage,
    ReviewManhourPage,
    BomListPage,
  },
  name: "review-subproject-content-tabs",

  props: {
    jobitemId: {
      default: null,
    },
    jobType: {
      default: null,
    },
    projectType: {
      default: null,
    },
    index: {
      default: null,
    },
    entrance: {
      default: null,
    },
  },

  data() {
    return {
      loginUser: {},
      loginCustomer: {},

      activeName: 'review_sheet',
      jobItem: {},
      PROJECT_TYPE: PROJECT_TYPE,
    }
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    _this.getJobitemSimpleInfo(_this.jobitemId);
  },

  methods: {
    handleClick(tab, event) {
      console.log(tab, event);
    },

    getLoginUser() {
      let _this = this;
      if (Tool.getLoginUser().userGroup === LOGIN_USER_GROUP.CUSTOMER.key) {
        _this.loginUser = Tool.getLoginUser().adminUser;
        _this.loginCustomer = Tool.getLoginUser();
      } else {
        _this.loginUser = Tool.getLoginUser();
        _this.loginCustomer = Tool.getLoginUser().customer;
      }
    },

    getJobitemSimpleInfo(jobitemId){
      let _this = this;
      let formData = new window.FormData();
      formData.append('jobitemId', jobitemId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getJobitemSimpleInfo', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.jobItem = resp.content;
        }else{
          Toast.warning(resp.message, Swal);
        }
      });
    },

  },
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd';

.paper-height {
  height: calc(100vh - 200px);
}

</style>
