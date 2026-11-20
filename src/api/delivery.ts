/**
 * 物流配送相关API
 */
import request from '@/utils/request'

/* ==================== 配送人员管理 ==================== */

/** 获取配送人员列表 */
export const getDeliveryPersonList = (params: DeliveryPersonQuery) =>
  request<Result<PageResult<DeliveryPerson>>>({
    url: '/delivery/person/list',
    method: 'get',
    params
  })

/** 获取配送人员详情 */
export const getDeliveryPersonDetail = (id: string) =>
  request<Result<DeliveryPerson>>({
    url: `/delivery/person/${id}`,
    method: 'get'
  })

/** 新增配送人员 */
export const addDeliveryPerson = (data: Omit<DeliveryPerson, 'id' | 'createTime' | 'currentTaskCount' | 'totalDeliveryCount'>) =>
  request<Result<void>>({
    url: '/delivery/person',
    method: 'post',
    data
  })

/** 更新配送人员 */
export const updateDeliveryPerson = (data: DeliveryPerson) =>
  request<Result<void>>({
    url: '/delivery/person',
    method: 'put',
    data
  })

/** 删除配送人员 */
export const deleteDeliveryPerson = (id: string) =>
  request<Result<void>>({
    url: `/delivery/person/${id}`,
    method: 'delete'
  })

/** 修改配送人员状态 */
export const updatePersonStatus = (id: string, status: number) =>
  request<Result<void>>({
    url: `/delivery/person/${id}/status/${status}`,
    method: 'put'
  })

/** 获取空闲配送人员列表 */
export const getIdleDeliveryPersons = () =>
  request<Result<DeliveryPerson[]>>({
    url: '/delivery/person/idle',
    method: 'get'
  })

/* ==================== 配送任务管理 ==================== */

/** 获取配送任务列表 */
export const getDeliveryTaskList = (params: DeliveryTaskQuery) =>
  request<Result<PageResult<DeliveryTask>>>({
    url: '/delivery/task/list',
    method: 'get',
    params
  })

/** 获取配送任务详情 */
export const getDeliveryTaskDetail = (id: string) =>
  request<Result<DeliveryTask>>({
    url: `/delivery/task/${id}`,
    method: 'get'
  })

/** 创建配送任务（指派配送） */
export const createDeliveryTask = (data: { scheduleTaskId: string; deliveryPersonId: string }) =>
  request<Result<DeliveryTask>>({
    url: '/delivery/task',
    method: 'post',
    data
  })

/** 开始配送（取货） */
export const startDelivery = (taskId: string) =>
  request<Result<void>>({
    url: `/delivery/task/${taskId}/start`,
    method: 'put'
  })

/** 完成配送 */
export const completeDelivery = (data: CompleteDeliveryRequest) =>
  request<Result<void>>({
    url: '/delivery/task/complete',
    method: 'put',
    data
  })

/** 异常上报 */
export const reportException = (taskId: string, remark: string) =>
  request<Result<void>>({
    url: `/delivery/task/${taskId}/exception`,
    method: 'put',
    data: { remark }
  })

/* ==================== 配送轨迹 ==================== */

/** 获取配送轨迹 */
export const getDeliveryTrack = (taskId: string) =>
  request<Result<DeliveryTrack[]>>({
    url: `/delivery/task/${taskId}/track`,
    method: 'get'
  })

/** 上报配送位置 */
export const reportLocation = (data: { taskId: string; longitude: number; latitude: number; address?: string }) =>
  request<Result<void>>({
    url: '/delivery/task/location',
    method: 'post',
    data
  })
