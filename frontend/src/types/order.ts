export type OrderQueryType = 'buy' | 'sell';
export type TradeType = 1 | 2;
export type OrderStatus = 1 | 2 | 3 | 4;

export interface CreateOrderPayload {
  itemId: number | string;
  tradeType: TradeType;
  price: number | string;
}

export interface OrderRecord {
  orderId: number;
  orderNo: string;
  buyerId?: number;
  sellerId?: number;
  itemId: number;
  itemTitle?: string;
  itemCover?: string;
  peerName?: string;
  price: number;
  tradeType: TradeType;
  orderStatus: OrderStatus;
  createTime: string;
  finishTime?: string | null;
}

export interface OrderDetail extends OrderRecord {
  buyerId: number;
  sellerId: number;
}

export interface AddCommentPayload {
  orderId: number | string;
  content: string;
  score: number;
}

export interface CommentRecord {
  commentId: number;
  orderId: number;
  buyerId: number;
  sellerId: number;
  itemId: number;
  buyerName?: string;
  content: string;
  score: number;
  createTime: string;
}
