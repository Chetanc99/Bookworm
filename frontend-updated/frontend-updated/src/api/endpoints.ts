export const API_ENDPOINTS = {
  AUTH: {
    LOGIN: "/auth/login",
    REGISTER: "/auth/register",
    VERIFY_OTP: "/auth/verify-otp",
    FORGOT_PASSWORD: "/auth/forgot-password",
    RESET_PASSWORD: "/auth/reset-password",
    VERIFY_EMAIL: "/auth/verify-email",
    RESEND_VERIFICATION: "/auth/resend-verification",
  },
  PRODUCTS: {
    BASE: "/products",
    GENRES: "/genres",
    LANGUAGES: "/languages",
    SUBCATEGORIES: "/subcategories",
    CREDITS: (productId: number) => `/products/${productId}/credits`,
    CREDIT_BY_ID: (productId: number, productCreditId: number) => `/products/${productId}/credits/${productCreditId}`,
    STAKEHOLDERS: (productId: number) => `/products/${productId}/stakeholders`,
    STAKEHOLDER_BY_ID: (productId: number, productStakeholderId: number) =>
      `/products/${productId}/stakeholders/${productStakeholderId}`,
    BENEFICIARIES: (productId: number) => `/products/${productId}/beneficiaries`,
  },
  STAKEHOLDERS: {
    BASE: "/stakeholders",
  },
  CREDIT_TYPES: {
    BASE: "/credit-types",
  },
  CART: {
    BASE: (userId: number) => `/users/${userId}/cart`,
    ITEMS: (userId: number) => `/users/${userId}/cart/items`,
    ITEM_BY_ID: (userId: number, cartItemId: number) => `/users/${userId}/cart/items/${cartItemId}`,
  },
  CHECKOUT: {
    PURCHASES: (userId: number) => `/users/${userId}/purchases`,
    RENTALS: (userId: number) => `/users/${userId}/rentals`,
    INVOICE: (userId: number, purchaseTransactionId: number) =>
      `/users/${userId}/purchases/${purchaseTransactionId}/invoice`,
  },
  SHELF: {
    BASE: (userId: number) => `/users/${userId}/shelf`,
  },
  LIBRARY: {
    PACKAGES: "/library-packages",
    PACKAGE_BY_ID: (id: number) => `/library-packages/${id}`,
    ACTIVE_SUBSCRIPTION: (userId: number) => `/users/${userId}/library/subscriptions/active`,
    SUBSCRIBE: (userId: number, packageId: number) => `/users/${userId}/library/subscriptions/${packageId}`,
    BORROWS: (userId: number) => `/users/${userId}/library/borrows`,
    RETURN: (userId: number, userLibraryId: number) => `/users/${userId}/library/borrows/${userLibraryId}/return`,
    ACTIVE_BORROWS: (userId: number) => `/users/${userId}/library/borrows/active`,
  },
  ADMIN: {
    USERS: "/admin/users",
    USER_BY_ID: (userId: number) => `/admin/users/${userId}`,
    USER_ROLE: (userId: number) => `/admin/users/${userId}/role`,
    USER_ACTIVATE: (userId: number) => `/admin/users/${userId}/activate`,
    USER_DEACTIVATE: (userId: number) => `/admin/users/${userId}/deactivate`,
    TRANSACTIONS: "/admin/transactions",
    REVENUE_SUMMARY: "/admin/transactions/summary",
    AUDIT_LOGS: "/admin/audit-logs",
    UPLOADS: (path: string) => `/admin/uploads/${path}`,
    BULK_IMPORT: "/admin/products/bulk-import",
  },
  BENEFICIARIES: {
    BASE: "/beneficiaries",
    BY_ID: (id: number) => `/beneficiaries/${id}`,
    ROYALTIES: (id: number) => `/beneficiaries/${id}/royalties`,
    ROYALTY_SUMMARY: (id: number) => `/beneficiaries/${id}/royalties/summary`,
  },
};
