<!--
 * Author: 周朔鹏
 * Date: 2025-02-05
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="page-container">
    <div class="page-left">
      <div v-if="jobItem">
        <JobitemAllInfoPlain
            :key="jobItem.id"
            :jobitem-id="jobItem.id"
            :job-type="jobItem.type"
            :project-type="jobItem.projectType"
            :index="0"
            :entrance="entrance"
        ></JobitemAllInfoPlain>
        <div class="hr hr-10 hr-dotted"></div>
        <div v-if="jobItem.characterId">
          <CharacterOwnInfoPlain
              :key="jobItem.characterId"
              :character-id="jobItem.characterId"
          ></CharacterOwnInfoPlain>
        </div>
        <div v-else>
          <div v-if="jobItem.subprojectId">
            <SubprojectOwnInfoPlain
                :key="jobItem.subprojectId"
                :subproject-id="jobItem.subprojectId"
            ></SubprojectOwnInfoPlain>
          </div>

        </div>
      </div>
    </div>
    <div class="page-right">
      <div>
        <QualityAnalysisEdit
            :key="jobitemId"
            :jobitem-id="jobitemId"
            :job-type="jobType"
            :project-type="projectType"
            :index="index"
            :entrance="entrance">
        </QualityAnalysisEdit>
      </div>
    </div>
  </div>
</template>

<script>
import File from "../../components/rdms/file";
import Swal from "sweetalert2";
import QualityAnalysisEdit from "@/components/rdms/quality-analysis-edit.vue";
import JobitemAllInfoPlain from "@/components/std/jobitem-all-info-plain.vue";
import CharacterOwnInfoPlain from "@/components/rdms/character-own-info-plain.vue";
import SubprojectOwnInfoPlain from "@/components/rdms/subproject-own-info-plain.vue";

export default {
  components: {
    CharacterOwnInfoPlain,
    SubprojectOwnInfoPlain,
    File,
    QualityAnalysisEdit,
    JobitemAllInfoPlain,
  },
  name: "jobitem-quality-analysis-container",

  props: {
    jobitemId: {  //修订任务号
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
      originCharacterId: '',
      JOB_ITEM_TYPE: JOB_ITEM_TYPE,
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
