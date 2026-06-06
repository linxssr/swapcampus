<template>
  <div class="admin-layout">
    <aside class="sidebar">
      <div class="brand">SwapCampus Admin</div>
      <RouterLink to="/admin/dashboard">数据看板</RouterLink>
      <RouterLink to="/admin/items">商品审核</RouterLink>
      <RouterLink to="/admin/users">用户管理</RouterLink>
      <RouterLink to="/admin/reports">举报处理</RouterLink>
      <RouterLink to="/admin/categories">分类管理</RouterLink>
    </aside>
    <main class="content">
      <RouterView />
    </main>
  </div>
</template>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
}

.sidebar {
  display: flex;
  flex-direction: column;
  gap: 16px;
  width: 240px;
  padding: 24px;
  color: #e5e7eb;
  background: #111827;
}

.brand {
  margin-bottom: 24px;
  font-size: 18px;
  font-weight: 700;
  color: #ffffff;
}

.content {
  flex: 1;
  padding: 32px;
}
</style>
