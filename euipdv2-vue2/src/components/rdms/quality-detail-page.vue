<!--
 * Author: 周朔鹏
 * Date: 2024-01-03
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div style="height: 100%;" >
    <div class="paper-height">
      <div style="height: 100%; overflow: scroll;">
        <div class="a4-paper-big-simple" style="padding: 50px 0">
          <QualityDetailPlain
              :key="qualityId"
              :quality-id="qualityId"
          ></QualityDetailPlain>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
import Swal from "sweetalert2";
import FileSaver from 'file-saver';
import QualityDetailPlain from "@/components/rdms/quality-detail-plain.vue";

export default {
  name: 'quality-detail-page',

  components: {
    FileSaver,
    QualityDetailPlain,
  },

  props:{
    qualityId: {
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

