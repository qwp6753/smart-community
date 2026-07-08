<template>
  <div class="page-container">
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="姓名">
          <el-input v-model="searchForm.userName" placeholder="请输入姓名" clearable />
        </el-form-item>
        <el-form-item label="所属小区">
          <el-select v-model="searchForm.communityId" placeholder="请选择" clearable>
            <el-option v-for="c in communities" :key="c.communityId" :label="c.name" :value="c.communityId" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Plus" type="success" @click="handleAdd">新增</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column label="人脸" width="80" align="center">
        <template #default="{ row }">
          <el-avatar v-if="row.faceUrl" :size="40" :src="getFaceImageUrl(row.faceUrl)" />
          <el-icon v-else :size="40" color="#c0c4cc"><UserFilled /></el-icon>
        </template>
      </el-table-column>
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
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button type="warning" :icon="Camera" size="small" @click="handleFaceCollect(row)">采集人脸</el-button>
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

    <!-- 编辑弹窗 -->
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
            <el-option v-for="c in communities" :key="c.communityId" :label="c.name" :value="c.communityId" />
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

    <!-- 人脸采集弹窗 -->
    <el-dialog v-model="faceDialogVisible" title="人脸采集" width="560px" @close="stopFaceCamera">
      <div class="face-collect">
        <!-- 摄像头选择 -->
        <div class="face-device-select" v-if="!faceSnapshot">
          <span>选择摄像头：</span>
          <el-select v-model="faceDeviceId" placeholder="请选择" @change="switchFaceDevice" style="width: 260px">
            <el-option v-for="d in faceDevices" :key="d.deviceId" :label="d.label" :value="d.deviceId" />
          </el-select>
        </div>
        <div class="camera-box">
          <video ref="faceVideoRef" autoplay playsinline class="face-video" />
          <canvas ref="faceCanvasRef" class="face-canvas" />
        </div>
        <div v-if="faceSnapshot" class="face-snapshot">
          <img :src="faceSnapshot" class="face-img" />
          <p class="face-tip">确认照片清晰后点击"保存人脸"</p>
        </div>
      </div>
      <template #footer>
        <el-button v-if="!faceSnapshot" type="primary" :disabled="!faceVideoReady" :loading="!faceVideoReady" @click="captureFace">
          {{ faceVideoReady ? '拍照' : '摄像头启动中...' }}
        </el-button>
        <el-button v-if="faceSnapshot" @click="retakeFace">重拍</el-button>
        <el-button v-if="faceSnapshot" type="primary" :loading="faceUploading" @click="saveFace">保存人脸</el-button>
        <el-button @click="faceDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Edit, Delete, Camera, UserFilled } from '@element-plus/icons-vue'
import { listPersons, createPerson, updatePerson, deletePerson, uploadFace } from '@/api/person'
import { listCommunities } from '@/api/community'

const loading = ref(false)
const tableData = ref([])
const communities = ref([])
const dialogVisible = ref(false)
const formRef = ref()
const isEdit = ref(false)
const editId = ref(null)

// 人脸采集相关
const faceDialogVisible = ref(false)
const faceVideoRef = ref(null)
const faceCanvasRef = ref(null)
const faceSnapshot = ref('')
const faceUploading = ref(false)
const currentFacePersonId = ref(null)
const faceDevices = ref([])
const faceDeviceId = ref('')
const faceVideoReady = ref(false)
let faceStream = null

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
  editId.value = row.personId
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
    await deletePerson(row.personId)
    ElMessage.success('删除成功')
    fetchData()
  }).catch(() => {})
}

// ---- 人脸采集 ----

const handleFaceCollect = async (row) => {
  currentFacePersonId.value = row.personId
  faceSnapshot.value = ''
  faceDevices.value = []
  faceDeviceId.value = ''
  faceDialogVisible.value = true
  try {
    // 优先使用前置摄像头（电脑内置摄像头），facingMode: 'user' 即为面向用户的摄像头
    const tempStream = await navigator.mediaDevices.getUserMedia({ video: { facingMode: 'user' }, audio: false })
    const devices = await navigator.mediaDevices.enumerateDevices()
    faceDevices.value = devices.filter(d => d.kind === 'videoinput')
    tempStream.getTracks().forEach(t => t.stop())
    // 优先匹配电脑内置摄像头：中文系统常见"内置"、"集成"、"笔记本"、"前置"、"内嵌"
    const builtIn = faceDevices.value.find(d => {
      const label = d.label || ''
      return label.includes('Integrated') || label.includes('Built-in') ||
             label.includes('内置') || label.includes('集成') || label.includes('笔记本') ||
             label.includes('前置') || label.includes('内嵌') || label.includes('内建')
    })
    faceDeviceId.value = builtIn?.deviceId || faceDevices.value[0]?.deviceId || ''
    if (faceDeviceId.value) {
      await startFaceCamera(faceDeviceId.value)
    }
  } catch (e) {
    ElMessage.error('无法访问摄像头，请检查浏览器权限')
    faceDialogVisible.value = false
  }
}

const startFaceCamera = async (deviceId) => {
  stopFaceCamera()
  faceVideoReady.value = false
  try {
    faceStream = await navigator.mediaDevices.getUserMedia({
      video: { deviceId: { exact: deviceId } },
      audio: false
    })
    if (faceVideoRef.value) {
      faceVideoRef.value.srcObject = faceStream
      // 等待视频元数据加载完毕，确保 videoWidth 有效
      faceVideoRef.value.onloadedmetadata = () => {
        faceVideoReady.value = true
      }
    }
  } catch (e) {
    ElMessage.error('启动摄像头失败')
  }
}

const switchFaceDevice = (deviceId) => {
  faceDeviceId.value = deviceId
  startFaceCamera(deviceId)
}

// 限制图片最大分辨率，避免过高像素导致腾讯云API报ImageEmpty
const MAX_FACE_IMAGE_WIDTH = 800

const captureFace = () => {
  const video = faceVideoRef.value
  if (!video || video.videoWidth === 0 || video.videoHeight === 0) {
    ElMessage.warning('摄像头画面未就绪，请稍后再试')
    return
  }
  if (!faceCanvasRef.value) return
  const canvas = faceCanvasRef.value
  // 按比例缩放到最大宽度
  const scale = Math.min(1, MAX_FACE_IMAGE_WIDTH / video.videoWidth)
  canvas.width = Math.round(video.videoWidth * scale)
  canvas.height = Math.round(video.videoHeight * scale)
  canvas.getContext('2d').drawImage(video, 0, 0, canvas.width, canvas.height)
  faceSnapshot.value = canvas.toDataURL('image/jpeg', 0.85)
  stopFaceCamera()
}

const retakeFace = () => {
  faceSnapshot.value = ''
  handleFaceCollect({ personId: currentFacePersonId.value })
}

const saveFace = async () => {
  if (!faceSnapshot.value) return
  faceUploading.value = true
  try {
    await uploadFace(currentFacePersonId.value, faceSnapshot.value)
    ElMessage.success('人脸照片保存成功')
    faceDialogVisible.value = false
    fetchData()
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    faceUploading.value = false
  }
}

const stopFaceCamera = () => {
  if (faceStream) {
    faceStream.getTracks().forEach(t => t.stop())
    faceStream = null
  }
}

// 通过本地路径显示人脸照片（实际项目中可能是文件服务URL）
const getFaceImageUrl = (faceUrl) => {
  // faceUrl 是服务端存储的本地路径，这里无法直接访问，显示默认图标
  // 实际部署时可配置静态资源映射
  return ''
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

/* 人脸采集 */
.face-collect {
  text-align: center;
}
.face-device-select {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  font-size: 13px;
  color: #606266;
}
.camera-box {
  background: #000;
  border-radius: 8px;
  overflow: hidden;
  display: flex;
  justify-content: center;
}
.face-video {
  width: 100%;
  max-height: 360px;
  object-fit: contain;
}
.face-canvas {
  display: none;
}
.face-snapshot {
  margin-top: 16px;
}
.face-img {
  max-width: 200px;
  max-height: 200px;
  border: 2px solid #409eff;
  border-radius: 8px;
}
.face-tip {
  color: #909399;
  font-size: 13px;
  margin-top: 8px;
}
</style>
