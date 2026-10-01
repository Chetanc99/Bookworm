export type MediaType = "BOOK" | "AUDIOBOOK" | "VIDEO_COURSE" | "PODCAST";

export interface Product {
  productId: number;
  subcategoryId: number;
  subcategoryName: string;
  genreId?: number;
  genreName?: string;
  languageId?: number;
  languageName?: string;
  mediaType?: MediaType;
  episodeCount?: number;
  title: string;
  shortDescription?: string;
  description?: string;
  price: number;
  pages?: number;
  duration?: number;
  coverImage?: string;
  filePath?: string;
  fileType?: string;
  isRentable: boolean;
  isLibraryEligible: boolean;
  rentRate?: number;
  minRentDays?: number;
  isAvailable: boolean;
}

export interface Genre {
  genreId: number;
  genreName: string;
  description?: string;
}

export interface Language {
  languageId: number;
  languageName: string;
}

export interface Subcategory {
  subcategoryId: number;
  subcategoryName: string;
  categoryId?: number;
}

export interface ProductBeneficiaryLine {
  beneficiaryId: number;
  royaltyPercentage?: number;
}

export interface ProductRequest {
  subcategoryId: number;
  genreId?: number;
  languageId?: number;
  beneficiaryId?: number;
  // Preferred royalty-split path - a full replace of the product's beneficiary
  // allocation on every update (see ProductBeneficiaryServiceImpl.replaceAssignments).
  // Undefined = "don't touch existing allocation"; [] = "clear all splits".
  beneficiaries?: ProductBeneficiaryLine[];
  mediaType?: MediaType;
  episodeCount?: number;
  title: string;
  shortDescription?: string;
  description?: string;
  price: number;
  pages?: number;
  duration?: number;
  coverImage?: string;
  filePath?: string;
  fileType?: string;
  isRentable?: boolean;
  isLibraryEligible?: boolean;
  rentRate?: number;
  minRentDays?: number;
  isAvailable?: boolean;
}

export interface ProductCreditResponse {
  productCreditId: number;
  creditTypeId: number;
  creditTypeName: string;
  creditValue: string;
}

export interface ProductStakeholderResponse {
  productStakeholderId: number;
  stakeholderId: number;
  stakeholderName: string;
  stakeholderType?: string;
  role?: string;
}

export interface ProductBeneficiaryResponse {
  productBeneficiaryId: number;
  beneficiaryId: number;
  beneficiaryName: string;
  royaltyPercentage: number;
}

export interface StakeholderOption {
  stakeholderId: number;
  name: string;
  type?: string;
}

export interface CreditTypeOption {
  creditTypeId: number;
  creditTypeName: string;
}

export interface ProductTranslationResponse {
  productTranslationId: number;
  languageId: number;
  languageName: string;
  title: string;
  shortDescription?: string;
  description?: string;
}
