<script setup lang="ts">
/**
 * 订单列表页面
 */
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrderList, deleteOrder, updateOrderStatus, cancelOrder, exportOrders } from '@/api/order'

const router = useRouter()

/* 加载状态 */
const loading = ref(false)

/* 订单列表数据 */
const orderList = ref<Order[]>([])
const total = ref(0)

/* 查询参数 */
const queryParams = reactive<OrderQuery>({
  pageNum: 1,
  pageSize: 10,
  orderNo: '',
  customerName: '',
  orderStatus: undefined,
  deliveryArea: '',
  startTime: '',
  endTime: ''
})

/* 日期范围 */
const dateRange = ref<[string, string] | null>(null)

/* 订单状态选项 */
const orderStatusOptions = [
  { label: '全部', value: undefined },
  { label: '待调度', value: 0 },
  { label: '已调度', value: 1 },
  { label: '配送中', value: 2 },
  { label: '已完成', value: 3 },
  { label: '已取消', value: 4 }
]

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

/* 获取订单列表 */
const fetchOrderList = async () => {
  loading.value = true
  try {
    /* 处理日期范围 */
    if (dateRange.value) {
      queryParams.startTime = dateRange.value[0]
      queryParams.endTime = dateRange.value[1]
    } else {
      queryParams.startTime = ''
      queryParams.endTime = ''
    }
    
    const res = await getOrderList(queryParams)
    orderList.value = res.data.data.records || []
    total.value = res.data.data.total || 0
  } catch (error) {
    /* 模拟数据 */
    orderList.value = [
      {
        id: '1',
        orderNo: 'ORD20250121001',
        customerName: '张三',
        customerPhone: '13800138001',
        deliveryAddress: '北京市朝阳区建国路88号',
        deliveryArea: '朝阳区',
        expectedDeliveryTime: '2025-01-21 12:00:00',
        totalAmount: 256.00,
        orderStatus: 0,
        createTime: '2025-01-21 09:30:00'
      },
      {
        id: '2',
        orderNo: 'ORD20250121002',
        customerName: '李四',
        customerPhone: '13800138002',
        deliveryAddress: '北京市海淀区中关村大街1号',
        deliveryArea: '海淀区',
        expectedDeliveryTime: '2025-01-21 13:00:00',
        totalAmount: 188.50,
        orderStatus: 1,
        createTime: '2025-01-21 10:15:00'
      },
      {
        id: '3',
        orderNo: 'ORD20250121003',
        customerName: '王五',
        customerPhone: '13800138003',
        deliveryAddress: '北京市西城区金融街20号',
        deliveryArea: '西城区',
        expectedDeliveryTime: '2025-01-21 11:30:00',
        totalAmount: 320.00,
        orderStatus: 2,
        createTime: '2025-01-21 08:45:00'
      },
      {
        id: '4',
        orderNo: 'ORD20250120001',
        customerName: '赵六',
        customerPhone: '13800138004',
        deliveryAddress: '北京市东城区王府井大街100号',
        deliveryArea: '东城区',
        expectedDeliveryTime: '2025-01-20 12:00:00',
        totalAmount: 425.00,
        orderStatus: 3,
        createTime: '2025-01-20 09:00:00'
      }
    ] as Order[]
    total.value = 4
  } finally {
    loading.value = false
  }
}

/* 搜索 */
const handleSearch = () => {
  queryParams.pageNum = 1
  fetchOrderList()
}

/* 重置 */
const handleReset = () => {
  queryParams.orderNo = ''
  queryParams.customerName = ''
  queryParams.orderStatus = undefined
  queryParams.deliveryArea = ''
  dateRange.value = null
  queryParams.pageNum = 1
  fetchOrderList()
}

/* 新增订单 */
const handleAdd = () => {
  router.push('/order/add')
}

/* 查看详情 */
const handleView = (row: Order) => {
  router.push(`/order/detail/${row.id}`)
}

/* 取消订单 */
const handleCancel = async (row: Order) => {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await cancelOrder(row.id)
    ElMessage.success('订单已取消')
    fetchOrderList()
  } catch (e) {
    /* 取消操作或请求失败 */
  }
}

/* 删除订单 */
const handleDelete = async (row: Order) => {
  try {
    await ElMessageBox.confirm('确定要删除该订单吗？删除后不可恢复！', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteOrder(row.id)
    ElMessage.success('删除成功')
    fetchOrderList()
  } catch (e) {
    /* 取消操作或请求失败 */
  }
}

/* 导出订单 */
const handleExport = async () => {
  try {
    const res = await exportOrders(queryParams)
    const blob = new Blob([res.data], { type: 'application/vnd.ms-excel' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `订单列表_${new Date().getTime()}.xlsx`
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

/* 分页变化 */
const handlePageChange = (page: number) => {
  queryParams.pageNum = page
  fetchOrderList()
}

/* 每页数量变化 */
const handleSizeChange = (size: number) => {
  queryParams.pageSize = size
  queryParams.pageNum = 1
  fetchOrderList()
}

/* 自动刷新定时器 */
let refreshTimer: ReturnType<typeof setInterval> | null = null

/* 启动自动刷新 */
const startAutoRefresh = () => {
  refreshTimer = setInterval(() => {
    fetchOrderList()
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
  fetchOrderList()
  startAutoRefresh()
})

onUnmounted(() => {
  stopAutoRefresh()
})
</script>

<template>
  <div class="order-list-container">
    <!-- 搜索表单 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="queryParams" inline>
        <el-form-item label="订单编号">
          <el-input 
            v-model="queryParams.orderNo" 
            placeholder="请输入订单编号" 
            clearable 
            style="width: 180px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="客户名称">
          <el-input 
            v-model="queryParams.customerName" 
            placeholder="请输入客户名称" 
            clearable 
            style="width: 150px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="queryParams.orderStatus" placeholder="请选择" clearable style="width: 120px">
            <el-option 
              v-for="item in orderStatusOptions" 
              :key="item.value" 
              :label="item.label" 
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <!-- <el-form-item label="配送区域">
          <el-input 
            v-model="queryParams.deliveryArea" 
            placeholder="请输入配送区域" 
            clearable 
            style="width: 150px"
          /> -->
        <!-- </el-form-item> -->
        <el-form-item label="下单时间">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="-"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 240px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作按钮 -->
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span>订单列表</span>
          <div class="header-buttons">
            <el-button type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon>
              新增订单
            </el-button>
            <el-button type="success" @click="handleExport">
              <el-icon><Download /></el-icon>
              导出
            </el-button>
          </div>
        </div>
      </template>

      <!-- 表格 -->
      <el-table :data="orderList" v-loading="loading" stripe border style="width: 100%">
        <el-table-column prop="orderNo" label="订单编号" width="160" fixed />
        <el-table-column prop="customerName" label="客户名称" width="100" />
        <el-table-column prop="customerPhone" label="联系电话" width="120" />
        <el-table-column prop="deliveryAddress" label="配送地址" min-width="200" show-overflow-tooltip />
        <!-- <el-table-column prop="deliveryArea" label="配送区域" width="100" /> -->
        <el-table-column prop="expectedDeliveryTime" label="期望配送时间" width="160" />
        <el-table-column prop="totalAmount" label="订单金额" width="100" align="right">
          <template #default="{ row }">
            <span class="amount">¥{{ row.totalAmount.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="orderStatus" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.orderStatus)" size="small">
              {{ statusText(row.orderStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" width="160" />
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">详情</el-button>
            <el-button 
              v-if="row.orderStatus === 0" 
              type="warning" 
              link 
              @click="handleCancel(row)"
            >
              取消
            </el-button>
            <el-button 
              v-if="row.orderStatus === 4" 
              type="danger" 
              link 
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>
  </div>
</template>

<style scoped lang="scss">
.order-list-container {
  .search-card {
    margin-bottom: 16px;
    
    :deep(.el-card__body) {
      padding-bottom: 0;
    }
  }
  
  .table-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      span {
        font-size: 16px;
        font-weight: 600;
      }
      
      .header-buttons {
        display: flex;
        gap: 10px;
      }
    }
    
    .amount {
      color: #F56C6C;
      font-weight: 600;
    }
    
    .pagination-container {
      margin-top: 16px;
      display: flex;
      justify-content: flex-end;
    }
  }
}
</style>
