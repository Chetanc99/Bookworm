import React from "react";
import { BookMarked, Download, FileText, Clock, Library } from "lucide-react";
import { useAuth } from "../../hooks/useAuth";
import { useFetch } from "../../hooks/useFetch";
import { getShelfAPI } from "../../services/shelf.service";
import { resolveFileUrl, FALLBACK_BOOK_IMAGE } from "../../api/client";
import { Badge } from "../../components/ui/Badge";
import { EmptyState } from "../../components/ui/EmptyState";
import { Skeleton } from "../../components/ui/Skeleton";
import { Button } from "../../components/ui/Button";
import { UIShelfItem } from "../../types/library";

export const ShelfPage: React.FC = () => {
  const { user } = useAuth();

  const { data: shelfItems, isLoading } = useFetch(
    async (): Promise<(UIShelfItem & { source: string })[]> => {
      if (!user?.userId) return [];
      const res = await getShelfAPI(user.userId);
      return (res.data || []).map((item) => ({
        id: item.productId,
        name: item.productTitle,
        image: resolveFileUrl(item.coverImage),
        purchaseType: item.source === "PURCHASE" ? "buy" : item.source === "LIBRARY" ? "borrow" : "rent",
        source: item.source,
        productExpiryDate: item.expiresAt || null,
        filePath: item.filePath || null,
      }));
    },
    [user?.userId]
  );

  return (
    <div className="min-h-screen bg-[#FDFCFB] dark:bg-slate-950 py-12">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 space-y-8">
        <div className="border-b border-slate-100 dark:border-slate-800 pb-4">
          <h1 className="font-serif text-3xl font-bold text-slate-900 dark:text-white">
            My Digital Shelf
          </h1>
          <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">
            Access, view, and read all your purchased, rented, and borrowed library eBooks
          </p>
        </div>

        {isLoading ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
            {[1, 2, 3, 4].map((n) => (
              <Skeleton key={n} className="h-80 w-full rounded-3xl" />
            ))}
          </div>
        ) : !shelfItems || shelfItems.length === 0 ? (
          <EmptyState
            icon={<BookMarked size={48} className="text-slate-300 dark:text-slate-600" />}
            title="Your Shelf is Empty"
            description="You don't own, rent, or borrow any digital books yet. Browse our catalog to get started."
          />
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
            {shelfItems.map((item) => (
              <div
                key={item.id}
                className="bg-white dark:bg-slate-900 border border-slate-100 dark:border-slate-800 rounded-3xl p-4 shadow-sm space-y-4 flex flex-col justify-between"
              >
                <div className="relative aspect-[2/3] rounded-2xl overflow-hidden bg-slate-100 dark:bg-slate-800 shadow-inner">
                  <img
                    src={item.image}
                    alt={item.name}
                    className="w-full h-full object-cover"
                    onError={(e: any) => {
                      e.target.src = FALLBACK_BOOK_IMAGE;
                    }}
                  />
                  <div className="absolute top-3 left-3">
                    {item.source === "PURCHASE" && (
                      <Badge variant="gold">Purchased</Badge>
                    )}
                    {item.source === "RENT" && (
                      <Badge variant="warning">Rented</Badge>
                    )}
                    {item.source === "LIBRARY" && (
                      <Badge variant="info">Borrowed (Pass)</Badge>
                    )}
                  </div>
                </div>

                <div className="space-y-2">
                  <h3 className="font-serif font-bold text-base text-slate-900 dark:text-white line-clamp-2">
                    {item.name}
                  </h3>
                  {item.productExpiryDate && (
                    <p className="text-xs text-amber-500 flex items-center gap-1 font-medium">
                      <Clock size={12} /> {item.source === "LIBRARY" ? "Due Date:" : "Expires:"} {new Date(item.productExpiryDate).toLocaleDateString()}
                    </p>
                  )}
                </div>

                {item.filePath ? (
                  <a
                    href={resolveFileUrl(item.filePath)}
                    target="_blank"
                    rel="noreferrer"
                    className="block"
                  >
                    <Button variant="gold" size="sm" className="w-full" leftIcon={<FileText size={14} />}>
                      Read / View Book
                    </Button>
                  </a>
                ) : (
                  <Button variant="outline" size="sm" className="w-full" disabled leftIcon={<Download size={14} />}>
                    Content Pending
                  </Button>
                )}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};
