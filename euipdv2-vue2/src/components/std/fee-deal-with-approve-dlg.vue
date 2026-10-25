<!--
 * Author: 周朔鹏
 * Date: 2025-02-28
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div id="container" ref="printRef">
      <div class="a4-paper-print">
        <div class="ca-form">
          <div class="ca-form-header">
            <div class="ca-form-header-line">
              <div class="ca-form-header-line-label">
                <span>申请单号:</span>
              </div>
              <div class="ca-form-header-line-content" style="margin-left: 8px;">
                {{ dialogInfo.code }}
              </div>
            </div>
          </div>
          <div class="ca-form-title">
            <div class="ca-form-title-line">
              费用处理审批单
            </div>
            <div class="hr hr-6 dotted"></div>
          </div>
          <div class="ca-form-content">
            <div class="ca-form-content-line">
              <div class="ca-form-content-line-label">
                费用金额:
              </div>
              <div class="ca-form-content-line-content">
                {{ dialogInfo.accountCost | rmbFormat }}
              </div>
            </div>
            <div class="ca-form-content-line">
              <div class="ca-form-content-line-label">
                费用类型:
              </div>
              <div class="ca-form-content-line-content">
                {{ FEE_TYPE_STD | optionKV(dialogInfo.type) }}
              </div>
            </div>
            <div class="ca-form-content-line">
              <div class="ca-form-content-line-label">
                领用事由:
              </div>
              <div class="ca-form-content-line-content">
                {{ dialogInfo.reason }}
              </div>
            </div>
            <div class="ca-form-content-line">
              <div class="ca-form-content-line-label">
                审批意见:
              </div>
              <div class="ca-form-content-line-content">
                {{ dialogInfo.approveDescription }}
              </div>
            </div>
          </div>
          <div class="hr hr-6 dotted"></div>
          <div class="ca-form-footer">
              <div class="ca-form-footer-line">
                <div style="margin-left: 100px; width: 70px; display: inline-block;">
                  审批人:
                </div>
                <div style="margin-left: 5px; width: 80px; display: inline-block;">
                  {{ dialogInfo.writerName }}
                </div>
                <div style="margin-left: 100px; width: 80px; display: inline-block;">
                  审批时间:
                </div>
                <div style="margin-left: 5px;width: 180px; display: inline-block;">
                  {{ dialogInfo.approveTime | dateFormat }}
                </div>
              </div>
          </div>
          <div class="hr hr-6 dotted hr-double"></div>
          <div>
            <div>
              <div style="line-height: 28px;">
                费用审批表扫描件:
              </div>
              <div v-for="(item, index) in dialogInfo.approveFileList" :key="index" style="line-height: 25px; margin-left: 60px;">
                《{{ item.name }}》
              </div>
            </div>
            <div>
              <div style="line-height: 28px;">
                费用处理相关票据或证明文件:
              </div>
              <div v-for="(item, index) in dialogInfo.dealWithFileList" :key="index" style="line-height: 25px; margin-left: 60px;">
                《{{ item.name }}》
              </div>
            </div>
          </div>
          <div class="hr hr-6 dotted hr-double"></div>
          <div style="margin-top: 30px; margin-bottom: 5px;">
            使用说明: 请自行打印此审批单, 连同相关票据或证明文件原件, 提交财务部门进行报销或核销.
          </div>
          <ApproveSignatureCore
              :serial-no="1"
          ></ApproveSignatureCore>
          <ApproveSignatureCore
              :serial-no="2"
          ></ApproveSignatureCore>
          <ApproveSignatureCore
              :serial-no="3"
          ></ApproveSignatureCore>
          <div class="hr hr-18 dotted hr-double"></div>
          <div id="no-print" class="no-print">
            <div style="display: flex; flex-direction: row; justify-content: end; margin-top: 30px;padding-bottom: 20px;">
              <button type="button" class="ca-btn-primary-sm" @click="toPrint()">打印</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import {dateFormat, optionKV, rmbFormat} from "@/filter/filter";
import MaterialAddDlg from "@/components/std/material-add-dlg.vue";
import ApproveSignatureCore from "@/components/rdms/approve-signature-core.vue";

export default {
  components: {
    MaterialAddDlg,
    ApproveSignatureCore,
  },
  name: "fee-deal-with-approve-dlg",

  props: {
    applicationCode: {
      default: null
    },
    closeDlg: {
      type: Function,
      default: null
    },
  },

  data() {
    return {
      dialogInfo: {
        code: '',
      },
      MATERIAL_USAGE_MODE: MATERIAL_USAGE_MODE,
      APPLICATION_STATUS: APPLICATION_STATUS,

      loginUser: {},
      loginCustomer: {},
      jobItem: {},

      FEE_TYPE_STD: FEE_TYPE_STD,
    };
  },

  computed: {
    getNowTime() {
      return dateFormat(new Date());
    },
  },

  mounted: function () {
    let _this = this;

    _this.getLoginUser();
    if (Tool.isNotEmpty(_this.applicationCode)) {
      _this.getFeeApplicationByCode(_this.applicationCode);
    }
  },

  methods: {
    toPrint () {
      let _this = this;
      _this.$print(this.$refs.printRef);
      _this.$bus.$emit('refresh-fee-application-list', true);
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

    getFeeApplicationByCode(code) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/fee/getFeeApplicationByCode/' + code).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.dialogInfo = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      });
    },

    close() {
      let _this = this;
      _this.closeDlg();
    },
  },
}
</script>

<style lang="scss" scoped>
@import '../../../public/static/scss/caipd';

</style>
