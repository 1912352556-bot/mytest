<script setup lang="ts">
/**
 * 待调度订单页面
 */
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPendingScheduleOrders } from '@/api/order'
import { createScheduleTask, batchCreateScheduleTasks, autoAssignTasks } from '@/api/schedule'

const router = useRouter()

/* 加载状态 */
const loading = ref(false)

/* 自动分配进行中 */
const autoAssigning = ref(false)

/* 待调度订单列表 */
const pendingOrders = ref<Order[]>([])

/* 选中的订单 */
const selectedOrders = ref<Order[]>([])

/* 获取待调度订单 */
const fetchPendingOrders = async () => {
  loading.value = true
  try {
    const res = await getPendingScheduleOrders()
    pendingOrders.value = res.data.data || []
  } catch (error) {
    ElMessage.error('获取待调度订单失败')
    console.error('获取待调度订单失败:', error)
    pendingOrders.value = []
  } finally {
    loading.value = false
  }
}

/* 选择变化 */
const handleSelectionChange = (selection: Order[]) => {
  selectedOrders.value = selection
}

/* 单个调度 */
const handleSchedule = async (row: Order) => {
  try {
    await ElMessageBox.confirm(`确定要为订单 ${row.orderNo} 创建调度任务吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    await createScheduleTask(row.id)
    ElMessage.success('调度任务创建成功')
    fetchPendingOrders()
  } catch (e) {
    /* 取消操作 */
  }
}

/* 批量调度 */
const handleBatchSchedule = async () => {
  if (selectedOrders.value.length === 0) {
    ElMessage.warning('请选择要调度的订单')
    return
  }
  
  try {
    await ElMessageBox.confirm(`确定要为选中的 ${selectedOrders.value.length} 个订单创建调度任务吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    const orderIds = selectedOrders.value.map(o => o.id)
    const res = await batchCreateScheduleTasks(orderIds)
    const data = res.data.data as { success?: number; assigned?: number } | undefined
    ElMessage.success(
      `已创建 ${data?.success ?? orderIds.length} 个调度任务，自动分配 ${data?.assigned ?? 0} 单`
    )
    fetchPendingOrders()
  } catch (e) {
    /* 取消操作 */
  }
}

/* 一键自动分配：给所有待调度订单匹配配送员 */
const handleAutoAssign = async () => {
  try {
    await ElMessageBox.confirm(
      '将为所有「待调度」订单自动匹配配送员（优先任务数最少、未休息的人），确认继续吗？',
      '自动分配',
      { confirmButtonText: '开始分配', cancelButtonText: '取消', type: 'info' }
    )
  } catch (e) {
    return
  }
  autoAssigning.value = true
  try {
    const res = await autoAssignTasks()
    const data = res.data.data as { assigned?: number; failed?: number } | undefined
    ElMessage.success(
      `自动分配完成：成功 ${data?.assigned ?? 0} 单` +
        (data?.failed ? `，${data.failed} 单暂无可用配送员` : '')
    )
    fetchPendingOrders()
  } catch (error) {
    console.error('自动分配失败:', error)
  } finally {
    autoAssigning.value = false
  }
}

/* 查看订单详情 */
const handleViewOrder = (row: Order) => {
  router.push(`/order/detail/${row.id}`)
}

/* 判断是否超时 */
const isTimeout = (createTime: string) => {
  const create = new Date(createTime).getTime()
  const now = Date.now()
  return now - create > 2 * 60 * 60 * 1000 /* 2小时 */
}

onMounted(() => {
  fetchPendingOrders()
})
</script>

<template>
  <div class="pending-schedule-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>待调度订单</span>
          <div class="header-actions">
            <el-button type="success" :loading="autoAssigning" @click="handleAutoAssign">
              <el-icon><MagicStick /></el-icon>
              自动分配
            </el-button>
            <el-button type="primary" :disabled="selectedOrders.length === 0" @click="handleBatchSchedule">
              <el-icon><Operation /></el-icon>
              批量调度 ({{ selectedOrders.length }})
            </el-button>
            <el-button @click="fetchPendingOrders">
              <el-icon><Refresh /></el-icon>刷新
            </el-button>
          </div>
        </div>
      </template>

      <el-alert 
        v-if="pendingOrders.some(o => isTimeout(o.createTime || ''))"
        type="warning"
        :closable="false"
        show-icon
        style="margin-bottom: 16px"
      >
        <template #title>
          存在超时未调度订单（超过2小时），请尽快处理！
        </template>
      </el-alert>

      <el-table 
        :data="pendingOrders" 
        v-loading="loading" 
        stripe 
        border
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="orderNo" label="订单编号" width="160">
          <template #default="{ row }">
            <span>{{ row.orderNo }}</span>
            <el-tag v-if="isTimeout(row.createTime)" type="danger" size="small" class="ml8">超时</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="customerName" label="客户名称" width="100" />
        <el-table-column prop="deliveryArea" label="配送区域" width="100" />
        <el-table-column prop="deliveryAddress" label="配送地址" min-width="200" show-overflow-tooltip />
        <el-table-column prop="expectedDeliveryTime" label="期望配送时间" width="160" />
        <el-table-column prop="totalAmount" label="订单金额" width="100" align="right">
          <template #default="{ row }">
            <span class="amount">¥{{ row.totalAmount.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" width="160" />
        <el-table-column label="操作" width="150" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleSchedule(row)">调度</el-button>
            <el-button type="info" link @click="handleViewOrder(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="pendingOrders.length === 0 && !loading" description="暂无待调度订单" />
    </el-card>
  </div>
</template>

<style scoped lang="scss">
.pending-schedule-container {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    span { font-size: 16px; font-weight: 600; }
    .header-actions { display: flex; gap: 10px; }
  }
  .amount { color: #F56C6C; font-weight: 600; }
  .ml8 { margin-left: 8px; }
}
</style>
