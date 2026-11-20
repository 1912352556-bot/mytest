<script setup lang="ts">
import { ElMessage } from 'element-plus';
import { useRoute, useRouter, type Router } from 'vue-router'
import { useUserInfoStore } from '@/stores/useUserInfoStore';

const userStore = useUserInfoStore()
const currentUser = userStore.getUserInfo()
const router: Router = useRouter()
const route = useRoute()
const quit = () => {
    userStore.removeUserInfo()
    /* 激活登录路由组件 */
    router.push('/')
}
</script>

<template>
    <div class="100vh 100vw flex-center">
        <el-container class="flex-center">
            <el-header class="header flex-space-between header-bgColor" height="7vh" style="width: 100%;">
                <div class="flex pl75" style="justify-content: center;">
                    <img src="../../assets/etoak_logo.png" style="height: 40px;">
                    <h1 style="color: #f5f5f5;" class="text-shadow">XX网</h1>
                </div>
                <!-- <div style="height: 100%;">
                    <el-menu active-text-color="#B21821"
                        style="height: 100%; background-color:#2D3A4B; display: flex; justify-content: center;"
                        :default-active="route.path ? route.path : ''" text-color="#f5f5f5" :router="true"
                        mode="horizontal" :ellipsis="false">

                        <el-menu-item index="/layout/buy" style="height: 100%;"><h2>购票首页</h2></el-menu-item>
                        <el-menu-item v-show="currentUser.role" index="/layout/control" style="height: 100%;"><h2>电影场次管理</h2></el-menu-item>
                    </el-menu>
                </div> -->
                <div class="flex" style="color: antiquewhite;">
                    <h2 class="mr40 text-shadow">欢迎您回来{{ currentUser.username }}</h2>
                    <el-dropdown>
                        <!-- 这是下拉菜单的标题 -->
                        <span class="el-dropdown-link flex-center text-shadow" style="color: antiquewhite;">
                            <h2>菜单</h2>
                            <el-icon class="el-icon--right">
                                <arrow-down />
                            </el-icon>
                        </span>
                        <!-- 此处为下拉菜单列表项 -->
                        <template #dropdown>
                            <el-dropdown-menu>
                                <!-- :icon 绑定图标 -->

                                <el-dropdown-item @click="quit">退出登录</el-dropdown-item>
                            </el-dropdown-menu>
                        </template>
                    </el-dropdown>
                </div>
            </el-header>
            <el-main style="width: 90%;"><router-view></router-view></el-main>
        </el-container>
    </div>
</template>

<style scoped>




:deep(.el-menu-item:hover) {
    color: #ff9900 !important;
    /*  hover 时的字体颜色，可自定义 */

}
</style>