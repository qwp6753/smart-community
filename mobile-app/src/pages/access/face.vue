<template>
  <view class="page">
    <view class="card">
      <view class="card-title">人脸识别门禁</view>
      <view class="preview" v-if="imagePath">
        <image :src="imagePath" mode="aspectFit" class="preview-img" />
      </view>
      <view class="actions">
        <button class="btn-primary" @click="takePhoto">拍照识别</button>
        <button v-if="imagePath" class="btn-warning" @click="choosePhoto">重新选取</button>
      </view>
      <view v-if="result" class="result">
        <view class="tag" :class="result.success ? 'tag-success' : 'tag-error'">
          <text>{{ result.success ? '识别成功' : '识别失败' }}</text>
        </view>
        <text class="result-text">匹配：{{ result.personName || '未知' }}</text>
        <text class="result-text" v-if="result.confidence">置信度：{{ (result.confidence * 100).toFixed(1) }}%</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { post } from '@/utils/request'

const imagePath = ref('')
const result = ref(null)

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
  try {
    const fs = uni.getFileSystemManager()
    const base64 = fs.readFileSync(filePath, 'base64')
    const res = await post('/face/identify', { image: base64 })
    result.value = res.data
  } catch (e) {
    uni.showToast({ title: '识别失败，请重试', icon: 'none' })
    result.value = { success: false, errorMessage: '识别失败' }
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
  margin-bottom: 24rpx;
}
.preview {
  margin-bottom: 24rpx;
}
.preview-img {
  width: 100%;
  height: 400rpx;
  border-radius: 12rpx;
}
.actions {
  display: flex;
  gap: 16rpx;
  margin-bottom: 24rpx;
}
.result {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  align-items: center;
}
.result-text {
  font-size: 28rpx;
  color: #333;
}
.btn-primary {
  background: #2b85e4;
  color: #fff;
  border: none;
  border-radius: 8rpx;
  font-size: 30rpx;
  flex: 1;
}
.btn-warning {
  background: #f0a020;
  color: #fff;
  border: none;
  border-radius: 8rpx;
  font-size: 30rpx;
  flex: 1;
}
.tag {
  padding: 8rpx 24rpx;
  border-radius: 6rpx;
  font-size: 26rpx;
}
.tag-success {
  background: #e8f5e9;
  color: #18a058;
}
.tag-error {
  background: #fbe9e7;
  color: #d03050;
}
</style>
