export type CartIntent = "PURCHASE" | "RENT";

export interface CartItemResponse {
  cartItemId: number;
  productId: number;
  productTitle: string;
  coverImage?: string;
  intent: CartIntent;
  rentDays?: number | null;
  lineTotal: number;
  addedAt?: string;
}

export interface CartResponse {
  cartId: number;
  userId: number;
  items: CartItemResponse[];
  grandTotal: number;
  updatedAt?: string;
}

export interface UICartItem {
  cartItemId: number;
  id: number;
  name: string;
  image: string;
  price: number;
  intent: CartIntent;
  rentDays?: number | null;
  rentable: boolean;
  rentPerDay: number;
  minRentDays: number;
}
