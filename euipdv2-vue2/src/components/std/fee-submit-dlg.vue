<!--
 * Author: 周朔鹏
 * Date: 2024-01-05
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div class="dialog-container">
      <el-form
          label-width="130px"
          :show-message=false>
        <el-row>
          <el-col :span="11">
            <el-form-item label="申请单号:">
              <el-col :span="23">
                <span>{{ dialogInfo.code }}</span>
              </el-col>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="申请人:">
              <el-col :span="23">
                <span>{{ dialogInfo.writerName }}</span>
              </el-col>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="11">
            <el-form-item label="实际费用:">
              <el-col :span="23">
                <el-input type="number" v-model="dialogInfo.actualCost" style="width: 130px;"></el-input> 元
              </el-col>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="23">
            <el-form-item label="提交说明" required >
              <el-input type="textarea" rows="5" v-model="dialogInfo.submitDescription"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div  class="dialog-footer">
        <div style="display: flex; justify-content: center;">
          <button type="button" v-on:click="submit()" class="ca-btn-primary">提交</button>
          <button type="button" v-on:click="close()" class="ca-btn-primary">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import FileRdms from "../rdms/file-rdms.vue";
import Swal from "sweetalert2";
import FileUploadCore from "@/components/std/file-upload-core.vue";
export default {
  components: {
    FileUploadCore,
    FileRdms,
  },
  name: "fee-submit-dlg",

  props: {
    applicationCode: {
      default: null,
    },
    closeDlg: {
      type: Function,
      default: null
    },
  },

  data() {
    return {
      loginUser: {},
      loginCustomer: {},
      dialogInfo: {},
    };
  },

  computed: {
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if(Tool.isNotEmpty(_this.applicationCode)){
      _this.getFeeApplicationByCode(_this.applicationCode);
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

    getFeeApplicationByCode(code) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/fee/getFeeApplicationByCode/' + code).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.dialogInfo = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      });
    },

    submit() {
      let _this = this;
      // 保存校验
      if (1 !== 1
          || !Validator.require(_this.dialogInfo.submitDescription, "提交说明",Swal)
          || !Validator.require(_this.dialogInfo.actualCost, "实际费用",Swal)
          || !Validator.length(_this.dialogInfo.submitDescription, "提交说明", 2, 500,Swal)
          || !Validator.isNumber(_this.dialogInfo.actualCost, "提交说明", Swal)
      ) {
        return;
      }
      _this.dialogInfo.loginUserId = _this.loginUser.customerUserId;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/fee/submit', _this.dialogInfo).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          Toast.success("提交成功", Swal);
          _this.$bus.$emit('refresh-fee-application-list', true);
          _this.$bus.$emit('fee-deal-with-dlg-close', true);
          _this.$bus.$emit('notify-quality-fee-application-num', true);
          _this.close();
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    close(){
      let _this = this;
      _this.closeDlg();
    },

  },
}
</script>

<style lang="scss" scoped>
@import '../../../public/static/scss/caipd';

.text-box{
  display: inline-block;
  padding-left: 3px;
  padding-right: 15px;
  line-height: 30px;
  //width: 100px;
  background-color: rgba(204, 204, 204, 0.20);
}
.dialog-footer{
  background-color: $colorBackground;
  margin-top: 20px;
  padding: 20px 0;
}
</style>
