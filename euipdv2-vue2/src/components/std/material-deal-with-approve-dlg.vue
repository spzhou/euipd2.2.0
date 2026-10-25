<!--
 * Author: 周朔鹏
 * Date: 2025-02-26
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div id="container" ref="printRef">
      <div class="a4-paper-print">
        <div class="ca-form">
          <div class="ca-form-header">
            <div class="ca-form-header-line">
              <div class="ca-form-header-line-label">
                <span>申请单号:</span>
              </div>
              <div class="ca-form-header-line-content" style="margin-left: 8px;">
                {{ dialogInfo.code }}
              </div>
            </div>
<!--            <div class="hr hr-6 dotted"></div>-->
          </div>
          <div class="ca-form-title">
            <div class="ca-form-title-line">
              物料处置审批单
            </div>
            <div class="hr hr-6 dotted"></div>
          </div>
          <table class="ca-table">
            <tr class="ca-table-header">
              <th class="ca-table-header-column-cell" style="width: 40px;">
                序号
              </th>
              <th class="ca-table-header-column-cell" style="width: 180px;">
                物料编码
              </th>
              <th class="ca-table-header-column-cell" style="width: 180px;">
                物料名称
              </th>
              <th class="ca-table-header-column-cell" style="width: 100px;">
                型号
              </th>
              <th class="ca-table-header-column-cell" style="width: 65px;">
                单位
              </th>
              <th class="ca-table-header-column-cell" style="width: 65px;">
                数量
              </th>
              <th class="ca-table-header-column-cell" style="width: 85px;">
                单价/元
              </th>
              <th class="ca-table-header-column-cell" style="width: 85px;">
                小计/元
              </th>
            </tr>
            <tr class="ca-table-content" v-for="(item, index) in materialList" :key="index">
              <td class="ca-table-content-column-cell" style="width: 40px;">
                {{ index + 1 }}
              </td>
              <td class="ca-table-content-column-cell" style="width: 180px;">
                {{ item.bomCode }}
              </td>
              <td class="ca-table-content-column-cell" style="width: 180px;">
                {{ item.name }}
              </td>
              <td class="ca-table-content-column-cell" style="width: 100px;">
                {{ item.model }}
              </td>
              <td class="ca-table-content-column-cell" style="width: 65px;">
                {{ item.unit }}
              </td>
              <td class="ca-table-content-column-cell" style="width: 65px;">
                {{ item.amount }}
              </td>
              <td class="ca-table-content-column-cell" style="width: 85px;">
                {{ item.preTaxUnitPrice }}
              </td>
              <td class="ca-table-content-column-cell" style="width: 85px;">
                {{ item.preTaxSumPrice }}
              </td>
            </tr>
            <tr class="ca-table-sum">
              <td class="ca-table-sum-column-cell">
                总计
              </td>
              <td class="ca-table-sum-column-cell">
              </td>
              <td class="ca-table-sum-column-cell">
              </td>
              <td class="ca-table-sum-column-cell">
              </td>
              <td class="ca-table-sum-column-cell">
              </td>
              <td class="ca-table-sum-column-cell">
              </td>
              <td class="ca-table-sum-column-cell">
              </td>
              <td class="ca-table-sum-column-cell">
                {{ getSumPrice | rmbFormat }}
              </td>
            </tr>
          </table>
          <div class="ca-form-content">
            <div class="ca-form-content-line">
              <div class="ca-form-content-line-label">
                使用方式:
              </div>
              <div class="ca-form-content-line-content">
                {{ MATERIAL_USAGE_MODE | optionKV(dialogInfo.usageMode) }}
              </div>
            </div>
            <div class="ca-form-comment-line-content">
              <div v-if="dialogInfo.usageMode===MATERIAL_USAGE_MODE.EXPENDITURE.key">
                说明: "费用化"是指上述物料将在研发工作中消耗掉, 作为项目成本记账.
              </div>
              <div v-if="dialogInfo.usageMode===MATERIAL_USAGE_MODE.CAPITALIZATION.key">
                说明: "资产化"是指上述物料将被转化为公司固定资产或者成本被开发的产品的一部分. 如果转化为公司固定资产,
                将在评审后归还, 由公司资产部门确认; 如果作为被开发产品的一部分, 则将在评审后, 交付给相关部门, 由接收部门核验;
                如果项目宣告研发失败, 则需要评估上述物料是否可回收, 由相关部门评审后, 决定项目物料成本计入.
              </div>
              <div v-if="dialogInfo.usageMode===MATERIAL_USAGE_MODE.BORROW.key">
                说明: "借用"是指上述物料将被研发人员临时使用. 主要指相关工具、设备、临时物料等. 上述物料将在评审前归还,
                根据实际情况和相关公司制度, 确定损耗或折旧后, 进行成本入账. 一般要求, A项目评审前, 需归还A项目所借用的项目物资,
                B项目再用时, 在B项目下重新借用, 以便成本归集和管理.
              </div>
            </div>
            <div class="ca-form-content-line">
              <div class="ca-form-content-line-label">
                <div v-if="dialogInfo.opeStatus === MATERIAL_OPE_STATUS.RETURN_APPROVED.key">
                  退料原因:
                </div>
                <div v-else>
                  领用事由:
                </div>
              </div>
              <div class="ca-form-content-line-content">
                {{ dialogInfo.reason }}
              </div>
            </div>
            <div class="ca-form-content-line">
              <div class="ca-form-content-line-label">
                审批意见:
              </div>
              <div class="ca-form-content-line-content">
                {{ dialogInfo.approveDescription }}
              </div>
            </div>
          </div>
          <div class="hr hr-6 dotted"></div>
          <div class="ca-form-footer">
              <div class="ca-form-footer-line">
                <div style="margin-left: 100px; width: 70px; display: inline-block;">
                  审批人:
                </div>
                <div style="margin-left: 5px; width: 80px; display: inline-block;">
                  {{ dialogInfo.writerName }}
                </div>
                <div style="margin-left: 100px; width: 80px; display: inline-block;">
                  审批时间:
                </div>
                <div style="margin-left: 5px;width: 180px; display: inline-block;">
                  {{ dialogInfo.approveTime | dateFormat }}
                </div>
              </div>
          </div>
          <div class="hr hr-6 dotted hr-double"></div>
          <div>
            <div>
              <div style="line-height: 28px;">
                <div v-if="dialogInfo.opeStatus === MATERIAL_OPE_STATUS.RETURN_APPROVED.key">
                  物料退料审批表扫描件:
                </div>
                <div v-else>
                  物料申领审批表扫描件:
                </div>
              </div>
              <div v-for="(item, index) in dialogInfo.approveFileList" :key="index" style="line-height: 25px; margin-left: 60px;">
                《{{ item.name }}》
              </div>
            </div>
            <div>
              <div style="line-height: 28px;">
                物料处置相关签署文件:
              </div>
              <div v-for="(item, index) in dialogInfo.dealWithFileList" :key="index" style="line-height: 25px; margin-left: 60px;">
                《{{ item.name }}》
              </div>
            </div>
          </div>
          <div class="hr hr-6 dotted hr-double"></div>
          <div style="margin-top: 30px; margin-bottom: 5px;">
            使用说明: 请自行打印此审批单, 经相关领导审批后, 到"物料处置"页面上传并提交. 审批完成后, 将解除您对相关物料的管理责任.
          </div>
          <ApproveSignatureCore
              :serial-no="1"
          ></ApproveSignatureCore>
          <ApproveSignatureCore
              :serial-no="2"
          ></ApproveSignatureCore>
          <ApproveSignatureCore
              :serial-no="3"
          ></ApproveSignatureCore>
          <div class="hr hr-18 dotted hr-double"></div>
          <div id="no-print" class="no-print">
            <div style="display: flex; flex-direction: row; justify-content: end; margin-top: 30px;padding-bottom: 20px;">
              <button type="button" class="ca-btn-primary-sm" @click="toPrint()">打印</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import {dateFormat, optionKV, rmbFormat} from "@/filter/filter";
import MaterialAddDlg from "@/components/std/material-add-dlg.vue";
import ApproveSignatureCore from "@/components/rdms/approve-signature-core.vue";

export default {
  components: {
    MaterialAddDlg,
    ApproveSignatureCore,
  },
  name: "material-deal-with-approve-dlg",

  props: {
    applicationCode: {
      default: null
    },
    closeDlg: {
      type: Function,
      default: null
    },
  },

  data() {
    return {
      dialogInfo: {
        code: '',
      },
      materialList: [],
      MATERIAL_USAGE_MODE: MATERIAL_USAGE_MODE,
      APPLICATION_STATUS: APPLICATION_STATUS,
      MATERIAL_OPE_STATUS: MATERIAL_OPE_STATUS,

      loginUser: {},
      loginCustomer: {},
      jobItem: {},

    };
  },

  computed: {
    getNowTime() {
      return dateFormat(new Date());
    },
    getMaterialListStatus() {
      let _this = this;
      return Tool.isNotEmpty_ES5(_this.materialList);
    },
    getSumPrice() {
      let _this = this;
      let sums = 0;
      const values = _this.materialList.map(item => Number(item.preTaxSumPrice));
      if (!values.every(value => isNaN(value))) {
        sums = values.reduce((prev, curr) => {
          const value = Number(curr);
          if (!isNaN(value)) {
            return prev + curr;
          } else {
            return prev;
          }
        }, 0);
      } else {
        sums = 0;
      }
      return sums;
    },
  },

  mounted: function () {
    let _this = this;

    _this.getLoginUser();
    if (Tool.isNotEmpty(_this.applicationCode)) {
      _this.getMaterialApplicationByCode(_this.applicationCode);
    }
  },

  methods: {
    toPrint () {
      let _this = this;
      // _this.updateMaterialManageAuxStatus(_this.applicationCode);
      _this.$print(this.$refs.printRef);

    },

    // updateMaterialManageAuxStatus(code){
    //   let _this = this;
    //   _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/material/updateMaterialManageAuxStatus/' + code).then((response) => {
    //     let resp = response.data;
    //     if (resp.success) {
    //       _this.$bus.$emit('refresh-material-application-list', true);
    //     }else{
    //       Toast.warning(resp.message, Swal);
    //     }
    //   });
    // },

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

    priceChange(item) {
      let _this = this;
      _this.materialList.forEach(material => {
        if (material.id === item.id) {
          material.preTaxSumPrice = material.amount * material.preTaxUnitPrice;
        }
      });
    },

    getMaterialApplicationByCode(code) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/material/getMaterialApplicationByCode/' + code).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.dialogInfo = resp.content;
          _this.materialList = _this.dialogInfo.materialList;
        } else {
          Toast.warning(resp.message, Swal);
        }
      });
    },

    /**
     * 审批
     */
    approve(flag) {
      let _this = this;
      // 保存校验
      if (1 !== 1
          || !Validator.require(_this.dialogInfo.code, "申请单号", Swal)
          || !Validator.require(_this.dialogInfo.approveDescription, "审批意见", Swal)

          || !Validator.length(_this.dialogInfo.approveDescription, "审批意见", 10, 500, Swal)
      ) {
        return;
      }
      let priceFlag = true;
      if (flag === 'approve' && Tool.isNotEmpty_ES5(_this.materialList)) {
        _this.materialList.forEach(item => {
          if (1 !== 1
              || !Validator.require(item.preTaxSumPrice, "物料价格", Swal)
          ) {
            priceFlag = false;
          }
        })
      }

      if (priceFlag) {
        _this.dialogInfo.materialList = _this.materialList;
        if (flag === 'approve') {
          _this.dialogInfo.approveStatus = APPLICATION_STATUS.APPROVED.key;
        } else {
          _this.dialogInfo.approveStatus = APPLICATION_STATUS.REJECT.key;
        }
        _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/material/approveMaterialApplication', _this.dialogInfo).then((response) => {
          let resp = response.data;
          if (resp.success) {
            _this.close();
            _this.$bus.$emit('refresh-character-material-list', true);
            _this.$bus.$emit('refresh-develop-character-list', true);
            _this.$bus.$emit('notify-develop-count-flag', true);  //研发管理
            Toast.success("申请已发出", Swal);
          }
        })
      }
    },

    close() {
      let _this = this;
      _this.closeDlg();
    },

    getSummaries(param) {
      const {columns, data} = param;
      const sums = [];
      columns.forEach((column, index) => {
        if (index === 0) {
          sums[index] = '总价';
          return;
        }
        if (index === 1 || index === 2 || index === 3 || index === 4 || index === 5 || index === 6) {
          sums[index] = '';
          return;
        }
        const values = data.map(item => Number(item[column.property]));
        if (!values.every(value => isNaN(value))) {
          sums[index] = values.reduce((prev, curr) => {
            const value = Number(curr);
            if (!isNaN(value)) {
              return prev + curr;
            } else {
              return prev;
            }
          }, 0);
          sums[index] = rmbFormat(sums[index]);
        } else {
          sums[index] = 'N/A';
        }
      });
      return sums;
    },

  },
}
</script>

<style>
.el-input--mini .el-input__inner {
  height: 28px;
  line-height: 28px;
  text-align: center !important;
}
</style>

<style lang="scss" scoped>
@import '../../../public/static/scss/caipd';

.text-box {
  display: inline-block;
  padding-left: 3px;
  padding-right: 15px;
  line-height: 30px;
  //width: 100px;
  background-color: rgba(204, 204, 204, 0.20);
}

.dialog-width {
  width: 100%;
}

.red-color {
  color: red;
}
</style>
