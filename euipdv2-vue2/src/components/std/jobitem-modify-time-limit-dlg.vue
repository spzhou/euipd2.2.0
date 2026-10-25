<!--
 * Author: 周朔鹏
 * Date: 2024-11-05
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div class="dialog-container">
      <el-form
          :model="jobItem"
          label-width="130px"
          :show-message=false
          class="dialog-width">
        <el-row>
          <el-col :span="11">
            <el-form-item label="任务名称">
              <el-col :span="23">
                {{ jobItem.jobName }}
              </el-col>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="6">
            <el-form-item label="任务执行人">
              <el-col :span="24">
                {{ jobItem.executorName }}
              </el-col>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="22">
            <el-form-item label="任务描述">
              <div v-rich-html="jobItem.taskDescription"></div>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row style="margin-top: 5px;">
          <el-col :span="22">
            <el-form-item label="任务工时">
              {{ jobItem.manhour }} 标准工时
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="7">
            <el-form-item label="提交时间" required>
              <el-date-picker
                  type="date"
                  placeholder="选择日期"
                  :picker-options="pickerOptions"
                  v-model="jobItem.planSubmissionTime">
              </el-date-picker>
              <div v-if="windowHint" class="issue-window-hint">{{ windowHint }}</div>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div class="clearfix form-actions">
        <div style="display: flex; justify-content: center;">
          <button type="button" v-on:click="modifyTimeLimit()" class="ca-btn-primary">修改工期</button>
          <button type="button" v-on:click="close()" class="ca-btn-primary">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import jobitemCreateDlgMixin from "@/mixins/jobitemCreateDlgMixin";

export default {
  components: {
  },

  name: "jobitem-modify-time-limit-dlg",

  mixins: [jobitemCreateDlgMixin],

  props: {
    jobitemId: {
      default: null
    },
    closeDlg: {
      type: Function,
      default: null
    }
  },

  data() {
    return {
      loginUser: {},
      loginCustomer: {},

      jobItem: {
        manhour: 0,
      },

      pickerOptions: {
        disabledDate: (v) => this.jobItem.fromPlan
            ? this.disabledDateBeforeToday(v)
            : this.disabledSubmitDateInWindow(v, this.jobItem.type),
      },
    };
  },

  computed: {
    windowHint() {
      return this.jobItem.fromPlan ? '' : this.issueWindowHintFor(this.jobItem.type);
    },
  },

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

    /**
     * 保存客户信息
     */
    modifyTimeLimit() {
      let _this = this;
      // 保存校验
      if (1 !== 1
          || !Validator.require(_this.jobItem.id, "任务ID",Swal)
      ) {
        return;
      }

      Swal.fire({
        title: '确认',
        text: "确认修改任务工期吗?",
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#409EFF',
        cancelButtonColor: '#909399',
        confirmButtonText: '确认!',
        cancelButtonText: '取消!'
      }).then((result) => {
        if (result.isConfirmed) {
          _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/modifyTimeLimit', _this.jobItem).then((response)=>{
            let resp = response.data;
            if (resp.success) {
              Toast.success("工期修改成功!", Swal);
              _this.$bus.$emit('refresh-gantt-list', true);
              _this.close();
            } else {
              Toast.warning(resp.message, Swal);
              _this.close();
            }
          })
        }
      }).catch(_ => {
      });
    },

    close(){
      let _this = this;
      _this.closeDlg();
    },

  },
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd.scss';

.dialog-width{
  width: 100%;
}

.issue-window-hint {
  font-size: 12px;
  line-height: 1.4;
  color: #909399;
}

</style>
