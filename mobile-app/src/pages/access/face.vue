<template>
  <view class="page">
    <view class="card">
      <view class="card-title">人脸识别门禁</view>
      <view class="card-desc">拍照后系统将自动识别身份并记录出入</view>

      <!-- 出入类型选择 -->
      <view class="type-row">
        <view class="type-item" :class="{ active: accessType === 'in' }" @click="accessType = 'in'">
          <text class="type-icon">🚪</text>
          <text class="type-label">进入小区</text>
        </view>
        <view class="type-item" :class="{ active: accessType === 'out' }" @click="accessType = 'out'">
          <text class="type-icon">🚶</text>
          <text class="type-label">离开小区</text>
        </view>
      </view>

      <view class="preview" v-if="imagePath">
        <image :src="imagePath" mode="aspectFit" class="preview-img" />
      </view>

      <view class="actions">
        <button class="btn-primary" @click="takePhoto">{{ imagePath ? '重新拍照' : '拍照识别' }}</button>
        <button v-if="!imagePath" class="btn-secondary" @click="choosePhoto">从相册选择</button>
      </view>

      <view v-if="result" class="result">
        <view class="result-card" :class="result.success ? 'result-success' : 'result-fail'">
          <text class="result-title">{{ result.success ? '✅ 识别成功' : '⚠️ 未识别' }}</text>
          <text class="result-name" v-if="result.personName">{{ result.personName }}</text>
          <text class="result-confidence" v-if="result.confidence">
            置信度：{{ (result.confidence * 100).toFixed(1) }}%
          </text>
          <text class="result-msg" v-if="!result.success">
            {{ result.errorMessage || '未匹配到居民信息' }}
          </text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { post } from '@/utils/request'

const imagePath = ref('')
const result = ref(null)
const accessType = ref('in')

const takePhoto = () => {
  uni.chooseImage({
    count: 1,
    sourceType: ['camera'],
    success: (res) => {
      imagePath.value = res.tempFilePaths[0]
      identifyFace(res.tempFilePaths[0])
    }
  })
}

const choosePhoto = () => {
  uni.chooseImage({
    count: 1,
    sourceType: ['album'],
    success: (res) => {
      imagePath.value = res.tempFilePaths[0]
      identifyFace(res.tempFilePaths[0])
    }
  })
}

const identifyFace = async (filePath) => {
  uni.showLoading({ title: '识别中...' })
  try {
    const fs = uni.getFileSystemManager()
    const base64 = fs.readFileSync(filePath, 'base64')
    const res = await post('/face/identify', {
      image: base64,
      type: accessType.value,
      location: '移动端人脸识别门禁'
    })
    result.value = res.data
    if (res.data.success) {
      uni.showToast({ title: `欢迎，${res.data.personName}`, icon: 'success' })
    } else {
      uni.showToast({ title: '未识别到身份', icon: 'none' })
    }
  } catch (e) {
    uni.showToast({ title: '识别失败，请重试', icon: 'none' })
    result.value = { success: false, errorMessage: '网络请求失败' }
  } finally {
    uni.hideLoading()
  }
}
</script>

<style scoped lang="scss">
.page {
  padding: 32rpx;
}
.card {
  background: #fff;
  border-radius: 16rpx;
  padding: 32rpx;
}
.card-title {
  font-size: 32rpx;
  font-weight: 600;
  margin-bottom: 8rpx;
}
.card-desc {
  font-size: 24rpx;
  color: #999;
  margin-bottom: 24rpx;
}

/* 出入类型选择 */
.type-row {
  display: flex;
  gap: 16rpx;
  margin-bottom: 24rpx;
}
.type-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20rpx 0;
  border: 2rpx solid #e4e7ed;
  border-radius: 12rpx;
  transition: all 0.2s;
}
.type-item.active {
  border-color: #2b85e4;
  background: #ecf5ff;
}
.type-icon {
  font-size: 40rpx;
  margin-bottom: 8rpx;
}
.type-label {
  font-size: 26rpx;
  color: #333;
}

.preview {
  margin-bottom: 24rpx;
}
.preview-img {
  width: 100%;
  height: 400rpx;
  border-radius: 12rpx;
  background: #f5f7fa;
}
.actions {
  display: flex;
  gap: 16rpx;
  margin-bottom: 24rpx;
}
.result {
  margin-top: 8rpx;
}
.result-card {
  padding: 24rpx;
  border-radius: 12rpx;
  text-align: center;
}
.result-success {
  background: #f0f9eb;
}
.result-fail {
  background: #fef0f0;
}
.result-title {
  display: block;
  font-size: 30rpx;
  font-weight: 600;
  margin-bottom: 8rpx;
}
.result-name {
  display: block;
  font-size: 32rpx;
  color: #333;
  margin-bottom: 4rpx;
}
.result-confidence {
  display: block;
  font-size: 26rpx;
  color: #909399;
}
.result-msg {
  display: block;
  font-size: 26rpx;
  color: #f56c6c;
  margin-top: 4rpx;
}

.btn-primary {
  background: #2b85e4;
  color: #fff;
  border: none;
  border-radius: 8rpx;
  font-size: 30rpx;
  flex: 1;
}
.btn-secondary {
  background: #f5f7fa;
  color: #606266;
  border: 1px solid #dcdfe6;
  border-radius: 8rpx;
  font-size: 30rpx;
  flex: 1;
}
</style>
