import React, { useState } from "react";
import { Link } from "react-router-dom";
import { BookOpen, Mail, ArrowLeft, Send } from "lucide-react";
import { forgotPassword } from "../../services/auth.service";
import { Input } from "../../components/ui/Input";
import { Button } from "../../components/ui/Button";
import toast from "react-hot-toast";

export const ForgotPasswordPage: React.FC = () => {
  const [email, setEmail] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [submitted, setSubmitted] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!email) return;
    setIsLoading(true);
    try {
      const msg = await forgotPassword(email);
      setSubmitted(true);
      toast.success(msg || "Reset email instructions sent!");
    } catch (err: any) {
      toast.error(err.message || "Failed to process forgot password request");
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
          <h2 className="text-2xl font-serif font-bold text-white">Reset Password</h2>
          <p className="text-sm text-slate-400">
            {submitted
              ? "Check your inbox for password reset instructions."
              : "Enter your registered email address to receive reset instructions."}
          </p>
        </div>

        {!submitted ? (
          <form onSubmit={handleSubmit} className="space-y-4">
            <Input
              label="Email Address"
              type="email"
              placeholder="you@example.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              leftIcon={<Mail size={18} />}
              required
            />

            <Button
              type="submit"
              variant="gold"
              size="lg"
              className="w-full mt-2"
              isLoading={isLoading}
              rightIcon={<Send size={18} />}
            >
              Send Reset Link
            </Button>
          </form>
        ) : (
          <div className="bg-slate-800/50 p-4 rounded-2xl border border-slate-700 text-center space-y-3">
            <p className="text-xs text-slate-300">
              If an account exists for <span className="font-bold text-white">{email}</span>, a token has been generated.
            </p>
            <Link to="/reset-password">
              <Button variant="outline" size="sm" className="w-full mt-2">
                Have a reset token? Click here
              </Button>
            </Link>
          </div>
        )}

        <div className="text-center pt-4 border-t border-slate-800">
          <Link to="/login" className="inline-flex items-center gap-1.5 text-xs text-amber-400 font-bold hover:underline">
            <ArrowLeft size={14} /> Back to Sign In
          </Link>
        </div>
      </div>
    </div>
  );
};
