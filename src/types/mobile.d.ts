/**
 * 移动端（客户 / 配送员）类型定义
 */

/** 移动端当前用户资料 */
declare interface MobileProfile {
  id: string
  username: string
  realname: string
  phone?: string
  role: number
  roleName: string
  avatar?: string
  person?: DeliveryPerson
}

/** 移动端商品（客户首页） */
declare interface MobileDish {
  id: string
  dishName: string
  dishCode: string
  categoryId: string
  categoryName?: string
  price: number
  unit: string
  spec?: string
  description?: string
  imageUrl?: string
  stock: number
}

/** 商品分类 */
declare interface MobileCategory {
  id: string
  categoryName: string
  sortOrder: number
}

/** 客户下单请求 */
declare interface MobileCreateOrderRequest {
  customerName?: string
  customerPhone?: string
  deliveryAddress: string
  deliveryArea: string
  expectedDeliveryTime: string
  remark?: string
  /** 地图选点经度（可选） */
  longitude?: number
  /** 地图选点纬度（可选） */
  latitude?: number
  items: Array<{ dishId: string; quantity: number }>
}

/** 配送员工作台统计 */
declare interface MobileSummary {
  personId: string
  personName: string
  phone?: string
  vehicleNo?: string
  status: number
  currentTaskCount: number
  totalDeliveryCount: number
  pendingCount: number
  deliveringCount: number
  completedTodayCount: number
}
