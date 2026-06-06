import { get, post, put } from '@/utils/request';

export function createOrder(data: unknown) {
  return post<void>('/order/create', data);
}

export function getOrderDetail(orderId: number | string) {
  return get<unknown>(`/order/${orderId}`);
}

export function finishOrder(orderId: number | string) {
  return put<void>(`/order/finish/${orderId}`);
}

export function cancelOrder(orderId: number | string) {
  return put<void>(`/order/cancel/${orderId}`);
}

export function addComment(data: unknown) {
  return post<void>('/comment/add', data);
}

export function getItemComments(itemId: number | string) {
  return get<unknown[]>(`/comment/item/${itemId}`);
}
