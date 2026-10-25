<!--
 * Author: 周朔鹏
 * Date: 2024-11-22
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <button type="button" v-on:click="selectFile()" class="btn" style="width: 150px;">
      <i class="ace-icon fa fa-upload"></i>
      {{text}}
    </button>
    <input class="hidden" type="file" ref="file" v-on:change="uploadFile()" v-bind:id="inputId+'-input'">
  </div>
</template>

<script>
  import Swal from "sweetalert2";
  import { Loading } from 'element-ui';
  import $ from 'jquery';

  export default {
    name: 'ftp-rdms',
    props: {
      text: {
        default: "上传文件"
      },
      inputId: {
        default: "file-upload"
      },
      suffixs: {
        default: []
      },
      customer: {
        default: ""
      },
      use: {
        default: ""
      },
      folder: {
        default: ""
      },
      isWebFile:{
        type: Boolean,
        default: false,
      },
      afterUpload: {
        type: Function,
        default: null
      },
    },
    data: function () {
      return {
      }
    },
    methods: {
      uploadFile () {
        let _this = this;
        let formData = new window.FormData();
        let file = _this.$refs.file.files[0];

        // 判断文件格式
        let suffixs = _this.suffixs;
        let fileName = file.name;
        let suffix = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length).toLowerCase();
        let validateSuffix = false;
        for (let i = 0; i < suffixs.length; i++) {
          if (suffixs[i].toLowerCase() === suffix) {
            validateSuffix = true;
            break;
          }
        }
        if (!validateSuffix) {
          //Toast.warning("文件格式不正确！只支持上传：" + suffixs.join(","));
          Swal.fire({
            position: 'top-end',
            icon: 'warning',
            title: "文件格式不正确！只支持上传：" + suffixs.join(","),
            showConfirmButton: false,
            timer: 1500
          })
          $("#" + _this.inputId + "-input").val("");
          return;
        }

        if (file.size && file.size > 209715200) {
          //Toast.warning("文件格式不正确！只支持上传：" + suffixs.join(","));
          Swal.fire({
            position: 'top-end',
            icon: 'warning',
            title: "单个文件大小不能超过200M",
            showConfirmButton: false,
            timer: 2500
          })
          $("#" + _this.inputId + "-input").val("");
          return;
        }

        // key："file"必须和后端controller参数名一致
        formData.append('folder', _this.folder);
        formData.append('file', file);
        formData.append('size', file.size);
        formData.append('use', _this.use);
        formData.append('customer', _this.customer);

        let requestPath = "";
        if(_this.isWebFile){
          requestPath = '/ftp/ftp/single-upload';
        }else{
          requestPath = '/ftp/ftp/single-upload';
        }

        let loadingInstance = Loading.service({ fullscreen: true });
        let BOX = document.querySelector(".el-loading-mask");
        if (_this.isHTMLElement(BOX)) {
          (BOX).style.zIndex = "999999";
        } else {
          console.log('element is not an HTMLElement');
        }

        _this.$ajax.post(process.env.VUE_APP_SERVER + requestPath, formData).then((response)=>{
          loadingInstance.close();
          let resp = response.data;
          console.log("上传文件成功：", resp);
          //这里是组件的回调函数 对应 v-bind:after-upload="afterUploadContentFile"
          _this.afterUpload(resp);
          $("#" + _this.inputId + "-input").val("");
        });
      },

      isHTMLElement(element) {
        return element instanceof HTMLElement;
      },
      selectFile () {
        let _this = this;
        $("#" + _this.inputId + "-input").trigger("click");
      }
    }
  }
</script>
