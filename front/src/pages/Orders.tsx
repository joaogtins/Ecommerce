import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { ordersApi } from '../api/orders';
import type { Order } from '../types/order';
import { formatBRL, formatDate, formatStatus, statusColor } from '../utils/format';

export default function Orders() {
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    ordersApi
      .mine()
      .then(setOrders)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="max-w-4xl mx-auto px-4 py-10">
      <h1 className="font-display text-3xl text-ink mb-8">Meus pedidos</h1>

      {loading && <p className="text-ink-soft">Carregando…</p>}
      {error && <p className="text-red-600 text-sm">Não foi possível carregar seus pedidos ({error}).</p>}

      {!loading && !error && orders.length === 0 && (
        <div className="text-center py-16">
          <p className="text-ink-soft mb-6">Você ainda não fez nenhum pedido.</p>
          <Link to="/produtos" className="inline-block rounded-full bg-trie-600 text-white px-6 py-3 text-sm">
            Começar a comprar
          </Link>
        </div>
      )}

      <ul className="space-y-4">
        {orders.map((order) => (
          <li key={order.id} className="bg-white border border-trie-100 rounded-lg p-5">
            <div className="flex flex-wrap items-center justify-between gap-2 mb-3">
              <div>
                <p className="text-sm font-medium text-ink">Pedido #{String(order.id).padStart(4, '0')}</p>
                <p className="text-xs text-ink-soft">{formatDate(order.createdAt)}</p>
              </div>
              <span className={`text-xs px-3 py-1 rounded-full font-medium ${statusColor(order.status)}`}>
                {formatStatus(order.status)}
              </span>
            </div>

            <ul className="text-sm text-ink-soft space-y-1 mb-3">
              {order.items.map((item) => (
                <li key={item.id} className="flex justify-between">
                  <span>
                    {item.productName} {item.variantSize ? `— ${item.variantSize}` : ''} × {item.quantity}
                  </span>
                  <span>{formatBRL(item.subtotal)}</span>
                </li>
              ))}
            </ul>

            <div className="flex justify-between items-center border-t border-trie-100 pt-3">
              <span className="text-sm text-ink-soft">Total</span>
              <span className="text-sm font-medium text-ink">{formatBRL(order.totalAmount)}</span>
            </div>
          </li>
        ))}
      </ul>
    </div>
  );
}
