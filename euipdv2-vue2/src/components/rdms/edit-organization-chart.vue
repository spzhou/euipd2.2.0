<!--
 * Author: 周朔鹏
 * Date: 2025-04-25
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
-->

<template>
  <div>
    <el-row :gutter="12">
      <el-col :span="9">
        <el-tree style="width: 100%"
                 :data="departmentTree"
                 node-key="id"
                 :default-expand-all="true"
                 :highlight-current="true"
                 :expand-on-click-node="false"
                 :props="defaultProps"
                 @node-click="nodeClick">
          <span class="custom-tree-node" slot-scope="{ data }">
            <span>
              <span :class="{ 'unassigned-node': data.id === UNASSIGNED }">{{ data.name }}</span>
              <span class="node-meta" v-if="data.managerName">（{{ data.managerName }}）</span>
              <span class="node-meta">{{ data.directCount }}/{{ data.totalCount }}人</span>
            </span>
            <span v-if="data.id !== UNASSIGNED" style="margin-right: 10px;">
              <el-button v-if="data.parentId" plain size="mini" @click.stop="editDepartmentDlg(data)">编辑</el-button>
              <el-button plain size="mini" @click.stop="addDepartmentClick(data)">添加部门</el-button>
            </span>
          </span>
        </el-tree>
        <div class="tree-tip">人数为“直属/含下级”，负责人计入所负责部门。</div>
      </el-col>
      <el-col :span="15">
        <div class="table-toolbar">
          <span class="table-title">{{ currentNode ? currentNode.name : '请选择部门' }}</span>
          <el-button v-if="isUnassigned" type="primary" plain size="mini"
                     :disabled="selectedUsers.length === 0"
                     @click="assignDlg = true">归入部门</el-button>
        </div>
        <el-table
            :data="users"
            border
            stripe
            highlight-current-row
            style="width: 100%"
            @selection-change="selectionChange"
            @row-click="rowClick">
          <el-table-column v-if="isUnassigned" type="selection" width="45" align="center"></el-table-column>
          <el-table-column label="序号" align="center" width="50">
            <template slot-scope="scope">
              {{ scope.$index + 1 }}
            </template>
          </el-table-column>
          <el-table-column prop="jobNum" align="center" label="工号" width="100"></el-table-column>
          <el-table-column align="center" label="姓名">
            <template slot-scope="scope">
              {{ scope.row.trueName }}
              <el-tag v-if="currentNode && scope.row.id === currentNode.managerId" size="mini" type="warning">负责人</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="jobLevel" align="center" sortable label="职级"></el-table-column>
          <el-table-column prop="title" align="center" label="职务"></el-table-column>
          <el-table-column prop="jobPosition" align="center" label="岗位名称"></el-table-column>
        </el-table>

        <div v-if="relation" class="relation-panel">
          <div class="relation-title">汇报关系：{{ relation.person?.trueName }}</div>
          <div class="relation-line">
            <span class="relation-label">上级链：</span>
            <span v-if="!relation.superiors || relation.superiors.length === 0">无</span>
            <span v-for="(p, i) in relation.superiors" :key="p.id">
              <span v-if="i > 0"> → </span>{{ p.trueName }}<span class="node-meta">（{{ p.departmentName }}）</span>
            </span>
          </div>
          <div class="relation-line">
            <span class="relation-label">直接下级：</span>
            <span v-if="!relation.directSubordinates || relation.directSubordinates.length === 0">无</span>
            <el-tag v-for="p in relation.directSubordinates" :key="p.id" size="mini" class="relation-tag">
              {{ p.trueName }}
            </el-tag>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 编辑部门-->
    <el-dialog
        :visible.sync="editTeamDlg"
        width="29cm"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="editTeamDlg">
        <EditDepartment
            :key="department?.id"
            :current-department="department"
            :close-function="closeDialog"
            :refresh-list="getDepartments"
        ></EditDepartment>
      </div>
    </el-dialog>

    <!-- 添加部门-->
    <el-dialog
        :visible.sync="addSubDepartmentDlg"
        width="29cm"
        :before-close="handleClose"
        :close-on-click-modal=false>
      <div slot="title" style="font-size: 20px;">{{ dialogTitle }}</div>
      <div v-if="addSubDepartmentDlg">
        <AddDepartment
            :key="department?.id"
            :current-department="department"
            :close-function="closeDialog"
            :refresh-list="getDepartments"
        ></AddDepartment>
      </div>
    </el-dialog>

    <!-- 归入部门-->
    <el-dialog
        title="归入部门"
        :visible.sync="assignDlg"
        width="480px"
        :close-on-click-modal=false>
      <div>已选 {{ selectedUsers.length }} 人，归入：</div>
      <el-select v-model="assignTarget" filterable placeholder="请选择部门" style="width: 100%; margin-top: 10px;">
        <el-option v-for="d in departmentOptions" :key="d.id" :label="d.label" :value="d.id"></el-option>
      </el-select>
      <span slot="footer">
        <el-button size="small" @click="assignDlg = false">取消</el-button>
        <el-button size="small" type="primary" :disabled="!assignTarget" @click="assignMembers">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import Swal from "sweetalert2";
import AddDepartment from "@/components/rdms/add-department.vue";
import EditDepartment from "@/components/rdms/edit-department.vue";
import { Loading } from 'element-ui'

const UNASSIGNED = 'UNASSIGNED';

export default {
  name: "edit-organization-chart",
  components: {
    AddDepartment,
    EditDepartment
  },
  data: function () {
    return {
      UNASSIGNED,
      department: {},
      editTeamDlg: false,
      addSubDepartmentDlg: false,
      assignDlg: false,
      assignTarget: '',
      dialogTitle: '',
      users: [],
      selectedUsers: [],
      currentNode: null,
      relation: null,
      departmentTree: [],
      defaultProps: {
        children: 'children',
        label: 'name',
      },
    };
  },

  computed: {
    isUnassigned() {
      return this.currentNode && this.currentNode.id === UNASSIGNED;
    },
    departmentOptions() {
      const options = [];
      const walk = (nodes, prefix) => {
        (nodes || []).forEach(n => {
          if (n.id === UNASSIGNED) {
            return;
          }
          options.push({ id: n.id, label: prefix + n.name });
          walk(n.children, prefix + n.name + ' / ');
        });
      };
      walk(this.departmentTree, '');
      return options;
    },
  },

  mounted: function () {
    this.getDepartments();
  },

  methods: {
    getDepartments() {
      let _this = this;
      let loadingInstance = Loading.service({ fullscreen: true });
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/department/getOrgTree').then((response) => {
        loadingInstance.close();
        let resp = response.data;
        if (resp.success) {
          _this.departmentTree = resp.content ? [resp.content] : [];
          if (_this.currentNode) {
            const node = _this.findNode(_this.departmentTree, _this.currentNode.id);
            if (node) {
              _this.nodeClick(node);
            } else {
              _this.currentNode = null;
              _this.users = [];
            }
          }
        } else {
          Toast.warning(resp.message, Swal);
        }
      }).catch(() => loadingInstance.close())
    },

    findNode(nodes, id) {
      for (const n of nodes || []) {
        if (n.id === id) {
          return n;
        }
        const found = this.findNode(n.children, id);
        if (found) {
          return found;
        }
      }
      return null;
    },

    nodeClick(data) {
      let _this = this;
      _this.currentNode = data;
      _this.relation = null;
      _this.selectedUsers = [];
      const url = data.id === UNASSIGNED
          ? '/customer/department/listUnassigned'
          : '/customer/department/getDepartmentMemberList/' + data.id;
      let loadingInstance = Loading.service({ fullscreen: true });
      _this.$ajax.post(process.env.VUE_APP_SERVER + url).then((response) => {
        loadingInstance.close();
        let resp = response.data;
        if (resp.success) {
          const members = resp.content || [];
          const manager = members.find(u => u.id === data.managerId);
          const others = members.filter(u => u.id !== data.managerId);
          if (manager) {
            _this.users = [manager].concat(others);
          } else if (data.managerId) {
            _this.users = [{ id: data.managerId, trueName: data.managerName }].concat(others);
          } else {
            _this.users = others;
          }
        } else {
          Toast.warning(resp.message, Swal);
        }
      }).catch(() => loadingInstance.close())
    },

    rowClick(row) {
      let _this = this;
      if (_this.isUnassigned || !row.id) {
        return;
      }
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/department/getSuperior/' + row.id).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.relation = resp.content;
        }
      })
    },

    selectionChange(rows) {
      this.selectedUsers = rows;
    },

    assignMembers() {
      let _this = this;
      _this.$ajax.post(process.env.VUE_APP_SERVER + '/customer/department/assignMembers', {
        id: _this.assignTarget,
        memberIdList: _this.selectedUsers.map(u => u.id),
      }).then((response) => {
        let resp = response.data;
        if (resp.success) {
          _this.assignDlg = false;
          _this.assignTarget = '';
          _this.getDepartments();
          Toast.success("归入成功", Swal);
        } else {
          Toast.warning(resp.message, Swal);
        }
      })
    },

    toDepartmentProps(node) {
      return {
        id: node.id,
        parent: node.parentId,
        label: node.name,
        name: node.code,
        managerId: node.managerId,
        description: node.description,
        fixed: node.fixed,
        memberIdList: node.memberIds || [],
        children: node.children && node.children.length > 0 ? node.children : null,
      };
    },

    editDepartmentDlg(node) {
      this.department = this.toDepartmentProps(node);
      this.editTeamDlg = true;
      this.dialogTitle = "部门编辑";
    },
    addDepartmentClick(node) {
      this.department = this.toDepartmentProps(node);
      this.addSubDepartmentDlg = true;
      this.dialogTitle = "添加子部门";
    },
    handleClose(done) {
      done();
    },
    closeDialog() {
      this.editTeamDlg = false;
      this.addSubDepartmentDlg = false;
    },
  },

};
</script>

<style lang="scss">
.el-tree-node__content {
  display: -webkit-box;
  display: -ms-flexbox;
  display: flex;
  -webkit-box-align: center;
  -ms-flex-align: center;
  align-items: center;
  height: 30px;
  cursor: pointer;
}
</style>

<style lang="scss" scoped>
.custom-tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  padding-right: 8px;
}
.node-meta {
  color: #909399;
  font-size: 12px;
  margin-left: 6px;
}
.unassigned-node {
  color: #E6A23C;
}
.tree-tip {
  color: #909399;
  font-size: 12px;
  margin-top: 8px;
}
.table-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}
.table-title {
  font-weight: 600;
}
.relation-panel {
  margin-top: 12px;
  padding: 10px 12px;
  border: 1px solid #EBEEF5;
  border-radius: 4px;
  font-size: 13px;
}
.relation-title {
  font-weight: 600;
  margin-bottom: 6px;
}
.relation-line {
  line-height: 26px;
}
.relation-label {
  color: #606266;
}
.relation-tag {
  margin-right: 6px;
}
</style>
