#!/usr/bin/env bash
# =====================================================================
# EUIPD 研发过程管理系统 运维脚本
#
#   ./euipd.sh init               首次安装：检查环境与构建产物、生成口令与密钥、构建镜像
#   ./euipd.sh up                 启动全部服务并等待就绪（首次启动时初始化数据库与超级管理员）
#   ./euipd.sh down               停止并移除容器（数据保留）
#   ./euipd.sh restart [服务...]   重启全部或指定服务
#   ./euipd.sh status             查看容器状态并做连通性检查
#   ./euipd.sh logs [服务]         跟踪日志，例如 ./euipd.sh logs customer
#   ./euipd.sh backup             备份数据库、对象存储、配置与密钥
#   ./euipd.sh restore <备份目录>  从备份恢复（覆盖现有数据）
#   ./euipd.sh upgrade            替换 app/jars 与 web/dist 后重建镜像并启动
#   ./euipd.sh mysql [参数...]     以 root 进入数据库命令行，可重定向执行脚本：./euipd.sh mysql < x.sql
#   ./euipd.sh unlock <手机号>     解除该手机号的登录锁定并清零失败计数
# =====================================================================
set -euo pipefail

cd "$(dirname "$(readlink -f "$0" 2>/dev/null || echo "$0")")"
ROOT="$(pwd)"
APP_SERVICES=(gateway system customer file2 ftp)

info()  { printf '\033[32m[EUIPD]\033[0m %s\n' "$*"; }
warn()  { printf '\033[33m[EUIPD]\033[0m %s\n' "$*" >&2; }
fail()  { printf '\033[31m[EUIPD]\033[0m %s\n' "$*" >&2; exit 1; }

compose() { docker compose --env-file "$ROOT/.env" -f "$ROOT/docker-compose.yml" "$@"; }

# 在 mysql 容器内以 root 执行命令；口令取容器自身环境变量，不出现在宿主进程参数中
mysql_exec() { compose exec -T mysql sh -c 'export MYSQL_PWD="$MYSQL_ROOT_PASSWORD"; exec "$@"' sh "$@"; }

# 逐行读取 .env，不经 shell 求值，口令中的特殊字符不会被执行
read_env_file() {
  local file="$1" line key value
  while IFS= read -r line || [ -n "$line" ]; do
    line="${line%$'\r'}"
    [[ "$line" =~ ^[[:space:]]*(#|$) ]] && continue
    [[ "$line" == *=* ]] || continue
    key="${line%%=*}"
    value="${line#*=}"
    [[ "$key" =~ ^[A-Za-z_][A-Za-z0-9_]*$ ]] || continue
    if [[ "$value" =~ ^\"(.*)\"$ ]] || [[ "$value" =~ ^\'(.*)\'$ ]]; then value="${BASH_REMATCH[1]}"; fi
    export "$key=$value"
  done <"$file"
}

load_env() {
  [ -f .env ] || fail "缺少 .env：请先执行 cp .env.example .env 并按注释填写"
  read_env_file .env
  DATA_DIR="${DATA_DIR:-./data}"
  HTTP_PORT="${HTTP_PORT:-80}"
  PUBLIC_URL_RAW="${PUBLIC_URL:-}"
  if [ -n "${PUBLIC_URL:-}" ]; then
    PUBLIC_URL="${PUBLIC_URL%/}"
  elif [ "$HTTP_PORT" = "80" ]; then
    PUBLIC_URL="http://${SERVER_HOST:-}"
  else
    PUBLIC_URL="http://${SERVER_HOST:-}:${HTTP_PORT}"
  fi
  # 本机检查经 nginx 端口直连，不依赖外部 HTTPS 代理
  if [ "$HTTP_PORT" = "80" ]; then LOCAL_URL="http://127.0.0.1"; else LOCAL_URL="http://127.0.0.1:${HTTP_PORT}"; fi
}

# 读写 .env 中的单个变量（只替换“键=”开头的行）
env_get() { grep -E "^$1=" .env | tail -n 1 | cut -d= -f2- || true; }
env_set() {
  local key="$1" value="$2"
  if grep -qE "^${key}=" .env; then
    local tmp; tmp="$(mktemp)"
    awk -v k="$key" -v v="$value" 'BEGIN{FS=OFS="="} $1==k{print k"="v; next} {print}' .env >"$tmp"
    cat "$tmp" >.env && rm -f "$tmp"
  else
    printf '%s=%s\n' "$key" "$value" >>.env
  fi
}
random_secret() { LC_ALL=C tr -dc 'A-Za-z0-9' </dev/urandom | head -c "${1:-24}" || true; }

# 系统版本取 app/VERSION（由 scripts/build-artifacts.sh 生成）
app_version() { sed -n 's/^version=//p' app/VERSION 2>/dev/null | head -n 1 | grep . || echo unknown; }

# 拉取镜像；失败且 .env 设有 REGISTRY_MIRROR 时，改以带加速前缀的名称拉取再打回原名
pull_image() {
  local image="$1" mirrored
  docker image inspect "$image" >/dev/null 2>&1 && return 0
  info "拉取镜像 ${image}"
  docker pull --quiet "$image" >/dev/null 2>&1 && return 0
  [ -n "${REGISTRY_MIRROR:-}" ] || return 1
  local first="${image%%/*}"
  if [[ "$image" == */* ]] && [[ "$first" == *.* || "$first" == *:* ]]; then return 1; fi
  if [[ "$image" == */* ]]; then mirrored="${REGISTRY_MIRROR}/${image}"; else mirrored="${REGISTRY_MIRROR}/library/${image}"; fi
  warn "直接拉取失败，改从 ${mirrored} 拉取"
  docker pull --quiet "$mirrored" >/dev/null && docker image tag "$mirrored" "$image"
}

# 构建前先用 docker pull 取基础镜像：构建解析 FROM 时不会依次换用 registry-mirrors 中的下一个地址
ensure_base_image() {
  pull_image "$1" || fail "拉取基础镜像 $1 失败：请按部署说明书配置镜像加速（registry-mirrors 或 .env 的 REGISTRY_MIRROR）后重试"
}

# 以 app/jars 构建 euipd/app:current，并另打版本号标签；.env 中 EUIPD_VERSION 可指定启用任一已存版本
build_app_image() {
  local ver; ver="$(app_version)"
  ensure_base_image "${JAVA_IMAGE:-eclipse-temurin:11-jre}"
  info "构建应用镜像（app/jars → euipd/app:current，系统版本 ${ver}）"
  EUIPD_VERSION=current compose build gateway
  [ "$ver" = "unknown" ] || docker image tag euipd/app:current "euipd/app:${ver}"
  if [ -n "${EUIPD_VERSION:-}" ] && [ "${EUIPD_VERSION}" != "current" ]; then
    warn ".env 中 EUIPD_VERSION=${EUIPD_VERSION}，启动的是该版本镜像而不是刚构建的 current"
  fi
}

collabora_enabled() { [[ ",${COMPOSE_PROFILES:-}," == *",collabora,"* ]]; }

check_prerequisites() {
  command -v docker >/dev/null || fail "未安装 Docker，请先按部署说明书安装 Docker Engine"
  docker compose version >/dev/null 2>&1 || fail "未安装 Docker Compose 插件（docker compose）"
  docker info >/dev/null 2>&1 || fail "无法连接 Docker 服务：请确认已启动且当前用户有权限（root 或 docker 组）"
  command -v openssl >/dev/null || fail "未找到 openssl，请先安装"
  command -v curl >/dev/null || fail "未找到 curl，请先安装"
}

# 构建产物由 scripts/build-artifacts.sh 生成，缺任何一项都无法部署
check_artifacts() {
  local missing=() m
  for m in "${APP_SERVICES[@]}"; do [ -f "app/jars/${m}.jar" ] || missing+=("app/jars/${m}.jar"); done
  [ -f web/dist/index.html ] || missing+=("web/dist/index.html")
  [ -f mysql/db/euipd-schema.sql ] || missing+=("mysql/db/euipd-schema.sql")
  [ -f mysql/db/init-super-admin.sql ] || missing+=("mysql/db/init-super-admin.sql")
  [ -f mysql/my.cnf ] || missing+=("mysql/my.cnf")
  [ -d minio/web-assets ] || missing+=("minio/web-assets/")
  if collabora_enabled; then [ -f collabora/Dockerfile ] || missing+=("collabora/Dockerfile"); fi
  [ "${#missing[@]}" -eq 0 ] || fail "缺少构建产物：${missing[*]}。请先在源码仓库中执行 deploy-package/scripts/build-artifacts.sh"
}

validate_env() {
  [ -n "${SERVER_HOST:-}" ] || fail ".env 中 SERVER_HOST 未填写"
  case "$SERVER_HOST" in
    127.*|localhost|0.0.0.0) fail "SERVER_HOST 须为浏览器可访问的服务器地址，不能是 ${SERVER_HOST}" ;;
  esac
  [[ "$HTTP_PORT" =~ ^[0-9]+$ ]] || fail "HTTP_PORT 须为数字"
  if [ -n "${PUBLIC_URL_RAW:-}" ] && ! [[ "$PUBLIC_URL_RAW" =~ ^https?:// ]]; then fail "PUBLIC_URL 须以 http:// 或 https:// 开头"; fi
  [[ "${DOCKER_SUBNET_PREFIX:-172.30.0}" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]] || fail "DOCKER_SUBNET_PREFIX 须形如 172.30.0"
  local k
  for k in MYSQL_ROOT_PASSWORD EUIPD_DB_PASSWORD EUIPD_REDIS_PASSWORD MINIO_ROOT_USER MINIO_ROOT_PASSWORD \
           EUIPD_OSS_ACCESS_KEY_ID EUIPD_OSS_ACCESS_KEY_SECRET; do
    [ -n "${!k:-}" ] || fail ".env 中 ${k} 为空，请先执行 ./euipd.sh init"
  done
  [ "${#MINIO_ROOT_PASSWORD}" -ge 8 ] || fail "MINIO_ROOT_PASSWORD 至少 8 位"
  [ "${#EUIPD_OSS_ACCESS_KEY_SECRET}" -ge 8 ] || fail "EUIPD_OSS_ACCESS_KEY_SECRET 至少 8 位"
  local ip="${EUIPD_INITIAL_PASSWORD:-}"
  [ -n "$ip" ] || fail ".env 中 EUIPD_INITIAL_PASSWORD（成员初始口令）未填写"
  [ "${#ip}" -ge 8 ] && [ "${#ip}" -le 32 ] && [[ "$ip" =~ [A-Za-z] ]] && [[ "$ip" =~ [0-9] ]] \
    || fail "EUIPD_INITIAL_PASSWORD 须 8 至 32 位并同时含字母与数字"
  [ -f keys/jwt-private.pem ] && [ -f keys/jwt-public.pem ] || fail "缺少令牌密钥 keys/jwt-*.pem，请先执行 ./euipd.sh init"
}

# 数据库尚未初始化时，超级管理员参数必须合法
validate_super_admin() {
  [[ "${SUPER_ADMIN_PHONE:-}" =~ ^[0-9]{11}$ ]] || fail "SUPER_ADMIN_PHONE 须为 11 位手机号"
  local pwd="${SUPER_ADMIN_PASSWORD:-}" name="${SUPER_ADMIN_NAME:-平台管理员}"
  [ -n "$pwd" ] || fail ".env 中 SUPER_ADMIN_PASSWORD（超级管理员初始口令）未填写"
  [ "${#pwd}" -ge 8 ] && [ "${#pwd}" -le 32 ] || fail "SUPER_ADMIN_PASSWORD 长度须为 8 至 32 位"
  [[ "$pwd" =~ [A-Za-z] ]] && [[ "$pwd" =~ [0-9] ]] || fail "SUPER_ADMIN_PASSWORD 须同时包含字母与数字"
  [ "${#name}" -le 20 ] || fail "SUPER_ADMIN_NAME 不得超过 20 个字符"
  case "${name}${pwd}" in
    *\'*|*\"*|*\\*|*\`*|*\$*) fail "SUPER_ADMIN_NAME 与 SUPER_ADMIN_PASSWORD 不得含引号、反斜杠、反引号或 \$" ;;
  esac
}

database_initialized() { [ -d "${DATA_DIR}/mysql/mycourse_bk" ]; }

prepare_dirs() {
  mkdir -p "${DATA_DIR}"/{mysql,redis,minio} "${DATA_DIR}"/logs/nginx backups
  chmod 755 mysql/init/01-init-db.sh minio/init.sh app/entrypoint.sh
  chmod 644 mysql/my.cnf
  # MinIO 容器以 uid 1001 运行
  if [ "$(id -u)" = "0" ]; then
    chown -R 1001:1001 "${DATA_DIR}/minio"
  elif [ "$(uname -s)" = "Linux" ] && [ "$(stat -c %u "${DATA_DIR}/minio")" != "1001" ]; then
    warn "请以 root 执行，或手工执行：sudo chown -R 1001:1001 ${DATA_DIR}/minio"
  fi
}

# 可选的离线镜像：images/ 下的 *.tar 或 *.tar.gz 在本机缺少对应镜像时导入
load_offline_images() {
  local f
  for f in images/*.tar images/*.tar.gz; do
    [ -f "$f" ] || continue
    info "导入离线镜像 $f"
    case "$f" in *.gz) gunzip -c "$f" | docker load ;; *) docker load -i "$f" ;; esac
  done
}

cmd_init() {
  check_prerequisites
  if [ ! -f .env ]; then
    cp .env.example .env
    chmod 600 .env
    fail "已生成 .env，请先填写 SERVER_HOST、SUPER_ADMIN_PHONE、SUPER_ADMIN_PASSWORD、EUIPD_INITIAL_PASSWORD 后重新执行 ./euipd.sh init"
  fi
  chmod 600 .env

  local k generated=0
  [ -n "$(env_get MINIO_ROOT_USER)" ] || env_set MINIO_ROOT_USER euipd-admin
  [ -n "$(env_get EUIPD_OSS_ACCESS_KEY_ID)" ] || env_set EUIPD_OSS_ACCESS_KEY_ID euipd-app
  for k in MYSQL_ROOT_PASSWORD EUIPD_DB_PASSWORD EUIPD_REDIS_PASSWORD MINIO_ROOT_PASSWORD EUIPD_OSS_ACCESS_KEY_SECRET; do
    if [ -z "$(env_get "$k")" ]; then
      env_set "$k" "$(random_secret 24)"
      generated=$((generated + 1))
    fi
  done
  [ "$generated" -gt 0 ] && info "已为 ${generated} 个空口令生成随机值并写入 .env（请妥善保管 .env）"

  mkdir -p keys
  if [ ! -f keys/jwt-private.pem ]; then
    openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out keys/jwt-private.pem 2>/dev/null
    openssl pkey -in keys/jwt-private.pem -pubout -out keys/jwt-public.pem
    info "已生成令牌签名密钥 keys/jwt-private.pem、keys/jwt-public.pem"
  fi
  chmod 700 keys
  # 应用容器以 root 运行，密钥只需属主可读
  chmod 600 keys/jwt-private.pem
  chmod 644 keys/jwt-public.pem

  load_env
  validate_env
  database_initialized || validate_super_admin
  check_artifacts
  prepare_dirs

  load_offline_images
  build_app_image
  if collabora_enabled; then
    info "构建 Collabora 镜像（首次需下载约 1GB 基础镜像）"
    ensure_base_image "${COLLABORA_BASE_IMAGE:-collabora/code:24.04.13.2.1}"
    compose build collabora
  fi
  info "拉取基础组件镜像"
  local image
  for image in "${MYSQL_IMAGE:-mysql:8.0}" "${REDIS_IMAGE:-redis:7-alpine}" "${NGINX_IMAGE:-nginx:1.27-alpine}" \
               "${MINIO_IMAGE:-bitnamilegacy/minio:2025.4.22}"; do
    pull_image "$image" || fail "拉取镜像 ${image} 失败：请按部署说明书配置镜像加速（registry-mirrors 或 .env 的 REGISTRY_MIRROR）后重试"
  done
  info "初始化完成。下一步：./euipd.sh up"
}

wait_healthy() {
  local timeout="${1:-600}" start now svc cid state pending
  start=$(date +%s)
  while :; do
    pending=()
    for svc in mysql redis minio "${APP_SERVICES[@]}"; do
      cid="$(compose ps -q "$svc" 2>/dev/null || true)"
      if [ -z "$cid" ]; then pending+=("$svc"); continue; fi
      state="$(docker inspect -f '{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' "$cid" 2>/dev/null || echo unknown)"
      [ "$state" = "healthy" ] || pending+=("$svc:$state")
    done
    if collabora_enabled; then
      cid="$(compose ps -q collabora 2>/dev/null || true)"
      state="$( [ -n "$cid" ] && docker inspect -f '{{.State.Health.Status}}' "$cid" 2>/dev/null || echo missing)"
      [ "$state" = "healthy" ] || pending+=("collabora:$state")
    fi
    [ "${#pending[@]}" -eq 0 ] && return 0
    now=$(date +%s)
    if [ $((now - start)) -ge "$timeout" ]; then
      warn "等待超时，未就绪：${pending[*]}"
      warn "请执行 ./euipd.sh logs <服务名> 查看原因"
      return 1
    fi
    printf '\r[EUIPD] 等待服务就绪（%ss）：%s\033[K' "$((now - start))" "${pending[*]}"
    sleep 5
  done
}

cmd_up() {
  check_prerequisites
  load_env
  validate_env
  check_artifacts
  local first_init=0
  if ! database_initialized; then
    validate_super_admin
    first_init=1
  fi
  prepare_dirs
  compose up -d --remove-orphans
  if wait_healthy 900; then
    echo
    info "全部服务已就绪：${PUBLIC_URL}"
    if [ "$first_init" = 1 ]; then
      info "数据库已初始化。超级管理员登录手机号：${SUPER_ADMIN_PHONE}，口令为 .env 中的 SUPER_ADMIN_PASSWORD"
      warn "首次登录确认无误后，请在系统内修改超级管理员口令，并清空 .env 中的 SUPER_ADMIN_PASSWORD"
    fi
    cmd_check || true
  else
    echo
    exit 1
  fi
}

cmd_down() { load_env; compose down; }

cmd_restart() {
  load_env
  if [ "$#" -gt 0 ]; then compose restart "$@"; else compose restart; fi
  wait_healthy 900 && { echo; info "重启完成"; }
}

cmd_logs() { load_env; compose logs -f --tail=200 "$@"; }

cmd_check() {
  load_env
  local ok=1 code
  info "系统版本：$(app_version)（应用镜像 euipd/app:${EUIPD_VERSION:-current}）"
  code="$(curl -s -o /dev/null -w '%{http_code}' "${LOCAL_URL}/" || true)"
  if [ "$code" = "200" ]; then info "前端页面：正常"; else warn "前端页面：HTTP ${code}"; ok=0; fi

  code="$(curl -s -o /dev/null -w '%{http_code}' "${LOCAL_URL}/api/system/auth/public-key" || true)"
  if [ "$code" = "200" ]; then info "后端接口（经网关）：正常"; else warn "后端接口：HTTP ${code}"; ok=0; fi

  # 对象存储经 nginx 访问；未授权读取私有桶应返回 403
  code="$(curl -s -o /dev/null -w '%{http_code}' "${LOCAL_URL}/euipd-doc/" || true)"
  if [ "$code" = "403" ]; then info "对象存储（经 nginx）：正常"; else warn "对象存储（经 nginx）：HTTP ${code}"; ok=0; fi

  # 服务端也通过访问地址读写对象存储，宿主防火墙拦截容器回访宿主地址时这里会失败
  code="$(compose exec -T file2 curl -s -o /dev/null -w '%{http_code}' --max-time 5 "${PUBLIC_URL}/euipd-doc/" 2>/dev/null || true)"
  if [ "$code" = "403" ]; then info "对象存储（服务端回访 ${PUBLIC_URL}）：正常"; else warn "对象存储（服务端回访 ${PUBLIC_URL}）：失败（${code:-无应答}），见部署说明书“故障排查”"; ok=0; fi

  if collabora_enabled; then
    code="$(curl -s -o /dev/null -w '%{http_code}' "${LOCAL_URL}/hosting/discovery" || true)"
    if [ "$code" = "200" ]; then info "Collabora 在线查看：正常"; else warn "Collabora：HTTP ${code}"; ok=0; fi
  fi
  [ "$ok" = 1 ]
}

cmd_status() {
  load_env
  compose ps
  echo
  cmd_check || true
}

cmd_backup() {
  load_env
  umask 077
  local ts dir
  ts="$(date +%Y%m%d-%H%M%S)"
  dir="backups/${ts}"
  mkdir -p "$dir"
  chmod 700 "$dir"
  info "备份数据库 → ${dir}/mycourse_bk.sql.gz"
  mysql_exec mysqldump -uroot --single-transaction --routines --triggers --events --hex-blob \
    --default-character-set=utf8mb4 --databases mycourse_bk | gzip >"${dir}/mycourse_bk.sql.gz"
  gzip -t "${dir}/mycourse_bk.sql.gz"

  info "备份对象存储 → ${dir}/minio.tar.gz"
  tar -C "${DATA_DIR}" -czf "${dir}/minio.tar.gz" minio

  info "备份配置与密钥 → ${dir}/config.tar.gz"
  tar -czf "${dir}/config.tar.gz" .env keys config nginx

  local keep="${BACKUP_KEEP:-14}" old
  # shellcheck disable=SC2012
  ls -1d backups/*/ 2>/dev/null | sort -r | tail -n +"$((keep + 1))" | while IFS= read -r old; do
    rm -rf "$old"
  done
  info "备份完成：${dir}（保留最近 ${keep} 份）"
  du -sh "$dir"
}

cmd_restore() {
  load_env
  local dir="${1:-}"
  [ -n "$dir" ] && [ -f "${dir}/mycourse_bk.sql.gz" ] || fail "用法：./euipd.sh restore backups/<时间戳>"
  warn "恢复将覆盖当前数据库与对象存储中的全部数据。"
  read -r -p "确认恢复请输入 RESTORE：" answer
  [ "$answer" = "RESTORE" ] || fail "已取消"

  info "停止应用服务"
  compose stop nginx "${APP_SERVICES[@]}" || true
  collabora_enabled && compose stop collabora || true

  info "恢复数据库"
  mysql_exec mysql -uroot -e "DROP DATABASE IF EXISTS mycourse_bk"
  gunzip -c "${dir}/mycourse_bk.sql.gz" | mysql_exec mysql -uroot
  mysql_exec mysql -uroot -e "GRANT ALL PRIVILEGES ON mycourse_bk.* TO 'euipd'@'%'; FLUSH PRIVILEGES"

  if [ -f "${dir}/minio.tar.gz" ]; then
    info "恢复对象存储"
    compose stop minio
    rm -rf "${DATA_DIR}/minio.restore-old"
    mv "${DATA_DIR}/minio" "${DATA_DIR}/minio.restore-old"
    tar -C "${DATA_DIR}" -xzf "${dir}/minio.tar.gz"
    rm -rf "${DATA_DIR}/minio.restore-old"
    prepare_dirs
  fi

  info "清理缓存中的会话与统计数据"
  compose exec -T redis sh -c 'REDISCLI_AUTH="$REDIS_PASSWORD" redis-cli FLUSHALL' >/dev/null

  compose up -d
  wait_healthy 900 && { echo; info "恢复完成"; }
}

cmd_upgrade() {
  check_prerequisites
  load_env
  validate_env
  check_artifacts
  if docker image inspect euipd/app:current >/dev/null 2>&1; then
    docker image tag euipd/app:current euipd/app:previous
    info "当前镜像已另存为 euipd/app:previous，可用于回退"
  fi
  build_app_image
  info "重建应用容器"
  compose up -d --no-deps customer system file2 ftp gateway
  # 公共静态资源以部署包为准重新同步
  compose up -d --no-deps --force-recreate minio-init
  # web/dist 整目录替换后，运行中的 nginx 仍挂载旧目录，须重启才能挂载新目录
  compose up -d --no-deps nginx
  compose restart nginx >/dev/null
  wait_healthy 900 && { echo; info "升级完成"; cmd_check || true; }
}

cmd_mysql() {
  load_env
  if [ -t 0 ]; then
    compose exec mysql sh -c 'export MYSQL_PWD="$MYSQL_ROOT_PASSWORD"; exec mysql -uroot --default-character-set=utf8mb4 mycourse_bk "$@"' sh "$@"
  else
    mysql_exec mysql -uroot --default-character-set=utf8mb4 mycourse_bk "$@"
  fi
}

# 登录失败计数与锁定键由 JDK 序列化写入，键名带二进制前缀，按 *euipd:auth:login-*:<登录名> 模式删除
cmd_unlock() {
  load_env
  local name="${1:-}"
  [[ "$name" =~ ^[0-9]{11}$ ]] || fail "用法：./euipd.sh unlock <11 位手机号>"
  local n
  n="$(compose exec -T redis sh -c 'export REDISCLI_AUTH="$REDIS_PASSWORD"; exec redis-cli --no-raw EVAL "local n=0 for _,k in ipairs(redis.call(\"KEYS\",ARGV[1])) do n=n+redis.call(\"DEL\",k) end return n" 0 "$1"' sh "*euipd:auth:login-*:${name}" | tr -cd '0-9')"
  info "已删除 ${name} 的登录失败计数与锁定键 ${n:-0} 个"
}

usage() { sed -n '3,15p' "$0" | sed 's/^# \{0,1\}//'; }

case "${1:-}" in
  init) shift; cmd_init "$@" ;;
  up|start) shift; cmd_up "$@" ;;
  down|stop) shift; cmd_down "$@" ;;
  restart) shift; cmd_restart "$@" ;;
  status|ps) shift; cmd_status "$@" ;;
  check) shift; cmd_check "$@" ;;
  logs) shift; cmd_logs "$@" ;;
  backup) shift; cmd_backup "$@" ;;
  restore) shift; cmd_restore "$@" ;;
  upgrade) shift; cmd_upgrade "$@" ;;
  mysql) shift; cmd_mysql "$@" ;;
  unlock) shift; cmd_unlock "$@" ;;
  *) usage; exit 1 ;;
esac
