import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useCart } from '../context/CartContext';
import { cartApi } from '../api/cart';
import { formatBRL } from '../utils/format';

export default function Cart() {
  const { cart, loading, removeItem, refresh } = useCart();
  const navigate = useNavigate();
  const [checkingOut, setCheckingOut] = useState(false);
  const [checkoutError, setCheckoutError] = useState<string | null>(null);
  const [removingId, setRemovingId] = useState<number | null>(null);

  async function handleRemove(itemId: number) {
    setRemovingId(itemId);
    try {
      await removeItem(itemId);
    } finally {
      setRemovingId(null);
    }
  }

  async function handleCheckout() {
    if (!cart) return;
    setCheckingOut(true);
    setCheckoutError(null);
    try {
      const result = await cartApi.checkout(cart.id);
      await refresh();
      window.open(result.whatsappLink, '_blank', 'noopener,noreferrer');
      navigate(`/pedidos`);
    } catch (err) {
      setCheckoutError(err instanceof Error ? err.message : 'Não foi possível finalizar o pedido.');
    } finally {
      setCheckingOut(false);
    }
  }

  if (loading) {
    return <div className="max-w-4xl mx-auto px-4 py-16 text-ink-soft">Carregando carrinho…</div>;
  }

  if (!cart || cart.items.length === 0) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-20 text-center">
        <h1 className="font-display text-2xl text-ink mb-2">Seu carrinho está vazio</h1>
        <p className="text-ink-soft mb-6">Que tal dar uma olhada nas nossas peças em destaque?</p>
        <Link to="/produtos" className="inline-block rounded-full bg-trie-600 text-white px-6 py-3 text-sm">
          Ver produtos
        </Link>
      </div>
    );
  }

  return (
    <div className="max-w-5xl mx-auto px-4 py-10">
      <h1 className="font-display text-3xl text-ink mb-8">Meu carrinho</h1>

      <div className="grid sm:grid-cols-[1fr_320px] gap-8 items-start">
        <ul className="space-y-4">
          {cart.items.map((item) => (
            <li
              key={item.id}
              className="flex items-center gap-4 bg-white border border-trie-100 rounded-lg p-4"
            >
              <div className="flex-1">
                <p className="text-sm font-medium text-ink">{item.productName}</p>
                {item.variantSize && <p className="text-xs text-ink-soft">Tamanho: {item.variantSize}</p>}
                <p className="text-xs text-ink-soft">Qtd: {item.quantity}</p>
              </div>
              <div className="text-right">
                <p className="text-sm font-medium text-trie-700">{formatBRL(item.subtotal)}</p>
                <button
                  onClick={() => handleRemove(item.id)}
                  disabled={removingId === item.id}
                  className="mt-1 text-xs text-red-600 hover:underline disabled:opacity-50"
                >
                  {removingId === item.id ? 'Removendo…' : 'Remover'}
                </button>
              </div>
            </li>
          ))}
        </ul>

        <aside className="bg-white border border-trie-100 rounded-lg p-5">
          <h2 className="font-display text-lg text-ink mb-4">Resumo do pedido</h2>
          <div className="flex justify-between text-sm text-ink-soft mb-2">
            <span>Subtotal</span>
            <span>{formatBRL(cart.totalAmount)}</span>
          </div>
          <div className="flex justify-between text-base font-medium text-ink border-t border-trie-100 pt-3 mt-3">
            <span>Total</span>
            <span>{formatBRL(cart.totalAmount)}</span>
          </div>

          {checkoutError && <p className="mt-3 text-sm text-red-600">{checkoutError}</p>}

          <button
            onClick={handleCheckout}
            disabled={checkingOut}
            className="mt-5 w-full rounded-full bg-trie-600 hover:bg-trie-700 disabled:bg-trie-300 text-white py-3 text-sm font-medium transition-colors"
          >
            {checkingOut ? 'Finalizando…' : 'Finalizar pedido pelo WhatsApp'}
          </button>
          <p className="mt-2 text-xs text-ink-soft text-center">
            Você será redirecionado ao WhatsApp para confirmar o pagamento.
          </p>
        </aside>
      </div>
    </div>
  );
}
