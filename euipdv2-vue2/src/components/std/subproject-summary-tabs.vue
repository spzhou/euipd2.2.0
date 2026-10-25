<!--
 * Author: 周朔鹏
 * Date: 2025-02-25
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div style="height: 100%; margin-top: -25px;">
    <div class="paper-height">
      <div style="margin: 0 0;">
        <el-tabs
            v-model="activeName">
          <el-tab-pane v-if="projectType === PROJECT_TYPE.SUB_PROJECT.key" label=" 预算执行统计表" name="budget-execution">
            <div v-if="activeName === 'budget-execution'">
              <budgetDirectStatisticsPage
                  :key="subprojectId"
                  :x-project-id="subprojectId"
                  :project-type="PROJECT_TYPE.SUB_PROJECT.key"
              ></budgetDirectStatisticsPage>
            </div>
          </el-tab-pane>
          <el-tab-pane v-if="projectType === PROJECT_TYPE.PROJECT.key" label=" 完整项目预算执行表" name="project-budget-execution">
            <div v-if="activeName === 'project-budget-execution'">
              <budgetDirectStatisticsPage
                  :key="subprojectId"
                  :x-project-id="subprojectId"
                  :project-type="PROJECT_TYPE.PROJECT.key"
              ></budgetDirectStatisticsPage>
            </div>
          </el-tab-pane>
          <el-tab-pane label="特性清单" name="develop_jobitem">
            <div v-if="activeName === 'develop_jobitem'">
              <SummaryCharacterListPage
                  :key="subprojectId"
                  :subproject-id="subprojectId">
              </SummaryCharacterListPage>
            </div>
          </el-tab-pane>
          <el-tab-pane label="文档汇总" name="archive-true">
            <div v-if="activeName === 'archive-true'">
              <SubprojectArchiveTree
                  :key="subprojectId"
                  :project-id="subprojectId">
              </SubprojectArchiveTree>
            </div>
          </el-tab-pane>
          <el-tab-pane label="综合统计表" name="subproject-summary">
            <div v-if="activeName === 'subproject-summary'">
              <SubprojectExeSummaryPage
                  :key="subprojectId"
                  :subproject-id="subprojectId"
              ></SubprojectExeSummaryPage>
            </div>
          </el-tab-pane>
          <el-tab-pane label="费用清单" name="fee-sheet">
            <div v-if="activeName === 'fee-sheet'">
              <SubprojectFeeSummaryPage
                  :key="((subprojectId) + '-' + (projectType))"
                  :subproject-id="subprojectId"
                  :project-type="projectType"
              ></SubprojectFeeSummaryPage>
            </div>
          </el-tab-pane>
          <el-tab-pane label="基线" name="baseline">
            <div v-if="activeName === 'baseline'">
              <BaselineList
                  :key="((subprojectId) + '-' + (projectType))"
                  :owner-type="projectType === PROJECT_TYPE.PROJECT.key ? 'PROJECT' : 'SUBPROJECT'"
                  :owner-id="subprojectId">
              </BaselineList>
            </div>
          </el-tab-pane>

        </el-tabs>
      </div>
    </div>
  </div>
</template>

<script>

import SubprojectArchiveTree from "@/components/rdms/subproject-archive-tree.vue";
import SummaryCharacterListPage from "@/components/std/summary-character-list-page.vue";
import budgetDirectStatisticsPage from "@/components/std/budget-direct-statistics-page.vue";
import SubprojectExeSummaryPage from "@/components/std/subproject-exe-summary-page.vue";
import SubprojectFeeSummaryPage from "@/components/std/subproject-fee-summary-page.vue";
import BaselineList from "@/components/std/baseline-list.vue";

export default {
  components: {
    budgetDirectStatisticsPage,
    SubprojectArchiveTree,
    SummaryCharacterListPage,
    SubprojectExeSummaryPage,
    SubprojectFeeSummaryPage,
    BaselineList,
  },
  name: "subproject-summary-tabs",

  props: {
    subprojectId: {
      default: null,
    },
    projectType: {
      default: PROJECT_TYPE.SUB_PROJECT.key,
    },
  },

  data() {
    return {
      activeName: 'budget-execution',
      PROJECT_TYPE: PROJECT_TYPE,
    }
  },

  mounted: function () {
    let _this = this;
    if(_this.projectType === PROJECT_TYPE.SUB_PROJECT.key){
      _this.activeName = 'budget-execution';
    }else{
      _this.activeName = 'project-budget-execution';
    }
  },

  computed: {
  },

  methods: {
  },
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd';

.paper-height {
  height: calc(100vh - 220px);
}

</style>
