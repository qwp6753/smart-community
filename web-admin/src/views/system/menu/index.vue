<template>
  <div class="page-container">
    <div class="search-bar">
      <el-button type="success" :icon="Plus" @click="handleAdd">新增</el-button>
    </div>

    <el-table :data="menuTree" border stripe v-loading="loading" row-key="menuId" :tree-props="{ children: 'children' }">
      <el-table-column prop="menuName" label="菜单名称" min-width="180" />
      <el-table-column prop="path" label="路由路径" min-width="180" />
      <el-table-column prop="component" label="组件路径" min-width="180" />
      <el-table-column prop="icon" label="图标" width="100" />
      <el-table-column prop="type" label="类型" width="80">
        <template #default="{ row }">
          <el-tag v-if="row.type === 0" type="primary">目录</el-tag>
          <el-tag v-else-if="row.type === 1" type="success">菜单</el-tag>
          <el-tag v-else type="info">按钮</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="permission" label="权限标识" min-width="160" />
      <el-table-column prop="sort" label="排序" width="70" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑菜单' : '新增菜单'" width="600px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="菜单名称" prop="menuName">
          <el-input v-model="form.menuName" placeholder="请输入菜单名称" />
        </el-form-item>
        <el-form-item label="上级菜单">
          <el-select v-model="form.parentId" placeholder="无（顶级菜单）" clearable style="width: 100%">
            <el-option :value="0" label="无（顶级菜单）" />
            <el-option v-for="item in flatMenus" :key="item.menuId" :value="item.menuId" :label="item.menuName" />
          </el-select>
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio :value="0">目录</el-radio>
            <el-radio :value="1">菜单</el-radio>
            <el-radio :value="2">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.type !== 2" label="路由路径">
          <el-input v-model="form.path" placeholder="如 /system/users" />
        </el-form-item>
        <el-form-item v-if="form.type !== 2" label="组件路径">
          <el-input v-model="form.component" placeholder="如 system/user/index 或 Layout" />
        </el-form-item>
        <el-form-item v-if="form.type !== 2" label="图标">
          <el-input v-model="form.icon" placeholder="如 User" />
        </el-form-item>
        <el-form-item label="权限标识">
          <el-input v-model="form.permission" placeholder="如 system:user:view" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { listMenus, createMenu, updateMenu, deleteMenu } from '@/api/menu'

const loading = ref(false)
const allMenus = ref([])
const dialogVisible = ref(false)
const formRef = ref()
const isEdit = ref(false)
const editId = ref(null)

const form = reactive({
  menuName: '',
  parentId: 0,
  type: 1,
  path: '',
  component: '',
  icon: '',
  permission: '',
  sort: 0,
  status: 1
})

const rules = {
  menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }]
}

// 构建树形结构
const buildTree = (menus, parentId = 0) => {
  return menus
    .filter(m => m.parentId === parentId)
    .sort((a, b) => a.sort - b.sort)
    .map(m => ({
      ...m,
      children: buildTree(menus, m.menuId)
    }))
}

const menuTree = computed(() => buildTree(allMenus.value))

// 扁平化菜单（用于上级菜单选择器，只显示目录和菜单，排除按钮）
const flatMenus = computed(() => allMenus.value.filter(m => m.type !== 2))

const fetchData = async () => {
  loading.value = true
  try {
    const res = await listMenus()
    allMenus.value = res.data
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  formRef.value?.resetFields()
  Object.assign(form, { menuName: '', parentId: 0, type: 1, path: '', component: '', icon: '', permission: '', sort: 0, status: 1 })
  isEdit.value = false
  editId.value = null
}

const handleAdd = () => {
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  resetForm()
  isEdit.value = true
  editId.value = row.menuId
  Object.assign(form, {
    menuName: row.menuName,
    parentId: row.parentId,
    type: row.type,
    path: row.path,
    component: row.component,
    icon: row.icon,
    permission: row.permission,
    sort: row.sort,
    status: row.status
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    if (isEdit.value) {
      await updateMenu(editId.value, form)
      ElMessage.success('更新成功')
    } else {
      await createMenu(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    console.error(e)
  }
}

const handleDelete = (row) => {
  const hasChildren = allMenus.value.some(m => m.parentId === row.menuId)
  if (hasChildren) {
    ElMessage.warning('请先删除子菜单')
    return
  }
  ElMessageBox.confirm('确定删除该菜单吗？', '提示', { type: 'warning' }).then(async () => {
    await deleteMenu(row.menuId)
    ElMessage.success('删除成功')
    fetchData()
  }).catch(() => {})
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.page-container { padding: 20px; }
.search-bar { margin-bottom: 20px; }
</style>
