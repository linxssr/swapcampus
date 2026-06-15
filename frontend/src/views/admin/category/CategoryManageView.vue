<template>
  <div class="category-manage">
    <div class="page-header">
      <h2>分类管理</h2>
      <button class="btn-add" @click="openAddDialog">
        + 新增分类
      </button>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <input
        v-model="searchKeyword"
        type="text"
        placeholder="搜索分类名称..."
        class="search-input"
        @keyup.enter="loadCategories(1)"
      />
      <button class="search-btn" @click="loadCategories(1)">搜索</button>
    </div>

    <div v-if="loading" class="loading">加载中...</div>

    <div v-else-if="categories.length === 0" class="empty">
      暂无分类
    </div>

    <div v-else>
      <div class="category-list">
        <table class="category-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>分类名称</th>
              <th>排序</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="category in categories" :key="category.category_id">
              <td>{{ category.category_id }}</td>
              <td>
                <span v-if="editingId !== category.category_id">
                  {{ category.category_name }}
                </span>
                <input
                  v-else
                  v-model="editForm.name"
                  type="text"
                  class="edit-input"
                />
              </td>
              <td>
                <span v-if="editingId !== category.category_id">
                  {{ category.sort }}
                </span>
                <input
                  v-else
                  v-model="editForm.sort"
                  type="number"
                  class="edit-input-small"
                />
              </td>
              <td>
                <span :class="['status-badge', category.status === 1 ? 'active' : 'disabled']">
                  {{ category.status === 1 ? '启用' : '禁用' }}
                </span>
              </td>
              <td class="actions">
                <template v-if="editingId !== category.category_id">
                  <button class="btn-edit" @click="startEdit(category)">编辑</button>
                  <button
                    class="btn-status"
                    :class="category.status === 1 ? 'btn-disable' : 'btn-enable'"
                    @click="toggleStatus(category.category_id, category.status)"
                  >
                    {{ category.status === 1 ? '禁用' : '启用' }}
                  </button>
                </template>
                <template v-else>
                  <button class="btn-save" @click="saveEdit">保存</button>
                  <button class="btn-cancel" @click="cancelEdit">取消</button>
                </template>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 分页组件 -->
      <div class="pagination" v-if="totalPages > 1">
        <button :disabled="currentPage === 1" @click="goToPage(currentPage - 1)">
          上一页
        </button>
        <span class="page-info">第 {{ currentPage }} / {{ totalPages }} 页</span>
        <button :disabled="currentPage === totalPages" @click="goToPage(currentPage + 1)">
          下一页
        </button>
      </div>
    </div>

    <!-- 新增分类弹窗 -->
    <div v-if="showAddDialog" class="modal-overlay" @click.self="closeAddDialog">
      <div class="modal">
        <h3>新增分类</h3>
        <div class="form-item">
          <label>分类名称</label>
          <input v-model="addForm.name" type="text" placeholder="请输入分类名称" />
        </div>
        <div class="form-item">
          <label>排序（数字越小越靠前）</label>
          <input v-model.number="addForm.sort" type="number" placeholder="请输入排序" />
        </div>
        <div class="modal-buttons">
          <button class="btn-confirm" @click="addCategory" :disabled="adding">确认</button>
          <button class="btn-cancel" @click="closeAddDialog">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAdminAuthStore } from '@/stores/modules/adminAuth'

const adminAuthStore = useAdminAuthStore()
const categories = ref<any[]>([])
const loading = ref(false)
const currentPage = ref(1)
const totalPages = ref(1)
const total = ref(0)
const searchKeyword = ref('')

// 编辑相关
const editingId = ref<number | null>(null)
const editForm = ref({
  name: '',
  sort: 0
})

// 新增弹窗
const showAddDialog = ref(false)
const adding = ref(false)
const addForm = ref({
  name: '',
  sort: 0
})

// 加载分类列表
const loadCategories = async (page: number = 1) => {
  currentPage.value = page
  loading.value = true

  try {
    let url = `/api/v1/admin/category/list?page=${page}&size=10`
    if (searchKeyword.value.trim()) {
      url += `&keyword=${encodeURIComponent(searchKeyword.value)}`
    }

    const res = await fetch(url, {
      headers: { 'Authorization': `Bearer ${adminAuthStore.token}` }
    })
    const data = await res.json()

    if (data.code === 200) {
      categories.value = data.data || []
      total.value = data.total || 0
      totalPages.value = data.totalPages || 1
    }
  } catch (error) {
    console.error('加载分类失败:', error)
  } finally {
    loading.value = false
  }
}

// 跳转页面
const goToPage = (page: number) => {
  if (page >= 1 && page <= totalPages.value) {
    loadCategories(page)
  }
}

// 打开新增弹窗
const openAddDialog = () => {
  addForm.value = { name: '', sort: 0 }
  showAddDialog.value = true
}

// 关闭新增弹窗
const closeAddDialog = () => {
  showAddDialog.value = false
  addForm.value = { name: '', sort: 0 }
}

// 新增分类
const addCategory = async () => {
  if (!addForm.value.name.trim()) {
    alert('请输入分类名称')
    return
  }

  adding.value = true
  try {
    const res = await fetch('/api/v1/admin/category/add', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${adminAuthStore.token}`
      },
      body: JSON.stringify({
        name: addForm.value.name,
        sort: addForm.value.sort || 0
      })
    })
    const data = await res.json()

    if (data.code === 200) {
      alert('新增成功')
      closeAddDialog()
      await loadCategories(currentPage.value)
    } else {
      alert(data.message || '新增失败')
    }
  } catch (error) {
    console.error('新增失败:', error)
    alert('新增失败')
  } finally {
    adding.value = false
  }
}

// 开始编辑
const startEdit = (category: any) => {
  editingId.value = category.category_id
  editForm.value = {
    name: category.category_name,
    sort: category.sort
  }
}

// 保存编辑
const saveEdit = async () => {
  if (!editForm.value.name.trim()) {
    alert('请输入分类名称')
    return
  }

  try {
    const res = await fetch('/api/v1/admin/category/update', {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${adminAuthStore.token}`
      },
      body: JSON.stringify({
        categoryId: editingId.value,
        name: editForm.value.name,
        sort: editForm.value.sort
      })
    })
    const data = await res.json()

    if (data.code === 200) {
      alert('修改成功')
      editingId.value = null
      await loadCategories(currentPage.value)
    } else {
      alert(data.message || '修改失败')
    }
  } catch (error) {
    console.error('修改失败:', error)
    alert('修改失败')
  }
}

// 取消编辑
const cancelEdit = () => {
  editingId.value = null
  editForm.value = { name: '', sort: 0 }
}

// 切换状态（启用/禁用）
const toggleStatus = async (categoryId: number, currentStatus: number) => {
  const newStatus = currentStatus === 1 ? 0 : 1
  const action = newStatus === 1 ? '启用' : '禁用'

  if (!confirm(`确定要${action}该分类吗？`)) return

  try {
    const res = await fetch(`/api/v1/admin/category/status?categoryId=${categoryId}&status=${newStatus}`, {
      method: 'PUT',
      headers: {
        'Authorization': `Bearer ${adminAuthStore.token}`
      }
    })
    const data = await res.json()

    if (data.code === 200) {
      alert(`${action}成功`)
      await loadCategories(currentPage.value)
    } else {
      alert(data.message || `${action}失败`)
    }
  } catch (error) {
    console.error(`${action}失败:`, error)
    alert(`${action}失败`)
  }
}

onMounted(() => {
  loadCategories(1)
})
</script>

<style scoped>
.category-manage {
  background: white;
  border-radius: 16px;
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e5e7eb;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
}

.btn-add {
  padding: 8px 20px;
  background: #2563eb;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}

.btn-add:hover {
  background: #1d4ed8;
}

.search-bar {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
}

.search-input {
  padding: 8px 16px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  width: 240px;
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

.category-table {
  width: 100%;
  border-collapse: collapse;
}

.category-table th,
.category-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #e5e7eb;
}

.category-table th {
  background: #f9fafb;
  font-weight: 600;
  color: #374151;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
}

.status-badge.active {
  background: #d1fae5;
  color: #10b981;
}

.status-badge.disabled {
  background: #fee2e2;
  color: #dc2626;
}

.actions {
  display: flex;
  gap: 8px;
}

.btn-edit, .btn-status, .btn-save, .btn-cancel {
  padding: 4px 12px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 12px;
}

.btn-edit {
  background: #e0e7ff;
  color: #4f46e5;
}

.btn-disable {
  background: #fee2e2;
  color: #dc2626;
}

.btn-enable {
  background: #d1fae5;
  color: #10b981;
}

.btn-save {
  background: #10b981;
  color: white;
}

.btn-cancel {
  background: #9ca3af;
  color: white;
}

.edit-input {
  padding: 4px 8px;
  border: 1px solid #e5e7eb;
  border-radius: 4px;
  width: 120px;
}

.edit-input-small {
  padding: 4px 8px;
  border: 1px solid #e5e7eb;
  border-radius: 4px;
  width: 60px;
}

/* 弹窗样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal {
  background: white;
  border-radius: 16px;
  padding: 24px;
  width: 400px;
}

.modal h3 {
  margin: 0 0 20px;
  font-size: 18px;
}

.form-item {
  margin-bottom: 16px;
}

.form-item label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

.form-item input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  font-size: 14px;
  box-sizing: border-box;
}

.modal-buttons {
  display: flex;
  gap: 12px;
  margin-top: 20px;
}

.btn-confirm {
  flex: 1;
  padding: 10px;
  background: #2563eb;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
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

.loading, .empty {
  text-align: center;
  padding: 60px;
  color: #9ca3af;
}
</style>