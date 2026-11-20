<script setup lang="ts">
/**
 * 登录页面 - 生鲜即时配送调度系统
 */
import { ref, reactive, toRefs, onMounted } from 'vue'
import { type FormInstance, type FormRules, ElMessage } from 'element-plus'
import { useRouter, useRoute, type Router } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getMd5Info } from '@/utils/md5'

const router: Router = useRouter()
const route = useRoute()
const userStore = useUserStore()

/* 封装登录的表单 */
const loginFormRef = ref<FormInstance | undefined>()

/* 加载状态 */
const loading = ref(false)

/* 封装登录表单的表单项 */
const loginForm = reactive<LoginUser>({
  username: '',
  password: ''
})
const { username, password } = toRefs(loginForm)

/* 封装登录表单的验证规则 */
const loginRules = reactive<FormRules<LoginUser>>({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3-20个字符之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度在6-32个字符之间', trigger: 'blur' }
  ]
})

/* 表单登录 */
const handleLogin = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  
  await formEl.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        /* 调用登录API - 直接发送明文密码，后端使用BCrypt验证 */
        await userStore.login({
          username: username.value,
          password: password.value
        })
        
        ElMessage.success('登录成功！')
        
        /* 获取重定向地址 */
        const redirect = route.query.redirect as string
        router.push(redirect || '/dashboard/home')
      } catch (error: any) {
        console.error('登录失败:', error)
      } finally {
        loading.value = false
      }
    } else {
      ElMessage.warning('请正确填写登录信息')
    }
  })
}

/* 表单重置 */
const handleReset = (formEl: FormInstance | undefined) => {
  if (!formEl) return
  formEl.resetFields()
}

/* 跳转注册页 */
const goRegister = () => {
  router.push({ name: 'register' })
}

/* 回车键登录 */
const handleKeyup = (e: KeyboardEvent) => {
  if (e.key === 'Enter') {
    handleLogin(loginFormRef.value)
  }
}

onMounted(() => {
  /* 添加键盘事件监听 */
  document.addEventListener('keyup', handleKeyup)
})
</script>

<template>
  <div class="login-container">
    <!-- 背景动画 -->
    <div class="login-background">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
      <div class="shape shape-3"></div>
    </div>
    
    <!-- 登录卡片 -->
    <div class="login-card">
      <!-- Logo和标题 -->
      <div class="login-header">
        <el-icon :size="48" color="#16A34A"><Cherry /></el-icon>
        <h1 class="login-title">生鲜即时配送调度系统</h1>
        <p class="login-subtitle">高效调度、智能配送、实时管控</p>
      </div>
      
      <!-- 登录表单 -->
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        size="large"
        status-icon
      >
        <el-form-item prop="username">
          <el-input
            v-model="username"
            placeholder="请输入用户名"
            prefix-icon="User"
            clearable
            autofocus
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            clearable
            show-password
            type="password"
          />
        </el-form-item>
        
        <!-- 记住密码 -->
        <div class="login-options">
          <el-checkbox>记住密码</el-checkbox>
          <el-link type="primary" :underline="false">忘记密码？</el-link>
        </div>
        
        <!-- 登录按钮 -->
        <el-form-item>
          <el-button
            type="primary"
            class="login-btn"
            :loading="loading"
            @click="handleLogin(loginFormRef)"
          >
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form-item>
        
        <!-- 其他操作 -->
        <div class="login-footer">
          <span>还没有账号？</span>
          <el-link type="primary" :underline="false" @click="goRegister">立即注册</el-link>
        </div>
      </el-form>
      
      <!-- 角色提示 -->
      <div class="role-tips">
        <el-divider>测试账号</el-divider>
        <div class="role-list">
          <el-tag type="primary">管理员: admin</el-tag>
          <el-tag type="success">调度员: scheduler</el-tag>
          <el-tag type="warning">配送员: delivery</el-tag>
        </div>
        <p class="role-password">默认密码: 123456</p>
      </div>
    </div>
    
    <!-- 版权信息 -->
    <div class="login-copyright">
      <p>Copyright © 2025 生鲜即时配送调度系统</p>
    </div>
  </div>
</template>

<style scoped lang="scss">
.login-container {
  width: 100%;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  background: linear-gradient(135deg, #0b3a24 0%, #16a34a 55%, #4ade80 100%);
  overflow: hidden;
}

/* 背景动画 */
.login-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  
  .shape {
    position: absolute;
    border-radius: 50%;
    animation: float 6s ease-in-out infinite;
    
    &.shape-1 {
      width: 300px;
      height: 300px;
      background: rgba(255, 255, 255, 0.1);
      top: -100px;
      right: -100px;
      animation-delay: 0s;
    }
    
    &.shape-2 {
      width: 200px;
      height: 200px;
      background: rgba(255, 255, 255, 0.15);
      bottom: -50px;
      left: -50px;
      animation-delay: 2s;
    }
    
    &.shape-3 {
      width: 150px;
      height: 150px;
      background: rgba(255, 255, 255, 0.1);
      top: 50%;
      left: 20%;
      animation-delay: 4s;
    }
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
  }
  50% {
    transform: translateY(-20px) rotate(10deg);
  }
}

/* 登录卡片 */
.login-card {
  width: 420px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.96);
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(6, 46, 30, 0.28);
  backdrop-filter: blur(10px);
  z-index: 1;
}

/* 登录头部 */
.login-header {
  text-align: center;
  margin-bottom: 30px;
  
  .login-title {
    margin: 16px 0 8px;
    font-size: 24px;
    font-weight: 600;
    color: #14342b;
  }
  
  .login-subtitle {
    font-size: 14px;
    color: #909399;
  }
}

/* 登录表单 */
.login-form {
  :deep(.el-input__wrapper) {
    padding: 8px 12px;
  }
  
  .login-options {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
  }
  
  .login-btn {
    width: 100%;
    height: 44px;
    font-size: 16px;
  }
}

/* 登录底部 */
.login-footer {
  text-align: center;
  color: #909399;
  
  .el-link {
    margin-left: 4px;
  }
}

/* 角色提示 */
.role-tips {
  margin-top: 24px;
  
  :deep(.el-divider__text) {
    font-size: 12px;
    color: #909399;
  }
  
  .role-list {
    display: flex;
    justify-content: center;
    gap: 8px;
    margin-bottom: 8px;
    
    .el-tag {
      font-size: 12px;
    }
  }
  
  .role-password {
    text-align: center;
    font-size: 12px;
    color: #909399;
  }
}

/* 版权信息 */
.login-copyright {
  position: absolute;
  bottom: 20px;
  text-align: center;
  
  p {
    color: rgba(255, 255, 255, 0.8);
    font-size: 12px;
  }
}

/* 响应式适配 */
@media screen and (max-width: 480px) {
  .login-card {
    width: 90%;
    padding: 30px 20px;
  }
}
</style>