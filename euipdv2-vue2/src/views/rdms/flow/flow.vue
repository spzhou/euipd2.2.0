<!--
 * Author: 周朔鹏
 * Date: 2026-10-06
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->
<template>
  <div style="padding: 12px 16px 24px;">
    <div style="font-size: 18px;">流程配置</div>
    <div style="color: #666; margin: 4px 0 12px;">查看全部既有流程的流程图，绘制并发布本机构的流程定义，或按业务主键查看已启动实例的进度。费用等业务页面仍走原有办理入口。</div>
    <el-tabs v-model="tab">
      <el-tab-pane label="流程图" name="gallery">
        <flow-gallery></flow-gallery>
      </el-tab-pane>
      <el-tab-pane label="设计" name="design" lazy>
        <flow-designer></flow-designer>
      </el-tab-pane>
      <el-tab-pane label="进度" name="progress" lazy>
        <flow-viewer></flow-viewer>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import FlowDesigner from '@/components/rdms/flow/flow-designer.vue';
import FlowGallery from '@/components/rdms/flow/flow-gallery.vue';
import FlowViewer from '@/components/rdms/flow/flow-viewer.vue';

export default {
  name: 'FlowConfigPage',
  components: { FlowDesigner: FlowDesigner, FlowGallery: FlowGallery, FlowViewer: FlowViewer },
  data: function () {
    return { tab: 'gallery' };
  }
};
</script>
