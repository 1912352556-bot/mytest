/**
 * 用户权限相关API
 */
import request from '@/utils/request'

/** 用户登录 */
export const login = (data: LoginUser) =>
  request<Result<UserInfo>>({
    url: '/user/login',
    method: 'post',
    data
  })

/** 用户注册 */
export const register = (data: RegUser) =>
  request<Result<void>>({
    url: '/user/register',
    method: 'post',
    data
  })

/** 退出登录 */
export const logout = () =>
  request<Result<void>>({
    url: '/user/logout',
    method: 'post'
  })

/** 获取当前用户信息 */
export const getCurrentUser = () =>
  request<Result<UserInfo>>({
    url: '/user/info',
    method: 'get'
  })

/** 修改密码 */
export const changePassword = (data: { oldPassword: string; newPassword: string }) =>
  request<Result<void>>({
    url: '/user/password',
    method: 'put',
    data
  })

/** 获取用户列表 */
export const getUserList = (params: UserQuery) =>
  request<Result<PageResult<UserInfo>>>({
    url: '/user/list',
    method: 'get',
    params
  })

/** 新增用户 */
export const addUser = (data: RegUser) =>
  request<Result<void>>({
    url: '/user',
    method: 'post',
    data
  })

/** 更新用户 */
export const updateUser = (data: UserInfo) =>
  request<Result<void>>({
    url: '/user',
    method: 'put',
    data
  })

/** 删除用户 */
export const deleteUser = (id: string) =>
  request<Result<void>>({
    url: `/user/${id}`,
    method: 'delete'
  })

/** 重置用户密码 */
export const resetPassword = (id: string) =>
  request<Result<void>>({
    url: `/user/${id}/password/reset`,
    method: 'put'
  })

/** 修改用户状态 */
export const updateUserStatus = (id: string, status: number) =>
  request<Result<void>>({
    url: `/user/${id}/status/${status}`,
    method: 'put'
  })
