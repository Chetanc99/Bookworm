import React, { useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { CheckCircle2, ShieldCheck, ArrowLeft } from "lucide-react";
import { useAuth } from "../../hooks/useAuth";
import { useFetchById } from "../../hooks/useFetchById";
import { getLibraryPackageById, subscribeToPackage } from "../../services/library.service";
import { Button } from "../../components/ui/Button";
import toast from "react-hot-toast";

export const SubscribeConfirmPage: React.FC = () => {
  const { libraryPackageId } = useParams<{ libraryPackageId: string }>();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [isLoading, setIsLoading] = useState(false);

  const { data: pkg, isLoading: isPkgLoading } = useFetchById(
    (id) => getLibraryPackageById(Number(id)),
    libraryPackageId
  );

  const handleConfirmSubscription = async () => {
    if (!user?.userId || !pkg) {
      toast.error("Please sign in to complete your subscription");
      navigate("/login");
      return;
    }

    setIsLoading(true);
    try {
      await subscribeToPackage(user.userId, pkg.libraryPackageId);
      toast.success(`Subscribed to ${pkg.packageName}!`);
      navigate("/my-library");
    } catch (err: any) {
      toast.error(err.message || "Failed to complete subscription");
    } finally {
      setIsLoading(false);
    }
  };

  if (isPkgLoading || !pkg) {
    return (
      <div className="min-h-screen bg-[#FDFCFB] dark:bg-slate-950 flex items-center justify-center">
        <div className="w-8 h-8 border-4 border-amber-500 border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-[#FDFCFB] dark:bg-slate-950 py-16 flex items-center justify-center p-4">
      <div className="w-full max-w-md bg-white dark:bg-slate-900 border border-slate-100 dark:border-slate-800 rounded-3xl p-8 shadow-2xl space-y-6">
        <div>
          <button
            onClick={() => navigate("/library")}
            className="text-xs font-bold text-amber-500 hover:underline flex items-center gap-1 mb-2"
          >
            <ArrowLeft size={14} /> Back to Packages
          </button>
          <h1 className="font-serif text-2xl font-bold text-slate-900 dark:text-white">
            Confirm Subscription
          </h1>
          <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">
            Review your package details before activating
          </p>
        </div>

        <div className="bg-slate-50 dark:bg-slate-800/50 p-6 rounded-2xl border border-slate-100 dark:border-slate-800 space-y-4">
          <div>
            <span className="text-xs font-bold text-amber-500 uppercase tracking-widest">
              Selected Package
            </span>
            <h3 className="font-serif font-bold text-xl text-slate-900 dark:text-white mt-1">
              {pkg.packageName}
            </h3>
            <p className="text-xs text-slate-500 mt-1">{pkg.description}</p>
          </div>

          <div className="pt-3 border-t border-slate-200 dark:border-slate-700 flex justify-between items-center text-sm font-semibold">
            <span>Duration:</span>
            <span>{pkg.durationDays} Days</span>
          </div>

          <div className="flex justify-between items-center text-sm font-semibold">
            <span>Max Borrows:</span>
            <span>{pkg.maxConcurrentBorrows} Books</span>
          </div>

          <div className="pt-3 border-t border-slate-200 dark:border-slate-700 flex justify-between items-center text-lg font-serif font-bold">
            <span>Total Price:</span>
            <span className="text-amber-500">₹{pkg.price.toLocaleString("en-IN")}</span>
          </div>
        </div>

        <Button
          variant="gold"
          size="lg"
          className="w-full"
          isLoading={isLoading}
          onClick={handleConfirmSubscription}
          rightIcon={<CheckCircle2 size={18} />}
        >
          Confirm & Activate Subscription
        </Button>
      </div>
    </div>
  );
};
