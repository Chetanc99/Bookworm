import api from "../api/client";
import { API_ENDPOINTS } from "../api/endpoints";
import {
  ProductCreditResponse,
  ProductStakeholderResponse,
  ProductBeneficiaryResponse,
  ProductBeneficiaryLine,
  StakeholderOption,
  CreditTypeOption,
  ProductTranslationResponse,
} from "../types/product";

// Product-level attribution/credit/royalty-split management, used by the
// Manage Catalog "Edit & Files" modal. Each of these is its own join-table
// resource on the backend (PRODUCT_CREDITS / PRODUCT_STAKEHOLDERS /
// PRODUCT_BENEFICIARIES) - not part of the plain ProductRequest payload
// (beneficiaries is the one exception, which the base updateProduct() call
// already supports directly) - so they're added/removed independently of
// the main "Save Product Changes" submit, mirroring how BeneficiariesPage
// manages beneficiaries independently of ProductsPage.

export const getProductCredits = async (productId: number): Promise<ProductCreditResponse[]> => {
  const res = await api.get(API_ENDPOINTS.PRODUCTS.CREDITS(productId));
  return res.data;
};

export const addProductCredit = async (
  productId: number,
  creditTypeId: number,
  creditValue: string
): Promise<ProductCreditResponse> => {
  const res = await api.post(API_ENDPOINTS.PRODUCTS.CREDITS(productId), { creditTypeId, creditValue });
  return res.data;
};

export const updateProductCredit = async (
  productId: number,
  productCreditId: number,
  creditTypeId: number,
  creditValue: string
): Promise<ProductCreditResponse> => {
  const res = await api.put(API_ENDPOINTS.PRODUCTS.CREDIT_BY_ID(productId, productCreditId), {
    creditTypeId,
    creditValue,
  });
  return res.data;
};

export const removeProductCredit = async (productId: number, productCreditId: number): Promise<void> => {
  await api.delete(API_ENDPOINTS.PRODUCTS.CREDIT_BY_ID(productId, productCreditId));
};

export const getProductStakeholders = async (productId: number): Promise<ProductStakeholderResponse[]> => {
  const res = await api.get(API_ENDPOINTS.PRODUCTS.STAKEHOLDERS(productId));
  return res.data;
};

// "Author name(s)" in the admin brief maps to a ProductStakeholder credit with
// role="Author" - there's no separate "author" field on Product itself
// (see Product entity javadoc: attribution lives in PRODUCT_STAKEHOLDERS, not
// as columns on PRODUCTS, so a book can have multiple authors/editors/etc.).
export const addProductStakeholder = async (
  productId: number,
  stakeholderId: number,
  role: string
): Promise<ProductStakeholderResponse> => {
  const res = await api.post(API_ENDPOINTS.PRODUCTS.STAKEHOLDERS(productId), { stakeholderId, role });
  return res.data;
};

export const updateProductStakeholder = async (
  productId: number,
  productStakeholderId: number,
  stakeholderId: number,
  role: string
): Promise<ProductStakeholderResponse> => {
  const res = await api.put(API_ENDPOINTS.PRODUCTS.STAKEHOLDER_BY_ID(productId, productStakeholderId), {
    stakeholderId,
    role,
  });
  return res.data;
};

export const removeProductStakeholder = async (
  productId: number,
  productStakeholderId: number
): Promise<void> => {
  await api.delete(API_ENDPOINTS.PRODUCTS.STAKEHOLDER_BY_ID(productId, productStakeholderId));
};

export const getProductBeneficiaries = async (productId: number): Promise<ProductBeneficiaryResponse[]> => {
  const res = await api.get(API_ENDPOINTS.PRODUCTS.BENEFICIARIES(productId));
  return res.data;
};

// Full replace, same semantics as including `beneficiaries` in a
// ProductRequest PUT - used when the admin edits splits independently of
// the rest of the product form.
export const replaceProductBeneficiaries = async (
  productId: number,
  lines: ProductBeneficiaryLine[]
): Promise<ProductBeneficiaryResponse[]> => {
  const res = await api.put(API_ENDPOINTS.PRODUCTS.BENEFICIARIES(productId), lines);
  return res.data;
};

export const getAllStakeholders = async (activeOnly = true): Promise<StakeholderOption[]> => {
  const res = await api.get(API_ENDPOINTS.STAKEHOLDERS.BASE, { params: { activeOnly } });
  return res.data;
};

export const getAllCreditTypes = async (): Promise<CreditTypeOption[]> => {
  const res = await api.get(API_ENDPOINTS.CREDIT_TYPES.BASE);
  return res.data;
};

// Bilingual display overlays - see ProductTranslation entity javadoc (backend).
export const getProductTranslations = async (productId: number): Promise<ProductTranslationResponse[]> => {
  const res = await api.get(`/products/${productId}/translations`);
  return res.data;
};

export const upsertProductTranslation = async (
  productId: number,
  payload: { languageId: number; title: string; shortDescription?: string; description?: string }
): Promise<ProductTranslationResponse> => {
  const res = await api.put(`/products/${productId}/translations`, payload);
  return res.data;
};

export const removeProductTranslation = async (productId: number, languageId: number): Promise<void> => {
  await api.delete(`/products/${productId}/translations/${languageId}`);
};
