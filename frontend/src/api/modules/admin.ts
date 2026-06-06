import { get, post, put } from '@/utils/request';
import type { LoginTokenPayload } from '@/types/auth';

export function adminLogin(data: unknown) {
  return post<LoginTokenPayload>('/admin/login', data);
}

export function auditItem(data: unknown) {
  return put<void>('/admin/item/audit', data);
}

export function banUser(uid: number | string, data?: unknown) {
  return put<void>(`/admin/user/ban/${uid}`, data);
}

export function getReportList(params?: unknown) {
  return get<unknown[]>('/admin/report/list', { params });
}

export function handleReport(data: unknown) {
  return put<void>('/admin/report/handle', data);
}

export function addCategory(data: unknown) {
  return post<void>('/admin/category/add', data);
}

export function updateCategory(data: unknown) {
  return put<void>('/admin/category/update', data);
}
