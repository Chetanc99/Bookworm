import React from "react";
import { Download, FileText, ShoppingBag } from "lucide-react";
import { useAuth } from "../../hooks/useAuth";
import { useFetch } from "../../hooks/useFetch";
import { getOrderHistoryAPI, downloadInvoice } from "../../services/checkout.service";
import { Table, Column } from "../../components/ui/Table";
import { Badge } from "../../components/ui/Badge";
import { Button } from "../../components/ui/Button";
import { OrderHistoryRow } from "../../types/order";
import toast from "react-hot-toast";

export const OrdersPage: React.FC = () => {
  const { user } = useAuth();

  const { data: orderData, isLoading } = useFetch(
    async () => {
      const res = await getOrderHistoryAPI();
      return res.data;
    },
    [user?.userId]
  );

  const handleDownloadInvoice = async (row: OrderHistoryRow) => {
    if (row.type !== "PURCHASE" || !user?.userId) return;
    try {
      await downloadInvoice(user.userId, row.rawId);
      toast.success("Invoice downloaded!");
    } catch (err: any) {
      toast.error("Failed to download invoice PDF");
    }
  };

  const columns: Column<OrderHistoryRow>[] = [
    {
      key: "transactionId",
      header: "Transaction ID",
      render: (row) => (
        <span className="font-mono text-xs font-bold text-slate-900 dark:text-white">
          {row.transactionId}
        </span>
      ),
    },
    {
      key: "type",
      header: "Type",
      render: (row) => (
        <Badge variant={row.type === "PURCHASE" ? "gold" : "warning"}>
          {row.type}
        </Badge>
      ),
    },
    {
      key: "productName",
      header: "Product Title",
      render: (row) => (
        <span className="font-serif font-bold text-slate-900 dark:text-white">
          {row.productName}
        </span>
      ),
    },
    {
      key: "amount",
      header: "Amount",
      render: (row) => (
        <span className="font-semibold text-slate-900 dark:text-white">
          ₹{row.amount.toLocaleString("en-IN")}
        </span>
      ),
    },
    {
      key: "orderDate",
      header: "Order Date",
      render: (row) => (
        <span className="text-xs text-slate-500">
          {new Date(row.orderDate).toLocaleString()}
        </span>
      ),
    },
    {
      key: "actions",
      header: "Invoice",
      render: (row) =>
        row.type === "PURCHASE" ? (
          <Button
            variant="outline"
            size="sm"
            onClick={() => handleDownloadInvoice(row)}
            leftIcon={<Download size={14} />}
          >
            PDF
          </Button>
        ) : (
          <span className="text-xs text-slate-400">N/A</span>
        ),
    },
  ];

  return (
    <div className="min-h-screen bg-[#FDFCFB] dark:bg-slate-950 py-12">
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 space-y-8">
        <div className="border-b border-slate-100 dark:border-slate-800 pb-4">
          <h1 className="font-serif text-3xl font-bold text-slate-900 dark:text-white">
            Order & Rental History
          </h1>
          <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">
            View all past transactions, purchases, rentals, and download PDF receipts
          </p>
        </div>

        <Table
          columns={columns}
          data={orderData || []}
          keyExtractor={(row) => row.transactionId + row.productName}
          isLoading={isLoading}
          emptyMessage="No order history found."
        />
      </div>
    </div>
  );
};
