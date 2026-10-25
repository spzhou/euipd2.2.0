# Collabora CODE + 公文字体
# fonts/ 下的 .ttf/.otf 由 fetch-fonts.sh 下载；目录为空时按 99-euipd-fonts.conf 的备选字体渲染。
ARG COLLABORA_IMAGE=docker.1ms.run/collabora/code:24.04.13.2.1
FROM ${COLLABORA_IMAGE}

USER root
COPY fonts/ /usr/share/fonts/truetype/euipd/
COPY 99-euipd-fonts.conf /etc/fonts/conf.d/99-euipd-fonts.conf
RUN rm -f /usr/share/fonts/truetype/euipd/.gitkeep \
    && fc-cache -f \
    && (coolconfig update-system-template || true)
USER cool
