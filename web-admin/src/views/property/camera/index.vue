<template>
  <div class="page-container">
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="名称">
          <el-input v-model="searchForm.name" placeholder="请输入名称" clearable />
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
      <el-table-column prop="name" label="名称" min-width="150" />
      <el-table-column prop="ipAddress" label="IP地址" width="160" />
      <el-table-column label="所属小区" min-width="150">
        <template #default="{ row }">
          {{ getCommunityName(row.communityId) }}
        </template>
      </el-table-column>
      <el-table-column prop="location" label="位置" min-width="200" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '在线' : '离线' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button type="success" :icon="VideoCamera" size="small" @click="handlePreview(row)">预览</el-button>
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
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入名称" />
        </el-form-item>
        <el-form-item label="IP地址" prop="ipAddress">
          <el-input v-model="form.ipAddress" placeholder="请输入IP地址" />
        </el-form-item>
        <el-form-item label="所属小区" prop="communityId">
          <el-select v-model="form.communityId" placeholder="请选择小区">
            <el-option v-for="c in communities" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="位置" prop="location">
          <el-input v-model="form.location" placeholder="请输入位置" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择状态">
            <el-option label="在线" :value="1" />
            <el-option label="离线" :value="0" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 摄像头预览弹窗 -->
    <el-dialog v-model="previewVisible" title="摄像头预览" width="720px" @close="stopPreview">
      <div class="device-select">
        <span>选择摄像头：</span>
        <el-select v-model="selectedDeviceId" placeholder="请选择" @change="switchDevice" style="width: 300px">
          <el-option v-for="d in videoDevices" :key="d.deviceId" :label="d.label" :value="d.deviceId" />
        </el-select>
      </div>
      <div class="preview-container">
        <video ref="videoRef" autoplay playsinline class="preview-video" />
        <canvas ref="canvasRef" class="preview-canvas" />
      </div>
      <div v-if="snapshot" class="snapshot-preview">
        <img :src="snapshot" class="snapshot-img" />
      </div>
      <template #footer>
        <el-button type="primary" @click="takeSnapshot">拍照</el-button>
        <el-button v-if="snapshot" type="success" @click="downloadSnapshot">下载截图</el-button>
        <el-button @click="previewVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Edit, Delete, VideoCamera } from '@element-plus/icons-vue'
import { listCameras, createCamera, updateCamera, deleteCamera } from '@/api/camera'
import { listCommunities } from '@/api/community'

const loading = ref(false)
const tableData = ref([])
const communities = ref([])
const dialogVisible = ref(false)
const formRef = ref()
const isEdit = ref(false)
const editId = ref(null)

// 摄像头预览相关
const previewVisible = ref(false)
const videoRef = ref(null)
const canvasRef = ref(null)
const snapshot = ref('')
const videoDevices = ref([])
const selectedDeviceId = ref('')
let mediaStream = null

const searchForm = reactive({ name: '', communityId: '' })
const pagination = reactive({ current: 1, size: 10, total: 0 })

const form = reactive({
  name: '',
  ipAddress: '',
  communityId: null,
  location: '',
  status: 1
})

const rules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  ipAddress: [{ required: true, message: '请输入IP地址', trigger: 'blur' }],
  communityId: [{ required: true, message: '请选择小区', trigger: 'change' }]
}

const dialogTitle = computed(() => isEdit.value ? '编辑摄像头' : '新增摄像头')

const getCommunityName = (id) => {
  const c = communities.value.find((item) => item.id === id)
  return c ? c.name : '-'
}

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
    if (searchForm.name) params.name = searchForm.name
    if (searchForm.communityId) params.communityId = searchForm.communityId
    const res = await listCameras(params)
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
    ipAddress: '',
    communityId: null,
    location: '',
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
      await updateCamera(editId.value, form)
      ElMessage.success('更新成功')
    } else {
      await createCamera(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    console.error(e)
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定删除该摄像头吗？', '提示', { type: 'warning' }).then(async () => {
    await deleteCamera(row.id)
    ElMessage.success('删除成功')
    fetchData()
  }).catch(() => {})
}

// 摄像头预览
const handlePreview = async () => {
  snapshot.value = ''
  previewVisible.value = true
  try {
    // 先获取临时流以授权设备访问
    const tempStream = await navigator.mediaDevices.getUserMedia({ video: true, audio: false })
    // 枚举所有视频设备
    const devices = await navigator.mediaDevices.enumerateDevices()
    videoDevices.value = devices.filter(d => d.kind === 'videoinput')
    // 停止临时流
    tempStream.getTracks().forEach(t => t.stop())
    // 优先选择电脑内置摄像头
    const builtIn = videoDevices.value.find(d =>
      d.label.includes('Integrated') || d.label.includes('Built-in') || d.label.includes('内建') || d.label.includes('集成')
    )
    selectedDeviceId.value = builtIn?.deviceId || videoDevices.value[0]?.deviceId || ''
    if (selectedDeviceId.value) {
      await startCamera(selectedDeviceId.value)
    }
  } catch (e) {
    ElMessage.error('无法访问摄像头，请检查浏览器权限设置')
    previewVisible.value = false
  }
}

const startCamera = async (deviceId) => {
  stopPreview()
  try {
    mediaStream = await navigator.mediaDevices.getUserMedia({
      video: { deviceId: { exact: deviceId } },
      audio: false
    })
    if (videoRef.value) {
      videoRef.value.srcObject = mediaStream
    }
  } catch (e) {
    ElMessage.error('启动摄像头失败')
  }
}

const switchDevice = (deviceId) => {
  selectedDeviceId.value = deviceId
  startCamera(deviceId)
}

const stopPreview = () => {
  if (mediaStream) {
    mediaStream.getTracks().forEach(track => track.stop())
    mediaStream = null
  }
  if (videoRef.value) {
    videoRef.value.srcObject = null
  }
}

const takeSnapshot = () => {
  if (!videoRef.value || !canvasRef.value) return
  const video = videoRef.value
  const canvas = canvasRef.value
  canvas.width = video.videoWidth
  canvas.height = video.videoHeight
  canvas.getContext('2d').drawImage(video, 0, 0)
  snapshot.value = canvas.toDataURL('image/png')
}

const downloadSnapshot = () => {
  if (!snapshot.value) return
  const link = document.createElement('a')
  link.href = snapshot.value
  link.download = `camera-snapshot-${Date.now()}.png`
  link.click()
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
.device-select {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}
.preview-container {
  display: flex;
  justify-content: center;
  background: #000;
  border-radius: 8px;
  overflow: hidden;
}
.preview-video {
  width: 100%;
  max-height: 400px;
  object-fit: contain;
}
.preview-canvas {
  display: none;
}
.snapshot-preview {
  margin-top: 16px;
  text-align: center;
}
.snapshot-img {
  max-width: 100%;
  max-height: 200px;
  border: 2px solid #409eff;
  border-radius: 8px;
}
</style>
