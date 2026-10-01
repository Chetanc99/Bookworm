export interface PurchaseItemResponse {
  purchaseItemId: number;
  productId: number;
  productTitle: string;
  unitPrice: number;
}

export interface PurchaseTransactionResponse {
  purchaseTransactionId: number;
  userId: number;
  totalAmount: number;
  items: PurchaseItemResponse[];
  createdAt: string;
}

export interface RentTransactionResponse {
  rentTransactionId: number;
  userId: number;
  productId: number;
  productTitle: string;
  rentDays: number;
  dailyRate: number;
  totalAmount: number;
  expiresAt: string;
  createdAt: string;
}

export interface OrderHistoryRow {
  transactionId: string;
  rawId: number;
  type: "PURCHASE" | "RENT";
  productName: string;
  amount: number;
  orderDate: string;
}
