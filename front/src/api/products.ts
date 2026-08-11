import { apiFetch } from './client';
import type { Product } from '../types/product';

export const productsApi = {
  featured: () => apiFetch<Product[]>('/api/products/featured', { auth: false }),
  newCollection: () => apiFetch<Product[]>('/api/products/new-collection', { auth: false }),
  all: () => apiFetch<Product[]>('/api/products', { auth: false }),
  byId: (id: number | string) => apiFetch<Product>(`/api/products/${id}`, { auth: false }),
  search: (q: string) =>
    apiFetch<Product[]>(`/api/products/search?q=${encodeURIComponent(q)}`, { auth: false }),
  categories: () => apiFetch<string[]>('/api/products/categories', { auth: false }),
};
