<!--
 * Author: 周朔鹏
 * Date: 2025-02-26
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
                :data="materialManageList"
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
                            使用方式:
                          </div>
                        </div>
                        <div class="ca-form-content">
                          {{ MATERIAL_USAGE_MODE | optionKV(props.row.usageMode) }}
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
                  prop="usageMode"
                  :formatter="usageModeFormat"
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

    <!--显示任务过程信息-->
    <el-dialog
        width="29cm"
        :visible.sync="openApproveSheetDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
    <!--用于显示物料信息-->
      <div v-if="openApproveSheetDlg">
        <MaterialApproveDlg
            :key="applicationCode"
            :application-code="applicationCode"
            :job-type="JOB_ITEM_TYPE.REVIEW.key"
            :doc-type="DOC_TYPE.SUB_PROJECT.key"
            :close-dlg = closeDialog
        ></MaterialApproveDlg>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import Swal from "sweetalert2";
import {optionKV, rmbFormat} from "@/filter/filter";
import MaterialApproveDlg from "@/components/std/material-approve-dlg.vue";

export default {
  components: {
    MaterialApproveDlg,

  },
  name: "review-material-page",

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
    getJobDescriptionFontNum() {
      if (Tool.isNotEmpty(this.jobitemProperty.jobDescription)) {
        return Tool.richTextLength(this.jobitemProperty.jobDescription);
      }
      return 0;
    },
  },

  data() {
    return {
      loginUser: {},
      loginCustomer: {},
      materialManageList: [],

      MATERIAL_USAGE_MODE: MATERIAL_USAGE_MODE,
      JOB_ITEM_TYPE: JOB_ITEM_TYPE,
      DOC_TYPE: DOC_TYPE,

      openApproveSheetDlg: false,
      applicationCode: '',
      dialogTitle: '',

      jobItem: {},
      jobitemProperty: {
        jobDescription: '',
      },
    };
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if (Tool.isNotEmpty(_this.jobitemId)) {
      _this.getMaterialListByReviewJobitem(_this.jobitemId);
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

    typeFormat(row, column) {
      return optionKV(JOB_ITEM_TYPE, row.type);
    },
    currencyFormat(row, column) {
      if (row.accountCost) {
        return rmbFormat(row.accountCost);
      }
      return 0;
    },
    usageModeFormat(row, column) {
      return optionKV(MATERIAL_USAGE_MODE, row.usageMode);
    },

    getMaterialListByReviewJobitem(jobitemId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getMaterialListByReviewJobitem/' + jobitemId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.materialManageList = resp.content;
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
