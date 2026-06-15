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
              <el-avatar
                :size="32"
                :src="authStore.userInfo?.avatar || ''"
                :icon="!authStore.userInfo?.avatar ? 'UserFilled' : undefined"
              />
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
  background: var(--color-bg);
}

.user-header {
  display: flex;
  align-items: center;
  height: 60px;
  padding: 0 40px;
  background: var(--color-surface);
  border-bottom: var(--border-pixel);
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 2px 0 var(--color-border);
}

.brand {
  font-size: 20px;
  font-weight: 800;
  color: var(--color-text);
  margin-right: 40px;
  text-decoration: none;
  letter-spacing: -0.5px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.brand::before {
  content: '⚔';
  font-size: 18px;
}

.nav {
  display: flex;
  gap: 4px;
  flex: 1;
}

.nav a {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-sub);
  text-decoration: none;
  padding: 6px 14px;
  border: 2px solid transparent;
  border-radius: var(--radius-pixel);
  transition: all 0.1s;
}

.nav a:hover {
  color: var(--color-text);
  background: #f0ede6;
  border-color: var(--color-border);
}

.nav a.router-link-active {
  color: var(--color-text);
  background: var(--color-primary);
  border-color: var(--color-border);
  box-shadow: var(--shadow-hard-sm);
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
  padding: 5px 12px;
  border: var(--border-pixel);
  border-radius: var(--radius-pixel);
  background: var(--color-surface);
  box-shadow: var(--shadow-hard-sm);
  transition: box-shadow 0.1s, transform 0.1s;
}

.user-avatar:hover {
  box-shadow: 1px 1px 0 var(--color-border);
  transform: translate(1px, 1px);
}

.username {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text);
}
</style>
