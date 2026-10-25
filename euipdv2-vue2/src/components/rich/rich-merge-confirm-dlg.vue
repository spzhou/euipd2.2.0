<!--
 * 分支合入确认（实现方案第 8.5.3 节）：展示项目锁定版本的编号变化、评审结论、章节差异、预算变化与受影响工单，
 * 确认后调用 branch/approve-merge；勾选的工单同时更新定义引用。纳入分支的预算变化取纳入版本的申请值。
-->
<template>
  <div class="rich-merge" v-loading="loading">
    <div v-if="preview.branch">
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="特性">{{ preview.characterName }}</el-descriptions-item>
        <el-descriptions-item label="主开发版本">{{ preview.devVersion }}</el-descriptions-item>
        <el-descriptions-item :label="preview.adoption ? '纳入分支' : '修改分支'">{{ preview.branch.name }}</el-descriptions-item>
        <el-descriptions-item label="分支状态">{{ branchStatusText(preview.branch.status) }}</el-descriptions-item>
        <el-descriptions-item label="项目锁定版本">{{ preview.currentLockLabel || '无' }}</el-descriptions-item>
        <el-descriptions-item label="合入后锁定版本">{{ preview.nextLockLabel }}</el-descriptions-item>
        <el-descriptions-item label="评审通过版本">{{ preview.reviewedVersionLabel || '无' }}</el-descriptions-item>
        <el-descriptions-item label="变更评审">
          {{ preview.reviewJobitemName || '无' }}<span v-if="preview.reviewResult">（{{ reviewResultText(preview.reviewResult) }}）</span>
        </el-descriptions-item>
        <el-descriptions-item v-if="preview.budgetBefore !== undefined" label="预算变化" :span="2">
          {{ preview.budgetBefore }} → {{ preview.budgetAfter }}
        </el-descriptions-item>
      </el-descriptions>

      <el-alert v-if="preview.outdated" type="warning" :closable="false" show-icon style="margin-top: 8px;"
                :title="preview.adoption ? '项目锁定版本已变化，应当放弃本纳入分支并重新发起纳入项目。'
                                         : '分支基线已不是当前最新版本，应当先更新基线并重新评审后再合入。'"></el-alert>
      <el-alert v-if="preview.characterStatus === CHARACTER_STATUS.REVIEW.key" type="warning" :closable="false"
                show-icon style="margin-top: 8px;" title="特性评审进行中，不得合入。"></el-alert>

      <div class="rich-merge__title">章节差异（当前锁定版本 → 评审通过版本）</div>
      <div v-if="changedSections.length === 0" class="rich-merge__empty">正文无变化</div>
      <div v-for="s in changedSections" :key="s.code" class="rich-merge__section">
        <div class="rich-merge__section-name">{{ s.name }}</div>
        <div class="rich-merge__diff">
          <div class="rich-merge__side"><div class="rich-merge__side-title">合入前</div><div v-rich-html="s.fromHtml || '（空）'"></div></div>
          <div class="rich-merge__side"><div class="rich-merge__side-title">合入后</div><div v-rich-html="s.toHtml || '（空）'"></div></div>
        </div>
      </div>

      <div class="rich-merge__title">引用旧版本的未完成工单（勾选的工单在合入时更新定义引用）</div>
      <div v-if="!preview.affectedJobitems || preview.affectedJobitems.length === 0" class="rich-merge__empty">无</div>
      <el-checkbox-group v-else v-model="repinJobitemIds">
        <div v-for="j in preview.affectedJobitems" :key="j.id" class="rich-merge__job">
          <el-checkbox :label="j.id">
            {{ j.jobName }}（{{ jobTypeText(j.type) }}，执行人：{{ j.executorName || '-' }}，引用版本：{{ j.pinVersionLabel || '-' }}）
          </el-checkbox>
        </div>
      </el-checkbox-group>

      <div class="rich-merge__title">合入说明</div>
      <el-input type="textarea" :rows="3" maxlength="500" show-word-limit v-model="confirmNote"></el-input>

      <div class="rich-merge__actions">
        <el-button type="primary" :disabled="!canMerge" :loading="submitting" @click="merge">确认合入</el-button>
        <el-button @click="close">取消</el-button>
      </div>
    </div>
  </div>
</template>

<script>
import Swal from 'sweetalert2'

const BRANCH_STATUS_TEXT = {
  OPEN: '编辑中', REVIEWING: '变更评审中', APPROVED: '评审通过、待合入',
  MERGED: '已合入', ABANDONED: '已放弃', OUTDATED: '已过期'
}
const REVIEW_RESULT_TEXT = { PASS: '通过', CONDITIONAL: '有条件通过', SUPPLEMENT: '增补', REJECT: '驳回' }

export default {
  name: 'rich-merge-confirm-dlg',
  props: {
    branchId: { type: String, default: null },
    closeDlg: { type: Function, default: null }
  },
  data () {
    return {
      loading: false,
      submitting: false,
      preview: {},
      repinJobitemIds: [],
      confirmNote: '',
      CHARACTER_STATUS: CHARACTER_STATUS
    }
  },
  computed: {
    changedSections () {
      return (this.preview.compare || []).filter(s => s.changed)
    },
    canMerge () {
      const p = this.preview
      return p.branch && p.branch.status === 'APPROVED' && !p.outdated && p.reviewedVersionId
        && p.characterStatus !== CHARACTER_STATUS.REVIEW.key
    }
  },
  mounted () {
    this.load()
  },
  methods: {
    load () {
      if (!this.branchId) return
      this.loading = true
      this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/rich-doc/get-merge-preview', { branchId: this.branchId })
        .then(response => {
          const resp = response.data
          if (resp.success) {
            this.preview = resp.content || {}
            this.repinJobitemIds = (this.preview.affectedJobitems || []).map(j => j.id)
          } else {
            Toast.warning(resp.message, Swal)
          }
        })
        .finally(() => { this.loading = false })
    },
    merge () {
      if (!this.canMerge) return
      this.submitting = true
      this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/rich-doc/branch/approve-merge', {
        branchId: this.branchId,
        reviewedVersionId: this.preview.reviewedVersionId,
        confirmNote: this.confirmNote,
        repinJobitemIds: this.repinJobitemIds
      }).then(response => {
        const resp = response.data
        if (resp.success) {
          Toast.success('已合入，项目锁定版本 ' + (this.preview.nextLockLabel || ''), Swal)
          this.$emit('merged', resp.content)
          this.close()
        } else {
          Toast.warning(resp.message, Swal)
        }
      }).finally(() => { this.submitting = false })
    },
    close () {
      if (this.closeDlg) this.closeDlg()
    },
    branchStatusText (s) {
      return BRANCH_STATUS_TEXT[s] || s
    },
    reviewResultText (r) {
      return REVIEW_RESULT_TEXT[r] || r
    },
    jobTypeText (t) {
      return (JOB_ITEM_TYPE[t] && JOB_ITEM_TYPE[t].value) || t
    }
  }
}
</script>

<style scoped>
.rich-merge { text-align: left; }
.rich-merge__title { margin: 14px 0 6px; font-weight: 600; }
.rich-merge__empty { color: #999; }
.rich-merge__section { margin-bottom: 10px; border: 1px solid #eee; border-radius: 4px; }
.rich-merge__section-name { padding: 4px 8px; background: #fafafa; border-bottom: 1px solid #eee; font-weight: 600; }
.rich-merge__diff { display: flex; }
.rich-merge__side { flex: 1; padding: 6px 8px; min-width: 0; overflow-x: auto; }
.rich-merge__side + .rich-merge__side { border-left: 1px solid #eee; }
.rich-merge__side-title { color: #888; font-size: 12px; margin-bottom: 4px; }
.rich-merge__job { margin: 4px 0; }
.rich-merge__actions { margin-top: 16px; text-align: center; }
</style>
