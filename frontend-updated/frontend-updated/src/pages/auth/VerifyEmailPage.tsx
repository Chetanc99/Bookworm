import React, { useEffect, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { CheckCircle2, XCircle, Loader2 } from "lucide-react";
import { verifyEmail } from "../../services/auth.service";
import { Button } from "../../components/ui/Button";

export const VerifyEmailPage: React.FC = () => {
  const [searchParams] = useSearchParams();
  const token = searchParams.get("token");
  const [status, setStatus] = useState<"loading" | "success" | "error">("loading");
  const [message, setMessage] = useState("");

  useEffect(() => {
    if (!token) {
      setStatus("error");
      setMessage("Missing verification token.");
      return;
    }

    verifyEmail(token)
      .then((msg) => {
        setStatus("success");
        setMessage(msg || "Email verified successfully!");
      })
      .catch((err) => {
        setStatus("error");
        setMessage(err.message || "Failed to verify email.");
      });
  }, [token]);

  return (
    <div className="min-h-screen bg-slate-950 flex items-center justify-center p-4">
      <div className="w-full max-w-md bg-slate-900 border border-slate-800 rounded-3xl p-8 shadow-2xl text-center space-y-6">
        {status === "loading" && (
          <div className="flex flex-col items-center gap-3">
            <Loader2 className="w-12 h-12 text-amber-500 animate-spin" />
            <h2 className="text-xl font-serif font-bold text-white">Verifying Your Email...</h2>
          </div>
        )}

        {status === "success" && (
          <div className="flex flex-col items-center gap-3">
            <CheckCircle2 className="w-16 h-16 text-emerald-500" />
            <h2 className="text-2xl font-serif font-bold text-white">Email Verified!</h2>
            <p className="text-sm text-slate-400">{message}</p>
            <Link to="/login" className="w-full pt-4">
              <Button variant="gold" size="lg" className="w-full">
                Proceed to Sign In
              </Button>
            </Link>
          </div>
        )}

        {status === "error" && (
          <div className="flex flex-col items-center gap-3">
            <XCircle className="w-16 h-16 text-rose-500" />
            <h2 className="text-2xl font-serif font-bold text-white">Verification Failed</h2>
            <p className="text-sm text-slate-400">{message}</p>
            <Link to="/login" className="w-full pt-4">
              <Button variant="outline" size="lg" className="w-full">
                Back to Sign In
              </Button>
            </Link>
          </div>
        )}
      </div>
    </div>
  );
};
