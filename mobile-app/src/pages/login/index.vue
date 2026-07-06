<template>
  <view class="page">
    <view class="title">智慧小区</view>
    <view class="card">
      <input class="input" v-model="form.username" placeholder="用户名" />
      <view style="height: 12px" />
      <input class="input" v-model="form.password" type="password" placeholder="密码" />
      <view style="height: 12px" />
      <view class="captcha-row">
        <input class="input captcha-input" v-model="form.captchaCode" placeholder="验证码" />
        <image
          class="captcha-img"
          :src="captchaImage"
          mode="aspectFill"
          @click="refreshCaptcha"
        />
      </view>
      <view style="height: 16px" />
      <button class="btn-primary" :loading="loading" @click="onLogin">登录</button>
    </view>
  </view>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { post, get } from '@/utils/request'

const loading = ref(false)
const captchaKey = ref('')
const captchaImage = ref('')

const form = reactive({
  username: 'admin',
  password: 'admin123',
  captchaCode: ''
})

const refreshCaptcha = async () => {
  try {
    const res = await get('/auth/captcha')
    captchaKey.value = res.data.captchaKey
    captchaImage.value = res.data.captchaImage
    form.captchaCode = ''
  } catch (e) {
    uni.showToast({ title: '获取验证码失败', icon: 'none' })
  }
}

onMounted(() => {
  refreshCaptcha()
})

const onLogin = async () => {
  if (!form.username || !form.password) {
    uni.showToast({ title: '请输入账号密码', icon: 'none' })
    return
  }
  if (!form.captchaCode) {
    uni.showToast({ title: '请输入验证码', icon: 'none' })
    return
  }
  loading.value = true
  try {
    const res = await post('/auth/login', {
      username: form.username,
      password: form.password,
      captchaKey: captchaKey.value,
      captchaCode: form.captchaCode
    })
    uni.setStorageSync('token', res.data.token)
    uni.setStorageSync('userInfo', JSON.stringify(res.data.userInfo))
    uni.reLaunch({ url: '/pages/home/index' })
  } catch (e) {
    refreshCaptcha()
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
.input {
  border: 1px solid #dcdfe6;
  border-radius: 8rpx;
  padding: 20rpx 24rpx;
  font-size: 28rpx;
}
.btn-primary {
  background: #2b85e4;
  color: #fff;
  border: none;
  border-radius: 8rpx;
  font-size: 30rpx;
}

.captcha-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.captcha-input {
  flex: 1;
}

.captcha-img {
  width: 200rpx;
  height: 72rpx;
  border-radius: 8rpx;
  border: 1px solid #dcdfe6;
  flex-shrink: 0;
}
</style>
