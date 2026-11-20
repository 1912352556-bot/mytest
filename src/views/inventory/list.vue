<script setup lang="ts">
/**
 * 库存列表页面
 */
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getInventoryList, stockIn, stockOut, setWarningThreshold } from '@/api/inventory'

const router = useRouter()
const loading = ref(false)
const inventoryList = ref<Inventory[]>([])
const total = ref(0)

const queryParams = reactive<InventoryQuery>({
  pageNum: 1,
  pageSize: 10,
  dishName: '',
  isWarning: undefined
})

/* 出入库弹窗 */
const stockDialogVisible = ref(false)
const stockDialogTitle = ref('入库')
const stockForm = reactive({
  inventoryId: '',
  dishName: '',
  changeType: 1,
  quantity: 1,
  remark: ''
})

/* 获取库存列表 */
const fetchInventoryList = async () => {
  loading.value = true
  try {
    const res = await getInventoryList(queryParams)
    inventoryList.value = res.data.data.records || []
    total.value = res.data.data.total || 0
  } catch (error) {
    inventoryList.value = [
      { id: '1', dishId: '1', dishName: '宫保鸡丁', dishCode: 'GBGD', currentStock: 100, warningThreshold: 20, unit: '份', lastInTime: '2025-01-20 10:00:00', lastOutTime: '2025-01-21 09:30:00', status: 1 },
      { id: '2', dishId: '2', dishName: '鱼香肉丝', dishCode: 'YXRS', currentStock: 15, warningThreshold: 20, unit: '份', lastInTime: '2025-01-19 14:00:00', lastOutTime: '2025-01-21 10:00:00', status: 1 },
      { id: '3', dishId: '3', dishName: '红烧排骨', dishCode: 'HSPG', currentStock: 50, warningThreshold: 15, unit: '份', lastInTime: '2025-01-21 08:00:00', lastOutTime: '2025-01-21 11:00:00', status: 1 },
      { id: '4', dishId: '4', dishName: '清炒时蔬', dishCode: 'QTSS', currentStock: 8, warningThreshold: 10, unit: '份', lastInTime: '2025-01-18 16:00:00', lastOutTime: '2025-01-21 09:00:00', status: 1 }
    ] as Inventory[]
    total.value = 4
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { queryParams.pageNum = 1; fetchInventoryList() }
const handleReset = () => {
  queryParams.dishName = ''
  queryParams.isWarning = undefined
  queryParams.pageNum = 1
  fetchInventoryList()
}

/* 是否低于预警阈值 */
const isLowStock = (row: Inventory) => row.currentStock <= row.warningThreshold

/* 入库 */
const handleStockIn = (row: Inventory) => {
  stockDialogTitle.value = '入库'
  stockForm.inventoryId = row.id
  stockForm.dishName = row.dishName || ''
  stockForm.changeType = 1
  stockForm.quantity = 1
  stockForm.remark = ''
  stockDialogVisible.value = true
}

/* 出库 */
const handleStockOut = (row: Inventory) => {
  stockDialogTitle.value = '出库'
  stockForm.inventoryId = row.id
  stockForm.dishName = row.dishName || ''
  stockForm.changeType = 2
  stockForm.quantity = 1
  stockForm.remark = ''
  stockDialogVisible.value = true
}

/* 确认出入库 */
const handleConfirmStock = async () => {
  if (stockForm.quantity <= 0) {
    ElMessage.warning('数量必须大于0')
    return
  }
  try {
    if (stockForm.changeType === 1) {
      await stockIn({
        inventoryId: stockForm.inventoryId,
        quantity: stockForm.quantity,
        remark: stockForm.remark
      })
      ElMessage.success('入库成功')
    } else {
      await stockOut({
        inventoryId: stockForm.inventoryId,
        quantity: stockForm.quantity,
        remark: stockForm.remark
      })
      ElMessage.success('出库成功')
    }
    stockDialogVisible.value = false
    fetchInventoryList()
  } catch (error) {}
}

/* 设置预警阈值 */
const handleSetThreshold = async (row: Inventory) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入预警阈值', '设置预警阈值', {
      inputValue: String(row.warningThreshold),
      inputPattern: /^[1-9]\d*$/,
      inputErrorMessage: '请输入正整数'
    })
    await setWarningThreshold(row.id, Number(value))
    ElMessage.success('设置成功')
    fetchInventoryList()
  } catch (e) {}
}

/* 查看记录 */
const handleViewRecord = (row: Inventory) => {
  router.push(`/inventory/record/${row.id}`)
}

const handlePageChange = (page: number) => { queryParams.pageNum = page; fetchInventoryList() }

/* 自动刷新定时器 */
let refreshTimer: ReturnType<typeof setInterval> | null = null

/* 启动自动刷新 */
const startAutoRefresh = () => {
  refreshTimer = setInterval(() => {
    fetchInventoryList()
  }, 30000) /* 每30秒刷新一次 */
}

/* 停止自动刷新 */
const stopAutoRefresh = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

onMounted(() => {
  fetchInventoryList()
  startAutoRefresh()
})

onUnmounted(() => {
  stopAutoRefresh()
})
</script>

<template>
  <div class="inventory-list-container">
    <el-card shadow="never" class="search-card">
      <el-form :model="queryParams" inline>
        <el-form-item label="商品名称">
          <el-input v-model="queryParams.dishName" placeholder="请输入" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="库存状态">
          <el-select v-model="queryParams.isWarning" placeholder="请选择" clearable style="width: 120px">
            <el-option label="全部" :value="undefined" />
            <el-option label="库存预警" :value="true" />
            <el-option label="库存正常" :value="false" />
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
          <span>库存列表</span>
          <el-button type="warning" @click="router.push('/inventory/warning')">
            <el-icon><Warning /></el-icon>库存预警
          </el-button>
        </div>
      </template>
      <el-table :data="inventoryList" v-loading="loading" stripe border>
        <el-table-column prop="dishCode" label="商品编码" width="120" />
        <el-table-column prop="dishName" label="商品名称" width="150" />
        <el-table-column prop="currentStock" label="当前库存" width="120" align="center">
          <template #default="{ row }">
            <span :class="{ 'low-stock': isLowStock(row) }">{{ row.currentStock }} {{ row.unit }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="warningThreshold" label="预警阈值" width="120" align="center">
          <template #default="{ row }">{{ row.warningThreshold }} {{ row.unit }}</template>
        </el-table-column>
        <el-table-column label="库存状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="isLowStock(row)" type="danger" size="small">低库存</el-tag>
            <el-tag v-else type="success" size="small">正常</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastInTime" label="最近入库时间" width="160" />
        <el-table-column prop="lastOutTime" label="最近出库时间" width="160" />
        <el-table-column label="操作" width="250" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="success" link @click="handleStockIn(row)">入库</el-button>
            <el-button type="warning" link @click="handleStockOut(row)">出库</el-button>
            <el-button type="primary" link @click="handleSetThreshold(row)">设阈值</el-button>
            <el-button type="info" link @click="handleViewRecord(row)">记录</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-container">
        <el-pagination :current-page="queryParams.pageNum" :page-size="queryParams.pageSize" :total="total" layout="total, prev, pager, next" @current-change="handlePageChange" />
      </div>
    </el-card>

    <!-- 出入库弹窗 -->
    <el-dialog v-model="stockDialogVisible" :title="stockDialogTitle" width="400px">
      <el-form :model="stockForm" label-width="80px">
        <el-form-item label="商品">{{ stockForm.dishName }}</el-form-item>
        <el-form-item label="数量">
          <el-input-number v-model="stockForm.quantity" :min="1" :max="9999" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="stockForm.remark" type="textarea" :rows="2" placeholder="请输入备注（选填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="stockDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmStock">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.inventory-list-container {
  .search-card { margin-bottom: 16px; :deep(.el-card__body) { padding-bottom: 0; } }
  .card-header { display: flex; justify-content: space-between; align-items: center; span { font-size: 16px; font-weight: 600; } }
  .pagination-container { margin-top: 16px; display: flex; justify-content: flex-end; }
  .low-stock { color: #F56C6C; font-weight: 600; }
}
</style>
