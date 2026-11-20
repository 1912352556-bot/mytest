<template>
  <div class="m-page">
    <van-nav-bar title="任务详情" left-arrow :border="false" fixed placeholder @click-left="goBack" />

    <van-loading v-if="loading" class="m-detail__loading" vertical>加载中...</van-loading>

    <template v-else-if="task">
      <div class="m-detail__status" :class="statusCardClass">
        <div class="m-detail__status-text">{{ DELIVERY_STATUS_TEXT[task.deliveryStatus] || '未知状态' }}</div>
        <div class="m-detail__status-sub">
          期望 {{ formatTime(task.expectedDeliveryTime, true) }} 送达
          <span v-if="expectedHint(task.expectedDeliveryTime)">· {{ expectedHint(task.expectedDeliveryTime) }}</span>
        </div>
      </div>

      <van-steps :active="stepActive" active-color="#16a34a" class="m-card">
        <van-step>待取货</van-step>
        <van-step>配送中</van-step>
        <van-step>已送达</van-step>
      </van-steps>

      <!-- 客户信息 -->
      <div class="m-card">
        <div class="m-card__head">
          <span class="m-title">客户信息</span>
          <div class="m-detail__head-actions">
            <van-button
              v-if="hasLocation"
              size="mini"
              round
              plain
              type="success"
              icon="guide-o"
              @click="navigate"
            >
              导航
            </van-button>
            <van-button
              v-if="task.customerPhone"
              size="mini"
              round
              plain
              type="primary"
              icon="phone-o"
              @click="callCustomer"
            >
              联系客户
            </van-button>
          </div>
        </div>
        <div class="m-detail__row"><span class="m-muted">客户姓名</span><span>{{ task.customerName || '-' }}</span></div>
        <div class="m-detail__row"><span class="m-muted">联系电话</span><span>{{ task.customerPhone || '-' }}</span></div>
        <div class="m-detail__row">
          <span class="m-muted">配送地址</span>
          <span class="m-detail__addr">{{ task.deliveryAddress }}</span>
        </div>
        <div v-if="task.remark" class="m-detail__row"><span class="m-muted">备注</span><span>{{ task.remark }}</span></div>
      </div>

      <!-- 订单信息 -->
      <div class="m-card">
        <div class="m-card__head">
          <span class="m-title">订单信息</span>
          <span class="m-price">{{ Number(task.totalAmount || 0).toFixed(2) }}</span>
        </div>
        <div class="m-detail__row"><span class="m-muted">配送单号</span><span>{{ task.deliveryNo }}</span></div>
        <div class="m-detail__row"><span class="m-muted">订单编号</span><span>{{ task.orderNo || '-' }}</span></div>
        <div class="m-detail__row"><span class="m-muted">任务创建</span><span>{{ formatTime(task.createTime, true) }}</span></div>
        <div v-if="task.pickupTime" class="m-detail__row"><span class="m-muted">取货时间</span><span>{{ formatTime(task.pickupTime, true) }}</span></div>
        <div v-if="task.deliveryTime" class="m-detail__row"><span class="m-muted">送达时间</span><span>{{ formatTime(task.deliveryTime, true) }}</span></div>
      </div>

      <!-- 底部操作 -->
      <div v-if="task.deliveryStatus === 0 || task.deliveryStatus === 1" class="m-fixed-bar">
        <van-button
          v-if="task.deliveryStatus === 0"
          round
          block
          type="primary"
          @click="start"
        >
          开始配送（已取货）
        </van-button>
        <van-button
          v-if="task.deliveryStatus === 1"
          round
          block
          type="success"
          @click="complete"
        >
          确认送达
        </van-button>
      </div>
    </template>

    <van-empty v-else description="任务不存在或无权查看" />
  </div>
</template>

<script setup lang="ts">
/**
 * 配送员：任务详情与操作
 */
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showConfirmDialog, showToast } from 'vant'
import { completeMyTask, getMyTaskDetail, startMyTask } from '@/api/mobile'
import { DELIVERY_STATUS_TEXT, expectedHint, formatTime } from '@/utils/mobileStatus'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const task = ref<DeliveryTask | null>(null)

const stepActive = computed(() => {
  const status = task.value?.deliveryStatus
  if (status === 1) return 1
  if (status === 2) return 2
  return 0
})

const statusCardClass = computed(() => {
  const status = task.value?.deliveryStatus
  if (status === 1) return 'm-detail__status--run'
  if (status === 2) return 'm-detail__status--done'
  if (status === 3) return 'm-detail__status--cancel'
  return ''
})

const loadDetail = async () => {
  loading.value = true
  try {
    const res = await getMyTaskDetail(String(route.params.id))
    task.value = res.data.data
  } catch (error) {
    task.value = null
  } finally {
    loading.value = false
  }
}

const start = async () => {
  if (!task.value) return
  try {
    await startMyTask(task.value.id)
    showToast('已开始配送')
    loadDetail()
  } catch (error) {
    /* 错误已统一提示 */
  }
}

const complete = async () => {
  if (!task.value) return
  try {
    await showConfirmDialog({
      title: '确认送达',
      message: '确认该订单已送达客户？确认后订单将标记为已完成。',
      confirmButtonText: '确认送达'
    })
  } catch (e) {
    return
  }
  try {
    await completeMyTask(task.value.id)
    showToast('配送已完成')
    loadDetail()
  } catch (error) {
    /* 错误已统一提示 */
  }
}

const callCustomer = () => {
  if (task.value?.customerPhone) {
    window.location.href = `tel:${task.value.customerPhone}`
  }
}

/* 是否有客户下单时的地图选点坐标 */
const hasLocation = computed(
  () => task.value?.longitude != null && task.value?.latitude != null
)

/* 调用高德地图 URI 打开导航（手机端会唤起高德 App，未安装则打开网页版） */
const navigate = () => {
  const current = task.value
  if (!current || current.longitude == null || current.latitude == null) {
    return
  }
  const name = encodeURIComponent(current.deliveryAddress || '配送地址')
  window.location.href = `https://uri.amap.com/marker?position=${current.longitude},${current.latitude}&name=${name}&coordinate=gaode&callnative=1&src=prefab-mobile`
}

const goBack = () => {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.replace('/m/courier/tasks')
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
  background: linear-gradient(135deg, #f59e0b, #d97706);
  margin-bottom: 10px;
}

.m-detail__status--run {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
}

.m-detail__status--done {
  background: linear-gradient(135deg, #16a34a, #0f5132);
}

.m-detail__status--cancel {
  background: linear-gradient(135deg, #f87171, #dc2626);
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

.m-detail__row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  font-size: 13px;
  padding: 5px 0;
}

.m-detail__head-actions {
  display: flex;
  gap: 6px;
}

.m-detail__row > span:last-child {
  flex: 1;
  text-align: right;
  color: #2c3b32;
}

.m-detail__addr {
  word-break: break-all;
}

.m-page {
  padding-bottom: 96px;
}
</style>
