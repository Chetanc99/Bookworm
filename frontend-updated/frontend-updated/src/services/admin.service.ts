import api from "../api/client";
import { API_ENDPOINTS } from "../api/endpoints";
import { PageResponse } from "../types/api";
import { UserAdminResponse, AdminTransactionResponse, RevenueSummaryResponse, AuditLogResponse } from "../types/admin";

export const getAllUsers = async (page = 0, size = 20): Promise<PageResponse<UserAdminResponse>> => {
  const res = await api.get(API_ENDPOINTS.ADMIN.USERS, { params: { page, size } });
  return res.data;
};

export const getUserById = async (userId: number): Promise<UserAdminResponse> => {
  const res = await api.get(API_ENDPOINTS.ADMIN.USER_BY_ID(userId));
  return res.data;
};

export const changeUserRole = async (userId: number, roleName: string): Promise<UserAdminResponse> => {
  const res = await api.patch(API_ENDPOINTS.ADMIN.USER_ROLE(userId), { roleName });
  return res.data;
};

export const activateUser = async (userId: number): Promise<UserAdminResponse> => {
  const res = await api.patch(API_ENDPOINTS.ADMIN.USER_ACTIVATE(userId));
  return res.data;
};

export const deactivateUser = async (userId: number): Promise<UserAdminResponse> => {
  const res = await api.patch(API_ENDPOINTS.ADMIN.USER_DEACTIVATE(userId));
  return res.data;
};

export const getAllTransactions = async (page = 0, size = 20): Promise<PageResponse<AdminTransactionResponse>> => {
  const res = await api.get(API_ENDPOINTS.ADMIN.TRANSACTIONS, { params: { page, size } });
  return res.data;
};

export const getRevenueSummary = async (): Promise<RevenueSummaryResponse> => {
  const res = await api.get(API_ENDPOINTS.ADMIN.REVENUE_SUMMARY);
  return res.data;
};

export const getAuditLogs = async (page = 0, size = 20): Promise<PageResponse<AuditLogResponse>> => {
  const res = await api.get(API_ENDPOINTS.ADMIN.AUDIT_LOGS, { params: { page, size } });
  return res.data;
};
