import { useSelector, useDispatch } from "react-redux";
import { RootState, AppDispatch } from "../store";
import { logoutSession, setAuthSession } from "../store/slices/authSlice";
import { AuthResponse } from "../types/auth";

export function useAuth() {
  const dispatch = useDispatch<AppDispatch>();
  const { user, token, isLoggedIn, otpRequired, pendingEmail } = useSelector(
    (state: RootState) => state.auth
  );

  const loginSuccess = (auth: AuthResponse) => {
    dispatch(setAuthSession(auth));
  };

  const logout = () => {
    dispatch(logoutSession());
  };

  const isAdmin = user?.role === "ADMIN";

  return {
    user,
    token,
    isLoggedIn,
    otpRequired,
    pendingEmail,
    isAdmin,
    loginSuccess,
    logout,
  };
}
