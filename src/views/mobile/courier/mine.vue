<template>
  <div class="m-page">
    <van-nav-bar title="我的" :border="false" fixed placeholder />

    <div class="m-mine__hero">
      <div class="m-mine__avatar">{{ avatarText }}</div>
      <div class="m-mine__info">
        <div class="m-mine__name">{{ summary?.personName || userInfo?.realname || '配送员' }}</div>
        <div class="m-mine__sub">
          <span>{{ summary?.phone || userInfo?.phone || '未绑定手机号' }}</span>
          <span class="m-mine__role">配送员</span>
        </div>
      </div>
    </div>

    <!-- 接单状态 -->
    <div class="m-card m-mine__duty">
      <div>
        <div class="m-title">接单状态</div>
        <div class="m-muted">休息时将不再接收新任务提醒</div>
      </div>
      <van-switch
        v-model="onDuty"
        size="22px"
        :loading="switching"
        active-color="#16a34a"
        @update:model-value="toggleDuty"
      />
    </div>

    <!-- 配送数据 -->
    <div class="m-card">
      <div class="m-card__head">
        <span class="m-title">配送数据</span>
        <span class="m-muted">{{ dutyText }}</span>
      </div>
      <div class="m-mine__stats">
        <div class="m-mine__stat">
          <div class="m-mine__stat-num">{{ summary?.pendingCount ?? 0 }}</div>
          <div class="m-mine__stat-label">待取货</div>
        </div>
        <div class="m-mine__stat">
          <div class="m-mine__stat-num">{{ summary?.deliveringCount ?? 0 }}</div>
          <div class="m-mine__stat-label">配送中</div>
        </div>
        <div class="m-mine__stat">
          <div class="m-mine__stat-num">{{ summary?.completedTodayCount ?? 0 }}</div>
          <div class="m-mine__stat-label">今日完成</div>
        </div>
        <div class="m-mine__stat">
          <div class="m-mine__stat-num">{{ summary?.totalDeliveryCount ?? 0 }}</div>
          <div class="m-mine__stat-label">累计配送</div>
        </div>
      </div>
    </div>

    <van-cell-group inset class="m-mine__cells">
      <van-cell title="我的任务" icon="logistics" is-link @click="router.push('/m/courier/tasks')" />
      <van-cell title="车牌号" icon="logistics" :value="summary?.vehicleNo || '未登记'" />
      <van-cell title="退出登录" icon="cross" is-link @click="logout" />
    </van-cell-group>

    <div class="m-mine__tips">生鲜即时配送调度系统 · 配送员端</div>
  </div>
</template>

<script setup lang="ts">
/**
 * 配送员：个人中心 / 工作台
 */
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showConfirmDialog, showToast } from 'vant'
import { getCourierSummary, updateCourierStatus } from '@/api/mobile'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo)

const summary = ref<MobileSummary | null>(null)
const onDuty = ref(true)
const switching = ref(false)

const avatarText = computed(() => {
  const name = summary.value?.personName || userInfo.value?.realname || '配'
  return name.slice(0, 1)
})

const dutyText = computed(() => {
  const status = summary.value?.status
  if (status === 1) return '配送中'
  if (status === 2) return '休息中'
  return '空闲接单'
})

const loadSummary = async () => {
  try {
    const res = await getCourierSummary()
    summary.value = res.data.data
    onDuty.value = res.data.data.status !== 2
  } catch (error) {
    /* 错误已统一提示 */
  }
}

const toggleDuty = async (value: boolean) => {
  switching.value = true
  try {
    await updateCourierStatus(value ? 0 : 2)
    showToast(value ? '已开始接单' : '已切换到休息')
    await loadSummary()
  } catch (error) {
    onDuty.value = !value
  } finally {
    switching.value = false
  }
}

const logout = async () => {
  try {
    await showConfirmDialog({
      title: '退出登录',
      message: '确认退出当前账号吗？',
      confirmButtonText: '退出'
    })
  } catch (e) {
    return
  }
  userStore.clearUserInfo()
  router.replace('/m/login')
}

onMounted(loadSummary)
</script>

<style scoped>
.m-mine__hero {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 16px;
  border-radius: 16px;
  background: linear-gradient(135deg, #16a34a, #0f5132);
  color: #fff;
  margin-bottom: 12px;
}

.m-mine__avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.22);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: 700;
}

.m-mine__name {
  font-size: 18px;
  font-weight: 600;
}

.m-mine__sub {
  margin-top: 6px;
  font-size: 12px;
  opacity: 0.92;
  display: flex;
  align-items: center;
  gap: 8px;
}

.m-mine__role {
  padding: 1px 8px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.22);
}

.m-mine__duty {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.m-mine__stats {
  display: flex;
  margin-top: 6px;
}

.m-mine__stat {
  flex: 1;
  text-align: center;
}

.m-mine__stat-num {
  font-size: 19px;
  font-weight: 700;
  color: #14342b;
}

.m-mine__stat-label {
  font-size: 11px;
  color: #93a49a;
  margin-top: 2px;
}

.m-mine__cells {
  margin-top: 12px;
}

.m-mine__tips {
  text-align: center;
  font-size: 11px;
  color: #b3c0b8;
  margin-top: 24px;
}
</style>
