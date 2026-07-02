<template>
  <div class="page-container">
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="访客姓名">
          <el-input v-model="searchForm.name" placeholder="请输入姓名" clearable />
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
      <el-table-column prop="name" label="访客姓名" min-width="120" />
      <el-table-column prop="mobile" label="手机号" width="140" />
      <el-table-column label="被访人" min-width="120">
        <template #default="{ row }">
          {{ getPersonName(row.personId) }}
        </template>
      </el-table-column>
      <el-table-column prop="visitTime" label="来访时间" width="180" />
      <el-table-column prop="leaveTime" label="离开时间" width="180" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '有效' : '失效' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" :icon="Edit" size="small" @click="handleEdit(row)">编辑</el-button>
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
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="mobile">
          <el-input v-model="form.mobile" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="form.idCard" placeholder="请输入身份证号" />
        </el-form-item>
        <el-form-item label="所属小区" prop="communityId">
          <el-select v-model="form.communityId" placeholder="请选择小区">
            <el-option v-for="c in communities" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="被访人" prop="personId">
          <el-select v-model="form.personId" placeholder="请选择被访人" filterable>
            <el-option v-for="p in persons" :key="p.id" :label="p.userName" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="来访时间" prop="visitTime">
          <el-date-picker v-model="form.visitTime" type="datetime" placeholder="选择来访时间" value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
        <el-form-item label="离开时间" prop="leaveTime">
          <el-date-picker v-model="form.leaveTime" type="datetime" placeholder="选择离开时间" value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
        <el-form-item label="来访事由" prop="reason">
          <el-input v-model="form.reason" type="textarea" placeholder="请输入来访事由" />
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
import { ElMessage } from 'element-plus'
import { Search, Plus, Edit } from '@element-plus/icons-vue'
import { listVisitors, createVisitor, updateVisitor } from '@/api/visitor'
import { listCommunities } from '@/api/community'
import { listPersons } from '@/api/person'

const loading = ref(false)
const tableData = ref([])
const communities = ref([])
const persons = ref([])
const dialogVisible = ref(false)
const formRef = ref()
const isEdit = ref(false)
const editId = ref(null)

const searchForm = reactive({ name: '', communityId: '' })
const pagination = reactive({ current: 1, size: 10, total: 0 })

const form = reactive({
  name: '',
  mobile: '',
  idCard: '',
  communityId: null,
  personId: null,
  visitTime: '',
  leaveTime: '',
  reason: '',
  status: 1
})

const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  mobile: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  personId: [{ required: true, message: '请选择被访人', trigger: 'change' }],
  communityId: [{ required: true, message: '请选择小区', trigger: 'change' }],
  visitTime: [{ required: true, message: '请选择来访时间', trigger: 'change' }]
}

const dialogTitle = computed(() => isEdit.value ? '编辑访客' : '新增访客')

const getPersonName = (id) => {
  const p = persons.value.find((item) => item.id === id)
  return p ? p.userName : '-'
}

const loadCommunities = async () => {
  try {
    const res = await listCommunities({ current: 1, size: 100 })
    communities.value = res.data.records
  } catch (e) {
    console.error(e)
  }
}

const loadPersons = async () => {
  try {
    const res = await listPersons({ current: 1, size: 1000 })
    persons.value = res.data.records
  } catch (e) {
    console.error(e)
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = { current: pagination.current, size: pagination.size }
    if (searchForm.name) params.name = searchForm.name
    if (searchForm.communityId) params.communityId = searchForm.communityId
    const res = await listVisitors(params)
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
    mobile: '',
    idCard: '',
    communityId: null,
    personId: null,
    visitTime: '',
    leaveTime: '',
    reason: '',
    status: 1
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
      await updateVisitor(editId.value, form)
      ElMessage.success('更新成功')
    } else {
      await createVisitor(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  loadCommunities()
  loadPersons()
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
</style>
