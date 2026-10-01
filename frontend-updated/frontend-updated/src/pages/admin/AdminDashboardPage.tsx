import React from "react";
import { Link } from "react-router-dom";
import { DollarSign, ShoppingBag, Clock, Users, BookOpen, ShieldAlert, ArrowUpRight } from "lucide-react";
import { useFetch } from "../../hooks/useFetch";
import { getRevenueSummary, getAllTransactions, getAllUsers } from "../../services/admin.service";
import { Table, Column } from "../../components/ui/Table";
import { Badge } from "../../components/ui/Badge";
import { AdminTransactionResponse } from "../../types/admin";

export const AdminDashboardPage: React.FC = () => {
  const { data: revenue } = useFetch(getRevenueSummary, []);
  const { data: transactionsData, isLoading } = useFetch(() => getAllTransactions(0, 5), []);
  const { data: usersData } = useFetch(() => getAllUsers(0, 1), []);

  const stats = [
    {
      title: "Total Sales Revenue",
      value: `₹${(revenue?.totalRevenue || 0).toLocaleString("en-IN")}`,
      icon: <DollarSign size={24} className="text-amber-500" />,
    },
    {
      title: "Permanent Purchases",
      value: revenue?.purchaseCount || 0,
      icon: <ShoppingBag size={24} className="text-emerald-500" />,
    },
    {
      title: "Active Rentals",
      value: revenue?.rentCount || 0,
      icon: <Clock size={24} className="text-sky-500" />,
    },
    {
      title: "Registered Users",
      value: usersData?.totalElements || 0,
      icon: <Users size={24} className="text-purple-500" />,
    },
  ];

  const columns: Column<AdminTransactionResponse>[] = [
    {
      key: "transactionId",
      header: "Tx ID",
      render: (tx) => <span className="font-mono text-xs text-slate-300">#{tx.transactionId}</span>,
    },
    {
      key: "transactionType",
      header: "Type",
      render: (tx) => (
        <Badge variant={tx.transactionType === "PURCHASE" ? "gold" : "warning"}>
          {tx.transactionType}
        </Badge>
      ),
    },
    {
      key: "userFullName",
      header: "Customer",
      render: (tx) => (
        <div>
          <p className="font-bold text-white text-xs">{tx.userFullName}</p>
          <p className="text-[10px] text-slate-400">{tx.userEmail}</p>
        </div>
      ),
    },
    {
      key: "totalAmount",
      header: "Amount",
      render: (tx) => (
        <span className="font-bold text-amber-400">
          ₹{tx.totalAmount.toLocaleString("en-IN")}
        </span>
      ),
    },
    {
      key: "createdAt",
      header: "Timestamp",
      render: (tx) => (
        <span className="text-[10px] text-slate-400">
          {new Date(tx.createdAt).toLocaleString()}
        </span>
      ),
    },
  ];

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-3xl font-serif font-bold text-white">Dashboard Overview</h1>
        <p className="text-sm text-slate-400 mt-1">Real-time metrics, system sales, and activity</p>
      </div>

      {/* STAT CARDS */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        {stats.map((stat, i) => (
          <div key={i} className="bg-slate-900 border border-slate-800 rounded-3xl p-6 space-y-3 shadow-md">
            <div className="flex items-center justify-between">
              <span className="text-xs font-semibold uppercase text-slate-400 tracking-wider">
                {stat.title}
              </span>
              <div className="p-2 bg-slate-800 rounded-xl">{stat.icon}</div>
            </div>
            <p className="text-3xl font-serif font-bold text-white">{stat.value}</p>
          </div>
        ))}
      </div>

      {/* RECENT TRANSACTIONS TABLE */}
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <h3 className="font-serif font-bold text-xl text-white">Recent Transactions</h3>
          <Link
            to="/admin/transactions"
            className="text-xs text-amber-400 font-semibold hover:underline flex items-center gap-1"
          >
            View All <ArrowUpRight size={14} />
          </Link>
        </div>

        <Table
          columns={columns}
          data={transactionsData?.content || []}
          keyExtractor={(tx) => tx.transactionType + tx.transactionId}
          isLoading={isLoading}
          emptyMessage="No recent transactions recorded."
        />
      </div>
    </div>
  );
};
