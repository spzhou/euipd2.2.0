<!--
 * Author: 周朔鹏
 * Date: 2024-12-25
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <el-row>
      <el-col :span="24">
        <el-table
            ref="multipleTable"
            :data="characters"
            tooltip-effect="dark"
            style="width: 100%"
            :row-class-name="tableRowClassName"
            @row-click="tableRowClick">
          <el-table-column
              prop="id"
              align="center"
              label="ID"
              width="100">
          </el-table-column>
          <el-table-column
              prop="characterName"
              align="center"
              show-overflow-tooltip
              label="特性名称">
          </el-table-column>
          <el-table-column
              prop="subprojectName"
              label="子项目名称"
              align="center"
              show-overflow-tooltip>
          </el-table-column>
          <el-table-column
              prop="status"
              :formatter="characterStatusFormat"
              label="状态"
              align="center"
              width="80"
              show-overflow-tooltip>
          </el-table-column>
          <el-table-column
              prop="iterationVersion"
              :formatter="versionNumFormat"
              label="特性版本"
              width="220"
              align="center"
              show-overflow-tooltip>
          </el-table-column>
          <el-table-column
              prop="writerName"
              label="编制人"
              width="90"
              align="center"
              show-overflow-tooltip>
          </el-table-column>
          <el-table-column
              prop="budget"
              :formatter="currencyFormat"
              label="预算金额"
              align="center"
              width="110">
          </el-table-column>
          <el-table-column
              prop="haveUsedBudget"
              :formatter="haveUsedBudgetFormat"
              label="发生费用"
              align="center"
              width="110">
          </el-table-column>
          <el-table-column
              prop="leftTime"
              :formatter="leftTimeFormat"
              label="剩余时间"
              align="center"
              width="140">
          </el-table-column>
          <el-table-column
              prop="planCompleteTime"
              :formatter="datetimeFormat"
              label="计划完成时间"
              align="center"
              width="110">
          </el-table-column>
          <el-table-column
              fixed="right"
              label="操作"
              align="center"
              width="120">
            <template slot-scope="scope">
              <div>
                <div style="display: flex; flex-direction: row; justify-content: center;">
                  <el-button style="margin: 0 5px;"
                             @click.stop="showArchiveDialog(scope.row)"
                             type="primary"
                             plain
                             size="small">
                    执行数据
                  </el-button>
                </div>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </el-col>
    </el-row>

    <!--执行数据-->
    <el-dialog
        :title="((characterItem.characterName) + ' 特性开发档案')"
        width="32cm"
        :visible.sync="characterArchiveVisible"
        :before-close="handleClose"
        :close-on-click-modal=true>
      <div>
        <CharacterArchiveDlg
            :key="characterItem.id"
            :character-id="characterItem.id"
        ></CharacterArchiveDlg>
      </div>
      <div style="width: 100%; height: 50px;"></div>
    </el-dialog>

  </div>
</template>

<script>
import Swal from "sweetalert2";
import {iterationFormat, rmbFormat, dateFormat, formatSecond, optionKV} from "@/filter/filter"
import VersionLabel from "@/shared/version-label";
import CharacterArchiveDlg from "@/components/std/character-archive-dlg.vue";

export default {
  components: {
    File,
    CharacterArchiveDlg,
  },
  name: "character-developing-data",

  props: {
    projectId: {
      default: null,
    },
    index: {
      default: null,
    },
    projectType: {
      default: null,
    },
    entrance: {
      default: null,
    },

  },

  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},

      characterArchiveVisible: false,
      characters: [],
      characterItem: {},
      CHARACTER_STATUS: CHARACTER_STATUS,
    }
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();

    if (Tool.isNotEmpty(_this.projectId)) {
      _this.listAllCharactersByProjectId(_this.projectId);
    }

  },

  beforeDestroy() {

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

    tableRowClick(row, column, e){
      let _this = this;
      _this.showArchiveDialog(row);
    },

    listAllCharactersByProjectId(projectId) {
      let _this = this;
      let formData = new window.FormData();
      formData.append('projectId', projectId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/listAllCharactersByProjectId', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.characters = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    currencyFormat(row, column) {
      if (row.budget) {
        if(row.rdType !== RD_TYPE.RESEARCH.key){
          return rmbFormat(row.budget);
        }else{
          return '-';
        }
      }
      return 0;
    },

    haveUsedBudgetFormat(row, column) {
      if (row.haveUsedBudget) {
        if(row.rdType !== RD_TYPE.RESEARCH.key){
          return rmbFormat(row.haveUsedBudget);
        }else{
          return '-';
        }
      }
      if(row.rdType !== RD_TYPE.RESEARCH.key){
        return 0;
      }else{
        return '-';
      }
    },

    datetimeFormat(row, column) {
      if(row.rdType === RD_TYPE.DEVELOPMENT.key){
        if (row.planCompleteTime) {
          return dateFormat(row.planCompleteTime).substring(0, 10);
        }
        return 0;
      }else{
        return '-';
      }
    },

    versionNumFormat(row, column) {
      return VersionLabel.lockedWithLatest(row.devVersion, row.lockedIterationVersion, row.iterationVersion)
          || iterationFormat(row.iterationVersion);
    },

    characterStatusFormat(row, column) {
      return optionKV(CHARACTER_STATUS, row.status);
    },

    leftTimeFormat(row, column) {
      if(row.rdType === RD_TYPE.DEVELOPMENT.key){
        let leftTime = new Date(row.planCompleteTime).getTime();
        let left = leftTime / 1000 - Date.now() / 1000;
        return formatSecond(left);
      }else{
        return '-';
      }
    },


    showArchiveDialog(row, column, event) {
      let _this = this;
      _this.characterItem = row;
      _this.characterArchiveVisible = true;
    },

    handleClose(done) {
      let _this = this;
      _this.characterArchiveVisible = false;
      done()
    },

    tableRowClassName({row, rowIndex}) {
      if (Number(1.2) >= Number(Number(row.haveUsedBudget) / Number(row.budget)) && Number(Number(row.haveUsedBudget) / Number(row.budget)) > Number(1.0) ) {
        return 'warning-row-1';
      }else if (Number(1.5) >= Number(Number(row.haveUsedBudget) / Number(row.budget))  && Number(Number(row.haveUsedBudget) / Number(row.budget)) > Number(1.2) ) {
        return 'warning-row-2';
      }else if (Number(1.8) >= Number(Number(row.haveUsedBudget) / Number(row.budget))  && Number(Number(row.haveUsedBudget) / Number(row.budget)) > Number(1.5) ) {
        return 'warning-row-3';
      }else if (Number(2.0) >= Number(Number(row.haveUsedBudget) / Number(row.budget))  && Number(Number(row.haveUsedBudget) / Number(row.budget)) > Number(1.8) ) {
        return 'warning-row-4';
      }else if (Number(Number(row.haveUsedBudget) / Number(row.budget)) > Number(2.0) ) {
        return 'warning-row-5';
      }
      return '';
    }
  }
}
</script>

<style>
.el-table .warning-row-1 {
  background: #FDF5E6FF;
}
.el-table .warning-row-2 {
  background: #fde8c1;
}
.el-table .warning-row-3 {
  background: #fcdba0;
}
.el-table .warning-row-4 {
  background: #facd7c;
}
.el-table .warning-row-5 {
  background: #fac059;
}

</style>

<style lang="scss" scoped>

</style>
