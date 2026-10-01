import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { ArrowLeft } from "lucide-react";
import { createProduct } from "../../services/product.service";
import { upsertProductTranslation } from "../../services/productDetail.service";
import { getAllLanguages } from "../../services/product.service";
import { useFetch } from "../../hooks/useFetch";
import { ProductForm } from "../../components/domain/products/ProductForm";
import { findEnglishLanguageId } from "../../utils/language";
import { ProductRequest } from "../../types/product";
import toast from "react-hot-toast";

export const AddProductPage: React.FC = () => {
  const navigate = useNavigate();
  const [isSaving, setIsSaving] = useState(false);
  const { data: languages } = useFetch(getAllLanguages, []);
  const englishLanguageId = findEnglishLanguageId(languages);

  const handleSubmit = async (payload: ProductRequest, englishTitle: string | null) => {
    setIsSaving(true);
    try {
      const created = await createProduct(payload);

      // Persist the English Title (requirement #16) as a ProductTranslation
      // for the English language, same mechanism the Manage Catalog
      // Translations manager uses - see ProductTranslationService.upsert.
      if (englishTitle && englishLanguageId) {
        try {
          await upsertProductTranslation(created.productId, {
            languageId: englishLanguageId,
            title: englishTitle,
          });
        } catch (err: any) {
          // The product itself saved successfully - a failed translation
          // upsert shouldn't be reported as "failed to publish product".
          toast.error("Product saved, but the English Title could not be saved: " + (err.message || "unknown error"));
        }
      }

      toast.success("Product published with 10% royalty split configuration!");
      navigate("/admin/products");
    } catch (err: any) {
      toast.error(err.message || "Failed to publish product");
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <div className="max-w-3xl space-y-6">
      <div className="flex items-center gap-4 border-b border-slate-800 pb-4">
        <button
          onClick={() => navigate("/admin/products")}
          className="p-2 rounded-xl bg-slate-900 text-slate-400 hover:text-white"
        >
          <ArrowLeft size={20} />
        </button>
        <div>
          <h1 className="text-3xl font-serif font-bold text-white">Add New Book / Media</h1>
          <p className="text-sm text-slate-400 mt-1">Publish a new digital title & assign author/publisher royalties</p>
        </div>
      </div>

      <div className="bg-slate-900 border border-slate-800 rounded-3xl p-8">
        <ProductForm mode="create" onSubmit={handleSubmit} isSaving={isSaving} />
      </div>
    </div>
  );
};
