<!--
 * Author: 周朔鹏
 * Date: 2023-12-16
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="character-content">
    <table class="table-out">
      <tr class="table-out-tr">
        <table class="table-inn">
          <tr class="table-inn-tr">
            <th class="table-inn-th" style="width: 200px; text-align: center;font-weight: normal;">
              <span>签字序号: {{ serialNo }}</span>
            </th>
            <th class="table-inn-th" style="width: 600px;font-weight: normal;">
              <span style="margin-left: 10px;">
                <small><em>(说明: 由公司流程规定的审批人审批签字.)</em></small>
              </span>
            </th>
          </tr>
        </table>
      </tr>
      <tr class="table-out-tr">
        <table class="table-inn">
          <tr class="table-inn-tr">
            <th class="table-inn-th">
              <div style="width: 800px; height: 100px; position: relative;">
                <div style="position: absolute; top: 70px; left: 400px;">
                  <div style="width: 400px;">
                    <div style="display: inline-block; margin-right: 150px;font-weight: normal;">签字:</div><div style="display: inline-block;font-weight: normal;">日期:</div>
                  </div>
                </div>
              </div>
            </th>
          </tr>
        </table>
      </tr>
    </table>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import FileSaver from 'file-saver';


export default {
  components: {
  },
  name: "approve-signature-core",

  props: {
    serialNo: {
      default: null
    },
  },

  computed: {
  },

  data() {
    return {
    };
  },

  mounted: function () {
    let _this = this;
  },

  methods: {
  },
}
</script>

<style>
.character-content {
  background: white;
  width: 800px;
  height: auto;
  display: block;
  box-sizing: border-box;
  font-weight: normal;
}
.table-out {
  width: 800px;
  margin: 10px auto;
  border-top: 1px dotted rgba(26, 26, 26, 0.49);
  border-right: 1px dotted rgba(26, 26, 26, 0.49);
  border-bottom: 1px dotted rgba(26, 26, 26, 0.49);
  border-left: 1px dotted rgba(26, 26, 26, 0.49);
}
.table-out-tr {
  line-height: 28px;
  width: 100%;
}
.table-inn {
  width: 100%;
}
.table-inn-tr {
  line-height: 28px;
  width: 100%;
}
.table-inn-th {
  border-top: 1px dotted rgba(26, 26, 26, 0.49);
  border-bottom: 1px dotted rgba(26, 26, 26, 0.49);
}

</style>

<style lang="scss" scoped>

</style>
