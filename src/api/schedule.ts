/**
 * 调度任务相关API
 */
import request from '@/utils/request'

/** 获取调度任务列表 */
export const getScheduleTaskList = (params: ScheduleTaskQuery) =>
  request<Result<PageResult<ScheduleTask>>>({
    url: '/schedule/list',
    method: 'get',
    params
  })

/** 获取调度任务详情 */
export const getScheduleTaskDetail = (id: string) =>
  request<Result<ScheduleTask>>({
    url: `/schedule/${id}`,
    method: 'get'
  })

/** 创建调度任务（手动调度） */
export const createScheduleTask = (orderId: string) =>
  request<Result<ScheduleTask>>({
    url: '/schedule',
    method: 'post',
    data: { orderId }
  })

/** 批量创建调度任务（自动调度） */
export const batchCreateScheduleTasks = (orderIds: string[]) =>
  request<Result<{ success: number; fail: number }>>({
    url: '/schedule/batch',
    method: 'post',
    data: { orderIds }
  })

/** 自动分配调度任务（按区域/时间） */
export const autoAssignTasks = (params?: { area?: string; date?: string }) =>
  request<Result<{ assigned: number; failed: number }>>({
    url: '/schedule/auto-assign',
    method: 'post',
    data: params
  })

/** 手动分配调度任务 */
export const assignTask = (data: AssignTaskRequest) =>
  request<Result<void>>({
    url: '/schedule/assign',
    method: 'put',
    data
  })

/** 更新调度任务状态 */
export const updateTaskStatus = (id: string, status: number) =>
  request<Result<void>>({
    url: `/schedule/${id}/status/${status}`,
    method: 'put'
  })

/** 取消调度任务 */
export const cancelScheduleTask = (id: string, remark?: string) =>
  request<Result<void>>({
    url: `/schedule/${id}/cancel`,
    method: 'put',
    data: { remark }
  })

/** 获取调度日志 */
export const getScheduleLogs = (taskId: string) =>
  request<Result<ScheduleLog[]>>({
    url: `/schedule/${taskId}/logs`,
    method: 'get'
  })

/** 获取待分配任务列表 */
export const getPendingAssignTasks = () =>
  request<Result<ScheduleTask[]>>({
    url: '/schedule/pending-assign',
    method: 'get'
  })

/** 获取可分配的配送人员列表 */
export const getAvailableDeliveryPersons = () =>
  request<Result<DeliveryPerson[]>>({
    url: '/schedule/available-persons',
    method: 'get'
  })
