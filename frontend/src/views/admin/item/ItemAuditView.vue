<template>
  <div class="item-audit">
    <div class="page-header">
      <h2>商品审核</h2>
       <div class="search-bar">
            <input
                  v-model="searchKeyword"
                  type="text"
                  placeholder="搜索商品标题..."
                  class="search-input"
                  @keyup.enter="loadItems(auditStatus, 1)"
                />
                <button class="search-btn" @click="loadItems(auditStatus, 1)">搜索</button>
       </div>
    </div>

    <!-- 状态标签 -->
    <div class="tabs">
      <button
        :class="{ active: auditStatus === 0 }"
        @click="loadItems(0, 1)"
      >
        待审核 ({{ pendingCount }})
      </button>
      <button
        :class="{ active: auditStatus === 1 }"
        @click="loadItems(1, 1)"
      >
        已通过
      </button>
      <button
        :class="{ active: auditStatus === 2 }"
        @click="loadItems(2, 1)"
      >
        已驳回
      </button>

    </div>

    <div v-if="loading" class="loading">加载中...</div>

    <div v-else-if="items.length === 0" class="empty">
      暂无商品
    </div>

    <div v-else>
      <div class="item-list">
        <div v-for="item in items" :key="item.item_id" class="item-card" @click="goToItemDetail(item.item_id)">
          <div class="item-image">
            <img :src="item.cover_url || '/default-image.png'" :alt="item.title" />
          </div>

          <div class="item-info">
            <h3>{{ item.title }}</h3>
            <p class="price">¥{{ item.price }}</p>
            <p class="desc">{{ item.description?.slice(0, 100) }}...</p>
            <div class="meta">
              <span>发布者: {{ item.username || `用户${item.user_id}` }}</span>
              <span>分类: {{ item.category_name || '-' }}</span>
              <span>发布时间: {{ formatDate(item.create_time) }}</span>
            </div>
          </div>

          <div class="item-actions" v-if="auditStatus === 0">
            <button class="btn-approve" @click.stop="handleAudit(item.item_id, 1)">
              ✓ 通过
            </button>
            <button class="btn-reject" @click.stop="handleAudit(item.item_id, 2)">
              ✗ 驳回
            </button>
          </div>

          <div class="item-status" v-else>
            <span :class="['status-badge', auditStatus === 1 ? 'approved' : 'rejected']">
              {{ auditStatus === 1 ? '已通过' : '已驳回' }}
            </span>
          </div>
        </div>
      </div>

      <!-- 分页组件 -->
      <div class="pagination" v-if="totalPages > 1">
        <button
          :disabled="currentPage === 1"
          @click="goToPage(currentPage - 1)"
        >
          上一页
        </button>
        <span class="page-info">第 {{ currentPage }} / {{ totalPages }} 页</span>
        <button
          :disabled="currentPage === totalPages"
          @click="goToPage(currentPage + 1)"
        >
          下一页
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAdminAuthStore } from '@/stores/modules/adminAuth'
import { auditItem } from '@/api/modules/admin'
import { useRouter } from 'vue-router'

const adminAuthStore = useAdminAuthStore()
const items = ref<any[]>([])
const loading = ref(false)
const auditStatus = ref(0)
const pendingCount = ref(0)
const currentPage = ref(1)
const totalPages = ref(1)
const total = ref(0)

const router = useRouter()  // 新增

// 搜索关键词
const searchKeyword = ref('')

const formatDate = (dateStr?: string) => {
  if (!dateStr) return ''
  return dateStr.replace('T', ' ').slice(0, 16)
}

// 加载商品列表
const loadItems = async (status: number, page: number = 1) => {
  auditStatus.value = status
  currentPage.value = page
  loading.value = true

  try {
    let url = `/api/v1/admin/item/list?auditStatus=${status}&page=${page}&size=10`

    // 添加搜索关键词
    if (searchKeyword.value.trim()) {
      url += `&keyword=${encodeURIComponent(searchKeyword.value)}`
    }

    const res = await fetch(url, {
      headers: { 'Authorization': `Bearer ${adminAuthStore.token}` }
    })
    const data = await res.json()

    if (data.code === 200) {
      items.value = data.data || []
      total.value = data.total || 0
      totalPages.value = data.totalPages || 1

      if (status === 0) {
        pendingCount.value = data.total || 0
      }
    }
  } catch (error) {
    console.error('加载商品失败:', error)
  } finally {
    loading.value = false
  }
}

// 跳转页面
const goToPage = (page: number) => {
  if (page >= 1 && page <= totalPages.value) {
    loadItems(auditStatus.value, page)
  }
}
// 跳转到商品详情页（用户端路由为 /items/:id）
const goToItemDetail = (itemId: number) => {
  window.open(`/items/${itemId}`, '_blank')
}

// 审核商品
const handleAudit = async (itemId: number, auditStatusValue: number) => {
  try {
    const res = await auditItem({ itemId, auditStatus: auditStatusValue })
    console.log('响应:', res)
    if (res.code === 200) {
      alert(auditStatusValue === 1 ? '审核通过' : '已驳回')
      await loadItems(auditStatus.value, currentPage.value)
    } else {
      alert(res.message || '审核失败')
    }
  } catch (error) {
    console.error('审核失败:', error)
    alert('审核失败')
    console.error('错误:', error)
  }
}

onMounted(() => {
  loadItems(0, 1)
})
</script>

<style scoped>
.item-audit {
  background: white;
  border-radius: 16px;
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e5e7eb;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
}

.tabs {
  display: flex;
  gap: 8px;
}

.tabs button {
  padding: 8px 20px;
  background: #f3f4f6;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
}

.tabs button.active {
  background: #2563eb;
  color: white;
}

.item-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.item-card {
  display: flex;
  gap: 20px;
  padding: 20px;
  background: #f9fafb;
  border-radius: 16px;
  transition: all 0.3s;
}

.item-card:hover {
  background: #f3f4f6;
}

.item-image img {
  width: 120px;
  height: 120px;
  object-fit: cover;
  border-radius: 12px;
}

.item-info {
  flex: 1;
}

.item-info h3 {
  margin: 0 0 8px;
  font-size: 18px;
}

.price {
  font-size: 20px;
  font-weight: bold;
  color: #f59e0b;
  margin: 0 0 8px;
}

.desc {
  color: #6b7280;
  font-size: 14px;
  margin: 0 0 12px;
  line-height: 1.5;
}

.meta {
  display: flex;
  gap: 20px;
  font-size: 12px;
  color: #9ca3af;
}

.item-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  justify-content: center;
}

.btn-approve {
  padding: 8px 24px;
  background: #10b981;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}

.btn-reject {
  padding: 8px 24px;
  background: #ef4444;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}

.item-status {
  display: flex;
  align-items: center;
}

.status-badge {
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 14px;
}

.status-badge.approved {
  background: #d1fae5;
  color: #10b981;
}

.status-badge.rejected {
  background: #fee2e2;
  color: #dc2626;
}

.loading, .empty {
  text-align: center;
  padding: 60px;
  color: #9ca3af;
}


.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #e5e7eb;
}

.pagination button {
  padding: 8px 16px;
  background: #f3f4f6;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}

.pagination button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  font-size: 14px;
  color: #6b7280;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e5e7eb;
}

.search-bar {
  display: flex;
  gap: 8px;
}

.search-input {
  padding: 8px 16px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  width: 220px;
  font-size: 14px;
}

.search-btn {
  padding: 8px 20px;
  background: #2563eb;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}

.search-btn:hover {
  background: #1d4ed8;
}

</style>