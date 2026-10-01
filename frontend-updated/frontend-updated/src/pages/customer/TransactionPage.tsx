import React from "react";
import { useNavigate } from "react-router-dom";
import { ShoppingBag, Clock, ArrowRight, ArrowLeft } from "lucide-react";
import { useAuth } from "../../hooks/useAuth";
import { useCart } from "../../hooks/useCart";
import { updateCartItemAPI, getCartAPI } from "../../services/cart.service";
import { Button } from "../../components/ui/Button";
import { resolveFileUrl, FALLBACK_BOOK_IMAGE } from "../../api/client";
import { UICartItem } from "../../types/cart";
import toast from "react-hot-toast";

export const TransactionPage: React.FC = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const { items, updateItems, setType, purchaseTotal, rentalTotal } = useCart();

  const refreshCartState = async () => {
    if (!user?.userId) return;
    const res = await getCartAPI(user.userId);
    const mapped: UICartItem[] = (res.data?.items || []).map((i) => ({
      cartItemId: i.cartItemId,
      id: i.productId,
      name: i.productTitle,
      image: resolveFileUrl(i.coverImage),
      price: Number(i.lineTotal || 0),
      intent: i.intent,
      rentDays: i.rentDays,
      rentable: true,
      rentPerDay: Number(i.rentRate || 0),
      minRentDays: i.minRentDays || 1,
    }));
    updateItems(mapped);
  };

  const handleToggleIntent = async (item: UICartItem, targetIntent: "PURCHASE" | "RENT") => {
    if (!user?.userId) return;
    try {
      const defaultRentDays = targetIntent === "RENT" ? (item.minRentDays || 7) : null;
      await updateCartItemAPI(user.userId, item.cartItemId, item.id, targetIntent, defaultRentDays);
      await refreshCartState();
      toast.success(`Switched "${item.name}" to ${targetIntent}`);
    } catch (err: any) {
      toast.error(err.message || "Failed to update item intent");
    }
  };

  const handleUpdateRentDays = async (item: UICartItem, days: number) => {
    if (!user?.userId) return;
    try {
      await updateCartItemAPI(user.userId, item.cartItemId, item.id, "RENT", days);
      await refreshCartState();
      toast.success(`Set rental duration for "${item.name}" to ${days} days`);
    } catch (err: any) {
      toast.error(err.message || "Failed to update rental duration");
    }
  };

  const handleProceed = (type: "buy" | "rent") => {
    setType(type);
    navigate("/payment");
  };

  return (
    <div className="min-h-screen bg-[#FDFCFB] dark:bg-slate-950 py-12">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 space-y-8">
        <div>
          <button
            onClick={() => navigate("/cart")}
            className="text-xs font-bold text-amber-500 hover:underline flex items-center gap-1 mb-2"
          >
            <ArrowLeft size={14} /> Back to Cart
          </button>
          <h1 className="font-serif text-3xl font-bold text-slate-900 dark:text-white">
            Purchase vs. Rental Options
          </h1>
          <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">
            Choose whether to buy items permanently or rent them for custom durations
          </p>
        </div>

        <div className="space-y-4">
          {items.map((item) => (
            <div
              key={item.cartItemId}
              className="bg-white dark:bg-slate-900 border border-slate-100 dark:border-slate-800 rounded-3xl p-6 shadow-sm flex flex-col md:flex-row md:items-center justify-between gap-4"
            >
              <div className="flex items-center gap-4">
                <img
                  src={item.image}
                  alt={item.name}
                  className="w-16 h-24 object-cover rounded-xl shadow-inner bg-slate-100 dark:bg-slate-800"
                  onError={(e: any) => {
                    e.target.src = FALLBACK_BOOK_IMAGE;
                  }}
                />
                <div>
                  <h3 className="font-serif font-bold text-lg text-slate-900 dark:text-white">
                    {item.name}
                  </h3>
                  <p className="text-sm font-semibold text-amber-500 mt-1">
                    Line Total: ₹{item.price.toLocaleString("en-IN")}
                  </p>
                  {item.intent === "RENT" && (
                    <div className="flex items-center gap-2 mt-2">
                      <span className="text-xs text-slate-400">Rent Duration:</span>
                      {[7, 14, 30].map((d) => (
                        <button
                          key={d}
                          onClick={() => handleUpdateRentDays(item, d)}
                          className={`px-2.5 py-1 rounded-lg text-xs font-bold transition-colors ${
                            item.rentDays === d
                              ? "bg-amber-500 text-slate-950"
                              : "bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-400 hover:bg-slate-200"
                          }`}
                        >
                          {d} Days
                        </button>
                      ))}
                    </div>
                  )}
                </div>
              </div>

              <div className="flex items-center gap-3">
                <Button
                  variant={item.intent === "PURCHASE" ? "gold" : "outline"}
                  size="sm"
                  onClick={() => handleToggleIntent(item, "PURCHASE")}
                  leftIcon={<ShoppingBag size={14} />}
                >
                  Buy Permanent
                </Button>
                <Button
                  variant={item.intent === "RENT" ? "gold" : "outline"}
                  size="sm"
                  onClick={() => handleToggleIntent(item, "RENT")}
                  leftIcon={<Clock size={14} />}
                >
                  Rent Book
                </Button>
              </div>
            </div>
          ))}
        </div>

        {/* PROCEED ACTIONS */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6 pt-4">
          <div className="bg-white dark:bg-slate-900 border border-slate-100 dark:border-slate-800 rounded-3xl p-6 shadow-sm space-y-4">
            <h4 className="font-serif font-bold text-lg text-slate-900 dark:text-white">
              Checkout Permanent Purchases
            </h4>
            <p className="text-xs text-slate-500">
              Total for items selected as BUY:{" "}
              <span className="font-bold text-slate-900 dark:text-white">
                ₹{purchaseTotal.toLocaleString("en-IN")}
              </span>
            </p>
            <Button
              variant="gold"
              size="md"
              className="w-full"
              disabled={purchaseTotal === 0}
              onClick={() => handleProceed("buy")}
              rightIcon={<ArrowRight size={16} />}
            >
              Pay Permanent Purchase (₹{purchaseTotal})
            </Button>
          </div>

          <div className="bg-white dark:bg-slate-900 border border-slate-100 dark:border-slate-800 rounded-3xl p-6 shadow-sm space-y-4">
            <h4 className="font-serif font-bold text-lg text-slate-900 dark:text-white">
              Checkout Rental Items
            </h4>
            <p className="text-xs text-slate-500">
              Total for items selected as RENT:{" "}
              <span className="font-bold text-slate-900 dark:text-white">
                ₹{rentalTotal.toLocaleString("en-IN")}
              </span>
            </p>
            <Button
              variant="gold"
              size="md"
              className="w-full"
              disabled={rentalTotal === 0}
              onClick={() => handleProceed("rent")}
              rightIcon={<ArrowRight size={16} />}
            >
              Pay Rental Fee (₹{rentalTotal})
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
};
