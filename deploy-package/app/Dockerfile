# EUIPD 后端服务镜像：gateway、system、customer、file2、ftp 共用，由环境变量 SERVICE 选择启动的服务
ARG JAVA_IMAGE=eclipse-temurin:11-jre
FROM ${JAVA_IMAGE}

WORKDIR /app
COPY jars/ /app/jars/
COPY entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh && mkdir -p /app/logs

# customer 首次启动要建立流程引擎表，启动宽限期按 5 分钟设置
HEALTHCHECK --interval=15s --timeout=6s --start-period=300s --retries=5 \
  CMD ["/app/entrypoint.sh", "health"]

ENTRYPOINT ["/app/entrypoint.sh"]
