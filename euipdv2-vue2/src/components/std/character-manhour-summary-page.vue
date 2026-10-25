<!--
 * Author: 周朔鹏
 * Date: 2024-04-02
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
              开发人员工时信息统计:
            </div>
            <el-table
                :data="manhourSummaryList"
                style="width: 100%;margin-bottom: 20px;"
                row-key="id"
                show-summary
                border>
              <el-table-column
                  prop="workerName"
                  align="center"
                  label="姓名">
              </el-table-column>
              <el-table-column
                  prop="devJobNum"
                  align="center"
                  label="开发任务数">
              </el-table-column>
              <el-table-column
                  prop="devJobSumManhour"
                  :formatter="devJobSumManhourFormat"
                  align="center"
                  label="开发工时">
              </el-table-column>
              <el-table-column
                  prop="taskJobNum"
                  align="center"
                  label="任务任务数">
              </el-table-column>
              <el-table-column
                  prop="taskJobSumManhour"
                  :formatter="taskJobSumManhourFormat"
                  align="center"
                  label="任务工时">
              </el-table-column>
              <el-table-column
                  prop="testJobNum"
                  align="center"
                  label="测试任务数">
              </el-table-column>
              <el-table-column
                  prop="testJobSumManhour"
                  :formatter="testJobSumManhourFormat"
                  align="center"
                  label="测试工时">
              </el-table-column>
              <el-table-column
                  prop="sumJobNum"
                  align="center"
                  label="任务总数">
              </el-table-column>
              <el-table-column
                  prop="sumManhour"
                  align="center"
                  label="工时总数">
              </el-table-column>
            </el-table>
            <div style="display: flex; flex-direction: column; align-items: flex-end;">
              <small><em><i class="el-icon-info"></i>备注1: 如果任务类任务工时超发, 任务任务的执行人相应任务的工时为负, 数值为工时差额.</em></small>
              <small><em><i class="el-icon-info"></i>备注2: 在工时总数汇总时, 测试工时转换为开发工时进行累计计算.</em></small>
            </div>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<script>
import Swal from "sweetalert2";

export default {
  components: {

  },
  name: "character-manhour-summary-page",

  props: {
    characterId: {
      default: null,
    },

  },

  computed: {

  },

  data() {
    return {
      loginUser: {},
      loginCustomer: {},

      manhourSummaryList: [],
    };
  },

  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    if (Tool.isNotEmpty(_this.characterId)) {
      _this.getManhourSummaryByCharacterId(_this.characterId);
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

    taskJobSumManhourFormat(row){
      if(row.taskJobSumManhour){
        return row.taskJobSumManhour.toFixed(2);
      }
    },

    testJobSumManhourFormat(row){
      if(row.testJobSumManhour){
        return row.testJobSumManhour.toFixed(2);
      }
    },

    devJobSumManhourFormat(row){
      if(row.devJobSumManhour){
        return row.devJobSumManhour.toFixed(2);
      }
    },

    getManhourSummaryByCharacterId(characterId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/manhour/getManhourSummaryByCharacterId/' + characterId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.manhourSummaryList = resp.content;
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
  height: calc(100vh - 255px);
}

</style>
