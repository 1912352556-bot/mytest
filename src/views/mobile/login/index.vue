<template>
  <div class="m-login">
    <div class="m-login__hero">
      <div class="m-login__logo">🥬</div>
      <h1 class="m-login__title">生鲜即时配送</h1>
      <p class="m-login__subtitle">客户下单 · 配送员接单</p>
    </div>

    <van-form class="m-login__form" @submit="onSubmit">
      <van-cell-group inset>
        <van-field
          v-model="form.username"
          name="username"
          label="账号"
          placeholder="请输入账号"
          clearable
          :rules="[{ required: true, message: '请输入账号' }]"
        />
        <van-field
          v-model="form.password"
          type="password"
          name="password"
          label="密码"
          placeholder="请输入密码"
          :rules="[{ required: true, message: '请输入密码' }]"
        />
      </van-cell-group>

      <div class="m-login__actions">
        <van-button round block type="primary" native-type="submit" :loading="loading">
          登 录
        </van-button>
      </div>
    </van-form>

    <div class="m-login__demo">
      <div class="m-login__demo-title">演示账号（密码 123456）</div>
      <div class="m-login__demo-list">
        <van-tag plain type="primary" size="large" @click="fillDemo('customer01')">
          客户 customer01
        </van-tag>
        <van-tag plain type="success" size="large" @click="fillDemo('delivery01')">
          配送员 delivery01
        </van-tag>
      </div>
      <div class="m-login__tips">
        管理员 / 调度员请使用电脑端登录
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 移动端登录：复用 PC 端 /user/login 接口，按角色进入对应首页
 */
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const fillDemo = (username: string) => {
  form.username = username
  form.password = '123456'
}

const onSubmit = async () => {
  loading.value = true
  try {
    const user = await userStore.login({
      username: form.username.trim(),
      password: form.password
    })
    if (user.role === 3) {
      router.replace('/m/customer/home')
    } else if (user.role === 2) {
      router.replace('/m/courier/tasks')
    } else {
      showToast('该账号请使用电脑端登录')
      router.replace('/dashboard/home')
    }
  } catch (error) {
    /* 具体错误已由请求拦截器统一提示 */
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.m-login {
  min-height: 100vh;
  background: linear-gradient(180deg, #16a34a 0%, #22c55e 32%, #f4f8f5 32%, #f4f8f5 100%);
  padding: 0 16px 32px;
}

.m-login__hero {
  padding: 56px 0 32px;
  text-align: center;
  color: #fff;
}

.m-login__logo {
  width: 64px;
  height: 64px;
  margin: 0 auto 12px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.18);
  font-size: 32px;
  line-height: 64px;
  backdrop-filter: blur(4px);
}

.m-login__title {
  font-size: 22px;
  font-weight: 700;
  letter-spacing: 2px;
}

.m-login__subtitle {
  margin-top: 6px;
  font-size: 13px;
  opacity: 0.9;
}

.m-login__form {
  margin-top: 8px;
  background: #fff;
  border-radius: 16px;
  padding: 8px 0 16px;
  box-shadow: 0 8px 24px rgba(20, 52, 43, 0.08);
}

.m-login__actions {
  padding: 16px 16px 0;
}

.m-login__demo {
  margin-top: 20px;
  text-align: center;
}

.m-login__demo-title {
  color: #6b7f73;
  font-size: 12px;
  margin-bottom: 10px;
}

.m-login__demo-list {
  display: flex;
  justify-content: center;
  gap: 12px;
}

.m-login__tips {
  margin-top: 18px;
  font-size: 12px;
  color: #9aa8a0;
}
</style>
