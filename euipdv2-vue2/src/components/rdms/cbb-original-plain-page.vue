<!--
 * Author: 周朔鹏
 * Date: 2025-04-18
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="ca-plain-container" >
    <div v-if="cbbId">
      <div class="hr hr-18 dotted hr-double"></div>
      <CbbOwnInfoPlain
          :key="cbbId"
          :cbb-id="cbbId"
      ></CbbOwnInfoPlain>
    </div>
  </div>
</template>

<script>
import CbbOwnInfoPlain from "@/components/rdms/cbb-own-info-plain.vue";
import Swal from "sweetalert2";

export default {
  components: {
    CbbOwnInfoPlain,
  },
  name: "cbb-original-plain-page",

  props: {
    cbbId: {
      default: null,
    },
  },

  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},

      qualityList: [],
    }
  },

  computed: {},

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
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

  }
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd.scss';

</style>
