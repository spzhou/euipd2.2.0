<!--
 * Author: 周朔鹏
 * Date: 2024-03-20
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div style="height: 100%;">
    <div class="paper-height">
      <div style="height: 100%; overflow: scroll;">
        <div class="a4-paper-big-plain" style="width: 100%;">
          <div>
            <div style="margin: 0px 30px;">
              <div>
                <div class="ca-form-header" style="margin-bottom: 5px;">
                  <div style="font-size: 14px; font-weight: bold; margin-top: 20px;">
                    可拣选物料
                    <small><em>(备注1: 此处的单位数量等信息只具有参考意义, 在BOM编制过程中, 相关信息均可修改.)</em></small>
                  </div>
                  <div class="hr hr-6 dotted"></div>
                </div>
                <el-table
                    :data="materialList"
                    stripe
                    @selection-change="handleSelectionChange"
                    style="width: 100%">
                  <el-table-column
                      v-if="!subProject.bomStatus || subProject.bomStatus === CHARACTER_BOM_STATUS.EDIT.key"
                      type="selection"
                      width="55">
                  </el-table-column>
                  <el-table-column
                      prop="bomCode"
                      show-overflow-tooltip
                      align="center"
                      label="物料编码">
                  </el-table-column>
                  <el-table-column
                      prop="name"
                      show-overflow-tooltip
                      align="center"
                      label="名称">
                  </el-table-column>
                  <el-table-column
                      prop="model"
                      show-overflow-tooltip
                      align="center"
                      label="型号">
                  </el-table-column>
                  <el-table-column
                      prop="unit"
                      sortable
                      show-overflow-tooltip
                      align="center"
                      label="单位">
                  </el-table-column>
                  <el-table-column
                      prop="preTaxUnitPrice"
                      :formatter="unitPriceFormat"
                      sortable
                      show-overflow-tooltip
                      align="center"
                      label="单价(元)">
                  </el-table-column>
                </el-table>
              </div>
            </div>
            <div class="clearfix form-actions">
              <div style="display: flex; justify-content: center;">
                <div>
                  <div v-if="!subProject.bomStatus || subProject.bomStatus === CHARACTER_BOM_STATUS.EDIT.key">
                    <el-button type="primary" v-on:click="saveBomSelected()">提交BOM拣选</el-button>
                  </div>
                  <div v-else>
                    <el-button type="primary" plain disabled>提交BOM拣选</el-button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import JobitemDetailInfoDlg from "@/components/std/jobitem-detail-info-dlg.vue";
import {optionKV, rmbFormat} from "@/filter/filter";
import ApproveSignatureCore from "@/components/rdms/approve-signature-core.vue";
import FileSaver from 'file-saver';

export default {
  components: {
    ApproveSignatureCore,
    JobitemDetailInfoDlg,

  },
  name: "bom-selected-list-subproject-dlg",

  props: {
    subprojectId: {
      default: null,
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
      materialList: [],
      multipleSelection: [],
      materialToBom: {},

      dialogTitle: '',

      subProject: {},

      APPLICATION_STATUS: APPLICATION_STATUS,
      CHARACTER_BOM_STATUS: CHARACTER_BOM_STATUS,
    };
  },

  computed: {
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if (Tool.isNotEmpty(_this.subprojectId)) {
      _this.getSubProjectInfo(_this.subprojectId);
      _this.getDirectMaterialListForBomSelected(_this.subprojectId);
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

    getSubProjectInfo(subprojectId){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/sub-project/getSubProjectSimpleInfo/' + subprojectId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.subProject = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    handleSelectionChange(val) {
      let _this = this;
      _this.materialToBom.materialDtoList = val;
    },

    unitPriceFormat(row, column) {
      if (row.preTaxUnitPrice) {
        return rmbFormat(row.preTaxUnitPrice);
      }
      return 0;
    },

    getDirectMaterialListForBomSelected(subprojectId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/material/getDirectMaterialListForBomSelected/' + subprojectId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.materialList = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      });
    },

    saveBomSelected() {
      let _this = this;
      if(Tool.isNotEmpty(_this.materialToBom.materialDtoList) && Tool.isNotEmpty_ES5(_this.materialToBom.materialDtoList)) {
        _this.materialToBom.subprojectId = _this.subprojectId;
        _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/material/saveMaterialListToBom_subproject', _this.materialToBom).then((response)=>{
          let resp = response.data;
          if (resp.success) {
            _this.getDirectMaterialListForBomSelected(_this.subprojectId);
            _this.$bus.$emit('refresh-bom-edit-list-subproject-dlg', true);
            _this.closeDlg();
            Toast.success("保存成功", Swal);
          }
        })
      }else{
        Swal.fire({
          title: '物料选择不能为空!',
          icon: 'info',
          confirmButtonColor: '#409EFF',
          cancelButtonColor: '#909399',
          confirmButtonText: '确认!',
          cancelButtonText: '取消!'
        }).then((result) => {
          if (result.isConfirmed) {
            done();
          }
        }).catch(_ => {
        });
      }
    },

  },
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd';

.paper-height {
  height: calc(100vh - 200px);
}

</style>
