/**
 * 数据字典相关API
 */
import request from '@/utils/request'

/* ==================== 字典类型管理 ==================== */

/** 获取字典类型列表 */
export const getDictTypeList = (params?: { dictCode?: string; dictName?: string }) =>
  request<Result<DictType[]>>({
    url: '/dict/type/list',
    method: 'get',
    params
  })

/** 获取字典类型详情 */
export const getDictTypeDetail = (id: string) =>
  request<Result<DictType>>({
    url: `/dict/type/${id}`,
    method: 'get'
  })

/** 新增字典类型 */
export const addDictType = (data: Omit<DictType, 'id' | 'createTime'>) =>
  request<Result<void>>({
    url: '/dict/type',
    method: 'post',
    data
  })

/** 更新字典类型 */
export const updateDictType = (data: DictType) =>
  request<Result<void>>({
    url: '/dict/type',
    method: 'put',
    data
  })

/** 删除字典类型 */
export const deleteDictType = (id: string) =>
  request<Result<void>>({
    url: `/dict/type/${id}`,
    method: 'delete'
  })

/* ==================== 字典数据管理 ==================== */

/** 根据字典类型获取字典数据列表 */
export const getDictDataList = (dictTypeId: string) =>
  request<Result<DictData[]>>({
    url: `/dict/data/list/${dictTypeId}`,
    method: 'get'
  })

/** 根据字典编码获取字典数据列表 */
export const getDictDataByCode = (dictCode: string) =>
  request<Result<DictData[]>>({
    url: `/dict/data/code/${dictCode}`,
    method: 'get'
  })

/** 新增字典数据 */
export const addDictData = (data: Omit<DictData, 'id'>) =>
  request<Result<void>>({
    url: `/dict/data`,
    method: 'post',
    data
  })

/** 更新字典数据 */
export const updateDictData = (data: DictData) =>
  request<Result<void>>({
    url: '/dict/data',
    method: 'put',
    data
  })

/** 删除字典数据 */
export const deleteDictData = (id: string) =>
  request<Result<void>>({
    url: `/dict/data/${id}`,
    method: 'delete'
  })

/* ==================== 常用字典数据获取 ==================== */

/** 获取订单状态字典 */
export const getOrderStatusDict = () => getDictDataByCode('order_status')

/** 获取商品分类字典 */
export const getDishCategoryDict = () => getDictDataByCode('dish_category')

/** 获取配送区域字典 */
export const getDeliveryAreaDict = () => getDictDataByCode('delivery_area')

/** 获取角色类型字典 */
export const getRoleTypeDict = () => getDictDataByCode('role_type')
