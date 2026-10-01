import React from "react";
import { Link } from "react-router-dom";
import { Library, CheckCircle2, RotateCcw, BookOpen, Clock } from "lucide-react";
import { useAuth } from "../../hooks/useAuth";
import { useFetch } from "../../hooks/useFetch";
import { getActiveSubscription, getActiveBorrows, returnBorrowedItem } from "../../services/library.service";
import { Badge } from "../../components/ui/Badge";
import { Button } from "../../components/ui/Button";
import { EmptyState } from "../../components/ui/EmptyState";
import { Skeleton } from "../../components/ui/Skeleton";
import toast from "react-hot-toast";

export const MySubscriptionPage: React.FC = () => {
  const { user } = useAuth();

  const { data: subscription, isLoading: isSubLoading, refetch: refetchSub } = useFetch(
    async () => {
      if (!user?.userId) return null;
      return getActiveSubscription(user.userId);
    },
    [user?.userId]
  );

  const { data: borrows, isLoading: isBorrowsLoading, refetch: refetchBorrows } = useFetch(
    async () => {
      if (!user?.userId) return [];
      return getActiveBorrows(user.userId);
    },
    [user?.userId]
  );

  const handleReturnItem = async (userLibraryId: number) => {
    if (!user?.userId) return;
    try {
      await returnBorrowedItem(user.userId, userLibraryId);
      toast.success("Borrowed book returned successfully");
      refetchBorrows();
    } catch (err: any) {
      toast.error(err.message || "Failed to return book");
    }
  };

  return (
    <div className="min-h-screen bg-[#FDFCFB] dark:bg-slate-950 py-12">
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 space-y-8">
        <div className="border-b border-slate-100 dark:border-slate-800 pb-4">
          <h1 className="font-serif text-3xl font-bold text-slate-900 dark:text-white">
            My Library Subscription
          </h1>
          <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">
            Manage your active membership package and current borrowed books
          </p>
        </div>

        {/* ACTIVE SUBSCRIPTION DETAILS */}
        {isSubLoading ? (
          <Skeleton className="h-40 w-full rounded-3xl" />
        ) : !subscription ? (
          <EmptyState
            icon={<Library size={48} className="text-slate-300 dark:text-slate-600" />}
            title="No Active Subscription"
            description="You do not currently have an active library subscription package."
            action={
              <Link to="/library">
                <Button variant="gold" size="md">
                  View Subscription Packages
                </Button>
              </Link>
            }
          />
        ) : (
          <div className="bg-gradient-to-r from-amber-500/10 via-amber-500/5 to-transparent border border-amber-500/30 rounded-3xl p-8 space-y-4">
            <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
              <div>
                <Badge variant="gold" size="md">
                  ACTIVE MEMBERSHIP
                </Badge>
                <h2 className="font-serif font-bold text-2xl text-slate-900 dark:text-white mt-2">
                  {subscription.packageName}
                </h2>
                <p className="text-xs text-slate-500 mt-1">
                  Valid from {new Date(subscription.startDate).toLocaleDateString()} until{" "}
                  <strong className="text-slate-900 dark:text-white">
                    {new Date(subscription.endDate).toLocaleDateString()}
                  </strong>
                </p>
              </div>

              <Link to="/products">
                <Button variant="gold" size="md" leftIcon={<BookOpen size={16} />}>
                  Browse & Borrow Books
                </Button>
              </Link>
            </div>
          </div>
        )}

        {/* ACTIVE BORROWED BOOKS */}
        {subscription && (
          <div className="space-y-6 pt-4">
            <h3 className="font-serif font-bold text-xl text-slate-900 dark:text-white">
              Currently Borrowed Books ({borrows?.length || 0})
            </h3>

            {isBorrowsLoading ? (
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                {[1, 2].map((n) => (
                  <Skeleton key={n} className="h-32 w-full rounded-2xl" />
                ))}
              </div>
            ) : !borrows || borrows.length === 0 ? (
              <p className="text-sm text-slate-400 font-medium">
                You have no active borrowed books at this moment.
              </p>
            ) : (
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                {borrows.map((item) => (
                  <div
                    key={item.userLibraryId}
                    className="bg-white dark:bg-slate-900 border border-slate-100 dark:border-slate-800 rounded-2xl p-5 shadow-sm flex items-center justify-between gap-4"
                  >
                    <div className="space-y-1">
                      <h4 className="font-serif font-bold text-slate-900 dark:text-white">
                        {item.productTitle}
                      </h4>
                      <p className="text-xs text-amber-500 flex items-center gap-1">
                        <Clock size={12} /> Due: {new Date(item.dueDate).toLocaleDateString()}
                      </p>
                    </div>

                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => handleReturnItem(item.userLibraryId)}
                      leftIcon={<RotateCcw size={14} />}
                    >
                      Return
                    </Button>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
};
