import React, { useEffect, useState } from "react";
import { PlusCircle, Save, Award } from "lucide-react";
import { useFetch } from "../../../hooks/useFetch";
import { getAllSubcategories, getAllGenres, getAllLanguages } from "../../../services/product.service";
import { getAllBeneficiaries } from "../../../services/beneficiary.service";
import { uploadCoverImage, uploadContentFile } from "../../../services/upload.service";
import { Input } from "../../ui/Input";
import { Button } from "../../ui/Button";
import { FileUpload } from "../../ui/FileUpload";
import { Product, ProductRequest, MediaType } from "../../../types/product";
import { isEnglishLanguageName } from "../../../utils/language";
import toast from "react-hot-toast";

const MEDIA_TYPE_OPTIONS: { value: MediaType; label: string }[] = [
  { value: "BOOK", label: "Book (eBook)" },
  { value: "AUDIOBOOK", label: "Audiobook" },
  { value: "VIDEO_COURSE", label: "Video Course" },
  { value: "PODCAST", label: "Podcast" },
];

export interface ProductFormValues extends Partial<ProductRequest> {
  // Not part of ProductRequest - handled separately as a ProductTranslation
  // upsert by the caller (see AddProductPage/ManageProductsPage), same
  // pattern as beneficiaries/credits/stakeholders being their own endpoints.
  englishTitle?: string;
}

export interface ProductFormProps {
  mode: "create" | "edit";
  // Pre-populates every field for edit mode; omitted (or partial) for create.
  initialValues?: Partial<Product> & { englishTitle?: string };
  onSubmit: (payload: ProductRequest, englishTitle: string | null) => Promise<void>;
  isSaving?: boolean;
  submitLabel?: string;
  // Edit mode embeds this form inside a modal alongside the author/credit/
  // beneficiary/translation managers (ManageProductsPage) - those keep their
  // own existing state/handlers untouched; this component owns only the
  // base ProductRequest fields shared with Add Product, per the "reuse the
  // Add New Product form" requirement.
  footer?: React.ReactNode;
}

/**
 * Shared base-field form for both "Add New Product" and "Edit Product".
 * Replaces the old, independently-drifted edit form in ManageProductsPage -
 * same fields, same validation, same DTO mapping, so the two screens can
 * never again fall out of sync on what a product supports.
 */
export const ProductForm: React.FC<ProductFormProps> = ({
  mode,
  initialValues,
  onSubmit,
  isSaving = false,
  submitLabel,
  footer,
}) => {
  const [formState, setFormState] = useState<Partial<ProductRequest>>({
    subcategoryId: initialValues?.subcategoryId ?? 1,
    genreId: initialValues?.genreId,
    languageId: initialValues?.languageId ?? 1,
    beneficiaryId: 1,
    title: initialValues?.title ?? "",
    shortDescription: initialValues?.shortDescription ?? "",
    description: initialValues?.description ?? "",
    price: initialValues?.price ?? 299,
    pages: initialValues?.pages ?? 200,
    duration: initialValues?.duration ?? 0,
    coverImage: initialValues?.coverImage,
    filePath: initialValues?.filePath,
    isRentable: initialValues?.isRentable ?? true,
    rentRate: initialValues?.rentRate ?? 29,
    minRentDays: initialValues?.minRentDays ?? 7,
    isLibraryEligible: initialValues?.isLibraryEligible ?? true,
    isAvailable: initialValues?.isAvailable ?? true,
    fileType: initialValues?.fileType ?? "PDF",
    mediaType: initialValues?.mediaType ?? "BOOK",
    episodeCount: initialValues?.episodeCount,
  });
  // English Title is only collected as a separate field when the product's
  // own language is NOT English - see requirement #16. When language IS
  // English, the main title already IS the English title, so no duplicate
  // field/data entry is needed or shown.
  const [englishTitle, setEnglishTitle] = useState(initialValues?.englishTitle || "");

  const [isUploading, setIsUploading] = useState(false);

  const { data: subcategories } = useFetch(getAllSubcategories, []);
  const { data: genres } = useFetch(getAllGenres, []);
  const { data: languages } = useFetch(getAllLanguages, []);
  const { data: beneficiaries } = useFetch(() => getAllBeneficiaries(true), []);

  const selectedLanguageName = languages?.find((l) => l.languageId === formState.languageId)?.languageName;
  const isEnglishSelected = isEnglishLanguageName(selectedLanguageName);

  // If the admin switches language TO English after having typed an English
  // title, drop the now-redundant field rather than silently keeping stale
  // text around that the form no longer shows.
  useEffect(() => {
    if (isEnglishSelected && englishTitle) {
      setEnglishTitle("");
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isEnglishSelected]);

  // Edit mode opens the modal (and mounts this form) BEFORE the product's
  // existing translations have finished loading - ManageProductsPage fetches
  // stakeholders/credits/beneficiaries/translations asynchronously via
  // loadProductSubResources, which resolves after the initial render. Since
  // useState's initializer only runs once on mount, the English Title field
  // would otherwise stay permanently blank even after the real value arrives
  // a moment later - this syncs it in whenever a non-empty value shows up.
  useEffect(() => {
    if (initialValues?.englishTitle) {
      setEnglishTitle(initialValues.englishTitle);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [initialValues?.englishTitle]);

  const handleCoverUpload = async (file: File) => {
    setIsUploading(true);
    try {
      const url = await uploadCoverImage(file);
      setFormState((prev) => ({ ...prev, coverImage: url }));
      toast.success("Cover image uploaded!");
    } catch (err: any) {
      toast.error("Failed to upload cover image");
    } finally {
      setIsUploading(false);
    }
  };

  const handleContentUpload = async (file: File) => {
    setIsUploading(true);
    try {
      const url = await uploadContentFile(file);
      setFormState((prev) => ({ ...prev, filePath: url }));
      toast.success("Content file uploaded!");
    } catch (err: any) {
      toast.error("Failed to upload content file");
    } finally {
      setIsUploading(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!formState.title || formState.price === undefined) {
      toast.error("Please fill in title and price");
      return;
    }
    if (!formState.languageId) {
      toast.error("Please select a language");
      return;
    }
    if (!formState.subcategoryId) {
      toast.error("Please select a subcategory");
      return;
    }
    if (!isEnglishSelected && !englishTitle.trim() && mode === "create") {
      // Not a hard requirement server-side (translations are optional), but
      // strongly nudged here since this is the one chance during creation to
      // capture it inline - after this, it has to be added via the
      // Translations manager in Manage Catalog instead.
      const proceed = window.confirm(
        "No English Title was entered. The product will be saved without an English translation - continue?"
      );
      if (!proceed) return;
    }

    const payload: ProductRequest = {
      subcategoryId: formState.subcategoryId,
      genreId: formState.genreId || null,
      languageId: formState.languageId,
      beneficiaryId: formState.beneficiaryId || 1,
      title: formState.title,
      shortDescription: formState.shortDescription || "",
      description: formState.description || "",
      price: Number(formState.price),
      pages: formState.pages ? Number(formState.pages) : 0,
      duration: formState.duration ? Number(formState.duration) : 0,
      coverImage: formState.coverImage || "/Books-images/1984.jpg",
      filePath: formState.filePath || "/uploads/content/sample.pdf",
      fileType: formState.fileType || "PDF",
      isRentable: !!formState.isRentable,
      // rentRate is required whenever the title is rentable OR library-eligible
      // (library borrows bill against the same per-day rate) - see
      // ProductServiceImpl.needsRentRate/validateRentFields on the backend.
      rentRate: formState.isRentable || formState.isLibraryEligible ? Number(formState.rentRate || 20) : null,
      minRentDays: formState.isRentable ? Number(formState.minRentDays || 7) : null,
      isLibraryEligible: !!formState.isLibraryEligible,
      isAvailable: formState.isAvailable !== false,
      mediaType: formState.mediaType || "BOOK",
      episodeCount: formState.episodeCount ? Number(formState.episodeCount) : null,
    };

    await onSubmit(payload, isEnglishSelected ? null : englishTitle.trim() || null);
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-6">
      <Input
        label="Book Title"
        placeholder="e.g. Clean Architecture in Practice"
        value={formState.title || ""}
        onChange={(e) => setFormState({ ...formState, title: e.target.value })}
        required
      />

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="space-y-1">
          <label className="text-xs font-semibold text-slate-400">Subcategory</label>
          <select
            value={formState.subcategoryId || 1}
            onChange={(e) => setFormState({ ...formState, subcategoryId: Number(e.target.value) })}
            className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-sm text-white focus:outline-none focus:border-amber-500"
          >
            {subcategories?.map((sub) => (
              <option key={sub.subcategoryId} value={sub.subcategoryId}>
                {sub.subcategoryName}
              </option>
            ))}
            {!subcategories?.length && <option value={1}>eBooks</option>}
          </select>
        </div>

        <div className="space-y-1">
          <label className="text-xs font-semibold text-slate-400">Genre</label>
          <select
            value={formState.genreId || ""}
            onChange={(e) => setFormState({ ...formState, genreId: e.target.value ? Number(e.target.value) : undefined })}
            className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-sm text-white focus:outline-none focus:border-amber-500"
          >
            <option value="">None / General</option>
            {genres?.map((g) => (
              <option key={g.genreId} value={g.genreId}>
                {g.genreName}
              </option>
            ))}
          </select>
        </div>

        <div className="space-y-1">
          <label className="text-xs font-semibold text-slate-400">Language</label>
          <select
            value={formState.languageId || 1}
            onChange={(e) => setFormState({ ...formState, languageId: Number(e.target.value) })}
            className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-sm text-white focus:outline-none focus:border-amber-500"
          >
            {languages?.map((lang) => (
              <option key={lang.languageId} value={lang.languageId}>
                {lang.languageName}
              </option>
            ))}
            {!languages?.length && <option value={1}>English</option>}
          </select>
        </div>
      </div>

      {/* ENGLISH TITLE - only shown when the product's own language isn't English */}
      {!isEnglishSelected && (
        <Input
          label="English Title"
          placeholder="English translation of the title (shown to users viewing English content)"
          value={englishTitle}
          onChange={(e) => setEnglishTitle(e.target.value)}
        />
      )}

      {/* MEDIA TYPE */}
      <div className="grid grid-cols-2 gap-4">
        <div className="space-y-1">
          <label className="text-xs font-semibold text-slate-400">Media Type</label>
          <select
            value={formState.mediaType || "BOOK"}
            onChange={(e) => setFormState({ ...formState, mediaType: e.target.value as MediaType })}
            className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-sm text-white focus:outline-none focus:border-amber-500"
          >
            {MEDIA_TYPE_OPTIONS.map((opt) => (
              <option key={opt.value} value={opt.value}>
                {opt.label}
              </option>
            ))}
          </select>
        </div>
        {(formState.mediaType === "AUDIOBOOK" ||
          formState.mediaType === "VIDEO_COURSE" ||
          formState.mediaType === "PODCAST") && (
          <Input
            label="Episode Count"
            type="number"
            placeholder="e.g. 12"
            value={formState.episodeCount !== undefined ? formState.episodeCount : ""}
            onChange={(e) =>
              setFormState({ ...formState, episodeCount: e.target.value ? Number(e.target.value) : undefined })
            }
          />
        )}
      </div>

      {/* AUTHOR / PUBLISHER BENEFICIARY SELECTION - create mode only; edit mode
          manages royalty splits via the dedicated Royalty Beneficiaries manager
          (multi-beneficiary, already more capable than this single-select). */}
      {mode === "create" && (
        <div className="bg-slate-950/60 p-4 rounded-2xl border border-slate-800/80 space-y-2">
          <div className="flex items-center gap-2">
            <Award className="text-amber-500" size={18} />
            <label className="text-xs font-bold uppercase tracking-wider text-amber-400">
              Author / Publisher Royalty Beneficiary (10% Royalty)
            </label>
          </div>
          <select
            value={formState.beneficiaryId || 1}
            onChange={(e) => setFormState({ ...formState, beneficiaryId: Number(e.target.value) })}
            className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2.5 text-sm text-white focus:outline-none focus:border-amber-500"
          >
            {beneficiaries?.map((ben) => (
              <option key={ben.beneficiaryId} value={ben.beneficiaryId}>
                {ben.name} ({ben.description || "Active Beneficiary"})
              </option>
            ))}
            {!beneficiaries?.length && <option value={1}>HarperCollins Authors Guild</option>}
          </select>
          <p className="text-[11px] text-slate-400 leading-tight">
            10% of every sale or rental price will automatically accrue as royalty to this beneficiary's ledger.
          </p>
        </div>
      )}

      <div className="grid grid-cols-2 gap-4">
        <Input
          label="Purchase Price (₹)"
          type="number"
          placeholder="499"
          value={formState.price !== undefined ? formState.price : ""}
          onChange={(e) => setFormState({ ...formState, price: Number(e.target.value) })}
          required
        />
        <Input
          label="Page Count"
          type="number"
          placeholder="350"
          value={formState.pages !== undefined ? formState.pages : ""}
          onChange={(e) => setFormState({ ...formState, pages: Number(e.target.value) })}
        />
      </div>

      <Input
        label="Short Description"
        placeholder="Brief summary of the book"
        value={formState.shortDescription || ""}
        onChange={(e) => setFormState({ ...formState, shortDescription: e.target.value })}
      />

      <div className="space-y-1">
        <label className="text-xs font-semibold text-slate-400">Full Description</label>
        <textarea
          value={formState.description || ""}
          onChange={(e) => setFormState({ ...formState, description: e.target.value })}
          rows={4}
          placeholder="Full product description"
          className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-sm text-white focus:outline-none focus:border-amber-500"
        />
      </div>

      {/* RENTABLE & LIBRARY OPTIONS */}
      <div className="bg-slate-950 p-4 rounded-2xl border border-slate-800 space-y-4">
        <div className="flex items-center justify-between">
          <div>
            <span className="text-sm font-bold text-white">Enable Book Rental</span>
            <p className="text-xs text-slate-400">Allow users to rent this title by the day</p>
          </div>
          <input
            type="checkbox"
            checked={!!formState.isRentable}
            onChange={(e) => setFormState({ ...formState, isRentable: e.target.checked })}
            className="w-5 h-5 accent-amber-500 rounded cursor-pointer"
          />
        </div>

        <div className="flex items-center justify-between pt-2 border-t border-slate-800">
          <div>
            <span className="text-sm font-bold text-white">Library Pass Eligible</span>
            <p className="text-xs text-slate-400">Allow active subscribers to borrow this title</p>
          </div>
          <input
            type="checkbox"
            checked={!!formState.isLibraryEligible}
            onChange={(e) => setFormState({ ...formState, isLibraryEligible: e.target.checked })}
            className="w-5 h-5 accent-amber-500 rounded cursor-pointer"
          />
        </div>

        {(formState.isRentable || formState.isLibraryEligible) && (
          <div className="grid grid-cols-2 gap-4 pt-2 border-t border-slate-800">
            <Input
              label="Rent Rate per Day (₹)"
              type="number"
              value={formState.rentRate || 25}
              onChange={(e) => setFormState({ ...formState, rentRate: Number(e.target.value) })}
            />
            {formState.isRentable && (
              <Input
                label="Min Rental Days"
                type="number"
                value={formState.minRentDays || 7}
                onChange={(e) => setFormState({ ...formState, minRentDays: Number(e.target.value) })}
              />
            )}
          </div>
        )}
      </div>

      <div className="flex items-center gap-2 text-sm text-slate-300">
        <input
          type="checkbox"
          checked={formState.isAvailable !== false}
          onChange={(e) => setFormState({ ...formState, isAvailable: e.target.checked })}
          className="w-4 h-4 accent-amber-500"
        />
        Listed / available on the storefront
      </div>

      <div className="grid grid-cols-2 gap-4">
        <FileUpload
          label="Cover Image File"
          accept="image/*"
          uploadedUrl={formState.coverImage}
          onFileSelect={handleCoverUpload}
          isUploading={isUploading}
        />
        <FileUpload
          label="PDF eBook Content File"
          accept="application/pdf"
          uploadedUrl={formState.filePath}
          onFileSelect={handleContentUpload}
          isUploading={isUploading}
        />
      </div>

      <Button
        type="submit"
        variant="gold"
        size="lg"
        className="w-full mt-4"
        isLoading={isSaving}
        leftIcon={mode === "create" ? <PlusCircle size={18} /> : <Save size={18} />}
      >
        {submitLabel || (mode === "create" ? "Publish Product To Catalog" : "Save Changes")}
      </Button>

      {footer}
    </form>
  );
};
