/*
 * 按需动态加载第三方 SDK（Aliplayer / 阿里云上传），避免阻塞首屏
 */
(function (global) {
  function loadScript(src) {
    return new Promise(function (resolve, reject) {
      if (!src) {
        resolve();
        return;
      }
      var existed = document.querySelector('script[src="' + src + '"]');
      if (existed) {
        if (existed.getAttribute('data-loaded') === '1') {
          resolve();
        } else {
          existed.addEventListener('load', function () { resolve(); });
          existed.addEventListener('error', reject);
        }
        return;
      }
      var s = document.createElement('script');
      s.src = src;
      s.async = true;
      s.onload = function () {
        s.setAttribute('data-loaded', '1');
        resolve();
      };
      s.onerror = reject;
      document.head.appendChild(s);
    });
  }

  function loadCss(href) {
    return new Promise(function (resolve) {
      if (!href) {
        resolve();
        return;
      }
      if (document.querySelector('link[href="' + href + '"]')) {
        resolve();
        return;
      }
      var l = document.createElement('link');
      l.rel = 'stylesheet';
      l.href = href;
      l.onload = function () { resolve(); };
      l.onerror = function () { resolve(); };
      document.head.appendChild(l);
    });
  }

  function staticRoot() {
    var scripts = document.getElementsByTagName('script');
    for (var i = 0; i < scripts.length; i++) {
      var src = scripts[i].src || '';
      var idx = src.indexOf('static/js/lazy-sdk.js');
      if (idx >= 0) {
        return src.substring(0, idx);
      }
    }
    return '/';
  }

  global.LazySdk = {
    loadAliplayer: function () {
      if (global.Aliplayer) {
        return Promise.resolve(global.Aliplayer);
      }
      var css = 'https://g.alicdn.com/apsara-media-box/imp-web-player/2.25.1/skins/default/aliplayer-min.css';
      var js = 'https://g.alicdn.com/apsara-media-box/imp-web-player/2.25.1/aliplayer-min.js';
      return loadCss(css).then(function () {
        return loadScript(js);
      }).then(function () {
        return global.Aliplayer;
      });
    },

    loadAliyunUpload: function () {
      if (global.AliyunUpload || (global.OSS && global.Vod)) {
        return Promise.resolve();
      }
      // 阿里云上传 SDK 不随仓库提供，须自行下载后放入 public/static/aliyunlib/aliyun-upload-sdk/
      var root = staticRoot() + 'static/aliyunlib/aliyun-upload-sdk/';
      return loadScript(root + 'lib/es6-promise.min.js')
        .then(function () { return loadScript(root + 'lib/aliyun-oss-sdk-6.17.1.min.js'); })
        .then(function () { return loadScript(root + 'aliyun-upload-sdk-1.5.3.min.js'); })
        .catch(function () {
          throw new Error('未找到阿里云视频上传 SDK，请按 README 将其放入 public/static/aliyunlib/aliyun-upload-sdk/');
        });
    }
  };
})(typeof window !== 'undefined' ? window : this);
