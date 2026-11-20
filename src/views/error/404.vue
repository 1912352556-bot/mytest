<template>
  <div class="error-page">
    <div class="error-content">
      <div class="error-code">404</div>
      <div class="error-title">页面未找到</div>
      <div class="error-description">
        抱歉，您访问的页面不存在或已被移除
      </div>
      <div class="error-actions">
        <el-button type="primary" @click="goHome">
          <el-icon><HomeFilled /></el-icon>
          返回首页
        </el-button>
        <el-button @click="goBack">
          <el-icon><Back /></el-icon>
          返回上页
        </el-button>
      </div>
    </div>
    <div class="error-illustration">
      <svg viewBox="0 0 400 300" xmlns="http://www.w3.org/2000/svg">
        <defs>
          <linearGradient id="grad1" x1="0%" y1="0%" x2="100%" y2="100%">
            <stop offset="0%" style="stop-color:#16A34A;stop-opacity:1" />
            <stop offset="100%" style="stop-color:#86EFAC;stop-opacity:1" />
          </linearGradient>
        </defs>
        <rect x="50" y="50" width="300" height="200" rx="10" fill="#f5f7fa" stroke="#dcdfe6" stroke-width="2"/>
        <circle cx="100" cy="80" r="8" fill="#f56c6c"/>
        <circle cx="130" cy="80" r="8" fill="#e6a23c"/>
        <circle cx="160" cy="80" r="8" fill="#67c23a"/>
        <line x1="50" y1="100" x2="350" y2="100" stroke="#dcdfe6" stroke-width="2"/>
        <text x="200" y="160" text-anchor="middle" font-size="48" font-weight="bold" fill="url(#grad1)">404</text>
        <text x="200" y="200" text-anchor="middle" font-size="16" fill="#909399">Page Not Found</text>
        <path d="M150 220 Q200 250 250 220" stroke="#dcdfe6" stroke-width="3" fill="none"/>
      </svg>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { HomeFilled, Back } from '@element-plus/icons-vue'

const router = useRouter()

const goHome = () => {
  router.push('/')
}

const goBack = () => {
  router.back()
}
</script>

<style scoped lang="scss">
.error-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #0f5132 0%, #16a34a 60%, #4ade80 100%);
  padding: 20px;
}

.error-content {
  text-align: center;
  color: #fff;
  margin-bottom: 40px;
}

.error-code {
  font-size: 120px;
  font-weight: bold;
  line-height: 1;
  text-shadow: 4px 4px 0 rgba(0, 0, 0, 0.2);
  animation: bounce 1s ease-in-out infinite alternate;
}

@keyframes bounce {
  from {
    transform: translateY(0);
  }
  to {
    transform: translateY(-10px);
  }
}

.error-title {
  font-size: 32px;
  margin: 20px 0 10px;
  font-weight: 500;
}

.error-description {
  font-size: 16px;
  opacity: 0.8;
  margin-bottom: 30px;
}

.error-actions {
  display: flex;
  gap: 15px;
  justify-content: center;

  .el-button {
    padding: 12px 30px;
    font-size: 16px;
    border-radius: 25px;

    .el-icon {
      margin-right: 8px;
    }
  }
}

.error-illustration {
  width: 400px;
  max-width: 100%;

  svg {
    width: 100%;
    height: auto;
    filter: drop-shadow(0 10px 30px rgba(0, 0, 0, 0.2));
  }
}

@media (max-width: 768px) {
  .error-code {
    font-size: 80px;
  }

  .error-title {
    font-size: 24px;
  }

  .error-illustration {
    width: 300px;
  }

  .error-actions {
    flex-direction: column;
  }
}
</style>
