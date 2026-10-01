import React, { useState } from "react";
import { Search, Edit, UploadCloud, FileSpreadsheet, Plus, X } from "lucide-react";
import { useFetch } from "../../hooks/useFetch";
import { browseProducts, updateProduct, getAllLanguages } from "../../services/product.service";
import { bulkImportProducts } from "../../services/bulkImport.service";
import { getAllBeneficiaries } from "../../services/beneficiary.service";
import {
  getProductBeneficiaries,
  replaceProductBeneficiaries,
  getProductTranslations,
  upsertProductTranslation,
  removeProductTranslation,
} from "../../services/productDetail.service";
import { Table, Column } from "../../components/ui/Table";
import { Pagination } from "../../components/ui/Pagination";
import { Input } from "../../components/ui/Input";
import { Button } from "../../components/ui/Button";
import { Modal } from "../../components/ui/Modal";
import { FileUpload } from "../../components/ui/FileUpload";
import { ProductForm } from "../../components/domain/products/ProductForm";
import {
  Product,
  ProductRequest,
  ProductBeneficiaryResponse,
  ProductTranslationResponse,
} from "../../types/product";
import { resolveFileUrl, FALLBACK_BOOK_IMAGE } from "../../api/client";
import toast from "react-hot-toast";

export const ManageProductsPage: React.FC = () => {
  const [page, setPage] = useState(0);
  const [keyword, setKeyword] = useState("");
  const [editingProduct, setEditingProduct] = useState<Product | null>(null);
  const [isSaving, setIsSaving] = useState(false);
  const [isBulkImportOpen, setIsBulkImportOpen] = useState(false);
  const [isUploading, setIsUploading] = useState(false);

  // Royalty-split sub-resource for the product currently being edited -
  // its own join-table endpoint, loaded/refreshed independently of the main
  // product form (see productDetail.service.ts). Credits/Stakeholders
  // management is intentionally not exposed here (out of scope for now).
  const [productBeneficiaries, setProductBeneficiaries] = useState<ProductBeneficiaryResponse[]>([]);
  const [newBeneficiaryId, setNewBeneficiaryId] = useState<number | "">("");
  const [newBeneficiaryPct, setNewBeneficiaryPct] = useState<number | "">("");
  const [isSavingBeneficiaries, setIsSavingBeneficiaries] = useState(false);

  // Bilingual display overlays for the product currently being edited.
  const [translations, setTranslations] = useState<ProductTranslationResponse[]>([]);
  const [newTranslationLanguageId, setNewTranslationLanguageId] = useState<number | "">("");
  const [newTranslationTitle, setNewTranslationTitle] = useState("");
  const [newTranslationShortDesc, setNewTranslationShortDesc] = useState("");
  const [newTranslationDesc, setNewTranslationDesc] = useState("");
  const [isSavingTranslation, setIsSavingTranslation] = useState(false);

  const { data: pageData, isLoading, refetch } = useFetch(
    () => browseProducts({ page, size: 10, keyword: keyword || undefined }),
    [page, keyword]
  );

  const { data: languages } = useFetch(getAllLanguages, []);
  const { data: allBeneficiaries } = useFetch(() => getAllBeneficiaries(true), []);

  const loadProductSubResources = async (productId: number) => {
    // Each of these is an independent endpoint - one failing (e.g. the
    // Translations endpoint returning 404 on a backend that hasn't picked up
    // the ProductTranslation feature yet) must not prevent the other from
    // loading. Promise.all fails fast on the FIRST rejection and never
    // resolves the rest, which is what previously turned one bad endpoint
    // into "Failed to load product attribution/royalty details" for
    // everything, even though Beneficiaries was fine.
    const [benResult, trResult] = await Promise.allSettled([
      getProductBeneficiaries(productId),
      getProductTranslations(productId),
    ]);

    if (benResult.status === "fulfilled") setProductBeneficiaries(benResult.value);
    if (trResult.status === "fulfilled") setTranslations(trResult.value);

    const failedLabels: string[] = [];
    if (benResult.status === "rejected") failedLabels.push("Royalty Beneficiaries");
    if (trResult.status === "rejected") failedLabels.push("Translations");
    if (failedLabels.length > 0) {
      toast.error(`Failed to load: ${failedLabels.join(", ")}. Other sections loaded normally.`);
    }
  };

  const handleSaveTranslation = async () => {
    if (!editingProduct || !newTranslationLanguageId || !newTranslationTitle.trim()) return;
    setIsSavingTranslation(true);
    try {
      await upsertProductTranslation(editingProduct.productId, {
        languageId: Number(newTranslationLanguageId),
        title: newTranslationTitle.trim(),
        shortDescription: newTranslationShortDesc.trim() || undefined,
        description: newTranslationDesc.trim() || undefined,
      });
      toast.success("Translation saved");
      setNewTranslationLanguageId("");
      setNewTranslationTitle("");
      setNewTranslationShortDesc("");
      setNewTranslationDesc("");
      const tr = await getProductTranslations(editingProduct.productId);
      setTranslations(tr);
    } catch (err: any) {
      toast.error(err.message || "Failed to save translation");
    } finally {
      setIsSavingTranslation(false);
    }
  };

  const handleRemoveTranslation = async (languageId: number) => {
    if (!editingProduct) return;
    try {
      await removeProductTranslation(editingProduct.productId, languageId);
      setTranslations((prev) => prev.filter((t) => t.languageId !== languageId));
    } catch (err: any) {
      toast.error(err.message || "Failed to remove translation");
    }
  };

  const handleStartEdit = (product: Product) => {
    // ProductForm derives its own field state from `initialValues` (the
    // product itself) - no separate formState mirror needed here anymore.
    setEditingProduct(product);
    loadProductSubResources(product.productId);
  };

  const handleAddBeneficiaryLine = async () => {
    if (!editingProduct || !newBeneficiaryId) return;
    setIsSavingBeneficiaries(true);
    try {
      const nextLines = [
        ...productBeneficiaries.map((b) => ({ beneficiaryId: b.beneficiaryId, royaltyPercentage: b.royaltyPercentage })),
        { beneficiaryId: Number(newBeneficiaryId), royaltyPercentage: newBeneficiaryPct ? Number(newBeneficiaryPct) : undefined },
      ];
      const updated = await replaceProductBeneficiaries(editingProduct.productId, nextLines);
      setProductBeneficiaries(updated);
      setNewBeneficiaryId("");
      setNewBeneficiaryPct("");
      toast.success("Royalty split added");
    } catch (err: any) {
      toast.error(err.message || "Failed to add royalty split (check total does not exceed 100%)");
    } finally {
      setIsSavingBeneficiaries(false);
    }
  };

  const handleRemoveBeneficiaryLine = async (beneficiaryId: number) => {
    if (!editingProduct) return;
    setIsSavingBeneficiaries(true);
    try {
      const nextLines = productBeneficiaries
        .filter((b) => b.beneficiaryId !== beneficiaryId)
        .map((b) => ({ beneficiaryId: b.beneficiaryId, royaltyPercentage: b.royaltyPercentage }));
      const updated = await replaceProductBeneficiaries(editingProduct.productId, nextLines);
      setProductBeneficiaries(updated);
    } catch (err: any) {
      toast.error(err.message || "Failed to remove royalty split");
    } finally {
      setIsSavingBeneficiaries(false);
    }
  };

  const handleSaveProduct = async (payload: ProductRequest, englishTitle: string | null) => {
    if (!editingProduct) return;
    setIsSaving(true);
    try {
      await updateProduct(editingProduct.productId, payload);

      // Same English Title capture Add Product uses (requirement #16),
      // reusing the Translations manager's own upsert - keeps this a single
      // code path instead of a second, form-embedded translation write.
      const englishLanguageId = languages?.find((l) => l.languageName?.toLowerCase() === "english")?.languageId;
      if (englishTitle && englishLanguageId) {
        try {
          await upsertProductTranslation(editingProduct.productId, {
            languageId: englishLanguageId,
            title: englishTitle,
          });
        } catch (err: any) {
          toast.error("Product saved, but the English Title could not be saved: " + (err.message || "unknown error"));
        }
      }

      toast.success("Product updated successfully!");
      loadProductSubResources(editingProduct.productId);
      refetch();
    } catch (err: any) {
      toast.error(err.message || "Failed to update product");
    } finally {
      setIsSaving(false);
    }
  };

  const handleBulkImportFile = async (file: File) => {
    setIsUploading(true);
    try {
      const res = await bulkImportProducts(file);
      toast.success(`Import complete! ${res.successCount} succeeded, ${res.failureCount} failed.`);
      setIsBulkImportOpen(false);
      refetch();
    } catch (err: any) {
      toast.error(err.message || "Bulk import failed");
    } finally {
      setIsUploading(false);
    }
  };

  const columns: Column<Product>[] = [
    {
      key: "coverImage",
      header: "Cover",
      render: (p) => (
        <img
          src={resolveFileUrl(p.coverImage)}
          alt={p.title}
          className="w-10 h-14 object-cover rounded shadow-sm bg-slate-800"
          onError={(e: any) => {
            e.target.src = FALLBACK_BOOK_IMAGE;
          }}
        />
      ),
    },
    {
      key: "title",
      header: "Title",
      render: (p) => (
        <div>
          <p className="font-bold text-white text-sm">{p.title}</p>
          <p className="text-xs text-amber-400">{p.genreName || p.subcategoryName}</p>
        </div>
      ),
    },
    {
      key: "price",
      header: "Price",
      render: (p) => <span className="font-bold text-white">₹{p.price}</span>,
    },
    {
      key: "actions",
      header: "Manage",
      render: (p) => (
        <Button
          variant="outline"
          size="sm"
          onClick={() => handleStartEdit(p)}
          leftIcon={<Edit size={14} />}
        >
          Edit & Files
        </Button>
      ),
    },
  ];

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-serif font-bold text-white">Catalog CMS</h1>
          <p className="text-sm text-slate-400 mt-1">Manage books, attach cover images/PDFs, bulk import</p>
        </div>

        <div className="flex items-center gap-3">
          <Button
            variant="gold"
            size="md"
            onClick={() => setIsBulkImportOpen(true)}
            leftIcon={<FileSpreadsheet size={18} />}
          >
            Bulk Import Excel
          </Button>
        </div>
      </div>

      <div className="flex gap-4">
        <Input
          placeholder="Search catalog by title..."
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          leftIcon={<Search size={18} />}
        />
      </div>

      <Table
        columns={columns}
        data={pageData?.content || []}
        keyExtractor={(p) => p.productId}
        isLoading={isLoading}
      />

      <Pagination
        currentPage={page}
        totalPages={pageData?.totalPages || 0}
        totalElements={pageData?.totalElements}
        onPageChange={(newPage) => setPage(newPage)}
      />

      {/* EDIT MODAL */}
      <Modal
        isOpen={!!editingProduct}
        onClose={() => setEditingProduct(null)}
        title={`Edit Product #${editingProduct?.productId}`}
        maxWidth="xl"
      >
        {editingProduct && (
          <div className="space-y-4 max-h-[75vh] overflow-y-auto pr-2">
            {/* BASE PRODUCT FIELDS - the exact same ProductForm component Add
                New Product uses (same validation, same DTO mapping, same UI),
                pre-populated from the product being edited. Replaces the old
                independently-maintained edit form that had drifted out of
                sync with Add Product (missing description, full media-type
                support, etc.) - see requirement #17. */}
            <ProductForm
              mode="edit"
              initialValues={{
                ...editingProduct,
                englishTitle: translations.find((t) => t.languageName?.toLowerCase() === "english")?.title || "",
              }}
              onSubmit={handleSaveProduct}
              isSaving={isSaving}
            />

            {/* PRODUCT BENEFICIARIES / ROYALTY SPLIT */}
            <div className="border-t border-slate-800 pt-4 space-y-3">
              <h3 className="text-sm font-bold text-white">Royalty Beneficiaries</h3>
              <div className="space-y-2">
                {productBeneficiaries.length === 0 && (
                  <p className="text-xs text-slate-500">No royalty splits configured for this product.</p>
                )}
                {productBeneficiaries.map((b) => (
                  <div
                    key={b.productBeneficiaryId}
                    className="flex items-center justify-between bg-slate-800/60 rounded-lg px-3 py-2 text-xs"
                  >
                    <span className="text-slate-200">
                      <span className="font-semibold text-white">{b.beneficiaryName}</span> — {b.royaltyPercentage}%
                    </span>
                    <button
                      type="button"
                      onClick={() => handleRemoveBeneficiaryLine(b.beneficiaryId)}
                      className="text-slate-400 hover:text-rose-400"
                      disabled={isSavingBeneficiaries}
                    >
                      <X size={14} />
                    </button>
                  </div>
                ))}
              </div>
              <div className="flex gap-2">
                <select
                  value={newBeneficiaryId}
                  onChange={(e) => setNewBeneficiaryId(e.target.value ? Number(e.target.value) : "")}
                  className="flex-1 px-3 py-2 rounded-lg bg-slate-800 border border-slate-700 text-xs text-white"
                >
                  <option value="">Select beneficiary...</option>
                  {allBeneficiaries?.map((b) => (
                    <option key={b.beneficiaryId} value={b.beneficiaryId}>
                      {b.name}
                    </option>
                  ))}
                </select>
                <input
                  type="number"
                  value={newBeneficiaryPct}
                  onChange={(e) => setNewBeneficiaryPct(e.target.value ? Number(e.target.value) : "")}
                  placeholder="% (optional)"
                  className="w-28 px-3 py-2 rounded-lg bg-slate-800 border border-slate-700 text-xs text-white"
                />
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  onClick={handleAddBeneficiaryLine}
                  isLoading={isSavingBeneficiaries}
                  leftIcon={<Plus size={14} />}
                >
                  Add
                </Button>
              </div>
              <p className="text-[11px] text-slate-500">
                Total allocation across all beneficiaries cannot exceed 100%. Changing a split never alters
                already-earned royalty history (historical ledger entries are frozen at the time they were earned).
              </p>
            </div>

            {/* BILINGUAL DISPLAY TRANSLATIONS */}
            <div className="border-t border-slate-800 pt-4 space-y-3">
              <h3 className="text-sm font-bold text-white">Translations (Bilingual Display)</h3>
              <div className="space-y-2">
                {translations.length === 0 && (
                  <p className="text-xs text-slate-500">No translations added yet - the product displays in its base language only.</p>
                )}
                {translations.map((t) => (
                  <div
                    key={t.productTranslationId}
                    className="flex items-center justify-between bg-slate-800/60 rounded-lg px-3 py-2 text-xs"
                  >
                    <span className="text-slate-200">
                      <span className="font-semibold text-white">{t.languageName}</span>: {t.title}
                    </span>
                    <button
                      type="button"
                      onClick={() => handleRemoveTranslation(t.languageId)}
                      className="text-slate-400 hover:text-rose-400"
                    >
                      <X size={14} />
                    </button>
                  </div>
                ))}
              </div>
              <div className="space-y-2">
                <div className="flex gap-2">
                  <select
                    value={newTranslationLanguageId}
                    onChange={(e) => setNewTranslationLanguageId(e.target.value ? Number(e.target.value) : "")}
                    className="w-40 px-3 py-2 rounded-lg bg-slate-800 border border-slate-700 text-xs text-white"
                  >
                    <option value="">Language...</option>
                    {languages?.map((lang) => (
                      <option key={lang.languageId} value={lang.languageId}>
                        {lang.languageName}
                      </option>
                    ))}
                  </select>
                  <input
                    value={newTranslationTitle}
                    onChange={(e) => setNewTranslationTitle(e.target.value)}
                    placeholder="Translated title"
                    className="flex-1 px-3 py-2 rounded-lg bg-slate-800 border border-slate-700 text-xs text-white"
                  />
                </div>
                <input
                  value={newTranslationShortDesc}
                  onChange={(e) => setNewTranslationShortDesc(e.target.value)}
                  placeholder="Translated short description (optional)"
                  className="w-full px-3 py-2 rounded-lg bg-slate-800 border border-slate-700 text-xs text-white"
                />
                <textarea
                  value={newTranslationDesc}
                  onChange={(e) => setNewTranslationDesc(e.target.value)}
                  placeholder="Translated full description (optional)"
                  rows={2}
                  className="w-full px-3 py-2 rounded-lg bg-slate-800 border border-slate-700 text-xs text-white"
                />
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  onClick={handleSaveTranslation}
                  isLoading={isSavingTranslation}
                  leftIcon={<Plus size={14} />}
                >
                  Save Translation
                </Button>
              </div>
              <p className="text-[11px] text-slate-500">
                Adding a translation for a language a user selects as their display preference will show this
                title/description instead of the base text - the base product record (pricing, availability,
                royalties) is unaffected.
              </p>
            </div>

            <div className="pt-4 flex justify-end">
              <Button variant="outline" type="button" onClick={() => setEditingProduct(null)}>
                Close
              </Button>
            </div>
          </div>
        )}
      </Modal>

      {/* BULK IMPORT MODAL */}
      <Modal
        isOpen={isBulkImportOpen}
        onClose={() => setIsBulkImportOpen(false)}
        title="Bulk Import Excel / CSV Catalog"
      >
        <div className="space-y-4">
          <p className="text-xs text-slate-400">
            Upload your master product spreadsheet. Automatically creates products & library packages.
          </p>
          <FileUpload
            label="Select Spreadsheet File (.xlsx, .csv)"
            accept=".xlsx,.csv"
            onFileSelect={handleBulkImportFile}
            isUploading={isUploading}
          />
        </div>
      </Modal>
    </div>
  );
};
