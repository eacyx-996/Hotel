<template>
  <el-container class="layout">
    <el-aside width="220px" class="sidebar">
      <div class="logo">
        <span class="logo-icon">🏨</span>
        <span class="logo-text">酒店管理</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        background-color="transparent"
        text-color="#a6adb8"
        active-text-color="#fff"
        router
        class="side-menu"
      >
        <el-menu-item index="/dashboard"><el-icon><DataAnalysis /></el-icon><span>数据统计</span></el-menu-item>
        <el-menu-item index="/room-type"><el-icon><House /></el-icon><span>房型管理</span></el-menu-item>
        <el-menu-item index="/room"><el-icon><OfficeBuilding /></el-icon><span>客房管理</span></el-menu-item>
        <el-menu-item index="/order"><el-icon><Tickets /></el-icon><span>订单管理</span></el-menu-item>
        <el-menu-item index="/user"><el-icon><User /></el-icon><span>用户管理</span></el-menu-item>
      </el-menu>
      <div class="sidebar-footer">酒店管理 v1.0</div>
    </el-aside>
    <el-container>
      <el-header class="topbar" height="60px">
        <span class="page-title">{{ currentTitle }}</span>
        <el-dropdown @command="handleCommand">
          <span class="user-chip">
            <el-avatar :size="32" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" />
            <span class="username">{{ userStore.userInfo?.realName || userStore.userInfo?.username || '用户' }}</span>
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      <el-main class="main-content">
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

<style scoped>
.layout { height: 100vh; }

.sidebar {
  background: linear-gradient(180deg, #001529 0%, #0b2545 100%);
  display: flex;
  flex-direction: column;
}

.logo {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 20px 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.logo-icon { font-size: 24px; }

.logo-text {
  color: #fff;
  font-size: 18px;
  font-weight: 600;
  letter-spacing: 2px;
}

.side-menu {
  border-right: none;
  flex: 1;
  padding: 8px 10px;
}

.side-menu :deep(.el-menu-item) {
  border-radius: 8px;
  margin-bottom: 4px;
  height: 46px;
}

.side-menu :deep(.el-menu-item:hover) {
  background-color: rgba(255, 255, 255, 0.08);
}

.side-menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, #409eff, #3375b9);
  color: #fff;
}

.sidebar-footer {
  padding: 14px;
  text-align: center;
  color: rgba(255, 255, 255, 0.3);
  font-size: 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}

.topbar {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  z-index: 1;
}

.page-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.user-chip {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 10px;
  border-radius: 20px;
  transition: background 0.2s;
}

.user-chip:hover { background: #f5f7fa; }

.username { font-size: 14px; color: #303133; }

.main-content {
  background-color: #f0f2f5;
  padding: 0;
}

.fade-enter-active, .fade-leave-active { transition: opacity 0.2s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
