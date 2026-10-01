import api from "../api/client";
import { API_ENDPOINTS } from "../api/endpoints";
import { BeneficiaryResponse } from "../types/beneficiary";

export const getAllBeneficiaries = async (activeOnly = true): Promise<BeneficiaryResponse[]> => {
  const res = await api.get(API_ENDPOINTS.BENEFICIARIES.BASE, { params: { activeOnly } });
  return res.data;
};

export const createBeneficiary = async (payload: {
  name: string;
  description?: string;
  beneficiaryTypeId?: number | null;
}): Promise<BeneficiaryResponse> => {
  const res = await api.post(API_ENDPOINTS.BENEFICIARIES.BASE, payload);
  return res.data;
};

export const updateBeneficiary = async (
  beneficiaryId: number,
  payload: { name: string; description?: string; beneficiaryTypeId?: number | null; isActive?: boolean }
): Promise<BeneficiaryResponse> => {
  const res = await api.put(API_ENDPOINTS.BENEFICIARIES.BY_ID(beneficiaryId), payload);
  return res.data;
};

export const deactivateBeneficiary = async (beneficiaryId: number): Promise<BeneficiaryResponse> => {
  const res = await api.delete(API_ENDPOINTS.BENEFICIARIES.BY_ID(beneficiaryId));
  return res.data;
};

// Reuses the same PUT /beneficiaries/{id} endpoint update() already exposes
// (BeneficiaryRequest.isActive lets it flip either direction) - no new
// backend endpoint needed just to toggle a beneficiary back on. name must be
// re-sent because BeneficiaryRequest.name is @NotBlank on the backend, so
// callers pass the beneficiary's current name/description/type through.
export const activateBeneficiary = async (
  beneficiaryId: number,
  current: { name: string; description?: string; beneficiaryTypeId?: number | null }
): Promise<BeneficiaryResponse> => {
  const res = await api.put(API_ENDPOINTS.BENEFICIARIES.BY_ID(beneficiaryId), {
    ...current,
    isActive: true,
  });
  return res.data;
};
