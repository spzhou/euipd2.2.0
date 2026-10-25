<!--
 * Author: 周朔鹏
 * Date: 2025-04-25
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
    name: 'file-rdms',
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
      toastWarn(title, timer) {
        Swal.fire({
          position: 'top-end',
          icon: 'warning',
          title: title,
          showConfirmButton: false,
          timer: timer || 1500,
          returnFocus: false,
          didOpen: () => {
            const container = Swal.getContainer();
            if (container) {
              container.style.zIndex = '10000';
            }
          }
        });
      },
      uploadFile () {
        let _this = this;
        let formData = new window.FormData();
        let file = _this.$refs.file.files[0];
        if (!file) {
          return;
        }

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
          _this.toastWarn("文件格式不正确！只支持上传：" + suffixs.join(","));
          $("#" + _this.inputId + "-input").val("");
          return;
        }

        if (file.size && file.size > 1024*1024*1024) {
          _this.toastWarn("单个文件大小不能超过1024M", 2500);
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
          requestPath = '/file/rdms/oss-file-upload-web';
        }else{
          requestPath = '/file/rdms/oss-simple-rdms';
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
          if (!resp || resp.success === false || !resp.content) {
            _this.toastWarn((resp && resp.message) || "上传失败，请重试", 2500);
            $("#" + _this.inputId + "-input").val("");
            return;
          }
          if (typeof _this.afterUpload === 'function') {
            _this.afterUpload(resp);
          }
          $("#" + _this.inputId + "-input").val("");
        }).catch((e) => {
          loadingInstance.close();
          console.error(e);
          _this.toastWarn("上传失败，请重试", 2500);
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
