<template>
  <div class="home-page">
    <div class="hero-banner">
      <div class="hero-content">
        <h1 class="hero-title">校园闲置交易平台</h1>
        <p class="hero-subtitle">安全便捷的校内二手交易</p>
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
import { getItemsByCategory } from '@/api/modules/item';
import type { CategoryVO, ItemVO } from '@/types/item';

const router = useRouter();
const searchKey = ref('');
const itemsLoading = ref(false);
const categoryList = ref<CategoryVO[]>([]);
const recentItems = ref<ItemVO[]>([]);

function categoryEmoji(name: string): string {
  const map: Record<string, string> = {
    '数码电子': '📱',
    '书籍教材': '📚',
    '生活用品': '🛒',
    '服装鞋包': '👕',
    '运动户外': '⚽',
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
    const cats = categoryList.value;
    const allItems: ItemVO[] = [];
    for (const cat of cats.slice(0, 3)) {
      try {
        const res = await getItemsByCategory(cat.categoryId);
        if (res.data) {
          allItems.push(...res.data);
        }
      } catch {
        // skip failed category
      }
    }
    recentItems.value = allItems.slice(0, 8);
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 60px 0;
  margin-bottom: 32px;
}

.hero-content {
  max-width: 1200px;
  margin: 0 auto;
  text-align: center;
}

.hero-title {
  margin: 0;
  font-size: 40px;
  font-weight: 700;
  color: #fff;
}

.hero-subtitle {
  margin: 12px 0 32px;
  font-size: 18px;
  color: rgba(255, 255, 255, 0.8);
}

.search-bar {
  max-width: 560px;
  margin: 0 auto;
}

.search-input {
  width: 100%;
}

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
  font-size: 22px;
  font-weight: 700;
  color: #1f2937;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 16px;
}

.category-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 20px 12px;
  background: #fff;
  border-radius: 12px;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  border: 1px solid #e5e7eb;
}

.category-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  border-color: #667eea;
}

.category-icon {
  font-size: 36px;
}

.category-name {
  font-size: 14px;
  font-weight: 600;
  color: #374151;
}

.item-grid,
.loading-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
}

.skeleton-card {
  height: 300px;
  border-radius: 12px;
}

.item-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #e5e7eb;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.item-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.item-cover {
  position: relative;
  height: 200px;
  background: #f3f4f6;
}

.cover-img {
  width: 100%;
  height: 100%;
  display: block;
}

.audit-overlay {
  position: absolute;
  top: 8px;
  right: 8px;
}

.item-info {
  padding: 14px;
}

.item-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 10px;
}

.item-price {
  font-size: 20px;
  font-weight: 700;
  color: #ef4444;
}

.item-quality {
  font-size: 12px;
  color: #9ca3af;
  background: #f3f4f6;
  padding: 2px 8px;
  border-radius: 4px;
}

.empty-state {
  padding: 40px 0;
  background: #fff;
  border-radius: 12px;
}
</style>
