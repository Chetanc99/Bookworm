import React from "react";
import { Product } from "../../../types/product";
import { resolveFileUrl } from "../../../api/client";
import { Modal } from "../../ui/Modal";
import { Button } from "../../ui/Button";
import { Badge } from "../../ui/Badge";
import { ShoppingBag, BookOpen, Clock, FileText, Globe, Library, Headphones, Video, Mic } from "lucide-react";
import { ProductAvailability } from "../../../hooks/useOwnershipMap";

const MEDIA_TYPE_META: Record<string, { label: string; icon: React.ReactNode }> = {
  AUDIOBOOK: { label: "Audiobook", icon: <Headphones size={12} /> },
  VIDEO_COURSE: { label: "Video Course", icon: <Video size={12} /> },
  PODCAST: { label: "Podcast", icon: <Mic size={12} /> },
};

export interface ProductDetailModalProps {
  product: Product | null;
  onClose: () => void;
  onAddToCart?: (product: Product) => void;
  onRentToCart?: (product: Product) => void;
  onBorrow?: (product: Product) => void;
  hasActiveSubscription?: boolean;
  availability?: ProductAvailability;
}

export const ProductDetailModal: React.FC<ProductDetailModalProps> = ({
  product,
  onClose,
  onAddToCart,
  onRentToCart,
  onBorrow,
  hasActiveSubscription = false,
  availability,
}) => {
  if (!product) return null;

  const imageUrl = resolveFileUrl(product.coverImage);
  const canPurchase = availability?.canPurchase !== false;
  const canRent = availability?.canRent !== false;
  const canBorrow = availability?.canBorrow !== false;

  return (
    <Modal isOpen={!!product} onClose={onClose} maxWidth="xl">
      <div className="grid grid-cols-1 md:grid-cols-5 gap-6">
        {/* COVER IMAGE */}
        <div className="md:col-span-2 aspect-[2/3] rounded-2xl overflow-hidden bg-slate-100 dark:bg-slate-800 shadow-md">
          <img
            src={imageUrl}
            alt={product.title}
            className="w-full h-full object-cover"
            onError={(e: any) => {
              e.target.src = "https://images.unsplash.com/photo-1541963463532-d68292c34b19?w=400&auto=format&fit=crop&q=60";
            }}
          />
        </div>

        {/* DETAILS */}
        <div className="md:col-span-3 flex flex-col justify-between space-y-4">
          <div className="space-y-3">
            <div className="flex items-center gap-2 flex-wrap">
              {product.mediaType && MEDIA_TYPE_META[product.mediaType] && (
                <Badge variant="info" className="flex items-center gap-1">
                  {MEDIA_TYPE_META[product.mediaType].icon}
                  {MEDIA_TYPE_META[product.mediaType].label}
                </Badge>
              )}
              {product.genreName && <Badge variant="gold">{product.genreName}</Badge>}
              {product.languageName && <Badge variant="neutral">{product.languageName}</Badge>}
              {product.isRentable && <Badge variant="warning">Rentable</Badge>}
              {product.isLibraryEligible && <Badge variant="info">Library Pass</Badge>}
            </div>

            <h2 className="text-2xl font-serif font-bold text-slate-900 dark:text-white leading-snug">
              {product.title}
            </h2>

            <div className="grid grid-cols-2 gap-3 bg-slate-50 dark:bg-slate-800/50 p-3 rounded-xl text-xs text-slate-600 dark:text-slate-300">
              {product.pages && (
                <div className="flex items-center gap-1.5">
                  <FileText size={14} className="text-amber-500" />
                  <span>{product.pages} Pages</span>
                </div>
              )}
              {product.duration && (
                <div className="flex items-center gap-1.5">
                  <Clock size={14} className="text-amber-500" />
                  <span>{product.duration} Mins</span>
                </div>
              )}
              <div className="flex items-center gap-1.5">
                <Globe size={14} className="text-amber-500" />
                <span>{product.languageName || "English"}</span>
              </div>
              <div className="flex items-center gap-1.5">
                <BookOpen size={14} className="text-amber-500" />
                <span>{product.fileType || "PDF"} Format</span>
              </div>
            </div>

            <p className="text-sm text-slate-600 dark:text-slate-300 leading-relaxed max-h-40 overflow-y-auto">
              {product.description || product.shortDescription || "No detailed description available."}
            </p>
          </div>

          <div className="pt-4 border-t border-slate-100 dark:border-slate-800 flex flex-col gap-3">
            <div className="flex items-center justify-between">
              <div>
                <span className="text-xs text-slate-400 font-medium">Purchase Price</span>
                <p className="text-2xl font-serif font-bold text-slate-900 dark:text-white">
                  ₹{product.price.toLocaleString("en-IN")}
                </p>
              </div>
              {product.isRentable && product.rentRate && (
                <div className="text-right">
                  <span className="text-xs text-amber-500 font-medium">Rent Rate</span>
                  <p className="text-lg font-bold text-amber-500">
                    ₹{product.rentRate}/day (min {product.minRentDays || 1} days)
                  </p>
                </div>
              )}
            </div>

            <div className="flex flex-col gap-2">
              {hasActiveSubscription && product.isLibraryEligible && onBorrow && (
                <Button
                  variant="gold"
                  size="md"
                  className="w-full font-bold shadow-md"
                  onClick={() => {
                    onBorrow(product);
                    onClose();
                  }}
                  leftIcon={<Library size={16} />}
                  disabled={!canBorrow}
                  title={!canBorrow ? availability?.borrowReason : undefined}
                >
                  Borrow with Library Pass
                </Button>
              )}

              <div className="flex gap-2">
                {onAddToCart && (
                  <Button
                    variant={hasActiveSubscription && product.isLibraryEligible ? "outline" : "gold"}
                    size="md"
                    className="flex-1"
                    onClick={() => {
                      onAddToCart(product);
                      onClose();
                    }}
                    leftIcon={<ShoppingBag size={16} />}
                    disabled={!canPurchase}
                    title={!canPurchase ? availability?.purchaseReason : undefined}
                  >
                    {availability?.status === "PURCHASED" ? "Already Owned" : "Buy Product"}
                  </Button>
                )}

                {product.isRentable && onRentToCart && (
                  <Button
                    variant="outline"
                    size="md"
                    className="flex-1 border-amber-500/50 text-amber-600 dark:text-amber-400 hover:bg-amber-500/10"
                    onClick={() => {
                      onRentToCart(product);
                      onClose();
                    }}
                    leftIcon={<Clock size={16} />}
                    disabled={!canRent}
                    title={!canRent ? availability?.rentReason : undefined}
                  >
                    Rent Book
                  </Button>
                )}
              </div>
              {!canPurchase && availability?.purchaseReason && (
                <p className="text-xs text-slate-500">{availability.purchaseReason}</p>
              )}
              {canPurchase && !canRent && availability?.rentReason && (
                <p className="text-xs text-slate-500">{availability.rentReason}</p>
              )}
              {canRent && !canBorrow && availability?.borrowReason && (
                <p className="text-xs text-slate-500">{availability.borrowReason}</p>
              )}
            </div>
          </div>
        </div>
      </div>
    </Modal>
  );
};
