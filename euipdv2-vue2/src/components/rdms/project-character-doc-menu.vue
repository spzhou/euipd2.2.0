<!--
 * Author: 周朔鹏
 * Date: 2023-12-24
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="original-container" >
    <div>
      <CharacterTree
          :key="projectId"
          :project-id="projectId"
          :callback-function="clickedObject"
      ></CharacterTree>
    </div>
  </div>
</template>

<script>
import Swal from "sweetalert2";

import CharacterTree from "@/components/rdms/character-tree.vue";

export default {
  components: {
    CharacterTree,
  },
  name: "project-character-doc-menu",

  props: {
    projectId: {
      default: null,
    },
    refreshDetailPage :{
      type: Function,
      default: null,
    }
  },

  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},

      projectInfo: {},
    }
  },

  computed: {},

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if(Tool.isNotEmpty(_this.projectId)){
      _this.getProjectSimpleInfo(_this.projectId);
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

    getProjectSimpleInfo(projectId){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/getProjectSimpleInfo/' + projectId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.projectInfo = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    clickedObject(data, docType){
      let _this = this;
      _this.refreshDetailPage(data, docType);
    }
  }
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd.scss';

.original-container{
  height: 100%;
  margin: 8px 3px;
  overflow-y: scroll;
}
</style>
