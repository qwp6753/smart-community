<template>
  <view class="page">
    <AppCard title="访客登记">
      <!-- 访客姓名 -->
      <view class="form-group">
        <text class="form-label">访客姓名 <text class="required">*</text></text>
        <input class="form-input" v-model="form.name" placeholder="请输入访客姓名" />
      </view>

      <!-- 手机号 -->
      <view class="form-group">
        <text class="form-label">手机号</text>
        <input class="form-input" v-model="form.mobile" type="number" placeholder="请输入手机号" />
      </view>

      <!-- 身份证号 -->
      <view class="form-group">
        <text class="form-label">身份证号</text>
        <input class="form-input" v-model="form.idCard" placeholder="请输入身份证号" />
      </view>

      <!-- 来访事由 -->
      <view class="form-group">
        <text class="form-label">来访事由</text>
        <input class="form-input" v-model="form.reason" placeholder="如：探亲、送货、维修等" />
      </view>

      <!-- 所属小区 -->
      <view class="form-group">
        <text class="form-label">所属小区</text>
        <picker mode="selector" :range="communityList" range-key="name" @change="onCommunityChange">
          <view class="picker-input">
            <text :class="selectedCommunity ? '' : 'placeholder'">
              {{ selectedCommunity || '请选择小区' }}
            </text>
            <text class="picker-arrow">▼</text>
          </view>
        </picker>
      </view>

      <!-- 提交按钮 -->
      <AppButton type="primary" :loading="loading" @click="submit" style="margin-top: 32rpx;">
        提交登记
      </AppButton>

      <!-- 成功后操作 -->
      <view v-if="success" class="success-actions">
        <AppButton type="outline" @click="resetForm">继续登记</AppButton>
        <AppButton type="text" @click="goVisitorList">查看访客列表 →</AppButton>
      </view>
    </AppCard>
  </view>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { createVisitor } from '@/api/visitor'
import { listCommunities } from '@/api/community'
import AppCard from '@/components/AppCard.vue'
import AppButton from '@/components/AppButton.vue'

const loading = ref(false)
const success = ref(false)
const communityList = ref([])
const selectedCommunity = ref('')

const form = reactive({
  name: '',
  mobile: '',
  idCard: '',
  reason: '',
  communityId: 1,
  personId: null
})

onMounted(async () => {
  try {
    const res = await listCommunities({ current: 1, size: 50 })
    communityList.value = res.data.records || []
    if (communityList.value.length > 0) {
      form.communityId = communityList.value[0].communityId
      selectedCommunity.value = communityList.value[0].name
    }
  } catch (e) { /* 忽略 */ }
})

const onCommunityChange = (e) => {
  const idx = e.detail.value
  const comm = communityList.value[idx]
  if (comm) {
    form.communityId = comm.communityId
    selectedCommunity.value = comm.name
  }
}

const submit = async () => {
  if (!form.name.trim()) {
    uni.showToast({ title: '请输入访客姓名', icon: 'none' })
    return
  }
  loading.value = true
  try {
    await createVisitor({ ...form, name: form.name.trim(), communityId: form.communityId })
    success.value = true
    uni.showToast({ title: '登记成功', icon: 'success' })
  } catch (e) {
    uni.showToast({ title: '提交失败，请重试', icon: 'none' })
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  form.name = ''
  form.mobile = ''
  form.idCard = ''
  form.reason = ''
  success.value = false
}

const goVisitorList = () => {
  uni.navigateTo({ url: '/pages/visitor/list' })
}
</script>

<style scoped lang="scss">
.page {
  padding: 24rpx;
  min-height: 100vh;
  background: #f6f7fb;
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

.required { color: #ff4d4f; }

.form-input {
  width: 100%;
  height: 80rpx;
  background: #f6f7fb;
  border: 2rpx solid #e5e6eb;
  border-radius: 12rpx;
  padding: 0 24rpx;
  font-size: 28rpx;
  color: #1f2329;
  box-sizing: border-box;
}

.picker-input {
  width: 100%;
  height: 80rpx;
  background: #f6f7fb;
  border: 2rpx solid #e5e6eb;
  border-radius: 12rpx;
  padding: 0 24rpx;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  box-sizing: border-box;
  font-size: 28rpx;
  color: #1f2329;

  .placeholder { color: #c0c4cc; }
  .picker-arrow { font-size: 24rpx; color: #999; }
}

.success-actions {
  margin-top: 24rpx;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}
</style>
