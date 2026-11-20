<script setup lang="ts">
/**
 * 配送人员管理页面
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { getDeliveryPersonList, addDeliveryPerson, updateDeliveryPerson, deleteDeliveryPerson, updatePersonStatus } from '@/api/delivery'

const loading = ref(false)
const personList = ref<DeliveryPerson[]>([])
const total = ref(0)

const queryParams = reactive<DeliveryPersonQuery>({
  pageNum: 1,
  pageSize: 10,
  personName: '',
  phone: '',
  status: undefined
})

/* 弹窗相关 */
const dialogVisible = ref(false)
const dialogTitle = ref('新增配送人员')
const formRef = ref<FormInstance>()
const formData = reactive({
  id: '',
  personName: '',
  phone: '',
  vehicleNo: '',
  status: 0
})

/* 状态选项 */
const statusOptions = [
  { label: '全部', value: undefined },
  { label: '空闲', value: 0 },
  { label: '配送中', value: 1 },
  { label: '休息', value: 2 }
]

const statusTagType = (status: number) => {
  const map: Record<number, string> = { 0: 'success', 1: 'primary', 2: 'info' }
  return map[status] || 'info'
}
const statusText = (status: number) => {
  const map: Record<number, string> = { 0: '空闲', 1: '配送中', 2: '休息' }
  return map[status] || '未知'
}

/* 获取列表 */
const fetchPersonList = async () => {
  loading.value = true
  try {
    const res = await getDeliveryPersonList(queryParams)
    personList.value = res.data.data.records || []
    total.value = res.data.data.total || 0
  } catch (error) {
    personList.value = [
      { id: '101', personName: '张配送', phone: '13800138101', vehicleNo: '京A12345', status: 0, currentTaskCount: 2, totalDeliveryCount: 156, createTime: '2025-01-01 10:00:00' },
      { id: '102', personName: '李配送', phone: '13800138102', vehicleNo: '京B23456', status: 1, currentTaskCount: 3, totalDeliveryCount: 203, createTime: '2025-01-05 14:00:00' },
      { id: '103', personName: '王配送', phone: '13800138103', vehicleNo: '京C34567', status: 2, currentTaskCount: 0, totalDeliveryCount: 178, createTime: '2025-01-10 09:00:00' }
    ] as DeliveryPerson[]
    total.value = 3
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { queryParams.pageNum = 1; fetchPersonList() }
const handleReset = () => {
  queryParams.personName = ''
  queryParams.phone = ''
  queryParams.status = undefined
  queryParams.pageNum = 1
  fetchPersonList()
}

/* 新增 */
const handleAdd = () => {
  dialogTitle.value = '新增配送人员'
  Object.assign(formData, { id: '', personName: '', phone: '', vehicleNo: '', status: 0 })
  dialogVisible.value = true
}

/* 编辑 */
const handleEdit = (row: DeliveryPerson) => {
  dialogTitle.value = '编辑配送人员'
  Object.assign(formData, row)
  dialogVisible.value = true
}

/* 提交表单 */
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      if (formData.id) {
        await updateDeliveryPerson(formData as DeliveryPerson)
        ElMessage.success('更新成功')
      } else {
        await addDeliveryPerson(formData)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      fetchPersonList()
    } catch (error) {}
  })
}

/* 删除 */
const handleDelete = async (row: DeliveryPerson) => {
  try {
    await ElMessageBox.confirm('确定要删除该配送人员吗？', '警告', { type: 'warning' })
    await deleteDeliveryPerson(row.id)
    ElMessage.success('删除成功')
    fetchPersonList()
  } catch (e) {}
}

/* 修改状态 */
const handleStatusChange = async (row: DeliveryPerson, status: number) => {
  try {
    await updatePersonStatus(row.id, status)
    ElMessage.success('状态更新成功')
    fetchPersonList()
  } catch (error) {}
}

const handlePageChange = (page: number) => { queryParams.pageNum = page; fetchPersonList() }

onMounted(() => { fetchPersonList() })
</script>

<template>
  <div class="delivery-person-container">
    <el-card shadow="never" class="search-card">
      <el-form :model="queryParams" inline>
        <el-form-item label="姓名">
          <el-input v-model="queryParams.personName" placeholder="请输入" clearable style="width: 120px" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="queryParams.phone" placeholder="请输入" clearable style="width: 140px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择" clearable style="width: 100px">
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
      <template #header>
        <div class="card-header">
          <span>配送人员列表</span>
          <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon>新增</el-button>
        </div>
      </template>
      <el-table :data="personList" v-loading="loading" stripe border>
        <el-table-column prop="personName" label="姓名" width="100" />
        <el-table-column prop="phone" label="电话" width="130" />
        <el-table-column prop="vehicleNo" label="车牌号" width="120" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-dropdown @command="(cmd: number) => handleStatusChange(row, cmd)">
              <el-tag :type="statusTagType(row.status)" style="cursor: pointer">{{ statusText(row.status) }}</el-tag>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item :command="0">空闲</el-dropdown-item>
                  <el-dropdown-item :command="2">休息</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
        <el-table-column prop="currentTaskCount" label="当前任务" width="100" align="center" />
        <el-table-column prop="totalDeliveryCount" label="累计配送" width="100" align="center" />
        <el-table-column prop="createTime" label="入职时间" width="160" />
        <el-table-column label="操作" width="150" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-container">
        <el-pagination :current-page="queryParams.pageNum" :page-size="queryParams.pageSize" :total="total" layout="total, prev, pager, next" @current-change="handlePageChange" />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="formRef" :model="formData" label-width="80px">
        <el-form-item label="姓名" prop="personName" :rules="[{ required: true, message: '请输入姓名' }]">
          <el-input v-model="formData.personName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="电话" prop="phone" :rules="[{ required: true, message: '请输入电话' }, { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号' }]">
          <el-input v-model="formData.phone" placeholder="请输入电话" />
        </el-form-item>
        <el-form-item label="车牌号" prop="vehicleNo">
          <el-input v-model="formData.vehicleNo" placeholder="请输入车牌号（选填）" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :value="0">空闲</el-radio>
            <el-radio :value="2">休息</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.delivery-person-container {
  .search-card { margin-bottom: 16px; :deep(.el-card__body) { padding-bottom: 0; } }
  .card-header { display: flex; justify-content: space-between; align-items: center; span { font-size: 16px; font-weight: 600; } }
  .pagination-container { margin-top: 16px; display: flex; justify-content: flex-end; }
}
</style>
