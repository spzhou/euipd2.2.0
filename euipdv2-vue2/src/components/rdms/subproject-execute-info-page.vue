<!--
 * Author: 周朔鹏
 * Date: 2024-04-02
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="paper-height">
    <div style="height: 100%; overflow: scroll;">
      <div class="a4-paper-big-simple">
        <div style="padding: 20px 20px; margin: 30px;">
          <div>
            <p>
              <i class="el-icon-s-opportunity" style="color: red;"></i> 项目/子项目评审的条件是
            </p>
            <p style="margin-left: 30px;">
              1. 项目/子项目项下,所有的特性开发全部完成;
            </p>
            <p style="margin-left: 30px;">
              2. 项目/子项目项下,所有的任务任务全部完成;
            </p>
            <p style="margin-left: 30px;">
              3. 项目/子项目项下,所有下一级子项目开发全部完成;
            </p>
          </div>
          <div class="hr hr-10 dotted"></div>
          <div>
            <SubprojectShowSummary
                :key="projectInfo.id"
                :sub-project="projectInfo"
                :entrance="entrance"
            ></SubprojectShowSummary>
          </div>
          <div class="hr hr-10 dotted hr-double"></div>
          <div>
            <SubprojectExecuteInfoCore
                :key="projectInfo.id"
                :sub-project-id="projectInfo.id"
                :entrance="entrance"
            ></SubprojectExecuteInfoCore>
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
import SubprojectExecuteInfoCore from "@/components/rdms/subproject-execute-info-core.vue";

export default {
  components: {
    SubprojectExecuteInfoCore,
    ProjectShowSummary,
    ReviewJobitemShowPlain,
    SubprojectShowSummary,
    FileRdms,
  },
  name: "subproject-execute-info-page",

  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},

      projectInfo: {},

      PROJECT_TYPE: PROJECT_TYPE,
    }
  },

  props: {
    subProjectId: {
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
    if(Tool.isNotEmpty(_this.projectType) && Tool.isNotEmpty(_this.subProjectId)){
      if(_this.projectType === PROJECT_TYPE.SUB_PROJECT.key){
        _this.getSubProjectDetailInfo(_this.subProjectId);
      }
    }
  },

  methods: {
    getSubProjectDetailInfo(subprojectId){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/sub-project/getSubProjectDetailInfo/' + subprojectId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.projectInfo = resp.content;
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
