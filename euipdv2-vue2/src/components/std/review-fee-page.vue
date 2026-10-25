<!--
 * Author: 周朔鹏
 * Date: 2024-06-27
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div style="height: 100%;">
    <div class="paper-height">
      <div style="height: 100%; overflow: scroll;">
        <div class="a4-paper-big-simple">
          <div style="margin: 50px 60px;">
            <el-table
                :data="feeManageList"
                stripe
                style="width: 100%">
              <el-table-column type="expand">
                <template slot-scope="props">
                  <div style="margin-left: 60px;">
                    <div class="ca-plain-form">
                      <div class="ca-form-line">
                        <div class="ca-form-label-xxl">
                          <div class="ca-form-label-title" style="font-size: 13px; color: #99a9bf;">
                            申请单号:
                          </div>
                        </div>
                        <div class="ca-form-content">
                          <a href="javascript:void(0);" @click="openApproveSheet(props.row.code)">{{ props.row.code }}</a>
                        </div>
                      </div>
                      <div class="ca-form-line">
                        <div class="ca-form-label-xxl">
                          <div class="ca-form-label-title" style="font-size: 13px; color: #99a9bf;">
                            申请标题:
                          </div>
                        </div>
                        <div class="ca-form-content">
                          {{ props.row.name }}
                        </div>
                      </div>
                      <div class="ca-form-line">
                        <div class="ca-form-label-xxl">
                          <div class="ca-form-label-title" style="font-size: 13px; color: #99a9bf;">
                            特性名称:
                          </div>
                        </div>
                        <div class="ca-form-content">
                          {{ props.row.characterName }}
                        </div>
                      </div>
                      <div class="ca-form-line">
                        <div class="ca-form-label-xxl">
                          <div class="ca-form-label-title" style="font-size: 13px; color: #99a9bf;">
                            金额:
                          </div>
                        </div>
                        <div class="ca-form-content">
                          {{ props.row.accountCost | rmbFormat }}
                        </div>
                      </div>
                      <div class="ca-form-line">
                        <div class="ca-form-label-xxl">
                          <div class="ca-form-label-title" style="font-size: 13px; color: #99a9bf;">
                            费用类型:
                          </div>
                        </div>
                        <div class="ca-form-content">
                          {{ FEE_TYPE_STD | optionKV(props.row.type) }}
                        </div>
                      </div>
                      <div class="ca-form-line">
                        <div class="ca-form-label-xxl">
                          <div class="ca-form-label-title" style="font-size: 13px; color: #99a9bf;">
                            申请原因:
                          </div>
                        </div>
                        <div class="ca-form-content">
                          {{ props.row.reason }}
                        </div>
                      </div>
                      <div class="ca-form-line">
                        <div class="ca-form-label-xxl">
                          <div class="ca-form-label-title" style="font-size: 13px; color: #99a9bf;">
                            申请人:
                          </div>
                        </div>
                        <div class="ca-form-content">
                          {{ props.row.writerName }}
                        </div>
                      </div>
                      <div class="ca-form-line">
                        <div class="ca-form-label-xxl">
                          <div class="ca-form-label-title" style="font-size: 13px; color: #99a9bf;">
                            审批人:
                          </div>
                        </div>
                        <div class="ca-form-content">
                          {{ props.row.nextNodeName }}
                        </div>
                      </div>
                      <div class="ca-form-line">
                        <div class="ca-form-label-xxl">
                          <div class="ca-form-label-title" style="font-size: 13px; color: #99a9bf;">
                            审批意见:
                          </div>
                        </div>
                        <div class="ca-form-content">
                          {{ props.row.approveDescription }}
                        </div>
                      </div>
                    </div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column
                  prop="code"
                  label="申请单号"
                  width="180">
              </el-table-column>
              <el-table-column
                  prop="type"
                  :formatter="typeFormat"
                  label="使用方式">
              </el-table-column>
              <el-table-column
                  prop="accountCost"
                  :formatter="currencyFormat"
                  label="金额">
              </el-table-column>
              <el-table-column
                  prop="writerName"
                  label="申请人">
              </el-table-column>
              <el-table-column
                  prop="nextNodeName"
                  label="审批人">
              </el-table-column>
            </el-table>
          </div>

        </div>
      </div>
    </div>

    <!--显示费用审批表-->
    <el-dialog
        width="29cm"
        :visible.sync="openApproveSheetDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div>
        <FeeApproveDlg
            :key="applicationCode"
            :application-code="applicationCode"
            :job-type="JOB_ITEM_TYPE.REVIEW.key"
            :doc-type="DOC_TYPE.CHARACTER.key"
            :close-dlg = closeDialog
        ></FeeApproveDlg>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import Swal from "sweetalert2";
import {optionKV, rmbFormat} from "@/filter/filter";
import FeeApproveDlg from "@/components/std/fee-approve-dlg.vue";

export default {
  components: {
    FeeApproveDlg,

  },
  name: "review-fee-page",

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

  computed: {
  },

  data() {
    return {
      loginUser: {},
      loginCustomer: {},
      feeManageList: [],

      FEE_TYPE_STD: FEE_TYPE_STD,
      JOB_ITEM_TYPE: JOB_ITEM_TYPE,
      DOC_TYPE: DOC_TYPE,

      openApproveSheetDlg: false,
      applicationCode: '',
      dialogTitle: '',

    };
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if (Tool.isNotEmpty(_this.jobitemId)) {
      _this.getFeeListByReviewJobitem(_this.jobitemId);
    }
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

    currencyFormat(row, column) {
      if (row.accountCost) {
        return rmbFormat(row.accountCost);
      }
      return 0;
    },
    typeFormat(row, column) {
      return optionKV(FEE_TYPE_STD, row.type);
    },

    getFeeListByReviewJobitem(jobitemId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getFeeListByReviewJobitem/' + jobitemId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.feeManageList = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      });
    },

    openApproveSheet(code) {
      let _this = this;
      _this.applicationCode = code;
      _this.openApproveSheetDlg = true;
      _this.dialogTitle = "领料审批表";
    },
    handleClose(done) {
      let _this = this;
      _this.openApproveSheetDlg = false;
      done();
    },

    closeDialog() {
      let _this = this;
      _this.openApproveSheetDlg = false;
    },
  },
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd';

.paper-height {
  height: calc(100vh - 255px);
}

</style>
