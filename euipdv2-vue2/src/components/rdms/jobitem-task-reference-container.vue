<!--
 * Author: 周朔鹏
 * Date: 2024-11-04
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
 * 工单的参考资料：工单有特性时取该特性的集成特性清单，否则取工单所在子项目（项目级工单取根子项目）的清单。
-->

<template>
  <div>
    <ArchiveReferenceTreeCharacter
        v-if="jobItem.characterId"
        :key="('character-' + jobItem.characterId)"
        :character-id="jobItem.characterId"
    ></ArchiveReferenceTreeCharacter>
    <ArchiveReferenceTreeCharacter
        v-else-if="subprojectId"
        :key="('subproject-' + subprojectId)"
        :subproject-id="subprojectId"
    ></ArchiveReferenceTreeCharacter>
    <p v-else-if="loaded" style="margin: 15px;">该工单未关联特性或子项目，无参考资料。</p>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import ArchiveReferenceTreeCharacter from "@/components/rdms/archive-reference-tree-character.vue";

export default {
  components: {
    ArchiveReferenceTreeCharacter,
  },
  name: "jobitem-task-reference-container",

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
  },

  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},
      jobItem: {},
      loaded: false,
    }
  },

  computed: {
    // 项目级工单不带子项目编号，根子项目编号与项目编号相同
    subprojectId() {
      return this.jobItem.subprojectId || this.jobItem.projectId;
    },
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if(Tool.isNotEmpty(_this.jobitemId)){
      _this.getJobitemDetailInfo(_this.jobitemId);
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

    getJobitemDetailInfo(jobitemId) {
      let _this = this;
      let formData = new window.FormData();
      formData.append('jobitemId', jobitemId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getJobitemDetailInfo', formData).then((response) => {
        let resp = response.data;
        _this.loaded = true;
        if (resp.success) {
          _this.jobItem = resp.content || {};
        }else{
          Toast.warning(resp.message, Swal);
        }
      });
    },
  },
}
</script>
