<script setup lang="ts">
/**
 * 后台管理主布局组件
 */
import { computed, ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import { menuRoutes } from '@/router'
import { ElMessageBox } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const appStore = useAppStore()

/* 侧边栏折叠状态 */
const isCollapse = computed(() => appStore.sidebarCollapsed)

/* 当前激活的菜单 */
const activeMenu = computed(() => appStore.activeMenu || route.path)

/* 面包屑 */
const breadcrumbs = computed(() => appStore.breadcrumbs)

/* 用户信息 */
const userInfo = computed(() => userStore.userInfo)

/* 用户角色名称 */
const roleName = computed(() => userStore.userRoleName)

/* 根据用户角色过滤菜单 */
const filteredMenus = computed(() => {
  const userRole = userStore.userRole
  return menuRoutes.filter(menu => {
    const roles = menu.meta?.roles as number[] | undefined
    if (!roles || roles.length === 0) return true
    return roles.includes(userRole)
  }).map(menu => ({
    ...menu,
    children: menu.children?.filter(child => {
      const childRoles = child.meta?.roles as number[] | undefined
      if (!childRoles || childRoles.length === 0) return true
      return childRoles.includes(userRole)
    }).filter(child => !child.meta?.hidden)
  }))
})

/* 切换侧边栏折叠状态 */
const toggleSidebar = () => {
  appStore.toggleSidebar()
}

/* 处理菜单点击 */
const handleMenuSelect = (index: string) => {
  router.push(index)
}

/* 退出登录 */
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await userStore.logout()
  } catch (e) {
    /* 取消操作 */
  }
}

/* 跳转到个人中心 */
const goProfile = () => {
  router.push('/profile')
}

/* 获取菜单图标 */
const getMenuIcon = (icon?: string) => {
  return icon || 'Menu'
}
</script>

<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="layout-aside">
      <!-- Logo区域 -->
      <div class="logo-container" :class="{ 'is-collapse': isCollapse }">
        <el-icon size="28" color="#4ADE80"><Cherry /></el-icon>
        <span v-show="!isCollapse" class="logo-text">生鲜配送调度系统</span>
      </div>
      
      <!-- 菜单 -->
      <el-scrollbar class="menu-scrollbar">
        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapse"
          :collapse-transition="false"
          background-color="#14342B"
          text-color="#C7DFD3"
          active-text-color="#4ADE80"
          @select="handleMenuSelect"
          router
        >
          <template v-for="menu in filteredMenus" :key="menu.path">
            <!-- 有子菜单 -->
            <el-sub-menu v-if="menu.children && menu.children.length > 0" :index="menu.path">
              <template #title>
                <el-icon><component :is="getMenuIcon(menu.meta?.icon as string)" /></el-icon>
                <span>{{ menu.meta?.title }}</span>
              </template>
              <el-menu-item 
                v-for="child in menu.children" 
                :key="child.path"
                :index="`${menu.path}/${child.path}`"
              >
                <el-icon><component :is="getMenuIcon(child.meta?.icon as string)" /></el-icon>
                <span>{{ child.meta?.title }}</span>
              </el-menu-item>
            </el-sub-menu>
            <!-- 无子菜单 -->
            <el-menu-item v-else :index="menu.redirect || menu.path">
              <el-icon><component :is="getMenuIcon(menu.meta?.icon as string)" /></el-icon>
              <span>{{ menu.meta?.title }}</span>
            </el-menu-item>
          </template>
        </el-menu>
      </el-scrollbar>
    </el-aside>

    <!-- 主区域 -->
    <el-container class="main-container">
      <!-- 头部 -->
      <el-header class="layout-header">
        <div class="header-left">
          <!-- 折叠按钮 -->
          <el-icon 
            class="collapse-btn" 
            @click="toggleSidebar"
          >
            <component :is="isCollapse ? 'Expand' : 'Fold'" />
          </el-icon>
          
          <!-- 面包屑 -->
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/dashboard/home' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item 
              v-for="(item, index) in breadcrumbs" 
              :key="index"
              :to="item.path ? { path: item.path } : undefined"
            >
              {{ item.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        
        <div class="header-right">
          <!-- 全屏按钮 -->
          <el-tooltip content="刷新" placement="bottom">
            <el-icon class="header-icon" @click="() => location.reload()"><Refresh /></el-icon>
          </el-tooltip>
          
          <!-- 用户下拉菜单 -->
          <el-dropdown trigger="click" @command="(cmd: string) => cmd === 'logout' ? handleLogout() : goProfile()">
            <div class="user-info">
              <el-avatar :size="32" :src="userInfo?.avatar">
                <el-icon><UserFilled /></el-icon>
              </el-avatar>
              <span class="username">{{ userInfo?.realname || userInfo?.username }}</span>
              <el-tag size="small" type="info" class="role-tag">{{ roleName }}</el-tag>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人中心
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 内容区域 -->
      <el-main class="layout-main">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <keep-alive>
              <component :is="Component" />
            </keep-alive>
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped lang="scss">
.layout-container {
  height: 100vh;
  width: 100%;
}

.layout-aside {
  background-color: var(--app-sidebar-bg);
  transition: width 0.3s;
  overflow: hidden;
  
  .logo-container {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0 16px;
    background-color: var(--app-sidebar-deep);
    border-bottom: 1px solid rgba(74, 222, 128, 0.12);
    
    &.is-collapse {
      padding: 0;
    }
    
    .logo-text {
      margin-left: 10px;
      font-size: 16px;
      font-weight: bold;
      color: #fff;
      white-space: nowrap;
      letter-spacing: 1px;
    }
  }
  
  .menu-scrollbar {
    height: calc(100vh - 60px);
  }
  
  :deep(.el-menu) {
    border-right: none;
  }

  /* 菜单项：悬停淡绿底，激活项左侧亮绿竖条 */
  :deep(.el-menu-item),
  :deep(.el-sub-menu__title) {
    &:hover {
      background-color: rgba(74, 222, 128, 0.08) !important;
    }
  }

  :deep(.el-menu-item.is-active) {
    background-color: rgba(74, 222, 128, 0.14) !important;
    box-shadow: inset 3px 0 0 var(--app-sidebar-active);
  }
}

.main-container {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.layout-header {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(20, 52, 43, 0.08);
  border-bottom: 1px solid var(--app-border);
  
  .header-left {
    display: flex;
    align-items: center;
    
    .collapse-btn {
      font-size: 20px;
      cursor: pointer;
      margin-right: 20px;
      
      &:hover {
        color: var(--app-brand);
      }
    }
  }
  
  .header-right {
    display: flex;
    align-items: center;
    
    .header-icon {
      font-size: 18px;
      cursor: pointer;
      margin-right: 20px;
      
      &:hover {
        color: var(--app-brand);
      }
    }
    
    .user-info {
      display: flex;
      align-items: center;
      cursor: pointer;
      
      .username {
        margin: 0 8px;
        font-size: 14px;
      }
      
      .role-tag {
        margin-right: 8px;
      }
    }
  }
}

.layout-main {
  padding: 20px;
  background-color: var(--app-bg);
  overflow: auto;
}

/* 路由切换动画 */
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(30px);
}
</style>
