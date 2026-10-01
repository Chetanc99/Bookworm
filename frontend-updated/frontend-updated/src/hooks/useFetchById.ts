import { useFetch } from "./useFetch";

export function useFetchById<T>(
  fetchByIdFn: (id: number | string) => Promise<T>,
  id: number | string | null | undefined
) {
  return useFetch<T>(
    () => {
      if (id === null || id === undefined) {
        return Promise.reject(new Error("No ID provided"));
      }
      return fetchByIdFn(id);
    },
    [id],
    { enabled: id !== null && id !== undefined }
  );
}
