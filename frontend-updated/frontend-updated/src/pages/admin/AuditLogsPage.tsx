import React, { useState } from "react";
import { FileText } from "lucide-react";
import { useFetch } from "../../hooks/useFetch";
import { getAuditLogs } from "../../services/admin.service";
import { Table, Column } from "../../components/ui/Table";
import { Pagination } from "../../components/ui/Pagination";
import { Badge } from "../../components/ui/Badge";
import { AuditLogResponse } from "../../types/admin";

export const AuditLogsPage: React.FC = () => {
  const [page, setPage] = useState(0);

  const { data: pageData, isLoading } = useFetch(
    () => getAuditLogs(page, 15),
    [page]
  );

  const columns: Column<AuditLogResponse>[] = [
    {
      key: "auditLogId",
      header: "Log ID",
      render: (l) => <span className="font-mono text-xs text-slate-300">#{l.auditLogId}</span>,
    },
    {
      key: "actorEmail",
      header: "Admin Actor",
      render: (l) => <span className="font-bold text-white text-xs">{l.actorEmail || "SYSTEM"}</span>,
    },
    {
      key: "action",
      header: "Action",
      render: (l) => <Badge variant="gold">{l.action}</Badge>,
    },
    {
      key: "entityType",
      header: "Entity Target",
      render: (l) => (
        <span className="text-xs font-mono text-amber-400">
          {l.entityType} #{l.entityId}
        </span>
      ),
    },
    {
      key: "details",
      header: "Details",
      render: (l) => <span className="text-xs text-slate-300 line-clamp-1">{l.details}</span>,
    },
    {
      key: "createdAt",
      header: "Timestamp",
      render: (l) => <span className="text-[10px] text-slate-400">{new Date(l.createdAt).toLocaleString()}</span>,
    },
  ];

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-serif font-bold text-white">System Audit Logs</h1>
        <p className="text-sm text-slate-400 mt-1">Immutable trail of admin actions and catalog updates</p>
      </div>

      <Table
        columns={columns}
        data={pageData?.content || []}
        keyExtractor={(l) => l.auditLogId}
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
