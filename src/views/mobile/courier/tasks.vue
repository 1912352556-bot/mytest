<template>
  <div class="m-page m-tasks">
    <van-nav-bar title="我的配送任务" :border="false" fixed placeholder />

    <!-- 工作台概览 -->
    <div class="m-tasks__summary">
      <div class="m-tasks__summary-head">
        <div>
          <div class="m-tasks__hi">你好，{{ summary?.personName || userInfo?.realname || '配送员' }}</div>
          <div class="m-tasks__sub">
            {{ summary?.vehicleNo ? `车牌 ${summary.vehicleNo}` : '安全配送，注意时效' }}
          </div>
        </div>
        <span class="m-tasks__duty" :class="dutyClass">{{ dutyText }}</span>
      </div>
      <div class="m-tasks__stats">
        <div class="m-tasks__stat">
          <div class="m-tasks__stat-num">{{ summary?.pendingCount ?? 0 }}</div>
          <div class="m-tasks__stat-label">待取货</div>
        </div>
        <div class="m-tasks__stat">
          <div class="m-tasks__stat-num">{{ summary?.deliveringCount ?? 0 }}</div>
          <div class="m-tasks__stat-label">配送中</div>
        </div>
        <div class="m-tasks__stat">
          <div class="m-tasks__stat-num">{{ summary?.completedTodayCount ?? 0 }}</div>
          <div class="m-tasks__stat-label">今日完成</div>
        </div>
      </div>
    </div>

    <van-tabs v-model:active="activeTab" sticky swipeable @change="onTabChange">
      <van-tab v-for="tab in tabs" :key="tab.value" :name="tab.value" :title="tab.label" />
    </van-tabs>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list
        v-model:loading="loading"
        v-model:error="error"
        :finished="finished"
        finished-text="没有更多任务了"
        error-text="加载失败，点击重试"
        @load="onLoad"
      >
        <div v-for="task in tasks" :key="task.id" class="m-task m-card" @click="goDetail(task)">
          <div class="m-card__head">
            <span class="m-muted">{{ task.orderNo || task.deliveryNo }}</span>
            <span class="m-tag" :class="DELIVERY_STATUS_CLASS[task.deliveryStatus]">
              {{ DELIVERY_STATUS_TEXT[task.deliveryStatus] || '未知' }}
            </span>
          </div>

          <div class="m-task__address">
            <van-icon name="location" color="#16a34a" />
            <span>{{ task.deliveryAddress }}</span>
          </div>

          <div class="m-task__meta">
            <span>{{ task.customerName || '客户' }}</span>
            <a v-if="task.customerPhone" class="m-task__phone" @click.stop="callCustomer(task.customerPhone)">
              <van-icon name="phone" /> {{ task.customerPhone }}
            </a>
          </div>

          <div class="m-task__meta">
            <span>期望 {{ formatTime(task.expectedDeliveryTime) }} 送达</span>
            <span v-if="expectedHint(task.expectedDeliveryTime)" class="m-task__hint">
              {{ expectedHint(task.expectedDeliveryTime) }}
            </span>
          </div>

          <div class="m-task__foot">
            <span class="m-price">{{ Number(task.totalAmount || 0).toFixed(2) }}</span>
            <div class="m-task__actions">
              <van-button
                v-if="task.deliveryStatus === 0"
                size="small"
                round
                type="primary"
                @click.stop="startTask(task)"
              >
                开始配送
              </van-button>
              <van-button
                v-if="task.deliveryStatus === 1"
                size="small"
                round
                type="success"
                @click.stop="completeTask(task)"
              >
                确认送达
              </van-button>
              <van-button
                v-if="task.deliveryStatus === 0 || task.deliveryStatus === 1"
                size="small"
                round
                plain
                type="danger"
                @click.stop="openException(task)"
              >
                异常
              </van-button>
              <van-button size="small" round plain @click.stop="goDetail(task)">详情</van-button>
            </div>
          </div>
        </div>
      </van-list>
    </van-pull-refresh>

    <van-empty v-if="!loading && !tasks.length" description="当前没有任务" />

    <!-- 异常上报 -->
    <van-popup v-model:show="showException" position="bottom" round :style="{ height: '42%' }">
      <div class="m-sheet">
        <div class="m-sheet__title">异常上报</div>
        <div class="m-sheet__scroll">
          <van-cell-group inset>
            <van-field
              v-model="exceptionRemark"
              type="textarea"
              rows="3"
              autosize
              maxlength="100"
              show-word-limit
              placeholder="请描述异常情况，如 客户电话无法接通、地址有误"
            />
          </van-cell-group>
        </div>
        <div class="m-sheet__footer">
          <van-button block round type="danger" :loading="submitting" @click="submitException">
            提交异常
          </van-button>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup lang="ts">
/**
 * 配送员：我的配送任务列表（按状态筛选、开始配送、确认送达、异常上报）
 */
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showConfirmDialog, showToast } from 'vant'
import {
  completeMyTask,
  getCourierSummary,
  getMyTasks,
  reportTaskException,
  startMyTask
} from '@/api/mobile'
import { DELIVERY_STATUS_CLASS, DELIVERY_STATUS_TEXT, expectedHint, formatTime } from '@/utils/mobileStatus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo)

const tabs = [
  { label: '待取货', value: '0' },
  { label: '配送中', value: '1' },
  { label: '已送达', value: '2' },
  { label: '异常', value: '3' }
]

const activeTab = ref('0')
const tasks = ref<DeliveryTask[]>([])
const summary = ref<MobileSummary | null>(null)
const loading = ref(false)
const refreshing = ref(false)
const finished = ref(false)
const error = ref(false)
const pageNum = ref(1)
const pageSize = 10

const showException = ref(false)
const exceptionRemark = ref('')
const submitting = ref(false)
const exceptionTask = ref<DeliveryTask | null>(null)

const dutyText = computed(() => {
  const status = summary.value?.status
  if (status === 1) return '配送中'
  if (status === 2) return '休息中'
  return '空闲接单'
})

const dutyClass = computed(() => {
  const status = summary.value?.status
  if (status === 1) return 'm-tasks__duty--busy'
  if (status === 2) return 'm-tasks__duty--rest'
  return ''
})

const loadSummary = async () => {
  try {
    const res = await getCourierSummary()
    summary.value = res.data.data
  } catch (err) {
    /* 错误已统一提示 */
  }
}

const onLoad = async () => {
  try {
    const res = await getMyTasks({
      pageNum: pageNum.value,
      pageSize,
      deliveryStatus: Number(activeTab.value)
    })
    const page = res.data.data
    if (pageNum.value === 1) {
      tasks.value = page.records || []
    } else {
      tasks.value = tasks.value.concat(page.records || [])
    }
    finished.value = tasks.value.length >= Number(page.total || 0)
    pageNum.value += 1
  } catch (err) {
    error.value = true
  } finally {
    loading.value = false
  }
}

const reset = () => {
  tasks.value = []
  pageNum.value = 1
  finished.value = false
  error.value = false
  loading.value = true
  onLoad()
}

const onTabChange = () => {
  reset()
}

const onRefresh = async () => {
  await loadSummary()
  tasks.value = []
  pageNum.value = 1
  finished.value = false
  error.value = false
  loading.value = true
  await onLoad()
  refreshing.value = false
}

const startTask = async (task: DeliveryTask) => {
  try {
    await startMyTask(task.id)
    showToast('已开始配送')
    await onRefresh()
  } catch (err) {
    /* 错误已统一提示 */
  }
}

const completeTask = async (task: DeliveryTask) => {
  try {
    await showConfirmDialog({
      title: '确认送达',
      message: `确认订单 ${task.orderNo || task.deliveryNo} 已送达客户？`,
      confirmButtonText: '确认送达'
    })
  } catch (e) {
    return
  }
  try {
    await completeMyTask(task.id)
    showToast('配送已完成')
    await onRefresh()
  } catch (err) {
    /* 错误已统一提示 */
  }
}

const openException = (task: DeliveryTask) => {
  exceptionTask.value = task
  exceptionRemark.value = ''
  showException.value = true
}

const submitException = async () => {
  if (!exceptionTask.value) return
  if (!exceptionRemark.value.trim()) {
    showToast('请填写异常说明')
    return
  }
  submitting.value = true
  try {
    await reportTaskException(exceptionTask.value.id, exceptionRemark.value.trim())
    showToast('异常已上报')
    showException.value = false
    await onRefresh()
  } catch (err) {
    /* 错误已统一提示 */
  } finally {
    submitting.value = false
  }
}

const callCustomer = (phone?: string) => {
  if (phone) {
    window.location.href = `tel:${phone}`
  }
}

const goDetail = (task: DeliveryTask) => {
  router.push(`/m/courier/task/${task.id}`)
}

onMounted(() => {
  loadSummary()
})
</script>

<style scoped>
.m-tasks {
  padding-bottom: 24px;
}

.m-tasks__summary {
  border-radius: 16px;
  padding: 16px;
  margin-bottom: 10px;
  color: #fff;
  background: linear-gradient(135deg, #16a34a, #0f5132);
}

.m-tasks__summary-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
}

.m-tasks__hi {
  font-size: 16px;
  font-weight: 600;
}

.m-tasks__sub {
  margin-top: 4px;
  font-size: 12px;
  opacity: 0.85;
}

.m-tasks__duty {
  padding: 3px 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.2);
  font-size: 12px;
}

.m-tasks__duty--busy {
  background: rgba(255, 122, 69, 0.85);
}

.m-tasks__duty--rest {
  background: rgba(255, 255, 255, 0.15);
}

.m-tasks__stats {
  display: flex;
  margin-top: 16px;
}

.m-tasks__stat {
  flex: 1;
  text-align: center;
}

.m-tasks__stat-num {
  font-size: 20px;
  font-weight: 700;
}

.m-tasks__stat-label {
  font-size: 11px;
  opacity: 0.85;
  margin-top: 2px;
}

.m-task {
  margin-top: 10px;
}

.m-task__address {
  display: flex;
  gap: 6px;
  font-size: 14px;
  color: #1f2d24;
  font-weight: 500;
  margin-top: 4px;
  line-height: 1.4;
}

.m-task__meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12px;
  color: #6b7f73;
  margin-top: 6px;
}

.m-task__phone {
  color: #16a34a;
  display: inline-flex;
  align-items: center;
  gap: 2px;
}

.m-task__hint {
  color: #d97706;
}

.m-task__foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px dashed #eaf4ee;
}

.m-task__actions {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.m-sheet {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.m-sheet__title {
  padding: 16px;
  text-align: center;
  font-size: 16px;
  font-weight: 600;
  color: #14342b;
}

.m-sheet__scroll {
  flex: 1;
  overflow-y: auto;
}

.m-sheet__footer {
  padding: 10px 16px calc(10px + env(safe-area-inset-bottom));
  border-top: 1px solid #e3efe7;
}
</style>
