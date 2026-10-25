<!--
 * Author: 周朔鹏
 * Date: 2025-02-13
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="ca-plain-form">
    <div class="ca-form-line" style="font-weight: bold">
      <div class="ca-form-label-l">
        <div class="ca-form-label-title">
          <i class="el-icon-star-on"></i>
          项目名称:
        </div>
      </div>
      <div class="ca-form-content">
        {{ project.projectName }}
      </div>
    </div>
    <div v-if="setupStage !== SETUP_STAGE.SETUP.key" class="ca-form-line">
      <div class="ca-form-label">
        <div class="ca-form-label-title">
          项目编号:
        </div>
      </div>
      <div class="ca-form-content">
        {{ project.projectCode }}
      </div>
    </div>
    <div v-if="project.rdType !== RD_TYPE.MODULE.key" class="ca-form-line">
      <div class="ca-form-label">
        <div class="ca-form-label-title">
          开发版本:
        </div>
      </div>
      <div class="ca-form-content">
        {{ project.devVersion }}
      </div>
    </div>
    <div v-if="project.id && setupStage !== SETUP_STAGE.SETUP.key" class="ca-form-line">
      <div class="ca-form-label">
        <div class="ca-form-label-title">
          所属产品:
        </div>
      </div>
      <div class="ca-form-content">
        <span v-if="project.productId">{{ productName || '已登记' }}</span>
        <template v-else>
          <span style="color: #E6A23C;">未登记所属产品，请补选</span>
          <el-button v-if="canFillProduct && !fillingProduct" type="warning" plain size="mini" style="margin-left: 8px;"
                     @click="fillingProduct = true">补选</el-button>
          <div v-if="fillingProduct" style="width: 500px; margin-top: 6px;">
            <product-owner-picker ref="productPicker" :value="productOwner"></product-owner-picker>
            <div style="margin-top: 6px;">
              <el-button type="primary" size="mini" @click="saveProduct">保存</el-button>
              <el-button size="mini" @click="fillingProduct = false">取消</el-button>
            </div>
          </div>
        </template>
      </div>
    </div>
    <div class="ca-form-line">
      <div class="ca-form-label">
        <div class="ca-form-label-title">
          项目概要:
        </div>
      </div>
      <div class="ca-form-content">
        <div v-rich-html="project.projectIntroduce">
        </div>
      </div>
    </div>
    <div class="ca-form-line">
      <div class="ca-form-label">
        <div class="ca-form-label-title">
          项目监管:
        </div>
      </div>
      <div class="ca-form-content">
        {{ project.superviseName }}
      </div>
    </div>
    <div class="ca-form-line">
      <div class="ca-form-label">
        <div class="ca-form-label-title">
          产品经理:
        </div>
      </div>
      <div class="ca-form-content">
        {{ project.productManagerName }}
      </div>
    </div>
    <div class="ca-form-line">
      <div class="ca-form-label">
        <div class="ca-form-label-title">
          系统工程:
        </div>
      </div>
      <div class="ca-form-content">
        {{ project.systemManagerName }}
      </div>
    </div>
    <div v-if="setupStage !== SETUP_STAGE.SETUP.key">
      <div class="ca-form-line">
        <div class="ca-form-label">
          <div class="ca-form-label-title">
            项目经理:
          </div>
        </div>
        <div class="ca-form-content">
          {{ project.projectManagerName }}
        </div>
      </div>
      <div class="ca-form-line">
        <div class="ca-form-label">
          <div class="ca-form-label-title">
            测试主管:
          </div>
        </div>
        <div class="ca-form-content">
          {{ project.testManagerName }}
        </div>
      </div>
      <div class="ca-form-line">
        <div class="ca-form-label">
          <div class="ca-form-label-title">
            核心成员:
          </div>
        </div>
        <div class="ca-form-content">
          {{ project.keyMemberListStr }}
        </div>
      </div>
      <div class="ca-form-line">
        <div class="ca-form-label">
          <div class="ca-form-label-title">
            预算金额:
          </div>
        </div>
        <div class="ca-form-content">
          {{ project.budget + project.addCharge  | rmbFormat }}
        </div>
      </div>
      <div class="ca-form-line">
        <div class="ca-form-label">
          <div class="ca-form-label-title">
            激励政策:
          </div>
        </div>
        <div class="ca-form-content">
          {{ project.incentivePolicy }}
        </div>
      </div>
      <div class="ca-form-line">
        <div class="ca-form-label">
          <div class="ca-form-label-title">
            计划发布:
          </div>
        </div>
        <div class="ca-form-content">
          {{ project.releaseTimeStr }}
        </div>
      </div>
    </div>
    <div v-if="project.fileList && project.fileList.length>0">
      <div class="ca-form-line">
        <div class="ca-form-label">
          <div class="ca-form-label-link">
            附件清单:
          </div>
        </div>
      </div>
      <div v-for="(item, i ) in project.fileList" :key="item.id+'-'+i"
           style="display: flex; flex-direction: row; align-items: center; margin-left: 16px;">
        <div v-if="item">
          {{ item.name }}
        </div>
        <div v-if="item && item.authStatus">
          <el-button type="text" style="margin-left: 25px;" @click="downloadFile(item)">下载
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import FileSaver from 'file-saver';

import Swal from "sweetalert2";
import ProductOwnerPicker from "@/components/rdms/product-owner-picker";

export default {
  components: {
    ProductOwnerPicker,
  },
  name: "project-show-summary",

  props: {
    project: {
      default: null,
    },
    setupStage: {
      default: SETUP_STAGE.SETUPED.key,
    },
  },

  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},

      SETUP_STAGE: SETUP_STAGE,
      RD_TYPE: RD_TYPE,

      productName: '',
      fillingProduct: false,
      productOwner: { productId: '', newProductName: '', newProductCode: '' },
    }
  },

  computed: {
    /** 项目的产品经理补选，最终管理人可代办。 */
    canFillProduct() {
      const uid = this.loginUser && this.loginUser.customerUserId;
      const bossOrAdmin = typeof RoleAccess !== 'undefined'
          && RoleAccess.hasAny(['UltiMng'], Tool.getLoginUser().roleList);
      return bossOrAdmin || (!!uid && !!this.project && this.project.productManagerId === uid);
    },
  },

  watch: {
    'project.productId': function () {
      this.loadProductName();
    },
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    _this.loadProductName();
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

    loadProductName() {
      let _this = this;
      if (!_this.project || !_this.project.productId) {
        _this.productName = '';
        return;
      }
      _this.$ajax.get(process.env.VUE_APP_SERVER + '/customer/pre-project/product/list').then((response) => {
        let resp = response.data;
        const found = resp.success && (resp.content || []).find(item => item.id === _this.project.productId);
        _this.productName = found ? found.productName : '';
      });
    },

    saveProduct() {
      let _this = this;
      if (!_this.$refs.productPicker.validate()) {
        return;
      }
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/product/update', {
        id: _this.project.id,
        productId: _this.productOwner.productId,
        newProductName: (_this.productOwner.newProductName || '').trim(),
        newProductCode: (_this.productOwner.newProductCode || '').trim(),
      }).then((response) => {
        let resp = response.data;
        if (resp.success) {
          Toast.success(resp.message || '所属产品已登记', Swal);
          _this.fillingProduct = false;
          _this.$set(_this.project, 'productId', resp.content && resp.content.productId);
        } else {
          Toast.warning(resp.message, Swal);
        }
      });
    },

    downloadFile(file) {
      let _this = this;
      _this.getSignedUrl(file.path, file.suffix);
    },
    getSignedUrl (fileFullName, suffix) {
      let _this = this;
      let formData = new window.FormData();
      formData.append('fileFullName', fileFullName);
      formData.append('customer', _this.loginCustomer.id);
      formData.append('user', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/file/rdms/oss-get-signed-url', formData).then((response)=>{
        let resp = response.data;
        Tool.openOssSignedUrl(resp, suffix);
        // FileSaver.saveAs(resp.content, fileName);
      });
    },

  }
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd.scss';
p {
  //margin-top: 6px;
  font-size: 14px;
  margin: 0 !important;
}

.form-group {
  margin-bottom: 6px !important;
}

.control-label {
  text-align: right;
  margin: 0 !important;
  padding: 0 !important;
}
</style>
