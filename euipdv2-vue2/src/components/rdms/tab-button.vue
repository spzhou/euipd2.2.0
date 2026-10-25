<!--
 * Author: 周朔鹏
 * Date: 2023-12-24
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <slot></slot>
    <span>
      <i class="el-icon-close close-small-btn" @click.stop="close"></i>
    </span>
  </div>
</template>

<script>
// import Swal from "sweetalert2";
export default {
  components: {
  },
  name: "tab-button",

  props:{
    indexIn: {
      type: Number,
      default: null
    },
    closeFunction: {
      type: Function,
      default: null
    },
  },

  data: function() {
    return {
    index: this.indexIn,
    }
  },

  computed: {
  },

  mounted: function() {
  },

  methods: {
    close(){
      let _this = this;
      _this.closeFunction(_this.index);
    },

  }
}
</script>

<style lang="scss">
.close-small-btn{
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  margin-left: 4px;
  cursor: pointer;
}
.close-small-btn:hover{
  background: rgba(186, 191, 197, 0.6);
}

@import './../../../public/static/scss/caipd.scss';
</style>
