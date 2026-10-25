#!/usr/bin/env bash
# 下载公文渲染所需的开源字体到 docker/collabora/fonts/，之后执行 docker compose --profile rich build collabora。
# FONT_SOURCE=npm（缺省）从 npm 镜像（NPM_REGISTRY，缺省 https://registry.npmmirror.com）取 @fontpkg 字体包；
# FONT_SOURCE=github 从 GitHub 发布地址下载，可设置 GITHUB_PROXY（如 https://ghproxy.example.com/）加速。
set -euo pipefail

cd "$(dirname "$0")"
mkdir -p fonts
SOURCE="${FONT_SOURCE:-npm}"
REGISTRY="${NPM_REGISTRY:-https://registry.npmmirror.com}"
PROXY="${GITHUB_PROXY:-}"
TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT

# 从 npm 包中取出一个字体文件：fetch_npm 目标文件名 包名 版本 包内路径
fetch_npm() {
  local name="$1" pkg="$2" ver="$3" inner="$4"
  if [ -s "fonts/$name" ]; then
    echo "skip $name"
    return
  fi
  local tgz="$TMP/$(echo "$pkg-$ver" | tr '/@' '__').tgz"
  if [ ! -s "$tgz" ]; then
    echo "download $pkg@$ver"
    curl -fL --retry 3 --connect-timeout 20 -C - -o "$tgz" "$REGISTRY/$pkg/-/${pkg##*/}-$ver.tgz"
  fi
  tar xzf "$tgz" -O "package/$inner" > "fonts/$name.part"
  mv "fonts/$name.part" "fonts/$name"
}

fetch_url() {
  local name="$1" url="$2"
  if [ -s "fonts/$name" ]; then
    echo "skip $name"
    return
  fi
  echo "download $name"
  curl -fL --retry 3 --connect-timeout 20 -o "fonts/$name.part" "${PROXY}${url}"
  mv "fonts/$name.part" "fonts/$name"
}

if [ "$SOURCE" = "github" ]; then
  # 思源宋体（标题 Heavy、页码 Regular）
  fetch_url SourceHanSerifSC-Heavy.otf   https://github.com/adobe-fonts/source-han-serif/raw/release/SubsetOTF/CN/SourceHanSerifCN-Heavy.otf
  fetch_url SourceHanSerifSC-Regular.otf https://github.com/adobe-fonts/source-han-serif/raw/release/SubsetOTF/CN/SourceHanSerifCN-Regular.otf
  # 思源黑体（一级层次序数）
  fetch_url SourceHanSansSC-Regular.otf  https://github.com/adobe-fonts/source-han-sans/raw/release/SubsetOTF/CN/SourceHanSansCN-Regular.otf
  # 朱雀仿宋（正文）
  fetch_url ZhuqueFangsong-Regular.ttf   https://github.com/TrionesType/zhuque/releases/latest/download/ZhuqueFangsong-Regular.ttf
  # 霞鹜文楷（二级层次序数、副标题）
  fetch_url LXGWWenKai-Regular.ttf       https://github.com/lxgw/LxgwWenKai/releases/latest/download/LXGWWenKai-Regular.ttf
else
  fetch_npm SourceHanSerifSC-Heavy.otf   @fontpkg/source-han-serif-sc 2.3.1 SourceHanSerifSC-Heavy.otf
  fetch_npm SourceHanSerifSC-Regular.otf @fontpkg/source-han-serif-sc 2.3.1 SourceHanSerifSC-Regular.otf
  fetch_npm SourceHanSansSC-Regular.otf  @fontpkg/source-han-sans-sc 2.5.0 SourceHanSansSC-Regular.otf
  fetch_npm ZhuqueFangsong-Regular.ttf   @fontpkg/zhuque-fangsong-technical-preview 0.212.0 ZhuqueFangsong-Regular.ttf
  fetch_npm LXGWWenKai-Regular.ttf       @fontpkg/lxgw-wen-kai 1.520.0 LXGWWenKai-Regular.ttf
fi

ls -lh fonts
