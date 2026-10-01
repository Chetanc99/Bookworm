import React from "react";
import { Link } from "react-router-dom";
import { Library, Check, Zap, ArrowRight } from "lucide-react";
import { useFetch } from "../../hooks/useFetch";
import { getAllLibraryPackages } from "../../services/library.service";
import { Card } from "../../components/ui/Card";
import { Button } from "../../components/ui/Button";
import { Skeleton } from "../../components/ui/Skeleton";

export const LibraryPackagesPage: React.FC = () => {
  const { data: packages, isLoading } = useFetch(getAllLibraryPackages, []);

  return (
    <div className="min-h-screen bg-[#FDFCFB] dark:bg-slate-950 py-16">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 space-y-12">
        <div className="text-center max-w-2xl mx-auto space-y-4">
          <div className="inline-flex items-center gap-2 px-4 py-1.5 rounded-full bg-amber-500/10 text-amber-500 text-xs font-bold uppercase tracking-widest">
            <Library size={16} /> Subscription Plans
          </div>
          <h1 className="font-serif text-4xl font-bold text-slate-900 dark:text-white">
            Library Subscription Membership
          </h1>
          <p className="text-sm text-slate-500 dark:text-slate-400 leading-relaxed">
            Get access to borrow eligible digital books for fixed terms with concurrent reading allowances.
          </p>
        </div>

        {isLoading ? (
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            {[1, 2, 3].map((n) => (
              <Skeleton key={n} className="h-96 w-full rounded-3xl" />
            ))}
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            {packages?.map((pkg) => (
              <Card
                key={pkg.libraryPackageId}
                className="flex flex-col justify-between p-8 border border-slate-100 dark:border-slate-800 rounded-3xl"
              >
                <div className="space-y-6">
                  <div>
                    <span className="text-xs font-bold text-amber-500 uppercase tracking-widest">
                      {pkg.durationDays} Days Duration
                    </span>
                    <h3 className="font-serif font-bold text-2xl text-slate-900 dark:text-white mt-1">
                      {pkg.packageName}
                    </h3>
                    <p className="text-sm text-slate-500 dark:text-slate-400 mt-2 leading-relaxed">
                      {pkg.description}
                    </p>
                  </div>

                  <div className="pt-4 border-t border-slate-100 dark:border-slate-800">
                    <span className="text-xs text-slate-400 font-medium">Subscription Price</span>
                    <p className="text-4xl font-serif font-bold text-slate-900 dark:text-white mt-1">
                      ₹{pkg.price.toLocaleString("en-IN")}
                    </p>
                  </div>

                  <ul className="space-y-3 text-xs text-slate-600 dark:text-slate-300">
                    <li className="flex items-center gap-2">
                      <Check size={16} className="text-emerald-500" />
                      <span>Up to <strong>{pkg.maxConcurrentBorrows}</strong> simultaneous book borrows</span>
                    </li>
                    <li className="flex items-center gap-2">
                      <Check size={16} className="text-emerald-500" />
                      <span>Access to entire Library Eligible catalog</span>
                    </li>
                    <li className="flex items-center gap-2">
                      <Check size={16} className="text-emerald-500" />
                      <span>{pkg.durationDays} Days full membership period</span>
                    </li>
                  </ul>
                </div>

                <Link to={`/library/subscribe/${pkg.libraryPackageId}`} className="block pt-8">
                  <Button variant="gold" size="lg" className="w-full" rightIcon={<ArrowRight size={18} />}>
                    Choose Package
                  </Button>
                </Link>
              </Card>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};
