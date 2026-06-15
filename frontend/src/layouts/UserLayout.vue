<template>
  <div class="user-layout">
    <header class="user-header">
      <RouterLink class="brand" to="/">SwapCampus</RouterLink>
      <nav class="nav">
        <RouterLink to="/">首页</RouterLink>
        <RouterLink to="/items">商品检索</RouterLink>
        <RouterLink to="/publish">发布闲置</RouterLink>
        <RouterLink to="/chat">聊天</RouterLink>
        <RouterLink to="/orders">我的订单</RouterLink>
      </nav>
      <div class="header-actions">
        <template v-if="authStore.isLoggedIn">
          <el-dropdown @command="handleUserCommand">
            <div class="user-avatar">
              <el-avatar :size="32" icon="UserFilled" />
              <span class="username">{{ username }}</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button size="small" @click="router.push('/login')">登录</el-button>
          <el-button size="small" type="primary" @click="router.push('/register')">注册</el-button>
        </template>
      </div>
    </header>
    <main>
      <RouterView />
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/modules/auth';

const router = useRouter();
const authStore = useAuthStore();
const username = computed(() => authStore.displayName || '用户');

function handleUserCommand(cmd: string) {
  if (cmd === 'profile') {
    router.push('/profile');
  } else if (cmd === 'logout') {
    authStore.clearAuth();
    router.push('/login');
  }
}
</script>

<style scoped>
.user-layout {
  min-height: 100vh;
  background: #f5f7fb;
}

.user-header {
  display: flex;
  align-items: center;
  height: 64px;
  padding: 0 40px;
  background: #ffffff;
  border-bottom: 1px solid #e5e7eb;
  position: sticky;
  top: 0;
  z-index: 100;
}

.brand {
  font-size: 22px;
  font-weight: 700;
  color: #2563eb;
  margin-right: 40px;
  text-decoration: none;
}

.nav {
  display: flex;
  gap: 24px;
  flex: 1;
}

.nav a {
  font-size: 15px;
  color: #4b5563;
  text-decoration: none;
  transition: color 0.2s;
}

.nav a:hover,
.nav a.router-link-active {
  color: #2563eb;
  font-weight: 600;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-avatar {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 8px;
  transition: background 0.15s;
}

.user-avatar:hover {
  background: #f3f4f6;
}

.username {
  font-size: 14px;
  color: #374151;
}
</style>
