import React from "react";
import { Link, useNavigate } from "react-router-dom";
import { Star, ArrowRight, ShieldCheck, Zap, Headphones } from "lucide-react";
import { useFetch } from "../../hooks/useFetch";
import { useAuth } from "../../hooks/useAuth";
import { browseProducts, getAllLanguages } from "../../services/product.service";
import { addToCartAPI } from "../../services/cart.service";
import { ProductCard } from "../../components/domain/products/ProductCard";
import { Skeleton } from "../../components/ui/Skeleton";
import { Button } from "../../components/ui/Button";
import { findEnglishLanguageId } from "../../utils/language";
import toast from "react-hot-toast";

export const HomePage: React.FC = () => {
  const navigate = useNavigate();
  const { user } = useAuth();

  const { data: languages } = useFetch(getAllLanguages, []);
  const englishLanguageId = findEnglishLanguageId(languages);

  const { data: featuredData, isLoading } = useFetch(
    () => browseProducts({ page: 0, size: 4, displayLanguageId: englishLanguageId }),
    [englishLanguageId]
  );

  const handleAddToCart = async (product: any) => {
    if (!user?.userId) {
      toast.error("Please sign in to add items to cart");
      navigate("/login");
      return;
    }
    try {
      await addToCartAPI(user.userId, product.productId, "PURCHASE", null);
      toast.success(`"${product.title}" added to cart!`);
    } catch (err: any) {
      toast.error(err.message || "Failed to add to cart");
    }
  };

  const handleRentToCart = async (product: any) => {
    if (!user?.userId) {
      toast.error("Please sign in to rent books");
      navigate("/login");
      return;
    }
    try {
      await addToCartAPI(user.userId, product.productId, "RENT", product.minRentDays || 7);
      toast.success(`Rental of "${product.title}" added to cart!`);
    } catch (err: any) {
      toast.error(err.message || "Failed to add rental");
    }
  };

  return (
    <div className="space-y-20 pb-20">
      {/* HERO SECTION */}
      <section className="relative overflow-hidden bg-slate-950 text-white pt-20 pb-28">
        <div className="absolute inset-0 bg-[radial-gradient(ellipse_80%_80%_at_50%_-20%,rgba(245,158,11,0.15),rgba(255,255,255,0))]" />
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 relative z-10 grid grid-cols-1 lg:grid-cols-2 gap-12 items-center">
          <div className="space-y-6 text-center lg:text-left">
            <div className="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-amber-500/10 border border-amber-500/20 text-amber-400 text-xs font-semibold uppercase tracking-widest">
              <Star size={14} className="fill-amber-400" /> Discover Your Next Favorite Book
            </div>
            <h1 className="font-serif text-4xl sm:text-5xl lg:text-6xl font-bold leading-tight tracking-tight">
              Unlock Unlimited Stories & Technical Knowledge
            </h1>
            <p className="text-slate-400 text-base sm:text-lg max-w-xl mx-auto lg:mx-0 leading-relaxed">
              Buy, rent, or subscribe to thousands of curated eBooks, audiobooks, academic journals, and literature instantly.
            </p>
            <div className="flex flex-wrap gap-4 justify-center lg:justify-start pt-2">
              <Link to="/products">
                <Button variant="gold" size="lg" rightIcon={<ArrowRight size={18} />}>
                  Explore Books
                </Button>
              </Link>
              <Link to="/library">
                <Button variant="outline" size="lg" className="border-slate-700 text-white hover:bg-slate-800">
                  View Subscription Plans
                </Button>
              </Link>
            </div>
          </div>

          <div className="relative flex justify-center">
            <div className="w-72 sm:w-80 aspect-[2/3] rounded-3xl overflow-hidden shadow-2xl border-4 border-amber-500/30 rotate-3 transform hover:rotate-0 transition-transform duration-500">
              <img
                src="https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=600&auto=format&fit=crop&q=80"
                alt="Book hero"
                className="w-full h-full object-cover"
              />
            </div>
          </div>
        </div>
      </section>

      {/* FEATURES HIGHLIGHT */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div className="bg-white dark:bg-slate-900 p-8 rounded-3xl border border-slate-100 dark:border-slate-800 shadow-sm space-y-3">
            <div className="w-12 h-12 rounded-2xl bg-amber-500/10 text-amber-500 flex items-center justify-center">
              <Zap size={24} />
            </div>
            <h3 className="font-serif font-bold text-xl text-slate-900 dark:text-white">Flexible Ownership</h3>
            <p className="text-sm text-slate-500 dark:text-slate-400 leading-relaxed">
              Buy permanently, rent by the day, or borrow through library subscription packages.
            </p>
          </div>

          <div className="bg-white dark:bg-slate-900 p-8 rounded-3xl border border-slate-100 dark:border-slate-800 shadow-sm space-y-3">
            <div className="w-12 h-12 rounded-2xl bg-emerald-500/10 text-emerald-500 flex items-center justify-center">
              <ShieldCheck size={24} />
            </div>
            <h3 className="font-serif font-bold text-xl text-slate-900 dark:text-white">Verified Content</h3>
            <p className="text-sm text-slate-500 dark:text-slate-400 leading-relaxed">
              Direct publishing partnerships ensuring authentic content, clear text, and high quality audio.
            </p>
          </div>

          <div className="bg-white dark:bg-slate-900 p-8 rounded-3xl border border-slate-100 dark:border-slate-800 shadow-sm space-y-3">
            <div className="w-12 h-12 rounded-2xl bg-sky-500/10 text-sky-500 flex items-center justify-center">
              <Headphones size={24} />
            </div>
            <h3 className="font-serif font-bold text-xl text-slate-900 dark:text-white">Multi-Device Access</h3>
            <p className="text-sm text-slate-500 dark:text-slate-400 leading-relaxed">
              Read PDFs directly in your personal digital shelf from any phone, tablet, or web browser.
            </p>
          </div>
        </div>
      </section>

      {/* FEATURED BOOKS */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 space-y-8">
        <div className="flex items-end justify-between border-b border-slate-100 dark:border-slate-800 pb-4">
          <div>
            <span className="text-xs font-bold text-amber-500 uppercase tracking-widest">Featured Literature</span>
            <h2 className="font-serif text-3xl font-bold text-slate-900 dark:text-white mt-1">Trending Books</h2>
          </div>
          <Link to="/products" className="text-sm font-bold text-amber-500 hover:underline flex items-center gap-1">
            View All Catalog <ArrowRight size={16} />
          </Link>
        </div>

        {isLoading ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
            {[1, 2, 3, 4].map((n) => (
              <Skeleton key={n} className="h-96 w-full rounded-3xl" />
            ))}
          </div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
            {featuredData?.content?.map((product) => (
              <ProductCard
                key={product.productId}
                product={product}
                onAddToCart={handleAddToCart}
                onRentToCart={handleRentToCart}
              />
            ))}
          </div>
        )}
      </section>
    </div>
  );
};
