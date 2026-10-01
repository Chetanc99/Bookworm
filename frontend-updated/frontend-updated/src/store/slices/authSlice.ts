import { createSlice, PayloadAction } from "@reduxjs/toolkit";
import { User, AuthResponse } from "../../types/auth";
import { getCurrentUser } from "../../services/auth.service";

interface AuthState {
  user: User | null;
  token: string | null;
  isLoggedIn: boolean;
  otpRequired: boolean;
  pendingEmail: string | null;
}

const initialToken = sessionStorage.getItem("token");
const initialUser = getCurrentUser();

const initialState: AuthState = {
  user: initialUser,
  token: initialToken,
  isLoggedIn: !!initialToken,
  otpRequired: false,
  pendingEmail: null,
};

const authSlice = createSlice({
  name: "auth",
  initialState,
  reducers: {
    setAuthSession: (state, action: PayloadAction<AuthResponse>) => {
      if (action.payload.otpRequired) {
        state.otpRequired = true;
        state.pendingEmail = action.payload.email;
      } else {
        state.user = {
          userId: action.payload.userId,
          email: action.payload.email,
          fullName: action.payload.fullName,
          role: action.payload.role,
        };
        state.token = action.payload.token;
        state.isLoggedIn = true;
        state.otpRequired = false;
        state.pendingEmail = null;
      }
    },
    logoutSession: (state) => {
      state.user = null;
      state.token = null;
      state.isLoggedIn = false;
      state.otpRequired = false;
      state.pendingEmail = null;
      sessionStorage.removeItem("token");
      sessionStorage.removeItem("user");
    },
    clearOtpFlag: (state) => {
      state.otpRequired = false;
      state.pendingEmail = null;
    },
  },
});

export const { setAuthSession, logoutSession, clearOtpFlag } = authSlice.actions;
export default authSlice.reducer;
