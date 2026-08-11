import { createContext, useCallback, useContext, useEffect, useState, type ReactNode } from 'react';
import { cartApi } from '../api/cart';
import { ApiError } from '../api/client';
import { useAuth } from './AuthContext';
import type { Order } from '../types/order';

interface CartContextValue {
  cart: Order | null;
  loading: boolean;
  itemCount: number;
  refresh: () => Promise<void>;
  addItem: (variantId: number, quantity: number) => Promise<void>;
  removeItem: (itemId: number) => Promise<void>;
  clear: () => Promise<void>;
}

const CartContext = createContext<CartContextValue | undefined>(undefined);

export function CartProvider({ children }: { children: ReactNode }) {
  const { isAuthenticated } = useAuth();
  const [cart, setCart] = useState<Order | null>(null);
  const [loading, setLoading] = useState(false);

  const refresh = useCallback(async () => {
    if (!isAuthenticated) {
      setCart(null);
      return;
    }
    setLoading(true);
    try {
      const data = await cartApi.get();
      setCart(data);
    } catch (err) {
      // 404 = ainda não existe carrinho para o cliente; não é um erro real
      if (err instanceof ApiError && err.status === 404) {
        setCart(null);
      } else {
        throw err;
      }
    } finally {
      setLoading(false);
    }
  }, [isAuthenticated]);

  useEffect(() => {
    refresh();
  }, [refresh]);

  async function addItem(variantId: number, quantity: number) {
    const updated = await cartApi.addItem(variantId, quantity);
    setCart(updated);
  }

  async function removeItem(itemId: number) {
    await cartApi.removeItem(itemId);
    await refresh();
  }

  async function clear() {
    await cartApi.clear();
    setCart(null);
  }

  const itemCount = cart?.items.reduce((sum, item) => sum + item.quantity, 0) ?? 0;

  return (
    <CartContext.Provider value={{ cart, loading, itemCount, refresh, addItem, removeItem, clear }}>
      {children}
    </CartContext.Provider>
  );
}

export function useCart() {
  const ctx = useContext(CartContext);
  if (!ctx) throw new Error('useCart deve ser usado dentro de <CartProvider>');
  return ctx;
}
