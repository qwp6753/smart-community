<template>
  <view class="page">
    <u--input v-model="keyword" placeholder="输入姓名搜索" clearable>
      <template #suffix>
        <u-icon name="search" size="22" @click="search" />
      </template>
    </u--input>
    <view style="height: 16px" />
    <view v-if="list.length" class="person-list">
      <view v-for="item in list" :key="item.personId" class="person-card">
        <view class="person-info">
          <text class="name">{{ item.userName }}</text>
          <text class="detail">{{ item.sex }} · {{ item.houseNo }}</text>
          <text class="detail">{{ item.mobile }}</text>
        </view>
        <u-tag :text="item.personType === 'owner' ? '业主' : '租户'" :type="item.personType === 'owner' ? 'success' : 'warning'" />
      </view>
    </view>
    <u-empty v-else text="暂无数据" />
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
  } catch (e) { }
}
</script>

<style scoped lang="scss">
.page {
  padding: 32rpx;
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
</style>
