<script setup lang="ts">
/**
 * 订单统计页面
 */
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { getOrderStatistics, getDailyOrderCount, getMonthlyOrderCount } from '@/api/statistics'

const loading = ref(false)
const dateType = ref<'day' | 'month'>('day')
const year = ref(new Date().getFullYear())

const orderChartRef = ref<HTMLElement | null>(null)
const amountChartRef = ref<HTMLElement | null>(null)
let orderChart: echarts.ECharts | null = null
let amountChart: echarts.ECharts | null = null

/* 统计数据 */
const summaryData = reactive({
  totalOrders: 0,
  totalAmount: 0,
  avgOrderAmount: 0,
  completionRate: 0
})

/* 初始化订单量图表 */
const initOrderChart = (dates: string[], data: number[]) => {
  if (!orderChartRef.value) return
  if (orderChart) orderChart.dispose()
  orderChart = echarts.init(orderChartRef.value)
  
  const option: echarts.EChartsOption = {
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: dates, boundaryGap: false },
    yAxis: { type: 'value', name: '订单数' },
    series: [{
      name: '订单数',
      type: 'line',
      data: data,
      smooth: true,
      itemStyle: { color: '#16A34A' },
      areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(64,158,255,0.4)' }, { offset: 1, color: 'rgba(64,158,255,0.1)' }]) },
      markPoint: { data: [{ type: 'max', name: '最大值' }, { type: 'min', name: '最小值' }] },
      markLine: { data: [{ type: 'average', name: '平均值' }] }
    }]
  }
  orderChart.setOption(option)
}

/* 初始化营业额图表 */
const initAmountChart = (dates: string[], data: number[]) => {
  if (!amountChartRef.value) return
  if (amountChart) amountChart.dispose()
  amountChart = echarts.init(amountChartRef.value)
  
  const option: echarts.EChartsOption = {
    tooltip: { trigger: 'axis', formatter: '{b}<br/>{a}: ¥{c}' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: dates },
    yAxis: { type: 'value', name: '金额(元)' },
    series: [{
      name: '营业额',
      type: 'bar',
      data: data,
      itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#FF7A45' }, { offset: 1, color: '#FFB08C' }]), borderRadius: [4, 4, 0, 0] },
      barWidth: '60%'
    }]
  }
  amountChart.setOption(option)
}

/* 加载数据 */
const loadData = async () => {
  loading.value = true
  try {
    if (dateType.value === 'day') {
      // 获取最近30天的数据
      const endDate = new Date().toISOString().split('T')[0]
      const startDate = new Date(Date.now() - 29 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]

      const res = await getOrderStatistics({ startDate, endDate })
      const data = res.data.data || []

      const dates = data.map((item: any) => item.date?.substring(5) || '') // 显示 MM-DD
      const orderData = data.map((item: any) => item.orderCount || 0)
      const amountData = data.map((item: any) => item.totalAmount || 0)

      // 计算汇总数据
      const totalOrders = orderData.reduce((a: number, b: number) => a + b, 0)
      const totalAmount = amountData.reduce((a: number, b: number) => a + b, 0)
      const completedCount = data.reduce((sum: number, item: any) => sum + (item.completedCount || 0), 0)

      summaryData.totalOrders = totalOrders
      summaryData.totalAmount = totalAmount
      summaryData.avgOrderAmount = totalOrders > 0 ? Math.round(totalAmount / totalOrders * 100) / 100 : 0
      summaryData.completionRate = totalOrders > 0 ? Math.round(completedCount / totalOrders * 1000) / 10 : 0

      initOrderChart(dates, orderData)
      initAmountChart(dates, amountData)
    } else {
      // 获取月度数据
      const res = await getMonthlyOrderCount({ year: year.value })
      const data = res.data.data || []

      const months = data.map((item: any) => item.month || '')
      const orderData = data.map((item: any) => item.count || 0)
      // 月度金额需要另外计算，这里用订单数 * 平均客单价估算
      const avgAmount = 120 // 估算平均客单价
      const amountData = orderData.map((count: number) => count * avgAmount)

      // 计算汇总数据
      const totalOrders = orderData.reduce((a: number, b: number) => a + b, 0)
      const totalAmount = amountData.reduce((a: number, b: number) => a + b, 0)

      summaryData.totalOrders = totalOrders
      summaryData.totalAmount = totalAmount
      summaryData.avgOrderAmount = avgAmount
      summaryData.completionRate = 0 // 月度数据暂不计算完成率

      initOrderChart(months, orderData)
      initAmountChart(months, amountData)
    }
  } catch (error) {
    ElMessage.error('获取统计数据失败')
    console.error('获取统计数据失败:', error)
  } finally {
    loading.value = false
  }
}

const handleResize = () => {
  orderChart?.resize()
  amountChart?.resize()
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  orderChart?.dispose()
  amountChart?.dispose()
})
</script>

<template>
  <div class="statistics-order-container">
    <!-- 汇总卡片 -->
    <el-row :gutter="20" class="summary-cards">
      <el-col :xs="12" :sm="6">
        <el-card shadow="never" class="summary-card">
          <div class="summary-value">{{ summaryData.totalOrders }}</div>
          <div class="summary-label">总订单数</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="never" class="summary-card">
          <div class="summary-value">¥{{ summaryData.totalAmount.toLocaleString() }}</div>
          <div class="summary-label">总营业额</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="never" class="summary-card">
          <div class="summary-value">¥{{ summaryData.avgOrderAmount }}</div>
          <div class="summary-label">客单价</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="never" class="summary-card">
          <div class="summary-value">{{ summaryData.completionRate }}%</div>
          <div class="summary-label">完成率</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 筛选 -->
    <el-card shadow="never" class="filter-card">
      <el-radio-group v-model="dateType" @change="loadData">
        <el-radio-button value="day">按日统计</el-radio-button>
        <el-radio-button value="month">按月统计</el-radio-button>
      </el-radio-group>
      <el-select v-if="dateType === 'month'" v-model="year" @change="loadData" style="margin-left: 16px">
        <el-option v-for="y in [2024, 2025, 2026]" :key="y" :label="`${y}年`" :value="y" />
      </el-select>
    </el-card>

    <!-- 图表 -->
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card shadow="never">
          <template #header><span class="card-title">订单量统计</span></template>
          <div ref="orderChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="20" class="mt20">
      <el-col :span="24">
        <el-card shadow="never">
          <template #header><span class="card-title">营业额统计</span></template>
          <div ref="amountChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped lang="scss">
.statistics-order-container {
  .summary-cards { margin-bottom: 20px; }
  .summary-card {
    text-align: center;
    .summary-value { font-size: 28px; font-weight: 600; color: #16A34A; }
    .summary-label { font-size: 14px; color: #909399; margin-top: 8px; }
  }
  .filter-card { margin-bottom: 20px; }
  .card-title { font-size: 16px; font-weight: 600; }
  .chart-container { height: 350px; }
  .mt20 { margin-top: 20px; }
}
</style>
