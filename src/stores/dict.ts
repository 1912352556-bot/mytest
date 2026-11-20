/**
 * 数据字典状态管理
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { 
  getDictDataByCode,
  getOrderStatusDict,
  getDeliveryAreaDict 
} from '@/api/dict'

/* 字典缓存接口 */
interface DictCache {
  [key: string]: DictData[]
}

export const useDictStore = defineStore('dict', () => {
  /* 字典数据缓存 */
  const dictCache = ref<DictCache>({})

  /* 加载状态 */
  const loading = ref<Record<string, boolean>>({})

  /* 根据字典编码获取字典数据 */
  async function getDictByCode(dictCode: string, forceRefresh = false): Promise<DictData[]> {
    /* 如果缓存中存在且不强制刷新，直接返回 */
    if (dictCache.value[dictCode] && !forceRefresh) {
      return dictCache.value[dictCode]
    }

    /* 防止重复请求 */
    if (loading.value[dictCode]) {
      return dictCache.value[dictCode] || []
    }

    try {
      loading.value[dictCode] = true
      const res = await getDictDataByCode(dictCode)
      dictCache.value[dictCode] = res.data.data || []
      return dictCache.value[dictCode]
    } catch (error) {
      console.error(`获取字典[${dictCode}]失败:`, error)
      return []
    } finally {
      loading.value[dictCode] = false
    }
  }

  /* 根据字典编码和值获取标签 */
  function getDictLabel(dictCode: string, value: string | number): string {
    const dict = dictCache.value[dictCode]
    if (!dict) return String(value)
    const item = dict.find(d => d.dictValue === String(value))
    return item?.dictLabel || String(value)
  }

  /* 获取订单状态字典 */
  async function getOrderStatusOptions(): Promise<DictData[]> {
    return getDictByCode('order_status')
  }

  /* 获取配送区域字典 */
  async function getDeliveryAreaOptions(): Promise<DictData[]> {
    return getDictByCode('delivery_area')
  }

  /* 获取角色类型字典 */
  async function getRoleOptions(): Promise<DictData[]> {
    return getDictByCode('role_type')
  }

  /* 获取调度任务状态字典 */
  async function getScheduleStatusOptions(): Promise<DictData[]> {
    return getDictByCode('schedule_status')
  }

  /* 获取配送任务状态字典 */
  async function getDeliveryStatusOptions(): Promise<DictData[]> {
    return getDictByCode('delivery_status')
  }

  /* 清除所有缓存 */
  function clearCache(): void {
    dictCache.value = {}
  }

  /* 清除指定字典缓存 */
  function clearDictCache(dictCode: string): void {
    delete dictCache.value[dictCode]
  }

  /* 预加载常用字典 */
  async function preloadDict(): Promise<void> {
    const dictCodes = [
      'order_status',
      'delivery_area',
      'role_type',
      'schedule_status',
      'delivery_status',
      'dish_category'
    ]
    await Promise.all(dictCodes.map(code => getDictByCode(code)))
  }

  return {
    dictCache,
    loading,
    getDictByCode,
    getDictLabel,
    getOrderStatusOptions,
    getDeliveryAreaOptions,
    getRoleOptions,
    getScheduleStatusOptions,
    getDeliveryStatusOptions,
    clearCache,
    clearDictCache,
    preloadDict
  }
})
