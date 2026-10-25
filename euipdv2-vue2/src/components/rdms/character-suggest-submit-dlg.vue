<!--
 * Author: 周朔鹏
 * Date: 2024-11-12
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div class="dialog-container">
      <div class="ca-form">
        <el-form label-width="100px" size="small">
          <el-row>
            <el-col :span="11">
              <el-form-item label="特性名称" required>
                <el-col :span="23">
                  {{ characterItem.characterName }}
                </el-col>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="22">
              <el-form-item label="开发必要性" required>
                <el-input type="textarea" rows="5" v-model="jobItem.taskDescription"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row style="margin-top: 5px;">
            <el-col :span="24">
              <el-form-item label="隶属项目">
                <el-col :span="20">
                  <el-select v-model="jobItem.subprojectId" filterable placeholder="请选择">
                    <el-option
                        v-for="item in projectList"
                        :key="item.id"
                        :label="item.label"
                        :value="item.id">
                    </el-option>
                  </el-select>
                </el-col>
              </el-form-item>
            </el-col>
          </el-row>
<!--          <el-row>
            <el-col :span="7">
              <el-form-item label="提交时间" required>
                <el-date-picker
                    type="date"
                    placeholder="选择日期"
                    :picker-options="pickerOptions"
                    v-model="jobItem.planSubmissionTime">
                </el-date-picker>
              </el-form-item>
            </el-col>
          </el-row>-->
        </el-form>
      </div>
      <div style="margin-top: 50px;">
        <div class="clearfix form-actions">
          <div style="display: flex; justify-content: center;">
            <button v-on:click="submitSuggest()" class="ca-btn-primary" type="button">
              提交建议
            </button>
            <button v-on:click="close()" class="ca-btn-primary" type="button">
              关闭
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
import Swal from "sweetalert2";
import {dateFormat, fixed2fractional, iterationFormat} from "@/filter/filter";

export default {
  components:{

  },

  name: 'character-suggest-submit-dlg',
  props:{
    characterItem: {
      default: null
    },
    closeFunction: {
      type: Function,
      default: null
    },
  },

  data() {
    return {
      loginUser: {},
      loginCustomer: {},

      jobItem: {
        id: Uuid.shortUuid(8),
        subprojectId: '',
        taskDescription: '',
        jobName: `特性建议: ${this.characterItem.characterName}`,
        customerId: this.characterItem.customerId,
        characterId: this.characterItem.id,
        status: JOB_ITEM_STATUS.HANDLING.key,
        type: JOB_ITEM_TYPE.SUGGEST.key,
        manhour: 0,
      },
      projectList:[],  //参与开发的项目列表

      projectInfo: {},

      pickerOptions: {
        disabledDate(v) {
          return v.getTime() < new Date().getTime() - 86400000;//  - 86400000是否包括当天
        }
      },
    };
  },

  computed: {
  },

  mounted: function() {
    let _this =this;
    _this.getLoginUser();
    _this.getTakePartInSubprojectList();
  },
  methods:{
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

    getTakePartInSubprojectList(){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/sub-project/getTakePartInSubprojectList/'+ _this.loginUser.customerUserId ).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.projectList = resp.content;
        } else {
          Toast.warning(resp_submit.message, Swal);
        }
      })
    },

    submitSuggest() {
      let _this = this;
      if (1 !== 1
          || !Validator.require(_this.jobItem.taskDescription, "开发必要性", Swal)
          || !Validator.require(_this.jobItem.subprojectId, "隶属项目", Swal)
          // || !Validator.require(_this.jobItem.planSubmissionTime, "计划提交时间", Swal)

          || !Validator.length(_this.jobItem.taskDescription, "开发必要性", 10, 500, Swal)
      ) {
        return;
      }

      let nowDate = new Date();
      nowDate.setDate(nowDate.getDate() + 3)
      _this.jobItem.planSubmissionTime = nowDate;

      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/save', _this.jobItem ).then((response) => {
        let resp_submit = response.data;
        if (resp_submit.success) {
          _this.close();
          Toast.success("提交成功", Swal);
          _this.$bus.$emit('refresh-character-suggest-list', true);
          _this.$bus.$emit('refresh-manhour-num', true);
        } else {
          Toast.warning(resp_submit.message, Swal);
        }
      })
    },

    close(){
      let _this = this;
      _this.closeFunction();
    },
  }
};
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd.scss';
.ca-form{
  font-size: 14px;
  margin-left: 30px;
}
</style>
