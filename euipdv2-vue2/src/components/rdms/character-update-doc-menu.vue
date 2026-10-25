<!--
 * Author: 周朔鹏
 * Date: 2024-12-15
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="original-container" >
    <div style="display: flex; flex-direction: row; justify-content: space-between">
      <div style="margin: 15px 0 8px 10px; font-weight: bold;">迭代特性清单:</div>
      <div  v-if="getMultiSelectStatus" style="margin-right: 15px; margin-top: 10px;">
        <el-button type="primary" size="mini" @click.stop="mergeCharacterJob">功能合并</el-button>
      </div>
    </div>
    <el-checkbox-group
        v-model="checkedCharacterIdList"
        @change="handleCheckedCitiesChange">
      <div id="character-item" v-for="(character, i) in updateCharacterList" :key="i" style="margin-left: 15px;">
        <a href="javascript:void(0)" @click="refreshCharacterInfo(character)">
          <div style="display: flex; flex-direction: row; justify-content: space-between; align-items: center; line-height: 28px;">
            <div>
              <el-checkbox
                  :label="character.id"
                  :key="character.id">
                {{ character.characterName }}
              </el-checkbox>
            </div>
            <div v-if="! getMultiSelectStatus" style="margin-right: 10px;">
              <el-button  v-if="character.status === CHARACTER_STATUS.UPDATE.key" plain size="mini" @click.stop="updateCharacterJob(character)">迭代定义</el-button>
            </div>
          </div>
        </a>
      </div>
    </el-checkbox-group>

    <!--特性迭代任务-->
    <el-dialog
        width="29cm"
        :visible.sync="characterUpdateDlg"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div>
        <JobitemCharacterUpdateDlg
            :key="characterItem.id"
            :character-id="characterItem.id"
            :close-dlg = closeDialog
        ></JobitemCharacterUpdateDlg>
      </div>
    </el-dialog>

    <!--功能合并任务-->
    <el-dialog
        width="29cm"
        :visible.sync="characterMergeDlg"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="getMergeListStatus">
        <JobitemCharacterMergeDlg
            :key="random"
            :character-id-list="checkedCharacterIdList"
            :close-dlg = closeDialog
        ></JobitemCharacterMergeDlg>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import Swal from "sweetalert2";

import CharacterTree from "@/components/rdms/character-tree.vue";
import JobitemCharacterUpdateDlg from "@/components/std/jobitem-character-update-dlg.vue";
import JobitemCharacterMergeDlg from "@/components/std/jobitem-character-merge-dlg.vue";

export default {
  components: {
    JobitemCharacterUpdateDlg,
    CharacterTree,
    JobitemCharacterMergeDlg,
  },
  name: "character-update-doc-menu",

  props: {
    preProjectId: {
      default: null,
    },
    refreshDetailPage :{
      type: Function,
      default: null,
    }
  },

  data: function () {
    return {
      checkedCharacterIdList: [],
      random: 0,
      loginUser: {},
      loginCustomer: {},
      updateCharacterList: {},

      characterUpdateDlg: false,
      characterMergeDlg: false,

      dialogTitle: '特性迭代定义',
      characterItem: {},

      CHARACTER_STATUS: CHARACTER_STATUS,

      checked: [],
    }
  },

  computed: {
    multipleSel(){
      let _this = this;
      let selectNum =0;
      _this.checked.forEach(status => {
        if(status){
          selectNum ++;
        }
      })
      return selectNum > 1;
    },

    getMergeListStatus(){
      let _this = this;
      if(Tool.isNotEmpty(_this.checkedCharacterIdList) && Tool.isNotEmpty_ES5(_this.checkedCharacterIdList)){
        return true;
      }else{
        return false;
      }
    },
    getMultiSelectStatus(){
      let _this = this;
      if(_this.checkedCharacterIdList.length>1){
        return true;
      }else{
        return false;
      }
    }
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if(Tool.isNotEmpty(_this.preProjectId)){
      _this.getUpdateCharacterList(_this.preProjectId);
    }

    _this.$bus.$on('refresh-character-update-doc-menu', (data)=>{
      if(data){
        _this.getUpdateCharacterList(_this.preProjectId);
      }
    });
  },

  beforeDestroy() {
    let _this = this;
    _this.$bus.$off('refresh-character-update-doc-menu');
  },
  methods: {
    handleCheckedCitiesChange(value) {
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

    getUpdateCharacterList(preProjectId) {
      let _this = this;
      let formData = new window.FormData();
      formData.append('preProjectId', preProjectId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/getUpdateCharacterList', formData).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.updateCharacterList = resp.content;
          _this.checkedCharacterIdList = [];
          if(Tool.isNotEmpty(_this.updateCharacterList) && Tool.isNotEmpty_ES5(_this.updateCharacterList) && Tool.isNotEmpty_ES5(_this.updateCharacterList[0])){
            _this.refreshCharacterInfo(_this.updateCharacterList[0]);
          }
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    refreshCharacterInfo(character){
      let _this = this;
      _this.refreshDetailPage(character, DOC_TYPE.CHARACTER.key);
    },

    updateCharacterJob(character) {
      let _this = this;
      _this.characterItem = character;
      _this.characterUpdateDlg = true;
      _this.dialogTitle = "特性迭代定义";
    },

    mergeCharacterJob() {
      let _this = this;
      _this.characterMergeDlg = true;
      _this.random = Math.random();
      _this.dialogTitle = "功能合并定义";
    },

    closeDialog() {
      let _this = this;
      _this.characterUpdateDlg = false;
      _this.characterMergeDlg = false;
    },
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

#character-item :hover{
  background-color: rgba(187, 215, 248, 0.2);
  cursor:pointer;
}
</style>
