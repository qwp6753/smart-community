<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stat-cards">
      <el-col :span="4" v-for="item in statCards" :key="item.label" :style="{ minWidth: '180px' }">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-icon" :style="{ color: item.color }">
              <el-icon :size="28"><component :is="item.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value" :style="{ color: item.color }">{{ item.value }}</div>
              <div class="stat-label">{{ item.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表行：出入趋势 + 人员类型 -->
    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :xs="24" :lg="16">
        <el-card>
          <template #header>
            <div class="chart-header">
              <span>出入趋势</span>
              <span class="chart-subtitle">最近7天</span>
            </div>
          </template>
          <div ref="trendChartRef" style="height: 360px"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="8">
        <el-card>
          <template #header>
            <div class="chart-header">
              <span>人员类型分布</span>
            </div>
          </template>
          <div ref="personTypeChartRef" style="height: 360px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 小区居民分布 -->
    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="chart-header">
              <span>各小区居民数量</span>
            </div>
          </template>
          <div ref="communityChartRef" style="height: 320px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷入口 -->
    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="24">
        <el-card>
          <template #header>快捷入口</template>
          <el-row :gutter="12">
            <el-col :xs="12" :sm="8" :md="4" v-for="item in quickLinks" :key="item.label">
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
import { ref, onMounted, onUnmounted } from 'vue'
import { getDashboard, getRecordTrend, getPersonType, getCommunityPerson } from '@/api/dashboard'
import * as echarts from 'echarts'

// ---- 图表容器引用 ----
const trendChartRef = ref()
const personTypeChartRef = ref()
const communityChartRef = ref()

let trendChart = null
let personTypeChart = null
let communityChart = null

// ---- 统计卡片 ----
const statCards = ref([
  { label: '小区总数', value: 0, icon: 'House', color: '#409EFF' },
  { label: '居民总数', value: 0, icon: 'Avatar', color: '#67C23A' },
  { label: '今日进入', value: 0, icon: 'TurnOn', color: '#E6A23C' },
  { label: '今日离开', value: 0, icon: 'TurnOff', color: '#F56C6C' },
  { label: '访客总数', value: 0, icon: 'Notebook', color: '#909399' }
])

const quickLinks = [
  { label: '小区管理', icon: 'House', path: '/property/communities' },
  { label: '居民管理', icon: 'Avatar', path: '/property/persons' },
  { label: '出入记录', icon: 'Tickets', path: '/access/records' },
  { label: '访客登记', icon: 'Notebook', path: '/access/visitors' },
  { label: '摄像头', icon: 'VideoCamera', path: '/property/cameras' },
  { label: '用户管理', icon: 'User', path: '/system/users' }
]

// ---- 窗口resize处理 ----
function onResize() {
  trendChart?.resize()
  personTypeChart?.resize()
  communityChart?.resize()
}

// ---- 初始化 ----
onMounted(async () => {
  await Promise.all([loadDashboard(), loadRecordTrend(), loadPersonType(), loadCommunityPerson()])
  window.addEventListener('resize', onResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', onResize)
  trendChart?.dispose()
  personTypeChart?.dispose()
  communityChart?.dispose()
})

// ---- 加载首页统计 ----
async function loadDashboard() {
  try {
    const data = await getDashboard()
    statCards.value[0].value = data.communityCount ?? 0
    statCards.value[1].value = data.personCount ?? 0
    statCards.value[2].value = data.todayInCount ?? 0
    statCards.value[3].value = data.todayOutCount ?? 0
    statCards.value[4].value = data.visitorCount ?? 0
  } catch (e) {
    console.error('加载首页统计失败', e)
  }
}

// ---- 加载出入趋势 ----
async function loadRecordTrend() {
  try {
    const data = await getRecordTrend(7)
    const dates = data.map(d => d.date)
    const inData = data.map(d => d.inCount)
    const outData = data.map(d => d.outCount)

    trendChart = echarts.init(trendChartRef.value)
    trendChart.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['进入', '离开'], bottom: 0 },
      grid: { left: 50, right: 20, top: 20, bottom: 40 },
      xAxis: { type: 'category', data: dates, axisLabel: { rotate: 30 } },
      yAxis: { type: 'value', minInterval: 1 },
      series: [
        { name: '进入', type: 'line', data: inData, smooth: true, itemStyle: { color: '#67C23A' } },
        { name: '离开', type: 'line', data: outData, smooth: true, itemStyle: { color: '#F56C6C' } }
      ]
    })
  } catch (e) {
    console.error('加载出入趋势失败', e)
  }
}

// ---- 加载人员类型 ----
async function loadPersonType() {
  try {
    const data = await getPersonType()
    const names = data.map(d => d.label)
    const values = data.map(d => d.count)

    personTypeChart = echarts.init(personTypeChartRef.value)
    personTypeChart.setOption({
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      series: [
        {
          type: 'pie',
          radius: ['50%', '75%'],
          center: ['50%', '50%'],
          label: { show: true, formatter: '{b}\n{d}%' },
          data: data.map(d => ({ name: d.label, value: d.count })),
          itemStyle: { borderRadius: 4 }
        }
      ]
    })
  } catch (e) {
    console.error('加载人员类型统计失败', e)
  }
}

// ---- 加载小区居民统计 ----
async function loadCommunityPerson() {
  try {
    const data = await getCommunityPerson()
    const names = data.map(d => d.communityName)
    const values = data.map(d => d.personCount)

    communityChart = echarts.init(communityChartRef.value)
    communityChart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: 60, right: 30, top: 20, bottom: 40 },
      xAxis: { type: 'category', data: names, axisLabel: { rotate: names.length > 5 ? 30 : 0 } },
      yAxis: { type: 'value', minInterval: 1 },
      series: [
        {
          type: 'bar',
          data: values,
          barWidth: '40%',
          itemStyle: {
            borderRadius: [6, 6, 0, 0],
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#409EFF' },
              { offset: 1, color: '#a0cfff' }
            ])
          }
        }
      ]
    })
  } catch (e) {
    console.error('加载小区居民统计失败', e)
  }
}
</script>

<style scoped>
.dashboard {
  padding: 4px 0;
}

/* 统计卡片 */
.stat-cards .el-col {
  margin-bottom: 8px;
}
.stat-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 4px 0;
}
.stat-icon {
  flex-shrink: 0;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  background: #f0f2f5;
}
.stat-info {
  flex: 1;
  min-width: 0;
}
.stat-value {
  font-size: 28px;
  font-weight: 700;
  line-height: 1.2;
}
.stat-label {
  margin-top: 2px;
  font-size: 13px;
  color: #909399;
}

/* 图表头部 */
.chart-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.chart-subtitle {
  font-size: 12px;
  color: #909399;
  font-weight: normal;
}

/* 快捷入口 */
.quick-card {
  text-align: center;
  cursor: pointer;
  margin-bottom: 8px;
  padding: 8px 0;
}
.quick-card:hover {
  background-color: #ecf5ff;
}
.quick-label {
  margin-top: 6px;
  font-size: 13px;
  color: #606266;
}
</style>
