import { useState } from "react";
import toast from "react-hot-toast";

export interface UseMutationOptions<TData, TVariables> {
  onSuccess?: (data: TData, variables: TVariables) => void;
  onError?: (error: Error, variables: TVariables) => void;
  successMessage?: string | ((data: TData) => string);
  showToast?: boolean;
}

export function useMutation<TData = any, TVariables = any>(
  mutationFn: (variables: TVariables) => Promise<TData>,
  options: UseMutationOptions<TData, TVariables> = {}
) {
  const { onSuccess, onError, successMessage, showToast = true } = options;
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [error, setError] = useState<Error | null>(null);
  const [data, setData] = useState<TData | null>(null);

  const mutate = async (variables: TVariables): Promise<TData | null> => {
    setIsLoading(true);
    setError(null);
    try {
      const res = await mutationFn(variables);
      setData(res);
      if (showToast) {
        const msg = typeof successMessage === "function" ? successMessage(res) : successMessage;
        if (msg) toast.success(msg);
      }
      if (onSuccess) onSuccess(res, variables);
      return res;
    } catch (err: any) {
      const errorObj = err instanceof Error ? err : new Error(err.message || "Action failed");
      setError(errorObj);
      if (showToast) {
        toast.error(errorObj.message);
      }
      if (onError) onError(errorObj, variables);
      return null;
    } finally {
      setIsLoading(false);
    }
  };

  return { mutate, isLoading, error, data, reset: () => { setError(null); setData(null); } };
}
