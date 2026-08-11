import { apiFetch } from './client';
import type { Order, OrderStatusHistory } from '../types/order';

export const ordersApi = {
  mine: () => apiFetch<Order[]>('/api/orders/me'),
  byId: (id: number) => apiFetch<Order>(`/api/orders/${id}`),
  history: (id: number) => apiFetch<OrderStatusHistory[]>(`/api/orders/${id}/history`),
};
