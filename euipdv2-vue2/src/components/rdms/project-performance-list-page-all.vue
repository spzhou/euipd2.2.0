<!--
 * Author: 周朔鹏
 * Date: 2025-02-27
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div v-if="generating" class="summary-generating">
      <i class="el-icon-loading"></i> 数据生成中，请稍候……
    </div>
    <div v-else class="performance-page-height" style="overflow-y: scroll">
      <div style="height: 100%;">
        <el-row>
          <el-col :span=18>
            <el-row v-for="(item, i) in dataArray" :key="i" style="border-top: 1px solid #eee; border-bottom: 1px solid #eee;">
              <el-row style="border-bottom: 1px dot-dash #eee">
                <el-col :span=5>
                  <div style="display: flex; flex-direction: row; justify-content: flex-start; align-items: center;">
                    <div style="margin-top: 7px; width: 60px;">
                      项目编号:
                    </div>
                    <div style="padding-left: 8px; font-weight: 500; font-size: 15px;">
                      {{ item.projectCode }}
                    </div>
                  </div>
                  <div style="display: flex; flex-direction: row; justify-content: flex-start; align-items: center;">
                    <div style="margin-top: 7px; width: 60px;">
                      项目名称:
                    </div>
                    <div class="no-wrap-ellipsis" style="padding-left: 8px; font-weight: 500; font-size: 15px; margin-top: 7px; width: 230px;">
                      {{ item.projectName }}
                    </div>
                  </div>

                  <div style="display: flex; flex-direction: row; justify-content: flex-start; align-items: center; margin-top: 7px;">
                    <div style="width: 60px;">
                      产品经理:
                    </div>
                    <div style="padding-left: 8px; font-weight: 400; width: 60px;">
                      {{ item.productManagerName }}
                    </div>
                  </div>
                  <div style="display: flex; flex-direction: row; justify-content: flex-start; align-items: center;">
                    <div style=" width: 60px;">
                      项目经理:
                    </div>
                    <div style="padding-left: 8px; font-weight: 400;">
                      {{ item.projectManagerName }}
                    </div>
                  </div>
                  <div style="display: flex; flex-direction: row; justify-content: flex-start; align-items: center;">
                    <div style="width: 60px;">
                      测试主管:
                    </div>
                    <div style="padding-left: 8px; font-weight: 400;">
                      {{ item.testManagerName }}
                    </div>
                  </div>
                </el-col>
                <el-col :span=19>
                  <el-row>
                    <el-col :span=4 style="text-align: center;">
                      <el-progress type="dashboard" :percentage="item.budgetImpRate_show" :color="progressColor(item.budgetImpRate_show)"></el-progress>
                      <div style="margin-top: -20px;">预算执行进度</div>
                    </el-col>
                    <el-col :span=4 style="text-align: center;">
                      <el-progress type="dashboard" :percentage="item.manhourImpRate_show" :color="progressColor(item.manhourImpRate_show)"></el-progress>
                      <div style="margin-top: -20px;">工时投入进度</div>
                    </el-col>
                    <el-col :span=4 style="text-align: center;">
                      <el-row>待评审里程碑</el-row>
                      <el-row>
                        <div class="no-wrap-ellipsis" style="font-size: 15px; font-weight: 400; margin-top: 5px;">
                          {{ item.nextReviewMilestone != null? item.nextReviewMilestone.milestoneName : ""}}
                        </div>
                      </el-row>
                      <el-row>
                        <div style="font-size: 16px; font-weight: 500; margin-top: 2px;">
                          {{ item.nextReviewMilestone != null? item.nextReviewMilestone.reviewTimeStr : ""}}
                        </div>
                      </el-row>
                      <el-row style="margin-top: 2px;">
                        倒计时
                      </el-row>
                      <el-row :style="{ color: item.toReviewDay < 0 ? 'red' : 'black' }" style="font-size: 30px">
                        {{ item.toReviewDay }}
                      </el-row>
                    </el-col>
                    <el-col :span=4 style="text-align: center;">
                      <el-row>
                        <div style="margin-top: 0;">
                          发布时间
                        </div>
                      </el-row>
                      <el-row>
                        <div style="font-size: 16px; font-weight: 500; margin-top: 10px;">
                          {{ item.releaseTime | shortDateFormat }}
                        </div>
                      </el-row>
                      <el-row>
                        <div style="margin-top:20px;">
                          倒计时
                        </div>
                      </el-row>
                      <el-row :style="{ color: item.toReleaseDay < 0 ? 'red' : 'black' }">
                        <div style="font-size: 30px">
                          {{ item.toReleaseDay }}
                        </div>

                      </el-row>
                    </el-col>
                    <el-col :span=4 style="text-align: center;">
                      <el-row>
                        <el-col :span=18 style="text-align: right;">平均任务工时:</el-col>
                        <el-col :span=6>{{ item.averageJobitemTime }}</el-col>
                      </el-row>
                      <el-row style="margin-top: 3px;">
                        <el-col :span=18 style="text-align: right;">项目延期率:</el-col>
                        <el-col :span=6>{{ item.overdueRate | fixed2fractional}}</el-col>
                      </el-row>
                      <el-row style="margin-top: 3px;">
                        <el-col :span=18 style="text-align: right;">逾期工单数量:</el-col>
                        <el-col :span=6>{{ item.overtimeJobitemNum }}</el-col>
                      </el-row>
                      <el-row style="margin-top: 3px;">
                        <el-col :span=18 style="text-align: right;">完工工单数量:</el-col>
                        <el-col :span=6>{{ item.completeJobitemNum }}</el-col>
                      </el-row>
                      <el-row style="margin-top: 3px;">
                        <el-col :span=18 style="text-align: right;">总任务数量:</el-col>
                        <el-col :span=6>{{ item.sumJobitemNum }}</el-col>
                      </el-row>
                      <el-row style="margin-top: 3px;">
                        <el-col :span=18 style="text-align: right;">提交文档数量:</el-col>
                        <el-col :span=6>{{ item.submitFileNum }}</el-col>
                      </el-row>
                    </el-col>
                    <el-col :span=4 style="text-align: center;">
                      <div v-if="item.performanceList != null && item.performanceList[0] != null">
                        <el-row style="margin-top: 2px;">绩效冠军</el-row>
                        <el-row style="font-size: 14px;">
                          <el-col :span=12 style="text-align: right;">{{ item.performanceList != null && item.performanceList[0] != null? item.performanceList[0].customerUserName : "" }}</el-col>
                          <el-col :span=10 :offset=1 style="text-align: left;">{{ item.performanceList != null && item.performanceList[0] != null? item.performanceList[0].submitHours : "" | fixed2fractional}}</el-col>
                        </el-row>
                      </div>
                      <div v-if="item.performanceList != null && item.performanceList[1] != null">
                        <el-row style="margin-top: 6px;">绩效亚军</el-row>
                        <el-row style="font-size: 14px;">
                          <el-col :span=12 style="text-align: right;">{{ item.performanceList != null && item.performanceList[1] != null? item.performanceList[1].customerUserName : "" }}</el-col>
                          <el-col :span=10 :offset=1 style="text-align: left;">{{  item.performanceList != null && item.performanceList[1] != null? item.performanceList[1].submitHours : "" | fixed2fractional}}</el-col>
                        </el-row>
                      </div>
                      <div v-if="item.performanceList != null && item.performanceList[2] != null">
                        <el-row style="margin-top: 6px;">绩效季军</el-row>
                        <el-row style="font-size: 14px;">
                          <el-col :span=12 style="text-align: right;">{{ item.performanceList != null && item.performanceList[2] != null? item.performanceList[2].customerUserName : "" }}</el-col>
                          <el-col :span=10 :offset=1 style="text-align: left;">{{  item.performanceList != null && item.performanceList[2] != null? item.performanceList[2].submitHours : "" | fixed2fractional}}</el-col>
                        </el-row>
                      </div>
                    </el-col>
                  </el-row>
                </el-col>
              </el-row>
            </el-row>
          </el-col>
          <el-col :span="6">
            <div style="border-left: solid 1px #eee; margin-left: 10px; height: 100%;">
              <div style="display: flex; flex-direction: column; justify-content: center; align-items: center; margin-top: 35px;">
                <div style="font-size: 25px; margin: 30px 0 5px">年度综合绩效榜</div>
                <div style="margin-bottom: 3px;">{{ currentDate | shortDateFormat }}</div>
              </div>
              <el-table
                  :data="performanceList"
                  style="width: 100%;"
                  row-key="id"
                  stripe>
                <el-table-column label="工作量排名" align="center" width="80">
                  <template slot-scope="scope">
                    {{ scope.$index + 1 }}
                  </template>
                </el-table-column>
                <el-table-column
                    prop="customerUserName"
                    align="center"
                    label="姓名">
                </el-table-column>
                <el-table-column
                    prop="submitHours"
                    :formatter="submitHourFormat"
                    align="center"
                    label="交付工时">
                </el-table-column>
                <el-table-column
                    prop="score"
                    :formatter="scoreFormat"
                    align="center"
                    label="交付评分">
                </el-table-column>
              </el-table>
            </div>

          </el-col>
        </el-row>
      </div>
    </div>

  </div>
</template>

<script>

import Swal from "sweetalert2";
import {Loading} from "element-ui";
import {fixed2fractional, rmbFormat, shortDateFormat} from "@/filter/filter";

export default {
  components: {

  },
  name: "project-performance-list-page-all",

  props: {
    customerId: {
      default: null,
    }
  },

  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},
      customerPerformanceData: {},

      dataArray: [/* 你的数据数组 */],
      currentIndex: 0,
      intervalId: null,
      intervalId_day: null,
      dataToRead: [],
      performanceList: [],
      generating: false,
      retryTimer: null,
      currentDate: new Date(), // 添加当前日期属性
    }
  },
  mounted: function () {
    let _this = this;
    _this.getLoginUser();
    _this.getCustomerPerformanceInfo();
    _this.startReading();
  },

  beforeDestroy: function () {
    let _this = this;
    _this.stopReading();
  },

  methods: {
    rmbFormat,
    shortDateFormat,
    fixed2fractional,
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

    startReading() {
      let _this = this;
      _this.intervalId_day = setInterval(() => {
        _this.getCustomerPerformanceInfo();
      }, 1000 * 60 * 60 *12);

    },

    stopReading() {
      let _this = this;
      clearInterval(_this.intervalId_day);
      clearTimeout(_this.retryTimer);
    },

    getCustomerPerformanceInfo() {
      let _this = this;
      let loadingInstance = Loading.service({ fullscreen: true });
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/summary/getCustomerSummaryInfo').then((response)=>{
        loadingInstance.close();
        let resp = response.data;
        if (resp.success) {
          _this.customerPerformanceData = resp.content;
          _this.generating = !resp.content;
          if (_this.generating) {
            _this.scheduleRetry();
          }

          if(_this.customerPerformanceData){
            _this.dataArray = JSON.parse(JSON.stringify(_this.customerPerformanceData.projectSummaryList || []));
          }else{
            _this.dataArray = null;
          }

          if(_this.dataArray !== null){
            _this.dataArray.forEach((item, index) => {
              // 获取当前日期
              const today = new Date();
              // 计算两个日期之间的毫秒差
              // 转换 nextReviewMilestone.reviewTime 为 Date 类型
              const reviewTime = item.nextReviewMilestone ? _this.parseDate(item.nextReviewMilestone.reviewTime) : null;
              if (reviewTime) {
                // 计算两个日期之间的毫秒差
                const differenceInTime = reviewTime - today;
                // 将毫秒差转换为天数差，并确保结果为整数
                item.toReviewDay = Math.floor(differenceInTime / (1000 * 3600 * 24));
              } else {
                item.toReviewDay = 0; // 或其他默认值
              }

              item.budgetImpRate_show = parseInt(parseFloat(item.budgetImpRate).toFixed(2) * 100) ;
              item.manhourImpRate_show = parseInt(parseFloat(item.manhourImpRate).toFixed(2) * 100) ;
            })
          }

          if(_this.customerPerformanceData){
            _this.performanceList = _this.customerPerformanceData.customerPerformanceList || [];
            _this.performanceList.sort((a, b) => b.submitHours - a.submitHours);
          }else{
            _this.performanceList = [];
          }
        } else {
          Toast.warning(resp.message,Swal);
        }
      }).catch(() => {
        loadingInstance.close();
      })
    },
    scheduleRetry() {
      clearTimeout(this.retryTimer);
      this.retryTimer = setTimeout(() => this.getCustomerPerformanceInfo(), 15000);
    },
    parseDate(dateString) {
      try {
        const date = new Date(dateString);
        if (isNaN(date.getTime())) {
          console.error(`Invalid date string: ${dateString}`);
          return null;
        }
        return date;
      } catch (error) {
        console.error(`Error parsing date string: ${dateString}`, error);
        return null;
      }
    },
    submitHourFormat(row, column) {
      return fixed2fractional(row.submitHours);
    },
    scoreFormat(row, column) {
      return fixed2fractional(row.score);
    },
    progressColor(percentage) {
      if (percentage < 10) {
        return '#f4bd0b'; // 红色
      } else if (percentage < 70) {
        return '#46fa14'; // 橙色
      } else if (percentage <= 90) {
        return '#145dfa'; // 橙色
      } else if (percentage > 90){
        return '#f33625'; // 绿色
      }
    },

  }
}
</script>
<style>
.summary-generating {
  padding: 120px 0;
  text-align: center;
  font-size: 22px;
  color: #909399;
}
.no-wrap-ellipsis {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.performance-page-height{
  display: block;
  width: 100%;
  height: calc(100vh - 170px);
}
</style>
