/**
 * 统计分析相关API
 */
import request from '@/utils/request'

/** 获取首页统计概览 */
export const getDashboardOverview = () =>
  request<Result<DashboardOverview>>({
    url: '/statistics/overview',
    method: 'get'
  })

/** 获取订单统计数据 */
export const getOrderStatistics = (params: StatisticsQuery) =>
  request<Result<OrderStatistics[]>>({
    url: '/statistics/order',
    method: 'get',
    params
  })

/** 获取调度完成率统计 */
export const getScheduleStatistics = (params: StatisticsQuery) =>
  request<Result<ScheduleStatistics[]>>({
    url: '/statistics/schedule',
    method: 'get',
    params
  })

/** 获取库存周转统计 */
export const getInventoryTurnoverStatistics = (params?: StatisticsQuery) =>
  request<Result<InventoryTurnoverStatistics[]>>({
    url: '/statistics/inventory-turnover',
    method: 'get',
    params
  })

/** 获取库存统计 */
export const getInventoryStatistics = () =>
  request<Result<InventoryStatistics[]>>({
    url: '/statistics/inventory',
    method: 'get'
  })

/** 获取每日订单量统计 */
export const getDailyOrderCount = (params: { startDate: string; endDate: string }) =>
  request<Result<Array<{ date: string; count: number }>>>({
    url: '/statistics/order/daily',
    method: 'get',
    params
  })

/** 获取每月订单量统计 */
export const getMonthlyOrderCount = (params: { year: number }) =>
  request<Result<Array<{ month: string; count: number }>>>({
    url: '/statistics/order/monthly',
    method: 'get',
    params
  })

/** 获取订单状态分布 */
export const getOrderStatusDistribution = () =>
  request<Result<Array<{ status: number; statusName: string; count: number }>>>({
    url: '/statistics/order/status-distribution',
    method: 'get'
  })

/** 获取配送区域订单分布 */
export const getAreaOrderDistribution = () =>
  request<Result<Array<{ area: string; count: number; amount: number }>>>({
    url: '/statistics/order/area-distribution',
    method: 'get'
  })

/** 获取热销商品排行 */
export const getHotDishRanking = (params?: { limit?: number }) =>
  request<Result<Array<{ dishId: string; dishName: string; quantity: number; amount: number }>>>({
    url: '/statistics/dish/hot-ranking',
    method: 'get',
    params
  })

/** 获取配送人员绩效统计 */
export const getDeliveryPersonPerformance = (params?: StatisticsQuery) =>
  request<Result<Array<{ personId: string; personName: string; deliveryCount: number; avgTime: number }>>>({
    url: '/statistics/delivery/performance',
    method: 'get',
    params
  })
