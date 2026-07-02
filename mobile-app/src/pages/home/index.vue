<template>
  <view class="page">
    <view class="header">
      <text class="greeting">您好，{{ userName }}</text>
    </view>

    <view class="grid">
      <view class="grid-item" @click="navTo('/pages/access/face')">
        <u-icon name="camera-fill" size="40" color="#2b85e4" />
        <text>人脸门禁</text>
      </view>
      <view class="grid-item" @click="navTo('/pages/person/search')">
        <u-icon name="search" size="40" color="#18a058" />
        <text>查居民</text>
      </view>
      <view class="grid-item" @click="navTo('/pages/visitor/create')">
        <u-icon name="man-add-fill" size="40" color="#f0a020" />
        <text>访客登记</text>
      </view>
      <view class="grid-item" @click="navTo('/pages/map/index')">
        <u-icon name="map-fill" size="40" color="#d03050" />
        <text>小区地图</text>
      </view>
    </view>

    <view class="card">
      <view class="card-title">今日统计</view>
      <view class="stats">
        <view class="stat-item">
          <text class="stat-num">{{ stats.recordCount }}</text>
          <text class="stat-label">出入记录</text>
        </view>
        <view class="stat-item">
          <text class="stat-num">{{ stats.visitorCount }}</text>
          <text class="stat-label">访客</text>
        </view>
        <view class="stat-item">
          <text class="stat-num">{{ stats.personCount }}</text>
          <text class="stat-label">居民</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { get } from '@/utils/request'

const userName = ref('管理员')
const stats = ref({ recordCount: 0, visitorCount: 0, personCount: 0 })

onMounted(async () => {
  const info = uni.getStorageSync('userInfo')
  if (info) {
    try {
      const u = JSON.parse(info)
      userName.value = u.realName || u.username || '管理员'
    } catch (e) { }
  }
  try {
    const res = await get('/dashboard/stats')
    stats.value = res.data
  } catch (e) { }
})

const navTo = (url) => {
  uni.navigateTo({ url })
}
</script>

<style scoped lang="scss">
.page {
  padding: 32rpx;
}
.header {
  padding: 40rpx 0;
}
.greeting {
  font-size: 40rpx;
  font-weight: 700;
  color: #1f2329;
}
.grid {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  margin-bottom: 32rpx;
}
.grid-item {
  width: calc(50% - 8rpx);
  background: #fff;
  border-radius: 16rpx;
  padding: 40rpx 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
  font-size: 28rpx;
  color: #333;
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
.stats {
  display: flex;
  justify-content: space-around;
}
.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.stat-num {
  font-size: 40rpx;
  font-weight: 700;
  color: #2b85e4;
}
.stat-label {
  font-size: 24rpx;
  color: #999;
  margin-top: 8rpx;
}
</style>
