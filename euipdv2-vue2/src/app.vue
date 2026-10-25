<!--
 * Author: 周朔鹏
 * Date: 2024-02-04
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div id="app-vue">
    <router-view/>
  </div>
</template>

<style lang="scss">
@import "../public/static/scss/elment-cover.scss";

* {
  -webkit-box-sizing: border-box;
  -moz-box-sizing: border-box;
  box-sizing: border-box;
  scrollbar-width: none;
}
div::-webkit-scrollbar {
  width: 0;
}

*:before,
*:after {
  -webkit-box-sizing: border-box;
  -moz-box-sizing: border-box;
  box-sizing: border-box;
}
html {
  position: relative;
  font-size: 13px;
  -webkit-text-size-adjust: 100%;
  -webkit-tap-highlight-color: rgba(0, 0, 0, 0);
}
body {
  font-family: "Helvetica Neue",Helvetica,"PingFang SC","Hiragino Sans GB","Microsoft YaHei","微软雅黑",Arial,sans-serif;
  font-size: 13px;
  line-height: 1.42857143;
  color: #333;
  background-color: #fff;
  margin: 0;
}
p {
  margin: 0 0;
}
input::placeholder {
  color: rgba(0, 111, 255, 0.5);
  font-size: 1.0em;
  font-style: italic;
}
</style>
