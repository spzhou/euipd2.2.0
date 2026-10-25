<!--
 * Author: 周朔鹏
 * Date: 2025-02-27
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="dialog-container">
    <BugDetailPlain
        :key="bugItem.id"
        :bugfeedback-id="bugItem.id">
    </BugDetailPlain>
    <div class="clearfix form-actions">
      <div style="display: flex; justify-content: center;">
        <button v-on:click="accept()" class="ca-btn-primary" type="button">
          接 受
        </button>
        <button v-on:click="reject()" class="ca-btn-primary" type="button">
          驳 回
        </button>
        <button v-on:click="close()" class="ca-btn-primary" type="button">
          关 闭
        </button>
      </div>
    </div>
  </div>
</template>
<script>
import Swal from "sweetalert2";
import BugDetailPlain from "@/components/rdms/bug-detail-plain.vue";

export default {
  name: 'bug-recheck-dlg',

  components: {
    BugDetailPlain,

  },

  props:{
    bugItem: {
      default: null,
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
    };
  },

  computed: {

  },

  mounted: function() {
    let _this =this;
    _this.getLoginUser();

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

    accept() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/bugfeedback/accept/'+ _this.bugItem.id).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          Toast.success("保存成功", Swal);
          _this.close();
          _this.$bus.$emit('refresh-quality-define-list', true);
          _this.$bus.$emit('notify-quality-recheck-num', true);
          _this.$bus.$emit('refresh-manhour-num', true);
          _this.$bus.$emit('refresh-bug-feedback-list', true);
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    /**
     * 提交需求
     */
    reject() {
      let _this = this;
      let text = `是否确认驳回操作?`;
      Swal.fire({
        title: '确认？',
        text: text,
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#409EFF',
        cancelButtonColor: '#909399',
        confirmButtonText: '确认!',
        cancelButtonText: '取消!'
      }).then((result) => {
        if (result.value) {
          _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/bugfeedback/reject/' + _this.bugItem.id).then((response)=>{
            let resp = response.data;
            if (resp.success) {
              Toast.success("保存成功", Swal);
              _this.$bus.$emit('refresh-quality-define-list', true);
              _this.$bus.$emit('notify-quality-recheck-num', true);
              _this.$bus.$emit('notify-jobitem-count-flag', true);  //执行管理
              _this.$bus.$emit('refresh-manhour-num', true);
              _this.$bus.$emit('refresh-bug-feedback-list', true);
              _this.close();
            } else {
              Toast.warning(resp.message, Swal);
            }
          })
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

</style>

