import api from "../api/client";
import { API_ENDPOINTS } from "../api/endpoints";
import { PageResponse } from "../types/api";
import { Product, Genre, Language, Subcategory, ProductRequest, MediaType } from "../types/product";

export interface BrowseParams {
  subcategoryId?: number;
  genreId?: number;
  languageId?: number;
  isRentable?: boolean;
  mediaType?: MediaType;
  keyword?: string;
  // Overlays a ProductTranslation's title/description onto each result if one
  // exists for this language - see ProductServiceImpl.overlayTranslation on
  // the backend. Distinct from `languageId` above, which FILTERS the catalog
  // by a product's own base language rather than translating display text.
  displayLanguageId?: number;
  page?: number;
  size?: number;
}

export const browseProducts = async ({
  subcategoryId,
  genreId,
  languageId,
  isRentable,
  mediaType,
  keyword,
  displayLanguageId,
  page = 0,
  size = 20,
}: BrowseParams = {}): Promise<PageResponse<Product>> => {
  const res = await api.get(API_ENDPOINTS.PRODUCTS.BASE, {
    params: { subcategoryId, genreId, languageId, isRentable, mediaType, keyword, displayLanguageId, page, size },
  });
  return res.data;
};

export const getProductById = async (productId: number, displayLanguageId?: number): Promise<Product> => {
  const res = await api.get(`${API_ENDPOINTS.PRODUCTS.BASE}/${productId}`, {
    params: displayLanguageId ? { displayLanguageId } : {},
  });
  return res.data;
};

export const getAllGenres = async (subcategoryId?: number): Promise<Genre[]> => {
  const res = await api.get(API_ENDPOINTS.PRODUCTS.GENRES, {
    params: subcategoryId ? { subcategoryId } : {},
  });
  return res.data;
};

export const getAllLanguages = async (): Promise<Language[]> => {
  const res = await api.get(API_ENDPOINTS.PRODUCTS.LANGUAGES);
  return res.data;
};

export const getAllSubcategories = async (categoryId?: number): Promise<Subcategory[]> => {
  const res = await api.get(API_ENDPOINTS.PRODUCTS.SUBCATEGORIES, {
    params: categoryId ? { categoryId } : {},
  });
  return res.data;
};

export const createProduct = async (payload: ProductRequest): Promise<Product> => {
  const res = await api.post(API_ENDPOINTS.PRODUCTS.BASE, payload);
  return res.data;
};

export const updateProduct = async (
  productId: number,
  payload: ProductRequest
): Promise<Product> => {
  const res = await api.put(`${API_ENDPOINTS.PRODUCTS.BASE}/${productId}`, payload);
  return res.data;
};
