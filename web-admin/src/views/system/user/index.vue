<template>
  <div class="page-container">
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button type="success" :icon="Plus" @click="handleAdd">新增</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="userId" label="ID" width="70" />
      <el-table-column prop="username" label="用户名" min-width="120" />
      <el-table-column prop="realName" label="真实姓名" min-width="120" />
      <el-table-column prop="mobile" label="手机号" min-width="130" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" min-width="160" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <div class="action-btns">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-dropdown @command="(cmd) => handleDropdown(cmd, row)">
              <el-button type="primary" link>
                更多<el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="role">分配角色</el-dropdown-item>
                  <el-dropdown-item command="resetPwd">重置密码</el-dropdown-item>
                  <el-dropdown-item command="delete" divided>删除</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrap">
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchData"
        @current-change="fetchData"
      />
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑用户' : '新增用户'" width="500px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="isEdit" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item v-if="!isEdit" label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.mobile" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item v-if="!isEdit" label="分配角色">
          <el-checkbox-group v-model="form.roleIds">
            <el-checkbox v-for="role in allRoles" :key="role.roleId" :value="role.roleId">
              {{ role.roleName }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 分配角色弹窗 -->
    <el-dialog v-model="roleDialogVisible" title="分配角色" width="400px">
      <el-checkbox-group v-model="selectedRoleIds">
        <el-checkbox v-for="role in allRoles" :key="role.roleId" :value="role.roleId">
          {{ role.roleName }}（{{ role.roleCode }}）
        </el-checkbox>
      </el-checkbox-group>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleRoleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, ArrowDown } from '@element-plus/icons-vue'
import { listUsers, createUser, updateUser, deleteUser, getUserRoles, assignUserRoles, resetPassword } from '@/api/user'
import { listAllRoles } from '@/api/role'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const roleDialogVisible = ref(false)
const formRef = ref()
const isEdit = ref(false)
const editId = ref(null)

const searchForm = reactive({ username: '' })
const pagination = reactive({ current: 1, size: 10, total: 0 })

const form = reactive({
  username: '',
  password: '',
  realName: '',
  mobile: '',
  status: 1,
  roleIds: []
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }]
}

const allRoles = ref([])
const selectedRoleIds = ref([])
const roleUserId = ref(null)

const fetchData = async () => {
  loading.value = true
  try {
    const params = { current: pagination.current, size: pagination.size, ...searchForm }
    const res = await listUsers(params)
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchData()
}

const resetForm = () => {
  formRef.value?.resetFields()
  Object.assign(form, { username: '', password: '', realName: '', mobile: '', status: 1, roleIds: [] })
  isEdit.value = false
  editId.value = null
}

const handleAdd = async () => {
  resetForm()
  // 加载全部角色供选择
  try {
    const res = await listAllRoles()
    allRoles.value = res.data
  } catch (e) {
    console.error(e)
  }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  resetForm()
  isEdit.value = true
  editId.value = row.userId
  Object.assign(form, { username: row.username, realName: row.realName, mobile: row.mobile, status: row.status })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    if (isEdit.value) {
      await updateUser(editId.value, form)
      ElMessage.success('更新成功')
    } else {
      await createUser(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    console.error(e)
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定删除该用户吗？', '提示', { type: 'warning' }).then(async () => {
    await deleteUser(row.userId)
    ElMessage.success('删除成功')
    fetchData()
  }).catch(() => {})
}

const handleDropdown = (command, row) => {
  switch (command) {
    case 'role':
      handleRole(row)
      break
    case 'resetPwd':
      handleResetPwd(row)
      break
    case 'delete':
      handleDelete(row)
      break
  }
}

const handleRole = async (row) => {
  roleUserId.value = row.userId
  try {
    const [rolesRes, userRolesRes] = await Promise.all([
      listAllRoles(),
      getUserRoles(row.userId)
    ])
    allRoles.value = rolesRes.data
    selectedRoleIds.value = userRolesRes.data
    roleDialogVisible.value = true
  } catch (e) {
    console.error(e)
  }
}

const handleRoleSubmit = async () => {
  try {
    await assignUserRoles(roleUserId.value, selectedRoleIds.value)
    ElMessage.success('角色分配成功')
    roleDialogVisible.value = false
  } catch (e) {
    console.error(e)
  }
}

const handleResetPwd = (row) => {
  ElMessageBox.prompt('请输入新密码', '重置密码', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputType: 'password',
    inputValidator: (val) => val && val.length >= 6 ? true : '密码至少6位'
  }).then(async ({ value }) => {
    await resetPassword(row.userId, value)
    ElMessage.success('密码重置成功')
  }).catch(() => {})
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.page-container { padding: 20px; }
.search-bar { margin-bottom: 20px; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 20px; }
.action-btns { display: flex; align-items: center; gap: 8px; }
</style>
