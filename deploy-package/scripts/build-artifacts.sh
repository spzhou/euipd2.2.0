#!/usr/bin/env bash
# =====================================================================
# 从源码生成部署包所需的构建产物（均不入库）：
#   app/jars/<模块>.jar、app/VERSION      后端 5 个微服务（Maven 构建）
#   web/dist/                            前端（npm run build-prod）
#   mysql/db/、mysql/my.cnf              建库脚本与 MySQL 配置
#   minio/web-assets/                    登录页等公共图片
#   collabora/                           Collabora 镜像构建目录（含已下载的字体）
#
# 用法（在源码仓库中执行）：
#   deploy-package/scripts/build-artifacts.sh [选项]
#     --skip-backend    不重新构建后端，沿用 app/jars
#     --skip-frontend   不重新构建前端，沿用 web/dist
#     --pack            另外打包为 release/euipd-deploy-<版本>.tar.gz，供拷贝到服务器
#
# 前端构建所需的 VUE_APP_PASSWORD_KEY 依次取自：环境变量、deploy-package/.env；
# 都为空时生成随机值并写入 deploy-package/.env（该文件不入库）。
# 该值编译进前端，系统投入使用后每次构建都须使用同一个值。
# =====================================================================
set -euo pipefail

PKG="$(cd "$(dirname "$0")/.." && pwd)"
REPO="$(cd "$PKG/.." && pwd)"
SERVER_DIR="$REPO/euipdv2-master"
WEB_DIR="$REPO/euipdv2-vue2"
MODULES=(gateway system customer file2 ftp)

SKIP_BACKEND=0
SKIP_FRONTEND=0
PACK=0
for arg in "$@"; do
  case "$arg" in
    --skip-backend) SKIP_BACKEND=1 ;;
    --skip-frontend) SKIP_FRONTEND=1 ;;
    --pack) PACK=1 ;;
    -h|--help) sed -n '3,19p' "$0" | sed 's/^# \{0,1\}//'; exit 0 ;;
    *) echo "未知参数：$arg（--help 查看用法）" >&2; exit 1 ;;
  esac
done

info() { printf '\033[32m[build]\033[0m %s\n' "$*"; }
fail() { printf '\033[31m[build]\033[0m %s\n' "$*" >&2; exit 1; }

[ -f "$SERVER_DIR/pom.xml" ] && [ -f "$WEB_DIR/package.json" ] || fail "请在完整的源码仓库中执行（未找到 euipdv2-master 或 euipdv2-vue2）"

VERSION="$(sed -n 's/.*"version": *"\([^"]*\)".*/\1/p' "$WEB_DIR/package.json" | head -n 1)"
[ -n "$VERSION" ] || VERSION=unknown

# ---------- 后端 ----------
if [ "$SKIP_BACKEND" = 0 ]; then
  command -v java >/dev/null || fail "未找到 java，需要 JDK 11 或 17"
  info "构建后端（版本 ${VERSION}）"
  (cd "$SERVER_DIR" && ./mvnw -q -DskipTests clean package)
  mkdir -p "$PKG/app/jars"
  rm -f "$PKG/app/jars/"*.jar
  for m in "${MODULES[@]}"; do
    [ -f "$SERVER_DIR/$m/target/$m.jar" ] || fail "未生成 $m/target/$m.jar"
    cp "$SERVER_DIR/$m/target/$m.jar" "$PKG/app/jars/$m.jar"
  done
  {
    echo "version=${VERSION}"
    echo "build_time=$(date '+%Y-%m-%d %H:%M:%S')"
    commit="$(git -C "$REPO" rev-parse --short HEAD 2>/dev/null || true)"
    [ -z "$commit" ] || echo "commit=${commit}"
  } >"$PKG/app/VERSION"
  info "后端产物：app/jars/{$(IFS=,; echo "${MODULES[*]}")}.jar"
else
  for m in "${MODULES[@]}"; do [ -f "$PKG/app/jars/$m.jar" ] || fail "--skip-backend 但缺少 app/jars/$m.jar"; done
fi

# ---------- 前端 ----------
env_value() { [ -f "$PKG/.env" ] && grep -E "^$1=" "$PKG/.env" | tail -n 1 | cut -d= -f2- || true; }

if [ "$SKIP_FRONTEND" = 0 ]; then
  command -v npm >/dev/null || fail "未找到 npm，需要 Node.js 16 以上"
  KEY="${VUE_APP_PASSWORD_KEY:-$(env_value VUE_APP_PASSWORD_KEY)}"
  if [ -z "$KEY" ]; then
    KEY="$(LC_ALL=C tr -dc 'a-f0-9' </dev/urandom | head -c 32 || true)"
    if [ ! -f "$PKG/.env" ]; then
      cp "$PKG/.env.example" "$PKG/.env"
      chmod 600 "$PKG/.env"
    fi
    if grep -qE '^VUE_APP_PASSWORD_KEY=' "$PKG/.env"; then
      tmp="$(mktemp)"
      awk -v v="$KEY" 'BEGIN{FS=OFS="="} $1=="VUE_APP_PASSWORD_KEY"{print $1"="v; next} {print}' "$PKG/.env" >"$tmp"
      cat "$tmp" >"$PKG/.env" && rm -f "$tmp"
    else
      echo "VUE_APP_PASSWORD_KEY=$KEY" >>"$PKG/.env"
    fi
    info "已生成 VUE_APP_PASSWORD_KEY 并写入 deploy-package/.env；以后升级构建须使用同一个值"
  fi
  info "构建前端"
  (
    cd "$WEB_DIR"
    if [ -f package-lock.json ]; then npm ci --no-audit --no-fund; else npm install --no-audit --no-fund; fi
    VUE_APP_PASSWORD_KEY="$KEY" npm run build-prod
  )
  rm -rf "$PKG/web/dist"
  mkdir -p "$PKG/web"
  cp -R "$WEB_DIR/dist" "$PKG/web/dist"
  info "前端产物：web/dist/"
else
  [ -f "$PKG/web/dist/index.html" ] || fail "--skip-frontend 但缺少 web/dist/index.html"
fi

# ---------- 数据库、对象存储与 Collabora 资源 ----------
mkdir -p "$PKG/mysql/db"
cp "$SERVER_DIR/doc/db/euipd-schema.sql" "$SERVER_DIR/doc/db/init-super-admin.sql" "$PKG/mysql/db/"
cp "$SERVER_DIR/docker/mysql/my.cnf" "$PKG/mysql/my.cnf"

rm -rf "$PKG/minio/web-assets"
cp -R "$SERVER_DIR/docker/minio/web-assets" "$PKG/minio/web-assets"

rm -rf "$PKG/collabora"
mkdir -p "$PKG/collabora/fonts"
cp "$SERVER_DIR/docker/collabora/Dockerfile" "$SERVER_DIR/docker/collabora/99-euipd-fonts.conf" \
   "$SERVER_DIR/docker/collabora/fetch-fonts.sh" "$PKG/collabora/"
cp -R "$SERVER_DIR/docker/collabora/fonts/." "$PKG/collabora/fonts/"
font_count="$(find "$PKG/collabora/fonts" -type f \( -name '*.ttf' -o -name '*.otf' -o -name '*.ttc' \) | wc -l | tr -d ' ')"
info "资源文件：mysql/db/、mysql/my.cnf、minio/web-assets/、collabora/（中文字体 ${font_count} 个）"

chmod 755 "$PKG/euipd.sh" "$PKG/app/entrypoint.sh" "$PKG/mysql/init/01-init-db.sh" "$PKG/minio/init.sh" \
          "$PKG/collabora/fetch-fonts.sh"

# ---------- 打包 ----------
if [ "$PACK" = 1 ]; then
  mkdir -p "$REPO/release"
  out="$REPO/release/euipd-deploy-${VERSION}.tar.gz"
  # 打包内容不含 .env、keys、data、backups 等服务器本地文件
  # macOS 的 bsdtar 会写入扩展属性头，Linux 解压时逐个告警
  tar_opts=()
  if tar --version 2>/dev/null | grep -q bsdtar; then tar_opts=(--no-xattrs --no-mac-metadata); fi
  COPYFILE_DISABLE=1 tar "${tar_opts[@]}" -C "$REPO" -czf "$out" \
    --exclude='deploy-package/.env' --exclude='deploy-package/keys' --exclude='deploy-package/data' \
    --exclude='deploy-package/backups' --exclude='deploy-package/images' --exclude='.DS_Store' \
    --exclude='deploy-package/tools-package' --exclude='deploy-package/EUIPD阿里云测试环境账号口令.md' \
    deploy-package
  info "部署包：${out}（$(du -h "$out" | cut -f1)）"
fi

info "构建完成。部署步骤见 deploy-package/EUIPD部署说明书.md"
