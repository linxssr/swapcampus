import { get, post, put } from '@/utils/request';
import type {
  LoginResult,
  RegisterParams,
  UserInfo,
  UpdateUserParams,
  UpdatePasswordParams,
} from '@/types/auth';

export function userLogin(data: { studentId: string; password: string }) {
  return post<LoginResult>('/user/login', data);
}

export function userRegister(data: RegisterParams) {
  return post<void>('/user/register', data);
}

export function getUserInfo() {
  return get<UserInfo>('/user/info');
}

export function updateUserInfo(data: UpdateUserParams) {
  return put<void>('/user/update', data);
}

export function updatePassword(data: UpdatePasswordParams) {
  return put<void>('/user/pwd', data);
}

export function getMyItemsApi(params?: { page?: number; size?: number }) {
  return get<unknown[]>('/user/myItem', { params });
}

export function getMyOrdersApi(params?: {
  type?: 'buy' | 'sell';
  page?: number;
  size?: number;
}) {
  return get<unknown[]>('/user/myOrder', { params });
}

export const loginApi = userLogin;
export const registerApi = userRegister;
export const getUserInfoApi = getUserInfo;
export const updateUserInfoApi = updateUserInfo;
export const updatePasswordApi = updatePassword;

export const uploadAvatarApi = (file: File, token: string) => {
  const formData = new FormData();
  formData.append('file', file);
  return fetch('/api/v1/user/avatar', {
    method: 'POST',
    headers: { Authorization: `Bearer ${token}` },
    body: formData,
  }).then((res) => res.json());
};

export function getUserProfile(userId: number | string) {
  return get<{ userId: number; username: string; creditScore: number; avatar?: string }>(`/user/profile/${userId}`);
}

export function getUserPublishedItems(userId: number | string) {
  return get<unknown[]>(`/item/user/${userId}`);
}
