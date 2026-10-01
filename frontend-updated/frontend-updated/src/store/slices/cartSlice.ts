import { createSlice, PayloadAction } from "@reduxjs/toolkit";
import { UICartItem } from "../../types/cart";

interface CartState {
  items: UICartItem[];
  lastPurchaseType: "buy" | "rent";
  upiId: string;
}

const initialState: CartState = {
  items: [],
  lastPurchaseType: "buy",
  upiId: "",
};

const cartSlice = createSlice({
  name: "cart",
  initialState,
  reducers: {
    setCartItems: (state, action: PayloadAction<UICartItem[]>) => {
      state.items = action.payload;
    },
    clearCartState: (state) => {
      state.items = [];
    },
    setLastPurchaseType: (state, action: PayloadAction<"buy" | "rent">) => {
      state.lastPurchaseType = action.payload;
    },
    setUpiId: (state, action: PayloadAction<string>) => {
      state.upiId = action.payload;
    },
  },
});

export const { setCartItems, clearCartState, setLastPurchaseType, setUpiId } = cartSlice.actions;
export default cartSlice.reducer;
