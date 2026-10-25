<!--
 * Author: 周朔鹏
 * Date: 2024-02-08
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <el-row>
      <el-col :span="24" style="width: 100%; text-align: center; font-size: 28px;margin-top: 25px;">
        {{ characterItem.characterName }}
      </el-col>
    </el-row>
    <div>
      <CharacterDetailInfoCore
          :id="('character-main-show')"
          :key="characterItem.id"
          :character-id="characterItem.id"
      ></CharacterDetailInfoCore>
    </div>
  </div>
</template>

<script>
import FileSaver from 'file-saver';
import Swal from "sweetalert2";
import CharacterDetailInfoCore from "@/components/std/character-detail-info-core.vue";
import {dateFormat, optionKV, shortDateFormat} from "@/filter/filter";

export default {
  components: {
    FileSaver,
    CharacterDetailInfoCore,
  },
  name: "character-doc-detail-page",

  props: {
    character: {
      default: null,
    },
  },

  data() {
    return {
      loginUser: {},
      loginCustomer: {},

      characterItem: {},

      processAttachments: [],  //过程流转中产生的附件
      characterBudget: {},

      devJobitemList: [],
    };
  },

  computed: {

  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();

    if(Tool.isNotEmpty_ES5(_this.character)){
      _this.characterItem = _this.character;
      if(Tool.isNotEmpty(_this.character.devJobitemPlainList)){
        _this.devJobitemList = _this.character.devJobitemPlainList;
      }
    }
  },

  beforeDestroy() {
  },

  methods: {
    rowClick(row){
      let _this = this;
      _this.addTab(row);

    },

    addTab(jobItem) {
      let tabName = jobItem.id;
      let hasTheItem = false;
      this.pageTabs.forEach((item, index)=>{
        if(item.name === jobItem.id){
          hasTheItem = true;
        }
      });

      if(! hasTheItem){
        this.pageTabs.push({
          title: jobItem.jobName,
          name: tabName,
          doctype: DOC_TYPE.JOBITEM.key,
        });
      }

      this.activeTabName = tabName;
    },

    removeTab(targetName) {
      let tabs = this.pageTabs;
      let activeName = this.activeTabName;
      if (activeName === targetName) {
        tabs.forEach((tab, index) => {
          if (tab.name === targetName) {
            let nextTab = tabs[index + 1] || tabs[index - 1];
            if (nextTab) {
              activeName = nextTab.name;
            }
          }
        });
      }

      this.activeTabName = activeName;
      this.pageTabs = tabs.filter(tab => tab.name !== targetName);
    },



    creatTimeFormat(row, column){
      return dateFormat(row.createTime);
    },

    jobitemTypeFormat(row) {
      return optionKV(JOB_ITEM_TYPE, row.type);
    },

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

  },
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd';

.paper-height {
  height: calc(100vh - 250px);
}

</style>
