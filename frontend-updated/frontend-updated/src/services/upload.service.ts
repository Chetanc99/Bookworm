import api from "../api/client";
import { API_ENDPOINTS } from "../api/endpoints";

const upload = async (file: File, path: string): Promise<string> => {
  const formData = new FormData();
  formData.append("file", file);
  const res = await api.post(API_ENDPOINTS.ADMIN.UPLOADS(path), formData, {
    headers: { "Content-Type": undefined },
  });
  return res.data.url;
};

export const uploadCoverImage = (file: File) => upload(file, "images");
export const uploadContentFile = (file: File) => upload(file, "content");
