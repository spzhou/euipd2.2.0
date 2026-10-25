<!--
 * Author: 周朔鹏
 * Date: 2025-07-30
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div class="dialog-container">
      <div class="form-horizontal">
        <div class="form-group">
          <label class="col-sm-2 control-label no-padding-right"> 目标项目名称: </label>
          <div class="col-sm-10">
            <select v-model="setupedProjectId" class="col-xs-10 col-sm-5">
              <option v-for="o in projectList" :key="o.id" v-bind:value="o.id">{{ o.projectName }}</option>
            </select>
          </div>
        </div>
        <div class="hr hr-18 dotted hr-double"></div>
        <div style="margin-left: 107px; font-size: 16px; margin-bottom: 15px;">特性清单</div>
        <div style="display: flex; justify-content: center;">
          <div style="width: var(--table-out-width, 100%); max-width: 22cm;">
            <el-table
                ref="characterTable"
                :data="characterList"
                border
                stripe
                style="width: 100%"
                highlight-current-row
                @selection-change="handleSelectionChange">
              <el-table-column
                  type="selection"
                  width="55">
              </el-table-column>
              <el-table-column label="序号" align="center" width="50">
                <template slot-scope="scope">
                  {{ scope.$index + 1 }}
                </template>
              </el-table-column>
              <el-table-column
                  prop="characterName"
                  label="特性名称">
              </el-table-column>
              <el-table-column
                  prop="writerName"
                  label="撰写人"
                  align="center">
              </el-table-column>
              <el-table-column
                  prop="devVersion"
                  :formatter="versionNumFormat"
                  label="规划版本"
                  width="70">
              </el-table-column>
              <el-table-column
                  prop="createTime"
                  :formatter="dateFormat"
                  label="撰写日期"
                  align="center"
                  width="90">
              </el-table-column>
            </el-table>
          </div>
        </div>
        <div class="clearfix form-actions">
          <div style="display: flex; justify-content: center;">
            <button v-on:click="append()" class="ca-btn-primary" type="button">
              追加
            </button>
            <button v-on:click="close()" class="ca-btn-primary" type="button">
              放弃
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
import Swal from "sweetalert2";

export default {

  name: 'character-append',
  props: {
    preProject: {
      default: null,
    },
    index: {
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

      characterList: [],
      projectList: [],
      setupProject: {},
      setupedProjectId: '',

      multipleSelection: [],
    };
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    _this.setupProject = _this.preProject;
    _this.listProjectsByPreProjectId();
    _this.listAllApprovedCharacters();
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
      _this.getArchivedCharacterListByCustomerId(_this.loginCustomer.id);
    },

    handleSelectionChange(val) {
      let _this = this;
      _this.multipleSelection = val;
      console.log("_this.multipleSelection:", _this.multipleSelection);
    },

    dateFormat(row, column) {
      return row.createTimeStr.substring(0, 10);
      /*console.log("row:", row);
      console.log("column:", column);*/
    },
    versionNumFormat(row, column) {
      return row.devVersion == null ? '' : String(row.devVersion);
    },

    listAllApprovedCharacters() {
      // debugger
      let _this = this;
      let formData = new window.FormData();
      formData.append('preProjectId', _this.preProject.id);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/listAllApprovedByPreProjectId', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.characterList = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    //根据preProjectId查询对应的已立项项目
    //由于一个预立项项目,可能会对应若干个立项的项目,所以返回应为List
    listProjectsByPreProjectId() {
      // debugger
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/listProjectsByPreProjectId/' + _this.preProject.id).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.projectList = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    /**
     * 向已立项项目中追加特性
     */
    append() {
      let _this = this;
      if (_this.setupedProjectId === null || _this.setupedProjectId === '') {
        return
      }
      _this.setupProject.id = _this.setupedProjectId;
      _this.setupProject.characterList = _this.multipleSelection;  //只有选中的会被追加

      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/append-characters', _this.setupProject).then((response) => {
        let resp = response.data;
        if (resp.success) {
          Toast.success("保存成功", Swal);
          _this.close();
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    close() {
      let _this = this;
      _this.closeFunction();
    },
  }
};
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd.scss';

</style>
