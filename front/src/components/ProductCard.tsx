import { Link } from 'react-router-dom';
import type { Product } from '../types/product';
import { productDisplayPrice } from '../utils/format';

export default function ProductCard({ product }: { product: Product }) {
  return (
    <Link
      to={`/produtos/${product.id}`}
      className="group block rounded-lg overflow-hidden bg-white border border-trie-100 hover:shadow-md transition-shadow"
    >
      <div className="aspect-square bg-trie-100 overflow-hidden">
        {product.imageUrl ? (
          <img
            src={product.imageUrl}
            alt={product.name}
            className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
          />
        ) : (
          <div className="w-full h-full grid place-items-center text-trie-400 text-sm">Sem imagem</div>
        )}
      </div>
      <div className="p-3">
        <p className="text-sm text-ink line-clamp-1">{product.name}</p>
        <p className="text-sm font-medium text-trie-700 mt-1">{productDisplayPrice(product)}</p>
      </div>
    </Link>
  );
}
