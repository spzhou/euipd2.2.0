<!--
 * Word 导入（实现方案第 10.4 节）：上传 .docx、.doc、.wps、.odt、.rtf（20MB 以内），后端按一级标题拆分章节；
 * 预览每个章节，未能归入章节的内容可以指派追加到某一章节；确认后以 apply 事件返回 { 字段名: HTML }，
 * 由页面填入编辑器，保存时来源记为 IMPORT。
-->
<template>
  <el-dialog title="从 Word 导入" :visible="visible" width="26cm" append-to-body
             :close-on-click-modal="false" @close="close">
    <div v-loading="parsing" element-loading-text="正在解析文档…">
      <div class="rich-import__pick">
        <input ref="file" type="file" accept=".docx,.doc,.wps,.odt,.rtf" @change="onPick">
        <span class="rich-import__hint">支持 .docx、.doc、.wps、.odt、.rtf，20MB 以内；一级标题与章节名称一致的内容归入对应章节</span>
      </div>
      <div v-if="result">
        <el-alert v-for="(w, i) in result.warnings" :key="'w' + i" :title="w" type="warning" :closable="false"
                  show-icon style="margin-bottom: 6px;"></el-alert>
        <div v-for="s in sections" :key="s.code" class="rich-import__section">
          <div class="rich-import__head">
            <el-checkbox v-model="chosen[s.code]" :disabled="!merged[s.code]">{{ s.name }}</el-checkbox>
            <span v-if="!merged[s.code]" class="rich-import__muted">文档中无此章节</span>
            <span v-else class="rich-import__muted">{{ textLength(merged[s.code]) }} 字</span>
          </div>
          <div v-if="merged[s.code]" class="rich-import__preview" v-rich-html="merged[s.code]"></div>
        </div>
        <div v-if="result.unassigned" class="rich-import__section">
          <div class="rich-import__head">
            <b>未归入章节的内容</b>
            <span class="rich-import__muted">追加到</span>
            <el-select v-model="unassignedTarget" size="mini" clearable placeholder="不导入" style="width: 200px;">
              <el-option v-for="s in sections" :key="s.code" :label="s.name" :value="s.code"></el-option>
            </el-select>
          </div>
          <div class="rich-import__preview" v-rich-html="result.unassigned"></div>
        </div>
        <div class="rich-import__muted">勾选的章节将替换编辑器中的对应内容；导入后仍须保存。</div>
      </div>
    </div>
    <div slot="footer">
      <el-button size="small" @click="close">取消</el-button>
      <el-button size="small" type="primary" :disabled="!result || !anyChosen" @click="apply">填入编辑器</el-button>
    </div>
  </el-dialog>
</template>

<script>
import Swal from 'sweetalert2'
import { sectionsOf } from './rich-sections'

const MAX_BYTES = 20 * 1024 * 1024

export default {
  name: 'rich-import-dlg',
  props: {
    visible: { type: Boolean, default: false },
    bizType: { type: String, required: true }
  },
  data () {
    return {
      parsing: false,
      result: null,
      chosen: {},
      unassignedTarget: ''
    }
  },
  computed: {
    sections () {
      return sectionsOf(this.bizType)
    },
    merged () {
      const out = {}
      const src = (this.result && this.result.sections) || {}
      this.sections.forEach(s => { out[s.code] = src[s.code] || '' })
      if (this.result && this.result.unassigned && this.unassignedTarget) {
        out[this.unassignedTarget] = (out[this.unassignedTarget] || '') + this.result.unassigned
      }
      return out
    },
    anyChosen () {
      return this.sections.some(s => this.chosen[s.code] && this.merged[s.code])
    }
  },
  watch: {
    visible (val) {
      if (val) this.reset()
    },
    unassignedTarget (code) {
      if (code) this.$set(this.chosen, code, true)
    }
  },
  methods: {
    reset () {
      this.result = null
      this.chosen = {}
      this.unassignedTarget = ''
      if (this.$refs.file) this.$refs.file.value = ''
    },
    close () {
      this.$emit('update:visible', false)
    },
    textLength (html) {
      return Tool.richTextLength(html)
    },
    onPick (e) {
      const file = e.target.files && e.target.files[0]
      if (!file) return
      if (file.size > MAX_BYTES) {
        Toast.warning('文件超过 20MB', Swal)
        return
      }
      const form = new FormData()
      form.append('file', file)
      form.append('bizType', this.bizType)
      this.parsing = true
      this.result = null
      this.$ajax.post(process.env.VUE_APP_SERVER + '/file/rdms/rich-doc/word-parse', form)
        .then(response => {
          const resp = response.data
          if (!resp.success) {
            Toast.warning(resp.message, Swal)
            return
          }
          this.result = resp.content
          const chosen = {}
          this.sections.forEach(s => { chosen[s.code] = !!(resp.content.sections || {})[s.code] })
          this.chosen = chosen
        })
        .finally(() => { this.parsing = false })
    },
    apply () {
      const out = {}
      this.sections.forEach(s => {
        if (this.chosen[s.code] && this.merged[s.code]) out[s.field] = this.merged[s.code]
      })
      this.$emit('apply', out)
      this.close()
    }
  }
}
</script>

<style scoped>
.rich-import__pick { display: flex; align-items: center; gap: 10px; margin-bottom: 10px; }
.rich-import__hint, .rich-import__muted { color: #888; font-size: 12px; }
.rich-import__section { border: 1px solid #eee; border-radius: 4px; margin-bottom: 8px; }
.rich-import__head { display: flex; align-items: center; gap: 10px; padding: 4px 8px; background: #fafafa; border-bottom: 1px solid #eee; }
.rich-import__preview { max-height: 220px; overflow: auto; padding: 6px 10px; text-align: left; }
</style>
