# EUIPD 前端（euipdv2-vue2）

EUIPD 研发过程管理系统的前端，Vue 2.7 + Element UI。项目介绍与完整运行步骤见仓库根目录 [README.md](../README.md)。

## 技术栈

Vue 2.7、Vue Router 3、Element UI 2.15、axios、Tiptap（富文本）、bpmn-js（流程图）、dhtmlx-gantt（甘特图，标准版为 GPL-2.0，见根目录 NOTICE）、AntV G6、Chart.js、Vue CLI 4。

## 运行

要求 Node.js 16（17 以上需设置 `NODE_OPTIONS=--openssl-legacy-provider`）。

```bash
npm install
npm run serve-dev     # 开发服务 http://localhost:8080，使用 .env.dev
npm run build-prod    # 生产构建，使用 .env.prod，输出到 dist/
npm run lint          # 代码检查
```

## 环境变量

| 变量 | 说明 |
| --- | --- |
| `VUE_APP_SERVER` | 接口基址：开发为网关地址 `http://127.0.0.1:18000`，生产为同源前缀 `/api` |
| `VUE_APP_OSS_DOC_BASE` | 对象存储文档桶的公共地址 |
| `VUE_APP_OSS_WEB_BASE` | 对象存储公共资源桶的地址，登录页图片等经 `webAssetUrl()` 从此处读取 |
| `VUE_APP_VERSION`、`VUE_APP_USERTYPE` | 产品版本与用户类型标识，保持缺省值 |
| `VUE_APP_PASSWORD_KEY` | 机构注册口令的加盐字符串，仓库中为空。自行生成随机值（如 `openssl rand -hex 16`）写入 `.env.dev.local`、`.env.prod.local`；该值会编译进前端代码，不能当作密钥 |

模板见 `.env.example`。前端不保存任何口令或密钥。

## 目录

```
src/
├── main.js  app.vue  router.js
├── views/        页面
├── components/   组件
├── shared/  utils/  mixins/  filter/  plugins/
public/
├── static/       静态脚本、样式与第三方库
├── user-protocol.html  privacy-policy.html   用户协议与隐私政策模板，部署方应当替换为正式文本
└── import_workers_template.xlsx              成员导入模板
```
