<!--
 * Author: 周朔鹏
 * Date: 2024-11-06
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
    <div v-if="characterId">
      <div class="hr hr-18 dotted hr-double"></div>
      <CharacterOwnInfoPlain
          :key="characterId"
          :character-id="characterId"
          :project-type="projectType"
      ></CharacterOwnInfoPlain>
    </div>
    <div v-if="characterId">
      <div v-if="qualityList && qualityList.length>0">
        <div v-for="(item, i) in qualityList" :key="i">
          <div class="hr hr-18 dotted hr-double"></div>
          <QualityOwnInfoPlain
              :key="item.id"
              :quality-id="item.id"
          ></QualityOwnInfoPlain>
        </div>
      </div>
    </div>

  </div>
</template>

<script>
import JobitemOwnInfoPlain from "@/components/std/jobitem-own-info-plain.vue";
import CharacterOwnInfoPlain from "@/components/rdms/character-own-info-plain.vue";
import QualityOwnInfoPlain from "@/components/rdms/quality-own-info-plain.vue";
import Swal from "sweetalert2";

export default {
  components: {
    JobitemOwnInfoPlain,
    CharacterOwnInfoPlain,
    QualityOwnInfoPlain,
  },
  name: "character-original-plain-page",

  props: {
    characterId: {
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

      qualityList: [],
    }
  },

  computed: {},

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if(Tool.isNotEmpty(_this.characterId)){
      _this.listQualityByCharacterId(_this.characterId);
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
    listQualityByCharacterId(characterId) {
      let _this = this;
      let formData = new window.FormData();
      formData.append('characterId', characterId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/quality/listQualityByCharacterId', formData).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.qualityList = resp.content;
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },
  }
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd.scss';

</style>
