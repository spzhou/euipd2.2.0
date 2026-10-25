<!--
 * Author: 周朔鹏
 * Date: 2024-11-05
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="page-container">
    <div class="page-left">
        <div style="text-align: right; margin: 0 10px 6px 0;">
          <el-button type="primary" plain size="small" @click="referenceVisible = true">参考资料</el-button>
        </div>
        <CharacterOriginalPlainPage
            :key="jobItem.characterId"
            :jobitem-id="jobitemId"
            :index="index"
            :project-type="projectType"
            :character-id="jobItem.characterId"> <!--这是原始记录的id-->
        </CharacterOriginalPlainPage>
    </div>
    <div class="page-right">
      <JobitemSubmitPropertyPage
          :key="jobitemId"
          :jobitem-id="jobitemId"
          :job-type="jobType"
          :project-type="projectType"
          :index="index"
          :entrance="entrance">
      </JobitemSubmitPropertyPage>
    </div>

    <el-dialog title="参考资料" width="92%" append-to-body :visible.sync="referenceVisible" :close-on-click-modal="false">
      <JobitemTaskReferenceContainer
          v-if="referenceVisible"
          :key="('reference-' + (jobitemId))"
          :jobitem-id="jobitemId"
          :job-type="jobType"
          :project-type="projectType"
          :index="index"
          :entrance="entrance">
      </JobitemTaskReferenceContainer>
    </el-dialog>
  </div>
</template>

<script>
import File from "../../components/rdms/file";
import Swal from "sweetalert2";
import CharacterOriginalPlainPage from "@/components/rdms/character-original-plain-page.vue";
import JobitemSubmitPropertyPage from "@/components/rdms/jobitem-submit-property-page";
import JobitemTaskReferenceContainer from "@/components/rdms/jobitem-task-reference-container.vue";

export default {
  components: {
    File,
    CharacterOriginalPlainPage,
    JobitemSubmitPropertyPage,
    JobitemTaskReferenceContainer,
  },
  name: "jobitem-develop-container",

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

  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},

      jobItem: {
        default: null,
      },
      randomInt: 0,
      referenceVisible: false,
      PROJECT_TYPE: PROJECT_TYPE,
    }
  },

  computed: {},

  mounted: function () {
    let _this = this;
    _this.getLoginUser();

    if(Tool.isNotEmpty(_this.jobitemId)){
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

    refreshIterationCreate(){
      let _this = this;
      _this.randomInt = Math.random();
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
        }else{
          Toast.warning(resp.message, Swal);
        }
      });
    },

  },
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd.scss';

</style>
