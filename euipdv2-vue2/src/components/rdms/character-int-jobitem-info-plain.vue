<!--
 * Author: 周朔鹏
 * Date: 2023-12-28
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="ca-plain-container" >
    <div v-if="jobitem">
      <JobitemOwnInfoPlain
          :key="jobitem.id"
          :jobitem-id="jobitem.id"
          :prefix="'集成'"
          :job-type="jobitem.jobType"
          :project-type="jobitem.projectType"
          :index="index"
          :entrance="entrance"
      ></JobitemOwnInfoPlain>
    </div>
    <div class="hr hr-18 dotted hr-double"></div>
    <CharacterOwnInfoPlain
        :key="characterId"
        :character-id="characterId"
        :project-type="projectType"
    ></CharacterOwnInfoPlain>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import JobitemOwnInfoPlain from "@/components/std/jobitem-own-info-plain.vue";
import CharacterOwnInfoPlain from "@/components/rdms/character-own-info-plain.vue";

export default {
  components: {
    CharacterOwnInfoPlain,
    JobitemOwnInfoPlain,
  },
  name: "character-int-jobitem-info-plain",

  props: {
    characterId: {
      default: null,
    },
    index: {
      default: null,
    },
    projectType: {
      default: null,
    },
    entrance: {
      default: null,
    }
  },

  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},

      jobitem: {},
    }
  },

  computed: {},

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if(Tool.isNotEmpty(_this.characterId)){
      _this.getIntegrationJobitemInfo(_this.characterId);
    }

    _this.$bus.$on('refresh-character-int-jobitem-info-plain', (data) => {
      if(data){
        _this.getIntegrationJobitemInfo(_this.characterId);
      }
    });
  },

  beforeDestroy(){
    let _this = this;
    _this.$bus.$off('refresh-character-int-jobitem-info-plain');
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

    getIntegrationJobitemInfo(characterId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/getIntegrationJobitemInfo/' + characterId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.jobitem = resp.content;
        }else{
          Toast.warning(resp.message, Swal);
        }
      });
    },
  }
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd.scss';

.original-container{
  height: 100%;
  margin: 12px 2px 5px 15px;
  padding-right: 10px;
  //min-width: 360px;
  overflow-y: scroll;
}
</style>
