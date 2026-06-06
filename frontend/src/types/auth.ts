export type UserRole = 'user' | 'admin';

export interface LoginTokenPayload {
  token: string;
  role: UserRole;
}
