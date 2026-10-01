import React, { useState } from "react";
import { Link, useNavigate, useLocation } from "react-router-dom";
import {
  BookOpen,
  ShoppingBag,
  User as UserIcon,
  LogOut,
  ShieldAlert,
  Menu,
  X,
  Library,
  BookMarked,
  Clock,
} from "lucide-react";
import { useAuth } from "../../hooks/useAuth";
import { useCart } from "../../hooks/useCart";

export const Navbar: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { user, isLoggedIn, isAdmin, logout } = useAuth();
  const { itemCount } = useCart();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const handleLogout = () => {
    logout();
    // Home, not /login - and replace so the just-logged-out session's protected
    // page doesn't sit one "back" away from a page that will immediately bounce
    // to /login anyway (ProtectedRoute re-checks isLoggedIn from the live Redux
    // store on every render, so back navigation there is already safe either way).
    navigate("/", { replace: true });
  };

  const isActive = (path: string) => location.pathname === path;

  const navLinkClass = (path: string) =>
    `text-sm font-medium transition-colors hover:text-amber-500 ${
      isActive(path)
        ? "text-amber-500 font-bold border-b-2 border-amber-500 pb-1"
        : "text-slate-700 dark:text-slate-300"
    }`;

  return (
    <header className="sticky top-0 z-40 w-full backdrop-blur-md bg-white/90 dark:bg-slate-950/90 border-b border-slate-100 dark:border-slate-800 transition-colors">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-20 flex items-center justify-between">
        {/* BRAND LOGO */}
        <Link to="/" className="flex items-center gap-2 group">
          <div className="w-10 h-10 rounded-2xl bg-slate-900 dark:bg-white flex items-center justify-center text-amber-400 dark:text-slate-950 shadow-md group-hover:scale-105 transition-transform">
            <BookOpen size={22} />
          </div>
          <span className="font-serif font-bold text-2xl tracking-wider text-slate-900 dark:text-white">
            BOOKWORM<span className="text-amber-500">.</span>
          </span>
        </Link>

        {/* DESKTOP NAV LINKS */}
        <nav className="hidden md:flex items-center gap-8">
          <Link to="/" className={navLinkClass("/")}>
            Home
          </Link>
          <Link to="/products" className={navLinkClass("/products")}>
            Browse Books
          </Link>
          <Link to="/library" className={navLinkClass("/library")}>
            Library Packages
          </Link>
          {isLoggedIn && (
            <>
              <Link to="/my-library" className={navLinkClass("/my-library")}>
                My Subscription
              </Link>
              <Link to="/shelf" className={navLinkClass("/shelf")}>
                My Shelf
              </Link>
              <Link to="/orders" className={navLinkClass("/orders")}>
                Order History
              </Link>
            </>
          )}
          {isAdmin && (
            <Link
              to="/admin/dashboard"
              className="flex items-center gap-1 text-xs font-bold text-amber-600 bg-amber-50 dark:bg-amber-950/40 px-3 py-1.5 rounded-full border border-amber-200 dark:border-amber-800 hover:bg-amber-100 transition-colors"
            >
              <ShieldAlert size={14} />
              Admin CMS
            </Link>
          )}
        </nav>

        {/* RIGHT ACTIONS */}
        <div className="hidden md:flex items-center gap-4">
          <Link
            to="/cart"
            className="relative p-2.5 rounded-xl text-slate-700 dark:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
          >
            <ShoppingBag size={22} />
            {itemCount > 0 && (
              <span className="absolute -top-1 -right-1 w-5 h-5 bg-amber-500 text-slate-950 font-bold text-xs rounded-full flex items-center justify-center shadow-md animate-bounce">
                {itemCount}
              </span>
            )}
          </Link>

          {isLoggedIn ? (
            <div className="flex items-center gap-3 pl-3 border-l border-slate-200 dark:border-slate-800">
              <div className="flex flex-col text-right">
                <span className="text-sm font-bold text-slate-800 dark:text-white line-clamp-1">
                  {user?.fullName}
                </span>
                <span className="text-xs text-slate-400 font-medium">
                  {user?.role}
                </span>
              </div>
              <button
                onClick={handleLogout}
                title="Sign out"
                className="p-2 rounded-xl text-rose-500 hover:bg-rose-50 dark:hover:bg-rose-950/30 transition-colors cursor-pointer"
              >
                <LogOut size={20} />
              </button>
            </div>
          ) : (
            <div className="flex items-center gap-2">
              <Link
                to="/login"
                className="px-4 py-2 text-sm font-semibold text-slate-700 dark:text-slate-200 hover:text-amber-500 transition-colors"
              >
                Sign In
              </Link>
              <Link
                to="/register"
                className="px-5 py-2.5 text-sm font-semibold bg-amber-500 hover:bg-amber-600 text-slate-950 rounded-xl shadow-md transition-all hover:shadow-amber-500/20"
              >
                Get Started
              </Link>
            </div>
          )}
        </div>

        {/* MOBILE MENU TOGGLE */}
        <button
          onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
          className="md:hidden p-2 rounded-xl text-slate-700 dark:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-800"
        >
          {mobileMenuOpen ? <X size={24} /> : <Menu size={24} />}
        </button>
      </div>

      {/* MOBILE MENU DROPDOWN */}
      {mobileMenuOpen && (
        <div className="md:hidden bg-white dark:bg-slate-900 border-b border-slate-100 dark:border-slate-800 px-4 pt-2 pb-6 space-y-3">
          <Link
            to="/"
            onClick={() => setMobileMenuOpen(false)}
            className="block py-2 text-sm font-medium text-slate-800 dark:text-white"
          >
            Home
          </Link>
          <Link
            to="/products"
            onClick={() => setMobileMenuOpen(false)}
            className="block py-2 text-sm font-medium text-slate-800 dark:text-white"
          >
            Browse Books
          </Link>
          <Link
            to="/library"
            onClick={() => setMobileMenuOpen(false)}
            className="block py-2 text-sm font-medium text-slate-800 dark:text-white"
          >
            Library Packages
          </Link>
          {isLoggedIn && (
            <>
              <Link
                to="/my-library"
                onClick={() => setMobileMenuOpen(false)}
                className="block py-2 text-sm font-medium text-slate-800 dark:text-white"
              >
                My Subscription
              </Link>
              <Link
                to="/shelf"
                onClick={() => setMobileMenuOpen(false)}
                className="block py-2 text-sm font-medium text-slate-800 dark:text-white"
              >
                My Shelf
              </Link>
              <Link
                to="/orders"
                onClick={() => setMobileMenuOpen(false)}
                className="block py-2 text-sm font-medium text-slate-800 dark:text-white"
              >
                Order History
              </Link>
            </>
          )}
          {isAdmin && (
            <Link
              to="/admin/dashboard"
              onClick={() => setMobileMenuOpen(false)}
              className="block py-2 text-sm font-bold text-amber-600"
            >
              Admin CMS Dashboard
            </Link>
          )}
          <div className="pt-4 border-t border-slate-100 dark:border-slate-800 flex items-center justify-between">
            <Link
              to="/cart"
              onClick={() => setMobileMenuOpen(false)}
              className="flex items-center gap-2 font-semibold text-slate-800 dark:text-white"
            >
              <ShoppingBag size={20} />
              <span>Cart ({itemCount})</span>
            </Link>
            {isLoggedIn ? (
              <button
                onClick={() => {
                  setMobileMenuOpen(false);
                  handleLogout();
                }}
                className="text-sm font-semibold text-rose-500"
              >
                Sign Out
              </button>
            ) : (
              <Link
                to="/login"
                onClick={() => setMobileMenuOpen(false)}
                className="text-sm font-semibold text-amber-600"
              >
                Sign In
              </Link>
            )}
          </div>
        </div>
      )}
    </header>
  );
};
