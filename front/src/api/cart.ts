import { apiFetch } from './client';
import type { Order, CheckoutResult } from '../types/order';

export const cartApi = {
  get: () => apiFetch<Order>('/api/cart'),
  addItem: (variantId: number, quantity: number) =>
    apiFetch<Order>('/api/cart/items', { method: 'POST', body: { variantId, quantity } }),
  removeItem: (itemId: number) =>
    apiFetch<void>(`/api/cart/items/${itemId}`, { method: 'DELETE' }),
  clear: () => apiFetch<void>('/api/cart', { method: 'DELETE' }),
  checkout: (cartId: number) =>
    apiFetch<CheckoutResult>(`/api/orders/${cartId}/checkout`, { method: 'POST' }),
};
