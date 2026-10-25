<!--
  发任务对话框的公共表单。名称、执行人、描述、工时、提交时间和底部按钮在这里。
  关联任务、特性、质量单、附件等差异由调用方用插槽填入。提交仍由调用方处理。
-->
<template>
  <div>
    <div class="dialog-container">
      <el-form
          :model="info"
          label-width="130px"
          :show-message="false"
          class="dialog-width">
        <el-row :style="nameRowStyle">
          <el-col :span="nameSpan">
            <el-form-item label="任务名称" required>
              <el-col :span="nameInnerSpan">
                <el-input v-model="info.jobName"></el-input>
              </el-col>
            </el-form-item>
          </el-col>
          <el-col v-if="executorOnNameRow" :span="executorSpan">
            <el-form-item label="任务执行人" required>
              <el-col :span="24">
                <el-select v-model="info.executorId" filterable placeholder="任务执行人" @change="emitExecutorChange">
                  <el-option v-for="o in executorOptions" :key="o.id" :value="o[executorValueKey]" :label="o.trueName"></el-option>
                </el-select>
              </el-col>
            </el-form-item>
          </el-col>
          <slot name="beside-name"></slot>
        </el-row>

        <el-row v-if="showExecutor && !executorOnNameRow" :style="executorRowStyle">
          <el-col :span="executorSpan">
            <el-form-item label="任务执行人" required>
              <el-col :span="24">
                <el-select v-model="info.executorId" filterable placeholder="任务执行人" @change="emitExecutorChange">
                  <el-option v-for="o in executorOptions" :key="o.id" :value="o[executorValueKey]" :label="o.trueName"></el-option>
                </el-select>
              </el-col>
            </el-form-item>
          </el-col>
          <slot name="beside-executor"></slot>
        </el-row>

        <slot name="chart"></slot>

        <el-row v-if="showDescription">
          <el-col :span="descSpan">
            <el-form-item label="任务描述" required>
              <el-input type="textarea" rows="5" v-model="info.taskDescription"></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <slot name="after-description"></slot>

        <el-row v-if="showManhour" :style="manhourMargin ? 'margin-top: 5px;' : ''">
          <el-col :span="manhourSpan">
            <el-form-item label="任务工时" required>
              <el-col :span="manhourInnerSpan">
                <el-form-item required>
                  <div style="display: flex; flex-direction: row;">
                    <el-input
                        :style="manhourInputWidth ? ('width: ' + manhourInputWidth + ';') : ''"
                        type="number"
                        v-model="info.manhour"
                        @change="onManhourChange"></el-input>
                    <div :style="'width: ' + hourLabelWidth + '; margin-left: 5px;'"> 小时</div>
                    <slot name="manhour-extra"></slot>
                  </div>
                </el-form-item>
              </el-col>
            </el-form-item>
          </el-col>
          <template v-if="timeWithManhour">
            <el-col :span="7">
              <el-form-item :label="submitTimeLabel" required>
                <el-date-picker
                    type="date"
                    placeholder="选择日期"
                    :picker-options="pickerOptions"
                    v-model="info.planSubmissionTime">
                </el-date-picker>
                <div v-if="windowHint" class="issue-window-hint">{{ windowHint }}</div>
              </el-form-item>
            </el-col>
          </template>
        </el-row>

        <el-row v-if="showSubmitTime && !timeWithManhour">
          <slot name="time-prefix"></slot>
          <el-col :span="7">
            <el-form-item :label="submitTimeLabel" required>
              <el-date-picker
                  type="date"
                  placeholder="选择日期"
                  :picker-options="pickerOptions"
                  v-model="info.planSubmissionTime">
              </el-date-picker>
              <div v-if="windowHint" class="issue-window-hint">{{ windowHint }}</div>
            </el-form-item>
          </el-col>
          <slot name="submit-hint"></slot>
        </el-row>

        <slot name="in-form"></slot>
      </el-form>

      <slot name="below-form"></slot>

      <div class="clearfix form-actions">
        <div style="display: flex; justify-content: center;">
          <button
              v-if="!gateSubmit || !overLoad10"
              type="button"
              class="ca-btn-primary"
              :disabled="submitDisabled"
              @click="$emit('submit')">{{ submitText }}</button>
          <button type="button" class="ca-btn-primary" @click="$emit('cancel')">{{ cancelText }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "jobitem-create-form",
  props: {
    info: {
      type: Object,
      required: true,
    },
    executors: {
      type: Array,
      default: function () {
        return [];
      },
    },
    executorValueKey: {
      type: String,
      default: "id",
    },
    nameSpan: {
      type: Number,
      default: 12,
    },
    nameInnerSpan: {
      type: Number,
      default: 22,
    },
    nameRowStyle: {
      type: String,
      default: "",
    },
    executorSpan: {
      type: Number,
      default: 6,
    },
    executorRowStyle: {
      type: String,
      default: "",
    },
    executorOnNameRow: {
      type: Boolean,
      default: false,
    },
    showExecutor: {
      type: Boolean,
      default: true,
    },
    descSpan: {
      type: Number,
      default: 22,
    },
    showDescription: {
      type: Boolean,
      default: true,
    },
    showManhour: {
      type: Boolean,
      default: true,
    },
    manhourSpan: {
      type: Number,
      default: 22,
    },
    manhourInnerSpan: {
      type: Number,
      default: 24,
    },
    manhourMargin: {
      type: Boolean,
      default: true,
    },
    manhourInputWidth: {
      type: String,
      default: "100px",
    },
    hourLabelWidth: {
      type: String,
      default: "70px",
    },
    showSubmitTime: {
      type: Boolean,
      default: true,
    },
    submitTimeLabel: {
      type: String,
      default: "提交时间",
    },
    pickerOptions: {
      type: Object,
      default: null,
    },
    windowHint: {
      type: String,
      default: "",
    },
    timeWithManhour: {
      type: Boolean,
      default: false,
    },
    overLoad10: {
      type: Boolean,
      default: false,
    },
    gateSubmit: {
      type: Boolean,
      default: true,
    },
    submitText: {
      type: String,
      default: "发任务",
    },
    cancelText: {
      type: String,
      default: "取消",
    },
    submitDisabled: {
      type: Boolean,
      default: false,
    },
  },
  computed: {
    executorOptions() {
      return this.executors || [];
    },
  },
  methods: {
    emitExecutorChange(value) {
      this.$emit("executor-change", value);
    },
    onManhourChange(value) {
      this.$emit("manhour-change", value);
    },
  },
}
</script>

<style scoped>
.issue-window-hint {
  font-size: 12px;
  line-height: 1.4;
  color: #909399;
}
</style>
