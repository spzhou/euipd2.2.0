<!--
 * Author: 周朔鹏
 * Date: 2023-12-23
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="ca-plain-form">
    <div class="ca-form-line" style="font-weight: bold">
      <div class="ca-form-label-l">
        <div class="ca-form-label-title">
          <i class="el-icon-star-on"></i>
          评审标题:
        </div>
      </div>
      <div class="ca-form-content">
        {{ review.jobName }}
      </div>
    </div>
    <div class="ca-form-line">
      <div class="ca-form-label">
        <div class="ca-form-label-title">
          项目名称:
        </div>
      </div>
      <div class="ca-form-content">
        {{ review.projectName }}
      </div>
    </div>
    <div class="ca-form-line">
      <div class="ca-form-label">
        <div class="ca-form-label-title">
          评审组长:
        </div>
      </div>
      <div class="ca-form-content">
        {{ review.reviewGroupLeaderName || review.nextNodeName }}
      </div>
    </div>
    <div class="ca-form-line">
      <div class="ca-form-label">
        <div class="ca-form-label-title">
          评审委员:
        </div>
      </div>
      <div class="ca-form-content">
        {{ review.reviewWorkerIdStr }}
      </div>
    </div>
    <div class="ca-form-line">
      <div class="ca-form-label">
        <div class="ca-form-label-title">
          评审结果:
        </div>
      </div>
      <div class="ca-form-content">
        {{ REVIEW_RESULT | optionKV( review.reviewResult ) }}
      </div>
    </div>
    <div class="ca-form-line">
      <div class="ca-form-label">
        <div class="ca-form-label-title">
          评审主单状态:
        </div>
      </div>
      <div class="ca-form-content">
        {{ JOB_ITEM_STATUS | optionKV( review.status ) }}
      </div>
    </div>
    <div v-if="review.propertyDto && review.propertyDto.jobDescription">
      <div class="ca-form-line">
        <div class="ca-form-label">
          <div class="ca-form-label-title">
            评审意见:
          </div>
        </div>
      </div>
      <div class="ca-form-line-content"
           v-rich-html="review.propertyDto.jobDescription">
      </div>
    </div>
    <div class="ca-form-line">
      <div class="ca-form-label">
        <div class="ca-form-label-title">
          评审时间:
        </div>
      </div>
      <div class="ca-form-content">
        {{ review.propertyDto.createTime | dateFormat }}
      </div>
    </div>
  </div>
</template>

<script>

export default {
  components: {
  },
  name: "review-jobitem-show-plain",

  props: {
    review: {
      default: null,
    },
  },

  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},

      REVIEW_RESULT: REVIEW_RESULT,
      JOB_ITEM_STATUS: JOB_ITEM_STATUS,
    }
  },

  computed: {},

  mounted: function () {
    let _this = this;
  },

  methods: {
    getLoginUser() {
      let _this = this;
      if (Tool.getLoginUser().userGroup === LOGIN_USER_GROUP.CUSTOMER.key) {
        _this.loginUser = Tool.getLoginUser().adminUser;
        _this.loginCustomer = Tool.getLoginUser();
      } else {
        _this.loginUser = Tool.getLoginUser();
        _this.loginCustomer = Tool.getLoginUser().customer;
      }
    },
  }
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd.scss';

</style>
