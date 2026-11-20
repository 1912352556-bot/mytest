/**
 * 应用全局状态管理
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAppStore = defineStore('app', () => {
  /* 侧边栏折叠状态 */
  const sidebarCollapsed = ref(false)

  /* 当前激活的菜单 */
  const activeMenu = ref('')

  /* 面包屑导航 */
  const breadcrumbs = ref<Array<{ title: string; path?: string }>>([])

  /* 页面标签列表 */
  const tabs = ref<Array<{ title: string; path: string; name: string }>>([])

  /* 当前激活的标签 */
  const activeTab = ref('')

  /* 全局加载状态 */
  const loading = ref(false)

  /* 主题设置 */
  const theme = ref({
    primaryColor: '#16A34A',
    darkMode: false
  })

  /* 切换侧边栏折叠状态 */
  function toggleSidebar(): void {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  /* 设置侧边栏折叠状态 */
  function setSidebarCollapsed(collapsed: boolean): void {
    sidebarCollapsed.value = collapsed
  }

  /* 设置当前激活的菜单 */
  function setActiveMenu(menu: string): void {
    activeMenu.value = menu
  }

  /* 设置面包屑导航 */
  function setBreadcrumbs(items: Array<{ title: string; path?: string }>): void {
    breadcrumbs.value = items
  }

  /* 添加标签页 */
  function addTab(tab: { title: string; path: string; name: string }): void {
    const exists = tabs.value.find(t => t.path === tab.path)
    if (!exists) {
      tabs.value.push(tab)
    }
    activeTab.value = tab.path
  }

  /* 移除标签页 */
  function removeTab(path: string): string | null {
    const index = tabs.value.findIndex(t => t.path === path)
    if (index > -1) {
      tabs.value.splice(index, 1)
      /* 如果关闭的是当前标签，则激活前一个或后一个标签 */
      if (activeTab.value === path && tabs.value.length > 0) {
        const newIndex = Math.min(index, tabs.value.length - 1)
        activeTab.value = tabs.value[newIndex].path
        return activeTab.value
      }
    }
    return null
  }

  /* 关闭其他标签 */
  function closeOtherTabs(path: string): void {
    tabs.value = tabs.value.filter(t => t.path === path)
    activeTab.value = path
  }

  /* 关闭所有标签 */
  function closeAllTabs(): void {
    tabs.value = []
    activeTab.value = ''
  }

  /* 设置全局加载状态 */
  function setLoading(status: boolean): void {
    loading.value = status
  }

  /* 设置主题 */
  function setTheme(newTheme: Partial<typeof theme.value>): void {
    theme.value = { ...theme.value, ...newTheme }
  }

  return {
    sidebarCollapsed,
    activeMenu,
    breadcrumbs,
    tabs,
    activeTab,
    loading,
    theme,
    toggleSidebar,
    setSidebarCollapsed,
    setActiveMenu,
    setBreadcrumbs,
    addTab,
    removeTab,
    closeOtherTabs,
    closeAllTabs,
    setLoading,
    setTheme
  }
}, {
  persist: {
    enabled: true,
    strategies: [
      {
        key: 'app',
        storage: localStorage,
        paths: ['sidebarCollapsed', 'theme']
      }
    ]
  }
})
