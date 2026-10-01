import api from "../api/client";
import { API_ENDPOINTS } from "../api/endpoints";
import { AuthResponse, LoginPayload, RegisterPayload, User } from "../types/auth";

export const login = async (email: string, password: string): Promise<AuthResponse> => {
  const res = await api.post(API_ENDPOINTS.AUTH.LOGIN, {
    email: email.trim().toLowerCase(),
    password: password.trim(),
  });
  const auth: AuthResponse = res.data;
  if (!auth.otpRequired && auth.token) {
    persistSession(auth);
  }
  return auth;
};

export const verifyOtp = async (email: string, code: string): Promise<AuthResponse> => {
  const res = await api.post(API_ENDPOINTS.AUTH.VERIFY_OTP, { email, code });
  const auth: AuthResponse = res.data;
  if (auth.token) {
    persistSession(auth);
  }
  return auth;
};

export const register = async (payload: RegisterPayload): Promise<AuthResponse> => {
  const res = await api.post(API_ENDPOINTS.AUTH.REGISTER, {
    fullName: payload.fullName,
    email: payload.email.trim().toLowerCase(),
    password: payload.password,
  });
  const auth: AuthResponse = res.data;
  if (auth.token) {
    persistSession(auth);
  }
  return auth;
};

export const logout = (): void => {
  sessionStorage.removeItem("token");
  sessionStorage.removeItem("user");
};

export const persistSession = (auth: AuthResponse): void => {
  sessionStorage.setItem("token", auth.token);
  sessionStorage.setItem(
    "user",
    JSON.stringify({
      userId: auth.userId,
      email: auth.email,
      fullName: auth.fullName,
      role: auth.role,
    })
  );
};

export const getCurrentUser = (): User | null => {
  const stored = sessionStorage.getItem("user");
  return stored ? JSON.parse(stored) : null;
};

export const isAdmin = (user: User | null = getCurrentUser()): boolean => user?.role === "ADMIN";

export const isAuthenticated = (): boolean => !!sessionStorage.getItem("token");

export const forgotPassword = async (email: string): Promise<string> => {
  const res = await api.post(API_ENDPOINTS.AUTH.FORGOT_PASSWORD, { email });
  return res.envelope?.message || "Reset link requested successfully";
};

export const resetPassword = async (token: string, newPassword: string): Promise<string> => {
  const res = await api.post(API_ENDPOINTS.AUTH.RESET_PASSWORD, { token, newPassword });
  return res.envelope?.message || "Password reset successfully";
};

export const verifyEmail = async (token: string): Promise<string> => {
  const res = await api.get(API_ENDPOINTS.AUTH.VERIFY_EMAIL, { params: { token } });
  return res.envelope?.message || "Email verified successfully";
};

export const resendVerification = async (email: string): Promise<string> => {
  const res = await api.post(API_ENDPOINTS.AUTH.RESEND_VERIFICATION, { email });
  return res.envelope?.message || "Verification email sent";
};
