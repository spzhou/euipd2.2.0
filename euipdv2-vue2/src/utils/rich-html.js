/**
 * 富文本显示过滤。白名单与后端 RichHtmlSanitizer 保持一致；
 * 迁移的历史内容由 Quill 生成，保留 ql- 前缀类名、h1、sub、sup。
 */
import DOMPurify from 'dompurify'
import { getCachedUrl, invalidateAssetUrl, resolveAssetUrls } from '@/components/rich/rich-asset-cache'
import './rich-html.css'

const ALLOWED_TAGS = [
  'p', 'h1', 'h2', 'h3', 'h4', 'ul', 'ol', 'li', 'blockquote', 'pre', 'code', 'hr', 'br',
  'strong', 'em', 'u', 's', 'span', 'mark', 'a', 'sub', 'sup',
  'table', 'thead', 'tbody', 'tr', 'th', 'td', 'img'
]
const ALLOWED_ATTR = ['class', 'style', 'href', 'target', 'rel', 'colspan', 'rowspan',
  'data-asset-id', 'width', 'data-align']

const ASSET_ID = /^[A-Za-z0-9]{1,32}$/
const WIDTH = /^\d{1,4}(px|%)?$/
const COLOR = /^(#[0-9a-fA-F]{3,8}|rgba?\(\s*[\d.]+%?\s*,\s*[\d.]+%?\s*,\s*[\d.]+%?\s*(,\s*[\d.]+\s*)?\)|[a-zA-Z]{3,20})$/
const TEXT_ALIGN = ['left', 'right', 'center', 'justify']
const IMG_ALIGN = ['left', 'right', 'center']

function filterClass (value) {
  return value.split(/\s+/)
    .filter(name => /^ql-[A-Za-z0-9_-]{1,37}$/.test(name))
    .join(' ')
}

function filterStyle (value) {
  return value.split(';').map(decl => {
    const idx = decl.indexOf(':')
    if (idx <= 0) return ''
    const name = decl.slice(0, idx).trim().toLowerCase()
    const v = decl.slice(idx + 1).trim()
    if ((name === 'color' || name === 'background-color') && COLOR.test(v)) return name + ': ' + v + ';'
    if (name === 'text-align' && TEXT_ALIGN.indexOf(v.toLowerCase()) >= 0) return name + ': ' + v + ';'
    return ''
  }).join('')
}

let hooksInstalled = false

function installHooks () {
  if (hooksInstalled) return
  hooksInstalled = true
  DOMPurify.addHook('uponSanitizeAttribute', (node, data) => {
    if (data.attrName === 'class') {
      data.attrValue = filterClass(data.attrValue)
      data.keepAttr = data.attrValue.length > 0
    } else if (data.attrName === 'style') {
      data.attrValue = filterStyle(data.attrValue)
      data.keepAttr = data.attrValue.length > 0
    } else if (data.attrName === 'width' && node.nodeName !== 'IMG') {
      data.keepAttr = false
    } else if ((data.attrName === 'colspan' || data.attrName === 'rowspan') &&
      node.nodeName !== 'TD' && node.nodeName !== 'TH') {
      data.keepAttr = false
    }
  })
  DOMPurify.addHook('afterSanitizeAttributes', node => {
    if (node.nodeName === 'A' && node.hasAttribute('href')) {
      node.setAttribute('rel', 'noopener noreferrer')
      node.setAttribute('target', '_blank')
    }
    if (node.nodeName === 'IMG') {
      if (node.hasAttribute('width') && !WIDTH.test(node.getAttribute('width'))) node.removeAttribute('width')
      if (node.hasAttribute('data-align') && IMG_ALIGN.indexOf(node.getAttribute('data-align')) < 0) {
        node.removeAttribute('data-align')
      }
    }
  })
}

// ALLOWED_URI_REGEXP 会作用于不在 URI 安全清单内的全部属性，非链接属性必须列入 ADD_URI_SAFE_ATTR
const PURIFY_CONFIG = {
  ALLOWED_TAGS,
  ALLOWED_ATTR,
  ADD_URI_SAFE_ATTR: ['data-asset-id', 'width', 'data-align', 'colspan', 'rowspan', 'target', 'rel'],
  ALLOW_DATA_ATTR: false,
  ALLOWED_URI_REGEXP: /^(?:https?:|mailto:)/i
}

const HTML_ESCAPES = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }

/** 不含标签的纯文本按原换行分行显示。 */
function plainTextToHtml (text) {
  return text.replace(/[&<>"']/g, ch => HTML_ESCAPES[ch]).replace(/\r?\n/g, '<br>')
}

export function sanitizeRichHtml (value) {
  if (value === null || value === undefined || value === false) return ''
  const source = String(value)
  if (source.indexOf('<') < 0 && source.indexOf('\n') >= 0) return plainTextToHtml(source)
  installHooks()
  const clean = DOMPurify.sanitize(source, PURIFY_CONFIG)
  if (clean.indexOf('<img') < 0) return clean
  const div = document.createElement('div')
  div.innerHTML = clean
  div.querySelectorAll('img').forEach(img => {
    if (!ASSET_ID.test(img.getAttribute('data-asset-id') || '')) img.parentNode.removeChild(img)
  })
  return div.innerHTML
}

/** 纯文本字数：去除标签，空白不计，图片不计；按码点计数，与后端一致。 */
export function richTextLength (html) {
  if (!html) return 0
  const doc = new DOMParser().parseFromString(String(html), 'text/html')
  doc.querySelectorAll('script, style, template').forEach(n => n.remove())
  const text = (doc.body && doc.body.textContent) || ''
  return Array.from(text.replace(/[\s\u00a0\u3000]/g, '')).length
}

function setSrc (img, url) {
  if (url && img.getAttribute('src') !== url) img.setAttribute('src', url)
}

/** 经签名地址缓存回填 img[data-asset-id] 的 src；加载失败时换取一次新地址。 */
function fillImages (el) {
  const imgs = Array.prototype.slice.call(el.querySelectorAll('img[data-asset-id]'))
  if (!imgs.length) return
  const missing = []
  imgs.forEach(img => {
    const id = img.getAttribute('data-asset-id')
    img.addEventListener('error', () => {
      if (img.getAttribute('data-retried')) return
      img.setAttribute('data-retried', '1')
      invalidateAssetUrl(id)
      resolveAssetUrls([id]).then(map => setSrc(img, map[id]))
    })
    const url = getCachedUrl(id)
    if (url) setSrc(img, url)
    else missing.push(id)
  })
  if (!missing.length) return
  resolveAssetUrls(missing).then(map => {
    imgs.forEach(img => {
      if (!img.getAttribute('src')) setSrc(img, map[img.getAttribute('data-asset-id')])
    })
  })
}

function render (el, value) {
  el.classList.add('rich-html')
  el.innerHTML = sanitizeRichHtml(value)
  fillImages(el)
}

/** v-rich-html：替代 v-html，输出前经白名单过滤，图片地址由前端回填。 */
export const richHtmlDirective = {
  bind (el, binding) {
    render(el, binding.value)
  },
  update (el, binding) {
    if (binding.value !== binding.oldValue) render(el, binding.value)
  }
}

export default {
  sanitize: sanitizeRichHtml,
  textLength: richTextLength
}
