/**
 * 富文本图片签名地址缓存：同一轮渲染内的请求合并为一次批量换取，地址到期前一分钟视为失效。
 */
import axios from 'axios'

const SIGNED_URL_API = '/file/rdms/rich-asset/signed-url'
const UPLOAD_API = '/file/rdms/rich-asset/upload'
const MAX_BATCH = 200
const EXPIRE_MARGIN_MS = 60 * 1000
const FLUSH_DELAY_MS = 20

const cache = new Map()
const pending = new Map()
let batch = null

function server () {
  return process.env.VUE_APP_SERVER || ''
}

export function putAssetUrl (assetId, url, expiresInSec) {
  if (!assetId || !url) return
  const ttl = (Number(expiresInSec) > 0 ? Number(expiresInSec) : 3600) * 1000
  cache.set(assetId, { url, expireAt: Date.now() + ttl - EXPIRE_MARGIN_MS })
}

export function getCachedUrl (assetId) {
  const hit = cache.get(assetId)
  if (!hit) return null
  if (hit.expireAt <= Date.now()) {
    cache.delete(assetId)
    return null
  }
  return hit.url
}

export function invalidateAssetUrl (assetId) {
  cache.delete(assetId)
}

export function clearAssetCache () {
  cache.clear()
  pending.clear()
}

async function flush (current) {
  const ids = Array.from(current.ids)
  for (let i = 0; i < ids.length; i += MAX_BATCH) {
    const chunk = ids.slice(i, i + MAX_BATCH)
    try {
      const resp = await axios.post(server() + SIGNED_URL_API, { assetIds: chunk })
      const data = resp && resp.data
      if (data && data.success !== false && data.content && data.content.urls) {
        const urls = data.content.urls
        Object.keys(urls).forEach(id => putAssetUrl(id, urls[id], data.content.expiresIn))
      }
    } catch (e) {
      console.warn('rich asset signed-url failed', e)
    }
  }
  ids.forEach(id => pending.delete(id))
  current.resolve()
}

/**
 * 返回 { assetId: url }；无权限或失败的图片不出现在结果中。
 */
export function resolveAssetUrls (assetIds) {
  const ids = Array.from(new Set((assetIds || []).filter(Boolean)))
  const waits = []
  ids.forEach(id => {
    if (getCachedUrl(id)) return
    if (pending.has(id)) {
      waits.push(pending.get(id))
      return
    }
    if (!batch) {
      let resolve
      const promise = new Promise(r => { resolve = r })
      batch = { ids: new Set(), promise, resolve }
      const current = batch
      setTimeout(() => {
        batch = null
        flush(current)
      }, FLUSH_DELAY_MS)
    }
    batch.ids.add(id)
    pending.set(id, batch.promise)
    waits.push(batch.promise)
  })
  return Promise.all(waits).then(() => {
    const result = {}
    ids.forEach(id => {
      const url = getCachedUrl(id)
      if (url) result[id] = url
    })
    return result
  })
}

/**
 * 上传一张图片，成功后写入缓存并返回 { assetId, url, width, height }。
 * 失败时抛出 Error，message 为可直接提示的文字。
 */
export async function uploadAsset (file) {
  const formData = new window.FormData()
  formData.append('file', file)
  const resp = await axios.post(server() + UPLOAD_API, formData, { timeout: 60000 })
  const data = resp && resp.data
  if (!data || data.success === false || !data.content || !data.content.assetId) {
    throw new Error((data && data.message) || '图片上传失败，请重试')
  }
  const c = data.content
  putAssetUrl(c.assetId, c.url, c.expiresIn)
  return { assetId: c.assetId, url: c.url, width: c.width, height: c.height }
}
