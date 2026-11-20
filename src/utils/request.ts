/**
 * Axios请求封装 - 统一处理请求拦截、响应拦截、错误处理
 */
import axios, { type AxiosInstance, type AxiosRequestConfig, type InternalAxiosRequestConfig } from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { showToast } from 'vant'
import router from '@/router'

/* 当前是否处于移动端页面（/m 开头） */
const isMobileView = (): boolean => window.location.pathname.startsWith('/m')

/* 统一错误提示：移动端用 Vant Toast，PC 端用 Element Message */
const errorTip = (msg: string): void => {
  if (isMobileView()) {
    showToast(msg)
  } else {
    ElMessage.error(msg)
  }
}

/* 统一警告提示 */
const warnTip = (msg: string): void => {
  if (isMobileView()) {
    showToast(msg)
  } else {
    ElMessage.warning(msg)
  }
}

/* Token存储的Key */
const TOKEN_KEY = 'access_token'

/* 获取Token */
export const getToken = (): string | null => {
  return localStorage.getItem(TOKEN_KEY)
}

/* 设置Token */
export const setToken = (token: string): void => {
  localStorage.setItem(TOKEN_KEY, token)
}

/* 移除Token */
export const removeToken = (): void => {
  localStorage.removeItem(TOKEN_KEY)
}

/* 创建自定义 axios 实例 */
const request: AxiosInstance = axios.create({
  /* 设置基本地址 */
  baseURL: import.meta.env.VITE_APP_BASE_API,
  /* 设置超时时间 单位ms */
  timeout: 15000,
  /* 设置请求头 */
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
})

/* 是否正在刷新Token的标记 */
let isRefreshing = false
/* 重试队列 */
let retryRequests: Array<(token: string) => void> = []

/* 请求拦截器 - 添加Token */
request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    /* 获取Token */
    const token = getToken()
    /* 如果Token存在，则添加到请求头 */
    if (token && config.headers) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    console.error('请求拦截器错误:', error)
    return Promise.reject(error)
  }
)

/* 响应拦截器 - 统一处理响应和错误 */
request.interceptors.response.use(
  (response) => {
    const { data } = response
    
    /* 如果是文件下载，直接返回 */
    if (response.config.responseType === 'blob') {
      return response
    }
    
    /* 业务状态码判断 */
    if (data.code === 200 || data.flag === true) {
      return response
    }
    
    /* 业务错误处理 */
    errorTip(data.msg || '请求失败')
    return Promise.reject(new Error(data.msg || '请求失败'))
  },
  (error) => {
    const { response, message } = error
    
    if (response) {
      const { status, data } = response
      
      switch (status) {
        case 401:
          /* Token过期或无效 */
          removeToken()
          localStorage.removeItem('userInfo')
          if (isMobileView()) {
            /* 移动端：直接回登录页，不弹 Element 确认框 */
            if (!isRefreshing) {
              isRefreshing = true
              showToast('登录状态已过期，请重新登录')
              router.push({ path: '/m/login' }).finally(() => {
                isRefreshing = false
              })
            }
          } else if (!isRefreshing) {
            isRefreshing = true
            ElMessageBox.confirm('登录状态已过期，请重新登录', '系统提示', {
              confirmButtonText: '重新登录',
              cancelButtonText: '取消',
              type: 'warning'
            }).then(() => {
              /* 跳转到登录页 */
              router.push({ path: '/' })
            }).finally(() => {
              isRefreshing = false
            })
          }
          break
        case 403:
          errorTip('没有权限访问该资源')
          break
        case 404:
          errorTip('请求的资源不存在')
          break
        case 500:
          errorTip(data?.msg || '服务器内部错误')
          break
        default:
          errorTip(data?.msg || `请求失败: ${status}`)
      }
    } else if (message.includes('Network Error')) {
      warnTip('网络连接失败，请检查网络')
    } else if (message.includes('timeout')) {
      warnTip('请求超时，请稍后重试')
    } else {
      errorTip(message || '请求失败')
    }
    
    return Promise.reject(error)
  }
)

/* 导出请求实例 */
export default request