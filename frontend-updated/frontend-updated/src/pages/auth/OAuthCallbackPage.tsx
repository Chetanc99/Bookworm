import React, { useEffect, useRef } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";
import { persistSession } from "../../services/auth.service";
import toast from "react-hot-toast";

/**
 * Landing page for the Google OAuth2 redirect. SecurityConfig's
 * OAuth2AuthenticationSuccessHandler sends the browser here with the same
 * fields AuthResponse carries (token/userId/email/fullName/role) as query
 * params - there's no other way to hand a token back after a full-page
 * redirect. We rebuild an AuthResponse-shaped object client-side, persist it
 * exactly like a normal email/password login, and drop into the app.
 */
export const OAuthCallbackPage: React.FC = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { loginSuccess } = useAuth();
  const handled = useRef(false);

  useEffect(() => {
    if (handled.current) return;
    handled.current = true;

    const error = searchParams.get("error");
    if (error) {
      toast.error(error);
      navigate("/login", { replace: true });
      return;
    }

    const token = searchParams.get("token");
    const userId = searchParams.get("userId");
    const email = searchParams.get("email");
    const fullName = searchParams.get("fullName");
    const role = searchParams.get("role");

    if (!token || !userId || !email || !role) {
      toast.error("Google sign-in did not complete. Please try again.");
      navigate("/login", { replace: true });
      return;
    }

    const auth = {
      token,
      userId: Number(userId),
      email,
      fullName: fullName || email,
      role,
      otpRequired: false,
    };

    persistSession(auth);
    loginSuccess(auth);
    toast.success(`Welcome, ${auth.fullName}!`);
    navigate(role === "ADMIN" ? "/admin/dashboard" : "/", { replace: true });
  }, [searchParams, navigate, loginSuccess]);

  return (
    <div className="min-h-screen bg-slate-950 flex items-center justify-center p-4">
      <p className="text-slate-400">Signing you in with Google...</p>
    </div>
  );
};
