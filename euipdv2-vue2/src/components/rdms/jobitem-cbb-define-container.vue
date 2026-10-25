<!--
 * Author: 周朔鹏
 * Date: 2025-04-15
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="page-container">
    <div class="page-left">
      <ReviewSubprojectInfoPlain
          :key="jobItem.subprojectId"
          :sub-project-id="jobItem.subprojectId"
          :review-jobitem-id="jobItem.id"
          :disable-application="true"
      ></ReviewSubprojectInfoPlain>
    </div>
    <div v-if="reviewMode" class="page-right" style="display: flex; flex-direction: column;">
      <div style="padding: 10px 20px; border-bottom: 1px dashed #dcdfe6;">
        <div style="font-size: 14px; font-weight: bold; margin-bottom: 8px;">
          CBB 定义审核
          <small style="margin-left: 12px; font-weight: normal;">通过后发布集成级 CBB 到特性仓库；驳回后由执行人继续修改。</small>
        </div>
        <div v-if="canReview" style="display: flex; align-items: center;">
          <el-input v-model="rejectReason" size="small" placeholder="驳回原因（驳回时填写）" style="flex: 1; min-width: 180px; max-width: 360px; margin-right: 12px;"></el-input>
          <el-button type="primary" size="small" @click="approveDefine">审核通过</el-button>
          <el-button type="warning" size="small" :disabled="!rejectReason" @click="rejectDefine">驳回修改</el-button>
        </div>
        <div v-else>
          <small>已提交，等待子项目产品经理审核。</small>
        </div>
      </div>
      <div style="flex: 1; overflow: auto;">
        <CharacterOwnInfoPlain
            v-if="jobItem.characterId"
            :key="'define-' + jobItem.characterId"
            :character-id="jobItem.characterId"
            :project-type="projectType"
        ></CharacterOwnInfoPlain>
      </div>
    </div>
    <div v-else class="page-right">
      <CbbDefineCreate
          :key="jobItem.id + randomInt"
          :jobitem-id="jobItem.id"
          :job-type="jobType"
          :project-type="projectType"
          :index="index"
          :entrance="entrance"
          :refresh="refreshDecomposeCreate">
      </CbbDefineCreate>
    </div>
  </div>
</template>

<script>
import File from "../../components/rdms/file";
import DemandDetailOriginalShowPlain from "@/components/rdms/demand-detail-original-info-plain.vue";
import CharacterDemandCreate from "@/components/rdms/character-demand-create";
import ReviewSubprojectInfoPlain from "@/components/rdms/review-subproject-info-plain.vue";
import Swal from "sweetalert2";
import CbbDefineCreate from "@/components/rdms/cbb-define-create.vue";
import CharacterOwnInfoPlain from "@/components/rdms/character-own-info-plain.vue";

export default {
  components: {
    ReviewSubprojectInfoPlain,
    File,
    DemandDetailOriginalShowPlain,
    CharacterDemandCreate,
    CbbDefineCreate,
    CharacterOwnInfoPlain,
  },
  name: "jobitem-cbb-define-container",

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

      randomInt: 0,
      ENTRANCE_CLASS: ENTRANCE_CLASS,
      jobItem: {
        default: null,
      },
      rejectReason: '',
    }
  },

  computed: {
    reviewMode() {
      return this.jobItem && this.jobItem.status === JOB_ITEM_STATUS.SUBMIT.key;
    },
    canReview() {
      let roleList = (Tool.getLoginUser() || {}).roleList;
      return this.jobItem.nextNode === this.loginUser.customerUserId
          || (typeof RoleAccess !== 'undefined' && RoleAccess.hasAny(['UltiMng'], roleList));
    },
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    _this.getJobitemSimpleInfo(_this.jobitemId);
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

    refreshDecomposeCreate(){
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

    approveDefine() {
      let _this = this;
      Swal.fire({
        title: '确认',
        text: "审核通过后生成 CBB 并发布到特性仓库，确认通过吗?",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#409EFF',
        cancelButtonColor: '#909399',
        confirmButtonText: '确认!',
        cancelButtonText: '取消!'
      }).then((result) => {
        if (result.isConfirmed) {
          _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/cbb/define/approve/' + _this.jobItem.id).then((response) => {
            let resp = response.data;
            if (resp.success) {
              Toast.success("审核通过", Swal);
              _this.$bus.publish({ areas: _this.$bus.areasForEntrance(_this.entrance), reason: 'cbb-define-approve' });
              _this.getJobitemSimpleInfo(_this.jobItem.id);
            } else {
              Toast.warning(resp.message, Swal);
            }
          });
        }
      }).catch(_ => {
      });
    },

    rejectDefine() {
      let _this = this;
      let formData = new window.FormData();
      formData.append('reason', _this.rejectReason);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/cbb/define/reject/' + _this.jobItem.id, formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          Toast.success("已驳回", Swal);
          _this.rejectReason = '';
          _this.$bus.publish({ areas: _this.$bus.areasForEntrance(_this.entrance), reason: 'cbb-define-reject' });
          _this.getJobitemSimpleInfo(_this.jobItem.id);
        } else {
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
