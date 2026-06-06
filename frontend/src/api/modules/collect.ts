import { del, get, post } from '@/utils/request';

export function addCollect(data: unknown) {
  return post<void>('/collect/add', data);
}

export function cancelCollect(id: number | string) {
  return del<void>(`/collect/cancel/${id}`);
}

export function getMyCollects() {
  return get<unknown[]>('/collect/my');
}
