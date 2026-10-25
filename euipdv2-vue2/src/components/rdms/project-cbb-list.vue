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
            :data="cbbList"
            tooltip-effect="dark"
            style="width: 100%"
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
              min-width="300"
              label="特性名称">
          </el-table-column>
          <el-table-column
              prop="iterationVersion"
              :formatter="versionNumFormat"
              label="特性编号"
              min-width="120"
              align="center"
              show-overflow-tooltip>
          </el-table-column>
          <el-table-column
              prop="updateTime"
              :formatter="datetimeFormat"
              label="更新时间"
              min-width="120"
              align="center"
              show-overflow-tooltip>
          </el-table-column>
          <el-table-column
              prop="referenceCbbStatus"
              :formatter="cbbStatusFormat"
              label="CBB 状态"
              width="100"
              align="center">
          </el-table-column>
          <el-table-column
              label="操作"
              width="110"
              align="center">
            <template slot-scope="scope">
              <el-button v-if="scope.row.referenceCbbId" type="primary" plain size="mini"
                         @click.stop="openTechDoc(scope.row.referenceCbbId)">技术文档</el-button>
            </template>
          </el-table-column>
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

    <CbbTechDocDialog :cbb-id="techDocCbbId" :visible.sync="techDocVisible"></CbbTechDocDialog>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import CharacterDetailInfoCoreAll from "@/components/std/character-detail-info-core-all.vue";
import CbbTechDocDialog from "@/components/rdms/cbb-tech-doc-dialog.vue";
import {iterationFormat, rmbFormat, dateFormat, formatSecond, optionKV} from "@/filter/filter"

export default {
  components: {
    CbbTechDocDialog,
    CharacterDetailInfoCoreAll,
    File,
  },
  name: "project-cbb-list",

  props: {
    projectId: {
      default: null
    },
  },

  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},

      characterInfoVisible: false,
      characterItem: {},
      cbbList: [],
      techDocCbbId: null,
      techDocVisible: false,
    }
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    _this.getCbbList(_this.projectId);
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

    getCbbList(projectId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/getCbbList/' + projectId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.cbbList = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    datetimeFormat(row, column) {
      if (row.updateTime) {
        return dateFormat(row.updateTime).substring(0, 10);
      }
      return 0;
    },

    versionNumFormat(row, column) {
      return iterationFormat(row.iterationVersion);
    },

    cbbStatusFormat(row) {
      return row.referenceCbbStatus ? optionKV(CBB_STATUS, row.referenceCbbStatus) : '';
    },

    openTechDoc(cbbId) {
      this.techDocCbbId = cbbId;
      this.techDocVisible = true;
    },

    tableRowClick(row) {
      let _this = this;
      _this.characterItem = row;
      _this.characterInfoVisible = true;
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
