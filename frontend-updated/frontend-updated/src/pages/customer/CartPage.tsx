import React, { useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Trash2, ShoppingBag, ArrowRight, ArrowLeft } from "lucide-react";
import { useAuth } from "../../hooks/useAuth";
import { useCart } from "../../hooks/useCart";
import { getCartAPI, removeFromCartAPI, clearCartAPI } from "../../services/cart.service";
import { resolveFileUrl, FALLBACK_BOOK_IMAGE } from "../../api/client";
import { Button } from "../../components/ui/Button";
import { EmptyState } from "../../components/ui/EmptyState";
import { UICartItem } from "../../types/cart";
import toast from "react-hot-toast";

export const CartPage: React.FC = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const { items, updateItems, clear, purchaseTotal } = useCart();

  const fetchUserCart = async () => {
    if (!user?.userId) return;
    try {
      const res = await getCartAPI(user.userId);
      const cartResponse = res.data;
      const mapped: UICartItem[] = (cartResponse?.items || []).map((item) => ({
        cartItemId: item.cartItemId,
        id: item.productId,
        name: item.productTitle,
        image: resolveFileUrl(item.coverImage),
        price: Number(item.lineTotal || 0),
        intent: item.intent,
        rentDays: item.rentDays,
        rentable: false,
        rentPerDay: 0,
        minRentDays: 1,
      }));
      updateItems(mapped);
    } catch (err: any) {
      toast.error(err.message || "Failed to load cart");
    }
  };

  useEffect(() => {
    fetchUserCart();
  }, [user]);

  const handleRemoveItem = async (cartItemId: number) => {
    if (!user?.userId) return;
    try {
      await removeFromCartAPI(user.userId, cartItemId);
      toast.success("Item removed from cart");
      fetchUserCart();
    } catch (err: any) {
      toast.error(err.message || "Failed to remove item");
    }
  };

  const handleClearCart = async () => {
    if (!user?.userId) return;
    try {
      await clearCartAPI(user.userId);
      clear();
      toast.success("Cart cleared");
    } catch (err: any) {
      toast.error(err.message || "Failed to clear cart");
    }
  };

  return (
    <div className="min-h-screen bg-[#FDFCFB] dark:bg-slate-950 py-12">
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 space-y-8">
        <div className="flex items-center justify-between border-b border-slate-100 dark:border-slate-800 pb-4">
          <div>
            <h1 className="font-serif text-3xl font-bold text-slate-900 dark:text-white">
              Shopping Cart
            </h1>
            <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">
              Review your items before selecting payment intent
            </p>
          </div>
          {items.length > 0 && (
            <button
              onClick={handleClearCart}
              className="text-xs text-rose-500 font-bold hover:underline flex items-center gap-1"
            >
              <Trash2 size={14} /> Clear Cart
            </button>
          )}
        </div>

        {items.length === 0 ? (
          <EmptyState
            icon={<ShoppingBag size={48} className="text-slate-300 dark:text-slate-600" />}
            title="Your Cart is Empty"
            description="Looks like you haven't added any books to your cart yet."
            action={
              <Link to="/products">
                <Button variant="gold" size="md">
                  Explore Catalog
                </Button>
              </Link>
            }
          />
        ) : (
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
            <div className="lg:col-span-2 space-y-4">
              {items.map((item) => (
                <div
                  key={item.cartItemId}
                  className="bg-white dark:bg-slate-900 border border-slate-100 dark:border-slate-800 rounded-3xl p-5 shadow-sm flex items-center gap-5"
                >
                  <img
                    src={item.image}
                    alt={item.name}
                    className="w-20 h-28 object-cover rounded-xl shadow-inner bg-slate-100 dark:bg-slate-800"
                    onError={(e: any) => {
                      e.target.src = FALLBACK_BOOK_IMAGE;
                    }}
                  />
                  <div className="flex-1">
                    <h3 className="font-serif font-bold text-lg text-slate-900 dark:text-white">
                      {item.name}
                    </h3>
                    <p className="text-xs font-semibold text-amber-500 uppercase mt-1">
                      Intent: {item.intent}
                    </p>
                    <p className="text-lg font-serif font-bold text-slate-900 dark:text-white mt-2">
                      ₹{item.price.toLocaleString("en-IN")}
                    </p>
                  </div>
                  <button
                    onClick={() => handleRemoveItem(item.cartItemId)}
                    className="p-2 text-slate-400 hover:text-rose-500 transition-colors"
                  >
                    <Trash2 size={20} />
                  </button>
                </div>
              ))}
            </div>

            {/* ORDER SUMMARY */}
            <div className="bg-white dark:bg-slate-900 border border-slate-100 dark:border-slate-800 rounded-3xl p-6 h-fit shadow-sm space-y-6">
              <h3 className="font-serif font-bold text-xl text-slate-900 dark:text-white border-b border-slate-100 dark:border-slate-800 pb-3">
                Order Summary
              </h3>

              <div className="space-y-3 text-sm">
                <div className="flex justify-between text-slate-600 dark:text-slate-300">
                  <span>Items Total ({items.length})</span>
                  <span className="font-semibold text-slate-900 dark:text-white">
                    ₹{purchaseTotal.toLocaleString("en-IN")}
                  </span>
                </div>
                <div className="flex justify-between text-slate-600 dark:text-slate-300">
                  <span>Estimated Tax</span>
                  <span className="font-semibold text-emerald-500">FREE</span>
                </div>
                <div className="pt-3 border-t border-slate-100 dark:border-slate-800 flex justify-between text-lg font-serif font-bold text-slate-900 dark:text-white">
                  <span>Total Amount</span>
                  <span className="text-amber-500">₹{purchaseTotal.toLocaleString("en-IN")}</span>
                </div>
              </div>

              <Link to="/transaction" className="block pt-2">
                <Button variant="gold" size="lg" className="w-full" rightIcon={<ArrowRight size={18} />}>
                  Proceed to Intent Select
                </Button>
              </Link>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
