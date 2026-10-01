export interface UserAdminResponse {
  userId: number;
  email: string;
  fullName: string;
  roleName: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface AdminTransactionResponse {
  transactionType: "PURCHASE" | "RENT";
  transactionId: number;
  userId: number;
  userEmail: string;
  userFullName: string;
  totalAmount: number;
  status?: string;
  createdAt: string;
}

export interface RevenueSummaryResponse {
  totalRevenue: number;
  purchaseCount: number;
  rentCount: number;
}

export interface AuditLogResponse {
  auditLogId: number;
  actorUserId: number;
  actorEmail: string;
  action: string;
  entityType: string;
  entityId: string;
  details: string;
  createdAt: string;
}

export interface BulkImportResponse {
  totalRows: number;
  successCount: number;
  failureCount: number;
  results: {
    rowNumber: number;
    status: "SUCCESS" | "FAILURE";
    message: string;
    productTitle?: string;
  }[];
}
