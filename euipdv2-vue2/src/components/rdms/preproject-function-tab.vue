<!--
 * Author: 周朔鹏
 * Date: 2025-03-01
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div style="position: relative;">
      <el-badge v-show="handlingNum>0" :value="handlingNum" class="item" style="position: absolute; left: 73px; top: 3px; z-index: 100;"></el-badge>
      <el-badge v-show="examineNum>0" :value="examineNum" class="item" style="position: absolute; left: 170px; top: 3px; z-index: 100;"></el-badge>
      <el-badge v-show="evaluateNum>0" :value="evaluateNum" class="item" style="position: absolute; left: 267px; top: 3px; z-index: 100;"></el-badge>
    </div>
    <el-tabs v-model="activeName" type="card" @tab-click="handleClick">
<!--     特性定义 -->
      <el-tab-pane label="特性定义" name="jobitem-testing">
        <div v-if="activeName === 'jobitem-testing'">
          <FunctionPreprojectWriting
              :key="('jobitem-testing-' + (preProjectId))"
              ref="jobitemTestingList"
              :pre-project-id="preProjectId"
              :job-type="jobType"
              :project-type="projectType"
              :index="index"
              :entrance="entrance"
          ></FunctionPreprojectWriting>
        </div>
      </el-tab-pane>
<!--     功能审核 -->
      <el-tab-pane label="特性签审" name="jobitem-evaluate">
        <div v-if="preProject && activeName === 'jobitem-evaluate'">
          <character-joblist-examine
              :key="('jobitem-evaluate-' + (preProject.id))"
              ref="examineList"
              :pre-project="preProject ? preProject : null"
              :project-type="projectType"
              :index="index"
              :doc-type="DOC_TYPE.CHARACTER.key"
              :entrance="entrance"
          ></character-joblist-examine>
        </div>
      </el-tab-pane>
      <!--     待评价 -->
      <el-tab-pane label="质量评价" name="evaluate">
        <div v-if="preProject && activeName === 'evaluate'">
          <CharacterJoblistEvaluate
              :key="('evaluate-' + (preProject.id))"
              ref="evaluateList"
              :pre-project="preProject"
              :project-type="projectType"
          ></CharacterJoblistEvaluate>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import FunctionPreprojectWriting from "@/components/rdms/function-preproject-writing.vue";
import CharacterJoblistExamine from "@/components/rdms/character-joblist-examine.vue";
import CharacterJoblistEvaluate from "@/components/rdms/character-joblist-evaluate.vue";
import CharacterJoblistApproved from "@/components/rdms/character-joblist-approved.vue";

export default {
  components: {
    CharacterJoblistApproved,
    CharacterJoblistEvaluate,
    CharacterJoblistExamine,
    FunctionPreprojectWriting,
  },
  name: "preproject-function-tab",

  props: {
    preProjectId: {
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
      handlingNum: 0,
      examineNum: 0,
      evaluateNum: 0,

      DOC_TYPE: DOC_TYPE,

      preProject: {
        default: null,
      },
    }
  },

  computed: {},

  mounted: function () {
    let _this = this;
    _this.getLoginUser();

    _this.getExamineNum();
    _this.getHandlingNum();
    _this.getEvaluateNum();
    _this.getPreProjectSimpleInfo();

    _this.$bus.$on('refresh-preproject-function-tab-num', (data) => {
      if(data){
        _this.getExamineNum();
        _this.getHandlingNum();
        _this.getEvaluateNum();
      }
    });
  },

  beforeDestroy(){
    let _this = this;
    _this.$bus.$off('refresh-preproject-function-tab-num');
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

    getPreProjectSimpleInfo() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/pre-project/getPreProjectSimpleInfo/'+ _this.preProjectId).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.preProject = resp.content;
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    handleClick(tab, event) {
      let _this = this;
      /*if(tab.name === 'jobitem-testing'){
        // _this.$refs.jobitemTestingList.getJobitemListBySubprojectId(1);
      }else if(tab.name === 'jobitem-evaluate'){
        // _this.$refs.jobitemEvaluateList.getCharacterListAndAllSubmitTestJobitem(1);
      }*/
    },

    getHandlingNum(){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getHandlingByPreProjectId',{
        customerId: _this.loginCustomer.id,
        preProjectId: _this.preProjectId,
        status: JOB_ITEM_STATUS.HANDLING.key,
        userId: _this.loginUser.customerUserId,
        // type: JOB_ITEM_TYPE.DEMAND.key
      }).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.handlingNum = resp.content;
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    getExamineNum(){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getExamineNumByPreProjectId',{
        customerId: _this.loginCustomer.id,
        preProjectId: _this.preProjectId,
        status: JOB_ITEM_STATUS.SUBMIT.key,
        userId: _this.loginUser.customerUserId,
        // type: JOB_ITEM_TYPE.DEMAND.key
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
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getEvaluateNumByPreProjectId',{
        customerId: _this.loginCustomer.id,
        preProjectId: _this.preProjectId,
        status: JOB_ITEM_STATUS.EVALUATE.key,
        userId: _this.loginUser.customerUserId,
        // type: JOB_ITEM_TYPE.DEMAND.key
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
