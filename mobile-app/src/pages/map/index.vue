<template>
  <view class="page">
    <view class="card-title">小区地图</view>
    <!-- id 在 H5 模式下会渲染到 DOM 上，供 AMap 挂载 -->
    <view id="amapContainer" class="map-container"></view>
  </view>
</template>

<script setup>
import { onMounted, onUnmounted } from 'vue'
import { get } from '@/utils/request'

let map = null

const loadMap = async () => {
  // 1. 获取地图配置
  let config
  try {
    const res = await get('/map/config')
    config = res.data
  } catch (e) {
    uni.showToast({ title: '地图配置加载失败', icon: 'none' })
    return
  }

  if (!config.ak) {
    uni.showToast({ title: '地图密钥未配置', icon: 'none' })
    return
  }

  // 2. 等待 DOM 容器（uni-app H5 会把 view 渲染为 div）
  await new Promise((resolve) => {
    const check = () => {
      if (document.getElementById('amapContainer')) {
        resolve()
      } else {
        setTimeout(check, 50)
      }
    }
    check()
  })

  // 3. 加载 AMap SDK（安全密钥必须在脚本加载前设置）
  await new Promise((resolve) => {
    if (window.AMap && window.AMap.Map) {
      resolve()
      return
    }
    if (config.securityJsCode) {
      window._AMapSecurityConfig = { securityJsCode: config.securityJsCode }
    }
    window.__amapReady = () => {
      resolve()
    }
    const script = document.createElement('script')
    script.src = `https://webapi.amap.com/maps?v=2.0&key=${config.ak}&callback=__amapReady&plugin=AMap.InfoWindow`
    script.async = true
    document.head.appendChild(script)
  })

  // 4. 创建地图
  const centerLng = parseFloat(config.centerLng) || 116.397428
  const centerLat = parseFloat(config.centerLat) || 39.90923

  map = new window.AMap.Map('amapContainer', {
    zoom: 13,
    center: [centerLng, centerLat],
    resizeEnable: true
  })

  // 5. 放置小区标记
  const markerList = config.markers || []
  if (markerList.length === 0) return

  const points = []

  markerList.forEach((m) => {
    const lng = parseFloat(m.lng)
    const lat = parseFloat(m.lat)
    if (isNaN(lng) || isNaN(lat)) return
    points.push([lng, lat])

    // 标签文本
    const labelDiv = `<div style="
      background:#2b85e4;
      color:#fff;
      padding:4px 10px;
      border-radius:4px;
      font-size:12px;
      white-space:nowrap;
      box-shadow:0 1px 4px rgba(0,0,0,.3);
    ">${m.name}</div>`

    const marker = new window.AMap.Marker({
      position: [lng, lat],
      title: m.name,
      animation: 'AMAP_ANIMATION_DROP',
      label: { content: labelDiv, direction: 'top' }
    })
    marker.setMap(map)

    // 点击弹信息窗
    marker.on('click', () => {
      const info = new window.AMap.InfoWindow({
        content: `<div style="padding:6px 10px;font-size:13px">
          <div style="font-weight:bold;margin-bottom:4px">${m.name}</div>
          <div style="color:#666">${m.address || ''}</div>
        </div>`,
        offset: [0, -30]
      })
      info.open(map, marker.getPosition())
    })
  })

  // 自适应视野
  if (points.length > 0) {
    map.setFitView(points.map(p => new window.AMap.LngLat(p[0], p[1])), false, [60, 100, 60, 60])
  }
}

onMounted(() => {
  // 延迟确保 DOM 渲染完成
  setTimeout(loadMap, 200)
})

onUnmounted(() => {
  if (map) {
    map.destroy()
    map = null
  }
})
</script>

<style scoped lang="scss">
.page {
  padding: 0;
  height: 100vh;
  display: flex;
  flex-direction: column;
}
.card-title {
  font-size: 32rpx;
  font-weight: 600;
  padding: 24rpx 32rpx;
  background: #fff;
  flex-shrink: 0;
}
.map-container {
  flex: 1;
  width: 100%;
}
</style>
