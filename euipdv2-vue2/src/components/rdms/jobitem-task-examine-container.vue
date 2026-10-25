<!--
 * Author: 周朔鹏
 * Date: 2025-02-24
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<!-- -->
<template>
  <div class="tab-container">
    <div class="tab-left">
      <div style="height: calc(100vh - 278px); overflow: scroll; overflow-x: hidden">
        <el-table
            ref="evaluateItemTable"
            :data="jobitemList"
            row-key="id"
            border
            default-expand-all
            stripe
            style="width: 100%;"
            highlight-current-row
            :tree-props="{children: 'children', hasChildren: 'hasChildren'}"
            @row-click="rowClick">
          <el-table-column   label="序号" align="center" width="80">
            <template slot-scope="scope">
              <div v-if="scope.row.showFlag === 'children'">
                {{ scope.$index }}
              </div>
              <div v-else>
                原任务
              </div>
            </template>
          </el-table-column>
          <el-table-column
              prop="jobName"
              label="任务名称"
              width="300">
          </el-table-column>
          <el-table-column
              fixed="right"
              label="操作"
              align="center"
              width="102">
            <template slot-scope="scope">
              <div v-if="scope.row.showFlag !== 'children'">
                <div v-if="jobType===JOB_ITEM_TYPE.TEST.key">缺陷清单</div>
                <div v-else>完工清单</div>
              </div>
              <div v-else>
                {{ JOB_ITEM_STATUS | optionKV(scope.row.status) }}
              </div>
            </template>
          </el-table-column>
          <el-table-column
              fixed="right"
              width="35">
            <template slot-scope="scope">
              <i v-show="scope.row.id === currentRow.id" class="el-icon-caret-right" style="font-size: 24px; color: #4794EF;"></i>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
    <div class="tab-right">
      <div v-if="jobitem && jobitem.parentOrChildren === PARENT_CHILDREN.PARENT.key">
        <develop-completed-list-assist
            :key="jobitem.id"
            :jobitem-id="jobitem.id"
            :job-type="jobType"
            :project-type="projectType"
            :index="index"
            :entrance="entrance"
        ></develop-completed-list-assist>
      </div>
      <div v-if="jobitem && jobitem.parentOrChildren === PARENT_CHILDREN.CHILDREN.key">
        <JobitemExaminePage
            :key="jobitem.id + 'jobitem'"
            :jobitem-id="jobitem.id"
            :job-type="jobitem.type"
            :inherit-job-type="jobType"
            :project-type="projectType"
            :index="index"
            :entrance="entrance"
        ></JobitemExaminePage>
      </div>
    </div>
  </div>
</template>

<script>
import File from "../../components/rdms/file";
import Swal from "sweetalert2";
import JobitemExaminePage from "@/components/std/jobitem-examine-page.vue";
import DevelopCompletedListAssist from "@/components/rdms/develop-completed-list-assist";

export default {
  components: {
    File,
    JobitemExaminePage,
    DevelopCompletedListAssist,
  },
  name: "jobitem-task-examine-container",

  props: {
    jobitemId: {
      default: null,
    },
    jobType: {
      default: null,
    },
    projectType: {
      default: null,
    },
    index: {
      default: null,
    },
    entrance: {
      default: null,
    },
  },

  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},
      random: 0,

      jobitemList: [],

      issueJobitemDlg: false,
      dialogTitle: '',
      PARENT_CHILDREN: PARENT_CHILDREN,
      PROJECT_TYPE: PROJECT_TYPE,
      JOB_ITEM_STATUS: JOB_ITEM_STATUS,
      JOB_ITEM_TYPE: JOB_ITEM_TYPE,

      currentRow: {},
      jobitem: {},
      dlgJobItem: {},

    }
  },

  computed: {},

  mounted: function () {
    let _this = this;
    _this.random = Math.random();
    _this.getLoginUser();
    if(Tool.isNotEmpty(_this.jobitemId)){
      _this.getJobitemAndAllEvaluateJobitems(_this.jobitemId);
    }

    _this.$bus.$on('refresh-jobitem-task-examine-container', (data) => {
      if(data){
        _this.getJobitemAndAllEvaluateJobitems(_this.jobitemId);
      }
    });
  },

  beforeDestroy(){
    let _this = this;
    _this.$bus.$off('refresh-jobitem-task-examine-container');
  },

  methods: {
    getLoginUser(){
      let _this = this;
      if(Tool.getLoginUser().userGroup === LOGIN_USER_GROUP.CUSTOMER.key){
        _this.loginUser = Tool.getLoginUser().adminUser;
        _this.loginCustomer =Tool.getLoginUser();
      }else{
        _this.loginUser = Tool.getLoginUser();
        _this.loginCustomer = Tool.getLoginUser().customer;
      }
    },

    rowClick(row, column, e){
      let _this = this;
      _this.currentRow = row;
      _this.jobitem = row;
    },

    getJobitemAndAllEvaluateJobitems(jobitemId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getJobitemAndAllEvaluateJobitems/' + jobitemId).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.jobitemList = resp.content;
          if(_this.jobitemList){
            if(Tool.isNotEmpty(_this.jobitemList[1])){
              _this.currentRow = _this.jobitemList[1];
            }else{
              _this.currentRow = _this.jobitemList[0];
            }
            _this.$refs.evaluateItemTable.setCurrentRow(_this.currentRow);
            _this.jobitem = _this.currentRow;
          }
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

  },
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd.scss';

</style>
