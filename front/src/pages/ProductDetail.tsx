import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { productsApi } from '../api/products';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';
import { ApiError } from '../api/client';
import type { Product, Variant } from '../types/product';
import { formatBRL } from '../utils/format';

export default function ProductDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();
  const { addItem } = useCart();

  const [product, setProduct] = useState<Product | null>(null);
  const [selectedVariant, setSelectedVariant] = useState<Variant | null>(null);
  const [quantity, setQuantity] = useState(1);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [adding, setAdding] = useState(false);
  const [addError, setAddError] = useState<string | null>(null);
  const [added, setAdded] = useState(false);

  useEffect(() => {
    if (!id) return;
    setLoading(true);
    productsApi
      .byId(id)
      .then((data) => {
        setProduct(data);
        setSelectedVariant(data.variants[0] ?? null);
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, [id]);

  async function handleAddToCart() {
    if (!selectedVariant) return;
    if (!isAuthenticated) {
      navigate('/entrar', { state: { from: { pathname: `/produtos/${id}` } } });
      return;
    }
    setAdding(true);
    setAddError(null);
    setAdded(false);
    try {
      await addItem(selectedVariant.id, quantity);
      setAdded(true);
    } catch (err) {
      if (err instanceof ApiError && err.status === 409) {
        setAddError('Estoque insuficiente para essa quantidade.');
      } else {
        setAddError(err instanceof Error ? err.message : 'Não foi possível adicionar ao carrinho.');
      }
    } finally {
      setAdding(false);
    }
  }

  if (loading) {
    return <div className="max-w-6xl mx-auto px-4 py-16 text-ink-soft">Carregando…</div>;
  }

  if (error || !product) {
    return (
      <div className="max-w-6xl mx-auto px-4 py-16 text-red-600">
        Não foi possível carregar este produto{error ? ` (${error})` : ''}.
      </div>
    );
  }

  const outOfStock = selectedVariant?.stockQuantity != null && selectedVariant.stockQuantity <= 0;

  return (
    <div className="max-w-6xl mx-auto px-4 py-10 grid sm:grid-cols-2 gap-10">
      <div className="aspect-square rounded-xl bg-trie-100 overflow-hidden">
        {product.imageUrl ? (
          <img src={product.imageUrl} alt={product.name} className="w-full h-full object-cover" />
        ) : (
          <div className="w-full h-full grid place-items-center text-trie-400">Sem imagem</div>
        )}
      </div>

      <div>
        <p className="text-xs uppercase tracking-widest text-trie-600">{product.category}</p>
        <h1 className="font-display text-3xl text-ink mt-1">{product.name}</h1>
        <p className="text-2xl text-trie-700 font-medium mt-3">
          {selectedVariant ? formatBRL(selectedVariant.price) : '—'}
        </p>

        {product.description && <p className="mt-4 text-sm text-ink-soft leading-relaxed">{product.description}</p>}

        {product.variants.length > 1 && (
          <div className="mt-6">
            <p className="text-sm font-medium text-ink mb-2">Tamanho</p>
            <div className="flex flex-wrap gap-2">
              {product.variants.map((v) => (
                <button
                  key={v.id}
                  onClick={() => setSelectedVariant(v)}
                  className={`rounded-full border px-4 py-1.5 text-sm transition-colors ${
                    selectedVariant?.id === v.id
                      ? 'bg-trie-600 border-trie-600 text-white'
                      : 'border-trie-300 text-ink hover:border-trie-500'
                  }`}
                >
                  {v.size ?? v.sku}
                </button>
              ))}
            </div>
          </div>
        )}

        <div className="mt-6 flex items-center gap-3">
          <label htmlFor="quantity" className="text-sm text-ink">
            Quantidade
          </label>
          <input
            id="quantity"
            type="number"
            min={1}
            value={quantity}
            onChange={(e) => setQuantity(Math.max(1, Number(e.target.value)))}
            className="w-16 rounded-md border border-trie-300 px-2 py-1 text-sm"
          />
        </div>

        {outOfStock && <p className="mt-3 text-sm text-red-600">Esse tamanho está sem estoque no momento.</p>}
        {addError && <p className="mt-3 text-sm text-red-600">{addError}</p>}
        {added && <p className="mt-3 text-sm text-emerald-600">Adicionado ao carrinho!</p>}

        <button
          onClick={handleAddToCart}
          disabled={!selectedVariant || outOfStock || adding}
          className="mt-6 w-full sm:w-auto rounded-full bg-trie-600 hover:bg-trie-700 disabled:bg-trie-300 disabled:cursor-not-allowed text-white px-8 py-3 text-sm font-medium transition-colors"
        >
          {adding ? 'Adicionando…' : 'Adicionar ao carrinho'}
        </button>

        {selectedVariant?.sku && <p className="mt-4 text-xs text-ink-soft">SKU: {selectedVariant.sku}</p>}
      </div>
    </div>
  );
}
