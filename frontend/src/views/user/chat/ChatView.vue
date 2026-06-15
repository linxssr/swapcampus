<template>
  <div class="chat-page">
    <section class="chat-layout">
      <aside class="conversation-panel">
        <div class="panel-header">
          <h2>消息</h2>
        </div>
        <div v-if="conversations.length === 0" class="empty-list">
          <div class="empty-state">
            <div class="empty-icon">💬</div>
            <p>暂无会话</p>
          </div>
        </div>
        <div v-else class="conversation-list">
          <RouterLink
            v-for="item in conversations"
            :key="item.peerUid"
            class="conversation-item"
            :class="{ active: Number(route.params.toUid) === item.peerUid }"
            :to="buildRoomPath(item.peerUid)"
            @click="clearUnread(item.peerUid)"
          >
            <div class="avatar-wrap">
              <img v-if="item.peerAvatar" :src="item.peerAvatar" alt="" />
              <span v-else>{{ getAvatarText(item.peerName, item.peerUid) }}</span>
              <i v-if="item.unreadCount > 0" class="unread-dot"></i>
            </div>
            <div class="conv-body">
              <div class="conv-top">
                <strong>{{ item.peerName || `用户${item.peerUid}` }}</strong>
                <time>{{ formatTime(item.lastSendTime) }}</time>
              </div>
              <div class="conv-bottom">
                <span class="preview">{{ getPreview(item) }}</span>
                <em v-if="item.unreadCount > 0" class="badge">{{ item.unreadCount > 99 ? '99+' : item.unreadCount }}</em>
              </div>
            </div>
          </RouterLink>
        </div>
      </aside>

      <main class="room-panel">
        <RouterView v-slot="{ Component }">
          <component
            :is="Component"
            v-if="Component"
            :key="route.fullPath"
            @message-change="loadConversations"
          />
          <div v-else class="room-empty">
            <div class="empty-card">
              <div class="empty-icon-big">💬</div>
              <h3>选择联系人开始聊天</h3>
              <p>从左侧会话列表选择，或从商品详情页发起聊天</p>
            </div>
          </div>
        </RouterView>
      </main>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { getChatConversations } from '@/api/modules/chat';
import { useAuthStore } from '@/stores/modules/auth';
import type { ChatConversationVO } from '@/types/chat';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();
const conversations = ref<ChatConversationVO[]>([]);

const currentUid = computed(() => authStore.userInfo?.userId ?? null);

onMounted(async () => {
  if (!authStore.isLoggedIn) {
    ElMessage.error('请先登录');
    router.push('/login');
    return;
  }
  
  if (!authStore.userInfo) {
    await authStore.fetchUserInfo();
  }
  
  loadConversations();
  handleQueryRedirect();
});

watch(currentUid, (uid) => {
  if (uid) loadConversations();
});

function handleQueryRedirect() {
  const toUid = route.query.toUid;
  if (toUid && !route.params.toUid) {
    router.replace({
      path: `/chat/room/${toUid}`,
      query: { itemId: route.query.itemId as string },
    });
  }
}

async function loadConversations() {
  try {
    const result = await getChatConversations();
    conversations.value = result.data || [];
  } catch (error) {
    console.error('加载会话列表失败:', error);
    conversations.value = [];
  }
}

function buildRoomPath(peerUid: number) {
  return {
    path: `/chat/room/${peerUid}`,
    query: { itemId: route.query.itemId },
  };
}

function clearUnread(peerUid: number) {
  const item = conversations.value.find((c) => c.peerUid === peerUid);
  if (item) item.unreadCount = 0;
}

function getPreview(item: ChatConversationVO) {
  if (item.lastMsgType === 2) return '[图片]';
  if (item.lastMsgType === 3) return '[表情]';
  return item.lastContent || '';
}

function getAvatarText(name: string | undefined, uid: number) {
  return name?.slice(0, 1) || String(uid).slice(-1);
}

function formatTime(value: string) {
  if (!value) return '';
  const d = new Date(value);
  const now = new Date();
  if (d.toDateString() === now.toDateString()) {
    return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
  }
  return `${d.getMonth() + 1}-${d.getDate()}`;
}
</script>

<style scoped>
.chat-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px 16px;
  box-sizing: border-box;
}

.chat-layout {
  display: grid;
  grid-template-columns: 300px 1fr;
  height: calc(100vh - 120px);
  min-height: 600px;
  background: var(--color-surface);
  border: var(--border-pixel);
  border-radius: var(--radius-pixel);
  box-shadow: var(--shadow-hard);
  overflow: hidden;
}

/* ── Sidebar ── */
.conversation-panel {
  display: flex;
  flex-direction: column;
  border-right: var(--border-pixel);
  background: var(--color-surface);
}

.panel-header {
  display: flex;
  align-items: center;
  height: 56px;
  padding: 0 18px;
  border-bottom: var(--border-pixel);
  background: #f0ede6;
  flex-shrink: 0;
}

.panel-header h2 {
  margin: 0;
  font-size: 16px;
  font-weight: 800;
  color: var(--color-text);
  letter-spacing: 0.5px;
}

.empty-list {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.empty-state {
  text-align: center;
  color: var(--color-text-mute);
}

.empty-state .empty-icon {
  font-size: 36px;
  margin-bottom: 8px;
}

.empty-state p {
  margin: 0;
  font-size: 13px;
  font-weight: 600;
}

.conversation-list {
  flex: 1;
  overflow-y: auto;
}

.conversation-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  cursor: pointer;
  text-decoration: none;
  color: inherit;
  border-bottom: 1.5px solid #ede9e0;
  transition: background 0.1s;
}

.conversation-item:hover {
  background: #f9f7f1;
}

.conversation-item.active {
  background: #f0ede6;
  border-left: 3px solid var(--color-primary-dk);
}

.avatar-wrap {
  position: relative;
  flex-shrink: 0;
  width: 44px;
  height: 44px;
  border: 2px solid var(--color-border);
  border-radius: var(--radius-pixel);
  background: #f0ede6;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-text);
  font-weight: 700;
  font-size: 16px;
  overflow: hidden;
}

.avatar-wrap img { width: 100%; height: 100%; object-fit: cover; }

.unread-dot {
  position: absolute;
  top: -3px;
  right: -3px;
  width: 9px;
  height: 9px;
  background: var(--color-danger);
  border: 2px solid var(--color-surface);
  border-radius: 50%;
}

.conv-body { flex: 1; min-width: 0; }

.conv-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 6px;
}

.conv-top strong {
  font-size: 14px;
  font-weight: 700;
  color: var(--color-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.conv-top time {
  flex-shrink: 0;
  font-size: 11px;
  font-weight: 600;
  color: var(--color-text-mute);
}

.conv-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 4px;
}

.conv-bottom .preview {
  flex: 1;
  font-size: 12px;
  font-weight: 500;
  color: var(--color-text-sub);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.conv-bottom .badge {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  margin-left: 6px;
  font-size: 10px;
  font-weight: 700;
  font-style: normal;
  color: #fff;
  background: var(--color-danger);
  border: 1.5px solid var(--color-border);
  border-radius: var(--radius-pixel);
}

/* ── Room panel ── */
.room-panel {
  min-width: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.room-empty {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f9f7f1;
  background-image:
    repeating-linear-gradient(0deg, transparent, transparent 31px, rgba(45,48,71,0.04) 31px, rgba(45,48,71,0.04) 32px),
    repeating-linear-gradient(90deg, transparent, transparent 31px, rgba(45,48,71,0.04) 31px, rgba(45,48,71,0.04) 32px);
}

.empty-card {
  text-align: center;
  padding: 32px;
  border: var(--border-pixel);
  border-radius: var(--radius-pixel);
  background: var(--color-surface);
  box-shadow: var(--shadow-hard);
}

.empty-icon-big { font-size: 48px; margin-bottom: 14px; }

.empty-card h3 {
  margin: 0 0 8px;
  font-size: 17px;
  font-weight: 800;
  color: var(--color-text);
}

.empty-card p {
  margin: 0;
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text-sub);
}
</style>
