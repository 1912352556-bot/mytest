/**
 * 路由权限守卫 - 统一处理路由访问权限
 *
 * PC 端（Element Plus）与移动端（/m，Vant）共用同一个守卫：
 * - 移动端未登录 → /m/login；角色不符 → 回到自己角色的移动端首页
 * - PC 端逻辑保持原样；客户（role=3）访问 PC 页面时引导到移动端
 */
import router from '.'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import { ElMessage } from 'element-plus'
import { getToken } from '@/utils/request'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

/* 配置 NProgress */
NProgress.configure({ showSpinner: false })

/* 白名单路由 - 无需登录即可访问 */
const whiteList = ['/', '/register', '/404', '/m/login']

/* 是否移动端路由 */
const isMobilePath = (path: string) => path === '/m' || path.startsWith('/m/')

/* 移动端首页（客户 / 配送员），其他角色返回 null */
const mobileHome = (role?: number): string | null => {
  if (role === 2) return '/m/courier/tasks'
  if (role === 3) return '/m/customer/home'
  return null
}

/* 路由前置守卫 */
router.beforeEach(async (to, from, next) => {
  /* 开始进度条 */
  NProgress.start()

  /* 设置页面标题 */
  const title = to.meta?.title as string
  if (title) {
    document.title = `${title} - 生鲜即时配送调度系统`
  }

  /* 获取Token */
  const token = getToken()
  /* 获取用户仓库 */
  const userStore = useUserStore()
  const appStore = useAppStore()

  /* ==================== 移动端 ==================== */
  if (isMobilePath(to.path)) {
    /* 登录页特殊处理 */
    if (to.path === '/m/login') {
      if (token) {
        if (!userStore.userInfo) {
          userStore.restoreUserInfo()
        }
        const home = mobileHome(userStore.userInfo?.role)
        if (home) {
          next({ path: home })
          NProgress.done()
          return
        }
      }
      next()
      return
    }

    if (!token) {
      next({ path: '/m/login', query: { redirect: to.fullPath } })
      NProgress.done()
      return
    }

    if (!userStore.userInfo) {
      userStore.restoreUserInfo()
    }

    if (!userStore.userInfo) {
      try {
        await userStore.fetchUserInfo()
      } catch (error) {
        userStore.clearUserInfo()
        ElMessage.error('登录状态已过期，请重新登录')
        next({ path: '/m/login' })
        NProgress.done()
        return
      }
    }

    const role = userStore.userInfo?.role
    const home = mobileHome(role)

    /* /m 根路径按角色分流 */
    if (to.path === '/m') {
      next(home ? { path: home } : { path: '/dashboard/home' })
      NProgress.done()
      return
    }

    /* 管理员 / 调度员没有移动端页面 */
    if (!home) {
      ElMessage.warning('该账号请使用电脑端')
      next({ path: '/dashboard/home' })
      NProgress.done()
      return
    }

    /* 角色校验 */
    const roles = to.meta?.roles as number[] | undefined
    if (roles && roles.length > 0 && !roles.includes(role as number)) {
      ElMessage.error('您没有权限访问该页面')
      next({ path: home })
      NProgress.done()
      return
    }

    next()
    return
  }

  /* ==================== PC 端 ==================== */
  if (token) {
    /* 已登录 */
    if (to.path === '/') {
      /* 已登录访问登录页，重定向到首页 */
      next({ path: '/dashboard/home' })
      NProgress.done()
    } else {
      /* 检查是否有用户信息 */
      if (!userStore.userInfo) {
        /* 尝试从本地存储恢复 */
        userStore.restoreUserInfo()
      }

      if (userStore.userInfo) {
        /* 客户账号只能使用移动端 */
        if (userStore.userInfo.role === 3 && !whiteList.includes(to.path)) {
          ElMessage.warning('客户账号请使用移动端')
          next({ path: '/m/customer/home' })
          NProgress.done()
          return
        }

        /* 检查路由权限 */
        const roles = to.meta?.roles as number[] | undefined
        if (roles && roles.length > 0) {
          /* 需要权限验证 */
          if (userStore.hasPermission(roles as RoleType[])) {
            /* 设置当前激活菜单 */
            appStore.setActiveMenu(to.path)
            next()
          } else {
            ElMessage.error('您没有权限访问该页面')
            next({ path: '/dashboard/home' })
            NProgress.done()
          }
        } else {
          /* 无需权限验证 */
          appStore.setActiveMenu(to.path)
          next()
        }
      } else {
        /* 没有用户信息，尝试获取 */
        try {
          await userStore.fetchUserInfo()
          /* 重新进入当前路由 */
          next({ ...to, replace: true })
        } catch (error) {
          /* 获取失败，清除Token并跳转登录页 */
          userStore.clearUserInfo()
          ElMessage.error('登录状态已过期，请重新登录')
          next({ path: '/' })
          NProgress.done()
        }
      }
    }
  } else {
    /* 未登录 */
    if (whiteList.includes(to.path)) {
      /* 白名单路由，直接放行 */
      next()
    } else {
      /* 非白名单路由，重定向到登录页 */
      ElMessage.warning('请先登录')
      next({ path: '/', query: { redirect: to.fullPath } })
      NProgress.done()
    }
  }
})

/* 路由后置守卫 */
router.afterEach((to, from) => {
  /* 结束进度条 */
  NProgress.done()

  /* 移动端不使用 PC 的面包屑 / 页签，直接返回 */
  if (isMobilePath(to.path)) {
    return
  }

  /* 更新面包屑 */
  const appStore = useAppStore()
  const breadcrumbs: Array<{ title: string; path?: string }> = []
  
  /* 生成面包屑 */
  to.matched.forEach((route) => {
    if (route.meta?.title && !route.meta?.hidden) {
      breadcrumbs.push({
        title: route.meta.title as string,
        path: route.path
      })
    }
  })
  
  appStore.setBreadcrumbs(breadcrumbs)

  /* 添加标签页 */
  if (to.meta?.title && !to.meta?.hidden) {
    appStore.addTab({
      title: to.meta.title as string,
      path: to.path,
      name: to.name as string
    })
  }
})
