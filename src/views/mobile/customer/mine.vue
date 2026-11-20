<template>
  <div class="m-page">
    <van-nav-bar title="我的" :border="false" fixed placeholder />

    <!-- 用户信息 -->
    <div class="m-mine__hero">
      <div class="m-mine__avatar">{{ avatarText }}</div>
      <div class="m-mine__info">
        <div class="m-mine__name">{{ profile?.realname || userInfo?.realname || '客户' }}</div>
        <div class="m-mine__sub">
          <span>{{ profile?.phone || userInfo?.phone || '未绑定手机号' }}</span>
          <span class="m-mine__role">{{ profile?.roleName || '客户' }}</span>
        </div>
      </div>
    </div>

    <van-cell-group inset class="m-mine__cells">
      <van-cell title="我的订单" icon="orders-o" is-link @click="router.push('/m/customer/orders')" />
      <van-cell title="去下单" icon="shop-o" is-link @click="router.push('/m/customer/home')" />
      <van-cell title="联系客服" icon="service-o" is-link value="400-000-0000" @click="callService" />
    </van-cell-group>

    <van-cell-group inset class="m-mine__cells">
      <van-cell title="账号" :value="profile?.username || userInfo?.username" />
      <van-cell title="退出登录" icon="cross" is-link @click="logout" />
    </van-cell-group>

    <div class="m-mine__tips">生鲜即时配送调度系统 · 移动端</div>
  </div>
</template>

<script setup lang="ts">
/**
 * 客户：个人中心
 */
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showConfirmDialog } from 'vant'
import { getMobileProfile } from '@/api/mobile'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const profile = ref<MobileProfile | null>(null)

const userInfo = computed(() => userStore.userInfo)

const avatarText = computed(() => {
  const name = profile.value?.realname || userInfo.value?.realname || '客'
  return name.slice(0, 1)
})

const loadProfile = async () => {
  try {
    const res = await getMobileProfile()
    profile.value = res.data.data
  } catch (error) {
    /* 忽略，使用本地缓存信息兜底 */
  }
}

const callService = () => {
  window.location.href = 'tel:4000000000'
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

onMounted(loadProfile)
</script>

<style scoped>
.m-mine__hero {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 16px;
  border-radius: 16px;
  background: linear-gradient(135deg, #16a34a, #22c55e);
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

.m-mine__cells {
  margin-bottom: 12px;
}

.m-mine__tips {
  text-align: center;
  font-size: 11px;
  color: #b3c0b8;
  margin-top: 24px;
}
</style>
