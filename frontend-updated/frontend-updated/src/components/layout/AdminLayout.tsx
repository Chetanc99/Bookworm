import React from "react";
import { Link, useNavigate, useLocation } from "react-router-dom";
import {
  LayoutDashboard,
  BookOpen,
  PlusCircle,
  Users,
  DollarSign,
  Award,
  ShieldAlert,
  FileText,
  LogOut,
  ArrowLeft,
} from "lucide-react";
import { useAuth } from "../../hooks/useAuth";

export const AdminLayout: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const navigate = useNavigate();
  const location = useLocation();
  const { user, logout } = useAuth();

  const handleLogout = () => {
    logout();
    // Home, not /login - see Navbar.handleLogout for the same reasoning.
    navigate("/", { replace: true });
  };

  const navItems = [
    { label: "Dashboard Overview", path: "/admin/dashboard", icon: <LayoutDashboard size={18} /> },
    { label: "Manage Catalog", path: "/admin/products", icon: <BookOpen size={18} /> },
    { label: "Add New Product", path: "/admin/add-product", icon: <PlusCircle size={18} /> },
    { label: "User Management", path: "/admin/users", icon: <Users size={18} /> },
    { label: "Transactions & Sales", path: "/admin/transactions", icon: <DollarSign size={18} /> },
    { label: "Royalties Ledger", path: "/admin/royalties", icon: <Award size={18} /> },
    { label: "Beneficiaries", path: "/admin/beneficiaries", icon: <ShieldAlert size={18} /> },
    { label: "Audit Logs", path: "/admin/audit-logs", icon: <FileText size={18} /> },
  ];

  return (
    <div className="min-h-screen flex bg-slate-950 text-slate-100">
      {/* SIDEBAR */}
      <aside className="w-72 bg-slate-900 border-r border-slate-800 flex flex-col fixed inset-y-0 left-0 z-40">
        <div className="p-6 border-b border-slate-800 flex items-center justify-between">
          <Link to="/admin/dashboard" className="flex items-center gap-2">
            <div className="w-9 h-9 rounded-xl bg-amber-500 flex items-center justify-center text-slate-950 font-bold">
              <ShieldAlert size={20} />
            </div>
            <span className="font-serif font-bold text-xl text-white tracking-wider">
              ADMIN<span className="text-amber-500">.</span>CMS
            </span>
          </Link>
        </div>

        <nav className="flex-1 p-4 space-y-1 overflow-y-auto">
          {navItems.map((item) => {
            const active = location.pathname === item.path;
            return (
              <Link
                key={item.path}
                to={item.path}
                className={`flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-medium transition-all ${
                  active
                    ? "bg-amber-500 text-slate-950 font-bold shadow-lg shadow-amber-500/20"
                    : "text-slate-400 hover:bg-slate-800 hover:text-white"
                }`}
              >
                {item.icon}
                <span>{item.label}</span>
              </Link>
            );
          })}
        </nav>

        <div className="p-4 border-t border-slate-800 space-y-2">
          <Link
            to="/"
            className="flex items-center gap-2 px-4 py-2.5 rounded-xl text-xs font-semibold text-slate-400 hover:bg-slate-800 hover:text-white transition-colors"
          >
            <ArrowLeft size={16} />
            Back to Customer Store
          </Link>
          <button
            onClick={handleLogout}
            className="w-full flex items-center gap-2 px-4 py-2.5 rounded-xl text-xs font-semibold text-rose-400 hover:bg-rose-950/30 transition-colors"
          >
            <LogOut size={16} />
            Sign Out Admin
          </button>
        </div>
      </aside>

      {/* MAIN CONTENT AREA */}
      <div className="flex-1 pl-72 flex flex-col min-h-screen">
        <header className="h-20 border-b border-slate-800 bg-slate-900/50 backdrop-blur-md px-8 flex items-center justify-between sticky top-0 z-30">
          <h2 className="text-lg font-serif font-bold text-white">
            System Administration Portal
          </h2>
          <div className="flex items-center gap-3">
            <div className="text-right">
              <p className="text-sm font-bold text-white">{user?.fullName}</p>
              <p className="text-xs text-amber-400 font-semibold">{user?.email}</p>
            </div>
            <div className="w-10 h-10 rounded-full bg-amber-500/20 border border-amber-500/40 flex items-center justify-center font-bold text-amber-400">
              {user?.fullName?.charAt(0) || "A"}
            </div>
          </div>
        </header>

        <main className="flex-1 p-8">{children}</main>
      </div>
    </div>
  );
};
