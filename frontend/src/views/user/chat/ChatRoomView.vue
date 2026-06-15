<template>
  <section class="chat-room">
    <header class="room-header">
      <div class="peer-info">
        <div class="peer-avatar">{{ peerName.slice(0, 1) }}</div>
        <div class="peer-meta">
          <h3>{{ peerName }}</h3>
          <p class="status">{{ connected ? '在线' : '连接中...' }}</p>
        </div>
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
  background: #fff;
}

.room-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
  padding: 0 20px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
  flex-shrink: 0;
}

.peer-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.peer-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #0095ff, #33b6ff);
  color: #fff;
  font-weight: 600;
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.peer-meta h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
  color: #1a1a1a;
}

.peer-meta .status {
  margin: 2px 0 0;
  font-size: 12px;
  color: #52c41a;
}

.close-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  text-decoration: none;
  color: #999;
  transition: background 0.15s;
}

.close-btn:hover {
  background: #f5f5f5;
}

.close-icon {
  font-size: 24px;
  line-height: 1;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px 20px;
  background: #f5f5f5;
}

.message-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.empty-hint {
  color: #bbb;
  font-size: 14px;
}

.time-divider {
  display: flex;
  justify-content: center;
  margin: 16px 0;
}

.time-divider span {
  padding: 4px 12px;
  font-size: 12px;
  color: #999;
  background: #e8e8e8;
  border-radius: 12px;
}

.message-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 16px;
}

.message-row.mine {
  flex-direction: row-reverse;
}

.msg-avatar {
  flex-shrink: 0;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #0095ff, #33b6ff);
  color: #fff;
  font-weight: 600;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.bubble-wrap {
  position: relative;
  display: flex;
  flex-direction: column;
  max-width: 65%;
}

.message-row.mine .bubble-wrap {
  align-items: flex-end;
}

.unread-indicator {
  position: absolute;
  top: -4px;
  right: -4px;
  width: 8px;
  height: 8px;
  background: #ff3b30;
  border-radius: 50%;
  z-index: 1;
}

.bubble {
  padding: 10px 14px;
  border-radius: 14px;
  background: #fff;
  color: #1a1a1a;
  word-break: break-word;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
}

.bubble.bubble-img {
  padding: 4px;
  background: transparent;
  box-shadow: none;
}

.message-row.mine .bubble {
  background: #0095ff;
  color: #fff;
}

.message-row.mine .bubble.bubble-img {
  background: transparent;
}

.msg-text {
  font-size: 14px;
  line-height: 1.5;
  white-space: pre-wrap;
}

.msg-image {
  display: block;
  width: 200px;
  max-height: 240px;
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
}

.read-status {
  margin-top: 4px;
  font-size: 11px;
  color: #bbb;
}

.input-bar {
  position: relative;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  background: #fff;
  border-top: 1px solid #f0f0f0;
  flex-shrink: 0;
}

.upload-wrapper {
  display: inline-flex;
  flex-shrink: 0;
}

.tool-btn {
  width: 38px;
  height: 38px;
  border: none;
  background: #f5f5f5;
  border-radius: 50%;
  font-size: 18px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.15s;
  flex-shrink: 0;
}

.tool-btn:hover {
  background: #eee;
}

.emoji-panel {
  position: absolute;
  bottom: 70px;
  left: 16px;
  display: grid;
  grid-template-columns: repeat(6, 40px);
  gap: 6px;
  padding: 12px;
  background: #fff;
  border: 1px solid #f0f0f0;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  z-index: 10;
}

.emoji-btn {
  width: 40px;
  height: 40px;
  border: none;
  background: #f8f8f8;
  border-radius: 8px;
  font-size: 20px;
  cursor: pointer;
  transition: background 0.15s, transform 0.1s;
}

.emoji-btn:hover {
  background: #eef6ff;
  transform: scale(1.1);
}

.msg-input {
  flex: 1;
  height: 42px;
  padding: 10px 16px;
  border: 1px solid #e5e5e5;
  border-radius: 21px;
  outline: none;
  resize: none;
  font-size: 14px;
  line-height: 1.5;
  transition: border-color 0.15s;
  min-width: 0;
}

.msg-input:focus {
  border-color: #0095ff;
}

.msg-input::placeholder {
  color: #bbb;
}

.send-btn {
  height: 42px;
  padding: 0 20px;
  border: none;
  background: #0095ff;
  color: #fff;
  font-size: 14px;
  font-weight: 500;
  border-radius: 21px;
  cursor: pointer;
  transition: background 0.15s;
  flex-shrink: 0;
}

.send-btn:hover:not(:disabled) {
  background: #0080e0;
}

.send-btn:disabled {
  background: #b8dcff;
  cursor: not-allowed;
}
</style>
