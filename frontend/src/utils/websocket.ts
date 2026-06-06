import { StorageKey } from '@/constants/storage';
import type { ChatMessagePayload } from '@/types/chat';

export function createChatSocket(tokenOverride?: string) {
  const token = tokenOverride || localStorage.getItem(StorageKey.Token) || '';
  const baseUrl = import.meta.env.VITE_WS_BASE_URL;
  const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws';
  const host = window.location.host;
  const path = `${baseUrl}/chat?token=${encodeURIComponent(token)}`;

  return new WebSocket(`${protocol}://${host}${path}`);
}

export function sendChatMessage(socket: WebSocket, payload: ChatMessagePayload) {
  socket.send(JSON.stringify(payload));
}
