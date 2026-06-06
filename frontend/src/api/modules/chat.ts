import { get, post, put } from '@/utils/request';
import type { ChatConversationVO, ChatMessageVO } from '@/types/chat';

export function getChatConversations(uid?: number | string) {
  return get<ChatConversationVO[]>('/chat/conversations', {
    params: uid ? { uid } : undefined,
  });
}

export function uploadChatImage(data: FormData) {
  return post<string>('/chat/upload', data);
}

export function getChatHistory(toUid: number | string, uid?: number | string) {
  return get<ChatMessageVO[]>(`/chat/history/${toUid}`, {
    params: uid ? { uid } : undefined,
  });
}

export function markChatMessageRead(msgId: number | string, uid?: number | string) {
  return put<void>(`/chat/read/${msgId}`, undefined, {
    params: uid ? { uid } : undefined,
  });
}
