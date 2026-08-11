export type OrderStatus =
  | 'DRAFT'
  | 'PENDING'
  | 'PAID'
  | 'PREPARING'
  | 'OUT_FOR_DELIVERY'
  | 'DELIVERED'
  | 'CANCELLED';

export interface OrderItem {
  id: number;
  variantId: number;
  productName: string;
  variantSize: string | null;
  sku: string;
  quantity: number;
  priceAtPurchase: number;
  subtotal: number;
}

export interface OrderStatusHistory {
  id: number;
  fromStatus: OrderStatus | null;
  toStatus: OrderStatus;
  changedAt: string;
  notes: string | null;
}

export interface Order {
  id: number;
  customerId: number;
  status: OrderStatus;
  totalAmount: number;
  reservedUntil: string | null;
  createdAt: string;
  items: OrderItem[];
  statusHistory: OrderStatusHistory[];
}

export interface CheckoutResult {
  orderId: number;
  status: OrderStatus;
  whatsappLink: string;
  totalAmount: number;
}
