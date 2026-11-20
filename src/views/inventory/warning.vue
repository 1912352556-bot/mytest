<script setup lang="ts">
/**
 * 库存预警页面
 */
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getWarningList, stockIn } from '@/api/inventory'

const router = useRouter()
const loading = ref(false)
const warningList = ref<Inventory[]>([])

/* 获取预警库存 */
const fetchWarningInventory = async () => {
  loading.value = true
  try {
    const res = await getWarningList()
    warningList.value = res.data.data || []
  } catch (error) {
    ElMessage.error('获取库存预警列表失败')
    console.error('获取库存预警列表失败:', error)
    warningList.value = []
  } finally {
    loading.value = false
  }
}

/* 快速入库 */
const handleQuickStockIn = async (row: Inventory, quantity: number) => {
  try {
    await stockIn({
      inventoryId: row.id,
      quantity: quantity,
      remark: '库存预警快速补货'
    })
    ElMessage.success(`已入库 ${quantity} ${row.unit}`)
    fetchWarningInventory()
  } catch (error) {}
}

const goBack = () => { router.push('/inventory/list') }

onMounted(() => { fetchWarningInventory() })
</script>

<template>
  <div class="inventory-warning-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span><el-icon color="#F56C6C"><Warning /></el-icon> 库存预警列表</span>
          <el-button @click="goBack">返回库存列表</el-button>
        </div>
      </template>

      <el-alert type="warning" :closable="false" show-icon style="margin-bottom: 16px">
        <template #title>以下商品库存已低于预警阈值，请及时补货！</template>
      </el-alert>

      <el-table :data="warningList" v-loading="loading" stripe border>
        <el-table-column prop="dishCode" label="商品编码" width="120" />
        <el-table-column prop="dishName" label="商品名称" width="150" />
        <el-table-column prop="currentStock" label="当前库存" width="120" align="center">
          <template #default="{ row }">
            <span class="low-stock">{{ row.currentStock }} {{ row.unit }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="warningThreshold" label="预警阈值" width="120" align="center">
          <template #default="{ row }">{{ row.warningThreshold }} {{ row.unit }}</template>
        </el-table-column>
        <el-table-column label="缺货数量" width="120" align="center">
          <template #default="{ row }">
            <span class="shortage">{{ row.warningThreshold - row.currentStock }} {{ row.unit }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="lastInTime" label="最近入库时间" width="160" />
        <el-table-column label="快速补货" width="200" align="center">
          <template #default="{ row }">
            <el-button type="success" size="small" @click="handleQuickStockIn(row, 10)">+10</el-button>
            <el-button type="success" size="small" @click="handleQuickStockIn(row, 50)">+50</el-button>
            <el-button type="success" size="small" @click="handleQuickStockIn(row, 100)">+100</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="warningList.length === 0 && !loading" description="暂无预警库存，库存充足" />
    </el-card>
  </div>
</template>

<style scoped lang="scss">
.inventory-warning-container {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    span { font-size: 16px; font-weight: 600; display: flex; align-items: center; gap: 8px; }
  }
  .low-stock { color: #F56C6C; font-weight: 600; }
  .shortage { color: #E6A23C; font-weight: 600; }
}
</style>
