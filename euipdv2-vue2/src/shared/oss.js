/**
 * 公共资源桶 euipd-web 中静态文件的访问地址。前缀取构建变量 VUE_APP_OSS_WEB_BASE，
 * 开发为 MinIO 直连地址，部署版为同源的 /euipd-web。
 */
export function webAssetUrl(name) {
  const base = (process.env.VUE_APP_OSS_WEB_BASE || '').replace(/\/$/, '');
  return base + '/' + encodeURIComponent(name);
}
