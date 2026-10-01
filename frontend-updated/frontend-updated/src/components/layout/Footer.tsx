import React from "react";
import { Link } from "react-router-dom";
import { BookOpen, Heart, Github, Twitter, Mail } from "lucide-react";

export const Footer: React.FC = () => {
  return (
    <footer className="bg-slate-950 text-slate-400 pt-16 pb-12 border-t border-slate-800">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-10 mb-12">
          {/* BRAND COLUMN */}
          <div className="space-y-4 md:col-span-1">
            <Link to="/" className="flex items-center gap-2">
              <div className="w-10 h-10 rounded-2xl bg-white flex items-center justify-center text-slate-950 font-bold shadow-md">
                <BookOpen size={22} />
              </div>
              <span className="font-serif font-bold text-2xl tracking-wider text-white">
                BOOKWORM<span className="text-amber-500">.</span>
              </span>
            </Link>
            <p className="text-sm text-slate-400 leading-relaxed">
              Your digital sanctuary for literature, academic papers, eBooks, and audiobooks. Buy, rent, or subscribe.
            </p>
          </div>

          {/* QUICK LINKS */}
          <div>
            <h4 className="font-serif font-bold text-white text-base mb-4 tracking-wide">
              Explore
            </h4>
            <ul className="space-y-2.5 text-sm">
              <li>
                <Link to="/products" className="hover:text-amber-400 transition-colors">
                  Catalog & Books
                </Link>
              </li>
              <li>
                <Link to="/library" className="hover:text-amber-400 transition-colors">
                  Subscription Packages
                </Link>
              </li>
              <li>
                <Link to="/cart" className="hover:text-amber-400 transition-colors">
                  Shopping Cart
                </Link>
              </li>
            </ul>
          </div>

          {/* USER ACCOUNT */}
          <div>
            <h4 className="font-serif font-bold text-white text-base mb-4 tracking-wide">
              Account
            </h4>
            <ul className="space-y-2.5 text-sm">
              <li>
                <Link to="/shelf" className="hover:text-amber-400 transition-colors">
                  My Digital Shelf
                </Link>
              </li>
              <li>
                <Link to="/my-library" className="hover:text-amber-400 transition-colors">
                  Active Subscription
                </Link>
              </li>
              <li>
                <Link to="/orders" className="hover:text-amber-400 transition-colors">
                  Order History
                </Link>
              </li>
            </ul>
          </div>

          {/* NEWSLETTER */}
          <div>
            <h4 className="font-serif font-bold text-white text-base mb-4 tracking-wide">
              Stay Connected
            </h4>
            <p className="text-sm text-slate-400 mb-3">
              Subscribe to get curated book recommendations and new releases.
            </p>
            <div className="flex gap-2">
              <input
                type="email"
                placeholder="Enter your email"
                className="bg-slate-900 border border-slate-800 rounded-xl px-3 py-2 text-sm text-white placeholder-slate-500 focus:outline-none focus:border-amber-500 w-full"
              />
              <button className="bg-amber-500 hover:bg-amber-600 text-slate-950 font-bold px-4 py-2 rounded-xl text-sm transition-colors">
                Join
              </button>
            </div>
          </div>
        </div>

        <div className="pt-8 border-t border-slate-900 flex flex-col md:flex-row items-center justify-between text-xs text-slate-500 gap-4">
          <p>© {new Date().getFullYear()} BookWorm Inc. All rights reserved.</p>
          <div className="flex items-center gap-1">
            <span>Crafted with</span>
            <Heart size={14} className="text-rose-500 fill-rose-500" />
            <span>for book lovers worldwide</span>
          </div>
        </div>
      </div>
    </footer>
  );
};
