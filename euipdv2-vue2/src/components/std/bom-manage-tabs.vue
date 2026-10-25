<!--
 * Author: 周朔鹏
 * Date: 2025-02-25
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div style="height: 100%;">
    <div>
      <div>
        <el-tabs
            v-model="activeName"
            type="card"
            @tab-click="handleClick">
          <el-tab-pane label="物料清单"
                       name="first">
            <div v-if="activeName === 'first'">
              <MaterialSummaryListPage
                  :key="characterId"
                  :character-id="characterId"
                  :job-type="JOB_ITEM_TYPE.DEVELOP.key"
                  :project-type="PROJECT_TYPE.SUB_PROJECT.key"
                  :index="index"
                  :entrance="ENTRANCE_CLASS.DEVELOP.key"
              ></MaterialSummaryListPage>
            </div>
          </el-tab-pane>
          <el-tab-pane label="BOM拣选"
                       name="second">
            <div v-if="activeName === 'second'">
              <BomSelectedListPage
                  :key="characterId"
                  :character-id="characterId"
                  :job-type="JOB_ITEM_TYPE.DEVELOP.key"
                  :project-type="PROJECT_TYPE.SUB_PROJECT.key"
                  :index="index"
                  :entrance="ENTRANCE_CLASS.DEVELOP.key"
              ></BomSelectedListPage>
            </div>
          </el-tab-pane>
          <el-tab-pane label="BOM编制"
                       name="third">
            <div v-if="activeName === 'third'">
              <BomEditList
                  :key="characterId"
                  :character-id="characterId"
                  :job-type="JOB_ITEM_TYPE.DEVELOP.key"
                  :project-type="PROJECT_TYPE.SUB_PROJECT.key"
                  :index="index"
                  :entrance="ENTRANCE_CLASS.DEVELOP.key"
              ></BomEditList>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import {optionKV, rmbFormat} from "@/filter/filter";
import MaterialSummaryListPage from "@/components/std/material-summary-list-page.vue";
import BomSelectedListPage from "@/components/std/bom-selected-list-dlg.vue";
import BomEditList from "@/components/std/bom-edit-list.vue";

export default {
  components: {
    MaterialSummaryListPage,
    BomSelectedListPage,
    BomEditList,
  },
  name: "bom-manage-tabs",

  props: {
    characterId: {
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

      activeName: 'first',
      JOB_ITEM_TYPE: JOB_ITEM_TYPE,
      PROJECT_TYPE: PROJECT_TYPE,
      ENTRANCE_CLASS: ENTRANCE_CLASS,
    };
  },

  computed: {
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
  },

  methods: {
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

    handleClick(tab, event){

    },

    typeFormat(row, column) {
      return optionKV(JOB_ITEM_TYPE, row.type);
    },

    itemBudgetFormat(row, column) {
      if (row.itemBudget) {
        return rmbFormat(row.itemBudget);
      }
      return 0;
    },

    activeChargeFormat(row, column) {
      if (row.activeCharge) {
        return rmbFormat(row.activeCharge);
      }
      return 0;
    },

    budgetRateFormat(row, column) {
      if (row.budgetRate) {
        return (row.budgetRate * 100).toFixed(2);  //保留两位小数
      }
      return 0;
    },

    showJobitemProcess() {
      let _this = this;
      _this.showJobitemProcessDlg = true;
      _this.dialogTitle = "任务处理过程";
    },
    handleClose(done) {
      let _this = this;
      _this.showJobitemProcessDlg = false;
      done();
    },

    closeDialog() {
      let _this = this;
      _this.showJobitemProcessDlg = false;
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
