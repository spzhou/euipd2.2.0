<!--
 * Author: 周朔鹏
 * Date: 2026-10-12
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="org-workload">
    <div class="org-toolbar">
      <stat-period-picker v-model="period" @change="reload"></stat-period-picker>
      <el-select v-model="projectId" size="small" filterable clearable placeholder="全部项目" style="width: 220px"
                 @change="reload">
        <el-option v-for="p in scope.projects" :key="p.id" :label="p.name" :value="p.id"></el-option>
      </el-select>
      <el-checkbox v-model="includeDescendants" @change="loadMembers">成员含下级部门</el-checkbox>
      <el-button size="small" icon="el-icon-refresh" @click="reload">刷新</el-button>
      <el-button size="small" icon="el-icon-download" :disabled="!members.length" @click="exportExcel">导出 Excel</el-button>
      <span class="org-scope-tip" v-if="scope.level">数据范围：{{ levelName }}，{{ scope.userCount }} 人</span>
    </div>

    <el-row :gutter="12">
      <el-col :span="7">
        <div class="org-tree-box" v-loading="treeLoading">
          <el-tree ref="tree" :data="treeData" node-key="id" :props="{label: 'name', children: 'children'}"
                   default-expand-all highlight-current :expand-on-click-node="false" @node-click="selectNode">
            <span class="org-tree-node" slot-scope="{ data }">
              <span class="org-tree-name">{{ data.name }}</span>
              <span class="org-tree-meta">
                {{ data.total.memberCount }} 人
                <el-tag size="mini" :type="fmt.loadLevel(data.total.loadRate)">{{ fmt.percent(data.total.loadRate, 0) }}</el-tag>
              </span>
            </span>
          </el-tree>
        </div>
      </el-col>
      <el-col :span="17">
        <div class="org-title">
          {{ selected ? selected.name : '' }}
          <span v-if="selected && selected.managerName" class="org-manager">负责人：{{ selected.managerName }}</span>
          <span v-if="projectId" class="org-manager">已按项目筛选，绩效金额不按项目分摊，不显示</span>
        </div>
        <el-row :gutter="8" class="org-cards">
          <el-col :span="4" v-for="card in cards" :key="card.label">
            <div class="org-card">
              <div class="org-card-value">{{ card.value }}</div>
              <div class="org-card-label">{{ card.label }}</div>
            </div>
          </el-col>
        </el-row>
        <stat-chart type="bar" stacked rate-axis y-label="个人工时" :height="260" :labels="chartLabels"
                    :datasets="chartDatasets" empty-text="当期无成员工作量" @point-click="openPersonByIndex"></stat-chart>
        <el-table :data="members" size="mini" border stripe v-loading="memberLoading" max-height="420"
                  :default-sort="{prop: 'deliverWorkerHours', order: 'descending'}" class="org-table">
          <el-table-column label="姓名" width="90" fixed>
            <template slot-scope="s">
              <a class="org-link" @click="openPerson(s.row)">{{ s.row.trueName }}</a>
            </template>
          </el-table-column>
          <el-table-column prop="departmentName" label="部门" min-width="110" show-overflow-tooltip></el-table-column>
          <el-table-column prop="inProgressNum" label="进行中" width="66" align="right" sortable></el-table-column>
          <el-table-column prop="overdueNum" label="逾期" width="60" align="right" sortable></el-table-column>
          <el-table-column prop="completedNum" label="完成" width="60" align="right" sortable></el-table-column>
          <el-table-column prop="overdueCompletedNum" label="延期完成" width="80" align="right" sortable></el-table-column>
          <el-table-column prop="deliverStandHours" label="交付标准工时" width="104" align="right" sortable>
            <template slot-scope="s">{{ fmt.hours(s.row.deliverStandHours) }}</template>
          </el-table-column>
          <el-table-column prop="deliverWorkerHours" label="交付个人工时" width="104" align="right" sortable>
            <template slot-scope="s">{{ fmt.hours(s.row.deliverWorkerHours) }}</template>
          </el-table-column>
          <el-table-column prop="loadRate" label="负荷率" width="76" align="right" sortable>
            <template slot-scope="s">
              <span :class="'org-level-' + fmt.loadLevel(s.row.loadRate)">{{ fmt.percent(s.row.loadRate) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="delayRate" label="延期率" width="70" align="right" sortable>
            <template slot-scope="s">{{ fmt.percent(s.row.delayRate) }}</template>
          </el-table-column>
          <el-table-column prop="reworkRate" label="返工率" width="70" align="right" sortable>
            <template slot-scope="s">
              <span :title="s.row.reworkNum + ' / ' + s.row.reworkBaseNum">{{ fmt.percent(s.row.reworkRate) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="avgScore" label="平均评分" width="80" align="right" sortable>
            <template slot-scope="s">{{ fmt.score(s.row.avgScore) }}</template>
          </el-table-column>
          <el-table-column prop="weekDayLoad" label="未来5个工作日日均负荷" width="150" align="right">
            <template slot-scope="s">{{ fmt.hours(s.row.weekDayLoad) }}</template>
          </el-table-column>
          <el-table-column prop="staffFee" label="人员费" width="96" align="right" sortable>
            <template slot-scope="s">{{ fmt.money(s.row.staffFee) }}</template>
          </el-table-column>
          <el-table-column v-if="managerial" prop="performance" label="绩效" width="96" align="right" sortable>
            <template slot-scope="s">{{ fmt.money(s.row.performance) }}</template>
          </el-table-column>
        </el-table>
        <div class="org-note">
          负荷率 = 交付个人工时 ÷ 期间法定工时（{{ fmt.hours(lawHours) }} 小时），部门的负荷率即部门吸收率；个人工时按标准费率与成员职级费率换算。
          返工率 = 期间完成的非缺陷工单中被缺陷工单关联（经测试任务指向被测工单）的占比。历史月份的部门归属取当月快照。
        </div>
      </el-col>
    </el-row>

    <person-workload-dlg :visible="personVisible" :customer-user-id="person.customerUserId" :true-name="person.trueName"
                         :period="query()" :show-performance="managerial" @close="personVisible = false"
                         @go-performance="goPerformance"></person-workload-dlg>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import StatChart from "@/components/statistics/stat-chart.vue";
import StatPeriodPicker from "@/components/statistics/stat-period-picker.vue";
import PersonWorkloadDlg from "@/components/statistics/person-workload-dlg.vue";
import * as fmt from "@/shared/stat-format";
import { getScope, getDepartmentTree, listDepartmentMembers } from "@/shared/statistics-api";

const LEVEL_NAMES = { ORG: '全机构', DEPARTMENT: '所辖部门', PROJECT: '所辖项目成员', SELF: '本人' };

function findNode(node, id) {
  if (!node) {
    return null;
  }
  if (node.id === id) {
    return node;
  }
  for (const child of node.children || []) {
    const hit = findNode(child, id);
    if (hit) {
      return hit;
    }
  }
  return null;
}

export default {
  name: "org-workload",
  components: { StatChart, StatPeriodPicker, PersonWorkloadDlg },
  props: {
    // 由驾驶舱跳转时预选的部门
    focusDepartmentId: { type: String, default: '' },
  },

  data() {
    return {
      fmt: fmt,
      scope: { projects: [] },
      period: null,
      projectId: '',
      includeDescendants: true,
      tree: null,
      selectedId: '',
      members: [],
      treeLoading: false,
      memberLoading: false,
      personVisible: false,
      person: { customerUserId: '', trueName: '' },
      treeSeq: 0,
      memberSeq: 0,
    };
  },

  computed: {
    levelName() {
      return LEVEL_NAMES[this.scope.level] || this.scope.level;
    },
    // 绩效金额仅对机构级、部门级可见（ST-D12），服务端已对其他级别置空
    managerial() {
      return fmt.isManagerial(this.scope.level);
    },
    treeData() {
      return this.tree ? [this.tree] : [];
    },
    selected() {
      return findNode(this.tree, this.selectedId) || this.tree;
    },
    lawHours() {
      const total = this.selected && this.selected.total;
      return total && total.memberCount ? total.lawHours / total.memberCount : null;
    },
    cards() {
      const s = (this.selected && this.selected.total) || {};
      const cards = [
        { label: '成员数', value: fmt.count(s.memberCount) },
        { label: '进行中 / 逾期', value: fmt.count(s.inProgressNum) + ' / ' + fmt.count(s.overdueNum) },
        { label: '完成 / 延期完成', value: fmt.count(s.completedNum) + ' / ' + fmt.count(s.overdueCompletedNum) },
        { label: '交付标准工时', value: fmt.hours(s.deliverStandHours) },
        { label: '交付个人工时', value: fmt.hours(s.deliverWorkerHours) },
        { label: '人均交付工时', value: fmt.hours(s.perCapitaDeliverHours) },
        { label: '工时负荷率（吸收率）', value: fmt.percent(s.loadRate) },
        { label: '延期率', value: fmt.percent(s.delayRate) },
        { label: '返工率', value: fmt.percent(s.reworkRate) + '（' + fmt.count(s.reworkNum) + ' / ' + fmt.count(s.reworkBaseNum) + '）' },
        { label: '平均评分', value: fmt.score(s.avgScore) },
        { label: '在办个人工时', value: fmt.hours(s.inProgressWorkerHours) },
        { label: '人员费 / 已付', value: fmt.money(s.staffFee, 0) + ' / ' + fmt.money(s.staffFeePaid, 0) },
      ];
      if (this.managerial) {
        cards.push({ label: '绩效', value: this.projectId ? '-' : fmt.money(s.performance, 0) });
      }
      return cards;
    },
    chartLabels() {
      return this.members.map(m => m.trueName);
    },
    chartDatasets() {
      return [
        { label: '交付个人工时', data: this.members.map(m => m.deliverWorkerHours || 0) },
        { label: '在办个人工时', data: this.members.map(m => m.inProgressWorkerHours || 0) },
        { label: '负荷率', type: 'line', yAxisID: 'rate', color: '#F56C6C', data: this.members.map(m => Number(m.loadRate || 0)) },
      ];
    }
  },

  mounted() {
    getScope().then(scope => {
      this.scope = Object.assign({ projects: [] }, scope || {});
    }).catch(err => Toast.warning(err.message, Swal));
  },

  methods: {
    query() {
      return Object.assign({}, this.period || {}, {
        projectId: this.projectId || null,
        includeDescendants: this.includeDescendants,
      });
    },

    reload() {
      if (!this.period) {
        return;
      }
      const seq = ++this.treeSeq;
      this.treeLoading = true;
      getDepartmentTree(this.query()).then(tree => {
        if (seq !== this.treeSeq) {
          return;
        }
        this.tree = tree;
        const keep = this.selectedId || this.focusDepartmentId;
        this.selectedId = keep && findNode(tree, keep) ? keep : (tree ? tree.id : '');
        this.$nextTick(() => {
          if (this.$refs.tree && this.selectedId) {
            this.$refs.tree.setCurrentKey(this.selectedId);
          }
        });
        this.loadMembers();
      }).catch(err => {
        Toast.warning(err.message, Swal);
      }).finally(() => {
        if (seq === this.treeSeq) {
          this.treeLoading = false;
        }
      });
    },

    selectNode(data) {
      this.selectedId = data.id;
      this.loadMembers();
    },

    loadMembers() {
      if (!this.selectedId) {
        this.members = [];
        return;
      }
      const seq = ++this.memberSeq;
      this.memberLoading = true;
      listDepartmentMembers(this.selectedId, this.query()).then(rows => {
        if (seq === this.memberSeq) {
          this.members = rows || [];
        }
      }).catch(err => {
        Toast.warning(err.message, Swal);
      }).finally(() => {
        if (seq === this.memberSeq) {
          this.memberLoading = false;
        }
      });
    },

    openPerson(row) {
      this.person = { customerUserId: row.customerUserId, trueName: row.trueName };
      this.personVisible = true;
    },

    openPersonByIndex(index) {
      const row = this.members[index];
      if (row) {
        this.openPerson(row);
      }
    },

    goPerformance(customerUserId) {
      this.personVisible = false;
      this.$emit('go-performance', customerUserId);
    },

    async exportExcel() {
      const XLSX = await import(/* webpackChunkName: "xlsx" */ 'xlsx');
      const rows = this.members.map(m => {
        const row = {
          '姓名': m.trueName,
          '工号': m.jobNum || '',
          '部门': m.departmentName,
          '进行中': m.inProgressNum,
          '逾期': m.overdueNum,
          '完成': m.completedNum,
          '延期完成': m.overdueCompletedNum,
          '交付标准工时': m.deliverStandHours,
          '交付个人工时': m.deliverWorkerHours,
          '负荷率': fmt.percent(m.loadRate),
          '延期率': fmt.percent(m.delayRate),
          '返工率': fmt.percent(m.reworkRate),
          '返工工单': m.reworkNum,
          '完成非缺陷工单': m.reworkBaseNum,
          '平均评分': m.avgScore === null || m.avgScore === undefined ? '' : Number(m.avgScore),
          '未来5个工作日日均负荷': m.weekDayLoad,
          '人员费': Number(m.staffFee || 0),
          '人员费已付': Number(m.staffFeePaid || 0),
        };
        if (this.managerial) {
          row['绩效'] = m.performance === null || m.performance === undefined ? '' : Number(m.performance);
        }
        return row;
      });
      const book = XLSX.utils.book_new();
      XLSX.utils.book_append_sheet(book, XLSX.utils.json_to_sheet(rows), '成员工作量');
      const name = (this.selected ? this.selected.name : '组织') + '-工作量-' + (this.period ? this.period.periodValue : '');
      XLSX.writeFile(book, name.replace(/[\\/:*?"<>|~]/g, '_') + '.xlsx');
    }
  }
}
</script>

<style scoped>
.org-toolbar {
  margin-bottom: 10px;
}

.org-toolbar > * {
  margin-right: 8px;
  vertical-align: middle;
}

.org-scope-tip {
  color: #909399;
  font-size: 12px;
}

.org-tree-box {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 6px 0;
  min-height: 300px;
  max-height: 760px;
  overflow: auto;
}

.org-tree-node {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-right: 8px;
  font-size: 13px;
}

.org-tree-name {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-right: 6px;
}

.org-tree-meta {
  flex-shrink: 0;
  color: #909399;
  font-size: 12px;
}

.org-title {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 8px;
}

.org-manager {
  font-size: 12px;
  font-weight: normal;
  color: #909399;
  margin-left: 12px;
}

.org-cards {
  margin-bottom: 6px;
}

.org-card {
  background: #f5f7fa;
  border-radius: 4px;
  padding: 8px 6px;
  margin-bottom: 8px;
  text-align: center;
}

.org-card-value {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.org-card-label {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.org-table {
  margin-top: 8px;
}

.org-link {
  cursor: pointer;
}

.org-level-danger {
  color: #F56C6C;
}

.org-level-warning {
  color: #E6A23C;
}

.org-note {
  margin-top: 6px;
  color: #909399;
  font-size: 12px;
}
</style>
