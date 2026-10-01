import api from "../api/client";
import { API_ENDPOINTS } from "../api/endpoints";
import { PageResponse } from "../types/api";
import { PurchaseTransactionResponse, RentTransactionResponse, OrderHistoryRow } from "../types/order";
import { getCurrentUser } from "./auth.service";

export const purchaseCheckoutAPI = (userId: number) =>
  api.post<PurchaseTransactionResponse>(API_ENDPOINTS.CHECKOUT.PURCHASES(userId));

export const rentCheckoutAPI = (userId: number) =>
  api.post<RentTransactionResponse[]>(API_ENDPOINTS.CHECKOUT.RENTALS(userId));

export const getPurchaseHistoryAPI = (userId: number, page = 0, size = 20) =>
  api.get<PageResponse<PurchaseTransactionResponse>>(API_ENDPOINTS.CHECKOUT.PURCHASES(userId), {
    params: { page, size },
  });

export const getRentHistoryAPI = (userId: number, page = 0, size = 20) =>
  api.get<PageResponse<RentTransactionResponse>>(API_ENDPOINTS.CHECKOUT.RENTALS(userId), {
    params: { page, size },
  });

export const downloadInvoice = async (userId: number, purchaseTransactionId: number): Promise<void> => {
  const res = await api.get(API_ENDPOINTS.CHECKOUT.INVOICE(userId, purchaseTransactionId), {
    responseType: "blob",
  });
  const url = window.URL.createObjectURL(new Blob([res.data], { type: "application/pdf" }));
  const link = document.createElement("a");
  link.href = url;
  link.download = `bookworm-invoice-${purchaseTransactionId}.pdf`;
  document.body.appendChild(link);
  link.click();
  link.remove();
  window.URL.revokeObjectURL(url);
};

export const getOrderHistoryAPI = async (): Promise<{ data: OrderHistoryRow[] }> => {
  const user = getCurrentUser();
  if (!user?.userId) {
    return { data: [] };
  }

  const [purchasesRes, rentalsRes] = await Promise.all([
    getPurchaseHistoryAPI(user.userId).catch(() => ({ data: { content: [] } as any })),
    getRentHistoryAPI(user.userId).catch(() => ({ data: { content: [] } as any })),
  ]);

  const purchaseRows: OrderHistoryRow[] = (purchasesRes.data?.content ?? []).flatMap((tx: PurchaseTransactionResponse) =>
    (tx.items ?? []).map((item) => ({
      transactionId: `P${tx.purchaseTransactionId}`,
      rawId: tx.purchaseTransactionId,
      type: "PURCHASE" as const,
      productName: item.productTitle,
      amount: item.unitPrice,
      orderDate: tx.createdAt,
    }))
  );

  const rentalRows: OrderHistoryRow[] = (rentalsRes.data?.content ?? []).map((tx: RentTransactionResponse) => ({
    transactionId: `R${tx.rentTransactionId}`,
    rawId: tx.rentTransactionId,
    type: "RENT" as const,
    productName: tx.productTitle,
    amount: tx.totalAmount,
    orderDate: tx.createdAt,
  }));

  const rows = [...purchaseRows, ...rentalRows].sort(
    (a, b) => new Date(b.orderDate).getTime() - new Date(a.orderDate).getTime()
  );

  return { data: rows };
};
