<!--
 * Author: 周朔鹏
 * Date: 2024-06-14
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <div class="form-horizontal">
      <div class="form-group">
        <div style="display: flex; justify-content: center;">
          <div class="col-sm-9" style="margin-left: -30px;">
            <div style=" font-size: 15px; margin-bottom: 15px;">项目里程碑</div>
            <el-button size="small" type="primary" @click="createMilestoneBtn()" class="fr">添加里程碑</el-button>
          </div>
        </div>
      </div>
      <div class="form-group">
        <div style="display: flex; justify-content: center;">
          <div class="col-sm-9" style="margin-left: -30px;">
            <table id="simple-table-worker" class="table table-bordered table-hover">
              <thead>
              <tr>
                <th class="col-sm-1">里程碑ID</th>
                <th class="col-sm-3">里程碑名称</th>
                <th class="col-sm-1">评审时间</th>
                <th class="col-sm-1" style="text-align: center;">操作</th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="milestone in milestoneList" :key="milestone.id">
                <td>{{milestone.id}}</td>
                <td>{{milestone.milestoneName}}</td>
                <td>{{milestone.reviewTimeStr}}</td>
                <td>
                  <div style="display: flex; flex-direction: row; justify-content: space-evenly; align-items: center;">
                    <div class="hidden-sm hidden-xs btn-group" style="display: flex; justify-content: center;">
                      <button v-on:click="editMilestoneBtn(milestone)" class="ca-btn-primary-sm">
                        <i class="ace-icon fa fa-pencil bigger-120"></i>修改
                      </button>
                    </div>
                    <div v-if="milestone.isRelease === null" class="hidden-sm hidden-xs btn-group" style="display: flex; justify-content: center;">
                      <button v-on:click="deleteMilestone(milestone.id)" class="ca-btn-primary-sm">
                        <i class="ace-icon fa fa-pencil bigger-120"></i>删除
                      </button>
                    </div>
                  </div>
                </td>
              </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>

    <el-dialog
        :title="dialogTitle"
        :visible.sync="dialogFormVisible"
        width="29cm"
        append-to-body
        :modal-append-to-body="true"
        custom-class="project-milestone-dialog"
        :close-on-click-modal="false"
        @opened="ensureDialogOnTop">
      <el-row>
        <el-col :span="21">
          <el-form :model="milestone">
            <el-form-item label="里程碑名称" :label-width="formLabelWidth">
              <el-col :span="8">
                <el-input v-model="milestone.milestoneName" autocomplete="off"></el-input>
              </el-col>
            </el-form-item>
            <el-form-item label="里程碑验收指标" :label-width="formLabelWidth">
              <el-input
                  type="textarea"
                  :rows="3"
                  placeholder="请输入内容"
                  v-model="milestone.checkOut">
              </el-input>
            </el-form-item>
            <el-form-item v-if="dialogFlag !=='edit' && !hasReleaseMilestone" label="里程碑类型" :label-width="formLabelWidth">
                <el-checkbox label="发布里程碑" v-model="milestoneType"></el-checkbox>
            </el-form-item>
            <el-form-item v-if="dialogFlag ==='edit' || !milestoneType" label="评审时间" :label-width="formLabelWidth">
              <el-date-picker
                  v-model="milestone.reviewTime"
                  type="date"
                  placeholder="选择日期"
                  :picker-options="pickerOptions"
                  popper-class="project-milestone-datepicker"
                  :append-to-body="true"
                  @change="onDateChange"
                  @focus="ensureDatePickerOnTop"
              >
              </el-date-picker>
            </el-form-item>
          </el-form>
        </el-col>
      </el-row>
      <div class="clearfix form-actions">
        <div style="display: flex; justify-content: center;">
          <button
              class="ca-btn-primary" type="button"
              @click="saveMilestone()">
            确 定
          </button>
          <button class="ca-btn-primary" type="button"  @click="dialogFormVisible = false">取 消</button>
        </div>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import File from "../../components/rdms/file";
import Swal from "sweetalert2";
import moment from 'moment';

export default {
  components: {
    File,
  },
  name: "project-milestone",

  props:{
    project: {
      default: null,
    },
    milestones: [],
    index: {
      default: null,
    },
    getDbRecord: {
      default: true,
    },
    closeFunction: {
      type: Function,
      default: null
    },
  },

  data: function() {
    return {
      // project: this.projectIn,  //不能这样使用初始化,因为,这样初始化会阻断父组件向本组件传递新参数
      milestone: {},
      milestoneList: [],
      dialogFormVisible: false,
      formLabelWidth: '180px',
      dialogTitle: '添加里程碑',
      dialogFlag: '',
      milestoneType: false,
      hasReleaseMilestone: false,

      pickerOptions: {
        disabledDate(v) {
          return v.getTime() < new Date().getTime() - 86400000;//  - 86400000是否包括当天
        }
      },
    }
  },

  computed: {
  },

  mounted: function() {
    let _this =this;
    _this.milestoneList = _this.milestones;
    if(_this.getDbRecord){
      _this.listMilestone()
    }
  },

  methods: {
    /** Swal 默认 z-index 低于 Element Dialog，需抬到最上层 */
    fireSwalAboveDialog(options) {
      return Swal.fire(Object.assign({
        returnFocus: false,
        didOpen: () => {
          const container = Swal.getContainer();
          if (container) {
            container.style.zIndex = '10000';
          }
          if (document.activeElement && document.activeElement.blur) {
            document.activeElement.blur();
          }
        }
      }, options));
    },
    /** 嵌套在立项 Dialog 内时，保证里程碑 Dialog/遮罩在最上层 */
    ensureDialogOnTop() {
      this.$nextTick(() => {
        const dialogEl = document.querySelector('.project-milestone-dialog');
        const wrapper = dialogEl && dialogEl.closest('.el-dialog__wrapper');
        if (wrapper) {
          wrapper.style.zIndex = '5000';
        }
        const modals = document.querySelectorAll('body > .v-modal');
        if (modals.length) {
          modals[modals.length - 1].style.zIndex = '4999';
        }
      });
    },
    /** 日期面板默认 z-index 低于里程碑 Dialog(5000)，需要抬到更上层 */
    ensureDatePickerOnTop() {
      this.$nextTick(() => {
        const panels = document.querySelectorAll('.project-milestone-datepicker');
        panels.forEach((panel) => {
          panel.style.zIndex = '5100';
        });
      });
    },
    onDateChange(){
      let _this = this;
      let releaseTime = moment(_this.project.releaseTime);
      let reviewTime = moment(_this.milestone.reviewTime);
      if(_this.milestone.isRelease !=="1" && releaseTime < reviewTime){
        _this.fireSwalAboveDialog({
          title: '发布时间',
          text: '请正确填写发布时间,或正确填写里程碑时间',
          showCancelButton: false,
          confirmButtonColor: '#409EFF',
          cancelButtonColor: '#909399',
          confirmButtonText: '确认!',
          cancelButtonText: '取消!'
        }).then(() => {
          _this.milestone.reviewTime = null;
        })
      }
    },
    createMilestoneBtn(){
      let _this = this;
      if (Tool.isEmpty(_this.project.releaseTime)) {
        _this.fireSwalAboveDialog({
          title: '发布时间',
          text: '请正确填写发布时间',
          showCancelButton: false,
          confirmButtonColor: '#409EFF',
          cancelButtonColor: '#909399',
          confirmButtonText: '确认!',
          cancelButtonText: '取消!'
        });
        return;
      }
      _this.milestone = {};
      _this.dialogTitle = "添加里程碑";
      _this.dialogFlag = 'create';
      _this.milestoneType = false;
      _this.milestone.projectName = _this.project.projectName;
      _this.milestone.projectId = _this.project.id;
      _this.$nextTick(() => {
        _this.dialogFormVisible = true;
      });
    },

    editMilestoneBtn(milestone){
      let _this = this;
      _this.dialogTitle = "编辑里程碑";
      _this.dialogFlag = 'edit';
      _this.milestone = milestone;
      _this.milestone.projectName = _this.project.projectName;
      _this.milestone.projectId = _this.project.id;
      _this.$nextTick(() => {
        _this.dialogFormVisible = true;
      });
    },

    deleteMilestone(id) {
      let _this = this;
      _this.fireSwalAboveDialog({
        title: '确认？',
        text: "删除后不可恢复，确认删除？",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#409EFF',
        cancelButtonColor: '#909399',
        confirmButtonText: '确认!',
        cancelButtonText: '取消!'
      }).then((result) => {
        if (result.isConfirmed || result.value) {
          _this.$ajax.delete(process.env.VUE_APP_SERVER + '/customer/milestone/del/' + id).then((response)=>{
            let resp = response.data;
            if (resp.success) {
              _this.listMilestone()
            } else {
              Toast.warning(resp.message, Swal);
            }
          });
        }
      })
    },
    /**
     * 保存里程碑信息
     */
    saveMilestone(){
      let _this = this;
      // 保存校验
      if (1 !== 1
          || !Validator.require(_this.milestone.milestoneName, "里程碑名称",Swal)
          || !Validator.require(_this.milestone.checkOut, "验收指标",Swal)
          || !Validator.require(_this.milestone.projectId, "项目ID",Swal)
          // || !Validator.require(_this.milestone.reviewTime, "评审时间",Swal)

          || !Validator.length(_this.milestone.milestoneName, "里程碑名称", 2, 50,Swal)
          || !Validator.length(_this.milestone.checkOut, "验收指标", 6, 500,Swal)
      ) {
        return;
      }
      if(_this.dialogFlag === 'create' && _this.milestoneType){
        //发布里程碑
        _this.milestone.reviewTime = _this.project.releaseTime;
        _this.milestone.isRelease = "1";
      }
      _this.milestone.releaseTime = _this.project.releaseTime; //如果没有发布历程碑,就需要创建一个发布里程碑,如需这个时间
      if(_this.dialogFlag === 'create'){
        _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/milestone/save', _this.milestone).then((response)=>{
          let resp = response.data;
          if (resp.success) {
            Toast.success("保存成功", Swal);
            _this.listMilestone();
            _this.dialogFormVisible = false; //关闭dialog
            _this.milestone = {};
          } else {
            Toast.warning(resp.message, Swal);
          }
        })
      }else if(_this.dialogFlag === 'edit'){
        _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/milestone/edit', _this.milestone).then((response)=>{
          let resp = response.data;
          if (resp.success) {
            Toast.success("保存成功", Swal);
            _this.listMilestone();
            _this.dialogFormVisible = false; //关闭dialog
            _this.milestone = {};
          } else {
            Toast.warning(resp.message, Swal);
          }
        })
      }
    },

    listMilestone() {
      let _this = this;
      if (!_this.project || !_this.project.id) {
        return;
      }
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/milestone/listByProjectId/'+ _this.project.id).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.milestoneList = resp.content;
          _this.milestoneList.forEach(item => {
            if(item.isRelease === "1"){
              _this.hasReleaseMilestone = true;
            }
          })

          _this.$parent.getReleaseTime(_this.milestoneList);
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    close(){
      let _this = this;
      _this.closeFunction(_this.index);
    },
  }
}
</script>

<style lang="scss">
/* 覆盖 Element 内联 z-index，保证日期面板高于里程碑 Dialog(5000) */
.project-milestone-datepicker {
  z-index: 5100 !important;
}
</style>
