<!--
 * Author: 周朔鹏
 * Date: 2023-12-23
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="ca-plain-container" >
    <div v-if="jobitemId">
      <JobitemOwnInfoPlain
          :key="jobitemId"
          :jobitem-id="jobitemId"
          :prefix="jobNamePrefix"
          :job-type="jobType"
          :project-type="projectType"
          :index="index"
          :entrance="entrance"
      ></JobitemOwnInfoPlain>
    </div>
    <div class="hr hr-18 dotted hr-double"></div>
    <div v-for="(characterId, index) in getCharacterIdList" :key="index">
      <CharacterOwnInfoPlain
          :key="characterId"
          :character-id="characterId"
          :project-type="projectType"
          :show-budget="true"
      ></CharacterOwnInfoPlain>
    </div>


  </div>
</template>

<script>
import JobitemOwnInfoPlain from "@/components/std/jobitem-own-info-plain.vue";
import CharacterOwnInfoPlain from "@/components/rdms/character-own-info-plain.vue";

export default {
  components: {
    JobitemOwnInfoPlain,
    CharacterOwnInfoPlain,
  },
  name: "character-merge-plain-page",

  props: {
    characterIdStr: {
      default: null,
    },
    jobitemId: {
      default: null,
    },
    jobNamePrefix: {
      default: '',
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

    }
  },

  computed: {
    getCharacterIdList(){
      let _this = this;
      return JSON.parse(_this.characterIdStr);
    }
  },

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
