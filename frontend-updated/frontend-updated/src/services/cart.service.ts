import api from "../api/client";
import { API_ENDPOINTS } from "../api/endpoints";
import { CartResponse, CartIntent } from "../types/cart";

export const getCartAPI = (userId: number) =>
  api.get<CartResponse>(API_ENDPOINTS.CART.BASE(userId));

export const addToCartAPI = (
  userId: number,
  productId: number,
  intent: CartIntent = "PURCHASE",
  rentDays: number | null = null
) =>
  api.post<CartResponse>(API_ENDPOINTS.CART.ITEMS(userId), {
    productId,
    intent,
    rentDays,
  });

export const updateCartItemAPI = (
  userId: number,
  cartItemId: number,
  productId: number,
  intent: CartIntent,
  rentDays: number | null = null
) =>
  api.put<CartResponse>(API_ENDPOINTS.CART.ITEM_BY_ID(userId, cartItemId), {
    productId,
    intent,
    rentDays,
  });

export const removeFromCartAPI = (userId: number, cartItemId: number) =>
  api.delete<CartResponse>(API_ENDPOINTS.CART.ITEM_BY_ID(userId, cartItemId));

export const clearCartAPI = (userId: number) =>
  api.delete(API_ENDPOINTS.CART.BASE(userId));
