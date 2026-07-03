<template>
  <view class="page">
    <view class="card">
      <view class="card-title">访客登记</view>
      <input class="input" v-model="form.name" placeholder="访客姓名" />
      <view style="height: 12px" />
      <input class="input" v-model="form.mobile" placeholder="手机号" />
      <view style="height: 12px" />
      <input class="input" v-model="form.idCard" placeholder="身份证号" />
      <view style="height: 12px" />
      <input class="input" v-model="form.reason" placeholder="来访原因" />
      <view style="height: 16px" />
      <button class="btn-primary" :loading="loading" @click="submit">提交登记</button>
    </view>
  </view>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { post } from '@/utils/request'

const loading = ref(false)
const form = reactive({
  name: '',
  mobile: '',
  idCard: '',
  reason: '',
  communityId: 1,
  personId: null
})

const submit = async () => {
  if (!form.name) {
    uni.showToast({ title: '请输入访客姓名', icon: 'none' })
    return
  }
  loading.value = true
  try {
    await post('/visitors', form)
    uni.showToast({ title: '登记成功', icon: 'success' })
    uni.navigateBack()
  } catch (e) {
    uni.showToast({ title: '提交失败，请重试', icon: 'none' })
  } finally {
    loading.value = false
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
</style>
