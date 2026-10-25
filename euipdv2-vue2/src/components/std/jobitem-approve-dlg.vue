<!--
 * Author: 周朔鹏
 * Date: 2025-03-06
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="dialog-container">
    <el-row>
      <el-col v-if="hasDeclare" :offset="2">
        <div style="margin-bottom: 10px;">
          <div style="display: flex; flex-direction: row; justify-content: flex-start; align-items: center; line-height: 40px;">
            <div style="display: flex; flex-direction: row; justify-content: flex-start; align-items: center; width: 180px;">
              <span style="line-height: 28px; width: 75px;">计划工时:</span>
              <span style="width: 90px;">{{ jobItem.manhour }} 小时</span>
            </div>
            <div style="display: flex; flex-direction: row; justify-content: flex-start; align-items: center;">
              <span style="line-height: 28px; width: 75px; font-weight: 500">申报工时:</span>
              <span style="width: 80px; margin-right: 3px;">
              <el-input type="number" size="small" v-model="declareManhour"/>
            </span>
              <span>小时</span>
            </div>
          </div>
          <div>
            <span style="font-weight: 500;">工时申报原因:</span> {{ declareReason }}
          </div>
        </div>
        <div class="hr hr-16 hr-dotted"></div>
      </el-col>
      <el-col :offset="2">
        <div style="display: flex; flex-direction: row;">
          <span style="margin-right: 15px; margin-bottom: 10px; font-size: 14px;">工作评价:</span>
          <el-rate
              v-model="jobItem.evaluateScore"
              :max=10
              :texts="['1分','2分','3分','4分','5分','6分','7分','8分','9分','10分']"
              show-text>
          </el-rate>
        </div>
      </el-col>
    </el-row>
    <el-row style="margin-bottom: 5px;">
      <el-col :offset="2">
        <el-form>
          <el-form-item prop="desc">
            <span slot="label">{{ "评价意见:" }}</span>
            <el-col :span="18">
              <el-input type="textarea" rows="2" v-model="jobItem.evaluateDesc"></el-input>
            </el-col>
          </el-form-item>
        </el-form>
      </el-col>
    </el-row>
    <div style="height: 50px;"></div>
    <div class="clearfix form-actions">
      <div style="display: flex; justify-content: center;">
        <button v-on:click="saveEvaluate()" class="ca-btn-primary" type="button">评价&提交</button>
        <button type="button" v-on:click="close()" class="ca-btn-primary">关闭</button>
      </div>
    </div>
  </div>
</template>

<script>
import FileRdms from "@/components/rdms/file-rdms.vue";
import Swal from "sweetalert2";

export default {
  components: {
    FileRdms,
  },

  name: "jobitem-approve-dlg",

  data: function() {
    return {
      loginUser: {},
      loginCustomer: {},
      declareManhour: 0,
      declareReason: '0',
      hasDeclare: false,

      JOB_ITEM_TYPE: JOB_ITEM_TYPE,
      ENTRANCE_CLASS: ENTRANCE_CLASS,

      jobItem: {
        evaluateScore: 8,
      },
    }
  },

  props: {
    jobitemId: {
      default: null,
    },
    jobType: {
      default: null,
    },
    inheritJobType: {
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
    closeFunction: {
      type: Function,
      default: null
    },
  },

  computed: {
  },

  mounted: function() {
    let _this = this;
    _this.getLoginUser();
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

      _this.getJobitemSimpleInfo(_this.jobitemId);
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
          if (_this.jobItem) {
            _this.declareManhour = _this.jobItem.declareManhour;
            _this.declareReason = _this.jobItem.declareReason;
            _this.hasDeclare = Number(_this.jobItem.declareManhour) > 0;
          }
          if(_this.jobItem && ( Tool.isEmpty(_this.jobItem.evaluateScore) || _this.jobItem.evaluateScore === 0)){
            _this.jobItem.evaluateScore = 8;
          }
        }else{
          Toast.warning(resp.message, Swal);
        }
      });
    },

    saveEvaluate() {
      let _this = this;

      if (1 !== 1
          || !Validator.require(_this.jobItem.evaluateScore, "工作评分", Swal)
          || !Validator.require(_this.jobItem.evaluateDesc, "评级意见", Swal)

          || !Validator.isNumber(_this.jobItem.evaluateScore, "工作评分",  Swal)
          || !Validator.length(_this.jobItem.evaluateDesc, "评级意见",2, 1000,  Swal)
      ) {
        return;
      }

      _this.jobItem.id = _this.jobitemId;
      _this.jobItem.loginUserId = _this.loginUser.customerUserId;

      _this.jobItem.projectType = _this.projectType;
      _this.jobItem.type = _this.jobType;

      // 审批人清空或改为 0 即不采用申报
      _this.jobItem.declareManhour = Number(_this.declareManhour) || 0;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/saveEvaluate', _this.jobItem).then((response) => {
        let resp = response.data;
        if (resp.success) {
          Toast.success("保存成功", Swal);
          _this.close();
          _this.$bus.publish({ areas: _this.$bus.areasForEntrance(_this.entrance), reason: 'jobitem-approve' });

          
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    close(){
      let _this = this;
      _this.closeFunction();
    },

  }
}
</script>

<style lang="scss">
@import '../../../public/static/scss/caipd';

</style>
