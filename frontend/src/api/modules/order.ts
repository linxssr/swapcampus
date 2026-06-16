import { get, post, put } from '@/utils/request';
import type {
  AddCommentPayload,
  CommentRecord,
  CreateOrderPayload,
  OrderDetail,
  OrderQueryType,
  OrderRecord,
} from '@/types/order';

export function createOrder(data: CreateOrderPayload) {
  return post<string>('/order/create', data);
}

export function getOrderDetail(orderNo: number | string) {
  return get<OrderDetail>(`/order/${orderNo}`);
}

export function confirmOrder(orderNo: number | string) {
  return put<void>(`/order/confirm/${orderNo}`);
}

export function finishOrder(orderNo: number | string) {
  return put<void>(`/order/finish/${orderNo}`);
}

export function cancelOrder(orderNo: number | string) {
  return put<void>(`/order/cancel/${orderNo}`);
}

export function addComment(data: AddCommentPayload) {
  return post<void>('/comment/add', data);
}

export function getItemComments(itemId: number | string) {
  return get<CommentRecord[]>(`/comment/item/${itemId}`);
}

export function getOrderComment(orderId: number | string) {
  return get<CommentRecord | null>(`/comment/order/${orderId}`);
}

export function getMyOrders(type: OrderQueryType) {
  return get<OrderRecord[]>('/user/myOrder', { params: { type } });
}

export function getUserSellerComments(userId: number | string) {
  return get<CommentRecord[]>(`/comment/seller/${userId}`);
}
