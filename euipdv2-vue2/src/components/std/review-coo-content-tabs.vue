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
              <JobitemReviewSheetPage
                  :key="jobitemId"
                  :jobitem-id="jobitemId"
                  :job-type="jobType"
                  :project-type="projectType"
                  :index="index"
                  :entrance="entrance">
              </JobitemReviewSheetPage>
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
import ReviewDevelopPage from "@/components/std/review-develop-page.vue";
import ReviewTestPage from "@/components/std/review-test-page.vue";
import ReviewMaterialPage from "@/components/std/review-material-page.vue";
import ReviewFeePage from "@/components/std/review-fee-page.vue";
import ReviewSummaryPage from "@/components/std/review-summary-page.vue";
import JobitemReviewSheetPage from "@/components/rdms/jobitem-review-sheet-page.vue";
import ReviewBomList from "@/components/std/review-bom-list.vue";

export default {
  components: {
    JobitemReviewSheetPage,
    ReviewDevelopPage,
    ReviewTestPage,
    ReviewMaterialPage,
    ReviewFeePage,
    ReviewSummaryPage,
    ReviewBomList,
  },
  name: "review-coo-content-tabs",

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
      activeName: 'review_sheet',
    }
  },

  computed: {
  },

  mounted: function () {
  },

  methods: {
    handleClick(tab, event) {
      console.log(tab, event);
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
