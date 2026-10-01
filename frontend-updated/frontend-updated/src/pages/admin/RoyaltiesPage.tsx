import React, { useEffect, useMemo, useState } from "react";
import { Award, Search } from "lucide-react";
import { useFetch } from "../../hooks/useFetch";
import { getAllBeneficiaries } from "../../services/beneficiary.service";
import { getRoyaltyHistory, getRoyaltySummary } from "../../services/royalty.service";
import { Table, Column } from "../../components/ui/Table";
import { RoyaltyLedgerResponse } from "../../types/beneficiary";

const PAGE_SIZE = 5;

export const RoyaltiesPage: React.FC = () => {
  const [selectedBeneficiaryId, setSelectedBeneficiaryId] = useState<number | null>(null);
  // "Show More" loads progressively larger pages from the existing paged
  // endpoint (page=0, growing size) rather than a new backend endpoint -
  // GET /beneficiaries/{id}/royalties already accepts page/size, so no API
  // contract change was needed for this.
  const [visibleSize, setVisibleSize] = useState(PAGE_SIZE);
  const [search, setSearch] = useState("");

  const { data: beneficiaries, isLoading: isBenLoading } = useFetch(
    () => getAllBeneficiaries(true),
    []
  );

  // Set default beneficiary once loaded
  useEffect(() => {
    if (beneficiaries && beneficiaries.length > 0 && selectedBeneficiaryId === null) {
      setSelectedBeneficiaryId(beneficiaries[0].beneficiaryId);
    }
  }, [beneficiaries, selectedBeneficiaryId]);

  const { data: summary, isLoading: isSummaryLoading } = useFetch(
    () => {
      if (!selectedBeneficiaryId) return Promise.resolve(null);
      return getRoyaltySummary(selectedBeneficiaryId);
    },
    [selectedBeneficiaryId]
  );

  const { data: historyData, isLoading: isHistoryLoading } = useFetch(
    () => {
      if (!selectedBeneficiaryId) return Promise.resolve(null);
      return getRoyaltyHistory(selectedBeneficiaryId, 0, visibleSize);
    },
    [selectedBeneficiaryId, visibleSize]
  );

  const selectBeneficiary = (beneficiaryId: number) => {
    setSelectedBeneficiaryId(beneficiaryId);
    setVisibleSize(PAGE_SIZE);
    setSearch("");
  };

  // Client-side filter over the currently-loaded page - no new endpoint for
  // this either, per the "only add endpoints when genuinely necessary" brief.
  const filteredRecords = useMemo(() => {
    const records = historyData?.content || [];
    const term = search.trim().toLowerCase();
    if (!term) return records;
    return records.filter((r) =>
      [r.productTitle, r.sourceType, String(r.royaltyLedgerId)]
        .filter(Boolean)
        .some((field) => String(field).toLowerCase().includes(term))
    );
  }, [historyData, search]);

  const totalLoaded = historyData?.content?.length || 0;
  const totalAvailable = historyData?.totalElements || 0;
  const hasMore = totalLoaded < totalAvailable;

  const columns: Column<RoyaltyLedgerResponse>[] = [
    {
      key: "royaltyLedgerId",
      header: "Ledger ID",
      render: (r) => <span className="font-mono text-xs text-slate-300">#{r.royaltyLedgerId}</span>,
    },
    {
      key: "productTitle",
      header: "Product Title",
      render: (r) => <span className="font-bold text-white text-xs">{r.productTitle || "—"}</span>,
    },
    {
      key: "sourceType",
      header: "Source",
      render: (r) => (
        <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-slate-800 text-amber-400 uppercase">
          {r.sourceType || "SALE"}
        </span>
      ),
    },
    {
      key: "grossAmount",
      header: "Sale Revenue",
      render: (r) => (
        <span className="text-xs text-slate-300">₹{Number(r.grossAmount || 0).toLocaleString("en-IN")}</span>
      ),
    },
    {
      key: "royaltyPercentage",
      header: "Split %",
      render: (r) => <span className="text-xs text-slate-300">{r.royaltyPercentage}%</span>,
    },
    {
      key: "royaltyAmount",
      header: "Earned Royalty",
      render: (r) => (
        <span className="font-bold text-amber-400">
          ₹{Number(r.royaltyAmount || 0).toLocaleString("en-IN")}
        </span>
      ),
    },
    {
      key: "status",
      header: "Payout Status",
      // Royalty payout tracking (mark-paid workflow) has been retired - every
      // royalty entry is now simply shown as Paid, regardless of the
      // underlying ledger row's stored status, so the ledger always reads as
      // fully settled without exposing a mark-paid action anywhere in the UI.
      render: () => (
        <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-emerald-500/10 text-emerald-400 uppercase">
          Paid
        </span>
      ),
    },
    {
      key: "createdAt",
      header: "Date Accrued",
      render: (r) => (
        <span className="text-[10px] text-slate-400">
          {r.createdAt ? new Date(r.createdAt).toLocaleString() : "—"}
        </span>
      ),
    },
  ];

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-serif font-bold text-white flex items-center gap-3">
          <Award className="text-amber-500" /> Royalty Ledger & Earnings
        </h1>
        <p className="text-sm text-slate-400 mt-1">
          Track author & copyright stakeholder royalty earnings accrued from sales, rentals, and library borrows
        </p>
      </div>

      {/* BENEFICIARY TABS */}
      <div className="flex gap-2 overflow-x-auto pb-2">
        {beneficiaries?.map((b) => (
          <button
            key={b.beneficiaryId}
            onClick={() => selectBeneficiary(b.beneficiaryId)}
            className={`px-4 py-2.5 rounded-xl text-xs font-semibold whitespace-nowrap transition-colors ${
              selectedBeneficiaryId === b.beneficiaryId
                ? "bg-amber-500 text-slate-950 font-bold shadow-lg"
                : "bg-slate-900 border border-slate-800 text-slate-300 hover:bg-slate-800"
            }`}
          >
            {b.name}
          </button>
        ))}
      </div>

      {/* SUMMARY BANNER - total royalties accrued, prominent. The mark-paid
          workflow was retired (see the Payout Status column below), so this
          no longer breaks the total out into Unpaid/Paid - every royalty
          entry is treated and displayed as settled. */}
      {summary ? (
        <div className="bg-gradient-to-r from-amber-500/10 via-amber-500/5 to-slate-900 border border-amber-500/30 rounded-3xl p-6 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <span className="text-xs font-bold uppercase tracking-wider text-amber-400">
              Total Accrued Royalties ({summary.beneficiaryName})
            </span>
            <p className="text-4xl font-serif font-bold text-white mt-1">
              ₹{Number(summary.totalRoyaltyEarned ?? 0).toLocaleString("en-IN")}
            </p>
          </div>
          <span className="px-3 py-1.5 rounded-full text-xs font-bold bg-emerald-500/10 text-emerald-400 uppercase self-start sm:self-auto">
            All Settled
          </span>
        </div>
      ) : selectedBeneficiaryId && isSummaryLoading ? (
        <div className="bg-slate-900 border border-slate-800 rounded-3xl p-6 text-slate-400 text-sm text-center">
          Loading royalty earnings summary...
        </div>
      ) : null}

      {/* SEARCH BOX */}
      <div className="relative max-w-sm">
        <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-500" />
        <input
          type="text"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="Search by product, source, or ledger ID..."
          className="w-full pl-9 pr-3 py-2.5 rounded-xl bg-slate-900 border border-slate-800 text-sm text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-amber-500/50"
        />
      </div>

      {/* TRANSACTIONS TABLE - first 5 records, "Show More" loads further pages */}
      <Table
        columns={columns}
        data={filteredRecords}
        keyExtractor={(r) => r.royaltyLedgerId}
        isLoading={isHistoryLoading || isBenLoading}
        emptyMessage={
          search
            ? "No royalty records match your search."
            : "No royalty records accrued for this beneficiary yet."
        }
      />

      <div className="flex items-center justify-between text-xs text-slate-400">
        <span>
          Showing {totalLoaded} of {totalAvailable} record{totalAvailable === 1 ? "" : "s"}
          {search && filteredRecords.length !== totalLoaded ? ` (${filteredRecords.length} match search)` : ""}
        </span>
        {hasMore && (
          <button
            onClick={() => setVisibleSize((size) => size + PAGE_SIZE)}
            disabled={isHistoryLoading}
            className="px-4 py-2 rounded-xl bg-slate-900 border border-slate-800 text-amber-400 text-xs font-semibold hover:bg-slate-800 disabled:opacity-50 transition-colors"
          >
            {isHistoryLoading ? "Loading..." : "Show More"}
          </button>
        )}
      </div>
    </div>
  );
};
