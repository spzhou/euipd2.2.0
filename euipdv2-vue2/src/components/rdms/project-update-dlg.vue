<!--
 * Author: 周朔鹏
 * Date: 2025-02-20
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div class="dialog-container">
      <div class="ca-form">
        <el-form label-width="100px" size="small">
          <el-form-item
              label="迭代项目名称:">
            <el-input style="width: 300px;"
                v-model="projectInfo.projectName"
            ></el-input>
          </el-form-item>
          <el-form-item
              label="升级方式:">
            <el-radio-group v-model="bumpType" size="mini" @change="getNewVersion">
              <el-radio-button label="major">大版本</el-radio-button>
              <el-radio-button label="minor">功能版本</el-radio-button>
              <el-radio-button label="patch">维护版本</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item
              label="新主开发版本:">
            <el-input style="width: 300px;"
                v-model="newVersion"
            ></el-input>
            <span style="margin-left: 8px; color: #909399;">源项目 {{ projectInfo.devVersion }}</span>
          </el-form-item>
          <el-form-item
              label="所属产品:">
            <span v-if="projectInfo.productId">沿用源项目所属产品</span>
            <div v-else-if="projectInfo.id" style="width: 500px;">
              <product-owner-picker ref="productPicker" :value="productOwner"></product-owner-picker>
              <small style="color: #909399;">源项目尚未登记所属产品，迭代时一并登记</small>
            </div>
          </el-form-item>
          <el-form-item
              label="项目简介:">
            <el-input style="width: 683px;"
                      type="textarea"
                      :autosize="{ minRows: 3, maxRows: 10}"
                      placeholder="请输入项目简介"
                v-model="projectInfo.projectIntroduce"
            ></el-input>
          </el-form-item>
          <el-form-item
              label="产品经理:">
            <el-select v-model="projectInfo.productManagerId" filterable placeholder="请选择">
              <el-option
                  v-for="item in productManagers"
                  :key="item.id"
                  :label="item.trueName"
                  :value="item.id">
              </el-option>
            </el-select>
          </el-form-item>
        </el-form>
      </div>
      <div>
        <div style="width: 20cm; margin: 0 auto;">
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
                :selectable="row => row.versionUpdate !== 'UPDATED'"
                align="center"
                width="55">
            </el-table-column>
            <el-table-column  label="序号" align="center" width="50">
              <template slot-scope="scope">
                {{scope.$index+1}}
              </template>
            </el-table-column>
            <el-table-column
                prop="characterName"
                align="center"
                label="特性名称">
            </el-table-column>
            <el-table-column
                prop="writerName"
                width="110"
                label="撰写人"
                align="center">
            </el-table-column>
            <el-table-column
                prop="iterationVersion"
                :formatter="versionNumFormat"
                align="center"
                label="特性版本"
                width="80">
            </el-table-column>
          </el-table>
        </div>
      </div>

      <div style="margin-top: 50px;">
        <div class="clearfix form-actions">
          <div style="display: flex; justify-content: center;">
            <el-button type="primary" v-if="characterList.length > 0" v-on:click="saveProjectUpdate()">
              升级开发
            </el-button>
            <el-button type="info" disabled v-if="characterList.length <= 0">
              升级开发
            </el-button>
            <el-button type="primary" v-on:click="close()">
              关闭
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
import Swal from "sweetalert2";
import {dateFormat, fixed2fractional, iterationFormat} from "@/filter/filter";
import DevVersionUtil from "@/utils/dev-version-util";
import VersionLabel from "@/shared/version-label";
import ProductOwnerPicker from "@/components/rdms/product-owner-picker";
import { postWithMaintenance } from "@/shared/dev-version-guard";

export default {
  components:{
    ProductOwnerPicker,
  },

  name: 'project-update-dlg',
  props:{
    projectId: {
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

      projectInfo: {},

      characterList: [],
      multipleSelection: [],

      productManagers: [],
      productManagerId: '',

      newVersion: '',
      bumpType: 'minor',
      productOwner: { productId: '', newProductName: '', newProductCode: '' },
    };
  },

  computed: {
  },

  mounted: function() {
    let _this =this;
    _this.getLoginUser();

    if(Tool.isNotEmpty(_this.projectId)){
      _this.getProjectAndArchivedCharacters(_this.projectId);
      _this.listProductManagerByCustomerId();
    }

  },
  methods:{
    fixed2fractional,
    getNewVersion(){
      let _this = this;
      const bump = { major: DevVersionUtil.bumpMajor, minor: DevVersionUtil.bumpMinor, patch: DevVersionUtil.bumpDevVersion }[_this.bumpType];
      const bumped = bump(_this.projectInfo.devVersion);
      _this.newVersion = bumped || '';
      return _this.newVersion;
    },

    getProjectAndArchivedCharacters(projectId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/getProjectAndArchivedCharacters/' + projectId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.projectInfo = resp.content;
          _this.characterList = resp.content.characters || [];
          _this.$nextTick(() => {
            _this.$refs.characterTable.clearSelection();
            _this.characterList.forEach(row => {
              if (row.versionUpdate !== 'UPDATED') {
                _this.$refs.characterTable.toggleRowSelection(row, true);
              }
            });
          });
          _this.getNewVersion();
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

    listProductManagerByCustomerId() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/listProductManagerByCustomerId/'+ _this.loginCustomer.id).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.productManagers = resp.content;
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    /**
     */
    saveProjectUpdate() {
      let _this = this;
      if (!_this.multipleSelection.length) {
        Toast.warning("请勾选要迭代的特性", Swal);
        return;
      }
      if (1 !== 1
          || !Validator.require(_this.projectInfo.projectName, "项目名称", Swal)
          || !Validator.require(_this.newVersion, "版本号", Swal)

          || !Validator.length(_this.projectInfo.projectName, "项目名称", 4, 30, Swal)
          || !DevVersionUtil.requireValid(_this.newVersion, "版本号", Swal)
          || (!_this.projectInfo.productId && !_this.$refs.productPicker.validate())
      ) {
        return;
      }

      Swal.fire({
        title: '确认',
        text: "是否确认创建版本升级预立项项目?",
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#409EFF',
        cancelButtonColor: '#909399',
        confirmButtonText: '确认!',
        cancelButtonText: '取消!'
      }).then((result) => {
        if (result.value) {
          postWithMaintenance(_this, process.env.VUE_APP_SERVER + '/customer/character/saveProjectUpdate', {
            projectId: _this.projectId,
            characterDtoList: _this.multipleSelection,
            customerId: _this.loginCustomer.id,
            preProjectName: _this.projectInfo.projectName,
            devVersion: String(_this.newVersion || '').trim(),
            preProjectIntroduce: _this.projectInfo.projectIntroduce,
            productManagerId: _this.projectInfo.productManagerId,
            systemManagerId: _this.projectInfo.systemManagerId,
            rdType: RD_TYPE.DEVELOPMENT.key,
            productId: _this.projectInfo.productId || _this.productOwner.productId,
            newProductName: (_this.productOwner.newProductName || '').trim(),
            newProductCode: (_this.productOwner.newProductCode || '').trim(),
          }).then((response)=>{
            let resp = response.data;
            if (resp.cancelled) {
              return;
            }
            if (resp.success) {
              _this.close();
              _this.$bus.$emit('refresh-character-tree', true);
              _this.$bus.$emit('notify-character-update-job-flag', true);
              _this.$bus.$emit('refresh-product-list', true);
              _this.getProjectAndArchivedCharacters(_this.projectId);
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

    handleSelectionChange(val) {
      let _this = this;
      _this.multipleSelection = val;
    },

    creatTimeFormat(row, column){
      return dateFormat(row.createTime);
    },

    versionNumFormat(row, column) {
      return VersionLabel.featureVersion(this.projectInfo.devVersion, row.iterationVersion)
          || iterationFormat(row.iterationVersion);
    },
  }
};
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd.scss';
.ca-form{
  font-size: 14px;
  margin-left: 30px;
}
</style>
