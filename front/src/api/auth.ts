import { apiFetch } from './client';
import type { AuthResult, LoginPayload, RegisterPayload } from '../types/auth';

export const authApi = {
  login: (payload: LoginPayload) =>
    apiFetch<AuthResult>('/api/auth/login', { method: 'POST', body: payload, auth: false }),
  register: (payload: RegisterPayload) =>
    apiFetch<AuthResult>('/api/auth/register', { method: 'POST', body: payload, auth: false }),
};
