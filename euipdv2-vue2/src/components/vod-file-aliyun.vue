<!--
 * Author: 周朔鹏
 * Date: 2025-03-14
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <el-button type="primary" v-on:click="selectFile()">
      {{text}}
    </el-button>
    <input class="hidden" type="file" ref="file" v-on:change="fileChange($event)" v-bind:id="inputId+'-input'">
    <div class="upload">
      <div>
        <label class="status">上传状态: <span>{{statusText}}</span></label>
      </div>
      <div style="display: flex; flex-direction: row; justify-content: space-between; align-items: center;">
        <el-button @click="authUpload" :disabled="uploadDisabled"><i class="ace-icon fa fa-upload"></i> 开始上传</el-button>
        <el-progress style="width: 550px;" :percentage="authProgress" ></el-progress>
<!--        <button @click="pauseUpload" :disabled="pauseDisabled">暂停</button>
        <button :disabled="resumeDisabled" @click="resumeUpload">恢复上传</button>-->
<!--        <span class="progress">上传进度: <i id="auth-progress">{{authProgress}}</i> %</span>-->
      </div>
    </div>
    <!-- 隐藏的video元素用于获取视频时长 -->
    <video ref="video" style="display: none;"></video>
  </div>
</template>

<script>
  import Swal from "sweetalert2";

  export default {
    name: 'vod-file-aliyun',
    props: {
      text: {
        default: "上传VOD文件"
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
      shardSize: {
        default: 1 * 1024 * 1024
      },
      url: {
        default: "vod"
      },
      afterUpload: {
        type: Function,
        default: null
      },
    },
    data: function () {
      return {
        timeout: '',
        partSize: '',
        parallel: '',
        retryCount: '',
        retryDuration: '',
        region: 'cn-shanghai',
        userId: '1248828747616382',
        file: null,
        authProgress: 0,
        uploadDisabled: true,
        resumeDisabled: true,
        pauseDisabled: true,
        uploader: null,
        statusText: '',
        param: {},
        videoId: '',
        videoPath: '',
        duration: 0,
      }
    },

    methods: {
      fileChange (e) {
        let _this = this;
        let file = e.target.files[0]
        _this.file = file;

        // 创建一个URL对象用于video元素加载文件
        let videoUrl = URL.createObjectURL(file);
        _this.$refs.video.src = videoUrl;

        // 监听loadedmetadata事件以获取视频时长
        _this.$refs.video.addEventListener('loadedmetadata', function () {
          _this.duration = _this.$refs.video.duration;
          console.log('视频时长:', _this.duration, '秒');

          // 在这里可以进行其他操作，比如验证视频时长是否符合要求
          if (_this.duration > 3600 * 5) { // 假设视频时长不能超过1小时
            Swal.fire({
              position: 'top-end',
              icon: 'warning',
              title: "视频时长不能超过5小时！",
              showConfirmButton: false,
              timer: 1500
            });
            $("#" + _this.inputId + "-input").val("");
            return;
          }
        });

        //文件合规性判断与数据准备
        // 生成文件标识，标识多次上传的是不是同一个文件
        let key = hex_md5(file.name + file.size + file.type);
        let key10 = parseInt(key, 16);
        let key62 = Tool._10to62(key10);
        console.log(key, key10, key62);
        console.log(hex_md5(Array()));

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

        let size = file.size;
        if(size > (1024 * 1024 * 1023)){ //稍微小一点点
          Toast.success("单个文件不能大于1G ！", Swal);
          return;
        }

        // 计算文件分片
        _this.partSize = _this.shardSize
        let shardIndex = 1;		//分片索引，1表示第1个分片
        _this.parallel = Math.ceil(size / _this.shardSize); //总片数

        _this.param = {
          'use': _this.use,
          'name': file.name,
          'suffix': suffix,
          'size': file.size,
          'key': key62,
          'vod': '',
        };

        /*检查相关文件是否已经上传过了,如果上传过了,就"极速秒传"*/
        _this.$ajax.get(process.env.VUE_APP_SERVER + '/file/admin/check/' + _this.param.key).then((response)=>{
          let resp = response.data;
          if (resp.success) {
            let obj = resp.content;
            if (obj) {
              _this.param.vod = obj.vod;
              _this.sectionSave(_this.param.key);
              $("#" + _this.inputId + "-input").val("");
              //关闭选择文件时创建的uploader
              if (_this.uploader) {
                _this.uploader.stopUpload()
                _this.authProgress = 0
                _this.statusText = ""
              }
              _this.authProgress = 100
              //Toast.success("文件极速秒传成功！");
              Swal.fire({
                position: 'top-end',
                icon: 'success',
                title: "文件极速秒传成功",
                showConfirmButton: false,
                timer: 1000
              })
            }else{
              /*以下是阿里云的sdk*/
              if (!_this.file) {
                alert("请先选择需要上传的文件!")
                return
              }
              var Title = _this.file.name
              var userData = '{"Vod":{}}'

              //如果存在uploader,就结束他,然后重新初始化一个uploader
              if (_this.uploader) {
                _this.uploader.stopUpload()
                _this.authProgress = 0
                _this.statusText = ""
              }
              // 按需加载阿里云上传 SDK 后再初始化
              const ensureSdk = (typeof LazySdk !== 'undefined')
                ? LazySdk.loadAliyunUpload()
                : Promise.resolve();
              ensureSdk.then(function () {
                _this.uploader = _this.createUploader()
                console.log(userData)
                _this.uploader.addFile(_this.file, null, null, null, userData)
                _this.uploadDisabled = false
                _this.pauseDisabled = true
                _this.resumeDisabled = true
              }).catch(function (e) {
                _this.statusText = e.message
              })
              /*上面的部分是aliyun SDK的初始化*/
            }
          } else {
            Toast.warning(resp.message,Swal);
          }
        })


      },

      authUpload () {
        let _this = this;
        // 然后调用 startUpload 方法, 开始上传
        if (_this.uploader !== null) {
          // Progress.show(_this.authProgress);
          // Loading.show();
          _this.uploader.startUpload()
          _this.uploadDisabled = true
          _this.pauseDisabled = false
        }
      },
      // 暂停上传
      pauseUpload () {
        // Loading.hide();
        // Progress.hide();
        let _this = this;
        if (_this.uploader !== null) {
          _this.uploader.stopUpload()
          _this.resumeDisabled = false
          _this.pauseDisabled = true
        }
      },
      // 恢复上传
      resumeUpload () {
        let _this = this;
        if (_this.uploader !== null) {
          //Progress.show(_this.authProgress);
          // Loading.show();
          _this.uploader.startUpload()
          _this.resumeDisabled = true
          _this.pauseDisabled = false
        }
      },
      createUploader (type) {
        let self = this
        let uploader = new AliyunUpload.Vod({
          timeout: self.timeout || 60000,
          partSize: self.partSize || 1048576,
          parallel: self.parallel || 5,
          retryCount: self.retryCount || 3,
          retryDuration: self.retryDuration || 2,
          region: self.region,
          userId: self.userId,
          // 添加文件成功
          addFileSuccess: function (uploadInfo) {
            self.uploadDisabled = false
            self.resumeDisabled = false
            self.statusText = '添加文件成功, 等待上传...'
            console.log("addFileSuccess: " + uploadInfo.file.name)
          },
          // 开始上传
          onUploadstarted: function (uploadInfo) {
            // 如果是 UploadAuth 上传方式, 需要调用 uploader.setUploadAuthAndAddress 方法
            // 如果是 UploadAuth 上传方式, 需要根据 uploadInfo.videoId是否有值，调用点播的不同接口获取uploadauth和uploadAddress
            // 如果 uploadInfo.videoId 有值，调用刷新视频上传凭证接口，否则调用创建视频上传凭证接口
            // 注意: 这里是测试 demo 所以直接调用了获取 UploadAuth 的测试接口, 用户在使用时需要判断 uploadInfo.videoId 存在与否从而调用 openApi
            // 如果 uploadInfo.videoId 存在, 调用 刷新视频上传凭证接口(https://help.aliyun.com/document_detail/55408.html)
            // 如果 uploadInfo.videoId 不存在,调用 获取视频上传地址和凭证接口(https://help.aliyun.com/document_detail/55407.html)
            if (!uploadInfo.videoId) {
              self.$ajax.post(process.env.VUE_APP_SERVER + '/file/admin/get-address-auth', self.param).then((response) => {
                let resp = response.data;
                if (resp.success) {
                  let data = resp.content;
                  console.log("成功获得上传地址和凭证：", resp);
                  let uploadAuth = data.uploadAuth
                  let uploadAddress = data.uploadAddress
                  // let videoId = data.videoId;
                  self.videoId = data.videoId;
                  self.videoPath = data.path;
                  uploader.setUploadAuthAndAddress(uploadInfo, uploadAuth, uploadAddress, self.videoId)

                  self.statusText = '文件开始上传...'
                  console.log("onUploadStarted:" + uploadInfo.file.name + ", endpoint:" + uploadInfo.endpoint + ", bucket:" + uploadInfo.bucket + ", object:" + uploadInfo.object)
                } else {
                  Toast.warning(resp.message,Swal);
                }
              });
            } else {
              self.param.vod = uploadInfo.videoId;
              self.$ajax.post(process.env.VUE_APP_SERVER + '/file/admin/refresh-address-auth', self.param).then((response) => {
                let resp = response.data;
                if (resp.success) {
                  let data = resp.content;
                  console.log("成功获得上传地址和凭证：", resp);
                  let uploadAuth = data.uploadAuth
                  let uploadAddress = data.uploadAddress
                  // let videoId = data.videoId;
                  self.videoId = data.videoId;
                  self.videoPath = data.path;
                  uploader.setUploadAuthAndAddress(uploadInfo, uploadAuth, uploadAddress, self.videoId)

                  self.statusText = '文件开始上传...'
                  console.log("onUploadStarted:" + uploadInfo.file.name + ", endpoint:" + uploadInfo.endpoint + ", bucket:" + uploadInfo.bucket + ", object:" + uploadInfo.object)
                } else {
                  Toast.warning(resp.message,Swal);
                }
              });
            }
          },
          // 文件上传成功
          onUploadSucceed: function (uploadInfo) {
            console.log("onUploadSucceed: " + uploadInfo.file.name + ", endpoint:" + uploadInfo.endpoint + ", bucket:" + uploadInfo.bucket + ", object:" + uploadInfo.object)
            self.statusText = '文件上传成功!'

          },
          // 文件上传失败
          onUploadFailed: function (uploadInfo, code, message) {
            console.log("onUploadFailed: file:" + uploadInfo.file.name + ",code:" + code + ", message:" + message)
            self.statusText = '文件上传失败!'
          },
          // 取消文件上传
          onUploadCanceled: function (uploadInfo, code, message) {
            console.log("Canceled file: " + uploadInfo.file.name + ", code: " + code + ", message:" + message)
            self.statusText = '文件已暂停上传'
          },
          // 文件上传进度，单位：字节, 可以在这个函数中拿到上传进度并显示在页面上
          onUploadProgress: function (uploadInfo, totalSize, progress) {
            console.log("onUploadProgress:file:" + uploadInfo.file.name + ", fileSize:" + totalSize + ", percent:" + Math.ceil(progress * 100) + "%")
            let progressPercent = Math.ceil(progress * 100)
            self.authProgress = progressPercent
            self.statusText = '文件上传中...'
          },
          // 上传凭证超时
          onUploadTokenExpired: function (uploadInfo) {
            self.$ajax.post(process.env.VUE_APP_SERVER + '/file/admin/refresh-address-auth', this.param).then((response) => {
              let resp = response.data;
              if (resp.success) {
                let data = resp.content;
                console.log("成功获得上传地址和凭证：", resp);
                let uploadAuth = data.UploadAuth
                uploader.resumeUploadWithAuth(uploadAuth)
                console.log('upload expired and resume upload with uploadauth ' + uploadAuth)
              } else {
                Toast.warning(resp.message,Swal);
              }
            });
            self.statusText = '文件超时...'
          },
          // 全部文件上传结束
          onUploadEnd: function (uploadInfo) {
            console.log("onUploadEnd: uploaded all the files")
            self.statusText = '文件上传完毕'
            self.param.vod = self.videoId;
            //1. 自动将已经上传的文件保存到file表中, 对数据库中的所有文件进行记录,以便实现极速秒传功能,以及将来通过算法,删除垃圾文件等操作
            self.$ajax.post(process.env.VUE_APP_SERVER + '/file/admin/file-save', self.param).then((response) => {
              let resp = response.data;
              if (resp.success) {
                let data = resp.content;
                console.log("上传文件记录已经保存到file表中：", resp);
              } else {
                Toast.warning(resp.message,Swal);
              }
            });

            //2. 通过回调将需要用户保存的数据返回给用户,由用户决定如何保存或保存在哪里
            let title = self.file.name.substring(0,self.file.name.lastIndexOf(".")).toLowerCase();
            let callbackParam = {
              'vod': self.videoId,
              'path': self.videoPath,
              'title': title,
              'time': self.duration,
            };
            self.afterUpload(callbackParam);
            // Progress.hide();
            // Loading.hide();
          }
        })
        return uploader
      },
      sectionSave(key){
        let _this = this;
        console.log("极速秒传生产小节数据")

        //1. 自动将已经上传的文件保存到file表中, 对数据库中的所有文件进行记录,以便实现极速秒传功能,以及将来通过算法,删除垃圾文件等操作
        this.$ajax.get(process.env.VUE_APP_SERVER + '/file/admin/file-info/'+ key).then((response) => {
          let resp = response.data;
          if (resp.success) {
            let data = resp.content;
            let callbackParam = {
              'vod': data.vod,
              'path': data.path,
              'title': data.name.substring(0,this.file.name.lastIndexOf(".")),
              'time': _this.duration,
            };
            /*//停止实际上传动作
            this.pauseUpload ();*/

            this.afterUpload(callbackParam);
          } else {
            Toast.warning(resp.message,Swal);
          }
        });
      },

      selectFile () {
        $("#" + this.inputId + "-input").trigger("click");
      }
    }
  }
</script>

<style>
.progress {
  line-height: 22px;
  font-size: 14px;
  float:right;
  margin-right: 20px;
}
.progress i {
  font-style: normal;
}
</style>
