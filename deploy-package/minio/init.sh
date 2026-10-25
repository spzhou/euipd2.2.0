#!/bin/sh
# 建业务桶并创建应用专用账号；重复执行不改变已有数据
set -e

mc alias set local http://minio:9000 "${MINIO_ROOT_USER}" "${MINIO_ROOT_PASSWORD}" >/dev/null

mc mb --ignore-existing local/euipd-doc
mc mb --ignore-existing local/euipd-web
# euipd-doc 保存业务文件，只能经预签名地址访问；euipd-web 存放公开静态资源，允许匿名下载
mc anonymous set none local/euipd-doc
mc anonymous set download local/euipd-web

# 账号已存在时只更新口令，与 .env 保持一致
mc admin user add local "${EUIPD_OSS_ACCESS_KEY_ID}" "${EUIPD_OSS_ACCESS_KEY_SECRET}" >/dev/null
# mc 镜像不含 grep，用 case 判断是否已授予 readwrite
case "$(mc admin user info local "${EUIPD_OSS_ACCESS_KEY_ID}")" in
  *readwrite*) ;;
  *) mc admin policy attach local readwrite --user "${EUIPD_OSS_ACCESS_KEY_ID}" >/dev/null ;;
esac

# 公共静态资源（登录页图片等），以部署包为准覆盖同名对象
if [ -d /web-assets ] && [ -n "$(ls -A /web-assets 2>/dev/null)" ]; then
  mc cp --recursive --quiet /web-assets/ local/euipd-web/ >/dev/null
  echo "已同步公共资源：$(ls -A /web-assets | tr '\n' ' ')"
fi

echo "MinIO 就绪：桶 euipd-doc、euipd-web，应用账号 ${EUIPD_OSS_ACCESS_KEY_ID}"
