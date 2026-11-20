<script setup lang="ts">
/**
 * 调度统计页面
 */
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { getScheduleStatistics } from '@/api/statistics'

const loading = ref(false)
const completionChartRef = ref<HTMLElement | null>(null)
const efficiencyChartRef = ref<HTMLElement | null>(null)
let completionChart: echarts.ECharts | null = null
let efficiencyChart: echarts.ECharts | null = null

/* 汇总数据 */
const summaryData = reactive({
  totalTasks: 0,
  completedTasks: 0,
  completionRate: 0,
  avgScheduleTime: 0
})

/* 初始化完成率图表 */
const initCompletionChart = (data: ScheduleStatistics[]) => {
  if (!completionChartRef.value) return
  completionChart = echarts.init(completionChartRef.value)

  const dates = data.map(d => d.date?.substring(5) || '')

  const option: echarts.EChartsOption = {
    tooltip: { trigger: 'axis' },
    legend: { data: ['总任务', '已完成', '完成率'], top: 10 },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: dates },
    yAxis: [
      { type: 'value', name: '任务数', position: 'left' },
      { type: 'value', name: '完成率(%)', position: 'right', max: 100 }
    ],
    series: [
      { name: '总任务', type: 'bar', data: data.map(d => d.totalTasks || 0), itemStyle: { color: '#16A34A' } },
      { name: '已完成', type: 'bar', data: data.map(d => d.completedTasks || 0), itemStyle: { color: '#14B8A6' } },
      { name: '完成率', type: 'line', yAxisIndex: 1, data: data.map(d => d.completionRate || 0), itemStyle: { color: '#E6A23C' }, smooth: true }
    ]
  }
  completionChart.setOption(option)
}

/* 初始化效率图表 */
const initEfficiencyChart = () => {
  if (!efficiencyChartRef.value) return
  efficiencyChart = echarts.init(efficiencyChartRef.value)
  
  const option: echarts.EChartsOption = {
    tooltip: { trigger: 'item' },
    series: [{
      type: 'gauge',
      startAngle: 180,
      endAngle: 0,
      min: 0,
      max: 100,
      splitNumber: 10,
      progress: { show: true, width: 20 },
      pointer: { show: false },
      axisLine: { lineStyle: { width: 20 } },
      axisTick: { show: false },
      splitLine: { show: false },
      axisLabel: { show: false },
      title: { offsetCenter: [0, '30%'], fontSize: 16 },
      detail: { valueAnimation: true, fontSize: 40, offsetCenter: [0, '-10%'], formatter: '{value}%', color: '#16A34A' },
      data: [{ value: summaryData.completionRate, name: '调度完成率' }]
    }]
  }
  efficiencyChart.setOption(option)
}

/* 加载数据 */
const loadData = async () => {
  loading.value = true
  try {
    const endDate = new Date().toISOString().split('T')[0]
    const startDate = new Date(Date.now() - 6 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]

    const res = await getScheduleStatistics({ startDate, endDate })
    const data = res.data.data || []

    // 计算汇总数据
    summaryData.totalTasks = data.reduce((sum: number, item: ScheduleStatistics) => sum + (item.totalTasks || 0), 0)
    summaryData.completedTasks = data.reduce((sum: number, item: ScheduleStatistics) => sum + (item.completedTasks || 0), 0)
    summaryData.completionRate = summaryData.totalTasks > 0
      ? Math.round(summaryData.completedTasks / summaryData.totalTasks * 1000) / 10
      : 0
    summaryData.avgScheduleTime = data.length > 0
      ? Math.round(data.reduce((sum: number, item: ScheduleStatistics) => sum + (item.completionRate || 0), 0) / data.length)
      : 0

    // 初始化图表
    initCompletionChart(data)
    initEfficiencyChart()
  } catch (error) {
    ElMessage.error('获取调度统计数据失败')
    console.error('获取调度统计数据失败:', error)
  } finally {
    loading.value = false
  }
}

const handleResize = () => {
  completionChart?.resize()
  efficiencyChart?.resize()
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  completionChart?.dispose()
  efficiencyChart?.dispose()
})
</script>

<template>
  <div class="statistics-schedule-container">
    <!-- 汇总卡片 -->
    <el-row :gutter="20" class="summary-cards">
      <el-col :xs="12" :sm="6">
        <el-card shadow="never" class="summary-card">
          <div class="summary-value">{{ summaryData.totalTasks }}</div>
          <div class="summary-label">总调度任务</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="never" class="summary-card">
          <div class="summary-value">{{ summaryData.completedTasks }}</div>
          <div class="summary-label">已完成任务</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="never" class="summary-card">
          <div class="summary-value success">{{ summaryData.completionRate }}%</div>
          <div class="summary-label">完成率</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="never" class="summary-card">
          <div class="summary-value">{{ summaryData.avgScheduleTime }}分钟</div>
          <div class="summary-label">平均调度时间</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :xs="24" :md="14">
        <el-card shadow="never">
          <template #header><span class="card-title">调度任务完成情况（近7天）</span></template>
          <div ref="completionChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="10">
        <el-card shadow="never">
          <template #header><span class="card-title">调度效率仪表盘</span></template>
          <div ref="efficiencyChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped lang="scss">
.statistics-schedule-container {
  .summary-cards { margin-bottom: 20px; }
  .summary-card {
    text-align: center;
    .summary-value { font-size: 28px; font-weight: 600; color: #16A34A; &.success { color: #67C23A; } }
    .summary-label { font-size: 14px; color: #909399; margin-top: 8px; }
  }
  .card-title { font-size: 16px; font-weight: 600; }
  .chart-container { height: 350px; }
}
</style>
