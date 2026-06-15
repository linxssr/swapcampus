<template>
  <div class="admin-layout">
    <!-- 顶部导航 -->
    <header class="admin-header">
      <div class="header-left">
        <button class="menu-toggle" @click="sidebarCollapsed = !sidebarCollapsed">
          ☰
        </button>
        <RouterLink class="brand" to="/admin/dashboard">SwapCampus 管理后台</RouterLink>
      </div>
      <div class="admin-info">
        <span class="admin-name">{{ adminAuthStore.adminInfo?.realName || '管理员' }}</span>
        <button class="logout-btn" @click="handleLogout">退出</button>
      </div>
    </header>

    <div class="admin-body">
      <!-- 侧边栏 -->
      <aside class="sidebar" :class="{ collapsed: sidebarCollapsed }">
        <nav class="sidebar-nav">
          <RouterLink to="/admin/dashboard" class="nav-item">
            <span class="nav-icon">📊</span>
            <span class="nav-text">仪表盘</span>
          </RouterLink>
          <RouterLink to="/admin/item/audit" class="nav-item">
            <span class="nav-icon">🛒</span>
            <span class="nav-text">商品审核</span>
          </RouterLink>
          <RouterLink to="/admin/user/manage" class="nav-item">
            <span class="nav-icon">👥</span>
            <span class="nav-text">用户管理</span>
          </RouterLink>
          <RouterLink to="/admin/report/list" class="nav-item">
            <span class="nav-icon">⚠️</span>
            <span class="nav-text">举报管理</span>
          </RouterLink>
          <RouterLink to="/admin/category/manage" class="nav-item">
            <span class="nav-icon">📁</span>
            <span class="nav-text">分类管理</span>
          </RouterLink>
          <RouterLink to="/admin/order/list" class="nav-item">
            <span class="nav-icon">📋</span>
            <span class="nav-text">订单管理</span>
          </RouterLink>
        </nav>
      </aside>

      <!-- 主内容区 -->
      <main class="admin-main">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAdminAuthStore } from '@/stores/modules/adminAuth'

const router = useRouter()
const adminAuthStore = useAdminAuthStore()
const sidebarCollapsed = ref(false)

const handleLogout = () => {
  adminAuthStore.logout()
  router.push('/admin/login')
}
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
  background: #f5f7fa;
}

/* 顶部导航 */
.admin-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
  padding: 0 24px;
  background: #ffffff;
  border-bottom: 1px solid #e5e7eb;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.menu-toggle {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: #6b7280;
}

.brand {
  font-size: 20px;
  font-weight: 700;
  color: #2563eb;
  text-decoration: none;
}

.admin-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.admin-name {
  font-size: 14px;
  color: #374151;
}

.logout-btn {
  padding: 6px 16px;
  background: #fee2e2;
  color: #dc2626;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}

/* 侧边栏 */
.admin-body {
  display: flex;
}

.sidebar {
  width: 240px;
  background: white;
  border-right: 1px solid #e5e7eb;
  transition: width 0.3s;
  min-height: calc(100vh - 64px);
}

.sidebar.collapsed {
  width: 70px;
}

.sidebar-nav {
  padding: 20px 0;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  color: #4b5563;
  text-decoration: none;
  transition: all 0.3s;
}

.nav-item:hover {
  background: #f3f4f6;
  color: #2563eb;
}

.nav-item.router-link-active {
  background: #eff6ff;
  color: #2563eb;
  border-right: 3px solid #2563eb;
}

.nav-icon {
  font-size: 20px;
}

.sidebar.collapsed .nav-text {
  display: none;
}

/* 主内容 */
.admin-main {
  flex: 1;
  padding: 24px;
}
</style>