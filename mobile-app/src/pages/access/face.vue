<template>
  <view class="page">
    <AppCard title="人脸识别门禁" subtitle="拍照后系统将自动识别身份并记录出入">
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

      <!-- 拍照预览 -->
      <view class="preview" v-if="imagePath">
        <image :src="imagePath" mode="aspectFit" class="preview-img" />
      </view>
      <view v-else class="preview-placeholder">
        <text class="placeholder-icon">📷</text>
        <text class="placeholder-text">请拍照或选择照片</text>
      </view>

      <!-- 操作按钮 -->
      <view class="actions">
        <AppButton type="primary" :block="true" @click="takePhoto" :loading="identifying">
          {{ imagePath ? '重新拍照' : '拍照识别' }}
        </AppButton>
        <AppButton v-if="!imagePath" type="outline" :block="true" @click="choosePhoto">
          从相册选择
        </AppButton>
      </view>

      <!-- 识别结果 -->
      <view v-if="result" class="result-card" :class="result.success ? 'result-success' : 'result-fail'">
        <view v-if="result.success" class="result-success-content">
          <text class="result-emoji">✅</text>
          <text class="result-title">识别成功</text>
          <text class="result-name" @click="goPersonDetail">{{ result.personName }}</text>
          <text class="result-confidence">
            置信度：{{ result.confidence ? (result.confidence * 100).toFixed(1) : '0' }}%
          </text>
        </view>
        <view v-else class="result-fail-content">
          <text class="result-emoji">⚠️</text>
          <text class="result-title">未识别</text>
          <text class="result-msg">{{ result.errorMessage || '未匹配到居民信息' }}</text>
        </view>
      </view>

      <!-- 识别历史 -->
      <view v-if="history.length > 0" class="history-section">
        <text class="history-title">识别记录</text>
        <view v-for="(item, i) in history" :key="i" class="history-item">
          <view class="history-left">
            <text class="history-name">{{ item.personName || '未知' }}</text>
            <text class="history-time">{{ item.time }}</text>
          </view>
          <view class="history-tag" :class="item.success ? 'tag-ok' : 'tag-fail'">
            {{ item.success ? '成功' : '失败' }}
          </view>
        </view>
      </view>
    </AppCard>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { identifyFace } from '@/api/face'
import AppCard from '@/components/AppCard.vue'
import AppButton from '@/components/AppButton.vue'

const imagePath = ref('')
const result = ref(null)
const accessType = ref('in')
const identifying = ref(false)
const history = ref([])

const takePhoto = () => {
  uni.chooseImage({
    count: 1,
    sourceType: ['camera'],
    success: (res) => {
      imagePath.value = res.tempFilePaths[0]
      doIdentify(res.tempFilePaths[0])
    }
  })
}

const choosePhoto = () => {
  uni.chooseImage({
    count: 1,
    sourceType: ['album'],
    success: (res) => {
      imagePath.value = res.tempFilePaths[0]
      doIdentify(res.tempFilePaths[0])
    }
  })
}

const doIdentify = async (filePath) => {
  identifying.value = true
  uni.showLoading({ title: '识别中...', mask: true })
  try {
    const fs = uni.getFileSystemManager()
    const base64 = fs.readFileSync(filePath, 'base64')
    const res = await identifyFace({
      image: base64,
      type: accessType.value,
      location: '移动端人脸识别门禁'
    })
    result.value = res.data

    // 记录历史
    history.value.unshift({
      personName: res.data.personName || '未知',
      time: new Date().toLocaleString('zh-CN'),
      success: res.data.success
    })
    if (history.value.length > 5) history.value.pop()

    if (res.data.success) {
      uni.showToast({ title: `欢迎，${res.data.personName}`, icon: 'success' })
    } else {
      uni.showToast({ title: '未识别到身份', icon: 'none' })
    }
  } catch (e) {
    uni.showToast({ title: '识别失败，请重试', icon: 'none' })
    result.value = { success: false, errorMessage: '网络请求失败' }
    history.value.unshift({
      personName: '未知',
      time: new Date().toLocaleString('zh-CN'),
      success: false
    })
  } finally {
    uni.hideLoading()
    identifying.value = false
  }
}

const goPersonDetail = () => {
  // 识别成功后可以跳转居民详情，但目前API不返回personId
  uni.showToast({ title: '可前往居民查询查看详情', icon: 'none', duration: 2000 })
}
</script>

<style scoped lang="scss">
.page { padding: 24rpx; }

.type-row {
  display: flex;
  gap: 16rpx;
  margin-bottom: 32rpx;
}

.type-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24rpx 0;
  border: 2rpx solid #e5e6eb;
  border-radius: 12rpx;
  transition: all 0.2s;

  &.active {
    border-color: #1677ff;
    background: #e6f4ff;
  }
}

.type-icon { font-size: 40rpx; margin-bottom: 8rpx; }
.type-label { font-size: 26rpx; color: #1f2329; }

.preview {
  margin-bottom: 24rpx;
}

.preview-img {
  width: 100%;
  height: 400rpx;
  border-radius: 12rpx;
  background: #f5f5f5;
}

.preview-placeholder {
  width: 100%;
  height: 240rpx;
  border-radius: 12rpx;
  background: #f6f7fb;
  border: 2rpx dashed #e5e6eb;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  margin-bottom: 24rpx;

  .placeholder-icon { font-size: 64rpx; margin-bottom: 12rpx; }
  .placeholder-text { font-size: 26rpx; color: #999; }
}

.actions {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  margin-bottom: 24rpx;
}

.result-card {
  padding: 32rpx;
  border-radius: 16rpx;
  text-align: center;
  margin-bottom: 24rpx;
}

.result-success { background: #e6f8f1; }
.result-fail { background: #fff1f0; }

.result-emoji { font-size: 48rpx; display: block; margin-bottom: 12rpx; }
.result-title {
  display: block;
  font-size: 32rpx;
  font-weight: 600;
  color: #1f2329;
  margin-bottom: 8rpx;
}
.result-name {
  display: block;
  font-size: 36rpx;
  font-weight: 700;
  color: #00b96b;
  margin-bottom: 8rpx;
  padding: 8rpx 0;
}

.result-confidence {
  display: block;
  font-size: 26rpx;
  color: #646a73;
}

.result-msg {
  display: block;
  font-size: 28rpx;
  color: #ff4d4f;
  margin-top: 8rpx;
}

.history-section {
  border-top: 1rpx solid #f0f0f0;
  padding-top: 24rpx;
}

.history-title {
  display: block;
  font-size: 30rpx;
  font-weight: 600;
  color: #1f2329;
  margin-bottom: 16rpx;
}

.history-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #f5f5f5;

  &:last-child { border-bottom: none; }
}

.history-name { font-size: 28rpx; color: #1f2329; display: block; }
.history-time { font-size: 22rpx; color: #999; margin-top: 4rpx; display: block; }

.history-tag {
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
  font-size: 22rpx;
  font-weight: 500;
}

.tag-ok { background: #e6f8f1; color: #00b96b; }
.tag-fail { background: #fff1f0; color: #ff4d4f; }
</style>
