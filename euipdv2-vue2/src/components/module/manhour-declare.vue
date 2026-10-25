<!--
 * Author: 周朔鹏
 * Date: 2025-03-06
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div style="display: flex; flex-direction: column; justify-content: center; align-items: center">
      <div>
        <div style="line-height: 31px;">
          <i class="el-icon-finished"></i><span style="margin-left: 10px;">是否已经按照工作任务要求完成的全部工作,并正确填写信息?</span>
        </div>
        <div style="line-height: 31px;">
          <i class="el-icon-finished"></i><span style="margin-left: 10px;">是否已经将相关的附件上传到系统?</span>
        </div>
        <div style="line-height: 31px;">
          <el-checkbox v-model="declare" @change="checkboxChang">
            <div v-if="declare" style="display: flex; flex-direction: row; align-items: center; justify-content: flex-start;">
              <div>
                工时申报:
              </div>
              <div style="width: 80px; margin-left: 10px;">
                <el-input type="number"
                          size="small"
                          placeholder="请输入申报工时"
                          v-model="manhour"
                          @change="onManhourChange"
                ></el-input>
              </div>
              <div style="margin-left: 10px;">小时 ( 请根据实际情况填写 )</div>
            </div>
            <div v-else>
              是否需要进行工时申报? ( 非必须 , 申报后需要审批确认 )
            </div>
          </el-checkbox>
        </div>
        <div v-if="declare">
          <el-input type="textarea" rows="3" v-model="reason" placeholder="请填写申报原因"></el-input>
        </div>
      </div>
    </div>
    <div class="clearfix form-actions" style="margin-top: 30px;">
      <div style="display: flex; justify-content: center;">
        <button type="button" v-on:click="declareConfirm()" :disabled="busy" class="ca-btn-primary">确认</button>
        <button type="button" v-on:click="close()" class="ca-btn-primary">取消</button>
      </div>
    </div>
  </div>
</template>

<script>
  import Swal from "sweetalert2";

  export default {
    name: 'manhour-declare',
    props: {
      jobitemId: {
        default: null,
      },
      confirmClicked: {
        type: Function,
        default: null
      },
      closeDlg: {
        type: Function,
        default: null
      }
    },
    data: function () {
      return {
        loginUser: {},
        loginCustomer: {},

        manhour: 0,
        reason: '',
        declare: false,
        manhourDeclare: {},
        jobItem: {},
        busy: false,
      }
    },
    mounted: function () {
      let _this = this;
      //读取任务单是否有申报工时信息
      _this.getLoginUser();

      if(_this.jobitemId){
        _this.getJobitemSimpleInfo(_this.jobitemId);
      }
    },

    computed: {
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

      declareConfirm(){
        let _this = this;
        if (_this.busy) {
          return;
        }
        if(_this.declare && (!Validator.require(_this.reason, "申请原因", Swal)
            || !Validator.length(_this.reason, "申请原因", 4, 200, Swal))){
          return;
        }

        let declare = {
          jobitemId: _this.jobitemId,
          originManhour: _this.jobItem.manhour,
          manhour: _this.manhour,
          reason: _this.reason,
        }
        if(_this.declare){
          _this.busy = true;
          _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/manhour-declare/save', declare).then((response) => {
            let resp = response.data;
            if (resp.success) {
              //回调函数
              _this.confirmClicked(declare);
            } else {
              Toast.warning(resp.message, Swal);
            }
          }).finally(() => {
            _this.busy = false;
          })
        }else{
          _this.confirmClicked(declare);
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
            if(_this.jobItem.manhour){
              _this.manhour = _this.jobItem.manhour;
            }else{
              _this.manhour = 0;
            }
          }else{
            Toast.warning(resp.message, Swal);
          }
        });
      },

      onManhourChange() {
        let _this = this;
        let tem = _this.manhour;
        if (_this.manhour < 0) {
          _this.manhour = 0;
        }
        if (_this.manhour > (_this.jobItem.manhour * 3)) {
          _this.manhour = (_this.jobItem.manhour * 3);
        }
      },

      checkboxChang() {
        let _this = this;
        if(!_this.declare){
          _this.manhour = _this.jobItem.manhour;
          _this.reason = '';
        }
      },

      close() {
        let _this = this;
        _this.closeDlg();
      },

    }
  }
</script>

