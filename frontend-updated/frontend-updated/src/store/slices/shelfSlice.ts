import { createSlice, PayloadAction } from "@reduxjs/toolkit";
import { UIShelfItem } from "../../types/library";

interface ShelfState {
  items: UIShelfItem[];
  isLoading: boolean;
}

const initialState: ShelfState = {
  items: [],
  isLoading: false,
};

const shelfSlice = createSlice({
  name: "shelf",
  initialState,
  reducers: {
    setShelfItems: (state, action: PayloadAction<UIShelfItem[]>) => {
      state.items = action.payload;
      state.isLoading = false;
    },
    setShelfLoading: (state, action: PayloadAction<boolean>) => {
      state.isLoading = action.payload;
    },
    clearShelf: (state) => {
      state.items = [];
      state.isLoading = false;
    },
  },
});

export const { setShelfItems, setShelfLoading, clearShelf } = shelfSlice.actions;
export default shelfSlice.reducer;
