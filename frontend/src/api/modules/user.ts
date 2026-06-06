import { get, post, put } from '@/utils/request';
import type { LoginTokenPayload } from '@/types/auth';

export function userLogin(data: unknown) {
  return post<LoginTokenPayload>('/user/login', data);
}

export function userRegister(data: unknown) {
  return post<void>('/user/register', data);
}

export function getUserInfo() {
  return get<unknown>('/user/info');
}

export function updateUserInfo(data: unknown) {
  return put<void>('/user/update', data);
}
