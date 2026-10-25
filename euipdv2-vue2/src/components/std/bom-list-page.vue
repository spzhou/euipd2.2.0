<!--
 * Author: 周朔鹏
 * Date: 2024-11-05
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div style="height: 100%;">
    <div class="paper-height">
      <div style="height: 100%; overflow: scroll;">
        <div class="a4-paper-big-simple">
          <BomEditListSubprojectDlg
              :key="subprojectId"
              :subproject-id="subprojectId"
              :read-only="true"
              :close-dlg="closeDialog">
          </BomEditListSubprojectDlg>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import { rmbFormat} from "@/filter/filter";
import BomEditListSubprojectDlg from "@/components/std/bom-edit-list-subproject-dlg.vue";

export default {
  components: {
    BomEditListSubprojectDlg

  },
  name: "bom-list-page",

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
      jobItem: {},
      subprojectId: '',

      allCharacterList: [],
      characterItem: {},
      characterArchiveVisible: false,
      characterReviewInfoDlg: false,
      dialogTitle: '',


      characterExeInfo: {},

    };
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if (Tool.isNotEmpty(_this.jobitemId)) {
      _this.getJobitemSimpleInfo(_this.jobitemId);
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

    getJobitemSimpleInfo(jobitemId){
      let _this = this;
      let formData = new window.FormData();
      formData.append('jobitemId', jobitemId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getJobitemSimpleInfo', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.jobItem = resp.content;
          _this.subprojectId = _this.jobItem.subprojectId;
        }else{
          Toast.warning(resp.message, Swal);
        }
      });
    },


    sumBudgetApprovedFormat(row, column){
      return rmbFormat(row.sumBudgetApproved);
    },

    sumBudgetActiveFormat(row, column){
      return rmbFormat(row.sumBudgetActive);
    },

    materialFeeActiveFormat(row, column){
      return rmbFormat(row.materialFeeActive);
    },

    otherFeeActiveFormat(row, column){
      return rmbFormat(row.otherFeeActive);
    },

    budgetRateFormat(row, column){
      return (row.budgetRate * 100).toFixed(1) + "%";
    },

    showArchiveDialog(row, column, event) {
      let _this = this;
      _this.characterItem = row;
      _this.characterArchiveVisible = true;
    },


    rowClick(row, column, e){
      let _this = this;
      _this.characterExeInfo = row;
      _this.characterReviewInfoDlg = true;
      _this.dialogTitle = "特性文档汇总";
    },

    handleClose(done) {
      let _this = this;
      _this.characterReviewInfoDlg = false;
      _this.characterArchiveVisible = false;
      done();
    },

    closeDialog() {
      let _this = this;
      _this.characterReviewInfoDlg = false;
      _this.characterArchiveVisible = false;
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
