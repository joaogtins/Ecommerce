import { Link, useNavigate } from 'react-router-dom';
import { useState, type FormEvent } from 'react';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';

const NAV_LINKS = [
  { label: 'Início', to: '/' },
  { label: 'Todos os produtos', to: '/produtos' },
  { label: 'Colares', to: '/produtos?categoria=Colares' },
  { label: 'Brincos', to: '/produtos?categoria=Brincos' },
  { label: 'Anéis', to: '/produtos?categoria=Aneis' },
  { label: 'Pulseiras', to: '/produtos?categoria=Pulseiras' },
];

/** Faixa superior com o mesmo texto repetido, como no design original ("PROMOÇÃO" em loop). */
function PromoBar() {
  const text = 'FRETE GRÁTIS ACIMA DE R$300 · PEÇAS EM PRATA 925 ';
  return (
    <div className="bg-trie-800 text-trie-50 text-xs py-1.5 promo-marquee">
      <span className="px-2">{text.repeat(12)}</span>
    </div>
  );
}

export default function Header() {
  const { isAuthenticated, user } = useAuth();
  const { itemCount } = useCart();
  const navigate = useNavigate();
  const [query, setQuery] = useState('');

  function handleSearch(e: FormEvent) {
    e.preventDefault();
    if (query.trim()) {
      navigate(`/produtos?busca=${encodeURIComponent(query.trim())}`);
    }
  }

  return (
    <header className="sticky top-0 z-40 bg-trie-100/95 backdrop-blur border-b border-trie-200">
      <PromoBar />
      <div className="max-w-6xl mx-auto px-4 py-3 flex items-center gap-4">
        <Link to="/" className="shrink-0 flex items-center gap-2">
          <span className="w-9 h-9 rounded-full bg-trie-500 text-white grid place-items-center font-display text-sm">
            TP
          </span>
          <span className="hidden sm:block font-display text-lg text-ink">Triê Pratas</span>
        </Link>

        <form onSubmit={handleSearch} className="flex-1 max-w-md">
          <div className="relative">
            <input
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="O que você procura?"
              className="w-full rounded-full bg-white border border-trie-200 py-2 pl-4 pr-10 text-sm text-ink placeholder:text-ink-soft focus:outline-none focus:ring-2 focus:ring-trie-400"
            />
            <button
              type="submit"
              aria-label="Buscar"
              className="absolute right-3 top-1/2 -translate-y-1/2 text-trie-600"
            >
              ⌕
            </button>
          </div>
        </form>

        <div className="flex items-center gap-4 text-ink">
          <Link
            to={isAuthenticated ? '/pedidos' : '/entrar'}
            className="text-sm hover:text-trie-600"
            title={isAuthenticated ? user?.name : 'Entrar'}
          >
            👤
          </Link>
          <Link to="/carrinho" className="relative text-sm hover:text-trie-600" title="Carrinho">
            🛍
            {itemCount > 0 && (
              <span className="absolute -top-2 -right-2 bg-trie-600 text-white text-[10px] rounded-full w-4 h-4 grid place-items-center">
                {itemCount}
              </span>
            )}
          </Link>
        </div>
      </div>

      <nav className="max-w-6xl mx-auto px-4 pb-2 flex gap-5 text-xs uppercase tracking-wide text-ink-soft overflow-x-auto">
        {NAV_LINKS.map((link) => (
          <Link key={link.label} to={link.to} className="whitespace-nowrap hover:text-trie-700">
            {link.label}
          </Link>
        ))}
        {isAuthenticated && (
          <Link to="/pedidos" className="whitespace-nowrap hover:text-trie-700">
            Meus pedidos
          </Link>
        )}
      </nav>
    </header>
  );
}
