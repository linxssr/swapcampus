export interface ChatMessagePayload {
  toUid: number | string;
  content?: string;
  msgType?: number;
  imageUrl?: string;
}

export interface ChatMessageVO {
  msgId: number;
  fromUid: number;
  toUid: number;
  content: string;
  msgType: number;
  imageUrl?: string;
  sendTime: string;
  isRead: number;
}

export interface ChatConversationVO {
  peerUid: number;
  peerName: string;
  peerAvatar?: string;
  lastMsgId: number;
  lastContent: string;
  lastMsgType: number;
  lastSendTime: string;
  unreadCount: number;
}
