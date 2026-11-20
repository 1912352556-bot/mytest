<script setup lang="ts">
/**
 * 订单详情页面
 */
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrderDetail, updateOrderStatus, cancelOrder } from '@/api/order'

const router = useRouter()
const route = useRoute()

/* 加载状态 */
const loading = ref(false)

/* 订单详情数据 */
const orderDetail = ref<Order | null>(null)

/* 订单ID */
const orderId = computed(() => route.params.id as string)

/* 状态标签类型 */
const statusTagType = (status: number) => {
  const typeMap: Record<number, string> = {
    0: 'warning',
    1: 'primary',
    2: 'info',
    3: 'success',
    4: 'danger'
  }
  return typeMap[status] || 'info'
}

/* 状态文本 */
const statusText = (status: number) => {
  const textMap: Record<number, string> = {
    0: '待调度',
    1: '已调度',
    2: '配送中',
    3: '已完成',
    4: '已取消'
  }
  return textMap[status] || '未知'
}

/* 获取订单详情 */
const fetchOrderDetail = async () => {
  if (!orderId.value) {
    ElMessage.error('订单ID不存在')
    router.push('/order/list')
    return
  }

  loading.value = true
  try {
    const res = await getOrderDetail(orderId.value)
    orderDetail.value = res.data.data
  } catch (error) {
    ElMessage.error('获取订单详情失败')
    console.error('获取订单详情失败:', error)
  } finally {
    loading.value = false
  }
}

/* 取消订单 */
const handleCancel = async () => {
  if (!orderDetail.value) return
  
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await cancelOrder(orderDetail.value.id)
    ElMessage.success('订单已取消')
    fetchOrderDetail()
  } catch (e) {
    /* 取消操作 */
  }
}

/* 返回列表 */
const goBack = () => {
  router.push('/order/list')
}

/* 去调度 */
const goSchedule = () => {
  router.push('/schedule/pending')
}

onMounted(() => {
  fetchOrderDetail()
})

// 监听路由参数变化，当订单ID改变时重新加载数据
watch(
  () => route.params.id,
  (newId, oldId) => {
    if (newId && newId !== oldId) {
      fetchOrderDetail()
    }
  }
)
</script>

<template>
  <div class="order-detail-container" v-loading="loading">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>订单详情</span>
          <el-button @click="goBack">返回列表</el-button>
        </div>
      </template>

      <template v-if="orderDetail">
        <!-- 订单状态 -->
        <div class="order-status-bar">
          <div class="status-info">
            <el-tag :type="statusTagType(orderDetail.orderStatus)" size="large">
              {{ statusText(orderDetail.orderStatus) }}
            </el-tag>
            <span class="order-no">订单编号：{{ orderDetail.orderNo }}</span>
          </div>
          <div class="status-actions">
            <el-button 
              v-if="orderDetail.orderStatus === 0" 
              type="primary" 
              @click="goSchedule"
            >
              去调度
            </el-button>
            <el-button 
              v-if="orderDetail.orderStatus === 0" 
              type="warning" 
              @click="handleCancel"
            >
              取消订单
            </el-button>
          </div>
        </div>

        <!-- 基本信息 -->
        <el-divider content-position="left">基本信息</el-divider>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="客户名称">{{ orderDetail.customerName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ orderDetail.customerPhone }}</el-descriptions-item>
          <!-- <el-descriptions-item label="配送区域">{{ orderDetail.deliveryArea }}</el-descriptions-item> -->
          <el-descriptions-item label="期望配送时间">{{ orderDetail.expectedDeliveryTime }}</el-descriptions-item>
          <el-descriptions-item label="配送地址" :span="2">{{ orderDetail.deliveryAddress }}</el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ orderDetail.createTime }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ orderDetail.updateTime }}</el-descriptions-item>
          <el-descriptions-item v-if="orderDetail.actualDeliveryTime" label="实际配送时间">
            {{ orderDetail.actualDeliveryTime }}
          </el-descriptions-item>
          <el-descriptions-item label="创建人">{{ orderDetail.creatorName }}</el-descriptions-item>
        </el-descriptions>

        <!-- 订单商品 -->
        <el-divider content-position="left">订单商品</el-divider>
        <el-table :data="orderDetail.orderItems" stripe border>
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column prop="dishCode" label="商品编码" width="120" />
          <el-table-column prop="dishName" label="商品名称" />
          <el-table-column prop="unitPrice" label="单价" width="100" align="right">
            <template #default="{ row }">¥{{ row.unitPrice.toFixed(2) }}</template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="80" align="center" />
          <el-table-column prop="subtotal" label="小计" width="100" align="right">
            <template #default="{ row }">
              <span class="subtotal">¥{{ row.subtotal.toFixed(2) }}</span>
            </template>
          </el-table-column>
        </el-table>

        <!-- 订单金额 -->
        <div class="order-amount">
          <span>订单总金额：</span>
          <span class="amount">¥{{ orderDetail.totalAmount.toFixed(2) }}</span>
        </div>

        <!-- 订单备注 -->
        <el-divider content-position="left">订单备注</el-divider>
        <div class="order-remark">
          {{ orderDetail.remark || '无' }}
        </div>
      </template>
    </el-card>
  </div>
</template>

<style scoped lang="scss">
.order-detail-container {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    span {
      font-size: 16px;
      font-weight: 600;
    }
  }
  
  .order-status-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 20px;
    background-color: #f5f7fa;
    border-radius: 8px;
    margin-bottom: 20px;
    
    .status-info {
      display: flex;
      align-items: center;
      gap: 16px;
      
      .order-no {
        color: #606266;
        font-size: 14px;
      }
    }
    
    .status-actions {
      display: flex;
      gap: 10px;
    }
  }
  
  .subtotal {
    color: #F56C6C;
    font-weight: 600;
  }
  
  .order-amount {
    margin-top: 16px;
    text-align: right;
    font-size: 16px;
    
    .amount {
      color: #F56C6C;
      font-size: 24px;
      font-weight: 600;
      margin-left: 8px;
    }
  }
  
  .order-remark {
    padding: 16px;
    background-color: #f5f7fa;
    border-radius: 8px;
    color: #606266;
    line-height: 1.6;
  }
}
</style>
