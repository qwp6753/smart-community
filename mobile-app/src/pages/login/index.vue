<template>
  <view class="page">
    <!-- Logo区域 -->
    <view class="logo-area">
      <view class="logo-icon">🏘️</view>
      <text class="logo-title">智慧小区</text>
      <text class="logo-subtitle">Smart Community</text>
    </view>

    <!-- 登录表单 -->
    <AppCard :padding="'40rpx 32rpx'" :margin="'0 32rpx'">
      <view class="form-group">
        <text class="form-label">用户名</text>
        <input
          class="form-input"
          v-model="form.username"
          placeholder="请输入用户名"
          placeholder-style="color: #c0c4cc; font-size: 28rpx;"
        />
      </view>

      <view class="form-group">
        <text class="form-label">密码</text>
        <input
          class="form-input"
          v-model="form.password"
          type="password"
          placeholder="请输入密码"
          placeholder-style="color: #c0c4cc; font-size: 28rpx;"
        />
      </view>

      <view class="form-group">
        <text class="form-label">验证码</text>
        <view class="captcha-row">
          <input
            class="form-input captcha-input"
            v-model="form.captchaCode"
            placeholder="请输入验证码"
            placeholder-style="color: #c0c4cc; font-size: 28rpx;"
          />
          <image
            class="captcha-img"
            :src="captchaImage"
            mode="aspectFill"
            @click="refreshCaptcha"
          />
        </view>
      </view>

      <!-- 使用view+tap，uni-app中最可靠的事件方式 -->
      <view class="login-btn" @tap="onLogin">
        <text>{{ loading ? '登录中...' : '登 录' }}</text>
      </view>

      <view class="login-tips">
        <text class="tip-text">忘记密码？请联系管理员重置</text>
      </view>
    </AppCard>

    <!-- 底部版权 -->
    <view class="footer">
      <text class="footer-text">智慧小区管理系统 v1.0</text>
    </view>
  </view>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { get } from '@/utils/request'
import AppCard from '@/components/AppCard.vue'

const loading = ref(false)
const captchaKey = ref('')
const captchaImage = ref('')

const form = reactive({
  username: 'admin',
  password: 'admin123',
  captchaCode: ''
})

// 使用静默请求获取验证码，避免loading冲突
const refreshCaptcha = async () => {
  try {
    const res = await get('/auth/captcha', undefined, true)
    captchaKey.value = res.data.captchaKey
    captchaImage.value = res.data.captchaImage
    form.captchaCode = ''
  } catch (e) {
    // 静默失败，不影响页面
  }
}

onMounted(() => {
  refreshCaptcha()
})

const onLogin = () => {
  if (!form.username || !form.password) {
    uni.showToast({ title: '请输入账号和密码', icon: 'none' })
    return
  }
  if (!form.captchaCode) {
    uni.showToast({ title: '请输入验证码', icon: 'none' })
    return
  }

  loading.value = true
  uni.showLoading({ title: '登录中...', mask: true })

  // 直接调用 request，不使用 silent 模式以确保流程清晰
  const BASE_URL = 'http://localhost:8080/api'
  uni.request({
    url: BASE_URL + '/auth/login',
    method: 'POST',
    data: {
      username: form.username,
      password: form.password,
      captchaKey: captchaKey.value,
      captchaCode: form.captchaCode
    },
    header: { 'Content-Type': 'application/json' },
    timeout: 15000,
    success: (res) => {
      uni.hideLoading()
      loading.value = false
      const data = res.data
      if (data.code === 200) {
        const { token, userInfo } = data.data
        uni.setStorageSync('token', token)
        uni.setStorageSync('userInfo', JSON.stringify(userInfo))
        // tabBar页面必须用switchTab，reLaunch无法跳转
        uni.switchTab({ url: '/pages/tabbar/home/index' })
      } else {
        refreshCaptcha()
        uni.showToast({ title: data.message || '登录失败', icon: 'none' })
      }
    },
    fail: (err) => {
      uni.hideLoading()
      loading.value = false
      refreshCaptcha()
      uni.showToast({ title: '网络错误，请检查后端是否启动', icon: 'none' })
    }
  })
}
</script>

<style scoped lang="scss">
.page {
  min-height: 100vh;
  background: linear-gradient(180deg, #e6f4ff 0%, #f6f7fb 40%);
  display: flex;
  flex-direction: column;
  padding-top: 120rpx;
}

.logo-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 48rpx;
}

.logo-icon {
  font-size: 80rpx;
  margin-bottom: 16rpx;
}

.logo-title {
  font-size: 48rpx;
  font-weight: 700;
  color: #1f2329;
  margin-bottom: 8rpx;
}

.logo-subtitle {
  font-size: 26rpx;
  color: #999;
  letter-spacing: 4rpx;
}

.form-group {
  margin-bottom: 28rpx;
}

.form-label {
  display: block;
  font-size: 28rpx;
  font-weight: 500;
  color: #1f2329;
  margin-bottom: 12rpx;
}

.form-input {
  width: 100%;
  height: 88rpx;
  background: #f6f7fb;
  border: 2rpx solid #e5e6eb;
  border-radius: 12rpx;
  padding: 0 24rpx;
  font-size: 28rpx;
  color: #1f2329;
  box-sizing: border-box;
}

.captcha-row {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 16rpx;
}

.captcha-input {
  flex: 1;
}

.captcha-img {
  width: 220rpx;
  height: 88rpx;
  border-radius: 12rpx;
  border: 2rpx solid #e5e6eb;
  flex-shrink: 0;
  background: #ffffff;
}

// 登录按钮（view模拟）
.login-btn {
  width: 100%;
  height: 88rpx;
  background: #1677ff;
  color: #ffffff;
  font-size: 32rpx;
  font-weight: 500;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 16rpx;

  // 点击态
  &:active {
    background: #0958d9;
  }
}

.login-tips {
  margin-top: 24rpx;
  text-align: center;
}

.tip-text {
  font-size: 24rpx;
  color: #c0c4cc;
}

.footer {
  margin-top: auto;
  padding: 48rpx 0 32rpx;
  text-align: center;
}

.footer-text {
  font-size: 24rpx;
  color: #c0c4cc;
}
</style>
