/**
 * 生鲜即时配送调度系统 - 全局类型定义
 */

/* ==================== 通用类型 ==================== */

/** 统一响应结果 */
declare interface Result<T = any> {
  code: number
  flag: boolean
  msg: string
  data: T
}

/** 分页查询参数 */
declare interface PageQuery {
  pageNum: number
  pageSize: number
}

/** 分页响应结果 */
declare interface PageResult<T = any> {
  records: T[]
  total: number
  pageNum: number
  pageSize: number
  pages: number
}

/** 下拉选项类型 */
declare interface SelectOption {
  label: string
  value: string | number
}

/* ==================== 用户权限模块 ==================== */

/** 登录请求参数 */
declare interface LoginUser {
  username: string
  password: string
}

/** 登录响应 - 用户信息 */
declare interface UserInfo {
  id: string
  username: string
  realname: string
  phone?: string
  email?: string
  gender: number
  role: RoleType
  avatar?: string
  token?: string
  createTime?: string
  updateTime?: string
}

/** 注册用户 */
declare interface RegUser {
  id?: string
  username: string
  password: string
  realname: string
  email: string
  phone: string
  gender: number
  role: number
  path?: string
}

/** 角色类型：0-管理员 1-调度员 2-配送员 3-客户 */
declare type RoleType = 0 | 1 | 2 | 3

/** 角色枚举映射 */
declare const RoleMap: Record<RoleType, string>

/** 用户查询参数 */
declare interface UserQuery extends PageQuery {
  username?: string
  realname?: string
  role?: RoleType
  phone?: string
}

/* ==================== 数据字典模块 ==================== */

/** 数据字典 */
declare interface DictType {
  id: string
  dictCode: string
  dictName: string
  description?: string
  status: number
  createTime?: string
}

/** 字典数据项 */
declare interface DictData {
  id: string
  dictTypeId: string
  dictCode: string
  dictLabel: string
  dictValue: string
  sortOrder: number
  status: number
  remark?: string
}

/** 订单状态类型：0-待调度 1-已调度 2-配送中 3-已完成 4-取消 */
declare type OrderStatusType = 0 | 1 | 2 | 3 | 4

/* ==================== 生鲜商品模块 ==================== */

/** 储存温层：1-常温 2-冷藏 3-冷冻 */
declare type StorageType = 1 | 2 | 3

/** 生鲜商品信息 */
declare interface Dish {
  id: string
  dishName: string
  dishCode: string
  categoryId: string
  categoryName?: string
  price: number
  unit: string
  description?: string
  imageUrl?: string
  status: number
  /** 保质期（天） */
  shelfLifeDays?: number
  /** 储存温层：1-常温 2-冷藏 3-冷冻 */
  storageType?: StorageType
  /** 产地 */
  origin?: string
  /** 规格，如 500g/份 */
  spec?: string
  /** 损耗率（%） */
  lossRate?: number
  /** 当前库存（关联 t_inventory 查询，只读） */
  stock?: number
  createTime?: string
  updateTime?: string
}

/** 生鲜商品查询参数 */
declare interface DishQuery extends PageQuery {
  dishName?: string
  categoryId?: string
  storageType?: StorageType
  status?: number
}

/** 生鲜商品分类 */
declare interface DishCategory {
  id: string
  categoryName: string
  sortOrder: number
  status: number
}

/* ==================== 库存模块 ==================== */

/** 库存信息 */
declare interface Inventory {
  id: string
  dishId: string
  dishName?: string
  dishCode?: string
  currentStock: number
  warningThreshold: number
  unit: string
  lastInTime?: string
  lastOutTime?: string
  status: number
  createTime?: string
  updateTime?: string
}

/** 库存查询参数 */
declare interface InventoryQuery extends PageQuery {
  dishName?: string
  isWarning?: boolean
}

/** 库存变更记录 */
declare interface InventoryRecord {
  id: string
  inventoryId: string
  dishId?: string
  dishName?: string
  changeType: number // 0-入库 1-出库 2-订单出库
  changeQuantity?: number // 兼容字段
  quantity?: number
  beforeStock: number
  afterStock: number
  orderId?: string
  orderNo?: string
  remark?: string
  operatorId?: string
  operatorName?: string
  createTime?: string
}

/** 库存变更请求 */
declare interface InventoryChangeRequest {
  inventoryId: string
  quantity: number
  orderId?: string
  remark?: string
}

/* ==================== 订单模块 ==================== */

/** 订单信息 */
declare interface Order {
  id: string
  orderNo: string
  customerName: string
  customerPhone: string
  deliveryAddress: string
  deliveryArea: string
  /** 配送地址经度（地图选点） */
  longitude?: number
  /** 配送地址纬度（地图选点） */
  latitude?: number
  deliveryLocation?: LocationInfo  // 配送位置坐标
  expectedDeliveryTime: string
  actualDeliveryTime?: string
  totalAmount: number
  orderStatus: OrderStatusType
  remark?: string
  creatorId: string
  creatorName?: string
  createTime?: string
  updateTime?: string
  orderItems?: OrderItem[]
}

/** 订单明细 */
declare interface OrderItem {
  id: string
  orderId: string
  dishId: string
  dishName?: string
  dishCode?: string
  quantity: number
  unitPrice: number
  subtotal: number
}

/** 订单查询参数 */
declare interface OrderQuery extends PageQuery {
  orderNo?: string
  customerName?: string
  orderStatus?: OrderStatusType
  deliveryArea?: string
  startTime?: string
  endTime?: string
}

/** 位置信息 */
declare interface LocationInfo {
  address: string
  longitude: number
  latitude: number
  city?: string
  district?: string
}

/** 创建订单请求 */
declare interface CreateOrderRequest {
  customerName: string
  customerPhone: string
  deliveryAddress: string
  deliveryArea: string
  deliveryLocation?: LocationInfo  // 配送位置坐标
  expectedDeliveryTime: string
  remark?: string
  orderItems: Array<{
    dishId: string
    quantity: number
  }>
}

/** 订单状态更新请求 */
declare interface UpdateOrderStatusRequest {
  orderId: string
  orderStatus: OrderStatusType
  remark?: string
}

/* ==================== 调度模块 ==================== */

/** 调度任务 */
declare interface ScheduleTask {
  id: string
  taskNo: string
  orderId: string
  orderNo?: string
  deliveryArea: string
  expectedDeliveryTime: string
  assigneeId?: string
  assigneeName?: string
  taskStatus: number // 0-待分配 1-已分配 2-执行中 3-已完成
  priority: number
  remark?: string
  createTime?: string
  updateTime?: string
}

/** 调度任务查询参数 */
declare interface ScheduleTaskQuery extends PageQuery {
  taskNo?: string
  orderNo?: string
  deliveryArea?: string
  taskStatus?: number
  assigneeId?: string
  startTime?: string
  endTime?: string
}

/** 调度任务分配请求 */
declare interface AssignTaskRequest {
  taskId: string
  assigneeId: string
  priority?: number
  remark?: string
}

/** 调度日志 */
declare interface ScheduleLog {
  id: string
  taskId: string
  taskNo?: string
  operationType: string
  operationContent: string
  operatorId: string
  operatorName?: string
  createTime?: string
}

/* ==================== 配送模块 ==================== */

/** 配送人员 */
declare interface DeliveryPerson {
  id: string
  personName: string
  phone: string
  vehicleNo?: string
  status: number // 0-空闲 1-配送中 2-休息
  currentTaskCount: number
  totalDeliveryCount: number
  createTime?: string
}

/** 配送人员查询参数 */
declare interface DeliveryPersonQuery extends PageQuery {
  personName?: string
  phone?: string
  status?: number
}

/** 配送任务 */
declare interface DeliveryTask {
  id: string
  deliveryNo: string
  scheduleTaskId: string
  orderId: string
  orderNo?: string
  deliveryPersonId: string
  deliveryPersonName?: string
  customerName?: string
  customerPhone?: string
  deliveryAddress: string
  deliveryStatus: number // 0-待取货 1-配送中 2-已送达 3-异常
  pickupTime?: string
  deliveryTime?: string
  remark?: string
  /** 期望送达时间（来自订单，移动端使用） */
  expectedDeliveryTime?: string
  /** 关联订单状态：0-待调度 1-已调度 2-配送中 3-已完成 4-已取消 */
  orderStatus?: number
  /** 关联订单金额 */
  totalAmount?: number
  createTime?: string
  longitude?: number // 客户地址经度
  latitude?: number  // 客户地址纬度
  distance?: string  // 路线距离（公里）
  estimatedTime?: number // 预计时间（分钟）
}

/** 配送任务查询参数 */
declare interface DeliveryTaskQuery extends PageQuery {
  deliveryNo?: string
  orderNo?: string
  deliveryPersonId?: string
  deliveryStatus?: number
  startTime?: string
  endTime?: string
}

/** 配送轨迹 */
declare interface DeliveryTrack {
  id: string
  deliveryTaskId: string
  longitude: number
  latitude: number
  address?: string
  recordTime: string
}

/** 配送完成请求 */
declare interface CompleteDeliveryRequest {
  deliveryTaskId: string
  remark?: string
}

/* ==================== 统计分析模块 ==================== */

/** 首页统计概览 */
declare interface DashboardOverview {
  todayOrderCount: number
  todayCompletedCount: number
  pendingScheduleCount: number
  deliveringCount: number
  lowStockCount: number
  totalOrderAmount: number
}

/** 订单统计 */
declare interface OrderStatistics {
  date: string
  orderCount: number
  completedCount: number
  cancelledCount: number
  totalAmount: number
}

/** 调度完成率统计 */
declare interface ScheduleStatistics {
  date: string
  totalTasks: number
  completedTasks: number
  completionRate: number
}

/** 库存周转统计 */
declare interface InventoryTurnoverStatistics {
  dishName: string
  inQuantity: number
  outQuantity: number
  currentStock: number
  turnoverRate: number
}

/** 库存统计 */
declare interface InventoryStatistics {
  dishName: string
  inQuantity: number
  outQuantity: number
  turnoverRate: number
}

/** 统计查询参数 */
declare interface StatisticsQuery {
  startDate: string
  endDate: string
  type?: 'day' | 'month'
}

/* ==================== 菜单与权限 ==================== */

/** 菜单项 */
declare interface MenuItem {
  path: string
  name: string
  title: string
  icon?: string
  children?: MenuItem[]
  roles?: RoleType[]
  hidden?: boolean
}
