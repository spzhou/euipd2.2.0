<!--
 * Author: 周朔鹏
 * Date: 2025-02-25
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div style="position: relative;">
      <el-badge v-show="handlingNum>0" :value="handlingNum" class="item" style="position: absolute; left: 73px; top: 3px; z-index: 100;"></el-badge>
      <el-badge v-show="evaluateNum>0" :value="evaluateNum" class="item" style="position: absolute; left: 170px; top: 3px; z-index: 100;"></el-badge>
    </div>
    <el-tabs v-model="activeName" type="card" @tab-click="handleClick">

<!--     在测任务 -->
      <el-tab-pane label="在测任务" name="jobitem-testing">
        <div v-if="activeName === 'jobitem-testing'">
          <test-joblist-testing
              :key="('jobitem-testing-' + (subProjectId))"
              ref="jobitemTestingList"
              :sub-project-id="subProjectId"
              :job-type="jobType"
              :project-type="projectType"
              :index="index"
              :entrance="entrance"
          ></test-joblist-testing>
        </div>
      </el-tab-pane>

<!--     任务审核 -->
      <el-tab-pane label="任务审核" name="jobitem-evaluate">
        <div v-if="activeName === 'jobitem-evaluate'">
          <test-joblist-evaluate
              :key="('jobitem-evaluate-' + (subProjectId))"
              ref="jobitemEvaluateList"
              :sub-project-id="subProjectId"
              :job-type="jobType"
              :project-type="projectType"
              :index="index"
              :entrance="entrance"
          ></test-joblist-evaluate>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import TestJoblistTesting from "@/components/rdms/test-joblist-testing.vue";
import TestJoblistEvaluate from "@/components/rdms/test-joblist-evaluate.vue";

export default {
  components: {
    TestJoblistTesting,
    TestJoblistEvaluate,
  },
  name: "test-joblist-tab",

  props: {
    subProjectId: {
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

  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},

      activeName: 'jobitem-evaluate',
      evaluateNum: 0,
      handlingNum: 0,

      preProject: {
        default: null,
      },
    }
  },

  computed: {},

  mounted: function () {
    let _this = this;
    _this.getLoginUser();

    _this.getEvaluateNum();
    _this.getHandlingNum();

    _this.$bus.$on('refresh-test-joblist-tab-num', (data) => {
      if(data){
        _this.getEvaluateNum();
        _this.getHandlingNum();
      }
    });
  },

  beforeDestroy(){
    let _this = this;
    _this.$bus.$off('refresh-test-joblist-tab-num');
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

    handleClick(tab, event) {
      let _this = this;
      if(tab.name === 'jobitem-testing'){
        // _this.$refs.jobitemTestingList.getJobitemListBySubprojectId(1);
      }else if(tab.name === 'jobitem-evaluate'){
        // _this.$refs.jobitemEvaluateList.getCharacterListAndAllSubmitTestJobitem(1);
      }
    },

    getHandlingNum(){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getEvaluateNumBySubprojectId',{
        customerId: _this.loginCustomer.id,
        subprojectId: _this.subProjectId,
        status: JOB_ITEM_STATUS.HANDLING.key,
        type: JOB_ITEM_TYPE.TEST.key
      }).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.handlingNum = resp.content;
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    getEvaluateNum(){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getEvaluateNumBySubprojectId',{
        customerId: _this.loginCustomer.id,
        subprojectId: _this.subProjectId,
        status: JOB_ITEM_STATUS.SUBMIT.key,
        type: JOB_ITEM_TYPE.TEST.key
      }).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.evaluateNum = resp.content;
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

  },
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd.scss';

</style>
