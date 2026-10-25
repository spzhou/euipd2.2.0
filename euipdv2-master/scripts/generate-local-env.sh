#!/usr/bin/env bash
# 生成本地开发与系统测试用的口令、密钥与测试账号文件（均被 .gitignore 忽略，不得提交）：
#   euipdv2-master/docker/.env.local     以 docker/env.example 为模板，必填项填入随机值
#   euipdv2-vue2/.env.dev.local          前端口令加盐字符串 VUE_APP_PASSWORD_KEY
# 用法（在 euipdv2-master/ 目录）：
#   bash scripts/generate-local-env.sh            docker/.env.local 已存在时保留原有取值、按模板补齐新增项；前端文件已存在时跳过
#   bash scripts/generate-local-env.sh --force    全部重新生成（已初始化的数据库与容器口令不会随之改变）
# 测试超级管理员手机号缺省 13800000000，可用环境变量 SUPER_ADMIN_PHONE 指定。
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
TEMPLATE="$PROJECT_ROOT/docker/env.example"
BACKEND_ENV="$PROJECT_ROOT/docker/.env.local"
WEB_ENV="$PROJECT_ROOT/../euipdv2-vue2/.env.dev.local"
FORCE=0
[ "${1:-}" = "--force" ] && FORCE=1

# 字母数字随机串，保证同时含字母与数字（满足初始口令规则）
rand_pw() {
  local len="$1" s
  while :; do
    s="$(LC_ALL=C tr -dc 'A-Za-z0-9' < /dev/urandom 2>/dev/null | head -c "$len" || true)"
    [[ "$s" =~ [A-Za-z] && "$s" =~ [0-9] ]] && { printf '%s' "$s"; return; }
  done
}

write_backend() {
  local mysql redis minio_user minio_pw rabbit_pw
  mysql="$(rand_pw 20)"
  redis="$(rand_pw 20)"
  minio_user="euipd-minio"
  minio_pw="$(rand_pw 20)"
  rabbit_pw="$(rand_pw 20)"

  value_of() {
    case "$1" in
      MYSQL_ROOT_PASSWORD|EUIPD_DB_PASSWORD) printf '%s' "$mysql" ;;
      REDIS_PASSWORD|EUIPD_REDIS_PASSWORD) printf '%s' "$redis" ;;
      MINIO_ROOT_USER|EUIPD_OSS_ACCESS_KEY_ID) printf '%s' "$minio_user" ;;
      MINIO_ROOT_PASSWORD|EUIPD_OSS_ACCESS_KEY_SECRET) printf '%s' "$minio_pw" ;;
      RABBITMQ_DEFAULT_USER) printf 'euipd' ;;
      RABBITMQ_DEFAULT_PASS) printf '%s' "$rabbit_pw" ;;
      EUIPD_INITIAL_PASSWORD) printf '%s' "$(rand_pw 12)" ;;
      SUPER_ADMIN_PHONE) printf '%s' "${SUPER_ADMIN_PHONE:-13800000000}" ;;
      SUPER_ADMIN_PASSWORD) printf '%s' "$(rand_pw 12)" ;;
    esac
  }

  # 补齐模式下沿用原文件中的取值（含已启用的可选项），只为模板新增的键生成值
  local old="${1:-}"
  has_old() { [ -n "$old" ] && grep -q "^$1=" "$old"; }
  old_value() { grep -m1 "^$1=" "$old" | cut -d= -f2-; }

  local tmp line key val seen=" "
  tmp="$(mktemp)"
  {
    echo "# 由 scripts/generate-local-env.sh 于 $(date '+%Y-%m-%d %H:%M') 生成，仅用于本地开发与系统测试。"
    echo "# 本文件含口令，已被 .gitignore 忽略，不得提交或外传。"
    while IFS= read -r line || [ -n "$line" ]; do
      if [[ "$line" =~ ^([A-Z0-9_]+)=(.*)$ ]]; then
        key="${BASH_REMATCH[1]}"
        seen="$seen$key "
        if has_old "$key"; then
          echo "$key=$(old_value "$key")"
        elif [ -z "${BASH_REMATCH[2]}" ]; then
          echo "$key=$(value_of "$key")"
        else
          echo "$line"
        fi
      elif [[ "$line" =~ ^#\ ([A-Z0-9_]+)= ]] && has_old "${BASH_REMATCH[1]}"; then
        key="${BASH_REMATCH[1]}"
        seen="$seen$key "
        echo "$key=$(old_value "$key")"
      else
        echo "$line"
      fi
    done < "$TEMPLATE"
    if [ -n "$old" ]; then
      local extra=""
      while IFS= read -r line; do
        [[ "$line" =~ ^([A-Z0-9_]+)= ]] || continue
        [[ "$seen" == *" ${BASH_REMATCH[1]} "* ]] && continue
        extra="$extra$line"$'\n'
      done < "$old"
      if [ -n "$extra" ]; then
        echo
        echo "# ---------- 原文件中模板未列出的项 ----------"
        printf '%s' "$extra"
      fi
    fi
  } > "$tmp"
  mv "$tmp" "$BACKEND_ENV"
  chmod 600 "$BACKEND_ENV"
}

write_web() {
  umask 077
  {
    echo "# 由 scripts/generate-local-env.sh 生成，已被 .gitignore 忽略，不得提交。"
    echo "VUE_APP_PASSWORD_KEY=$(LC_ALL=C tr -dc 'a-f0-9' < /dev/urandom 2>/dev/null | head -c 32 || true)"
  } > "$WEB_ENV"
}

[ -f "$TEMPLATE" ] || { echo "缺少模板 $TEMPLATE" >&2; exit 1; }

if [ -f "$BACKEND_ENV" ] && [ "$FORCE" -ne 1 ]; then
  backup="$(mktemp)"
  cp "$BACKEND_ENV" "$backup"
  write_backend "$backup"
  rm -f "$backup"
  echo "已存在 docker/.env.local：保留原有取值，按模板补齐新增项（重新生成请加 --force）"
else
  write_backend
  echo "已生成 docker/.env.local"
fi

if [ -d "$(dirname "$WEB_ENV")" ]; then
  if [ -f "$WEB_ENV" ] && [ "$FORCE" -ne 1 ]; then
    echo "已存在 euipdv2-vue2/.env.dev.local，跳过（覆盖请加 --force）"
  else
    write_web
    echo "已生成 euipdv2-vue2/.env.dev.local"
  fi
fi

echo "测试超级管理员：手机号与口令见 docker/.env.local 中的 SUPER_ADMIN_PHONE、SUPER_ADMIN_PASSWORD"
