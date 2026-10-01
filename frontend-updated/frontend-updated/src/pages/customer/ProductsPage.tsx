import React, { useState } from "react";
import { Search, Filter, RefreshCw, Library, Zap } from "lucide-react";
import { useFetch } from "../../hooks/useFetch";
import { browseProducts, getAllGenres, getAllLanguages } from "../../services/product.service";
import { addToCartAPI } from "../../services/cart.service";
import { getActiveSubscription, borrowProduct } from "../../services/library.service";
import { useAuth } from "../../hooks/useAuth";
import { ProductCard } from "../../components/domain/products/ProductCard";
import { ProductDetailModal } from "../../components/domain/products/ProductDetailModal";
import { Product, MediaType } from "../../types/product";
import { Input } from "../../components/ui/Input";
import { Skeleton } from "../../components/ui/Skeleton";
import { Pagination } from "../../components/ui/Pagination";
import { EmptyState } from "../../components/ui/EmptyState";
import { useDispatch } from "react-redux";
import { setCartModalMessage } from "../../store/slices/uiSlice";
import { useOwnershipMap } from "../../hooks/useOwnershipMap";
import { findEnglishLanguageId } from "../../utils/language";
import toast from "react-hot-toast";

const MEDIA_TYPE_OPTIONS: { value: MediaType; label: string }[] = [
  { value: "BOOK", label: "Books" },
  { value: "AUDIOBOOK", label: "Audiobooks" },
  { value: "VIDEO_COURSE", label: "Video Courses" },
  { value: "PODCAST", label: "Podcasts" },
];

export const ProductsPage: React.FC = () => {
  const dispatch = useDispatch();
  const { user } = useAuth();
  const [selectedGenreId, setSelectedGenreId] = useState<number | undefined>(undefined);
  const [selectedLanguageId, setSelectedLanguageId] = useState<number | undefined>(undefined);
  const [isRentableOnly, setIsRentableOnly] = useState<boolean | undefined>(undefined);
  const [selectedMediaType, setSelectedMediaType] = useState<MediaType | undefined>(undefined);
  const [searchText, setSearchText] = useState<string>("");
  const [page, setPage] = useState<number>(0);
  const [selectedProduct, setSelectedProduct] = useState<Product | null>(null);

  const { data: genres } = useFetch(getAllGenres, []);
  const { data: languages } = useFetch(getAllLanguages, []);
  // The language dropdown was removed from the Navbar (bilingual display is no
  // longer a user-chosen preference) - but English titles should still show
  // wherever an English ProductTranslation exists, so every browse request
  // now always asks for the English overlay rather than a user-selected one.
  const englishLanguageId = findEnglishLanguageId(languages);

  const { data: activeSub } = useFetch(
    () => (user?.userId ? getActiveSubscription(user.userId) : Promise.resolve(null)),
    [user?.userId]
  );

  // Mirrors AcquisitionEligibilityServiceImpl's ownership rules client-side so
  // Buy/Rent/Borrow can be disabled with an explanation instead of only
  // failing after the backend rejects the request - see useOwnershipMap.
  const { getAvailability, refetch: refetchOwnership } = useOwnershipMap();

  const { data: pageData, isLoading, refetch } = useFetch(
    () =>
      browseProducts({
        genreId: selectedGenreId,
        languageId: selectedLanguageId,
        isRentable: isRentableOnly,
        mediaType: selectedMediaType,
        keyword: searchText.trim() || undefined,
        displayLanguageId: englishLanguageId,
        page,
        size: 12,
      }),
    [selectedGenreId, selectedLanguageId, isRentableOnly, selectedMediaType, searchText, page, englishLanguageId]
  );

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setPage(0);
    refetch();
  };

  const handleAddToCart = async (product: Product) => {
    if (!user?.userId) {
      toast.error("Please login to add items to your cart");
      return;
    }

    try {
      await addToCartAPI(user.userId, product.productId, "PURCHASE", null);
      toast.success(`"${product.title}" added to cart!`);
    } catch (err: any) {
      const msg = err.message || "Failed to add to cart";
      if (msg.toLowerCase().includes("already present")) {
        dispatch(setCartModalMessage("Book is already present in your cart"));
      } else {
        // Covers the case where the ownership map is briefly stale (e.g. a
        // purchase completed in another tab) and the backend's authoritative
        // AcquisitionEligibilityServiceImpl check is what actually caught it.
        toast.error(msg);
      }
    }
  };

  const handleRentToCart = async (product: Product) => {
    if (!user?.userId) {
      toast.error("Please login to rent books");
      return;
    }

    try {
      const rentDays = product.minRentDays || 7;
      await addToCartAPI(user.userId, product.productId, "RENT", rentDays);
      toast.success(`Rental of "${product.title}" (${rentDays} days) added to cart!`);
    } catch (err: any) {
      const msg = err.message || "Failed to add rental to cart";
      if (msg.toLowerCase().includes("already present")) {
        dispatch(setCartModalMessage("Rental item is already present in your cart"));
      } else {
        toast.error(msg);
      }
    }
  };

  const handleBorrow = async (product: Product) => {
    if (!user?.userId) {
      toast.error("Please login to borrow books");
      return;
    }

    try {
      await borrowProduct(user.userId, product.productId, 14);
      toast.success(`Successfully borrowed "${product.title}"! Added to My Subscriptions & Shelf.`);
      // Borrowing (unlike add-to-cart) takes effect immediately, so the
      // ownership map used to disable Buy/Rent/Borrow needs a fresh read.
      refetchOwnership();
    } catch (err: any) {
      const msg = err.message || "Failed to borrow book";
      toast.error(msg);
    }
  };

  const handleResetFilters = () => {
    setSelectedGenreId(undefined);
    setSelectedLanguageId(undefined);
    setIsRentableOnly(undefined);
    setSelectedMediaType(undefined);
    setSearchText("");
    setPage(0);
  };

  return (
    <div className="min-h-screen bg-[#FDFCFB] dark:bg-slate-950 py-10">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-8">
          <div>
            <h1 className="font-serif text-3xl font-bold text-slate-900 dark:text-white flex items-center gap-3">
              Explore Books & Media
              {activeSub && (
                <span className="text-xs font-sans px-3 py-1 bg-amber-500/10 text-amber-600 dark:text-amber-400 border border-amber-500/20 rounded-full flex items-center gap-1 font-bold">
                  <Library size={14} /> Pass Active: {activeSub.packageName}
                </span>
              )}
            </h1>
            <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">
              Explore our complete digital catalog across languages, genres, and rentable titles
            </p>
          </div>

          <form onSubmit={handleSearchSubmit} className="flex gap-2 w-full md:w-96">
            <Input
              placeholder="Search by title or author..."
              value={searchText}
              onChange={(e) => {
                setSearchText(e.target.value);
                setPage(0);
              }}
              leftIcon={<Search size={18} />}
            />
          </form>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-4 gap-8">
          {/* SIDEBAR FILTERS */}
          <aside className="lg:col-span-1 bg-white dark:bg-slate-900 border border-slate-100 dark:border-slate-800 rounded-3xl p-6 h-fit shadow-sm space-y-6">
            <div className="flex items-center justify-between border-b border-slate-100 dark:border-slate-800 pb-3">
              <div className="flex items-center gap-2 font-serif font-bold text-slate-900 dark:text-white">
                <Filter size={18} className="text-amber-500" />
                <span>Filters</span>
              </div>
              <button
                onClick={handleResetFilters}
                className="text-xs text-amber-500 font-semibold hover:underline flex items-center gap-1"
              >
                <RefreshCw size={12} /> Reset
              </button>
            </div>

            {/* RENTABLE FILTER BUTTON */}
            <div className="space-y-2">
              <h4 className="text-xs font-bold uppercase tracking-wider text-slate-400">Availability</h4>
              <button
                onClick={() => {
                  setIsRentableOnly(isRentableOnly ? undefined : true);
                  setPage(0);
                }}
                className={`w-full text-left px-3 py-2.5 rounded-xl text-xs font-semibold flex items-center justify-between transition-colors ${
                  isRentableOnly
                    ? "bg-amber-500 text-slate-950 font-bold"
                    : "text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800 border border-slate-200 dark:border-slate-800"
                }`}
              >
                <span className="flex items-center gap-2">
                  <Zap size={14} className={isRentableOnly ? "fill-slate-950" : "text-amber-500"} />
                  Rentable Books Only
                </span>
                {isRentableOnly && <span className="text-[10px] font-bold uppercase">Active</span>}
              </button>
            </div>

            {/* MEDIA TYPE */}
            <div className="space-y-2">
              <h4 className="text-xs font-bold uppercase tracking-wider text-slate-400">Media Type</h4>
              <div className="space-y-1">
                <button
                  onClick={() => { setSelectedMediaType(undefined); setPage(0); }}
                  className={`w-full text-left px-3 py-2 rounded-xl text-xs font-semibold transition-colors ${
                    selectedMediaType === undefined
                      ? "bg-amber-500 text-slate-950"
                      : "text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800"
                  }`}
                >
                  All Types
                </button>
                {MEDIA_TYPE_OPTIONS.map((opt) => (
                  <button
                    key={opt.value}
                    onClick={() => { setSelectedMediaType(opt.value); setPage(0); }}
                    className={`w-full text-left px-3 py-2 rounded-xl text-xs font-semibold transition-colors ${
                      selectedMediaType === opt.value
                        ? "bg-amber-500 text-slate-950"
                        : "text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800"
                    }`}
                  >
                    {opt.label}
                  </button>
                ))}
              </div>
            </div>

            {/* GENRES */}
            <div className="space-y-2">
              <h4 className="text-xs font-bold uppercase tracking-wider text-slate-400">Genres</h4>
              <div className="space-y-1 max-h-48 overflow-y-auto pr-1">
                <button
                  onClick={() => { setSelectedGenreId(undefined); setPage(0); }}
                  className={`w-full text-left px-3 py-2 rounded-xl text-xs font-semibold transition-colors ${
                    selectedGenreId === undefined
                      ? "bg-amber-500 text-slate-950"
                      : "text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800"
                  }`}
                >
                  All Genres
                </button>
                {genres?.map((g) => (
                  <button
                    key={g.genreId}
                    onClick={() => { setSelectedGenreId(g.genreId); setPage(0); }}
                    className={`w-full text-left px-3 py-2 rounded-xl text-xs font-semibold transition-colors ${
                      selectedGenreId === g.genreId
                        ? "bg-amber-500 text-slate-950"
                        : "text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800"
                    }`}
                  >
                    {g.genreName}
                  </button>
                ))}
              </div>
            </div>

            {/* LANGUAGES */}
            <div className="space-y-2">
              <h4 className="text-xs font-bold uppercase tracking-wider text-slate-400">Languages</h4>
              <div className="space-y-1">
                <button
                  onClick={() => { setSelectedLanguageId(undefined); setPage(0); }}
                  className={`w-full text-left px-3 py-2 rounded-xl text-xs font-semibold transition-colors ${
                    selectedLanguageId === undefined
                      ? "bg-amber-500 text-slate-950"
                      : "text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800"
                  }`}
                >
                  All Languages
                </button>
                {languages?.map((lang) => (
                  <button
                    key={lang.languageId}
                    onClick={() => { setSelectedLanguageId(lang.languageId); setPage(0); }}
                    className={`w-full text-left px-3 py-2 rounded-xl text-xs font-semibold transition-colors ${
                      selectedLanguageId === lang.languageId
                        ? "bg-amber-500 text-slate-950"
                        : "text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800"
                    }`}
                  >
                    {lang.languageName}
                  </button>
                ))}
              </div>
            </div>
          </aside>

          {/* MAIN PRODUCT GRID */}
          <main className="lg:col-span-3 space-y-6">
            {isLoading ? (
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
                {[1, 2, 3, 4, 5, 6].map((n) => (
                  <Skeleton key={n} className="h-96 w-full rounded-3xl" />
                ))}
              </div>
            ) : !pageData || pageData.content.length === 0 ? (
              <EmptyState
                title="No Books Found"
                description="We couldn't find any titles matching your selected filters or search text."
                action={
                  <button
                    onClick={handleResetFilters}
                    className="px-5 py-2.5 bg-amber-500 text-slate-950 font-bold text-sm rounded-xl"
                  >
                    Clear All Filters
                  </button>
                }
              />
            ) : (
              <>
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
                  {pageData.content.map((product) => (
                    <ProductCard
                      key={product.productId}
                      product={product}
                      onAddToCart={handleAddToCart}
                      onRentToCart={handleRentToCart}
                      onBorrow={handleBorrow}
                      onSelect={(p) => setSelectedProduct(p)}
                      hasActiveSubscription={!!activeSub}
                      availability={getAvailability(product.productId)}
                    />
                  ))}
                </div>

                <Pagination
                  currentPage={page}
                  totalPages={pageData.totalPages}
                  totalElements={pageData.totalElements}
                  onPageChange={(newPage) => setPage(newPage)}
                />
              </>
            )}
          </main>
        </div>
      </div>

      <ProductDetailModal
        product={selectedProduct}
        onClose={() => setSelectedProduct(null)}
        onAddToCart={handleAddToCart}
        onRentToCart={handleRentToCart}
        onBorrow={handleBorrow}
        hasActiveSubscription={!!activeSub}
        availability={selectedProduct ? getAvailability(selectedProduct.productId) : undefined}
      />
    </div>
  );
};
