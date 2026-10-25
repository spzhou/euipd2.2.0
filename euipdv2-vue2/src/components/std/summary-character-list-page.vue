<!--
 * Author: 周朔鹏
 * Date: 2024-06-27
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div style="height: 100%;">
    <div class="paper-height">
      <div style="height: 100%; overflow: scroll;">
        <div class="a4-paper-big-simple">
          <div style="margin: 50px 60px;">
            <small style="float: right; margin-bottom: 15px; margin-right: 10px;"> <em>操作指引: 点击条目可以查看详细信息.</em></small>
            <el-table
                :data="allCharacterList"
                row-key="id"
                stripe
                highlight-current-row
                @row-click="rowClick"
                style="width: 100%">
              <el-table-column   label="序号" align="center" width="50">
                <template slot-scope="scope">
                  {{scope.$index + 1}}
                </template>
              </el-table-column>
              <el-table-column
                  prop="itemName"
                  align="center"
                  show-overflow-tooltip
                  width="140"
                  label="特性名称">
              </el-table-column>
              <el-table-column
                  prop="sumBudgetApproved"
                  :formatter="sumBudgetApprovedFormat"
                  align="center"
                  show-overflow-tooltip
                  width="100"
                  label="总预算">
              </el-table-column>
              <el-table-column
                  prop="sumBudgetActive"
                  :formatter="sumBudgetActiveFormat"
                  align="center"
                  show-overflow-tooltip
                  width="100"
                  label="总支出">
              </el-table-column>
              <el-table-column
                  prop="budgetRate"
                  :formatter="budgetRateFormat"
                  align="center"
                  show-overflow-tooltip
                  width="100"
                  label="预算执行率">
              </el-table-column>
              <el-table-column
                  prop="devManhourActive"
                  align="center"
                  show-overflow-tooltip
                  width="90"
                  label="开发工时">
              </el-table-column>
              <el-table-column
                  prop="testManhourActive"
                  align="center"
                  show-overflow-tooltip
                  width="90"
                  label="测试工时">
              </el-table-column>
              <el-table-column
                  prop="materialFeeActive"
                  :formatter="materialFeeActiveFormat"
                  align="center"
                  show-overflow-tooltip
                  width="100"
                  label="物料费">
              </el-table-column>
              <el-table-column
                  prop="otherFeeActive"
                  :formatter="otherFeeActiveFormat"
                  align="center"
                  show-overflow-tooltip
                  width="100"
                  label="其他杂费">
              </el-table-column>
              <el-table-column
                  fixed="right"
                  label="操作"
                  align="center"
                  width="100">
                <template slot-scope="scope">
                  <div>
                    <div style="display: flex; flex-direction: row; justify-content: center;">
                      <div>
                        <el-tooltip content="对已经归档的档案进行查看" placement="top"
                                    effect="light" :open-delay=1000>
                          <el-button style="margin: 0 5px;"
                                     type="primary"
                                     plain
                                     size="small"
                                     @click.stop="showArchiveDialog(scope.row)">
                            执行数据
                          </el-button>
                        </el-tooltip>
                      </div>
                    </div>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </div>
    </div>

    <!--显示任务过程信息-->
    <el-dialog
        width="29cm"
        :visible.sync="characterReviewInfoDlg"
        :before-close="handleClose"
        append-to-body
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div>
        <CharacterDocInfoPage
            :key="characterExeInfo.id"
            :character-id="characterExeInfo.id">
        </CharacterDocInfoPage>
      </div>
    </el-dialog>

    <!--执行数据-->
    <el-dialog
        :title="((characterItem.itemName) + ' 特性开发档案')"
        width="32cm"
        :visible.sync="characterArchiveVisible"
        :before-close="handleClose"
        :close-on-click-modal=true>
      <div>
        <CharacterArchiveDlg
            :key="characterItem.id"
            :character-id="characterItem.id"
        ></CharacterArchiveDlg>
      </div>
      <div style="width: 100%; height: 50px;"></div>
    </el-dialog>

  </div>
</template>

<script>
import Swal from "sweetalert2";
import JobitemDetailInfoDlg from "@/components/std/jobitem-detail-info-dlg.vue";
import { rmbFormat} from "@/filter/filter";
import CharacterDocInfoPage from "@/components/std/character-doc-info-page.vue";
import CharacterArchiveDlg from "@/components/std/character-archive-dlg.vue";

export default {
  components: {
    CharacterArchiveDlg,
    CharacterDocInfoPage,
    JobitemDetailInfoDlg,

  },
  name: "summary-character-list-page",

  props: {
    subprojectId:{
      default: null,
    },
  },

  computed: {

  },

  data() {
    return {
      loginUser: {},
      loginCustomer: {},
      allCharacterList: [],
      characterItem: {},
      characterArchiveVisible: false,
      characterReviewInfoDlg: false,
      dialogTitle: '',

      jobItem: {},
      characterExeInfo: {},

    };
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if (Tool.isNotEmpty(_this.subprojectId)) {
      _this.getCharacterListBySubprojectId(_this.subprojectId);
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

    sumBudgetApprovedFormat(row, column){
      return rmbFormat(row.sumBudgetApproved);
    },

    sumBudgetActiveFormat(row, column){
      return rmbFormat(row.sumBudgetActive);
    },

    materialFeeActiveFormat(row, column){
      return rmbFormat(row.materialFeeActive);
    },

    otherFeeActiveFormat(row, column){
      return rmbFormat(row.otherFeeActive);
    },

    budgetRateFormat(row, column){
      return (row.budgetRate * 100).toFixed(1) + "%";
    },

    showArchiveDialog(row, column, event) {
      let _this = this;
      _this.characterItem = row;
      _this.characterArchiveVisible = true;
    },

    getCharacterListBySubprojectId(subprojectId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/sub-project/getCharacterListBySubprojectId/' + subprojectId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.allCharacterList = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      });
    },

    rowClick(row, column, e){
      let _this = this;
      _this.characterExeInfo = row;
      _this.characterReviewInfoDlg = true;
      _this.dialogTitle = "特性文档汇总";
    },

    handleClose(done) {
      let _this = this;
      _this.characterReviewInfoDlg = false;
      _this.characterArchiveVisible = false;
      done();
    },

    closeDialog() {
      let _this = this;
      _this.characterReviewInfoDlg = false;
      _this.characterArchiveVisible = false;
    },

  },
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd';

.paper-height {
  height: calc(100vh - 290px);
}

</style>
