<!--
 * Author: 周朔鹏
 * Date: 2024-12-08
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="paper-height">
    <div style="height: 100%; overflow: scroll;">
      <div class="a4-paper-big-simple">
        <div style="margin: 50px 30px 0">
          <el-form
              :model="projectInfo"
              ref="preproject"
              label-width="130px"
              :show-message=false>
            <el-row>
              <el-col :span="11">
                <el-form-item label="预立项项目名称" required>
                  <el-col :span="23">
                    <el-input v-text="projectInfo.preProjectName"></el-input>
                  </el-col>
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="开发版本">
                  <el-input v-text="projectInfo.devVersion" placeholder="01.01.0001"></el-input>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row>
              <el-col :span="23">
                <el-form-item label="项目简介" required>
                  <el-input type="textarea" rows="5" v-text="projectInfo.preProjectIntroduce" placeholder="项目简介"></el-input>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row style="margin-top: 5px;">
              <el-col :span="7">
                <el-form-item label="产品经理" required>
                  <el-col :span="24">
                    {{ projectInfo.productManagerName}}
                  </el-col>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import FileRdms from "@/components/rdms/file-rdms";
import Swal from "sweetalert2";

export default {
  components: {
    FileRdms,
  },
  name: "preproject-execute-info-page",

  data: function () {
    return {
      loginUser: {},
      loginCustomer: {},

      projectInfo: {},

      PROJECT_TYPE: PROJECT_TYPE,
    }
  },

  props: {
    preProjectId: {
      default: null,
    },
    projectType: {
      default: null
    },
    entrance: {
      default: null,
    }
  },

  computed: {},

  mounted: function () {
    let _this = this;
    if(Tool.isNotEmpty(_this.projectType) && Tool.isNotEmpty(_this.preProjectId)){
      if(_this.projectType === PROJECT_TYPE.PRE_PROJECT.key){
        _this.getPreProjectSimpleInfo(_this.preProjectId);
      }
    }
  },

  methods: {
    getPreProjectSimpleInfo(preProjectId){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/pre-project/getPreProjectSimpleInfo/' + preProjectId).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.projectInfo = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },
  }
}
</script>
<style>
.line-content{
  padding-top: 3px;
}
</style>

<style lang="scss">
@import './../../../public/static/scss/caipd.scss';
.paper-height {
  height: calc(100vh - 240px);
}

.ca-form{
  font-size: 14px;
  .ca-form-item{
    display: flex;
    flex-direction: row;
    justify-content: left;
    line-height: 25px;
    .ca-form-label{
      width: 100px;
      padding-right: 10px;
      display: flex;
      justify-content: right;
    }
    .ca-form-content{
      width: 820px;
      text-align: justify;
    }
  }
}
</style>
