<!--
 * 正式文件只读查看（实现方案第 9.4 节）：PDF 由 get-content 取字节后以 pdfjs-dist 绘制；
 * Word 由 get-viewer 取一次性 WOPI 凭证，以表单提交到 Collabora 只读页并在 iframe 中显示。
 * 下载经 signed-url 取 5 分钟有效的附件地址。
-->
<template>
  <el-dialog :title="dialogTitle" :visible="visible" width="92%" top="3vh" append-to-body
             :close-on-click-modal="false" @close="close">
    <div class="rich-viewer__bar">
      <el-radio-group v-model="mode" size="mini" @change="open">
        <el-radio-button label="pdf">PDF</el-radio-button>
        <el-radio-button label="docx">Word</el-radio-button>
      </el-radio-group>
      <el-checkbox v-model="keepColor" style="margin-left: 12px;" @change="open">保留原文字颜色</el-checkbox>
      <span class="rich-viewer__spacer"></span>
      <el-button size="mini" icon="el-icon-download" :disabled="!versionId" @click="download('pdf')">下载 PDF</el-button>
      <el-button size="mini" icon="el-icon-download" :disabled="!versionId" @click="download('docx')">下载 Word</el-button>
    </div>
    <div ref="body" class="rich-viewer__body" v-loading="loading" :element-loading-text="loadingText">
      <div v-if="error" class="rich-viewer__error">{{ error }}</div>
      <div v-show="mode === 'pdf' && !error" ref="pages" class="rich-viewer__pages"></div>
      <iframe v-if="mode === 'docx' && wopi" :name="frameName" class="rich-viewer__frame"
              allow="clipboard-read; clipboard-write" title="Word 只读查看"></iframe>
      <form v-if="wopi" ref="wopiForm" :action="wopi.url" method="post" :target="frameName" style="display: none;">
        <input type="hidden" name="access_token" :value="wopi.accessToken">
        <input type="hidden" name="access_token_ttl" :value="wopi.accessTokenTtl">
      </form>
    </div>
  </el-dialog>
</template>

<script>
import Swal from 'sweetalert2'

const RENDER = '/file/rdms/rich-render/'
const SLOW_MS = 3000
const MAX_PAGE_WIDTH = 1000
// 单个 canvas 像素上限（Safari 约 16.7M），超出时降低清晰度以免整页空白
const MAX_CANVAS_PIXELS = 16777216
// 视口上下各预绘制的距离，滚出该范围的页面释放画布
const RENDER_MARGIN = '1200px 0px'

let pdfjsPromise = null
function loadPdfjs () {
  if (!pdfjsPromise) {
    pdfjsPromise = Promise.all([
      import(/* webpackChunkName: "pdfjs" */ 'pdfjs-dist/legacy/build/pdf'),
      import(/* webpackChunkName: "pdfjs" */ '!!file-loader?name=static/js/[name].[hash:8].[ext]!pdfjs-dist/legacy/build/pdf.worker.min.js')
    ]).then(([lib, worker]) => {
      lib.GlobalWorkerOptions.workerSrc = worker.default || worker
      return lib
    })
  }
  return pdfjsPromise
}

function messageOf (buffer) {
  try {
    const body = JSON.parse(new TextDecoder('utf-8').decode(buffer))
    return body && body.message ? body.message : '文件生成失败'
  } catch (e) {
    return '文件生成失败'
  }
}

export default {
  name: 'rich-file-viewer',
  props: {
    visible: { type: Boolean, default: false },
    versionId: { type: String, default: null },
    format: { type: String, default: 'pdf' },
    title: { type: String, default: '' }
  },
  data () {
    return {
      mode: this.format === 'docx' ? 'docx' : 'pdf',
      keepColor: false,
      loading: false,
      slow: false,
      error: '',
      wopi: null,
      frameName: 'rich-wopi-' + Math.random().toString(36).slice(2, 10),
      seq: 0
    }
  },
  computed: {
    dialogTitle () {
      return this.title ? '正式文件：' + this.title : '正式文件'
    },
    loadingText () {
      return this.slow ? '首次生成需要较长时间，请稍候…' : '正在打开…'
    }
  },
  watch: {
    visible (val) {
      if (val) {
        this.mode = this.format === 'docx' ? 'docx' : 'pdf'
        this.$nextTick(this.open)
      } else {
        this.reset()
      }
    },
    versionId () {
      if (this.visible) this.open()
    }
  },
  created () {
    // pdf.js 文档与页面对象体量大且含 worker 句柄，放在 data 外以免被 Vue 深度观测
    this.pdfDoc = null
    this.pageSlots = []
    this.pageObserver = null
  },
  mounted () {
    if (this.visible) this.$nextTick(this.open)
  },
  beforeDestroy () {
    this.reset()
  },
  methods: {
    close () {
      this.$emit('update:visible', false)
    },
    reset () {
      this.seq++
      this.wopi = null
      this.error = ''
      this.loading = false
      if (this.pageObserver) {
        this.pageObserver.disconnect()
        this.pageObserver = null
      }
      this.pageSlots.forEach(this.releasePage)
      this.pageSlots = []
      if (this.pdfDoc) {
        this.pdfDoc.destroy()
        this.pdfDoc = null
      }
      if (this.$refs.pages) this.$refs.pages.innerHTML = ''
    },
    begin () {
      this.reset()
      const seq = this.seq
      this.loading = true
      this.slow = false
      setTimeout(() => { if (seq === this.seq && this.loading) this.slow = true }, SLOW_MS)
      return seq
    },
    open () {
      if (!this.versionId) return
      if (this.mode === 'docx') this.openWord()
      else this.openPdf()
    },
    openPdf () {
      const seq = this.begin()
      this.$ajax.post(process.env.VUE_APP_SERVER + RENDER + 'get-content',
        { versionId: this.versionId, keepColor: this.keepColor }, { responseType: 'arraybuffer' })
        .then(response => {
          if (seq !== this.seq) return null
          const type = String(response.headers['content-type'] || '')
          if (type.indexOf('application/pdf') < 0) throw new Error(messageOf(response.data))
          return loadPdfjs().then(lib => lib.getDocument({ data: new Uint8Array(response.data) }).promise)
        })
        .then(doc => {
          if (!doc) return null
          if (seq !== this.seq) {
            doc.destroy()
            return null
          }
          this.pdfDoc = doc
          return this.layoutPages(doc, seq)
        })
        .catch(e => { if (seq === this.seq) this.fail(e) })
        .finally(() => { if (seq === this.seq) this.loading = false })
    },
    async layoutPages (doc, seq) {
      const box = this.$refs.pages
      const width = Math.min(box.clientWidth || 900, MAX_PAGE_WIDTH)
      const observer = typeof IntersectionObserver === 'function'
        ? new IntersectionObserver(this.onPageIntersect, { root: this.$refs.body, rootMargin: RENDER_MARGIN })
        : null
      this.pageObserver = observer
      for (let n = 1; n <= doc.numPages; n++) {
        const page = await doc.getPage(n)
        if (seq !== this.seq) return
        const base = page.getViewport({ scale: 1 })
        const scale = width / base.width
        const el = document.createElement('div')
        el.className = 'rich-viewer__page'
        el.style.width = Math.floor(base.width * scale) + 'px'
        el.style.height = Math.floor(base.height * scale) + 'px'
        el.dataset.index = String(this.pageSlots.length)
        const slot = { page, scale, el, canvas: null, task: null }
        this.pageSlots.push(slot)
        box.appendChild(el)
        if (observer) observer.observe(el)
        else await this.drawPage(slot)
        if (n === 1) this.loading = false
      }
    },
    onPageIntersect (entries) {
      entries.forEach(entry => {
        const slot = this.pageSlots[Number(entry.target.dataset.index)]
        if (!slot || slot.el !== entry.target) return
        if (entry.isIntersecting) this.drawPage(slot)
        else this.releasePage(slot)
      })
    },
    drawPage (slot) {
      if (slot.canvas) return Promise.resolve()
      const ratio = window.devicePixelRatio || 1
      let viewport = slot.page.getViewport({ scale: slot.scale * ratio })
      const pixels = viewport.width * viewport.height
      if (pixels > MAX_CANVAS_PIXELS) {
        viewport = slot.page.getViewport({ scale: slot.scale * ratio * Math.sqrt(MAX_CANVAS_PIXELS / pixels) })
      }
      const canvas = document.createElement('canvas')
      canvas.width = Math.floor(viewport.width)
      canvas.height = Math.floor(viewport.height)
      const ctx = canvas.getContext('2d')
      if (!ctx) return Promise.resolve()
      slot.canvas = canvas
      slot.el.appendChild(canvas)
      const task = slot.page.render({ canvasContext: ctx, viewport })
      slot.task = task
      return task.promise
        .catch(e => {
          if (!e || e.name !== 'RenderingCancelledException') console.error('PDF 页面绘制失败:', e)
        })
        .finally(() => {
          if (slot.task === task) slot.task = null
        })
    },
    releasePage (slot) {
      if (slot.task) {
        slot.task.cancel()
        slot.task = null
      }
      if (slot.canvas) {
        slot.canvas.width = 0
        slot.canvas.height = 0
        slot.canvas.remove()
        slot.canvas = null
      }
    },
    openWord () {
      const seq = this.begin()
      this.$ajax.post(process.env.VUE_APP_SERVER + RENDER + 'get-viewer',
        { versionId: this.versionId, keepColor: this.keepColor })
        .then(response => {
          if (seq !== this.seq) return
          const resp = response.data
          if (!resp.success) throw new Error(resp.message)
          this.wopi = resp.content
          this.$nextTick(() => {
            if (this.$refs.wopiForm) this.$refs.wopiForm.submit()
          })
        })
        .catch(e => { if (seq === this.seq) this.fail(e) })
        .finally(() => { if (seq === this.seq) this.loading = false })
    },
    download (format) {
      this.$ajax.post(process.env.VUE_APP_SERVER + RENDER + 'signed-url',
        { versionId: this.versionId, format, keepColor: this.keepColor })
        .then(response => {
          const resp = response.data
          if (!resp.success) {
            Toast.warning(resp.message, Swal)
            return
          }
          const a = document.createElement('a')
          a.href = resp.content.url
          a.rel = 'noopener'
          document.body.appendChild(a)
          a.click()
          a.remove()
        })
    },
    fail (e) {
      this.error = (e && e.message) || '文件打开失败'
    }
  }
}
</script>

<style scoped>
.rich-viewer__bar { display: flex; align-items: center; gap: 6px; margin-bottom: 8px; }
.rich-viewer__spacer { flex: 1; }
.rich-viewer__body { min-height: 60vh; max-height: 80vh; overflow: auto; background: #f2f2f2; }
.rich-viewer__pages { display: flex; flex-direction: column; align-items: center; padding: 12px 0; gap: 12px; }
.rich-viewer__pages >>> .rich-viewer__page { flex: none; background: #fff; box-shadow: 0 1px 4px rgba(0, 0, 0, 0.2); }
.rich-viewer__pages >>> .rich-viewer__page canvas { display: block; width: 100%; height: 100%; }
.rich-viewer__frame { width: 100%; height: 78vh; border: 0; background: #fff; }
.rich-viewer__error { padding: 40px; text-align: center; color: #d15b47; }
</style>
