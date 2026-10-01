import { useMemo } from "react";
import { useFetch } from "./useFetch";
import { getShelfAPI } from "../services/shelf.service";
import { useAuth } from "./useAuth";
import { UserShelfItem } from "../types/library";

export type OwnershipStatus = "PURCHASED" | "RENTED_ACTIVE" | "BORROWED_ACTIVE" | "NONE";

export interface ProductAvailability {
  status: OwnershipStatus;
  canPurchase: boolean;
  canRent: boolean;
  canBorrow: boolean;
  // Human-readable reason surfaced next to a disabled action, mirroring the
  // exact messages AcquisitionEligibilityServiceImpl throws server-side - see
  // its javadoc for the authoritative business rules this mirrors:
  //   PURCHASED  -> blocks purchase/rent/borrow entirely (permanent ownership)
  //   RENTED (active) -> blocks rent/borrow, purchase still allowed (upgrade)
  //   BORROWED (active) -> blocks rent/borrow, purchase still allowed (upgrade)
  purchaseReason?: string;
  rentReason?: string;
  borrowReason?: string;
}

const NONE_AVAILABILITY: ProductAvailability = {
  status: "NONE",
  canPurchase: true,
  canRent: true,
  canBorrow: true,
};

/**
 * Client-side mirror of AcquisitionEligibilityServiceImpl's rules, built from
 * the user's own /shelf endpoint (the same UserShelf table the backend
 * validates against). This does NOT replace backend validation - the backend
 * is still the source of truth and re-checks on every add-to-cart/checkout/
 * borrow call - it exists purely so the UI can disable an action and explain
 * why *before* the user clicks it, instead of only finding out from a toast
 * error after a rejected request (see AcquisitionEligibilityServiceImpl
 * javadoc for the canonical rule set this mirrors).
 */
export function useOwnershipMap() {
  const { user } = useAuth();
  const { data: shelf, isLoading, refetch } = useFetch(
    () => (user?.userId ? getShelfAPI(user.userId).then((res) => res.data) : Promise.resolve<UserShelfItem[]>([])),
    [user?.userId]
  );

  const byProductId = useMemo(() => {
    const map = new Map<number, ProductAvailability>();
    if (!shelf) return map;
    const now = Date.now();

    for (const item of shelf) {
      const isActive = !item.expiresAt || new Date(item.expiresAt).getTime() > now;
      if (!isActive) continue; // expired RENT/LIBRARY rows impose no restriction

      if (item.source === "PURCHASE") {
        map.set(item.productId, {
          status: "PURCHASED",
          canPurchase: false,
          canRent: false,
          canBorrow: false,
          purchaseReason: "You already own this product",
          rentReason: "You already own this product - no need to rent it",
          borrowReason: "You already own this product - no need to borrow it",
        });
      } else if (item.source === "RENT" && map.get(item.productId)?.status !== "PURCHASED") {
        map.set(item.productId, {
          status: "RENTED_ACTIVE",
          canPurchase: true,
          canRent: false,
          canBorrow: false,
          rentReason: "You already have an active rental for this product",
          borrowReason: "You currently have this product actively rented - it can't also be borrowed",
        });
      } else if (item.source === "LIBRARY" && map.get(item.productId)?.status !== "PURCHASED") {
        map.set(item.productId, {
          status: "BORROWED_ACTIVE",
          canPurchase: true,
          canRent: false,
          canBorrow: false,
          rentReason: "You currently have this product borrowed from the library - return it first to rent it",
          borrowReason: "You already have this product borrowed",
        });
      }
    }
    return map;
  }, [shelf]);

  const getAvailability = (productId: number): ProductAvailability =>
    byProductId.get(productId) || NONE_AVAILABILITY;

  return { getAvailability, isLoading, refetch };
}
