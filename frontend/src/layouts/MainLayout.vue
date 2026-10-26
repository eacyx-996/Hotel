<template>
  <el-container style="height: 100vh">
    <el-aside width="220px" style="background-color: #304156">
      <div style="color: #fff; text-align: center; padding: 20px 0; font-size: 18px; font-weight: bold; border-bottom: 1px solid #3d4f63">
        🏨 酒店管理
      </div>
      <el-menu
        :default-active="activeMenu"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        router
      >
        <el-menu-item index="/dashboard"><el-icon><DataAnalysis /></el-icon><span>数据统计</span></el-menu-item>
        <el-menu-item index="/room-type"><el-icon><House /></el-icon><span>房型管理</span></el-menu-item>
        <el-menu-item index="/room"><el-icon><OfficeBuilding /></el-icon><span>客房管理</span></el-menu-item>
        <el-menu-item index="/order"><el-icon><Tickets /></el-icon><span>订单管理</span></el-menu-item>
        <el-menu-item index="/user"><el-icon><User /></el-icon><span>用户管理</span></el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header style="background: #fff; display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid #e4e7ed">
        <span style="font-size: 16px; font-weight: bold">{{ currentTitle }}</span>
        <el-dropdown @command="handleCommand">
          <span style="cursor: pointer; display: flex; align-items: center; gap: 8px">
            <el-avatar :size="32" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" />
            {{ userStore.userInfo?.username || '用户' }}
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      <el-main style="background-color: #f5f7fa; padding: 0">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)
const currentTitle = computed(() => route.meta.title || '酒店管理平台')

const handleCommand = async (cmd) => {
  if (cmd === 'logout') {
    await ElMessageBox.confirm('确定要退出登录吗?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  }
}
</script>

<style>
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
