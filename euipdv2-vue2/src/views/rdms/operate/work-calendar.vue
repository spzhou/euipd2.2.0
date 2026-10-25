<!--
 * Author: 周朔鹏
 * Date: 2026-10-20
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->
<template>
  <div style="padding: 12px 16px 24px;">
    <div style="font-size: 18px;">假日设置</div>
    <div style="color: #666; margin: 4px 0 12px; line-height: 1.6;">
      默认周一至周五上班、周六周日放假。点击日期可在上班和放假之间切换，法定节假日和调休上班可按区间设置。
      设置后，法定工时、任务工时的逐日均摊和未来 5 个工作日日均负荷都按本日历计算。
    </div>

    <div class="wc-toolbar">
      <span>按区间设置</span>
      <el-date-picker v-model="range" type="daterange" size="small" value-format="yyyy-MM-dd"
                      range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期"
                      style="width: 260px;"></el-date-picker>
      <el-radio-group v-model="rangeType" size="small">
        <el-radio-button label="HOLIDAY">放假</el-radio-button>
        <el-radio-button label="WORKDAY">上班</el-radio-button>
        <el-radio-button label="DEFAULT">恢复默认</el-radio-button>
      </el-radio-group>
      <el-input v-model="note" size="small" maxlength="30" clearable placeholder="备注，如国庆节、国庆调休"
                :disabled="rangeType === 'DEFAULT'" style="width: 200px;"></el-input>
      <el-button size="small" type="primary" :disabled="!range || range.length !== 2 || saving"
                 v-on:click="applyRange">应用</el-button>
    </div>

    <div style="display: flex; flex-wrap: wrap; align-items: flex-start; gap: 16px;">
      <div style="flex: 0 0 560px;">
        <div class="wc-month-bar">
          <el-button size="mini" icon="el-icon-arrow-left" v-on:click="shiftMonth(-1)"></el-button>
          <span class="wc-month-title">{{ year }} 年 {{ month }} 月</span>
          <el-button size="mini" icon="el-icon-arrow-right" v-on:click="shiftMonth(1)"></el-button>
          <el-button size="mini" type="text" v-on:click="goToday">回到本月</el-button>
        </div>
        <div class="wc-grid">
          <div v-for="w in weekNames" :key="'h' + w" class="wc-head">{{ w }}</div>
          <div v-for="(cell, i) in cells" :key="i"
               :class="cellClass(cell)"
               :title="cellTitle(cell)"
               v-on:click="toggle(cell)">
            <template v-if="cell">
              <div class="wc-day">{{ cell.day }}</div>
              <div class="wc-tag">{{ cell.workday ? '班' : '休' }}</div>
              <div class="wc-note">{{ cell.note }}</div>
            </template>
          </div>
        </div>
        <div class="wc-legend">
          <span><i class="wc-dot wc-dot-work"></i>工作日</span>
          <span><i class="wc-dot wc-dot-rest"></i>周末（默认放假）</span>
          <span><i class="wc-dot wc-dot-holiday"></i>设为放假</span>
          <span><i class="wc-dot wc-dot-shift"></i>设为上班</span>
        </div>
      </div>

      <div style="flex: 1; min-width: 320px;">
        <div style="margin-bottom: 6px; color: #666;">{{ year }} 年已设置 {{ list.length }} 天</div>
        <el-table :data="list" border size="small" max-height="520" empty-text="本年未设置，全部按默认">
          <el-table-column prop="date" label="日期" width="110"></el-table-column>
          <el-table-column label="星期" width="60">
            <template slot-scope="s">{{ weekNameOf(s.row.date) }}</template>
          </el-table-column>
          <el-table-column label="设置" width="70">
            <template slot-scope="s">
              <span :style="{ color: s.row.dayType === 'WORKDAY' ? '#1f6fd1' : '#c0392b' }">
                {{ s.row.dayType === 'WORKDAY' ? '上班' : '放假' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="note" label="备注" min-width="100"></el-table-column>
          <el-table-column label="操作" width="90" align="center">
            <template slot-scope="s">
              <el-button type="text" size="mini" v-on:click="restoreDay(s.row.date)">恢复默认</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script>
const WEEK_NAMES = ['一', '二', '三', '四', '五', '六', '日'];

function pad(n) {
  return n < 10 ? '0' + n : '' + n;
}

function dateKey(y, m, d) {
  return y + '-' + pad(m) + '-' + pad(d);
}

function isWeekend(y, m, d) {
  let dow = new Date(y, m - 1, d).getDay();
  return dow === 0 || dow === 6;
}

export default {
  name: 'work-calendar',
  data: function () {
    let now = new Date();
    return {
      year: now.getFullYear(),
      month: now.getMonth() + 1,
      loadedYear: null,
      settings: {},
      list: [],
      range: null,
      rangeType: 'HOLIDAY',
      note: '',
      saving: false,
      weekNames: WEEK_NAMES
    };
  },
  computed: {
    cells: function () {
      let first = new Date(this.year, this.month - 1, 1);
      let lead = (first.getDay() + 6) % 7;
      let days = new Date(this.year, this.month, 0).getDate();
      let result = [];
      for (let i = 0; i < lead; i++) {
        result.push(null);
      }
      for (let d = 1; d <= days; d++) {
        let key = dateKey(this.year, this.month, d);
        let set = this.settings[key];
        let weekend = isWeekend(this.year, this.month, d);
        result.push({
          key: key,
          day: d,
          weekend: weekend,
          dayType: set ? set.dayType : null,
          note: set ? (set.note || '') : '',
          workday: set ? set.dayType === 'WORKDAY' : !weekend
        });
      }
      while (result.length % 7 !== 0) {
        result.push(null);
      }
      return result;
    }
  },
  mounted: function () {
    this.load();
  },
  methods: {
    load: function () {
      let _this = this;
      let year = _this.year;
      _this.$ajax.get(process.env.VUE_APP_SERVER + '/customer/work-calendar/list/' + year).then(function (response) {
        let resp = response.data || {};
        if (resp.success === false) {
          _this.$message.error(resp.message || '无法读取假日设置');
          return;
        }
        if (year !== _this.year) {
          return;
        }
        let days = (resp.content && resp.content.days) || [];
        let map = {};
        days.forEach(function (d) {
          map[d.date] = d;
        });
        _this.settings = map;
        _this.list = days;
        _this.loadedYear = year;
      });
    },
    shiftMonth: function (step) {
      let m = this.month + step;
      let y = this.year;
      if (m < 1) {
        m = 12;
        y--;
      } else if (m > 12) {
        m = 1;
        y++;
      }
      this.month = m;
      if (y !== this.year) {
        this.year = y;
        this.load();
      }
    },
    goToday: function () {
      let now = new Date();
      this.month = now.getMonth() + 1;
      if (now.getFullYear() !== this.year) {
        this.year = now.getFullYear();
        this.load();
      }
    },
    weekNameOf: function (date) {
      let parts = date.split('-');
      let dow = new Date(+parts[0], +parts[1] - 1, +parts[2]).getDay();
      return '周' + WEEK_NAMES[(dow + 6) % 7];
    },
    cellClass: function (cell) {
      if (!cell) {
        return 'wc-cell wc-empty';
      }
      let cls = 'wc-cell';
      if (cell.dayType === 'HOLIDAY') {
        cls += ' wc-holiday';
      } else if (cell.dayType === 'WORKDAY') {
        cls += cell.weekend ? ' wc-shift' : ' wc-work';
      } else {
        cls += cell.weekend ? ' wc-rest' : ' wc-work';
      }
      return cls;
    },
    cellTitle: function (cell) {
      if (!cell) {
        return '';
      }
      let state = cell.workday ? '上班' : '放假';
      let source = cell.dayType ? '已设置' : '默认';
      return cell.key + ' ' + state + '（' + source + '），点击改为' + (cell.workday ? '放假' : '上班');
    },
    toggle: function (cell) {
      if (!cell || this.saving) {
        return;
      }
      let target = cell.workday ? 'HOLIDAY' : 'WORKDAY';
      let backToDefault = (target === 'WORKDAY') === !cell.weekend;
      this.submit(cell.key, cell.key, backToDefault ? 'DEFAULT' : target, this.note);
    },
    applyRange: function () {
      if (!this.range || this.range.length !== 2) {
        return;
      }
      this.submit(this.range[0], this.range[1], this.rangeType, this.note);
    },
    restoreDay: function (date) {
      this.submit(date, date, 'DEFAULT', '');
    },
    submit: function (start, end, dayType, note) {
      let _this = this;
      _this.saving = true;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/work-calendar/update', {
        startDate: start,
        endDate: end,
        dayType: dayType,
        note: dayType === 'DEFAULT' ? '' : note
      }).then(function (response) {
        let resp = response.data || {};
        if (resp.success === false) {
          _this.$message.error(resp.message || '保存失败');
          return;
        }
        _this.$message.success(resp.message || '已保存');
        _this.load();
      }).finally(function () {
        _this.saving = false;
      });
    }
  }
};
</script>

<style scoped>
.wc-toolbar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 14px;
}
.wc-month-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}
.wc-month-title {
  font-size: 16px;
  min-width: 110px;
  text-align: center;
}
.wc-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  border-top: 1px solid #e4e7ed;
  border-left: 1px solid #e4e7ed;
}
.wc-head {
  padding: 6px 0;
  text-align: center;
  background: #f5f7fa;
  color: #606266;
  border-right: 1px solid #e4e7ed;
  border-bottom: 1px solid #e4e7ed;
}
.wc-cell {
  position: relative;
  height: 64px;
  padding: 4px 6px;
  border-right: 1px solid #e4e7ed;
  border-bottom: 1px solid #e4e7ed;
  cursor: pointer;
  overflow: hidden;
}
.wc-cell:hover {
  outline: 2px solid #409eff;
  outline-offset: -2px;
}
.wc-empty {
  cursor: default;
  background: #fafafa;
}
.wc-empty:hover {
  outline: none;
}
.wc-day {
  font-size: 15px;
}
.wc-tag {
  position: absolute;
  top: 4px;
  right: 6px;
  font-size: 12px;
}
.wc-note {
  font-size: 12px;
  color: #666;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.wc-work {
  background: #fff;
}
.wc-work .wc-tag {
  color: #909399;
}
.wc-rest {
  background: #f2f3f5;
}
.wc-rest .wc-tag {
  color: #909399;
}
.wc-holiday {
  background: #fdecea;
}
.wc-holiday .wc-tag {
  color: #c0392b;
}
.wc-shift {
  background: #e8f1fd;
}
.wc-shift .wc-tag {
  color: #1f6fd1;
}
.wc-legend {
  display: flex;
  gap: 14px;
  margin-top: 8px;
  color: #666;
  font-size: 12px;
}
.wc-dot {
  display: inline-block;
  width: 10px;
  height: 10px;
  margin-right: 4px;
  border: 1px solid #dcdfe6;
  vertical-align: -1px;
}
.wc-dot-work {
  background: #fff;
}
.wc-dot-rest {
  background: #f2f3f5;
}
.wc-dot-holiday {
  background: #fdecea;
}
.wc-dot-shift {
  background: #e8f1fd;
}
</style>
