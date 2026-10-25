<!--
 * Author: 周朔鹏
 * Date: 2025-03-14
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <el-dialog
        width="29cm"
        :visible.sync="showDlg"
        :close-on-click-modal=false
        :show-close="false">
      <div>
        <AliPlayer v-bind:player-id="'modal-player-div'" ref="ali_player" ></AliPlayer>
        <span class="icon-close" @click="closeVideo"></span>
      </div>
    </el-dialog>

  </div>
</template>
<script>
  import AliPlayer from "@/components/ali-player.vue";
  export default {
    name: 'modal-player',
    components: {AliPlayer},
    data: function () {
      return {
        aliPlayer: {}, // 播放器实例
        showDlg: false,
      }
    },
    methods: {
      playUrl(url) {
        let _this = this;
        _this.$refs.ali_player.getVideoUrl(url);
      },

      playVod(vid) {
        let _this = this;
        _this.showDlg = true;

        this.$nextTick(() => {
          if (_this.showDlg) {
            _this.$refs.ali_player.getVideoAuth(vid);
          } else {
            console.error("播放异常!");
          }
        });

      },
      //视频播放功能
      closeVideo(){
        let vi = document.getElementsByTagName('video')[0]
        this.showDlg = false;
        if(vi){
          vi.pause();
        }

      },

    }
  }
</script>

<style scoped lang="scss">
@import './../../public/static/scss/mixin.scss';
  .icon-close{
    position:absolute;
    top:20px;
    right:20px;
    @include bgImg(20px,20px,'/imgs/icon-close.png');
    cursor:pointer;
    z-index:11;
  }
</style>
