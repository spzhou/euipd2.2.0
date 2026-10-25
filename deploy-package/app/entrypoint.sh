#!/bin/sh
# 按 SERVICE 启动对应的服务。配置叠加顺序（后者覆盖前者）：
#   jar 内配置 → /app/config/common/application-prod.properties → /app/config/${SERVICE}/application-prod.properties
set -e

case "${SERVICE}" in
  gateway) PORT=18000 ;;
  system) PORT=18001 ;;
  file2) PORT=18003 ;;
  customer) PORT=18005 ;;
  ftp) PORT=18007 ;;
  *) echo "未知的 SERVICE：${SERVICE}" >&2; exit 1 ;;
esac

# 健康检查：端口有 HTTP 应答即视为就绪（未登录访问返回 401、404 也算）
if [ "$1" = "health" ]; then
  exec curl -s -o /dev/null --max-time 5 "http://127.0.0.1:${PORT}/"
fi

# 浏览器访问系统的地址：PUBLIC_URL 优先（HTTPS 或域名访问时填写），否则按 SERVER_HOST 与 HTTP_PORT 拼出
if [ -n "${PUBLIC_URL}" ]; then
  EUIPD_PUBLIC_URL="${PUBLIC_URL%/}"
elif [ -z "${SERVER_HOST}" ]; then
  echo "SERVER_HOST 未设置" >&2
  exit 1
elif [ "${HTTP_PORT:-80}" = "80" ]; then
  EUIPD_PUBLIC_URL="http://${SERVER_HOST}"
else
  EUIPD_PUBLIC_URL="http://${SERVER_HOST}:${HTTP_PORT}"
fi
export EUIPD_PUBLIC_URL

mkdir -p /app/logs
cd /app

# shellcheck disable=SC2086
exec java ${JAVA_OPTS} \
  -Duser.timezone="${TZ:-Asia/Shanghai}" \
  -Dfile.encoding=UTF-8 \
  -Djava.security.egd=file:/dev/./urandom \
  -jar "/app/jars/${SERVICE}.jar" \
  --spring.profiles.active=prod \
  --server.port="${PORT}" \
  --spring.config.additional-location="optional:file:/app/config/common/,optional:file:/app/config/${SERVICE}/"
