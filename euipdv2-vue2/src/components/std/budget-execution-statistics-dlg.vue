<!--
 * Author: 周朔鹏
 * Date: 2024-11-07
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div class="dialog-container">
      <div style="width: 100%;">
        <budgetDirectStatisticsPage
            :key="((xProjectId) + '-' + (projectType))"
            :x-project-id="xProjectId"
            :project-type="projectType"
        ></budgetDirectStatisticsPage>
      </div>
      <div class="clearfix form-actions">
        <div style="display: flex; justify-content: center;">
<!--          <button type="button" v-on:click="saveBudgetList()" class="ca-btn-primary">提 交</button>-->
          <button type="button" v-on:click="close()" class="ca-btn-primary">关 闭</button>
        </div>
      </div>
    </div>

  </div>
</template>

<script>
import Swal from "sweetalert2";
import budgetDirectStatisticsPage from "@/components/std/budget-direct-statistics-page.vue";

export default {
  components: {
    budgetDirectStatisticsPage,
  },
  name: "budget-execution-statistics-dlg",

  props: {
    jobitemId: {
      default: null
    },
    projectType: {
      default: PROJECT_TYPE.SUB_PROJECT.key
    },
    closeDlg: {
      type: Function,
      default: null
    },
  },

  data() {
    return {
      loginUser: {},
      loginCustomer: {},
      jobItem: {},
      PROJECT_TYPE: PROJECT_TYPE,
      xProjectId: '',
    };
  },

  computed: {
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if(Tool.isNotEmpty(_this.jobitemId)){
      _this.getJobitemSimpleInfo(_this.jobitemId);
    }
  },

  methods: {
    getLoginUser(){
      let _this = this;
      if(Tool.getLoginUser().userGroup === LOGIN_USER_GROUP.CUSTOMER.key){
        _this.loginUser = Tool.getLoginUser().adminUser;
        _this.loginCustomer =Tool.getLoginUser();
      }else{
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
          if(_this.projectType === PROJECT_TYPE.SUB_PROJECT.key){
            _this.xProjectId = _this.jobItem.subprojectId;
          }else{
            _this.xProjectId = _this.jobItem.projectId;
          }
        }else{
          Toast.warning(resp.message, Swal);
        }
      });
    },

    /**
     * 提交申请
     */
/*    saveBudgetList() {
      let _this = this;
      _this.budgetExecutionList.forEach((item, index)=>{
        item.customerId = _this.jobItem.customerId;
        item.projectId = _this.jobItem.projectId;
        // item.parentId = ;  //后台程序填写
        item.subprojectId = _this.jobItem.subprojectId;
      })

      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/budget-direct-exe/saveBudgetExeList', _this.budgetExecutionList).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.$bus.$emit('refresh-jobitem-submit-property-page-subBudget', true);
          _this.close();
          Toast.success("保存成功", Swal);
        }
      })
    },*/

    close(){
      let _this = this;
      _this.closeDlg();
    },

  },
}
</script>

<style lang="scss" scoped>
@import '../../../public/static/scss/caipd';

.text-box{
  display: inline-block;
  padding-left: 3px;
  padding-right: 15px;
  line-height: 30px;
  //width: 100px;
  background-color: rgba(204, 204, 204, 0.20);
}
.dialog-width{
  width: 100%;
}
.red-color{
  color: red;
}
</style>
