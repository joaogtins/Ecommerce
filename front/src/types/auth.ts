export type UserRole = 'CUSTOMER' | 'ADMIN';

export interface AuthResult {
  token: string;
  id: number;
  name: string;
  email: string;
  role: UserRole;
}

export interface LoginPayload {
  email: string;
  password: string;
}

export interface RegisterPayload {
  name: string;
  email: string;
  password: string;
  phone?: string;
}
