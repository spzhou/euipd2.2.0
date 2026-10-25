<!--
 * Author: 周朔鹏
 * Date: 2025-01-14
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="page-wrapper">
    <el-row>
      <div style="width: 100%; height: 50px; background-color: #F5F5F5">
        <div style="line-height: 50px; font-size: 18px; margin-left: 20px;">
          <span style="margin-right: 3px;">姓名:</span><span style="margin-right: 15px;">{{ worker.trueName }}</span><span style="margin-right: 3px;">电话:</span><span>{{ worker.loginName }}</span>
        </div>
      </div>
    </el-row>
    <div class="hr hr-18 dotted hr-double"></div>
    <el-row>
      <el-col :span="8">
        <div style="width: 60%; margin: 50px auto 0; background-color: #F5F5F5;">
          <img :src="avatarSrc" alt="avatar" style="width: 100%;" @error="onAvatarError">
        </div>
      </el-col>
      <el-col :span="16">
        <table class="table">
          <tr class="table-tr">
            <th class="first-th">
              工号
            </th>
            <th class="second-th">
              {{ worker.jobNum }}
            </th>
          </tr>
          <tr class="table-tr">
            <th class="first-th">
              专业
            </th>
            <th class="second-th">
              {{ worker.subject }}
            </th>
          </tr>
          <tr class="table-tr">
            <th class="first-th">
              学历
            </th>
            <th class="second-th">
              {{ worker.educationBackground }}
            </th>
          </tr>
          <tr class="table-tr">
            <th class="first-th">
              学位
            </th>
            <th class="second-th">
              {{ worker.degree }}
            </th>
          </tr>
          <tr class="table-tr">
            <th class="first-th">
              职务
            </th>
            <th class="second-th">
              {{ worker.title }}
            </th>
          </tr>
          <tr class="table-tr">
            <th class="first-th">
              职级
            </th>
            <th class="second-th">
              {{ worker.jobLevel }}
            </th>
          </tr>
          <tr class="table-tr">
            <th class="first-th">
              岗位名称
            </th>
            <th class="second-th">
              {{ worker.jobPosition }}
            </th>
          </tr>
          <tr class="table-tr">
            <th class="first-th">
              办公电话
            </th>
            <th class="second-th">
              {{ worker.officeTel }}
            </th>
          </tr>
          <tr class="table-tr">
            <th class="first-th">
              办公邮件
            </th>
            <th class="second-th">
              {{ worker.officeEmail }}
            </th>
          </tr>
          <tr class="table-tr">
            <th class="first-th">
              加入时间
            </th>
            <th class="second-th">
              {{ worker.joinTime | dateFormat }}
            </th>
          </tr>
          <tr class="table-tr">
            <th class="first-th">
              生日
            </th>
            <th class="second-th">
              {{ worker.birthday | dateFormat }}
            </th>
          </tr>
          <tr class="table-tr">
            <th class="first-th">
              状态
            </th>
            <th class="second-th">
              {{ worker.status }}
            </th>
          </tr>
        </table>
      </el-col>

    </el-row>
    <div class="hr hr-18 dotted hr-double"></div>
    <el-row>
      <el-col>
        <div style="width: 100%; background-color: #F5F5F5; padding: 10px 0 5px;">
          <el-form :label-position="labelPosition" label-width="120px">
            <el-form-item label="能力描述">
              <el-col :span="24">
                <span style="display: block; width: 98%; background-color: white; border-radius: 3px;">
                  <span style=" margin-left: 5px;">
                    {{ worker.description }}
                  </span>
                </span>
              </el-col>
            </el-form-item>
          </el-form>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import FileRdms from "@/components/rdms/file-rdms";
import Swal from "sweetalert2";

export default {
  components: {
    FileRdms,
  },
  name: "profile",

  data: function() {
    return {
      loginUser: {},
      loginCustomer: {},

      labelPosition: 'right',
      worker: {},
      avatarSrc: '/imgs/panda.png',
    }
  },

  props: {
    workerId:{
      default: null,
    }
  },

  computed: {
  },

  mounted: function() {
    let _this = this;
    _this.getLoginUser();
    _this.getWorkerInfo();

  },

  methods: {
    getLoginUser(){
      let _this = this;
      _this.loginUser = Tool.getActorUser();
      _this.loginCustomer = Tool.getLoginCustomer();
    },

    getWorkerInfo() {
      let _this = this;
      // 如果_this.loginUser不为空,并且_this.loginUser.customerUserId不为空,则执行,否则返回
      if (!_this.loginUser || !_this.loginUser.customerUserId) {
        return;
      }
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/worker/get-worker-info/' + _this.loginUser.customerUserId).then((response)=>{
        let resp = response.data;
        if (resp.success) {
          _this.worker = resp.content;
          _this.avatarSrc = _this.resolveAvatarUrl(resp.content && resp.content.profile);
        } else {
          Toast.warning(resp.message,Swal);
        }
      })
    },

    resolveAvatarUrl(profile) {
      const fallback = '/imgs/panda.png';
      if (!profile || typeof profile !== 'string') {
        return fallback;
      }
      const p = profile.trim();
      if (!p || p.includes('defaultProfile')) {
        return fallback;
      }
      if (/aliyuncs\.com/i.test(p)) {
        const key = p.replace(/^https?:\/\/[^/]+\//i, '');
        const webBase = (process.env.VUE_APP_OSS_WEB_BASE || '').replace(/\/$/, '');
        return key && webBase ? `${webBase}/${key}` : fallback;
      }
      if (/^https?:\/\//i.test(p)) {
        return p;
      }
      const webBase = (process.env.VUE_APP_OSS_WEB_BASE || '').replace(/\/$/, '');
      return webBase ? `${webBase}/${p.replace(/^\//, '')}` : fallback;
    },

    onAvatarError() {
      if (this.avatarSrc !== '/imgs/panda.png') {
        this.avatarSrc = '/imgs/panda.png';
      }
    },
  }
}
</script>

<style lang="scss">
@import './../../../../public/static/scss/caipd.scss';

.table{
  width: 100%;
  border-style: solid;
  border-width: 1px 0;
  border-color: #F5F5F5;
}
.table-tr{
  height: 35px;
  border-style: solid;
  border-width: 0 0 1px 0;
  border-color: #F5F5F5;
}
.first-th{
  width: 25%;
  line-height: 30px;
  text-align: center;
  background-color: rgba(245,245,245,0.50);
}
.second-th{
  width: 75%;
  line-height: 30px;
  text-align: center;
}
</style>
