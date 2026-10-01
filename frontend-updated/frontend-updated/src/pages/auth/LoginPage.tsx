import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { BookOpen, Mail, Lock, KeyRound, ArrowRight } from "lucide-react";
import { login, verifyOtp } from "../../services/auth.service";
import { useAuth } from "../../hooks/useAuth";
import { Input } from "../../components/ui/Input";
import { Button } from "../../components/ui/Button";
import toast from "react-hot-toast";

// api/client.ts's baseURL already carries "/api/v1"; the OAuth2 handshake
// endpoints Spring Security registers (/oauth2/authorization/google) live
// at the app root, not under /api/v1, so strip that suffix here.
const API_ROOT = (import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api/v1").replace(
  /\/api\/v1\/?$/,
  ""
);
const GOOGLE_LOGIN_URL = `${API_ROOT}/oauth2/authorization/google`;

export const LoginPage: React.FC = () => {
  const navigate = useNavigate();
  const { loginSuccess } = useAuth();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [otp, setOtp] = useState("");
  const [isOtpStep, setIsOtpStep] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  const handleLoginSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!email || !password) {
      toast.error("Please enter email and password");
      return;
    }
    setIsLoading(true);
    try {
      const res = await login(email, password);
      if (res.otpRequired) {
        setIsOtpStep(true);
        toast.success("OTP sent to your email!");
      } else {
        loginSuccess(res);
        toast.success(`Welcome back, ${res.fullName}!`);
        if (res.role === "ADMIN" || res.role === "ROLE_ADMIN") {
          navigate("/admin/dashboard");
        } else {
          navigate("/");
        }
      }
    } catch (err: any) {
      toast.error(err.message || "Invalid credentials");
    } finally {
      setIsLoading(false);
    }
  };

  const handleOtpSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!otp) {
      toast.error("Please enter the verification OTP");
      return;
    }
    setIsLoading(true);
    try {
      const res = await verifyOtp(email, otp);
      loginSuccess(res);
      toast.success("OTP verified successfully!");
      if (res.role === "ADMIN") {
        navigate("/admin/dashboard");
      } else {
        navigate("/");
      }
    } catch (err: any) {
      toast.error(err.message || "Invalid or expired OTP");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-950 flex items-center justify-center p-4">
      <div className="w-full max-w-md bg-slate-900 border border-slate-800 rounded-3xl p-8 shadow-2xl space-y-6">
        <div className="text-center space-y-2">
          <Link to="/" className="inline-flex items-center gap-2 group mb-2">
            <div className="w-12 h-12 rounded-2xl bg-amber-500 flex items-center justify-center text-slate-950 font-bold shadow-lg">
              <BookOpen size={26} />
            </div>
          </Link>
          <h2 className="text-2xl font-serif font-bold text-white">
            {isOtpStep ? "Enter OTP Code" : "Sign In to BookWorm"}
          </h2>
          <p className="text-sm text-slate-400">
            {isOtpStep
              ? `We have sent a verification code to ${email}`
              : "Enter your account credentials to access your library"}
          </p>
        </div>

        {!isOtpStep ? (
          <form onSubmit={handleLoginSubmit} className="space-y-4">
            <Input
              label="Email Address"
              type="email"
              placeholder="you@example.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              leftIcon={<Mail size={18} />}
              required
            />

            <div className="space-y-1">
              <Input
                label="Password"
                type="password"
                placeholder="••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                leftIcon={<Lock size={18} />}
                required
              />
              <div className="flex justify-end pt-1">
                <Link
                  to="/forgot-password"
                  className="text-xs text-amber-400 hover:underline font-medium"
                >
                  Forgot password?
                </Link>
              </div>
            </div>

            <Button
              type="submit"
              variant="gold"
              size="lg"
              className="w-full mt-2"
              isLoading={isLoading}
              rightIcon={<ArrowRight size={18} />}
            >
              Sign In
            </Button>

            <div className="flex items-center gap-3 pt-1">
              <div className="h-px flex-1 bg-slate-800" />
              <span className="text-xs text-slate-500">or</span>
              <div className="h-px flex-1 bg-slate-800" />
            </div>

            <a
              href={GOOGLE_LOGIN_URL}
              className="w-full flex items-center justify-center gap-2 rounded-xl border border-slate-700 bg-slate-800 hover:bg-slate-700 transition-colors text-sm font-medium text-white py-2.5"
            >
              <svg width="18" height="18" viewBox="0 0 18 18" aria-hidden="true">
                <path fill="#4285F4" d="M17.64 9.2c0-.64-.06-1.25-.16-1.84H9v3.48h4.84a4.14 4.14 0 0 1-1.8 2.72v2.26h2.9c1.7-1.57 2.7-3.88 2.7-6.62z" />
                <path fill="#34A853" d="M9 18c2.43 0 4.47-.8 5.96-2.18l-2.9-2.26c-.8.54-1.84.86-3.06.86-2.35 0-4.34-1.59-5.05-3.72H.9v2.33A9 9 0 0 0 9 18z" />
                <path fill="#FBBC05" d="M3.95 10.7A5.4 5.4 0 0 1 3.67 9c0-.59.1-1.17.28-1.7V4.97H.9A9 9 0 0 0 0 9c0 1.45.35 2.83.9 4.03z" />
                <path fill="#EA4335" d="M9 3.58c1.32 0 2.51.45 3.44 1.35l2.58-2.58C13.46.89 11.43 0 9 0A9 9 0 0 0 .9 4.97l3.05 2.33C4.66 5.17 6.65 3.58 9 3.58z" />
              </svg>
              Continue with Google
            </a>
          </form>
        ) : (
          <form onSubmit={handleOtpSubmit} className="space-y-4">
            <Input
              label="6-Digit OTP Code"
              type="text"
              placeholder="123456"
              value={otp}
              onChange={(e) => setOtp(e.target.value)}
              leftIcon={<KeyRound size={18} />}
              required
            />

            <Button
              type="submit"
              variant="gold"
              size="lg"
              className="w-full"
              isLoading={isLoading}
            >
              Verify OTP & Sign In
            </Button>

            <button
              type="button"
              onClick={() => setIsOtpStep(false)}
              className="w-full text-xs text-slate-400 hover:text-white transition-colors"
            >
              ← Back to password login
            </button>
          </form>
        )}

        <div className="text-center pt-4 border-t border-slate-800">
          <p className="text-xs text-slate-400">
            Don't have an account?{" "}
            <Link to="/register" className="text-amber-400 font-bold hover:underline">
              Create an account
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
};
