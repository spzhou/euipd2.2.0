<!--
 * 版本演进关系图（实现方案第 9.3 节）：AntV G6 4.x，dagre 自左向右布局。
 * url 为 get-lineage 接口（特性、CBB、业务对象或开发版本），params 为其请求参数；
 * 节点为版本，关系为演进关系。点击节点打开侧栏：只读打开、下载、与当前锁定版本或上一版本对比。
-->
<template>
  <div class="rich-lineage">
    <div class="rich-lineage__bar">
      <el-checkbox v-model="includeWork" @change="load">显示工作版本与编制版本</el-checkbox>
      <span class="rich-lineage__label">展开层数</span>
      <el-select v-model="depth" size="mini" style="width: 70px;" @change="load">
        <el-option v-for="d in [1, 2, 3]" :key="d" :label="d" :value="d"></el-option>
      </el-select>
      <span class="rich-lineage__label">主开发版本</span>
      <el-select v-model="devFilter" size="mini" clearable placeholder="全部" style="width: 130px;" @change="draw">
        <el-option v-for="d in devVersions" :key="d" :label="d" :value="d"></el-option>
      </el-select>
      <span class="rich-lineage__label">版本类型</span>
      <el-select v-model="kindFilter" size="mini" multiple collapse-tags placeholder="全部" style="width: 150px;" @change="draw">
        <el-option v-for="(t, k) in KIND_TEXT" :key="k" :label="t" :value="k"></el-option>
      </el-select>
      <span class="rich-lineage__spacer"></span>
      <el-button size="mini" icon="el-icon-refresh" @click="load">刷新</el-button>
      <el-button size="mini" icon="el-icon-picture-outline" :disabled="!graph" @click="exportPng">导出 PNG</el-button>
    </div>
    <div class="rich-lineage__legend">
      <span><i class="lg lg-lock"></i>锁定版本</span>
      <span><i class="lg lg-work"></i>工作版本</span>
      <span><i class="lg lg-draft"></i>编制版本</span>
      <span><i class="lg lg-derived"></i>派生起点</span>
      <span>细实线：修改；粗实线：锁定、合入；虚线：分解、合并、迭代、生成 CBB</span>
    </div>
    <div ref="canvas" class="rich-lineage__canvas" :style="{ height: height + 'px' }" v-loading="loading">
      <div v-if="!loading && nodes.length === 0" class="rich-lineage__empty">尚无内容版本</div>
    </div>

    <el-drawer :visible.sync="drawerVisible" size="360px" append-to-body :title="drawerTitle">
      <div v-if="current" class="rich-lineage__side">
        <p><b>{{ current.title }}</b></p>
        <p>版本 {{ current.label }}（{{ kindText(current.kind) }}）<el-tag v-if="current.current" size="mini" type="success">当前</el-tag></p>
        <p><template v-if="current.featureLabel">特性版本 {{ current.featureLabel }}；</template>主开发版本 {{ current.devVersion || '-' }}；保存人 {{ current.editorName || '-' }}；{{ current.createTime | dateFormat }}</p>
        <p v-if="current.changeNote">说明：{{ current.changeNote }}</p>
        <p v-if="current.changedSections">改动章节：{{ current.changedSections }}</p>
        <p v-if="pinsText(current)">引用：{{ pinsText(current) }}</p>
        <div class="rich-lineage__actions">
          <el-button size="small" @click="openFile('pdf')">以 PDF 只读打开</el-button>
          <el-button size="small" @click="openFile('docx')">以 Word 只读打开</el-button>
          <el-button size="small" :disabled="!lockOf(current) || lockOf(current).id === current.id"
                     @click="compareWith(lockOf(current))">与当前锁定版本对比</el-button>
          <el-button size="small" :disabled="!previousOf(current)" @click="compareWith(previousOf(current))">与上一版本对比</el-button>
        </div>
      </div>
    </el-drawer>

    <RichFileViewer :visible.sync="viewerVisible" :version-id="viewerVersionId"
                    :format="viewerFormat" :title="viewerTitle"></RichFileViewer>

    <el-dialog title="演进关系" width="520px" append-to-body :visible.sync="edgeDlg.visible">
      <div v-if="edgeDlg.edge">
        <p>关系：{{ edgeText(edgeDlg.edge) }}</p>
        <p v-if="edgeDlg.edge.refType">关联：{{ edgeDlg.edge.refType }} {{ edgeDlg.edge.refId }}</p>
        <p>起点：{{ nodeTitle(edgeDlg.edge.from) }}</p>
        <p>终点：{{ nodeTitle(edgeDlg.edge.to) }}</p>
        <el-button size="small" @click="openVersion(edgeDlg.edge.from)">以 PDF 打开起点</el-button>
        <el-button size="small" @click="openVersion(edgeDlg.edge.to)">以 PDF 打开终点</el-button>
      </div>
    </el-dialog>

    <el-dialog title="版本对比" width="29cm" append-to-body :visible.sync="compareVisible">
      <div v-loading="comparing">
        <RichCompareView v-if="!comparing" :rows="compareRows" :from-label="compareFrom" :to-label="compareTo"></RichCompareView>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import Swal from 'sweetalert2'
import RichFileViewer from './rich-file-viewer.vue'
import RichCompareView from './rich-compare-view.vue'

const KIND_TEXT = { LOCK: '锁定版本', WORK: '工作版本', DRAFT: '编制版本', DERIVED: '派生起点' }
const EDGE_TEXT = {
  EDIT: '', BRANCH: '分支', LOCK: '锁定', MERGE_IN: '合入', SUCCEED: '接替', REBASE: '更新基线',
  RESTORE: '恢复', ITERATE: '迭代', SPLIT: '分解', MERGE: '合并', PUBLISH: '生成 CBB'
}
const DASHED = ['ITERATE', 'SPLIT', 'MERGE', 'PUBLISH', 'REBASE', 'RESTORE']
const THICK = ['LOCK', 'MERGE_IN', 'SUCCEED']
const COMPARE_PREFIX = {
  CHARACTER_DEF: 'character', CBB_DEF: 'cbb', DEMAND: 'demand', QUALITY: 'quality',
  BUG: 'bugfeedback', JOBITEM_PROPERTY: 'jobitem-property'
}

let g6Promise = null
function loadG6 () {
  if (!g6Promise) g6Promise = import(/* webpackChunkName: "g6" */ '@antv/g6').then(m => m.default || m)
  return g6Promise
}

function dateText (t) {
  if (!t) return ''
  const d = new Date(t)
  if (isNaN(d.getTime())) return String(t).substring(0, 10)
  const p = n => (n < 10 ? '0' : '') + n
  return d.getFullYear() + '-' + p(d.getMonth() + 1) + '-' + p(d.getDate())
}

export default {
  name: 'rich-lineage-graph',
  components: { RichFileViewer, RichCompareView },
  props: {
    url: { type: String, required: true },
    params: { type: Object, default: () => ({}) },
    defaultIncludeWork: { type: Boolean, default: true },
    height: { type: Number, default: 520 }
  },
  data () {
    return {
      KIND_TEXT,
      includeWork: this.defaultIncludeWork,
      depth: 1,
      devFilter: '',
      kindFilter: [],
      loading: false,
      nodes: [],
      edges: [],
      graph: null,
      current: null,
      drawerVisible: false,
      viewerVisible: false,
      viewerFormat: 'pdf',
      compareVisible: false,
      comparing: false,
      compareRows: [],
      compareFrom: '',
      compareTo: '',
      expandedBranches: {},
      viewerVersionId: null,
      viewerTitle: '',
      edgeDlg: { visible: false, edge: null }
    }
  },
  computed: {
    devVersions () {
      return Array.from(new Set(this.nodes.map(n => n.devVersion).filter(Boolean))).sort()
    },
    drawerTitle () {
      return this.current ? '版本 ' + this.current.label : '版本'
    },
    paramsKey () {
      return JSON.stringify(this.params || {})
    }
  },
  watch: {
    url () { this.load() },
    paramsKey () { this.load() }
  },
  mounted () {
    this.load()
    window.addEventListener('resize', this.fit)
  },
  beforeDestroy () {
    window.removeEventListener('resize', this.fit)
    if (this.graph) this.graph.destroy()
  },
  methods: {
    load () {
      if (!this.url) return
      this.loading = true
      const body = Object.assign({}, this.params, { includeWork: this.includeWork, depth: this.depth })
      this.$ajax.post(process.env.VUE_APP_SERVER + this.url, body)
        .then(response => {
          const resp = response.data
          if (!resp.success) {
            Toast.warning(resp.message, Swal)
            return
          }
          this.nodes = (resp.content && resp.content.nodes) || []
          this.edges = (resp.content && resp.content.edges) || []
          return this.draw()
        })
        .finally(() => { this.loading = false })
    },
    visibleNodes () {
      return this.nodes.filter(n => n.placeholder ||
        ((!this.devFilter || n.devVersion === this.devFilter) &&
          (this.kindFilter.length === 0 || this.kindFilter.indexOf(n.kind) >= 0)))
    },
    toGraphData () {
      const visible = this.visibleNodes()
      const fold = {}
      visible.forEach(n => {
        if (n.branchId && (n.kind === 'WORK' || n.kind === 'DRAFT') && !this.expandedBranches[n.branchId]) {
          (fold[n.branchId] = fold[n.branchId] || []).push(n)
        }
      })
      const idMap = {}
      const summaries = []
      Object.keys(fold).forEach(branchId => {
        const group = fold[branchId]
        const id = 'sum-' + branchId
        group.forEach(n => { idMap[n.id] = id })
        const sample = group[0]
        summaries.push({
          id: id,
          summary: true,
          branchId: branchId,
          title: sample.title,
          label: '分支摘要',
          kind: 'WORK',
          count: group.length,
          editorName: sample.editorName,
          placeholder: false
        })
      })
      const kept = visible.filter(n => !idMap[n.id]).concat(summaries)
      const ids = new Set(kept.map(n => n.id))
      const edges = []
      const seen = new Set()
      this.edges.forEach(e => {
        const from = idMap[e.from] || e.from
        const to = idMap[e.to] || e.to
        if (from === to || !ids.has(from) || !ids.has(to)) return
        const key = from + '>' + to + '>' + (e.type || '')
        if (seen.has(key)) return
        seen.add(key)
        edges.push(Object.assign({}, e, { from: from, to: to }))
      })
      return {
        nodes: kept.map(n => this.nodeModel(n)),
        edges: edges.map((e, i) => this.edgeModel(e, i))
      }
    },
    nodeModel (n) {
      if (n.summary) {
        return {
          id: n.id,
          label: '分支 ' + (n.editorName || '修改') + ' 的修改\n' + n.count + ' 个版本，点击展开',
          raw: n,
          style: { radius: 4, stroke: '#2a6496', fill: '#fff8e8', lineDash: [5, 3], lineWidth: 1 },
          size: [180, 50]
        }
      }
      if (n.placeholder) {
        return {
          id: n.id, label: '无权查看', raw: n,
          style: { fill: '#f0f0f0', stroke: '#bbb', lineDash: [2, 2] }, labelCfg: { style: { fill: '#999' } }
        }
      }
      const pins = this.pinsText(n)
      const lines = [
        (n.external ? n.title + ' ' : '') + n.label + ' ' + this.kindText(n.kind) + (n.current ? ' [当前]' : '')
      ]
      if (n.featureLabel) lines.push('特性版本 ' + n.featureLabel)
      lines.push((n.editorName || '') + ' ' + dateText(n.createTime))
      if (pins) lines.push(pins.length > 16 ? pins.substring(0, 16) + '…' : pins)
      const style = { radius: 4, stroke: '#2a6496', fill: '#fff', lineWidth: 1 }
      if (n.kind === 'LOCK') Object.assign(style, { lineWidth: 3, fill: '#dbeaf7' })
      else if (n.kind === 'DRAFT') Object.assign(style, { stroke: '#8aa9c4', fill: '#f6f8fa' })
      else if (n.kind === 'DERIVED') Object.assign(style, { lineDash: [4, 3], stroke: '#888' })
      return { id: n.id, label: lines.join('\n'), raw: n, style, size: [170, 18 * lines.length + 14] }
    },
    edgeModel (e, i) {
      const style = { stroke: '#7a8b99', lineWidth: 1, endArrow: true }
      if (THICK.indexOf(e.type) >= 0) style.lineWidth = e.type === 'SUCCEED' ? 4 : 2.5
      if (DASHED.indexOf(e.type) >= 0) style.lineDash = [5, 4]
      return {
        id: 'e' + i, source: e.from, target: e.to, label: EDGE_TEXT[e.type] || '', raw: e, style,
        labelCfg: { autoRotate: true, style: { fill: '#555', fontSize: 11, background: { fill: '#fff', padding: [1, 2, 1, 2] } } }
      }
    },
    draw () {
      return loadG6().then(G6 => {
        const data = this.toGraphData()
        if (this.graph) {
          this.graph.changeData(data)
          this.graph.fitView(20)
          return
        }
        const el = this.$refs.canvas
        if (!el) return
        const tooltip = new G6.Tooltip({
          offsetX: 12,
          offsetY: 12,
          itemTypes: ['node'],
          getContent: e => this.tooltipHtml(e.item.getModel().raw)
        })
        this.graph = new G6.Graph({
          container: el,
          width: el.clientWidth || 900,
          height: this.height,
          fitView: true,
          fitViewPadding: 20,
          minZoom: 0.2,
          maxZoom: 2,
          plugins: [tooltip],
          modes: { default: ['drag-canvas', 'zoom-canvas', 'drag-node'] },
          layout: { type: 'dagre', rankdir: 'LR', nodesep: 18, ranksep: 46 },
          defaultNode: { type: 'rect', labelCfg: { style: { fontSize: 12, fill: '#222' } } },
          defaultEdge: { type: 'polyline' }
        })
        this.graph.data(data)
        this.graph.render()
        this.graph.on('node:click', ev => this.onNode(ev.item.getModel().raw))
        this.graph.on('edge:click', ev => this.onEdge(ev.item.getModel().raw))
      })
    },
    fit () {
      if (!this.graph || !this.$refs.canvas) return
      this.graph.changeSize(this.$refs.canvas.clientWidth, this.height)
      this.graph.fitView(20)
    },
    tooltipHtml (n) {
      if (n && n.summary) return '<div class="rich-lineage__tip">点击展开该分支的 ' + n.count + ' 个版本</div>'
      if (!n || n.placeholder) return '<div class="rich-lineage__tip">无权查看</div>'
      const esc = s => String(s == null ? '' : s).replace(/[&<>"]/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' }[c]))
      const rows = [esc(n.title) + ' ' + esc(n.label)]
      if (n.changeNote) rows.push('说明：' + esc(n.changeNote))
      if (n.changedSections) rows.push('改动章节：' + esc(n.changedSections))
      const pins = this.pinsText(n)
      if (pins) rows.push('引用：' + esc(pins))
      return '<div class="rich-lineage__tip">' + rows.join('<br>') + '</div>'
    },
    onNode (n) {
      if (!n || n.placeholder) return
      if (n.summary) {
        this.$set(this.expandedBranches, n.branchId, !this.expandedBranches[n.branchId])
        this.draw()
        return
      }
      this.current = n
      this.drawerVisible = true
    },
    onEdge (e) {
      if (!e) return
      this.edgeDlg = { visible: true, edge: e }
    },
    edgeText (e) {
      return EDGE_TEXT[e.type] || '修改'
    },
    nodeTitle (id) {
      if (id && String(id).indexOf('sum-') === 0) return '分支摘要'
      const n = this.nodes.find(x => x.id === id)
      return n ? ((n.title || '') + ' ' + (n.label || '')).trim() : (id || '')
    },
    openVersion (id) {
      if (!id || String(id).indexOf('sum-') === 0) return
      const n = this.nodes.find(x => x.id === id)
      this.viewerVersionId = id
      this.viewerTitle = n ? ((n.title || '') + ' ' + (n.label || '')).trim() : ''
      this.viewerFormat = 'pdf'
      this.viewerVisible = true
    },
    pinsText (n) {
      return ((n && n.pins) || []).filter(p => p.active === 1 || p.active === true)
        .map(p => p.pinText || p.pinType).join('、')
    },
    kindText (k) {
      return KIND_TEXT[k] || k || ''
    },
    lockOf (n) {
      return this.nodes.find(x => x.current && x.lineId === n.lineId) || null
    },
    previousOf (n) {
      const e = this.edges.find(x => x.to === n.id && x.from !== n.id)
      return e ? this.nodes.find(x => x.id === e.from && !x.placeholder) || null : null
    },
    openFile (format) {
      if (!this.current) return
      this.viewerVersionId = this.current.id
      this.viewerTitle = (this.current.title || '') + ' ' + (this.current.label || '')
      this.viewerFormat = format
      this.viewerVisible = true
    },
    compareWith (other) {
      if (!other || !this.current) return
      const prefix = COMPARE_PREFIX[this.current.bizType] || 'character'
      this.compareFrom = other.title + ' ' + other.label
      this.compareTo = this.current.title + ' ' + this.current.label
      this.compareVisible = true
      this.comparing = true
      this.compareRows = []
      this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/' + prefix + '/rich-doc/get-compare',
        { fromVersionId: other.id, toVersionId: this.current.id, bizId: this.current.bizKey })
        .then(response => {
          const resp = response.data
          if (resp.success) this.compareRows = resp.content || []
          else Toast.warning(resp.message, Swal)
        })
        .finally(() => { this.comparing = false })
    },
    exportPng () {
      if (this.graph) this.graph.downloadFullImage('版本演进图', 'image/png', { backgroundColor: '#fff', padding: 20 })
    }
  }
}
</script>

<style scoped>
.rich-lineage { text-align: left; }
.rich-lineage__bar { display: flex; align-items: center; flex-wrap: wrap; gap: 6px; margin-bottom: 6px; }
.rich-lineage__label { color: #666; font-size: 12px; margin-left: 8px; }
.rich-lineage__spacer { flex: 1; }
.rich-lineage__legend { display: flex; flex-wrap: wrap; gap: 14px; color: #777; font-size: 12px; margin-bottom: 6px; }
.rich-lineage__legend .lg { display: inline-block; width: 18px; height: 11px; margin-right: 4px; vertical-align: middle; border: 1px solid #2a6496; background: #fff; }
.rich-lineage__legend .lg-lock { border-width: 3px; background: #dbeaf7; }
.rich-lineage__legend .lg-draft { border-color: #8aa9c4; background: #f6f8fa; }
.rich-lineage__legend .lg-derived { border-style: dashed; border-color: #888; }
.rich-lineage__canvas { position: relative; border: 1px solid #eee; border-radius: 4px; background: #fff; }
.rich-lineage__empty { position: absolute; inset: 0; display: flex; align-items: center; justify-content: center; color: #999; }
.rich-lineage__side { padding: 0 16px; line-height: 1.8; }
.rich-lineage__actions { display: flex; flex-direction: column; gap: 8px; margin-top: 12px; }
.rich-lineage__actions .el-button + .el-button { margin-left: 0; }
</style>

<style>
.rich-lineage__tip { max-width: 320px; padding: 6px 8px; font-size: 12px; line-height: 1.6; background: #fff; border: 1px solid #ddd; box-shadow: 0 1px 4px rgba(0, 0, 0, 0.15); }
</style>
