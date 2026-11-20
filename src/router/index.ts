/**
 * 路由配置 - 生鲜即时配送调度系统
 */
import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

/* 基础路由 - 无需登录 */
const baseRoutes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', hidden: true }
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('@/views/register/index.vue'),
    meta: { title: '注册', hidden: true }
  }
]

/* 主布局路由 */
const layoutRoutes: RouteRecordRaw[] = [
  {
    path: '/dashboard',
    name: 'Layout',
    component: () => import('@/layout/index.vue'),
    redirect: '/dashboard/home',
    meta: { title: '系统首页' },
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '首页', icon: 'HomeFilled', affix: true }
      }
    ]
  },
  /* 订单管理 */
  {
    path: '/order',
    name: 'Order',
    component: () => import('@/layout/index.vue'),
    redirect: '/order/list',
    meta: { title: '订单管理', icon: 'Document', roles: [0, 1] },
    children: [
      {
        path: 'list',
        name: 'OrderList',
        component: () => import('@/views/order/list.vue'),
        meta: { title: '订单列表', icon: 'List' }
      },
      {
        path: 'add',
        name: 'OrderAdd',
        component: () => import('@/views/order/add.vue'),
        meta: { title: '新增订单', icon: 'Plus' }
      },
      {
        path: 'detail/:id',
        name: 'OrderDetail',
        component: () => import('@/views/order/detail.vue'),
        meta: { title: '订单详情', hidden: true }
      }
    ]
  },
  /* 调度管理 */
  {
    path: '/schedule',
    name: 'Schedule',
    component: () => import('@/layout/index.vue'),
    redirect: '/schedule/list',
    meta: { title: '调度管理', icon: 'Calendar', roles: [0, 1] },
    children: [
      {
        path: 'list',
        name: 'ScheduleList',
        component: () => import('@/views/schedule/list.vue'),
        meta: { title: '调度任务列表', icon: 'List' }
      },
      {
        path: 'pending',
        name: 'SchedulePending',
        component: () => import('@/views/schedule/pending.vue'),
        meta: { title: '待调度订单', icon: 'Clock' }
      },
      {
        path: 'assign',
        name: 'ScheduleAssign',
        component: () => import('@/views/schedule/assign.vue'),
        meta: { title: '任务分配', icon: 'User' }
      }
    ]
  },
  /* 配送管理 */
  {
    path: '/delivery',
    name: 'Delivery',
    component: () => import('@/layout/index.vue'),
    redirect: '/delivery/task',
    meta: { title: '配送管理', icon: 'Van', roles: [0, 1, 2] },
    children: [
      {
        path: 'task',
        name: 'DeliveryTask',
        component: () => import('@/views/delivery/task.vue'),
        meta: { title: '配送任务', icon: 'Tickets' }
      },
      {
        path: 'person',
        name: 'DeliveryPerson',
        component: () => import('@/views/delivery/person.vue'),
        meta: { title: '配送人员', icon: 'UserFilled', roles: [0, 1] }
      },
      {
        path: 'track/:id',
        name: 'DeliveryTrack',
        component: () => import('@/views/delivery/track.vue'),
        meta: { title: '配送轨迹', hidden: true }
      }
    ]
  },
  /* 库存管理 */
  {
    path: '/inventory',
    name: 'Inventory',
    component: () => import('@/layout/index.vue'),
    redirect: '/inventory/list',
    meta: { title: '库存管理', icon: 'Box', roles: [0, 1] },
    children: [
      {
        path: 'list',
        name: 'InventoryList',
        component: () => import('@/views/inventory/list.vue'),
        meta: { title: '库存列表', icon: 'List' }
      },
      {
        path: 'warning',
        name: 'InventoryWarning',
        component: () => import('@/views/inventory/warning.vue'),
        meta: { title: '库存预警', icon: 'Warning' }
      },
      {
        path: 'record/:id',
        name: 'InventoryRecord',
        component: () => import('@/views/inventory/record.vue'),
        meta: { title: '出入库记录', hidden: true }
      }
    ]
  },
  /* 统计分析 */
  {
    path: '/statistics',
    name: 'Statistics',
    component: () => import('@/layout/index.vue'),
    redirect: '/statistics/overview',
    meta: { title: '统计分析', icon: 'DataAnalysis', roles: [0, 1] },
    children: [
      {
        path: 'overview',
        name: 'StatisticsOverview',
        component: () => import('@/views/statistics/overview.vue'),
        meta: { title: '数据概览', icon: 'DataBoard' }
      },
      {
        path: 'order',
        name: 'StatisticsOrder',
        component: () => import('@/views/statistics/order.vue'),
        meta: { title: '订单统计', icon: 'TrendCharts' }
      },
      {
        path: 'schedule',
        name: 'StatisticsSchedule',
        component: () => import('@/views/statistics/schedule.vue'),
        meta: { title: '调度统计', icon: 'PieChart' }
      },
      {
        path: 'inventory',
        name: 'StatisticsInventory',
        component: () => import('@/views/statistics/inventory.vue'),
        meta: { title: '库存统计', icon: 'Histogram' }
      }
    ]
  },
  /* 系统管理 */
  {
    path: '/system',
    name: 'System',
    component: () => import('@/layout/index.vue'),
    redirect: '/system/user',
    meta: { title: '系统管理', icon: 'Setting', roles: [0] },
    children: [
      {
        path: 'user',
        name: 'SystemUser',
        component: () => import('@/views/system/user.vue'),
        meta: { title: '用户管理', icon: 'User' }
      },
      {
        path: 'dict',
        name: 'SystemDict',
        component: () => import('@/views/system/dict.vue'),
        meta: { title: '数据字典', icon: 'Files' }
      },
      {
        path: 'dish',
        name: 'SystemDish',
        component: () => import('@/views/system/dish.vue'),
        meta: { title: '商品管理', icon: 'Food' }
      }
    ]
  }
]

/* 404页面 */
const errorRoutes: RouteRecordRaw[] = [
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '页面不存在', hidden: true }
  }
]

/* 创建路由实例 */
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [...baseRoutes, ...layoutRoutes, ...errorRoutes],
  /* 滚动行为 */
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
})

/* 导出布局路由供菜单使用 */
export const menuRoutes = layoutRoutes

export default router
