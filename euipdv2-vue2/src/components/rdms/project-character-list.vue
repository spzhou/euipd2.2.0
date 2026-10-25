<!--
 * Author: 周朔鹏
 * Date: 2025-02-23
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
            @selection-change="handleSelectionChange"
            @row-click="tableRowClick">
          <el-table-column
              prop="id"
              align="center"
              label="ID"
              width="90">
          </el-table-column>
          <el-table-column
              prop="characterName"
              align="center"
              show-overflow-tooltip
              label="特性名称">
          </el-table-column>
<!--          <el-table-column
              prop="subprojectName"
              label="子项目名称"
              align="center"
              width="110"
              show-overflow-tooltip>
          </el-table-column>-->
          <el-table-column
              prop="status"
              :formatter="characterStatusFormat"
              label="状态"
              align="center"
              width="120"
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
              width="120"
              align="center"
              show-overflow-tooltip>
          </el-table-column>
          <el-table-column
              prop="budget"
              :formatter="currencyFormat"
              label="预算"
              align="center"
              width="120">
          </el-table-column>
<!--          <el-table-column
              prop="leftTime"
              :formatter="leftTimeFormat"
              label="剩余时间"
              align="center"
              width="110">
          </el-table-column>
          <el-table-column
              prop="planCompleteTime"
              :formatter="datetimeFormat"
              label="计划完成时间"
              align="center"
              width="100">
          </el-table-column>-->
        </el-table>
      </el-col>
    </el-row>

    <!--特性定义表-->
    <el-dialog
        title="特性定义表"
        width="29cm"
        :visible.sync="characterInfoVisible"
        :before-close="handleClose"
        :close-on-click-modal=true>
      <div>
        <CharacterDetailInfoCoreAll
            :key="characterItem.id"
            :character-id="characterItem.id"
        ></CharacterDetailInfoCoreAll>
      </div>
      <div style="width: 100%; height: 50px;"></div>
    </el-dialog>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import CharacterDetailInfoCoreAll from "@/components/std/character-detail-info-core-all.vue";
import {iterationFormat, rmbFormat, dateFormat, formatSecond, optionKV} from "@/filter/filter"
import VersionLabel from "@/shared/version-label";

export default {
  components: {
    CharacterDetailInfoCoreAll,
    File,
  },
  name: "project-character-list",

  props: {
    xProjectId: {
      default: null
    },
    projectType: {
      default: null,
    },
    setupStage: {
      default: SETUP_STAGE.SETUPED.key,
    },
  },

  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},

      multipleSelection: [],

      JOB_ITEM_TYPE: JOB_ITEM_TYPE,
      ENTRANCE_CLASS: ENTRANCE_CLASS,

      characterInfoVisible: false,
      jobItemDlgVisible: false,
      distributionDlgVisible: false,
      jobItemDlgVisible_iteration: false,

      PROJECT_TYPE: PROJECT_TYPE,

      subprojects: [],
      characterItem: {},
      subprojectId: '',
      CHARACTER_STATUS: CHARACTER_STATUS,

      characters: [],
    }
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();

    if (Tool.isNotEmpty(_this.xProjectId)) {
      if (_this.projectType === PROJECT_TYPE.PROJECT.key) {
        if(_this.setupStage === SETUP_STAGE.SETUPED.key){
          _this.getSetupedCharacterListByProjectId(_this.xProjectId);
        }else if(_this.setupStage === SETUP_STAGE.SETUP.key){
          _this.getSetupCharacterListByProjectId(_this.xProjectId);
        }
      }
      if (_this.projectType === PROJECT_TYPE.SUB_PROJECT.key) {
        _this.getSetupedCharacterListBySubProjectId(_this.xProjectId);
      }
    }

  },

  beforeDestroy() {
    let _this = this;
    _this.$bus.$off('refresh-project-subproject-character-list');
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

    getSetupedCharacterListByProjectId(projectId) {
      let _this = this;
      let formData = new window.FormData();
      formData.append('projectId', projectId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/getSetupedCharacterListByProjectId', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.characters = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    getSetupCharacterListByProjectId(projectId) {
      let _this = this;
      let formData = new window.FormData();
      formData.append('projectId', projectId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/getSetupCharacterListByProjectId', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.characters = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    getSetupedCharacterListBySubProjectId(subprojectId) {
      let _this = this;
      let formData = new window.FormData();
      formData.append('subprojectId', subprojectId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/getSetupedCharacterListBySubProjectId', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.characters = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    characterStatusCompare: function (row) {
      let statusArray = [];
      statusArray.push(CHARACTER_STATUS.SETUPED.key);
      for (let i = 0; i < statusArray.length; i++) {
        if (statusArray[i] === row.status) {
          return true;
        }
      }
      return false;
    },

    handleSelectionChange(val) {
      this.multipleSelection = val;
    },

    currencyFormat(row, column) {
      if (row.budget) {
        return rmbFormat(row.budget);
      }
      return 0;
    },

    datetimeFormat(row, column) {
      if (row.planCompleteTime) {
        return dateFormat(row.planCompleteTime).substring(0, 10);
      }
      return 0;
    },

    versionNumFormat(row, column) {
      return VersionLabel.lockedWithLatest(row.devVersion, row.lockedIterationVersion, row.iterationVersion)
          || iterationFormat(row.iterationVersion);
    },

    characterStatusFormat(row, column) {
      return optionKV(CHARACTER_STATUS, row.status);
    },

    leftTimeFormat(row, column) {
      let leftTime = new Date(row.planCompleteTime).getTime();
      // let now = Date.now();
      let left = leftTime / 1000 - Date.now() / 1000;
      return formatSecond(left);
    },

    saveDistribution() {
      let _this = this;
      if (1 !== 1
          || !Validator.require(_this.subprojectId, "子项目ID", Swal)
          || !Validator.length(_this.subprojectId, "子项目ID", 8, 8, Swal)
      ) {
        return;
      }

      _this.characterItem.subprojectId = _this.subprojectId;

      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/saveDistribution', _this.characterItem).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.distributionDlgVisible = false;
          Toast.success("提交成功", Swal);
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    createJobItem_decompose(row) {
      let _this = this;
      _this.characterItem = row;
      _this.jobItemDlgVisible = true;
    },

    createJobItem_iteration(row) {
      let _this = this;
      _this.characterItem = row;
      _this.jobItemDlgVisible_iteration = true;
    },

    characterDistribution(row) {
      let _this = this;
      _this.characterItem = row;
      _this.ListSubprojectByIds();
      _this.distributionDlgVisible = true;
    },

    tableRowClick(row) {
      let _this = this;
      _this.characterItem = row;
      _this.characterInfoVisible = true;
    },

    closeDlg() {
      let _this = this;
      _this.jobItemDlgVisible = false;
    },

    closeCharacterIterationDlg() {
      let _this = this;
      _this.jobItemDlgVisible_iteration = false;
    },

    handleClose(done) {
      let _this = this;
      _this.characterInfoVisible = false;
      done()
    }
  }
}
</script>

<style lang="scss" scoped>

</style>
