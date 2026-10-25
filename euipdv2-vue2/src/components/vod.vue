<!--
 * Author: 周朔鹏
 * Date: 2025-03-13
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <vod-file-aliyun v-bind:input-id="inputId"
            v-bind:text="text"
            v-bind:suffixs="suffixs"
            v-bind:use="use"
            v-bind:after-upload="afterUpload"
            v-bind:shard-size="1 * 1024 * 1024"
            v-bind:url="'vod'"></vod-file-aliyun>
</template>

<script>
  import VodFileAliyun from "@/components/vod-file-aliyun";
  export default {
    components: {VodFileAliyun},
    name: 'vod',
    props: {
      text: {
        default: "选择要上传的文件"
      },
      inputId: {
        default: "vod-upload"
      },
      suffixs: {
        default: []
      },
      use: {
        default: ""
      },
      afterUpload: {
        type: Function,
        default: null
      },
    },
    methods: {
    }
  }
</script>
