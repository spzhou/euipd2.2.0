<!--
 * Author: 周朔鹏
 * Date: 2024-12-07
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div style="height: 100%;">
    <div class="paper-height">
      <div style="height: 100%; overflow: scroll;">
        <div class="a4-paper-big">
          <div v-if="jobItem.rdType !== RD_TYPE.RESEARCH.key && jobType === JOB_ITEM_TYPE.DECOMPOSE.key" style="width: 100%; margin: 30px 0 -30px;">
            <div style="margin-left: 50px; margin-bottom: 15px;">预算分解表: </div>
            <div style="width: 90%; margin: 0 auto 50px;">
              <el-table
                  ref="decomposeTable"
                  :data="decomposeBudgetList"
                  border
                  stripe
                  style="width: 100%"
                  highlight-current-row
                  :summary-method="getSummaries"
                  show-summary
                  row-key="id">
                <el-table-column   label="序号" align="center" width="50">
                  <template slot-scope="scope">
                    {{scope.$index+1}}
                  </template>
                </el-table-column>
                <el-table-column
                    prop="characterName"
                    show-overflow-tooltip
                    label="特性名称">
                </el-table-column>
                <el-table-column
                    prop="devManhourApproved"
                    label="开发工时"
                    align="center"
                    show-overflow-tooltip
                    width="90">
                </el-table-column>
                <el-table-column
                    prop="testManhourApproved"
                    label="测试工时"
                    align="center"
                    show-overflow-tooltip
                    width="90">
                </el-table-column>
                <el-table-column
                    prop="materialFeeApproved"
                    label="物料费"
                    align="center"
                    show-overflow-tooltip
                    width="90">
                </el-table-column>
                <el-table-column
                    prop="sumOtherFee"
                    label="其他杂费"
                    align="center"
                    show-overflow-tooltip
                    width="90">
                </el-table-column>
              </el-table>
            </div>
            <div class="hr hr-18 dotted hr-double"  ></div>
          </div>
          <JobitemDetailInfoCore
              :key="jobitemId"
              :jobitem-id="jobitemId"
          ></JobitemDetailInfoCore>
        </div>
      </div>
    </div>
    <div class="hr hr-5 dotted"></div>
    <div id="approve-handle" style="height: 150px;">
      <el-row style="height: 100%; overflow: scroll; overflow-x: hidden">
        <el-row>
          <el-col :offset="2">
            <div style="display: flex; flex-direction: row;">
              <span style="margin-right: 15px; margin-bottom: 10px; font-size: 14px;">工作评价:</span>
              <el-rate
                  v-model="evaluateScore"
                  :max=10
                  :texts="['1分','2分','3分','4分','5分','6分','7分','8分','9分','10分']"
                  show-text>
              </el-rate>
            </div>
          </el-col>
        </el-row>
        <el-row style="margin-bottom: 5px;">
          <el-col :offset="2">
            <el-form>
              <el-form-item prop="desc">
                <span slot="label">{{ "评价意见:" }}</span>
                <el-col :span="18">
                  <el-input type="textarea" rows="2" v-model="jobItem.evaluateDesc"></el-input>
                </el-col>
              </el-form-item>
            </el-form>
          </el-col>
        </el-row>

        <div class="hr hr-9 dotted hr-double"></div>
        <div>
          <el-row>
            <el-col>
              <div style="display: flex; justify-content: center;">
                <el-tooltip content="对任务执行人的工作作业评价非常重要, 将直接影响工作执行人的工作业绩!" placement="top" effect="light" :open-delay=1000>
                  <div>
                    <el-button type="primary" @click="saveEvaluate">作业评价</el-button>
                  </div>

                </el-tooltip>
              </div>
            </el-col>
          </el-row>
        </div>
      </el-row>
    </div>

    <!--    预算核定对话框-->
    <el-dialog
        :visible.sync="dialogVisible"
        width="23cm"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ ((dialogTitle) + ' : ' + (characterItem.characterName)) }}</div>
      <character-budget-confirm-dlg
          :key="characterItem.id"
          :character-item="characterItem"
          :close-function="closeDialog"
      ></character-budget-confirm-dlg>
    </el-dialog>
  </div>
</template>

<script>
import FileRdms from "@/components/rdms/file-rdms";
import FileSaver from 'file-saver';
import Swal from "sweetalert2";
import printJS from "print-js";
import JobitemDetailInfoCore from "@/components/std/jobitem-detail-info-core.vue";
import {rmbFormat} from "@/filter/filter";
import CharacterBudgetConfirmDlg from "@/components/rdms/character-budget-confirm-dlg";


export default {
  components: {
    FileRdms,
    FileSaver,
    JobitemDetailInfoCore,
    CharacterBudgetConfirmDlg,

  },
  name: "jobitem-detail-evaluate-page",

  props: {
    jobitemId: {
      default: null,
    },
    projectType: {
      default: null,
    },
    jobType: {
      default: null,
    },
    index: {
      default: null,
    },
    entrance: {
      default: null,
    },
  },

  data() {
    return {
      loginUser: {},
      loginCustomer: {},

      PROJECT_TYPE: PROJECT_TYPE,
      JOB_ITEM_TYPE: JOB_ITEM_TYPE,
      RD_TYPE: RD_TYPE,

      jobItem: {},
      evaluateScore: 9,

      fileItems: [],
      characterItem: {},
      attachCharacter: {},

      dialogVisible: false,
      decomposeBudgetList: [],
      characterShow: {},
      standardManhour: {},
      budget: 0,
      dialogTitle: '',
      originBudget: 0,

      resFile: {},
      resFiles: [],
      resFileIds: [],

      characterProcess: {},
      characterBudget: {},
      random: 0,

      printData: {
        printable: 'examine-print',
        type: 'html',
        // header: this.characterItem.preProjectName,
        // headerStyle: 'font-weight: 600; font-size: 30px;',
        ignore: ['no-print'],
        targetStyles: ['*'],  //css样式，写成*代表打印样式完全继承你页面的样式
        // maxWidth:'800px'  //打印界面最大宽度，适当调整可以解决打印页面过宽，显示不完整的问题
      },
    };
  },

  computed: {},

  mounted: function () {
    let _this = this;
    _this.random = Math.random();
    _this.getLoginUser();
    _this.getJobitemDetailInfo(_this.jobitemId);
    _this.getStandardManhour();
  },

  beforeDestroy() {
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

    getJobitemDetailInfo(jobitemId){
      let _this = this;
      let formData = new window.FormData();
      formData.append('jobitemId', jobitemId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/getJobitemDetailInfo', formData).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.jobItem = resp.content;
          if(_this.projectType === PROJECT_TYPE.DEV_PROJECT.key && _this.jobType === JOB_ITEM_TYPE.DECOMPOSE.key){
            _this.getBudget(_this.jobItem.characterId);
          }
        }else{
          Toast.warning(resp.message, Swal);
        }
      });
    },

    saveEvaluate() {
      let _this = this;

      if (1 !== 1
          || !Validator.require(_this.evaluateScore, "工作评分", Swal)
          || !Validator.require(_this.jobItem.evaluateDesc, "评级意见", Swal)

          || !Validator.isNumber(_this.evaluateScore, "工作评分",  Swal)
          || !Validator.length(_this.jobItem.evaluateDesc, "评级意见",2, 1000,  Swal)
      ) {
        return;
      }

      _this.jobItem.evaluateScore = _this.evaluateScore;
      _this.jobItem.loginUserId = _this.loginUser.customerUserId;
      _this.jobItem.projectType = _this.projectType;

      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/job-items/saveEvaluate', _this.jobItem).then((response) => {
        let resp = response.data;
        if (resp.success) {
          Toast.success("保存成功", Swal);
          _this.$bus.$emit('notify-character-evaluate-job-flag', true);

          if(_this.projectType === PROJECT_TYPE.PRE_PROJECT.key){
            _this.$bus.$emit('refresh-character-joblist-tab-num', true);
            _this.$bus.$emit('refresh-character-joblist-evaluate', true);
            _this.$bus.$emit('refresh-character-joblist-editing', true);
            _this.$bus.$emit('refresh-character-joblist-examine', true);
            _this.$bus.$emit('refresh-character-joblist-approved', true);
            _this.$bus.$emit('refresh-preproject-function-tab-num', true);
          }

          if(_this.projectType === PROJECT_TYPE.DEV_PROJECT.key){
            _this.$bus.$emit('refresh-system-handling-container-subproject', true);
            _this.$bus.$emit('refresh-system-examine-container-subproject', true);
            _this.$bus.$emit('refresh-system-evaluate-container-subproject', true);
            _this.$bus.$emit('refresh-system-joblist-tab-num', true);
            _this.$bus.$emit('refresh-project-list', true);

            _this.$bus.$emit('notify-project-job-flag', true);   //项目管理
            _this.$bus.$emit('notify-jobitem-count-flag',true);   //执行管理
          }

        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    getBudget(characterId){
      let _this = this;
      if(_this.jobItem.type === JOB_ITEM_TYPE.ITERATION.key){
        _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/budget/getCharacterBudget_iteration/' + characterId).then((response) => {
          let resp = response.data;
          if (resp.success) {
            _this.decomposeBudgetList.push(resp.content);
            _this.originBudget = resp.content.budget;
          }else{
            Toast.warning(resp.message, Swal);
          }
        });
      }else if(_this.jobItem.type === JOB_ITEM_TYPE.DECOMPOSE.key){
        _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/budget/getCharacterBudget_decompose/' + characterId).then((response) => {
          let resp = response.data;
          if (resp.success) {
            _this.decomposeBudgetList = resp.content.decomposeCharacterBudgets;
            _this.originBudget = resp.content.budget;
          }else{
            Toast.warning(resp.message, Swal);
          }
        });
      }

    },

    confirmManhour(index, row){
      let _this = this;
      // console.log("index:",index,"row:",row);
      _this.dialogVisible = true;
      _this.dialogTitle = "预算核定";
      _this.characterItem = row;
    },

    getStandardManhour(){
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/manhour/get-standard/' + _this.loginCustomer.id).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.standardManhour = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    //统计表格的汇总信息
    getSummaries(param) {
      let _this = this;
      const {columns, data} = param;
      console.log(data);
      const sums = [];
      const sums_number = [];
      columns.forEach((column, index) => {
        if ( index === 6 ) {
          // if (index === 0) {
          sums[index] = '';
          return;
        }
        if (index === 0) {
          sums[index] = '总计';
          return;
        }
        // const values = data.map(item => Number(item[column.property]));  //关键程序行
        const values = _this.decomposeBudgetList.map(item => Number(item[column.property]));  //关键程序行
        // const values = data.map(item => item[column.property]);

        if (!values.every(value => isNaN(value))) {
          sums[index] = values.reduce((prev, curr) => {
            const value = Number(curr);
            if (!isNaN(value)) {
              return prev + curr;
            } else {
              return prev;
            }
          }, 0);

          sums_number[index] = sums[index];

          if(index >= 2 && index <= 3){
            sums[index] += ' 小时';
          }else{
            sums[index] += ' 元';
          }
        } else {
          sums[index] = 'N/A';
        }
      });
      //计算总工时预算金额
      sums[1] = sums_number[2]* _this.standardManhour.devManhourFee
          + sums_number[3] * _this.standardManhour.testManhourFee
          + sums_number[4] + sums_number[5];
      _this.budget = sums[2];
      sums[1] = rmbFormat(sums[1]) + '元 ( 原总预算 : ' + rmbFormat( _this.originBudget) + ' 元)';
      // sums[2] = '总预算: ¥ ' + sums[2] + ' 元'
      return sums;
    },

    handlePrint(params) {
      printJS({
        printable: params.printable, // 'printFrom', // 标签元素id
        type: params.type || 'html',
        header: params.header, // '表单',
        targetStyles: ['*'],
        style: '@page {margin:20 10mm};', // 可选-打印时去掉眉页眉尾
        ignoreElements: params.ignore || [], // ['no-print']
        properties: params.properties || null
      })
    },

    listAttachment(characterId) {
      let _this = this;
      let formData = new window.FormData();
      formData.append('characterId', characterId);
      formData.append('loginUserId', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/file/rdms/getFileListByCharacterId', formData).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.fileItems = resp.content;
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    downloadAttachment(item) {
      //根据相对路径得到文件的绝对路径
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/file/rdms/getFileAbsPath', {
        id: item.id,
        customerId: _this.loginCustomer.id,
      }).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.fileDto = resp.content;
          _this.downloadFile(_this.fileDto);
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    downloadFile(file) {
      let _this = this;
      _this.getSignedUrl(file.path, file.suffix);
    },
    getSignedUrl (fileFullName, suffix) {
      let _this = this;
      let formData = new window.FormData();
      formData.append('fileFullName', fileFullName);
      formData.append('customer', _this.loginCustomer.id);
      formData.append('user', _this.loginUser.customerUserId);
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/file/rdms/oss-get-signed-url', formData).then((response)=>{
        let resp = response.data;
        Tool.openOssSignedUrl(resp, suffix);
        // FileSaver.saveAs(resp.content, fileName);
      });
    },

    handleClose(done) {
      let _this = this;
      _this.dialogVisible = false;
      done();
    },

    closeDialog() {
      let _this = this;
      _this.dialogVisible = false;
    },
  },
}
</script>

<style lang="scss" scoped>
@import './../../../public/static/scss/caipd';

.paper-height {
  height: calc(100vh - 430px);
}

/*.a4-paper-big {
  background: white;
  width: 26.3cm;
  min-height: 29.7cm;
  display: block;
  margin: 20px auto;
  padding: 10px 25px;
  margin-bottom: 0.5cm;
  box-shadow: 0 0 0.5cm rgba(0, 0, 0, 0.5);
  overflow-y: scroll;
  box-sizing: border-box;
}*/
</style>
