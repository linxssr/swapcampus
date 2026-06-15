<template>
  <section class="chat-room">
    <header class="room-header">
      <div class="peer-info" @click="goToUserHome">
        <div class="peer-avatar">{{ peerName.slice(0, 1) }}</div>
        <div class="peer-meta">
          <h3>{{ peerName }}</h3>
          <p class="status">{{ connected ? '在线' : '连接中...' }}</p>
        </div>
        <div class="go-profile-icon">›</div>
      </div>
      <RouterLink class="close-btn" :to="{ path: '/chat' }">
        <span class="close-icon">&times;</span>
      </RouterLink>
    </header>

    <main ref="messagePanelRef" class="message-list">
      <div v-if="groupedMessages.length === 0" class="message-empty">
        <div class="empty-hint">暂无消息，开始聊天吧</div>
      </div>
      <template v-for="group in groupedMessages" :key="group.date">
        <div class="time-divider">
          <span>{{ group.date }}</span>
        </div>
        <div
          v-for="msg in group.items"
          :key="msg.msgId"
          class="message-row"
          :class="{ mine: msg.fromUid === currentUid }"
        >
          <div class="msg-avatar">{{ msg.fromUid === currentUid ? '我' : peerName.slice(0, 1) }}</div>
          <div class="bubble-wrap">
            <i v-if="msg.toUid === currentUid && msg.isRead === 0" class="unread-indicator"></i>
            <div class="bubble" :class="{ 'bubble-img': isImageMessage(msg) }">
              <el-image
                v-if="isImageMessage(msg)"
                class="msg-image"
                :src="msg.imageUrl"
                :preview-src-list="[msg.imageUrl!]"
                fit="cover"
                preview-teleported
              />
              <span v-else class="msg-text">{{ msg.content }}</span>
            </div>
            <span v-if="msg.fromUid === currentUid" class="read-status">
              {{ msg.isRead === 1 ? '已读' : '未读' }}
            </span>
          </div>
        </div>
      </template>
    </main>

    <footer class="input-bar">
      <button class="tool-btn" type="button" @click="toggleEmoji">&#x1F60A;</button>
      <div class="upload-wrapper">
        <UploadImg @uploaded="sendImage">
          <button class="tool-btn" type="button">&#x1F4F7;</button>
        </UploadImg>
      </div>
      <div v-if="showEmojiPanel" class="emoji-panel">
        <button
          v-for="emoji in emojis"
          :key="emoji"
          type="button"
          class="emoji-btn"
          @click="appendEmoji(emoji)"
        >
          {{ emoji }}
        </button>
      </div>
      <textarea
        v-model="inputText"
        class="msg-input"
        placeholder="输入消息..."
        @keydown.enter.exact.prevent="sendText"
      ></textarea>
      <button
        class="send-btn"
        type="button"
        :disabled="!inputText.trim()"
        @click="sendText"
      >
        发送
      </button>
    </footer>
  </section>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import UploadImg from '@/components/upload/UploadImg.vue';
import { getChatHistory, markChatMessageRead } from '@/api/modules/chat';
import { getUserProfile } from '@/api/modules/user';
import { useAuthStore } from '@/stores/modules/auth';
import { StorageKey } from '@/constants/storage';
import type { ChatMessagePayload, ChatMessageVO } from '@/types/chat';

const emit = defineEmits<{
  messageChange: [];
}>();

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

const currentUid = computed(() => authStore.userInfo?.userId ?? null);
const toUid = Number(route.params.toUid || route.query.toUid || '0');
const peerName = ref(`用户${toUid}`);

const inputText = ref('');
const connected = ref(false);
const showEmojiPanel = ref(false);
const messages = ref<ChatMessageVO[]>([]);
const socket = ref<WebSocket | null>(null);
const messagePanelRef = ref<HTMLElement | null>(null);

const emojis = [
  '\u{1F60A}', '\u{1F602}', '\u{1F44D}', '\u2764\uFE0F',
  '\u{1F389}', '\u{1F62D}', '\u{1F60D}', '\u{1F64F}',
  '\u{1F914}', '\u{1F44B}', '\u{1F525}', '\u2728',
];

function isImageMessage(msg: ChatMessageVO): boolean {
  return Number(msg.msgType) === 2 && Boolean(msg.imageUrl);
}

const groupedMessages = computed(() => {
  const groups = new Map<string, ChatMessageVO[]>();
  messages.value.forEach((m) => {
    const date = formatDate(m.sendTime);
    groups.set(date, [...(groups.get(date) || []), m]);
  });
  return Array.from(groups.entries()).map(([date, items]) => ({ date, items }));
});

onMounted(async () => {
  if (!authStore.isLoggedIn) {
    ElMessage.error('请先登录');
    router.push('/login');
    return;
  }

  if (!authStore.userInfo) {
    await authStore.fetchUserInfo();
  }

  if (!currentUid.value) {
    ElMessage.error('获取用户信息失败，请重新登录');
    router.push('/login');
    return;
  }

  if (currentUid.value === toUid) {
    ElMessage.error('不能和自己聊天');
    router.push('/chat');
    return;
  }

  try {
    const result = await getUserProfile(toUid);
    if (result.code === 200 && result.data) {
      peerName.value = result.data.username || `用户${toUid}`;
    }
  } catch (error) {
    console.error('获取对方用户信息失败:', error);
  }

  await loadHistory();
  connectSocket();
});

onBeforeUnmount(() => {
  socket.value?.close();
});

async function loadHistory() {
  if (!currentUid.value) return;
  
  try {
    const result = await getChatHistory(toUid);
    messages.value = result.data || [];
    await scrollToBottom();
    emit('messageChange');
  } catch (error) {
    console.error('加载聊天记录失败:', error);
    messages.value = [];
  }
}

function connectSocket() {
  if (!currentUid.value) return;
  
  socket.value?.close();
  const token = localStorage.getItem(StorageKey.Token);
  if (!token) {
    ElMessage.error('未找到登录凭证');
    return;
  }
  
  const wsUrl = `ws://localhost:8080/ws/chat?token=${encodeURIComponent(token)}`;
  socket.value = new WebSocket(wsUrl);

  socket.value.onopen = () => {
    connected.value = true;
  };

  socket.value.onmessage = async (event) => {
    try {
      const payload = JSON.parse(event.data);
      if (payload.type === 'CONNECTED') return;

      const msg = payload as ChatMessageVO;
      if (!isCurrentRoomMessage(msg)) {
        emit('messageChange');
        return;
      }

      upsertMessage(msg);
      if (msg.toUid === currentUid.value && msg.isRead === 0) {
        await markChatMessageRead(msg.msgId);
        msg.isRead = 1;
      }
      await scrollToBottom();
      emit('messageChange');
    } catch {
      // ignore
    }
  };

  socket.value.onerror = () => {
    connected.value = false;
    ElMessage.error('聊天连接异常，请确认后端已启动');
  };

  socket.value.onclose = () => {
    connected.value = false;
  };
}

function isCurrentRoomMessage(msg: ChatMessageVO) {
  return [msg.fromUid, msg.toUid].includes(currentUid.value!) && [msg.fromUid, msg.toUid].includes(toUid);
}

function sendText() {
  const content = inputText.value.trim();
  if (!content) return;
  sendPayload({ toUid, content, msgType: 1 });
  inputText.value = '';
}

function sendImage(url: string) {
  sendPayload({ toUid, content: '[图片]', msgType: 2, imageUrl: url });
}

function toggleEmoji() {
  showEmojiPanel.value = !showEmojiPanel.value;
}

function appendEmoji(emoji: string) {
  inputText.value += emoji;
  showEmojiPanel.value = false;
}

function sendPayload(payload: ChatMessagePayload) {
  if (!socket.value || socket.value.readyState !== WebSocket.OPEN) {
    ElMessage.warning('WebSocket 未连接');
    return;
  }
  socket.value.send(JSON.stringify(payload));
}

function upsertMessage(msg: ChatMessageVO) {
  const idx = messages.value.findIndex((m) => m.msgId === msg.msgId);
  if (idx >= 0) {
    messages.value[idx] = msg;
  } else {
    messages.value.push(msg);
  }
}

async function scrollToBottom() {
  await nextTick();
  if (messagePanelRef.value) {
    messagePanelRef.value.scrollTop = messagePanelRef.value.scrollHeight;
  }
}

function goToUserHome() {
  router.push(`/user/${toUid}`);
}

function formatDate(value: string) {
  if (!value) return '今天';
  const d = new Date(value);
  const now = new Date();
  if (d.toDateString() === now.toDateString()) return '今天';
  const yesterday = new Date(now);
  yesterday.setDate(yesterday.getDate() - 1);
  if (d.toDateString() === yesterday.toDateString()) return '昨天';
  return `${d.getMonth() + 1}月${d.getDate()}日`;
}
</script>

<style scoped>
.chat-room {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: var(--color-surface);
}

/* ── Header ── */
.room-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 56px;
  padding: 0 16px;
  background: #f0ede6;
  border-bottom: var(--border-pixel);
  flex-shrink: 0;
}

.peer-info {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 5px 10px;
  border: 2px solid transparent;
  border-radius: var(--radius-pixel);
  transition: border-color 0.1s, background 0.1s;
}

.peer-info:hover {
  border-color: var(--color-primary-dk);
  background: rgba(157, 191, 158, 0.15);
}

.go-profile-icon {
  margin-left: 4px;
  font-size: 18px;
  color: var(--color-text-sub);
  transition: color 0.1s;
}

.peer-info:hover .go-profile-icon {
  color: var(--color-primary-dk);
}

.peer-avatar {
  width: 34px;
  height: 34px;
  border: 2px solid var(--color-border);
  border-radius: var(--radius-pixel);
  background: #f0ede6;
  color: var(--color-text);
  font-weight: 700;
  font-size: 13px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.peer-meta h3 {
  margin: 0;
  font-size: 14px;
  font-weight: 700;
  color: var(--color-text);
}

.peer-meta .status {
  margin: 2px 0 0;
  font-size: 11px;
  font-weight: 600;
  color: var(--color-primary-dk);
}

.close-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  border: var(--border-pixel);
  border-radius: var(--radius-pixel);
  text-decoration: none;
  color: var(--color-text-sub);
  font-size: 20px;
  transition: border-color 0.1s, color 0.1s;
}

.close-btn:hover {
  border-color: var(--color-danger);
  color: var(--color-danger);
}

.close-icon { line-height: 1; }

/* ── Message list ── */
.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px 20px;
  background: var(--color-bg);
  background-image:
    repeating-linear-gradient(0deg, transparent, transparent 31px, rgba(45,48,71,0.04) 31px, rgba(45,48,71,0.04) 32px),
    repeating-linear-gradient(90deg, transparent, transparent 31px, rgba(45,48,71,0.04) 31px, rgba(45,48,71,0.04) 32px);
}

.message-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.empty-hint {
  color: var(--color-text-mute);
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 1px;
  border: 2px dashed var(--color-text-mute);
  padding: 12px 24px;
  border-radius: var(--radius-pixel);
}

.time-divider {
  display: flex;
  justify-content: center;
  margin: 14px 0;
}

.time-divider span {
  padding: 3px 12px;
  font-size: 11px;
  font-weight: 700;
  color: var(--color-text-sub);
  background: var(--color-surface);
  border: 1.5px solid var(--color-border);
  border-radius: var(--radius-pixel);
}

.message-row {
  display: flex;
  align-items: flex-end;
  gap: 10px;
  margin-bottom: 14px;
}

.message-row.mine { flex-direction: row-reverse; }

/* ── Avatars ── */
.msg-avatar {
  flex-shrink: 0;
  width: 34px;
  height: 34px;
  border: 2px solid var(--color-border);
  border-radius: var(--radius-pixel);
  background: #f0ede6;
  color: var(--color-text);
  font-weight: 700;
  font-size: 13px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.message-row.mine .msg-avatar {
  background: var(--color-primary);
}

/* ── Bubble wrap ── */
.bubble-wrap {
  position: relative;
  display: flex;
  flex-direction: column;
  max-width: 62%;
}

.message-row.mine .bubble-wrap { align-items: flex-end; }

.unread-indicator {
  position: absolute;
  top: -4px;
  right: -4px;
  width: 8px;
  height: 8px;
  background: var(--color-danger);
  border: 2px solid var(--color-bg);
  border-radius: 50%;
  z-index: 1;
}

/* ── Bubbles with triangle pointers ── */
.bubble {
  position: relative;
  padding: 10px 14px;
  border: var(--border-pixel);
  border-radius: var(--radius-pixel);
  background: var(--color-surface);
  color: var(--color-text);
  word-break: break-word;
  box-shadow: var(--shadow-hard-sm);
}

/* triangle pointer — left (peer) */
.message-row:not(.mine) .bubble::before {
  content: '';
  position: absolute;
  left: -10px;
  bottom: 10px;
  border-top: 6px solid transparent;
  border-bottom: 6px solid transparent;
  border-right: 8px solid var(--color-border);
}

.message-row:not(.mine) .bubble::after {
  content: '';
  position: absolute;
  left: -7px;
  bottom: 11px;
  border-top: 5px solid transparent;
  border-bottom: 5px solid transparent;
  border-right: 7px solid var(--color-surface);
}

/* triangle pointer — right (mine) */
.message-row.mine .bubble {
  background: var(--color-primary);
  border-color: var(--color-border);
  color: var(--color-text);
  box-shadow: var(--shadow-hard-sm);
}

.message-row.mine .bubble::before {
  content: '';
  position: absolute;
  right: -10px;
  bottom: 10px;
  border-top: 6px solid transparent;
  border-bottom: 6px solid transparent;
  border-left: 8px solid var(--color-border);
}

.message-row.mine .bubble::after {
  content: '';
  position: absolute;
  right: -7px;
  bottom: 11px;
  border-top: 5px solid transparent;
  border-bottom: 5px solid transparent;
  border-left: 7px solid var(--color-primary);
}

.bubble.bubble-img {
  padding: 4px;
  background: transparent;
  border: none;
  box-shadow: none;
}

.bubble.bubble-img::before,
.bubble.bubble-img::after { display: none; }

.msg-text {
  font-size: 14px;
  line-height: 1.6;
  white-space: pre-wrap;
  font-weight: 500;
}

.msg-image {
  display: block;
  width: 190px;
  max-height: 230px;
  border: var(--border-pixel);
  border-radius: var(--radius-pixel);
  overflow: hidden;
  cursor: pointer;
}

.read-status {
  margin-top: 4px;
  font-size: 10px;
  font-weight: 600;
  color: var(--color-text-mute);
}

/* ── Input bar ── */
.input-bar {
  position: relative;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  background: #f0ede6;
  border-top: var(--border-pixel);
  flex-shrink: 0;
}

.upload-wrapper {
  display: inline-flex;
  flex-shrink: 0;
}

.tool-btn {
  width: 36px;
  height: 36px;
  border: var(--border-pixel);
  background: var(--color-surface);
  border-radius: var(--radius-pixel);
  font-size: 17px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: border-color 0.1s, background 0.1s;
  flex-shrink: 0;
  color: var(--color-text-sub);
}

.tool-btn:hover {
  border-color: var(--color-primary-dk);
  background: var(--color-primary);
  color: var(--color-text);
}

.emoji-panel {
  position: absolute;
  bottom: 64px;
  left: 14px;
  display: grid;
  grid-template-columns: repeat(6, 38px);
  gap: 4px;
  padding: 10px;
  background: var(--color-surface);
  border: var(--border-pixel);
  border-radius: var(--radius-pixel);
  box-shadow: var(--shadow-hard);
  z-index: 10;
}

.emoji-btn {
  width: 38px;
  height: 38px;
  border: 2px solid transparent;
  background: #f9f7f1;
  border-radius: var(--radius-pixel);
  font-size: 19px;
  cursor: pointer;
  transition: border-color 0.1s, transform 0.1s;
}

.emoji-btn:hover {
  border-color: var(--color-primary-dk);
  transform: scale(1.05);
}

.msg-input {
  flex: 1;
  height: 40px;
  padding: 8px 14px;
  border: var(--border-pixel);
  border-radius: var(--radius-pixel);
  outline: none;
  resize: none;
  font-size: 14px;
  font-weight: 500;
  line-height: 1.5;
  background: var(--color-surface);
  color: var(--color-text);
  transition: border-color 0.1s;
  min-width: 0;
}

.msg-input:focus { border-color: var(--color-primary-dk); }

.msg-input::placeholder { color: var(--color-text-mute); }

.msg-input:focus { border-color: var(--color-primary); }

.msg-input::placeholder { color: #3d4165; }

.send-btn {
  height: 40px;
  padding: 0 18px;
  border: 2px solid var(--color-primary-dk);
  background: var(--color-primary);
  color: #2D3047;
  font-size: 13px;
  font-weight: 700;
  border-radius: 2px;
  cursor: pointer;
  box-shadow: 2px 2px 0 var(--color-primary-dk);
  transition: box-shadow 0.1s, transform 0.1s;
  flex-shrink: 0;
  letter-spacing: 0.5px;
}

.send-btn:hover:not(:disabled) {
  box-shadow: 1px 1px 0 var(--color-primary-dk);
  transform: translate(1px, 1px);
}

.send-btn:active:not(:disabled) {
  box-shadow: none;
  transform: translate(2px, 2px);
}

.send-btn:disabled {
  background: #3d4165;
  border-color: #3d4165;
  color: #6b7280;
  cursor: not-allowed;
  box-shadow: none;
}
</style>
