<!--
 * Author: 周朔鹏
 * Date: 2025-03-04
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="page-container">
    <div class="page-left">
      <div v-if="jobType === JOB_ITEM_TYPE.CHARACTER_INT.key">
        <div style="margin: 10px 15px; font-size: 14px; font-weight: 600;">
          特性物料清单:
          <el-button type="primary" size="small" style="margin-left: 5px;" :disabled="!jobItem.characterId" @click="bomEditDlg = true">BOM编辑</el-button>
          <small style="margin-left: 8px; font-weight: normal;">特性评审前须完成物料清单编制并提交</small>
        </div>
        <CharacterIntJobitemAssistShowPlain
            :key="jobItem.characterId"
            :character-id="jobItem.characterId"
            :entrance="entrance"
            :project-type="projectType"
            :index="index"
        ></CharacterIntJobitemAssistShowPlain>
      </div>
<!--      <div v-else-if="jobType === JOB_ITEM_TYPE.TASK.key">
        <jobitem-review-notify-sheet-plaint
            :key="jobItem.id"
            :job-item-id="jobItem.id"
            :project-type="PROJECT_TYPE.SUB_PROJECT.key"
        ></jobitem-review-notify-sheet-plaint>
      </div>-->
      <div v-else>
        <JobitemInfoAllPlain
            :key="jobitemId"
            :jobitem-id="jobitemId"
            :job-type="jobType"
            :project-type="projectType"
            :index="index"
            :entrance="entrance"
            :en-reject="true"
        ></JobitemInfoAllPlain>
      </div>
    </div>
    <div class="page-right">
      <JobitemTaskSubmitPage
          :key="jobItem.id"
          :jobitem-id="jobItem.id"
          :job-type="jobType"
          :project-type="projectType"
          :index="index"
          :entrance="entrance">
      </JobitemTaskSubmitPage>
    </div>

    <el-dialog
        width="29cm"
        :visible.sync="bomEditDlg"
        append-to-body
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">BOM编辑</div>
      <div v-if="bomEditDlg">
        <BomEditList
            :key="jobItem.characterId"
            :character-id="jobItem.characterId"
            :close-dlg="closeBomEditDlg">
        </BomEditList>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import File from "../../components/rdms/file";
import Swal from "sweetalert2";
import CharacterIntJobitemAssistShowPlain from "@/components/rdms/character-int-jobitem-info-plain.vue";
import JobitemTaskSubmitPage from "@/components/rdms/jobitem-task-submit-page.vue";
import JobitemReviewNotifySheetPlaint from "@/components/rdms/jobitem-review-notify-sheet-plaint.vue";
import JobitemInfoAllPlain from "@/components/std/jobitem-all-info-plain.vue";
import BomEditList from "@/components/std/bom-edit-list.vue";

export default {
  components: {
    BomEditList,
    JobitemInfoAllPlain,
    JobitemReviewNotifySheetPlaint,
    File,
    CharacterIntJobitemAssistShowPlain,
    JobitemTaskSubmitPage,
  },
  name: "jobitem-task-submit-container",

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

      jobItem: {},
      bomEditDlg: false,
      PROJECT_TYPE: PROJECT_TYPE,
      JOB_ITEM_TYPE: JOB_ITEM_TYPE,

    }
  },

  computed: {},

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if(Tool.isNotEmpty(_this.jobitemId)){
      _this.getJobitemDetailInfo(_this.jobitemId);
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

    getJobitemDetailInfo(jobitemId) {
      let _this = this;
      let formData = new window.FormData();
      formData.append('jobitemId', jobitemId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getJobitemDetailInfo', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.jobItem = resp.content;
        }else{
          Toast.warning(resp.message, Swal);
        }
      });
    },

    closeBomEditDlg() {
      this.bomEditDlg = false;
    },

  },
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd.scss';

</style>
