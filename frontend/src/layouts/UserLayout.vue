<template>
  <div class="user-layout">
    <header class="user-header">
      <RouterLink class="brand" to="/">SwapCampus</RouterLink>
      <nav class="nav">
        <RouterLink to="/items">商品检索</RouterLink>
        <RouterLink to="/publish">发布闲置</RouterLink>
        <RouterLink to="/chat">聊天</RouterLink>
        <RouterLink to="/profile">个人中心</RouterLink>
      </nav>
    </header>
    <main>
      <RouterView />
    </main>
  </div>
</template>

<style scoped>
.user-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
  padding: 0 40px;
  background: #ffffff;
  border-bottom: 1px solid #e5e7eb;
}

.brand {
  font-size: 22px;
  font-weight: 700;
  color: #2563eb;
}

.nav {
  display: flex;
  gap: 24px;
  color: #4b5563;
}
</style>
