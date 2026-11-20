<script setup lang="ts">
/**
 * 首页仪表盘 - 数据概览
 */
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { getDashboardOverview, getDailyOrderCount, getOrderStatusDistribution } from '@/api/statistics'
import { getTimeoutOrders } from '@/api/order'

const router = useRouter()
const userStore = useUserStore()

/* 加载状态 */
const loading = ref(false)

/* 统计数据 */
const overview = ref<DashboardOverview>({
  todayOrderCount: 0,
  todayCompletedCount: 0,
  pendingScheduleCount: 0,
  deliveringCount: 0,
  lowStockCount: 0,
  totalOrderAmount: 0
})

/* 超时订单列表 */
const timeoutOrders = ref<Order[]>([])

/* 图表引用 */
const orderChartRef = ref<HTMLElement | null>(null)
const statusChartRef = ref<HTMLElement | null>(null)
let orderChart: echarts.ECharts | null = null
let statusChart: echarts.ECharts | null = null

/* 欢迎语 */
const welcomeText = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '凌晨好'
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 17) return '下午好'
  if (hour < 19) return '傍晚好'
  return '晚上好'
})

/* 统计卡片数据 */
const statCards = computed(() => [
  {
    title: '今日订单',
    value: overview.value.todayOrderCount,
    icon: 'Document',
    color: '#16A34A',
    bgColor: 'rgba(22, 163, 74, 0.10)',
    path: '/order/list'
  },
  {
    title: '已完成订单',
    value: overview.value.todayCompletedCount,
    icon: 'CircleCheck',
    color: '#67C23A',
    bgColor: 'rgba(103, 194, 58, 0.1)',
    path: '/order/list'
  },
  {
    title: '待调度',
    value: overview.value.pendingScheduleCount,
    icon: 'Clock',
    color: '#E6A23C',
    bgColor: 'rgba(230, 162, 60, 0.1)',
    path: '/schedule/pending'
  },
  {
    title: '配送中',
    value: overview.value.deliveringCount,
    icon: 'Van',
    color: '#14B8A6',
    bgColor: 'rgba(20, 184, 166, 0.10)',
    path: '/delivery/task'
  },
  {
    title: '库存预警',
    value: overview.value.lowStockCount,
    icon: 'Warning',
    color: '#F56C6C',
    bgColor: 'rgba(245, 108, 108, 0.1)',
    path: '/inventory/warning'
  },
  {
    title: '今日营业额',
    value: `¥${overview.value.totalOrderAmount.toFixed(2)}`,
    icon: 'Money',
    color: '#FF7A45',
    bgColor: 'rgba(255, 122, 69, 0.10)',
    path: '/statistics/order'
  }
])

/* 快捷操作 */
const quickActions = [
  { title: '新增订单', icon: 'Plus', path: '/order/add', color: '#16A34A' },
  { title: '待调度订单', icon: 'Clock', path: '/schedule/pending', color: '#E6A23C' },
  { title: '配送任务', icon: 'Van', path: '/delivery/task', color: '#14B8A6' },
  { title: '库存管理', icon: 'Box', path: '/inventory/list', color: '#909399' }
]

/* 获取统计数据 */
const fetchOverview = async () => {
  try {
    const res = await getDashboardOverview()
    overview.value = res.data.data
  } catch (error) {
    ElMessage.error('获取统计数据失败')
    console.error('获取统计数据失败:', error)
  }
}

/* 获取超时订单 */
const fetchTimeoutOrders = async () => {
  try {
    const res = await getTimeoutOrders()
    timeoutOrders.value = res.data.data || []
  } catch (error) {
    /* 使用模拟数据 */
    timeoutOrders.value = []
  }
}

/* 初始化订单趋势图表 */
const initOrderChart = async () => {
  if (!orderChartRef.value) return

  orderChart = echarts.init(orderChartRef.value)

  /* 获取近7天日期 */
  const dates = Array.from({ length: 7 }, (_, i) => {
    const date = new Date()
    date.setDate(date.getDate() - (6 - i))
    return `${date.getMonth() + 1}/${date.getDate()}`
  })

  /* 从API获取数据 */
  let orderData: number[] = []
  let completedData: number[] = []
  try {
    const endDate = new Date().toISOString().split('T')[0]
    const startDate = new Date(Date.now() - 6 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]
    const res = await getDailyOrderCount({ startDate, endDate })
    const apiData = res.data.data || []
    orderData = apiData.map((item: any) => item.count || 0)
    completedData = apiData.map((item: any) => item.completedCount || item.count || 0)
  } catch (error) {
    console.error('获取订单趋势数据失败:', error)
    orderData = [0, 0, 0, 0, 0, 0, 0]
    completedData = [0, 0, 0, 0, 0, 0, 0]
  }
  
  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    legend: {
      data: ['订单数', '完成数'],
      top: 10
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: dates,
      axisLine: { lineStyle: { color: '#E4E7ED' } },
      axisLabel: { color: '#606266' }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisTick: { show: false },
      splitLine: { lineStyle: { color: '#E4E7ED', type: 'dashed' } },
      axisLabel: { color: '#606266' }
    },
    series: [
      {
        name: '订单数',
        type: 'bar',
        data: orderData,
        itemStyle: { color: '#16A34A', borderRadius: [4, 4, 0, 0] },
        barWidth: 20
      },
      {
        name: '完成数',
        type: 'bar',
        data: completedData,
        itemStyle: { color: '#FF8A3D', borderRadius: [4, 4, 0, 0] },
        barWidth: 20
      }
    ]
  }
  
  orderChart.setOption(option)
}

/* 初始化订单状态分布图表 */
const initStatusChart = async () => {
  if (!statusChartRef.value) return

  statusChart = echarts.init(statusChartRef.value)

  /* 从API获取数据 */
  let statusData: Array<{ name: string; value: number; color: string }> = []
  try {
    const res = await getOrderStatusDistribution()
    const apiData = res.data.data || []
    const colorMap: Record<string, string> = {
      '待调度': '#F59E0B',
      '已调度': '#14B8A6',
      '配送中': '#FF7A45',
      '已完成': '#16A34A',
      '已取消': '#909399'
    }
    statusData = apiData.map((item: any) => ({
      name: item.statusName || '未知',
      value: item.count || 0,
      color: colorMap[item.statusName || ''] || '#909399'
    }))
  } catch (error) {
    console.error('获取订单状态分布数据失败:', error)
    statusData = []
  }

  // 过滤掉数量为0的数据，用于饼图显示
  const chartData = statusData.filter(item => item.value > 0)

  // 如果没有数据，显示暂无数据提示
  if (chartData.length === 0) {
    const option: echarts.EChartsOption = {
      title: {
        text: '暂无订单数据',
        left: 'center',
        top: 'center',
        textStyle: {
          fontSize: 16,
          color: '#909399'
        }
      },
      series: []
    }
    statusChart.setOption(option)
    return
  }

  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      top: 'center',
      formatter: (name: string) => {
        const item = statusData.find(s => s.name === name)
        return item ? `${name} (${item.value})` : name
      }
    },
    series: [
      {
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['60%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 4,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: '{b}\n{c}单'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 14,
            fontWeight: 'bold'
          }
        },
        labelLine: { show: true },
        data: chartData.map(item => ({
          name: item.name,
          value: item.value,
          itemStyle: { color: item.color }
        }))
      }
    ]
  }

  statusChart.setOption(option)
}

/* 跳转页面 */
const goTo = (path: string) => {
  router.push(path)
}

/* 窗口大小变化时重绘图表 */
const handleResize = () => {
  orderChart?.resize()
  statusChart?.resize()
}

/* 自动刷新定时器 */
let refreshTimer: ReturnType<typeof setInterval> | null = null

/* 启动自动刷新 */
const startAutoRefresh = () => {
  refreshTimer = setInterval(async () => {
    await Promise.all([fetchOverview(), fetchTimeoutOrders()])
  }, 30000) /* 每30秒刷新一次 */
}

/* 停止自动刷新 */
const stopAutoRefresh = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

onMounted(async () => {
  loading.value = true
  await Promise.all([fetchOverview(), fetchTimeoutOrders()])
  loading.value = false
  
  /* 初始化图表 */
  setTimeout(() => {
    initOrderChart()
    initStatusChart()
  }, 100)
  
  /* 监听窗口大小变化 */
  window.addEventListener('resize', handleResize)
  
  /* 启动自动刷新 */
  startAutoRefresh()
})

onUnmounted(() => {
  /* 清理事件监听和定时器 */
  window.removeEventListener('resize', handleResize)
  stopAutoRefresh()
  /* 销毁图表实例 */
  orderChart?.dispose()
  statusChart?.dispose()
})
</script>

<template>
  <div class="dashboard-container" v-loading="loading">
    <!-- 欢迎区域 -->
    <div class="welcome-section">
      <div class="welcome-text">
        <h2>{{ welcomeText }}，{{ userStore.userInfo?.realname || userStore.userInfo?.username }}！</h2>
        <p>欢迎使用生鲜即时配送调度系统，祝您工作愉快~</p>
      </div>
      <div class="welcome-time">
        {{ new Date().toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' }) }}
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :xs="12" :sm="8" :md="4" v-for="card in statCards" :key="card.title">
        <el-card class="stat-card" shadow="hover" @click="goTo(card.path)">
          <div class="stat-icon" :style="{ backgroundColor: card.bgColor }">
            <el-icon :size="28" :color="card.color"><component :is="card.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ card.value }}</span>
            <span class="stat-title">{{ card.title }}</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷操作 -->
    <el-card class="quick-actions-card" shadow="never">
      <template #header>
        <span class="card-title">快捷操作</span>
      </template>
      <div class="quick-actions">
        <div 
          v-for="action in quickActions" 
          :key="action.title" 
          class="action-item"
          @click="goTo(action.path)"
        >
          <el-icon :size="24" :color="action.color"><component :is="action.icon" /></el-icon>
          <span>{{ action.title }}</span>
        </div>
      </div>
    </el-card>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-section">
      <el-col :xs="24" :md="14">
        <el-card shadow="never">
          <template #header>
            <span class="card-title">近7天订单趋势</span>
          </template>
          <div ref="orderChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="10">
        <el-card shadow="never">
          <template #header>
            <span class="card-title">订单状态分布</span>
          </template>
          <div ref="statusChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 超时预警 -->
    <el-card v-if="timeoutOrders.length > 0" class="timeout-card" shadow="never">
      <template #header>
        <div class="timeout-header">
          <span class="card-title">
            <el-icon color="#F56C6C"><Warning /></el-icon>
            超时未调度订单
          </span>
          <el-tag type="danger" size="small">{{ timeoutOrders.length }}个</el-tag>
        </div>
      </template>
      <el-table :data="timeoutOrders" stripe style="width: 100%">
        <el-table-column prop="orderNo" label="订单编号" width="180" />
        <el-table-column prop="customerName" label="客户名称" width="120" />
        <el-table-column prop="deliveryAddress" label="配送地址" show-overflow-tooltip />
        <el-table-column prop="expectedDeliveryTime" label="期望配送时间" width="180" />
        <el-table-column prop="createTime" label="下单时间" width="180" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="goTo(`/order/detail/${row.id}`)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<style scoped lang="scss">
.dashboard-container {
  padding: 0;
}

/* 欢迎区域 */
.welcome-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px;
  background: linear-gradient(135deg, #0f5132 0%, #16a34a 60%, #4ade80 100%);
  border-radius: 10px;
  color: #fff;
  margin-bottom: 20px;
  box-shadow: 0 6px 18px rgba(22, 163, 74, 0.18);
  
  .welcome-text {
    h2 {
      margin: 0 0 8px;
      font-size: 24px;
      font-weight: 600;
    }
    p {
      margin: 0;
      opacity: 0.9;
    }
  }
  
  .welcome-time {
    font-size: 14px;
    opacity: 0.9;
  }
}

/* 统计卡片 */
.stat-cards {
  margin-bottom: 20px;
  
  .stat-card {
    cursor: pointer;
    transition: all 0.3s;
    
    &:hover {
      transform: translateY(-4px);
    }
    
    :deep(.el-card__body) {
      display: flex;
      align-items: center;
      padding: 20px;
    }
    
    .stat-icon {
      width: 56px;
      height: 56px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 16px;
    }
    
    .stat-info {
      display: flex;
      flex-direction: column;
      
      .stat-value {
        font-size: 24px;
        font-weight: 600;
        color: #303133;
        line-height: 1.2;
      }
      
      .stat-title {
        font-size: 14px;
        color: #909399;
        margin-top: 4px;
      }
    }
  }
}

/* 快捷操作 */
.quick-actions-card {
  margin-bottom: 20px;
  
  .quick-actions {
    display: flex;
    gap: 16px;
    flex-wrap: wrap;
    
    .action-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 16px 24px;
      border-radius: 8px;
      background-color: #f5f7fa;
      cursor: pointer;
      transition: all 0.3s;
      
      &:hover {
        background-color: #e6f0ff;
        transform: translateY(-2px);
      }
      
      span {
        margin-top: 8px;
        font-size: 14px;
        color: #606266;
      }
    }
  }
}

/* 卡片标题 */
.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 图表区域 */
.chart-section {
  margin-bottom: 20px;
  
  .chart-container {
    height: 300px;
  }
}

/* 超时预警 */
.timeout-card {
  .timeout-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }
}

/* 响应式适配 */
@media screen and (max-width: 768px) {
  .welcome-section {
    flex-direction: column;
    text-align: center;
    
    .welcome-time {
      margin-top: 12px;
    }
  }
  
  .stat-cards {
    .el-col {
      margin-bottom: 12px;
    }
  }
}
</style>
