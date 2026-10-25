<!--
 * Author: 周朔鹏
 * Date: 2025-02-25
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div style="position: relative;">
      <el-badge v-show="examineNum>0" :value="examineNum" class="item" style="position: absolute; left: 180px; top: 3px; z-index: 100;"></el-badge>
      <el-badge v-show="evaluateNum>0" :value="evaluateNum" class="item" style="position: absolute; left: 260px; top: 3px; z-index: 100;"></el-badge>
    </div>
    <el-tabs v-model="activeName" type="card" @tab-click="handleClick">
<!--      编辑中-->
      <el-tab-pane label="已发任务" name="edit">
        <div v-if="activeName === 'edit'">
          <SystemHandlingContainer
              :key="('handling-' + (xProjectId))"
              ref="handlingList"
              :x-project-id="xProjectId"
              :project-type="projectType"
              :index="index"
              :entrance="entrance"
          ></SystemHandlingContainer>
        </div>
      </el-tab-pane>

<!--     待签审 -->
      <el-tab-pane label="特性签审" name="examine">
        <div v-if="activeName === 'examine'">
          <SystemExamineContainer
              :key="('examine-' + (xProjectId))"
              ref="examineList"
              :x-project-id="xProjectId"
              :project-type="projectType"
              :index="index"
              :entrance="entrance"
          ></SystemExamineContainer>
        </div>
      </el-tab-pane>

<!--     待评价 -->
      <el-tab-pane label="作业评价" name="evaluate">
        <div v-if="activeName === 'evaluate'">
          <SystemEvaluateContainer
              :key="('evaluate-' + (xProjectId))"
              ref="evaluateList"
              :x-project-id="xProjectId"
              :project-type="projectType"
              :index="index"
              :entrance="entrance"
          ></SystemEvaluateContainer>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import SystemHandlingContainer from "@/components/rdms/system-handling-container.vue";
import SystemExamineContainer from "@/components/rdms/system-examine-container.vue";
import SystemEvaluateContainer from "@/components/rdms/system-evaluate-container.vue";
import Swal from "sweetalert2";

export default {
  components: {
    SystemHandlingContainer,
    SystemExamineContainer,
    SystemEvaluateContainer,
  },
  name: "project-system-tab",

  props: {
    xProjectId: {
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

      activeName: 'examine',
      evaluateNum: 0,
      examineNum: 0,

    }
  },

  computed: {},

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    _this.getEvaluateNum();
    _this.getExamineNum();

    _this.$bus.$on('refresh-system-joblist-tab-num', (data) => {
      if(data){
        _this.getEvaluateNum();
        _this.getExamineNum();
      }
    });
  },

  beforeDestroy(){
    let _this = this;
    _this.$bus.$off('refresh-system-joblist-tab-num');
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
      if(tab.name === 'edit'){
        // _this.$refs.handlingList.getJobitemHandlingList(1);
      }else if(tab.name === 'examine'){
        _this.$refs.examineList.getJobitemExamineList_subproject(1);
      }else if(tab.name === 'evaluate'){
        // _this.$refs.evaluateList.getJobitemEvaluateList(1);
      }
    },

    getExamineNum(){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/get-system-examine-items-num',{
        customerId: _this.loginCustomer.id,
        projectId: _this.xProjectId,
        customerUserId: _this.loginUser.customerUserId,
      }).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.examineNum = resp.content;
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    getEvaluateNum(){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/get-system-evaluate-items-num',{
        customerId: _this.loginCustomer.id,
        projectId: _this.xProjectId,
        customerUserId: _this.loginUser.customerUserId,
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
