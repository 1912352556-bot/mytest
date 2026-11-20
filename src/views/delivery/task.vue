<script setup lang="ts">
/**
 * 配送任务页面
 */
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDeliveryTaskList, startDelivery, completeDelivery, reportException } from '@/api/delivery'

const router = useRouter()
const loading = ref(false)
const taskList = ref<DeliveryTask[]>([])
const total = ref(0)

const queryParams = reactive<DeliveryTaskQuery>({
  pageNum: 1,
  pageSize: 10,
  deliveryNo: '',
  orderNo: '',
  deliveryPersonId: '',
  deliveryStatus: undefined
})

/* 状态选项 */
const statusOptions = [
  { label: '全部', value: undefined },
  { label: '待取货', value: 0 },
  { label: '配送中', value: 1 },
  { label: '已送达', value: 2 },
  { label: '异常', value: 3 }
]

/* 状态标签 */
const statusTagType = (status: number) => {
  const map: Record<number, string> = { 0: 'warning', 1: 'primary', 2: 'success', 3: 'danger' }
  return map[status] || 'info'
}
const statusText = (status: number) => {
  const map: Record<number, string> = { 0: '待取货', 1: '配送中', 2: '已送达', 3: '异常' }
  return map[status] || '未知'
}

/* 获取配送任务列表 */
const fetchTaskList = async () => {
  loading.value = true
  try {
    const res = await getDeliveryTaskList(queryParams)
    taskList.value = res.data.data.records || []
    total.value = res.data.data.total || 0
  } catch (error) {
    taskList.value = [
      { id: '1', deliveryNo: 'DLV20250121001', scheduleTaskId: '1', orderId: '1', orderNo: 'ORD20250121001', deliveryPersonId: '101', deliveryPersonName: '张配送', customerName: '张三', customerPhone: '13800138001', deliveryAddress: '北京市朝阳区建国路88号', deliveryStatus: 1, pickupTime: '2025-01-21 11:00:00', createTime: '2025-01-21 10:30:00' },
      { id: '2', deliveryNo: 'DLV20250121002', scheduleTaskId: '2', orderId: '2', orderNo: 'ORD20250121002', deliveryPersonId: '102', deliveryPersonName: '李配送', customerName: '李四', customerPhone: '13800138002', deliveryAddress: '北京市海淀区中关村大街1号', deliveryStatus: 0, createTime: '2025-01-21 10:45:00' }
    ] as DeliveryTask[]
    total.value = 2
  } finally {
    loading.value = false
  }
}

/* 搜索 */
const handleSearch = () => {
  queryParams.pageNum = 1
  fetchTaskList()
}

/* 重置 */
const handleReset = () => {
  queryParams.deliveryNo = ''
  queryParams.orderNo = ''
  queryParams.deliveryStatus = undefined
  queryParams.pageNum = 1
  fetchTaskList()
}

/* 开始配送 */
const handleStart = async (row: DeliveryTask) => {
  try {
    await ElMessageBox.confirm('确定开始配送吗？', '提示', { type: 'info' })
    await startDelivery(row.id)
    ElMessage.success('已开始配送')
    fetchTaskList()
  } catch (e) {}
}

/* 完成配送 */
const handleComplete = async (row: DeliveryTask) => {
  try {
    await ElMessageBox.confirm('确定已送达吗？', '提示', { type: 'info' })
    await completeDelivery({ deliveryTaskId: row.id })
    ElMessage.success('配送完成')
    fetchTaskList()
  } catch (e) {}
}

/* 异常上报 */
const handleException = async (row: DeliveryTask) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入异常原因', '异常上报', { inputType: 'textarea' })
    if (value) {
      await reportException(row.id, value)
      ElMessage.success('异常已上报')
      fetchTaskList()
    }
  } catch (e) {}
}

/* 查看轨迹 */
const handleViewTrack = (row: DeliveryTask) => {
  router.push(`/delivery/track/${row.id}`)
}

const handlePageChange = (page: number) => {
  queryParams.pageNum = page
  fetchTaskList()
}

onMounted(() => {
  fetchTaskList()
})
</script>

<template>
  <div class="delivery-task-container">
    <el-card shadow="never" class="search-card">
      <el-form :model="queryParams" inline>
        <el-form-item label="配送编号">
          <el-input v-model="queryParams.deliveryNo" placeholder="请输入" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="订单编号">
          <el-input v-model="queryParams.orderNo" placeholder="请输入" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="配送状态">
          <el-select v-model="queryParams.deliveryStatus" placeholder="请选择" clearable style="width: 120px">
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch"><el-icon><Search /></el-icon>搜索</el-button>
          <el-button @click="handleReset"><el-icon><Refresh /></el-icon>重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <template #header><span class="card-title">配送任务列表</span></template>
      <el-table :data="taskList" v-loading="loading" stripe border>
        <el-table-column prop="deliveryNo" label="配送编号" width="150" />
        <el-table-column prop="orderNo" label="订单编号" width="150" />
        <el-table-column prop="deliveryPersonName" label="配送员" width="100" />
        <el-table-column prop="customerName" label="客户" width="100" />
        <el-table-column prop="deliveryAddress" label="配送地址" min-width="200" show-overflow-tooltip />
        <el-table-column prop="deliveryStatus" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.deliveryStatus)" size="small">{{ statusText(row.deliveryStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="pickupTime" label="取货时间" width="160" />
        <el-table-column prop="deliveryTime" label="送达时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button v-if="row.deliveryStatus === 0" type="primary" link @click="handleStart(row)">开始配送</el-button>
            <el-button v-if="row.deliveryStatus === 1" type="success" link @click="handleComplete(row)">确认送达</el-button>
            <el-button v-if="row.deliveryStatus === 1" type="warning" link @click="handleException(row)">异常</el-button>
            <el-button v-if="row.deliveryStatus >= 1" type="info" link @click="handleViewTrack(row)">轨迹</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-container">
        <el-pagination :current-page="queryParams.pageNum" :page-size="queryParams.pageSize" :total="total" layout="total, prev, pager, next" @current-change="handlePageChange" />
      </div>
    </el-card>
  </div>
</template>

<style scoped lang="scss">
.delivery-task-container {
  .search-card { margin-bottom: 16px; :deep(.el-card__body) { padding-bottom: 0; } }
  .card-title { font-size: 16px; font-weight: 600; }
  .pagination-container { margin-top: 16px; display: flex; justify-content: flex-end; }
}
</style>
