<script setup lang="ts">
/**
 * 库存统计页面
 */
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { getInventoryTurnoverStatistics, getInventoryStatistics, getDashboardOverview } from '@/api/statistics'

const loading = ref(false)
const turnoverChartRef = ref<HTMLElement | null>(null)
const stockChartRef = ref<HTMLElement | null>(null)
let turnoverChart: echarts.ECharts | null = null
let stockChart: echarts.ECharts | null = null

/* 汇总数据 */
const summaryData = reactive({
  totalDishes: 0,
  totalStock: 0,
  warningCount: 0,
  avgTurnover: 0
})

/* 初始化周转率图表 */
const initTurnoverChart = (data: InventoryTurnoverStatistics[]) => {
  if (!turnoverChartRef.value) return
  turnoverChart = echarts.init(turnoverChartRef.value)

  const option: echarts.EChartsOption = {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { data: ['入库量', '出库量', '周转率'], top: 10 },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: data.map(d => d.dishName) },
    yAxis: [
      { type: 'value', name: '数量', position: 'left' },
      { type: 'value', name: '周转率', position: 'right' }
    ],
    series: [
      { name: '入库量', type: 'bar', data: data.map(d => d.inQuantity), itemStyle: { color: '#16A34A' } },
      { name: '出库量', type: 'bar', data: data.map(d => d.outQuantity), itemStyle: { color: '#FF7A45' } },
      { name: '周转率', type: 'line', yAxisIndex: 1, data: data.map(d => d.turnoverRate), itemStyle: { color: '#14B8A6' }, smooth: true }
    ]
  }
  turnoverChart.setOption(option)
}

/* 初始化库存分布图表 */
const initStockChart = (data: InventoryTurnoverStatistics[]) => {
  if (!stockChartRef.value) return
  stockChart = echarts.init(stockChartRef.value)

  // 根据当前库存量生成饼图数据
  const colors = ['#F56C6C', '#67C23A', '#14B8A6', '#E6A23C', '#909399', '#8E44AD', '#16A085', '#D35400']
  const chartData = data
    .filter(item => item.currentStock > 0) // 只显示有库存的
    .slice(0, 8)
    .map((item, index) => ({
      name: item.dishName,
      value: item.currentStock, // 使用真实当前库存量
      itemStyle: { color: colors[index % colors.length] }
    }))

  const option: echarts.EChartsOption = {
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { orient: 'vertical', left: 'left', top: 'center' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['60%', '50%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 4, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 14 } },
      data: chartData.length > 0 ? chartData : [{ name: '暂无数据', value: 0, itemStyle: { color: '#909399' } }]
    }]
  }
  stockChart.setOption(option)
}

/* 加载数据 */
const loadData = async () => {
  loading.value = true
  try {
    // 获取概览数据
    const overviewRes = await getDashboardOverview()
    const overview = overviewRes.data.data
    summaryData.warningCount = overview?.lowStockCount || 0

    // 获取库存周转统计数据
    const turnoverRes = await getInventoryTurnoverStatistics()
    const turnoverData = turnoverRes.data.data || []

    // 计算汇总数据
    summaryData.totalDishes = turnoverData.length
    summaryData.totalStock = turnoverData.reduce((sum: number, item: InventoryTurnoverStatistics) =>
      sum + (item.inQuantity || 0) - (item.outQuantity || 0), 0)
    summaryData.avgTurnover = turnoverData.length > 0
      ? Math.round(turnoverData.reduce((sum: number, item: InventoryTurnoverStatistics) =>
        sum + (item.turnoverRate || 0), 0) / turnoverData.length * 100) / 100
      : 0

    // 初始化图表
    initTurnoverChart(turnoverData.slice(0, 5)) // 只显示前5个
    initStockChart(turnoverData)
  } catch (error) {
    ElMessage.error('获取库存统计数据失败')
    console.error('获取库存统计数据失败:', error)
  } finally {
    loading.value = false
  }
}

const handleResize = () => {
  turnoverChart?.resize()
  stockChart?.resize()
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  turnoverChart?.dispose()
  stockChart?.dispose()
})
</script>

<template>
  <div class="statistics-inventory-container">
    <!-- 汇总卡片 -->
    <el-row :gutter="20" class="summary-cards">
      <el-col :xs="12" :sm="6">
        <el-card shadow="never" class="summary-card">
          <div class="summary-value">{{ summaryData.totalDishes }}</div>
          <div class="summary-label">商品种类</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="never" class="summary-card">
          <div class="summary-value">{{ summaryData.totalStock }}</div>
          <div class="summary-label">总库存量</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="never" class="summary-card">
          <div class="summary-value warning">{{ summaryData.warningCount }}</div>
          <div class="summary-label">预警数量</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="never" class="summary-card">
          <div class="summary-value">{{ summaryData.avgTurnover }}</div>
          <div class="summary-label">平均周转率</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :xs="24" :md="14">
        <el-card shadow="never">
          <template #header><span class="card-title">库存周转情况</span></template>
          <div ref="turnoverChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="10">
        <el-card shadow="never">
          <template #header><span class="card-title">库存分类分布</span></template>
          <div ref="stockChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped lang="scss">
.statistics-inventory-container {
  .summary-cards { margin-bottom: 20px; }
  .summary-card {
    text-align: center;
    .summary-value { font-size: 28px; font-weight: 600; color: #16A34A; &.warning { color: #F56C6C; } }
    .summary-label { font-size: 14px; color: #909399; margin-top: 8px; }
  }
  .card-title { font-size: 16px; font-weight: 600; }
  .chart-container { height: 350px; }
}
</style>
