<template>
  <div class="m-page">
    <van-nav-bar title="订单详情" left-arrow :border="false" fixed placeholder @click-left="goBack" />

    <van-loading v-if="loading" class="m-detail__loading" vertical>加载中...</van-loading>

    <template v-else-if="order">
      <!-- 状态卡 -->
      <div class="m-detail__status" :class="statusCardClass">
        <div class="m-detail__status-text">{{ ORDER_STATUS_TEXT[order.orderStatus] || '未知状态' }}</div>
        <div class="m-detail__status-sub">
          <template v-if="order.orderStatus === 3">
            已于 {{ formatTime(order.actualDeliveryTime, true) }} 送达
          </template>
          <template v-else-if="order.orderStatus === 4">
            订单已取消{{ order.remark ? `：${order.remark}` : '' }}
          </template>
          <template v-else>
            期望 {{ formatTime(order.expectedDeliveryTime, true) }} 送达
            <span v-if="expectedHint(order.expectedDeliveryTime)">
              · {{ expectedHint(order.expectedDeliveryTime) }}
            </span>
          </template>
        </div>
      </div>

      <van-steps
        v-if="order.orderStatus !== 4"
        :active="stepActive"
        active-color="#16a34a"
        class="m-card"
      >
        <van-step>待接单</van-step>
        <van-step>配送中</van-step>
        <van-step>已送达</van-step>
      </van-steps>

      <!-- 商品明细 -->
      <div class="m-card">
        <div class="m-card__head">
          <span class="m-title">商品清单</span>
          <span class="m-muted">{{ items.length }} 种</span>
        </div>
        <div v-for="item in items" :key="item.id" class="m-detail__item">
          <span class="m-ellipsis">{{ item.dishName || `商品#${item.dishId}` }}</span>
          <span class="m-muted">×{{ item.quantity }}</span>
          <span class="m-price">{{ Number(item.subtotal).toFixed(2) }}</span>
        </div>
        <div class="m-detail__total">
          <span>合计</span>
          <span class="m-price">{{ Number(order.totalAmount).toFixed(2) }}</span>
        </div>
      </div>

      <!-- 配送信息 -->
      <div class="m-card">
        <div class="m-card__head">
          <span class="m-title">配送信息</span>
        </div>
        <div class="m-detail__row"><span class="m-muted">订单编号</span><span>{{ order.orderNo }}</span></div>
        <div class="m-detail__row"><span class="m-muted">下单时间</span><span>{{ formatTime(order.createTime, true) }}</span></div>
        <div class="m-detail__row"><span class="m-muted">期望送达</span><span>{{ formatTime(order.expectedDeliveryTime, true) }}</span></div>
        <div v-if="order.actualDeliveryTime" class="m-detail__row">
          <span class="m-muted">实际送达</span><span>{{ formatTime(order.actualDeliveryTime, true) }}</span>
        </div>
        <div class="m-detail__row"><span class="m-muted">配送区域</span><span>{{ order.deliveryArea }}</span></div>
        <div class="m-detail__row"><span class="m-muted">配送地址</span><span class="m-detail__addr">{{ order.deliveryAddress }}</span></div>
        <div v-if="hasLocation" class="m-detail__row">
          <span class="m-muted">地图位置</span>
          <span class="m-detail__map-link" @click="openMap">
            <van-icon name="location-o" /> 查看地图选点
          </span>
        </div>
        <div class="m-detail__row"><span class="m-muted">联系人</span><span>{{ order.customerName }} {{ order.customerPhone }}</span></div>
        <div v-if="order.remark" class="m-detail__row"><span class="m-muted">备注</span><span>{{ order.remark }}</span></div>
      </div>

      <div v-if="canCancel" class="m-detail__actions">
        <van-button round block plain type="warning" @click="cancel">取消订单</van-button>
      </div>
    </template>

    <van-empty v-else description="订单不存在或无权查看" />
  </div>
</template>

<script setup lang="ts">
/**
 * 客户：订单详情
 */
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showConfirmDialog, showToast } from 'vant'
import { cancelMyOrder, getMyOrderDetail } from '@/api/mobile'
import { ORDER_STATUS_TEXT, expectedHint, formatTime } from '@/utils/mobileStatus'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const order = ref<Order | null>(null)

const items = computed<OrderItem[]>(() => (order.value?.orderItems || []) as OrderItem[])

const stepActive = computed(() => {
  const status = order.value?.orderStatus
  if (status === 2) return 1
  if (status === 3) return 2
  return 0
})

const statusCardClass = computed(() => {
  const status = order.value?.orderStatus
  if (status === 3) return 'm-detail__status--done'
  if (status === 4) return 'm-detail__status--cancel'
  if (status === 2) return 'm-detail__status--run'
  return ''
})

const canCancel = computed(() => {
  const status = order.value?.orderStatus
  return status === 0 || status === 1
})

/* 下单时是否在地图上选过点 */
const hasLocation = computed(
  () => order.value?.longitude != null && order.value?.latitude != null
)

/* 打开高德地图查看配送位置 */
const openMap = () => {
  const current = order.value
  if (!current || current.longitude == null || current.latitude == null) {
    return
  }
  const name = encodeURIComponent(current.deliveryAddress || '配送地址')
  window.open(
    `https://uri.amap.com/marker?position=${current.longitude},${current.latitude}&name=${name}&coordinate=gaode&callnative=1&src=prefab-mobile`,
    '_blank'
  )
}

const loadDetail = async () => {
  loading.value = true
  try {
    const res = await getMyOrderDetail(String(route.params.id))
    order.value = res.data.data
  } catch (error) {
    order.value = null
  } finally {
    loading.value = false
  }
}

const cancel = async () => {
  if (!order.value) return
  try {
    await showConfirmDialog({
      title: '取消订单',
      message: '确认取消该订单吗？取消后库存会自动补回。',
      confirmButtonText: '确认取消'
    })
  } catch (e) {
    return
  }
  try {
    await cancelMyOrder(order.value.id, '客户在移动端取消')
    showToast('订单已取消')
    loadDetail()
  } catch (error) {
    /* 错误已统一提示 */
  }
}

const goBack = () => {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.replace('/m/customer/orders')
  }
}

onMounted(loadDetail)
</script>

<style scoped>
.m-detail__loading {
  padding: 60px 0;
}

.m-detail__status {
  border-radius: 14px;
  padding: 16px;
  color: #fff;
  background: linear-gradient(135deg, #22c55e, #16a34a);
  margin-bottom: 10px;
}

.m-detail__status--run {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
}

.m-detail__status--done {
  background: linear-gradient(135deg, #16a34a, #0f5132);
}

.m-detail__status--cancel {
  background: linear-gradient(135deg, #a1a1aa, #71717a);
}

.m-detail__status-text {
  font-size: 20px;
  font-weight: 700;
}

.m-detail__status-sub {
  margin-top: 6px;
  font-size: 12px;
  opacity: 0.92;
}

.m-detail__item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 0;
  font-size: 13px;
  color: #2c3b32;
}

.m-detail__item span:first-child {
  flex: 1;
  min-width: 0;
}

.m-detail__total {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px dashed #eaf4ee;
  font-size: 13px;
}

.m-detail__row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  font-size: 13px;
  padding: 5px 0;
}

.m-detail__row > span:last-child {
  text-align: right;
  color: #2c3b32;
  flex: 1;
}

.m-detail__addr {
  word-break: break-all;
}

.m-detail__map-link {
  color: #16a34a;
  display: inline-flex;
  align-items: center;
  gap: 2px;
}

.m-detail__actions {
  margin-top: 16px;
  padding-bottom: 64px;
}
</style>
