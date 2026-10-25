<!--
 * Author: 周朔鹏
 * Date: 2024-11-03
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="paper-height">
    <div style="height: 100%; overflow: scroll;">
      <div class="a4-paper-big-simple">
        <div style="padding: 20px 20px">
          <div v-if="projectType === PROJECT_TYPE.PROJECT.key">
            <ProjectShowSummary
                :key="projectInfo.id"
                :project="projectInfo"
                :setup-stage="SETUP_STAGE.SETUPED.key"
            ></ProjectShowSummary>
          </div>
          <div v-if="projectType === PROJECT_TYPE.SUB_PROJECT.key">
            <SubprojectShowSummary
                :key="projectInfo.id"
                :sub-project="projectInfo"
                :entrance="entrance"
            ></SubprojectShowSummary>
          </div>

          <!--评审信息-->
          <div class="hr hr-10 dotted hr-double"></div>
          <div v-if="projectType === PROJECT_TYPE.PROJECT.key">
            <div v-for="(item, index) in reviewJobitemList" :key="index">
              <div class="hr hr-10 dotted"></div>
              <ReviewJobitemShowPlain
                  :key="item.id"
                  :review="item"
              ></ReviewJobitemShowPlain>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import FileRdms from "@/components/rdms/file-rdms";
import Swal from "sweetalert2";
import {dateFormat, fixed2fractional, optionKV, rmbFormat} from "@/filter/filter";
import SubprojectShowSummary from "@/components/rdms/subproject-show-summary.vue";
import ReviewJobitemShowPlain from "@/components/std/review-jobitem-show-plain.vue";
import ProjectShowSummary from "@/components/rdms/project-show-summary.vue";

export default {
  components: {
    ProjectShowSummary,
    ReviewJobitemShowPlain,
    SubprojectShowSummary,
    FileRdms,
  },
  name: "mix-project-detail-info-page",

  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},

      projectInfo: {},
      reviewJobitemList: [],

      PROJECT_TYPE: PROJECT_TYPE,
      SETUP_STAGE: SETUP_STAGE,
    }
  },

  props: {
    mixProjectId: {
      default: null,
    },
    projectType: {
      default: null
    },
    entrance: {
      default: null,
    }
  },

  computed: {},

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if(Tool.isNotEmpty(_this.projectType) && Tool.isNotEmpty(_this.mixProjectId)){
      if(_this.projectType === PROJECT_TYPE.PROJECT.key){
        _this.getProjectDetailInfo(_this.mixProjectId);
      }
      if(_this.projectType === PROJECT_TYPE.SUB_PROJECT.key){
        _this.getSubProjectDetailInfo(_this.mixProjectId);
      }
    }
  },

  methods: {
    dateFormat,
    optionKV,
    rmbFormat,
    fixed2fractional,
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

    getProjectDetailInfo(projectId){
      let _this = this;
      let formData = new window.FormData();
      formData.append('projectId', projectId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/getProjectDetailInfo', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.projectInfo = resp.content;
          _this.reviewJobitemList = _this.projectInfo.reviewJobDetail;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    getSubProjectDetailInfo(subprojectId){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/sub-project/getSubProjectDetailInfo/' + subprojectId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.projectInfo = resp.content;
          _this.reviewJobitemList = _this.projectInfo.reviewJobDetail;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },
  }
}
</script>
<style>
.line-content{
  padding-top: 3px;
}
</style>

<style lang="scss">
@import './../../../public/static/scss/caipd.scss';
.paper-height {
  height: calc(100vh - 240px);
}

.ca-form{
  font-size: 14px;
  .ca-form-item{
    display: flex;
    flex-direction: row;
    justify-content: left;
    line-height: 25px;
    .ca-form-label{
      width: 100px;
      padding-right: 10px;
      display: flex;
      justify-content: right;
    }
    .ca-form-content{
      width: 820px;
      text-align: justify;
    }
  }
}
</style>
