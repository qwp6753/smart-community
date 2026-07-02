<template>
  <div class="page-container">
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="姓名">
          <el-input v-model="searchForm.userName" placeholder="请输入姓名" clearable />
        </el-form-item>
        <el-form-item label="所属小区">
          <el-select v-model="searchForm.communityId" placeholder="请选择" clearable>
            <el-option v-for="c in communities" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Plus" type="success" @click="handleAdd">新增</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="userName" label="姓名" min-width="120" />
      <el-table-column prop="mobile" label="手机号" width="140" />
      <el-table-column prop="sex" label="性别" width="70" />
      <el-table-column prop="houseNo" label="门牌号" min-width="120" />
      <el-table-column label="类型" width="100">
        <template #default="{ row }">
          <el-tag :type="row.personType === 'owner' ? 'primary' : 'info'">
            {{ row.personType === 'owner' ? '业主' : '租户' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="state" label="状态" width="80">
        <template #default="{ row }">
          <el-switch :model-value="row.state === 1" disabled />
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
        <el-form-item label="姓名" prop="userName">
          <el-input v-model="form.userName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="mobile">
          <el-input v-model="form.mobile" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="性别" prop="sex">
          <el-select v-model="form.sex" placeholder="请选择性别">
            <el-option label="男" value="男" />
            <el-option label="女" value="女" />
          </el-select>
        </el-form-item>
        <el-form-item label="门牌号" prop="houseNo">
          <el-input v-model="form.houseNo" placeholder="请输入门牌号" />
        </el-form-item>
        <el-form-item label="人员类型" prop="personType">
          <el-select v-model="form.personType" placeholder="请选择类型">
            <el-option label="业主" value="owner" />
            <el-option label="租户" value="tenant" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属小区" prop="communityId">
          <el-select v-model="form.communityId" placeholder="请选择小区">
            <el-option v-for="c in communities" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="state">
          <el-switch v-model="form.state" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
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
import { listPersons, createPerson, updatePerson, deletePerson } from '@/api/person'
import { listCommunities } from '@/api/community'

const loading = ref(false)
const tableData = ref([])
const communities = ref([])
const dialogVisible = ref(false)
const formRef = ref()
const isEdit = ref(false)
const editId = ref(null)

const searchForm = reactive({ userName: '', communityId: '' })
const pagination = reactive({ current: 1, size: 10, total: 0 })

const form = reactive({
  userName: '',
  mobile: '',
  sex: '',
  houseNo: '',
  personType: 'owner',
  communityId: null,
  state: 1,
  remark: ''
})

const rules = {
  userName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  mobile: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  houseNo: [{ required: true, message: '请输入门牌号', trigger: 'blur' }],
  personType: [{ required: true, message: '请选择人员类型', trigger: 'change' }],
  communityId: [{ required: true, message: '请选择小区', trigger: 'change' }]
}

const dialogTitle = computed(() => isEdit.value ? '编辑人员' : '新增人员')

const loadCommunities = async () => {
  try {
    const res = await listCommunities({ current: 1, size: 100 })
    communities.value = res.data.records
  } catch (e) {
    console.error(e)
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = { current: pagination.current, size: pagination.size }
    if (searchForm.userName) params.userName = searchForm.userName
    if (searchForm.communityId) params.communityId = searchForm.communityId
    const res = await listPersons(params)
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
    userName: '',
    mobile: '',
    sex: '',
    houseNo: '',
    personType: 'owner',
    communityId: null,
    state: 1,
    remark: ''
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
      await updatePerson(editId.value, form)
      ElMessage.success('更新成功')
    } else {
      await createPerson(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    console.error(e)
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定删除该人员吗？', '提示', { type: 'warning' }).then(async () => {
    await deletePerson(row.id)
    ElMessage.success('删除成功')
    fetchData()
  }).catch(() => {})
}

onMounted(() => {
  loadCommunities()
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
