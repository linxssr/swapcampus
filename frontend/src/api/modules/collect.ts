import { del, get, post } from '@/utils/request';
import type { ItemVO } from '@/types/item';

export interface CollectVO {
  collectId: number;
  userId: number;
  itemId: number;
  createTime: string;
  item?: ItemVO;
}

export function addCollect(data: { itemId: number }) {
  return post<void>('/collect/add', data);
}

export function cancelCollect(id: number | string) {
  return del<void>(`/collect/cancel/${id}`);
}

export function cancelCollectByItemId(itemId: number | string) {
  return del<void>(`/collect/cancel/item/${itemId}`);
}

export function getMyCollects() {
  return get<CollectVO[]>('/collect/my');
}
