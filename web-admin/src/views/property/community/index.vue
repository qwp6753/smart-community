<template>
  <div class="page-container">
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="小区名称">
          <el-input v-model="searchForm.name" placeholder="请输入名称" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Plus" type="success" @click="handleAdd">新增</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="name" label="小区名称" min-width="150" />
      <el-table-column prop="address" label="地址" min-width="200" />
      <el-table-column prop="totalBuilding" label="楼栋数" width="100" />
      <el-table-column prop="totalHouse" label="房屋数" width="100" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-switch :model-value="row.status === 1" disabled />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button type="primary" :icon="Edit" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" :icon="Delete" size="small" @click="handleDelete(row)">删除</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrap">
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchData"
        @current-change="fetchData"
      />
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="小区名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入小区名称" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入地址" />
        </el-form-item>
        <el-form-item label="楼栋数" prop="totalBuilding">
          <el-input-number v-model="form.totalBuilding" :min="0" />
        </el-form-item>
        <el-form-item label="房屋数" prop="totalHouse">
          <el-input-number v-model="form.totalHouse" :min="0" />
        </el-form-item>
        <el-form-item label="经度" prop="mapLng">
          <el-input-number v-model="form.mapLng" :precision="6" />
        </el-form-item>
        <el-form-item label="纬度" prop="mapLat">
          <el-input-number v-model="form.mapLat" :precision="6" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
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
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Edit, Delete } from '@element-plus/icons-vue'
import { listCommunities, createCommunity, updateCommunity, deleteCommunity } from '@/api/community'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const formRef = ref()
const isEdit = ref(false)
const editId = ref(null)

const searchForm = reactive({ name: '' })
const pagination = reactive({ current: 1, size: 10, total: 0 })

const form = reactive({
  name: '',
  address: '',
  totalBuilding: 0,
  totalHouse: 0,
  status: 1,
  mapLng: null,
  mapLat: null
})

const rules = {
  name: [{ required: true, message: '请输入小区名称', trigger: 'blur' }],
  address: [{ required: true, message: '请输入地址', trigger: 'blur' }]
}

const dialogTitle = computed(() => isEdit.value ? '编辑小区' : '新增小区')

const fetchData = async () => {
  loading.value = true
  try {
    const params = { current: pagination.current, size: pagination.size, ...searchForm }
    const res = await listCommunities(params)
    tableData.value = res.data.records
    pagination.total = res.data.total
    pagination.current = res.data.current
    pagination.size = res.data.size
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
  Object.assign(form, {
    name: '',
    address: '',
    totalBuilding: 0,
    totalHouse: 0,
    status: 1,
    mapLng: null,
    mapLat: null
  })
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
  editId.value = row.id
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    if (isEdit.value) {
      await updateCommunity(editId.value, form)
      ElMessage.success('更新成功')
    } else {
      await createCommunity(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    console.error(e)
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定删除该小区吗？', '提示', { type: 'warning' }).then(async () => {
    await deleteCommunity(row.id)
    ElMessage.success('删除成功')
    fetchData()
  }).catch(() => {})
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
}
.search-bar {
  margin-bottom: 20px;
}
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
.table-actions {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}
</style>
