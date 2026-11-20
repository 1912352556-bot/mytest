<script setup lang="ts">
/**
 * 配送轨迹页面
 */
import { ref, onMounted, computed, onUnmounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getDeliveryTaskDetail, getDeliveryTrack } from '@/api/delivery'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const taskDetail = ref<DeliveryTask | null>(null)
const trackList = ref<DeliveryTrack[]>([])
const mapContainer = ref<HTMLElement>()
const mapLoaded = ref(false)

let mapInstance: any = null
let pathLine: any = null
let startMarker: any = null
let endMarker: any = null

const taskId = computed(() => route.params.id as string)

/* 获取配送任务详情 */
const fetchTaskDetail = async () => {
  if (!taskId.value) return
  loading.value = true
  try {
    const res = await getDeliveryTaskDetail(taskId.value)
    taskDetail.value = res.data.data
  } catch (error) {
    ElMessage.error('获取配送任务详情失败')
    console.error('获取配送任务详情失败:', error)
  } finally {
    loading.value = false
  }
}

/* 获取配送轨迹 */
const fetchTrack = async () => {
  if (!taskId.value) return
  try {
    const res = await getDeliveryTrack(taskId.value)
    trackList.value = res.data.data || []
  } catch (error) {
    console.error('获取配送轨迹失败:', error)
    trackList.value = []
  }
}

/* 状态文本 */
const statusText = (status: number) => {
  const map: Record<number, string> = { 0: '待取货', 1: '配送中', 2: '已送达', 3: '异常' }
  return map[status] || '未知'
}

const goBack = () => {
  router.push('/delivery/task')
}

/**
 * 生成固定起点 - 基于订单ID生成确定性随机数
 */
const generateFixedStart = (endLng: number, endLat: number, seed: string): [number, number] => {
  // 使用订单ID作为种子，生成固定的随机数
  const hashCode = (str: string) => {
    let hash = 0
    for (let i = 0; i < str.length; i++) {
      const char = str.charCodeAt(i)
      hash = ((hash << 5) - hash) + char
      hash = hash & hash
    }
    return Math.abs(hash)
  }

  // 生成固定的距离和角度
  const seedNum = hashCode(seed)
  const distanceKm = 1 + (seedNum % 400) / 100 // 1-5公里
  const angle = (seedNum % 360) // 0-360度

  // 地球半径（公里）
  const earthRadius = 6371

  // 转换为弧度
  const radLat = (endLat * Math.PI) / 180
  const radLng = (endLng * Math.PI) / 180
  const radAngle = (angle * Math.PI) / 180

  // 计算起点坐标
  const radDistance = distanceKm / earthRadius
  const startLat = Math.asin(
    Math.sin(radLat) * Math.cos(radDistance) +
    Math.cos(radLat) * Math.sin(radDistance) * Math.cos(radAngle)
  )
  const startLng =
    radLng +
    Math.atan2(
      Math.sin(radAngle) * Math.sin(radDistance) * Math.cos(radLat),
      Math.cos(radDistance) - Math.sin(radLat) * Math.sin(startLat)
    )

  return [
    parseFloat(((startLng * 180) / Math.PI).toFixed(6)),
    parseFloat(((startLat * 180) / Math.PI).toFixed(6))
  ]
}

/**
 * 生成配送轨迹路径点
 */
const generateTrackPoints = (startLng: number, startLat: number, endLng: number, endLat: number) => {
  const points: [number, number][] = []
  // 生成10-20个轨迹点
  const numPoints = 10 + Math.floor(Math.random() * 11)

  for (let i = 0; i <= numPoints; i++) {
    const ratio = i / numPoints
    // 基础路径（线性插值）
    let lng = startLng + (endLng - startLng) * ratio
    let lat = startLat + (endLat - startLat) * ratio

    // 添加随机偏移，让路径看起来更自然
    if (i > 0 && i < numPoints) {
      const offset = 0.002 * Math.sin(i * 0.5) // 轻微的正弦波动
      lng += offset
      lat += offset * 0.5
    }

    points.push([lng, lat])
  }

  return points
}

/* 地理编码 - 根据地址获取坐标 */
const geocodeAddress = (address: string): Promise<[number, number] | null> => {
  return new Promise((resolve) => {
    if (typeof AMap === 'undefined') {
      resolve(null)
      return
    }

    AMap.plugin('AMap.Geocoder', () => {
      const geocoder = new AMap.Geocoder()
      geocoder.getLocation(address, (status: string, result: any) => {
        if (status === 'complete' && result.geocodes && result.geocodes.length > 0) {
          const location = result.geocodes[0].location
          resolve([location.getLng(), location.getLat()])
        } else {
          console.error('地理编码失败:', result)
          resolve(null)
        }
      })
    })
  })
}

/* 初始化地图 - 使用驾车路线规划 */
const initMap = async () => {
  if (!mapContainer.value || typeof AMap === 'undefined') {
    console.error('地图容器或SDK未加载')
    return
  }

  // 销毁旧地图
  if (mapInstance) {
    mapInstance.destroy()
    mapInstance = null
  }

  // 终点坐标
  let endLng: number
  let endLat: number
  let center: [number, number]

  // 优先使用已有的坐标，否则进行地理编码
  if (taskDetail.value?.longitude && taskDetail.value?.latitude) {
    endLng = taskDetail.value.longitude
    endLat = taskDetail.value.latitude
    center = [endLng, endLat]
  } else if (taskDetail.value?.deliveryAddress) {
    // 使用地理编码获取地址坐标
    ElMessage.info('正在定位地址...')
    const coords = await geocodeAddress(taskDetail.value.deliveryAddress)
    if (coords) {
      endLng = coords[0]
      endLat = coords[1]
      center = [endLng, endLat]
      // 更新到任务详情中
      taskDetail.value.longitude = endLng
      taskDetail.value.latitude = endLat
    } else {
      // 地理编码失败，使用默认位置
      ElMessage.warning('地址定位失败，使用默认位置')
      center = [116.397428, 39.90923]
      endLng = 116.397428
      endLat = 39.90923
    }
  } else {
    center = [116.397428, 39.90923]
    endLng = 116.397428
    endLat = 39.90923
  }

  // 创建地图
  mapInstance = new AMap.Map(mapContainer.value, {
    zoom: 14,
    center: new AMap.LngLat(center[0], center[1]),
    viewMode: '2D'
  })

  // 生成固定起点 - 使用订单ID作为种子，同一订单始终相同
  const seed = taskDetail.value?.orderId?.toString() || taskDetail.value?.deliveryNo || 'default'
  const [startLng, startLat] = generateFixedStart(endLng, endLat, seed)

  // 添加起点和终点标记 - 使用自定义HTML内容
  startMarker = new AMap.Marker({
    position: new AMap.LngLat(startLng, startLat),
    content: `<div style="
      width: 32px;
      height: 32px;
      background: #67C23A;
      border-radius: 50% 50% 50% 0;
      transform: rotate(-45deg);
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 0 2px 6px rgba(0,0,0,0.3);
      border: 2px solid #fff;
    "><span style="
      color: #fff;
      font-size: 14px;
      font-weight: bold;
      transform: rotate(45deg);
    ">起</span></div>`,
    title: '取货点',
    offset: new AMap.Pixel(-16, -32)
  })

  endMarker = new AMap.Marker({
    position: new AMap.LngLat(endLng, endLat),
    content: `<div style="
      width: 32px;
      height: 32px;
      background: #16A34A;
      border-radius: 50% 50% 50% 0;
      transform: rotate(-45deg);
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 0 2px 6px rgba(0,0,0,0.3);
      border: 2px solid #fff;
    "><span style="
      color: #fff;
      font-size: 14px;
      font-weight: bold;
      transform: rotate(45deg);
    ">终</span></div>`,
    title: '配送终点',
    offset: new AMap.Pixel(-16, -32)
  })

  mapInstance.add([startMarker, endMarker])

  // 使用驾车路线规划获取实际道路路径
  AMap.plugin('AMap.Driving', () => {
    const driving = new AMap.Driving({
      map: mapInstance,
      showTraffic: false,
      hideMarkers: true // 隐藏默认标记
    })

    driving.search(
      new AMap.LngLat(startLng, startLat),
      new AMap.LngLat(endLng, endLat),
      { waypoints: [] },
      (status: string, result: any) => {
        if (status === 'complete' && result.routes && result.routes.length > 0) {
          // 获取实际路线
          const route = result.routes[0]
          const distance = (route.distance / 1000).toFixed(2)
          const time = Math.round(route.time / 60)

          // 添加信息窗体
          const startInfo = new AMap.InfoWindow({
            content: `<div style="padding:8px;"><b>取货点</b>距终点约${distance}公里</div>`
          })
          startInfo.open(mapInstance, new AMap.LngLat(startLng, startLat))

          const endInfo = new AMap.InfoWindow({
            content: `<div style="padding:8px;"><b>配送终点</b><br/>${taskDetail.value?.deliveryAddress || '客户地址'}<br/>预计${time}分钟</div>`
          })
          endInfo.open(mapInstance, new AMap.LngLat(endLng, endLat))

          // 更新距离信息
          if (taskDetail.value) {
            taskDetail.value.distance = distance
            taskDetail.value.estimatedTime = time
          }
        } else {
          // 路线规划失败，使用备用直线
          console.warn('驾车路线规划失败，使用备用直线')
          const backupPath = [
            new AMap.LngLat(startLng, startLat),
            new AMap.LngLat(endLng, endLat)
          ]
          pathLine = new AMap.Polyline({
            path: backupPath,
            strokeColor: '#16A34A',
            strokeWeight: 6,
            strokeOpacity: 0.8
          })
          mapInstance.add(pathLine)

          const startInfo = new AMap.InfoWindow({
            content: `<div style="padding:8px;"><b>取货点</b>`
          })
          startInfo.open(mapInstance, new AMap.LngLat(startLng, startLat))

          const endInfo = new AMap.InfoWindow({
            content: `<div style="padding:8px;"><b>配送终点</b><br/>${taskDetail.value?.deliveryAddress || '客户地址'}</div>`
          })
          endInfo.open(mapInstance, new AMap.LngLat(endLng, endLat))
        }

        // 调整视野
        mapInstance.setFitView()
        mapLoaded.value = true
      }
    )
  })
}

/**
 * 计算两点之间的距离（公里）
 */
const calculateDistance = (lng1: number, lat1: number, lng2: number, lat2: number) => {
  const R = 6371 // 地球半径（公里）
  const dLat = ((lat2 - lat1) * Math.PI) / 180
  const dLng = ((lng2 - lng1) * Math.PI) / 180
  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos((lat1 * Math.PI) / 180) * Math.cos((lat2 * Math.PI) / 180) *
    Math.sin(dLng / 2) * Math.sin(dLng / 2)
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
  return R * c
}

onMounted(async () => {
  await fetchTaskDetail()
  await fetchTrack()
  // 延迟初始化地图
  setTimeout(initMap, 300)
})

onUnmounted(() => {
  if (mapInstance) {
    mapInstance.destroy()
    mapInstance = null
  }
})

// 监听路由参数变化，重新加载数据
watch(() => route.params.id, async (newId) => {
  if (newId) {
    // 销毁旧地图
    if (mapInstance) {
      mapInstance.destroy()
      mapInstance = null
    }
    mapLoaded.value = false

    // 重新获取数据
    await fetchTaskDetail()
    await fetchTrack()

    // 重新初始化地图
    setTimeout(initMap, 300)
  }
})
</script>

<template>
  <div class="delivery-track-container" v-loading="loading">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>配送轨迹</span>
          <el-button @click="goBack">返回列表</el-button>
        </div>
      </template>

      <!-- 配送任务信息 -->
      <template v-if="taskDetail">
        <el-descriptions :column="3" border class="mb20">
          <el-descriptions-item label="配送编号">{{ taskDetail.deliveryNo }}</el-descriptions-item>
          <el-descriptions-item label="订单编号">{{ taskDetail.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="配送状态">
            <el-tag>{{ statusText(taskDetail.deliveryStatus) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="配送员">{{ taskDetail.deliveryPersonName }}</el-descriptions-item>
          <el-descriptions-item label="客户">{{ taskDetail.customerName }}</el-descriptions-item>
          <el-descriptions-item label="客户电话">{{ taskDetail.customerPhone }}</el-descriptions-item>
          <el-descriptions-item label="配送地址" :span="3">{{ taskDetail.deliveryAddress }}</el-descriptions-item>
          <el-descriptions-item label="取货时间">{{ taskDetail.pickupTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="送达时间">{{ taskDetail.deliveryTime || '-' }}</el-descriptions-item>
        </el-descriptions>
      </template>

      <!-- 地图轨迹 -->
      <el-divider content-position="left">轨迹地图</el-divider>
      <div ref="mapContainer" class="track-map"></div>
      <div class="route-info" v-if="taskDetail?.distance">
        <el-tag type="success">预计距离：{{ taskDetail.distance }} 公里</el-tag>
        <el-tag type="primary">预计时间：{{ taskDetail.estimatedTime }} 分钟</el-tag>
      </div>
      <div class="map-legend">
        <span class="legend-item"><span class="dot start"></span> 取货点</span>
        <span class="legend-item"><span class="dot end"></span> 配送终点（客户地址）</span>
        <span class="legend-item"><span class="line"></span> 配送路线</span>
      </div>

      <!-- 轨迹时间线 -->
      <!-- <el-divider content-position="left">配送轨迹</el-divider>
      <el-timeline v-if="trackList.length > 0">
        <el-timeline-item
          v-for="track in trackList"
          :key="track.id"
          :timestamp="track.recordTime"
          placement="top"
        >
          <el-card shadow="never">
            <p><el-icon><Location /></el-icon> {{ track.address || '未知位置' }}</p>
            <p class="coordinates">经度: {{ track.longitude }}, 纬度: {{ track.latitude }}</p>
          </el-card>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-else description="暂无轨迹数据" /> -->
    </el-card>
  </div>
</template>

<style scoped lang="scss">
.delivery-track-container {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    span { font-size: 16px; font-weight: 600; }
  }
  .mb20 { margin-bottom: 20px; }
  .coordinates { color: #909399; font-size: 12px; margin-top: 4px; }

  .track-map {
    width: 100%;
    height: 400px;
    border: 1px solid #e4e7ed;
    border-radius: 4px;
    margin-bottom: 16px;
  }

  .route-info {
    display: flex;
    gap: 16px;
    margin-bottom: 16px;
  }

  .map-legend {
    display: flex;
    gap: 24px;
    margin-bottom: 20px;
    font-size: 14px;
    color: #606266;

    .legend-item {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .dot {
      width: 12px;
      height: 12px;
      border-radius: 50%;

      &.start {
        background: #67C23A;
      }

      &.end {
        background: #16A34A;
      }
    }

    .line {
      width: 24px;
      height: 4px;
      background: #16A34A;
      border-radius: 2px;
    }
  }
}
</style>
