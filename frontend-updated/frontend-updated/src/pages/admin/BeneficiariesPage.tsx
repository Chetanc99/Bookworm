import React, { useState } from "react";
import { PlusCircle, ShieldAlert } from "lucide-react";
import { useFetch } from "../../hooks/useFetch";
import {
  getAllBeneficiaries,
  createBeneficiary,
  deactivateBeneficiary,
  activateBeneficiary,
} from "../../services/beneficiary.service";
import { Table, Column } from "../../components/ui/Table";
import { Button } from "../../components/ui/Button";
import { Modal } from "../../components/ui/Modal";
import { Input } from "../../components/ui/Input";
import { Badge } from "../../components/ui/Badge";
import { BeneficiaryResponse } from "../../types/beneficiary";
import toast from "react-hot-toast";

export const BeneficiariesPage: React.FC = () => {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [isSaving, setIsSaving] = useState(false);

  const { data: beneficiaries, isLoading, refetch } = useFetch(
    () => getAllBeneficiaries(false),
    []
  );

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name) return;
    setIsSaving(true);
    try {
      await createBeneficiary({ name, description });
      toast.success("Beneficiary created successfully!");
      setIsModalOpen(false);
      setName("");
      setDescription("");
      refetch();
    } catch (err: any) {
      toast.error(err.message || "Failed to create beneficiary");
    } finally {
      setIsSaving(false);
    }
  };

  const handleDeactivate = async (beneficiaryId: number) => {
    try {
      await deactivateBeneficiary(beneficiaryId);
      toast.success("Beneficiary deactivated");
      refetch();
    } catch (err: any) {
      toast.error("Failed to deactivate beneficiary");
    }
  };

  // Inactive beneficiaries stay fully visible/queryable (activeOnly=false above) and
  // this only ever flips isActive back to true - existing ProductBeneficiary/
  // RoyaltyLedger rows from before deactivation are untouched either way, and once
  // reactivated the beneficiary is assignable to new products again.
  const handleActivate = async (beneficiary: BeneficiaryResponse) => {
    try {
      await activateBeneficiary(beneficiary.beneficiaryId, {
        name: beneficiary.name,
        description: beneficiary.description,
        beneficiaryTypeId: beneficiary.beneficiaryTypeId,
      });
      toast.success("Beneficiary activated");
      refetch();
    } catch (err: any) {
      toast.error("Failed to activate beneficiary");
    }
  };

  const columns: Column<BeneficiaryResponse>[] = [
    {
      key: "beneficiaryId",
      header: "ID",
      render: (b) => <span className="font-mono text-xs text-slate-300">#{b.beneficiaryId}</span>,
    },
    {
      key: "name",
      header: "Name",
      render: (b) => <span className="font-bold text-white text-sm">{b.name}</span>,
    },
    {
      key: "description",
      header: "Description",
      render: (b) => <span className="text-xs text-slate-300">{b.description || "-"}</span>,
    },
    {
      key: "isActive",
      header: "Status",
      render: (b) => (
        <Badge variant={b.isActive ? "success" : "danger"}>
          {b.isActive ? "ACTIVE" : "INACTIVE"}
        </Badge>
      ),
    },
    {
      key: "actions",
      header: "Actions",
      render: (b) =>
        b.isActive ? (
          <Button
            variant="danger"
            size="sm"
            onClick={() => handleDeactivate(b.beneficiaryId)}
          >
            Deactivate
          </Button>
        ) : (
          <Button
            variant="gold"
            size="sm"
            onClick={() => handleActivate(b)}
          >
            Activate
          </Button>
        ),
    },
  ];

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-serif font-bold text-white">Beneficiaries</h1>
          <p className="text-sm text-slate-400 mt-1">Manage copyright royalty recipients & authors</p>
        </div>

        <Button
          variant="gold"
          size="md"
          onClick={() => setIsModalOpen(true)}
          leftIcon={<PlusCircle size={18} />}
        >
          Add Beneficiary
        </Button>
      </div>

      <Table
        columns={columns}
        data={beneficiaries || []}
        keyExtractor={(b) => b.beneficiaryId}
        isLoading={isLoading}
      />

      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title="Create New Beneficiary"
      >
        <form onSubmit={handleCreate} className="space-y-4">
          <Input
            label="Beneficiary Name"
            placeholder="e.g. Oxford Publishing Ltd."
            value={name}
            onChange={(e) => setName(e.target.value)}
            required
          />

          <Input
            label="Description"
            placeholder="e.g. Primary copyright holder for engineering textbooks"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
          />

          <div className="pt-4 flex justify-end gap-3">
            <Button variant="outline" type="button" onClick={() => setIsModalOpen(false)}>
              Cancel
            </Button>
            <Button variant="gold" type="submit" isLoading={isSaving}>
              Save Beneficiary
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
