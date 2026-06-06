export interface ChatMessagePayload {
  toUid: number | string;
  content?: string;
  msgType?: 1 | 2 | 3;
  imageUrl?: string;
}

export interface ChatMessageVO {
  msgId: number;
  fromUid: number;
  toUid: number;
  content: string;
  msgType: 1 | 2 | 3;
  imageUrl?: string;
  sendTime: string;
  isRead: 0 | 1;
}

export interface ChatConversationVO {
  peerUid: number;
  peerName: string;
  peerAvatar?: string;
  lastMsgId: number;
  lastContent: string;
  lastMsgType: 1 | 2 | 3;
  lastSendTime: string;
  unreadCount: number;
}
