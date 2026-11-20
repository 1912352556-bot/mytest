<script setup lang="ts">
/**
 * 调度任务列表页面
 */
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getScheduleTaskList, cancelScheduleTask, getScheduleLogs } from '@/api/schedule'

const router = useRouter()

/* 加载状态 */
const loading = ref(false)

/* 任务列表数据 */
const taskList = ref<ScheduleTask[]>([])
const total = ref(0)

/* 查询参数 */
const queryParams = reactive<ScheduleTaskQuery>({
  pageNum: 1,
  pageSize: 10,
  taskNo: '',
  orderNo: '',
  deliveryArea: '',
  taskStatus: undefined,
  assigneeId: ''
})

/* 调度日志弹窗 */
const logDialogVisible = ref(false)
const currentLogs = ref<ScheduleLog[]>([])

/* 任务状态选项 */
const taskStatusOptions = [
  { label: '全部', value: undefined },
  { label: '待分配', value: 0 },
  { label: '已分配', value: 1 },
  { label: '执行中', value: 2 },
  { label: '已完成', value: 3 }
]

/* 状态标签类型 */
const statusTagType = (status: number) => {
  const typeMap: Record<number, string> = {
    0: 'warning',
    1: 'primary',
    2: 'info',
    3: 'success'
  }
  return typeMap[status] || 'info'
}

/* 状态文本 */
const statusText = (status: number) => {
  const textMap: Record<number, string> = {
    0: '待分配',
    1: '已分配',
    2: '执行中',
    3: '已完成'
  }
  return textMap[status] || '未知'
}

/* 获取任务列表 */
const fetchTaskList = async () => {
  loading.value = true
  try {
    const res = await getScheduleTaskList(queryParams)
    taskList.value = res.data.data.records || []
    total.value = res.data.data.total || 0
  } catch (error) {
    /* 模拟数据 */
    taskList.value = [
      {
        id: '1',
        taskNo: 'SCH20250121001',
        orderId: '1',
        orderNo: 'ORD20250121001',
        deliveryArea: '朝阳区',
        expectedDeliveryTime: '2025-01-21 12:00:00',
        assigneeId: '101',
        assigneeName: '张配送',
        taskStatus: 1,
        priority: 1,
        createTime: '2025-01-21 10:00:00'
      },
      {
        id: '2',
        taskNo: 'SCH20250121002',
        orderId: '2',
        orderNo: 'ORD20250121002',
        deliveryArea: '海淀区',
        expectedDeliveryTime: '2025-01-21 13:00:00',
        assigneeId: '',
        assigneeName: '',
        taskStatus: 0,
        priority: 2,
        createTime: '2025-01-21 10:15:00'
      },
      {
        id: '3',
        taskNo: 'SCH20250121003',
        orderId: '3',
        orderNo: 'ORD20250121003',
        deliveryArea: '西城区',
        expectedDeliveryTime: '2025-01-21 11:30:00',
        assigneeId: '102',
        assigneeName: '李配送',
        taskStatus: 2,
        priority: 1,
        createTime: '2025-01-21 08:45:00'
      }
    ] as ScheduleTask[]
    total.value = 3
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
  queryParams.taskNo = ''
  queryParams.orderNo = ''
  queryParams.deliveryArea = ''
  queryParams.taskStatus = undefined
  queryParams.pageNum = 1
  fetchTaskList()
}

/* 去分配 */
const handleAssign = (row: ScheduleTask) => {
  router.push('/schedule/assign')
}

/* 查看日志 */
const handleViewLogs = async (row: ScheduleTask) => {
  try {
    const res = await getScheduleLogs(row.id)
    currentLogs.value = res.data.data || []
  } catch (error) {
    /* 模拟数据 */
    currentLogs.value = [
      { id: '1', taskId: row.id, taskNo: row.taskNo, operationType: '创建任务', operationContent: '系统自动创建调度任务', operatorId: '1', operatorName: '系统', createTime: '2025-01-21 10:00:00' },
      { id: '2', taskId: row.id, taskNo: row.taskNo, operationType: '分配任务', operationContent: '分配给配送员张配送', operatorId: '1', operatorName: '管理员', createTime: '2025-01-21 10:05:00' }
    ] as ScheduleLog[]
  }
  logDialogVisible.value = true
}

/* 取消任务 */
const handleCancel = async (row: ScheduleTask) => {
  try {
    await ElMessageBox.confirm('确定要取消该调度任务吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await cancelScheduleTask(row.id)
    ElMessage.success('任务已取消')
    fetchTaskList()
  } catch (e) {
    /* 取消操作 */
  }
}

/* 分页变化 */
const handlePageChange = (page: number) => {
  queryParams.pageNum = page
  fetchTaskList()
}

/* 每页数量变化 */
const handleSizeChange = (size: number) => {
  queryParams.pageSize = size
  queryParams.pageNum = 1
  fetchTaskList()
}

onMounted(() => {
  fetchTaskList()
})
</script>

<template>
  <div class="schedule-list-container">
    <!-- 搜索表单 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="queryParams" inline>
        <el-form-item label="任务编号">
          <el-input v-model="queryParams.taskNo" placeholder="请输入任务编号" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="订单编号">
          <el-input v-model="queryParams.orderNo" placeholder="请输入订单编号" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="配送区域">
          <el-input v-model="queryParams.deliveryArea" placeholder="请输入配送区域" clearable style="width: 120px" />
        </el-form-item>
        <el-form-item label="任务状态">
          <el-select v-model="queryParams.taskStatus" placeholder="请选择" clearable style="width: 120px">
            <el-option v-for="item in taskStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span>调度任务列表</span>
          <el-button type="primary" @click="router.push('/schedule/pending')">
            <el-icon><Clock /></el-icon>待调度订单
          </el-button>
        </div>
      </template>

      <el-table :data="taskList" v-loading="loading" stripe border>
        <el-table-column prop="taskNo" label="任务编号" width="160" />
        <el-table-column prop="orderNo" label="订单编号" width="160" />
        <el-table-column prop="deliveryArea" label="配送区域" width="100" />
        <el-table-column prop="expectedDeliveryTime" label="期望配送时间" width="160" />
        <el-table-column prop="assigneeName" label="配送人员" width="100">
          <template #default="{ row }">
            <span v-if="row.assigneeName">{{ row.assigneeName }}</span>
            <el-tag v-else type="warning" size="small">未分配</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.priority === 1 ? 'danger' : 'info'" size="small">
              {{ row.priority === 1 ? '高' : '普通' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="taskStatus" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.taskStatus)" size="small">
              {{ statusText(row.taskStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <el-button v-if="row.taskStatus === 0" type="primary" link @click="handleAssign(row)">分配</el-button>
            <el-button type="info" link @click="handleViewLogs(row)">日志</el-button>
            <el-button v-if="row.taskStatus < 2" type="danger" link @click="handleCancel(row)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <!-- 调度日志弹窗 -->
    <el-dialog v-model="logDialogVisible" title="调度日志" width="600px">
      <el-timeline>
        <el-timeline-item
          v-for="log in currentLogs"
          :key="log.id"
          :timestamp="log.createTime"
          placement="top"
        >
          <el-card shadow="never">
            <h4>{{ log.operationType }}</h4>
            <p>{{ log.operationContent }}</p>
            <p class="log-operator">操作人：{{ log.operatorName }}</p>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.schedule-list-container {
  .search-card {
    margin-bottom: 16px;
    :deep(.el-card__body) { padding-bottom: 0; }
  }
  
  .table-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      span { font-size: 16px; font-weight: 600; }
    }
    .pagination-container { margin-top: 16px; display: flex; justify-content: flex-end; }
  }
  
  .log-operator { color: #909399; font-size: 12px; margin-top: 8px; }
}
</style>
