<!--
 * Author: 周朔鹏
 * Date: 2024-11-12
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <el-form label-width="180px">
      <el-form-item label="特性名称:">
        {{ characterInfo.characterName }}
      </el-form-item>
      <el-form-item label="特性编号:">
        {{ characterInfo.iterationVersion | iterationFormat }}
      </el-form-item>
      <el-form-item label="预立项项目名称:">
        <el-select v-model="preProjectId" filterable placeholder="请选择">
          <el-option
              v-for="item in preProjectList"
              :key="item.id"
              :label="item.preProjectName"
              :value="item.id">
          </el-option>
        </el-select>
      </el-form-item>
    </el-form>
    <div>
      <div style="margin: 15px 70px;">
        <small><i class="el-icon-info"></i> 操作提示: 请选择一个<strong><span style="font-size: 14px;">预立项项目</span></strong>, 被迭代的功能将在目标预立项项目中进行迭代定义管理. 如果不存在合适的预立项项目, 请到<router-link to="character"><strong><em> <span style="font-size: 14px;"> 特性管理 </span></em></strong> </router-link>中进行创建.</small>
      </div>
    </div>
    <div class="clearfix form-actions" style="margin-top: 50px;">
      <div style="display: flex; justify-content: center;">
        <button v-on:click="saveIterationCharacterRecord()" class="ca-btn-primary" type="button">
          分配
        </button>
        <button v-on:click="close()" class="ca-btn-primary" type="button">
          关闭
        </button>
      </div>
    </div>
  </div>
</template>
<script>
import Swal from "sweetalert2";

export default {
  components:{

  },

  name: 'character-update-dlg',
  props:{
    characterId: {
      default: null,
    },
    closeFunction: {
      type: Function,
      default: null
    },
  },

  data() {
    return {
      loginUser: {},
      loginCustomer: {},

      preProjectList: [],

      preProjectId: '',
      characterInfo: {},
    };
  },

  mounted: function() {
    let _this =this;
    _this.getLoginUser();

    if(Tool.isNotEmpty(_this.characterId)){
      _this.getCharacterSimpleInfo(_this.characterId);
      _this.listPreProject();
    }

  },
  methods:{
    getCharacterSimpleInfo(characterId){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/getCharacterSimpleInfo/'+ characterId).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.characterInfo = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    listPreProject() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/pre-project/list/' + _this.loginCustomer.id).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.preProjectList = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

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

    /**
     */
    saveIterationCharacterRecord() {
      let _this = this;

      Swal.fire({
        title: '确认',
        text: "是否确认功能分配?",
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#409EFF',
        cancelButtonColor: '#909399',
        confirmButtonText: '确认!',
        cancelButtonText: '取消!'
      }).then((result) => {
        if (result.value) {
          _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/saveIterationCharacterRecord', {
            characterId: _this.characterId,
            preProjectId: _this.preProjectId,
          }).then((response)=>{
            let resp = response.data;
            if (resp.success) {
              _this.close();
              _this.$bus.$emit('refresh-character-tree', true);
              _this.$bus.$emit('refresh-review-list', true);
              _this.$bus.$emit('notify-character-update-job-flag', true);
            } else {
              Toast.warning(resp.message, Swal);
            }
          })
        }
      })
    },

    close(){
      let _this = this;
      _this.closeFunction(_this.index);
    },
  }
};
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd.scss';
.ca-form{
  font-size: 14px;
  .ca-form-item{
    display: flex;
    flex-direction: row;
    justify-content: left;
    line-height: 25px;
    margin-bottom: 10px;
    .ca-form-label{
      width: 150px;
      display: flex;
      justify-content: right;
    }
    .ca-form-content{
      width: 100%;
      text-align: justify;
    }
  }
}
</style>
