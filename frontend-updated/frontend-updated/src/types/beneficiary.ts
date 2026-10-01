export interface BeneficiaryResponse {
  beneficiaryId: number;
  name: string;
  description?: string;
  beneficiaryTypeId?: number;
  beneficiaryTypeName?: string;
  defaultRoyaltyPercentage?: number;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface RoyaltyLedgerResponse {
  royaltyLedgerId: number;
  beneficiaryId: number;
  beneficiaryName: string;
  productId: number;
  productTitle: string;
  sourceType: string;
  sourceReferenceId?: number;
  grossAmount: number;
  royaltyPercentage: number;
  royaltyAmount: number;
  status?: "UNPAID" | "PAID" | "REVERSED";
  createdAt: string;
  paidAt?: string;
}

export interface RoyaltySummaryResponse {
  beneficiaryId: number;
  beneficiaryName: string;
  totalRoyaltyEarned: number;
  unpaidRoyalty: number;
  paidRoyalty: number;
}
