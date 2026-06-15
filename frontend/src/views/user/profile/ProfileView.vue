<template>
  <div class="page-container">
    <PageHeader title="个人中心" />

    <div class="profile-layout">
      <div class="user-card">
        <div class="user-info">
          <el-avatar
            :size="72"
            :src="userInfo.avatar || ''"
            :icon="!userInfo.avatar ? 'UserFilled' : undefined"
            class="avatar"
          />
          <div class="user-details">
            <div class="username">{{ userInfo.username }}</div>
            <div class="user-meta">
              <span>学号：{{ userInfo.studentId }}</span>
              <span v-if="userInfo.phone">手机：{{ userInfo.phone }}</span>
            </div>
            <div class="credit-row">
              <span class="credit-label">信用分</span>
              <el-rate :model-value="creditToRate(userInfo.creditScore)" disabled size="small" />
              <span class="credit-score">{{ userInfo.creditScore }}</span>
            </div>
          </div>
        </div>
        <el-button type="primary" text @click="showEditDialog = true">
          <el-icon><Edit /></el-icon>
          编辑资料
        </el-button>
      </div>

      <div class="content-tabs">
        <el-tabs v-model="activeTab" @tab-click="onTabClick">
          <el-tab-pane label="我的发布" name="items">
            <div v-if="myItemsLoading" class="loading">
              <el-skeleton :rows="4" animated />
            </div>
            <div v-else-if="myItems.length === 0" class="empty-state">
              <el-empty description="还没有发布商品">
                <el-button type="primary" @click="router.push('/publish')">发布闲置</el-button>
              </el-empty>
            </div>
            <div v-else class="item-grid">
              <div
                v-for="item in myItems"
                :key="item.itemId"
                class="item-card"
                @click="router.push(`/items/${item.itemId}`)"
              >
                <el-image :src="item.coverUrl" fit="cover" class="item-cover" />
                <div class="item-info">
                  <div class="item-title">{{ item.title }}</div>
                  <div class="item-price">¥{{ item.price }}</div>
                  <div class="item-meta">
                    <el-tag :type="auditTagType(item.auditStatus)" size="small">
                      {{ item.auditStatusDesc }}
                    </el-tag>
                  </div>
                </div>
                <div class="item-actions" @click.stop>
                  <el-button
                    v-if="item.auditStatus !== 0"
                    type="primary"
                    size="small"
                    @click="router.push({ path: '/publish', query: { itemId: item.itemId } })"
                  >
                    编辑
                  </el-button>
                  <el-button type="danger" size="small" plain @click="handleDeleteItem(item.itemId)">
                    删除
                  </el-button>
                </div>
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="我的收藏" name="collects">
            <div v-if="collectsLoading" class="loading">
              <el-skeleton :rows="4" animated />
            </div>
            <div v-else-if="myCollects.length === 0" class="empty-state">
              <el-empty description="还没有收藏商品">
                <el-button type="primary" @click="router.push('/items')">去逛逛</el-button>
              </el-empty>
            </div>
            <div v-else class="item-grid">
              <div
                v-for="collect in myCollects"
                :key="collect.collectId"
                class="item-card"
                @click="router.push(`/items/${collect.itemId}`)"
              >
                <el-image :src="collect.item?.coverUrl || ''" fit="cover" class="item-cover" />
                <div class="item-info">
                  <div class="item-title">{{ collect.item?.title || '商品' }}</div>
                  <div class="item-price">¥{{ collect.item?.price }}</div>
                  <div class="item-meta">
                    <span class="item-category">{{ collect.item?.categoryName }}</span>
                  </div>
                </div>
                <div class="item-actions" @click.stop>
                  <el-button type="warning" size="small" @click="handleCancelCollect(collect.collectId)">
                    取消收藏
                  </el-button>
                </div>
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="账号设置" name="settings">
            <div class="settings-section">
              <el-button type="danger" @click="handleLogout">退出登录</el-button>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>

    <el-dialog v-model="showEditDialog" title="编辑个人资料" width="480px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="昵称">
          <el-input v-model="editForm.username" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="editForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="头像">
          <UploadImg @uploaded="editForm.avatar = $event">
            <template #default>
              <div class="avatar-upload">
                <el-avatar v-if="editForm.avatar" :src="editForm.avatar" :size="64" />
                <el-avatar v-else :size="64" icon="UserFilled" />
                <span class="upload-tip">点击上传</span>
              </div>
            </template>
          </UploadImg>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" :loading="editLoading" @click="handleUpdateProfile">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { Edit } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import PageHeader from '@/components/common/PageHeader.vue';
import UploadImg from '@/components/upload/UploadImg.vue';
import { getMyItems, deleteItem } from '@/api/modules/item';
import { getMyCollects, cancelCollect } from '@/api/modules/collect';
import { getUserInfo, updateUserInfo } from '@/api/modules/user';
import { useAuthStore } from '@/stores/modules/auth';
import type { ItemVO } from '@/types/item';
import type { CollectVO } from '@/api/modules/collect';

const router = useRouter();
const authStore = useAuthStore();

const activeTab = ref('items');
const myItemsLoading = ref(false);
const collectsLoading = ref(false);
const myItems = ref<ItemVO[]>([]);
const myCollects = ref<CollectVO[]>([]);
const showEditDialog = ref(false);
const editLoading = ref(false);

const userInfo = reactive({
  userId: 0,
  studentId: '',
  username: '',
  phone: '',
  avatar: '',
  creditScore: 100,
});

const editForm = reactive({
  username: '',
  phone: '',
  avatar: '',
});

function creditToRate(score: number): number {
  if (score >= 90) return 5;
  if (score >= 70) return 4;
  if (score >= 50) return 3;
  if (score >= 30) return 2;
  return 1;
}

function auditTagType(status: number): 'warning' | 'success' | 'danger' {
  if (status === 0) return 'warning';
  if (status === 1) return 'success';
  return 'danger';
}

async function loadUserInfo() {
  try {
    const res = await getUserInfo();
    if (res.data) {
      userInfo.userId = res.data.userId;
      userInfo.studentId = res.data.studentId;
      userInfo.username = res.data.username;
      userInfo.phone = res.data.phone || '';
      userInfo.avatar = res.data.avatar || '';
      userInfo.creditScore = res.data.creditScore;
      editForm.username = res.data.username;
      editForm.phone = res.data.phone || '';
      editForm.avatar = res.data.avatar || '';
    }
  } catch {
    ElMessage.error('加载用户信息失败');
  }
}

async function loadMyItems() {
  myItemsLoading.value = true;
  try {
    const res = await getMyItems();
    myItems.value = res.data || [];
  } catch {
    ElMessage.error('加载我的发布失败');
  } finally {
    myItemsLoading.value = false;
  }
}

async function loadMyCollects() {
  collectsLoading.value = true;
  try {
    const res = await getMyCollects();
    myCollects.value = res.data || [];
  } catch {
    ElMessage.error('加载我的收藏失败');
  } finally {
    collectsLoading.value = false;
  }
}

function onTabClick(tab: { paneName: string }) {
  if (tab.paneName === 'collects' && myCollects.value.length === 0 && !collectsLoading.value) {
    loadMyCollects();
  }
}

async function handleDeleteItem(itemId: number) {
  try {
    await ElMessageBox.confirm('确定要删除该商品吗？', '删除商品', {
      type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消',
    });
    await deleteItem(itemId);
    ElMessage.success('删除成功');
    loadMyItems();
  } catch {
    // cancelled
  }
}

async function handleCancelCollect(collectId: number) {
  try {
    await cancelCollect(collectId);
    ElMessage.success('已取消收藏');
    loadMyCollects();
  } catch {
    ElMessage.error('操作失败');
  }
}

function handleUpdateProfile() {
  editLoading.value = true;
  updateUserInfo({
    username: editForm.username,
    phone: editForm.phone || undefined,
    avatar: editForm.avatar || undefined,
  })
    .then(() => {
      ElMessage.success('修改成功');
      showEditDialog.value = false;
      loadUserInfo();
    })
    .catch(() => {
      ElMessage.error('修改失败');
    })
    .finally(() => {
      editLoading.value = false;
    });
}

async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定退出登录吗？', '提示', {
      confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning',
    });
    authStore.clearAuth();
    ElMessage.success('已退出登录');
    router.push('/');
  } catch {
    // cancelled
  }
}

onMounted(async () => {
  if (!authStore.isLoggedIn) {
    router.push('/login');
    return;
  }
  await loadUserInfo();
  await loadMyItems();
});
</script>

<style scoped>
.profile-layout {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.user-card {
  background: var(--color-surface);
  border: var(--border-pixel);
  border-radius: var(--radius-pixel);
  box-shadow: var(--shadow-hard);
  padding: 24px 28px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 20px;
}

.username {
  font-size: 22px;
  font-weight: 800;
  color: var(--color-text);
}

.user-meta {
  display: flex;
  gap: 16px;
  margin-top: 6px;
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text-sub);
}

.credit-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
}

.credit-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text-sub);
}

.credit-score {
  font-size: 15px;
  font-weight: 800;
  color: var(--color-text);
  background: var(--color-secondary);
  border: 1.5px solid var(--color-border);
  border-radius: var(--radius-pixel);
  padding: 1px 8px;
}

.content-tabs {
  background: var(--color-surface);
  border: var(--border-pixel);
  border-radius: var(--radius-pixel);
  box-shadow: var(--shadow-hard);
  padding: 20px;
}

.loading,
.empty-state { padding: 24px 0; }

.settings-section { padding: 24px 0; }

.item-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(210px, 1fr));
  gap: 14px;
  margin-top: 14px;
}

.item-card {
  border: var(--border-pixel);
  border-radius: var(--radius-pixel);
  box-shadow: var(--shadow-hard-sm);
  overflow: hidden;
  cursor: pointer;
  transition: box-shadow 0.1s, transform 0.1s;
}

.item-card:hover {
  box-shadow: 1px 1px 0 var(--color-border);
  transform: translate(1px, 1px);
}

.item-cover {
  width: 100%;
  height: 170px;
  display: block;
  background: #f0ede6;
  border-bottom: var(--border-pixel);
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

.item-price {
  font-size: 17px;
  font-weight: 800;
  color: var(--color-danger);
  margin-top: 5px;
}

.item-meta { margin-top: 5px; }

.item-category {
  font-size: 11px;
  font-weight: 600;
  color: var(--color-text-sub);
}

.item-actions {
  padding: 0 12px 10px;
  display: flex;
  gap: 6px;
}

.avatar-upload {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
}

.upload-tip {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text-sub);
}
</style>
