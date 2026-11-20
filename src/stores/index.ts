/**
 * Pinia Store 统一导出
 */

export { useUserStore } from './user'
export { useAppStore } from './app'
export { useDictStore } from './dict'

// 兼容旧版本的store
export { useUserInfoStore } from './useUserInfoStore'
