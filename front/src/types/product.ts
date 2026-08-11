export type PricingType = 'FIXED' | 'BY_GRAM';

export interface Variant {
  id: number;
  size: string | null;
  weightInGrams: number | null;
  price: number;
  sku: string;
  isUniquePiece: boolean;
  stockQuantity: number | null;
}

export interface Product {
  id: number;
  name: string;
  description: string | null;
  category: string | null;
  material: string | null;
  pricingType: PricingType;
  pricePerGram: number | null;
  active: boolean;
  imageUrl: string | null;
  featured: boolean;
  newCollection: boolean;
  createdAt: string;
  variants: Variant[];
}
