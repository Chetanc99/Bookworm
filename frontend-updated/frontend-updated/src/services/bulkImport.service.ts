import api from "../api/client";
import { API_ENDPOINTS } from "../api/endpoints";
import { BulkImportResponse } from "../types/admin";

export const bulkImportProducts = async (file: File): Promise<BulkImportResponse> => {
  const formData = new FormData();
  formData.append("file", file);
  const res = await api.post(API_ENDPOINTS.ADMIN.BULK_IMPORT, formData, {
    headers: { "Content-Type": undefined },
  });
  return res.data;
};
