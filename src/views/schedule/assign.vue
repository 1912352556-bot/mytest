<script setup lang="ts">
/**
 * 任务分配页面
 */
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getPendingAssignTasks, assignTask, getAvailableDeliveryPersons } from '@/api/schedule'

/* 加载状态 */
const loading = ref(false)

/* 待分配任务列表 */
const pendingTasks = ref<ScheduleTask[]>([])

/* 可用配送人员列表 */
const deliveryPersons = ref<DeliveryPerson[]>([])

/* 分配弹窗 */
const assignDialogVisible = ref(false)
const assignForm = ref({
  taskId: '',
  assigneeId: '',
  priority: 2,
  remark: ''
})
const currentTask = ref<ScheduleTask | null>(null)

/* 获取待分配任务 */
const fetchPendingTasks = async () => {
  loading.value = true
  try {
    const res = await getPendingAssignTasks()
    pendingTasks.value = res.data.data || []
  } catch (error) {
    /* 模拟数据 */
    pendingTasks.value = [
      {
        id: '1', taskNo: 'SCH20250121001', orderId: '1', orderNo: 'ORD20250121001',
        deliveryArea: '朝阳区', expectedDeliveryTime: '2025-01-21 12:00:00',
        taskStatus: 0, priority: 1, createTime: '2025-01-21 10:00:00'
      },
      {
        id: '2', taskNo: 'SCH20250121002', orderId: '2', orderNo: 'ORD20250121002',
        deliveryArea: '海淀区', expectedDeliveryTime: '2025-01-21 13:00:00',
        taskStatus: 0, priority: 2, createTime: '2025-01-21 10:15:00'
      }
    ] as ScheduleTask[]
  } finally {
    loading.value = false
  }
}

/* 获取可用配送人员 */
const fetchDeliveryPersons = async () => {
  try {
    const res = await getAvailableDeliveryPersons()
    deliveryPersons.value = res.data.data || []
  } catch (error) {
    /* 模拟数据 */
    deliveryPersons.value = [
      { id: '101', personName: '张配送', phone: '13800138101', status: 0, currentTaskCount: 2, totalDeliveryCount: 156 },
      { id: '102', personName: '李配送', phone: '13800138102', status: 0, currentTaskCount: 1, totalDeliveryCount: 203 },
      { id: '103', personName: '王配送', phone: '13800138103', status: 0, currentTaskCount: 3, totalDeliveryCount: 178 }
    ] as DeliveryPerson[]
  }
}

/* 打开分配弹窗 */
const handleOpenAssign = (task: ScheduleTask) => {
  currentTask.value = task
  assignForm.value = {
    taskId: task.id,
    assigneeId: '',
    priority: task.priority || 2,
    remark: ''
  }
  assignDialogVisible.value = true
}

/* 确认分配 */
const handleConfirmAssign = async () => {
  if (!assignForm.value.assigneeId) {
    ElMessage.warning('请选择配送人员')
    return
  }
  
  try {
    await assignTask(assignForm.value as AssignTaskRequest)
    ElMessage.success('分配成功')
    assignDialogVisible.value = false
    fetchPendingTasks()
  } catch (error) {
    console.error('分配失败:', error)
  }
}

/* 人员状态文本 */
const personStatusText = (status: number) => {
  const map: Record<number, string> = { 0: '空闲', 1: '配送中', 2: '休息' }
  return map[status] || '未知'
}

onMounted(() => {
  fetchPendingTasks()
  fetchDeliveryPersons()
})
</script>

<template>
  <div class="assign-container">
    <el-row :gutter="20">
      <!-- 待分配任务 -->
      <el-col :span="16">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>待分配任务</span>
              <el-button @click="fetchPendingTasks"><el-icon><Refresh /></el-icon>刷新</el-button>
            </div>
          </template>
          
          <el-table :data="pendingTasks" v-loading="loading" stripe border>
            <el-table-column prop="taskNo" label="任务编号" width="150" />
            <el-table-column prop="orderNo" label="订单编号" width="150" />
            <el-table-column prop="deliveryArea" label="配送区域" width="100" />
            <el-table-column prop="expectedDeliveryTime" label="期望配送时间" width="160" />
            <el-table-column prop="priority" label="优先级" width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="row.priority === 1 ? 'danger' : 'info'" size="small">
                  {{ row.priority === 1 ? '高' : '普通' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80" align="center">
              <template #default="{ row }">
                <el-button type="primary" link @click="handleOpenAssign(row)">分配</el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <el-empty v-if="pendingTasks.length === 0 && !loading" description="暂无待分配任务" />
        </el-card>
      </el-col>
      
      <!-- 配送人员 -->
      <el-col :span="6">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>配送人员列表</span>
              <el-button @click="fetchDeliveryPersons"><el-icon><Refresh /></el-icon>刷新</el-button>
            </div>
          </template>

          <el-table :data="deliveryPersons" stripe border size="small">
            <el-table-column prop="personName" label="姓名" width="70" />
            <el-table-column prop="phone" label="电话" width="110" />
            <el-table-column prop="status" label="状态" width="70" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 0 ? 'success' : 'info'" size="small">
                  {{ personStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="currentTaskCount" label="任务" width="60" align="center" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 分配弹窗 -->
    <el-dialog v-model="assignDialogVisible" title="分配任务" width="500px">
      <el-form :model="assignForm" label-width="100px">
        <el-form-item label="任务编号">
          <span>{{ currentTask?.taskNo }}</span>
        </el-form-item>
        <el-form-item label="订单编号">
          <span>{{ currentTask?.orderNo }}</span>
        </el-form-item>
        <el-form-item label="配送区域">
          <span>{{ currentTask?.deliveryArea }}</span>
        </el-form-item>
        <el-form-item label="配送人员" required>
          <el-select v-model="assignForm.assigneeId" placeholder="请选择配送人员" style="width: 100%">
            <el-option 
              v-for="person in deliveryPersons.filter(p => p.status === 0)" 
              :key="person.id" 
              :label="`${person.personName} (当前${person.currentTaskCount}个任务)`"
              :value="person.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-radio-group v-model="assignForm.priority">
            <el-radio :value="1">高优先级</el-radio>
            <el-radio :value="2">普通</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="assignForm.remark" type="textarea" :rows="2" placeholder="请输入备注（选填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmAssign">确认分配</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.assign-container {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    span { font-size: 16px; font-weight: 600; }
  }
}
</style>
