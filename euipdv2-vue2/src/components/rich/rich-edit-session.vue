<!--
  内容文档编辑会话（实现方案第 5.4 节）：进入时申请编辑锁并每 5 分钟续期，
  每 60 秒及失焦时保存草稿，离开时释放锁。保存时由父组件读取 baseVersionId。
-->
<template>
  <div class="rich-session" v-if="characterId">
    <span v-if="view">{{ view.editTarget || '主线' }} {{ view.fullLabel || view.contentLabel || '' }}</span>
    <span v-if="lockText" class="rich-session__lock">{{ lockText }}</span>
    <span v-if="draftTime" class="rich-session__draft">草稿 {{ draftTime }}</span>
    <el-button v-if="lockedByOther" type="text" size="mini" @click="forceRelease">解除他人的锁</el-button>
    <el-button type="text" size="mini" @click="versionVisible = true">版本历史</el-button>
    <el-button type="text" size="mini" @click="lineageVisible = true">版本演进</el-button>
    <el-dialog title="版本历史" width="32cm" append-to-body :visible.sync="versionVisible">
      <RichVersionPanel v-if="versionVisible" :character-id="characterId" :manage="true"></RichVersionPanel>
    </el-dialog>
    <el-dialog title="版本演进" width="32cm" append-to-body :visible.sync="lineageVisible">
      <RichLineageGraph v-if="lineageVisible" url="/customer/character/rich-doc/get-lineage"
                        :params="{ characterId: characterId }"></RichLineageGraph>
    </el-dialog>
  </div>
</template>

<script>
import Swal from 'sweetalert2'
import { sectionsOf } from './rich-sections'
import RichVersionPanel from './rich-version-panel.vue'
import RichLineageGraph from './rich-lineage-graph.vue'

export default {
  name: 'rich-edit-session',
  components: { RichVersionPanel, RichLineageGraph },
  props: {
    characterId: { default: null },
    branchId: { default: null },
    sections: { type: Object, default: () => ({}) }
  },
  data () {
    return {
      view: null,
      lock: null,
      draftTime: '',
      versionVisible: false,
      lineageVisible: false,
      timer: null,
      heart: null
    }
  },
  computed: {
    lockedByOther () {
      return this.lock && this.lock.held && this.lock.mine === false
    },
    lockText () {
      if (!this.lock || !this.lock.held) return ''
      return this.lock.mine ? '编辑锁：本人' : ('编辑锁：' + (this.lock.userName || '他人'))
    }
  },
  watch: {
    characterId: { immediate: true, handler () { this.open() } },
    branchId () { this.open() }
  },
  mounted () {
    this.timer = setInterval(() => this.saveDraft(), 60000)
    this.heart = setInterval(() => this.lockAction('ACQUIRE'), 5 * 60 * 1000)
    window.addEventListener('blur', this.saveDraft)
  },
  beforeDestroy () {
    clearInterval(this.timer)
    clearInterval(this.heart)
    window.removeEventListener('blur', this.saveDraft)
    this.lockAction('RELEASE')
  },
  methods: {
    post (path, body) {
      return this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/character/rich-doc' + path, body)
    },
    open () {
      if (!this.characterId) return
      this.post('/get', { characterId: this.characterId, branchId: this.branchId || null }).then(response => {
        const resp = response.data
        if (!resp.success) return
        this.view = resp.content || {}
        this.lockAction('ACQUIRE')
        this.post('/draft/get', { characterId: this.characterId, branchId: this.branchId || this.view.branchId || null })
          .then(r => {
            const d = r.data
            if (!d.success || !d.content || !d.content.sections) return
            if (d.content.fresh === false) {
              Toast.warning('草稿基于的版本已变化，未自动恢复，可以对照后手工合入', Swal)
              return
            }
            const fields = {}
            sectionsOf('CHARACTER_DEF').forEach(def => {
              if (d.content.sections[def.code] != null) fields[def.field] = d.content.sections[def.code]
            })
            this.$emit('restore-draft', fields)
            this.draftTime = '已恢复'
          })
      })
    },
    lockAction (action) {
      if (!this.characterId) return
      const branchId = this.branchId || (this.view && this.view.branchId) || null
      this.post('/edit-lock', { characterId: this.characterId, branchId, action }).then(response => {
        const resp = response.data
        if (resp.success) this.lock = resp.content
      })
    },
    forceRelease () {
      this.lockAction('FORCE_RELEASE')
    },
    saveDraft () {
      if (!this.characterId || !this.view) return
      const defs = sectionsOf('CHARACTER_DEF')
      const sections = defs.map(d => ({ code: d.code, html: this.sections[d.field] || '' }))
      const branchId = this.branchId || this.view.branchId || null
      this.post('/draft/save', {
        characterId: this.characterId,
        branchId,
        baseVersionId: this.view.versionId,
        sections
      }).then(response => {
        if (response.data && response.data.success) {
          const t = new Date()
          const p = n => (n < 10 ? '0' : '') + n
          this.draftTime = p(t.getHours()) + ':' + p(t.getMinutes())
        }
      })
    },
    /** 父组件保存前调用，写入打开时的基础版本。 */
    stamp (item) {
      if (!item || !this.view) return item
      item.richBaseVersionId = this.view.versionId
      if (this.view.branchId) item.richBranchId = this.view.branchId
      return item
    }
  }
}
</script>

<style scoped>
.rich-session { margin: 8px 26px 0; color: #555; font-size: 13px; display: flex; flex-wrap: wrap; align-items: center; gap: 8px; }
.rich-session__lock { color: #8a5a00; }
.rich-session__draft { color: #666; }
</style>
