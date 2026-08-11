/**
 * Cliente HTTP central. Usa fetch nativo (evita dependência extra do axios
 * para uma app deste tamanho) e injeta o token JWT salvo no localStorage
 * em toda chamada autenticada.
 */

const TOKEN_KEY = 'trie_token';

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY);
}

export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token);
}

export function clearToken(): void {
  localStorage.removeItem(TOKEN_KEY);
}

export class ApiError extends Error {
  status: number;
  constructor(status: number, message: string) {
    super(message);
    this.status = status;
  }
}

interface RequestOptions {
  method?: 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE';
  body?: unknown;
  auth?: boolean; // envia o Bearer token quando disponível
}

/**
 * Em dev, o Vite faz proxy de /api para http://localhost:8080 (ver vite.config.ts).
 * Em produção, defina VITE_API_URL apontando para o backend.
 */
const BASE_URL = import.meta.env.VITE_API_URL ?? '';

export async function apiFetch<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const { method = 'GET', body, auth = true } = options;

  const headers: Record<string, string> = {};
  if (body !== undefined) headers['Content-Type'] = 'application/json';
  if (auth) {
    const token = getToken();
    if (token) headers['Authorization'] = `Bearer ${token}`;
  }

  const response = await fetch(`${BASE_URL}${path}`, {
    method,
    headers,
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });

  if (response.status === 204) {
    return undefined as T;
  }

  const text = await response.text();
  const data = text ? JSON.parse(text) : undefined;

  if (!response.ok) {
    const message = data?.message ?? data?.error ?? `Erro ${response.status}`;
    throw new ApiError(response.status, message);
  }

  return data as T;
}
