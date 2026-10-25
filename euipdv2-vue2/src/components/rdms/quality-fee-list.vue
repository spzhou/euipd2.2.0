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
        <p>费用管理: 是为<strong>项目经理</strong>提供的进行研发费用管理的一套功能页面.</p>
        <div class="hr hr-18 dotted hr-double"></div>
        <div>

          <el-table
              :data="feeManageList"
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
                align="center"
                label="申请单号"
                width="180"
                show-overflow-tooltip
            ></el-table-column>
            <el-table-column
                prop="name"
                show-overflow-tooltip
                label="申请标题"
                align="center"
            ></el-table-column>
            <el-table-column
                prop="characterName"
                show-overflow-tooltip
                label="所属特性"
                align="center"
            ></el-table-column>
            <el-table-column
                prop="status"
                sortable
                :formatter="statusFormat"
                width="110"
                label="状态"
                align="center"
            ></el-table-column>
            <el-table-column
                prop="type"
                :formatter="feeTypeFormat"
                width="110"
                sortable
                label="费用类型"
                align="center"
            ></el-table-column>
            <el-table-column
                prop="accountCost"
                :formatter="currencyFormat"
                width="130"
                sortable
                label="金额"
                align="center"
            ></el-table-column>
            <el-table-column
                prop="writerName"
                width="110"
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
                  <el-tooltip content="对相关特性/特性开发所需费用进行管理" placement="top" effect="light"
                              :open-delay=1000>
                    <el-button type="primary" plain size="small" v-on:click.stop="feeApprove(scope.row)">
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

    <!--费用审批-->
    <el-dialog
        width="29cm"
        :visible.sync="feeApproveDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div>
        <FeeApproveDlg
            :key="((appItem.code) + '-' + (random))"
            :application-code="appItem.code"
            :job-type="JOB_ITEM_TYPE.QUALITY.key"
            :doc-type="DOC_TYPE.CHARACTER.key"
            :close-dlg = closeDialog
        ></FeeApproveDlg>
      </div>

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
import FeeApproveDlg from "@/components/std/fee-approve-dlg.vue";

export default {
  components: {
    SubprojectTaskTab,
    DevelopCharacterTaskTab,
    File,
    TabButton,
    FeeApproveDlg,
  },
  name: "quality-fee-list",

  props: {
    qualityId: {
      default: null,
    },
    index: {
      default: null,
    },
  },

  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},

      feeManageList: [],

      APPLICATION_STATUS: APPLICATION_STATUS,
      JOB_ITEM_TYPE: JOB_ITEM_TYPE,
      DOC_TYPE: DOC_TYPE,

      feeApproveDlg: false,
      dialogTitle: '',
      appItem: '',

      random: 0,
    }
  },
  mounted: function () {
    let _this = this;
    _this.getLoginUser();

    _this.getListByQualityId(_this.qualityId);

    _this.$bus.$on('refresh-quality-fee-list', (data)=>{
      if(data){
        _this.getListByQualityId(_this.qualityId);
      }
    });

  },

  beforeDestroy: function () {
    let _this = this;
    _this.$bus.$off('refresh-quality-fee-list');
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

    feeTypeFormat(row, column) {
      return optionKV(FEE_TYPE_STD, row.type);
    },
    currencyFormat(row, column) {
      if (row.accountCost) {
        return rmbFormat(row.accountCost);
      }
      return 0;
    },

    rowClick(row, column, e) {
      let _this = this;
      _this.feeApprove(row);
    },

    feeApprove(application) {
      let _this = this;
      _this.appItem = application;
      _this.random = Math.random();
      _this.feeApproveDlg = true;
      _this.dialogTitle = "费用审批";
    },
    closeDialog() {
      let _this = this;
      _this.feeApproveDlg = false;
    },
    handleClose(done) {
      done();
    },

    getListByQualityId(qualityId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/fee/getListByQualityId/' + qualityId ).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.feeManageList = resp.content;
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
