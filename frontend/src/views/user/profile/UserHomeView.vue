<template>
  <div class="page-container">
    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="8" animated />
    </div>

    <div v-else-if="profile" class="user-home">
      <!-- 用户信息卡片 -->
      <div class="profile-card">
        <div class="profile-left">
          <el-avatar :size="80" :src="profile.avatar || ''" icon="UserFilled" class="avatar" />
          <div class="profile-info">
            <div class="username">{{ profile.username }}</div>
            <div class="credit-row">
              <span class="credit-label">信用分</span>
              <el-rate
                :model-value="creditToRate(profile.creditScore)"
                disabled
                :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
              />
              <span class="credit-score">{{ profile.creditScore }}</span>
            </div>
          </div>
        </div>
        <el-button
          v-if="!isSelf"
          type="primary"
          @click="router.push(`/chat/room/${profile.userId}`)"
        >
          <el-icon><ChatDotRound /></el-icon>
          联系 TA
        </el-button>
        <el-button v-else type="default" @click="router.push('/profile')">
          前往我的个人中心
        </el-button>
      </div>

      <!-- Tab 切换 -->
      <div class="content-card">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="TA 的发布" name="items">
            <div v-if="itemsLoading" class="tab-loading">
              <el-skeleton :rows="3" animated />
            </div>
            <div v-else-if="items.length === 0" class="tab-empty">
              <el-empty description="暂无发布商品" :image-size="80" />
            </div>
            <div v-else class="item-grid">
              <div
                v-for="item in items"
                :key="item.itemId"
                class="item-card"
                @click="router.push(`/items/${item.itemId}`)"
              >
                <el-image :src="item.coverUrl" fit="cover" class="item-cover" />
                <div class="item-info">
                  <div class="item-title">{{ item.title }}</div>
                  <div class="item-price">¥{{ item.price }}</div>
                  <div class="item-meta">
                    <el-tag
                      v-if="item.publishStatus === 0"
                      type="info"
                      size="small"
                      effect="plain"
                    >已下架</el-tag>
                    <el-tag v-else type="success" size="small" effect="plain">在售</el-tag>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="信用与评价" name="comments">
            <div v-if="commentsLoading" class="tab-loading">
              <el-skeleton :rows="4" animated />
            </div>
            <div v-else-if="comments.length === 0" class="tab-empty">
              <el-empty description="暂无评价记录" :image-size="80" />
            </div>
            <div v-else>
              <div class="credit-summary">
                <div class="summary-item">
                  <span class="summary-num">{{ comments.length }}</span>
                  <span class="summary-label">累计评价</span>
                </div>
                <el-divider direction="vertical" class="summary-divider" />
                <div class="summary-item">
                  <span class="summary-num good">{{ goodCount }}</span>
                  <span class="summary-label">好评</span>
                </div>
                <el-divider direction="vertical" class="summary-divider" />
                <div class="summary-item">
                  <span class="summary-num bad">{{ badCount }}</span>
                  <span class="summary-label">差评</span>
                </div>
                <el-divider direction="vertical" class="summary-divider" />
                <div class="summary-item">
                  <span class="summary-num">{{ avgScore }}</span>
                  <span class="summary-label">平均评分</span>
                </div>
              </div>

              <div class="comments-list">
                <div v-for="c in comments" :key="c.commentId" class="comment-card">
                  <div class="comment-top">
                    <div class="comment-left">
                      <el-avatar :size="36" icon="UserFilled" />
                      <div class="comment-user">
                        <span class="buyer-name">{{ c.buyerName || `用户#${c.buyerId}` }}</span>
                        <span class="comment-time">{{ formatTime(c.createTime) }}</span>
                      </div>
                    </div>
                    <el-rate
                      :model-value="c.score"
                      disabled
                      :colors="['#f7ba2a', '#f7ba2a', '#ff9900']"
                    />
                  </div>
                  <div v-if="c.itemTitle" class="comment-item-title">
                    <el-icon><GoodsFilled /></el-icon>
                    {{ c.itemTitle }}
                  </div>
                  <div class="comment-content">{{ c.content }}</div>
                </div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>

    <div v-else class="not-found">
      <el-empty description="用户不存在或已注销" />
      <el-button type="primary" @click="router.push('/')">返回首页</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ChatDotRound, GoodsFilled } from '@element-plus/icons-vue';
import dayjs from 'dayjs';
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getUserProfile } from '@/api/modules/user';
import { getUserSellerComments } from '@/api/modules/order';
import { get } from '@/utils/request';
import { useAuthStore } from '@/stores/modules/auth';
import type { CommentRecord } from '@/types/order';

interface ItemVO {
  itemId: number;
  title: string;
  price: number;
  coverUrl: string;
  publishStatus: number;
}

interface UserProfile {
  userId: number;
  username: string;
  creditScore: number;
  avatar?: string;
}

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

const userId = computed(() => Number(route.params.userId));
const isSelf = computed(() => authStore.userInfo?.userId === userId.value);

const loading = ref(true);
const profile = ref<UserProfile | null>(null);

const activeTab = ref('items');
const itemsLoading = ref(false);
const items = ref<ItemVO[]>([]);
const commentsLoading = ref(false);
const comments = ref<CommentRecord[]>([]);

const goodCount = computed(() => comments.value.filter((c) => c.score >= 4).length);
const badCount = computed(() => comments.value.filter((c) => c.score < 3).length);
const avgScore = computed(() => {
  if (!comments.value.length) return '0.0';
  const sum = comments.value.reduce((a, b) => a + b.score, 0);
  return (sum / comments.value.length).toFixed(1);
});

function creditToRate(score: number): number {
  if (score >= 90) return 5;
  if (score >= 70) return 4;
  if (score >= 50) return 3;
  if (score >= 30) return 2;
  return 1;
}

function formatTime(time: string) {
  return dayjs(time).format('YYYY-MM-DD HH:mm');
}

async function loadProfile() {
  loading.value = true;
  try {
    const res = await getUserProfile(userId.value);
    profile.value = res.data;
  } catch {
    profile.value = null;
  } finally {
    loading.value = false;
  }
}

async function loadItems() {
  itemsLoading.value = true;
  try {
    const res = await get<ItemVO[]>(`/item/user/${userId.value}`);
    items.value = res.data || [];
  } catch {
    items.value = [];
  } finally {
    itemsLoading.value = false;
  }
}

async function loadComments() {
  commentsLoading.value = true;
  try {
    const res = await getUserSellerComments(userId.value);
    comments.value = (res.data || []) as CommentRecord[];
  } catch {
    comments.value = [];
  } finally {
    commentsLoading.value = false;
  }
}

onMounted(async () => {
  await loadProfile();
  await Promise.all([loadItems(), loadComments()]);
});
</script>

<style scoped>
.loading-state {
  padding: 24px;
  background: #fff;
  border-radius: 12px;
}

.user-home {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.profile-card {
  background: #fff;
  border-radius: 16px;
  padding: 28px 32px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.profile-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.avatar {
  flex-shrink: 0;
}

.username {
  font-size: 22px;
  font-weight: 700;
  color: #1f2937;
}

.credit-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
}

.credit-label {
  font-size: 14px;
  color: #6b7280;
}

.credit-score {
  font-size: 16px;
  font-weight: 700;
  color: #f59e0b;
}

.content-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
}

.tab-loading,
.tab-empty {
  padding: 24px 0;
}

.item-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
  margin-top: 16px;
}

.item-card {
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
  transition: box-shadow 0.2s, transform 0.2s;
}

.item-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.item-cover {
  width: 100%;
  height: 180px;
  display: block;
  background: #f3f4f6;
}

.item-info {
  padding: 12px;
}

.item-title {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-price {
  font-size: 18px;
  font-weight: 700;
  color: #ef4444;
  margin-top: 6px;
}

.item-meta {
  margin-top: 6px;
}

.credit-summary {
  display: flex;
  align-items: center;
  gap: 0;
  background: #f9fafb;
  border-radius: 12px;
  padding: 20px 24px;
  margin-bottom: 24px;
}

.summary-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  flex: 1;
}

.summary-num {
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
}

.summary-num.good {
  color: #22c55e;
}

.summary-num.bad {
  color: #ef4444;
}

.summary-label {
  font-size: 13px;
  color: #9ca3af;
}

.summary-divider {
  height: 40px;
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.comment-card {
  padding: 18px 20px;
  background: #f9fafb;
  border-radius: 12px;
  border: 1px solid #f3f4f6;
}

.comment-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.comment-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.comment-user {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.buyer-name {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
}

.comment-time {
  font-size: 12px;
  color: #9ca3af;
}

.comment-item-title {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 8px;
}

.comment-content {
  font-size: 14px;
  color: #4b5563;
  line-height: 1.7;
  word-break: break-word;
}

.not-found {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 60px;
  background: #fff;
  border-radius: 12px;
}
</style>
