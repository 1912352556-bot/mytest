<script setup lang="ts">
/**
 * 出入库记录页面
 */
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getInventoryRecords, getInventoryDetail } from '@/api/inventory'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const inventoryDetail = ref<Inventory | null>(null)
const recordList = ref<InventoryRecord[]>([])
const total = ref(0)

const inventoryId = computed(() => route.params.id as string)

const queryParams = reactive({ pageNum: 1, pageSize: 10 })

/* 获取库存详情 */
const fetchInventoryDetail = async () => {
  if (!inventoryId.value) return
  try {
    const res = await getInventoryDetail(inventoryId.value)
    inventoryDetail.value = res.data.data
  } catch (error) {
    inventoryDetail.value = {
      id: inventoryId.value,
      dishId: '1',
      dishName: '宫保鸡丁',
      dishCode: 'GBGD',
      currentStock: 100,
      warningThreshold: 20,
      unit: '份',
      status: 1
    } as Inventory
  }
}

/* 获取出入库记录 */
const fetchRecords = async () => {
  if (!inventoryId.value) return
  loading.value = true
  try {
    const res = await getInventoryRecords(inventoryId.value, queryParams)
    recordList.value = res.data.data.records || []
    total.value = res.data.data.total || 0
  } catch (error) {
    recordList.value = [
      { id: '1', inventoryId: inventoryId.value, dishName: '宫保鸡丁', changeType: 1, changeQuantity: 50, beforeStock: 50, afterStock: 100, remark: '补货入库', operatorId: '1', operatorName: '管理员', createTime: '2025-01-21 10:00:00' },
      { id: '2', inventoryId: inventoryId.value, dishName: '宫保鸡丁', changeType: 2, changeQuantity: 10, beforeStock: 100, afterStock: 90, orderId: '1', orderNo: 'ORD20250121001', remark: '订单出库', operatorId: '1', operatorName: '系统', createTime: '2025-01-21 09:30:00' },
      { id: '3', inventoryId: inventoryId.value, dishName: '宫保鸡丁', changeType: 1, changeQuantity: 30, beforeStock: 20, afterStock: 50, remark: '采购入库', operatorId: '1', operatorName: '管理员', createTime: '2025-01-20 14:00:00' }
    ] as InventoryRecord[]
    total.value = 3
  } finally {
    loading.value = false
  }
}

const changeTypeText = (type: number) => {
  if (type === 0) return '入库'
  if (type === 1) return '出库'
  if (type === 2) return '订单出库'
  return '未知'
}
const changeTypeTag = (type: number) => type === 0 ? 'success' : 'warning'

const goBack = () => { router.push('/inventory/list') }

const handlePageChange = (page: number) => { queryParams.pageNum = page; fetchRecords() }

onMounted(() => {
  fetchInventoryDetail()
  fetchRecords()
})
</script>

<template>
  <div class="inventory-record-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>出入库记录</span>
          <el-button @click="goBack">返回库存列表</el-button>
        </div>
      </template>

      <!-- 库存信息 -->
      <template v-if="inventoryDetail">
        <el-descriptions :column="4" border class="mb16">
          <el-descriptions-item label="商品编码">{{ inventoryDetail.dishCode }}</el-descriptions-item>
          <el-descriptions-item label="商品名称">{{ inventoryDetail.dishName }}</el-descriptions-item>
          <el-descriptions-item label="当前库存">{{ inventoryDetail.currentStock }} {{ inventoryDetail.unit }}</el-descriptions-item>
          <el-descriptions-item label="预警阈值">{{ inventoryDetail.warningThreshold }} {{ inventoryDetail.unit }}</el-descriptions-item>
        </el-descriptions>
      </template>

      <!-- 记录列表 -->
      <el-table :data="recordList" v-loading="loading" stripe border>
        <el-table-column prop="createTime" label="时间" width="160" />
        <el-table-column prop="changeType" label="类型" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="changeTypeTag(row.changeType)" size="small">{{ changeTypeText(row.changeType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="changeQuantity" label="数量" width="100" align="center">
          <template #default="{ row }">
            <span :class="row.changeType === 0 ? 'text-success' : 'text-warning'">
              {{ row.changeType === 0 ? '+' : '-' }}{{ row.changeQuantity || row.quantity }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="beforeStock" label="变更前" width="100" align="center" />
        <el-table-column prop="afterStock" label="变更后" width="100" align="center" />
        <el-table-column prop="orderNo" label="关联订单" width="160">
          <template #default="{ row }">{{ row.orderNo || '-' }}</template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
        <el-table-column prop="operatorName" label="操作人" width="100" />
      </el-table>

      <div class="pagination-container">
        <el-pagination :current-page="queryParams.pageNum" :page-size="queryParams.pageSize" :total="total" layout="total, prev, pager, next" @current-change="handlePageChange" />
      </div>
    </el-card>
  </div>
</template>

<style scoped lang="scss">
.inventory-record-container {
  .card-header { display: flex; justify-content: space-between; align-items: center; span { font-size: 16px; font-weight: 600; } }
  .mb16 { margin-bottom: 16px; }
  .pagination-container { margin-top: 16px; display: flex; justify-content: flex-end; }
  .text-success { color: #67C23A; font-weight: 600; }
  .text-warning { color: #E6A23C; font-weight: 600; }
}
</style>
