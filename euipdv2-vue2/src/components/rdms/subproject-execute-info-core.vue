<!--
 * Author: 周朔鹏
 * Date: 2024-11-04
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <el-table
        :data="subprojectAndChildrenList"
        row-key="id"
        border
        default-expand-all
        stripe
        style="width: 100%;"
        highlight-current-row
        :tree-props="{children: 'children', hasChildren: 'hasChildren'}"
        @row-click="rowClick">
      <el-table-column
          prop="projectCode"
          label="项目编号"
          align="center"
          show-overflow-tooltip
          width="180">
      </el-table-column>
      <el-table-column
          prop="projectName"
          label="项目名称"
          show-overflow-tooltip
          align="center">
      </el-table-column>
      <el-table-column
          prop="status"
          :formatter="projectStatusFormat"
          label="项目状态"
          align="center"
          width="80">
      </el-table-column>
      <el-table-column label="任务信息" align="center">
        <el-table-column
            prop="jobSumNum"
            label="数量"
            align="center"
            width="60">
        </el-table-column>
        <el-table-column
            prop="jobComplateNum"
            label="完工"
            align="center"
            width="60">
        </el-table-column>
      </el-table-column>
      <el-table-column label="功能信息" align="center">
        <el-table-column
            prop="characterSumNum"
            label="数量"
            align="center"
            width="60">
        </el-table-column>
        <el-table-column
            prop="characterComplateNum"
            label="完成"
            align="center"
            width="60">
        </el-table-column>
      </el-table-column>
      <el-table-column v-if="subProject.rdType !== RD_TYPE.RESEARCH.key" label="预算" align="center">
        <el-table-column
            prop="budgetApproved"
            :formatter="budgetApprovedFormat"
            label="预算"
            align="center"
            width="90">
        </el-table-column>
        <el-table-column
            prop="budgetActive"
            :formatter="budgetActiveFormat"
            label="支出"
            align="center"
            width="90">
        </el-table-column>
        <el-table-column
            prop="budgetRate"
            label="执行率"
            align="center"
            width="90">
        </el-table-column>
      </el-table-column>
    </el-table>
    <small style="float: right; margin: 5px 10px;"><em>备注: 本表格不统计任务任务再下发的协作任务和测试任务, 原因是下级任务的费用是任务任务的一部分.</em></small>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import {optionKV, rmbFormat} from "@/filter/filter";

export default {
  components: {
  },
  name: "subproject-execute-info-core",

  props:{
    subProjectId: {
      default: null,
    },
    entrance: {
      default: null,
    }
  },

  data: function() {
    return {
      loginUser: {},
      loginCustomer: {},
      // executeInfo: {},
      // projectExecuteInfo: [],

      subprojectAndChildren: {},
      subprojectAndChildrenList: [],

      currentRow: {},
      subProject: {},

      RD_TYPE: RD_TYPE,
    }
  },

  computed: {

  },

  mounted: function() {
    let _this = this;
    if(Tool.isNotEmpty(_this.subProjectId) ){
      _this.getSubProjectInfo(_this.subProjectId);
      _this.getSubprojectAndChildrenExeSimpleInfo(_this.subProjectId);
    }
  },

  methods: {
    budgetApprovedFormat(row, column) {
      if (row.budgetApproved) {
        return rmbFormat(row.budgetApproved);
      }
      return 0;
    },

    budgetActiveFormat(row, column) {
      if (row.budgetActive) {
        return rmbFormat(row.budgetActive);
      }
      return 0;
    },

    rowClick(row, column, e){
      let _this = this;
      _this.currentRow = row;
    },

    projectStatusFormat(row, column) {
      return optionKV(PROJECT_STATUS, row.status);
    },

    getSubprojectAndChildrenExeSimpleInfo(subprojectId) {
      let _this = this;
      let formData = new window.FormData();
      formData.append('subprojectId', subprojectId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/sub-project/getSubprojectAndChildrenExeSimpleInfo', formData).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.subprojectAndChildrenList = resp.content;
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    getSubProjectInfo(subProjectId){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/sub-project/getSubProjectSimpleInfo/' + subProjectId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.subProject = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

  }
}
</script>

<style lang="scss" scoped>
@import "./../../../public/static/scss/caipd";

</style>
