<template>
  <view class="page">
    <view class="card">
      <view class="card-title">小区地图</view>
      <map
        class="map"
        :latitude="latitude"
        :longitude="longitude"
        :markers="markers"
        scale="13"
        show-location
      />
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { get } from '@/utils/request'

const latitude = ref(39.996)
const longitude = ref(116.480)
const markers = ref([])

onMounted(async () => {
  try {
    const res = await get('/map/config')
    const data = res.data
    if (data.centerLat) latitude.value = parseFloat(data.centerLat)
    if (data.centerLng) longitude.value = parseFloat(data.centerLng)
    if (data.markers && Array.isArray(data.markers)) {
      markers.value = data.markers.map((m, i) => ({
        id: i,
        latitude: m.lat,
        longitude: m.lng,
        title: m.name,
        iconPath: '',
        width: 30,
        height: 30,
        callout: {
          content: m.name,
          fontSize: 14,
          borderRadius: 8,
          padding: 8,
          display: 'ALWAYS'
        }
      }))
    }
  } catch (e) { }
})
</script>

<style scoped lang="scss">
.page {
  padding: 0;
  height: 100vh;
}
.card {
  height: 100%;
  display: flex;
  flex-direction: column;
}
.card-title {
  font-size: 32rpx;
  font-weight: 600;
  padding: 24rpx 32rpx;
  background: #fff;
}
.map {
  flex: 1;
  width: 100%;
}
</style>
