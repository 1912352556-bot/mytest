<script setup lang="ts">
/**
 * 数据概览页面 - 统计报表
 */
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { getDashboardOverview, getDailyOrderCount, getOrderStatusDistribution, getHotDishRanking } from '@/api/statistics'

const loading = ref(false)

/* 图表引用 */
const trendChartRef = ref<HTMLElement | null>(null)
const statusChartRef = ref<HTMLElement | null>(null)
const dishChartRef = ref<HTMLElement | null>(null)

let trendChart: echarts.ECharts | null = null
let statusChart: echarts.ECharts | null = null
let dishChart: echarts.ECharts | null = null

/* 初始化订单趋势图 */
const initTrendChart = async () => {
  if (!trendChartRef.value) return
  trendChart = echarts.init(trendChartRef.value)

  const dates = Array.from({ length: 30 }, (_, i) => {
    const d = new Date()
    d.setDate(d.getDate() - (29 - i))
    return `${d.getMonth() + 1}/${d.getDate()}`
  })

  /* 从API获取数据 */
  let orderData: number[] = []
  let amountData: number[] = []
  try {
    const endDate = new Date().toISOString().split('T')[0]
    const startDate = new Date(Date.now() - 29 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]
    const res = await getDailyOrderCount({ startDate, endDate })
    const apiData = res.data.data || []
    orderData = apiData.map((item: any) => item.count || 0)
    amountData = apiData.map((item: any) => item.amount || 0)
  } catch (error) {
    console.error('获取订单趋势数据失败:', error)
    orderData = dates.map(() => 0)
    amountData = dates.map(() => 0)
  }
  
  const option: echarts.EChartsOption = {
    tooltip: { trigger: 'axis', axisPointer: { type: 'cross' } },
    legend: { data: ['订单数', '营业额'], top: 10 },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: dates, boundaryGap: false },
    yAxis: [
      { type: 'value', name: '订单数', position: 'left' },
      { type: 'value', name: '营业额(元)', position: 'right' }
    ],
    series: [
      { name: '订单数', type: 'line', data: orderData, smooth: true, itemStyle: { color: '#16A34A' }, areaStyle: { color: 'rgba(22,163,74,0.18)' } },
      { name: '营业额', type: 'line', yAxisIndex: 1, data: amountData, smooth: true, itemStyle: { color: '#FF7A45' }, areaStyle: { color: 'rgba(255,122,69,0.18)' } }
    ]
  }
  trendChart.setOption(option)
}

/* 初始化订单状态分布图 */
const initStatusChart = async () => {
  if (!statusChartRef.value) return
  statusChart = echarts.init(statusChartRef.value)

  /* 从API获取数据 */
  let allData: Array<{ name: string; value: number; itemStyle: { color: string } }> = []
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
    allData = apiData.map((item: any) => ({
      name: item.statusName || '未知',
      value: item.count || 0,
      itemStyle: { color: colorMap[item.statusName || ''] || '#909399' }
    }))
  } catch (error) {
    console.error('获取订单状态分布数据失败:', error)
    allData = []
  }

  // 只显示有数据的状态
  const chartData = allData.filter(item => item.value > 0)

  // 如果没有数据，显示提示
  if (chartData.length === 0) {
    const option: echarts.EChartsOption = {
      title: {
        text: '暂无订单数据',
        left: 'center',
        top: 'center',
        textStyle: { fontSize: 16, color: '#909399' }
      },
      series: []
    }
    statusChart.setOption(option)
    return
  }

  const option: echarts.EChartsOption = {
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: {
      orient: 'vertical',
      left: 'left',
      top: 'center',
      formatter: (name: string) => {
        const item = allData.find(d => d.name === name)
        return item ? `${name} (${item.value})` : name
      }
    },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['60%', '50%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 4, borderColor: '#fff', borderWidth: 2 },
      label: {
        show: true,
        formatter: '{b}\n{c}单'
      },
      emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
      data: chartData
    }]
  }
  statusChart.setOption(option)
}

/* 初始化热销商品图 */
const initDishChart = async () => {
  if (!dishChartRef.value) return
  dishChart = echarts.init(dishChartRef.value)

  /* 从API获取数据 */
  let data: Array<{ name: string; value: number }> = []
  try {
    const res = await getHotDishRanking({ limit: 5 })
    const apiData = res.data.data || []
    data = apiData.map((item: any) => ({
      name: item.dishName || '',
      value: item.quantity || 0
    })).reverse()
  } catch (error) {
    console.error('获取热销商品数据失败:', error)
    data = []
  }
  
  const option: echarts.EChartsOption = {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '10%', bottom: '3%', containLabel: true },
    xAxis: { type: 'value', name: '销量' },
    yAxis: { type: 'category', data: data.map(d => d.name) },
    series: [{
      type: 'bar',
      data: data.map(d => d.value),
      itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [{ offset: 0, color: '#67C23A' }, { offset: 1, color: '#95d475' }]), borderRadius: [0, 4, 4, 0] },
      barWidth: 20,
      label: { show: true, position: 'right', formatter: '{c}' }
    }]
  }
  dishChart.setOption(option)
}

/* 窗口大小变化 */
const handleResize = () => {
  trendChart?.resize()
  statusChart?.resize()
  dishChart?.resize()
}

onMounted(async () => {
  await Promise.all([
    initTrendChart(),
    initStatusChart(),
    initDishChart()
  ])
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  statusChart?.dispose()
  dishChart?.dispose()
})
</script>

<template>
  <div class="statistics-overview-container">
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card shadow="never">
          <template #header><span class="card-title">订单趋势（近30天）</span></template>
          <div ref="trendChartRef" class="chart-container large"></div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" class="mt20">
      <el-col :span="24">
        <el-card shadow="never">
          <template #header><span class="card-title">订单状态分布</span></template>
          <div ref="statusChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" class="mt20">
      <el-col :span="24">
        <el-card shadow="never">
          <template #header><span class="card-title">热销商品TOP5</span></template>
          <div ref="dishChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped lang="scss">
.statistics-overview-container {
  .card-title { font-size: 16px; font-weight: 600; }
  .chart-container { height: 300px; &.large { height: 350px; } }
  .mt20 { margin-top: 20px; }
}
</style>
