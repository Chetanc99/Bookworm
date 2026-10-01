import { useState, useEffect, useCallback } from "react";

export interface UseFetchOptions<T> {
  enabled?: boolean;
  initialData?: T;
  onSuccess?: (data: T) => void;
  onError?: (error: Error) => void;
}

export function useFetch<T>(
  fetchFn: () => Promise<T>,
  deps: any[] = [],
  options: UseFetchOptions<T> = {}
) {
  const { enabled = true, initialData, onSuccess, onError } = options;
  const [data, setData] = useState<T | undefined>(initialData);
  const [isLoading, setIsLoading] = useState<boolean>(enabled);
  const [error, setError] = useState<Error | null>(null);

  const execute = useCallback(async () => {
    if (!enabled) return;
    setIsLoading(true);
    setError(null);
    try {
      const result = await fetchFn();
      setData(result);
      if (onSuccess) onSuccess(result);
    } catch (err: any) {
      const errorObj = err instanceof Error ? err : new Error(err.message || "An error occurred");
      setError(errorObj);
      if (onError) onError(errorObj);
    } finally {
      setIsLoading(false);
    }
  }, [enabled, ...deps]);

  useEffect(() => {
    execute();
  }, [execute]);

  return { data, isLoading, error, refetch: execute, setData };
}
