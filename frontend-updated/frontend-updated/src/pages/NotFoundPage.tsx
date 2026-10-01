import React from "react";
import { Link } from "react-router-dom";
import { BookX, ArrowLeft } from "lucide-react";
import { Button } from "../components/ui/Button";

export const NotFoundPage: React.FC = () => {
  return (
    <div className="min-h-screen bg-[#FDFCFB] dark:bg-slate-950 flex items-center justify-center p-4">
      <div className="w-full max-w-md bg-white dark:bg-slate-900 border border-slate-100 dark:border-slate-800 rounded-3xl p-8 shadow-2xl text-center space-y-6">
        <div className="w-20 h-20 rounded-full bg-amber-500/10 text-amber-500 flex items-center justify-center mx-auto">
          <BookX size={48} />
        </div>

        <div className="space-y-2">
          <h1 className="text-4xl font-serif font-bold text-slate-900 dark:text-white">
            404
          </h1>
          <h2 className="text-xl font-serif font-bold text-slate-800 dark:text-slate-200">
            Page Not Found
          </h2>
          <p className="text-sm text-slate-500 dark:text-slate-400">
            The requested page does not exist or has been moved.
          </p>
        </div>

        <Link to="/" className="block pt-2">
          <Button variant="gold" size="lg" className="w-full" leftIcon={<ArrowLeft size={18} />}>
            Back to Home
          </Button>
        </Link>
      </div>
    </div>
  );
};
