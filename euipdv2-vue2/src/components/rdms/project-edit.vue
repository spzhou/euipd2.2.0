<!--
 * Author: 周朔鹏
 * Date: 2025-02-28
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="paper-height">
    <div style="height: 100%; overflow-y: scroll;">
      <div class="a4-paper-big-simple">
        <div class="ca-form">
          <el-form label-width="150px" size="small">
            <el-form-item
                label="项目名称:"
                required>
              <el-input style="width: 300px;"
                        v-model="project.projectName"
              ></el-input>
            </el-form-item>
            <el-form-item
                required
                label="项目简介:">
              <el-input style="width: 700px;"
                        type="textarea"
                        :autosize="{ minRows: 5, maxRows: 15}"
                        placeholder="请填写项目简介"
                        v-model="project.projectIntroduce"
              ></el-input>
            </el-form-item>
            <el-form-item
                required
                label="监管委员:">
              <el-select v-model="project.supervise" filterable placeholder="请选择">
                <el-option
                    v-for="item in supervises"
                    :key="item.id"
                    :label="item.trueName"
                    :value="item.id">
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item
                required
                label="产品经理:">
              <el-select v-model="project.productManagerId" filterable placeholder="请选择">
                <el-option
                    v-for="item in productManagers"
                    :key="item.id"
                    :label="item.trueName"
                    :value="item.id">
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item
                required
                label="系统工程:">
              <el-select v-model="project.systemManagerId" filterable placeholder="请选择">
                <el-option
                    v-for="item in systemManagers"
                    :key="item.id"
                    :label="item.trueName"
                    :value="item.id">
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item
                required
                label="项目经理:">
              <el-select v-model="project.projectManagerId" filterable placeholder="请选择">
                <el-option
                    v-for="item in projectManagers"
                    :key="item.id"
                    :label="item.trueName"
                    :value="item.id">
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item
                required
                label="测试主管:">
              <el-select v-model="project.testManagerId" filterable placeholder="请选择">
                <el-option
                    v-for="item in workerList"
                    :key="item.key"
                    :label="item.label"
                    :value="item.key">
                </el-option>
              </el-select>
            </el-form-item>
            <div v-if="project.rdType !== RD_TYPE.MODULE.key">
              <el-form-item
                  required
                  label="核心成员:">
              </el-form-item>
              <div>
                <el-transfer style="margin-left: 72px; margin-bottom: 15px;"
                             ref="character-edit"
                             filterable
                             filter-placeholder="请输入姓名"
                             v-model="value"
                             :data="workerList"
                             :titles="titles">
                </el-transfer>
              </div>
              <el-form-item  v-show="!releaseTimeShowFlag"
                             required
                             label="发布时间:">
                <el-date-picker
                    v-model="project.releaseTime"
                    type="date"
                    placeholder="选择日期"
                    :picker-options="pickerOptions"
                    @change="onDateChange">
                </el-date-picker>
              </el-form-item>
            </div>
          </el-form>
        </div>
        <div v-if="project.rdType !== RD_TYPE.MODULE.key">
          <!-- 添加里程碑-->
          <div class="hr 8 dotted"></div>
          <div>
            <project-milestone
                ref="milestoneRef"
                :key="project.id + timeStamp"
                :project="project"
                :milestones="project.milestoneList">
            </project-milestone>
          </div>
        </div>
        <div v-if="project.rdType !== RD_TYPE.MODULE.key">
          <div class="hr 8 dotted"></div>
          <div style="margin: 0 144px 50px 110px; ">
            <div style="font-size: 15px; margin: 8px 0;">
              调整项目预算
            </div>
            <el-table
                :data="subprojectList"
                border
                style="width: 100%">
              <el-table-column
                  prop="projectCode"
                  align="center"
                  label="项目编号"
                  min-width="150">
              </el-table-column>
              <el-table-column
                  prop="label"
                  align="center"
                  label="项目名称"
                  min-width="200">
              </el-table-column>
              <el-table-column
                  align="center"
                  label="操作">
                <template slot-scope="scope">
                    <el-button type="primary" size="small" v-on:click="budgetEdit(scope.row)">
                      预算调整
                    </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
        <div>
          <div class="clearfix form-actions">
            <div style="display: flex; justify-content: center;">
              <button v-on:click="save()" class="ca-btn-primary" type="button">
                保存
              </button>
              <button v-on:click="close()" class="ca-btn-primary" type="button">
                关闭
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!--预算调整-->
    <el-dialog
        :visible.sync="budgetEditVisible"
        width="23cm"
        append-to-body
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ ((dialogTitle)) }}</div>
      <BudgetAdjustDlg
          :key="((subproject.id) + '-' + (random))"
          :subproject-id="subproject.id"
          :project-type="PROJECT_TYPE.SUB_PROJECT.key"
          :close-function="closeDialog"
      ></BudgetAdjustDlg>
    </el-dialog>
  </div>
</template>
<script>
import Swal from "sweetalert2";
import CharacterBudgetConfirm from "@/components/rdms/character-budget-confirm-dlg";
import CharacterDetailInfoCore from "@/components/std/character-detail-info-core.vue";
import projectMilestone from "@/components/rdms/project-milestone";
import moment from 'moment';
import BudgetAdjustDlg from "@/components/rdms/budget-adjust-dlg.vue";

export default {
  components:{
    BudgetAdjustDlg,
    CharacterBudgetConfirm,
    CharacterDetailInfoCore,
    projectMilestone,
  },

  name: 'project-edit',
  props:{
    projectId: {
      default: null,
    },
    index: {
      default: null,
    },
    closeFunction: {
      type: Function,
      default: null
    },
  },

  data() {
    return {
      loginUser: {},
      loginCustomer: {},

      value: [],
      workers: [],
      workerList: [],
      titles: ['可选人员','核心成员'],

      supervises: [],
      productManagers: [],
      projectManagers: [],
      systemManagers: [],

      characterList: [],
      project: {},
      originTeam: {},

      multipleSelection: [],
      subprojectList: [],

      standardManhour: {},
      dialogVisible: false,
      characterItem: {},
      subproject: {},

      budgetEditVisible: false,
      dialogTitle: '',
      budget: 0,

      characterShow: {},
      characterInfoVisible: false,

      originalReleaseTime: '',
      releaseTimeShowFlag: false,

      timeStamp: '',
      random: 0,

      CHARACTER_STATUS : CHARACTER_STATUS,
      PROJECT_TYPE : PROJECT_TYPE,
      RD_TYPE : RD_TYPE,

      pickerOptions: {
        disabledDate(v) {
          return v.getTime() < new Date().getTime() - 86400000;//  - 86400000是否包括当天
        }
      },
    };
  },

  mounted: function() {
    let _this =this;
    _this.getLoginUser();
   _this.listIpmtByCustomerId();
    _this.listProductManagerByCustomerId();
    _this.listProjectManagerByCustomerId();
    _this.listSystemManagerByCustomerId();
    _this.listAllWorker();
    _this.getProjectInfo(_this.projectId);
    _this.getSubprojectListByProjectId(_this.projectId);
    _this.timeStamp = Date.getTime;
  },

  methods:{
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

    listSystemManagerByCustomerId() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/listSystemManagerByCustomerId/'+ _this.loginCustomer.id).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.systemManagers = resp.content;
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    getProjectInfo(projectId){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/getProjectInfo/' + projectId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.project = resp.content;
          //将原始的团队保存下来
          _this.originTeam.originProjectManagerId = _this.project.projectManagerId;
          _this.originTeam.originProductManagerId = _this.project.productManagerId;
          _this.originTeam.originSystemManagerId = _this.project.systemManagerId;
          _this.originTeam.originTestManagerId = _this.project.testManagerId;
          _this.originTeam.originIpmtId = _this.project.supervise;
          if(_this.project.keyMemberList){
            _this.value = _this.project.keyMemberList;
          }
          _this.getReleaseTime(_this.project.milestoneList);
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },


    listIpmtByCustomerId() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/listIpmtByCustomerId/'+ _this.loginCustomer.id).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.supervises = resp.content;
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    listProductManagerByCustomerId() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/listProductManagerByCustomerId/'+ _this.loginCustomer.id).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.productManagers = resp.content;
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    listProjectManagerByCustomerId() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/listProjectManagerByCustomerId/'+ _this.loginCustomer.id).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.projectManagers = resp.content;
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    getSubprojectListByProjectId(projectId) {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/getSubprojectListByProjectId/' + projectId).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.subprojectList = resp.content;
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    listAllWorker() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/worker/listAllWorkers/'+ _this.loginCustomer.id).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.workers = resp.content;
          _this.workers.forEach((worker ) => {
            if(worker !== null){
              _this.workerList.push({
                label: worker.trueName,
                key: worker.id
              });
            }
          });
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    getReleaseTime(milestones){
      let _this = this;
      _this.project.milestoneList =milestones; //update milestones
      if(milestones){
        let milestone =  milestones.find(item => (item.isRelease === "1" && item.deleted === "0"));
        if(milestone){
          _this.project.releaseTime = milestone.reviewTime;
          _this.originalReleaseTime = milestone.reviewTime;
          _this.releaseTimeShowFlag = true;
        }
      }
    },

    onDateChange(){
      let _this =this;
      _this.timeStamp = Date.getTime;
      //至少要大于最远的里程碑日期
      let releaseTime = moment(_this.project.releaseTime);
      let original = moment(_this.originalReleaseTime);
      // debugger
      if(releaseTime < original){
        _this.project.releaseTime = _this.originalReleaseTime;
        Swal.fire({
          title: '发布时间错误',
          text: '发布时间不能早于发布里程碑时间,您可以修改发布里程碑时间',
          showCancelButton: false,
          confirmButtonColor: '#409EFF',
          cancelButtonColor: '#909399',
          confirmButtonText: '确认!',
          cancelButtonText: '取消!'
        }).then((result) => {
          console.log(result);
        })
      }
    },

    /**
     * 修改 确认
     */
    save() {
      let _this = this;
      _this.project.keyMemberList = _this.value;

      // 保存校验
      if (1 !== 1
          || !Validator.require(_this.project.projectName, "项目名称",Swal)
          || !Validator.require(_this.project.projectIntroduce, "项目简介",Swal)
          || !Validator.require(_this.project.supervise, "项目监管人",Swal)
          || !Validator.require(_this.project.productManagerId, "产品经理",Swal)
          || !Validator.require(_this.project.projectManagerId, "项目经理",Swal)
          || !Validator.require(_this.project.testManagerId, "测试主管",Swal)
          || !Validator.require(_this.project.systemManagerId, "系统工程",Swal)
          // || !Validator.require(_this.project.keyMemberList, "关键成员",Swal)

          || !Validator.length(_this.project.projectName, "项目名称", 2, 50,Swal)
          || !Validator.length(_this.project.projectIntroduce, "项目简介", 5, 500,Swal)
          || !Validator.length(_this.project.supervise, "项目监管人", 8, 8,Swal)
          || !Validator.length(_this.project.productManagerId, "产品经理", 8, 8,Swal)
          || !Validator.length(_this.project.projectManagerId, "项目经理", 8, 8,Swal)
          || !Validator.length(_this.project.testManagerId, "测试主管", 8, 8,Swal)
      ) {
        return;
      }
      //里程碑不能为空
/*      if(_this.project.rdType !== RD_TYPE.MODULE.key && (_this.project.milestoneList ===null || _this.project.milestoneList.length === 0)){
        Swal.fire({
          title: '里程碑不能为空',
          text: '请创建里程碑!',
          showCancelButton: false,
          confirmButtonColor: '#409EFF',
          cancelButtonColor: '#909399',
          confirmButtonText: '确认!',
          cancelButtonText: '取消!'
        }).then((result) => {
          console.log(result);
        })
        return;
      }*/

      //将原始的团队保存下来, 以供后台程序对比
      _this.project.originProjectManagerId = _this.originTeam.originProjectManagerId;
      _this.project.originProductManagerId = _this.originTeam.originProductManagerId;
      _this.project.originSystemManagerId = _this.originTeam.originSystemManagerId;
      _this.project.originTestManagerId = _this.originTeam.originTestManagerId;
      _this.project.originIpmtId = _this.originTeam.originIpmtId;

      Swal.fire({
        title: '确认',
        text: "是否确认修改项目信息?",
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#409EFF',
        cancelButtonColor: '#909399',
        confirmButtonText: '确认!',
        cancelButtonText: '取消!'
      }).then((result) => {
        if (result.isConfirmed) {
          _this.project.customerId = _this.loginCustomer.id;
          _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/edit', _this.project).then((response)=>{
            let resp = response.data;
            if (resp.success) {
              Toast.success("保存成功", Swal);
              _this.close();
              _this.$bus.$emit('refresh-project-setuped-all', true);
            } else {
              Toast.warning(resp.message, Swal);
            }
          })
        }
      }).catch(_ => {
      });

    },

    budgetEdit(row){
      let _this = this;
      _this.random = Math.random();
      _this.subproject = row;
      _this.budgetEditVisible = true;
      _this.dialogTitle = "预算调整";
    },
    closeDialog() {
      let _this = this;
      _this.budgetEditVisible = false;
    },

    handleClose(done) {
      done(); //el-dialog用于关闭对话框的内置函数.
    },

    close(){
      let _this = this;
      _this.closeFunction(_this.index);
    },

  }
};
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd.scss';
.ca-form{
  font-size: 14px;
  margin: 40px 30px 0;
}
.paper-height {
  height: calc(100vh - 200px);
}
</style>
