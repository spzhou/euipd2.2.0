<!--
 * Author: 周朔鹏
 * Date: 2024-10-10
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="footer">
    <div class="footer-logo">
      <img src="/static/rdms/image/EUIPD.png" alt="">
    </div>
    <div class="copyright">通用IPD研发管理系统</div>
  </div>
</template>
<script>
  export default{
    name:'nav-footer'
  }
</script>
<style lang="scss" scoped>
@import './../../public/static/scss/caipd';

  .footer{
    width: 100%;
    height:150px;
    padding-top: 0px;
    border-top:4px solid $colorRed;
    background-color:$colorFooter;
    color:$colorMinorFont;
    font-size:16px;
    text-align:center;
    .footer-logo{
      margin-top:46px;
      margin-bottom:10px;
      img{
        width:120px;
        //height:36px;
        margin-bottom:13px;
      }
    }
    .footer-link{
      a{
        color:$colorMinorFont;
        display:inline-block;
      }
      span{
        margin:0 10px;
      }
      margin-bottom:13px;
    }
    .copyright{
      .domain{
        color:$colorRed;
        font-size: 16px;
      }
    }
  }
</style>
