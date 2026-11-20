/**
 * 库存管理API
 */
import request from '@/utils/request'

/**
 * 获取库存列表
 */
export const getInventoryList = (params: any) =>
  request({
    url: '/inventory/list',
    method: 'get',
    params
  })

/**
 * 获取库存详情
 */
export const getInventoryDetail = (id: string) =>
  request({
    url: `/inventory/${id}`,
    method: 'get'
  })

/**
 * 新增库存
 */
export const addInventory = (data: any) =>
  request({
    url: '/inventory',
    method: 'post',
    data
  })

/**
 * 更新库存
 */
export const updateInventory = (data: any) =>
  request({
    url: '/inventory',
    method: 'put',
    data
  })

/**
 * 删除库存
 */
export const deleteInventory = (id: string) =>
  request({
    url: `/inventory/${id}`,
    method: 'delete'
  })

/**
 * 入库操作
 */
export const stockIn = (data: { inventoryId: string; quantity: number; remark?: string }) =>
  request({
    url: '/inventory/stock-in',
    method: 'post',
    data
  })

/**
 * 出库操作
 */
export const stockOut = (data: { inventoryId: string; quantity: number; remark?: string }) =>
  request({
    url: '/inventory/stock-out',
    method: 'post',
    data
  })

/**
 * 获取库存变更记录
 */
export const getInventoryRecords = (inventoryId: string, params: any) =>
  request({
    url: `/inventory/${inventoryId}/records`,
    method: 'get',
    params
  })

/**
 * 获取库存预警列表
 */
export const getWarningList = () =>
  request({
    url: '/inventory/warning',
    method: 'get'
  })

/**
 * 设置预警阈值
 */
export const setWarningThreshold = (id: string, threshold: number) =>
  request({
    url: `/inventory/${id}/threshold/${threshold}`,
    method: 'put'
  })

/**
 * 批量设置预警阈值
 */
export const batchSetThreshold = (data: { ids: string[]; threshold: number }) =>
  request({
    url: '/inventory/threshold/batch',
    method: 'put',
    data
  })
