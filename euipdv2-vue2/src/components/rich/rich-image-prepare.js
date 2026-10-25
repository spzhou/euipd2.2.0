/**
 * 上传前在浏览器内处理图片：超过 2MB 或长边超过 4096 像素的静态图片缩放并重新编码，
 * 带旋转标记的 JPEG 按实际方向重绘（服务端去除 EXIF 后不再保留旋转标记）。
 * 动图（GIF、APNG、动画 WebP）不处理，以免只剩首帧。
 */
const COMPRESS_OVER_BYTES = 2 * 1024 * 1024
const MAX_EDGE = 4096
const MAX_PIXELS = 50000000
const JPEG_QUALITY = 0.85
const HEADER_BYTES = 64 * 1024

function readBytes (blob) {
  return new Promise((resolve, reject) => {
    const reader = new window.FileReader()
    reader.onload = () => resolve(new Uint8Array(reader.result))
    reader.onerror = () => reject(reader.error)
    reader.readAsArrayBuffer(blob)
  })
}

function ascii (bytes, offset, length) {
  let s = ''
  for (let i = offset; i < offset + length && i < bytes.length; i++) s += String.fromCharCode(bytes[i])
  return s
}

function containsAscii (bytes, text) {
  const first = text.charCodeAt(0)
  for (let i = 0; i + text.length <= bytes.length; i++) {
    if (bytes[i] === first && ascii(bytes, i, text.length) === text) return true
  }
  return false
}

function isAnimated (header, type) {
  if (type === 'image/gif') return true
  if (type === 'image/png') return containsAscii(header, 'acTL')
  if (type === 'image/webp') return containsAscii(header, 'ANIM')
  return false
}

/** 读取 JPEG 的 EXIF 方向（1 为正常）；读不到时返回 1。 */
function jpegOrientation (b) {
  if (b[0] !== 0xFF || b[1] !== 0xD8) return 1
  let p = 2
  while (p + 4 <= b.length && b[p] === 0xFF) {
    const marker = b[p + 1]
    const len = (b[p + 2] << 8) | b[p + 3]
    if (marker === 0xDA) return 1
    if (marker === 0xE1 && ascii(b, p + 4, 4) === 'Exif') {
      const t = p + 10
      const le = b[t] === 0x49
      const u16 = o => (le ? b[o] | (b[o + 1] << 8) : (b[o] << 8) | b[o + 1])
      const u32 = o => (le
        ? (b[o] | (b[o + 1] << 8) | (b[o + 2] << 16) | (b[o + 3] << 24)) >>> 0
        : ((b[o] << 24) | (b[o + 1] << 16) | (b[o + 2] << 8) | b[o + 3]) >>> 0)
      const ifd = t + u32(t + 4)
      if (ifd + 2 > b.length) return 1
      const count = u16(ifd)
      for (let i = 0; i < count; i++) {
        const e = ifd + 2 + i * 12
        if (e + 10 > b.length) return 1
        if (u16(e) === 0x0112) return u16(e + 8) || 1
      }
      return 1
    }
    p += 2 + len
  }
  return 1
}

function loadImage (blob) {
  return new Promise((resolve, reject) => {
    const url = URL.createObjectURL(blob)
    const img = new window.Image()
    img.onload = () => { URL.revokeObjectURL(url); resolve(img) }
    img.onerror = () => { URL.revokeObjectURL(url); reject(new Error('decode failed')) }
    img.src = url
  })
}

function hasTransparency (ctx, w, h) {
  try {
    const data = ctx.getImageData(0, 0, w, h).data
    for (let i = 3; i < data.length; i += 4) {
      if (data[i] < 255) return true
    }
    return false
  } catch (e) {
    return true
  }
}

function renameExt (name, type) {
  const ext = type === 'image/png' ? 'png' : 'jpg'
  const base = String(name || 'image').replace(/\.[^.]*$/, '')
  return base + '.' + ext
}

/**
 * 返回可直接上传的文件；无需处理或浏览器无法解码时原样返回，交由服务端校验。
 * 像素超过 5000 万时抛出 Error，message 可直接提示。
 */
export async function prepareImage (file) {
  let header
  try {
    header = await readBytes(file.slice(0, HEADER_BYTES))
  } catch (e) {
    return file
  }
  if (isAnimated(header, file.type)) return file
  const rotated = file.type === 'image/jpeg' && jpegOrientation(header) > 1
  let img
  try {
    img = await loadImage(file)
  } catch (e) {
    return file
  }
  const w = img.naturalWidth
  const h = img.naturalHeight
  if (w * h > MAX_PIXELS) throw new Error('图片像素过多，不得超过 5000 万像素')
  const longEdge = Math.max(w, h)
  if (file.size <= COMPRESS_OVER_BYTES && longEdge <= MAX_EDGE && !rotated) return file

  const ratio = Math.min(1, MAX_EDGE / longEdge)
  const cw = Math.max(1, Math.round(w * ratio))
  const ch = Math.max(1, Math.round(h * ratio))
  const canvas = document.createElement('canvas')
  canvas.width = cw
  canvas.height = ch
  let type
  let blob
  try {
    const ctx = canvas.getContext('2d')
    if (!ctx) return file
    ctx.imageSmoothingEnabled = true
    ctx.imageSmoothingQuality = 'high'
    ctx.drawImage(img, 0, 0, cw, ch)
    type = file.type === 'image/jpeg' || !hasTransparency(ctx, cw, ch) ? 'image/jpeg' : 'image/png'
    blob = await new Promise(resolve => canvas.toBlob(resolve, type, JPEG_QUALITY))
  } finally {
    // 大图画布占用的显存不随引用释放而立即回收，主动清零
    canvas.width = 0
    canvas.height = 0
  }
  if (!blob) return file
  if (blob.size >= file.size && ratio === 1 && !rotated) return file
  return new window.File([blob], renameExt(file.name, type), { type })
}
