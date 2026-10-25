<!--
 * Author: 周朔鹏
 * Date: 2026-10-14
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="page-container" style="display: flex; flex-direction: column;">
    <div style="padding: 10px 20px; border-bottom: 1px dashed #dcdfe6;">
      <div style="font-size: 14px; font-weight: bold; margin-bottom: 8px;">
        {{ jobItem.jobName }}
        <small style="margin-left: 12px; font-weight: normal;">批准后特性废弃，未结束的开发、测试、集成工单取消，未执行预算退回项目。</small>
      </div>
      <div style="margin-bottom: 8px;">
        <span style="color: #909399;">终止原因：</span>
        <span v-html="jobItem.taskDescription"></span>
      </div>
      <div v-if="canReview" style="display: flex; align-items: center;">
        <el-input v-model="rejectReason" size="small" placeholder="不批准原因（不批准时填写）" style="flex: 1; min-width: 180px; max-width: 360px; margin-right: 12px;"></el-input>
        <el-button type="danger" size="small" @click="approveTerminate">批准终止</el-button>
        <el-button type="warning" size="small" :disabled="!rejectReason" @click="rejectTerminate">不批准</el-button>
      </div>
      <div v-else>
        <small>{{ reviewHint }}</small>
      </div>
    </div>
    <div style="flex: 1; overflow: auto;">
      <CharacterOwnInfoPlain
          v-if="jobItem.characterId"
          :key="'terminate-' + jobItem.characterId"
          :character-id="jobItem.characterId"
          :project-type="projectType"
      ></CharacterOwnInfoPlain>
    </div>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import CharacterOwnInfoPlain from "@/components/rdms/character-own-info-plain.vue";

export default {
  components: {
    CharacterOwnInfoPlain,
  },
  name: "jobitem-character-terminate-container",

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
      jobItem: {},
      rejectReason: '',
    }
  },

  computed: {
    pending() {
      return this.jobItem && this.jobItem.status === JOB_ITEM_STATUS.SUBMIT.key;
    },
    canReview() {
      let roleList = (Tool.getLoginUser() || {}).roleList;
      return this.pending && (this.jobItem.nextNode === this.loginUser.customerUserId
          || (typeof RoleAccess !== 'undefined' && RoleAccess.hasAny(['UltiMng'], roleList)));
    },
    reviewHint() {
      if (this.pending) {
        return '已提交，等待子项目产品经理审批。';
      }
      if (this.jobItem.status === JOB_ITEM_STATUS.COMPLETED.key) {
        return '已批准，特性已终止。';
      }
      return '终止申请未批准。';
    },
  },

  mounted: function () {
    this.getLoginUser();
    this.getJobitemSimpleInfo(this.jobitemId);
  },

  methods: {
    getLoginUser() {
      if (Tool.getLoginUser().userGroup === LOGIN_USER_GROUP.CUSTOMER.key) {
        this.loginUser = Tool.getLoginUser().adminUser;
      } else {
        this.loginUser = Tool.getLoginUser();
      }
    },

    getJobitemSimpleInfo(jobitemId) {
      let _this = this;
      let formData = new window.FormData();
      formData.append('jobitemId', jobitemId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getJobitemSimpleInfo', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.jobItem = resp.content || {};
        } else {
          Toast.warning(resp.message, Swal);
        }
      });
    },

    approveTerminate() {
      let _this = this;
      Swal.fire({
        title: '确认',
        text: "批准后特性废弃且不可恢复，确认批准终止吗?",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#F56C6C',
        cancelButtonColor: '#909399',
        confirmButtonText: '确认!',
        cancelButtonText: '取消!'
      }).then((result) => {
        if (!result.isConfirmed) {
          return;
        }
        _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/terminate/approve/' + _this.jobItem.id).then((response) => {
          let resp = response.data;
          if (resp.success) {
            Toast.success("特性已终止", Swal);
            _this.$bus.publish({ areas: _this.$bus.areasForEntrance(_this.entrance), reason: 'character-terminate-approve' });
            _this.getJobitemSimpleInfo(_this.jobItem.id);
          } else {
            Toast.warning(resp.message, Swal);
          }
        });
      }).catch(_ => {
      });
    },

    rejectTerminate() {
      let _this = this;
      let formData = new window.FormData();
      formData.append('reason', _this.rejectReason);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/terminate/reject/' + _this.jobItem.id, formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          Toast.success("已不批准", Swal);
          _this.rejectReason = '';
          _this.$bus.publish({ areas: _this.$bus.areasForEntrance(_this.entrance), reason: 'character-terminate-reject' });
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
