import { useSelector, useDispatch } from "react-redux";
import { RootState, AppDispatch } from "../store";
import { setCartItems, clearCartState, setLastPurchaseType, setUpiId } from "../store/slices/cartSlice";
import { UICartItem } from "../types/cart";

export function useCart() {
  const dispatch = useDispatch<AppDispatch>();
  const { items, lastPurchaseType, upiId } = useSelector((state: RootState) => state.cart);

  const updateItems = (cartItems: UICartItem[]) => {
    dispatch(setCartItems(cartItems));
  };

  const clear = () => {
    dispatch(clearCartState());
  };

  const setType = (type: "buy" | "rent") => {
    dispatch(setLastPurchaseType(type));
  };

  const setUpi = (upi: string) => {
    dispatch(setUpiId(upi));
  };

  const purchaseTotal = items
    .filter((item) => item.intent !== "RENT")
    .reduce((sum, b) => sum + b.price, 0);

  const rentalTotal = items
    .filter((item) => item.intent === "RENT")
    .reduce((sum, item) => sum + item.price, 0);

  return {
    items,
    itemCount: items.length,
    lastPurchaseType,
    upiId,
    purchaseTotal,
    rentalTotal,
    updateItems,
    clear,
    setType,
    setUpi,
  };
}
