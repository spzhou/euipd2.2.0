<!--
 * Author: 周朔鹏
 * Date: 2025-03-14
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<!--文档: https://help.aliyun.com/zh/vod/developer-reference/integration?scm=20140722.S_help%40%40%E6%96%87%E6%A1%A3%40%40125570.S_RQW%40ag0%2BBB2%40ag0%2BBB1%40ag0%2Bhot%2Bos0.ID_125570-RL_playauth-LOC_doc%7EUND%7Eab-OR_ser-PAR1_2102029c17419239295036801de7a9-V_4-P0_0-P1_0&spm=a2c4g.11186623.help-search.i20#2fa05c02fe0ux -->
<!--文档: https://video.aliyuncs.com/player/presentation/index.html?spm=a2c4g.11186623.0.0.6b7b5061Ob7enB&type=cover -->
<!--文档: https://github.com/aliyunvideo/AliyunPlayer_Web/blob/master/H5VodVueDemo/README.zh_CN.md -->
<template>
<div id="playerId"  style="width:100%; height:100%">
  <div id="J_prismPlayer"  style="width:100%; height:720px;"></div>
</div>
</template>

<script>
  import Swal from "sweetalert2";

  export default {
    name: 'ali-player',

    data: function () {
      return {
        aliPlayer: {}, // 播放器实例

        videoId:"",
        videoUrl:"",
        vodAuth:"",
      }
    },
    mounted(){

    },
    methods: {
      //播放不加密视频
      async getVideoUrl(url){
        if (typeof LazySdk !== 'undefined') {
          await LazySdk.loadAliplayer();
        }
        //如果已经有播放器了，则将播放器div删除
        if (this.aliPlayer) {
          this.aliPlayer = null;
          $("#" + 'J_prismPlayer').remove();
        }
        // 初始化播放器
        $("#playerId").append("<div id=\"" + "J_prismPlayer\"></div>");
        this.aliPlayer = new Aliplayer({
          id: 'J_prismPlayer',
          width: '29cm',
          height: '16.3cm',
          autoplay: true,
          source: url,
          //cover: '/imgs/product/gallery-1.png',
          cover: '',
        });
      },


      //播放加密视频
      //根据vid+playAuth获取播放链接
      getVideoAuth(vid){
        this.$ajax.get(process.env.VUE_APP_SERVER + '/file/admin/get-auth/' + vid).then((response)=>{
          let resp = response.data;
          let playAuth = '';
          if (resp.success) {
            playAuth = resp.content;
            if (playAuth) {
              this.videoInit(vid, playAuth);
            } else {
              Swal.fire({
                position: 'top-end',
                icon: 'warning',
                title: "视频未准备好!",
                showConfirmButton: false,
                timer: 1500
              })
            }
          } else {
            Swal.fire({
              position: 'top-end',
              icon: 'warning',
              title: "视频未准备好",
              showConfirmButton: false,
              timer: 1500
            })
          }
        })
      },
      async videoInit(vid, playAuth){
        if (typeof LazySdk !== 'undefined') {
          await LazySdk.loadAliplayer();
        }
        //如果已经有播放器了，则将播放器div删除
        if (this.aliPlayer) {
          this.aliPlayer = null;
          $("#" + 'J_prismPlayer').remove();
        }

        $("#playerId").append("<div id=\"" + "J_prismPlayer\"></div>");
        this.aliPlayer = new Aliplayer({
          id: 'J_prismPlayer',
          width: '29cm',
          height: '16.3cm',
          vid : vid, // 必选参数。音视频ID。示例：1e067a2831b641db90d570b6480f****。
          playauth : playAuth, // 必选参数。音视频播放凭证。
          authTimeout: 7200, // 播放地址的有效时长，单位：秒。该时长会覆盖在视频点播控制台设置的URL鉴权的有效时长。如果不传，则取默认值7200。如需设置此参数，请确保该时间大于视频的实际时长，防止播放地址在播放完成前过期。
          autoplay: true,
          cover: '',
        },function(player){
          console.log('The player is created.')
        });
      }
    }
  }
</script>
<style scoped>
.box_relative {
  position: absolute;
  left: 0px;
  top: 5px;
}
</style>
