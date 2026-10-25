/**
 * 富文本图片节点：正文只保存 data-asset-id、width、data-align，不保存 src。
 * 粘贴、拖拽、工具栏三种入口统一走 startUpload；粘贴 HTML 中的 data: 图片先转为上传占位。
 */
import { Extension } from '@tiptap/core'
import Image from '@tiptap/extension-image'
import { Plugin, PluginKey } from '@tiptap/pm/state'
import { getCachedUrl, invalidateAssetUrl, resolveAssetUrls, uploadAsset } from './rich-asset-cache'
import { prepareImage } from './rich-image-prepare'

export const IMAGE_TYPES = ['image/png', 'image/jpeg', 'image/gif', 'image/webp']
export const IMAGE_MAX_BYTES = 10 * 1024 * 1024
const RAW_MAX_BYTES = 50 * 1024 * 1024
const MIN_WIDTH = 40
const UPLOAD_ATTR = 'data-rich-upload'
export const NO_IMAGE_MESSAGE = '此处不支持插入图片'

let uploadSeq = 0
let pendingUploads = 0
let pastedQueue = []
let pastedRejected = 0

/** 是否还有图片在上传；上传中保存会丢失占位处的图片。 */
export function hasPendingUploads () {
  return pendingUploads > 0
}

function findUploadPos (doc, uploadId) {
  let found = null
  doc.descendants((node, pos) => {
    if (found !== null) return false
    if (node.type.name === 'image' && node.attrs.uploadId === uploadId) {
      found = pos
      return false
    }
    return true
  })
  return found
}

function reporter (onError) {
  return typeof onError === 'function' ? onError : msg => console.warn(msg)
}

/** 超过 2MB 的静态图片上传前会压缩，因此原文件放宽到 50MB；GIF 不压缩，仍按 10MB。 */
function checkFile (file) {
  if (IMAGE_TYPES.indexOf(file.type) < 0) return '图片格式不支持，只接受 PNG、JPEG、GIF、WebP'
  if (file.type === 'image/gif' && file.size > IMAGE_MAX_BYTES) return 'GIF 图片超过 10MB'
  if (file.size > RAW_MAX_BYTES) return '图片超过 50MB，请先压缩'
  return null
}

function nextUploadId () {
  return 'u' + (++uploadSeq) + '_' + Date.now()
}

/** 文档中已有 uploadId 占位：压缩、上传后替换为正式图片；失败时移除占位并提示。 */
function startUpload (editor, uploadId, file, report) {
  pendingUploads++
  prepareImage(file).then(prepared => {
    if (prepared.size > IMAGE_MAX_BYTES) {
      throw new Error(prepared === file ? '图片超过 10MB' : '图片压缩后仍超过 10MB')
    }
    return uploadAsset(prepared)
  }).finally(() => { pendingUploads-- }).then(res => {
    if (editor.isDestroyed) return
    const p = findUploadPos(editor.state.doc, uploadId)
    if (p === null) return
    const node = editor.state.doc.nodeAt(p)
    editor.view.dispatch(editor.state.tr.setNodeMarkup(p, undefined, {
      ...node.attrs, uploadId: null, assetId: res.assetId, src: res.url
    }))
  }).catch(e => {
    if (!editor.isDestroyed) {
      const p = findUploadPos(editor.state.doc, uploadId)
      if (p !== null) {
        const node = editor.state.doc.nodeAt(p)
        editor.view.dispatch(editor.state.tr.delete(p, p + node.nodeSize))
      }
    }
    report((e && e.message) || '图片上传失败，请重试')
  })
}

/**
 * 在 pos 处插入上传占位，上传完成后替换为正式图片；失败时移除占位并调用 onError。
 */
export function uploadImageFiles (editor, files, pos, onError) {
  const report = reporter(onError)
  Array.from(files || []).forEach(file => {
    const problem = checkFile(file)
    if (problem) {
      report(problem)
      return
    }
    const uploadId = nextUploadId()
    const at = typeof pos === 'number' ? Math.min(pos, editor.state.doc.content.size) : editor.state.selection.from
    editor.chain().insertContentAt(at, { type: 'image', attrs: { uploadId } }).run()
    startUpload(editor, uploadId, file, report)
  })
}

function imageFiles (dataTransfer) {
  if (!dataTransfer || !dataTransfer.files) return []
  return Array.from(dataTransfer.files).filter(f => f.type && f.type.indexOf('image/') === 0)
}

function htmlOf (dataTransfer) {
  return (dataTransfer && dataTransfer.getData('text/html')) || ''
}

/**
 * Word、Excel 等复制时剪贴板同时带 HTML 与整段截图，此时应当粘贴文字；
 * 只有 HTML 不含文字（如网页中复制单张图片）时才上传剪贴板中的图片文件。
 */
function hasRichText (dataTransfer) {
  const html = htmlOf(dataTransfer)
  if (!html) return false
  const body = new window.DOMParser().parseFromString(html, 'text/html').body
  return !!(body && body.textContent.replace(/\s+/g, '').length)
}

function dataUrlToFile (src) {
  const m = /^data:(image\/[a-z0-9.+-]+);base64,([\s\S]*)$/i.exec(String(src).trim())
  if (!m) return null
  const type = m[1].toLowerCase() === 'image/jpg' ? 'image/jpeg' : m[1].toLowerCase()
  let bin
  try {
    bin = window.atob(m[2].replace(/\s+/g, ''))
  } catch (e) {
    return null
  }
  const bytes = new Uint8Array(bin.length)
  for (let i = 0; i < bin.length; i++) bytes[i] = bin.charCodeAt(i)
  return new window.File([bytes], 'pasted.' + type.split('/')[1], { type })
}

/**
 * 粘贴、拖入的 HTML 在解析前经过此处：data: 图片换成上传占位并排队，
 * 其他没有 data-asset-id 的图片（本地路径、外站地址）计数后移除。
 */
function transformPastedImages (html) {
  pastedQueue = []
  pastedRejected = 0
  if (!html || !/<img\b/i.test(html)) return html
  return html.replace(/<img\b[^>]*>/gi, tag => {
    const el = new window.DOMParser().parseFromString(tag, 'text/html').body.firstChild
    if (!el || el.nodeName !== 'IMG') return tag
    el.removeAttribute(UPLOAD_ATTR)
    if (el.getAttribute('data-asset-id')) return el.outerHTML
    const file = dataUrlToFile(el.getAttribute('src') || '')
    if (!file || checkFile(file)) {
      pastedRejected++
      return ''
    }
    const uploadId = nextUploadId()
    pastedQueue.push({ uploadId, file })
    return '<img ' + UPLOAD_ATTR + '="' + uploadId + '">'
  })
}

function takePasted () {
  const taken = { queue: pastedQueue, rejected: pastedRejected }
  pastedQueue = []
  pastedRejected = 0
  return taken
}

/** 粘贴内容已同步插入文档后调用：为占位启动上传，并提示被移除的图片。 */
function flushPasted (editor, report) {
  const { queue, rejected } = takePasted()
  if (rejected) report('粘贴内容中有 ' + rejected + ' 张图片无法读取，已移除；请另存为文件后插入')
  queue.forEach(item => startUpload(editor, item.uploadId, item.file, report))
}

/** 剪贴板或拖入内容：只有图片文件时上传文件并返回 true；否则交由默认粘贴处理。 */
function handleTransfer (editor, dataTransfer, event, pos, report) {
  const files = imageFiles(dataTransfer)
  if (files.length && !hasRichText(dataTransfer)) {
    takePasted()
    event.preventDefault()
    uploadImageFiles(editor, files, pos, report)
    return true
  }
  if (htmlOf(dataTransfer)) flushPasted(editor, report)
  else takePasted()
  return false
}

export const RichImage = Image.extend({
  draggable: true,

  addAttributes () {
    return {
      assetId: {
        default: null,
        parseHTML: el => el.getAttribute('data-asset-id'),
        renderHTML: attrs => (attrs.assetId ? { 'data-asset-id': attrs.assetId } : {})
      },
      src: {
        default: null,
        parseHTML: () => null,
        renderHTML: () => ({})
      },
      width: {
        default: null,
        parseHTML: el => el.getAttribute('width'),
        renderHTML: attrs => (attrs.width ? { width: attrs.width } : {})
      },
      align: {
        default: null,
        parseHTML: el => el.getAttribute('data-align'),
        renderHTML: attrs => (attrs.align ? { 'data-align': attrs.align } : {})
      },
      uploadId: {
        default: null,
        parseHTML: el => el.getAttribute(UPLOAD_ATTR),
        rendered: false
      }
    }
  },

  parseHTML () {
    return [{ tag: 'img[data-asset-id]' }, { tag: 'img[' + UPLOAD_ATTR + ']' }]
  },

  addNodeView () {
    return ({ node, getPos, editor }) => {
      let current = node
      const dom = document.createElement('div')
      dom.className = 'rich-image'
      const img = document.createElement('img')
      img.draggable = false
      const handle = document.createElement('span')
      handle.className = 'rich-image__handle'
      const placeholder = document.createElement('span')
      placeholder.className = 'rich-image__uploading'
      placeholder.textContent = '图片上传中…'
      dom.appendChild(img)
      dom.appendChild(handle)
      dom.appendChild(placeholder)

      let retried = false
      const applySrc = () => {
        const id = current.attrs.assetId
        if (!id) return
        const url = current.attrs.src || getCachedUrl(id)
        if (url) {
          if (img.getAttribute('src') !== url) img.setAttribute('src', url)
          return
        }
        resolveAssetUrls([id]).then(map => {
          if (map[id] && current.attrs.assetId === id) img.setAttribute('src', map[id])
        })
      }
      img.addEventListener('error', () => {
        const id = current.attrs.assetId
        if (!id || retried) return
        retried = true
        invalidateAssetUrl(id)
        resolveAssetUrls([id]).then(map => { if (map[id]) img.setAttribute('src', map[id]) })
      })

      const render = () => {
        const uploading = !!current.attrs.uploadId
        dom.classList.toggle('is-uploading', uploading)
        img.style.display = uploading ? 'none' : ''
        placeholder.style.display = uploading ? '' : 'none'
        handle.style.display = uploading || !editor.isEditable ? 'none' : ''
        if (current.attrs.width) img.setAttribute('width', current.attrs.width)
        else img.removeAttribute('width')
        if (current.attrs.align) dom.setAttribute('data-align', current.attrs.align)
        else dom.removeAttribute('data-align')
        applySrc()
      }

      handle.addEventListener('mousedown', ev => {
        ev.preventDefault()
        ev.stopPropagation()
        const startX = ev.clientX
        const startWidth = img.getBoundingClientRect().width
        const maxWidth = editor.view.dom.clientWidth || startWidth
        const onMove = e => {
          const w = Math.max(MIN_WIDTH, Math.min(maxWidth, startWidth + e.clientX - startX))
          img.style.width = Math.round(w) + 'px'
        }
        const onUp = () => {
          document.removeEventListener('mousemove', onMove)
          document.removeEventListener('mouseup', onUp)
          const w = Math.round(img.getBoundingClientRect().width)
          img.style.width = ''
          const pos = typeof getPos === 'function' ? getPos() : null
          if (pos === null || pos === undefined) return
          editor.view.dispatch(editor.state.tr.setNodeMarkup(pos, undefined, {
            ...current.attrs, width: w + 'px'
          }))
        }
        document.addEventListener('mousemove', onMove)
        document.addEventListener('mouseup', onUp)
      })

      render()
      return {
        dom,
        update (updated) {
          if (updated.type.name !== 'image') return false
          current = updated
          render()
          return true
        },
        selectNode () { dom.classList.add('is-selected') },
        deselectNode () { dom.classList.remove('is-selected') },
        ignoreMutation: () => true
      }
    }
  },

  addProseMirrorPlugins () {
    const editor = this.editor
    const report = reporter(this.options.onError)
    return [
      new Plugin({
        key: new PluginKey('richImageUpload'),
        props: {
          transformPastedHTML: transformPastedImages,
          handlePaste (view, event) {
            return handleTransfer(editor, event.clipboardData, event, view.state.selection.from, report)
          },
          handleDrop (view, event, slice, moved) {
            if (moved) {
              takePasted()
              return false
            }
            const coords = view.posAtCoords({ left: event.clientX, top: event.clientY })
            return handleTransfer(editor, event.dataTransfer, event, coords ? coords.pos : view.state.selection.from, report)
          }
        }
      })
    ]
  }
})

/**
 * 简版模式不注册图片节点：只有图片文件时拦截并提示；文字与图片混合时粘贴文字，图片被丢弃并提示。
 */
export const NoImageGuard = Extension.create({
  name: 'richNoImageGuard',

  addOptions () {
    return { onReject: null }
  },

  addProseMirrorPlugins () {
    const notify = reporter(this.options.onReject)
    const check = (dataTransfer, event) => {
      const files = imageFiles(dataTransfer)
      if (files.length && !hasRichText(dataTransfer)) {
        event.preventDefault()
        notify(NO_IMAGE_MESSAGE)
        return true
      }
      if (files.length || /<img\b/i.test(htmlOf(dataTransfer))) notify(NO_IMAGE_MESSAGE)
      return false
    }
    return [
      new Plugin({
        key: new PluginKey('richNoImageGuard'),
        props: {
          handlePaste: (view, event) => check(event.clipboardData, event),
          handleDrop: (view, event, slice, moved) => (moved ? false : check(event.dataTransfer, event))
        }
      })
    ]
  }
})

export default RichImage
