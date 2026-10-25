<!--
 * Author: 周朔鹏
 * Date: 2025-02-25
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div style="position: relative;">
      <el-badge v-show="examineNum>0" :value="examineNum" class="item" style="position: absolute; left: 172px; top: 3px; z-index: 100;"></el-badge>
<!--      <el-badge v-show="intCompleteFlag" :value="1" class="item" style="position: absolute; left: 270px; top: 3px; z-index: 100;"></el-badge>-->
    </div>
    <el-tabs v-model="activeName" type="card" @tab-click="handleClick">

      <!--     项目任务 -->
      <el-tab-pane label="特性测试" name="develop">
        <div v-if="activeName === 'develop'">
          <TestCharacterJobitemIssue
              :key="characterId"
              :character-id="characterId"
              :job-type="jobType"
              :project-type="projectType"
              :index="index"
              :entrance="entrance"
          ></TestCharacterJobitemIssue>
        </div>
      </el-tab-pane>

      <!--     工作审核 -->
      <el-tab-pane label="任务审核" name="examine">
        <div v-if="activeName === 'examine'">
          <TestCharacterJobitemExamine
              ref="examineJobitemList"
              :index="index"
              :key="characterId"
              :character-id="characterId"
              :job-type="jobType"
              :project-type="projectType"
              :entrance="entrance"
          ></TestCharacterJobitemExamine>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import ProjectTaskSubmitContainer from "@/components/rdms/project-task-submit-container.vue";

import ReviewDevelopContainer from "@/components/rdms/review-develop-container.vue";
import JobitemReviewPjmContainer from "@/components/rdms/jobitem-review-pjm-container.vue";
import Swal from "sweetalert2";
import TestCharacterJobitemIssue from "@/components/rdms/test-character-jobitem-issue.vue";
import TestCharacterJobitemExamine from "@/components/rdms/test-character-jobitem-examine.vue";

export default {
  components: {
    JobitemReviewPjmContainer,
    ProjectTaskSubmitContainer,

    ReviewDevelopContainer,
    TestCharacterJobitemIssue,
    TestCharacterJobitemExamine,
  },
  name: "test-character-task-tab",

  props: {
    characterId: {
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

      subProjectId: {
        default: null,
      },

      // intCompleteFlag: false,

      activeName: 'develop',
      evaluateNum: 0,
      examineNum: 0,

    }
  },

  computed: {},

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    // _this.intCompleteFlag = false;
    if(Tool.isNotEmpty(_this.characterId)){
      _this.getExamineNum(_this.characterId);
      // _this.getCharacterDevelopCompleteStatus(_this.characterId);
      // _this.$refs.jobitemList.getCharacterAndJobitemList(_this.characterId);
    }

    _this.$bus.$on('refresh-test-character-task-tab-num', (data) => {
      if(data){
        _this.getExamineNum(_this.characterId);
      }
    });
    // 调用 showIntSubmit 函数
/*    _this.$bus.$on('refresh-test-character-task-tab', (data) => {
      if(data){
        _this.getCharacterDevelopCompleteStatus(_this.characterId);
      }
    });*/

  },

  beforeDestroy(){
    let _this = this;
    _this.$bus.$off('refresh-test-character-task-tab-num');
    _this.$bus.$off('refresh-test-character-task-tab');
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
      if(tab.name === 'develop'){
        // _this.$refs.jobitemList.getCharacterAndJobitemList(_this.characterId);
      }else if(tab.name === 'examine') {
        // _this.$refs.examineJobitemList.getCharacterAndSubmitJobitems(_this.characterId);
      }
    },

    getCharacterDevelopCompleteStatus(characterId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/getCharacterDevelopCompleteStatus/' + characterId).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.intCompleteFlag = resp.content;
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    getExamineNum(characterId){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getExamineNum_character_test/' + characterId).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.examineNum = resp.content;
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
