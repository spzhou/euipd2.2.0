<!--
 * Author: 周朔鹏
 * Date: 2024-11-22
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div style="height: 100%;">
    <div class="paper-height">
      <div style="height: 100%; overflow: scroll;">
        <div class="a4-paper-big-simple">
          <div style="margin: 30px 0 0;">
            <el-row style="margin-top: 10px; margin-left: 55px; padding-right: 20px;">
              <el-col :span="23">
                <div style="display: flex; justify-content: space-between;">
                  <div>任务号: {{ jobItem.jobSerial }}</div>
                </div>
              </el-col>
            </el-row>
            <div>
              <table class="table-out">
                <tr>
                  <table  class="table-head">
                    <tr style="border-bottom: 1px solid #1a1a1a;">
                      <th style="width: 100%; text-align: center; font-size: 16px; font-weight: bold;">
                        <span>{{ jobItem.jobName }}</span>
                      </th>
                    </tr>
                  </table>
                </tr>
                <tr>
                  <table class="table-inn">
                    <tr style="border-top: 1px solid #1a1a1a;">
                      <th style="text-align: center;">
                        <span>通知内容</span>
                      </th>
                      <th style="width: 100px; text-align: center;">
                        通知时间
                      </th>
                      <th style="width: 160px; text-align: center;">
                          {{ jobItem.createTime | dateFormat }}
                      </th>
                    </tr>
                  </table>
                </tr>
                <tr>
                  <table class="table-inn">
                    <tr>
                      <th>
                        <div style="width: 880px; padding: 0 8px; min-height: 50px; word-wrap:break-word; font-size: 14px; font-weight: 400;" v-rich-html="jobItem.taskDescription ? jobItem.taskDescription : '无内容'"></div>
                      </th>
                    </tr>
                  </table>
                </tr>
              </table>
            </div>
            <div>
              <div class="clearfix form-actions" style="margin-top: 50px;">
                <div style="display: flex; justify-content: center;">
                  <el-tooltip content="确定后, 将不再提示!" placement="top" effect="light" :open-delay=1000>
                    <button type="button" v-on:click="confirmNotify()" class="ca-btn-primary">确定</button>
                  </el-tooltip>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import CharacterBudgetConfirmDlg from "@/components/rdms/character-budget-confirm-dlg";
import JobitemDetailInfoDlg from "@/components/std/jobitem-detail-info-dlg.vue";


export default {
  components: {
    CharacterBudgetConfirmDlg,
    JobitemDetailInfoDlg,

  },
  name: "budget-adjust-notify-sheet-page",

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
    confirmPath: {
      type: String,
      default: '/customer/job-items/confirmBudgetAdjustNotify/',
    },
  },

  computed: {
  },

  data() {
    return {
      loginUser: {},
      loginCustomer: {},

      jobItem: {},
    };
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if (Tool.isNotEmpty(_this.jobitemId)) {
      _this.getJobitemDetailInfo(_this.jobitemId);
    }
  },

  beforeDestroy() {
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

    getJobitemDetailInfo(jobitemId){
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

    confirmNotify() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + _this.confirmPath + _this.jobitemId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          Toast.success("保存成功", Swal);
          _this.$bus.$emit('close-manhour-tab', _this.index);
          _this.$bus.$emit('notify-jobitem-count-flag', true);  //执行管理
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

  },
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd';

.paper-height {
  height: calc(100vh - 200px);
}

</style>
