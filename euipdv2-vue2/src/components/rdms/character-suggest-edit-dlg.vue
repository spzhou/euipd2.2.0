<!--
 * Author: 周朔鹏
 * Date: 2024-07-01
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div  id="myContent" class="item-container" v-on:scroll="myContentScroll" style="display: flex; flex-direction: column;">
<!--    <div class="a4-paper-big-plain" :style="{ height: contentHeight }">-->
    <div class="a4-paper-big-plain" style="height: 100%; margin-bottom: 20px;">
      <div>
        <!--特性编辑表-->
        <div>
          <CharacterSimpleDefineCore
              ref="suggest_character_create"
              :key="('suggest-' + (suggestItem.id))"
              :character-item-init="suggestItem"
              :save-character-info = suggestCharacterSave
          ></CharacterSimpleDefineCore>
        </div>
        <div class="hr hr-18 dotted hr-double"></div>
        <el-row>
          <div style="display: flex; justify-content: space-evenly; align-items: center;">
            <div>
              <span>
                填表人 :
              </span>
              <span>
                {{ loginUser.trueName }}
              </span>
            </div>
            <div>
              <span>
                填表日期 :
              </span>
              <span>
                {{ currentTimeStr }}
              </span>
            </div>
          </div>
        </el-row>
        <div class="hr hr-18 dotted hr-double"></div>
        <div style="display: flex; justify-content: center;">
          <button type="button" v-on:click="saveSuggestCharacter('save')" class="ca-btn-primary">保存</button>
          <button type="button" v-on:click="saveSuggestCharacter('submit')" class="ca-btn-primary">提交</button>
          <button type="button" v-on:click="close()" class="ca-btn-primary">关闭</button>
        </div>
      </div>
    </div>

    <!--提交特性建议-->
    <el-dialog
        width="29.0cm"
        append-to-body
        :visible.sync="submitSuggestDlg"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div>
        <CharacterSuggestSubmitDlg
            :key="characterItem.id"
            :character-item="characterItem"
            :close-function="closeDialog"
        ></CharacterSuggestSubmitDlg>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import Swal from "sweetalert2";
import CharacterSimpleDefineCore from "@/components/rdms/character-simple-define-core.vue";
import CharacterSuggestSubmitDlg from "@/components/rdms/character-suggest-submit-dlg.vue";

export default {
  components: {
    CharacterSimpleDefineCore,
    CharacterSuggestSubmitDlg,
  },

  name: "character-suggest-edit-dlg",

  props: {
    suggestItem: {
      default: null,
    },
    closeFunction: {
      type: Function,
      default: null
    },
  },

  computed: {
    contentHeight(){
      return this.myContentHeight+120 +'px';
    },
  },

  data() {
    return {
      loginUser: {},
      loginCustomer: {},

      characterItem: {
        devManhour: 0,
        testManhour: 0,
        materialFee: 0,
        otherFee: 0,
      },

      myContentHeight: 1000,

      PROJECT_TYPE: PROJECT_TYPE,

      currentTime: {},
      currentTimeStr: '',

      submitSuggestDlg: false,
      dialogTitle: '',
    };
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();

    _this.currentTime = new Date();
    _this.currentTimeStr = _this.currentTime.toLocaleString()

    _this.myContentHeight = document.getElementById("myContent").scrollHeight;
  },

  methods: {
    close(){
      let _this = this;
      Swal.fire({
        title: '请确认',
        text: "如果有修订, 关闭前请及时保存",
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#409EFF',
        cancelButtonColor: '#909399',
        confirmButtonText: '保存!',
        cancelButtonText: '关闭!'
      }).then((result) => {
        if (result.isConfirmed) {
          _this.saveSuggestCharacter('save');
        }else{
          _this.closeFunction();
        }
      }).catch(_ => {});
      },

    myContentScroll(){
      let _this = this;
      _this.myContentHeight = document.getElementById("myContent").scrollHeight;
    },

    getLoginUser(){
      let _this = this;
      if(Tool.getLoginUser().userGroup === LOGIN_USER_GROUP.CUSTOMER.key){
        _this.loginUser = Tool.getLoginUser().adminUser;
        // console.log("loginUser:", _this.loginUser);
        _this.loginCustomer =Tool.getLoginUser();
      }else{
        _this.loginUser = Tool.getLoginUser();
        _this.loginCustomer = Tool.getLoginUser().customer;
      }
    },

    saveSuggestCharacter(flag){
      let _this = this;
      // _this.opeFlag = flag;
      _this.$refs.suggest_character_create.opeFlag = flag;
      _this.$refs.suggest_character_create.saveCharacterItem();
    },

    suggestCharacterSave(characterInfo) {
      let _this = this;

      _this.characterItem.id = characterInfo.id;
      _this.characterItem.iterationVersion = 1;
      _this.characterItem.characterName = characterInfo.characterName;
      _this.characterItem.functionDescription = characterInfo.functionDescription;
      _this.characterItem.workCondition = characterInfo.workCondition;
      _this.characterItem.inputLogicalDesc = characterInfo.inputLogicalDesc;
      _this.characterItem.functionLogicalDesc = characterInfo.functionLogicalDesc;
      _this.characterItem.outputLogicalDesc = characterInfo.outputLogicalDesc;
      _this.characterItem.testMethod = characterInfo.testMethod;

      _this.characterItem.customerId = _this.loginCustomer.id;
      _this.characterItem.writerId = _this.loginUser.customerUserId;

      {
        _this.characterItem.jobitemType = JOB_ITEM_TYPE.SUGGEST.key; //固定参数
        _this.characterItem.projectType = PROJECT_TYPE.DEV_PROJECT.key;
        _this.characterItem.loginUserId = _this.loginUser.customerUserId;
      }

      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/saveRecordAndData', _this.characterItem).then((response) => {
        let resp = response.data;
        if (resp.success) {
          if(characterInfo.opeFlag === 'save'){
            Toast.success("保存成功", Swal);
            _this.$bus.$emit('refresh-character-suggest-list', true);
            _this.closeFunction();
          }
          if(characterInfo.opeFlag === 'submit'){
            _this.submitSuggestDlg = true;
            _this.dialogTitle = "特性建议";
          }
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    closeDialog() {
      let _this = this;
      _this.submitSuggestDlg = false;
      _this.closeFunction();
    },
    handleClose(done) {
      done();
    },
  },
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd';

.item-container {
  height: 100%;
  width: 100%;
  //margin: 0 2px 0 12px;
  //padding-right: 10px;
  overflow-y: scroll;
  display: -webkit-box; /* 老版本语法: Safari, iOS, Android browser, older WebKit browsers. */
  display: -moz-box; /* 老版本语法: Firefox (buggy) */
  display: -ms-flexbox; /* 混合版本语法: IE 10 */
  display: -webkit-flex; /* 新版本语法: Chrome 21+ */
  display: flex; /* 新版本语法: Opera 12.1, Firefox 22+ */

}
</style>
