import axios, { AxiosInstance, InternalAxiosRequestConfig, AxiosResponse } from "axios";

// Extend AxiosResponse to hold original envelope
declare module "axios" {
  export interface AxiosResponse {
    envelope?: any;
  }
}

const api: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api/v1",
  headers: {
    "Content-Type": "application/json",
  },
});

/* ================= REQUEST INTERCEPTOR ================= */
api.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // Exclude public auth endpoints from attaching token if unauthenticated
    if (
      config.url?.includes("/auth/login") ||
      config.url?.includes("/auth/register")
    ) {
      return config;
    }

    const token = sessionStorage.getItem("token");
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
  },
  (error) => Promise.reject(error)
);

/* ================= RESPONSE INTERCEPTOR ================= */
api.interceptors.response.use(
  (response: AxiosResponse) => {
    // Unwrap ApiResponse<T> envelope if present: { success, message, data, timestamp }
    if (
      response.data &&
      typeof response.data === "object" &&
      "data" in response.data &&
      "success" in response.data
    ) {
      response.envelope = response.data;
      response.data = response.data.data;
    }
    return response;
  },
  (error) => {
    if (error.response?.status === 401) {
      sessionStorage.removeItem("token");
      sessionStorage.removeItem("user");
      if (window.location.pathname !== "/login") {
        window.location.href = "/login";
      }
    }
    const backendMessage = error.response?.data?.message;
    if (backendMessage) {
      error.message = backendMessage;
    }
    return Promise.reject(error);
  }
);

const apiOrigin = (import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api/v1").replace(
  /\/api\/v1\/?$/,
  ""
);

export const FALLBACK_BOOK_IMAGE = "https://images.unsplash.com/photo-1541963463532-d68292c34b19?w=400&auto=format&fit=crop&q=60";

export const resolveFileUrl = (path?: string | null): string => {
  if (!path || path.trim() === "") return FALLBACK_BOOK_IMAGE;
  if (/^https?:\/\//i.test(path)) return path;
  if (path.startsWith("/Books-images") || path.startsWith("Books-images")) {
    return path.startsWith("/") ? path : `/${path}`;
  }
  if (path.startsWith("/uploads") || path.startsWith("uploads")) {
    return `${apiOrigin}${path.startsWith("/") ? "" : "/"}${path}`;
  }
  return path.startsWith("/") ? path : `/${path}`;
};

export default api;
