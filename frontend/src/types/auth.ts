export type UserRole = 'user' | 'admin';

export interface LoginTokenPayload {
  token: string;
  role: UserRole;
}

export interface UserInfoVO {
  userId: number;
  studentId: string;
  username: string;
  phone?: string;
  avatar?: string;
  creditScore: number;
  status: number;
  createTime?: string;
}

export interface LoginParams {
  studentId: string;
  password: string;
}

export interface LoginResult {
  token: string;
  userId: number;
  username: string;
  creditScore: number;
  createTime?: string;
}

export interface UserInfo {
  userId: number;
  studentId: string;
  username: string;
  phone: string | null;
  avatar: string | null;
  creditScore: number;
  status: number;
  createTime: string;
}

export interface RegisterParams {
  studentId: string;
  username: string;
  password: string;
  phone?: string;
}

export interface UpdateUserParams {
  username?: string;
  phone?: string;
  avatar?: string;
}

export interface UpdatePasswordParams {
  oldPassword: string;
  newPassword: string;
}

export interface MyItem {
  item_id: number;
  title: string;
  price: number;
  cover_url: string;
  publish_status: number;
  create_time: string;
}

export interface MyOrder {
  order_id: number;
  order_no: string;
  price: number;
  trade_type: number;
  order_status: number;
  create_time: string;
  finish_time: string | null;
}
