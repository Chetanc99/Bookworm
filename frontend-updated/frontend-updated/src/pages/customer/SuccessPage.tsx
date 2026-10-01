import React from "react";
import { Link } from "react-router-dom";
import { CheckCircle, BookMarked, History } from "lucide-react";
import { Button } from "../../components/ui/Button";

export const SuccessPage: React.FC = () => {
  return (
    <div className="min-h-screen bg-[#FDFCFB] dark:bg-slate-950 flex items-center justify-center p-4">
      <div className="w-full max-w-md bg-white dark:bg-slate-900 border border-slate-100 dark:border-slate-800 rounded-3xl p-8 shadow-2xl text-center space-y-6">
        <div className="w-20 h-20 rounded-full bg-emerald-500/10 text-emerald-500 flex items-center justify-center mx-auto animate-bounce">
          <CheckCircle size={48} />
        </div>

        <div className="space-y-2">
          <h1 className="text-3xl font-serif font-bold text-slate-900 dark:text-white">
            Order Confirmed!
          </h1>
          <p className="text-sm text-slate-500 dark:text-slate-400">
            Thank you for your purchase. Your digital books have been added directly to your personal shelf.
          </p>
        </div>

        <div className="flex flex-col gap-3 pt-4">
          <Link to="/shelf" className="w-full">
            <Button variant="gold" size="lg" className="w-full" leftIcon={<BookMarked size={18} />}>
              Go to My Digital Shelf
            </Button>
          </Link>
          <Link to="/orders" className="w-full">
            <Button variant="outline" size="lg" className="w-full" leftIcon={<History size={18} />}>
              View Order History
            </Button>
          </Link>
        </div>
      </div>
    </div>
  );
};
