<!--
 * Author: 周朔鹏
 * Date: 2026-10-13
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div class="dialog-container">
    <div style="margin: 0 40px 20px 40px">
      <el-form label-width="110px" size="small">
        <el-form-item label="项目:">
          <span>{{ project.projectName }}</span>
        </el-form-item>
        <el-form-item label="产品配置:">
          <el-select v-model="bomConfigId" style="width: 360px;" placeholder="现行产品配置">
            <el-option value="" :label="bomConfigs.length ? '现行产品配置' : '无产品配置：项目全部现行特性'"></el-option>
            <el-option v-for="c in bomConfigs" :key="c.id" :value="c.id" :label="c.name + (c.isActive === 1 ? '（现行）' : '')"></el-option>
          </el-select>
          <div><small>发布时按所选产品配置生成发布基线，基线生成后不随配置修改而变。</small></div>
        </el-form-item>
        <el-form-item label="发布说明:">
          <el-input v-model="releaseNote" type="textarea" :rows="3" maxlength="500" show-word-limit placeholder="可不填"></el-input>
        </el-form-item>
        <el-form-item label="交付文件:">
          <div v-if="candidateFiles.length === 0"><small>项目暂无文件，可在下方上传交付文件。</small></div>
          <el-checkbox-group v-model="deliveryFileIds">
            <div v-for="f in candidateFiles" :key="f.id">
              <el-checkbox :label="f.id">{{ f.name }}</el-checkbox>
            </div>
          </el-checkbox-group>
          <FileUploadCore
              :input-id="('product-publish-' + project.id)"
              :key="('product-publish-upload-' + project.id)"
              :x-project-id="project.id"
              :item-id="project.id"
              :file-group="FILE_GROUPING.PROJECT.key"
              :project-type="PROJECT_TYPE.PROJECT.key"
              :read-db-record="false"
              v-on:uploadFileList="onUploaded"
          ></FileUploadCore>
        </el-form-item>
      </el-form>
    </div>
    <div class="clearfix form-actions">
      <div style="display: flex; justify-content: center;">
        <button v-on:click="publish()" :disabled="submitting" class="ca-btn-primary" type="button">发布</button>
        <button v-on:click="close()" class="ca-btn-primary" type="button">取消</button>
      </div>
    </div>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import FileUploadCore from "@/components/std/file-upload-core.vue";

/** 发布产品版本：选择产品配置（缺省为现行配置）、填写发布说明、勾选交付文件，发布时生成发布基线。 */
export default {
  name: "product-publish-dlg",
  components: { FileUploadCore },

  props: {
    project: { type: Object, default: () => ({}) },
    closeFunction: { type: Function, default: null },
  },

  data() {
    return {
      bomConfigs: [],
      bomConfigId: '',
      releaseNote: '',
      candidateFiles: [],
      deliveryFileIds: [],
      submitting: false,
      FILE_GROUPING: FILE_GROUPING,
      PROJECT_TYPE: PROJECT_TYPE,
    };
  },

  mounted() {
    this.listBomConfigs();
    this.listProjectFiles();
  },

  methods: {
    listBomConfigs() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/project/getBomConfigListByProjectId/' + _this.project.id).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.bomConfigs = resp.content || [];
          const active = _this.bomConfigs.find(c => c.isActive === 1);
          _this.bomConfigId = active ? active.id : '';
        } else {
          Toast.warning(resp.message, Swal);
        }
      });
    },

    listProjectFiles() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/file/rdms/getFileListByUseAndItemId', {
        use: FILE_GROUPING.PROJECT.key,
        itemId: _this.project.id,
      }).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.candidateFiles = resp.content || [];
        }
      });
    },

    onUploaded(resFiles) {
      let _this = this;
      (resFiles || []).forEach(f => {
        if (!_this.candidateFiles.some(c => c.id === f.id)) {
          _this.candidateFiles.push(f);
        }
        if (_this.deliveryFileIds.indexOf(f.id) < 0) {
          _this.deliveryFileIds.push(f.id);
        }
      });
    },

    publish() {
      let _this = this;
      _this.submitting = true;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/product/publish-from-project', {
        projectId: _this.project.id,
        productName: _this.project.projectName,
        releaseNote: (_this.releaseNote || '').trim(),
        bomConfigId: _this.bomConfigId,
        deliveryFileIdListStr: JSON.stringify(_this.deliveryFileIds),
      }).then((response) => {
        _this.submitting = false;
        let resp = response.data;
        if (resp.success) {
          Toast.success(resp.message || "发布成功!", Swal);
          _this.close(true);
        } else {
          Toast.warning(resp.message, Swal);
        }
      }).catch(() => {
        _this.submitting = false;
      });
    },

    close(done) {
      if (this.closeFunction) {
        this.closeFunction(done === true);
      }
    },
  },
};
</script>
