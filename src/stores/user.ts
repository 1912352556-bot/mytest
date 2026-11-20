/**
 * 用户信息状态管理
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { setToken, removeToken, getToken } from '@/utils/request'
import { login as loginApi, logout as logoutApi, getCurrentUser } from '@/api/user'
import router from '@/router'

export const useUserStore = defineStore('user', () => {
  /* 用户信息 */
  const userInfo = ref<UserInfo | null>(null)
  /* Token */
  const token = ref<string | null>(getToken())

  /* 计算属性：是否已登录 */
  const isLoggedIn = computed(() => !!token.value && !!userInfo.value)

  /* 计算属性：用户角色 */
  const userRole = computed(() => userInfo.value?.role ?? -1)

  /* 计算属性：用户角色名称 */
  const userRoleName = computed(() => {
    const roleMap: Record<number, string> = {
      0: '管理员',
      1: '调度员',
      2: '配送员'
    }
    return userInfo.value ? roleMap[userInfo.value.role] || '未知角色' : ''
  })

  /* 计算属性：是否是管理员 */
  const isAdmin = computed(() => userInfo.value?.role === 0)

  /* 计算属性：是否是调度员 */
  const isScheduler = computed(() => userInfo.value?.role === 1)

  /* 计算属性：是否是配送员 */
  const isDeliveryPerson = computed(() => userInfo.value?.role === 2)

  /* 登录 */
  async function login(loginForm: LoginUser): Promise<UserInfo> {
    try {
      const res = await loginApi(loginForm)
      const data = res.data.data
      
      /* 保存Token */
      if (data.token) {
        token.value = data.token
        setToken(data.token)
      }
      
      /* 保存用户信息 */
      userInfo.value = data
      localStorage.setItem('userInfo', JSON.stringify(data))
      
      return data
    } catch (error) {
      throw error
    }
  }

  /* 退出登录 */
  async function logout(): Promise<void> {
    try {
      await logoutApi()
    } catch (error) {
      console.error('退出登录接口调用失败:', error)
    } finally {
      /* 清除状态 */
      clearUserInfo()
      /* 跳转到登录页 */
      router.push({ path: '/' })
    }
  }

  /* 获取用户信息 */
  async function fetchUserInfo(): Promise<UserInfo | null> {
    try {
      const res = await getCurrentUser()
      userInfo.value = res.data.data
      localStorage.setItem('userInfo', JSON.stringify(res.data.data))
      return res.data.data
    } catch (error) {
      console.error('获取用户信息失败:', error)
      return null
    }
  }

  /* 从本地存储恢复用户信息 */
  function restoreUserInfo(): void {
    const stored = localStorage.getItem('userInfo')
    if (stored) {
      try {
        userInfo.value = JSON.parse(stored)
      } catch (e) {
        console.error('恢复用户信息失败:', e)
        clearUserInfo()
      }
    }
  }

  /* 清除用户信息 */
  function clearUserInfo(): void {
    userInfo.value = null
    token.value = null
    removeToken()
    localStorage.removeItem('userInfo')
  }

  /* 更新用户信息 */
  function updateUserInfo(info: Partial<UserInfo>): void {
    if (userInfo.value) {
      userInfo.value = { ...userInfo.value, ...info }
      localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    }
  }

  /* 检查权限 */
  function hasPermission(roles: RoleType[]): boolean {
    if (!userInfo.value) return false
    return roles.includes(userInfo.value.role)
  }

  return {
    userInfo,
    token,
    isLoggedIn,
    userRole,
    userRoleName,
    isAdmin,
    isScheduler,
    isDeliveryPerson,
    login,
    logout,
    fetchUserInfo,
    restoreUserInfo,
    clearUserInfo,
    updateUserInfo,
    hasPermission
  }
}, {
  persist: {
    enabled: true,
    strategies: [
      {
        key: 'user',
        storage: localStorage,
        paths: ['token']
      }
    ]
  }
})
