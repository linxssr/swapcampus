<template>
  <div class="home-page">
    <div class="hero-banner">
      <div class="hero-content">
        <div class="hero-brand">
          <img src="@/assets/images/闲鸟.png" alt="闲鸟" class="hero-mascot" />
          <h1 class="hero-title">闲鸟</h1>
        </div>
        <p class="hero-subtitle">安全便捷的校内二手交易平台</p>
        <div class="search-bar">
          <el-input
            v-model="searchKey"
            placeholder="搜索你想要的商品..."
            size="large"
            class="search-input"
            @keyup.enter="handleSearch"
          >
            <template #append>
              <el-button :icon="Search" @click="handleSearch" />
            </template>
          </el-input>
        </div>
      </div>
    </div>

    <div class="section">
      <div class="section-header">
        <h2 class="section-title">商品分类</h2>
      </div>
      <div class="category-grid">
        <div
          v-for="cat in categoryList"
          :key="cat.categoryId"
          class="category-card"
          @click="router.push(`/items?categoryId=${cat.categoryId}`)"
        >
          <div class="category-icon">{{ categoryEmoji(cat.categoryName) }}</div>
          <div class="category-name">{{ cat.categoryName }}</div>
        </div>
      </div>
    </div>

    <div class="section">
      <div class="section-header">
        <h2 class="section-title">最新发布</h2>
        <el-button type="primary" text @click="router.push('/items')">
          查看更多
          <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>

      <div v-if="itemsLoading" class="loading-grid">
        <el-skeleton v-for="i in 8" :key="i" animated class="skeleton-card" />
      </div>
      <div v-else-if="recentItems.length === 0" class="empty-state">
        <el-empty description="暂无商品，去发布第一件闲置吧" />
      </div>
      <div v-else class="item-grid">
        <div
          v-for="item in recentItems"
          :key="item.itemId"
          class="item-card"
          @click="router.push(`/items/${item.itemId}`)"
        >
          <div class="item-cover">
            <el-image :src="item.coverUrl" fit="cover" class="cover-img" />
            <div v-if="item.auditStatus !== 1" class="audit-overlay">
              <el-tag :type="item.auditStatus === 0 ? 'warning' : 'danger'" size="small">
                {{ item.auditStatusDesc }}
              </el-tag>
            </div>
          </div>
          <div class="item-info">
            <div class="item-title">{{ item.title }}</div>
            <div class="item-bottom">
              <span class="item-price">¥{{ item.price }}</span>
              <span class="item-quality">{{ item.qualityDesc }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ArrowRight, Search } from '@element-plus/icons-vue';
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { getCategoryList } from '@/api/modules/category';
import { filterItems } from '@/api/modules/item';
import type { CategoryVO, ItemVO } from '@/types/item';

const router = useRouter();
const searchKey = ref('');
const itemsLoading = ref(false);
const categoryList = ref<CategoryVO[]>([]);
const recentItems = ref<ItemVO[]>([]);

function categoryEmoji(name: string): string {
  const map: Record<string, string> = {
    '数码电子': '📱',
    '数码电子产品': '📱',
    '书籍教材': '📚',
    '教材书籍教辅': '📚',
    '二手书': '📖',
    '生活用品': '🛒',
    '日用家居用品': '🏠',
    '服装鞋包': '👕',
    '服饰鞋帽穿搭': '👕',
    '运动户外': '⚽',
    '运动健身器材': '🏋️',
    '美妆护肤饰品': '💄',
    '手工制品': '🎨',
    '手工材料包': '🧵',
    '周边': '🎮',
    '其他': '📦',
  };
  return map[name] || '📦';
}

async function loadCategories() {
  try {
    const res = await getCategoryList();
    if (res.data) {
      categoryList.value = res.data;
    }
  } catch {
    // 静默失败，首页分类加载失败不影响使用
  }
}

async function loadRecentItems() {
  itemsLoading.value = true;
  try {
    const res = await filterItems({});
    const all = res.data ?? [];
    all.sort((a, b) => new Date(b.createTime).getTime() - new Date(a.createTime).getTime());
    recentItems.value = all.slice(0, 8);
  } catch {
    recentItems.value = [];
  } finally {
    itemsLoading.value = false;
  }
}

function handleSearch() {
  if (!searchKey.value.trim()) {
    ElMessage.warning('请输入搜索关键词');
    return;
  }
  router.push({ path: '/items', query: { key: searchKey.value } });
}

onMounted(async () => {
  await loadCategories();
  await loadRecentItems();
});
</script>

<style scoped>
.hero-banner {
  background: var(--color-primary);
  border-bottom: var(--border-pixel);
  padding: 56px 0;
  margin-bottom: 32px;
  position: relative;
  overflow: hidden;
}

.hero-banner::before {
  content: '';
  position: absolute;
  inset: 0;
  background-image:
    repeating-linear-gradient(0deg, transparent, transparent 31px, rgba(45,48,71,0.06) 31px, rgba(45,48,71,0.06) 32px),
    repeating-linear-gradient(90deg, transparent, transparent 31px, rgba(45,48,71,0.06) 31px, rgba(45,48,71,0.06) 32px);
  pointer-events: none;
}

.hero-content {
  max-width: 1200px;
  margin: 0 auto;
  text-align: center;
  position: relative;
}

.hero-brand {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 4px;
}

.hero-mascot {
  width: 80px;
  height: 80px;
  object-fit: contain;
  image-rendering: pixelated;
  flex-shrink: 0;
  position: relative;
  z-index: 1;
}

.hero-title {
  margin: 0;
  font-size: 38px;
  font-weight: 800;
  color: var(--color-text);
  letter-spacing: -1px;
}

.hero-subtitle {
  margin: 10px 0 28px;
  font-size: 16px;
  color: var(--color-text);
  opacity: 0.75;
}

.search-bar {
  max-width: 540px;
  margin: 0 auto;
}

.search-input { width: 100%; }

.section {
  max-width: 1200px;
  margin: 0 auto 40px;
  padding: 0 24px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.section-title {
  margin: 0;
  font-size: 20px;
  font-weight: 800;
  color: var(--color-text);
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-title::before {
  content: '▶';
  font-size: 14px;
  color: var(--color-primary-dk);
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 12px;
}

.category-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 18px 12px;
  background: var(--color-surface);
  border: var(--border-pixel);
  border-radius: var(--radius-pixel);
  box-shadow: var(--shadow-hard-sm);
  cursor: pointer;
  transition: box-shadow 0.1s, transform 0.1s;
}

.category-card:hover {
  box-shadow: 1px 1px 0 var(--color-border);
  transform: translate(1px, 1px);
}

.category-icon { font-size: 32px; }

.category-name {
  font-size: 13px;
  font-weight: 700;
  color: var(--color-text);
}

.item-grid,
.loading-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(230px, 1fr));
  gap: 16px;
}

.skeleton-card {
  height: 290px;
  border-radius: var(--radius-pixel);
}

.item-card {
  background: var(--color-surface);
  border: var(--border-pixel);
  border-radius: var(--radius-pixel);
  box-shadow: var(--shadow-hard);
  overflow: hidden;
  cursor: pointer;
  transition: box-shadow 0.1s, transform 0.1s;
}

.item-card:hover {
  box-shadow: var(--shadow-hard-hover);
  transform: translate(2px, 2px);
}

.item-cover {
  position: relative;
  height: 195px;
  background: #f0ede6;
  border-bottom: var(--border-pixel);
}

.cover-img { width: 100%; height: 100%; display: block; }

.audit-overlay {
  position: absolute;
  top: 8px;
  right: 8px;
}

.item-info { padding: 12px 14px; }

.item-title {
  font-size: 14px;
  font-weight: 700;
  color: var(--color-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 8px;
}

.item-price {
  font-size: 19px;
  font-weight: 800;
  color: var(--color-danger);
}

.item-quality {
  font-size: 11px;
  font-weight: 600;
  color: var(--color-text-sub);
  background: #f0ede6;
  border: 1.5px solid var(--color-border);
  padding: 1px 7px;
  border-radius: var(--radius-pixel);
}

.empty-state {
  padding: 40px 0;
  background: var(--color-surface);
  border: var(--border-pixel);
  border-radius: var(--radius-pixel);
  box-shadow: var(--shadow-hard);
}
</style>
