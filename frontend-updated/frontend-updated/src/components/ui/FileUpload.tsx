import React, { useRef } from "react";
import { UploadCloud, CheckCircle2, Loader2 } from "lucide-react";

export interface FileUploadProps {
  label: string;
  accept?: string;
  onFileSelect: (file: File) => void;
  isUploading?: boolean;
  uploadedUrl?: string | null;
  helperText?: string;
}

export const FileUpload: React.FC<FileUploadProps> = ({
  label,
  accept,
  onFileSelect,
  isUploading = false,
  uploadedUrl,
  helperText,
}) => {
  const inputRef = useRef<HTMLInputElement>(null);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      onFileSelect(e.target.files[0]);
    }
  };

  return (
    <div className="w-full flex flex-col gap-1.5">
      <label className="text-xs font-semibold uppercase tracking-wider text-slate-600 dark:text-slate-300">
        {label}
      </label>
      <div
        onClick={() => !isUploading && inputRef.current?.click()}
        className={`border-2 border-dashed rounded-2xl p-6 flex flex-col items-center justify-center cursor-pointer transition-all duration-300 bg-slate-50/50 dark:bg-slate-900/50 hover:bg-slate-100/50 dark:hover:bg-slate-800/50 ${
          uploadedUrl
            ? "border-emerald-500/50 bg-emerald-50/30 dark:bg-emerald-950/20"
            : "border-slate-300 dark:border-slate-700 hover:border-amber-500"
        }`}
      >
        <input
          ref={inputRef}
          type="file"
          accept={accept}
          onChange={handleChange}
          className="hidden"
        />

        {isUploading ? (
          <div className="flex flex-col items-center gap-2">
            <Loader2 className="w-8 h-8 text-amber-500 animate-spin" />
            <span className="text-sm font-medium text-slate-600 dark:text-slate-300">
              Uploading file...
            </span>
          </div>
        ) : uploadedUrl ? (
          <div className="flex flex-col items-center gap-2 text-emerald-600 dark:text-emerald-400">
            <CheckCircle2 size={32} />
            <span className="text-sm font-semibold truncate max-w-xs">
              File uploaded: {uploadedUrl.split("/").pop()}
            </span>
            <span className="text-xs text-slate-400">Click to replace</span>
          </div>
        ) : (
          <div className="flex flex-col items-center gap-2 text-slate-500">
            <UploadCloud size={32} className="text-slate-400" />
            <span className="text-sm font-medium text-slate-700 dark:text-slate-200">
              Click to browse or drag file here
            </span>
            {helperText && <span className="text-xs text-slate-400">{helperText}</span>}
          </div>
        )}
      </div>
    </div>
  );
};
