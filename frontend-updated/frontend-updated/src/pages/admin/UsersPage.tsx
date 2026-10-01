import React, { useState } from "react";
import { Users, Shield, UserX, UserCheck } from "lucide-react";
import { useFetch } from "../../hooks/useFetch";
import { getAllUsers, changeUserRole, activateUser, deactivateUser } from "../../services/admin.service";
import { Table, Column } from "../../components/ui/Table";
import { Pagination } from "../../components/ui/Pagination";
import { Badge } from "../../components/ui/Badge";
import { Button } from "../../components/ui/Button";
import { UserAdminResponse } from "../../types/admin";
import toast from "react-hot-toast";

export const UsersPage: React.FC = () => {
  const [page, setPage] = useState(0);

  const { data: pageData, isLoading, refetch } = useFetch(
    () => getAllUsers(page, 10),
    [page]
  );

  const handleRoleToggle = async (user: UserAdminResponse) => {
    const nextRole = user.roleName === "ADMIN" ? "CUSTOMER" : "ADMIN";
    try {
      await changeUserRole(user.userId, nextRole);
      toast.success(`Role updated to ${nextRole} for ${user.fullName}`);
      refetch();
    } catch (err: any) {
      toast.error(err.message || "Failed to change role");
    }
  };

  const handleStatusToggle = async (user: UserAdminResponse) => {
    try {
      if (user.isActive) {
        await deactivateUser(user.userId);
        toast.success(`Deactivated user ${user.fullName}`);
      } else {
        await activateUser(user.userId);
        toast.success(`Activated user ${user.fullName}`);
      }
      refetch();
    } catch (err: any) {
      toast.error(err.message || "Failed to update user status");
    }
  };

  const columns: Column<UserAdminResponse>[] = [
    {
      key: "userId",
      header: "User ID",
      render: (u) => <span className="font-mono text-xs text-slate-300">#{u.userId}</span>,
    },
    {
      key: "fullName",
      header: "Full Name",
      render: (u) => <span className="font-bold text-white text-sm">{u.fullName}</span>,
    },
    {
      key: "email",
      header: "Email Address",
      render: (u) => <span className="text-xs text-slate-300">{u.email}</span>,
    },
    {
      key: "roleName",
      header: "Role",
      render: (u) => (
        <Badge variant={u.roleName === "ADMIN" ? "gold" : "info"}>
          {u.roleName}
        </Badge>
      ),
    },
    {
      key: "isActive",
      header: "Status",
      render: (u) => (
        <Badge variant={u.isActive ? "success" : "danger"}>
          {u.isActive ? "ACTIVE" : "INACTIVE"}
        </Badge>
      ),
    },
    {
      key: "actions",
      header: "Actions",
      render: (u) => (
        <div className="flex items-center gap-2">
          <Button
            variant="outline"
            size="sm"
            onClick={() => handleRoleToggle(u)}
            leftIcon={<Shield size={14} />}
          >
            Toggle Role
          </Button>
          <Button
            variant={u.isActive ? "danger" : "outline"}
            size="sm"
            onClick={() => handleStatusToggle(u)}
          >
            {u.isActive ? "Deactivate" : "Activate"}
          </Button>
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-serif font-bold text-white">User Management</h1>
        <p className="text-sm text-slate-400 mt-1">Manage system accounts, permissions, and roles</p>
      </div>

      <Table
        columns={columns}
        data={pageData?.content || []}
        keyExtractor={(u) => u.userId}
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
