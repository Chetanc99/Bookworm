import api from "../api/client";
import { API_ENDPOINTS } from "../api/endpoints";
import { PageResponse } from "../types/api";
import { RoyaltyLedgerResponse, RoyaltySummaryResponse } from "../types/beneficiary";

export const getRoyaltyHistory = async (
  beneficiaryId: number,
  page = 0,
  size = 20
): Promise<PageResponse<RoyaltyLedgerResponse>> => {
  const res = await api.get(API_ENDPOINTS.BENEFICIARIES.ROYALTIES(beneficiaryId), {
    params: { page, size },
  });
  return res.data;
};

export const getRoyaltySummary = async (beneficiaryId: number): Promise<RoyaltySummaryResponse> => {
  const res = await api.get(API_ENDPOINTS.BENEFICIARIES.ROYALTY_SUMMARY(beneficiaryId));
  return res.data;
};
