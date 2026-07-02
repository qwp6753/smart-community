<template>
  <div>
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="4" v-for="item in statItems" :key="item.label">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-value">{{ item.value }}</div>
            <div class="stat-label">{{ item.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card>
          <template #header>系统概览</template>
          <div ref="chartRef" style="height: 350px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>快捷入口</template>
          <el-row :gutter="12">
            <el-col :span="8" v-for="item in quickLinks" :key="item.label">
              <el-card shadow="hover" class="quick-card" @click="$router.push(item.path)">
                <el-icon :size="32"><component :is="item.icon" /></el-icon>
                <div>{{ item.label }}</div>
              </el-card>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getStats } from '@/api/dashboard'
import * as echarts from 'echarts'

const chartRef = ref()
const statItems = ref([
  { label: '小区总数', value: 0 },
  { label: '居民总数', value: 0 },
  { label: '摄像头数', value: 0 },
  { label: '出入记录', value: 0 },
  { label: '访客数', value: 0 }
])

const quickLinks = [
  { label: '小区管理', icon: 'House', path: '/property/communities' },
  { label: '居民管理', icon: 'Avatar', path: '/property/persons' },
  { label: '出入记录', icon: 'Tickets', path: '/access/records' },
  { label: '访客登记', icon: 'Notebook', path: '/access/visitors' },
  { label: '摄像头', icon: 'VideoCamera', path: '/property/cameras' },
  { label: '用户管理', icon: 'User', path: '/system/users' }
]

onMounted(async () => {
  try {
    const res = await getStats()
    const data = res.data
    statItems.value[0].value = data.communityCount || 0
    statItems.value[1].value = data.personCount || 0
    statItems.value[2].value = data.cameraCount || 0
    statItems.value[3].value = data.recordCount || 0
    statItems.value[4].value = data.visitorCount || 0

    const chart = echarts.init(chartRef.value)
    chart.setOption({
      tooltip: { trigger: 'item' },
      series: [
        {
          name: '数据概览',
          type: 'pie',
          radius: ['40%', '70%'],
          data: [
            { value: statItems.value[0].value, name: '小区总数' },
            { value: statItems.value[1].value, name: '居民总数' },
            { value: statItems.value[2].value, name: '摄像头数' },
            { value: statItems.value[3].value, name: '出入记录' },
            { value: statItems.value[4].value, name: '访客数' }
          ]
        }
      ]
    })
  } catch (e) {
    console.error(e)
  }
})
</script>

<style scoped>
.stat-cards .stat-item {
  text-align: center;
  padding: 10px 0;
}
.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #409EFF;
}
.stat-label {
  margin-top: 8px;
  color: #909399;
}
.quick-card {
  text-align: center;
  cursor: pointer;
  margin-bottom: 12px;
}
.quick-card:hover {
  background-color: #ecf5ff;
}
</style>
