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
          <el-tab-pane label="文档汇总" name="doc_summary">
            <div v-if="activeName === 'doc_summary'">
              <ReviewDocSummaryPage
                  :key="jobitemId"
                  :jobitem-id="jobitemId"
                  :job-type="jobType"
                  :project-type="projectType"
                  :index="index"
                  :entrance="entrance">
              </ReviewDocSummaryPage>
            </div>
          </el-tab-pane>
          <el-tab-pane label="开发任务" name="develop_jobitem">
            <div v-if="activeName === 'develop_jobitem'">
              <ReviewDevelopPage
                  :key="jobitemId"
                  :jobitem-id="jobitemId"
                  :job-type="jobType"
                  :project-type="projectType"
                  :index="index"
                  :entrance="entrance">
              </ReviewDevelopPage>
            </div>
          </el-tab-pane>
          <el-tab-pane label="测试工单" name="test_jobitem">
            <div v-if="activeName === 'test_jobitem'">
              <ReviewTestPage
                  :key="jobitemId"
                  :jobitem-id="jobitemId"
                  :job-type="jobType"
                  :project-type="projectType"
                  :index="index"
                  :entrance="entrance">
              </ReviewTestPage>
            </div>
          </el-tab-pane>
          <el-tab-pane label="物料领用" name="material">
            <div v-if="activeName === 'material'">
              <ReviewMaterialPage
                  :key="jobitemId"
                  :jobitem-id="jobitemId"
                  :job-type="jobType"
                  :project-type="projectType"
                  :index="index"
                  :entrance="entrance">
              </ReviewMaterialPage>
            </div>
          </el-tab-pane>
          <el-tab-pane label="其他费用" name="other_fee">
            <div v-if="activeName === 'other_fee'">
              <ReviewFeePage
                  :key="jobitemId"
                  :jobitem-id="jobitemId"
                  :job-type="jobType"
                  :project-type="projectType"
                  :index="index"
                  :entrance="entrance">
              </ReviewFeePage>
            </div>
          </el-tab-pane>
          <el-tab-pane label="BOM清单" name="bom_list">
            <div v-if="activeName === 'bom_list'">
              <ReviewBomList
                  :key="jobitemId"
                  :jobitem-id="jobitemId"
                  :job-type="jobType"
                  :project-type="projectType"
                  :index="index"
                  :entrance="entrance">
              </ReviewBomList>
            </div>
          </el-tab-pane>
          <el-tab-pane label="工时投入" name="manhour">
            <div v-if="activeName === 'manhour'">
              <ReviewManhourPage
                  :key="jobitemId"
                  :jobitem-id="jobitemId"
                  :job-type="jobType"
                  :project-type="projectType"
                  :index="index"
                  :entrance="entrance">
              </ReviewManhourPage>
            </div>
          </el-tab-pane>
          <el-tab-pane label="综合统计表" name="summary">
            <div v-if="activeName === 'summary'">
              <ReviewSummaryPage
                  :key="jobitemId"
                  :jobitem-id="jobitemId"
                  :job-type="jobType"
                  :project-type="projectType"
                  :index="index"
                  :entrance="entrance">
              </ReviewSummaryPage>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
</template>

<script>
import ReviewSheetPage from "@/components/std/review-sheet-page.vue";
import ReviewDevelopPage from "@/components/std/review-develop-page.vue";
import ReviewTestPage from "@/components/std/review-test-page.vue";
import ReviewMaterialPage from "@/components/std/review-material-page.vue";
import ReviewFeePage from "@/components/std/review-fee-page.vue";
import ReviewSummaryPage from "@/components/std/review-summary-page.vue";
import ReviewBomList from "@/components/std/review-bom-list.vue";
import ReviewManhourPage from "@/components/std/review-manhour-page.vue";
import ReviewDocSummaryPage from "@/components/std/review-doc-summary-page.vue";
import Swal from "sweetalert2";

export default {
  components: {
    ReviewBomList,
    ReviewSheetPage,
    ReviewDevelopPage,
    ReviewTestPage,
    ReviewMaterialPage,
    ReviewFeePage,
    ReviewSummaryPage,
    ReviewManhourPage,
    ReviewDocSummaryPage
  },
  name: "review-content-tabs",

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

      RD_TYPE: RD_TYPE,
    }
  },

  computed: {
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
