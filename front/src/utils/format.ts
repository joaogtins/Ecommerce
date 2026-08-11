export function formatBRL(value: number | null | undefined): string {
  if (value == null) return '—';
  return value.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
}

export function formatDate(value: string | null | undefined): string {
  if (!value) return '—';
  return new Date(value).toLocaleDateString('pt-BR', { day: '2-digit', month: 'long', year: 'numeric' });
}

const STATUS_LABEL: Record<string, string> = {
  DRAFT: 'Carrinho',
  PENDING: 'Aguardando pagamento',
  PAID: 'Pago',
  PREPARING: 'Em preparo',
  OUT_FOR_DELIVERY: 'A caminho',
  DELIVERED: 'Entregue',
  CANCELLED: 'Cancelado',
};

export function formatStatus(status: string): string {
  return STATUS_LABEL[status] ?? status;
}

/** Cor de badge por status, usando a paleta trie-* */
export function statusColor(status: string): string {
  switch (status) {
    case 'PAID':
    case 'DELIVERED':
      return 'bg-emerald-100 text-emerald-700';
    case 'CANCELLED':
      return 'bg-red-100 text-red-700';
    case 'PREPARING':
    case 'OUT_FOR_DELIVERY':
      return 'bg-trie-200 text-trie-800';
    default:
      return 'bg-trie-100 text-trie-700';
  }
}

/** Preço de exibição de um produto: fixo, ou "a partir de" quando varia por variante/grama */
export function productDisplayPrice(product: {
  pricingType: string;
  pricePerGram: number | null;
  variants: { price: number }[];
}): string {
  if (product.pricingType === 'BY_GRAM' && product.pricePerGram) {
    return `${formatBRL(product.pricePerGram)} / g`;
  }
  const prices = product.variants.map((v) => v.price).filter((p) => p != null);
  if (prices.length === 0) return '—';
  const min = Math.min(...prices);
  return formatBRL(min);
}
