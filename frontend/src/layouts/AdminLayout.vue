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
  background: #f0ede6;
}

.admin-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 56px;
  padding: 0 24px;
  background: var(--color-text);
  border-bottom: 2px solid #1a1c2e;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 14px;
}

.menu-toggle {
  background: none;
  border: 2px solid #3d4165;
  border-radius: 2px;
  font-size: 16px;
  cursor: pointer;
  color: #9ca3af;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: border-color 0.1s, color 0.1s;
}

.menu-toggle:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.brand {
  font-size: 16px;
  font-weight: 800;
  color: var(--color-primary);
  text-decoration: none;
  letter-spacing: 0.5px;
}

.admin-info {
  display: flex;
  align-items: center;
  gap: 14px;
}

.admin-name {
  font-size: 13px;
  font-weight: 600;
  color: #9ca3af;
}

.logout-btn {
  padding: 5px 14px;
  background: transparent;
  color: var(--color-danger);
  border: 2px solid var(--color-danger);
  border-radius: 2px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 700;
  box-shadow: 2px 2px 0 #7a3025;
  transition: box-shadow 0.1s, transform 0.1s;
}

.logout-btn:hover {
  box-shadow: 1px 1px 0 #7a3025;
  transform: translate(1px, 1px);
}

.admin-body { display: flex; }

.sidebar {
  width: 220px;
  background: var(--color-surface);
  border-right: var(--border-pixel);
  transition: width 0.2s;
  min-height: calc(100vh - 56px);
  flex-shrink: 0;
}

.sidebar.collapsed { width: 60px; }

.sidebar-nav { padding: 16px 0; }

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 18px;
  color: var(--color-text-sub);
  text-decoration: none;
  font-size: 14px;
  font-weight: 600;
  border-left: 3px solid transparent;
  transition: background 0.1s, color 0.1s, border-color 0.1s;
}

.nav-item:hover {
  background: #f0ede6;
  color: var(--color-text);
}

.nav-item.router-link-active {
  background: #f0ede6;
  color: var(--color-text);
  border-left-color: var(--color-primary);
  font-weight: 800;
}

.nav-icon { font-size: 18px; flex-shrink: 0; }

.sidebar.collapsed .nav-text { display: none; }

.admin-main {
  flex: 1;
  padding: 24px;
  min-width: 0;
}
</style>