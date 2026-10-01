import { createSlice, PayloadAction } from "@reduxjs/toolkit";

interface UIState {
  ownershipWarningBookName: string | null;
  cartModalMessage: string | null;
  isSidebarOpen: boolean;
}

const initialState: UIState = {
  ownershipWarningBookName: null,
  cartModalMessage: null,
  isSidebarOpen: false,
};

const uiSlice = createSlice({
  name: "ui",
  initialState,
  reducers: {
    setOwnershipWarning: (state, action: PayloadAction<string | null>) => {
      state.ownershipWarningBookName = action.payload;
    },
    setCartModalMessage: (state, action: PayloadAction<string | null>) => {
      state.cartModalMessage = action.payload;
    },
    toggleSidebar: (state) => {
      state.isSidebarOpen = !state.isSidebarOpen;
    },
    closeSidebar: (state) => {
      state.isSidebarOpen = false;
    },
  },
});

export const { setOwnershipWarning, setCartModalMessage, toggleSidebar, closeSidebar } = uiSlice.actions;
export default uiSlice.reducer;
