<template>
  <div class="page-container">
    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="8" animated />
    </div>

    <div v-else-if="item" class="item-detail">
      <div class="detail-main">
        <div class="gallery">
          <div class="main-image">
            <el-image
              :src="activeImage"
              fit="contain"
              class="main-img"
              :preview-src-list="item.imageUrls || []"
              :initial-index="activeIndex"
            >
              <template #error><div class="img-error">暂无图片</div></template>
            </el-image>
          </div>
          <div v-if="item.imageUrls && item.imageUrls.length > 1" class="thumbnail-list">
            <div
              v-for="(url, idx) in item.imageUrls"
              :key="idx"
              class="thumbnail"
              :class="{ active: idx === activeIndex }"
              @click="activeIndex = idx"
            >
              <el-image :src="toProxiedUrl(url)" fit="cover" />
            </div>
          </div>
        </div>

        <div class="info-panel">
          <div class="item-header">
            <div class="title-row">
              <h1 class="item-title">{{ item.title }}</h1>
              <el-tag v-if="item.publishStatus === 0" type="info" effect="plain">已下架</el-tag>
              <el-tag v-else-if="item.auditStatus === 0" type="warning" effect="plain">{{ item.auditStatusDesc }}</el-tag>
              <el-tag v-else-if="item.auditStatus === 2" type="danger" effect="plain">{{ item.auditStatusDesc }}</el-tag>
            </div>
            <div class="item-meta">
              <span>分类：{{ item.categoryName }}</span>
              <span>成色：{{ item.qualityDesc }}</span>
              <span>发布时间：{{ formatTime(item.createTime) }}</span>
            </div>
          </div>

          <div class="price-section">
            <span class="price-label">价格</span>
            <span class="price-value">¥{{ item.price }}</span>
          </div>

          <div class="description-section">
            <h3>商品描述</h3>
            <p class="description-text">{{ item.description || '暂无描述' }}</p>
          </div>

          <div class="seller-section">
            <h3>卖家信息</h3>
            <div class="seller-card" @click="handleViewSeller">
              <el-avatar :size="48" :src="item.sellerAvatar || ''" icon="UserFilled" class="seller-avatar" />
              <div class="seller-info">
                <div class="seller-name">{{ item.sellerName }}</div>
                <div class="seller-credit">
                  <span class="credit-label">信用分</span>
                  <el-rate :model-value="creditToRate(item.sellerCreditScore)" disabled :colors="['#99A9BF', '#F7BA2A', '#FF9900']" />
                  <span class="credit-score">{{ item.sellerCreditScore ?? '—' }}</span>
                </div>
              </div>
              <el-icon class="seller-arrow"><ArrowRight /></el-icon>
            </div>
          </div>

          <div v-if="item.isOwner" class="owner-actions">
            <el-button type="primary" @click="handleEdit"><el-icon><Edit /></el-icon>编辑商品</el-button>
            <el-button type="danger" plain @click="handleDelete"><el-icon><Delete /></el-icon>删除商品</el-button>
          </div>
          <div v-else-if="item.publishStatus === 1 && item.auditStatus === 1" class="buyer-actions">
            <el-button type="primary" size="large" @click="handleChat"><el-icon><ChatDotRound /></el-icon>联系卖家</el-button>
            <el-button type="success" size="large" @click="handleOrder"><el-icon><ShoppingCart /></el-icon>立即购买</el-button>
            <el-button :type="isCollected ? 'warning' : 'default'" size="large" @click="handleCollect">
              <el-icon><Star /></el-icon>{{ isCollected ? '已收藏' : '收藏' }}
            </el-button>
          </div>
          <div v-else-if="!item.isOwner" class="unavailable-notice">
            <el-alert v-if="item.auditStatus === 0" title="商品正在审核中，暂时无法购买" type="info" :closable="false" />
            <el-alert v-else-if="item.auditStatus === 2" title="商品审核未通过，无法购买" type="warning" :closable="false" />
            <el-alert v-else-if="item.publishStatus === 0" title="商品已下架" type="info" :closable="false" />
          </div>

          <div class="action-tip">
            <el-link type="info" :underline="false" @click="showReportDialog = true">
              <el-icon><Warning /></el-icon>举报商品
            </el-link>
          </div>
        </div>
      </div>

      <!-- 商品评价区域 -->
      <div v-if="item" class="comments-section">
        <h2 class="section-title">商品评价 ({{ comments.length }})</h2>
        <div v-if="commentsLoading" class="comments-loading">
          <el-skeleton :rows="3" animated />
        </div>
        <div v-else-if="comments.length > 0" class="comments-list">
          <div v-for="comment in comments" :key="comment.commentId" class="comment-item">
            <div class="comment-header">
              <el-avatar :size="40" icon="UserFilled" />
              <div class="comment-user-info">
                <span class="comment-username">{{ comment.buyerName || `用户#${comment.buyerId}` }}</span>
                <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
              </div>
              <el-rate :model-value="comment.score" disabled :colors="['#f7ba2a', '#f7ba2a', '#ff9900']" />
            </div>
            <div class="comment-content">{{ comment.content }}</div>
          </div>
        </div>
        <el-empty v-else description="暂无评价" :image-size="80" />
      </div>
    </div>

    <div v-else class="not-found">
      <el-empty description="商品不存在或已被删除" />
      <el-button type="primary" @click="router.push('/items')">返回商品列表</el-button>
    </div>

    <el-dialog v-model="showReportDialog" title="举报商品" width="480px">
      <el-form :model="reportForm" label-width="80px">
        <el-form-item label="举报原因">
          <el-input v-model="reportForm.reason" type="textarea" :rows="4"
            placeholder="请详细描述举报原因，帮助我们更快处理" maxlength="200" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showReportDialog = false">取消</el-button>
        <el-button type="danger" :loading="reportLoading" @click="submitReport">提交举报</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showOrderDialog" title="确认订单" width="480px">
      <div class="order-confirm">
        <div class="order-item-info">
          <el-image :src="toProxiedUrl(item?.coverUrl)" fit="cover" class="order-item-img" />
          <div class="order-item-detail">
            <div class="order-item-title">{{ item?.title }}</div>
            <div class="order-item-price">¥{{ item?.price }}</div>
          </div>
        </div>
        <el-divider />
        <el-form :model="orderForm" label-width="100px">
          <el-form-item label="交易方式">
            <el-radio-group v-model="orderForm.tradeType">
              <el-radio :value="1">面交</el-radio>
              <el-radio :value="2">校内快递柜中转</el-radio>
            </el-radio-group>
          </el-form-item>
          <div class="trade-tip">
            <span v-if="orderForm.tradeType === 1">买卖双方在线下约定时间地点进行交易</span>
            <span v-else>双方将物品放入校内快递柜进行中转交接</span>
          </div>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="showOrderDialog = false">取消</el-button>
        <el-button type="success" :loading="orderLoading" @click="submitOrder">确认下单</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import {
  ArrowRight, ChatDotRound, Delete, Edit, ShoppingCart, Star, Warning,
} from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { computed, onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import dayjs from 'dayjs';
import { addCollect, cancelCollectByItemId } from '@/api/modules/collect';
import { getItemDetail, deleteItem } from '@/api/modules/item';
import { createOrder, getItemComments } from '@/api/modules/order';
import { useHistory } from '@/composables/useHistory';
import { toProxiedUrl } from '@/utils/upload';
import type { ItemDetailVO } from '@/types/item';
import type { CommentRecord } from '@/types/order';

const { track } = useHistory();

const route = useRoute();
const router = useRouter();

const itemId = computed(() => route.params.id as string);
const loading = ref(true);
const item = ref<ItemDetailVO | null>(null);
const activeIndex = ref(0);
const isCollected = ref(false);

const showReportDialog = ref(false);
const reportForm = reactive({ reason: '' });
const reportLoading = ref(false);

const showOrderDialog = ref(false);
const orderForm = reactive({ tradeType: 1 });
const orderLoading = ref(false);

const comments = ref<CommentRecord[]>([]);
const commentsLoading = ref(false);

const activeImage = computed(() => {
  if (!item.value?.imageUrls?.length) return toProxiedUrl(item.value?.coverUrl);
  return toProxiedUrl(item.value.imageUrls[activeIndex.value] || item.value.coverUrl);
});

function formatTime(time: string) {
  return dayjs(time).format('YYYY-MM-DD HH:mm');
}

function creditToRate(score: number | null | undefined): number {
  if (!score) return 0;
  if (score >= 90) return 5;
  if (score >= 70) return 4;
  if (score >= 50) return 3;
  if (score >= 30) return 2;
  return 1;
}

async function loadItem() {
  loading.value = true;
  try {
    const res = await getItemDetail(itemId.value);
    item.value = res.data;
    isCollected.value = false;
    // 记录浏览历史
    if (res.data) {
      track(res.data.itemId, res.data.categoryId);
    }
    await loadComments();
  } catch {
    item.value = null;
  } finally {
    loading.value = false;
  }
}

async function loadComments() {
  commentsLoading.value = true;
  try {
    const res = await getItemComments(itemId.value);
    comments.value = res.data || [];
  } catch {
    comments.value = [];
  } finally {
    commentsLoading.value = false;
  }
}

async function handleEdit() {
  router.push({ path: '/publish', query: { itemId: itemId.value } });
}

async function handleDelete() {
  try {
    await ElMessageBox.confirm('确定要删除该商品吗？删除后将无法恢复。', '删除商品', {
      type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消',
    });
    await deleteItem(itemId.value);
    ElMessage.success('删除成功');
    router.push('/profile');
  } catch {
    // cancelled
  }
}

function handleChat() {
  if (!item.value) return;
  router.push({ path: `/chat/room/${item.value.userId}` });
}

function handleViewSeller() {
  if (!item.value) return;
  router.push({ path: `/user/${item.value.userId}` });
}

function handleOrder() {
  showOrderDialog.value = true;
}

async function submitOrder() {
  if (!item.value) return;
  orderLoading.value = true;
  try {
    await createOrder({
      itemId: Number(itemId.value),
      tradeType: orderForm.tradeType,
      price: Number(item.value.price),
    });
    ElMessage.success('下单成功，请在订单中查看详情');
    showOrderDialog.value = false;
    router.push('/orders');
  } catch {
    ElMessage.error('下单失败，请重试');
  } finally {
    orderLoading.value = false;
  }
}

async function handleCollect() {
  if (!item.value) return;
  try {
    if (isCollected.value) {
      await cancelCollectByItemId(itemId.value);
      isCollected.value = false;
      ElMessage.success('已取消收藏');
    } else {
      await addCollect({ itemId: Number(itemId.value) });
      isCollected.value = true;
      ElMessage.success('收藏成功');
    }
  } catch {
    ElMessage.error('操作失败，请重试');
  }
}

async function submitReport() {
  if (!reportForm.reason.trim()) {
    ElMessage.warning('请填写举报原因');
    return;
  }
  reportLoading.value = true;
  try {
    ElMessage.success('举报已提交，我们会尽快处理');
    showReportDialog.value = false;
    reportForm.reason = '';
  } catch {
    ElMessage.error('举报提交失败');
  } finally {
    reportLoading.value = false;
  }
}

onMounted(() => {
  loadItem();
});
</script>

<style scoped>
.loading-state {
  padding: 24px;
  background: var(--color-surface);
  border: var(--border-pixel);
  box-shadow: var(--shadow-hard);
}

.item-detail {
  background: var(--color-surface);
  border: var(--border-pixel);
  border-radius: var(--radius-pixel);
  box-shadow: var(--shadow-hard);
  padding: 32px;
}

.detail-main {
  display: grid;
  grid-template-columns: 460px 1fr;
  gap: 40px;
}

.gallery {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.main-image {
  width: 460px;
  height: 460px;
  overflow: hidden;
  background: #f0ede6;
  border: var(--border-pixel);
  border-radius: var(--radius-pixel);
}

.main-img { width: 100%; height: 100%; }

.img-error {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text-sub);
  background: #f0ede6;
}

.thumbnail-list {
  display: flex;
  gap: 8px;
  overflow-x: auto;
}

.thumbnail {
  width: 68px;
  height: 68px;
  flex-shrink: 0;
  overflow: hidden;
  border: 2px solid #d1cdc4;
  border-radius: var(--radius-pixel);
  cursor: pointer;
  transition: border-color 0.1s;
}

.thumbnail.active { border-color: var(--color-border); box-shadow: var(--shadow-hard-sm); }
.thumbnail .el-image { width: 100%; height: 100%; }

.info-panel {
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.item-header {
  border-bottom: var(--border-pixel);
  padding-bottom: 18px;
}

.title-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.item-title {
  flex: 1;
  margin: 0;
  font-size: 22px;
  font-weight: 800;
  color: var(--color-text);
  line-height: 1.4;
}

.item-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  margin-top: 10px;
  color: var(--color-text-sub);
  font-size: 13px;
  font-weight: 600;
}

.price-section {
  display: flex;
  align-items: center;
  gap: 12px;
  background: #f0ede6;
  border: var(--border-pixel);
  border-radius: var(--radius-pixel);
  padding: 14px 18px;
}

.price-label {
  font-size: 14px;
  font-weight: 700;
  color: var(--color-text-sub);
}

.price-value {
  font-size: 34px;
  font-weight: 800;
  color: var(--color-danger);
  letter-spacing: -1px;
}

.description-section h3,
.seller-section h3 {
  margin: 0 0 10px;
  font-size: 14px;
  font-weight: 800;
  color: var(--color-text);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.description-text {
  margin: 0;
  color: var(--color-text);
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-word;
  font-size: 14px;
}

.seller-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  background: #f9f7f1;
  border: var(--border-pixel);
  border-radius: var(--radius-pixel);
  cursor: pointer;
  box-shadow: var(--shadow-hard-sm);
  transition: box-shadow 0.1s, transform 0.1s;
}

.seller-card:hover {
  box-shadow: 1px 1px 0 var(--color-border);
  transform: translate(1px, 1px);
}

.seller-avatar { flex-shrink: 0; }
.seller-arrow { margin-left: auto; color: var(--color-text-sub); font-size: 16px; }

.seller-info { flex: 1; }

.seller-name {
  font-size: 15px;
  font-weight: 700;
  color: var(--color-text);
}

.seller-credit {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 4px;
}

.credit-label { font-size: 12px; font-weight: 600; color: var(--color-text-sub); }

.credit-score {
  font-size: 13px;
  font-weight: 700;
  color: var(--color-secondary);
  background: var(--color-text);
  padding: 1px 6px;
  border-radius: var(--radius-pixel);
}

.owner-actions,
.buyer-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.unavailable-notice { width: 100%; }

.action-tip { text-align: center; }

.order-confirm { padding: 8px 0; }

.order-item-info {
  display: flex;
  gap: 14px;
  align-items: center;
}

.order-item-img {
  width: 80px;
  height: 80px;
  border: var(--border-pixel);
  border-radius: var(--radius-pixel);
  flex-shrink: 0;
}

.order-item-detail { flex: 1; }

.order-item-title {
  font-size: 15px;
  font-weight: 700;
  color: var(--color-text);
}

.order-item-price {
  margin-top: 8px;
  font-size: 20px;
  font-weight: 800;
  color: var(--color-danger);
}

.trade-tip {
  margin-top: 8px;
  padding: 8px 12px;
  background: #f0ede6;
  border: 1.5px solid var(--color-border);
  border-radius: var(--radius-pixel);
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text);
}

.not-found {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 60px;
  background: var(--color-surface);
  border: var(--border-pixel);
  border-radius: var(--radius-pixel);
  box-shadow: var(--shadow-hard);
}

.comments-section {
  margin-top: 36px;
  padding-top: 28px;
  border-top: var(--border-pixel);
}

.section-title {
  margin: 0 0 20px;
  font-size: 18px;
  font-weight: 800;
  color: var(--color-text);
}

.comments-loading { padding: 16px 0; }

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.comment-item {
  padding: 18px 20px;
  background: #f9f7f1;
  border: var(--border-pixel);
  border-radius: var(--radius-pixel);
  box-shadow: var(--shadow-hard-sm);
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}

.comment-user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.comment-username { font-size: 14px; font-weight: 700; color: var(--color-text); }
.comment-time { font-size: 12px; font-weight: 600; color: var(--color-text-sub); }

.comment-content {
  font-size: 14px;
  color: var(--color-text);
  line-height: 1.7;
  word-break: break-word;
  white-space: pre-wrap;
}
</style>
