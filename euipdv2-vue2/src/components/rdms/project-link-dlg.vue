<!--
 * Author: 周朔鹏
 * Date: 2024-03-12
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="dialog-container">
    <div style="display: flex; flex-direction: column; justify-content: center; align-items: center; margin-bottom: 20px;">
      <div style="margin: 10px 0; font-weight: bold; font-size: 15px;">
        当前主项目为: {{ mainProject.projectName }}
      </div>
      <div>
        可并入项目: <small><em>( 可以多选 )</em></small>
      </div>
    </div>
    <div style="margin-bottom: 80px;">
      <el-checkbox-group v-model="checkList">
        <el-checkbox v-for="(file, i) in otherProjects" :label="file.id" :key="i" style="display: block; padding-left: 230px;">{{ file.projectName }}</el-checkbox>
      </el-checkbox-group>
    </div>
    <div class="clearfix form-actions">
      <div style="display: flex; justify-content: center;">
        <button v-on:click="confirm()" class="ca-btn-primary" type="button">
          确认
        </button>
        <button v-on:click="close()" class="ca-btn-primary" type="button">
          关闭
        </button>
      </div>
    </div>
  </div>
</template>
<script>
import Swal from "sweetalert2";
import FileRdms from "@/components/rdms/file-rdms";
import FileUploadCore from "@/components/std/file-upload-core.vue";
import FileSaver from 'file-saver';


export default {
  name: 'project-link-dlg',

  components: {
    FileRdms,
    FileUploadCore,
  },

  props:{
    projectId: {
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

      demand: {},

      projectList: [],
      checkList: [],

      otherProjects: [],
      mainProject: {},
    };
  },

  computed: {
  },

  mounted: function() {
    let _this =this;
    _this.getLoginUser();
    if(Tool.isNotEmpty(_this.projectId)){
      _this.getCompleteProjectListByCustomerId();
    }
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

    getCompleteProjectListByCustomerId() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/getCompleteProjectListByCustomerId/'+ _this.loginCustomer.id).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.projectList = resp.content;
          if(_this.projectList && _this.projectList.length>0){
            _this.projectList.forEach(item => {
              if(item && item.id === _this.projectId){
                _this.mainProject= item;
                if(_this.mainProject.joinedProjectIdsStr){
                  _this.checkList = JSON.parse(_this.mainProject.joinedProjectIdsStr);
                }
              }
            })
            _this.otherProjects = _this.projectList.filter(project => project.id !== _this.projectId);
          }
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    /**
     * 提交需求
     */
    confirm() {
      let _this = this;
      _this.mainProject.joinProjectIdList = _this.checkList;
      let text = `是否确认并入操作 ？`;
      Swal.fire({
        title: '确认？',
        text: text,
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#409EFF',
        cancelButtonColor: '#909399',
        confirmButtonText: '确认!',
        cancelButtonText: '取消!'
      }).then((result) => {
        if (result.value) {
          _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/joinToMainProject', _this.mainProject).then((response)=>{
            let resp = response.data;
            if (resp.success) {
              Toast.success("保存成功", Swal);
              _this.close();
            } else {
              Toast.warning(resp.message, Swal);
            }
          })
        }
      })
    },

    close(){
      let _this = this;
      _this.closeFunction();
    },

  }
};
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd.scss';

</style>

