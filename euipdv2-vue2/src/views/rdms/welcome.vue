<!--
 * Author: 周朔鹏
 * Date: 2025-08-04
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="page-height wb">
    <div class="wb-head">
      <img class="wb-logo" src="./../../../public/static/rdms/image/EUIPD.png" alt="logo">
      <div class="wb-head-text">
        <div class="wb-title">Elastic Universal Integration Product Development</div>
        <div class="wb-sub">
          {{ loginCustomer.customerName || loginCustomer.name }}<span v-if="loginUser.trueName" class="wb-sub-gap">{{ loginUser.trueName }}</span>
          <span class="wb-sub-gap">{{ today }}</span>
        </div>
      </div>
    </div>

    <template v-if="executorId">
      <template v-if="mailEnabled">
        <div class="wb-section">
          未读邮件<span v-if="mailUnread" class="wb-mail-count">{{ mailUnread }}</span>
          <a class="wb-more" @click="$router.push('/customer/mail/inbox')">进入邮箱</a>
        </div>
        <div class="wb-mail" v-loading="mailLoading">
          <div v-for="m in mailItems" :key="m.boxId" class="wb-mail-row"
               @click="$router.push({ path: '/customer/mail/inbox', query: { box: m.boxId } })">
            <span class="wb-mail-who">{{ m.senderName }}</span>
            <span class="wb-mail-subject">{{ m.subject || '（无主题）' }}<em>{{ m.summary }}</em></span>
            <i v-if="m.hasAttachment" class="fa fa-paperclip"></i>
            <span class="wb-mail-time">{{ mailTime(m.mailTime) }}</span>
          </div>
          <div v-if="!mailLoading && !mailItems.length" class="wb-mail-empty">没有未读邮件</div>
        </div>
      </template>

      <div class="wb-section">待办</div>
      <WorkbenchTodo :overdue-num="self.overdueNum || 0"></WorkbenchTodo>

      <el-row :gutter="12">
        <el-col :span="10">
          <div class="wb-section">
            我的工单
            <a class="wb-more" @click="$router.push('/customer/manhour')">进入执行管理</a>
          </div>
          <div class="wb-cards" v-loading="selfLoading">
            <div class="wb-card">
              <div class="wb-card-value">{{ self.inProgressNum || 0 }}</div>
              <div class="wb-card-label">在办</div>
            </div>
            <div class="wb-card">
              <div class="wb-card-value" :class="{'wb-danger': self.overdueNum > 0}">{{ self.overdueNum || 0 }}</div>
              <div class="wb-card-label">当前逾期</div>
            </div>
            <div class="wb-card">
              <div class="wb-card-value" :class="{'wb-warn': self.dueThisWeekNum > 0}">{{ self.dueThisWeekNum || 0 }}</div>
              <div class="wb-card-label">本周到期</div>
            </div>
          </div>
          <el-table :data="self.dueSoon || []" size="mini" border empty-text="没有在办工单" class="wb-due">
            <el-table-column label="工单" min-width="150" show-overflow-tooltip>
              <template slot-scope="s">
                {{ s.row.jobName }}
                <el-tag v-if="s.row.overdue" size="mini" type="danger">逾期</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="projectName" label="项目" min-width="110" show-overflow-tooltip></el-table-column>
            <el-table-column prop="typeName" label="类型" width="86" show-overflow-tooltip></el-table-column>
            <el-table-column label="计划提交" width="92">
              <template slot-scope="s">{{ fmt.date(s.row.planSubmissionTime) }}</template>
            </el-table-column>
          </el-table>
        </el-col>
        <el-col :span="14">
          <div class="wb-section">近 6 个月交付</div>
          <el-row :gutter="8">
            <el-col :span="12">
              <stat-chart type="line" y-label="标准工时" :height="236" :labels="monthLabels"
                          :datasets="[{label: '交付标准工时', data: months.map(m => m.deliverStandHours || 0)}]"
                          empty-text="近 6 个月无交付"></stat-chart>
            </el-col>
            <el-col :span="12">
              <stat-chart type="line" y-label="元" :height="236" :labels="monthLabels"
                          :datasets="[{label: '绩效金额', color: '#67C23A', data: months.map(m => Number(m.performance || 0))}]"
                          empty-text="近 6 个月无绩效"></stat-chart>
            </el-col>
          </el-row>
        </el-col>
      </el-row>

      <div class="wb-section">未来工作负荷</div>
      <MyChart :key="executorId" :chart-id="executorId" :executor-id="executorId"></MyChart>

      <div class="wb-section">
        当月绩效
        <el-button type="text" size="mini" class="wb-section-link" @click="detailVisible = true">查看计入明细</el-button>
      </div>
      <el-table :data="workerPerformanceList" border stripe size="mini">
        <el-table-column prop="customerUserName" min-width="80" align="center" label="姓名"></el-table-column>
        <el-table-column prop="yearMonth" min-width="100" align="center" label="绩效周期"></el-table-column>
        <el-table-column prop="submitHours" :formatter="submitHoursFormat" min-width="120" align="center"
                         label="交付总工时(标准)"></el-table-column>
        <el-table-column prop="delayRate" align="center" label="延期率" min-width="100"></el-table-column>
        <el-table-column prop="rebateRate" align="center" label="延期绩效折扣" min-width="100"></el-table-column>
        <el-table-column prop="performance" :formatter="performanceFormat" align="center" label="绩效工时费"
                         min-width="100"></el-table-column>
        <el-table-column prop="loadRate" align="center" label="负荷率" min-width="100"></el-table-column>
        <el-table-column prop="score" :formatter="scoreFormat" align="center" label="绩效评分"
                         min-width="100"></el-table-column>
      </el-table>
      <div class="wb-note">
        <div><i class="el-icon-info"></i> 工单结算绩效工时 = 工单绩效工时 × (1 − 延期率 × 延期折扣) × (绩效基础 + (1 − 绩效基础) × 任务单评分 ÷ 10)；延期折扣 0.3，绩效基础 0.5。</div>
        <div><i class="el-icon-info"></i> 绩效工时费 = 求和(工单结算绩效工时 × 标准工时费率)；绩效评分 = 求和(工单结算绩效工时) ÷ 总计划工时 × 100；负荷率 = 总工时 ÷ 当月法定工时。</div>
      </div>
      <performance-detail-dlg :visible="detailVisible" :customer-user-id="executorId"
                              @close="detailVisible = false"></performance-detail-dlg>
    </template>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import {rmbFormat} from "@/filter/filter";
import StatChart from "@/components/statistics/stat-chart.vue";
import WorkbenchTodo from "@/components/statistics/workbench-todo.vue";
import * as fmt from "@/shared/stat-format";
import { getWorkbenchSelf } from "@/shared/statistics-api";
import { listBox as listMailBox, shortTime } from "@/shared/mail-api";

export default {
  name: 'welcome',

  components: {
    MyChart: () => import(/* webpackChunkName: "chartjs" */ "@/components/util/myChart.vue"),
    StatChart,
    WorkbenchTodo,
    PerformanceDetailDlg: () => import(/* webpackChunkName: "statistics" */ "@/components/rdms/performance-detail-dlg.vue"),
  },

  data: function () {
    return {
      fmt: fmt,
      loginUser: {},
      loginCustomer: {},
      workerPerformanceList: [],
      self: {},
      selfLoading: false,
      detailVisible: false,
      mailItems: [],
      mailUnread: 0,
      mailLoading: false,
    }
  },

  computed: {
    executorId() {
      return this.loginUser?.customerUserId || '';
    },
    months() {
      return this.self.months || [];
    },
    monthLabels() {
      return this.months.map(m => m.name);
    },
    today() {
      return fmt.date(new Date());
    },
    mailEnabled() {
      return typeof Tool !== 'undefined' && Tool.hasMenuPermission
        && Tool.hasMenuPermission('5301', 'MAIL', 'VIEW')
        && this.loginCustomer.id !== SUPER_ADMIN_ID;
    },
  },

  mounted: function () {
    this.getLoginUser();
    if (this.executorId) {
      this.getCurrentPerformanceByLoginUser(this.executorId);
      this.loadSelf();
      this.loadMail();
    }
    if (this.$bus && this.$bus.subscribe) {
      this.unsubscribeMail = this.$bus.subscribe('mail', () => {
        if (this.executorId) this.loadMail();
      });
    }
  },

  beforeDestroy() {
    if (this.unsubscribeMail) this.unsubscribeMail();
  },

  activated() {
    if (this.executorId) {
      this.loadMail();
    }
  },

  methods: {
    mailTime: shortTime,

    loadMail() {
      if (!this.mailEnabled) {
        return;
      }
      this.mailLoading = true;
      listMailBox({ folder: 'UNREAD', page: 1, size: 5 }).then(p => {
        this.mailItems = (p && p.list) || [];
        this.mailUnread = (p && p.total) || 0;
      }).catch(() => {
        this.mailItems = [];
      }).finally(() => {
        this.mailLoading = false;
      });
    },

    getLoginUser() {
      this.loginUser = Tool.getActorUser() || {};
      this.loginCustomer = Tool.getLoginCustomer() || {};
    },

    loadSelf() {
      this.selfLoading = true;
      getWorkbenchSelf().then(content => {
        this.self = content || {};
      }).catch(err => {
        Toast.warning(err.message, Swal);
      }).finally(() => {
        this.selfLoading = false;
      });
    },

    getCurrentPerformanceByLoginUser(customerUserId) {
      this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/performance/getCurrentPerformanceByLoginUser/' + customerUserId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          this.workerPerformanceList = resp.content ? [resp.content] : [];
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    submitHoursFormat(row){
      return row.submitHours?.toFixed(2);
    },

    scoreFormat(row){
      return row.score?.toFixed(2);
    },

    performanceFormat(row){
      return rmbFormat(row.performance);
    },
  }
}

</script>

<style lang="scss">
@import "./../../../public/static/scss/caipd";
</style>

<style lang="scss" scoped>
.wb {
  overflow-y: auto;
  padding: 4px 16px 24px;
}

.wb-head {
  display: flex;
  align-items: center;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 8px;
}

.wb-logo {
  height: 46px;
  margin-right: 14px;
}

.wb-title {
  font-size: 20px;
  font-weight: bold;
}

.wb-sub {
  font-size: 13px;
  color: #909399;
  margin-top: 2px;
}

.wb-sub-gap {
  margin-left: 12px;
}

.wb-section {
  font-size: 15px;
  font-weight: 600;
  margin: 14px 0 8px;
}

.wb-section-link {
  margin-left: 10px;
  padding: 0;
  font-weight: normal;
}

.wb-more {
  font-size: 12px;
  font-weight: normal;
  margin-left: 10px;
  cursor: pointer;
}

.wb-cards {
  display: flex;
  gap: 10px;
  margin-bottom: 8px;
}

.wb-card {
  flex: 1;
  background: #f5f7fa;
  border-radius: 4px;
  padding: 8px 6px;
  text-align: center;
}

.wb-card-value {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.wb-card-label {
  font-size: 12px;
  color: #909399;
}

.wb-danger {
  color: #F56C6C;
}

.wb-warn {
  color: #E6A23C;
}

.wb-mail-count {
  display: inline-block;
  min-width: 18px;
  margin-left: 6px;
  padding: 0 6px;
  border-radius: 9px;
  background: #0f6cbd;
  color: #fff;
  font-size: 12px;
  line-height: 18px;
  text-align: center;
}

.wb-mail {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  min-height: 40px;
}

.wb-mail-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 7px 12px;
  border-bottom: 1px solid #f2f2f2;
  font-size: 13px;
  cursor: pointer;

  &:last-child {
    border-bottom: none;
  }

  &:hover {
    background: #f5f8fc;
  }

  .fa-paperclip {
    color: #909399;
  }
}

.wb-mail-who {
  flex: 0 0 90px;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.wb-mail-subject {
  flex: 1 1 auto;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 600;
  color: #0f6cbd;

  em {
    margin-left: 10px;
    font-style: normal;
    font-weight: normal;
    color: #909399;
  }
}

.wb-mail-time {
  flex: 0 0 auto;
  font-size: 12px;
  color: #606266;
}

.wb-mail-empty {
  padding: 10px 12px;
  font-size: 13px;
  color: #909399;
}

.wb-note {
  margin-top: 6px;
  color: #909399;
  font-size: 12px;
  line-height: 1.8;
}
</style>
