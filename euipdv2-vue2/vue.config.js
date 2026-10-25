/*
 * Author: 周朔鹏
 * Date: 2025-08-04
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
 */

const webpack = require('webpack');

// 系统版本取 package.json；VUE_APP_VERSION 是版型标识，不复用
process.env.VUE_APP_BUILD_VERSION = require('./package.json').version;

//本地开发环境的代理配置
module.exports = {
  devServer: {
    host: '0.0.0.0',
    port: 8080,
    disableHostCheck: true,
    // 热更新客户端固定走本机，避免 Wi‑Fi/网段切换后仍连旧的 Network IP导致 sockjs 超时
    public: 'localhost:8080',
    sockHost: 'localhost',
    sockPort: 8080,

    hot: true, // 启用热重载
    inline: true,
    watchOptions: {
      poll: true, // 在某些环境下需要启用轮询以检测文件变化
    }
  },
  css: {
    loaderOptions: {
      sass: {
        implementation: require('sass'),
        sassOptions: {
          quietDeps: true,
          silenceDeprecations: ['import', 'legacy-js-api']
        },
      }
    }
  },
  publicPath: process.env.NODE_ENV === 'production'
      ? '/'
      : '/',

  productionSourceMap: false,
  // Tiptap 与 ProseMirror 以 ES2020 发布（可选链等），webpack 4 无法直接解析
  transpileDependencies: ['@tiptap', /prosemirror-/, 'linkifyjs', 'pdfjs-dist', 'bpmn-js'],
  configureWebpack: {
    // 开发用 cheap map；生产不生成 source map，减小产物与下载
    devtool: process.env.NODE_ENV === 'development' ? 'cheap-module-source-map' : false,
    // 代码未调用 moment.locale，不打包 moment 的全部语言包
    plugins: [
      new webpack.IgnorePlugin({ resourceRegExp: /^\.\/locale$/, contextRegExp: /moment$/ })
    ],
    optimization: {
      splitChunks: {
        chunks: 'all',
        // webpack 4 缺省为 5，页面共用组件多时会把共享模块复制进各页面包
        maxAsyncRequests: 30,
        cacheGroups: {
          elementUI: {
            name: 'chunk-element-ui',
            test: /[\\/]node_modules[\\/]element-ui[\\/]/,
            priority: 30
          },
          vendors: {
            name: 'chunk-vendors',
            test: /[\\/]node_modules[\\/]/,
            priority: 10,
            chunks: 'initial'
          },
          // 不指定固定名称：按共用该模块的页面组合拆包，避免所有页面都下载同一个大包
          commons: {
            minChunks: 2,
            priority: 5,
            reuseExistingChunk: true
          }
        }
      }
    }
  },
  chainWebpack: config => {
    if (process.env.NODE_ENV === 'production') {
      config.optimization.minimizer('terser').tap(args => {
        args[0].terserOptions.compress.pure_funcs = ['console.log'];
        return args;
      });
      // cssnano 4 的 calc 化简不认识容器查询单位（caipd.scss 中的 cqw），开启时生产构建报错
      config.plugin('optimize-css').tap(args => {
        args[0].cssnanoOptions.preset[1].calc = false;
        return args;
      });
    }
  }
};
