<script setup lang="ts">
/**
 * 地图选点组件 - 基于高德地图
 */
import { ref, watch, onMounted, onUnmounted, computed } from 'vue'

/* Props */
const props = withDefaults(defineProps<{
  modelValue?: LocationInfo | null
  width?: string
  height?: string
  placeholder?: string
  disabled?: boolean
}>(), {
  modelValue: null,
  width: '100%',
  height: '400px',
  placeholder: '点击选择位置或输入地址搜索',
  disabled: false
})

/* Emits */
const emit = defineEmits<{
  (e: 'update:modelValue', value: LocationInfo | null): void
  (e: 'change', value: LocationInfo | null): void
}>()

/* 地图实例 */
const mapContainer = ref<HTMLElement>()
const searchInput = ref('')
const searchResults = ref<any[]>([])
const showResults = ref(false)
const currentLocation = ref<LocationInfo | null>(props.modelValue)
const selectedAddress = ref('')
const showMapDialog = ref(false)
const mapLoaded = ref(false)

// 地图实例
let mapInstance: any = null
let marker: any = null

/* 位置数据结构 */
interface LocationInfo {
  address: string
  longitude: number
  latitude: number
  city?: string
  district?: string
}

/* 获取用户当前位置 */
const getUserLocation = (): Promise<[number, number]> => {
  return new Promise((resolve) => {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          // 优先使用GPS坐标
          resolve([position.coords.longitude, position.coords.latitude])
        },
        () => {
          // 权限拒绝或失败，使用IP定位
          AMap.plugin('AMap.CitySearch', () => {
            const citySearch = new AMap.CitySearch()
            citySearch.getCityByIp((status: string, result: any) => {
              if (status === 'complete' && result.city) {
                // 使用城市中心点
                AMap.plugin('AMap.Geocoder', () => {
                  const geocoder = new AMap.Geocoder()
                  geocoder.getLocation(result.city, (status2: string, result2: any) => {
                    if (status2 === 'complete' && result2.geocodes.length > 0) {
                      const location = result2.geocodes[0].location
                      resolve([location.getLng(), location.getLat()])
                    } else {
                      resolve([116.397428, 39.90923]) // 默认北京
                    }
                  })
                })
              } else {
                resolve([116.397428, 39.90923]) // 默认北京
              }
            })
          })
        },
        { enableHighAccuracy: false, timeout: 5000 }
      )
    } else {
      resolve([116.397428, 39.90923]) // 默认北京
    }
  })
}

/* 初始化地图 */
const initMap = async () => {
  if (!mapContainer.value) return

  if (typeof AMap === 'undefined') {
    console.error('高德地图SDK未加载')
    return
  }

  // 确定地图中心点
  let center: [number, number]
  if (currentLocation.value) {
    center = [currentLocation.value.longitude, currentLocation.value.latitude]
  } else {
    // 获取用户当前位置
    center = await getUserLocation()
  }

  mapInstance = new AMap.Map(mapContainer.value, {
    zoom: 15,
    center: new AMap.LngLat(center[0], center[1]),
    viewMode: '2D'
  })

  // 添加点击事件
  mapInstance.on('click', (e: any) => {
    const lng = e.lnglat.getLng()
    const lat = e.lnglat.getLat()
    reverseGeocode(lng, lat)
  })

  // 添加标记
  if (currentLocation.value) {
    addMarker(currentLocation.value.longitude, currentLocation.value.latitude)
  }

  mapLoaded.value = true
}

/* 逆地理编码 */
const reverseGeocode = (lng: number, lat: number) => {
  if (typeof AMap === 'undefined') return

  AMap.plugin('AMap.Geocoder', () => {
    const geocoder = new AMap.Geocoder()
    geocoder.getAddress([lng, lat], (status: string, result: any) => {
      if (status === 'complete' && result.info === 'OK') {
        const addr = result.regeocode.formattedAddress
        updateLocation(lng, lat, addr, result.regeocode.addressComponent)
        addMarker(lng, lat)
      }
    })
  })
}

/* 更新位置信息 */
const updateLocation = (lng: number, lat: number, address: string, addressComponent?: any) => {
  const data: LocationInfo = {
    longitude: lng,
    latitude: lat,
    address: address
  }
  
  if (addressComponent) {
    data.city = addressComponent.city
    data.district = addressComponent.district
  }

  currentLocation.value = data
  selectedAddress.value = address
  emit('update:modelValue', data)
  emit('change', data)
}

/* 添加标记 */
const addMarker = (lng: number, lat: number) => {
  if (!mapInstance || typeof AMap === 'undefined') return

  if (marker) {
    mapInstance.remove(marker)
  }

  marker = new AMap.Marker({
    position: new AMap.LngLat(lng, lat),
    title: '配送位置'
  })
  mapInstance.add(marker)
}

/* 搜索 */
const searchAddress = (keyword: string) => {
  if (!keyword || typeof AMap === 'undefined') {
    searchResults.value = []
    return
  }

  AMap.plugin('AMap.Autocomplete', () => {
    AMap.plugin('AMap.PlaceSearch', () => {
      const placeSearch = new AMap.PlaceSearch({
        pageSize: 8,
        pageIndex: 1,
        city: '全国'
      })

      placeSearch.search(keyword, (status: string, result: any) => {
        if (status === 'complete' && result.info === 'OK') {
          searchResults.value = result.poiList.pois || []
          showResults.value = true
        } else {
          searchResults.value = []
        }
      })
    })
  })
}

/* 选择搜索结果 */
const selectResult = (item: any) => {
  const lng = item.location.getLng()
  const lat = item.location.getLat()
  const address = item.address + item.name

  updateLocation(lng, lat, address)
  
  // 更新地图中心
  if (mapInstance) {
    mapInstance.setCenter([lng, lat])
  }

  showResults.value = false
  searchInput.value = ''
  searchResults.value = []
}

/* 打开地图选择器 */
const openMapPicker = () => {
  if (props.disabled) return
  showMapDialog.value = true
  // 延迟初始化地图，等待 Dialog 渲染完成
  setTimeout(() => {
    initMap()
  }, 100)
}

/* 关闭地图选择器 */
const closeMapPicker = () => {
  showMapDialog.value = false
}

/* 确认选择 */
const confirmSelection = () => {
  showMapDialog.value = false
}

/* 清除选择 */
const clearSelection = () => {
  currentLocation.value = null
  selectedAddress.value = ''
  emit('update:modelValue', null)
  emit('change', null)
}

/* 监听搜索输入 */
let searchTimer: any = null
const handleSearchInput = () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    searchAddress(searchInput.value)
  }, 300)
}

/* 点击外部关闭搜索结果 */
const handleClickOutside = () => {
  showResults.value = false
}

/* 显示地址文本 */
const displayText = computed(() => {
  if (selectedAddress.value) return selectedAddress.value
  if (currentLocation.value?.address) return currentLocation.value.address
  return ''
})

/* 监听外部值变化 */
watch(() => props.modelValue, (newVal) => {
  if (newVal) {
    currentLocation.value = newVal
    selectedAddress.value = newVal.address || ''
  }
}, { immediate: true })

/* 组件卸载 */
onUnmounted(() => {
  if (mapInstance) {
    mapInstance.destroy()
    mapInstance = null
  }
  clearTimeout(searchTimer)
})
</script>

<template>
  <div class="map-picker-container" v-click-outside="handleClickOutside">
    <!-- 显示框 -->
    <div class="picker-input" :class="{ disabled: disabled }" @click="openMapPicker">
      <div v-if="displayText" class="address-text">{{ displayText }}</div>
      <div v-else class="placeholder">{{ placeholder }}</div>
      <div class="actions">
        <el-button 
          v-if="displayText && !disabled" 
          type="danger" 
          link 
          size="small"
          @click.stop="clearSelection"
        >
          清除
        </el-button>
        <el-icon class="map-icon"><Location /></el-icon>
      </div>
    </div>

    <!-- 坐标显示 -->
    <div v-if="currentLocation" class="coordinates">
      <span>经度: {{ currentLocation.longitude.toFixed(6) }}</span>
      <span>纬度: {{ currentLocation.latitude.toFixed(6) }}</span>
    </div>

    <!-- 地图选择弹窗 -->
    <el-dialog
      v-model="showMapDialog"
      title="选择配送位置"
      width="900px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <div class="map-picker-content">
        <!-- 工具栏 -->
        <div class="map-toolbar">
          <div class="map-type-tag">
            <el-icon><MapLocation /></el-icon>
            <span>高德地图</span>
          </div>
          
          <div class="search-box">
            <el-input
              v-model="searchInput"
              placeholder="输入地址搜索"
              clearable
              @input="handleSearchInput"
              @focus="showResults = searchResults.length > 0"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            
            <!-- 搜索结果 -->
            <div v-if="showResults && searchResults.length > 0" class="search-results">
              <div
                v-for="item in searchResults"
                :key="item.id"
                class="result-item"
                @click="selectResult(item)"
              >
                <div class="result-name">{{ item.name }}</div>
                <div class="result-address">{{ item.address }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 地图容器 -->
        <div ref="mapContainer" class="map-container"></div>

        <!-- 提示 -->
        <div class="map-tip">
          <el-icon><InfoFilled /></el-icon>
          <span>点击地图选择位置，或在上方搜索框中输入地址搜索</span>
        </div>

        <!-- 已选位置 -->
        <div v-if="selectedAddress" class="selected-info">
          <el-icon><LocationFilled /></el-icon>
          <span>{{ selectedAddress }}</span>
        </div>
      </div>

      <template #footer>
        <el-button @click="closeMapPicker">取消</el-button>
        <el-button type="primary" @click="confirmSelection">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.map-picker-container {
  .picker-input {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 8px 12px;
    border: 1px solid #dcdfe6;
    border-radius: 4px;
    cursor: pointer;
    min-height: 36px;
    background: #fff;
    transition: border-color 0.2s;

    &:hover:not(.disabled) {
      border-color: #16a34a;
    }

    &.disabled {
      cursor: not-allowed;
      background: #f5f7fa;
    }

    .address-text {
      flex: 1;
      font-size: 14px;
      color: #606266;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .placeholder {
      flex: 1;
      color: #c0c4cc;
      font-size: 14px;
    }

    .actions {
      display: flex;
      align-items: center;
      gap: 8px;

      .map-icon {
        color: #16a34a;
        font-size: 16px;
      }
    }
  }

  .coordinates {
    margin-top: 4px;
    font-size: 12px;
    color: #909399;
    display: flex;
    gap: 16px;
  }

  .map-picker-content {
    .map-toolbar {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 16px;
      gap: 16px;

      .map-type-tag {
        display: flex;
        align-items: center;
        gap: 6px;
        padding: 8px 16px;
        background: #ecf5ff;
        color: #16a34a;
        border-radius: 4px;
        font-size: 14px;
        font-weight: 500;
      }

      .search-box {
        flex: 1;
        max-width: 400px;
        position: relative;

        .search-results {
          position: absolute;
          top: 100%;
          left: 0;
          right: 0;
          background: #fff;
          border: 1px solid #e4e7ed;
          border-radius: 4px;
          box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
          z-index: 100;
          max-height: 300px;
          overflow-y: auto;

          .result-item {
            padding: 10px 12px;
            cursor: pointer;
            border-bottom: 1px solid #f0f0f0;

            &:last-child {
              border-bottom: none;
            }

            &:hover {
              background: #f5f7fa;
            }

            .result-name {
              font-size: 14px;
              color: #303133;
              font-weight: 500;
            }

            .result-address {
              font-size: 12px;
              color: #909399;
              margin-top: 4px;
            }
          }
        }
      }
    }

    .map-container {
      width: 100%;
      height: 400px;
      border: 1px solid #e4e7ed;
      border-radius: 4px;
    }

    .map-tip {
      margin-top: 12px;
      font-size: 12px;
      color: #909399;
      display: flex;
      align-items: center;
      gap: 4px;
    }

    .selected-info {
      margin-top: 12px;
      padding: 12px;
      background: #f0f9ff;
      border: 1px solid #b6dfff;
      border-radius: 4px;
      font-size: 14px;
      color: #16a34a;
      display: flex;
      align-items: flex-start;
      gap: 8px;
      word-break: break-all;

      .el-icon {
        flex-shrink: 0;
        margin-top: 2px;
      }
    }
  }
}
</style>
