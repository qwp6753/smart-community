<template>
  <div class="dashboard-container">
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="4" v-for="item in statItems" :key="item.label">
        <el-card shadow="hover" class="card-item">
          <div class="stat-item">
            <div class="stat-value">{{ item.value }}</div>
            <div class="stat-label">{{ item.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="14">
        <el-card shadow="hover">
          <template #header><div class="card-title">近7天小区出入流量趋势</div></template>
          <div ref="trendChartRef" style="height: 320px;"></div>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="hover">
          <template #header><div class="card-title">人员类型规模分布</div></template>
          <div ref="typeChartRef" style="height: 320px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="14">
        <el-card shadow="hover">
          <template #header><div class="card-title">各小区常住居民分布</div></template>
          <div ref="communityChartRef" style="height: 300px;"></div>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="hover">
          <template #header><div class="card-title">快捷入口</div></template>
          <el-row :gutter="12">
            <el-col :span="8" v-for="item in quickLinks" :key="item.label">
              <el-card shadow="hover" class="quick-card" @click="$router.push(item.path)">
                <el-icon :size="28"><component :is="item.icon" /></el-icon>
                <div class="quick-label">{{ item.label }}</div>
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

const trendChartRef = ref()
const typeChartRef = ref()
const communityChartRef = ref()

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

    // 1. 设置数字
    statItems.value[0].value = data.communityCount || 0
    statItems.value[1].value = data.personCount || 0
    statItems.value[2].value = data.cameraCount || 0
    statItems.value[3].value = data.recordCount || 0
    statItems.value[4].value = data.visitorCount || 0

    // 2. 渲染折线图 (近7天出入趋势)
    const trendChart = echarts.init(trendChartRef.value)
    trendChart.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['进入人次', '离开人次'] },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'category', boundaryGap: false, data: data.accessTrend?.dates || [] },
      yAxis: { type: 'value' },
      series: [
        { name: '进入人次', type: 'line', smooth: true, itemStyle: { color: '#67C23A' }, data: data.accessTrend?.in || [] },
        { name: '离开人次', type: 'line', smooth: true, itemStyle: { color: '#E6A23C' }, data: data.accessTrend?.out || [] }
      ]
    })

    // 3. 渲染饼图 (人员类型分布)
    const typeChart = echarts.init(typeChartRef.value)
    typeChart.setOption({
      tooltip: { trigger: 'item', formatter: '{b}: {c} 人 ({d}%)' },
      legend: { bottom: '0%', left: 'center' },
      series: [{
        type: 'pie',
        radius: ['45%', '75%'],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
        data: data.personTypeDistribution || []
      }]
    })

    // 4. 渲染柱状图 (小区分布)
    const communityChart = echarts.init(communityChartRef.value)
    const communityNames = (data.communityPersonStats || []).map(item => item.communityName)
    const communityCounts = (data.communityPersonStats || []).map(item => item.count)

    communityChart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'category', data: communityNames, axisLabel: { interval: 0, rotate: 15 } },
      yAxis: { type: 'value' },
      series: [{
        name: '常住人数',
        type: 'bar',
        barWidth: '40%',
        itemStyle: { color: '#409EFF', borderRadius: [4, 4, 0, 0] },
        data: communityCounts
      }]
    })
  } catch (e) {
    console.error(e)
  }
})
</script>

<style scoped>
.dashboard-container {
  padding: 10px;
}
.stat-cards .stat-item {
  text-align: center;
  padding: 5px 0;
}
.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #409EFF;
}
.stat-label {
  margin-top: 8px;
  color: #909399;
  font-size: 14px;
}
.card-title {
  font-weight: bold;
  font-size: 16px;
}
.quick-card {
  text-align: center;
  cursor: pointer;
  margin-bottom: 12px;
  transition: all 0.3s;
}
.quick-card:hover {
  background-color: #ecf5ff;
  transform: translateY(-2px);
}
.quick-label {
  margin-top: 8px;
  font-size: 13px;
  color: #606266;
}
</style>