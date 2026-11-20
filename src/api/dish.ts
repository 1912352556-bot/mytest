/**
 * 生鲜商品管理相关API
 */
import request from '@/utils/request'

/* ==================== 生鲜商品管理 ==================== */

/** 获取商品列表 */
export const getDishList = (params: DishQuery) =>
  request<Result<PageResult<Dish>>>({
    url: '/dish/list',
    method: 'get',
    params
  })

/** 获取商品详情 */
export const getDishDetail = (id: string) =>
  request<Result<Dish>>({
    url: `/dish/${id}`,
    method: 'get'
  })

/** 新增商品 */
export const  addDish = (data: Omit<Dish, 'id' | 'createTime' | 'updateTime'>) =>
  request<Result<void>>({
    url: '/dish',
    method: 'post',
    data
  })

/** 更新商品 */
export const updateDish = (data: Dish) =>
  request<Result<void>>({
    url: '/dish',
    method: 'put',
    data
  })

/** 删除商品 */
export const deleteDish = (id: string) =>
  request<Result<void>>({
    url: `/dish/${id}`,
    method: 'delete'
  })

/** 修改商品状态 */
export const updateDishStatus = (id: string, status: number) =>
  request<Result<void>>({
    url: `/dish/${id}/status/${status}`,
    method: 'put'
  })

/** 获取所有上架商品（用于下单选择） */
export const getActiveDishes = () =>
  request<Result<Dish[]>>({
    url: '/dish/active',
    method: 'get'
  })

/* ==================== 商品分类管理 ==================== */

/** 获取商品分类列表 */
export const getCategoryList = () =>
  request<Result<DishCategory[]>>({
    url: '/dish/category/list',
    method: 'get'
  })

/** 新增商品分类 */
export const addCategory = (data: Omit<DishCategory, 'id'>) =>
  request<Result<void>>({
    url: '/dish/category',
    method: 'post',
    data
  })

/** 更新商品分类 */
export const updateCategory = (data: DishCategory) =>
  request<Result<void>>({
    url: '/dish/category',
    method: 'put',
    data
  })

/** 删除商品分类 */
export const deleteCategory = (id: string) =>
  request<Result<void>>({
    url: `/dish/category/${id}`,
    method: 'delete'
  })
