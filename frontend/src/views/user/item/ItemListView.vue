<template>
  <div class="page-container">
    <PageHeader title="商品列表" />

    <div class="list-layout">
      <aside class="filter-sidebar">
        <div class="filter-section">
          <h3 class="filter-title">商品分类</h3>
          <div class="filter-options">
            <div
              class="filter-option"
              :class="{ active: !activeCategoryId }"
              @click="selectCategory(null)"
            >
              全部
            </div>
            <div
              v-for="cat in categoryList"
              :key="cat.categoryId"
              class="filter-option"
              :class="{ active: activeCategoryId === cat.categoryId }"
              @click="selectCategory(cat.categoryId)"
            >
              {{ cat.categoryName }}
            </div>
          </div>
        </div>

        <div class="filter-section">
          <h3 class="filter-title">价格区间</h3>
          <div class="price-range">
            <el-input-number
              v-model="filters.minPrice"
              :min="0"
              :precision="2"
              :controls="false"
              placeholder="最低价"
              size="small"
            />
            <span class="range-separator">-</span>
            <el-input-number
              v-model="filters.maxPrice"
              :min="0"
              :precision="2"
              :controls="false"
              placeholder="最高价"
              size="small"
            />
          </div>
        </div>

        <div class="filter-section">
          <h3 class="filter-title">商品成色</h3>
          <div class="filter-options">
            <div
              class="filter-option"
              :class="{ active: !filters.quality }"
              @click="filters.quality = undefined"
            >
              全部
            </div>
            <div
              v-for="(label, key) in QUALITY_MAP"
              :key="key"
              class="filter-option"
              :class="{ active: filters.quality === Number(key) }"
              @click="filters.quality = Number(key)"
            >
              {{ label }}
            </div>
          </div>
        </div>

        <div class="filter-actions">
          <el-button size="small" @click="resetFilters">重置</el-button>
          <el-button type="primary" size="small" @click="applyFilters">应用筛选</el-button>
        </div>
      </aside>

      <main class="list-main">
        <div class="search-bar">
          <el-input
            v-model="searchKey"
            placeholder="搜索商品标题..."
            size="large"
            clearable
            @clear="handleSearchClear"
            @keyup.enter="handleSearch"
          >
            <template #append>
              <el-button :icon="Search" @click="handleSearch" />
            </template>
          </el-input>
        </div>

        <div class="list-toolbar">
          <span class="result-count">共 {{ totalCount }} 件商品</span>
          <div class="sort-options">
            <span class="sort-label">排序：</span>
            <el-select v-model="sortBy" size="small" class="sort-select">
              <el-option value="time" label="最新发布" />
              <el-option value="price_asc" label="价格从低到高" />
              <el-option value="price_desc" label="价格从高到低" />
            </el-select>
          </div>
        </div>

        <div v-if="loading" class="item-grid">
          <el-skeleton v-for="i in 12" :key="i" animated class="skeleton-card" />
        </div>
        <div v-else-if="items.length === 0" class="empty-state">
          <el-empty :description="searchKey || activeCategoryId ? '没有找到符合条件的商品' : '暂无商品'" />
          <el-button v-if="searchKey || activeCategoryId" type="primary" @click="resetFilters">
            清除筛选
          </el-button>
        </div>

        <div v-else class="item-grid">
          <div
            v-for="item in items"
            :key="item.itemId"
            class="item-card"
            @click="router.push(`/items/${item.itemId}`)"
          >
            <div class="item-cover">
              <el-image :src="item.coverUrl" fit="cover" class="cover-img" />
              <div class="item-price-tag">¥{{ item.price }}</div>
            </div>
            <div class="item-info">
              <div class="item-title">{{ item.title }}</div>
              <div class="item-meta">
                <span class="item-category">{{ item.categoryName || '未分类' }}</span>
                <span class="item-quality">{{ item.qualityDesc }}</span>
              </div>
              <div class="item-footer">
                <span class="seller-name">
                  <el-icon><User /></el-icon>
                  {{ item.sellerName }}
                </span>
              </div>
            </div>
          </div>
        </div>

        <div v-if="totalCount > pageSize" class="pagination-wrapper">
          <AppPagination
            v-model:current-page="currentPage"
            :total="totalCount"
            :page-size="pageSize"
            @current-change="loadItems"
          />
        </div>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Search, User } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import { onMounted, reactive, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import PageHeader from '@/components/common/PageHeader.vue';
import AppPagination from '@/components/pagination/AppPagination.vue';
import { getCategoryList } from '@/api/modules/category';
import { getItemsByCategory, searchItems, filterItems } from '@/api/modules/item';
import { QUALITY_MAP, type ItemVO, type CategoryVO } from '@/types/item';

const route = useRoute();
const router = useRouter();

const loading = ref(false);
const categoryList = ref<CategoryVO[]>([]);
const items = ref<ItemVO[]>([]);
const totalCount = ref(0);
const currentPage = ref(1);
const pageSize = 20;
const sortBy = ref('time');
const searchKey = ref('');
const activeCategoryId = ref<number | null>(null);

const filters = reactive({
  minPrice: undefined as number | undefined,
  maxPrice: undefined as number | undefined,
  quality: undefined as number | undefined,
});

watch(
  () => route.query,
  (query) => {
    activeCategoryId.value = query.categoryId ? Number(query.categoryId) : null;
    searchKey.value = query.key ? (query.key as string) : '';
    currentPage.value = 1;
    loadItems();
  },
  { immediate: false },
);

watch(sortBy, () => {
  items.value = sortItems(items.value);
});

async function loadCategories() {
  try {
    const res = await getCategoryList();
    if (res.data) {
      categoryList.value = res.data;
    }
  } catch {
    ElMessage.error('加载分类失败');
  }
}

async function loadItems() {
  loading.value = true;
  try {
    let res;
    if (searchKey.value) {
      res = await searchItems(searchKey.value);
    } else if (
      filters.minPrice !== undefined ||
      filters.maxPrice !== undefined ||
      filters.quality !== undefined
    ) {
      res = await filterItems({
        categoryId: activeCategoryId.value ?? undefined,
        minPrice: filters.minPrice,
        maxPrice: filters.maxPrice,
        quality: filters.quality,
      });
    } else if (activeCategoryId.value) {
      res = await getItemsByCategory(activeCategoryId.value);
    } else {
      res = await filterItems({});
    }
    items.value = sortItems(res.data ?? []);
    totalCount.value = items.value.length;
  } catch {
    ElMessage.error('加载商品列表失败');
    items.value = [];
  } finally {
    loading.value = false;
  }
}

function sortItems(list: ItemVO[]): ItemVO[] {
  const sorted = [...list];
  if (sortBy.value === 'price_asc') {
    sorted.sort((a, b) => Number(a.price) - Number(b.price));
  } else if (sortBy.value === 'price_desc') {
    sorted.sort((a, b) => Number(b.price) - Number(a.price));
  } else {
    sorted.sort((a, b) => new Date(b.createTime).getTime() - new Date(a.createTime).getTime());
  }
  return sorted;
}

function selectCategory(cid: number | null) {
  activeCategoryId.value = cid;
  searchKey.value = '';
  currentPage.value = 1;
  router.replace({ query: cid ? { categoryId: String(cid) } : {} });
  loadItems();
}

function handleSearch() {
  if (!searchKey.value.trim()) {
    ElMessage.warning('请输入搜索关键词');
    return;
  }
  activeCategoryId.value = null;
  currentPage.value = 1;
  router.replace({ query: { key: searchKey.value } });
  loadItems();
}

function handleSearchClear() {
  searchKey.value = '';
  router.replace({ query: activeCategoryId.value ? { categoryId: String(activeCategoryId.value) } : {} });
  loadItems();
}

function applyFilters() {
  currentPage.value = 1;
  searchKey.value = '';
  router.replace({ query: {} });
  loadItems();
}

function resetFilters() {
  activeCategoryId.value = null;
  searchKey.value = '';
  filters.minPrice = undefined;
  filters.maxPrice = undefined;
  filters.quality = undefined;
  sortBy.value = 'time';
  currentPage.value = 1;
  router.replace({ query: {} });
  loadItems();
}

onMounted(async () => {
  await loadCategories();
  if (route.query.categoryId) {
    activeCategoryId.value = Number(route.query.categoryId);
  }
  if (route.query.key) {
    searchKey.value = route.query.key as string;
  }
  await loadItems();
});
</script>

<style scoped>
.list-layout {
  display: grid;
  grid-template-columns: 210px 1fr;
  gap: 20px;
  align-items: start;
}

.filter-sidebar {
  background: var(--color-surface);
  border: var(--border-pixel);
  border-radius: var(--radius-pixel);
  box-shadow: var(--shadow-hard);
  padding: 20px;
  position: sticky;
  top: 80px;
}

.filter-section {
  margin-bottom: 22px;
}

.filter-section:last-of-type {
  margin-bottom: 14px;
}

.filter-title {
  margin: 0 0 10px;
  font-size: 13px;
  font-weight: 800;
  color: var(--color-text);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  border-bottom: 1.5px solid #e5e0d8;
  padding-bottom: 6px;
}

.filter-options {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.filter-option {
  padding: 7px 10px;
  border: 2px solid transparent;
  border-radius: var(--radius-pixel);
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text-sub);
  cursor: pointer;
  transition: all 0.1s;
}

.filter-option:hover {
  background: #f0ede6;
  color: var(--color-text);
  border-color: var(--color-border);
}

.filter-option.active {
  background: var(--color-primary);
  color: var(--color-text);
  border-color: var(--color-border);
  box-shadow: var(--shadow-hard-sm);
}

.price-range {
  display: flex;
  align-items: center;
  gap: 6px;
}

.range-separator { color: var(--color-text-sub); font-weight: 700; }

.filter-actions { display: flex; gap: 8px; }

.list-main {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.search-bar {
  background: var(--color-surface);
  border: var(--border-pixel);
  border-radius: var(--radius-pixel);
  box-shadow: var(--shadow-hard-sm);
  padding: 14px;
}

.list-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--color-surface);
  border: var(--border-pixel);
  border-radius: var(--radius-pixel);
  box-shadow: var(--shadow-hard-sm);
  padding: 10px 14px;
}

.result-count {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text-sub);
}

.sort-options { display: flex; align-items: center; gap: 8px; }

.sort-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text-sub);
}

.sort-select { width: 140px; }

.item-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(195px, 1fr));
  gap: 14px;
}

.skeleton-card { height: 270px; }

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
  height: 168px;
  background: #f0ede6;
  border-bottom: var(--border-pixel);
}

.cover-img { width: 100%; height: 100%; display: block; }

.item-price-tag {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 5px 10px;
  background: var(--color-text);
  color: var(--color-secondary);
  font-size: 15px;
  font-weight: 800;
}

.item-info { padding: 10px 12px; }

.item-title {
  font-size: 13px;
  font-weight: 700;
  color: var(--color-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-meta {
  display: flex;
  gap: 6px;
  margin-top: 6px;
  align-items: center;
  flex-wrap: wrap;
}

.item-category {
  font-size: 11px;
  font-weight: 600;
  color: var(--color-text-sub);
  background: #f0ede6;
  border: 1.5px solid var(--color-border);
  padding: 1px 6px;
  border-radius: var(--radius-pixel);
}

.item-quality {
  font-size: 11px;
  font-weight: 600;
  color: var(--color-text-sub);
}

.item-footer { margin-top: 6px; }

.seller-name {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  font-weight: 600;
  color: var(--color-text-mute);
}

.empty-state {
  background: var(--color-surface);
  border: var(--border-pixel);
  border-radius: var(--radius-pixel);
  box-shadow: var(--shadow-hard);
  padding: 60px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  background: var(--color-surface);
  border: var(--border-pixel);
  border-radius: var(--radius-pixel);
  box-shadow: var(--shadow-hard-sm);
  padding: 14px;
}
</style>
