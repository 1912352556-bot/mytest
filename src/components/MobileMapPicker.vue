<template>
  <van-popup
    v-model:show="visible"
    position="bottom"
    round
    :style="{ height: '100%' }"
    :close-on-click-overlay="false"
    @opened="initMap"
    @closed="destroyMap"
  >
    <div class="map-picker">
      <div class="map-picker__head">
        <span class="map-picker__title">选择配送地址</span>
        <van-icon name="cross" size="18" @click="close" />
      </div>

      <van-search
        v-model="keyword"
        shape="round"
        placeholder="搜索小区、写字楼、门牌号"
        background="transparent"
        @search="searchAddress"
        @update:model-value="onKeywordInput"
      />

      <div class="map-picker__body">
        <div ref="mapEl" class="map-picker__map"></div>

        <!-- 搜索结果浮层 -->
        <div v-if="results.length" class="map-picker__results">
          <div v-for="poi in results" :key="poi.id" class="map-picker__result" @click="pickPoi(poi)">
            <div class="map-picker__result-name">{{ poi.name }}</div>
            <div class="map-picker__result-addr">{{ poi.address || poi.pname + poi.cityname + poi.adname }}</div>
          </div>
        </div>

        <van-loading v-if="locating" class="map-picker__loading" size="20px" vertical>定位中...</van-loading>
      </div>

      <div class="map-picker__selected">
        <van-icon name="location" color="#16a34a" size="18" />
        <span class="map-picker__address">{{ address || '点击地图选择位置' }}</span>
      </div>

      <div class="map-picker__footer">
        <van-button round plain icon="aim" @click="relocate">重新定位</van-button>
        <van-button round type="primary" :disabled="!address" @click="confirm">确定</van-button>
      </div>
    </div>
  </van-popup>
</template>

<script setup lang="ts">
/**
 * 移动端地图选点（高德地图）
 *
 * 用法：
 *   <MobileMapPicker ref="picker" @confirm="onConfirm" />
 *   picker.value?.open()          // 打开并定位到当前位置
 *   picker.value?.open(location)  // 打开并定位到已有坐标
 *
 * 与 PC 端 MapPicker 使用同一套高德地图 SDK（index.html 中的 script 加载，含 Geocoder / PlaceSearch 插件）。
 */
import { onBeforeUnmount, ref } from 'vue'
import { showToast } from 'vant'

const emit = defineEmits<{
  (e: 'confirm', value: LocationInfo): void
}>()

const visible = ref(false)
const keyword = ref('')
const results = ref<any[]>([])
const mapEl = ref<HTMLElement>()

const address = ref('')
const city = ref('')
const district = ref('')
const longitude = ref<number | null>(null)
const latitude = ref<number | null>(null)
const locating = ref(false)

let map: any = null
let marker: any = null
let geocoder: any = null
let searchTimer: ReturnType<typeof setTimeout> | null = null

/** 打开选择器；init 为已有坐标时优先使用 */
const open = (init?: LocationInfo | null) => {
  visible.value = true
  results.value = []
  keyword.value = ''
  if (init && init.longitude && init.latitude) {
    setAddress(init.address || '', init.longitude, init.latitude, init.city, init.district)
  } else {
    address.value = ''
    longitude.value = null
    latitude.value = null
  }
}

const close = () => {
  visible.value = false
}

const setAddress = (
  text: string,
  lng: number,
  lat: number,
  newCity?: string,
  newDistrict?: string
) => {
  address.value = text
  longitude.value = lng
  latitude.value = lat
  city.value = newCity || city.value
  district.value = newDistrict || district.value
}

/** 地图初始化 */
const initMap = () => {
  if (map || !mapEl.value) {
    return
  }
  if (typeof AMap === 'undefined') {
    showToast('地图加载失败，请检查网络')
    return
  }

  geocoder = new AMap.Geocoder({ radius: 200, extensions: 'all' })

  const center =
    longitude.value !== null && latitude.value !== null
      ? [longitude.value, latitude.value]
      : undefined

  map = new AMap.Map(mapEl.value, {
    zoom: 16,
    viewMode: '2D',
    center: center ? new AMap.LngLat(center[0], center[1]) : undefined
  })

  map.on('click', (event: any) => {
    const lng = event.lnglat.getLng()
    const lat = event.lnglat.getLat()
    reverseGeocode(lng, lat)
  })

  if (center) {
    placeMarker(center[0], center[1])
    map.setCenter(center)
  } else {
    locate()
  }
}

/** 逆地理编码：坐标 → 地址 */
const reverseGeocode = (lng: number, lat: number) => {
  if (!geocoder) {
    setAddress(`${lng.toFixed(6)}, ${lat.toFixed(6)}`, lng, lat)
    placeMarker(lng, lat)
    return
  }
  locating.value = true
  geocoder.getAddress([lng, lat], (status: string, result: any) => {
    locating.value = false
    if (status === 'complete' && result.info === 'OK' && result.regeocode) {
      const component = result.regeocode.addressComponent || {}
      setAddress(
        result.regeocode.formattedAddress,
        lng,
        lat,
        component.city || component.province,
        component.district
      )
    } else {
      setAddress(`${lng.toFixed(6)}, ${lat.toFixed(6)}`, lng, lat)
    }
    placeMarker(lng, lat)
    map?.setCenter([lng, lat])
  })
}

/** 放置标记 */
const placeMarker = (lng: number, lat: number) => {
  if (!map) {
    return
  }
  if (marker) {
    map.remove(marker)
  }
  marker = new AMap.Marker({
    position: new AMap.LngLat(lng, lat),
    anchor: 'bottom-center'
  })
  map.add(marker)
}

/** 定位：浏览器定位优先，失败回退到 IP 定位 */
const locate = () => {
  locating.value = true
  const fallback = () => {
    AMap.plugin('AMap.CitySearch', () => {
      const citySearch = new AMap.CitySearch()
      citySearch.getCityByIp((status: string, result: any) => {
        if (status === 'complete' && result.city) {
          geocoder.getLocation(result.city, (status2: string, result2: any) => {
            if (status2 === 'complete' && result2.geocodes?.length) {
              const point = result2.geocodes[0].location
              reverseGeocode(point.getLng(), point.getLat())
            } else {
              reverseGeocode(116.397428, 39.90923)
            }
          })
        } else {
          reverseGeocode(116.397428, 39.90923)
        }
      })
    })
  }

  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(
      (position) => {
        const lng = position.coords.longitude
        const lat = position.coords.latitude
        map?.setCenter([lng, lat])
        reverseGeocode(lng, lat)
      },
      () => fallback(),
      { enableHighAccuracy: false, timeout: 6000 }
    )
  } else {
    fallback()
  }
}

/** 重新定位 */
const relocate = () => {
  locate()
}

/** 关键字搜索地址 */
const onKeywordInput = () => {
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  searchTimer = setTimeout(() => {
    searchAddress()
  }, 400)
}

const searchAddress = () => {
  if (!keyword.value.trim() || typeof AMap === 'undefined') {
    results.value = []
    return
  }
  AMap.plugin('AMap.PlaceSearch', () => {
    const placeSearch = new AMap.PlaceSearch({ pageSize: 10, pageIndex: 1, city: '全国' })
    placeSearch.search(keyword.value.trim(), (status: string, result: any) => {
      if (status === 'complete' && result.info === 'OK' && result.poiList) {
        results.value = result.poiList.pois || []
      } else {
        results.value = []
        showToast('没有搜索到相关地址')
      }
    })
  })
}

/** 选中搜索结果 */
const pickPoi = (poi: any) => {
  const lng = poi.location?.getLng()
  const lat = poi.location?.getLat()
  if (lng === undefined || lat === undefined) {
    return
  }
  const text = `${poi.pname || ''}${poi.cityname || ''}${poi.adname || ''}${poi.address || ''}${
    poi.name || ''
  }`
  setAddress(text, lng, lat)
  results.value = []
  keyword.value = ''
  map?.setZoomAndCenter(16, [lng, lat])
  placeMarker(lng, lat)
  reverseGeocode(lng, lat)
}

const confirm = () => {
  if (longitude.value === null || latitude.value === null || !address.value) {
    showToast('请先选择地址')
    return
  }
  emit('confirm', {
    address: address.value,
    longitude: longitude.value,
    latitude: latitude.value,
    city: city.value,
    district: district.value
  })
  visible.value = false
}

/** 关闭后销毁地图，避免多次打开内存泄漏 */
const destroyMap = () => {
  if (map) {
    map.destroy()
    map = null
    marker = null
    geocoder = null
  }
}

onBeforeUnmount(() => {
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  destroyMap()
})

defineExpose({ open })
</script>

<style scoped>
.map-picker {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #f4f8f5;
}

.map-picker__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px 4px;
}

.map-picker__title {
  font-size: 16px;
  font-weight: 600;
  color: #14342b;
}

.map-picker__body {
  position: relative;
  flex: 1;
  min-height: 0;
}

.map-picker__map {
  width: 100%;
  height: 100%;
}

.map-picker__results {
  position: absolute;
  top: 0;
  left: 8px;
  right: 8px;
  max-height: 46%;
  overflow-y: auto;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 8px 20px rgba(20, 52, 43, 0.16);
  z-index: 5;
}

.map-picker__result {
  padding: 10px 12px;
  border-bottom: 1px solid #f0f5f2;
}

.map-picker__result:last-child {
  border-bottom: none;
}

.map-picker__result-name {
  font-size: 14px;
  color: #1f2d24;
  font-weight: 500;
}

.map-picker__result-addr {
  margin-top: 2px;
  font-size: 12px;
  color: #8a9a90;
}

.map-picker__loading {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: rgba(255, 255, 255, 0.9);
  padding: 10px 16px;
  border-radius: 10px;
}

.map-picker__selected {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 12px 16px;
  background: #fff;
  border-top: 1px solid #e3efe7;
}

.map-picker__address {
  flex: 1;
  font-size: 13px;
  line-height: 1.5;
  color: #2c3b32;
  word-break: break-all;
}

.map-picker__footer {
  display: flex;
  gap: 10px;
  padding: 10px 16px calc(10px + env(safe-area-inset-bottom));
  background: #fff;
}

.map-picker__footer :deep(.van-button) {
  flex: 1;
}
</style>
