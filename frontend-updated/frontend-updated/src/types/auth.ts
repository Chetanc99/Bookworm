export type UserRole = "CUSTOMER" | "ADMIN";

export interface User {
  userId: number;
  email: string;
  fullName: string;
  role: UserRole;
  isActive?: boolean;
}

export interface AuthResponse {
  token: string;
  userId: number;
  email: string;
  fullName: string;
  role: UserRole;
  otpRequired?: boolean;
}

export interface LoginPayload {
  email: string;
  password: string;
}

export interface RegisterPayload {
  fullName: string;
  email: string;
  password: string;
}
