<!--
 * Author: 周朔鹏
 * Date: 2025-02-05
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="dialog-container">
    <QualityDetailPlain
        :key="qualityId"
        :quality-id="qualityId">
    </QualityDetailPlain>
    <div class="clearfix form-actions">
      <div style="display: flex; justify-content: center;">
        <button v-on:click="close()" class="ca-btn-primary" type="button">
          关 闭
        </button>
      </div>
    </div>
  </div>
</template>
<script>

import QualityDetailPlain from "@/components/rdms/quality-detail-plain.vue";

export default {
  name: 'quality-dealwith-info',

  components: {
    QualityDetailPlain,

  },

  props:{
    qualityId: {
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

