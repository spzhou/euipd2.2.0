#!/bin/bash
# EUIPD 微服务启停脚本（本地 Maven，macOS 自带 bash 3.2 兼容）
#
# 每个服务在独立会话（setsid）中运行，调用脚本的终端、IDE 任务或代理会话结束后服务仍保持运行。
# 控制台输出与进程号写入 logs/console/<模块>.log、logs/console/<模块>.pid。
#
# 用法：
#   docker/start-services.sh [start] [--build] [--no-wait] [模块...]   启动（已在运行的目标模块先停止）
#   docker/start-services.sh restart [--build] [--no-wait] [模块...]   同 start
#   docker/start-services.sh stop [模块...]                            停止
#   docker/start-services.sh status                                    查看状态
#   docker/start-services.sh logs <模块>                               跟踪控制台日志
#
# 模块：system file2 customer ftp gateway；省略时为全部。
# 选项：
#   --build    停止后、启动前执行 mvn -DskipTests clean install（目标模块；公共库 server 以 jar 提供，不参与编译）
#   --no-wait  不等待端口就绪
# 环境变量：JAVA_HOME（未设置时在 macOS 上取 JDK 11）、WAIT_TIMEOUT（等待秒数，默认 450）；
#           EUIPD_* 口令与密钥从 docker/.env.local 加载

set -o pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT" || exit 1

ALL_MODULES="system file2 customer ftp gateway"
CONSOLE_DIR="$PROJECT_ROOT/logs/console"
WAIT_TIMEOUT="${WAIT_TIMEOUT:-450}"

if [ -z "${JAVA_HOME:-}" ] && [ -x /usr/libexec/java_home ]; then
  JAVA_HOME="$(/usr/libexec/java_home -v 11 2>/dev/null || true)"
fi
if [ -n "${JAVA_HOME:-}" ]; then
  export JAVA_HOME
  export PATH="$JAVA_HOME/bin:$PATH"
fi

# 配置文件中的口令与密钥为 ${EUIPD_*} 占位符，取值来自 docker/.env.local（键名见 docker/env.example）
if [ -f "$PROJECT_ROOT/docker/.env.local" ]; then
  set -a
  . "$PROJECT_ROOT/docker/.env.local"
  set +a
else
  echo "警告：缺少 docker/.env.local，服务将因 \${EUIPD_*} 占位符无法解析而启动失败；请按 docker/env.example 创建。" >&2
fi

usage() {
  sed -n '2,/^$/p' "$0" | sed -e 's/^# \{0,1\}//'
}

port_of() {
  case "$1" in
    gateway)   echo 18000 ;;
    system)    echo 18001 ;;
    file2)     echo 18003 ;;
    customer)  echo 18005 ;;
    ftp)       echo 18007 ;;
    *) return 1 ;;
  esac
}

listening_pid() {
  lsof -nP -t -iTCP:"$1" -sTCP:LISTEN 2>/dev/null | head -1
}

pid_alive() {
  [ -n "$1" ] && kill -0 "$1" 2>/dev/null
}

pid_of() {
  cat "$CONSOLE_DIR/$1.pid" 2>/dev/null
}

# 新会话中执行命令；进程号即会话与进程组号，停止时可整组结束（mvn 与其派生的 java）
detach() {
  if command -v setsid >/dev/null 2>&1; then
    setsid "$@" &
  else
    perl -MPOSIX -e 'POSIX::setsid() >= 0 or die "setsid: $!\n"; exec @ARGV or die "exec: $!\n"' -- "$@" &
  fi
}

stop_modules() {
  local m pid port lp i busy
  for m in "$@"; do
    pid=$(pid_of "$m")
    if pid_alive "$pid"; then
      kill -TERM -- "-$pid" 2>/dev/null || kill -TERM "$pid" 2>/dev/null
    fi
    pkill -TERM -f -- "-pl $m spring-boot:run" 2>/dev/null
    lp=$(listening_pid "$(port_of "$m")")
    [ -n "$lp" ] && kill -TERM "$lp" 2>/dev/null
  done

  for i in $(seq 1 20); do
    busy=0
    for m in "$@"; do
      if [ -n "$(listening_pid "$(port_of "$m")")" ] || pid_alive "$(pid_of "$m")"; then
        busy=1
      fi
    done
    [ "$busy" -eq 0 ] && break
    sleep 1
  done

  for m in "$@"; do
    pid=$(pid_of "$m")
    pid_alive "$pid" && kill -KILL -- "-$pid" 2>/dev/null
    pkill -KILL -f -- "-pl $m spring-boot:run" 2>/dev/null
    lp=$(listening_pid "$(port_of "$m")")
    [ -n "$lp" ] && kill -KILL "$lp" 2>/dev/null
    rm -f "$CONSOLE_DIR/$m.pid"
    echo "  [已停止] $m"
  done
}

build_modules() {
  local list="" m
  for m in "$@"; do list="${list:+$list,}$m"; done
  echo "编译 $list ，日志 logs/console/build.log"
  if ! mvn -DskipTests -pl "$list" -am clean install >"$CONSOLE_DIR/build.log" 2>&1; then
    echo "[失败] 编译失败，最后 30 行："
    tail -30 "$CONSOLE_DIR/build.log"
    exit 1
  fi
  echo "  [完成] 编译通过"
}

start_modules() {
  local m
  for m in "$@"; do
    detach mvn -pl "$m" spring-boot:run >"$CONSOLE_DIR/$m.log" 2>&1 </dev/null
    echo $! >"$CONSOLE_DIR/$m.pid"
    echo "  [已启动] $m  pid=$!  port=$(port_of "$m")  log=logs/console/$m.log"
  done
}

wait_ready() {
  local deadline m pending failed
  deadline=$(( $(date +%s) + WAIT_TIMEOUT ))
  echo "等待端口就绪（最长 ${WAIT_TIMEOUT} 秒）..."
  while :; do
    pending=""; failed=""
    for m in "$@"; do
      [ -n "$(listening_pid "$(port_of "$m")")" ] && continue
      if grep -qE 'APPLICATION FAILED TO START|BUILD FAILURE' "$CONSOLE_DIR/$m.log" 2>/dev/null \
         || ! pid_alive "$(pid_of "$m")"; then
        failed="$failed $m"
      else
        pending="$pending $m"
      fi
    done
    if [ -n "$failed" ]; then
      echo "[失败] 启动失败:$failed ，查看 logs/console/<模块>.log"
      return 1
    fi
    if [ -z "$pending" ]; then
      echo "[完成] 全部就绪"
      return 0
    fi
    if [ "$(date +%s)" -ge "$deadline" ]; then
      echo "[超时] 仍未就绪:$pending"
      return 1
    fi
    sleep 3
  done
}

show_status() {
  local m port pid lp state started
  printf '%-10s %-6s %-8s %-8s %s\n' MODULE PORT PID STATE LAST_STARTED
  for m in $ALL_MODULES; do
    port=$(port_of "$m")
    pid=$(pid_of "$m")
    lp=$(listening_pid "$port")
    if [ -n "$lp" ]; then
      state=up
    elif pid_alive "$pid"; then
      state=starting
    else
      state=down
    fi
    pid_alive "$pid" || pid="-"
    started=$(grep -oE 'Started [A-Za-z]+ in [0-9.]+ seconds' "$CONSOLE_DIR/$m.log" 2>/dev/null | tail -1)
    printf '%-10s %-6s %-8s %-8s %s\n' "$m" "$port" "$pid" "$state" "${started:--}"
  done
}

cmd=start
case "${1:-}" in
  start|restart|stop|status|logs) cmd=$1; shift ;;
  -h|--help|help) usage; exit 0 ;;
esac

build=0
wait=1
modules=""
for a in "$@"; do
  case "$a" in
    --build)   build=1 ;;
    --no-wait) wait=0 ;;
    -h|--help) usage; exit 0 ;;
    *)
      if ! port_of "$a" >/dev/null; then
        echo "未知模块: $a"
        usage
        exit 2
      fi
      modules="$modules $a"
      ;;
  esac
done
[ -z "$modules" ] && modules="$ALL_MODULES"

mkdir -p "$CONSOLE_DIR"

case "$cmd" in
  status)
    show_status
    ;;
  logs)
    set -- $modules
    if [ "$#" -ne 1 ]; then
      echo "logs 需要且只能指定一个模块"
      exit 2
    fi
    exec tail -n 200 -f "$CONSOLE_DIR/$1.log"
    ;;
  stop)
    echo "停止:$modules"
    stop_modules $modules
    ;;
  start|restart)
    command -v mvn >/dev/null 2>&1 || { echo "未找到 mvn"; exit 1; }
    echo "停止:$modules"
    stop_modules $modules
    [ "$build" -eq 1 ] && build_modules $modules
    echo "启动:$modules"
    start_modules $modules
    if [ "$wait" -eq 1 ]; then
      wait_ready $modules
      rc=$?
      show_status
      exit $rc
    fi
    sleep 1
    ;;
esac
