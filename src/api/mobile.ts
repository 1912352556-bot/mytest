/**
 * 移动端相关 API
 *
 * 客户（role=3）与配送员（role=2）共用同一个请求封装，
 * 身份由后端根据请求头里的 JWT 解析，前端不再传 userId。
 */
import request from '@/utils/request'

/* ==================== 通用 ==================== */

/** 当前登录用户资料 */
export const getMobileProfile = () =>
  request<Result<MobileProfile>>({
    url: '/mobile/profile',
    method: 'get'
  })

/* ==================== 客户 ==================== */

/** 客户首页：商品分类 + 上架商品（含库存） */
export const getCustomerHome = () =>
  request<Result<{ categories: MobileCategory[]; dishes: MobileDish[] }>>({
    url: '/mobile/customer/home',
    method: 'get'
  })

/** 客户下单（后端下单后立即自动派单，返回派单结果） */
export const createMobileOrder = (data: MobileCreateOrderRequest) =>
  request<
    Result<{
      orderId: string
      orderNo: string
      totalAmount: number
      orderStatus: number
      autoAssigned: boolean
      dispatchMessage: string
      deliveryPersonName?: string
    }>
  >({
    url: '/mobile/customer/orders',
    method: 'post',
    data
  })

/** 我的订单列表 */
export const getMyOrders = (params: { pageNum: number; pageSize: number; orderStatus?: number }) =>
  request<Result<PageResult<Order>>>({
    url: '/mobile/customer/orders',
    method: 'get',
    params
  })

/** 我的订单详情 */
export const getMyOrderDetail = (id: string) =>
  request<Result<Order>>({
    url: `/mobile/customer/orders/${id}`,
    method: 'get'
  })

/** 取消我的订单 */
export const cancelMyOrder = (id: string, reason?: string) =>
  request<Result<void>>({
    url: `/mobile/customer/orders/${id}/cancel`,
    method: 'put',
    data: { reason }
  })

/* ==================== 配送员 ==================== */

/** 我的配送任务列表 */
export const getMyTasks = (params: { pageNum: number; pageSize: number; deliveryStatus?: number }) =>
  request<Result<PageResult<DeliveryTask>>>({
    url: '/mobile/courier/tasks',
    method: 'get',
    params
  })

/** 我的配送任务详情 */
export const getMyTaskDetail = (id: string) =>
  request<Result<DeliveryTask>>({
    url: `/mobile/courier/tasks/${id}`,
    method: 'get'
  })

/** 开始配送 */
export const startMyTask = (id: string) =>
  request<Result<void>>({
    url: `/mobile/courier/tasks/${id}/start`,
    method: 'put'
  })

/** 完成配送 */
export const completeMyTask = (id: string, remark?: string) =>
  request<Result<void>>({
    url: `/mobile/courier/tasks/${id}/complete`,
    method: 'put',
    data: { remark }
  })

/** 异常上报 */
export const reportTaskException = (id: string, remark: string) =>
  request<Result<void>>({
    url: `/mobile/courier/tasks/${id}/exception`,
    method: 'put',
    data: { remark }
  })

/** 配送员工作台统计 */
export const getCourierSummary = () =>
  request<Result<MobileSummary>>({
    url: '/mobile/courier/summary',
    method: 'get'
  })

/** 修改工作状态：0-空闲 1-配送中 2-休息 */
export const updateCourierStatus = (status: number) =>
  request<Result<void>>({
    url: `/mobile/courier/status/${status}`,
    method: 'put'
  })
