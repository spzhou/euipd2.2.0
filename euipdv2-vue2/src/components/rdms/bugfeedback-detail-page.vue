<!--
 * Author: 周朔鹏
 * Date: 2025-02-25
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div style="height: 100%;" >
    <div class="paper-height">
      <div style="height: 100%; overflow: scroll;">
        <div class="a4-paper-big-simple" style="padding: 50px 0">
          <BugfeedbackDetailPlain
              :key="bugfeedbackId"
              :bugfeedback-id="bugfeedbackId"
          ></BugfeedbackDetailPlain>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
import Swal from "sweetalert2";
import FileSaver from 'file-saver';
import BugfeedbackDetailPlain from "@/components/rdms/bugfeedback-detail-plain.vue";

export default {
  name: 'bugfeedback-detail-page',

  components: {
    FileSaver,
    BugfeedbackDetailPlain,
  },

  props:{
    bugfeedbackId: {
      default: null,
    },
  },

  data() {
    return {
      loginUser: {},
      loginCustomer: {},

      resFiles: [],
      quality: {},

    };
  },

  computed: {
  },

  mounted: function() {
  },
  methods:{

  }
};
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd.scss';
.paper-height {
  height: calc(100vh - 250px);
}
</style>

