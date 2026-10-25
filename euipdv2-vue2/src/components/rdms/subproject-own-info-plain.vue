<!--
 * Author: 周朔鹏
 * Date: 2025-02-05
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <el-row>
      <el-col>
        <div class="ca-plain-form" style="border-left: dotted 1px;">
          <div class="ca-form-line" style="font-weight: bold">
            <div class="ca-form-label-xl" style="line-height: 38px;">
              <div class="ca-form-label-title">
                项目名称:
              </div>
            </div>
            <div class="ca-form-content">
              <div style="display: flex; flex-direction: row; justify-content: space-between; align-items: center;">
                <div style="line-height: 32px;">
                  {{ subProject.label }}
                </div>
              </div>
            </div>
          </div>
          <div class="ca-form-line">
            <div class="ca-form-label-xl">
              <div class="ca-form-label-title">
                项目编号:
              </div>
            </div>
            <div class="ca-form-content">
              {{ subProject.projectCode }}
            </div>
          </div>
          <div class="ca-form-line">
            <div class="ca-form-label-xl">
              <div class="ca-form-label-title">
                项目概要:
              </div>
            </div>
            <div class="ca-form-content">
              <div v-rich-html="subProject.introduce">
              </div>
            </div>
          </div>
          <div class="ca-form-line">
            <div class="ca-form-label-xl">
              <div class="ca-form-label-title">
                管理团队:
              </div>
            </div>
            <div class="ca-form-content">
              项目经理: {{ subProject.projectManagerName }} ;&nbsp;&nbsp; 产品经理: {{ subProject.productManagerName }}
              ;&nbsp;&nbsp; 测试主管: {{ subProject.testManagerName }}
            </div>
          </div>
          <div class="ca-form-line">
            <div class="ca-form-label-xl">
              <div class="ca-form-label-title">
                开发预算:
              </div>
            </div>
            <div class="ca-form-content">
              {{ subProject.budget | rmbFormat }}
            </div>
          </div>
          <div v-if=" subProject.rdType!==RD_TYPE.MODULE.key" class="ca-form-line">
            <div class="ca-form-label-xl">
              <div class="ca-form-label-title">
                直接预算:
              </div>
            </div>
            <div class="ca-form-content">
              {{ subProject.addCharge | rmbFormat }}
            </div>
          </div>
          <div class="ca-form-line">
            <div class="ca-form-label-xl">
              <div class="ca-form-label-title">
                总预算:
              </div>
            </div>
            <div class="ca-form-content">
              {{ subProject.budget + subProject.addCharge | rmbFormat }}
            </div>
          </div>
          <div v-if="subProject.rdType!==RD_TYPE.MODULE.key" class="ca-form-line">
            <div class="ca-form-label-xl">
              <div class="ca-form-label-title">
                激励政策:
              </div>
            </div>
            <div class="ca-form-content">
              <div style="word-wrap: break-word;" v-rich-html="subProject.incentivePolicy"></div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import Swal from "sweetalert2";

export default {
  components: {
  },

  name: "subproject-own-info-plain",

  props: {
    subprojectId: {
      default: null,
    },

  },

  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},

      RD_TYPE: RD_TYPE,
      subProject: {},
    }
  },

  computed: {
    isMainProject() {
      let _this = this;
      if (_this.subProject.id === _this.subProject.projectId) {
        return true;
      } else {
        return false;
      }
    },

  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if (Tool.isNotEmpty(_this.subprojectId)) {
       _this.getSubProjectInfo(_this.subprojectId);
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

    getSubProjectInfo(subProjectId){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/sub-project/getSubProjectSimpleInfo/' + subProjectId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.subProject = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },
  }
}
</script>

<style lang="scss" scoped>
@import "./../../../public/static/scss/caipd";
</style>
