<!--
 * Author: 周朔鹏
 * Date: 2024-04-27
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div style="height: 100%;">
    <div class="paper-height">
      <div style="height: 100%; overflow: scroll;">
        <div class="a4-paper-big-simple">
          <div style="margin: 50px 60px;">
            <div style="font-size: 14px; line-height: 28px; margin: 5px 0; font-weight: bold">
              项目综合信息一览表:
            </div>
            <div>
              <div class="hr hr-9 dotted"></div>
              <div>
                <SubprojectShowSummary
                    :key="subProject.id"
                    :sub-project="subProject"
                ></SubprojectShowSummary>
              </div>
              <div v-if="subProject.rdType !== RD_TYPE.RESEARCH.key" style="margin: 10px 10px;">
                <div class="hr hr-6 hr-dotted"></div>
                <div>
                  <div style="font-size: 14px; line-height: 28px; margin: 5px 0; font-weight: 600">
                    各级项目与特性开发预算执行情况:
                  </div>
                  <el-table
                      :data="budgetExeList"
                      style="width: 100%;margin-bottom: 20px;"
                      row-key="id"
                      border>
                    <el-table-column
                        prop="reviewTime"
                        :formatter="reviewTimeFormat"
                        align="center"
                        show-overflow-tooltip
                        width="100"
                        label="评审时间">
                    </el-table-column>
                    <el-table-column
                        prop="itemName"
                        align="center"
                        show-overflow-tooltip
                        width="150"
                        label="名称">
                    </el-table-column>
                    <el-table-column
                        prop="docType"
                        :formatter="docTypeFormat"
                        align="center"
                        show-overflow-tooltip
                        width="80"
                        label="类型">
                    </el-table-column>
                    <el-table-column
                        prop="sumBudgetApproved"
                        :formatter="sumBudgetApprovedFormat"
                        show-overflow-tooltip
                        width="110"
                        align="center"
                        label="总预算">
                    </el-table-column>
                    <el-table-column
                        prop="sumBudgetActive"
                        :formatter="sumBudgetActiveFormat"
                        show-overflow-tooltip
                        width="110"
                        align="center"
                        label="已执行">
                    </el-table-column>
                    <el-table-column
                        prop="budgetRate"
                        :formatter="budgetRateFormat"
                        align="center"
                        width="90"
                        label="预算执行率(%)">
                    </el-table-column>
                    <el-table-column
                        prop="devManhourApproved"
                        :formatter="devManhourApprovedFormat"
                        align="center"
                        width="80"
                        label="开发工时(预算)">
                    </el-table-column>
                    <el-table-column
                        prop="devManhourActive"
                        :formatter="devManhourActiveFormat"
                        align="center"
                        width="80"
                        label="开发工时(已执行)">
                    </el-table-column>
                    <el-table-column
                        prop="testManhourApproved"
                        :formatter="testManhourApprovedFormat"
                        align="center"
                        width="80"
                        label="测试工时(预算)">
                    </el-table-column>
                    <el-table-column
                        prop="testManhourActive"
                        align="center"
                        :formatter="testManhourActiveFormat"
                        width="80"
                        label="测试工时(已执行)">
                    </el-table-column>
                    <el-table-column
                        prop="materialFeeApproved"
                        :formatter="materialApproveFormat"
                        width="100"
                        align="center"
                        label="物料费(预算)">
                    </el-table-column>
                    <el-table-column
                        prop="materialFeeActive"
                        :formatter="materialActiveFormat"
                        width="100"
                        align="center"
                        label="物料费(实际)">
                    </el-table-column>
                    <el-table-column
                        prop="otherFeeApproved"
                        :formatter="feeApproveFormat"
                        width="110"
                        align="center"
                        label="其他费用 (预算)">
                    </el-table-column>
                    <el-table-column
                        prop="otherFeeActive"
                        :formatter="feeActiveFormat"
                        width="110"
                        align="center"
                        :label="('其他费用(实际)')">
                    </el-table-column>
                  </el-table>
                  <div style="width: 100%; text-align: right;">
                    <i class="el-icon-info"></i>
                    <small style="margin-left: 3px;">备注: "总预算"和"已执行"上级子项目中包含下级子项目; 其他各项数据为相应特性或子项目本身的执行情况, 不互相包含.</small>
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
import {fixed2fractional, optionKV, rmbFormat, shortDateFormat} from "@/filter/filter";
import ApproveSignatureCore from "@/components/rdms/approve-signature-core.vue";
import FileSaver from 'file-saver';
import FileRdms from "@/components/rdms/file-rdms.vue";
import CharacterDocInfoPage from "@/components/std/character-doc-info-page.vue";
import SubprojectShowSummary from "@/components/rdms/subproject-show-summary.vue";

export default {
  components: {
    SubprojectShowSummary,
    CharacterDocInfoPage,
    FileRdms,
    ApproveSignatureCore,
    JobitemDetailInfoDlg,

  },
  name: "subproject-summary-page",

  props: {
    subprojectId: {
      default: null,
    },
  },

  computed: {
  },

  data() {
    return {
      loginUser: {},
      loginCustomer: {},
      subProject: {},

      budgetExeList: [],

      subprojectExeInfo: [],
      characterExeInfoList: [],

      capitalizationMaterialList: [],
      expenditureMaterialList: [],
      borrowMaterialList: [],

      materialCheckFileList: [],

      characterDocInfoDlg: false,
      dialogTitle: '',

      characterExeInfo: {},

      reviewInfo: {},
      JOB_ITEM_STATUS: JOB_ITEM_STATUS,
      RD_TYPE: RD_TYPE,
    };
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if (Tool.isNotEmpty(_this.subprojectId)) {
      _this.getSubProjectInfo(_this.subprojectId);
      _this.getProjectAllReviewInfoList(_this.subprojectId);
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

    sumBudgetApprovedFormat(row, column) {
      if (row.sumBudgetApproved) {
        return rmbFormat(row.sumBudgetApproved);
      }
      return 0;
    },

    sumBudgetActiveFormat(row, column) {
      if (row.sumBudgetActive) {
        return rmbFormat(row.sumBudgetActive);
      }
      return 0;
    },

    materialApproveFormat(row, column) {
      if (row.materialFeeApproved) {
        return rmbFormat(row.materialFeeApproved);
      }
      return 0;
    },

    materialActiveFormat(row, column) {
      if (row.materialFeeActive) {
        return rmbFormat(row.materialFeeActive);
      }
      return 0;
    },

    feeApproveFormat(row, column) {
      if (row.otherFeeApproved) {
        return rmbFormat(row.otherFeeApproved);
      }
      return 0;
    },

    feeActiveFormat(row, column) {
      if (row.otherFeeActive) {
        return rmbFormat(row.otherFeeActive);
      }
      return 0;
    },

    devManhourApprovedFormat(row, column) {
      if (row.devManhourApproved) {
        return row.devManhourApproved.toFixed(2);  //保留两位小数
      }
      return 0;
    },

    devManhourActiveFormat(row, column) {
      if (row.devManhourActive) {
        return row.devManhourActive.toFixed(2);  //保留两位小数
      }
      return 0;
    },

    testManhourApprovedFormat(row, column) {
      if (row.testManhourApproved) {
        return row.testManhourApproved.toFixed(2);  //保留两位小数
      }
      return 0;
    },

    testManhourActiveFormat(row, column) {
      if (row.testManhourActive) {
        return row.testManhourActive.toFixed(2);  //保留两位小数
      }
      return 0;
    },

    typeFormat(row, column) {
      return optionKV(JOB_ITEM_TYPE, row.type);
    },

    docTypeFormat(row, column) {
      return optionKV(DOC_TYPE, row.docType);
    },

    itemBudgetFormat(row, column) {
      if (row.itemBudget) {
        return rmbFormat(row.itemBudget);
      }
      return 0;
    },
    reviewTimeFormat(row, column){
      return shortDateFormat(row.reviewTime);
    },
    activeChargeFormat(row, column) {
      if (row.activeCharge) {
        return rmbFormat(row.activeCharge);
      }
      return 0;
    },

    budgetRateFormat(row, column) {
      if (row.budgetRate) {
        return (row.budgetRate * 100).toFixed(2);  //保留两位小数
      }
      return 0;
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

    getProjectAllReviewInfoList(subProjectId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/sub-project/getProjectAllReviewInfoList/' + subProjectId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.budgetExeList = resp.content;
        }else{
          Toast.warning(resp.message, Swal);
        }
      });
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
