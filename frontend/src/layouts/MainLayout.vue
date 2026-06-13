<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter, type RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const isCollapse = ref(false)
const activeMenu = computed(() => route.path)

interface MenuItem {
  path: string
  name: string
  title: string
  icon: string
  roles?: string[]
}

const menuItems = computed<MenuItem[]>(() => {
  const items: MenuItem[] = []

  const homeRoute = router.options.routes.find((r) => r.path === '/')
  if (homeRoute && homeRoute.children) {
    homeRoute.children.forEach((child: RouteRecordRaw) => {
      const roles = child.meta?.roles as string[] | undefined
      if (roles && roles.length > 0) {
        if (!authStore.hasAnyRole(roles)) return
      }
      items.push({
        path: `/${child.path}`,
        name: child.name as string,
        title: child.meta?.title as string,
        icon: child.meta?.icon as string,
        roles: roles
      })
    })
  }
  return items
})

function handleMenuClick(path: string) {
  router.push(path)
}

function handleLogout() {
  authStore.logout()
}

const iconMap: Record<string, string> = {
  'DataAnalysis': 'DataAnalysis',
  'UserFilled': 'UserFilled',
  'Avatar': 'Avatar',
  'OfficeBuilding': 'OfficeBuilding',
  'HomeFilled': 'HomeFilled',
  'AlarmClock': 'AlarmClock',
  'WarningFilled': 'WarningFilled',
  'Monitor': 'Monitor',
  'Warning': 'Warning',
  'User': 'User',
  'FirstAidKit': 'FirstAidKit',
  'Calendar': 'Calendar',
  'Date': 'Calendar',
  'Management': 'Management',
  'Setting': 'Setting',
  'Document': 'Document'
}
</script>

<template>
  <el-container class="main-container">
    <el-aside :width="isCollapse ? '64px' : '220px'" class="sidebar">
      <div class="logo-container">
        <img src="/vite.svg" alt="logo" class="logo-img" />
        <span v-show="!isCollapse" class="logo-title">监狱管理平台</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :collapse-transition="false"
        background-color="#001529"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        @select="handleMenuClick"
      >
        <el-menu-item
          v-for="item in menuItems"
          :key="item.path"
          :index="item.path"
        >
          <el-icon><component :is="iconMap[item.icon]" /></el-icon>
          <template #title>{{ item.title }}</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-icon
            class="collapse-btn"
            :size="22"
            @click="isCollapse = !isCollapse"
          >
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <span class="header-title">监狱综合管理平台</span>
        </div>
        <div class="header-right">
          <el-dropdown trigger="click">
            <div class="user-info">
              <el-avatar :size="32" icon="UserFilled" />
              <span class="username">{{ authStore.userInfo?.realName || '管理员' }}</span>
              <span class="role-tag">{{ authStore.userInfo?.role || '管理员' }}</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item disabled>
                  {{ authStore.userInfo?.username }}
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.main-container {
  height: 100vh;
}

.sidebar {
  background-color: #001529;
  overflow: hidden;
  transition: width 0.3s;
}

.logo-container {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 16px;
  background-color: #001529;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo-img {
  width: 32px;
  height: 32px;
  flex-shrink: 0;
}

.logo-title {
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  margin-left: 10px;
  white-space: nowrap;
  overflow: hidden;
}

.el-menu {
  border-right: none;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  height: 60px;
}

.header-left {
  display: flex;
  align-items: center;
}

.collapse-btn {
  cursor: pointer;
  margin-right: 16px;
  color: #304156;
}

.collapse-btn:hover {
  color: #409eff;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
  color: #304156;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background 0.2s;
}

.user-info:hover {
  background: #f5f7fa;
}

.username {
  margin: 0 8px;
  font-size: 14px;
  color: #304156;
}

.role-tag {
  font-size: 12px;
  color: #909399;
  background: #f0f2f5;
  padding: 2px 8px;
  border-radius: 10px;
}

.main-content {
  background: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}
</style>