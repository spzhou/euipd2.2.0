<!--
 * 特性定义内容版本面板（实现方案第 9.3 节）：按开发版本线列出主线版本、修改分支及其版本，
 * 勾选两个版本后可以逐章节对比；每个版本可以以 PDF 或 Word 只读打开；“以关系图查看”打开演进关系图。
 * 可以新建、放弃修改分支，以及在基线变化后更新基线。最新版本编号大于项目锁定版本时可以发起纳入项目，
 * 纳入分支经变更评审通过后在此合入；修订分支的确认仍在特性审批页。
-->
<template>
  <div class="rich-versions" v-loading="loading">
    <div v-if="!loading && lines.length === 0" class="rich-versions__empty">尚无内容版本（正文仍为迁移前数据）</div>
    <div v-for="line in lines" :key="line.lineId" class="rich-versions__line">
      <div class="rich-versions__line-head">
        <span class="rich-versions__dev">主开发版本 {{ line.devVersion }}</span>
        <el-tag size="mini" :type="lineStatusType(line.status)">{{ lineStatusText(line.status) }}</el-tag>
        <span v-if="headLabel(line)" class="rich-versions__lock">最新版本：{{ headLabel(line) }}</span>
        <span v-if="lockLabel(line)" class="rich-versions__lock">项目锁定：{{ lockLabel(line) }}</span>
        <el-button v-if="manage && canAdopt(line)" type="text" size="mini" @click="adopt">纳入项目</el-button>
      </div>
      <el-table :data="line.main" size="mini" border>
        <el-table-column width="40" align="center">
          <template slot-scope="scope">
            <el-checkbox :value="selected.indexOf(scope.row.id) >= 0" @change="toggle(scope.row.id)"></el-checkbox>
          </template>
        </el-table-column>
        <el-table-column label="特性版本" width="130">
          <template slot-scope="scope">{{ scope.row.featureLabel || '-' }}</template>
        </el-table-column>
        <el-table-column prop="label" label="内容版本" width="80"></el-table-column>
        <el-table-column label="类型" width="80">
          <template slot-scope="scope">{{ kindText(scope.row.kind) }}</template>
        </el-table-column>
        <el-table-column prop="editorName" label="编辑人" width="90"></el-table-column>
        <el-table-column label="时间" width="150">
          <template slot-scope="scope">{{ scope.row.createTime | dateFormat }}</template>
        </el-table-column>
        <el-table-column label="说明">
          <template slot-scope="scope">{{ scope.row.changeNote || '' }}</template>
        </el-table-column>
        <el-table-column label="引用" width="200">
          <template slot-scope="scope">{{ pinsText(scope.row.pins) }}</template>
        </el-table-column>
        <el-table-column label="正式文件" width="110" align="center">
          <template slot-scope="scope">
            <el-button type="text" size="mini" @click="openFile(scope.row, 'pdf')">PDF</el-button>
            <el-button type="text" size="mini" @click="openFile(scope.row, 'docx')">Word</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-for="b in line.branches" :key="b.id" class="rich-versions__branch">
        <div class="rich-versions__branch-head">
          {{ b.adoption ? '纳入分支' : (b.editRecordId ? '修订分支' : '修改分支') }}：{{ b.name }}
          <el-tag size="mini" :type="branchStatusType(b.status)">{{ branchStatusText(b.status) }}</el-tag>
          <span class="rich-versions__muted">
            <template v-if="b.featureLabel">{{ b.adoption ? '纳入' : '修订为' }} {{ b.featureLabel }}，</template>基线 {{ b.baseVersionLabel || '-' }}，最新 {{ b.headVersionLabel || '-' }}，创建人 {{ b.creatorName || '-' }}
          </span>
          <el-button v-if="manage && (b.status === 'OPEN' || b.status === 'OUTDATED')" type="text" size="mini" @click="abandon(b)">放弃</el-button>
          <el-button v-if="manage && !b.adoption && (b.status === 'OPEN' || b.status === 'OUTDATED')" type="text" size="mini" @click="rebase(b)">更新基线</el-button>
          <el-button v-if="manage && b.adoption && b.status === 'OPEN'" type="text" size="mini" @click="openReview(b)">发起变更评审</el-button>
          <el-button v-if="manage && !b.editRecordId && b.status === 'APPROVED'" type="text" size="mini" @click="openMerge(b)">合入确认</el-button>
        </div>
        <el-table v-if="b.versions && b.versions.length" :data="b.versions" size="mini" border>
          <el-table-column width="40" align="center">
            <template slot-scope="scope">
              <el-checkbox :value="selected.indexOf(scope.row.id) >= 0" @change="toggle(scope.row.id)"></el-checkbox>
            </template>
          </el-table-column>
          <el-table-column label="特性版本" width="130">
            <template slot-scope="scope">{{ scope.row.featureLabel || '-' }}</template>
          </el-table-column>
          <el-table-column prop="label" label="内容版本" width="80"></el-table-column>
          <el-table-column prop="editorName" label="编辑人" width="90"></el-table-column>
          <el-table-column label="时间" width="150">
            <template slot-scope="scope">{{ scope.row.createTime | dateFormat }}</template>
          </el-table-column>
          <el-table-column label="说明">
            <template slot-scope="scope">{{ scope.row.changeNote || '' }}</template>
          </el-table-column>
          <el-table-column label="正式文件" width="110" align="center">
            <template slot-scope="scope">
              <el-button type="text" size="mini" @click="openFile(scope.row, 'pdf')">PDF</el-button>
              <el-button type="text" size="mini" @click="openFile(scope.row, 'docx')">Word</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
    <div v-if="lines.length" class="rich-versions__actions">
      <el-button size="small" type="primary" :disabled="selected.length !== 2" @click="compare">对比所选两个版本</el-button>
      <span class="rich-versions__muted">已选 {{ selected.length }} 个</span>
      <el-button size="small" @click="graphVisible = true">以关系图查看</el-button>
      <el-button v-if="manage" size="small" @click="createBranch">新建修改分支</el-button>
    </div>

    <el-dialog title="版本对比" width="29cm" append-to-body :visible.sync="compareVisible">
      <div v-loading="comparing">
        <RichCompareView v-if="!comparing" :rows="compareRows" :from-label="labelOf(compareIds[0])"
                         :to-label="labelOf(compareIds[1])"></RichCompareView>
      </div>
    </el-dialog>

    <el-dialog title="版本演进" width="32cm" append-to-body :visible.sync="graphVisible">
      <RichLineageGraph v-if="graphVisible" url="/customer/character/rich-doc/get-lineage"
                        :params="{ characterId: characterId }"></RichLineageGraph>
    </el-dialog>

    <el-dialog title="更新基线：章节有交集" width="29cm" append-to-body :visible.sync="conflictVisible">
      <p>下列章节在分支与当前最新版本中都有修改，请确定保留的内容后再次提交。</p>
      <div v-for="c in conflicts" :key="c.code" style="margin-bottom: 12px;">
        <div style="font-weight: 600;">{{ c.name }}</div>
        <div style="color: #666;">当前最新版本</div>
        <div v-rich-html="c.currentHtml"></div>
        <div style="color: #666;">本分支</div>
        <div v-rich-html="c.branchHtml"></div>
        <el-input type="textarea" :rows="3" v-model="resolved[c.code]" placeholder="填写本节最终内容"></el-input>
      </div>
      <span slot="footer">
        <el-button size="small" type="primary" @click="rebase(conflictBranch, true)">按上述内容更新基线</el-button>
      </span>
    </el-dialog>

    <el-dialog title="发起纳入项目变更评审" width="29cm" append-to-body :visible.sync="reviewVisible"
               :close-on-click-modal="false">
      <ReviewCreateIssueDlg v-if="reviewVisible && activeBranch" :key="activeBranch.id + '-adopt-review'"
                            :x-id="characterId" :doc-type="DOC_TYPE.CHARACTER.key"
                            :project-type="PROJECT_TYPE.SUB_PROJECT.key" :rich-branch-id="activeBranch.id"
                            :close-dlg="closeReview"></ReviewCreateIssueDlg>
    </el-dialog>

    <el-dialog title="纳入项目合入确认" width="29cm" append-to-body :visible.sync="mergeVisible"
               :close-on-click-modal="false">
      <RichMergeConfirmDlg v-if="mergeVisible && activeBranch" :key="activeBranch.id + '-merge'"
                           :branch-id="activeBranch.id" :close-dlg="closeMerge" @merged="afterMerged"></RichMergeConfirmDlg>
    </el-dialog>

    <RichFileViewer :visible.sync="viewerVisible" :version-id="viewerVersion ? viewerVersion.id : null"
                    :format="viewerFormat" :title="viewerVersion ? viewerVersion.label : ''"></RichFileViewer>
  </div>
</template>

<script>
import Swal from 'sweetalert2'
import RichCompareView from './rich-compare-view.vue'
import RichFileViewer from './rich-file-viewer.vue'
import RichLineageGraph from './rich-lineage-graph.vue'
import RichMergeConfirmDlg from './rich-merge-confirm-dlg.vue'
import ReviewCreateIssueDlg from '@/components/std/review-create-issue-dlg.vue'
import VersionLabel from '@/shared/version-label'

const KIND_TEXT = { DERIVED: '派生起点', DRAFT: '编制版本', WORK: '工作版本', LOCK: '锁定版本' }
const LINE_STATUS_TEXT = { DRAFTING: '编制中', ACTIVE: '已锁定', CLOSED: '已关闭' }
const BRANCH_STATUS_TEXT = {
  OPEN: '编辑中', REVIEWING: '变更评审中', APPROVED: '待合入',
  MERGED: '已合入', ABANDONED: '已放弃', OUTDATED: '已过期'
}

export default {
  name: 'rich-version-panel',
  components: { RichCompareView, RichFileViewer, RichLineageGraph, RichMergeConfirmDlg, ReviewCreateIssueDlg },
  props: {
    characterId: { type: String, default: null },
    manage: { type: Boolean, default: false }
  },
  data () {
    return {
      loading: false,
      lines: [],
      selected: [],
      compareVisible: false,
      comparing: false,
      compareRows: [],
      compareIds: [],
      graphVisible: false,
      viewerVisible: false,
      viewerVersion: null,
      viewerFormat: 'pdf',
      conflictVisible: false,
      conflicts: [],
      conflictBranch: null,
      resolved: {},
      activeBranch: null,
      reviewVisible: false,
      mergeVisible: false,
      DOC_TYPE: DOC_TYPE,
      PROJECT_TYPE: PROJECT_TYPE
    }
  },
  watch: {
    characterId () {
      this.load()
    }
  },
  mounted () {
    this.load()
  },
  methods: {
    post (path, body) {
      return this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/rich-doc' + path, body)
    },
    createBranch () {
      const name = window.prompt('修改分支名称')
      if (!name) return
      this.post('/branch/create', { characterId: this.characterId, name }).then(response => {
        const resp = response.data
        if (resp.success) {
          Toast.success('已新建修改分支', Swal)
          this.load()
        } else Toast.warning(resp.message, Swal)
      })
    },
    abandon (b) {
      this.post('/branch/abandon', { branchId: b.id, reason: '放弃修改分支' }).then(response => {
        const resp = response.data
        if (resp.success) this.load()
        else Toast.warning(resp.message, Swal)
      })
    },
    rebase (b, withResolved) {
      const sections = withResolved
        ? this.conflicts.map(c => ({ code: c.code, html: this.resolved[c.code] || '' }))
        : []
      this.post('/branch/rebase', { branchId: b.id, sections }).then(response => {
        const resp = response.data
        if (!resp.success) {
          Toast.warning(resp.message, Swal)
          return
        }
        const conflicts = (resp.content && resp.content.conflicts) || []
        if (conflicts.length) {
          this.conflictBranch = b
          this.conflicts = conflicts
          const resolved = {}
          conflicts.forEach(c => { resolved[c.code] = c.branchHtml || '' })
          this.resolved = resolved
          this.conflictVisible = true
          return
        }
        this.conflictVisible = false
        Toast.success('已更新基线', Swal)
        this.load()
      })
    },
    load () {
      if (!this.characterId) return
      this.loading = true
      this.selected = []
      this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/rich-doc/get-versions', { characterId: this.characterId })
        .then(response => {
          const resp = response.data
          if (resp.success) {
            this.lines = resp.content || []
          } else {
            Toast.warning(resp.message, Swal)
          }
        })
        .finally(() => { this.loading = false })
    },
    toggle (id) {
      const i = this.selected.indexOf(id)
      if (i >= 0) {
        this.selected.splice(i, 1)
      } else {
        if (this.selected.length >= 2) this.selected.shift()
        this.selected.push(id)
      }
    },
    compare () {
      if (this.selected.length !== 2) return
      this.compareVisible = true
      this.comparing = true
      this.compareRows = []
      this.compareIds = this.selected.slice()
      this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/rich-doc/get-compare', {
        fromVersionId: this.selected[0],
        toVersionId: this.selected[1]
      }).then(response => {
        const resp = response.data
        if (resp.success) {
          this.compareRows = resp.content || []
        } else {
          Toast.warning(resp.message, Swal)
        }
      }).finally(() => { this.comparing = false })
    },
    openFile (row, format) {
      this.viewerVersion = row
      this.viewerFormat = format
      this.viewerVisible = true
    },
    labelOf (id) {
      for (const line of this.lines) {
        const all = (line.main || []).concat(...(line.branches || []).map(b => b.versions || []))
        const v = all.find(r => r.id === id)
        if (v) return v.featureLabel ? v.featureLabel + '（' + v.label + '）' : VersionLabel.featureWithContent(line.devVersion, null, v.label)
      }
      return ''
    },
    rowOf (line, id) {
      return id ? (line.main || []).find(r => r.id === id) : null
    },
    lockLabel (line) {
      const v = this.rowOf(line, line.currentLockVersionId)
      return v ? (v.featureLabel || VersionLabel.featureWithContent(line.devVersion, null, v.label)) : ''
    },
    headLabel (line) {
      const v = this.rowOf(line, line.headVersionId)
      return v && v.featureLabel && line.headVersionId !== line.currentLockVersionId ? v.featureLabel : ''
    },
    canAdopt (line) {
      if (line.status !== 'ACTIVE') return false
      const head = this.rowOf(line, line.headVersionId)
      const lock = this.rowOf(line, line.currentLockVersionId)
      if (!head || head.featureNo == null || (lock && lock.featureNo != null && head.featureNo <= lock.featureNo)) {
        return false
      }
      return !(line.branches || []).some(b => b.adoption && (b.status === 'REVIEWING' || b.status === 'APPROVED'))
    },
    adopt () {
      this.post('/adoption/create', { characterId: this.characterId }).then(response => {
        const resp = response.data
        if (!resp.success) {
          Toast.warning(resp.message, Swal)
          return
        }
        this.load()
        this.openReview(resp.content)
      })
    },
    openReview (b) {
      this.activeBranch = b
      this.reviewVisible = true
    },
    closeReview () {
      this.reviewVisible = false
      this.load()
    },
    openMerge (b) {
      this.activeBranch = b
      this.mergeVisible = true
    },
    closeMerge () {
      this.mergeVisible = false
    },
    afterMerged () {
      this.mergeVisible = false
      this.load()
      this.$bus.publish({ areas: ['project', 'jobitem', 'badge'], reason: 'rich-merge' })
    },
    pinsText (pins) {
      return (pins || []).filter(p => p.active === 1 || p.active === true).map(p => p.pinText || p.pinType).join('、')
    },
    kindText (k) {
      return KIND_TEXT[k] || k
    },
    lineStatusText (s) {
      return LINE_STATUS_TEXT[s] || s
    },
    lineStatusType (s) {
      return s === 'DRAFTING' ? '' : s === 'ACTIVE' ? 'success' : 'info'
    },
    branchStatusText (s) {
      return BRANCH_STATUS_TEXT[s] || s
    },
    branchStatusType (s) {
      return s === 'APPROVED' || s === 'MERGED' ? 'success' : s === 'REVIEWING' ? 'warning' : s === 'OPEN' ? '' : 'info'
    }
  }
}
</script>

<style scoped>
.rich-versions { text-align: left; }
.rich-versions__empty { color: #999; padding: 8px 0; }
.rich-versions__line { margin-bottom: 14px; }
.rich-versions__line-head { display: flex; align-items: center; gap: 8px; margin-bottom: 6px; }
.rich-versions__dev { font-weight: 600; }
.rich-versions__lock, .rich-versions__muted { color: #888; font-size: 12px; }
.rich-versions__branch { margin: 8px 0 0 16px; }
.rich-versions__branch-head { display: flex; align-items: center; gap: 8px; margin-bottom: 4px; }
.rich-versions__actions { display: flex; align-items: center; gap: 10px; }
</style>
