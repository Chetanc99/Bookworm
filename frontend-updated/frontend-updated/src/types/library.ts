export interface LibraryPackage {
  libraryPackageId: number;
  packageName: string;
  description: string;
  price: number;
  durationDays: number;
  maxConcurrentBorrows: number;
  isActive: boolean;
}

export interface UserLibraryPackage {
  userLibraryPackageId: number;
  userId: number;
  libraryPackageId: number;
  packageName: string;
  startDate: string;
  endDate: string;
  status: string;
  purchasedAt: string;
}

export interface BorrowedItem {
  userLibraryId: number;
  productId: number;
  productTitle: string;
  borrowedAt: string;
  dueDate: string;
  status: string;
}

export interface UserShelfItem {
  userShelfId: number;
  productId: number;
  productTitle: string;
  coverImage?: string;
  source: "PURCHASE" | "RENT" | "LIBRARY";
  expiresAt?: string | null;
  acquiredAt: string;
  filePath?: string | null;
}

export interface UIShelfItem {
  id: number;
  name: string;
  image: string;
  purchaseType: "buy" | "rent";
  productExpiryDate?: string | null;
  filePath?: string | null;
}
