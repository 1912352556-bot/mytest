/**
 * 订单管理相关API
 */
import request from '@/utils/request'

/** 获取订单列表 */
export const getOrderList = (params: OrderQuery) =>
  request<Result<PageResult<Order>>>({
    url: '/order/list',
    method: 'get',
    params
  })

/** 获取订单详情 */
export const getOrderDetail = (id: string) =>
  request<Result<Order>>({
    url: `/order/${id}`,
    method: 'get'
  })

/** 新增订单 */
export const createOrder = (data: CreateOrderRequest) =>
  request<Result<Order>>({
    url: '/order',
    method: 'post',
    data
  })

/** 更新订单 */
export const updateOrder = (data: Order) =>
  request<Result<void>>({
    url: '/order',
    method: 'put',
    data
  })

/** 删除订单 */
export const deleteOrder = (id: string) =>
  request<Result<void>>({
    url: `/order/${id}`,
    method: 'delete'
  })

/** 更新订单状态 */
export const updateOrderStatus = (data: UpdateOrderStatusRequest) =>
  request<Result<void>>({
    url: '/order/status',
    method: 'put',
    data
  })

/** 取消订单 */
export const cancelOrder = (id: string, remark?: string) =>
  request<Result<void>>({
    url: `/order/${id}/cancel`,
    method: 'put',
    data: { remark }
  })

/** 批量导入订单 */
export const importOrders = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return request<Result<{ success: number; fail: number; failList?: string[] }>>({
    url: '/order/import',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/** 导出订单 */
export const exportOrders = (params: OrderQuery) =>
  request<Blob>({
    url: '/order/export',
    method: 'get',
    params,
    responseType: 'blob'
  })

/** 获取超时未调度订单 */
export const getTimeoutOrders = () =>
  request<Result<Order[]>>({
    url: '/order/timeout',
    method: 'get'
  })

/** 获取待调度订单列表 */
export const getPendingScheduleOrders = () =>
  request<Result<Order[]>>({
    url: '/order/pending-schedule',
    method: 'get'
  })
