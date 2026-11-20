<template>
  <div class="m-app">
    <div class="m-app__body">
      <router-view />
    </div>
    <van-tabbar v-model="active" route :border="false" fixed placeholder safe-area-inset-bottom>
      <van-tabbar-item v-for="tab in tabs" :key="tab.path" :to="tab.path" :icon="tab.icon">
        {{ tab.label }}
      </van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup lang="ts">
/**
 * 移动端布局：底部 TabBar 按角色切换
 * 客户：首页 / 订单 / 我的    配送员：配送任务 / 我的
 */
import { computed, ref } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const active = ref(0)
const role = computed(() => userStore.userInfo?.role)

const tabs = computed(() => {
  if (role.value === 2) {
    return [
      { path: '/m/courier/tasks', label: '配送任务', icon: 'logistics' },
      { path: '/m/courier/mine', label: '我的', icon: 'user-o' }
    ]
  }
  return [
    { path: '/m/customer/home', label: '首页', icon: 'shop-o' },
    { path: '/m/customer/orders', label: '订单', icon: 'orders-o' },
    { path: '/m/customer/mine', label: '我的', icon: 'user-o' }
  ]
})
</script>

<style scoped>
.m-app {
  min-height: 100vh;
}
</style>
