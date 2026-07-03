<template>
  <view class="page">
    <view class="search-bar">
      <input class="search-input" v-model="keyword" placeholder="输入姓名搜索" />
      <button class="search-btn" @click="search">搜索</button>
    </view>
    <view style="height: 16px" />
    <view v-if="list.length" class="person-list">
      <view v-for="item in list" :key="item.personId" class="person-card">
        <view class="person-info">
          <text class="name">{{ item.userName }}</text>
          <text class="detail">{{ item.sex }} · {{ item.houseNo }}</text>
          <text class="detail">{{ item.mobile }}</text>
        </view>
        <view class="tag" :class="item.personType === 'owner' ? 'tag-success' : 'tag-warning'">
          <text>{{ item.personType === 'owner' ? '业主' : '租户' }}</text>
        </view>
      </view>
    </view>
    <view v-else class="empty">
      <text class="empty-text">暂无数据</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { get } from '@/utils/request'

const keyword = ref('')
const list = ref([])

const search = async () => {
  if (!keyword.value.trim()) return
  try {
    const res = await get('/persons', { current: 1, size: 50, userName: keyword.value })
    list.value = res.data.records || []
  } catch (e) {
    uni.showToast({ title: '查询失败，请重试', icon: 'none' })
  }
}
</script>

<style scoped lang="scss">
.page {
  padding: 32rpx;
}
.search-bar {
  display: flex;
  gap: 12rpx;
}
.search-input {
  flex: 1;
  border: 1px solid #dcdfe6;
  border-radius: 8rpx;
  padding: 16rpx 20rpx;
  font-size: 28rpx;
}
.search-btn {
  background: #2b85e4;
  color: #fff;
  border: none;
  border-radius: 8rpx;
  font-size: 26rpx;
  padding: 0 24rpx;
}
.person-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}
.person-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx 32rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.person-info {
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}
.name {
  font-size: 32rpx;
  font-weight: 600;
}
.detail {
  font-size: 26rpx;
  color: #999;
}
.tag {
  padding: 8rpx 16rpx;
  border-radius: 6rpx;
  font-size: 24rpx;
}
.tag-success {
  background: #e8f5e9;
  color: #18a058;
}
.tag-warning {
  background: #fff3e0;
  color: #f0a020;
}
.empty {
  display: flex;
  justify-content: center;
  padding: 80rpx 0;
}
.empty-text {
  color: #999;
  font-size: 28rpx;
}
</style>
