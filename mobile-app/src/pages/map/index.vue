<template>
  <view class="page">
    <!-- H5 平台：高德地图 JS API -->
    <!-- #ifdef H5 -->
    <view id="amapContainer" class="map-container-h5"></view>
    <!-- #endif -->

    <!-- 微信小程序平台：原生 map 组件 -->
    <!-- #ifdef MP-WEIXIN -->
    <map
      class="map-container-mp"
      :latitude="centerLat"
      :longitude="centerLng"
      :markers="mpMarkers"
      :scale="14"
      :show-location="false"
      @markertap="onMarkerTap"
    />
    <!-- #endif -->

    <!-- 标记点列表 -->
    <view class="marker-list" v-if="markers.length > 0">
      <view class="marker-list__header">
        <text class="marker-list__title">小区列表</text>
        <text class="marker-list__count">共 {{ markers.length }} 个</text>
      </view>
      <scroll-view scroll-y class="marker-list__scroll">
        <view
          v-for="(m, i) in markers"
          :key="i"
          class="marker-item"
          @click="focusMarker(m, i)"
        >
          <view class="marker-item__dot">{{ i + 1 }}</view>
          <view class="marker-item__info">
            <text class="marker-item__name">{{ m.name }}</text>
            <text class="marker-item__addr">{{ m.address || '-' }}</text>
          </view>
          <text class="marker-item__arrow">📍</text>
        </view>
      </scroll-view>
    </view>

    <AppEmpty v-if="!loading && markers.length === 0" icon="🗺️" message="暂无小区位置数据" />
  </view>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { getMapConfig } from '@/api/face'
import AppEmpty from '@/components/AppEmpty.vue'

const loading = ref(true)
const centerLng = ref(116.397428)
const centerLat = ref(39.90923)
const markers = ref([])

// 微信小程序 markers 格式
const mpMarkers = ref([])

// H5 地图实例
let amapInstance = null

const loadConfig = async () => {
  loading.value = true
  try {
    const res = await getMapConfig()
    const config = res.data

    if (config.centerLng) centerLng.value = parseFloat(config.centerLng)
    if (config.centerLat) centerLat.value = parseFloat(config.centerLat)

    const list = config.markers || []
    markers.value = list

    // 构建小程序 markers
    mpMarkers.value = list.map((m, i) => ({
      id: i,
      latitude: parseFloat(m.lat),
      longitude: parseFloat(m.lng),
      title: m.name,
      iconPath: '/static/tabbar/home-active.png', // 使用已有图标作为标记点
      width: 30,
      height: 30,
      callout: {
        content: m.name + (m.address ? '\n' + m.address : ''),
        fontSize: 12,
        borderRadius: 8,
        padding: 8,
        display: 'BYCLICK'
      }
    }))

    // H5 平台初始化地图
    // #ifdef H5
    if (config.ak) {
      await initAmap(config)
    }
    // #endif
  } catch (e) {
    uni.showToast({ title: '地图配置加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

// ═══ H5：高德地图 JS API 初始化 ═══
// #ifdef H5
const initAmap = async (config) => {
  // 等待 DOM 容器
  await new Promise((resolve) => {
    const check = () => {
      if (document.getElementById('amapContainer')) resolve()
      else setTimeout(check, 50)
    }
    check()
  })

  // 加载 SDK
  await new Promise((resolve) => {
    if (window.AMap && window.AMap.Map) { resolve(); return }
    if (config.securityJsCode) {
      window._AMapSecurityConfig = { securityJsCode: config.securityJsCode }
    }
    window.__amapReady = () => resolve()
    const script = document.createElement('script')
    script.src = `https://webapi.amap.com/maps?v=2.0&key=${config.ak}&callback=__amapReady&plugin=AMap.InfoWindow`
    script.async = true
    document.head.appendChild(script)
  })

  // 创建地图
  amapInstance = new window.AMap.Map('amapContainer', {
    zoom: 13,
    center: [centerLng.value, centerLat.value],
    resizeEnable: true
  })

  // 放置标记
  const points = []
  markers.value.forEach((m) => {
    const lng = parseFloat(m.lng)
    const lat = parseFloat(m.lat)
    if (isNaN(lng) || isNaN(lat)) return
    points.push([lng, lat])

    const labelDiv = `<div style="
      background:#1677ff;color:#fff;padding:4px 12px;border-radius:6px;
      font-size:12px;white-space:nowrap;box-shadow:0 2px 8px rgba(0,0,0,.2);
    ">${m.name}</div>`

    const marker = new window.AMap.Marker({
      position: [lng, lat],
      title: m.name,
      animation: 'AMAP_ANIMATION_DROP',
      label: { content: labelDiv, direction: 'top' }
    })
    marker.setMap(amapInstance)

    marker.on('click', () => {
      const info = new window.AMap.InfoWindow({
        content: `<div style="padding:8px 12px;font-size:13px">
          <div style="font-weight:bold;margin-bottom:4px">${m.name}</div>
          <div style="color:#666">${m.address || ''}</div>
        </div>`,
        offset: [0, -30]
      })
      info.open(amapInstance, marker.getPosition())
    })
  })

  if (points.length > 0) {
    amapInstance.setFitView(points.map(p => new window.AMap.LngLat(p[0], p[1])), false, [60, 100, 60, 60])
  }
}
// #endif

// ═══ 微信小程序标记点击 ═══
const onMarkerTap = (e) => {
  const markerId = e.detail.markerId
  const m = markers.value[markerId]
  if (m) {
    uni.showModal({
      title: m.name,
      content: m.address || '暂无地址信息',
      showCancel: false,
      confirmText: '关闭'
    })
  }
}

// ═══ 从列表定位到标记 ═══
const focusMarker = (m, index) => {
  centerLng.value = parseFloat(m.lng)
  centerLat.value = parseFloat(m.lat)

  // #ifdef H5
  if (amapInstance) {
    amapInstance.setZoomAndCenter(16, [parseFloat(m.lng), parseFloat(m.lat)])
  }
  // #endif

  // #ifdef MP-WEIXIN
  uni.showToast({ title: `已定位到：${m.name}`, icon: 'none' })
  // #endif
}

onMounted(() => {
  setTimeout(loadConfig, 200)
})

onUnmounted(() => {
  // #ifdef H5
  if (amapInstance) {
    amapInstance.destroy()
    amapInstance = null
  }
  // #endif
})
</script>

<style scoped lang="scss">
.page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f6f7fb;
}

// H5 地图容器
.map-container-h5 {
  flex: 1;
  width: 100%;
  min-height: 400rpx;
}

// 小程序地图容器
.map-container-mp {
  width: 100%;
  height: 500rpx;
  flex-shrink: 0;
}

// 标记点列表
.marker-list {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #ffffff;
  border-radius: 20rpx 20rpx 0 0;
  margin-top: -20rpx;
  overflow: hidden;
  box-shadow: 0 -4rpx 20rpx rgba(0,0,0,0.06);
}

.marker-list__header {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  padding: 28rpx 32rpx 20rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.marker-list__title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1f2329;
}

.marker-list__count {
  font-size: 24rpx;
  color: #999;
}

.marker-list__scroll {
  flex: 1;
  padding: 0 32rpx;
}

.marker-item {
  display: flex;
  flex-direction: row;
  align-items: center;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #f5f5f5;

  &:last-child { border-bottom: none; }
}

.marker-item__dot {
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  background: #1677ff;
  color: #ffffff;
  font-size: 24rpx;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
  flex-shrink: 0;
}

.marker-item__info {
  flex: 1;
}

.marker-item__name {
  display: block;
  font-size: 28rpx;
  color: #1f2329;
  font-weight: 500;
  margin-bottom: 4rpx;
}

.marker-item__addr {
  font-size: 24rpx;
  color: #999;
}

.marker-item__arrow {
  font-size: 28rpx;
  color: #c0c4cc;
}
</style>
