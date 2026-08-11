import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { productsApi } from '../api/products';
import type { Product } from '../types/product';
import ProductCard from '../components/ProductCard';

function ProductRow({ title, products, loading }: { title: string; products: Product[]; loading: boolean }) {
  return (
    <section className="max-w-6xl mx-auto px-4 py-10">
      <div className="flex items-baseline justify-between mb-4">
        <h2 className="font-display text-2xl text-ink">{title}</h2>
        <Link to="/produtos" className="text-sm text-trie-600 hover:text-trie-800">
          Ver tudo →
        </Link>
      </div>
      {loading ? (
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
          {Array.from({ length: 4 }).map((_, i) => (
            <div key={i} className="aspect-square rounded-lg bg-trie-100 animate-pulse" />
          ))}
        </div>
      ) : products.length === 0 ? (
        <p className="text-sm text-ink-soft">Nenhum produto por aqui ainda.</p>
      ) : (
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
          {products.map((p) => (
            <ProductCard key={p.id} product={p} />
          ))}
        </div>
      )}
    </section>
  );
}

export default function Home() {
  const [featured, setFeatured] = useState<Product[]>([]);
  const [newCollection, setNewCollection] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    Promise.all([productsApi.featured(), productsApi.newCollection()])
      .then(([featuredData, newData]) => {
        if (cancelled) return;
        setFeatured(featuredData);
        setNewCollection(newData);
      })
      .catch((err) => !cancelled && setError(err.message))
      .finally(() => !cancelled && setLoading(false));
    return () => {
      cancelled = true;
    };
  }, []);

  return (
    <div>
      <section className="bg-trie-100">
        <div className="max-w-6xl mx-auto px-4 py-16 grid sm:grid-cols-2 gap-8 items-center">
          <div>
            <p className="text-xs uppercase tracking-widest text-trie-600 mb-2">Nova coleção</p>
            <h1 className="font-display text-4xl sm:text-5xl text-ink leading-tight">
              Joias que contam a sua história.
            </h1>
            <p className="mt-4 text-ink-soft max-w-md">
              Peças em prata 925, desenhadas para o dia a dia e para os momentos que você vai lembrar.
            </p>
            <Link
              to="/produtos"
              className="inline-block mt-6 rounded-full bg-trie-600 hover:bg-trie-700 text-white px-6 py-3 text-sm font-medium transition-colors"
            >
              Explorar coleção
            </Link>
          </div>
          <div className="aspect-[4/3] rounded-xl bg-trie-200 overflow-hidden">
            {newCollection[0]?.imageUrl && (
              <img
                src={newCollection[0].imageUrl}
                alt={newCollection[0].name}
                className="w-full h-full object-cover"
              />
            )}
          </div>
        </div>
      </section>

      {error && (
        <p className="max-w-6xl mx-auto px-4 mt-6 text-sm text-red-600">
          Não foi possível carregar os produtos agora ({error}).
        </p>
      )}

      <ProductRow title="Mais vendidos" products={featured} loading={loading} />
      <ProductRow title="Nova coleção" products={newCollection} loading={loading} />
    </div>
  );
}
