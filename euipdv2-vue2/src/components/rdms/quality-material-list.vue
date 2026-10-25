<!--
 * Author: 周朔鹏
 * Date: 2025-02-26
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div class="row">
      <div style="margin: 0 15px;">
        <p>物料管理: 是为<strong>主管经理</strong>提供的进行质量问题处理补充投料管理的页面. 这些成本会纳入质量成本的统计范围, 作为项目经理质量考核的重要依据.</p>
        <div class="hr hr-18 dotted hr-double"></div>
        <div>
          <el-table
              :data="materialList"
              row-key="id"
              border
              default-expand-all
              highlight-current-row
              style="width: 100%"
              :row-class-name="tableRowClassName"
              @row-click="rowClick">
            <el-table-column label="序号" align="center" width="90">
              <template slot-scope="scope">
                {{ scope.$index + 1 }}
              </template>
            </el-table-column>
            <el-table-column
                prop="code"
                width="180"
                label="申请单号"
                align="center"
                show-overflow-tooltip
            ></el-table-column>
            <el-table-column
                prop="name"
                label="申请标题"
                align="center"
                show-overflow-tooltip
            ></el-table-column>
            <el-table-column
                prop="characterName"
                show-overflow-tooltip
                label="所属特性"
                align="center"
            ></el-table-column>
            <el-table-column
                prop="status"
                width="110"
                sortable
                :formatter="statusFormat"
                label="状态"
                align="center"
            ></el-table-column>
            <el-table-column
                prop="usageMode"
                width="110"
                :formatter="usageModeFormat"
                sortable
                label="使用方式"
                align="center"
            ></el-table-column>
            <el-table-column
                prop="accountCost"
                width="110"
                :formatter="currencyFormat"
                sortable
                label="金额"
                align="center"
            ></el-table-column>
            <el-table-column
                prop="writerName"
                width="100"
                sortable
                label="申请人"
                align="center"
            ></el-table-column>
            <el-table-column
                prop="createTime"
                width="150"
                sortable
                :formatter="dateFormat"
                label="申请日期"
                align="center"
            ></el-table-column>
            <el-table-column
                fixed="right"
                label="操作"
                align="center"
                width="150">
              <template slot-scope="scope">
                <div>
                  <el-tooltip content="对相关特性/特性开发所需物料进行管理" placement="top" effect="light"
                              :open-delay=1000>
                    <el-button type="primary" plain size="small" v-on:click.stop="materialApprove(scope.row)">
                      <el-badge :is-dot="false">
                        详情 / 审批
                      </el-badge>
                    </el-button>
                  </el-tooltip>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>

    <!--物料审批-->
    <el-dialog
        width="33cm"
        :visible.sync="materialApproveDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <MaterialApproveDlgSap
          :key="((appItem.code) + '-' + (random))"
          :application-code="appItem.code"
          :job-type="JOB_ITEM_TYPE.TASK_FUNCTION.key"
          :doc-type="DOC_TYPE.PRE_PROJECT.key"
          :ope-status="appItem.opeStatus"
          :close-dlg = closeDialog
      ></MaterialApproveDlgSap>

    </el-dialog>

  </div>
</template>

<script>
import TabButton from "@/components/rdms/tab-button";
import File from "@/components/rdms/file";
import Swal from "sweetalert2";
import {dateFormat, fixed2fractional, optionKV, rmbFormat, shortDateFormat} from "@/filter/filter";
import SubprojectTaskTab from "@/components/rdms/subproject-task-tab.vue";
import DevelopCharacterTaskTab from "@/components/rdms/develop-character-task-tab.vue";
import MaterialApproveDlgSap from "@/components/std/material-approve-dlg-sap.vue";

export default {
  components: {
    MaterialApproveDlgSap,
    SubprojectTaskTab,
    DevelopCharacterTaskTab,
    File,
    TabButton,
  },
  name: "quality-material-list",

  props: {
    qualityId: {
      default: null,
    },
    index: {
      default: null,
    }
  },

  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},

      materialList: [],

      APPLICATION_STATUS: APPLICATION_STATUS,
      JOB_ITEM_TYPE: JOB_ITEM_TYPE,
      DOC_TYPE: DOC_TYPE,

      materialApproveDlg: false,
      dialogTitle: '',
      appItem: '',

      random: 0,
    }
  },
  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    _this.getListByQualityId(_this.qualityId);

    _this.$bus.$on('refresh-quality-material-list', (data)=>{
      if(data){
        _this.getListByQualityId(_this.qualityId);
      }
    });
  },

  beforeDestroy: function () {
    let _this = this;
    _this.$bus.$off('refresh-quality-material-list');
  },

  methods: {
    dateFormat(row, column) {
      return dateFormat(row.createTime);
    },

    tableRowClassName({row, rowIndex}) {
      if (row.status === APPLICATION_STATUS.APPLICATION.key || row.status === APPLICATION_STATUS.SUBMIT.key ) {
        return 'application-row';
      } else if (row.status === APPLICATION_STATUS.APPROVED.key) {
        return 'approve-row';
      } else if (row.status === APPLICATION_STATUS.REJECT.key) {
        return 'reject-row';
      }
      return '';
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

    statusFormat(row, column) {
      return optionKV(APPLICATION_STATUS, row.status);
    },

    usageModeFormat(row, column) {
      return optionKV(MATERIAL_USAGE_MODE, row.usageMode);
    },
    currencyFormat(row, column) {
      if (row.accountCost) {
        return rmbFormat(row.accountCost);
      }
      return 0;
    },

    rowClick(row, column, e) {
      let _this = this;
      _this.materialApprove(row);
    },

    materialApprove(application) {
      let _this = this;
      _this.appItem = application;
      _this.random = Math.random();
      _this.materialApproveDlg = true;
      _this.dialogTitle = "领料审批";
    },
    closeDialog() {
      let _this = this;
      _this.materialApproveDlg = false;
    },
    handleClose(done) {
      done();
    },

    getListByQualityId(qualityId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/material/getListByQualityId/' + qualityId ).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.materialList = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },
  }
}
</script>

<style lang="scss">
@import './../../../public/static/scss/caipd.scss';

.el-table .application-row {
  background: $colorRowApplication;
}

.el-table .approve-row {
  background: $colorRowApproved;
}
.el-table .reject-row {
  background: $colorRowReject;
}
.el-badge__content.is-dot {
  height: 10px;
  width: 10px;
  padding: 0;
  right: -2px !important;
  border-radius: 50%;
}
</style>
