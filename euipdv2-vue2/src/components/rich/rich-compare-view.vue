<!--
 * 两个版本的逐章节对比（实现方案第 8.10 节）：未变化章节折叠，变化章节并排显示两侧正文，
 * 并以 diff 按词标出纯文本增删。rows 为 get-compare 接口返回的章节列表。
-->
<template>
  <div class="rich-compare">
    <div v-if="changedRows.length === 0" class="rich-compare__empty">两个版本正文一致</div>
    <div v-for="s in changedRows" :key="s.code" class="rich-compare__section">
      <div class="rich-compare__name">{{ s.name }}</div>
      <div class="rich-compare__text">
        <span v-for="(p, i) in textDiff(s)" :key="i"
              :class="{ 'is-added': p.added, 'is-removed': p.removed }">{{ p.value }}</span>
      </div>
      <div class="rich-compare__sides">
        <div class="rich-compare__side">
          <div class="rich-compare__side-title">{{ fromLabel }}</div>
          <div v-rich-html="s.fromHtml || '（空）'"></div>
        </div>
        <div class="rich-compare__side">
          <div class="rich-compare__side-title">{{ toLabel }}</div>
          <div v-rich-html="s.toHtml || '（空）'"></div>
        </div>
      </div>
    </div>
    <div v-if="unchangedNames.length" class="rich-compare__unchanged">
      未变化章节：{{ unchangedNames.join('、') }}
    </div>
  </div>
</template>

<script>
import { diffWordsWithSpace } from 'diff'

function plainText (html) {
  if (!html) return ''
  const doc = new DOMParser().parseFromString(String(html), 'text/html')
  doc.querySelectorAll('script, style, template').forEach(n => n.remove())
  doc.querySelectorAll('p, li, h2, h3, h4, tr, br').forEach(n => n.append('\n'))
  return ((doc.body && doc.body.textContent) || '').replace(/\n{2,}/g, '\n').trim()
}

export default {
  name: 'rich-compare-view',
  props: {
    rows: { type: Array, default: () => [] },
    fromLabel: { type: String, default: '对比基准' },
    toLabel: { type: String, default: '对比版本' }
  },
  computed: {
    changedRows () {
      return this.rows.filter(s => s.changed)
    },
    unchangedNames () {
      return this.rows.filter(s => !s.changed).map(s => s.name)
    }
  },
  methods: {
    textDiff (s) {
      return diffWordsWithSpace(plainText(s.fromHtml), plainText(s.toHtml))
    }
  }
}
</script>

<style scoped>
.rich-compare { text-align: left; }
.rich-compare__empty, .rich-compare__unchanged { color: #999; padding: 6px 0; font-size: 12px; }
.rich-compare__section { margin-bottom: 10px; border: 1px solid #eee; border-radius: 4px; }
.rich-compare__name { padding: 4px 8px; background: #fafafa; border-bottom: 1px solid #eee; font-weight: 600; }
.rich-compare__text { padding: 6px 8px; white-space: pre-wrap; line-height: 1.7; border-bottom: 1px dashed #eee; }
.rich-compare__text .is-added { background: #e6ffed; color: #116329; text-decoration: underline; }
.rich-compare__text .is-removed { background: #ffeef0; color: #a40e26; text-decoration: line-through; }
.rich-compare__sides { display: flex; }
.rich-compare__side { flex: 1; padding: 6px 8px; min-width: 0; overflow-x: auto; }
.rich-compare__side + .rich-compare__side { border-left: 1px solid #eee; }
.rich-compare__side-title { color: #888; font-size: 12px; margin-bottom: 4px; }
</style>
