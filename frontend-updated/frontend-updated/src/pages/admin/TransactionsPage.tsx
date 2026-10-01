import React, { useState } from "react";
import { DollarSign } from "lucide-react";
import { useFetch } from "../../hooks/useFetch";
import { getAllTransactions } from "../../services/admin.service";
import { Table, Column } from "../../components/ui/Table";
import { Pagination } from "../../components/ui/Pagination";
import { Badge } from "../../components/ui/Badge";
import { AdminTransactionResponse } from "../../types/admin";

export const TransactionsPage: React.FC = () => {
  const [page, setPage] = useState(0);

  const { data: pageData, isLoading } = useFetch(
    () => getAllTransactions(page, 12),
    [page]
  );

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
      header: "Total Amount",
      render: (tx) => (
        <span className="font-bold text-amber-400">
          ₹{tx.totalAmount.toLocaleString("en-IN")}
        </span>
      ),
    },
    {
      key: "createdAt",
      header: "Created At",
      render: (tx) => (
        <span className="text-xs text-slate-400">
          {new Date(tx.createdAt).toLocaleString()}
        </span>
      ),
    },
  ];

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-serif font-bold text-white">System Transactions</h1>
        <p className="text-sm text-slate-400 mt-1">Audit of all store purchases and rental checkouts</p>
      </div>

      <Table
        columns={columns}
        data={pageData?.content || []}
        keyExtractor={(tx) => tx.transactionType + tx.transactionId}
        isLoading={isLoading}
      />

      <Pagination
        currentPage={page}
        totalPages={pageData?.totalPages || 0}
        totalElements={pageData?.totalElements}
        onPageChange={(newPage) => setPage(newPage)}
      />
    </div>
  );
};
