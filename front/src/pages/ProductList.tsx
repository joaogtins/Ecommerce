import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { productsApi } from '../api/products';
import type { Product } from '../types/product';
import ProductCard from '../components/ProductCard';

export default function ProductList() {
  const [searchParams, setSearchParams] = useSearchParams();
  const categoria = searchParams.get('categoria') ?? '';
  const busca = searchParams.get('busca') ?? '';

  const [products, setProducts] = useState<Product[]>([]);
  const [categories, setCategories] = useState<string[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    productsApi.categories().then(setCategories).catch(() => {});
  }, []);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError(null);

    const request = busca ? productsApi.search(busca) : productsApi.all();

    request
      .then((data) => {
        if (cancelled) return;
        const filtered = categoria ? data.filter((p) => p.category === categoria) : data;
        setProducts(filtered);
      })
      .catch((err) => !cancelled && setError(err.message))
      .finally(() => !cancelled && setLoading(false));

    return () => {
      cancelled = true;
    };
  }, [categoria, busca]);

  function selectCategory(cat: string) {
    const next = new URLSearchParams(searchParams);
    if (cat) next.set('categoria', cat);
    else next.delete('categoria');
    setSearchParams(next);
  }

  return (
    <div className="max-w-6xl mx-auto px-4 py-10">
      <div className="rounded-xl bg-trie-200 px-8 py-10 mb-8">
        <p className="text-xs uppercase tracking-widest text-trie-700 mb-1">Catálogo</p>
        <h1 className="font-display text-3xl text-ink">
          {busca ? `Resultados para "${busca}"` : 'Todas as peças'}
        </h1>
      </div>

      <div className="grid sm:grid-cols-[200px_1fr] gap-8">
        <aside>
          <h2 className="text-sm font-semibold text-ink mb-3">Categorias</h2>
          <ul className="space-y-2 text-sm">
            <li>
              <button
                onClick={() => selectCategory('')}
                className={`hover:text-trie-700 ${!categoria ? 'text-trie-700 font-medium' : 'text-ink-soft'}`}
              >
                Todas
              </button>
            </li>
            {categories.map((cat) => (
              <li key={cat}>
                <button
                  onClick={() => selectCategory(cat)}
                  className={`hover:text-trie-700 ${categoria === cat ? 'text-trie-700 font-medium' : 'text-ink-soft'}`}
                >
                  {cat}
                </button>
              </li>
            ))}
          </ul>
        </aside>

        <div>
          {error && <p className="text-sm text-red-600 mb-4">Não foi possível carregar os produtos ({error}).</p>}
          {loading ? (
            <div className="grid grid-cols-2 sm:grid-cols-3 gap-4">
              {Array.from({ length: 6 }).map((_, i) => (
                <div key={i} className="aspect-square rounded-lg bg-trie-100 animate-pulse" />
              ))}
            </div>
          ) : products.length === 0 ? (
            <p className="text-sm text-ink-soft">Nenhuma peça encontrada.</p>
          ) : (
            <div className="grid grid-cols-2 sm:grid-cols-3 gap-4">
              {products.map((p) => (
                <ProductCard key={p.id} product={p} />
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
