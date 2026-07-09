<template>
  <view class="page">
    <!-- 搜索栏 -->
    <view class="search-bar">
      <input class="search-input" v-model="keyword" placeholder="输入姓名搜索居民" @confirm="search" />
      <button class="search-btn" @click="search">搜索</button>
    </view>

    <!-- 居民列表 -->
    <scroll-view scroll-y class="list-scroll" v-if="list.length > 0">
      <view v-for="item in list" :key="item.personId" class="person-card" @click="goDetail(item.personId)">
        <view class="person-left">
          <view class="person-avatar">{{ item.userName?.charAt(0) || '?' }}</view>
          <view class="person-info">
            <text class="person-name">{{ item.userName || '-' }}</text>
            <text class="person-detail">{{ item.sex || '-' }} · {{ item.houseNo || '-' }}</text>
            <text class="person-detail">{{ item.mobile || '-' }}</text>
          </view>
        </view>
        <view class="person-right">
          <text class="person-type" :class="item.personType === 'owner' ? 'tag-owner' : 'tag-tenant'">
            {{ item.personType === 'owner' ? '业主' : '租户' }}
          </text>
          <text class="person-arrow">→</text>
        </view>
      </view>
    </scroll-view>

    <AppEmpty
      v-else-if="searched"
      icon="🔍"
      :message="`未找到“${keyword}”相关居民`"
    />
    <AppEmpty
      v-else
      icon="👥"
      message="输入姓名搜索小区居民"
    />
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { listPersons } from '@/api/person'
import AppEmpty from '@/components/AppEmpty.vue'

const keyword = ref('')
const list = ref([])
const searched = ref(false)

const search = async () => {
  if (!keyword.value.trim()) return
  searched.value = true
  try {
    const res = await listPersons({ current: 1, size: 50, userName: keyword.value })
    list.value = res.data.records || []
  } catch (e) {
    uni.showToast({ title: '查询失败', icon: 'none' })
  }
}

const goDetail = (personId) => {
  uni.navigateTo({ url: `/pages/person/detail?personId=${personId}` })
}
</script>

<style scoped lang="scss">
.page {
  padding: 24rpx;
  min-height: 100vh;
  background: #f6f7fb;
}

.search-bar {
  display: flex;
  gap: 12rpx;
  margin-bottom: 20rpx;
}

.search-input {
  flex: 1;
  background: #ffffff;
  border: 2rpx solid #e5e6eb;
  border-radius: 12rpx;
  padding: 18rpx 20rpx;
  font-size: 28rpx;
  height: 64rpx;
}

.search-btn {
  background: #1677ff;
  color: #fff;
  border: none;
  border-radius: 12rpx;
  font-size: 26rpx;
  padding: 18rpx 28rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  &::after { border: none; }
}

.list-scroll { flex: 1; }

.person-card {
  background: #ffffff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.06);
}

.person-left {
  display: flex;
  flex-direction: row;
  align-items: center;
  flex: 1;
}

.person-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background: #e6f4ff;
  color: #1677ff;
  font-size: 36rpx;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
  flex-shrink: 0;
}

.person-info {
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.person-name {
  font-size: 32rpx;
  font-weight: 600;
  color: #1f2329;
}

.person-detail {
  font-size: 24rpx;
  color: #999;
}

.person-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8rpx;
}

.person-type {
  padding: 4rpx 16rpx;
  border-radius: 20rpx;
  font-size: 22rpx;
  font-weight: 500;
}

.tag-owner { background: #e6f4ff; color: #1677ff; }
.tag-tenant { background: #fff7e6; color: #fa8c16; }

.person-arrow {
  font-size: 28rpx;
  color: #c0c4cc;
}
</style>
