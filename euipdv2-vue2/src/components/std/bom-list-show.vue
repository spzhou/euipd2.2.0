<!--
 * Author: 周朔鹏
 * Date: 2024-03-20
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div  id="submit-print" style="height: 100%;">
      <div style="display: flex; flex-direction: row; justify-content: center; align-items: center; font-size: 22px; font-weight: 500;">
        {{ title }}
      </div>
      <div class="hr hr-12 hr-dotted hr-double"></div>
      <el-table
          :data="bomMaterialList"
          stripe
          show-summary
          :summary-method="getSummaries"
          style="width: 900px; margin: 0 auto;">
        <el-table-column type="expand">
          <template slot-scope="props">
            <div style="margin-left: 60px;">
              <div class="ca-plain-form">
                <div class="ca-form-line">
                  <div class="ca-form-label-xxl">
                    <div class="ca-form-label-title" style="font-size: 13px; color: #99a9bf;">
                      物料编码:
                    </div>
                  </div>
                  <div class="ca-form-content">
                    {{ props.row.bomCode }}
                  </div>
                </div>
                <div class="ca-form-line">
                  <div class="ca-form-label-xxl">
                    <div class="ca-form-label-title" style="font-size: 13px; color: #99a9bf;">
                      物料名称:
                    </div>
                  </div>
                  <div class="ca-form-content">
                    {{ props.row.name }}
                  </div>
                </div>
                <div class="ca-form-line">
                  <div class="ca-form-label-xxl">
                    <div class="ca-form-label-title" style="font-size: 13px; color: #99a9bf;">
                      物料型号:
                    </div>
                  </div>
                  <div class="ca-form-content">
                    {{ props.row.model }}
                  </div>
                </div>
                <div class="ca-form-line">
                  <div class="ca-form-label-xxl">
                    <div class="ca-form-label-title" style="font-size: 13px; color: #99a9bf;">
                      未税单价:
                    </div>
                  </div>
                  <div class="ca-form-content">
                    {{ props.row.preTaxUnitPrice | rmbFormat }}
                  </div>
                </div>
                <div class="ca-form-line">
                  <div class="ca-form-label-xxl">
                    <div class="ca-form-label-title" style="font-size: 13px; color: #99a9bf;">
                      使用数量:
                    </div>
                  </div>
                  <div class="ca-form-content">
                    {{ props.row.amount }}
                  </div>
                </div>
                <div class="ca-form-line">
                  <div class="ca-form-label-xxl">
                    <div class="ca-form-label-title" style="font-size: 13px; color: #99a9bf;">
                      计量单位:
                    </div>
                  </div>
                  <div class="ca-form-content">
                    {{ props.row.unit }}
                  </div>
                </div>
                <div class="ca-form-line">
                  <div class="ca-form-label-xxl">
                    <div class="ca-form-label-title" style="font-size: 13px; color: #99a9bf;">
                      未税小计:
                    </div>
                  </div>
                  <div class="ca-form-content">
                    {{ props.row.preTaxSumPrice | rmbFormat }}
                  </div>
                </div>
                <div class="ca-form-line">
                  <div class="ca-form-label-xxl">
                    <div class="ca-form-label-title" style="font-size: 13px; color: #99a9bf;">
                      供应厂商:
                    </div>
                  </div>
                  <div class="ca-form-content">
                    {{ props.row.supplierName }}
                  </div>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column
            prop="bomCode"
            sortable
            show-overflow-tooltip
            align="center"
            label="物料编码">
        </el-table-column>
        <el-table-column
            prop="name"
            sortable
            show-overflow-tooltip
            align="center"
            label="物料名称">
        </el-table-column>
        <el-table-column
            prop="model"
            sortable
            show-overflow-tooltip
            align="center"
            label="物料型号">
        </el-table-column>
        <el-table-column
            prop="unit"
            width="100"
            sortable
            show-overflow-tooltip
            align="center"
            label="计量单位">
        </el-table-column>
        <el-table-column
            prop="amount"
            width="100"
            sortable
            show-overflow-tooltip
            align="center"
            label="使用数量">
        </el-table-column>
        <el-table-column
            prop="preTaxSumPrice"
            :formatter="sumPriceFormat"
            width="100"
            sortable
            show-overflow-tooltip
            align="center"
            label="成本小计">
        </el-table-column>
      </el-table>
    </div>
      <div style="text-align: right; margin-top: 15px;">
        <el-button v-print="print" type="primary">打印</el-button>
      </div>
  </div>
</template>

<script>
import {rmbFormat} from "@/filter/filter";
import print from 'vue-print-nb'
import Swal from "sweetalert2";

export default {
  components: {
  },

  directives: {
    print
  },

  name: "bom-list-show",

  props: {
    projectId: {
      default: null,
    },
    title: {
      default: '产品配置物料清单',
    },
    bomMaterialList: {
      default: null,
    },
  },

  data() {
    return {

      bomMaterialList_com: [],
      print: {
        id: 'submit-print',
        popTitle: '配置页眉标题', // 打印配置页上方的标题
        // extraHead: '打印', // 最上方的头部文字，附加在head标签上的额外标签，使用逗号分割
        preview: false, // 是否启动预览模式，默认是false
        // previewTitle: '预览的标题', // 打印预览的标题
        // previewPrintBtnLabel: '预览结束，开始打印', // 打印预览的标题下方的按钮文本，点击可进入打印
        zIndex: 20002, // 预览窗口的z-index，默认是20002，最好比默认值更高
        // previewBeforeOpenCallback () { console.log('正在加载预览窗口！'); console.log(that.msg, this) }, // 预览窗口打开之前的callback
        // previewOpenCallback () { console.log('已经加载完预览窗口，预览打开了！') }, // 预览窗口打开时的callback
        // beforeOpenCallback () { console.log('开始打印之前！') }, // 开始打印之前的callback
        // openCallback () { console.log('执行打印了！') }, // 调用打印时的callback
        // closeCallback () { console.log('关闭了打印工具！') }, // 关闭打印的callback(无法区分确认or取消)
        // clickMounted () { console.log('点击v-print绑定的按钮了！') },
        // url: 'http://localhost:8080/', // 打印指定的URL，确保同源策略相同
        // asyncUrl (reslove) {
        //   setTimeout(() => {
        //     reslove('http://localhost:8080/')
        //   }, 2000)
        // },
        standard: '',
        extarCss: ''
      }
    };
  },

  mounted: function () {
    let _this = this;
    _this.getCommonBomMaterialListByProjectId(_this.projectId);
  },

  methods: {
    unitPriceFormat(row, column) {
      if (row.preTaxUnitPrice) {
        return rmbFormat(row.preTaxUnitPrice);
      }
      return 0;
    },

    sumPriceFormat(row, column) {
      if (row.preTaxSumPrice) {
        return rmbFormat(row.preTaxSumPrice);
      }
      return 0;
    },

    getCommonBomMaterialListByProjectId(projectId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/material/getCommonBomMaterialListByProjectId/' + projectId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.bomMaterialList_com = resp.content;
          _this.bomMaterialList.push.apply(_this.bomMaterialList, _this.bomMaterialList_com);
        } else {
          Toast.warning(resp.message, Swal);
        }
      });
    },

    getSummaries(param) {
      let _this = this;
      const { columns, data } = param;
      const sums = [];
      columns.forEach((column, index) => {
        if (index === 1) {
          sums[index] = '总计';
          return;
        }
        if (index === 0 || index === 2 || index === 3 || index === 4 || index === 7) {
          sums[index] = '';
          return;
        }
        const values = data.filter(item => item.itemType !== BOM_ITEM_TYPE.CBB.key).map(item => Number(item[column.property]));
        if (!values.every(value => isNaN(value))) {
          sums[index] = values.reduce((prev, curr) => {
            const value = Number(curr);
            if (!isNaN(value)) {
              return prev + curr;
            } else {
              return prev;
            }
          }, 0);
          if(index === 6){
            sums[index] = rmbFormat(sums[index]);
          }
        } else {
          sums[index] = 'N/A';
        }
      });
      return sums;
    }
  },
}
</script>

<style>
/*
***设置打印内容样式，不影响页面原有样式
***以下内容为设置element table在打印时的样式，防止打印不全*/
@page {
  size: auto;
  margin: 3mm;
}
@media print {
  html {
    background-color: #ffffff;
    height: auto;
    margin: 0;
  }
  body {
    border: solid 1px #ffffff;
    margin: 50px auto;
  }
  #print table {
    table-layout: auto !important;
  }
  #print .el-table__header-wrapper .el-table__header {
    width: 100% !important;
    border: solid 1px #f2f2f2;
  }
  #print .el-table__body-wrapper .el-table__body {
    width: 100% !important;
    border: solid 1px #f2f2f2;
  }
  #print #pagetable table {
    table-layout: fixed !important;
  }
  #print .el-table__empty-block {
    width: 100% !important;
    height: auto !important;
  }
  #print .el-input-number--small{
    width: 100% !important;
  }
}
</style>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd';

</style>
