# ftp 服务接口

大文件经 FTP 服务器中转上传。

- 上下文路径：`/ftp`，缺省端口 18007；浏览器经网关访问，路径不变。
- 接口数：1。公共约定（请求头、响应结构、鉴权列含义）见 [README.md](README.md)。
- 本文件由 `scripts/generate-api-doc.py` 生成，修改 Controller 后重新生成，不要手工编辑。

## 目录

- [FTP](#ftp-ftpcontroller)（1）

## FTP FtpController

源码：`ftp/src/main/java/com/course/ftp/controller/FtpController.java`

| 方法 | 路径 | 说明 | 参数 | 返回内容 | 鉴权 |
| --- | --- | --- | --- | --- | --- |
| POST | `/ftp/ftp/single-upload` | 单文件上传接口 | 参 `folder`；表单 `file` | `FileDto` | FILE:UPDATE |
