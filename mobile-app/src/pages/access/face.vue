<template>
  <view class="page">
    <view class="card">
      <view class="card-title">人脸识别门禁</view>
      <view class="preview" v-if="imagePath">
        <image :src="imagePath" mode="aspectFit" class="preview-img" />
      </view>
      <view class="actions">
        <u-button type="primary" @click="takePhoto">拍照识别</u-button>
        <u-button v-if="imagePath" type="warning" @click="choosePhoto">重新选取</u-button>
      </view>
      <view v-if="result" class="result">
        <u-tag :text="result.success ? '识别成功' : '识别失败'" :type="result.success ? 'success' : 'error'" />
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
</style>
