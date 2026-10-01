import React from "react";
import { BrowserRouter } from "react-router-dom";
import { Provider } from "react-redux";
import { Toaster } from "react-hot-toast";
import { store } from "./store";
import { AppRoutes } from "./routes/AppRoutes";

export default function App() {
  return (
    <Provider store={store}>
      <BrowserRouter>
        <Toaster
          position="top-right"
          toastOptions={{
            duration: 4000,
            style: {
              background: "#0f172a",
              color: "#fff",
              borderRadius: "1rem",
              fontSize: "0.875rem",
            },
            success: {
              iconTheme: {
                primary: "#f59e0b",
                secondary: "#0f172a",
              },
            },
          }}
        />
        <AppRoutes />
      </BrowserRouter>
    </Provider>
  );
}
