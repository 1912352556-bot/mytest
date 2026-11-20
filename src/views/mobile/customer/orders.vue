<template>
  <div class="m-page">
    <van-nav-bar title="我的订单" :border="false" fixed placeholder />

    <van-tabs v-model:active="activeStatus" sticky swipeable @change="onTabChange">
      <van-tab v-for="tab in tabs" :key="tab.value" :name="tab.value" :title="tab.label" />
    </van-tabs>

    <van-list
      v-model:loading="loading"
      v-model:error="error"
      :finished="finished"
      finished-text="没有更多了"
      error-text="加载失败，点击重试"
      @load="onLoad"
    >
      <div v-for="order in orders" :key="order.id" class="m-order m-card" @click="goDetail(order)">
        <div class="m-card__head">
          <span class="m-muted">{{ order.orderNo }}</span>
          <span class="m-tag" :class="ORDER_STATUS_CLASS[order.orderStatus]">
            {{ ORDER_STATUS_TEXT[order.orderStatus] || '未知' }}
          </span>
        </div>

        <div class="m-order__row">
          <van-icon name="location-o" />
          <span class="m-ellipsis">{{ order.deliveryAddress }}</span>
        </div>
        <div class="m-order__row">
          <van-icon name="clock-o" />
          <span>期望 {{ formatTime(order.expectedDeliveryTime) }} 送达</span>
        </div>
        <div class="m-order__row">
          <van-icon name="calendar-o" />
          <span>下单 {{ formatTime(order.createTime, true) }}</span>
        </div>
        <div v-if="order.remark" class="m-order__remark">备注：{{ order.remark }}</div>

        <div class="m-order__foot">
          <span class="m-price">{{ Number(order.totalAmount).toFixed(2) }}</span>
          <div class="m-order__actions">
            <van-button
              v-if="canCancel(order)"
              size="small"
              round
              plain
              type="warning"
              @click.stop="cancel(order)"
            >
              取消订单
            </van-button>
            <van-button size="small" round plain type="primary">查看详情</van-button>
          </div>
        </div>
      </div>
    </van-list>

    <van-empty v-if="!loading && !orders.length" description="还没有订单，去首页逛逛吧">
      <van-button round type="primary" size="small" @click="goHome">去下单</van-button>
    </van-empty>
  </div>
</template>

<script setup lang="ts">
/**
 * 客户：我的订单（分页 + 状态筛选 + 取消）
 */
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showConfirmDialog, showToast } from 'vant'
import { cancelMyOrder, getMyOrders } from '@/api/mobile'
import { ORDER_STATUS_CLASS, ORDER_STATUS_TEXT, formatTime } from '@/utils/mobileStatus'

const router = useRouter()

const tabs = [
  { label: '全部', value: 'all' },
  { label: '待调度', value: '0' },
  { label: '已调度', value: '1' },
  { label: '配送中', value: '2' },
  { label: '已完成', value: '3' },
  { label: '已取消', value: '4' }
]

const activeStatus = ref('all')
const orders = ref<Order[]>([])
const loading = ref(false)
const finished = ref(false)
const error = ref(false)
const pageNum = ref(1)
const pageSize = 10

const currentStatus = () => (activeStatus.value === 'all' ? undefined : Number(activeStatus.value))

const onLoad = async () => {
  try {
    const res = await getMyOrders({
      pageNum: pageNum.value,
      pageSize,
      orderStatus: currentStatus()
    })
    const page = res.data.data
    if (pageNum.value === 1) {
      orders.value = page.records || []
    } else {
      orders.value = orders.value.concat(page.records || [])
    }
    finished.value = orders.value.length >= Number(page.total || 0)
    pageNum.value += 1
  } catch (err) {
    error.value = true
  } finally {
    loading.value = false
  }
}

const reset = () => {
  orders.value = []
  pageNum.value = 1
  finished.value = false
  error.value = false
  loading.value = true
  onLoad()
}

const onTabChange = () => {
  reset()
}

const canCancel = (order: Order) => order.orderStatus === 0 || order.orderStatus === 1

const cancel = async (order: Order) => {
  try {
    await showConfirmDialog({
      title: '取消订单',
      message: `确认取消订单 ${order.orderNo} 吗？取消后库存会自动补回。`,
      confirmButtonText: '确认取消'
    })
  } catch (e) {
    return
  }
  try {
    await cancelMyOrder(order.id, '客户在移动端取消')
    showToast('订单已取消')
    reset()
  } catch (err) {
    /* 错误已统一提示 */
  }
}

const goDetail = (order: Order) => {
  router.push(`/m/customer/order/${order.id}`)
}

const goHome = () => {
  router.push('/m/customer/home')
}
</script>

<style scoped>
/* 6 个状态页签在窄屏上紧凑排列，避免最后一个被裁切 */
:deep(.van-tab) {
  font-size: 12.5px;
  padding: 0 6px;
  flex-basis: 0;
}

.m-order {
  margin-top: 10px;
}

.m-order__row {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #4b5f53;
  margin-top: 6px;
  min-width: 0;
}

.m-order__row span {
  min-width: 0;
}

.m-order__remark {
  margin-top: 6px;
  font-size: 12px;
  color: #93a49a;
}

.m-order__foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px dashed #eaf4ee;
}

.m-order__actions {
  display: flex;
  gap: 8px;
}
</style>
