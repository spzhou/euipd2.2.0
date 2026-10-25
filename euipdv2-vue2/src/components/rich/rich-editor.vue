<!--
 * 富文本编辑组件（Tiptap 2）。
 * mode="full"：标题、格式、颜色、列表、对齐、链接、表格、图片；
 * mode="simple"：加粗、斜体、下划线、有序与无序列表、清除格式，不允许图片与表格。
 * v-model 输出 HTML；编辑器为空时输出空串。
-->
<template>
  <div class="rich-editor" :class="{ 'is-disabled': disabled, 'is-simple': mode === 'simple' }">
    <div v-if="editor && !disabled" class="rich-editor__toolbar">
      <template v-if="mode === 'full'">
        <button type="button" title="撤销" @click="run(c => c.undo())"><i class="fa fa-undo"></i></button>
        <button type="button" title="重做" @click="run(c => c.redo())"><i class="fa fa-repeat"></i></button>
        <span class="rich-editor__sep"></span>
        <button v-for="level in headingLevels" :key="'h' + level" type="button" :title="level + ' 级标题'"
                :class="{ active: editor.isActive('heading', { level }) }"
                @click="run(c => c.toggleHeading({ level }))">H{{ level }}</button>
        <span class="rich-editor__sep"></span>
      </template>
      <button type="button" title="加粗" :class="{ active: editor.isActive('bold') }"
              @click="run(c => c.toggleBold())"><i class="fa fa-bold"></i></button>
      <button type="button" title="斜体" :class="{ active: editor.isActive('italic') }"
              @click="run(c => c.toggleItalic())"><i class="fa fa-italic"></i></button>
      <button type="button" title="下划线" :class="{ active: editor.isActive('underline') }"
              @click="run(c => c.toggleUnderline())"><i class="fa fa-underline"></i></button>
      <template v-if="mode === 'full'">
        <button type="button" title="删除线" :class="{ active: editor.isActive('strike') }"
                @click="run(c => c.toggleStrike())"><i class="fa fa-strikethrough"></i></button>
        <label class="rich-editor__color" title="文字颜色">
          <i class="fa fa-font"></i>
          <input type="color" @input="run(c => c.setColor($event.target.value))">
        </label>
        <label class="rich-editor__color" title="背景高亮">
          <i class="fa fa-paint-brush"></i>
          <input type="color" value="#fff3a3" @input="run(c => c.setHighlight({ color: $event.target.value }))">
        </label>
      </template>
      <span class="rich-editor__sep"></span>
      <button type="button" title="有序列表" :class="{ active: editor.isActive('orderedList') }"
              @click="run(c => c.toggleOrderedList())"><i class="fa fa-list-ol"></i></button>
      <button type="button" title="无序列表" :class="{ active: editor.isActive('bulletList') }"
              @click="run(c => c.toggleBulletList())"><i class="fa fa-list-ul"></i></button>
      <template v-if="mode === 'full'">
        <button type="button" title="减少缩进（列表项）" :disabled="!editor.can().liftListItem('listItem')"
                @click="run(c => c.liftListItem('listItem'))"><i class="fa fa-outdent"></i></button>
        <button type="button" title="增加缩进（列表项）" :disabled="!editor.can().sinkListItem('listItem')"
                @click="run(c => c.sinkListItem('listItem'))"><i class="fa fa-indent"></i></button>
        <button type="button" title="引用" :class="{ active: editor.isActive('blockquote') }"
                @click="run(c => c.toggleBlockquote())"><i class="fa fa-quote-left"></i></button>
        <button type="button" title="代码块" :class="{ active: editor.isActive('codeBlock') }"
                @click="run(c => c.toggleCodeBlock())"><i class="fa fa-code"></i></button>
        <button type="button" title="分隔线" @click="run(c => c.setHorizontalRule())"><i class="fa fa-minus"></i></button>
        <span class="rich-editor__sep"></span>
        <button v-for="a in aligns" :key="a.value" type="button" :title="a.title"
                :class="{ active: editor.isActive({ textAlign: a.value }) }"
                @click="run(c => c.setTextAlign(a.value))"><i :class="'fa fa-align-' + a.value"></i></button>
        <span class="rich-editor__sep"></span>
        <button type="button" title="链接" :class="{ active: editor.isActive('link') }" @click="editLink">
          <i class="fa fa-link"></i></button>
        <button type="button" title="插入表格" @click="run(c => c.insertTable({ rows: 3, cols: 3, withHeaderRow: true }))">
          <i class="fa fa-table"></i></button>
        <template v-if="editor.isActive('table')">
          <button type="button" title="下方插入行" @click="run(c => c.addRowAfter())">+行</button>
          <button type="button" title="右侧插入列" @click="run(c => c.addColumnAfter())">+列</button>
          <button type="button" title="删除行" @click="run(c => c.deleteRow())">-行</button>
          <button type="button" title="删除列" @click="run(c => c.deleteColumn())">-列</button>
          <button type="button" title="合并所选单元格" :disabled="!editor.can().mergeCells()"
                  @click="run(c => c.mergeCells())">合并</button>
          <button type="button" title="拆分单元格" :disabled="!editor.can().splitCell()"
                  @click="run(c => c.splitCell())">拆分</button>
          <button type="button" title="删除表格" @click="run(c => c.deleteTable())">删表</button>
        </template>
        <button type="button" title="插入图片（也可直接粘贴或拖入）" @click="pickImage">
          <i class="fa fa-picture-o"></i></button>
        <template v-if="editor.isActive('image')">
          <button v-for="a in imageAligns" :key="'img-' + a.value" type="button" :title="'图片' + a.title"
                  :class="{ active: editor.getAttributes('image').align === a.value }"
                  @click="setImageAlign(a.value)"><i :class="'fa fa-align-' + a.value"></i></button>
        </template>
        <input ref="file" type="file" accept="image/png,image/jpeg,image/gif,image/webp" multiple
               style="display: none" @change="onPicked">
      </template>
      <span class="rich-editor__sep"></span>
      <button type="button" title="清除格式" @click="run(c => c.unsetAllMarks().clearNodes())">
        <i class="fa fa-eraser"></i></button>
    </div>
    <editor-content :editor="editor" class="rich-editor__content" :style="contentStyle"/>
    <div v-if="showCount" class="rich-editor__count" :class="{ over: count > maxLength || (count > 0 && count < minLength) }">
      {{ count }} / {{ maxLength }} 字
    </div>
  </div>
</template>

<script>
import { Editor, EditorContent } from '@tiptap/vue-2'
import StarterKit from '@tiptap/starter-kit'
import Underline from '@tiptap/extension-underline'
import TextStyle from '@tiptap/extension-text-style'
import Color from '@tiptap/extension-color'
import Highlight from '@tiptap/extension-highlight'
import TextAlign from '@tiptap/extension-text-align'
import Link from '@tiptap/extension-link'
import Placeholder from '@tiptap/extension-placeholder'
import Table from '@tiptap/extension-table'
import TableRow from '@tiptap/extension-table-row'
import TableHeader from '@tiptap/extension-table-header'
import TableCell from '@tiptap/extension-table-cell'
import { NoImageGuard, RichImage, uploadImageFiles } from './rich-image'
import { richTextLength } from '@/utils/rich-html'

// 一级标题留给章节标题；Quill 旧内容与粘贴内容中的 h1 降为 h2，避免被当作普通段落
const HEADING_LEVELS = [2, 3, 4]

function normalizeHeadings (html) {
  return html ? String(html).replace(/<(\/?)h1(?=[\s>/])/gi, '<$1h2') : ''
}

export default {
  name: 'rich-editor',
  components: { EditorContent },
  props: {
    value: { type: String, default: '' },
    mode: { type: String, default: 'full', validator: v => ['full', 'simple'].indexOf(v) >= 0 },
    placeholder: { type: String, default: '' },
    minLength: { type: Number, default: 0 },
    maxLength: { type: Number, default: 5000 },
    showCount: { type: Boolean, default: true },
    disabled: { type: Boolean, default: false },
    minHeight: { type: [Number, String], default: 120 }
  },
  data () {
    return {
      editor: null,
      lastEmitted: null,
      count: 0,
      headingLevels: HEADING_LEVELS,
      imageAligns: [
        { value: 'left', title: '左对齐' },
        { value: 'center', title: '居中' },
        { value: 'right', title: '右对齐' }
      ],
      aligns: [
        { value: 'left', title: '左对齐' },
        { value: 'center', title: '居中' },
        { value: 'right', title: '右对齐' },
        { value: 'justify', title: '两端对齐' }
      ]
    }
  },
  computed: {
    contentStyle () {
      const h = typeof this.minHeight === 'number' ? this.minHeight + 'px' : this.minHeight
      return { '--rich-min-height': h }
    }
  },
  watch: {
    value (val) {
      if (!this.editor || val === this.lastEmitted) return
      this.setContent(val)
    },
    disabled (val) {
      if (this.editor) this.editor.setEditable(!val)
    }
  },
  mounted () {
    this.editor = new Editor({
      extensions: this.buildExtensions(),
      content: normalizeHeadings(this.value),
      editable: !this.disabled,
      editorProps: { transformPastedHTML: normalizeHeadings },
      onUpdate: () => this.emitValue()
    })
    this.lastEmitted = this.value
    this.count = richTextLength(this.value)
  },
  beforeDestroy () {
    if (this.editor) this.editor.destroy()
  },
  methods: {
    buildExtensions () {
      const placeholder = Placeholder.configure({ placeholder: this.placeholder })
      if (this.mode === 'simple') {
        return [
          StarterKit.configure({
            heading: false, blockquote: false, codeBlock: false, code: false,
            horizontalRule: false, strike: false
          }),
          Underline,
          NoImageGuard.configure({ onReject: msg => this.warn(msg) }),
          placeholder
        ]
      }
      return [
        StarterKit.configure({ heading: { levels: HEADING_LEVELS } }),
        Underline,
        TextStyle,
        Color,
        Highlight.configure({ multicolor: true }),
        TextAlign.configure({ types: ['heading', 'paragraph'], alignments: ['left', 'center', 'right', 'justify'] }),
        Link.configure({ openOnClick: false, autolink: true, protocols: ['http', 'https', 'mailto'] }),
        Table.configure({ resizable: false }),
        TableRow,
        TableHeader,
        TableCell,
        RichImage.configure({ inline: false, allowBase64: false, onError: msg => this.warn(msg) }),
        placeholder
      ]
    },
    currentHtml () {
      return this.editor.isEmpty ? '' : this.editor.getHTML()
    },
    emitValue () {
      const html = this.currentHtml()
      this.count = richTextLength(html)
      this.lastEmitted = html
      this.$emit('input', html)
    },
    setContent (val) {
      this.editor.commands.setContent(normalizeHeadings(val), false)
      this.lastEmitted = val
      this.count = richTextLength(val)
    },
    run (fn) {
      if (!this.editor) return
      fn(this.editor.chain().focus()).run()
    },
    editLink () {
      const prev = this.editor.getAttributes('link').href || ''
      const url = window.prompt('链接地址（http、https 或 mailto），留空则取消链接', prev)
      if (url === null) return
      if (url.trim() === '') {
        this.run(c => c.extendMarkRange('link').unsetLink())
        return
      }
      if (!/^(https?:|mailto:)/i.test(url.trim())) {
        this.warn('链接只支持 http、https、mailto')
        return
      }
      this.run(c => c.extendMarkRange('link').setLink({ href: url.trim() }))
    },
    setImageAlign (align) {
      const current = this.editor.getAttributes('image').align
      this.run(c => c.updateAttributes('image', { align: current === align ? null : align }))
    },
    pickImage () {
      this.$refs.file.value = ''
      this.$refs.file.click()
    },
    onPicked (e) {
      uploadImageFiles(this.editor, e.target.files, this.editor.state.selection.from, msg => this.warn(msg))
    },
    warn (msg) {
      if (window.Toast && typeof window.Toast.warning === 'function') window.Toast.warning(msg)
      else window.alert(msg)
    }
  }
}
</script>

<style>
.rich-editor { width: 100%; box-sizing: border-box; border: 1px solid #d5d5d5; border-radius: 4px; background: #fff; text-align: left; }
.rich-editor.is-disabled { background: #f7f7f7; }
.rich-editor__toolbar { display: flex; flex-wrap: wrap; align-items: center; gap: 2px; padding: 4px 6px; border-bottom: 1px solid #e5e5e5; background: #fafafa; }
.rich-editor__toolbar button, .rich-editor__color { min-width: 28px; height: 26px; padding: 0 6px; border: 1px solid transparent; border-radius: 3px; background: transparent; color: #444; font-size: 13px; line-height: 24px; cursor: pointer; display: inline-flex; align-items: center; justify-content: center; position: relative; }
.rich-editor__toolbar button:hover, .rich-editor__color:hover { border-color: #ccc; background: #fff; }
.rich-editor__toolbar button.active { border-color: #6fb3e0; background: #e8f3fb; color: #2a6496; }
.rich-editor__toolbar button:disabled { opacity: 0.4; cursor: default; border-color: transparent; background: transparent; }
.rich-editor__color input { position: absolute; inset: 0; opacity: 0; cursor: pointer; width: 100%; height: 100%; }
.rich-editor__sep { width: 1px; height: 18px; margin: 0 4px; background: #ddd; }
.rich-editor__content .ProseMirror { min-height: var(--rich-min-height, 120px); padding: 8px 12px; outline: none; }
.rich-editor__content .ProseMirror p.is-editor-empty:first-child::before { content: attr(data-placeholder); color: #aaa; float: left; height: 0; pointer-events: none; }
.rich-editor__count { padding: 2px 8px; text-align: right; font-size: 12px; color: #999; border-top: 1px solid #f0f0f0; }
.rich-editor__count.over { color: #d15b47; }
.rich-image { position: relative; display: block; margin: 6px 0; line-height: 0; }
.rich-image[data-align="center"] { text-align: center; }
.rich-image[data-align="right"] { text-align: right; }
.rich-image img { max-width: 100%; height: auto; }
.rich-image.is-selected img { outline: 2px solid #6fb3e0; }
.rich-image__handle { position: absolute; width: 10px; height: 10px; background: #6fb3e0; border: 1px solid #fff; cursor: nwse-resize; display: none; }
.rich-image.is-selected .rich-image__handle { display: inline-block; margin-left: -10px; }
.rich-image__uploading { display: inline-block; padding: 18px 24px; line-height: 1.4; border: 1px dashed #bbb; color: #888; background: #fafafa; font-size: 12px; }
</style>
