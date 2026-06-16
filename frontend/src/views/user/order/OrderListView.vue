<template>
  <div class="page-container order-page">
    <PageHeader title="我的订单" />

    <el-card shadow="never" class="order-card">
      <el-tabs v-model="activeType" @tab-change="loadOrders">
        <el-tab-pane label="我买到的" name="buy" />
        <el-tab-pane label="我卖出的" name="sell" />
      </el-tabs>

      <el-table v-loading="loading" :data="orders" border stripe empty-text="暂无订单">
        <el-table-column prop="orderNo" label="订单号" min-width="180" />
        <el-table-column label="商品" min-width="200">
          <template #default="{ row }">
            <div class="item-cell">
              <el-image v-if="row.itemCover" :src="row.itemCover" fit="cover" class="item-thumb" />
              <span>{{ row.itemTitle || `商品#${row.itemId}` }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="对方" width="120">
          <template #default="{ row }">
            {{ row.peerName || (activeType === 'buy' ? `卖家#${row.sellerId}` : `买家#${row.buyerId}`) }}
          </template>
        </el-table-column>
        <el-table-column label="成交价" width="100">
          <template #default="{ row }">¥{{ row.price }}</template>
        </el-table-column>
        <el-table-column label="交易方式" width="110">
          <template #default="{ row }">
            {{ row.tradeType === 2 ? '校内快递柜' : '面交' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.orderStatus)">
              {{ getStatusText(row.orderStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="160">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="290" fixed="right">
          <template #default="{ row }">
            <el-space wrap>
              <el-button size="small" @click="viewDetail(row)">详情</el-button>
              <el-button size="small" @click="contactPeer(row)">联系对方</el-button>
              <el-button
                v-if="activeType === 'sell' && row.orderStatus === 1"
                size="small"
                type="primary"
                @click="handleConfirmOrder(row)"
              >确认订单</el-button>
              <el-button
                v-if="activeType === 'buy' && row.orderStatus === 2"
                size="small"
                type="success"
                @click="confirmFinish(row)"
              >确认收货</el-button>
              <el-button
                v-if="row.orderStatus === 1 || row.orderStatus === 2"
                size="small"
                type="warning"
                @click="confirmCancel(row)"
              >取消订单</el-button>
              <el-button
                v-if="activeType === 'buy' && row.orderStatus === 3"
                size="small"
                type="primary"
                @click="openCommentDialog(row)"
              >评价</el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 订单详情弹窗（含评价历史） -->
    <el-dialog v-model="detailVisible" title="订单详情" width="600px" destroy-on-close>
      <template v-if="currentOrder">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="商品">
            <div class="detail-item-cell">
              <el-image
                v-if="currentOrder.itemCover"
                :src="currentOrder.itemCover"
                fit="cover"
                class="detail-item-thumb"
              />
              <span>{{ currentOrder.itemTitle || `商品#${currentOrder.itemId}` }}</span>
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="对方用户">
            {{ currentOrder.peerName || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="成交价">¥{{ currentOrder.price }}</el-descriptions-item>
          <el-descriptions-item label="交易方式">
            {{ currentOrder.tradeType === 2 ? '校内快递柜中转' : '线下面交' }}
          </el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="getStatusTagType(currentOrder.orderStatus)">
              {{ getStatusText(currentOrder.orderStatus) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatTime(currentOrder.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="完成时间">
            {{ currentOrder.finishTime ? formatTime(currentOrder.finishTime) : '-' }}
          </el-descriptions-item>
        </el-descriptions>

        <div class="comment-section">
          <div class="comment-section-title">交易评价</div>
          <div v-if="detailCommentLoading" class="comment-loading">
            <el-skeleton :rows="2" animated />
          </div>
          <div v-else-if="currentComment" class="comment-card">
            <div class="comment-header">
              <el-avatar :size="36" icon="UserFilled" />
              <div class="comment-meta">
                <span class="comment-buyer">{{ currentComment.buyerName || `用户#${currentComment.buyerId}` }}</span>
                <span class="comment-time">{{ formatTime(currentComment.createTime) }}</span>
              </div>
              <el-rate
                :model-value="currentComment.score"
                disabled
                :colors="['#f7ba2a', '#f7ba2a', '#ff9900']"
              />
            </div>
            <div class="comment-content">{{ currentComment.content }}</div>
          </div>
          <el-empty v-else description="该订单暂无评价" :image-size="60" />
        </div>
      </template>
    </el-dialog>

    <!-- 评价弹窗 -->
    <el-dialog v-model="commentVisible" title="评价卖家" width="520px">
      <el-form ref="commentFormRef" :model="commentForm" :rules="commentRules" label-width="90px">
        <el-form-item label="评分" prop="score">
          <el-rate v-model="commentForm.score" :max="5" show-score />
        </el-form-item>
        <el-form-item label="评价内容" prop="content">
          <el-input
            v-model="commentForm.content"
            type="textarea"
            :rows="4"
            maxlength="500"
            show-word-limit
            placeholder="请描述交易体验，例如沟通是否及时、物品是否符合描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="commentVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitComment">提交评价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus';
import PageHeader from '@/components/common/PageHeader.vue';
import { addComment, cancelOrder, confirmOrder, finishOrder, getMyOrders, getOrderComment } from '@/api/modules/order';
import type { AddCommentPayload, CommentRecord, OrderQueryType, OrderRecord } from '@/types/order';

const router = useRouter();

const activeType = ref<OrderQueryType>('buy');
const loading = ref(false);
const orders = ref<OrderRecord[]>([]);

const detailVisible = ref(false);
const currentOrder = ref<OrderRecord | null>(null);
const currentComment = ref<CommentRecord | null>(null);
const detailCommentLoading = ref(false);

const commentVisible = ref(false);
const commentFormRef = ref<FormInstance>();
const submitting = ref(false);
const commentForm = reactive<AddCommentPayload>({ orderId: 0, score: 5, content: '' });

const commentRules: FormRules<AddCommentPayload> = {
  score: [{ required: true, message: '请选择评分', trigger: 'change' }],
  content: [
    { required: true, message: '请填写评价内容', trigger: 'blur' },
    { min: 2, max: 500, message: '评价内容长度应在 2 到 500 字之间', trigger: 'blur' },
  ],
};

onMounted(loadOrders);

async function loadOrders() {
  loading.value = true;
  try {
    const res = await getMyOrders(activeType.value);
    orders.value = (res.data || []) as OrderRecord[];
  } catch {
    ElMessage.error('加载订单失败');
    orders.value = [];
  } finally {
    loading.value = false;
  }
}

async function viewDetail(row: OrderRecord) {
  currentOrder.value = row;
  currentComment.value = null;
  detailVisible.value = true;

  if (row.orderId) {
    detailCommentLoading.value = true;
    try {
      const res = await getOrderComment(row.orderId);
      currentComment.value = (res.data as CommentRecord) || null;
    } catch {
      currentComment.value = null;
    } finally {
      detailCommentLoading.value = false;
    }
  }
}

function contactPeer(row: OrderRecord) {
  const peerId = activeType.value === 'buy' ? row.sellerId : row.buyerId;
  if (!peerId) { ElMessage.warning('无法获取对方用户ID'); return; }
  router.push(`/chat/room/${peerId}`);
}

async function handleConfirmOrder(row: OrderRecord) {
  try {
    await ElMessageBox.confirm('确认接受该订单吗？确认后买家可进行取件。', '确认订单', { type: 'info' });
    await confirmOrder(row.orderNo);
    ElMessage.success('订单已确认');
    loadOrders();
  } catch { /* cancelled */ }
}

async function confirmFinish(row: OrderRecord) {
  try {
    await ElMessageBox.confirm('确认已收到商品吗？确认后订单将完成。', '确认收货', { type: 'success' });
    await finishOrder(row.orderNo);
    ElMessage.success('确认收货成功');
    loadOrders();
  } catch { /* cancelled */ }
}

async function confirmCancel(row: OrderRecord) {
  try {
    await ElMessageBox.confirm('确定要取消订单吗？取消后商品将重新上架。', '取消订单', { type: 'warning' });
    await cancelOrder(row.orderNo);
    ElMessage.success('订单已取消');
    loadOrders();
  } catch { /* cancelled */ }
}

function openCommentDialog(row: OrderRecord) {
  commentForm.orderId = row.orderId;
  commentForm.score = 5;
  commentForm.content = '';
  commentVisible.value = true;
}

async function submitComment() {
  await commentFormRef.value?.validate();
  submitting.value = true;
  try {
    await addComment(commentForm);
    ElMessage.success('评价成功');
    commentVisible.value = false;
    loadOrders();
  } catch {
    ElMessage.error('评价失败');
  } finally {
    submitting.value = false;
  }
}

function getStatusText(status: number) {
  return ({ 1: '待确认', 2: '待取件', 3: '已完成', 4: '已取消' } as Record<number, string>)[status] || '未知';
}

function getStatusTagType(status: number): 'warning' | 'primary' | 'success' | 'info' {
  return (
    { 1: 'warning', 2: 'primary', 3: 'success', 4: 'info' } as Record<
      number,
      'warning' | 'primary' | 'success' | 'info'
    >
  )[status] || 'info';
}

function formatTime(time?: string | null) {
  if (!time) return '-';
  return new Date(time).toLocaleString('zh-CN', { hour12: false });
}
</script>

<style scoped>
.order-page { padding-bottom: 48px; }
.order-card { border-radius: 16px; }

.item-cell { display: flex; align-items: center; gap: 8px; }
.item-thumb { width: 40px; height: 40px; border-radius: 4px; flex-shrink: 0; }

.detail-item-cell { display: flex; align-items: center; gap: 10px; }
.detail-item-thumb { width: 52px; height: 52px; border-radius: 6px; flex-shrink: 0; }

.comment-section { margin-top: 24px; }

.comment-section-title {
  font-size: 15px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 14px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f3f4f6;
}

.comment-loading { padding: 8px 0; }

.comment-card {
  background: #f9fafb;
  border-radius: 10px;
  padding: 16px;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.comment-meta {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.comment-buyer { font-size: 14px; font-weight: 600; color: #1f2937; }
.comment-time { font-size: 12px; color: #9ca3af; }

.comment-content {
  font-size: 14px;
  color: #4b5563;
  line-height: 1.7;
  word-break: break-word;
}
</style>
