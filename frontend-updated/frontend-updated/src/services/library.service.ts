import api from "../api/client";
import { API_ENDPOINTS } from "../api/endpoints";
import { LibraryPackage, UserLibraryPackage, BorrowedItem } from "../types/library";

export const getAllLibraryPackages = async (): Promise<LibraryPackage[]> => {
  const res = await api.get(API_ENDPOINTS.LIBRARY.PACKAGES);
  return res.data;
};

export const getLibraryPackageById = async (libraryPackageId: number): Promise<LibraryPackage> => {
  const res = await api.get(API_ENDPOINTS.LIBRARY.PACKAGE_BY_ID(libraryPackageId));
  return res.data;
};

export const getActiveSubscription = async (userId: number): Promise<UserLibraryPackage | null> => {
  try {
    const res = await api.get(API_ENDPOINTS.LIBRARY.ACTIVE_SUBSCRIPTION(userId));
    return res.data;
  } catch (err: any) {
    if (err.response?.status === 400 || err.response?.status === 404) return null;
    throw err;
  }
};

export const subscribeToPackage = async (
  userId: number,
  libraryPackageId: number
): Promise<UserLibraryPackage> => {
  const res = await api.post(API_ENDPOINTS.LIBRARY.SUBSCRIBE(userId, libraryPackageId));
  return res.data;
};

export const borrowProduct = async (
  userId: number,
  productId: number,
  borrowDays: number
): Promise<BorrowedItem> => {
  const res = await api.post(API_ENDPOINTS.LIBRARY.BORROWS(userId), { productId, borrowDays });
  return res.data;
};

export const returnBorrowedItem = async (
  userId: number,
  userLibraryId: number
): Promise<BorrowedItem> => {
  const res = await api.post(API_ENDPOINTS.LIBRARY.RETURN(userId, userLibraryId));
  return res.data;
};

export const getActiveBorrows = async (userId: number): Promise<BorrowedItem[]> => {
  const res = await api.get(API_ENDPOINTS.LIBRARY.ACTIVE_BORROWS(userId));
  return res.data;
};
