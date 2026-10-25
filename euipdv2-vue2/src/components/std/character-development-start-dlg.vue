<!--
 * Author: 周朔鹏
 * Date: 2025-04-18
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div class="dialog-container">
      <div style="display: flex; flex-direction: column; justify-content: center; align-items: center; margin-bottom: 30px;">
        <div style="font-weight: 500; font-size: 18px; margin-bottom: 10px;">
          请选择特性开发需要参考的 CBB <small>(非必选)</small>
        </div>
        <div style="font-size: 15px; margin-bottom: 10px;">
          特性名称: {{ characterItem.characterName }}
        </div>
        <div style="width: 800px;">
          <div class="hr hr-10 dotted"></div>
        </div>
        <el-transfer
            filterable
            filter-placeholder="请输入关键词"
            v-model="cbbValue"
            :data="cbbData"
            :titles="cbbTitles">
        </el-transfer>
      </div>
      <div class="clearfix form-actions">
        <div style="display: flex; justify-content: center;">
          <button type="button" v-on:click="saveAttachment()" class="ca-btn-primary">启 动</button>
          <button type="button" v-on:click="close()" class="ca-btn-primary">取 消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>

import Swal from "sweetalert2";
import {optionKV} from "@/filter/filter";

export default {
  components: {
  },
  name: "character-development-start-dlg",

  props: {
    characterId: {
      default: null
    },
    startupDevelop: {
      type: Function,
      default: null
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

      cbbData: [],
      cbbValueStr: '',
      cbbValue: [],
      cbbTitles: ['可选 特性','已选 特性'],
      subProject: {},
      characterItem: {},

    };
  },

  computed: {
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if(_this.characterId){
      _this.getCharacterSimpleInfo(_this.characterId);
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

    getCharacterSimpleInfo(characterId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/getCharacterSimpleInfo/' + characterId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.characterItem = resp.content;
          // moduleIdListStr 登记的是 CBB 标识，穿梭框候选项以 CBB 标识为键
          if(_this.characterItem && _this.characterItem.moduleIdListStr){
            _this.cbbValue = JSON.parse(_this.characterItem.moduleIdListStr) || [];
          }
          _this.getSubProjectInfo(_this.characterItem.subprojectId);
        }else{
          Toast.warning(resp.message, Swal);
        }
      });
    },

    getSubProjectInfo(subProjectId) {
      let _this = this;
      let formData = new window.FormData();
      formData.append('subProjectId', subProjectId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/sub-project/getSubProjectInfo', formData).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.subProject = resp.content;
          if(_this.subProject && _this.subProject.cbbList){
            // 已发布的作候选；已停用、已废弃的仅在已登记时保留显示，选用门禁不复核原有标识
            _this.subProject.cbbList.forEach((item) => {
              const archived = item.status === CBB_STATUS.ARCHIVED.key;
              if(!archived && !_this.cbbValue.includes(item.id)){
                return;
              }
              _this.cbbData.push({
                label: archived ? item.cbbName : item.cbbName + '（' + optionKV(CBB_STATUS, item.status) + '）',
                key: item.id,
              });
            });
          }
          _this.cbbValue.forEach((id) => {
            if(!_this.cbbData.some(item => item.key === id)){
              _this.cbbData.push({ label: '已不在可选范围的 CBB', key: id });
            }
          });
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    saveAttachment(){
      let _this = this;
      _this.characterItem.moduleIdListStr = JSON.stringify(_this.cbbValue);
      _this.startupDevelop(_this.characterItem);
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
.dialog-width{
  width: 100%;
}
.red-color{
  color: red;
}
</style>
