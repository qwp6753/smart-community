<template>
  <view class="page">
    <view class="title">智慧小区</view>
    <view class="card">
      <u--input v-model="form.username" placeholder="用户名" clearable />
      <view style="height: 12px" />
      <u--input v-model="form.password" type="password" placeholder="密码" clearable />
      <view style="height: 16px" />
      <u-button type="primary" :loading="loading" @click="onLogin">登录</u-button>
    </view>
  </view>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { post } from '@/utils/request'

const loading = ref(false)
const form = reactive({
  username: 'admin',
  password: 'admin123'
})

const onLogin = async () => {
  if (!form.username || !form.password) {
    uni.showToast({ title: '请输入账号密码', icon: 'none' })
    return
  }
  loading.value = true
  try {
    const res = await post('/auth/login', form)
    uni.setStorageSync('token', res.data.token)
    uni.setStorageSync('userInfo', JSON.stringify(res.data.userInfo))
    uni.reLaunch({ url: '/pages/home/index' })
  } catch (e) {
    uni.showToast({ title: '登录失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.page {
  padding: 48rpx 32rpx;
}
.title {
  font-size: 44rpx;
  font-weight: 700;
  color: #1f2329;
  margin-top: 24rpx;
  margin-bottom: 40rpx;
}
.card {
  background: #ffffff;
  border-radius: 20rpx;
  padding: 32rpx;
}
</style>
